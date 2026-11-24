package com.gempukku.lotro.argonauth;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ArgonauthConfigTest {
    private final Map<String, String> _properties = new HashMap<>();
    private final Map<String, String> _environment = new HashMap<>();

    private ArgonauthConfig read() {
        return ArgonauthConfig.read(_properties::get, _environment::get);
    }

    private void optional() {
        _properties.put("argonauth.mode", "optional");
        _properties.put("argonauth.issuer", "https://auth.lotrtcgpc.net/");
        _properties.put("argonauth.client.id", "gemp");
        _properties.put("argonauth.client.secret", "s3cret");
        _properties.put("argonauth.redirect.uri", "https://play.lotrtcgpc.net/gemp-lotr-server/argonauth/callback");
        _properties.put("argonauth.postlogout.uri", "https://play.lotrtcgpc.net/gemp-lotr/ng/login");
    }

    private void assertRefused(String naming) {
        try {
            read();
            fail("read a configuration that should have been refused");
        } catch (IllegalStateException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains(naming));
        }
    }

    @Test
    public void withNothingSetItIsTestModeAndNothingElseIsRead() {
        assertSame(ArgonauthConfig.TEST, read());
        assertFalse(read().enabled());

        _properties.put("argonauth.mode", " Test ");
        _properties.put("argonauth.issuer", "not even an address");
        assertSame(ArgonauthConfig.TEST, read());
    }

    @Test
    public void optionalModeReadsEverySetting() {
        optional();
        ArgonauthConfig config = read();
        assertTrue(config.enabled());
        assertEquals(ArgonauthMode.OPTIONAL, config.mode());
        // the issuer is compared with what tokens say: no trailing slash
        assertEquals("https://auth.lotrtcgpc.net", config.issuer());
        assertEquals("gemp", config.clientId());
        assertEquals("s3cret", config.clientSecret());
        assertEquals("https://play.lotrtcgpc.net/gemp-lotr-server/argonauth/callback", config.redirectUri());
        assertEquals("https://play.lotrtcgpc.net/gemp-lotr/ng/login", config.postLogoutUri());
        assertEquals("https://auth.lotrtcgpc.net", config.backchannelUrl());
    }

    @Test
    public void theStagesNotBuiltAndUnknownModesStopTheServer() {
        for (String mode : new String[]{"pester", "required", "argonauth_only"}) {
            _properties.put("argonauth.mode", mode);
            assertRefused("not built");
        }
        _properties.put("argonauth.mode", "on");
        assertRefused("not a mode");
    }

    @Test
    public void aMissingSettingIsNamed() {
        for (String key : new String[]{"argonauth.issuer", "argonauth.client.id", "argonauth.client.secret",
                "argonauth.redirect.uri", "argonauth.postlogout.uri"}) {
            optional();
            _properties.remove(key);
            assertRefused(key);
        }
    }

    @Test
    public void theEnvironmentWinsOverTheFile() {
        optional();
        _properties.remove("argonauth.client.secret");
        _environment.put("ARGONAUTH_CLIENT_SECRET", "from the environment");
        _environment.put("ARGONAUTH_CLIENT_ID", "gemp-sandbox");
        ArgonauthConfig config = read();
        assertEquals("from the environment", config.clientSecret());
        assertEquals("gemp-sandbox", config.clientId());

        _properties.clear();
        _environment.put("ARGONAUTH_MODE", "pester");
        assertRefused("not built");
    }

    @Test
    public void plainHttpIsForThisMachineOnly() {
        optional();
        _properties.put("argonauth.issuer", "http://auth.lotrtcgpc.net");
        assertRefused("argonauth.issuer");

        _properties.put("argonauth.issuer", "http://localhost:1630");
        _properties.put("argonauth.redirect.uri", "http://localhost:17001/gemp-lotr-server/argonauth/callback");
        _properties.put("argonauth.postlogout.uri", "http://localhost:17001/gemp-lotr/ng/login");
        assertEquals("http://localhost:1630", read().issuer());

        _properties.put("argonauth.issuer", "ftp://localhost");
        assertRefused("argonauth.issuer");
        _properties.put("argonauth.issuer", "auth.lotrtcgpc.net");
        assertRefused("argonauth.issuer");
        _properties.put("argonauth.issuer", "https://auth.lotrtcgpc.net/?x=1");
        assertRefused("argonauth.issuer");
        _properties.put("argonauth.issuer", "https://auth lotrtcgpc.net");
        assertRefused("argonauth.issuer");
    }

    @Test
    public void theServerCanReachArgonauthAtAnotherAddress() {
        optional();
        _properties.put("argonauth.issuer", "http://localhost:1630");
        _properties.put("argonauth.redirect.uri", "http://localhost:17001/gemp-lotr-server/argonauth/callback");
        _properties.put("argonauth.postlogout.uri", "http://localhost:17001/gemp-lotr/ng/login");
        // from inside Gemp's container, Argonauth's container: plain http between them is expected
        _properties.put("argonauth.backchannel.url", "http://argonauth:8000");
        ArgonauthConfig config = read();

        assertEquals("http://argonauth:8000/identity/o/api/token",
                config.backchannel("http://localhost:1630/identity/o/api/token"));
        // an address that is not Argonauth's is left alone
        assertEquals("https://elsewhere.example/x", config.backchannel("https://elsewhere.example/x"));
    }
}
