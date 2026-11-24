package com.gempukku.lotro.argonauth;

import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.argonauth.ArgonauthClient.Assertion;
import com.gempukku.lotro.argonauth.ArgonauthClient.SignIn;
import com.gempukku.lotro.argonauth.ArgonauthException.Reason;
import com.gempukku.lotro.argonauth.FakeArgonauth.Person;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ArgonauthClientTest {
    private static final Person FRODO = new Person("760f5b53-1a93-4a03-91a7-d812c8f0fc23", "frodo", "Frodo Baggins");
    private static final Person SAM = new Person("0d9b6a0e-52a7-4a55-a3a5-5c0b1c7f7c11", "sam", "Samwise");

    private FakeArgonauth _argonauth;
    private ArgonauthClient _client;

    @Before
    public void start() throws Exception {
        _argonauth = new FakeArgonauth();
        _client = client();
    }

    @After
    public void stop() {
        _argonauth.close();
    }

    private ArgonauthClient client() {
        return new ArgonauthClient(_argonauth.config(), _argonauth.now::get, Duration.ofSeconds(5));
    }

    /** A whole sign-in as `person`, the browser's part played by the fake. */
    private SignIn signIn(Person person) throws ArgonauthException {
        String state = ArgonauthClient.randomToken();
        String nonce = ArgonauthClient.randomToken();
        String verifier = ArgonauthClient.randomToken();
        Map<String, String> back = _argonauth.signIn(_client.signInAddress(state, nonce, verifier), person);
        assertEquals(state, back.get("state"));
        return _client.completeSignIn(back.get("code"), verifier, nonce);
    }

    private void assertSignInFails(Reason reason, String saying) {
        try {
            signIn(FRODO);
            fail("the sign-in went through");
        } catch (ArgonauthException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains(saying));
            assertEquals(exp.getMessage(), reason, exp.getReason());
        }
    }

    // ---- the sign-in as it should go

    @Test
    public void aSignInGivesThePersonAndTheirGempAccounts() throws Exception {
        _argonauth.link(FRODO, 900001, "Frodo", "password");
        _argonauth.link(FRODO, 900002, "Underhill", "created");
        _argonauth.link(SAM, 900003, "SamGamgee", "session");

        SignIn signIn = signIn(FRODO);

        assertEquals(FRODO.sub(), signIn.sub());
        assertEquals("Frodo Baggins", signIn.displayName());
        assertEquals(_argonauth.now.get() + 600_000, signIn.tokenExpiresAt());
        assertFalse(signIn.idToken().isEmpty());
        // Sam's account is not here, and neither is Frodo's link in another system
        assertEquals(List.of(
                new ArgonauthClient.Link(900001, "Frodo", "password", true),
                new ArgonauthClient.Link(900002, "Underhill", "created", false)), signIn.links());
    }

    @Test
    public void theSignInAddressCarriesTheChallengeAndNeverTheVerifier() throws Exception {
        String verifier = ArgonauthClient.randomToken();
        String address = _client.signInAddress("the-state", "the-nonce", verifier);

        assertTrue(address, address.startsWith(_argonauth.issuer + "/identity/o/authorize?"));
        Map<String, String> query = FakeArgonauth.form(address.substring(address.indexOf('?') + 1));
        assertEquals("the-state", query.get("state"));
        assertEquals("the-nonce", query.get("nonce"));
        assertEquals("S256", query.get("code_challenge_method"));
        assertEquals(ArgonauthClient.challenge(verifier), query.get("code_challenge"));
        assertEquals(FakeArgonauth.REDIRECT_URI, query.get("redirect_uri"));
        assertFalse(address.contains(verifier));
        assertFalse(address.contains(FakeArgonauth.CLIENT_SECRET));
        // RFC 7636's own example
        assertEquals("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
                ArgonauthClient.challenge("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"));
    }

    @Test
    public void randomTokensAreLongAndDiffer() {
        String one = ArgonauthClient.randomToken();
        assertEquals(43, one.length());
        assertTrue(one.matches("[A-Za-z0-9_-]+"));
        assertNotEquals(one, ArgonauthClient.randomToken());
    }

    @Test
    public void aPersonWithNoDisplayNameIsKnownByWhatThereIs() throws Exception {
        _argonauth.userinfoAnswer = answer -> {
            answer.remove("display_name");
            answer.remove("name");
            return answer;
        };
        assertEquals("frodo", signIn(FRODO).displayName());

        _argonauth.userinfoAnswer = answer -> {
            answer.remove("display_name");
            answer.remove("name");
            answer.remove("preferred_username");
            answer.put("links", "not a list");
            return answer;
        };
        SignIn signIn = signIn(FRODO);
        assertEquals("PC Account", signIn.displayName());
        assertTrue(signIn.links().isEmpty());
    }

    @Test
    public void aLinkWhoseIdIsNotAPlayerIdIsLeftOut() throws Exception {
        _argonauth.link(FRODO, 900001, "Frodo", "password");
        _argonauth.userinfoAnswer = answer -> {
            for (String id : new String[]{"Frodo", "-4", "0", "12345678901234567890"}) {
                JSONObject odd = new JSONObject();
                odd.put("system", "gemp");
                odd.put("id", id);
                odd.put("name", "Odd");
                odd.put("proof", "admin");
                answer.getJSONArray("links").add(odd);
            }
            answer.getJSONArray("links").add("not a link");
            return answer;
        };
        assertEquals(List.of(new ArgonauthClient.Link(900001, "Frodo", "password", true)), signIn(FRODO).links());
    }

    // ---- the code

    @Test
    public void aCodeWorksOnce() throws Exception {
        String verifier = ArgonauthClient.randomToken();
        Map<String, String> back = _argonauth.signIn(_client.signInAddress(ArgonauthClient.randomToken(), "n", verifier), FRODO);
        _client.completeSignIn(back.get("code"), verifier, "n");
        try {
            _client.completeSignIn(back.get("code"), verifier, "n");
            fail("a spent code was accepted");
        } catch (ArgonauthException exp) {
            assertEquals(Reason.REJECTED, exp.getReason());
            assertTrue(exp.getMessage(), exp.getMessage().contains("invalid_grant"));
        }
    }

    @Test
    public void aCodeIsNoUseWithoutTheVerifierThatStartedIt() throws Exception {
        Map<String, String> back = _argonauth.signIn(
                _client.signInAddress(ArgonauthClient.randomToken(), "n", ArgonauthClient.randomToken()), FRODO);
        try {
            _client.completeSignIn(back.get("code"), ArgonauthClient.randomToken(), "n");
            fail("a code was accepted with another sign-in's verifier");
        } catch (ArgonauthException exp) {
            assertEquals(Reason.REJECTED, exp.getReason());
        }
    }

    @Test
    public void aTokenAnswerMissingAPartIsRefused() {
        _argonauth.tokenAnswer = answer -> {
            answer.remove("id_token");
            return answer;
        };
        assertSignInFails(Reason.REJECTED, "no ID token");

        _argonauth.tokenAnswer = answer -> {
            answer.put("access_token", " ");
            return answer;
        };
        assertSignInFails(Reason.REJECTED, "no access token");

        _argonauth.tokenAnswer = answer -> {
            answer.put("token_type", "mac");
            return answer;
        };
        assertSignInFails(Reason.REJECTED, "bearer");
    }

    // ---- the ID token: each thing that must be true of it, made false in turn

    private void withClaims(java.util.function.UnaryOperator<JWTClaimsSet.Builder> change) {
        _argonauth.claims = change;
    }

    @Test
    public void anIdTokenFromAnotherIssuerIsRefused() {
        withClaims(claims -> claims.issuer("https://auth.example.com"));
        assertSignInFails(Reason.REJECTED, "is from https://auth.example.com");
        // near misses of the right one
        withClaims(claims -> claims.issuer(_argonauth.issuer + "/"));
        assertSignInFails(Reason.REJECTED, "is from");
        withClaims(claims -> claims.issuer(null));
        assertSignInFails(Reason.REJECTED, "is from");
    }

    @Test
    public void anIdTokenForAnotherAppIsRefused() {
        withClaims(claims -> claims.audience("redbook-dev"));
        assertSignInFails(Reason.REJECTED, "another app");
        withClaims(claims -> claims.audience((String) null));
        assertSignInFails(Reason.REJECTED, "another app");
        // for this app among others, but issued to one of the others
        withClaims(claims -> claims.audience(List.of(FakeArgonauth.CLIENT_ID, "redbook-dev")).claim("azp", "redbook-dev"));
        assertSignInFails(Reason.REJECTED, "several apps");
        withClaims(claims -> claims.audience(List.of(FakeArgonauth.CLIENT_ID, "redbook-dev")));
        assertSignInFails(Reason.REJECTED, "several apps");
    }

    @Test
    public void anIdTokenForSeveralAppsIssuedToThisOneIsAccepted() throws Exception {
        withClaims(claims -> claims.audience(List.of("redbook-dev", FakeArgonauth.CLIENT_ID)).claim("azp", FakeArgonauth.CLIENT_ID));
        assertEquals(FRODO.sub(), signIn(FRODO).sub());
    }

    @Test
    public void anIdTokenOutOfDateIsRefused() {
        withClaims(claims -> claims.expirationTime(new Date(_argonauth.now.get() - 61_000)));
        assertSignInFails(Reason.REJECTED, "expired");
        withClaims(claims -> claims.expirationTime(null));
        assertSignInFails(Reason.REJECTED, "expired");
        withClaims(claims -> claims.issueTime(new Date(_argonauth.now.get() + 61_000)));
        assertSignInFails(Reason.REJECTED, "from the future");
    }

    @Test
    public void clocksMayDifferByLessThanAMinute() throws Exception {
        withClaims(claims -> claims.expirationTime(new Date(_argonauth.now.get() - 30_000))
                .issueTime(new Date(_argonauth.now.get() + 30_000)));
        assertEquals(FRODO.sub(), signIn(FRODO).sub());
        withClaims(claims -> claims.issueTime(null));
        assertEquals(FRODO.sub(), signIn(FRODO).sub());
    }

    @Test
    public void anIdTokenThatAnswersAnotherSignInIsRefused() {
        withClaims(claims -> claims.claim("nonce", ArgonauthClient.randomToken()));
        assertSignInFails(Reason.REJECTED, "does not answer this sign-in");
        withClaims(claims -> claims.claim("nonce", null));
        assertSignInFails(Reason.REJECTED, "does not answer this sign-in");
        withClaims(claims -> claims.claim("nonce", 12345));
        assertSignInFails(Reason.REJECTED, "does not answer this sign-in");
    }

    @Test
    public void aSignInWithoutANonceIsNeverAccepted() throws Exception {
        String verifier = ArgonauthClient.randomToken();
        Map<String, String> back = _argonauth.signIn(_client.signInAddress(ArgonauthClient.randomToken(), "", verifier), FRODO);
        try {
            _client.completeSignIn(back.get("code"), verifier, "");
            fail("a sign-in with an empty nonce went through");
        } catch (ArgonauthException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("does not answer this sign-in"));
        }
    }

    @Test
    public void anIdTokenNamingNobodyIsRefused() {
        withClaims(claims -> claims.subject(" "));
        assertSignInFails(Reason.REJECTED, "names nobody");
    }

    @Test
    public void anIdTokenSignedByAnotherKeyIsRefused() {
        // someone else's key under the published key's id
        _argonauth.idToken = claims -> FakeArgonauth.sign(claims,
                new com.nimbusds.jose.jwk.RSAKey.Builder(FakeArgonauth.OTHER_KEY).keyID("key-1").build());
        assertSignInFails(Reason.REJECTED, "signature is wrong");
        // and under its own id, which Argonauth does not publish
        _argonauth.idToken = claims -> FakeArgonauth.sign(claims, FakeArgonauth.OTHER_KEY);
        assertSignInFails(Reason.REJECTED, "does not publish");
    }

    @Test
    public void anIdTokenChangedAfterSigningIsRefused() {
        _argonauth.idToken = claims -> {
            String[] parts = FakeArgonauth.sign(claims, FakeArgonauth.KEY).split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8)
                    .replace(FRODO.sub(), SAM.sub());
            return parts[0] + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8))
                    + "." + parts[2];
        };
        assertSignInFails(Reason.REJECTED, "signature is wrong");
    }

    @Test
    public void anUnsignedIdTokenIsRefused() {
        _argonauth.idToken = claims -> {
            String header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
            String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(claims.toString().getBytes(StandardCharsets.UTF_8));
            return header + "." + payload + ".";
        };
        assertSignInFails(Reason.REJECTED, "not a signed token");

        _argonauth.idToken = claims -> "not.a.token";
        assertSignInFails(Reason.REJECTED, "not a signed token");
    }

    @Test
    public void anIdTokenMacSignedWithThePublicKeyIsRefused() {
        // The classic confusion: the verifier is handed the public key, and a careless one uses it as an HMAC secret.
        _argonauth.idToken = claims -> {
            try {
                SignedJWT token = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS256).keyID("key-1").build(), claims);
                token.sign(new MACSigner(FakeArgonauth.KEY.toRSAPublicKey().getEncoded()));
                return token.serialize();
            } catch (Exception exp) {
                throw new IllegalStateException(exp);
            }
        };
        assertSignInFails(Reason.REJECTED, "not RS256");
    }

    @Test
    public void aNewSigningKeyIsFetchedButNotMoreThanOnceAMinute() throws Exception {
        signIn(FRODO);
        long keyRequests = keyRequests();

        // Argonauth rotates its key
        _argonauth.signingKey = FakeArgonauth.OTHER_KEY;
        _argonauth.published = new JWKSet(List.of(FakeArgonauth.KEY.toPublicJWK(), FakeArgonauth.OTHER_KEY.toPublicJWK()));

        // within the minute the keys are not asked for again, so a flood of made-up key ids costs Argonauth nothing
        assertSignInFails(Reason.REJECTED, "does not publish");
        assertEquals(keyRequests, keyRequests());

        _argonauth.now.addAndGet(61_000);
        assertEquals(FRODO.sub(), signIn(FRODO).sub());
        assertEquals(keyRequests + 1, keyRequests());
    }

    private long keyRequests() {
        return _argonauth.requests.stream().filter(request -> request.equals("GET /.well-known/jwks.json")).count();
    }

    @Test
    public void aTokenThatNamesNoKeyIsCheckedAgainstTheOnlyKey() throws Exception {
        _argonauth.idToken = claims -> FakeArgonauth.sign(claims,
                new com.nimbusds.jose.jwk.RSAKey.Builder(FakeArgonauth.KEY).keyID(null).build());
        assertEquals(FRODO.sub(), signIn(FRODO).sub());

        // with two keys published there is no telling which, and it is not guessed
        _argonauth.published = new JWKSet(List.of(FakeArgonauth.KEY.toPublicJWK(), FakeArgonauth.OTHER_KEY.toPublicJWK()));
        _client = client();
        assertSignInFails(Reason.REJECTED, "does not publish");
    }

    @Test
    public void keysThatCannotBeReadStopTheSignIn() {
        _argonauth.statusFor.put("/.well-known/jwks.json", 404);
        assertSignInFails(Reason.UNAVAILABLE, "keys answered 404");

        _argonauth.statusFor.put("/.well-known/jwks.json", 200);
        _argonauth.bodyFor.put("/.well-known/jwks.json", "{\"keys\": 5}");
        assertSignInFails(Reason.REJECTED, "keys cannot be read");
    }

    // ---- userinfo

    @Test
    public void userinfoAboutSomeoneElseIsRefused() {
        _argonauth.userinfoAnswer = answer -> {
            answer.put("sub", SAM.sub());
            return answer;
        };
        assertSignInFails(Reason.REJECTED, "someone other than the ID token");
    }

    @Test
    public void userinfoRefusedStopsTheSignIn() {
        _argonauth.statusFor.put("/identity/o/api/userinfo", 401);
        assertSignInFails(Reason.REJECTED, "userinfo was refused: 401");
    }

    // ---- discovery

    @Test
    public void aDiscoveryDocumentForAnotherIssuerIsRefused() {
        _argonauth.discovery = document -> {
            document.put("issuer", "https://auth.example.com");
            return document;
        };
        assertSignInFails(Reason.REJECTED, "the discovery document is for https://auth.example.com");
    }

    @Test
    public void aDiscoveryDocumentCannotSendTheSecretElsewhere() {
        for (String name : new String[]{"authorization_endpoint", "token_endpoint", "userinfo_endpoint", "jwks_uri", "end_session_endpoint"}) {
            _client = client();
            _argonauth.discovery = document -> {
                document.put(name, "https://elsewhere.example/x");
                return document;
            };
            assertSignInFails(Reason.REJECTED, name + " is not under");
        }
        _client = client();
        // an address that merely begins with the issuer's is another host
        _argonauth.discovery = document -> {
            document.put("token_endpoint", _argonauth.issuer + ".elsewhere.example/token");
            return document;
        };
        assertSignInFails(Reason.REJECTED, "token_endpoint is not under");
        _argonauth.discovery = document -> {
            document.remove("userinfo_endpoint");
            return document;
        };
        assertSignInFails(Reason.REJECTED, "userinfo_endpoint is not under");
    }

    @Test
    public void addressesLearnedOnceOutliveAnOutageOfDiscovery() throws Exception {
        signIn(FRODO);
        long asked = _argonauth.requests.stream().filter(request -> request.endsWith("openid-configuration")).count();
        signIn(FRODO);
        assertEquals("the document is remembered", asked,
                _argonauth.requests.stream().filter(request -> request.endsWith("openid-configuration")).count());

        _argonauth.now.addAndGet(Duration.ofHours(2).toMillis());
        _argonauth.statusFor.put("/.well-known/openid-configuration", 503);
        assertEquals(FRODO.sub(), signIn(FRODO).sub());

        // but a document that has turned wrong is not papered over
        _argonauth.statusFor.clear();
        _argonauth.discovery = document -> {
            document.put("issuer", "https://auth.example.com");
            return document;
        };
        assertSignInFails(Reason.REJECTED, "the discovery document is for");
    }

    @Test
    public void withNothingLearnedAnOutageIsAnOutage() {
        _argonauth.statusFor.put("/.well-known/openid-configuration", 503);
        assertSignInFails(Reason.UNAVAILABLE, "answered 503");
        _argonauth.statusFor.put("/.well-known/openid-configuration", 404);
        assertSignInFails(Reason.UNAVAILABLE, "discovery document answered 404");
        _argonauth.statusFor.put("/.well-known/openid-configuration", 200);
        _argonauth.bodyFor.put("/.well-known/openid-configuration", "<html>");
        assertSignInFails(Reason.REJECTED, "not JSON");
        _argonauth.bodyFor.put("/.well-known/openid-configuration", "null");
        assertSignInFails(Reason.REJECTED, "with nothing");
    }

    // ---- the network

    @Test
    public void anArgonauthThatIsDownIsUnavailable() {
        _argonauth.close();
        assertSignInFails(Reason.UNAVAILABLE, "did not answer");
    }

    @Test
    public void anArgonauthThatIsSlowIsNotWaitedForForEver() throws Exception {
        _client = new ArgonauthClient(_argonauth.config(), _argonauth.now::get, Duration.ofMillis(300));
        _argonauth.delayMillis = 2_000;
        long started = System.nanoTime();
        assertSignInFails(Reason.UNAVAILABLE, "did not answer");
        assertTrue(Duration.ofNanos(System.nanoTime() - started).toMillis() < 1_500);
    }

    @Test
    public void aRedirectFromTheTokenAddressIsNotFollowed() throws Exception {
        String verifier = ArgonauthClient.randomToken();
        Map<String, String> back = _argonauth.signIn(_client.signInAddress(ArgonauthClient.randomToken(), "n", verifier), FRODO);
        _argonauth.statusFor.put("/identity/o/api/token", 307);
        try {
            _client.completeSignIn(back.get("code"), verifier, "n");
            fail("followed a redirect with the client secret");
        } catch (ArgonauthException exp) {
            assertEquals(Reason.REJECTED, exp.getReason());
            assertTrue(exp.getMessage(), exp.getMessage().contains("307"));
        }
    }

    @Test
    public void theServerReachesArgonauthAtItsBackChannelAddress() throws Exception {
        // Browsers and tokens know Argonauth by a name this server cannot reach.
        _argonauth.issuer = "http://localhost:1";
        _client = client();
        _argonauth.link(FRODO, 900001, "Frodo", "password");

        String address = _client.signInAddress(ArgonauthClient.randomToken(), "n", ArgonauthClient.randomToken());
        assertTrue(address, address.startsWith("http://localhost:1/identity/o/authorize?"));
        assertEquals(1, signIn(FRODO).links().size());
        assertEquals(FRODO.sub(), _client.ownerOf(900001).sub());
        assertTrue(_client.signOutAddress("the.id.token").startsWith("http://localhost:1/identity/o/logout?"));
    }

    // ---- signing out

    @Test
    public void theSignOutAddressNamesThisGempAndWhereToComeBack() throws Exception {
        String address = _client.signOutAddress("the.id.token");
        assertTrue(address, address.startsWith(_argonauth.issuer + "/identity/o/logout?"));
        Map<String, String> query = FakeArgonauth.form(address.substring(address.indexOf('?') + 1));
        assertEquals(Map.of("client_id", FakeArgonauth.CLIENT_ID, "post_logout_redirect_uri", FakeArgonauth.POST_LOGOUT_URI,
                "id_token_hint", "the.id.token"), query);

        assertFalse(_client.signOutAddress(null).contains("id_token_hint"));
        assertFalse(_client.signOutAddress("").contains("id_token_hint"));
    }

    // ---- the Link API

    @Test
    public void assertingALinkRecordsItAndCanBeRepeated() throws Exception {
        SignIn frodo = signIn(FRODO);

        assertEquals(Assertion.LINKED, _client.assertLink(frodo.accessToken(), 4711, "Underhill", "created"));
        assertEquals(new FakeArgonauth.LinkRow(FRODO.sub(), "Underhill", "created"), _argonauth.linkOf(4711));
        assertEquals(Assertion.LINKED, _client.assertLink(frodo.accessToken(), 4711, "Underhill", "created"));
        assertEquals(new ArgonauthClient.Owner(FRODO.sub(), "created"), _client.ownerOf(4711));
    }

    @Test
    public void anAccountOfAnotherPersonCannotBeTaken() throws Exception {
        _argonauth.link(SAM, 900003, "SamGamgee", "session");
        SignIn frodo = signIn(FRODO);

        assertEquals(Assertion.LINKED_ELSEWHERE, _client.assertLink(frodo.accessToken(), 900003, "SamGamgee", "password"));
        assertEquals(new ArgonauthClient.Owner(SAM.sub(), "session"), _client.ownerOf(900003));
    }

    @Test
    public void aLapsedTokenAndALimitAreToldApartFromFailures() throws Exception {
        SignIn frodo = signIn(FRODO);

        _argonauth.now.addAndGet(601_000);
        assertEquals(Assertion.TOKEN_LAPSED, _client.assertLink(frodo.accessToken(), 4711, "Underhill", "created"));
        assertNull(_argonauth.linkOf(4711));

        _argonauth.statusFor.put("/api/v1/links", 429);
        assertEquals(Assertion.RATE_LIMITED, _client.assertLink(frodo.accessToken(), 4711, "Underhill", "created"));
    }

    private void assertLinkFails(String accessToken, String proof, Reason reason, String saying) {
        try {
            _client.assertLink(accessToken, 4711, "Underhill", proof);
            fail("the link went through");
        } catch (ArgonauthException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains(saying));
            assertEquals(reason, exp.getReason());
        }
    }

    @Test
    public void otherRefusalsOfALinkAreFailures() throws Exception {
        SignIn frodo = signIn(FRODO);

        assertLinkFails(frodo.accessToken(), "admin", Reason.REJECTED, "403 proof_not_allowed");

        _argonauth.statusFor.put("/api/v1/links", 401);
        _argonauth.bodyFor.put("/api/v1/links", "{\"error\":\"invalid_client\"}");
        assertLinkFails(frodo.accessToken(), "created", Reason.REJECTED, "client id or secret is wrong");

        _argonauth.statusFor.put("/api/v1/links", 502);
        assertLinkFails(frodo.accessToken(), "created", Reason.UNAVAILABLE, "answered 502");

        // a success that is about some other account is not one
        _argonauth.statusFor.put("/api/v1/links", 201);
        _argonauth.bodyFor.put("/api/v1/links", "{\"system\":\"gemp\",\"id\":\"1\",\"sub\":\"x\"}");
        assertLinkFails(frodo.accessToken(), "created", Reason.REJECTED, "not the one asserted");
        _argonauth.bodyFor.put("/api/v1/links", "{\"system\":\"wiki\",\"id\":\"4711\",\"sub\":\"x\"}");
        assertLinkFails(frodo.accessToken(), "created", Reason.REJECTED, "not the one asserted");
        _argonauth.bodyFor.put("/api/v1/links", "not json");
        assertLinkFails(frodo.accessToken(), "created", Reason.REJECTED, "not JSON");
    }

    @Test
    public void anAccountNobodyOwnsHasNoOwner() throws Exception {
        assertNull(_client.ownerOf(4711));
    }

    private void assertLookupFails(Reason reason, String saying) {
        try {
            _client.ownerOf(4711);
            fail("the lookup went through");
        } catch (ArgonauthException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains(saying));
            assertEquals(reason, exp.getReason());
        }
    }

    @Test
    public void aLookupThatCannotBeTrustedIsAFailure() {
        _argonauth.statusFor.put("/api/v1/links/", 429);
        assertLookupFails(Reason.UNAVAILABLE, "slow down");
        _argonauth.statusFor.put("/api/v1/links/", 401);
        assertLookupFails(Reason.REJECTED, "the lookup was refused: 401");
        _argonauth.statusFor.put("/api/v1/links/", 503);
        assertLookupFails(Reason.UNAVAILABLE, "answered 503");

        _argonauth.statusFor.put("/api/v1/links/", 200);
        _argonauth.bodyFor.put("/api/v1/links/", "{\"linked\":true,\"system\":\"gemp\",\"id\":\"1\",\"sub\":\"x\"}");
        assertLookupFails(Reason.REJECTED, "another account");
        _argonauth.bodyFor.put("/api/v1/links/", "{\"linked\":true,\"system\":\"forum\",\"id\":\"4711\",\"sub\":\"x\"}");
        assertLookupFails(Reason.REJECTED, "another account");
        _argonauth.bodyFor.put("/api/v1/links/", "{\"linked\":true,\"system\":\"gemp\",\"id\":\"4711\"}");
        assertLookupFails(Reason.REJECTED, "nobody on it");
    }
}
