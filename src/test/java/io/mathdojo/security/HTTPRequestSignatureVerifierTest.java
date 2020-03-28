package io.mathdojo.security;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    
    @Test
    public void testReturnsTrueIfSignatureHeader() {
        Map<String, String> testHeaders = new HashMap<String, String>();
        testHeaders.put("signature", "some-sig");

        HTTPRequestSignatureVerifier verifier = new HTTPRequestSignatureVerifier();
        assertTrue(verifier.verifySignatureHeader(testHeaders));
    }
    
}