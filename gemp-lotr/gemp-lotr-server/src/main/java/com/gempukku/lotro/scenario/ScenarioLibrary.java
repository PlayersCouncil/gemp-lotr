package com.gempukku.lotro.scenario;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.cache.Cached;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.patchnotes.CardLookup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.stream.Stream;

/**
 * The scenarios the server can offer.  They are kept in the data folder (scenarios.user.path), which is not part of
 * the repository:
 *
 *     public/                 the public scenarios: the ones everybody is offered.  Any .hjson file in it or under
 *                             it, except under campaigns/, which holds campaign files (ScenarioCampaign): lists of
 *                             scenarios to be played one after another.
 *     users/&lt;player name&gt;/    the scenarios that player has written; that player is the scenario's owner.
 *     archive/                every scenario file that was replaced or deleted (ScenarioArchive).
 *
 * The scenarios that ship with the game (gemp-lotr-cards, resources/scenarios) are copied into public/: one that is
 * new in the repository is put there, and one the repository has changed is put there in place of the old, as long
 * as the server's copy is still the one the repository last had.  A public scenario that was changed on the server
 * (a submission approved in its place, say) is never overwritten by the repository: where both have changed it, the
 * difference is listed for an admin to settle (conflicts), and one that was deleted on the server stays deleted.
 * What the repository last had is kept in public/.shipped.json.  Without a data folder (a developer's checkout, the
 * tests) the shipped folder is read as the public one.
 *
 * Beside the files, each folder keeps what is known about its scenarios in a .meta.json (Meta): a player's scenario's
 * place in review, a public scenario being disabled, and so on.
 *
 * A scenario is known by the id inside its file, never by its file name.  A user's scenario is known as
 * "&lt;player name&gt;/&lt;id&gt;", so no two authors can collide, and none can collide with a canonical scenario.
 *
 * Every file is parsed, checked against the card library, and then played through headless (ScenarioRunner), so what
 * is known about each scenario is not just whether it is well-formed but whether it works.  A file with something
 * wrong with it stays in the library with its errors, so that whoever has to fix it can be told what they are; the
 * others load regardless.
 *
 * The folders are read on first use, and looked at again at most every few seconds: a file that is new, gone or
 * changed (by path, size and modification time) is picked up without a restart, and files that did not change are not
 * read again.  Clearing the server cache, or reloading the card library, reads everything again.
 */
public class ScenarioLibrary implements Cached {
    private static final Logger LOG = LogManager.getLogger(ScenarioLibrary.class);

    public static final long DEFAULT_CHECK_INTERVAL_MS = 5000;
    /** How deep the folders are searched. */
    private static final int MAX_DEPTH = 8;

    /**
     * One scenario file and everything known about it.
     * @param id What the scenario is known as; null if the file could not be read far enough to tell.
     * @param path Where the file is, relative to its folder, with forward slashes ("basics/01-into-the-wild.hjson").
     * @param owner The player whose scenario it is; null for a canonical scenario.
     * @param def The scenario; null if the file is not a well-formed scenario.
     * @param format The scenario's format; null if there is no such format.
     * @param errors Why the scenario cannot be played at all.  Empty if it can.
     * @param warnings Things that are wrong but do not stop it being played (a card link that names no card).
     * @param report What happened when it was played through headless; null if it could not be played at all.
     * @param leftOut What had to be left out of the file for 'def' to be read at all (see ScenarioSalvage); empty if
     *                'def' is the file as it stands.  A scenario read like this has its mistakes among 'errors', so
     *                it cannot be played; it is for the authoring tool to open, and for nothing else.
     */
    public record Entry(String id, String path, String owner, ScenarioDefinition def, LotroFormat format,
                        List<String> errors, List<String> warnings, ScenarioRunReport report, List<ScenarioSalvage.LeftOut> leftOut) {
        public boolean canonical() {
            return owner == null;
        }

        /** Whether the authoring tool can open it: it can be played, or it could be read with its mistakes left out. */
        public boolean openable() {
            return id != null && def != null && format != null && (errors.isEmpty() || !leftOut.isEmpty());
        }

        /** Whether a game of it can be started. */
        public boolean playable() {
            return def != null && format != null && errors.isEmpty();
        }

        /** Whether the headless run proved as much of it as the scenario says it should. */
        public boolean verified() {
            return playable() && report != null && report.passed();
        }
    }

    /**
     * What is kept about a scenario beside its file, by the file's path, in its folder's .meta.json.  Plain fields,
     * for the JSON; a file with nothing kept about it has none.
     */
    public static final class Meta {
        /**
         * A player's scenario, in review: "submitted" (waiting for an admin, and not to be changed meanwhile),
         * "rejected" (changes were asked for: see feedback) or "approved"; null if it has not been submitted.
         */
        public String status;
        public Long submittedAt;
        /** What the admin who rejected it asked to be changed. */
        public String feedback;
        public Long reviewedAt;
        public String reviewedBy;
        /**
         * A player's scenario: the id of the public scenario it is a version of, because it was copied from that one
         * or was approved as it.  Approving it replaces that scenario.
         */
        public String origin;
        /** A public scenario: not offered on the Play screen, and not to be started from it. */
        public boolean disabled;
        /** A public scenario: the player's scenario it was approved from ("owner/path"), when, and by whom. */
        public String approvedFrom;
        public Long approvedAt;
        public String approvedBy;
        /**
         * A player's approved scenario: what its file said when it was approved (a hash), to tell whether it has been
         * changed since.
         */
        public String approvedHash;

        public Meta copy() {
            return JSON.parseObject(JSON.toJSONString(this), Meta.class);
        }

        boolean empty() {
            return status == null && submittedAt == null && feedback == null && reviewedAt == null && reviewedBy == null
                    && origin == null && !disabled && approvedFrom == null && approvedAt == null && approvedBy == null && approvedHash == null;
        }
    }

    /**
     * Where the public folder and the repository disagree in a way that is not for the server to settle.
     * @param path The file's path, within the public folder.
     * @param kind "changed": both changed it since the server last took the repository's.  "deleted": it was deleted
     *             here, and the repository has changed it since.  "different": the server had its own file there
     *             before it kept track, and it is not the repository's.
     */
    public record Conflict(String path, String kind) { }

    private record LoadedFile(long modified, long length, Entry entry) { }

    private static final String META = ".meta.json";
    private static final String SHIPPED = ".shipped.json";

    /** The scenarios that ship with the game: the repository's folder. */
    private final File _shippedFolder;
    /** The public scenarios: public/ in the data folder, or (with no data folder) the shipped folder itself. */
    private final File _canonicalFolder;
    /** The data folder; null if there is none. */
    private final File _userFolder;
    private final ScenarioArchive _archive;
    /** What is kept beside the files, by owner ("" for the public folder) and path; read when first asked for. */
    private final Map<String, Map<String, Meta>> _meta = new HashMap<>();
    private List<Conflict> _conflicts = List.of();
    /** The files of the shipped and the public folders, as they were when they were last compared. */
    private String _synced;
    /** Set when a file has been written, moved or deleted here: the folders are taken to have changed. */
    private boolean _reread;
    private final LotroCardBlueprintLibrary _library;
    private final Function<String, LotroFormat> _formats;
    private final CardLookup _cardLookup;
    private final long _checkIntervalMs;
    private final LongSupplier _clock;

    /** By the file's absolute path. */
    private Map<String, LoadedFile> _files = new HashMap<>();
    /** By id, in the order they are listed. */
    private Map<String, Entry> _byId = new LinkedHashMap<>();
    /** In the order they come (ScenarioCampaign.order, then id). */
    private List<ScenarioCampaign> _campaigns = List.of();
    private List<Entry> _entries = List.of();
    private boolean _loaded = false;
    private long _lastCheck = 0;

    /**
     * @param shippedFolder The scenarios that ship with the game.
     * @param userFolder The data folder; may be null, or not exist (when the library is made): then there are no user
     *                   scenarios, and the shipped folder is the public one.
     * @param formats Finds a format by its code; returns null if there is none.
     * @param cardLookup Finds the card a [[card link]] names when it is not one of the scenario's own; may be null.
     */
    public ScenarioLibrary(File shippedFolder, File userFolder, LotroCardBlueprintLibrary library, Function<String, LotroFormat> formats,
                           CardLookup cardLookup) {
        this(shippedFolder, userFolder, library, formats, cardLookup, DEFAULT_CHECK_INTERVAL_MS, System::currentTimeMillis);
    }

    public ScenarioLibrary(File shippedFolder, File userFolder, LotroCardBlueprintLibrary library, Function<String, LotroFormat> formats,
                           CardLookup cardLookup, long checkIntervalMs, LongSupplier clock) {
        //(a data folder that is not there is none: the server does not make one somewhere nobody meant it to be)
        if (userFolder != null && !userFolder.isDirectory()) {
            LOG.warn("The scenario data folder " + userFolder + " does not exist: there are no players' scenarios, and the shipped ones are read where they are");
            userFolder = null;
        }
        _shippedFolder = shippedFolder;
        _canonicalFolder = userFolder == null ? shippedFolder : new File(userFolder, "public");
        _userFolder = userFolder;
        _archive = userFolder == null ? null : new ScenarioArchive(new File(userFolder, "archive"));
        _library = library;
        _formats = formats;
        _cardLookup = cardLookup;
        _checkIntervalMs = checkIntervalMs;
        _clock = clock;
    }

    /**
     * @return The scenario with this id, or null.  It may not be playable; ask it.
     */
    public synchronized Entry get(String id) {
        refresh();
        return id == null ? null : _byId.get(id);
    }

    /**
     * @return Every scenario file, working or not: canonical ones first, each group in order of path.
     */
    public synchronized List<Entry> getAll() {
        refresh();
        return _entries;
    }

    /**
     * @return The scenarios a player can start, in the order to list them.
     */
    public synchronized List<Entry> getAvailableTo(String playerName, boolean admin) {
        refresh();
        List<Entry> result = new ArrayList<>();
        for (Entry entry : _byId.values())
            if (listedFor(entry, playerName, admin))
                result.add(entry);
        return result;
    }

    /** Whether a public scenario has been disabled: it is not offered, and cannot be started from the Play screen. */
    public synchronized boolean disabled(Entry entry) {
        Meta meta = entry.canonical() ? metaOf(null).get(entry.path()) : null;
        return meta != null && meta.disabled;
    }

    /**
     * Whether a player is offered a scenario on the Play screen: the public scenarios that work and have not been
     * disabled (an admin is offered the ones that do not work as well, to see why), and their own that can be
     * started.  Other players' scenarios are for their authors, and for admins to review (see startableBy).
     */
    public synchronized boolean listedFor(Entry entry, String playerName, boolean admin) {
        if (!entry.playable())
            return false;
        if (entry.canonical())
            return !disabled(entry) && (entry.verified() || admin);
        return entry.owner().equals(playerName);
    }

    /** Whether a player may start a game of it: what they are offered, and for an admin anything that can be started. */
    public synchronized boolean startableBy(Entry entry, String playerName, boolean admin) {
        return entry.playable() && (admin || listedFor(entry, playerName, admin));
    }

    /** @return The scenario whose file is at a path; null if there is none. */
    public synchronized Entry at(String owner, String path) {
        refresh();
        for (Entry entry : _entries)
            if (entry.path().equals(path) && java.util.Objects.equals(entry.owner(), owner))
                return entry;
        return null;
    }

    /**
     * @return The id a scenario's own file gives it: a player's scenario without its owner's name in front.  Null
     * if it has none.
     */
    public static String fileId(Entry entry) {
        if (entry.id() == null)
            return null;
        return entry.owner() == null ? entry.id() : entry.id().substring(entry.owner().length() + 1);
    }

    /** The players who have a folder of scenarios. */
    public synchronized List<String> owners() {
        List<String> owners = new ArrayList<>();
        File[] folders = _userFolder == null ? null : new File(_userFolder, "users").listFiles(File::isDirectory);
        if (folders != null)
            for (File folder : folders)
                owners.add(folder.getName());
        owners.sort(String.CASE_INSENSITIVE_ORDER);
        return owners;
    }

    public synchronized List<ScenarioCampaign> getCampaigns() {
        refresh();
        return _campaigns;
    }

    /**
     * @return The campaign a scenario is part of (the first, if it is listed by several); null if it is in none.
     */
    public synchronized ScenarioCampaign campaignOf(String scenarioId) {
        refresh();
        for (ScenarioCampaign campaign : _campaigns)
            if (campaign.scenarios().contains(scenarioId))
                return campaign;
        return null;
    }

    /**
     * @return Where a scenario stands in its campaign, for a client: {id, title, position, count, next: {id, title}}.
     * next is the scenario after it that this player can start, or null if there is none.  Null altogether if the
     * scenario is part of no campaign.
     */
    public synchronized Map<String, Object> campaignInfo(String scenarioId, String playerName, boolean admin) {
        ScenarioCampaign campaign = campaignOf(scenarioId);
        if (campaign == null)
            return null;
        int position = campaign.scenarios().indexOf(scenarioId);
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", campaign.id());
        info.put("title", campaign.title());
        info.put("position", position + 1);
        info.put("count", campaign.scenarios().size());
        Map<String, Object> next = null;
        for (int i = position + 1; i < campaign.scenarios().size() && next == null; i++) {
            Entry entry = _byId.get(campaign.scenarios().get(i));
            if (entry != null && listedFor(entry, playerName, admin)) {
                next = new LinkedHashMap<>();
                next.put("id", entry.id());
                next.put("title", entry.def().title);
            }
        }
        info.put("next", next);
        return info;
    }

    /**
     * @return A scenario's file as it is on disk; null if it cannot be read.
     */
    public synchronized String source(Entry entry) {
        File folder = entry.owner() == null ? _canonicalFolder
                : _userFolder == null ? null : new File(new File(_userFolder, "users"), entry.owner());
        if (folder == null)
            return null;
        try {
            return Files.readString(new File(folder, entry.path().replace('/', File.separatorChar)).toPath(), StandardCharsets.UTF_8);
        } catch (IOException | RuntimeException exp) {
            return null;
        }
    }

    /** Whether there is anywhere to keep scenarios that are not part of the game itself. */
    public boolean hasUserFolder() {
        return _userFolder != null;
    }

    /** A name for a folder or a file that cannot lead anywhere but into the folder it is in. */
    private static final String NAME = "[A-Za-z0-9_-][A-Za-z0-9_. -]*";
    /** How long the path of a scenario's file may be, folders and all. */
    public static final int MAX_PATH = 120;
    /** How many scenarios a player may keep. */
    public static final int MAX_OWN = 100;

    /** How many scenario files a player has. */
    public synchronized int countOf(String owner) {
        refresh();
        int count = 0;
        for (Entry entry : _entries)
            if (owner.equals(entry.owner()))
                count++;
        return count;
    }

    /**
     * @return Where a scenario's file is (or would be).
     * @throws IOException if that is not somewhere a scenario can be kept.
     */
    private File fileFor(String owner, String path) throws IOException {
        if (path != null && path.length() > MAX_PATH)
            throw new IOException("That is too long a path for a scenario's file: " + MAX_PATH + " characters at most, folders and all");
        if (path == null || !path.matches("(" + NAME + "/){0," + (MAX_DEPTH - 2) + "}" + NAME + "\\.hjson") || path.contains("..")
                || path.startsWith("campaigns/"))
            throw new IOException("'" + path + "' is not somewhere a scenario can be kept: up to " + (MAX_DEPTH - 2) + " folders and a file name, "
                    + "each starting with a letter or digit and made of letters, digits, spaces, dots, dashes and underscores, "
                    + "the file's ending in .hjson (and not under campaigns/)");
        if (owner != null && (!owner.matches(NAME) || owner.contains("..")))
            throw new IOException("'" + owner + "' cannot be the name of a folder");
        File folder;
        if (owner == null) {
            folder = _canonicalFolder;
        } else {
            if (_userFolder == null)
                throw new IOException("This server keeps no scenarios but its own (scenarios.user.path is not set)");
            folder = new File(new File(_userFolder, "users"), owner);
        }
        return new File(folder, path.replace('/', File.separatorChar));
    }

    /** @return Where a scenario's file is (or would be). */
    public File file(String owner, String path) throws IOException {
        return fileFor(owner, path);
    }

    /** Whether a player's scenario is in review: submitted, and not yet approved or rejected. */
    public synchronized boolean locked(String owner, String path) {
        return owner != null && "submitted".equals(meta(owner, path).status);
    }

    /** @throws IOException saying why, if a scenario's file cannot be kept there. */
    public void check(String owner, String path) throws IOException {
        fileFor(owner, path);
    }

    /**
     * Authoring: moves a scenario's file, and reads the folders again.
     * @throws IOException if there is no such file, there is already one where it is to go, or it cannot be moved.
     */
    public synchronized void move(String owner, String path, String toOwner, String toPath) throws IOException {
        File from = fileFor(owner, path);
        File to = fileFor(toOwner, toPath);
        if (!from.isFile())
            throw new IOException("There is no file at " + path);
        if (to.exists())
            throw new IOException("There is already a file at " + toPath);
        Files.createDirectories(to.getParentFile().toPath());
        Files.move(from.toPath(), to.toPath());
        prune(owner, from);
        //(what is kept about it goes with it)
        Meta meta = metaOf(owner).get(path);
        if (meta != null) {
            putMeta(owner, path, null);
            putMeta(toOwner, toPath, meta);
        }
        reload(from, to);
    }

    /** A folder that had nothing else in it is not left behind (nor the one it was in, and so on up to the top). */
    private void prune(String owner, File gone) {
        File top = owner == null ? _canonicalFolder : new File(new File(_userFolder, "users"), owner);
        for (File folder = gone.getParentFile(); folder != null && !folder.equals(top) && folder.toPath().startsWith(top.toPath()); folder = folder.getParentFile()) {
            String[] left = folder.list();
            if (left == null || left.length > 0 || !folder.delete())
                break;
        }
    }

    /** Reads the folders again at once, with these files read afresh whatever their time and size say. */
    private void reload(File... changed) {
        for (File file : changed)
            _files.remove(file.getAbsolutePath());
        _reread = true;
        _lastCheck = _clock.getAsLong();
        load();
        _loaded = true;
    }

    /** @return The text of a scenario's file. */
    public synchronized String read(String owner, String path) throws IOException {
        return Files.readString(fileFor(owner, path).toPath(), StandardCharsets.UTF_8);
    }

    /** The archive of what was replaced and deleted; null if there is no data folder, and nothing is kept. */
    public ScenarioArchive archive() {
        return _archive;
    }

    /**
     * Puts a scenario's file away in the archive, with what is kept about it.  The file stays where it is.
     * @return Its record; null if there is no archive.
     */
    public synchronized ScenarioArchive.Record archive(String owner, String path, String reason, String by) throws IOException {
        File file = fileFor(owner, path);
        if (!file.isFile())
            throw new IOException("There is no file at " + path);
        Entry entry = at(owner, path);
        Meta meta = metaOf(owner).get(path);
        return archiveFile(file, owner, path, entry == null ? null : fileId(entry),
                entry == null || entry.def() == null ? null : entry.def().title, reason, by, meta);
    }

    private ScenarioArchive.Record archiveFile(File file, String owner, String path, String id, String title, String reason, String by, Meta meta)
            throws IOException {
        return _archive == null ? null : _archive.put(file, owner, path, id, title, reason, by, meta == null ? null : meta.copy());
    }

    /**
     * Deletes a scenario's file, having put it away in the archive first, and what is kept about it.
     * @return Its record in the archive; null if there is no archive.
     */
    public synchronized ScenarioArchive.Record delete(String owner, String path, String reason, String by) throws IOException {
        File file = fileFor(owner, path);
        ScenarioArchive.Record record = archive(owner, path, reason, by);
        Files.delete(file.toPath());
        prune(owner, file);
        putMeta(owner, path, null);
        reload(file);
        return record;
    }

    /**
     * Puts back what was archived, where it was.  Whatever is there now is archived in its turn, so that this can be
     * undone too.
     * @return The scenario as it is back.
     */
    public synchronized Entry restore(String key, String by) throws IOException {
        ScenarioArchive.Record record = _archive == null ? null : _archive.get(key);
        if (record == null)
            throw new IOException("Nothing was archived as " + key);
        if (record.path() == null || record.path().startsWith("campaigns/")) {
            //(a campaign file, which no scenario path can name: put back beside the scenarios it lists)
            File to = new File(new File(_canonicalFolder, "campaigns"), new File(record.path()).getName());
            if (to.isFile())
                archiveFile(to, null, record.path(), null, null, "replaced by putting back what was archived as " + key, by, null);
            Files.createDirectories(to.getParentFile().toPath());
            Files.copy(_archive.file(record).toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
            _archive.restored(record, by);
            reload(to);
            return null;
        }
        File to = fileFor(record.owner(), record.path());
        if (to.isFile())
            archive(record.owner(), record.path(), "replaced by putting back what was archived as " + key, by);
        Files.createDirectories(to.getParentFile().toPath());
        Files.copy(_archive.file(record).toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
        putMeta(record.owner(), record.path(), record.meta());
        _archive.restored(record, by);
        reload(to);
        return at(record.owner(), record.path());
    }

    /* ------------------------------------------------------------------ what is kept beside the files */

    private File metaFile(String owner) {
        return owner == null ? new File(_canonicalFolder, META) : new File(new File(new File(_userFolder, "users"), owner), META);
    }

    private Map<String, Meta> metaOf(String owner) {
        return _meta.computeIfAbsent(owner == null ? "" : owner, key -> {
            Map<String, Meta> all = new LinkedHashMap<>();
            File file = owner != null && _userFolder == null ? null : metaFile(owner);
            if (file == null || !file.isFile())
                return all;
            try {
                JSONObject json = JSON.parseObject(Files.readString(file.toPath(), StandardCharsets.UTF_8));
                for (String path : json.keySet())
                    all.put(path, json.getJSONObject(path).toJavaObject(Meta.class));
            } catch (IOException | RuntimeException exp) {
                LOG.error("What is kept about the scenarios in " + file + " cannot be read; it is taken to be nothing", exp);
            }
            return all;
        });
    }

    /** @return What is kept about a scenario (a copy, to be changed and put back); empty if nothing is. */
    public synchronized Meta meta(String owner, String path) {
        Meta meta = metaOf(owner).get(path);
        return meta == null ? new Meta() : meta.copy();
    }

    /** Keeps what is known about a scenario; null (or nothing in it) to keep nothing. */
    public synchronized void putMeta(String owner, String path, Meta meta) throws IOException {
        Map<String, Meta> all = metaOf(owner);
        if (meta == null || meta.empty())
            all.remove(path);
        else
            all.put(path, meta.copy());
        File file = metaFile(owner);
        if (all.isEmpty() && !file.exists())
            return;
        Files.createDirectories(file.getParentFile().toPath());
        File written = new File(file.getParentFile(), META + ".new");
        Files.writeString(written.toPath(), JSON.toJSONString(all), StandardCharsets.UTF_8);
        Files.move(written.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    /* ------------------------------------------------------------------ the repository's scenarios */

    /** Where the public folder and the repository disagree, for an admin to settle (see settle). */
    public synchronized List<Conflict> conflicts() {
        refresh();
        return _conflicts;
    }

    /**
     * Settles a difference between the public folder and the repository.
     * @param takeRepository True to take the repository's file (what the server has is archived); false to keep the
     *                       server's (or its having deleted it), until the repository changes it again.
     */
    public synchronized void settle(String path, boolean takeRepository, String by) throws IOException {
        refresh();
        Conflict conflict = null;
        for (Conflict one : _conflicts)
            if (one.path().equals(path))
                conflict = one;
        if (conflict == null)
            throw new IOException("There is no difference with the repository at " + path + " to settle");
        File shipped = new File(_shippedFolder, path.replace('/', File.separatorChar));
        File ours = new File(_canonicalFolder, path.replace('/', File.separatorChar));
        Map<String, String> manifest = readManifest();
        if (takeRepository) {
            if (ours.isFile()) {
                Entry entry = at(null, path);
                archiveFile(ours, null, path, entry == null ? null : entry.id(), entry == null || entry.def() == null ? null : entry.def().title,
                        "replaced by the repository's version", by, metaOf(null).get(path));
            }
            Files.createDirectories(ours.getParentFile().toPath());
            Files.copy(shipped.toPath(), ours.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        manifest.put(path, hash(shipped));
        writeManifest(manifest);
        _synced = null;
        reload(ours);
    }

    /** "path size time;" for each scenario file of a folder: what has to be the same for nothing to have changed. */
    private static String listing(File folder) {
        StringBuilder listing = new StringBuilder();
        if (folder == null || !folder.isDirectory())
            return "";
        try (Stream<Path> paths = Files.walk(folder.toPath(), MAX_DEPTH)) {
            paths.filter(Files::isRegularFile).filter(path -> path.getFileName().toString().endsWith(".hjson")).sorted()
                    .forEach(path -> listing.append(folder.toPath().relativize(path)).append(' ').append(path.toFile().length())
                            .append(' ').append(path.toFile().lastModified()).append(';'));
        } catch (IOException | RuntimeException exp) {
            LOG.error("Could not list " + folder, exp);
        }
        return listing.toString();
    }

    /** What a file says, whatever its line endings: a checkout on Windows has the same scenario as one anywhere else. */
    public static String hash(File file) throws IOException {
        try {
            String text = Files.readString(file.toPath(), StandardCharsets.UTF_8).replace("\r", "");
            StringBuilder hex = new StringBuilder();
            for (byte b : MessageDigest.getInstance("SHA-1").digest(text.getBytes(StandardCharsets.UTF_8)))
                hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException exp) {
            throw new IllegalStateException(exp);
        }
    }

    private Map<String, String> readManifest() {
        Map<String, String> manifest = new LinkedHashMap<>();
        File file = new File(_canonicalFolder, SHIPPED);
        if (!file.isFile())
            return manifest;
        try {
            JSONObject json = JSON.parseObject(Files.readString(file.toPath(), StandardCharsets.UTF_8));
            for (String path : json.keySet())
                manifest.put(path, json.getString(path));
        } catch (IOException | RuntimeException exp) {
            LOG.error("What the repository last had (" + file + ") cannot be read; it is taken to be nothing", exp);
        }
        return manifest;
    }

    private void writeManifest(Map<String, String> manifest) throws IOException {
        Files.createDirectories(_canonicalFolder.toPath());
        File written = new File(_canonicalFolder, SHIPPED + ".new");
        Files.writeString(written.toPath(), JSON.toJSONString(manifest), StandardCharsets.UTF_8);
        Files.move(written.toPath(), new File(_canonicalFolder, SHIPPED).toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    /**
     * Brings what the repository has into the public folder, without undoing anything done on the server: a file new
     * in the repository is copied in; one the repository has changed replaces the server's if the server's is still
     * what the repository last had (the old one is archived); anything else is a conflict, and is left as it is.
     * Nothing is ever deleted because the repository no longer has it.
     */
    private void syncShipped() {
        if (_userFolder == null || _shippedFolder == null || !_shippedFolder.isDirectory())
            return;
        String before = listing(_shippedFolder) + "|" + listing(_canonicalFolder);
        if (before.equals(_synced))
            return;
        try {
            Map<String, String> manifest = readManifest();
            boolean changed = false;
            List<Conflict> conflicts = new ArrayList<>();
            List<Path> shipped;
            try (Stream<Path> paths = Files.walk(_shippedFolder.toPath(), MAX_DEPTH)) {
                shipped = paths.filter(Files::isRegularFile).filter(path -> path.getFileName().toString().endsWith(".hjson")).sorted().toList();
            }
            for (Path path : shipped) {
                String relative = _shippedFolder.toPath().relativize(path).toString().replace(File.separatorChar, '/');
                File ours = new File(_canonicalFolder, relative.replace('/', File.separatorChar));
                String theirs = hash(path.toFile());
                String last = manifest.get(relative);
                if (!ours.isFile()) {
                    if (last == null) {
                        Files.createDirectories(ours.getParentFile().toPath());
                        Files.copy(path, ours.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        manifest.put(relative, theirs);
                        changed = true;
                        LOG.info("Scenario " + relative + " is new in the repository, and has been made public");
                    } else if (!last.equals(theirs)) {
                        conflicts.add(new Conflict(relative, "deleted"));
                    }
                    continue;
                }
                String mine = hash(ours);
                if (last == null) {
                    if (mine.equals(theirs)) {
                        manifest.put(relative, theirs);
                        changed = true;
                    } else {
                        conflicts.add(new Conflict(relative, "different"));
                    }
                } else if (last.equals(theirs)) {
                    //(the repository has not changed it since: whatever the server has done to it stands)
                } else if (mine.equals(last) || mine.equals(theirs)) {
                    if (!mine.equals(theirs)) {
                        archiveFile(ours, null, relative, null, null, "updated from the repository", null, metaOf(null).get(relative));
                        Files.copy(path, ours.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        LOG.info("Scenario " + relative + " was changed in the repository, and the public one has been updated");
                    }
                    manifest.put(relative, theirs);
                    changed = true;
                } else {
                    conflicts.add(new Conflict(relative, "changed"));
                }
            }
            if (changed)
                writeManifest(manifest);
            _conflicts = List.copyOf(conflicts);
            for (Conflict conflict : conflicts)
                LOG.warn("Scenario " + conflict.path() + " differs from the repository's (" + conflict.kind() + "): Scenario Admin can settle it");
        } catch (IOException | RuntimeException exp) {
            LOG.error("The repository's scenarios could not be brought into " + _canonicalFolder, exp);
        }
        _synced = listing(_shippedFolder) + "|" + listing(_canonicalFolder);
    }

    /** Whether there is a file where a scenario would be saved. */
    public synchronized boolean exists(String owner, String path) {
        try {
            return fileFor(owner, path).exists();
        } catch (IOException exp) {
            return false;
        }
    }

    /**
     * Authoring: writes a scenario's file, new or in place of the one that is there, and reads the folders again.
     * @param owner Whose scenario it is; null to write into the canonical folder.
     * @param path Where in that folder, with forward slashes: "tutorials/basics-02.hjson".
     * @return What the library now knows about the file.
     * @throws IOException if the file cannot be written, or the path is not one a scenario can be kept at.
     */
    public synchronized Entry save(String owner, String path, String hjson) throws IOException {
        return save(owner, path, hjson, "written over in the authoring tool", null);
    }

    /**
     * @param reason Why a public scenario's file that is there already is being written over: it is archived first.
     *               (A player's own scenario is written over as often as they save it, and is not.)
     * @param by Who is writing it.
     */
    public synchronized Entry save(String owner, String path, String hjson, String reason, String by) throws IOException {
        return save(owner, path, hjson, reason, by, false);
    }

    /**
     * @param unlockedOnly To refuse to write over a player's scenario that is in review (see locked), as one check with
     *                     the writing.
     */
    public synchronized Entry save(String owner, String path, String hjson, String reason, String by, boolean unlockedOnly) throws IOException {
        if (unlockedOnly && locked(owner, path))
            throw new IOException("it has been submitted for review, and cannot be changed until an admin has approved or rejected it.  "
                    + "To go on working on it, copy it in My Scenarios.");
        File file = fileFor(owner, path);
        if (owner != null && !file.exists() && countOf(owner) >= MAX_OWN)
            throw new IOException("You have " + MAX_OWN + " scenarios, which is as many as a player can keep.  Delete one you no longer need first.");
        if (owner == null && file.isFile() && !Files.readString(file.toPath(), StandardCharsets.UTF_8).equals(hjson))
            archive(null, path, reason, by);
        Files.createDirectories(file.getParentFile().toPath());
        Files.writeString(file.toPath(), hjson, StandardCharsets.UTF_8);

        //Read at once, and whatever the file's time and size say: it has changed
        reload(file);
        for (Entry entry : _entries)
            if (entry.path().equals(path) && java.util.Objects.equals(entry.owner(), owner))
                return entry;
        throw new IOException("The file was written but the library did not find it again: " + file);
    }

    /**
     * @return What turns the text of this scenario's messages into what a client shows.
     */
    public ScenarioText textFor(Entry entry) {
        return new ScenarioText(entry.def(), _library, entry.format(), _cardLookup);
    }

    @Override
    public synchronized void clearCache() {
        _files = new HashMap<>();
        _meta.clear();
        _synced = null;
        _loaded = false;
    }

    @Override
    public synchronized int getItemCount() {
        return _entries.size();
    }

    private void refresh() {
        long now = _clock.getAsLong();
        if (_loaded && now - _lastCheck < _checkIntervalMs)
            return;
        _lastCheck = now;
        try {
            load();
        } catch (RuntimeException exp) {
            //Keep serving what was loaded before rather than nothing
            LOG.error("Could not read the scenario folders", exp);
        }
        _loaded = true;
    }

    private record Found(File file, String path, String owner) { }

    private void load() {
        syncShipped();
        List<Found> found = new ArrayList<>();
        find(_canonicalFolder, null, found);
        if (_userFolder != null) {
            File[] owners = new File(_userFolder, "users").listFiles(File::isDirectory);
            if (owners != null)
                for (File ownerFolder : owners)
                    find(ownerFolder, ownerFolder.getName(), found);
        }
        found.sort(Comparator.comparing((Found file) -> file.owner() == null ? "" : file.owner()).thenComparing(Found::path));

        //Campaign files are few and small, and are read afresh every time the folders are looked at
        List<ScenarioCampaign> campaigns = new ArrayList<>();
        readCampaigns(_canonicalFolder, null, campaigns);
        if (_userFolder != null) {
            File[] owners = new File(_userFolder, "users").listFiles(File::isDirectory);
            if (owners != null)
                for (File ownerFolder : owners)
                    readCampaigns(ownerFolder, ownerFolder.getName(), campaigns);
        }
        campaigns.sort(Comparator.comparing((ScenarioCampaign campaign) -> campaign.owner() == null ? "" : campaign.owner())
                .thenComparingInt(ScenarioCampaign::order).thenComparing(ScenarioCampaign::id));
        _campaigns = List.copyOf(campaigns);

        Map<String, LoadedFile> files = new HashMap<>();
        boolean changed = found.size() != _files.size();
        for (Found file : found) {
            String key = file.file().getAbsolutePath();
            LoadedFile before = _files.get(key);
            long modified = file.file().lastModified();
            long length = file.file().length();
            if (before != null && before.modified() == modified && before.length() == length) {
                files.put(key, before);
            } else {
                files.put(key, new LoadedFile(modified, length, read(file)));
                changed = true;
            }
        }
        //(a file that is gone, where another was read afresh, can leave the count as it was)
        changed |= _reread || !files.keySet().equals(_files.keySet());
        _reread = false;
        if (!changed && _loaded)
            return;

        //Ids are settled afresh each time, since which file loses a clash depends on which files there are: the
        // first that can be played has the id, and a file that cannot be played only has it if none can
        Map<String, Entry> holders = new HashMap<>();
        for (boolean playable : new boolean[]{true, false})
            for (Found file : found) {
                Entry entry = files.get(file.file().getAbsolutePath()).entry();
                if (entry.id() != null && entry.playable() == playable)
                    holders.putIfAbsent(entry.id(), entry);
            }
        Map<String, Entry> byId = new LinkedHashMap<>();
        List<Entry> entries = new ArrayList<>();
        for (Found file : found) {
            Entry entry = files.get(file.file().getAbsolutePath()).entry();
            Entry holder = entry.id() == null ? null : holders.get(entry.id());
            if (holder != null && holder != entry) {
                List<String> errors = new ArrayList<>(entry.errors());
                errors.add("id: '" + entry.id() + "' is already the id of " + holder.path() + ".  Ids have to be unique");
                entry = new Entry(null, entry.path(), entry.owner(), entry.def(), entry.format(), errors, entry.warnings(), entry.report(), entry.leftOut());
            } else if (entry.id() != null) {
                byId.put(entry.id(), entry);
            }
            entries.add(entry);
        }
        _files = files;
        _byId = byId;
        _entries = List.copyOf(entries);
    }

    private static void readCampaigns(File folder, String owner, List<ScenarioCampaign> campaigns) {
        File[] files = folder == null ? null : new File(folder, "campaigns").listFiles((dir, name) -> name.endsWith(".hjson"));
        if (files == null)
            return;
        for (File file : files) {
            try {
                campaigns.add(ScenarioCampaign.parse(Files.readString(file.toPath(), StandardCharsets.UTF_8), owner));
            } catch (ScenarioFormatException | IOException | RuntimeException exp) {
                LOG.warn("Campaign " + (owner == null ? "" : owner + "/") + file.getName() + " cannot be read: " + exp.getMessage());
            }
        }
    }

    private static void find(File folder, String owner, List<Found> found) {
        if (folder == null || !folder.isDirectory())
            return;
        Path root = folder.toPath();
        try (Stream<Path> paths = Files.walk(root, MAX_DEPTH)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".hjson"))
                    .forEach(path -> {
                        String relative = root.relativize(path).toString().replace(File.separatorChar, '/');
                        if (!relative.startsWith("campaigns/"))
                            found.add(new Found(path.toFile(), relative, owner));
                    });
        } catch (IOException | RuntimeException exp) {
            LOG.error("Could not read the scenario folder " + folder, exp);
        }
    }

    private static final java.util.regex.Pattern NO_SUCH_CARD = java.util.regex.Pattern.compile("there is no card called '([A-Za-z0-9_]+)'");
    /** The parts of a file that are for people to read, in which a card's name may turn up as a word of the language. */
    private static final Set<String> PROSE = Set.of("text", "header", "messages", "note", "title", "name", "summary", "otherwise", "author", "tags");

    /**
     * The parser stops at the first thing wrong, and a card that was renamed or taken out by hand is usually named in
     * more places than one.  For "there is no card called X", this is every line of the file that names X where a
     * card would be named, so that they can all be put right in one go.
     * @return Something to add to the message; empty if there is nothing to add.
     */
    static String elsewhere(String source, String message) {
        java.util.regex.Matcher missing = message == null || source == null ? null : NO_SUCH_CARD.matcher(message);
        if (missing == null || !missing.find())
            return "";
        String alias = missing.group(1);
        java.util.regex.Pattern named = java.util.regex.Pattern.compile("(?<![A-Za-z0-9_])" + java.util.regex.Pattern.quote(alias) + "(?![A-Za-z0-9_])");
        java.util.regex.Pattern keyed = java.util.regex.Pattern.compile("^\"?([A-Za-z][A-Za-z0-9_]*)\"?\\s*:(.*)$");
        List<String> lines = new ArrayList<>();
        String[] all = source.split("\n", -1);
        for (int i = 0; i < all.length; i++) {
            String line = all[i].strip();
            if (line.startsWith("#") || line.startsWith("//"))
                continue;
            java.util.regex.Matcher key = keyed.matcher(line);
            //(a line with no "name:" in front is a message's words)
            if (!key.matches() || PROSE.contains(key.group(1)))
                continue;
            if (named.matcher(key.group(2)).find())
                lines.add(String.valueOf(i + 1));
        }
        if (lines.isEmpty())
            return "";
        return "  ('" + alias + "' is named on line" + (lines.size() == 1 ? " " : "s ") + String.join(", ", lines) + " of the file.)";
    }

    /** What is wrong with a scenario that its file reads as, beyond what the parser sees. */
    private List<String> problems(ScenarioDefinition def) {
        List<String> problems = new ArrayList<>();
        if (_formats.apply(def.format) == null)
            problems.add("format: there is no format called '" + def.format + "'");
        problems.addAll(ScenarioValidator.validate(def, _library));
        return problems;
    }

    private Entry read(Found file) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<ScenarioSalvage.LeftOut> leftOut = List.of();
        ScenarioDefinition def = null;
        LotroFormat format = null;
        ScenarioRunReport report = null;
        String id = null;
        String where = (file.owner() == null ? "" : file.owner() + "/") + file.path();
        String source = null;
        try {
            source = Files.readString(file.file().toPath(), StandardCharsets.UTF_8);
            try {
                def = ScenarioParser.parse(source);
                errors.addAll(problems(def));
            } catch (ScenarioFormatException exp) {
                errors.add(exp.getMessage());
            }
            if (!errors.isEmpty()) {
                //It cannot be played.  It may still be something the authoring tool can open, with its mistakes
                // left out and shown; and reading it that way finds every mistake rather than the first.
                ScenarioSalvage.Result salvaged = ScenarioSalvage.read(source, this::problems);
                if (salvaged.def() != null) {
                    def = salvaged.def();
                    leftOut = salvaged.leftOut();
                }
                if (salvaged.def() != null || salvaged.errors().size() > errors.size()) {
                    errors.clear();
                    errors.addAll(salvaged.errors());
                }
                //(and where else a card that is not declared is named, once for each such card)
                Set<String> said = new java.util.HashSet<>();
                for (int i = 0; i < errors.size(); i++) {
                    String also = elsewhere(source, errors.get(i));
                    if (!also.isEmpty() && said.add(also))
                        errors.set(i, errors.get(i) + also);
                }
            }
            if (def != null) {
                //A user's scenario is known by its owner's name as well, whatever the file says
                if (file.owner() != null)
                    def.id = file.owner() + "/" + def.id;
                id = def.id;
                format = _formats.apply(def.format);
            }

            if (errors.isEmpty()) {
                warnings.addAll(new ScenarioText(def, _library, format, _cardLookup).check(def));
                report = ScenarioRunner.run(def, _library, format);
            }
        } catch (IOException exp) {
            errors.add("The file could not be read: " + exp.getMessage());
        } catch (RuntimeException exp) {
            //Nothing a scenario file says should be able to do this, but one bad file must not stop the rest loading
            LOG.error("Scenario " + where + " could not be loaded", exp);
            errors.add("Loading it threw " + exp);
        }

        if (!errors.isEmpty())
            LOG.warn("Scenario " + where + " cannot be played: " + String.join("; ", errors));
        else if (report != null && !report.passed())
            LOG.warn("Scenario " + where + " does not play through:\n" + report);
        return new Entry(id, file.path(), file.owner(), def, format, List.copyOf(errors), List.copyOf(warnings), report, leftOut);
    }
}
