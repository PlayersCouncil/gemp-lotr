package com.gempukku.lotro.patchnotes;

import com.gempukku.lotro.collection.TransferDAO;
import com.gempukku.lotro.common.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Announces each new patch note in the hall's announcement popup.
 * <p>
 * Every minute (and once shortly after start-up) the patch notes library is checked (it re-reads its folder only
 * when a file changed).  A note whose announcement window has not ended yet and that has no announcement gets one:
 * <ul>
 *     <li>start: the note's date at 00:00 server time (UTC); end: {@link #WINDOW} later.  Notes whose window has
 *     already ended are never announced, so the old change log stays quiet.</li>
 *     <li>title: the note's title (else "Patch notes YYYY-MM-DD").</li>
 *     <li>content (Markdown, as an admin would write it): the {@link #marker(String) marker} line, the title again as a
 *     {@code # heading}, the note's {@code summary} (plain text; a note without one gets its first paragraph
 *     instead), its first image and a bold "Read the full patch notes here" link to {@code #patch-notes/<slug>}.
 *     Culture tokens ({@code :isengard:}, {@link CultureIcons}) in the copied text become the culture's icon as a
 *     Markdown image (hall.css sizes it to the text in the popup); so do twilight tokens ({@code (1)}, {@code (X)},
 *     {@code :1twilight:}).</li>
 * </ul>
 * The marker ({@code <!-- gemp-patchnote:<slug> -->}, the first line) is how an announcement is known to belong to a
 * note: it is the dedupe key (see {@link TransferDAO#addServerAnnouncementIfAbsent}, which checks and inserts under a
 * database lock, so several server processes on one database create it once), and the Patch Notes page leaves marked
 * announcements out of its list of past announcements.  It is removed before an announcement is shown
 * ({@link #stripMarker(String)}).
 * <p>
 * An admin who wants an auto announcement gone should end it (set its {@code until} to now) or edit its text, keeping
 * the marker line: a deleted one is created again the next time a server starts while the note is inside its window.
 */
public class PatchNoteAnnouncer {
    private static final Logger _log = LogManager.getLogger(PatchNoteAnnouncer.class);

    public static final Duration WINDOW = Duration.ofDays(14);
    public static final long DEFAULT_CHECK_INTERVAL_MS = 60_000;
    public static final long DEFAULT_FIRST_CHECK_DELAY_MS = 15_000;
    /** where hall.html is served from: images in an announcement need absolute paths */
    public static final String HALL_ROOT = "/gemp-lotr/";
    public static final String READ_MORE = "Read the full patch notes here";

    private static final String MARKER_START = "<!-- gemp-patchnote:";
    private static final String MARKER_END = " -->";
    /** the marker line at the start of an announcement's content */
    public static final Pattern MARKER = Pattern.compile("^\\s*<!--\\s*gemp-patchnote:([a-z0-9-]+)\\s*-->[ \\t]*(?:\\r?\\n)?");
    private static final int MAX_TITLE = 255;

    /** a line holding only images (Markdown or HTML) and/or HTML comments */
    private static final Pattern DECORATION_LINE = Pattern.compile(
            "^(?:\\s*(?:!\\[[^\\]]*]\\([^)]*\\)|<img\\b[^>]*>|<!--.*?-->))+\\s*$", Pattern.CASE_INSENSITIVE);
    /** [text](destination or ![alt](destination: the destination */
    private static final Pattern LINK_DESTINATION = Pattern.compile("(]\\(\\s*)(<[^>\\n]*>|[^)\\s]+)");
    private static final Pattern HTML_TAG = Pattern.compile("</?[A-Za-z][A-Za-z0-9-]*(?:\\s[^<>]*)?/?>");
    private static final Pattern HTML_COMMENT = Pattern.compile("<!--.*?-->", Pattern.DOTALL);
    private static final Pattern BR_TAG = Pattern.compile("<br\\s*/?>", Pattern.CASE_INSENSITIVE);
    private static final Pattern IMG_SRC = Pattern.compile("<img\\b[^>]*?\\bsrc\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s>]+))",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern IMG_ALT = Pattern.compile("\\balt\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)')", Pattern.CASE_INSENSITIVE);
    private static final Pattern SCHEME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]*:");

    private final PatchNotesLibrary _library;
    private final TransferDAO _transferDAO;
    private final CardLookup _cards;
    private final Supplier<ZonedDateTime> _clock;
    private final Parser _parser = Parser.builder().build();
    /** notes this process knows to have their announcement (created now, or found already there) */
    private final Set<String> _announced = ConcurrentHashMap.newKeySet();
    /** notes whose announcement could not be created: how many times in a row (the log says so now and then) */
    private final Map<String, Integer> _failures = new ConcurrentHashMap<>();
    private ScheduledExecutorService _scheduler;

    /** An announcement for a note: what {@link TransferDAO#addServerAnnouncementIfAbsent} is given. */
    public record Announcement(String marker, String title, String content, ZonedDateTime start, ZonedDateTime until) {
    }

    /** @param cards resolves {@code [[card links]]} in the copied paragraph to card names; null leaves their text */
    public PatchNoteAnnouncer(PatchNotesLibrary library, TransferDAO transferDAO, CardLookup cards) {
        this(library, transferDAO, cards, DateUtils::Now);
    }

    public PatchNoteAnnouncer(PatchNotesLibrary library, TransferDAO transferDAO, CardLookup cards,
                              Supplier<ZonedDateTime> clock) {
        _library = library;
        _transferDAO = transferDAO;
        _cards = cards;
        _clock = clock;
    }

    // ---- running ----

    /** Checks once after {@code firstDelayMs}, then every {@code intervalMs}, on a daemon thread. */
    public synchronized void start(long firstDelayMs, long intervalMs) {
        if (_scheduler != null)
            return;
        _scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "patch-note-announcer");
            thread.setDaemon(true);
            return thread;
        });
        _scheduler.scheduleWithFixedDelay(this::checkSafely, firstDelayMs, intervalMs, TimeUnit.MILLISECONDS);
    }

    public synchronized void stop() {
        if (_scheduler != null)
            _scheduler.shutdownNow();
        _scheduler = null;
    }

    private void checkSafely() {
        try {
            check();
        } catch (Throwable exp) {
            _log.warn("Patch note announcements: check failed, trying again later", exp);
        }
    }

    /**
     * Creates the missing announcements of the notes whose window has not ended.
     *
     * @return how many were created
     */
    public synchronized int check() {
        ZonedDateTime now = _clock.get();
        int created = 0;
        List<PatchNote> inWindow = new ArrayList<>();
        for (PatchNote note : _library.getNotes()) {
            if (!start(note).plus(WINDOW).isAfter(now))
                break;      // newest first: every note from here on is older
            inWindow.add(note);
        }
        // oldest first, so a newer note always gets the higher id: the hall shows the running announcement with the
        // latest start, ties going to the higher id, so of two notes on the same day the one listed first (higher
        // `order`) is the one players see
        Collections.reverse(inWindow);
        for (PatchNote note : inWindow) {
            if (_announced.contains(note.getSlug()))
                continue;
            try {
                Announcement announcement = announcementFor(note);
                int id = _transferDAO.addServerAnnouncementIfAbsent(announcement.marker(), announcement.title(),
                        announcement.content(), announcement.start(), announcement.until());
                _announced.add(note.getSlug());
                _failures.remove(note.getSlug());
                if (id > 0) {
                    created++;
                    _log.info("Patch note " + note.getSlug() + " announced (announcement " + id + ", "
                            + DateUtils.FormatDateTime(announcement.start()) + " to "
                            + DateUtils.FormatDateTime(announcement.until()) + ")");
                }
            } catch (RuntimeException exp) {
                int failures = _failures.merge(note.getSlug(), 1, Integer::sum);
                if (failures == 1 || failures % 60 == 0)
                    _log.warn("Patch note " + note.getSlug() + ": could not create its announcement (" + failures
                            + " tries), trying again later", exp);
            }
        }
        return created;
    }

    // ---- the announcement ----

    public static ZonedDateTime start(PatchNote note) {
        return DateUtils.ParseDate(note.getDate());
    }

    public static String marker(String slug) {
        return MARKER_START + slug + MARKER_END;
    }

    /** @return the slug the content's marker names, or null when it has none */
    public static String markedSlug(String content) {
        if (content == null)
            return null;
        Matcher matcher = MARKER.matcher(content);
        return matcher.find() ? matcher.group(1) : null;
    }

    /** The content without its marker line (unchanged when it has none): what an announcement shows. */
    public static String stripMarker(String content) {
        if (content == null)
            return null;
        Matcher matcher = MARKER.matcher(content);
        return matcher.find() ? content.substring(matcher.end()) : content;
    }

    public Announcement announcementFor(PatchNote note) {
        ZonedDateTime start = start(note);
        return new Announcement(marker(note.getSlug()), title(note), basicPlane(content(note)), start, start.plus(WINDOW));
    }

    /** The popup's title: the note's title (else "Patch notes YYYY-MM-DD"), cut to the column's 255 characters. */
    public static String title(PatchNote note) {
        String title = basicPlane(note.getTitle() != null ? note.getTitle() : "Patch notes " + note.getDate());
        title = title.replaceAll("[\\r\\n]+", " ").trim();
        if (title.length() > MAX_TITLE)
            title = title.substring(0, MAX_TITLE - 3) + "...";
        return title;
    }

    private static final Pattern HEADING_PUNCTUATION = Pattern.compile("([\\\\*`\\[\\]<>|~#&!])");

    /**
     * The title as the text of a {@code # heading}: on one line, with the Markdown punctuation that could turn it
     * into something else escaped (a trailing {@code #} would otherwise be dropped as a closing sequence).
     * Underscores are left alone: the announcement renderer escapes them itself.
     */
    static String headingText(String title) {
        String text = title == null ? "" : title.replaceAll("\\s+", " ").trim();
        return HEADING_PUNCTUATION.matcher(text).replaceAll("\\\\$1");
    }

    /**
     * Drops characters outside the Basic Multilingual Plane (emoji): the announcements table is utf8 (3-byte), and
     * one such character would make every insert of the announcement fail.
     */
    static String basicPlane(String text) {
        if (text == null || text.codePoints().allMatch(cp -> cp <= 0xFFFF))
            return text;
        StringBuilder sb = new StringBuilder(text.length());
        text.codePoints().filter(cp -> cp <= 0xFFFF).forEach(sb::appendCodePoint);
        return sb.toString();
    }

    /**
     * The announcement's Markdown: marker line, the note's summary (its first paragraph when it has none), first
     * image, the link to the note.
     */
    public String content(PatchNote note) {
        StringBuilder sb = new StringBuilder();
        sb.append(marker(note.getSlug())).append('\n');
        sb.append("# ").append(headingText(title(note))).append("\n\n");

        String paragraph = note.getSummary() != null && !note.getSummary().isBlank()
                ? summaryForAnnouncement(note.getSummary())
                : paragraphForAnnouncement(firstParagraph(note.getMarkdown()), note.getPaths());
        if (!paragraph.isEmpty())
            sb.append(paragraph).append("\n\n");

        FirstImage image = firstImage(note.getMarkdown());
        if (image != null) {
            String url = absolute(image.url(), note.getPaths());
            if (!paragraph.contains("](" + url + ")") && !paragraph.contains("](" + url + " ")) {
                String alt = image.alt() == null ? "" : image.alt().replaceAll("[\\[\\]\\n\\r]", " ").trim();
                sb.append("![").append(alt).append("](").append(destination(url)).append(")\n\n");
            }
        }

        sb.append("**[").append(READ_MORE).append("](#patch-notes/").append(note.getSlug()).append(")**\n");
        return sb.toString();
    }

    /**
     * The note's first paragraph: its first block of lines, up to the first blank line (so a heading followed by a
     * bullet list, with no blank line between them, counts as one).  Leading blank lines, and lines holding only
     * images or comments, are skipped.
     */
    public static String firstParagraph(String markdown) {
        if (markdown == null)
            return "";
        String[] lines = markdown.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        int i = 0;
        while (i < lines.length && (lines[i].isBlank() || DECORATION_LINE.matcher(lines[i]).matches()))
            i++;
        List<String> block = new ArrayList<>();
        for (; i < lines.length && !lines[i].isBlank(); i++)
            block.add(lines[i]);
        return String.join("\n", block).strip();
    }

    /**
     * The paragraph as the announcement shows it (announcements are Markdown without HTML): card links become the
     * card's name (or their own text), HTML tags are dropped, and paths relative to the patch notes folder become
     * absolute.
     */
    public String paragraphForAnnouncement(String paragraph) {
        return paragraphForAnnouncement(paragraph, PatchNoteRenderer.PathResolver.ROOT);
    }

    /** @param paths resolves the note's relative paths (a note in a subfolder: next to it first) */
    public String paragraphForAnnouncement(String paragraph, PatchNoteRenderer.PathResolver paths) {
        if (paragraph == null || paragraph.isEmpty())
            return "";
        String text = plainCardLinks(cultureIconsInMarkdown(paragraph));
        text = HTML_COMMENT.matcher(text).replaceAll("");
        text = BR_TAG.matcher(text).replaceAll(" ");
        text = HTML_TAG.matcher(text).replaceAll("");
        Matcher matcher = LINK_DESTINATION.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String url = matcher.group(2);
            boolean pointy = url.startsWith("<") && url.endsWith(">");
            String bare = pointy ? url.substring(1, url.length() - 1) : url;
            String rewritten = pointy ? "<" + absolute(bare, paths) + ">" : absolute(bare, paths);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(1) + rewritten));
        }
        matcher.appendTail(sb);
        return sb.toString().strip();
    }

    private static final Pattern MARKDOWN_PUNCTUATION = Pattern.compile("([\\\\*`\\[\\]<>|~])");
    private static final Pattern LINE_START_MARKER = Pattern.compile("(?m)^(\\s*)([#>+=-])");
    private static final Pattern LINE_START_NUMBER = Pattern.compile("(?m)^(\\s*\\d+)([.)])");

    /**
     * The summary as the announcement shows it: the front matter's summary is plain text (the Patch Notes page shows
     * it as it is), so Markdown punctuation is escaped and a line never starts a heading, list or quote; card links
     * become the card's name (or their own text), as in {@link #paragraphForAnnouncement}.  Underscores are left
     * alone: the announcement renderer escapes them itself.
     */
    public String summaryForAnnouncement(String summary) {
        if (summary == null || summary.isBlank())
            return "";
        String plain = summary.strip();
        StringBuilder sb = new StringBuilder();
        int done = 0;
        for (CultureIcons.Match match : cultureTokens(plain, URL)) {
            sb.append(escapeSummary(plain.substring(done, match.start()))).append(cultureImage(match.icon()));
            done = match.end();
        }
        sb.append(escapeSummary(plain.substring(done)));
        String text = LINE_START_MARKER.matcher(sb.toString()).replaceAll("$1\\\\$2");
        return LINE_START_NUMBER.matcher(text).replaceAll("$1\\\\$2");
    }

    private String escapeSummary(String text) {
        return MARKDOWN_PUNCTUATION.matcher(plainCardLinks(text)).replaceAll("\\\\$1");
    }

    // ---- culture icons ----

    private static final Pattern URL = Pattern.compile("(?i)\\b(?:https?|ftp)://\\S+|\\bwww\\.\\S+");
    /** what a culture token in Markdown must not be inside: code, card links, links and images, autolinks and HTML, URLs */
    private static final Pattern MARKDOWN_PROTECTED = Pattern.compile(
            "(`+)[\\s\\S]*?\\1"
                    + "|\\[\\[[^\\]\\n]*]]"
                    + "|!?\\[[^\\]\\n]*]\\([^)\\n]*\\)"
                    + "|!?\\[[^\\]\\n]*]\\[[^\\]\\n]*]"
                    + "|<[^>\\n]*>"
                    + "|(?i)\\b(?:https?|ftp)://\\S+|\\bwww\\.\\S+");

    /** The culture's icon as a Markdown image the popup can show: absolute path, the name as alt text and tooltip. */
    public static String cultureImage(CultureIcons.Icon icon) {
        return "![" + icon.name() + "](" + HALL_ROOT + icon.src() + " \"" + icon.name() + "\")";
    }

    /** the culture tokens of a text, leaving out those inside what {@code protectedParts} matches */
    private static List<CultureIcons.Match> cultureTokens(String text, Pattern protectedParts) {
        List<CultureIcons.Match> tokens = CultureIcons.find(text);
        if (tokens.isEmpty())
            return tokens;
        List<int[]> ranges = new ArrayList<>();
        Matcher matcher = protectedParts.matcher(text);
        while (matcher.find())
            ranges.add(new int[]{matcher.start(), matcher.end()});
        List<CultureIcons.Match> result = new ArrayList<>();
        for (CultureIcons.Match token : tokens) {
            boolean inside = false;
            for (int[] range : ranges)
                inside |= token.start() < range[1] && token.end() > range[0];
            if (!inside)
                result.add(token);
        }
        return result;
    }

    /** Markdown with its culture tokens (outside code, links and the like) as {@link #cultureImage} images. */
    public static String cultureIconsInMarkdown(String markdown) {
        List<CultureIcons.Match> tokens = cultureTokens(markdown, MARKDOWN_PROTECTED);
        if (tokens.isEmpty())
            return markdown;
        StringBuilder sb = new StringBuilder();
        int done = 0;
        for (CultureIcons.Match token : tokens) {
            sb.append(markdown, done, token.start()).append(cultureImage(token.icon()));
            done = token.end();
        }
        return sb.append(markdown.substring(done)).toString();
    }

    /** {@code [[1_5]]} -> Cleaving Blow, {@code [[Cleaving Blow]]} -> Cleaving Blow, {@code [[51_5|the errata]]} -> the errata */
    public String plainCardLinks(String text) {
        Matcher matcher = PatchNoteRenderer.CARD_LINK.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String reference = matcher.group(1).trim();
            String custom = matcher.group(2) == null ? null : matcher.group(2).trim();
            String label;
            if (custom != null && !custom.isEmpty()) {
                label = custom;
            } else if (reference.isEmpty()) {
                label = matcher.group(0);
            } else if (_cards != null && LibraryCardLookup.BLUEPRINT_ID.matcher(reference).matches()) {
                CardLookup.Result card = _cards.resolve(reference);
                label = card.isFound() && card.name() != null ? card.name() : reference;
            } else {
                label = reference;
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(label));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    record FirstImage(String url, String alt) {
    }

    /** The first image anywhere in the note (Markdown or an HTML {@code <img>}), not counting code; null when none. */
    FirstImage firstImage(String markdown) {
        if (markdown == null || markdown.isBlank())
            return null;
        Node document = _parser.parse(markdown);
        FirstImage[] found = new FirstImage[1];
        document.accept(new AbstractVisitor() {
            @Override
            public void visit(Image image) {
                if (found[0] == null && image.getDestination() != null && !image.getDestination().isBlank())
                    found[0] = new FirstImage(image.getDestination(), altText(image));
            }

            @Override
            public void visit(HtmlInline html) {
                if (found[0] == null)
                    found[0] = htmlImage(html.getLiteral());
            }

            @Override
            public void visit(HtmlBlock html) {
                if (found[0] == null)
                    found[0] = htmlImage(html.getLiteral());
            }

            @Override
            protected void visitChildren(Node parent) {
                if (found[0] == null)
                    super.visitChildren(parent);
            }
        });
        return found[0];
    }

    private static String altText(Image image) {
        StringBuilder sb = new StringBuilder();
        image.accept(new AbstractVisitor() {
            @Override
            public void visit(Text text) {
                sb.append(text.getLiteral());
            }
        });
        return sb.toString();
    }

    private static FirstImage htmlImage(String html) {
        if (html == null)
            return null;
        Matcher src = IMG_SRC.matcher(html);
        if (!src.find())
            return null;
        String url = src.group(1) != null ? src.group(1) : src.group(2) != null ? src.group(2) : src.group(3);
        if (url == null || url.isBlank())
            return null;
        String tag = html.substring(src.start());
        int close = tag.indexOf('>');
        Matcher alt = IMG_ALT.matcher(close < 0 ? tag : tag.substring(0, close));
        String altText = alt.find() ? (alt.group(1) != null ? alt.group(1) : alt.group(2)) : "";
        return new FirstImage(url.trim(), altText);
    }

    /** A path relative to the patch notes folder -> an absolute path on this site; anything else is left alone. */
    static String absolute(String url) {
        return absolute(url, PatchNoteRenderer.PathResolver.ROOT);
    }

    /** A relative path in a note -> an absolute path on this site (next to the note first); anything else is left alone. */
    static String absolute(String url, PatchNoteRenderer.PathResolver paths) {
        String trimmed = url == null ? "" : url.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("/") || SCHEME.matcher(trimmed).find())
            return trimmed;
        return HALL_ROOT + PatchNoteRenderer.rebase(trimmed, paths);
    }

    private static String destination(String url) {
        return url.matches(".*[\\s()<>].*") ? "<" + url.replace("<", "%3C").replace(">", "%3E") + ">" : url;
    }
}
