package io.mathdojo.security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.microsoft.azure.functions.HttpMethod;
/**
 * This class is an implementation of the verification of Signed HTTP Messages as detailed by
 * the RFC draft: https://tools.ietf.org/html/draft-cavage-http-signatures-12
 */
public class HTTPRequestSignatureVerifier {

	private static final String REQUEST_TARGET_SIGNATURE_PARAM_KEY = "(request-target)";
	private static final String SIGNATURE_HEADER_KEY = "signature";
	private static final String ALGORITHM_SIGNATURE_PARAM_KEY = "algorithm";
	private static final String KEYID_SIGNATURE_PARAM_KEY = "keyId";
	private static final Map<String, String> SUPPORTED_MAP_OF_ALGORITHMS = Collections
		.singletonMap("rsa-sha256", "SHA256withRSA");
	private final KeyFactory RSA_KEY_FACTORY;

	private final Map<String, PublicKey> mapOfKeyIdAndPubKey;

	/** 
	 * Creates an instance of the HTTPRequestSignatureVerifier class
	 * <p>
	 * Must be initialised with a map of keyIds and associated base-64 encoded DER formatted public
	 * keys. At present this only supports RSA formatted keys
	 * @param mapOfKeyIdAndB64EncDerPubKey must contain at least one keyId string and a base-64 encoded
	 * DER formatted public key
	 * @throws NoSuchAlgorithmException
	 */
	public HTTPRequestSignatureVerifier(Map<String, String> mapOfKeyIdAndB64EncDerPubKey)
			throws NoSuchAlgorithmException {
		RSA_KEY_FACTORY = KeyFactory.getInstance("RSA");
		this.mapOfKeyIdAndPubKey = new HashMap<>();
		mapOfKeyIdAndB64EncDerPubKey.forEach((eachKeyId, eachB64EncDerPubKey) -> {
			byte[] publicKeyBytes = Base64.getDecoder().decode(eachB64EncDerPubKey);
			KeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
			PublicKey publicKey;
			try {
				publicKey = RSA_KEY_FACTORY.generatePublic(keySpec);
				this.mapOfKeyIdAndPubKey.put(eachKeyId, publicKey);
			} catch (InvalidKeySpecException e) {
				String message = "An invalid key was supplied in the creation of the HTTPRequestSignatureVerifier";
				throw new RuntimeException(message, e);
			}
		});

	}

	
	/** 
	 * Verfies the signature found in the <code>signature</code> header of a request
	 * <p>
	 * Currently only supports verification of signed header contents. Will be unable
	 * to perform verification against signatures that include a digest of the HTTP 
	 * request body 
	 * @param suppliedHeaders Key-value map of headers in the request
	 * @param requestPath Path of the request
	 * @param requestMethod Method of the request
	 * @return boolean
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws HTTPRequestSignatureVerificationException
	 * @throws SignatureException
	 * @throws UnsupportedEncodingException
	 */
	public boolean verifySignatureHeader(Map<String, String> suppliedHeaders, String requestPath,
			HttpMethod requestMethod) throws NoSuchAlgorithmException, InvalidKeyException,
			HTTPRequestSignatureVerificationException, SignatureException, UnsupportedEncodingException {
		if (suppliedHeaders.get(SIGNATURE_HEADER_KEY) == null) {
			return false;
		}

		String signatureHeaderValue = suppliedHeaders.get(SIGNATURE_HEADER_KEY);
		Map<String, String> mapOfSignatureParams = createMapOfSignatureParams(signatureHeaderValue);
		String signatureAlgorithm = mapOfSignatureParams.get(ALGORITHM_SIGNATURE_PARAM_KEY);
		String keyIdToUse = mapOfSignatureParams.get(KEYID_SIGNATURE_PARAM_KEY);
		String extractedHTTPRequestSignature = mapOfSignatureParams.get(SIGNATURE_HEADER_KEY);

		String recreatedSigningString = recreateSigningString(suppliedHeaders, requestPath, requestMethod);

		Signature signature = Signature.getInstance(SUPPORTED_MAP_OF_ALGORITHMS.get(
			signatureAlgorithm));

		PublicKey pubKeyToUse = mapOfKeyIdAndPubKey.get(keyIdToUse);
		signature.initVerify(pubKeyToUse);
		signature.update(recreatedSigningString.getBytes("ASCII"));

		boolean verificationStatus = signature.verify(Base64.getDecoder().decode(extractedHTTPRequestSignature));

		return verificationStatus;
	}

	
	/** 
	 * Verfies that the parameters found in the signature header meet certain criteria as described
	 * by: https://tools.ietf.org/html/draft-cavage-http-signatures-12#section-2.1
	 * @param signatureAlgorithm
	 * @param keyIdToUse
	 * @param extractedHTTPRequestSignature
	 * @throws HTTPRequestSignatureVerificationException
	 */
	private void verifySignatureHeaderParams(String signatureAlgorithm, String keyIdToUse, String extractedHTTPRequestSignature)
			throws HTTPRequestSignatureVerificationException {
		// TODO #3: Allow Algorithms to be null but validate against algorithm of keyId's key otherwise
		if (signatureAlgorithm == null) {
			throw new HTTPRequestSignatureVerificationException(
				"no algorithm was included in the signature header");
		} else if(!SUPPORTED_MAP_OF_ALGORITHMS.containsKey(signatureAlgorithm)) {
			throw new HTTPRequestSignatureVerificationException(
				"algorithm in signature header is not supported by the verifier");
		}

		if (keyIdToUse == null) {
			throw new HTTPRequestSignatureVerificationException(
				"no keyId field found in value of signature header");
		} else if(mapOfKeyIdAndPubKey.get(keyIdToUse) == null) {
			throw new HTTPRequestSignatureVerificationException(
				"keyId in signature header is unknown by the verifier");
		}

		if (extractedHTTPRequestSignature == null) {
			throw new HTTPRequestSignatureVerificationException(
					"no signature field found in value of signature header");
		}
	}

	
	/** 
	 * Creates a map of parameters found in the signature header. 
	 * <p>
	 * Deconstruction follows spec:
	 * https://tools.ietf.org/html/draft-cavage-http-signatures-12#section-2.3
	 * @param signatureHeaderValue
	 * @return Map<String, String>
	 * @throws HTTPRequestSignatureVerificationException
	 */
	public Map<String, String> createMapOfSignatureParams(String signatureHeaderValue)
			throws HTTPRequestSignatureVerificationException {
		String headerValueWithoutSignaturePrefix = signatureHeaderValue.replaceAll("Signature ", "");
		String[] listOfSignatureValues = headerValueWithoutSignaturePrefix.split(",");
		Map<String, String> signatureValueContents = new HashMap<>();

		try {
			Arrays.stream(listOfSignatureValues).forEach(each -> {
				String[] arrayOfSplitContents = each.split("=");
				String signatureFieldName = arrayOfSplitContents[0];
				String signatureValueWithQuotes = arrayOfSplitContents[1].replace("\"", "");
				signatureValueContents.put(signatureFieldName, signatureValueWithQuotes);
			});
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new HTTPRequestSignatureVerificationException("no parameters found in value of signature header");
		}
		String signatureAlgorithm = signatureValueContents.get(ALGORITHM_SIGNATURE_PARAM_KEY);
		String keyIdToUse = signatureValueContents.get(KEYID_SIGNATURE_PARAM_KEY);
		String extractedHTTPRequestSignature = signatureValueContents.get(SIGNATURE_HEADER_KEY);
		verifySignatureHeaderParams(signatureAlgorithm, keyIdToUse, extractedHTTPRequestSignature);
		return signatureValueContents;
	}

	
	/** 
	 * Reconstructs the signing string from the requests headers, method and path
	 * <p>
	 * Construction follows spec:
	 * https://tools.ietf.org/html/draft-cavage-http-signatures-12#section-2.3
	 * @param headers
	 * @param requestPath
	 * @param requestMethod
	 * @return String
	 * @throws HTTPRequestSignatureVerificationException
	 */
	public String recreateSigningString(Map<String, String> headers, String requestPath, HttpMethod requestMethod)
			throws HTTPRequestSignatureVerificationException {
		String signatureHeaderValue = headers.get(SIGNATURE_HEADER_KEY);
		Map<String, String> signatureValueContents = createMapOfSignatureParams(signatureHeaderValue);

		String[] headerKeysForSigningString = signatureValueContents.get("headers").split(" ");

		List<String> listOfSigningStringContents = Arrays.stream(headerKeysForSigningString).map(eachHeaderKey -> {
			if (REQUEST_TARGET_SIGNATURE_PARAM_KEY.equals(eachHeaderKey)) {
				return (eachHeaderKey + ": " + requestMethod.toString().toLowerCase() + " " + requestPath);
			}
			return eachHeaderKey + ": " + headers.get(eachHeaderKey);
		}).collect(Collectors.toList());
		String recreatedSigningString = String.join("\n", listOfSigningStringContents);

		return recreatedSigningString;
	}

}
