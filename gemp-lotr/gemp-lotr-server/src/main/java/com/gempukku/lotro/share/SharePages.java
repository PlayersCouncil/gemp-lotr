package com.gempukku.lotro.share;

import com.gempukku.lotro.common.CardInfo;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.formats.ErrataCatalog;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.patchnotes.CultureIcons;
import com.gempukku.lotro.patchnotes.PatchNote;
import com.gempukku.lotro.patchnotes.PatchNotesLibrary;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Share links: {@code /gemp-lotr/share/<kind>/<id>} answers with a tiny page that carries Open Graph / Twitter card
 * tags (so a link pasted into Discord, Facebook, a forum... previews with a title, a description and a picture) and
 * sends a person straight on to the real page (meta refresh plus script; they never see it).
 * <pre>
 *   kind         id                   goes to                                       preview picture
 *   patch-notes  [slug]               hall.html#patch-notes[/slug]                   the note's first screenshot
 *   errata       card id (1_5, 51_5)  hall.html#errata-&lt;id&gt;                       the errata's card image
 *   format       format code          hall.html#format-&lt;code&gt;                     the site picture
 *   card         blueprint id         hall.html#card-&lt;id&gt; (the card display)       the card image
 *   deck         share code           /gemp-lotr-server/deck/html?id=&lt;code&gt;        the Ring-bearer's image
 * </pre>
 * The deck kind takes the code the deck builder's "share" button makes today (base64 of {@code owner|deck name},
 * {@link LotroDeck#GenerateDeckSharingURL}), so the old {@code /share/deck?id=<code>} links can be pointed here.
 * <p>
 * A malformed id is no page ({@link #page} returns null: the handler answers 404).  A well-formed id that names
 * nothing (an old note, a removed format) still gets a page with the site's generic preview that goes on to the hall
 * link, where the page itself says what is missing.
 */
public class SharePages {
    public static final String SITE_NAME = "GEMP";
    /** relative to the web root (/gemp-lotr/), as in hall.html's own og:image */
    public static final String DEFAULT_IMAGE = "images/splash.jpg";
    public static final String WEB_PATH = "/gemp-lotr/";
    public static final int DESCRIPTION_LENGTH = 200;

    private static final Pattern SLUG = PatchNotesLibrary.SLUG;
    private static final Pattern BLUEPRINT_ID = Pattern.compile("^\\d{1,3}_\\d{1,4}$");
    private static final Pattern FORMAT_CODE = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");
    private static final Pattern DECK_CODE = Pattern.compile("^[A-Za-z0-9+/=_-]{4,1024}$");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    /** Finds a player's deck by the owner and name a deck share code holds; null when there is none. */
    public interface DeckSource {
        LotroDeck find(String owner, String deckName);
    }

    /**
     * @param title       og:title
     * @param description og:description (plain text)
     * @param image       og:image: an absolute URL, or a path relative to the web root
     * @param target      where a person goes: a path relative to the web root (hall.html#...) or an absolute path
     * @param largeImage  true for a wide picture (a screenshot, the site picture): twitter:card summary_large_image
     */
    public record Page(String title, String description, String image, String target, boolean largeImage) {
    }

    private final PatchNotesLibrary _patchNotes;
    private final LotroCardBlueprintLibrary _cards;
    private final LotroFormatLibrary _formats;
    private final CardImages _cardImages;
    private final DeckSource _decks;

    public SharePages(PatchNotesLibrary patchNotes, LotroCardBlueprintLibrary cards, LotroFormatLibrary formats,
                      CardImages cardImages, DeckSource decks) {
        _patchNotes = patchNotes;
        _cards = cards;
        _formats = formats;
        _cardImages = cardImages;
        _decks = decks;
    }

    /**
     * @param kind patch-notes, errata, format, card or deck
     * @param id   what to show (already URL-decoded); may be empty for patch-notes (the newest)
     * @return the page, or null when the kind is unknown or the id malformed
     */
    public Page page(String kind, String id) {
        if (kind == null)
            return null;
        id = id == null ? "" : id.trim();
        return switch (kind) {
            case "patch-notes" -> patchNotePage(id.toLowerCase(Locale.ROOT));
            case "errata" -> BLUEPRINT_ID.matcher(id).matches() ? errataPage(id) : null;
            case "format" -> FORMAT_CODE.matcher(id).matches() ? formatPage(id) : null;
            case "card" -> BLUEPRINT_ID.matcher(id).matches() ? cardPage(id) : null;
            case "deck" -> deckPage(id);
            default -> null;
        };
    }

    // ---- the kinds ----

    private Page patchNotePage(String slug) {
        if (slug.isEmpty() || _patchNotes == null)
            return new Page("GEMP patch notes", "What changed on GEMP, newest first.", DEFAULT_IMAGE,
                    "hall.html#patch-notes", true);
        if (!SLUG.matcher(slug).matches())
            return null;
        String target = "hall.html#patch-notes/" + slug;
        PatchNote note = _patchNotes.get(slug);
        if (note == null)
            return new Page("GEMP patch notes", "What changed on GEMP, newest first.", DEFAULT_IMAGE, target, true);

        // YYYY-MM-DD, as the Patch Notes page shows it (an international audience)
        String date = note.getDate().toString();
        String title = note.getTitle() != null ? note.getTitle()
                : (note.isAnnouncement() ? "GEMP announcement of " : "GEMP update of ") + date;
        Document html = Jsoup.parseBodyFragment(note.getHtml(_patchNotes.getRenderer()));
        // culture icons read as their names in the preview's text (and are not its picture)
        for (Element icon : html.select("img.patchnote-culture"))
            icon.replaceWith(new org.jsoup.nodes.TextNode(icon.attr("alt")));
        String description = note.getSummary() == null ? null : CultureIcons.plainText(note.getSummary());
        if (description == null || description.isBlank()) {
            StringBuilder text = new StringBuilder();
            for (Element block : html.select("p, li")) {
                if (block.parents().is("li"))
                    continue;           // a nested list is part of its item's text
                text.append(block.text()).append(' ');
                if (text.length() > DESCRIPTION_LENGTH)
                    break;
            }
            description = text.toString();
        }
        description = shorten(description);
        description = note.getTitle() != null ? date + ": " + description : description;
        if (description.isBlank())
            description = "What changed on GEMP on " + date + ".";

        Element img = html.selectFirst("img[src]");
        String image = img != null ? img.attr("src") : null;
        return new Page(title, description.trim(), image == null || image.isBlank() ? DEFAULT_IMAGE : image, target,
                true);
    }

    private Page errataPage(String id) {
        String target = "hall.html#errata-" + id;
        String base = ErrataCatalog.errataBase(id);
        String errataId = base != null ? id : errataOf(id);
        String originalId = base != null ? base : id;
        LotroCardBlueprint errata = blueprint(errataId);
        LotroCardBlueprint original = blueprint(originalId);
        LotroCardBlueprint card = errata != null ? errata : original;
        if (card == null)
            return new Page("PC Errata", "The Players Council's card errata on GEMP, old and new side by side.",
                    DEFAULT_IMAGE, target, true);

        String name = card.getFullName();
        String collectorInfo = collectorInfo(original != null ? original : card);
        String description = "Errata of " + name + (collectorInfo != null ? " (" + collectorInfo + ")" : "")
                + ", old and new side by side.";
        String text = plainGameText(card);
        if (!text.isEmpty())
            description = shorten(description + " Now: " + text);
        String image = errata != null ? _cardImages.imageUrl(errataId, errata) : null;
        if (image == null && original != null)
            image = _cardImages.imageUrl(originalId, original);
        return new Page("PC Errata: " + name, description, image == null ? DEFAULT_IMAGE : image, target, image == null);
    }

    private Page formatPage(String code) {
        String target = "hall.html#format-" + code;
        LotroFormat format = _formats == null ? null : _formats.getFormat(code);
        if (format == null)
            return new Page("Format Definitions", "The sets, bans and errata of every GEMP format.", DEFAULT_IMAGE,
                    target, true);
        return new Page(format.getName() + " — Format Definitions",
                "The sets, card bans, restrictions and errata of " + format.getName() + " on GEMP.", DEFAULT_IMAGE,
                target, true);
    }

    private Page cardPage(String id) {
        String target = "hall.html#card-" + id;
        LotroCardBlueprint card = blueprint(id);
        if (card == null)
            return new Page("A GEMP card", "Lord of the Rings TCG cards on GEMP.", DEFAULT_IMAGE, target, true);
        String collectorInfo = collectorInfo(card);
        StringBuilder description = new StringBuilder();
        if (collectorInfo != null)
            description.append(collectorInfo).append(". ");
        description.append(plainGameText(card));
        String image = _cardImages.imageUrl(id, card);
        return new Page(card.getFullName(), shorten(description.toString()), image == null ? DEFAULT_IMAGE : image,
                target, image == null);
    }

    private Page deckPage(String code) {
        if (!DECK_CODE.matcher(code).matches())
            return null;
        String owner;
        String deckName;
        try {
            String decoded = new String(Base64.getDecoder().decode(code), StandardCharsets.UTF_8);
            int bar = decoded.indexOf('|');
            if (bar <= 0 || bar == decoded.length() - 1 || decoded.indexOf('|', bar + 1) >= 0)
                return null;
            owner = decoded.substring(0, bar);
            deckName = decoded.substring(bar + 1);
        } catch (IllegalArgumentException exp) {
            return null;
        }
        String target = "/gemp-lotr-server/deck/html?id=" + URLEncoder.encode(code, StandardCharsets.UTF_8);
        LotroDeck deck = _decks == null ? null : _decks.find(owner, deckName);
        if (deck == null)
            return new Page("A GEMP deck", "A Lord of the Rings TCG deck shared from GEMP.", DEFAULT_IMAGE, target, true);

        String format = deck.getTargetFormat();
        LotroFormat lotroFormat = format == null || _formats == null ? null : _formats.getFormat(format);
        if (lotroFormat == null && format != null && _formats != null)
            lotroFormat = _formats.getFormatByName(format);
        StringBuilder description = new StringBuilder("A deck by ").append(owner);
        if (lotroFormat != null)
            description.append(" for ").append(lotroFormat.getName());
        else if (format != null && !format.isBlank())
            description.append(" for ").append(format);
        LotroCardBlueprint ringBearer = blueprint(deck.getRingBearer());
        if (ringBearer != null)
            description.append(", with ").append(ringBearer.getFullName()).append(" as Ring-bearer");
        description.append('.');
        int cards = deck.getDrawDeckCards() == null ? 0 : deck.getDrawDeckCards().size();
        if (cards > 0)
            description.append(' ').append(cards).append(" cards in the draw deck.");
        String image = ringBearer == null ? null : _cardImages.imageUrl(deck.getRingBearer(), ringBearer);
        return new Page(deckName, description.toString(), image == null ? DEFAULT_IMAGE : image, target, image == null);
    }

    // ---- helpers ----

    private LotroCardBlueprint blueprint(String id) {
        if (id == null || _cards == null)
            return null;
        try {
            return _cards.getLotroCardBlueprint(id);
        } catch (CardNotFoundException | RuntimeException exp) {
            return null;
        }
    }

    /** The PC errata of an original card (5x_n before a playtest 7x_n), or null when it has none. */
    private String errataOf(String originalId) {
        if (_cards == null)
            return null;
        String[] parts = originalId.split("_");
        int set = Integer.parseInt(parts[0]);
        int[] candidates = set <= 19 ? new int[]{set + 50, set + 70} : set >= 100 && set <= 149 ? new int[]{set + 50} : new int[0];
        for (int candidate : candidates) {
            String id = candidate + "_" + parts[1];
            if (_cards.getBaseCards().containsKey(id))
                return id;
        }
        return null;
    }

    private static String collectorInfo(LotroCardBlueprint card) {
        CardInfo info = card == null ? null : card.getCardInfo();
        return info == null || info.collInfo == null || info.collInfo.isBlank() ? null : info.collInfo.trim();
    }

    private static String plainGameText(LotroCardBlueprint card) {
        String text = card.getGameText();
        if (text == null || text.isBlank())
            return "";
        return WHITESPACE.matcher(Jsoup.parse(text).text()).replaceAll(" ").trim();
    }

    /** Collapses whitespace and cuts at a word to at most {@link #DESCRIPTION_LENGTH} characters. */
    static String shorten(String text) {
        if (text == null)
            return "";
        String flat = WHITESPACE.matcher(text).replaceAll(" ").trim();
        if (flat.length() <= DESCRIPTION_LENGTH)
            return flat;
        int cut = flat.lastIndexOf(' ', DESCRIPTION_LENGTH - 1);
        if (cut < DESCRIPTION_LENGTH / 2)
            cut = DESCRIPTION_LENGTH - 1;
        return flat.substring(0, cut).replaceAll("[\\s,;:.—-]+$", "") + "…";
    }

    // ---- the page ----

    /**
     * @param page     what to show
     * @param origin   the site's origin ("https://play.lotrtcgpc.net"), for the absolute URLs the tags need
     * @param shareUrl the share link itself (og:url), absolute
     * @return the HTML: the preview tags, and an immediate redirect to the page's target
     */
    public static String html(Page page, String origin, String shareUrl) {
        String target = absolutePath(page.target());
        String image = absoluteUrl(page.image(), origin);
        StringBuilder html = new StringBuilder(1500);
        html.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n<meta charset=\"utf-8\">\n");
        html.append("<title>").append(escape(page.title())).append(" · ").append(SITE_NAME).append("</title>\n");
        meta(html, "name", "description", page.description());
        meta(html, "name", "robots", "noindex");
        meta(html, "property", "og:site_name", SITE_NAME);
        meta(html, "property", "og:type", "website");
        meta(html, "property", "og:title", page.title());
        meta(html, "property", "og:description", page.description());
        meta(html, "property", "og:url", shareUrl);
        meta(html, "property", "og:image", image);
        meta(html, "name", "twitter:card", page.largeImage() ? "summary_large_image" : "summary");
        meta(html, "name", "twitter:title", page.title());
        meta(html, "name", "twitter:description", page.description());
        meta(html, "name", "twitter:image", image);
        html.append("<meta http-equiv=\"refresh\" content=\"0; url=").append(escape(target)).append("\">\n");
        html.append("<script>window.location.replace(").append(jsString(target)).append(");</script>\n");
        html.append("</head>\n<body>\n<p><a href=\"").append(escape(target)).append("\">")
                .append(escape(page.title())).append("</a> on ").append(SITE_NAME).append("</p>\n</body>\n</html>\n");
        return html.toString();
    }

    private static void meta(StringBuilder html, String attribute, String name, String content) {
        html.append("<meta ").append(attribute).append("=\"").append(name).append("\" content=\"")
                .append(escape(content)).append("\">\n");
    }

    /** "hall.html#x" -> "/gemp-lotr/hall.html#x"; an absolute path stays as it is */
    static String absolutePath(String target) {
        return target.startsWith("/") ? target : WEB_PATH + target;
    }

    static String absoluteUrl(String url, String origin) {
        if (url.startsWith("https://") || url.startsWith("http://"))
            return url;
        return origin + absolutePath(url);
    }

    public static String escape(String text) {
        if (text == null)
            return "";
        StringBuilder sb = new StringBuilder(text.length() + 16);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /** A JavaScript string literal that is also safe inside a script element. */
    static String jsString(String text) {
        StringBuilder sb = new StringBuilder(text.length() + 8).append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"' || c == '\\' || c == '<' || c == '>' || c == '&' || c == '\'' || c < 0x20
                    || c == ' ' || c == ' ')
                sb.append(String.format("\\u%04x", (int) c));
            else
                sb.append(c);
        }
        return sb.append('"').toString();
    }
}
