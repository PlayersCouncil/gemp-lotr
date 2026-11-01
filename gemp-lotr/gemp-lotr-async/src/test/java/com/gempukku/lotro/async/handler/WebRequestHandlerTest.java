package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class WebRequestHandlerTest {
    @Rule
    public TemporaryFolder webRoot = new TemporaryFolder();

    private WebRequestHandler handler;
    private File ngIndex;

    @Before
    public void setUp() throws Exception {
        File root = webRoot.getRoot().getCanonicalFile();
        Files.writeString(new File(root, "index.html").toPath(), "root");
        File appDir = new File(root, "ng" + File.separator + "deckbuilder");
        appDir.mkdirs();
        ngIndex = new File(appDir, "index.html");
        Files.writeString(ngIndex.toPath(), "angular");
        Files.writeString(new File(appDir, "main-ABC123.js").toPath(), "js");
        new File(root, "empty").mkdirs();

        handler = new WebRequestHandler(root.getPath() + File.separator);
    }

    private File serve(String uri) throws Exception {
        // A plain proxy rather than a Mockito mock: records the file passed to writeFile, ignores everything else
        AtomicReference<File> written = new AtomicReference<>();
        ResponseWriter writer = (ResponseWriter) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{ResponseWriter.class}, (proxy, method, args) -> {
                    if (method.getName().equals("writeFile"))
                        written.set((File) args[0]);
                    return null;
                });
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/gemp-lotr/" + uri);
        handler.handleRequest(uri, request, new HashMap<>(), writer, "127.0.0.1");
        assertNotNull("No file was written for " + uri, written.get());
        return written.get().getCanonicalFile();
    }

    private void assertStatus(String uri, int expectedStatus) throws Exception {
        try {
            serve(uri);
            fail("Expected HTTP " + expectedStatus + " for " + uri);
        } catch (HttpProcessingException exp) {
            assertEquals(expectedStatus, exp.getStatus());
        }
    }

    @Test
    public void directoryWithTrailingSlashServesItsIndex() throws Exception {
        assertEquals(ngIndex.getCanonicalFile(), serve("ng/deckbuilder/"));
    }

    @Test
    public void directoryWithoutTrailingSlashServesItsIndex() throws Exception {
        assertEquals(ngIndex.getCanonicalFile(), serve("ng/deckbuilder"));
    }

    @Test
    public void filesInsideTheAppDirectoryAreServedDirectly() throws Exception {
        assertEquals("main-ABC123.js", serve("ng/deckbuilder/main-ABC123.js").getName());
    }

    @Test
    public void directoryWithoutIndexIsNotFound() throws Exception {
        assertStatus("empty/", 404);
    }

    @Test
    public void pathTraversalIsStillForbidden() throws Exception {
        assertStatus("ng/../../etc/passwd", 403);
    }
}
