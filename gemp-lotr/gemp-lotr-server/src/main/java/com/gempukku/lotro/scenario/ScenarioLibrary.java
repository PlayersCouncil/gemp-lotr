package com.gempukku.lotro.scenario;

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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.stream.Stream;

/**
 * The scenarios the server can offer, read from two folders:
 *
 *     the canonical folder    the scenarios that ship with the game (gemp-lotr-cards, resources/scenarios).  Any
 *                             .hjson file in it or under it, except under campaigns/, which holds campaign files
 *                             (ScenarioCampaign): lists of scenarios to be played one after another.
 *     the user folder         scenarios that players have written, which are not part of the repository.  Only files
 *                             under users/&lt;player name&gt;/ are read; that player is the scenario's owner.
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
     */
    public record Entry(String id, String path, String owner, ScenarioDefinition def, LotroFormat format,
                        List<String> errors, List<String> warnings, ScenarioRunReport report) {
        public boolean canonical() {
            return owner == null;
        }

        /** Whether a game of it can be started. */
        public boolean playable() {
            return def != null && format != null && errors.isEmpty();
        }

        /** Whether the headless run proved as much of it as the scenario says it should. */
        public boolean verified() {
            return playable() && report != null && report.passed();
        }

        /**
         * Whether a player gets to see and play it.  Everybody gets the canonical scenarios that work.  A scenario's
         * owner and the admins get it as long as it can be started at all, working or not: that is how it gets fixed.
         */
        public boolean availableTo(String playerName, boolean admin) {
            if (!playable())
                return false;
            if (admin || (owner != null && owner.equals(playerName)))
                return true;
            return canonical() && verified();
        }
    }

    private record LoadedFile(long modified, long length, Entry entry) { }

    private final File _canonicalFolder;
    private final File _userFolder;
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
     * @param userFolder May be null, or not exist: then there are no user scenarios.
     * @param formats Finds a format by its code; returns null if there is none.
     * @param cardLookup Finds the card a [[card link]] names when it is not one of the scenario's own; may be null.
     */
    public ScenarioLibrary(File canonicalFolder, File userFolder, LotroCardBlueprintLibrary library, Function<String, LotroFormat> formats,
                           CardLookup cardLookup) {
        this(canonicalFolder, userFolder, library, formats, cardLookup, DEFAULT_CHECK_INTERVAL_MS, System::currentTimeMillis);
    }

    public ScenarioLibrary(File canonicalFolder, File userFolder, LotroCardBlueprintLibrary library, Function<String, LotroFormat> formats,
                           CardLookup cardLookup, long checkIntervalMs, LongSupplier clock) {
        _canonicalFolder = canonicalFolder;
        _userFolder = userFolder;
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
            if (entry.availableTo(playerName, admin))
                result.add(entry);
        return result;
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
            if (entry != null && entry.availableTo(playerName, admin)) {
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
        //A folder that had nothing else in it is not left behind (nor the one it was in, and so on up to the top)
        File top = owner == null ? _canonicalFolder : new File(new File(_userFolder, "users"), owner);
        for (File folder = from.getParentFile(); folder != null && !folder.equals(top) && folder.toPath().startsWith(top.toPath()); folder = folder.getParentFile()) {
            String[] left = folder.list();
            if (left == null || left.length > 0 || !folder.delete())
                break;
        }
        _files.remove(from.getAbsolutePath());
        _files.remove(to.getAbsolutePath());
        _lastCheck = _clock.getAsLong();
        load();
        _loaded = true;
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
        File file = fileFor(owner, path);
        Files.createDirectories(file.getParentFile().toPath());
        Files.writeString(file.toPath(), hjson, StandardCharsets.UTF_8);

        //Read at once, and whatever the file's time and size say: it has changed
        _files.remove(file.getAbsolutePath());
        _lastCheck = _clock.getAsLong();
        load();
        _loaded = true;
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
        if (!changed && _loaded)
            return;

        //Ids are settled afresh each time, since which file loses a clash depends on which files there are
        Map<String, Entry> byId = new LinkedHashMap<>();
        List<Entry> entries = new ArrayList<>();
        for (Found file : found) {
            Entry entry = files.get(file.file().getAbsolutePath()).entry();
            Entry holder = entry.id() == null ? null : byId.get(entry.id());
            if (holder != null) {
                List<String> errors = new ArrayList<>(entry.errors());
                errors.add("id: '" + entry.id() + "' is already the id of " + holder.path() + ".  Ids have to be unique");
                entry = new Entry(null, entry.path(), entry.owner(), entry.def(), entry.format(), errors, entry.warnings(), entry.report());
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

    private Entry read(Found file) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        ScenarioDefinition def = null;
        LotroFormat format = null;
        ScenarioRunReport report = null;
        String id = null;
        String where = (file.owner() == null ? "" : file.owner() + "/") + file.path();
        try {
            def = ScenarioParser.parse(Files.readString(file.file().toPath(), StandardCharsets.UTF_8));
            //A user's scenario is known by its owner's name as well, whatever the file says
            if (file.owner() != null)
                def.id = file.owner() + "/" + def.id;
            id = def.id;

            format = _formats.apply(def.format);
            if (format == null)
                errors.add("format: there is no format called '" + def.format + "'");
            errors.addAll(ScenarioValidator.validate(def, _library));

            if (errors.isEmpty()) {
                warnings.addAll(new ScenarioText(def, _library, format, _cardLookup).check(def));
                report = ScenarioRunner.run(def, _library, format);
            }
        } catch (ScenarioFormatException exp) {
            errors.add(exp.getMessage());
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
        return new Entry(id, file.path(), file.owner(), def, format, List.copyOf(errors), List.copyOf(warnings), report);
    }
}
