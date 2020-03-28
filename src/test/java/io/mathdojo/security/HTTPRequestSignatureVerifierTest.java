package io.mathdojo.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

    @Test
    public void testExtractsSignatureStringFromSignatureHeader() throws HTTPRequestSignatureVerificationException {
        List<String> testHeaderList = Arrays.asList("header1", "header2");
        String testSignatureString = "somethingSignedAndB64Encoded";
        String testSignatureHeaderValue = createSignatureString("someKeyId", "someAlg", testHeaderList,
        testSignatureString);

        HTTPRequestSignatureVerifier verifier = new HTTPRequestSignatureVerifier();

        String extractedSignatureString = verifier.extractSignatureStringFromSignatureHeader(testSignatureHeaderValue);
        assertEquals(testSignatureString, extractedSignatureString);
    }

    @Test(expected = HTTPRequestSignatureVerificationException.class)
    public void exceptionThrownIfNoSignatureParamInHeaderValue() throws HTTPRequestSignatureVerificationException {
        String testSignatureString = "";
        String testSignatureHeaderValue = "badformat=2";

        HTTPRequestSignatureVerifier verifier = new HTTPRequestSignatureVerifier();

        String extractedSignatureString = verifier.extractSignatureStringFromSignatureHeader(testSignatureHeaderValue);
        assertEquals(testSignatureString, extractedSignatureString);
    }

    @Test(expected = HTTPRequestSignatureVerificationException.class)
    public void exceptionThrownIfHeaderValueHasNoParams() throws HTTPRequestSignatureVerificationException {
        String testSignatureString = "";
        String testSignatureHeaderValue = "iAmAHeaderWithoutParams";

        HTTPRequestSignatureVerifier verifier = new HTTPRequestSignatureVerifier();

        String extractedSignatureString = verifier.extractSignatureStringFromSignatureHeader(testSignatureHeaderValue);
        assertEquals(testSignatureString, extractedSignatureString);
    }

    private String createSignatureString(String keyId, String algorithm, List<String> headersInSignature,
            String signatureString) {
        String spaceSeparatedHeaderNames = headersInSignature.stream().reduce("",
                (accumulatedString, currentValue) -> accumulatedString + " " + currentValue);
        return String.format("Signature keyId=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"", keyId,
                algorithm, spaceSeparatedHeaderNames, signatureString);
    }
}