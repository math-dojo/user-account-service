package io.mathdojo.security;

import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class HTTPRequestSignatureVerifierTest {
    
    @Test
    public void testReturnsFalseIfNoSignatureHeader() {
        Map<String, String> testHeaders = new HashMap<String, String>();
        testHeaders.put("content-type", "application/json");

        HTTPRequestSignatureVerifier verifier = new HTTPRequestSignatureVerifier();
        assertFalse(verifier.verifySignatureHeader(testHeaders));
    }    

    
}