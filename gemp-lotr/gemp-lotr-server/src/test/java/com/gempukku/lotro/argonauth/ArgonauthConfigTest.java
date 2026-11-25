package com.gempukku.lotro.argonauth;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ArgonauthConfigTest {
    private final Map<String, String> _properties = new HashMap<>();

    private ArgonauthConfig read() {
        return ArgonauthConfig.read(_properties::get);
    }

    private void optional() {
        _properties.put("sso.mode", "optional");
        _properties.put("sso.issuer", "https://auth.lotrtcgpc.net/");
        _properties.put("sso.client.id", "gemp");
        _properties.put("sso.client.secret", "s3cret");
        _properties.put("sso.redirect.uri", "https://play.lotrtcgpc.net/gemp-lotr-server/argonauth/callback");
        _properties.put("sso.postlogout.uri", "https://play.lotrtcgpc.net/gemp-lotr/ng/login");
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

        _properties.put("sso.mode", " Test ");
        _properties.put("sso.issuer", "not even an address");
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
            _properties.put("sso.mode", mode);
            assertRefused("not built");
        }
        _properties.put("sso.mode", "on");
        assertRefused("not a mode");
    }

    @Test
    public void aMissingSettingIsNamed() {
        for (String key : new String[]{"sso.issuer", "sso.client.id", "sso.client.secret",
                "sso.redirect.uri", "sso.postlogout.uri"}) {
            optional();
            _properties.remove(key);
            assertRefused(key);
        }
        // and as what it is called in docker/.env, which is where it is set
        optional();
        _properties.remove("sso.client.id");
        assertRefused("sso.client.id (SSO_CLIENT_ID in docker/.env) is not set");
        _properties.put("sso.client.id", "gemp");
        _properties.put("sso.postlogout.uri", "ftp://x");
        assertRefused("sso.postlogout.uri (SSO_POSTLOGOUT_URI in docker/.env)=ftp://x must be");
    }

    @Test
    public void aSettingLeftBlankIsAsIfAbsent() {
        // gemp-lotr.properties takes each of these from docker/.env (AppConfigTest), where most are left empty:
        // with the mode left at test, that is test mode
        for (String key : new String[]{"sso.issuer", "sso.client.id", "sso.client.secret",
                "sso.redirect.uri", "sso.postlogout.uri", "sso.backchannel.url"})
            _properties.put(key, "");
        _properties.put("sso.mode", "test");
        assertSame(ArgonauthConfig.TEST, read());
        _properties.put("sso.mode", " ");
        assertSame(ArgonauthConfig.TEST, read());

        // an empty back channel is the issuer
        optional();
        _properties.put("sso.backchannel.url", "");
        assertEquals(read().issuer(), read().backchannelUrl());

        // switched on with something still blank: the server stops and names it
        _properties.put("sso.client.secret", " ");
        assertRefused("sso.client.secret");
    }

    @Test
    public void plainHttpIsForThisMachineOnly() {
        optional();
        _properties.put("sso.issuer", "http://auth.lotrtcgpc.net");
        assertRefused("sso.issuer");

        _properties.put("sso.issuer", "http://localhost:1630");
        _properties.put("sso.redirect.uri", "http://localhost:17001/gemp-lotr-server/argonauth/callback");
        _properties.put("sso.postlogout.uri", "http://localhost:17001/gemp-lotr/ng/login");
        assertEquals("http://localhost:1630", read().issuer());

        _properties.put("sso.issuer", "ftp://localhost");
        assertRefused("sso.issuer");
        _properties.put("sso.issuer", "auth.lotrtcgpc.net");
        assertRefused("sso.issuer");
        _properties.put("sso.issuer", "https://auth.lotrtcgpc.net/?x=1");
        assertRefused("sso.issuer");
        _properties.put("sso.issuer", "https://auth lotrtcgpc.net");
        assertRefused("sso.issuer");
    }

    @Test
    public void theServerCanReachArgonauthAtAnotherAddress() {
        optional();
        _properties.put("sso.issuer", "http://localhost:1630");
        _properties.put("sso.redirect.uri", "http://localhost:17001/gemp-lotr-server/argonauth/callback");
        _properties.put("sso.postlogout.uri", "http://localhost:17001/gemp-lotr/ng/login");
        // from inside Gemp's container, Argonauth's container: plain http between them is expected
        _properties.put("sso.backchannel.url", "http://argonauth:8000");
        ArgonauthConfig config = read();

        assertEquals("http://argonauth:8000/identity/o/api/token",
                config.backchannel("http://localhost:1630/identity/o/api/token"));
        // an address that is not Argonauth's is left alone
        assertEquals("https://elsewhere.example/x", config.backchannel("https://elsewhere.example/x"));

        _properties.put("sso.backchannel.url", "http://host.docker.internal:1630");
        assertEquals("http://host.docker.internal:1630", read().backchannelUrl());
        // out on the network the client secret does not travel in clear
        _properties.put("sso.backchannel.url", "http://auth.lotrtcgpc.net");
        assertRefused("sso.backchannel.url");
        _properties.put("sso.backchannel.url", "http://10.0.0.5:8000");
        assertRefused("sso.backchannel.url");
        _properties.put("sso.backchannel.url", "https://auth.lotrtcgpc.net");
        assertEquals("https://auth.lotrtcgpc.net", read().backchannelUrl());
    }
}
