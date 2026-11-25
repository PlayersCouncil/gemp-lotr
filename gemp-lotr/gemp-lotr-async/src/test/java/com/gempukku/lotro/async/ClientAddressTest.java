package com.gempukku.lotro.async;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Which address a request is taken to come from: what IP bans, the login limit and a player's recorded address
 * all go by. nginx here sets `X-Forwarded-For $proxy_add_x_forwarded_for`: the client's own header, then a comma,
 * then the address nginx saw.
 */
public class ClientAddressTest {
    private static final String SOCKET = "172.28.1.1";

    @Test
    public void behindNginxItIsTheAddressNginxSaw() {
        assertEquals("203.0.113.7", GempukkuHttpRequestHandler.clientAddress("203.0.113.7", SOCKET));
        assertEquals("2001:db8::7", GempukkuHttpRequestHandler.clientAddress("2001:db8::7", SOCKET));
    }

    @Test
    public void aHeaderTheClientSentItselfChangesNothing() {
        // a banned player claiming to be someone else, or nobody: nginx still appends who they are
        assertEquals("203.0.113.7", GempukkuHttpRequestHandler.clientAddress("10.0.0.1, 203.0.113.7", SOCKET));
        assertEquals("203.0.113.7", GempukkuHttpRequestHandler.clientAddress("anything at all,203.0.113.7", SOCKET));
        assertEquals("203.0.113.7", GempukkuHttpRequestHandler.clientAddress("1.1.1.1, 2.2.2.2,  203.0.113.7 ", SOCKET));
    }

    @Test
    public void withNoProxyTheSocketSays() {
        assertEquals(SOCKET, GempukkuHttpRequestHandler.clientAddress(null, SOCKET));
        assertEquals(SOCKET, GempukkuHttpRequestHandler.clientAddress("", SOCKET));
        assertEquals(SOCKET, GempukkuHttpRequestHandler.clientAddress("203.0.113.7, ", SOCKET));
    }
}
