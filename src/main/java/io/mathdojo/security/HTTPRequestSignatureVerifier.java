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

public class HTTPRequestSignatureVerifier {

	private static final String REQUEST_TARGET_SIGNATURE_PARAM_KEY = "(request-target)";
	private static final String SIGNATURE_HEADER_KEY = "signature";
	private static final String ALGORITHM_SIGNATURE_PARAM_KEY = "algorithm";
	private static final Map<String, String> SUPPORTED_MAP_OF_ALGORITHMS = Collections
		.singletonMap("rsa-sha256", "SHA256withRSA");
	private final PublicKey PUBLIC_KEY;

	public HTTPRequestSignatureVerifier(String b64RepresentationOfPublicKeyDer)
			throws InvalidKeySpecException, NoSuchAlgorithmException {
		byte[] publicKeyBytes = Base64.getDecoder().decode(b64RepresentationOfPublicKeyDer);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		KeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
		PUBLIC_KEY = kf.generatePublic(keySpec);

	}

	public boolean verifySignatureHeader(Map<String, String> suppliedHeaders, String requestPath,
			HttpMethod requestMethod) throws NoSuchAlgorithmException, InvalidKeyException,
			HTTPRequestSignatureVerificationException, SignatureException, UnsupportedEncodingException {
		if (suppliedHeaders.get(SIGNATURE_HEADER_KEY) == null) {
			return false;
		}

		String signatureHeaderValue = suppliedHeaders.get(SIGNATURE_HEADER_KEY);
		String signatureAlgorithm = createMapOfSignatureParams(signatureHeaderValue).get(ALGORITHM_SIGNATURE_PARAM_KEY);

		if (signatureAlgorithm == null) {
			throw new HTTPRequestSignatureVerificationException("no algorithm was included in the signature header");
		} else if(!SUPPORTED_MAP_OF_ALGORITHMS.containsKey(signatureAlgorithm)) {
			throw new HTTPRequestSignatureVerificationException("algorithm in signature header is not supported by the verifier");
		}

		String extractedHTTPRequestSignature = extractSignatureStringFromSignatureHeader(signatureHeaderValue);

		String recreatedSigningString = recreateSigningString(suppliedHeaders, requestPath, requestMethod);

		Signature signature = Signature.getInstance(SUPPORTED_MAP_OF_ALGORITHMS.get(
			signatureAlgorithm));
		signature.initVerify(PUBLIC_KEY);
		signature.update(recreatedSigningString.getBytes("ASCII"));

		boolean verificationStatus = signature.verify(Base64.getDecoder().decode(extractedHTTPRequestSignature));

		return verificationStatus;
	}

	public String extractSignatureStringFromSignatureHeader(String signatureHeaderValue)
			throws HTTPRequestSignatureVerificationException {
		Map<String, String> signatureValueContents = createMapOfSignatureParams(signatureHeaderValue);

		String signatureString = signatureValueContents.get(SIGNATURE_HEADER_KEY);
		if (signatureString == null) {
			throw new HTTPRequestSignatureVerificationException(
					"no signature field found in value of signature header");
		}
		return signatureString;
	}

	private Map<String, String> createMapOfSignatureParams(String signatureHeaderValue)
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
		return signatureValueContents;
	}

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
