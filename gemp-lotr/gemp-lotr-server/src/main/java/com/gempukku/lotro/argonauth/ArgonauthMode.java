package com.gempukku.lotro.argonauth;

/** How far this Gemp has gone over to PC accounts (PC-ACCOUNT.md, "Modes"). */
public enum ArgonauthMode {
    /** No PC account anywhere: today's Gemp. The default, for local development and test servers. */
    TEST,
    /** Sign in with a PC account or with the old password; new accounts only through a PC account. */
    OPTIONAL;

    /** The stages named in the plan that are not built: the server refuses to start in them. */
    private static final java.util.Set<String> NOT_BUILT = java.util.Set.of("pester", "required", "argonauth_only");

    public static ArgonauthMode parse(String value) {
        String mode = value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
        if (mode.isEmpty() || mode.equals("test"))
            return TEST;
        if (mode.equals("optional"))
            return OPTIONAL;
        if (NOT_BUILT.contains(mode))
            throw new IllegalStateException("argonauth.mode (SSO_MODE in docker/.env)=" + mode + " is not built yet: use test or optional");
        throw new IllegalStateException("argonauth.mode (SSO_MODE in docker/.env)=" + mode + " is not a mode: use test or optional");
    }
}
