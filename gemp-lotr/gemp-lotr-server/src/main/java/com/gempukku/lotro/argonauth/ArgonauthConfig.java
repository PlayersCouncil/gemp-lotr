package com.gempukku.lotro.argonauth;

import java.net.URI;
import java.util.Locale;
import java.util.function.Function;

/**
 * This Gemp's PC account settings (PC-ACCOUNT.md, "Modes"), read once at start. In test mode nothing but the mode
 * is read. In any other mode a missing or malformed setting stops the server from starting, naming the setting.
 *
 * The settings are the `argonauth.*` properties, which gemp-lotr.properties takes from the environment variables
 * `SSO_*` like the database's (AppConfig): docker/.env.example lists them, and docker/.env, which is not
 * committed, is where they and the client secret go. One left blank is as if absent.
 *
 * @param issuer         Argonauth's address as browsers and tokens have it, no trailing slash
 * @param redirectUri    this Gemp's callback address, exactly as registered at Argonauth
 * @param postLogoutUri  where Argonauth sends the browser after a sign-out, exactly as registered
 * @param backchannelUrl where this server reaches Argonauth; the issuer unless set otherwise
 */
public record ArgonauthConfig(ArgonauthMode mode, String issuer, String clientId, String clientSecret,
                              String redirectUri, String postLogoutUri, String backchannelUrl) {

    public static final ArgonauthConfig TEST = new ArgonauthConfig(ArgonauthMode.TEST, "", "", "", "", "", "");

    /** Whether PC accounts exist for this Gemp at all. */
    public boolean enabled() {
        return mode != ArgonauthMode.TEST;
    }

    /**
     * The address this server uses for one of Argonauth's own (from its discovery document, or built on the issuer):
     * the same, with the back-channel address in the issuer's place.
     */
    public String backchannel(String address) {
        return address.startsWith(issuer) ? backchannelUrl + address.substring(issuer.length()) : address;
    }

    public static ArgonauthConfig read(Function<String, String> properties) {
        Function<String, String> setting = key -> {
            String value = properties.apply(key);
            return value == null ? "" : value.trim();
        };
        ArgonauthMode mode = ArgonauthMode.parse(setting.apply("argonauth.mode"));
        if (mode == ArgonauthMode.TEST)
            return TEST;

        String issuer = address(setting, "argonauth.issuer");
        String backchannel = setting.apply("argonauth.backchannel.url").isEmpty()
                ? issuer : address(setting, "argonauth.backchannel.url");
        return new ArgonauthConfig(mode, issuer,
                required(setting, "argonauth.client.id"), required(setting, "argonauth.client.secret"),
                address(setting, "argonauth.redirect.uri"), address(setting, "argonauth.postlogout.uri"),
                backchannel);
    }

    /** A setting as the person who has to fix it knows it: argonauth.client.id is SSO_CLIENT_ID in docker/.env. */
    private static String named(String key) {
        return key + " (" + key.replace("argonauth.", "SSO_").replace('.', '_').toUpperCase(Locale.ROOT)
                + " in docker/.env)";
    }

    private static String required(Function<String, String> setting, String key) {
        String value = setting.apply(key);
        if (value.isEmpty())
            throw new IllegalStateException(named(key) + " is not set, and argonauth.mode is not test");
        return value;
    }

    /** An http(s) address without a trailing slash; plain http only for this machine. */
    private static String address(Function<String, String> setting, String key) {
        String value = required(setting, key);
        URI uri;
        try {
            uri = URI.create(value);
        } catch (IllegalArgumentException exp) {
            throw new IllegalStateException(named(key) + "=" + value + " is not an address");
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        boolean thisMachine = host.equals("localhost") || host.equals("127.0.0.1");
        // The back channel carries the client secret. Unencrypted it may only go to another container on this
        // machine: the Docker host, or a name with no dot in it (a compose service).
        boolean insideThisMachine = key.equals("argonauth.backchannel.url")
                && (host.equals("host.docker.internal") || !host.contains("."));
        if (host.isEmpty() || !(scheme.equals("https") || (scheme.equals("http") && (thisMachine || insideThisMachine))))
            throw new IllegalStateException(named(key) + "=" + value + " must be an https address (http only for localhost"
                    + (key.equals("argonauth.backchannel.url") ? ", host.docker.internal or a container's name)" : ")"));
        if (uri.getQuery() != null || uri.getFragment() != null)
            throw new IllegalStateException(named(key) + "=" + value + " must not have a query or a fragment");
        return value.endsWith("/") && uri.getPath().equals("/") ? value.substring(0, value.length() - 1) : value;
    }
}
