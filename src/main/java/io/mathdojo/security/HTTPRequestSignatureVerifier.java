package io.mathdojo.security;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HTTPRequestSignatureVerifier {

    public HTTPRequestSignatureVerifier() {
        
    }

	public boolean verifySignatureHeader(Map<String, String> suppliedHeaders) {
		if(suppliedHeaders.get("signature") == null) {
			return false;
		}
		return true;
	}

	public String extractSignatureStringFromSignatureHeader(String signatureHeaderValue) throws HTTPRequestSignatureVerificationException {
		Map<String, String> signatureValueContents = createMapOfSignatureParams(signatureHeaderValue);

		String signatureString = signatureValueContents.get("signature");
		if(signatureString == null) {
			throw new HTTPRequestSignatureVerificationException("no signature field found in value of signature header");
		}
		return signatureString;
	}

	private Map<String, String> createMapOfSignatureParams(String signatureHeaderValue) throws HTTPRequestSignatureVerificationException {
		String headerValueWithoutSignaturePrefix = signatureHeaderValue.replaceAll("Signature ", "");
		String[] listOfSignatureValues = headerValueWithoutSignaturePrefix.split(",");
		Map<String, String> signatureValueContents =  new HashMap<>();

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

}
