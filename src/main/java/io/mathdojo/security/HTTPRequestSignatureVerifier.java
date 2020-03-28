package io.mathdojo.security;

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

}
