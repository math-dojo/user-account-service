package io.mathdojo.useraccountservice.security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
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
	 * <p>
	 * When {@code signatureAlgorithm} is {@code null} (i.e. the header omits the {@code algorithm}
	 * param), the algorithm is inferred from the registered public key for {@code keyIdToUse}
	 * (resolves TODO #3).
	 *
	 * @return the resolved algorithm string — either the supplied value or the inferred one
	 * @throws HTTPRequestSignatureVerificationException
	 */
	private String verifySignatureHeaderParams(String signatureAlgorithm, String keyIdToUse,
			String extractedHTTPRequestSignature) throws HTTPRequestSignatureVerificationException {

		// Validate keyId first — we need the key to infer the algorithm when it is absent
		if (keyIdToUse == null) {
			throw new HTTPRequestSignatureVerificationException(
				"no keyId field found in value of signature header");
		}
		PublicKey keyForId = mapOfKeyIdAndPubKey.get(keyIdToUse);
		if (keyForId == null) {
			throw new HTTPRequestSignatureVerificationException(
				"keyId in signature header is unknown by the verifier");
		}

		if (signatureAlgorithm == null) {
			// Infer from key type, e.g. "RSA" → "rsa-sha256" / "SHA256withRSA"
			String keyAlgorithmName = keyForId.getAlgorithm();
			signatureAlgorithm = SUPPORTED_MAP_OF_ALGORITHMS.entrySet().stream()
				.filter(e -> e.getValue().toUpperCase().contains(keyAlgorithmName.toUpperCase()))
				.map(Map.Entry::getKey)
				.findFirst()
				.orElseThrow(() -> new HTTPRequestSignatureVerificationException(
					"no supported algorithm found for key type: " + keyAlgorithmName));
		} else if (!SUPPORTED_MAP_OF_ALGORITHMS.containsKey(signatureAlgorithm)) {
			throw new HTTPRequestSignatureVerificationException(
				"algorithm in signature header is not supported by the verifier");
		}

		if (extractedHTTPRequestSignature == null) {
			throw new HTTPRequestSignatureVerificationException(
				"no signature field found in value of signature header");
		}

		return signatureAlgorithm;
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
				// Split on the FIRST '=' only so that base64 padding (==) in values is preserved
				String[] arrayOfSplitContents = each.split("=", 2);
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

		// resolvedAlgorithm may be inferred when null; write it back so callers always find it
		String resolvedAlgorithm = verifySignatureHeaderParams(
			signatureAlgorithm, keyIdToUse, extractedHTTPRequestSignature);
		signatureValueContents.put(ALGORITHM_SIGNATURE_PARAM_KEY, resolvedAlgorithm);

		return signatureValueContents;
	}

	
	/** 
	 * Reconstructs the signing string from the requests headers, method and path	 * <p>
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

		String[] headerKeysForSigningString = signatureValueContents.get("headers") != null
				? signatureValueContents.get("headers").split(" ")
				: new String[0];

		if (headerKeysForSigningString.length == 0) {
			throw new HTTPRequestSignatureVerificationException(
				"no headers parameter found in signature header; cannot reconstruct signing string");
		}

		List<String> listOfSigningStringContents = Arrays.stream(headerKeysForSigningString).map(eachHeaderKey -> {
			if (REQUEST_TARGET_SIGNATURE_PARAM_KEY.equals(eachHeaderKey)) {
				return (eachHeaderKey + ": " + requestMethod.toString().toLowerCase() + " " + requestPath);
			}
			return eachHeaderKey + ": " + headers.get(eachHeaderKey);
		}).collect(Collectors.toList());
		String recreatedSigningString = String.join("\n", listOfSigningStringContents);

		return recreatedSigningString;
	}

	/**
	 * Verifies the {@code Digest} header against the supplied request body bytes.
	 * <p>
	 * If no {@code Digest} header is present the method returns without error (the header is
	 * optional per RFC 3230). Currently supports {@code SHA-256} only.
	 *
	 * @param headers          key-value map of request headers (lowercase keys expected)
	 * @param requestBodyBytes raw bytes of the request body
	 * @throws HTTPRequestSignatureVerificationException if the digest does not match or is malformed
	 * @throws NoSuchAlgorithmException                  if SHA-256 is unavailable in this JVM
	 */
	public void verifyDigestHeader(Map<String, String> headers, byte[] requestBodyBytes)
			throws HTTPRequestSignatureVerificationException, NoSuchAlgorithmException {
		String digestHeader = headers.get("digest");
		if (digestHeader == null) {
			return; // Digest header is optional; nothing to verify
		}

		int separatorIndex = digestHeader.indexOf('=');
		if (separatorIndex < 0) {
			throw new HTTPRequestSignatureVerificationException(
				"malformed Digest header: missing '=' separator");
		}

		String algorithm = digestHeader.substring(0, separatorIndex).trim();
		String expectedDigest = digestHeader.substring(separatorIndex + 1).trim();

		if (!"SHA-256".equalsIgnoreCase(algorithm)) {
			throw new HTTPRequestSignatureVerificationException(
				"unsupported digest algorithm: " + algorithm + " (only SHA-256 is supported)");
		}

		MessageDigest md = MessageDigest.getInstance("SHA-256");
		String actualDigest = Base64.getEncoder().encodeToString(md.digest(requestBodyBytes));

		if (!actualDigest.equals(expectedDigest)) {
			throw new HTTPRequestSignatureVerificationException(
				"request body digest does not match Digest header");
		}
	}

}
