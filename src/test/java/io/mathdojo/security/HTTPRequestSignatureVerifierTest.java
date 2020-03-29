package io.mathdojo.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.azure.functions.HttpMethod;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class HTTPRequestSignatureVerifierTest {

    private static KeyPair KEYPAIR1_KEY_PAIR;
    private static KeyPair KEYPAIR2_KEY_PAIR;
    private static String b64RepresentationOfKey1;
    private static HTTPRequestSignatureVerifier verifier;

    @BeforeClass
    public static void setUp() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        KEYPAIR1_KEY_PAIR = keyGen.generateKeyPair();
        KEYPAIR2_KEY_PAIR = keyGen.generateKeyPair();
        b64RepresentationOfKey1 = Base64.getEncoder().encodeToString(
            KEYPAIR1_KEY_PAIR.getPublic().getEncoded());
        verifier = new HTTPRequestSignatureVerifier(b64RepresentationOfKey1);
    }

    @Test
    public void testReturnsFalseIfNoSignatureHeader() {
        Map<String, String> testHeaders = new HashMap<String, String>();
        testHeaders.put("content-type", "application/json");

        assertFalse(verifier.verifySignatureHeader(testHeaders));
    }

    @Test
    public void testReturnsTrueIfSignatureHeader() {
        Map<String, String> testHeaders = new HashMap<String, String>();
        testHeaders.put("signature", "some-sig");

        assertTrue(verifier.verifySignatureHeader(testHeaders));
    }

    @Test
    public void testExtractsSignatureStringFromSignatureHeader() throws HTTPRequestSignatureVerificationException {
        List<String> testHeaderList = Arrays.asList("header1", "header2");
        String testSignatureString = "somethingSignedAndB64Encoded";
        String testSignatureHeaderValue = createSignatureString("someKeyId", "someAlg", testHeaderList,
                testSignatureString);


        String extractedSignatureString = verifier.extractSignatureStringFromSignatureHeader(testSignatureHeaderValue);
        assertEquals(testSignatureString, extractedSignatureString);
    }

    @Test(expected = HTTPRequestSignatureVerificationException.class)
    public void exceptionThrownIfNoSignatureParamInHeaderValue() throws HTTPRequestSignatureVerificationException {
        String testSignatureString = "";
        String testSignatureHeaderValue = "badformat=2";

        String extractedSignatureString = verifier.extractSignatureStringFromSignatureHeader(testSignatureHeaderValue);
        assertEquals(testSignatureString, extractedSignatureString);
    }

    @Test(expected = HTTPRequestSignatureVerificationException.class)
    public void exceptionThrownIfHeaderValueHasNoParams() throws HTTPRequestSignatureVerificationException {
        String testSignatureString = "";
        String testSignatureHeaderValue = "iAmAHeaderWithoutParams";

        String extractedSignatureString = verifier.extractSignatureStringFromSignatureHeader(testSignatureHeaderValue);
        assertEquals(testSignatureString, extractedSignatureString);
    }

    @Test
    public void testSigningStringRecreatedCorrectlyFromSignatureInfoIfOneParam() throws 
            HTTPRequestSignatureVerificationException {

        List<String> testHeaderList = Arrays.asList("authorization");
        String testSignatureString = "somethingSignedAndB64Encoded";
        String testSignatureHeaderValue = createSignatureString("someKeyId", "someAlg", testHeaderList,
                testSignatureString);

        Map<String, String> testHeaders = new HashMap<String, String>();
        testHeaders.put("content-type", "application/json");
        testHeaders.put("authorization", "garbages");
        ;
        testHeaders.put("signature", testSignatureHeaderValue);

        String requestTarget = "/some/path";
        HttpMethod method = HttpMethod.GET;

        String recreatedSigningString = verifier.recreateSigningString(testHeaders, requestTarget, method);
        String expectedRecreatedSigningString = "authorization: garbages";
        assertEquals(expectedRecreatedSigningString, recreatedSigningString);
    }

    @Test
    public void testSigningStringRecreatedCorrectlyFromSignatureInfoIfMultipleParams() throws 
            HTTPRequestSignatureVerificationException {

        List<String> testHeaderList = Arrays.asList("content-type", "date");
        String testSignatureString = "somethingSignedAndB64Encoded";
        String testSignatureHeaderValue = createSignatureString("someKeyId", "someAlg", testHeaderList,
                testSignatureString);

        Map<String, String> testHeaders = new HashMap<String, String>();
        testHeaders.put("content-type", "application/json");
        String dateString = "Fri, 27 Mar 2020 07:49:21 UTC";
        testHeaders.put("date", dateString);
        testHeaders.put("signature", testSignatureHeaderValue);

        String requestTarget = "/some/path";
        HttpMethod method = HttpMethod.GET;

        String recreatedSigningString = verifier.recreateSigningString(testHeaders, requestTarget, method);
        String expectedRecreatedSigningString = ("content-type: application/json"
            +"\n"+"date: "+dateString);
        assertEquals(expectedRecreatedSigningString, recreatedSigningString);
    }

    @Test
    public void testSigningStringRecreatedCorrectlyIfParamListContainsRequestTarget() throws 
            HTTPRequestSignatureVerificationException {

        List<String> testHeaderList = Arrays.asList("(request-target)", "date");
        String testSignatureString = "somethingSignedAndB64Encoded";
        String testSignatureHeaderValue = createSignatureString("someKeyId", "someAlg", testHeaderList,
                testSignatureString);

        Map<String, String> testHeaders = new HashMap<String, String>();
        testHeaders.put("content-type", "application/json");
        String dateString = "Fri, 27 Mar 2020 07:49:21 UTC";
        testHeaders.put("date", dateString);
        testHeaders.put("signature", testSignatureHeaderValue);

        String requestTarget = "/some/path";
        HttpMethod method = HttpMethod.GET;

        String recreatedSigningString = verifier.recreateSigningString(testHeaders, requestTarget, method);
        String expectedRecreatedSigningString = ("(request-target): "
        + method.toString().toLowerCase() + " " + requestTarget +"\n" 
        + "date: " + dateString);
        assertEquals(expectedRecreatedSigningString, recreatedSigningString);
    }

    @Ignore
    public void testSignatureCanBeVerifiedCorrectly() {

    }

    private String createSignatureString(String keyId, String algorithm, List<String> headerKeysUsedInSignature,
            String signatureString) {
        String spaceSeparatedHeaderNames = String.join(" ", headerKeysUsedInSignature);
        return String.format("Signature keyId=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"", keyId,
                algorithm, spaceSeparatedHeaderNames, signatureString);
    }
}
