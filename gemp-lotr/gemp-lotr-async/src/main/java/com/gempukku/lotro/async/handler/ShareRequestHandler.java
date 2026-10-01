package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.common.AppConfig;
import com.gempukku.lotro.db.DeckDAO;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.patchnotes.PatchNotesLibrary;
import com.gempukku.lotro.share.CardImages;
import com.gempukku.lotro.share.SharePages;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Share links (public, GET/HEAD, no login): {@code /gemp-lotr/share/<kind>/<id>} answers with a small HTML page whose
 * Open Graph / Twitter tags make the link preview nicely where it is pasted, and which sends a person on to the real
 * page at once.  The kinds and where they go are {@link SharePages}':
 * <pre>
 *   /gemp-lotr/share/patch-notes[/&lt;slug&gt;]   -> /gemp-lotr/hall.html#patch-notes[/&lt;slug&gt;]
 *   /gemp-lotr/share/errata/&lt;card id&gt;        -> /gemp-lotr/hall.html#errata-&lt;card id&gt;
 *   /gemp-lotr/share/format/&lt;code&gt;           -> /gemp-lotr/hall.html#format-&lt;code&gt;
 *   /gemp-lotr/share/card/&lt;blueprint id&gt;     -> /gemp-lotr/hall.html#card-&lt;blueprint id&gt;
 *   /gemp-lotr/share/deck/&lt;share code&gt;       -> /gemp-lotr-server/deck/html?id=&lt;share code&gt;
 *   /gemp-lotr/share/deck?id=&lt;share code&gt;    (the same; the form today's /share/deck?id= links take)
 * </pre>
 * An unknown kind or a malformed id is a 404.  The page is the same for everyone, so it may be cached
 * ({@code Cache-Control: public, max-age=300}).  Every value in it is HTML-escaped.
 * <p>
 * Registered by {@link RootUriRequestHandler} ahead of the static files under /gemp-lotr/ (there is no share folder).
 * The same links also answer at the site root ({@code /share/<kind>/<id>}, and the deck builder's
 * {@code /share/deck?id=<code>}), for a proxy that forwards /share/ to this server.
 */
public class ShareRequestHandler implements UriRequestHandler {
    private static final Logger _log = LogManager.getLogger(ShareRequestHandler.class);

    public static final String PREFIX = "/gemp-lotr/share/";
    /** the same links at the site root, as the deck builder's /share/deck?id= links are made today */
    public static final String ROOT_PREFIX = "/share/";
    /** when the request carries no usable Host header (hall.html's own og:url) */
    public static final String DEFAULT_ORIGIN = "https://play.lotrtcgpc.net";
    public static final String CACHE_CONTROL = "public, max-age=300";

    private static final Pattern HOST = Pattern.compile("^[A-Za-z0-9.-]{1,253}(:\\d{1,5})?$");

    private final SharePages _pages;

    public ShareRequestHandler(Map<Type, Object> context) {
        this(pagesFrom(context));
    }

    public ShareRequestHandler(SharePages pages) {
        _pages = pages;
    }

    private static SharePages pagesFrom(Map<Type, Object> context) {
        DeckDAO deckDao = (DeckDAO) context.get(DeckDAO.class);
        PlayerDAO playerDao = (PlayerDAO) context.get(PlayerDAO.class);
        String webPath = AppConfig.getWebPath();
        return new SharePages(
                (PatchNotesLibrary) context.get(PatchNotesLibrary.class),
                (LotroCardBlueprintLibrary) context.get(LotroCardBlueprintLibrary.class),
                (LotroFormatLibrary) context.get(LotroFormatLibrary.class),
                new CardImages(webPath == null ? null : new File(webPath)),
                deckDao == null || playerDao == null ? null : (owner, deckName) -> {
                    try {
                        Player player = playerDao.getPlayer(owner);
                        return player == null ? null : deckDao.getDeckForPlayer(player, deckName);
                    } catch (RuntimeException exp) {
                        _log.debug("Share link for a deck of " + owner + ": " + exp.getMessage());
                        return null;
                    }
                });
    }

    /** Whether a request path is a share link (the router hands those to this handler). */
    public static boolean handles(String uri) {
        return uri.startsWith(PREFIX) || uri.startsWith(ROOT_PREFIX);
    }

    /**
     * @param uri the whole path (no query), starting with {@link #PREFIX} or {@link #ROOT_PREFIX}
     */
    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (request.method() != HttpMethod.GET && request.method() != HttpMethod.HEAD)
            throw new HttpProcessingException(405);
        String rest;
        if (uri.startsWith(PREFIX))
            rest = uri.substring(PREFIX.length());
        else if (uri.startsWith(ROOT_PREFIX))
            rest = uri.substring(ROOT_PREFIX.length());
        else
            throw new HttpProcessingException(404);

        int slash = rest.indexOf('/');
        String kind = (slash < 0 ? rest : rest.substring(0, slash)).toLowerCase(Locale.ROOT);
        String id = slash < 0 ? "" : QueryStringDecoder.decodeComponent(rest.substring(slash + 1));
        if (kind.equals("deck") && id.isEmpty()) {
            // /gemp-lotr/share/deck?id=<code>: the deck builder's links as they are made today
            var ids = new QueryStringDecoder(request.uri()).parameters().get("id");
            id = ids == null || ids.isEmpty() ? "" : ids.get(0);
        }
        if (!kind.equals("deck") && id.endsWith("/"))
            id = id.substring(0, id.length() - 1);

        SharePages.Page page = _pages.page(kind, id);
        if (page == null)
            throw new HttpProcessingException(404);

        String origin = origin(request);
        String html = SharePages.html(page, origin, origin + shareLink(kind, id));

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), "text/html; charset=UTF-8");
        headers.put(HttpHeaderNames.CACHE_CONTROL.toString(), CACHE_CONTROL);
        headers.put("X-Content-Type-Options", "nosniff");
        headers.put("Referrer-Policy", "no-referrer-when-downgrade");
        responseWriter.writeByteResponse(html.getBytes(StandardCharsets.UTF_8), headers);
    }

    /** The share link's canonical form (og:url), e.g. /gemp-lotr/share/errata/1_5. */
    static String shareLink(String kind, String id) {
        if (id.isEmpty())
            return PREFIX + kind;
        if (kind.equals("deck"))
            return PREFIX + "deck?id=" + java.net.URLEncoder.encode(id, StandardCharsets.UTF_8);
        return PREFIX + kind + "/" + id;
    }

    /**
     * The site's origin as the visitor reached it: X-Forwarded-Proto (set by the proxy in front) or https, and the Host
     * header when it is a plain host[:port]; otherwise {@link #DEFAULT_ORIGIN}.
     */
    static String origin(HttpRequest request) {
        String host = request.headers().get(HttpHeaderNames.HOST);
        if (host == null || !HOST.matcher(host.trim()).matches())
            return DEFAULT_ORIGIN;
        host = host.trim();
        String proto = request.headers().get("X-Forwarded-Proto");
        if (proto != null)
            proto = proto.split(",")[0].trim().toLowerCase(Locale.ROOT);
        if (!"http".equals(proto) && !"https".equals(proto)) {
            String bare = host.replaceFirst(":\\d+$", "");
            boolean local = bare.equals("localhost") || bare.startsWith("127.") || bare.startsWith("192.168.")
                    || bare.startsWith("10.");
            proto = local ? "http" : "https";
        }
        return proto + "://" + host;
    }
}
