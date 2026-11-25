package com.gempukku.lotro.argonauth;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;

import static com.gempukku.lotro.argonauth.ArgonauthException.Reason.REJECTED;
import static com.gempukku.lotro.argonauth.ArgonauthException.Reason.UNAVAILABLE;

/**
 * Everything Gemp says to Argonauth: the sign-in (OpenID Connect, authorization code with PKCE) and the Link API.
 * The wire contract is Argonauth's docs/INTEGRATING.md; what Gemp does with the answers is PC-ACCOUNT.md.
 *
 * Every call here waits on the network, with a timeout: none of it may run on one of the server's IO threads.
 */
public class ArgonauthClient {
    private static final Logger _log = LogManager.getLogger(ArgonauthClient.class);

    static final String SCOPE = "openid profile links";
    /** The system this Gemp's links are in. Argonauth takes it from the client's registration; it is checked on the way back. */
    static final String SYSTEM = "gemp";

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration ANSWER_TIMEOUT = Duration.ofSeconds(10);
    /** Argonauth's addresses and keys are asked for again after this long. */
    private static final long REMEMBERED_MILLIS = Duration.ofHours(1).toMillis();
    /** A key id nobody has heard of sends Gemp back for the keys, but not more often than this. */
    private static final long KEYS_AGAIN_MILLIS = Duration.ofMinutes(1).toMillis();
    /** After failing to fetch Argonauth's addresses or keys, how long before trying again: until then, what is held is used, or the failure repeated. */
    private static final long TRY_AGAIN_MILLIS = Duration.ofSeconds(30).toMillis();
    /** How far Argonauth's clock and this one may differ. */
    private static final long CLOCK_SKEW_MILLIS = Duration.ofMinutes(1).toMillis();

    private static final SecureRandom _random = new SecureRandom();

    /** One of this Gemp's accounts, as Argonauth has it linked to the person who signed in. */
    public record Link(int playerId, String name, String proof, boolean primary) {
    }

    /** A completed sign-in: who it is (`sub`, for ever; `displayName`, for now) and which Gemp accounts are theirs. */
    public record SignIn(String sub, String displayName, String accessToken, long tokenExpiresAt, String idToken,
                         List<Link> links) {
    }

    /** What Argonauth said to "this person owns this account of mine". */
    public enum Assertion {
        LINKED,
        /** The account belongs to a different PC account. Gemp is not told whose. */
        LINKED_ELSEWHERE,
        /** The person's token has lapsed (ten minutes): they pass through Argonauth again first. */
        TOKEN_LAPSED,
        RATE_LIMITED
    }

    private record Endpoints(String authorize, String token, String userinfo, String keys, String logout,
                             boolean takesSignUps, long readAt) {
    }

    private record Answer(int status, String body) {
        JSONObject json() throws ArgonauthException {
            try {
                JSONObject json = JSON.parseObject(body);
                if (json == null)
                    throw new ArgonauthException(REJECTED, "Argonauth answered " + status + " with nothing");
                return json;
            } catch (RuntimeException exp) {
                throw new ArgonauthException(REJECTED, "Argonauth answered " + status + " with something that is not JSON");
            }
        }
    }

    private final ArgonauthConfig _config;
    private final HttpClient _http;
    private final LongSupplier _clock;
    private final Duration _answerTimeout;

    // Each of the two fetches is made by one thread at a time. The locks are not monitors: a virtual thread that
    // waits for one, or on the network while holding one, does not hold up the others' carrier.
    private final ReentrantLock _endpointsLock = new ReentrantLock();
    private final ReentrantLock _keysLock = new ReentrantLock();
    private volatile Endpoints _endpoints;
    private volatile ArgonauthException _endpointsFailure;
    private volatile long _endpointsFailedAt;
    private volatile JWKSet _keys;
    private volatile long _keysReadAt;
    private volatile ArgonauthException _keysFailure;
    private volatile long _keysFailedAt;

    public ArgonauthClient(ArgonauthConfig config) {
        this(config, System::currentTimeMillis, ANSWER_TIMEOUT);
    }

    ArgonauthClient(ArgonauthConfig config, LongSupplier clock, Duration answerTimeout) {
        _config = config;
        _clock = clock;
        _answerTimeout = answerTimeout;
        // Redirects are never followed: none of Argonauth's API answers is one, and a followed one could carry the
        // client secret to another address.
        _http = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NEVER).version(HttpClient.Version.HTTP_1_1).build();
    }

    /** A value nobody can guess, for `state`, `nonce` and the PKCE verifier: 43 characters, all safe in an address. */
    public static String randomToken() {
        byte[] bytes = new byte[32];
        _random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** PKCE, S256: what is sent to Argonauth in place of the verifier, which only the token exchange gives up. */
    static String challenge(String codeVerifier) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException exp) {
            throw new IllegalStateException(exp);
        }
    }

    /** Where the browser goes to sign in. Argonauth sends it back to the redirect address with `code` and `state`. */
    public String signInAddress(String state, String nonce, String codeVerifier) throws ArgonauthException {
        return signInAddress(state, nonce, codeVerifier, false);
    }

    /**
     * As above; with `signUp`, for someone who has said they have no PC account yet: Argonauth is asked for its
     * sign-up page in place of its sign-in page (`prompt=create`, OpenID Connect's "Initiating User Registration"),
     * if its discovery document says it takes that. If it does not, the sign-in page it is, which has its own way
     * to sign up. Either way they come back signed in.
     */
    public String signInAddress(String state, String nonce, String codeVerifier, boolean signUp) throws ArgonauthException {
        Endpoints endpoints = endpoints();
        Map<String, String> query = new LinkedHashMap<>();
        query.put("response_type", "code");
        query.put("client_id", _config.clientId());
        query.put("redirect_uri", _config.redirectUri());
        query.put("scope", SCOPE);
        query.put("state", state);
        query.put("nonce", nonce);
        query.put("code_challenge", challenge(codeVerifier));
        query.put("code_challenge_method", "S256");
        if (signUp && endpoints.takesSignUps())
            query.put("prompt", "create");
        return endpoints.authorize() + "?" + form(query);
    }

    /**
     * Where the browser goes to end the person's session at Argonauth, which then sends it to this Gemp's
     * post-logout address. The ID token is what will let that through without a question, if Ketura turns the
     * question off (Argonauth's answers to Gemp, 2).
     */
    public String signOutAddress(String idToken) throws ArgonauthException {
        Map<String, String> query = new LinkedHashMap<>();
        query.put("client_id", _config.clientId());
        query.put("post_logout_redirect_uri", _config.postLogoutUri());
        if (idToken != null && !idToken.isEmpty())
            query.put("id_token_hint", idToken);
        return endpoints().logout() + "?" + form(query);
    }

    /**
     * Turns the `code` Argonauth sent back into the person: swaps it for tokens, checks the ID token (signature,
     * issuer, audience, expiry, and that it answers this sign-in's `nonce`), then asks who it is and what is linked.
     */
    public SignIn completeSignIn(String code, String codeVerifier, String nonce) throws ArgonauthException {
        Endpoints endpoints = endpoints();

        Map<String, String> body = new LinkedHashMap<>();
        body.put("grant_type", "authorization_code");
        body.put("code", code);
        body.put("redirect_uri", _config.redirectUri());
        body.put("code_verifier", codeVerifier);
        Answer tokenAnswer = send(HttpRequest.newBuilder(backchannel(endpoints.token()))
                .header("Authorization", basic())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form(body))));
        if (tokenAnswer.status() != 200)
            throw new ArgonauthException(REJECTED, "the code was not accepted: " + tokenAnswer.status() + " " + errorOf(tokenAnswer));
        JSONObject tokens = tokenAnswer.json();
        String accessToken = tokens.getString("access_token");
        String idToken = tokens.getString("id_token");
        if (isBlank(accessToken) || isBlank(idToken))
            throw new ArgonauthException(REJECTED, "the token answer has no access token or no ID token");
        if (!"bearer".equalsIgnoreCase(tokens.getString("token_type")))
            throw new ArgonauthException(REJECTED, "the access token is not a bearer token");
        long now = _clock.getAsLong();
        long lifetimeSeconds = Math.max(0, tokens.getLongValue("expires_in"));

        String sub = checkIdToken(idToken, nonce);

        Answer whoAnswer = send(HttpRequest.newBuilder(backchannel(endpoints.userinfo()))
                .header("Authorization", "Bearer " + accessToken).GET());
        if (whoAnswer.status() != 200)
            throw new ArgonauthException(REJECTED, "userinfo was refused: " + whoAnswer.status() + " " + errorOf(whoAnswer));
        JSONObject who = whoAnswer.json();
        // The two answers must be about one person (OpenID Connect Core 5.3.2).
        if (!sub.equals(who.getString("sub")))
            throw new ArgonauthException(REJECTED, "userinfo is about someone other than the ID token");

        String displayName = firstText(who.getString("display_name"), who.getString("name"),
                who.getString("preferred_username"), "PC Account");
        return new SignIn(sub, displayName, accessToken, now + lifetimeSeconds * 1000, idToken, links(who.get("links")));
    }

    /**
     * Tells Argonauth that the person whose token this is owns the Gemp account `playerId`, proved by `proof`
     * (`password`, `session` or `created`). Linking again what is already theirs is LINKED.
     */
    public Assertion assertLink(String accessToken, int playerId, String playerName, String proof) throws ArgonauthException {
        JSONObject body = new JSONObject();
        body.put("external_id", String.valueOf(playerId));
        body.put("external_name", playerName);
        body.put("proof", proof);
        Answer answer = send(HttpRequest.newBuilder(backchannel(_config.issuer() + "/api/v1/links"))
                .header("Authorization", basic())
                .header("X-Argonauth-User-Token", accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString())));
        String error = errorOf(answer);
        switch (answer.status()) {
            case 200, 201 -> {
                JSONObject link = answer.json();
                if (!SYSTEM.equals(link.getString("system")) || !String.valueOf(playerId).equals(link.getString("id")))
                    throw new ArgonauthException(REJECTED, "the link that came back is not the one asserted");
                return Assertion.LINKED;
            }
            case 409 -> {
                return Assertion.LINKED_ELSEWHERE;
            }
            case 429 -> {
                return Assertion.RATE_LIMITED;
            }
            case 401 -> {
                if ("invalid_user_token".equals(error))
                    return Assertion.TOKEN_LAPSED;
                throw new ArgonauthException(REJECTED, "this Gemp's client id or secret is wrong at Argonauth");
            }
            default -> throw new ArgonauthException(REJECTED, "the link was refused: " + answer.status() + " " + error);
        }
    }

    /** Who owns one of this Gemp's accounts, and by what proof, as Argonauth has it now. */
    public record Owner(String sub, String proof) {
    }

    /** The PC account that owns the Gemp account `playerId` now, or null when nobody does. */
    public Owner ownerOf(int playerId) throws ArgonauthException {
        Answer answer = send(HttpRequest.newBuilder(backchannel(_config.issuer() + "/api/v1/links/" + playerId))
                .header("Authorization", basic()).GET());
        if (answer.status() == 429)
            throw new ArgonauthException(UNAVAILABLE, "Argonauth says slow down");
        if (answer.status() != 200)
            throw new ArgonauthException(REJECTED, "the lookup was refused: " + answer.status() + " " + errorOf(answer));
        JSONObject link = answer.json();
        if (!SYSTEM.equals(link.getString("system")) || !String.valueOf(playerId).equals(link.getString("id")))
            throw new ArgonauthException(REJECTED, "the lookup answered about another account");
        if (!link.getBooleanValue("linked"))
            return null;
        String sub = link.getString("sub");
        if (isBlank(sub))
            throw new ArgonauthException(REJECTED, "a link with nobody on it");
        return new Owner(sub, firstText(link.getString("proof"), "unknown"));
    }

    // ---- the ID token

    /** The `sub` of an ID token that is Argonauth's, for this Gemp, in date, and the answer to `nonce`. */
    private String checkIdToken(String idToken, String nonce) throws ArgonauthException {
        SignedJWT token;
        JWTClaimsSet claims;
        try {
            token = SignedJWT.parse(idToken);
            claims = token.getJWTClaimsSet();
        } catch (ParseException exp) {
            throw new ArgonauthException(REJECTED, "the ID token is not a signed token");
        }
        // One algorithm, named here and never taken from the token: not `none`, and not a MAC made with the public key.
        if (!JWSAlgorithm.RS256.equals(token.getHeader().getAlgorithm()))
            throw new ArgonauthException(REJECTED, "the ID token is signed with " + token.getHeader().getAlgorithm() + ", not RS256");
        RSAKey key = key(token.getHeader().getKeyID());
        try {
            if (!token.verify(new RSASSAVerifier(key.toRSAPublicKey())))
                throw new ArgonauthException(REJECTED, "the ID token's signature is wrong");
        } catch (JOSEException exp) {
            throw new ArgonauthException(REJECTED, "the ID token's signature could not be checked", exp);
        }

        long now = _clock.getAsLong();
        if (!_config.issuer().equals(claims.getIssuer()))
            throw new ArgonauthException(REJECTED, "the ID token is from " + claims.getIssuer());
        List<String> audience = claims.getAudience();
        if (audience == null || !audience.contains(_config.clientId()))
            throw new ArgonauthException(REJECTED, "the ID token is for another app");
        if (audience.size() > 1 && !_config.clientId().equals(claims.getClaim("azp")))
            throw new ArgonauthException(REJECTED, "the ID token is for several apps and was not issued to this one");
        Date expires = claims.getExpirationTime();
        if (expires == null || expires.getTime() + CLOCK_SKEW_MILLIS <= now)
            throw new ArgonauthException(REJECTED, "the ID token has expired");
        Date issued = claims.getIssueTime();
        if (issued != null && issued.getTime() - CLOCK_SKEW_MILLIS > now)
            throw new ArgonauthException(REJECTED, "the ID token is from the future");
        Object answered = claims.getClaim("nonce");
        if (isBlank(nonce) || !(answered instanceof String) || !MessageDigest.isEqual(
                nonce.getBytes(StandardCharsets.UTF_8), ((String) answered).getBytes(StandardCharsets.UTF_8)))
            throw new ArgonauthException(REJECTED, "the ID token does not answer this sign-in");
        if (isBlank(claims.getSubject()))
            throw new ArgonauthException(REJECTED, "the ID token names nobody");
        return claims.getSubject();
    }

    /** Argonauth's signing key with this id. An id not among the keys Gemp holds sends it for the keys again, once. */
    private RSAKey key(String keyId) throws ArgonauthException {
        long now = _clock.getAsLong();
        JWKSet keys = _keys;
        if (keys == null || now - _keysReadAt > REMEMBERED_MILLIS)
            keys = readKeys(now, _keysReadAt);
        RSAKey key = signingKey(keys, keyId);
        if (key == null && now - _keysReadAt > KEYS_AGAIN_MILLIS)
            key = signingKey(readKeys(now, _keysReadAt), keyId);
        if (key == null)
            throw new ArgonauthException(REJECTED, "the ID token is signed with a key Argonauth does not publish");
        return key;
    }

    private static RSAKey signingKey(JWKSet keys, String keyId) {
        RSAKey only = null;
        int count = 0;
        for (JWK key : keys.getKeys()) {
            if (!(key instanceof RSAKey rsa) || (key.getKeyUse() != null && key.getKeyUse() != KeyUse.SIGNATURE))
                continue;
            if (keyId != null && keyId.equals(key.getKeyID()))
                return rsa;
            only = rsa;
            count++;
        }
        // A token that names no key is checked against the one key there is, and against none when there are several.
        return keyId == null && count == 1 ? only : null;
    }

    /** Fetches the keys, unless another thread has since `heldFrom` (when the caller looked), or a fetch has just failed. */
    private JWKSet readKeys(long now, long heldFrom) throws ArgonauthException {
        _keysLock.lock();
        try {
            if (_keys != null && _keysReadAt != heldFrom)
                return _keys;
            if (_keysFailure != null && now - _keysFailedAt < TRY_AGAIN_MILLIS) {
                if (_keys != null)
                    return _keys;
                throw _keysFailure;
            }
            try {
                Answer answer = send(HttpRequest.newBuilder(backchannel(endpoints().keys())).GET());
                if (answer.status() != 200)
                    throw new ArgonauthException(UNAVAILABLE, "Argonauth's keys answered " + answer.status());
                try {
                    _keys = JWKSet.parse(answer.body());
                } catch (ParseException exp) {
                    throw new ArgonauthException(REJECTED, "Argonauth's keys cannot be read");
                }
                _keysReadAt = now;
                _keysFailure = null;
                return _keys;
            } catch (ArgonauthException exp) {
                _keysFailure = exp;
                _keysFailedAt = now;
                throw exp;
            }
        } finally {
            _keysLock.unlock();
        }
    }

    // ---- discovery

    private Endpoints endpoints() throws ArgonauthException {
        Endpoints endpoints = _endpoints;
        long now = _clock.getAsLong();
        if (endpoints != null && now - endpoints.readAt() <= REMEMBERED_MILLIS)
            return endpoints;
        try {
            return readEndpoints(now, endpoints);
        } catch (ArgonauthException exp) {
            // Addresses that were good an hour ago are better than none while Argonauth is away.
            if (endpoints != null && exp.getReason() == UNAVAILABLE)
                return endpoints;
            throw exp;
        }
    }

    /** Fetches the discovery document, unless another thread has since the caller looked, or a fetch has just failed. */
    private Endpoints readEndpoints(long now, Endpoints held) throws ArgonauthException {
        _endpointsLock.lock();
        try {
            if (_endpoints != held)
                return _endpoints;
            if (_endpointsFailure != null && now - _endpointsFailedAt < TRY_AGAIN_MILLIS)
                throw _endpointsFailure;
            try {
                Answer answer = send(HttpRequest.newBuilder(
                        backchannel(_config.issuer() + "/.well-known/openid-configuration")).GET());
                if (answer.status() != 200)
                    throw new ArgonauthException(UNAVAILABLE, "Argonauth's discovery document answered " + answer.status());
                JSONObject document = answer.json();
                if (!_config.issuer().equals(document.getString("issuer")))
                    throw new ArgonauthException(REJECTED, "the discovery document is for " + document.getString("issuer")
                            + ", and sso.issuer is " + _config.issuer());
                _endpoints = new Endpoints(endpoint(document, "authorization_endpoint"), endpoint(document, "token_endpoint"),
                        endpoint(document, "userinfo_endpoint"), endpoint(document, "jwks_uri"),
                        endpoint(document, "end_session_endpoint"),
                        document.get("prompt_values_supported") instanceof JSONArray prompts && prompts.contains("create"), now);
                _endpointsFailure = null;
                return _endpoints;
            } catch (ArgonauthException exp) {
                _endpointsFailure = exp;
                _endpointsFailedAt = now;
                throw exp;
            }
        } finally {
            _endpointsLock.unlock();
        }
    }

    /** One of the discovery document's addresses, which must be the issuer's own: the client secret is sent to some. */
    private String endpoint(JSONObject document, String name) throws ArgonauthException {
        String address = document.getString(name);
        if (isBlank(address) || !address.startsWith(_config.issuer() + "/"))
            throw new ArgonauthException(REJECTED, "the discovery document's " + name + " is not under " + _config.issuer());
        return address;
    }

    // ---- plumbing

    private URI backchannel(String address) {
        return URI.create(_config.backchannel(address));
    }

    private String basic() {
        // As Argonauth reads it: the two joined by a colon, neither of them escaped first.
        return "Basic " + Base64.getEncoder().encodeToString(
                (_config.clientId() + ":" + _config.clientSecret()).getBytes(StandardCharsets.UTF_8));
    }

    private Answer send(HttpRequest.Builder request) throws ArgonauthException {
        HttpRequest built = request.timeout(_answerTimeout).header("Accept", "application/json").build();
        try {
            HttpResponse<String> response = _http.send(built, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 500)
                throw new ArgonauthException(UNAVAILABLE, "Argonauth answered " + response.statusCode() + " at " + built.uri().getPath());
            return new Answer(response.statusCode(), response.body());
        } catch (IOException exp) {
            throw new ArgonauthException(UNAVAILABLE, "Argonauth did not answer at " + built.uri().getPath() + ": " + exp, exp);
        } catch (InterruptedException exp) {
            Thread.currentThread().interrupt();
            throw new ArgonauthException(UNAVAILABLE, "interrupted while waiting for Argonauth", exp);
        }
    }

    /** The `error` of an answer that has one, for the log and for telling Argonauth's refusals apart. */
    private static String errorOf(Answer answer) {
        try {
            JSONObject json = JSON.parseObject(answer.body());
            String error = json == null ? null : json.getString("error");
            return error == null ? "" : error;
        } catch (RuntimeException exp) {
            return "";
        }
    }

    private static List<Link> links(Object value) {
        List<Link> links = new ArrayList<>();
        if (!(value instanceof JSONArray array))
            return links;
        for (Object item : array) {
            if (!(item instanceof JSONObject link) || !SYSTEM.equals(link.getString("system")))
                continue;
            try {
                int playerId = Integer.parseInt(String.valueOf(link.get("id")));
                if (playerId <= 0)
                    throw new NumberFormatException();
                links.add(new Link(playerId, link.getString("name"), link.getString("proof"), link.getBooleanValue("primary")));
            } catch (NumberFormatException exp) {
                _log.warn("Argonauth has a gemp link whose id is not a player id: " + link.get("id"));
            }
        }
        return links;
    }

    private static String form(Map<String, String> values) {
        StringBuilder form = new StringBuilder();
        for (Map.Entry<String, String> value : values.entrySet()) {
            if (form.length() > 0)
                form.append('&');
            form.append(URLEncoder.encode(value.getKey(), StandardCharsets.UTF_8)).append('=')
                    .append(URLEncoder.encode(value.getValue(), StandardCharsets.UTF_8));
        }
        return form.toString();
    }

    private static String firstText(String... values) {
        for (String value : values)
            if (!isBlank(value))
                return value.trim();
        return "";
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
