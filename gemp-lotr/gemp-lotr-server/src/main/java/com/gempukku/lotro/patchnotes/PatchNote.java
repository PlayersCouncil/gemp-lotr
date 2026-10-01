package com.gempukku.lotro.patchnotes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * One entry of the Patch Notes feed: an update from the patchnotes folder (its front matter and Markdown body), or a
 * past server announcement shown among them ({@link #KIND_ANNOUNCEMENT}, see {@link PatchNotesLibrary}).  The body is
 * rendered to HTML on first use and kept (the library drops the whole entry when its file or announcement changes).
 */
public class PatchNote {
    private static final Logger _log = LogManager.getLogger(PatchNote.class);

    public static final String KIND_NOTE = "note";
    public static final String KIND_ANNOUNCEMENT = "announcement";

    public static final String TAG_CARD_FIXES = "Card Fixes";
    public static final String TAG_PC_UPDATES = "PC Updates";
    public static final String TAG_USER_INTERFACE = "User Interface";
    public static final String TAG_ANNOUNCEMENT = "Announcement";
    /** The tags the Patch Notes page offers as filters, in the order of its filter bar (patchnotes/README.md). */
    public static final List<String> TAGS = List.of(TAG_CARD_FIXES, TAG_PC_UPDATES, TAG_USER_INTERFACE, TAG_ANNOUNCEMENT);

    private final String _slug;
    private final LocalDate _date;
    private final String _title;
    private final String _summary;
    private final int _order;
    private final String _markdown;
    private final List<String> _tags;
    private final String _kind;
    private final long _sequence;
    private volatile String _folder = "";
    private volatile PatchNoteRenderer.PathResolver _paths = PatchNoteRenderer.PathResolver.ROOT;
    private volatile String _html;

    public PatchNote(String slug, LocalDate date, String title, String summary, int order, String markdown) {
        this(slug, date, title, summary, order, markdown, List.of(), KIND_NOTE, 0);
    }

    /**
     * @param tags     canonical tags (see {@link #canonicalTag}); null counts as none
     * @param kind     {@link #KIND_NOTE} or {@link #KIND_ANNOUNCEMENT}
     * @param sequence orders entries with the same date and order: higher is newer (an announcement's start time)
     */
    public PatchNote(String slug, LocalDate date, String title, String summary, int order, String markdown,
                     List<String> tags, String kind, long sequence) {
        _slug = slug;
        _date = date;
        _title = title;
        _summary = summary;
        _order = order;
        _markdown = markdown;
        _tags = tags == null ? List.of() : List.copyOf(tags);
        _kind = kind == null ? KIND_NOTE : kind;
        _sequence = sequence;
    }

    /**
     * A tag as written in front matter, in its canonical spelling: one of {@link #TAGS} when it names one (ignoring
     * case, spaces, dashes and underscores: {@code card-fixes}, {@code card fixes} and {@code CardFixes} are all "Card
     * Fixes"), otherwise the text itself, trimmed.  null for a blank tag.
     */
    public static String canonicalTag(String tag) {
        if (tag == null || tag.isBlank())
            return null;
        String key = tagKey(tag);
        for (String known : TAGS) {
            if (tagKey(known).equals(key))
                return known;
        }
        return tag.trim();
    }

    public static boolean isKnownTag(String tag) {
        return tag != null && TAGS.contains(canonicalTag(tag));
    }

    private static String tagKey(String tag) {
        return tag.toLowerCase(Locale.ROOT).replaceAll("[\\s_-]+", "");
    }

    /** Canonical tags, in the order given, without blanks or repeats. */
    public static List<String> canonicalTags(List<String> tags) {
        List<String> result = new ArrayList<>();
        if (tags != null) {
            for (String tag : tags) {
                String canonical = canonicalTag(tag);
                if (canonical != null && !result.contains(canonical))
                    result.add(canonical);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public String getSlug() {
        return _slug;
    }

    public LocalDate getDate() {
        return _date;
    }

    /** null when the note has none (the page then heads it with its date) */
    public String getTitle() {
        return _title;
    }

    /** the one-paragraph blurb shown under the title; null when the note has none */
    public String getSummary() {
        return _summary;
    }

    /** orders several notes on the same day: higher is newer */
    public int getOrder() {
        return _order;
    }

    public String getMarkdown() {
        return _markdown;
    }

    /** canonical tags (see {@link #canonicalTag}); empty when the entry has none */
    public List<String> getTags() {
        return _tags;
    }

    /** whether the entry carries this tag (compared in canonical spelling) */
    public boolean hasTag(String tag) {
        String canonical = canonicalTag(tag);
        return canonical != null && _tags.contains(canonical);
    }

    /** {@link #KIND_NOTE} (a file in patchnotes/) or {@link #KIND_ANNOUNCEMENT} (a past server announcement) */
    public String getKind() {
        return _kind;
    }

    public boolean isAnnouncement() {
        return KIND_ANNOUNCEMENT.equals(_kind);
    }

    /** orders entries with the same date and order: higher is newer */
    public long getSequence() {
        return _sequence;
    }

    /**
     * Where the note's file is (set by {@link PatchNotesLibrary} as it reads it).
     *
     * @param folder its folder relative to the patchnotes folder ("" straight in it, else e.g. "2011" or "old/2011")
     * @param paths  how its relative image and link paths resolve
     */
    void locate(String folder, PatchNoteRenderer.PathResolver paths) {
        _folder = folder == null ? "" : folder;
        _paths = paths == null ? PatchNoteRenderer.PathResolver.ROOT : paths;
        _html = null;
    }

    /** the note's folder relative to the patchnotes folder: "" when it is straight in it */
    public String getFolder() {
        return _folder;
    }

    /** resolves the note's relative image and link paths to paths relative to the patchnotes folder */
    public PatchNoteRenderer.PathResolver getPaths() {
        return _paths;
    }

    public String getHtml(PatchNoteRenderer renderer) {
        String html = _html;
        if (html == null) {
            // a card link that names no card (or several) renders as plain text; say which, for the note's author
            html = renderer.render(_markdown, problem -> _log.warn("Patch note " + _slug + ": card link " + problem),
                    isAnnouncement(), _paths);
            _html = html;
        }
        return html;
    }

    /** {slug, kind, date, title, summary, tags} - what links to a note need */
    public Map<String, Object> toHeaderJson() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("slug", _slug);
        result.put("kind", _kind);
        result.put("date", _date.toString());
        result.put("title", _title);
        result.put("summary", _summary);
        result.put("tags", _tags);
        return result;
    }

    /** {slug, kind, date, title, summary, tags, summaryHtml, html} */
    public Map<String, Object> toJson(PatchNoteRenderer renderer) {
        Map<String, Object> result = toHeaderJson();
        // the summary with its culture icons ({@link CultureIcons}), escaped; null when it has none (shown as text)
        result.put("summaryHtml", _summary == null ? null : CultureIcons.plainTextHtml(_summary));
        result.put("html", getHtml(renderer));
        return result;
    }
}
