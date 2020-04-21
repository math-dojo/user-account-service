package io.mathdojo.useraccountservice.security;

import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;

public final class HTTPRequestSignatureVerifierSingleton {

    private static HTTPRequestSignatureVerifier INSTANCE;

    public static HTTPRequestSignatureVerifier getInstance() throws NoSuchAlgorithmException {
        if (INSTANCE == null) {
            System.out.println("Initialising Request Verification Class");
            INSTANCE = createVerifier(System.getenv("MATH_DOJO_HTTP_REQUEST_SIGNATURE_EXPECTED_KEYID"),
            System.getenv("MATH_DOJO_HTTP_REQUEST_SIGNATURE_B64_DER_PUBLIC_KEY"));
        }
         
        return INSTANCE;
    }
 
    // getters and setters
    private static HTTPRequestSignatureVerifier createVerifier(String expectedKeyId, String b64EncDerOfPublicKey)
            throws NoSuchAlgorithmException {
        Map<String, String> mapOfKeyIdAndB64EncDerPubKey = Collections.singletonMap(expectedKeyId,
                b64EncDerOfPublicKey);
        return new HTTPRequestSignatureVerifier(mapOfKeyIdAndB64EncDerPubKey);
    }
}