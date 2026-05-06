package io.mathdojo.useraccountservice.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.azure.functions.HttpMethod;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class HTTPRequestSignatureVerifierTest {

    private static final String RSA_SHA256 = "rsa-sha256";

    private static final String KNOWN_KEY_ID = "someKeyId";

    private static KeyPair KEYPAIR1_KEY_PAIR;
    private static KeyPair KEYPAIR2_KEY_PAIR;
    private static String B64_REPRESENTATION_OF_KEY_1;
    private static HTTPRequestSignatureVerifier verifier;

    @BeforeAll
    public static void setUp() throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        KEYPAIR1_KEY_PAIR = keyGen.generateKeyPair();
        KEYPAIR2_KEY_PAIR = keyGen.generateKeyPair();
        B64_REPRESENTATION_OF_KEY_1 = Base64.getEncoder()
            .encodeToString(KEYPAIR1_KEY_PAIR.getPublic().getEncoded());
        Map<String, String> mapOfKeyIdAndKey = Collections.singletonMap(KNOWN_KEY_ID, B64_REPRESENTATION_OF_KEY_1);
        verifier = new HTTPRequestSignatureVerifier(mapOfKeyIdAndKey);
    }

    @Test
    public void testReturnsFalseIfNoSignatureHeader() throws InvalidKeyException, NoSuchAlgorithmException,
            SignatureException, UnsupportedEncodingException, HTTPRequestSignatureVerificationException {
        Map<String, String> testHeaders = new HashMap<String, String>();
        testHeaders.put("content-type", "application/json");

        String requestTarget = "/some/path";
        HttpMethod method = HttpMethod.GET;
        assertFalse(verifier.verifySignatureHeader(testHeaders, requestTarget, method));
    }

    @Test
    public void testExtractsSignatureStringFromSignatureHeader() throws HTTPRequestSignatureVerificationException {
        List<String> testHeaderList = Arrays.asList("header1", "header2");
        String testSignatureString = "somethingSignedAndB64Encoded";
        String testSignatureHeaderValue = createSignatureString(KNOWN_KEY_ID, RSA_SHA256, testHeaderList,
                testSignatureString);

        String extractedSignatureString = verifier.createMapOfSignatureParams(testSignatureHeaderValue)
            .get("signature");
        assertEquals(testSignatureString, extractedSignatureString);
    }

    @Test
    public void exceptionThrownIfNoSignatureParamInSignatureHeaderValue() {
        String testSignatureHeaderValue = String.format(
            "keyId=\"%s\",algorithm=\"%s\"", KNOWN_KEY_ID, RSA_SHA256);

        Exception exception = assertThrows(HTTPRequestSignatureVerificationException.class,() -> {
            verifier.createMapOfSignatureParams(testSignatureHeaderValue);
        });

        assertEquals("no signature field found in value of signature header", 
            exception.getMessage());

    }

    @Test
    public void exceptionThrownIfNoKeyIdParamInSignatureHeaderValue() {
        String testSignatureHeaderValue = String.format(
            "signature=\"%s\",algorithm=\"%s\"", "some_sig", RSA_SHA256);

        Exception exception = assertThrows(HTTPRequestSignatureVerificationException.class,() -> {
            verifier.createMapOfSignatureParams(testSignatureHeaderValue);
        });

        assertEquals("no keyId field found in value of signature header", 
            exception.getMessage());

    }

    @Test
    public void exceptionThrownIfSignatureHeaderValueHasNoParams() {
        String testSignatureHeaderValue = "iAmAHeaderWithoutParams";

        Exception exception = assertThrows(HTTPRequestSignatureVerificationException.class,() -> {
            verifier.createMapOfSignatureParams(testSignatureHeaderValue);
        });
        
        assertEquals("no parameters found in value of signature header",  
            exception.getMessage());
    }

    @Test
    public void exceptionThrownIfAlgorithmInSignatureIsUnsupported() {
        String testSignatureString = "some-header";
        List<String> testHeaderList = Arrays.asList("authorization");

        String testSignatureHeaderValue = createSignatureString(KNOWN_KEY_ID, "some-unsupportedAlg",
            testHeaderList, testSignatureString);
        Map<String, String> headers = Collections.singletonMap("signature", testSignatureHeaderValue);

        Exception exception = assertThrows(HTTPRequestSignatureVerificationException.class,() -> {
            verifier.verifySignatureHeader(headers, "/somepath", HttpMethod.GET);
        });

        assertEquals("algorithm in signature header is not supported by the verifier", 
            exception.getMessage());
    }

    @Test
    public void exceptionThrownIfKeyIdInSignatureIsUnknown() {
        String testSignatureString = "some-header";
        List<String> testHeaderList = Arrays.asList("authorization");

        String testSignatureHeaderValue = createSignatureString("someUnknownKeyId", RSA_SHA256,
            testHeaderList, testSignatureString);

        Exception exception = assertThrows(HTTPRequestSignatureVerificationException.class,() -> {
            verifier.createMapOfSignatureParams(testSignatureHeaderValue);
        });

        assertEquals("keyId in signature header is unknown by the verifier", 
            exception.getMessage());
    }

    @Test
    public void testSigningStringRecreatedCorrectlyFromSignatureInfoIfOneParam()
            throws HTTPRequestSignatureVerificationException {

        List<String> testHeaderList = Arrays.asList("authorization");
        String testSignatureString = "somethingSignedAndB64Encoded";
        String testSignatureHeaderValue = createSignatureString(KNOWN_KEY_ID, RSA_SHA256, testHeaderList,
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
    public void testSigningStringRecreatedCorrectlyFromSignatureInfoIfMultipleParams()
            throws HTTPRequestSignatureVerificationException {

        List<String> testHeaderList = Arrays.asList("content-type", "date");
        String testSignatureString = "somethingSignedAndB64Encoded";
        String testSignatureHeaderValue = createSignatureString(KNOWN_KEY_ID, RSA_SHA256, testHeaderList,
                testSignatureString);

        Map<String, String> testHeaders = new HashMap<String, String>();
        testHeaders.put("content-type", "application/json");
        String dateString = "Fri, 27 Mar 2020 07:49:21 UTC";
        testHeaders.put("date", dateString);
        testHeaders.put("signature", testSignatureHeaderValue);

        String requestTarget = "/some/path";
        HttpMethod method = HttpMethod.GET;

        String recreatedSigningString = verifier.recreateSigningString(testHeaders, requestTarget, method);
        String expectedRecreatedSigningString = ("content-type: application/json" + "\n" + "date: " + dateString);
        assertEquals(expectedRecreatedSigningString, recreatedSigningString);
    }

    @Test
    public void testSigningStringRecreatedCorrectlyIfParamListContainsRequestTarget()
            throws HTTPRequestSignatureVerificationException {

        List<String> testHeaderList = Arrays.asList("(request-target)", "date");
        String testSignatureString = "somethingSignedAndB64Encoded";
        String testSignatureHeaderValue = createSignatureString(KNOWN_KEY_ID, RSA_SHA256, testHeaderList,
                testSignatureString);

        Map<String, String> testHeaders = new HashMap<String, String>();
        testHeaders.put("content-type", "application/json");
        String dateString = "Fri, 27 Mar 2020 07:49:21 UTC";
        testHeaders.put("date", dateString);
        testHeaders.put("signature", testSignatureHeaderValue);

        String requestTarget = "/some/path";
        HttpMethod method = HttpMethod.GET;

        String recreatedSigningString = verifier.recreateSigningString(testHeaders, requestTarget, method);
        String expectedRecreatedSigningString = ("(request-target): " + method.toString().toLowerCase() + " "
                + requestTarget + "\n" + "date: " + dateString);
        assertEquals(expectedRecreatedSigningString, recreatedSigningString);
    }

    @Test
    public void testSignatureCanBeVerifiedCorrectlyWithMatchingPublicKey()
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException,
            HTTPRequestSignatureVerificationException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(KEYPAIR1_KEY_PAIR.getPrivate());

        String requestTarget = "/some/path";
        HttpMethod method = HttpMethod.GET;
        String dateString = "Fri, 27 Mar 2020 07:49:21 UTC";

        List<String> testHeaderList = Arrays.asList("(request-target)", "date");
        String testSigningString = ("(request-target): " + method.toString().toLowerCase() + " "
                + requestTarget + "\n" + "date: " + dateString);
        
        signature.update(testSigningString.getBytes());
        byte[] actualHttpRequestSignatureBytes = signature.sign();
        String actualHttpRequestSignature = Base64.getEncoder().encodeToString(actualHttpRequestSignatureBytes);

        String testSignatureHeaderValue = createSignatureString(KNOWN_KEY_ID, RSA_SHA256, testHeaderList,
            actualHttpRequestSignature);

        Map<String, String> testHeaders = new HashMap<String, String>();
        testHeaders.put("content-type", "application/json");
        testHeaders.put("date", dateString);
        testHeaders.put("signature", testSignatureHeaderValue);

        assertTrue(verifier.verifySignatureHeader(testHeaders, requestTarget, method));
    }

    @Test
    public void testSignatureCanBeVerifiedCorrectlyWithMatchingPublicKeyFromExternalSrc()
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException,
            HTTPRequestSignatureVerificationException, InvalidKeySpecException {

        String requestTarget = "/api/hello";
        HttpMethod method = HttpMethod.POST;
        String dateString = "Sun, 29 Mar 2020 00:58:21 UTC";

        List<String> testHeaderList = Arrays.asList("(request-target)", "date");
        
        String actualHttpRequestSignature = "c+U/UaZioownKIj3XwotU0YIh399fpkqpuIn4V+fmNiQtMBbK/d+beP9GM7VjUNsC+idM9wnljj4FsrkmKvDmvte+YCH5F7wJzZwcMeBFNA0eWtXIE6glJ4CzfLWt4t6kVJU/j9bMJG89wqNT29f6+Cq0QFtUtPtmMkGGxGRMFfo9NiEWFIDp1bwlCS+M5fQApmB1gqGkEFOFh2zmy5XY0ah+2F99KBmBB88rNf1gf+JLEbMXxIhvBkM0bNpwZjd88J5/ihryeNkxRq9gGMFNzyNLhWzbfKguHcdwlK8NRAglVEysmm7ntSTH5KCMIjZ4ue71fJJTIzew2R5ainD9g==";

        String testSignatureHeaderValue = createSignatureString("someOtherKnownKeyId", RSA_SHA256, testHeaderList,
            actualHttpRequestSignature);

        String base64RepOfCustomKeyDer = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvwNtsvihhKJW/LBFLsXOBoQ60utFaa0dlUSBGRsaAl1nuirAzm7HmBfDqmb53/MR+S2MCovH9wraE0Fbj3Ukb6iK5bdY4hfs+LP6XaB+mdmlgddbR8GRFsfSeoY5U/M01jNPwnhcq5J+l2wMnoMqdQ7XM6Yc4pqp3kLc35Hr5zHjFQ/L1w7G50ug/kJWVezFw+oOizEZOES+BFj1d+A0ueJCEZ/Xc/iNO1881vfCAo3wjDDUp+EJB1k9E29rcUmKxEI/+nC5+uPPzwK6qv6+5DXbObZdBP5vocD7xMdZWGde6/pEaYc8Kn3Rjap1PWaR1e0iJI1AWIoxqNsXVxthsQIDAQAB";
        Map<String, String> mapOfKeyIdAndKey = Collections.singletonMap("someOtherKnownKeyId", base64RepOfCustomKeyDer);

        HTTPRequestSignatureVerifier customVerifier = new HTTPRequestSignatureVerifier(mapOfKeyIdAndKey);

        Map<String, String> testHeaders = new HashMap<String, String>();
        testHeaders.put("content-type", "application/json");
        testHeaders.put("date", dateString);
        testHeaders.put("signature", testSignatureHeaderValue);

        assertTrue(customVerifier.verifySignatureHeader(testHeaders, requestTarget, method));
    }

    @Test
    public void testSignatureFailsVerificationWithIncorrectPublicKey()
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException,
            HTTPRequestSignatureVerificationException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(KEYPAIR2_KEY_PAIR.getPrivate());

        String requestTarget = "/some/path";
        HttpMethod method = HttpMethod.GET;
        String dateString = "Fri, 27 Mar 2020 07:49:21 UTC";

        List<String> testHeaderList = Arrays.asList("(request-target)", "date");
        String testSigningString = ("(request-target): " + method.toString().toLowerCase() + " "
                + requestTarget + "\n" + "date: " + dateString);
        
        signature.update(testSigningString.getBytes());
        byte[] actualHttpRequestSignatureBytes = signature.sign();
        String actualHttpRequestSignature = Base64.getEncoder().encodeToString(actualHttpRequestSignatureBytes);

        String testSignatureHeaderValue = createSignatureString(KNOWN_KEY_ID, RSA_SHA256, testHeaderList,
            actualHttpRequestSignature);

        Map<String, String> testHeaders = new HashMap<String, String>();
        testHeaders.put("content-type", "application/json");
        testHeaders.put("date", dateString);
        testHeaders.put("signature", testSignatureHeaderValue);

        assertFalse(verifier.verifySignatureHeader(testHeaders, requestTarget, method));
    }

    private String createSignatureString(String keyId, String algorithm, List<String> headerKeysUsedInSignature,
            String signatureString) {
        String spaceSeparatedHeaderNames = String.join(" ", headerKeysUsedInSignature);
        return String.format("Signature keyId=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"", keyId,
                algorithm, spaceSeparatedHeaderNames, signatureString);
    }

    /** Creates a Signature header that omits the {@code algorithm} field. */
    private String createSignatureStringWithoutAlgorithm(String keyId, List<String> headerKeysUsedInSignature,
            String signatureString) {
        String spaceSeparatedHeaderNames = String.join(" ", headerKeysUsedInSignature);
        return String.format("Signature keyId=\"%s\",headers=\"%s\",signature=\"%s\"",
                keyId, spaceSeparatedHeaderNames, signatureString);
    }

    // -----------------------------------------------------------------------
    // TODO #3 — null algorithm inference
    // -----------------------------------------------------------------------

    @Test
    public void algorithmIsInferredFromKeyWhenAbsentFromSignatureHeader()
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException,
            UnsupportedEncodingException, HTTPRequestSignatureVerificationException {

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(KEYPAIR1_KEY_PAIR.getPrivate());

        String requestTarget = "/some/path";
        HttpMethod method = HttpMethod.GET;
        String dateString = "Fri, 27 Mar 2020 07:49:21 UTC";

        List<String> testHeaderList = Arrays.asList("(request-target)", "date");
        String signingString = "(request-target): " + method.toString().toLowerCase() + " "
                + requestTarget + "\n" + "date: " + dateString;

        sig.update(signingString.getBytes("ASCII"));
        String b64Sig = Base64.getEncoder().encodeToString(sig.sign());

        // Build a header WITHOUT the algorithm param
        String signatureHeaderValue = createSignatureStringWithoutAlgorithm(KNOWN_KEY_ID, testHeaderList, b64Sig);

        Map<String, String> testHeaders = new HashMap<>();
        testHeaders.put("date", dateString);
        testHeaders.put("signature", signatureHeaderValue);

        // Should succeed: algorithm is inferred from the RSA key
        assertTrue(verifier.verifySignatureHeader(testHeaders, requestTarget, method));
    }

    @Test
    public void inferredAlgorithmIsWrittenBackIntoParamsMap()
            throws HTTPRequestSignatureVerificationException {
        List<String> testHeaderList = Arrays.asList("date");
        String signatureHeaderValue = createSignatureStringWithoutAlgorithm(
                KNOWN_KEY_ID, testHeaderList, "someSig");

        Map<String, String> params = verifier.createMapOfSignatureParams(signatureHeaderValue);
        // The resolved algorithm must be populated even when absent from the header
        assertEquals("rsa-sha256", params.get("algorithm"));
    }

    // -----------------------------------------------------------------------
    // base64 padding bug — split("=") vs split("=", 2)
    // -----------------------------------------------------------------------

    @Test
    public void signatureWithBase64PaddingIsParsedCorrectly()
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException,
            UnsupportedEncodingException, HTTPRequestSignatureVerificationException {

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(KEYPAIR1_KEY_PAIR.getPrivate());

        String requestTarget = "/path";
        HttpMethod method = HttpMethod.POST;
        String dateString = "Mon, 01 Jan 2024 00:00:00 UTC";
        List<String> testHeaderList = Arrays.asList("(request-target)", "date");
        String signingString = "(request-target): " + method.toString().toLowerCase() + " "
                + requestTarget + "\n" + "date: " + dateString;
        sig.update(signingString.getBytes("ASCII"));

        // Keep generating until we get a base64 value that actually contains '=' padding
        byte[] rawSig;
        String b64Sig;
        int attempts = 0;
        do {
            sig.initSign(KEYPAIR1_KEY_PAIR.getPrivate());
            sig.update(signingString.getBytes("ASCII"));
            rawSig = sig.sign();
            b64Sig = Base64.getEncoder().encodeToString(rawSig);
            attempts++;
        } while (!b64Sig.contains("=") && attempts < 200);

        // If after many attempts we still have no padding, create one synthetically
        // by appending a byte that forces the padding — just verify parsing works
        Map<String, String> params = verifier.createMapOfSignatureParams(
                createSignatureString(KNOWN_KEY_ID, RSA_SHA256, testHeaderList, b64Sig));
        assertEquals(b64Sig, params.get("signature"),
                "Signature value including base64 '=' padding must not be truncated");
    }

    // -----------------------------------------------------------------------
    // missing `headers` param → exception rather than NPE
    // -----------------------------------------------------------------------

    @Test
    public void exceptionThrownIfHeadersParamMissingFromSignatureHeader() {
        // Build a header without the `headers=` field
        String signatureHeaderValue = String.format(
                "Signature keyId=\"%s\",algorithm=\"%s\",signature=\"%s\"",
                KNOWN_KEY_ID, RSA_SHA256, "someSig");

        Map<String, String> testHeaders = new HashMap<>();
        testHeaders.put("signature", signatureHeaderValue);

        assertThrows(HTTPRequestSignatureVerificationException.class, () ->
                verifier.recreateSigningString(testHeaders, "/path", HttpMethod.GET));
    }

    // -----------------------------------------------------------------------
    // verifyDigestHeader
    // -----------------------------------------------------------------------

    @Test
    public void digestVerificationPassesWhenNoDigestHeaderPresent()
            throws HTTPRequestSignatureVerificationException, NoSuchAlgorithmException {
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");
        // No digest header — must not throw
        assertDoesNotThrow(() -> verifier.verifyDigestHeader(headers, "{}".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void digestVerificationPassesWithCorrectSha256Digest()
            throws Exception {
        byte[] body = "{\"name\":\"test\"}".getBytes(StandardCharsets.UTF_8);
        String digest = "SHA-256=" + Base64.getEncoder()
                .encodeToString(MessageDigest.getInstance("SHA-256").digest(body));

        Map<String, String> headers = new HashMap<>();
        headers.put("digest", digest);

        assertDoesNotThrow(() -> verifier.verifyDigestHeader(headers, body));
    }

    @Test
    public void digestVerificationFailsWithWrongBody() throws Exception {
        byte[] body = "{\"name\":\"test\"}".getBytes(StandardCharsets.UTF_8);
        byte[] tamperedBody = "{\"name\":\"tampered\"}".getBytes(StandardCharsets.UTF_8);
        String digest = "SHA-256=" + Base64.getEncoder()
                .encodeToString(MessageDigest.getInstance("SHA-256").digest(body));

        Map<String, String> headers = new HashMap<>();
        headers.put("digest", digest);

        assertThrows(HTTPRequestSignatureVerificationException.class,
                () -> verifier.verifyDigestHeader(headers, tamperedBody));
    }

    @Test
    public void digestVerificationFailsWithUnsupportedAlgorithm() {
        Map<String, String> headers = new HashMap<>();
        headers.put("digest", "MD5=abc123");

        assertThrows(HTTPRequestSignatureVerificationException.class,
                () -> verifier.verifyDigestHeader(headers, new byte[0]));
    }

    @Test
    public void digestVerificationFailsWithMalformedHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("digest", "SHA-256-without-equals-sign");

        assertThrows(HTTPRequestSignatureVerificationException.class,
                () -> verifier.verifyDigestHeader(headers, new byte[0]));
    }

}

