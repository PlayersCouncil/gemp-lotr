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
        File appDir = new File(root, "ng");
        appDir.mkdirs();
        ngIndex = new File(appDir, "index.html");
        Files.writeString(ngIndex.toPath(), "angular");
        Files.writeString(new File(appDir, "main-ABC123.js").toPath(), "js");
        new File(appDir, "themes" + File.separator + "classic").mkdirs();
        Files.writeString(new File(appDir, "themes" + File.separator + "classic" + File.separator + "bg.png").toPath(), "png");
        new File(root, "empty").mkdirs();

        handler = new WebRequestHandler(root.getPath() + File.separator);
    }

    private File serve(String uri) throws Exception {
        return serve(uri, HttpMethod.GET);
    }

    private File serve(String uri, HttpMethod httpMethod) throws Exception {
        // A plain proxy rather than a Mockito mock: records the file passed to writeFile, ignores everything else
        AtomicReference<File> written = new AtomicReference<>();
        ResponseWriter writer = (ResponseWriter) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{ResponseWriter.class}, (proxy, method, args) -> {
                    if (method.getName().equals("writeFile"))
                        written.set((File) args[0]);
                    return null;
                });
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, httpMethod, "/gemp-lotr/" + uri);
        handler.handleRequest(uri, request, new HashMap<>(), writer, "127.0.0.1");
        assertNotNull("No file was written for " + uri, written.get());
        return written.get().getCanonicalFile();
    }

    private void assertStatus(String uri, int expectedStatus) throws Exception {
        assertStatus(uri, HttpMethod.GET, expectedStatus);
    }

    private void assertStatus(String uri, HttpMethod method, int expectedStatus) throws Exception {
        try {
            serve(uri, method);
            fail("Expected HTTP " + expectedStatus + " for " + uri);
        } catch (HttpProcessingException exp) {
            assertEquals(expectedStatus, exp.getStatus());
        }
    }

    @Test
    public void appRootServesItsIndex() throws Exception {
        assertEquals(ngIndex.getCanonicalFile(), serve("ng/"));
    }

    @Test
    public void routeWithTrailingSlashServesTheAppIndex() throws Exception {
        assertEquals(ngIndex.getCanonicalFile(), serve("ng/deckbuilder/"));
    }

    @Test
    public void routeWithoutTrailingSlashServesTheAppIndex() throws Exception {
        assertEquals(ngIndex.getCanonicalFile(), serve("ng/deckbuilder"));
    }

    @Test
    public void nestedRouteServesTheAppIndex() throws Exception {
        assertEquals(ngIndex.getCanonicalFile(), serve("ng/hall/tables/42"));
    }

    @Test
    public void filesInsideTheAppDirectoryAreServedDirectly() throws Exception {
        assertEquals("main-ABC123.js", serve("ng/main-ABC123.js").getName());
        assertEquals("bg.png", serve("ng/themes/classic/bg.png").getName());
    }

    @Test
    public void missingAssetInsideTheAppIsNotFound() throws Exception {
        assertStatus("ng/main-MISSING.js", 404);
        assertStatus("ng/themes/classic/missing.png", 404);
    }

    @Test
    public void routeFallbackIsOnlyForGet() throws Exception {
        assertStatus("ng/deckbuilder", HttpMethod.POST, 404);
    }

    @Test
    public void noFallbackOutsideTheApp() throws Exception {
        assertStatus("deckbuilder", 404);
        assertStatus("ngx/deckbuilder", 404);
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
