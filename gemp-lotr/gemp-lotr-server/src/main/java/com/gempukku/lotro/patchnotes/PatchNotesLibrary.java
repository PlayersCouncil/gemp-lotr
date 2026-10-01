package com.gempukku.lotro.patchnotes;

import com.gempukku.lotro.cache.Cached;
import com.gempukku.lotro.common.DBDefs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The patch notes shipped in the web root's {@code patchnotes/} folder: one {@code YYYY-MM-DD[-slug].md} file per
 * update (format: {@code patchnotes/README.md}), in the folder itself or in any subfolder (e.g. {@code 2011/}), up to
 * {@link #MAX_DEPTH} deep.  A note's name (its link) is its file name without .md, whatever folder it is in; two files
 * with the same name: the shallower one (then the first by path) wins and the other is skipped with a warning.  Other
 * files (README.md at any depth, images) and {@code img/} folders are ignored.  A note's relative image and link paths
 * resolve next to the note first, then from the patchnotes folder ({@link #pathResolver}).
 * <p>
 * The folder is read on first use and re-read when it changes: at most every {@code checkIntervalMs} the notes' paths,
 * sizes and modification times (in every subfolder) are compared with what was loaded, so a deploy (or an edit on the server) shows up
 * without a restart.  Notes whose file did not change keep their rendered HTML.  The admin panel's "Clear Server
 * Cache" ({@link Cached}) forces a full re-read.
 * <p>
 * Notes are ordered newest first: by date, then by {@code order} (higher first), then by slug.  In the feed, an
 * announcement comes after the notes of its day ({@link #ANNOUNCEMENT_ORDER}), and announcements of one day by start.
 * <p>
 * Cost: reading the folder parses only file names and front matter (about 40 ms for the ~360 notes); a note's HTML is
 * rendered the first time a page shows it (a few ms each once warm).  {@link #warmUp(int)} does both for the newest
 * page at start-up so the first visitor does not wait for it.
 * <p>
 * <b>The feed.</b>  The page shows the notes together with the past server announcements
 * ({@link #setAnnouncements(AnnouncementSource)}): every announcement whose start has passed, except those that
 * repeat a patch note (their content holds {@link #PATCH_NOTE_MARKER}; the server announces new notes itself), as an
 * entry {@code announcement-<id>} tagged "Announcement", dated by its start and rendered like a note.  They are read
 * again at most every {@link #DEFAULT_ANNOUNCEMENT_TTL_MS} (and on Clear Server Cache).  {@link #getNotes()},
 * {@link #getCount()} and {@link #getPage(int, int)} are the files only; the JSON methods, {@link #get(String)} and
 * {@link #getFeed(String)} are the whole feed.
 * <p>
 * <b>Tags.</b>  A note's front matter may carry {@code tags} ({@link PatchNote#TAGS} are the page's filters).  Every
 * JSON method takes a tag: paging, the month index and a note's neighbours are then those of the notes with that tag.
 */
public class PatchNotesLibrary implements Cached {
    private static final Logger _log = LogManager.getLogger(PatchNotesLibrary.class);

    public static final Pattern FILE_NAME = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2})(-[a-z0-9]+(?:-[a-z0-9]+)*)?\\.md$");
    /** a note's name (its file name without .md), or {@code announcement-<id>} for a server announcement */
    public static final Pattern SLUG = Pattern.compile(
            "^(?:\\d{4}-\\d{2}-\\d{2}(-[a-z0-9]+(?:-[a-z0-9]+)*)?|announcement-\\d{1,10})$");
    public static final String ANNOUNCEMENT_SLUG_PREFIX = "announcement-";
    /** An announcement whose content holds this repeats a patch note (the server announced it) and is left out. */
    public static final String PATCH_NOTE_MARKER = "<!-- gemp-patchnote:";
    /** YYYY-MM */
    public static final Pattern MONTH = Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])$");
    public static final long DEFAULT_ANNOUNCEMENT_TTL_MS = 2 * 60 * 1000;
    /** Announcements come after the notes of their day (a note's order is 0 unless it says otherwise). */
    public static final int ANNOUNCEMENT_ORDER = -1;

    public static final long DEFAULT_CHECK_INTERVAL_MS = 5000;
    public static final int MAX_PAGE_SIZE = 20;

    private static final Comparator<PatchNote> NEWEST_FIRST = Comparator
            .comparing(PatchNote::getDate).reversed()
            .thenComparing(Comparator.comparingInt(PatchNote::getOrder).reversed())
            .thenComparing(Comparator.comparingLong(PatchNote::getSequence).reversed())
            .thenComparing(Comparator.comparing(PatchNote::getSlug).reversed());

    private final File _folder;
    private final long _checkIntervalMs;
    private final LongSupplier _clock;
    private final PatchNoteRenderer _renderer;
    private final CardLookup _cards;

    private final Map<String, LoadedFile> _files = new HashMap<>();
    private List<PatchNote> _notes = null;           // newest first; null until loaded (or after clearCache)
    private String _fingerprint = null;
    private long _lastCheck = 0;

    private AnnouncementSource _announcementSource = null;
    private long _announcementTtlMs = DEFAULT_ANNOUNCEMENT_TTL_MS;
    private Supplier<LocalDateTime> _serverNow = () -> LocalDateTime.now(ZoneOffset.UTC);
    private List<PatchNote> _announcements = List.of();     // newest first
    private Map<Integer, LoadedAnnouncement> _announcementsById = new HashMap<>();
    private boolean _announcementsLoaded = false;
    private long _announcementsCheckedAt = 0;

    /** the feed (notes and announcements) and each tag's part of it; rebuilt when either changes */
    private final Map<String, Feed> _feeds = new HashMap<>();

    private record LoadedFile(long modified, long length, PatchNote note) {
    }

    /** a note file: the file, its path relative to the patchnotes folder ("2011/2011-09-21.md") and its folder ("2011") */
    private record NoteFile(File file, String path, String folder) {
    }

    /** how deep the folder is searched for notes */
    public static final int MAX_DEPTH = 8;

    private record LoadedAnnouncement(String title, String content, LocalDateTime start, PatchNote entry) {
    }

    /** entries newest first, and each one's position by slug */
    private record Feed(List<PatchNote> entries, Map<String, Integer> index) {
    }

    public PatchNotesLibrary(File folder) {
        this(folder, null);
    }

    /** @param cards resolves the notes' {@code [[card links]]}; null renders them as plain text */
    public PatchNotesLibrary(File folder, CardLookup cards) {
        this(folder, DEFAULT_CHECK_INTERVAL_MS, System::currentTimeMillis, cards);
    }

    public PatchNotesLibrary(File folder, long checkIntervalMs, LongSupplier clock) {
        this(folder, checkIntervalMs, clock, null);
    }

    public PatchNotesLibrary(File folder, long checkIntervalMs, LongSupplier clock, CardLookup cards) {
        _folder = folder;
        _checkIntervalMs = checkIntervalMs;
        _clock = clock;
        _cards = cards;
        _renderer = new PatchNoteRenderer(cards);
    }

    /**
     * Reads the folder and renders the newest page now (the server calls this once at start-up, off the request path),
     * so the first visitor of Server Info › Patch Notes gets a cached answer.
     */
    public void warmUp(int count) {
        long start = System.nanoTime();
        List<PatchNote> page;
        synchronized (this) {
            List<PatchNote> feed = getFeed(null);
            page = new ArrayList<>(feed.subList(0, Math.min(feed.size(), clampCount(count))));
        }
        for (PatchNote note : page)
            note.getHtml(_renderer);
        _log.info("Patch notes ready: " + getCount() + " notes, " + getAnnouncementCount() + " announcements, newest "
                + page.size() + " rendered in " + (System.nanoTime() - start) / 1_000_000 + " ms");
    }

    /**
     * Shows the past server announcements in the feed, read again at most every
     * {@link #DEFAULT_ANNOUNCEMENT_TTL_MS}.
     */
    public void setAnnouncements(AnnouncementSource source) {
        setAnnouncements(source, DEFAULT_ANNOUNCEMENT_TTL_MS, () -> LocalDateTime.now(ZoneOffset.UTC));
    }

    /**
     * @param ttlMs     how long a read of the announcements is used (on this library's clock)
     * @param serverNow the server's time (UTC, as the announcements table holds it): announcements starting later are
     *                  left out
     */
    public synchronized void setAnnouncements(AnnouncementSource source, long ttlMs, Supplier<LocalDateTime> serverNow) {
        _announcementSource = source;
        _announcementTtlMs = ttlMs;
        _serverNow = serverNow;
        forgetAnnouncements();
    }

    /** The announcements changed (an admin added one): read them again on the next request. */
    public synchronized void announcementsChanged() {
        _announcementsLoaded = false;
    }

    /**
     * The card library was reloaded: card links are looked up again, so every note is rendered again when next shown.
     */
    public synchronized void cardsChanged() {
        if (_cards instanceof LibraryCardLookup lookup)
            lookup.invalidate();
        clearCache();
    }

    public PatchNoteRenderer getRenderer() {
        return _renderer;
    }

    /** All notes (the files; not the announcements), newest first. */
    public synchronized List<PatchNote> getNotes() {
        refreshIfChanged();
        return _notes;
    }

    /**
     * The feed the page shows, newest first: the notes and the past announcements, or only those with a tag.
     *
     * @param tag a tag (any spelling {@link PatchNote#canonicalTag} accepts); null or blank for everything
     */
    public synchronized List<PatchNote> getFeed(String tag) {
        return feed(tag).entries();
    }

    public synchronized int getAnnouncementCount() {
        getFeed(null);
        return _announcements.size();
    }

    private Feed feed(String tag) {
        refreshIfChanged();
        refreshAnnouncementsIfDue();
        String key = tagKey(tag);
        Feed feed = _feeds.get(key);
        if (feed == null) {
            Feed all = _feeds.get("");
            if (all == null) {
                List<PatchNote> entries = new ArrayList<>(_notes.size() + _announcements.size());
                entries.addAll(_notes);
                entries.addAll(_announcements);
                entries.sort(NEWEST_FIRST);
                all = feedOf(entries);
                _feeds.put("", all);
            }
            if (key.isEmpty()) {
                feed = all;
            } else {
                List<PatchNote> entries = new ArrayList<>();
                for (PatchNote entry : all.entries()) {
                    if (entry.hasTag(key))
                        entries.add(entry);
                }
                feed = feedOf(entries);
            }
            // only cache All and the known tags: the endpoint is public, so caching whatever tag a request names would
            // let anyone grow this map without limit (an unknown tag matches only notes that use it, which is cheap)
            if (key.isEmpty() || PatchNote.isKnownTag(key))
                _feeds.put(key, feed);
        }
        return feed;
    }

    private static Feed feedOf(List<PatchNote> entries) {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < entries.size(); i++)
            index.put(entries.get(i).getSlug(), i);
        return new Feed(Collections.unmodifiableList(entries), index);
    }

    /** "" for everything, else the canonical tag */
    private static String tagKey(String tag) {
        String canonical = PatchNote.canonicalTag(tag);
        return canonical == null ? "" : canonical;
    }

    public synchronized int getCount() {
        return getNotes().size();
    }

    /**
     * @param start index of the first note, 0 = newest
     * @param count how many (clamped to 1..{@link #MAX_PAGE_SIZE})
     */
    public synchronized List<PatchNote> getPage(int start, int count) {
        List<PatchNote> notes = getNotes();
        int from = Math.max(0, Math.min(start, notes.size()));
        int to = Math.min(notes.size(), from + clampCount(count));
        return new ArrayList<>(notes.subList(from, to));
    }

    public static int clampCount(int count) {
        return Math.max(1, Math.min(MAX_PAGE_SIZE, count));
    }

    /** @return the entry's position in the whole feed, newest first, or -1 when there is no such note or announcement */
    public synchronized int indexOf(String slug) {
        return indexOf(slug, null);
    }

    /** @return the entry's position in the tag's feed, or -1 when it is not there */
    public synchronized int indexOf(String slug, String tag) {
        if (slug == null)
            return -1;
        Integer index = feed(tag).index().get(slug.toLowerCase(Locale.ROOT));
        return index == null ? -1 : index;
    }

    /** @return the note or announcement, or null when there is no such entry */
    public synchronized PatchNote get(String slug) {
        int index = indexOf(slug);
        return index < 0 ? null : feed(null).entries().get(index);
    }

    // ---- the JSON the Patch Notes page reads (PatchNotesRequestHandler) ----

    /** {@link #pageJson(int, int, String, String, String)} without a month or a tag */
    public synchronized Map<String, Object> pageJson(int start, int count, String from) {
        return pageJson(start, count, from, null, null);
    }

    /**
     * GET /patchnotes: {@code {total, start, count, tag, all, filters:[{tag, count}], notes:[{slug, kind, date, title,
     * summary, tags, html}]}}, newest first.  {@code total} counts the tag's entries, {@code all} every entry, and
     * {@code filters} gives each of {@link PatchNote#TAGS} with its count, for the filter bar.
     *
     * @param start index of the first entry (0 = newest; negative counts as 0)
     * @param count page size, clamped to 1..{@link #MAX_PAGE_SIZE}
     * @param from  when given, the page starts at this entry instead of at {@code start}
     * @param month when given (YYYY-MM, and no {@code from}), the page starts at the newest entry of that month, or,
     *              when the month has none, at the newest older one
     * @param tag   when given, only the entries with this tag
     * @return null when {@code from} names no entry of the tag's feed, or {@code month} is malformed or has nothing at
     * or before it
     */
    public synchronized Map<String, Object> pageJson(int start, int count, String from, String month, String tag) {
        List<PatchNote> entries = getFeed(tag);
        if (from != null && !from.isBlank()) {
            start = SLUG.matcher(from.toLowerCase(Locale.ROOT)).matches() ? indexOf(from, tag) : -1;
            if (start < 0)
                return null;
        } else if (month != null && !month.isBlank()) {
            start = firstIndexAtOrBefore(entries, month.trim());
            if (start < 0)
                return null;
        }
        start = Math.max(0, start);
        count = clampCount(count);
        int from0 = Math.min(start, entries.size());
        List<Map<String, Object>> page = new ArrayList<>();
        for (PatchNote note : entries.subList(from0, Math.min(entries.size(), from0 + count)))
            page.add(note.toJson(_renderer));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", entries.size());
        result.put("start", start);
        result.put("count", count);
        String key = tagKey(tag);
        result.put("tag", key.isEmpty() ? null : key);
        result.put("all", getFeed(null).size());
        result.put("filters", filtersJson());
        result.put("notes", page);
        return result;
    }

    /** [{tag, count}] for each of {@link PatchNote#TAGS} */
    private List<Map<String, Object>> filtersJson() {
        List<Map<String, Object>> filters = new ArrayList<>();
        for (String known : PatchNote.TAGS) {
            Map<String, Object> filter = new LinkedHashMap<>();
            filter.put("tag", known);
            filter.put("count", getFeed(known).size());
            filters.add(filter);
        }
        return filters;
    }

    /** the index of the newest entry in `month` or before it, or -1 (also for a malformed month) */
    private static int firstIndexAtOrBefore(List<PatchNote> entries, String month) {
        if (!MONTH.matcher(month).matches())
            return -1;
        YearMonth target = YearMonth.parse(month);
        for (int i = 0; i < entries.size(); i++) {
            if (!YearMonth.from(entries.get(i).getDate()).isAfter(target))
                return i;
        }
        return -1;
    }

    /**
     * GET /patchnotes/months: the months that have entries (of the tag), newest first, with the first entry of each
     * in feed order (its newest): {@code {tag, months:[{month: "2026-09", first: "2026-09-27-hall-overhaul",
     * count: 2}]}}.
     */
    public synchronized Map<String, Object> monthsJson(String tag) {
        List<Map<String, Object>> months = new ArrayList<>();
        Map<String, Object> current = null;
        for (PatchNote entry : getFeed(tag)) {
            String month = YearMonth.from(entry.getDate()).toString();
            if (current == null || !month.equals(current.get("month"))) {
                current = new LinkedHashMap<>();
                current.put("month", month);
                current.put("first", entry.getSlug());
                current.put("count", 0);
                months.add(current);
            }
            current.put("count", (Integer) current.get("count") + 1);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        String key = tagKey(tag);
        result.put("tag", key.isEmpty() ? null : key);
        result.put("months", months);
        return result;
    }

    /** {@link #noteJson(String, String)} in the whole feed */
    public synchronized Map<String, Object> noteJson(String slug) {
        return noteJson(slug, null);
    }

    /**
     * GET /patchnotes/&lt;slug&gt;: {@code {index, total, note:{slug, kind, date, title, summary, tags, html},
     * newer:{slug, kind, date, title, summary, tags}|null, older:...|null}}, the index and neighbours in the tag's feed.
     *
     * @return null when there is no such entry in the tag's feed
     */
    public synchronized Map<String, Object> noteJson(String slug, String tag) {
        if (slug == null || !SLUG.matcher(slug.toLowerCase(Locale.ROOT)).matches())
            return null;
        int index = indexOf(slug, tag);
        if (index < 0)
            return null;
        List<PatchNote> notes = getFeed(tag);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("index", index);
        result.put("total", notes.size());
        result.put("note", notes.get(index).toJson(_renderer));
        result.put("newer", index > 0 ? notes.get(index - 1).toHeaderJson() : null);
        result.put("older", index + 1 < notes.size() ? notes.get(index + 1).toHeaderJson() : null);
        return result;
    }

    @Override
    public synchronized void clearCache() {
        _files.clear();
        _notes = null;
        _fingerprint = null;
        _feeds.clear();
        forgetAnnouncements();
    }

    @Override
    public synchronized int getItemCount() {
        return (_notes == null ? 0 : _notes.size()) + _announcements.size();
    }

    private void forgetAnnouncements() {
        _announcements = List.of();
        _announcementsById = new HashMap<>();
        _announcementsLoaded = false;
        _feeds.clear();
    }

    // ---- announcements ----

    private void refreshAnnouncementsIfDue() {
        if (_announcementSource == null)
            return;
        long now = _clock.getAsLong();
        if (_announcementsLoaded && now - _announcementsCheckedAt < _announcementTtlMs)
            return;
        _announcementsLoaded = true;
        _announcementsCheckedAt = now;

        List<DBDefs.Announcement> rows;
        try {
            rows = _announcementSource.getPastAnnouncements();
        } catch (RuntimeException exp) {
            // the notes still show; the announcements are tried again after the TTL
            _log.warn("Could not read the announcements for the patch notes feed", exp);
            return;
        }
        LocalDateTime serverNow = _serverNow.get();
        Map<Integer, LoadedAnnouncement> byId = new HashMap<>();
        List<PatchNote> entries = new ArrayList<>();
        for (DBDefs.Announcement row : rows == null ? List.<DBDefs.Announcement>of() : rows) {
            if (row == null || row.start == null || row.start.isAfter(serverNow) || byId.containsKey(row.id))
                continue;
            if (row.content != null && row.content.contains(PATCH_NOTE_MARKER))
                continue;           // the server's own announcement of a patch note: the note is in the feed already
            LoadedAnnouncement previous = _announcementsById.get(row.id);
            LoadedAnnouncement current = previous != null && Objects.equals(previous.title(), row.title)
                    && Objects.equals(previous.content(), row.content) && previous.start().equals(row.start)
                    ? previous
                    : new LoadedAnnouncement(row.title, row.content, row.start, toEntry(row));
            byId.put(row.id, current);
            entries.add(current.entry());
        }
        entries.sort(NEWEST_FIRST);
        if (!entries.equals(_announcements)) {
            _announcements = List.copyOf(entries);
            _feeds.clear();
        }
        _announcementsById = byId;
    }

    /** An announcement as a feed entry: {@code announcement-<id>}, dated by its start, tagged Announcement. */
    static PatchNote toEntry(DBDefs.Announcement row) {
        String content = row.content == null ? "" : row.content.replace("\r\n", "\n").replace('\r', '\n').strip();
        return new PatchNote(ANNOUNCEMENT_SLUG_PREFIX + row.id, row.start.toLocalDate(), emptyToNull(row.title), null,
                ANNOUNCEMENT_ORDER, content, List.of(PatchNote.TAG_ANNOUNCEMENT), PatchNote.KIND_ANNOUNCEMENT,
                row.start.toEpochSecond(ZoneOffset.UTC));
    }

    private void refreshIfChanged() {
        long now = _clock.getAsLong();
        if (_notes != null && now - _lastCheck < _checkIntervalMs)
            return;
        _lastCheck = now;

        List<NoteFile> files = listNoteFiles();
        String fingerprint = fingerprint(files);
        if (_notes != null && fingerprint.equals(_fingerprint))
            return;

        Map<String, LoadedFile> loaded = new HashMap<>();
        List<PatchNote> notes = new ArrayList<>();
        Map<String, NoteFile> bySlug = new HashMap<>();
        for (NoteFile noteFile : files) {
            File file = noteFile.file();
            LoadedFile previous = _files.get(noteFile.path());
            LoadedFile current;
            if (previous != null && previous.modified() == file.lastModified() && previous.length() == file.length()) {
                current = previous;
            } else {
                try {
                    PatchNote note = readNote(file);
                    note.locate(noteFile.folder(), pathResolver(noteFile.folder()));
                    current = new LoadedFile(file.lastModified(), file.length(), note);
                } catch (IOException | IllegalArgumentException exp) {
                    _log.warn("Skipping patch note " + noteFile.path() + ": " + exp.getMessage());
                    continue;
                }
            }
            loaded.put(noteFile.path(), current);
            PatchNote note = current.note();
            NoteFile first = bySlug.putIfAbsent(note.getSlug(), noteFile);
            if (first != null) {
                // the files are in order (shallowest first, then by path), so the same one wins every time
                _log.warn("Skipping patch note " + noteFile.path() + ": " + first.path()
                        + " has the same name, and a note's name (its link) must be unique across all folders");
                continue;
            }
            notes.add(note);
        }
        notes.sort(NEWEST_FIRST);

        _files.clear();
        _files.putAll(loaded);
        _notes = Collections.unmodifiableList(notes);
        _fingerprint = fingerprint;
        _feeds.clear();
    }

    /**
     * Every note file in the folder and its subfolders (up to {@link #MAX_DEPTH} deep), skipping {@code img}
     * folders, hidden folders and anything that isn't named like a note (README.md, images...).  In order: the
     * shallowest first, then by path.
     */
    private List<NoteFile> listNoteFiles() {
        List<NoteFile> result = new ArrayList<>();
        File[] top = _folder.listFiles();
        if (top == null) {
            if (_notes == null)
                _log.warn("Patch notes folder " + _folder.getAbsolutePath() + " is missing or unreadable");
            return result;
        }
        collect(_folder, "", 0, result);
        result.sort(Comparator.comparingInt((NoteFile f) -> f.path().split("/").length).thenComparing(NoteFile::path));
        return result;
    }

    private static void collect(File dir, String folder, int depth, List<NoteFile> result) {
        File[] children = dir.listFiles();
        if (children == null)
            return;
        for (File child : children) {
            String name = child.getName();
            if (child.isDirectory()) {
                if (depth + 1 > MAX_DEPTH || name.startsWith(".") || name.equalsIgnoreCase("img")
                        || Files.isSymbolicLink(child.toPath()))
                    continue;
                collect(child, folder.isEmpty() ? name : folder + "/" + name, depth + 1, result);
            } else if (FILE_NAME.matcher(name.toLowerCase(Locale.ROOT)).matches()) {
                result.add(new NoteFile(child, folder.isEmpty() ? name : folder + "/" + name, folder));
            }
        }
    }

    private static String fingerprint(List<NoteFile> files) {
        StringBuilder sb = new StringBuilder();
        for (NoteFile noteFile : files) {
            File file = noteFile.file();
            sb.append(noteFile.path()).append('|').append(file.lastModified()).append('|').append(file.length()).append('\n');
        }
        return sb.toString();
    }

    /**
     * How a note in {@code folder} resolves its relative paths: to the file next to the note when there is one, else
     * (as a note straight in the patchnotes folder does) relative to the patchnotes folder, so a note moved into a
     * subfolder keeps its {@code img/...} pictures.
     */
    PatchNoteRenderer.PathResolver pathResolver(String folder) {
        if (folder == null || folder.isEmpty())
            return PatchNoteRenderer.PathResolver.ROOT;
        Path root = _folder.toPath().toAbsolutePath().normalize();
        return path -> {
            int cut = firstIndexOf(path, '?', '#');
            String file = cut < 0 ? path : path.substring(0, cut);
            String rest = cut < 0 ? "" : path.substring(cut);
            String joined = normalize(folder + "/" + file);
            if (joined == null || file.isEmpty())
                return path;
            try {
                Path candidate = root.resolve(percentDecode(joined)).normalize();
                if (candidate.startsWith(root) && Files.isRegularFile(candidate))
                    return joined + rest;
            } catch (RuntimeException ignored) {
                // an odd path: relative to the patchnotes folder, as before
            }
            return path;
        };
    }

    private static int firstIndexOf(String text, char a, char b) {
        int i = text.indexOf(a);
        int j = text.indexOf(b);
        return i < 0 ? j : j < 0 ? i : Math.min(i, j);
    }

    /** "2011/../img/a.png" -> "img/a.png"; null when it climbs out of the patchnotes folder */
    static String normalize(String path) {
        List<String> parts = new ArrayList<>();
        for (String part : path.split("/")) {
            if (part.isEmpty() || part.equals("."))
                continue;
            if (part.equals("..")) {
                if (parts.isEmpty())
                    return null;
                parts.remove(parts.size() - 1);
            } else {
                parts.add(part);
            }
        }
        return String.join("/", parts);
    }

    private static String percentDecode(String path) {
        if (path.indexOf('%') < 0)
            return path;
        try {
            return java.net.URLDecoder.decode(path.replace("+", "%2B"), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exp) {
            return path;
        }
    }

    static PatchNote readNote(File file) throws IOException {
        String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        return parse(file.getName(), text);
    }

    /**
     * Parses one note file.
     *
     * @param fileName {@code YYYY-MM-DD[-slug].md}; gives the slug, and the date when the front matter has none
     * @throws IllegalArgumentException when the name or the front matter is malformed
     */
    public static PatchNote parse(String fileName, String text) {
        String lowerName = fileName.toLowerCase(Locale.ROOT);
        Matcher nameMatch = FILE_NAME.matcher(lowerName);
        if (!nameMatch.matches())
            throw new IllegalArgumentException("file name must be YYYY-MM-DD.md or YYYY-MM-DD-some-name.md");
        String slug = lowerName.substring(0, lowerName.length() - 3);

        if (text.startsWith("﻿"))
            text = text.substring(1);
        text = text.replace("\r\n", "\n").replace('\r', '\n');

        Map<String, String> front = new LinkedHashMap<>();
        Map<String, List<String>> lists = new LinkedHashMap<>();
        String body = text;
        if (text.startsWith("---\n")) {
            int end = text.indexOf("\n---", 3);
            if (end < 0)
                throw new IllegalArgumentException("front matter opened with --- but never closed");
            int afterClose = text.indexOf('\n', end + 4);
            String closeLine = text.substring(end + 1, afterClose < 0 ? text.length() : afterClose).trim();
            if (!closeLine.equals("---"))
                throw new IllegalArgumentException("front matter must be closed by a line holding only ---");
            String listKey = null;          // a key with no value on its line: "- item" lines below it are its list
            for (String line : text.substring(4, end + 1).split("\n")) {
                if (line.isBlank() || line.trim().startsWith("#"))
                    continue;
                String trimmed = line.trim();
                if (listKey != null && (trimmed.startsWith("- ") || trimmed.equals("-"))) {
                    lists.computeIfAbsent(listKey, k -> new ArrayList<>()).add(unquote(trimmed.substring(1).trim()));
                    continue;
                }
                int colon = line.indexOf(':');
                if (colon <= 0)
                    throw new IllegalArgumentException("front matter line is not 'key: value': " + line);
                String key = line.substring(0, colon).trim().toLowerCase(Locale.ROOT);
                String value = line.substring(colon + 1).trim();
                front.put(key, unquote(value));
                listKey = value.isEmpty() ? key : null;
            }
            body = afterClose < 0 ? "" : text.substring(afterClose + 1);
        }

        LocalDate date;
        String dateText = front.get("date");
        try {
            date = LocalDate.parse(dateText != null && !dateText.isEmpty() ? dateText : nameMatch.group(1));
        } catch (DateTimeParseException exp) {
            throw new IllegalArgumentException("date must be YYYY-MM-DD: " + dateText);
        }

        int order = 0;
        String orderText = front.get("order");
        if (orderText != null && !orderText.isEmpty()) {
            try {
                order = Integer.parseInt(orderText);
            } catch (NumberFormatException exp) {
                throw new IllegalArgumentException("order must be a whole number: " + orderText);
            }
        }

        String summary = front.containsKey("summary") ? front.get("summary") : front.get("blurb");
        List<String> tags = PatchNote.canonicalTags(parseTags(front.get("tags"), lists.get("tags")));
        for (String tag : tags) {
            if (!PatchNote.isKnownTag(tag))
                _log.warn("Patch note " + fileName + ": tag '" + tag + "' is not one of the page's filters "
                        + PatchNote.TAGS + " (it still shows on the note)");
        }
        return new PatchNote(slug, date, emptyToNull(front.get("title")), emptyToNull(summary), order, body.strip(),
                tags, PatchNote.KIND_NOTE, 0);
    }

    /**
     * The tags of a note's front matter, as written: {@code tags: Card Fixes, User Interface}, the same in brackets
     * ({@code tags: [Card Fixes, "User Interface"]}), or a YAML list ({@code tags:} with {@code - Card Fixes} lines
     * below it).
     */
    static List<String> parseTags(String inline, List<String> listItems) {
        List<String> tags = new ArrayList<>();
        if (inline != null && !inline.isBlank()) {
            String value = inline.trim();
            if (value.startsWith("[") && value.endsWith("]"))
                value = value.substring(1, value.length() - 1);
            for (String part : value.split(","))
                tags.add(unquote(part.trim()));
        }
        if (listItems != null) {
            for (String item : listItems) {
                String value = item.trim();
                // "- [A, B]" or "- A, B" is taken as a list too
                tags.addAll(parseTags(value, null));
            }
        }
        tags.removeIf(String::isBlank);
        return tags;
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if (first == '"' && last == '"') {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < value.length() - 1; i++) {
                    char c = value.charAt(i);
                    if (c == '\\' && i + 1 < value.length() - 1) {
                        char next = value.charAt(++i);
                        sb.append(next == 'n' ? '\n' : next);
                    } else {
                        sb.append(c);
                    }
                }
                return sb.toString();
            }
            if (first == '\'' && last == '\'')
                return value.substring(1, value.length() - 1).replace("''", "'");
        }
        return value;
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
