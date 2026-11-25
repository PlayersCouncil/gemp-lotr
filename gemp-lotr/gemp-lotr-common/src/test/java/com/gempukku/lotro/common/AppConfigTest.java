package com.gempukku.lotro.common;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AppConfigTest {
    private final Map<String, String> _environment = new HashMap<>();

    private String resolve(String value) {
        return AppConfig.resolve(value, _environment::get);
    }

    @Test
    public void aValueWithNothingToResolveIsLeftAsItIs() {
        assertNull(resolve(null));
        assertEquals("", resolve(""));
        assertEquals("/etc/gemp-lotr/web/", resolve("/etc/gemp-lotr/web/"));
        assertEquals("/* ping */ select 1", resolve("/* ping */ select 1"));
        // the regular expression that is origin.allowed.pattern, and a password someone put a dollar in
        assertEquals("^.*$", resolve("^.*$"));
        assertEquals("pa$word1", resolve("pa$word1"));
        // only the form with a default is one of ours
        assertEquals("${DB_CONTAINER_NAME}", resolve("${DB_CONTAINER_NAME}"));
        assertEquals("$DB_CONTAINER_NAME", resolve("$DB_CONTAINER_NAME"));
    }

    @Test
    public void theEnvironmentWinsAndTheDefaultIsForWhenItSaysNothing() {
        assertEquals("gempuser", resolve("${MYSQL_USER:-gempuser}"));

        _environment.put("MYSQL_USER", "someone");
        assertEquals("someone", resolve("${MYSQL_USER:-gempuser}"));
    }

    @Test
    public void aVariableLeftBlankIsAsIfUnset() {
        // docker/.env.example lists the PC account's settings with nothing after the equals sign
        _environment.put("SSO_MODE", "");
        _environment.put("MYSQL_USER", "   ");
        assertEquals("test", resolve("${SSO_MODE:-test}"));
        assertEquals("gempuser", resolve("${MYSQL_USER:-gempuser}"));
    }

    @Test
    public void theDefaultCanBeEmptyOrAnAddress() {
        assertEquals("", resolve("${SSO_ISSUER:-}"));
        assertEquals("http://localhost:1630", resolve("${SSO_ISSUER:-http://localhost:1630}"));
        assertEquals("^.*$", resolve("${ORIGIN:-^.*$}"));

        _environment.put("SSO_ISSUER", "https://auth.lotrtcgpc.net");
        assertEquals("https://auth.lotrtcgpc.net", resolve("${SSO_ISSUER:-}"));
        assertEquals("https://auth.lotrtcgpc.net", resolve("${SSO_ISSUER:-http://localhost:1630}"));
    }

    @Test
    public void onlyThePlaceholderIsReplacedAndAValueCanHoldSeveral() {
        String url = "jdbc:mysql://${DB_CONTAINER_NAME:-db}:3306/${MYSQL_DATABASE:-gemp_db}";
        assertEquals("jdbc:mysql://db:3306/gemp_db", resolve(url));

        _environment.put("DB_CONTAINER_NAME", "gemp_db_2");
        assertEquals("jdbc:mysql://gemp_db_2:3306/gemp_db", resolve(url));

        _environment.put("MYSQL_DATABASE", "another");
        assertEquals("jdbc:mysql://gemp_db_2:3306/another", resolve(url));
    }

    @Test
    public void whatTheEnvironmentHoldsIsTakenAsItIs() {
        // a password with a dollar, a backslash or a placeholder of its own in it is not read a second time
        _environment.put("MYSQL_PASSWORD", "p$1\\w${MYSQL_USER:-x}");
        assertEquals("p$1\\w${MYSQL_USER:-x}", resolve("${MYSQL_PASSWORD:-gemppassword}"));
    }

    @Test
    public void thePropertiesFileItselfResolvesWithNothingInTheEnvironment() {
        // what a run outside Docker gets (an IDE, these tests): the container's own addresses
        assertEquals("80", AppConfig.getProperty("port"));
        assertEquals("^.*$", AppConfig.getProperty("origin.allowed.pattern"));
    }
}
