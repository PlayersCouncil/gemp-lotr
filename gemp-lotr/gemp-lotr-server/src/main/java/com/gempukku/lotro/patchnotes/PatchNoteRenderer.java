package com.gempukku.lotro.patchnotes;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns the Markdown body of a patch note into the HTML the Patch Notes page shows.
 * <p>
 * Patch notes are written by the maintainers and shipped in the repo, so Markdown may carry inline HTML (the old
 * change log used coloured {@code <span>}s, links, a list and an image).  The output is still cleaned: only the tags
 * and attributes a note plausibly needs survive, {@code style} is cut down to colours and font styles, and scripts,
 * event handlers and {@code javascript:} links are removed, so a pasted snippet can never run code in the hall.
 * <p>
 * Relative image and link paths are relative to the note's own folder, falling back to the patchnotes folder when no
 * such file is there ({@link PathResolver}; a note straight in patchnotes/ writes {@code img/foo.png}); they are
 * rewritten to {@code patchnotes/<folder>/img/foo.png}, which resolves next to hall.html.  Links to other hall pages ({@code #format-pc_movie},
 * {@code #pc-errata}) are kept as they are; links to other sites open in a new tab.
 * <p>
 * Card links: {@code [[Cleaving Blow]]}, {@code [[1C5]]}, {@code [[1_5]]} or {@code [[1_5|custom text]]} name a card
 * (see {@link CardLookup}); each becomes {@code <span class="cardHint patchnote-card" value="1_5">} around the text,
 * which the page opens in the zoomable card display.  A reference that names no card, or several, is left as plain
 * text (the text without the brackets) and reported to the caller's problem list.  Card links inside a code span or a
 * Markdown link are left alone, so {@code `[[1_5]]`} shows the syntax itself.
 * <p>
 * Culture icons: {@code :isengard:} or {@code [isengard]} becomes the culture's icon ({@link CultureIcons}), in plain
 * text only (not in code, a link's text, an image's description or a card link).  So do the twilight icons,
 * {@code (1)} / {@code (X)} and {@code :1twilight:}.
 */
public class PatchNoteRenderer {
    /** Where the patch notes folder is served from, relative to hall.html. */
    public static final String WEB_FOLDER = "patchnotes/";

    private static final String CLEAN_BASE = "https://gemp.invalid/gemp-lotr/" + WEB_FOLDER;
    private static final Pattern SCHEME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]*:");
    private static final Set<String> STYLE_PROPERTIES = Set.of(
            "color", "background-color", "font-weight", "font-style", "text-decoration");
    private static final Pattern STYLE_VALUE = Pattern.compile("^[#a-zA-Z0-9(),.%\\s-]+$");
    /** [[reference]] or [[reference|text]] */
    public static final Pattern CARD_LINK = Pattern.compile("\\[\\[([^\\[\\]|\\n]+)(?:\\|([^\\[\\]\\n]+))?]]");
    public static final String CARD_LINK_CLASS = "cardHint patchnote-card";

    /**
     * Where a relative path written in a note points, relative to the patchnotes folder (see
     * {@link PatchNotesLibrary}: a note in a subfolder looks next to itself first).
     */
    @FunctionalInterface
    public interface PathResolver {
        /**
         * @param path a relative path as the note wrote it (no scheme, not starting with / or #), without a leading ./
         * @return the path relative to the patchnotes folder
         */
        String resolve(String path);

        /** a note straight in the patchnotes folder: paths are relative to it */
        PathResolver ROOT = path -> path;
    }

    private final Parser _parser;
    private final HtmlRenderer _renderer;
    /** for server announcements: a single line break is a line break, as in the hall's announcement popup */
    private final HtmlRenderer _lineBreakRenderer;
    private final Safelist _safelist;
    private final CardLookup _cards;

    /** A renderer without a card library: card links render as their plain text. */
    public PatchNoteRenderer() {
        this(null);
    }

    /** @param cards resolves {@code [[...]]} card links; null leaves them as plain text */
    public PatchNoteRenderer(CardLookup cards) {
        _cards = cards;
        List<Extension> extensions = List.of(StrikethroughExtension.create(), AutolinkExtension.create());
        _parser = Parser.builder().extensions(extensions).build();
        _renderer = HtmlRenderer.builder()
                .extensions(extensions)
                .sanitizeUrls(true)
                .build();
        _lineBreakRenderer = HtmlRenderer.builder()
                .extensions(extensions)
                .sanitizeUrls(true)
                .softbreak("<br>")
                .build();

        _safelist = Safelist.relaxed()
                .addTags("del", "s", "ins", "hr", "mark", "kbd", "figure", "figcaption", "details", "summary")
                .addAttributes("span", "style", "data-card", "data-culture", "title")
                .addAttributes("div", "style")
                .addAttributes("p", "style")
                .addAttributes("li", "style")
                .addAttributes("td", "style")
                .addAttributes("th", "style")
                .addAttributes("font", "color")
                .addProtocols("a", "href", "#")
                .preserveRelativeLinks(true);
    }

    /**
     * @return sanitized HTML for a Markdown body; an empty string for a null or blank one
     */
    public String render(String markdown) {
        return render(markdown, problem -> {
        });
    }

    /**
     * @param problems told about each card link that could not be resolved (the link then renders as plain text)
     * @return sanitized HTML for a Markdown body; an empty string for a null or blank one
     */
    public String render(String markdown, Consumer<String> problems) {
        return render(markdown, problems, false);
    }

    /**
     * @param problems   told about each card link that could not be resolved (the link then renders as plain text)
     * @param lineBreaks true renders a single line break as a line break ({@code <br>}), as the hall's announcement
     *                   popup does (server announcements in the feed are written for it); false joins the lines of a
     *                   paragraph, as Markdown does
     * @return sanitized HTML for a Markdown body; an empty string for a null or blank one
     */
    public String render(String markdown, Consumer<String> problems, boolean lineBreaks) {
        return render(markdown, problems, lineBreaks, PathResolver.ROOT);
    }

    /**
     * @param paths resolves the note's relative image and link paths (a note in a subfolder: next to it first)
     * @return sanitized HTML for a Markdown body; an empty string for a null or blank one
     */
    public String render(String markdown, Consumer<String> problems, boolean lineBreaks, PathResolver paths) {
        if (markdown == null || markdown.isBlank())
            return "";
        PathResolver resolver = paths == null ? PathResolver.ROOT : paths;

        Node document = _parser.parse(markdown);
        // the culture markers carry a one-off key, so a note's own HTML can't pass for one
        String cultureKey = Long.toHexString(java.util.concurrent.ThreadLocalRandom.current().nextLong());
        cultureIcons(document, cultureKey);
        linkCards(document, problems);
        String html = (lineBreaks ? _lineBreakRenderer : _renderer).render(document);

        Document.OutputSettings output = new Document.OutputSettings().prettyPrint(false);

        // relative paths are relative to the patchnotes folder
        Document dirty = Jsoup.parseBodyFragment(html);
        dirty.outputSettings(output);
        for (Element img : dirty.select("img[src]"))
            img.attr("src", rebase(img.attr("src"), resolver));
        for (Element a : dirty.select("a[href]"))
            a.attr("href", rebase(a.attr("href"), resolver));

        String cleanHtml = Jsoup.clean(dirty.body().html(), CLEAN_BASE, _safelist, output);

        Document clean = Jsoup.parseBodyFragment(cleanHtml);
        clean.outputSettings(output);
        for (Element styled : clean.select("[style]")) {
            String style = filterStyle(styled.attr("style"));
            if (style.isEmpty())
                styled.removeAttr("style");
            else
                styled.attr("style", style);
        }
        for (Element a : clean.select("a[href]")) {
            String href = a.attr("href");
            if (SCHEME.matcher(href).find() || href.startsWith("//")) {
                a.attr("target", "_blank");
                a.attr("rel", "noopener noreferrer");
            } else {
                a.removeAttr("target");
            }
        }
        for (Element img : clean.select("img"))
            img.attr("loading", "lazy");
        for (Element culture : clean.select("span[data-culture]")) {
            String[] marker = culture.attr("data-culture").split(":", 2);
            CultureIcons.Icon icon = marker.length == 2 && marker[1].equals(cultureKey) ? CultureIcons.byCode(marker[0]) : null;
            if (icon == null)
                culture.removeAttr("data-culture");     // written by hand in the note: an ordinary span
            else
                culture.after(CultureIcons.html(icon)).remove();
        }
        for (Element card : clean.select("span[data-card]")) {
            String id = card.attr("data-card");
            card.removeAttr("data-card");
            if (LibraryCardLookup.BLUEPRINT_ID.matcher(id).matches()) {
                card.attr("class", CARD_LINK_CLASS);
                card.attr("value", id);
            }
        }
        return clean.body().html();
    }

    // ---- culture icons ----

    /** Replaces the culture tokens in the document's plain text with markers the cleaning turns into icons. */
    private void cultureIcons(Node document, String key) {
        List<Text> texts = new ArrayList<>();
        document.accept(new AbstractVisitor() {
            @Override
            public void visit(Link link) {
                // a link's text stays the link's
            }

            @Override
            public void visit(Image image) {
                // and an image's description stays text
            }

            @Override
            public void visit(Text text) {
                String literal = text.getLiteral();
                if (literal.indexOf(':') >= 0 || literal.indexOf('[') >= 0 || literal.indexOf(']') >= 0
                        || literal.indexOf('(') >= 0)
                    texts.add(text);
            }
        });
        for (Text piece : texts) {
            if (piece.getParent() == null)
                continue;         // merged into an earlier one
            // the whole run of text around it (the parser splits text at brackets), so a token's neighbours count
            Text text = piece;
            while (text.getPrevious() instanceof Text previous)
                text = previous;
            mergeFollowingText(text);
            String literal = text.getLiteral();
            List<CultureIcons.Match> matches = CultureIcons.find(literal);
            if (matches.isEmpty())
                continue;
            int done = 0;
            for (CultureIcons.Match match : matches) {
                if (match.start() > done)
                    text.insertBefore(new Text(literal.substring(done, match.start())));
                text.insertBefore(html("<span data-culture=\"" + escapeAttribute(match.icon().code()) + ":" + key
                        + "\"></span>"));
                done = match.end();
            }
            if (done < literal.length())
                text.insertBefore(new Text(literal.substring(done)));
            text.unlink();
        }
    }

    // ---- [[card links]] ----

    /** Replaces the card links in the document's text (not in code, not inside links) with card spans. */
    private void linkCards(Node document, Consumer<String> problems) {
        List<Text> texts = new ArrayList<>();
        document.accept(new AbstractVisitor() {
            @Override
            public void visit(Link link) {
                // the text of a link stays the link's
            }

            @Override
            public void visit(Image image) {
                // and an image's description stays text
            }

            @Override
            public void visit(Text text) {
                if (text.getLiteral().indexOf('[') >= 0)
                    texts.add(text);
            }
        });
        for (Text text : texts) {
            if (text.getParent() == null)
                continue;         // merged into an earlier one
            mergeFollowingText(text);
            linkCards(text, problems);
        }
    }

    /** Joins the Text nodes that follow this one (a card link may have been parsed as several pieces). */
    private static void mergeFollowingText(Text text) {
        Node next = text.getNext();
        while (next instanceof Text following) {
            text.setLiteral(text.getLiteral() + following.getLiteral());
            Node after = following.getNext();
            following.unlink();
            next = after;
        }
    }

    private void linkCards(Text text, Consumer<String> problems) {
        String literal = text.getLiteral();
        Matcher matcher = CARD_LINK.matcher(literal);
        int done = 0;
        boolean changed = false;
        while (matcher.find()) {
            String reference = matcher.group(1).trim();
            String custom = matcher.group(2) == null ? null : matcher.group(2).trim();
            if (reference.isEmpty())
                continue;
            changed = true;
            if (matcher.start() > done)
                text.insertBefore(new Text(literal.substring(done, matcher.start())));
            done = matcher.end();

            CardLookup.Result card = _cards == null ? CardLookup.Result.failed("no card library to look it up in")
                    : _cards.resolve(reference);
            String label = custom != null && !custom.isEmpty() ? custom
                    : card.isFound() && LibraryCardLookup.BLUEPRINT_ID.matcher(reference).matches() ? card.name()
                    : reference;
            if (!card.isFound()) {
                problems.accept(matcher.group(0) + ": " + card.error());
                text.insertBefore(new Text(label));
                continue;
            }
            text.insertBefore(html("<span data-card=\"" + escapeAttribute(card.blueprintId()) + "\" title=\""
                    + escapeAttribute(card.name()) + "\">"));
            text.insertBefore(new Text(label));
            text.insertBefore(html("</span>"));
        }
        if (!changed)
            return;
        if (done < literal.length())
            text.insertBefore(new Text(literal.substring(done)));
        text.unlink();
    }

    private static HtmlInline html(String literal) {
        HtmlInline node = new HtmlInline();
        node.setLiteral(literal);
        return node;
    }

    private static String escapeAttribute(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    /** A path relative to the patchnotes folder -> relative to hall.html; anything else is left alone. */
    static String rebase(String url) {
        return rebase(url, PathResolver.ROOT);
    }

    /**
     * A relative path in a note -> relative to hall.html ({@code paths} says where in the patchnotes folder it is);
     * anything else (absolute, a #hall link, another site) is left alone.
     */
    public static String rebase(String url, PathResolver paths) {
        String trimmed = url.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("/") || SCHEME.matcher(trimmed).find())
            return trimmed;
        while (trimmed.startsWith("./"))
            trimmed = trimmed.substring(2);
        if (trimmed.startsWith(WEB_FOLDER))
            return trimmed;
        return WEB_FOLDER + (paths == null ? trimmed : paths.resolve(trimmed));
    }

    /** Keeps only colour and font-style declarations with plain values. */
    static String filterStyle(String style) {
        List<String> kept = new ArrayList<>();
        for (String declaration : style.split(";")) {
            int colon = declaration.indexOf(':');
            if (colon < 0)
                continue;
            String property = declaration.substring(0, colon).trim().toLowerCase(Locale.ROOT);
            String value = declaration.substring(colon + 1).trim();
            String lower = value.toLowerCase(Locale.ROOT);
            if (STYLE_PROPERTIES.contains(property) && STYLE_VALUE.matcher(value).matches()
                    && !lower.contains("url") && !lower.contains("expression"))
                kept.add(property + ":" + value);
        }
        return String.join(";", kept);
    }
}
