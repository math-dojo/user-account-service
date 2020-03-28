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

	public String extractSignatureStringFromSignatureHeader(String signatureHeaderValue) {
		String headerValueWithoutSignaturePrefix = signatureHeaderValue.replaceAll("Signature ", "");
		String[] listOfSignatureValues = headerValueWithoutSignaturePrefix.split(",");
		Map<String, String> signatureValueContents =  new HashMap<>();

		Arrays.stream(listOfSignatureValues).forEach(each -> {
			String[] arrayOfSplitContents = each.split("=");
			String signatureFieldName = arrayOfSplitContents[0];
			String signatureValueWithQuotes = arrayOfSplitContents[1].replace("\"", "");
			signatureValueContents.put(signatureFieldName, signatureValueWithQuotes);
		});

		return signatureValueContents.get("signature");
	}

}
