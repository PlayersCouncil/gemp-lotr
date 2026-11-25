package com.gempukku.lotro.argonauth;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * An Argonauth for tests: the sign-in and the Link API as docs/INTEGRATING.md has them on the wire, answered in
 * this process with a key of its own, and with switches for each way a real one could go wrong or be imitated.
 */
public class FakeArgonauth implements AutoCloseable {
    public static final String CLIENT_ID = "gemp-dev";
    public static final String CLIENT_SECRET = "gemp-dev-secret";
    public static final String REDIRECT_URI = "http://localhost:17001/gemp-lotr-server/argonauth/callback";
    public static final String POST_LOGOUT_URI = "http://localhost:17001/gemp-lotr/ng/login";

    /** Made once: each takes a noticeable part of a second. */
    public static final RSAKey KEY = newKey("key-1");
    public static final RSAKey OTHER_KEY = newKey("key-2");

    public record Person(String sub, String username, String displayName) {
    }

    public record LinkRow(String sub, String name, String proof) {
    }

    private record Grant(Person person, String nonce, String challenge, String redirectUri) {
    }

    private record Token(Person person, long expiresAt) {
    }

    private final HttpServer _server;
    private final AtomicLong _serial = new AtomicLong();
    private final Map<String, Grant> _codes = new ConcurrentHashMap<>();
    private final Map<String, Token> _tokens = new ConcurrentHashMap<>();
    private final Map<Integer, LinkRow> _links = new ConcurrentHashMap<>();
    /** Every request answered, as "METHOD /path". */
    public final List<String> requests = java.util.Collections.synchronizedList(new ArrayList<>());

    /** The clock tokens are dated by; tests move it. */
    public final AtomicLong now = new AtomicLong(1_800_000_000_000L);

    // ---- switches: each null or default until a test sets it
    /** The issuer the discovery document and tokens name; the address answered at unless a test says otherwise. */
    public volatile String issuer;
    public volatile RSAKey signingKey = KEY;
    public volatile JWKSet published = new JWKSet(KEY.toPublicJWK());
    /** Changes the ID token's claims before signing. */
    public volatile UnaryOperator<JWTClaimsSet.Builder> claims = builder -> builder;
    /** Makes the whole ID token from its claims, in place of signing it properly. */
    public volatile Function<JWTClaimsSet, String> idToken;
    /** Changes the token answer before it is sent. */
    public volatile UnaryOperator<JSONObject> tokenAnswer = answer -> answer;
    /** Changes the userinfo answer before it is sent. */
    public volatile UnaryOperator<JSONObject> userinfoAnswer = answer -> answer;
    /** Changes the discovery document before it is sent. */
    public volatile UnaryOperator<JSONObject> discovery = document -> document;
    /** A status to answer instead, by path prefix: {"/api/v1/links": 429}. */
    public final Map<String, Integer> statusFor = new ConcurrentHashMap<>();
    /** A body to go with a status above, by the same path prefix. */
    public final Map<String, String> bodyFor = new ConcurrentHashMap<>();
    /**
     * Who is signed in at Argonauth in the browser that comes to its pages: `/identity/o/authorize` sends that
     * browser straight back signed in as them, and with nobody it comes back refused. Tests that play the
     * browser's part themselves use {@link #signIn} and never set this.
     */
    public volatile Person browserPerson;
    /** What the discovery document says `prompt` may be. The real one says none and login; `create` is asked of it. */
    public volatile List<String> promptValues = List.of("none", "login");
    /** The `prompt` of the last sign-in address followed, or null. */
    public volatile String lastPrompt;
    /** How long every answer is held back. */
    public volatile long delayMillis;
    /** Run while a lookup is being answered: something that happens in Gemp meanwhile. */
    public volatile Runnable duringLookup;
    /** The next assertion is recorded, and then answered 504: the answer lost on its way back. */
    public volatile boolean loseNextAssertAnswer;

    public FakeArgonauth() throws IOException {
        this(0);
    }

    /** On a port of the caller's choosing, for a Gemp set up to find it there (a browser run of the whole thing). */
    public FakeArgonauth(int port) throws IOException {
        _server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        _server.createContext("/", this::answer);
        _server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable, "fake-argonauth");
            thread.setDaemon(true);
            return thread;
        }));
        _server.start();
        issuer = address();
    }

    /** Where this one really answers. */
    public String address() {
        return "http://localhost:" + _server.getAddress().getPort();
    }

    public ArgonauthConfig config() {
        return new ArgonauthConfig(ArgonauthMode.OPTIONAL, issuer, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI,
                POST_LOGOUT_URI, address());
    }

    @Override
    public void close() {
        _server.stop(0);
    }

    // ---- what a test arranges

    public void link(Person person, int playerId, String name, String proof) {
        _links.put(playerId, new LinkRow(person.sub(), name, proof));
    }

    public void unlink(int playerId) {
        _links.remove(playerId);
    }

    public LinkRow linkOf(int playerId) {
        return _links.get(playerId);
    }

    /**
     * The browser's part: follows a sign-in address as the person, already signed in at Argonauth, and returns the
     * query Argonauth sends the browser back to Gemp with ("code" and "state").
     */
    public Map<String, String> signIn(String signInAddress, Person person) {
        URI uri = URI.create(signInAddress);
        if (!signInAddress.startsWith(issuer + "/identity/o/authorize?"))
            throw new AssertionError("not Argonauth's authorize address: " + signInAddress);
        return authorize(form(uri.getRawQuery()), person);
    }

    private Map<String, String> authorize(Map<String, String> query, Person person) {
        lastPrompt = query.get("prompt");
        if (lastPrompt != null && !promptValues.contains(lastPrompt))
            throw new AssertionError("a prompt Argonauth does not say it takes: " + lastPrompt);
        require(query, "response_type", "code");
        require(query, "client_id", CLIENT_ID);
        require(query, "redirect_uri", REDIRECT_URI);
        require(query, "scope", "openid profile links");
        require(query, "code_challenge_method", "S256");
        if (query.getOrDefault("state", "").length() < 20 || query.getOrDefault("code_challenge", "").length() != 43)
            throw new AssertionError("a sign-in needs a state and an S256 challenge: " + query);
        String code = "code-" + _serial.incrementAndGet();
        _codes.put(code, new Grant(person, query.get("nonce"), query.get("code_challenge"), query.get("redirect_uri")));
        Map<String, String> back = new LinkedHashMap<>();
        back.put("code", code);
        back.put("state", query.get("state"));
        return back;
    }

    private static void require(Map<String, String> query, String name, String value) {
        if (!value.equals(query.get(name)))
            throw new AssertionError("the sign-in address has " + name + "=" + query.get(name) + ", not " + value);
    }

    // ---- the wire

    private void answer(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        requests.add(method + " " + path);
        try {
            if (delayMillis > 0)
                Thread.sleep(delayMillis);
            for (Map.Entry<String, Integer> forced : statusFor.entrySet()) {
                if (path.startsWith(forced.getKey())) {
                    if (forced.getValue() / 100 == 3)
                        exchange.getResponseHeaders().add("Location", "http://elsewhere.invalid/");
                    send(exchange, forced.getValue(), bodyFor.getOrDefault(forced.getKey(), "{\"error\":\"forced\"}"));
                    return;
                }
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (path.equals("/.well-known/openid-configuration") && method.equals("GET"))
                send(exchange, 200, discovery.apply(discoveryDocument()).toJSONString());
            else if (path.equals("/.well-known/jwks.json") && method.equals("GET"))
                send(exchange, 200, published.toString());
            else if (path.equals("/identity/o/authorize") && method.equals("GET"))
                authorizePage(exchange);
            else if (path.equals("/identity/o/logout") && method.equals("GET"))
                logoutPage(exchange);
            else if (path.equals("/identity/o/api/token") && method.equals("POST"))
                token(exchange, form(body));
            else if (path.equals("/identity/o/api/userinfo") && method.equals("GET"))
                userinfo(exchange);
            else if (path.equals("/api/v1/links") && method.equals("POST"))
                assertLink(exchange, body);
            else if (path.startsWith("/api/v1/links/") && method.equals("GET"))
                lookup(exchange, path.substring("/api/v1/links/".length()));
            else
                send(exchange, 404, "{\"error\":\"not_found\"}");
        } catch (InterruptedException exp) {
            Thread.currentThread().interrupt();
        } catch (Exception exp) {
            send(exchange, 500, "{\"error\":\"" + exp + "\"}");
        }
    }

    /** The sign-in page, for a real browser: no page at all, as with a live Argonauth session. */
    private void authorizePage(HttpExchange exchange) throws IOException {
        Map<String, String> query = form(exchange.getRequestURI().getRawQuery());
        Person person = browserPerson;
        Map<String, String> back = new LinkedHashMap<>();
        if (person == null) {
            back.put("error", "access_denied");
            back.put("state", query.getOrDefault("state", ""));
        } else {
            back.putAll(authorize(query, person));
        }
        StringBuilder location = new StringBuilder(REDIRECT_URI).append('?');
        back.forEach((name, value) -> location.append(name).append('=')
                .append(java.net.URLEncoder.encode(value, StandardCharsets.UTF_8)).append('&'));
        exchange.getResponseHeaders().add("Location", location.substring(0, location.length() - 1));
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    /** Sign-out started by an app: with the ID token no question is asked, and the browser goes straight back. */
    private void logoutPage(HttpExchange exchange) throws IOException {
        Map<String, String> query = form(exchange.getRequestURI().getRawQuery());
        if (!CLIENT_ID.equals(query.get("client_id")) || !POST_LOGOUT_URI.equals(query.get("post_logout_redirect_uri"))
                || query.getOrDefault("id_token_hint", "").isEmpty()) {
            send(exchange, 400, "{\"error\":\"a sign-out Argonauth would not take, or would ask about\"}");
            return;
        }
        browserPerson = null;
        exchange.getResponseHeaders().add("Location", POST_LOGOUT_URI);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    private JSONObject discoveryDocument() {
        JSONObject document = new JSONObject();
        document.put("issuer", issuer);
        document.put("authorization_endpoint", issuer + "/identity/o/authorize");
        document.put("token_endpoint", issuer + "/identity/o/api/token");
        document.put("userinfo_endpoint", issuer + "/identity/o/api/userinfo");
        document.put("jwks_uri", issuer + "/.well-known/jwks.json");
        document.put("end_session_endpoint", issuer + "/identity/o/logout");
        document.put("prompt_values_supported", new JSONArray(promptValues));
        return document;
    }

    private boolean clientIsRight(HttpExchange exchange) {
        String expected = "Basic " + Base64.getEncoder().encodeToString(
                (CLIENT_ID + ":" + CLIENT_SECRET).getBytes(StandardCharsets.UTF_8));
        return expected.equals(exchange.getRequestHeaders().getFirst("Authorization"));
    }

    private void token(HttpExchange exchange, Map<String, String> form) throws Exception {
        if (!clientIsRight(exchange)) {
            send(exchange, 401, "{\"error\":\"invalid_client\"}");
            return;
        }
        // A code works once, whatever becomes of the attempt.
        Grant grant = _codes.remove(form.getOrDefault("code", ""));
        String verifier = form.getOrDefault("code_verifier", "");
        String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(
                MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII)));
        if (grant == null || !"authorization_code".equals(form.get("grant_type"))
                || !grant.redirectUri().equals(form.get("redirect_uri")) || !grant.challenge().equals(challenge)) {
            send(exchange, 400, "{\"error\":\"invalid_grant\"}");
            return;
        }
        String accessToken = "access-" + _serial.incrementAndGet();
        _tokens.put(accessToken, new Token(grant.person(), now.get() + 600_000));

        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder().issuer(issuer).audience(CLIENT_ID)
                .subject(grant.person().sub()).issueTime(new Date(now.get())).expirationTime(new Date(now.get() + 600_000));
        if (grant.nonce() != null)
            builder.claim("nonce", grant.nonce());
        JWTClaimsSet claimsSet = claims.apply(builder).build();
        Function<JWTClaimsSet, String> maker = idToken;
        JSONObject answer = new JSONObject();
        answer.put("access_token", accessToken);
        answer.put("token_type", "Bearer");
        answer.put("expires_in", 600);
        answer.put("scope", "openid profile links");
        answer.put("id_token", maker != null ? maker.apply(claimsSet) : sign(claimsSet, signingKey));
        send(exchange, 200, tokenAnswer.apply(answer).toJSONString());
    }

    /** An ID token with these claims, properly signed by `key`. */
    public static String sign(JWTClaimsSet claimsSet, RSAKey key) {
        try {
            SignedJWT token = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(), claimsSet);
            token.sign(new RSASSASigner(key));
            return token.serialize();
        } catch (Exception exp) {
            throw new IllegalStateException(exp);
        }
    }

    private Token bearer(String token) {
        Token found = token == null ? null : _tokens.get(token);
        return found == null || found.expiresAt() <= now.get() ? null : found;
    }

    private void userinfo(HttpExchange exchange) throws IOException {
        String authorization = exchange.getRequestHeaders().getFirst("Authorization");
        Token token = authorization != null && authorization.startsWith("Bearer ") ? bearer(authorization.substring(7)) : null;
        if (token == null) {
            send(exchange, 401, "{\"error\":\"invalid_token\"}");
            return;
        }
        Person person = token.person();
        JSONObject answer = new JSONObject();
        answer.put("sub", person.sub());
        answer.put("preferred_username", person.username());
        answer.put("display_name", person.displayName());
        answer.put("name", person.displayName());
        answer.put("locale", "en");
        JSONArray links = new JSONArray();
        boolean first = true;
        for (Map.Entry<Integer, LinkRow> link : new java.util.TreeMap<>(_links).entrySet()) {
            if (!link.getValue().sub().equals(person.sub()))
                continue;
            JSONObject row = linkJson(link.getKey(), link.getValue());
            row.remove("sub");
            row.remove("linked");
            if (first)
                row.put("primary", true);
            first = false;
            links.add(row);
        }
        // A link in another system, which this client must not mistake for one of its own.
        JSONObject elsewhere = new JSONObject();
        elsewhere.put("system", "tlhh");
        elsewhere.put("id", "881");
        elsewhere.put("name", person.displayName());
        elsewhere.put("proof", "email");
        links.add(elsewhere);
        answer.put("links", links);
        send(exchange, 200, userinfoAnswer.apply(answer).toJSONString());
    }

    private static JSONObject linkJson(int playerId, LinkRow link) {
        JSONObject row = new JSONObject();
        row.put("linked", true);
        row.put("system", "gemp");
        row.put("id", String.valueOf(playerId));
        row.put("name", link.name());
        row.put("proof", link.proof());
        row.put("sub", link.sub());
        return row;
    }

    private void assertLink(HttpExchange exchange, String body) throws IOException {
        if (!clientIsRight(exchange)) {
            send(exchange, 401, "{\"error\":\"invalid_client\"}");
            return;
        }
        Token token = bearer(exchange.getRequestHeaders().getFirst("X-Argonauth-User-Token"));
        if (token == null) {
            send(exchange, 401, "{\"error\":\"invalid_user_token\"}");
            return;
        }
        JSONObject asked = JSON.parseObject(body);
        String proof = asked.getString("proof");
        if (!(asked.get("external_id") instanceof String) || asked.getString("external_name") == null || proof == null) {
            send(exchange, 400, "{\"error\":\"bad_request\"}");
            return;
        }
        if (!List.of("password", "session", "created").contains(proof)) {
            send(exchange, 403, "{\"error\":\"proof_not_allowed\"}");
            return;
        }
        int playerId = Integer.parseInt(asked.getString("external_id"));
        LinkRow existing = _links.get(playerId);
        if (existing != null && !existing.sub().equals(token.person().sub())) {
            send(exchange, 409, "{\"error\":\"linked_elsewhere\"}");
            return;
        }
        if (existing == null)
            _links.put(playerId, new LinkRow(token.person().sub(), asked.getString("external_name"), proof));
        if (loseNextAssertAnswer) {
            loseNextAssertAnswer = false;
            send(exchange, 504, "{\"error\":\"gateway_timeout\"}");
            return;
        }
        send(exchange, existing == null ? 201 : 200, linkJson(playerId, _links.get(playerId)).toJSONString());
    }

    private void lookup(HttpExchange exchange, String id) throws IOException {
        if (!clientIsRight(exchange)) {
            send(exchange, 401, "{\"error\":\"invalid_client\"}");
            return;
        }
        Runnable during = duringLookup;
        if (during != null)
            during.run();
        LinkRow link = _links.get(Integer.parseInt(id));
        if (link == null)
            send(exchange, 200, "{\"linked\":false,\"system\":\"gemp\",\"id\":\"" + id + "\"}");
        else
            send(exchange, 200, linkJson(Integer.parseInt(id), link).toJSONString());
    }

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    public static Map<String, String> form(String encoded) {
        Map<String, String> values = new LinkedHashMap<>();
        if (encoded == null || encoded.isEmpty())
            return values;
        for (String pair : encoded.split("&")) {
            int equals = pair.indexOf('=');
            String name = equals < 0 ? pair : pair.substring(0, equals);
            String value = equals < 0 ? "" : pair.substring(equals + 1);
            values.put(URLDecoder.decode(name, StandardCharsets.UTF_8), URLDecoder.decode(value, StandardCharsets.UTF_8));
        }
        return values;
    }

    private static RSAKey newKey(String id) {
        try {
            return new RSAKeyGenerator(2048).keyID(id).generate();
        } catch (Exception exp) {
            throw new IllegalStateException(exp);
        }
    }
}
