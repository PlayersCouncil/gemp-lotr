package com.gempukku.lotro.scenario;

import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.LongSupplier;

/**
 * What players and admins do with scenario files, as opposed to writing them (ScenarioAuthoring): a player's own
 * scenarios (My Scenarios: copy, delete, submit for review), and the admins' review of what is submitted, and their
 * keeping of the public scenarios (Scenario Admin: approve, reject, disable, delete, restore from the archive).
 *
 * A player's scenario is in review when it has been submitted: it cannot then be changed, moved or deleted by its
 * author until an admin has approved or rejected it.  Approving it makes it public: a scenario linked to a public one
 * (it was copied from it, or was approved as it before) replaces that one, keeping its id and its place; any other
 * becomes a new public scenario.  Whatever a public scenario was before it was replaced is in the archive.
 */
public class ScenarioShelf {
    private static final Logger LOG = LogManager.getLogger(ScenarioShelf.class);

    public static final String SUBMITTED = "submitted";
    public static final String REJECTED = "rejected";
    public static final String APPROVED = "approved";
    /** How long what an admin asks to be changed may be. */
    public static final int MAX_FEEDBACK = 4000;

    /** Thrown when something cannot be done; its message says why, for the player. */
    public static class ShelfException extends Exception {
        public ShelfException(String message) {
            super(message);
        }
    }

    private final ScenarioLibrary _scenarios;
    private final LotroCardBlueprintLibrary _library;
    private final LongSupplier _clock;
    /** Told (owner, path) when a player's scenario is locked, so that a draft of it open in the authoring tool is closed. */
    private BiConsumer<String, String> _locked = (owner, path) -> { };

    public ScenarioShelf(ScenarioLibrary scenarios, LotroCardBlueprintLibrary library) {
        this(scenarios, library, System::currentTimeMillis);
    }

    public ScenarioShelf(ScenarioLibrary scenarios, LotroCardBlueprintLibrary library, LongSupplier clock) {
        _scenarios = scenarios;
        _library = library;
        _clock = clock;
    }

    public void onLocked(BiConsumer<String, String> locked) {
        _locked = locked;
    }

    /* ------------------------------------------------------------------ a player's own */

    /**
     * @return A player's scenarios, by kind, then campaign, then title: {path, id, fullId, title, kind, summary,
     * campaign:{id, title, position}|null, status, submittedAt, feedback, reviewedAt, reviewedBy, locked,
     * origin:{id, title, exists}|null, published: "current"|"changed"|null, playable, verified, openable, errors,
     * modified}.  'campaign' is the campaign of the scenario itself, or else of the public scenario it is linked to.
     * 'published' is for an approved scenario that is still linked to its public one: whether it is as it was
     * approved, or has been changed since.
     */
    public synchronized List<Map<String, Object>> mine(String player) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ScenarioLibrary.Entry entry : _scenarios.getAll()) {
            if (!player.equals(entry.owner()))
                continue;
            ScenarioLibrary.Meta meta = _scenarios.meta(player, entry.path());
            Map<String, Object> json = describe(entry);
            json.put("status", meta.status);
            json.put("submittedAt", meta.submittedAt);
            json.put("feedback", meta.feedback);
            json.put("reviewedAt", meta.reviewedAt);
            json.put("reviewedBy", meta.reviewedBy);
            json.put("locked", SUBMITTED.equals(meta.status));
            ScenarioLibrary.Entry origin = meta.origin == null ? null : _scenarios.get(meta.origin);
            if (meta.origin != null) {
                Map<String, Object> originJson = new LinkedHashMap<>();
                originJson.put("id", meta.origin);
                originJson.put("title", origin == null || origin.def() == null ? null : origin.def().title);
                originJson.put("exists", origin != null && origin.canonical());
                json.put("origin", originJson);
            } else {
                json.put("origin", null);
            }
            String published = null;
            if (APPROVED.equals(meta.status) && origin != null && origin.canonical()) {
                //(the public one has to be still as it was approved: not replaced since, from the repository, by an
                // admin or by another player's version)
                ScenarioLibrary.Meta originMeta = _scenarios.meta(null, origin.path());
                if ((player + "/" + entry.path()).equals(originMeta.approvedFrom)
                        && Objects.equals(originMeta.approvedHash, hashOf(null, origin.path())))
                    published = Objects.equals(meta.approvedHash, hashOf(player, entry.path())) ? "current" : "changed";
            }
            json.put("published", published);
            if (json.get("campaign") == null && origin != null && origin.canonical())
                json.put("campaign", campaign(origin.id()));
            result.add(json);
        }
        result.sort(ORDER);
        return result;
    }

    /** @return The public scenarios a player may take a copy of: those they are offered, and for an admin all that can be opened. */
    public synchronized List<Map<String, Object>> publicOnes(String player, boolean admin) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ScenarioLibrary.Entry entry : _scenarios.getAll()) {
            if (!entry.canonical() || !entry.openable())
                continue;
            if (!admin && !_scenarios.listedFor(entry, player, false))
                continue;
            Map<String, Object> json = describe(entry);
            json.put("author", entry.def().author);
            json.put("disabled", _scenarios.disabled(entry));
            result.add(json);
        }
        result.sort(ORDER);
        return result;
    }

    /**
     * Copies one of a player's scenarios: "Copy of" its title, an id of its own, a file beside it.  A scenario linked
     * to a public one gives a copy that is linked to it too (a submission that is in review can be copied to go on
     * working on it).
     * @return The copy.
     */
    public synchronized ScenarioLibrary.Entry copy(String player, String path) throws ShelfException {
        ScenarioLibrary.Entry entry = own(player, path);
        String id = freeId(player, ScenarioLibrary.fileId(entry).replaceAll("(-copy(-[0-9]+)?)+$", "") + "-copy");
        String title = "Copy of " + entry.def().title;
        ScenarioLibrary.Meta meta = new ScenarioLibrary.Meta();
        meta.origin = _scenarios.meta(player, path).origin;
        return write(player, freePath(player, beside(path, id)), retitled(read(player, path), id, title), meta);
    }

    /**
     * Copies a public scenario into a player's own folder, linked to it: submitted and approved, the copy replaces it.
     * It keeps its title and, if the player has no scenario of that id already, its id.
     * @return The copy.
     */
    public synchronized ScenarioLibrary.Entry copyPublic(String player, String id, boolean admin) throws ShelfException {
        ScenarioLibrary.Entry entry = _scenarios.get(id);
        if (entry == null || !entry.canonical() || !entry.openable() || (!admin && !_scenarios.listedFor(entry, player, false)))
            throw new ShelfException("There is no public scenario called '" + id + "'.");
        String ownId = freeId(player, entry.id());
        ScenarioLibrary.Meta meta = new ScenarioLibrary.Meta();
        meta.origin = entry.id();
        String text = read(null, entry.path());
        return write(player, freePath(player, ownId.equals(entry.id()) ? entry.path() : beside(entry.path(), ownId)),
                ownId.equals(entry.id()) ? text : retitled(text, ownId, entry.def().title), meta);
    }

    /**
     * The player's own copy of a public scenario, linked to it (as copyPublic makes it, or a version of it that was
     * submitted and sent back); null if they have none.  The first, if there are several.
     */
    public synchronized ScenarioLibrary.Entry ownCopy(String player, String id) {
        for (ScenarioLibrary.Entry entry : _scenarios.getAll())
            if (player.equals(entry.owner()) && id.equals(_scenarios.meta(player, entry.path()).origin))
                return entry;
        return null;
    }

    /**
     * Puts the public scenario over the player's own copy of it (see ownCopy), as copyPublic would have made it,
     * keeping the copy's file, id and what is noted about it.  With no copy, makes one.
     * @return The copy.
     */
    public synchronized ScenarioLibrary.Entry overwriteCopy(String player, String id, boolean admin) throws ShelfException {
        ScenarioLibrary.Entry entry = _scenarios.get(id);
        if (entry == null || !entry.canonical() || !entry.openable() || (!admin && !_scenarios.listedFor(entry, player, false)))
            throw new ShelfException("There is no public scenario called '" + id + "'.");
        ScenarioLibrary.Entry copy = ownCopy(player, id);
        if (copy == null)
            return copyPublic(player, id, admin);
        if (locked(player, copy.path()))
            throw new ShelfException("Your copy is in review, and cannot be changed until an admin has approved or rejected it.");
        String ownId = ScenarioLibrary.fileId(copy);
        String text = read(null, entry.path());
        //(what the copy had is kept in the archive, from which an admin can put it back)
        try {
            if (!read(player, copy.path()).equals(text))
                _scenarios.archive(player, copy.path(), "written over with the public version", player);
        } catch (IOException exp) {
            throw new ShelfException("Your copy could not be archived before it was written over: " + exp.getMessage());
        }
        return write(player, copy.path(), ownId == null || ownId.equals(entry.id()) ? text : retitled(text, ownId, entry.def().title),
                _scenarios.meta(player, copy.path()));
    }

    /** Deletes one of a player's scenarios (it goes to the archive, from which an admin can put it back). */
    public synchronized void delete(String player, String path) throws ShelfException {
        //(a file that cannot be read as a scenario, or whose id another has, can be deleted all the same)
        if (_scenarios.at(player, path) == null)
            throw new ShelfException("You have no scenario at " + path + ".");
        if (locked(player, path))
            throw new ShelfException("It is in review, and cannot be deleted until an admin has approved or rejected it.");
        try {
            _scenarios.delete(player, path, "deleted by its author", player);
        } catch (IOException exp) {
            throw new ShelfException("It could not be deleted: " + exp.getMessage());
        }
    }

    /**
     * Submits one of a player's scenarios for review.  It has to play through headless as it says it should; it is
     * locked from then until an admin has approved or rejected it.
     */
    public synchronized void submit(String player, String path) throws ShelfException {
        //(the library's lock is held from the check to the lock, so that the file checked is the file submitted: a
        // save in between waits, and then finds it locked)
        synchronized (_scenarios) {
            ScenarioLibrary.Entry entry = own(player, path);
            ScenarioLibrary.Meta meta = _scenarios.meta(player, path);
            if (SUBMITTED.equals(meta.status))
                throw new ShelfException("It has been submitted already.");
            if (!entry.playable())
                throw new ShelfException("It cannot be played as it is, and so cannot be submitted:\n" + String.join("\n", entry.errors()));
            if (!entry.verified())
                throw new ShelfException("It does not play through as it says it should, and so cannot be submitted"
                        + (entry.report() == null ? "." : ":\n" + entry.report()));
            meta.status = SUBMITTED;
            meta.submittedAt = _clock.getAsLong();
            meta.feedback = null;
            meta.reviewedAt = null;
            meta.reviewedBy = null;
            putMeta(player, path, meta);
        }
        _locked.accept(player, path);
    }

    /** Whether a player's scenario is in review, and so not to be changed by its author. */
    public boolean locked(String owner, String path) {
        return owner != null && SUBMITTED.equals(_scenarios.meta(owner, path).status);
    }

    /* ------------------------------------------------------------------ the admins' */

    /**
     * @return What Scenario Admin shows: {public:[...], conflicts:[{path, kind}], submissions:[...], archive:[...]}.
     * A public scenario: what describe gives, and {disabled, approvedFrom, approvedAt, approvedBy}.  A submission:
     * what describe gives, and {owner, submittedAt, replaces:{id, title, path}|null, clash:{id, title, path}|null}:
     * 'replaces' is the public scenario approving it replaces, 'clash' a public scenario of the same id that it is not
     * linked to (which stops it being approved as it is).  The archive: the latest records first, as many as 'limit'.
     */
    public synchronized Map<String, Object> adminState(int limit) {
        return adminState(limit, null);
    }

    /**
     * As adminState(limit), each public scenario saying, as 'ownCopy' ({id, title, path} or null), the admin's own
     * copy of it (see ownCopy).
     */
    public synchronized Map<String, Object> adminState(int limit, String admin) {
        List<Map<String, Object>> publics = new ArrayList<>();
        List<Map<String, Object>> submissions = new ArrayList<>();
        for (ScenarioLibrary.Entry entry : _scenarios.getAll()) {
            if (entry.canonical()) {
                Map<String, Object> json = describe(entry);
                ScenarioLibrary.Meta meta = _scenarios.meta(null, entry.path());
                json.put("disabled", meta.disabled);
                json.put("approvedFrom", meta.approvedFrom);
                json.put("approvedAt", meta.approvedAt);
                json.put("approvedBy", meta.approvedBy);
                json.put("author", entry.def() == null ? null : entry.def().author);
                ScenarioLibrary.Entry copy = admin == null || entry.id() == null ? null : ownCopy(admin, entry.id());
                json.put("ownCopy", copy == null ? null : brief(copy));
                publics.add(json);
                continue;
            }
            ScenarioLibrary.Meta meta = _scenarios.meta(entry.owner(), entry.path());
            if (!SUBMITTED.equals(meta.status))
                continue;
            Map<String, Object> json = describe(entry);
            json.put("owner", entry.owner());
            json.put("submittedAt", meta.submittedAt);
            ScenarioLibrary.Entry replaces = replaces(entry, meta);
            json.put("replaces", replaces == null ? null : brief(replaces));
            ScenarioLibrary.Entry clash = replaces == null ? _scenarios.get(targetId(entry, meta)) : null;
            json.put("clash", clash == null ? null : brief(clash));
            submissions.add(json);
        }
        publics.sort(ORDER);
        submissions.sort(Comparator.comparing(json -> (Long) json.get("submittedAt"), Comparator.nullsLast(Comparator.naturalOrder())));

        List<Map<String, Object>> conflicts = new ArrayList<>();
        for (ScenarioLibrary.Conflict conflict : _scenarios.conflicts()) {
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("path", conflict.path());
            json.put("kind", conflict.kind());
            conflicts.add(json);
        }

        List<Map<String, Object>> archive = new ArrayList<>();
        ScenarioArchive store = _scenarios.archive();
        List<ScenarioArchive.Record> records = store == null ? List.of() : store.list();
        for (ScenarioArchive.Record record : records) {
            if (archive.size() >= limit)
                break;
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("key", record.key());
            json.put("at", record.at());
            json.put("owner", record.owner());
            json.put("path", record.path());
            json.put("id", record.id());
            json.put("title", record.title());
            json.put("reason", record.reason());
            json.put("by", record.by());
            json.put("restoredAt", record.restoredAt());
            json.put("restoredBy", record.restoredBy());
            archive.add(json);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("public", publics);
        result.put("conflicts", conflicts);
        result.put("submissions", submissions);
        result.put("archive", archive);
        result.put("archived", records.size());
        result.put("hasArchive", store != null);
        return result;
    }

    /**
     * Approves a submission: it becomes public, in place of the public scenario it is linked to if there is one
     * (keeping that one's id and file), and as a new one otherwise.  The author's scenario is unlocked, and linked to
     * the public one from then on.
     * @return The public scenario.
     */
    public synchronized ScenarioLibrary.Entry approve(String owner, String path, String admin) throws ShelfException {
        ScenarioLibrary.Entry entry = submission(owner, path);
        if (!entry.verified())
            throw new ShelfException("It does not play through as it says it should (the cards or the server may have changed since it was "
                    + "submitted), and players are not offered a public scenario that does not.  Reject it, saying so.");
        ScenarioLibrary.Meta meta = _scenarios.meta(owner, path);
        ScenarioLibrary.Entry replaces = replaces(entry, meta);
        String id = targetId(entry, meta);
        String publicPath;
        ScenarioLibrary.Meta publicMeta;
        if (replaces != null) {
            publicPath = replaces.path();
            publicMeta = _scenarios.meta(null, publicPath);
        } else {
            ScenarioLibrary.Entry clash = _scenarios.get(id);
            if (clash != null)
                throw new ShelfException("There is a public scenario called '" + id + "' already (" + clash.path()
                        + "), which this is not a version of.  Reject it, asking for another id.");
            if (!id.matches("[a-z0-9][a-z0-9-]{1,60}"))
                throw new ShelfException("Its id, '" + id + "', is not one a public scenario can have: small letters, digits and dashes.");
            publicPath = freePublicPath(ScenarioAuthoring.filed(entry.path(), entry.def().kind));
            publicMeta = new ScenarioLibrary.Meta();
        }
        String text = read(owner, path);
        if (!id.equals(ScenarioLibrary.fileId(entry)))
            text = retitled(text, id, entry.def().title);
        ScenarioLibrary.Entry published;
        try {
            published = _scenarios.save(null, publicPath, text,
                    "replaced by approving " + owner + "/" + path, admin);
        } catch (IOException exp) {
            throw new ShelfException("It could not be made public: " + exp.getMessage());
        }
        long now = _clock.getAsLong();
        publicMeta.disabled = false;
        publicMeta.approvedFrom = owner + "/" + path;
        publicMeta.approvedAt = now;
        publicMeta.approvedBy = admin;
        publicMeta.approvedHash = hashOf(null, publicPath);
        putMeta(null, publicPath, publicMeta);

        meta.status = APPROVED;
        meta.reviewedAt = now;
        meta.reviewedBy = admin;
        meta.feedback = null;
        meta.origin = id;
        meta.approvedHash = hashOf(owner, path);
        putMeta(owner, path, meta);
        if (!published.playable())
            LOG.warn("Scenario " + owner + "/" + path + " was approved as " + publicPath + " but cannot be played there: " + published.errors());
        return published;
    }

    /** Rejects a submission, saying what is to be changed.  The author's scenario is unlocked. */
    public synchronized void reject(String owner, String path, String feedback, String admin) throws ShelfException {
        submission(owner, path);
        if (feedback != null && feedback.length() > MAX_FEEDBACK)
            throw new ShelfException("That is too long: " + MAX_FEEDBACK + " characters at most.");
        ScenarioLibrary.Meta meta = _scenarios.meta(owner, path);
        meta.status = REJECTED;
        meta.feedback = feedback == null || feedback.isBlank() ? null : feedback.strip();
        meta.reviewedAt = _clock.getAsLong();
        meta.reviewedBy = admin;
        putMeta(owner, path, meta);
    }

    /** Disables a public scenario (or enables it again): not offered on the Play screen, nor to be started from it. */
    public synchronized void disable(String id, boolean disabled) throws ShelfException {
        dataFolder();
        ScenarioLibrary.Entry entry = publicOne(id);
        ScenarioLibrary.Meta meta = _scenarios.meta(null, entry.path());
        meta.disabled = disabled;
        putMeta(null, entry.path(), meta);
    }

    /**
     * Deletes a public scenario's file (it goes to the archive, from which it can be put back).  By its path: a file
     * that cannot be read as a scenario, or whose id another has, can be deleted all the same.
     */
    public synchronized void deletePublic(String path, String admin) throws ShelfException {
        dataFolder();
        if (_scenarios.at(null, path) == null)
            throw new ShelfException("There is no public scenario at " + path + ".");
        try {
            _scenarios.delete(null, path, "deleted in Scenario Admin", admin);
        } catch (IOException exp) {
            throw new ShelfException("It could not be deleted: " + exp.getMessage());
        }
    }

    /** Puts back what is in the archive under a key; what is there now is archived in its turn. */
    public synchronized ScenarioLibrary.Entry restore(String key, String admin) throws ShelfException {
        try {
            return _scenarios.restore(key, admin);
        } catch (IOException exp) {
            throw new ShelfException("It could not be put back: " + exp.getMessage());
        }
    }

    /** Settles a difference between a public scenario and the repository's (see ScenarioLibrary.settle). */
    public synchronized void settle(String path, boolean takeRepository, String admin) throws ShelfException {
        try {
            _scenarios.settle(path, takeRepository, admin);
        } catch (IOException exp) {
            throw new ShelfException(exp.getMessage());
        }
    }

    /* ------------------------------------------------------------------ the rest */

    private static final List<String> KINDS = List.of("tutorial", "puzzle", "sandbox");

    @SuppressWarnings("unchecked")
    private static final Comparator<Map<String, Object>> ORDER = Comparator
            .comparing((Map<String, Object> json) -> {
                int kind = KINDS.indexOf((String) json.get("kind"));
                return kind < 0 ? KINDS.size() : kind;
            })
            .thenComparing(json -> json.get("campaign") == null ? "￿" : String.valueOf(((Map<String, Object>) json.get("campaign")).get("title")),
                    String.CASE_INSENSITIVE_ORDER)
            .thenComparing(json -> json.get("campaign") == null ? 0 : (Integer) ((Map<String, Object>) json.get("campaign")).get("position"))
            .thenComparing(json -> json.get("title") == null ? "" : (String) json.get("title"), String.CASE_INSENSITIVE_ORDER)
            .thenComparing(json -> (String) json.get("path"));

    /** {path, id, fullId, title, kind, summary, campaign, playable, verified, openable, errors, modified} */
    private Map<String, Object> describe(ScenarioLibrary.Entry entry) {
        ScenarioDefinition def = entry.def();
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("path", entry.path());
        json.put("id", ScenarioLibrary.fileId(entry));
        json.put("fullId", entry.id());
        json.put("title", def == null ? null : def.title);
        json.put("kind", def == null || def.kind == null ? null : def.kind.name().toLowerCase(Locale.ROOT));
        json.put("summary", def == null ? null : def.summary);
        json.put("campaign", entry.id() == null ? null : campaign(entry.id()));
        json.put("playable", entry.playable());
        json.put("verified", entry.verified());
        json.put("openable", entry.openable());
        json.put("errors", entry.errors());
        File file = fileOf(entry.owner(), entry.path());
        json.put("modified", file == null ? null : file.lastModified());
        return json;
    }

    private Map<String, Object> campaign(String id) {
        ScenarioCampaign campaign = _scenarios.campaignOf(id);
        if (campaign == null)
            return null;
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", campaign.id());
        json.put("title", campaign.title());
        json.put("position", campaign.scenarios().indexOf(id) + 1);
        return json;
    }

    private static Map<String, Object> brief(ScenarioLibrary.Entry entry) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", entry.id());
        json.put("title", entry.def() == null ? null : entry.def().title);
        json.put("path", entry.path());
        return json;
    }

    /** The public scenario a submission would replace; null if it would be a new one. */
    private ScenarioLibrary.Entry replaces(ScenarioLibrary.Entry entry, ScenarioLibrary.Meta meta) {
        if (meta.origin == null)
            return null;
        ScenarioLibrary.Entry origin = _scenarios.get(meta.origin);
        return origin != null && origin.canonical() ? origin : null;
    }

    /** The id a submission has once public: that of the public scenario it is a version of, or else its own. */
    private static String targetId(ScenarioLibrary.Entry entry, ScenarioLibrary.Meta meta) {
        return meta.origin != null ? meta.origin : ScenarioLibrary.fileId(entry);
    }

    private ScenarioLibrary.Entry own(String player, String path) throws ShelfException {
        ScenarioLibrary.Entry entry = _scenarios.at(player, path);
        if (entry == null)
            throw new ShelfException("You have no scenario at " + path + ".");
        if (entry.def() == null || entry.id() == null)
            throw new ShelfException("Its file cannot be read as a scenario:\n" + String.join("\n", entry.errors()));
        return entry;
    }

    private ScenarioLibrary.Entry submission(String owner, String path) throws ShelfException {
        ScenarioLibrary.Entry entry = owner == null ? null : _scenarios.at(owner, path);
        if (entry == null || !SUBMITTED.equals(_scenarios.meta(owner, path).status))
            throw new ShelfException("There is no submission of " + owner + "'s at " + path + ".  (Has another admin dealt with it?)");
        if (!entry.playable())
            throw new ShelfException("It cannot be played as it is:\n" + String.join("\n", entry.errors()));
        return entry;
    }

    /** Refuses what would change the repository's own folder, where there is no data folder (and no archive). */
    private void dataFolder() throws ShelfException {
        if (!_scenarios.hasUserFolder())
            throw new ShelfException("This server has no scenario data folder (scenarios.user.path), so the public scenarios are the "
                    + "repository's own files, and are not changed from here.");
    }

    private ScenarioLibrary.Entry publicOne(String id) throws ShelfException {
        ScenarioLibrary.Entry entry = _scenarios.get(id);
        if (entry == null || !entry.canonical())
            throw new ShelfException("There is no public scenario called '" + id + "'.");
        return entry;
    }

    private String read(String owner, String path) throws ShelfException {
        try {
            return _scenarios.read(owner, path);
        } catch (IOException exp) {
            throw new ShelfException("Its file cannot be read: " + exp.getMessage());
        }
    }

    private File fileOf(String owner, String path) {
        try {
            return _scenarios.file(owner, path);
        } catch (IOException exp) {
            return null;
        }
    }

    private String hashOf(String owner, String path) {
        try {
            return ScenarioLibrary.hash(_scenarios.file(owner, path));
        } catch (IOException exp) {
            return null;
        }
    }

    private ScenarioLibrary.Entry write(String player, String path, String text, ScenarioLibrary.Meta meta) throws ShelfException {
        try {
            ScenarioLibrary.Entry entry = _scenarios.save(player, path, text);
            putMeta(player, path, meta);
            return entry;
        } catch (IOException exp) {
            throw new ShelfException("It could not be written: " + exp.getMessage());
        }
    }

    private void putMeta(String owner, String path, ScenarioLibrary.Meta meta) throws ShelfException {
        try {
            _scenarios.putMeta(owner, path, meta);
        } catch (IOException exp) {
            throw new ShelfException("That could not be noted: " + exp.getMessage());
        }
    }

    /** An id none of a player's scenarios has: this one, or it with -2, -3 ... after it. */
    private String freeId(String player, String id) {
        String base = id.length() > 56 ? id.substring(0, 56).replaceAll("-+$", "") : id;
        Set<String> taken = new HashSet<>();
        for (ScenarioLibrary.Entry entry : _scenarios.getAll())
            if (player.equals(entry.owner()) && entry.id() != null)
                taken.add(ScenarioLibrary.fileId(entry));
        if (!taken.contains(base))
            return base;
        for (int n = 2; ; n++)
            if (!taken.contains(base + "-" + n))
                return base + "-" + n;
    }

    /** The path of a file named for an id, in the folder another is in. */
    private static String beside(String path, String id) {
        int slash = path.lastIndexOf('/');
        return (slash < 0 ? "" : path.substring(0, slash + 1)) + id + ".hjson";
    }

    private String freePath(String owner, String path) {
        String stem = path.substring(0, path.length() - ".hjson".length());
        String free = path;
        for (int n = 2; _scenarios.exists(owner, free); n++)
            free = stem + "-" + n + ".hjson";
        return free;
    }

    private String freePublicPath(String path) {
        return freePath(null, path);
    }

    /**
     * A scenario's file with another id and title.  The two lines are changed where they are, so that the rest of the
     * file (comments and all) is as it was; if that cannot be done, the scenario is written out afresh.
     */
    String retitled(String text, String id, String title) throws ShelfException {
        String changed = retitle(text, id, title);
        try {
            if (changed != null) {
                ScenarioDefinition def = ScenarioParser.parse(changed);
                if (id.equals(def.id) && Objects.equals(title, def.title))
                    return changed;
            }
            ScenarioDefinition def = ScenarioParser.parse(text);
            def.id = id;
            def.title = title;
            return ScenarioWriter.toHjson(def, _library);
        } catch (ScenarioFormatException | RuntimeException exp) {
            throw new ShelfException("Its file cannot be read as a scenario: " + exp.getMessage());
        }
    }

    /**
     * Sets the top-level id and title of an HJSON file in its text: each "id:" or "title:" line at the top level gets
     * the new value, and a title is added after the id if there is none.  It knows enough of HJSON to tell what is at
     * the top level (strings of all kinds, comments, values that run to the end of the line); the result is to be
     * checked by reading it.
     * @return The text changed; null if there is no top-level id line to change.
     */
    private static final java.util.regex.Pattern LITERAL = java.util.regex.Pattern.compile(
            "(-?[0-9]+(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?|true|false|null)\\s*(?:[,}\\]#]|//|$)");

    static String retitle(String text, String id, String title) {
        String[] lines = text.split("\n", -1);
        int[] depthAt = new int[lines.length];
        //(the depth at the start of each line; and where the root's braces put the top level)
        int depth = 0;
        int top = -1;
        java.util.ArrayDeque<Character> open = new java.util.ArrayDeque<>();
        boolean inMulti = false;
        boolean inBlock = false;
        for (int l = 0; l < lines.length; l++) {
            depthAt[l] = (inMulti || inBlock) ? -1 : depth;
            String line = lines[l];
            int i = 0;
            //(an element of an array, on a line of its own, that is not quoted or an object runs to the end of the line)
            if (!inMulti && !inBlock && !open.isEmpty() && open.peek() == '[') {
                String rest = line.stripLeading();
                if (!rest.isEmpty() && "{[\"']#/,".indexOf(rest.charAt(0)) < 0)
                    i = line.length();
            }
            while (i < line.length()) {
                char c = line.charAt(i);
                if (inMulti) {
                    int end = line.indexOf("'''", i);
                    if (end < 0) { i = line.length(); continue; }
                    inMulti = false;
                    i = end + 3;
                    continue;
                }
                if (inBlock) {
                    int end = line.indexOf("*/", i);
                    if (end < 0) { i = line.length(); continue; }
                    inBlock = false;
                    i = end + 2;
                    continue;
                }
                if (c == '#' || line.startsWith("//", i))
                    break;
                if (line.startsWith("/*", i)) { inBlock = true; i += 2; continue; }
                if (line.startsWith("'''", i)) { inMulti = true; i += 3; continue; }
                if (c == '"' || c == '\'') {
                    i++;
                    while (i < line.length() && line.charAt(i) != c)
                        i += line.charAt(i) == '\\' ? 2 : 1;
                    i++;
                    continue;
                }
                if (c == '{' || c == '[') {
                    if (top < 0 && c == '{' && depth == 0)
                        top = 1;
                    open.push(c);
                    depth++;
                    i++;
                    continue;
                }
                if (c == '}' || c == ']') {
                    if (!open.isEmpty())
                        open.pop();
                    depth--;
                    i++;
                    continue;
                }
                if (c == ':') {
                    int j = i + 1;
                    while (j < line.length() && (line.charAt(j) == ' ' || line.charAt(j) == '\t'))
                        j++;
                    if (j < line.length() && "{[\"'".indexOf(line.charAt(j)) < 0) {
                        //(a number, true, false or null ends where it ends, and a brace may follow it on the line;
                        // anything else runs to the end of the line, braces and all)
                        java.util.regex.Matcher literal = LITERAL.matcher(line).region(j, line.length());
                        if (!literal.lookingAt())
                            break;
                        i = literal.end(1);
                        continue;
                    }
                    i = j;
                    continue;
                }
                if (top < 0 && !Character.isWhitespace(c))
                    top = 0;
                i++;
            }
        }
        if (top < 0)
            top = 0;
        java.util.regex.Pattern key = java.util.regex.Pattern.compile("^(\\s*)\"?(id|title)\"?(\\s*:\\s*)(.*)$");
        int idLine = -1;
        boolean hasTitle = false;
        StringBuilder result = new StringBuilder();
        List<String> out = new ArrayList<>(List.of(lines));
        for (int l = 0; l < lines.length; l++) {
            if (depthAt[l] != top)
                continue;
            String cr = lines[l].endsWith("\r") ? "\r" : "";
            java.util.regex.Matcher m = key.matcher(lines[l].substring(0, lines[l].length() - cr.length()));
            if (!m.matches())
                continue;
            String value = m.group(2).equals("id") ? id : title;
            out.set(l, m.group(1) + m.group(2) + m.group(3) + org.hjson.JsonValue.valueOf(value).toString() + cr);
            if (m.group(2).equals("id"))
                idLine = l;
            else
                hasTitle = true;
        }
        if (idLine < 0)
            return null;
        if (!hasTitle && title != null) {
            String cr = lines[idLine].endsWith("\r") ? "\r" : "";
            java.util.regex.Matcher m = key.matcher(lines[idLine].substring(0, lines[idLine].length() - cr.length()));
            m.matches();
            out.add(idLine + 1, m.group(1) + "title: " + org.hjson.JsonValue.valueOf(title).toString() + cr);
        }
        for (int l = 0; l < out.size(); l++)
            result.append(l == 0 ? "" : "\n").append(out.get(l));
        return result.toString();
    }
}
