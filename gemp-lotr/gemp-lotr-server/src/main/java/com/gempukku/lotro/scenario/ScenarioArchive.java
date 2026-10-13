package com.gempukku.lotro.scenario;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Where a scenario's file goes when it is replaced or deleted, so that an accident can be undone.
 *
 * Each thing put away is a folder of its own, named for when it was put away:
 *
 *     archive/20261013-192103-120-1/scenario.hjson    the file as it was
 *     archive/20261013-192103-120-1/record.json       what it was, where it was, why it went, who sent it
 *
 * Nothing in here is ever deleted by the server.  It is plain files, to be looked through (or cleared out) by hand
 * as well as through Scenario Admin.
 */
public class ScenarioArchive {
    private static final Logger LOG = LogManager.getLogger(ScenarioArchive.class);
    private static final String FILE = "scenario.hjson";
    private static final String RECORD = "record.json";

    /**
     * What was put away.
     * @param key The name of its folder: what it is asked for by.
     * @param at When it was put away (milliseconds).
     * @param owner Whose scenario it was; null for a public one.
     * @param path Where its file was, within its folder.
     * @param id What the scenario was known as (a user's: without the owner's name in front).
     * @param title Its title, for people to recognise it by.
     * @param reason Why it was put away: "deleted", "replaced by ...", and so on.
     * @param by Who did what put it away; null for the server itself (an update from the repository).
     * @param meta What was kept about it beside the file (its status, its link to a public scenario, ...).
     * @param restoredAt When it was last put back, if it has been (milliseconds); null if never.
     * @param restoredBy Who put it back.
     */
    public record Record(String key, long at, String owner, String path, String id, String title, String reason, String by,
                         ScenarioLibrary.Meta meta, Long restoredAt, String restoredBy) { }

    private final File _folder;
    private final AtomicInteger _count = new AtomicInteger();

    public ScenarioArchive(File folder) {
        _folder = folder;
    }

    /**
     * Copies a file in, with what is known about it.
     * @return Its record.
     */
    public synchronized Record put(File file, String owner, String path, String id, String title, String reason, String by,
                                   ScenarioLibrary.Meta meta) throws IOException {
        long now = System.currentTimeMillis();
        SimpleDateFormat stamp = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
        stamp.setTimeZone(TimeZone.getTimeZone("UTC"));
        File into;
        String key;
        do {
            key = stamp.format(new Date(now)) + "-" + _count.incrementAndGet();
            into = new File(_folder, key);
        } while (into.exists());
        Files.createDirectories(into.toPath());
        Files.copy(file.toPath(), new File(into, FILE).toPath(), StandardCopyOption.REPLACE_EXISTING);
        Record record = new Record(key, now, owner, path, id, title, reason, by, meta, null, null);
        write(into, record);
        return record;
    }

    /** Everything that has been put away, the latest first. */
    public synchronized List<Record> list() {
        List<Record> records = new ArrayList<>();
        File[] folders = _folder.listFiles(File::isDirectory);
        if (folders == null)
            return records;
        for (File folder : folders) {
            Record record = read(folder);
            if (record != null)
                records.add(record);
        }
        records.sort(Comparator.comparingLong(Record::at).reversed().thenComparing(Record::key, Comparator.reverseOrder()));
        return records;
    }

    /** @return The record put away under a key; null if there is none. */
    public synchronized Record get(String key) {
        if (key == null || !key.matches("[0-9A-Za-z_-]+"))
            return null;
        File folder = new File(_folder, key);
        return folder.isDirectory() ? read(folder) : null;
    }

    /** The file as it was put away. */
    public File file(Record record) {
        return new File(new File(_folder, record.key()), FILE);
    }

    /** Notes that it has been put back (it stays in the archive: it can be put back again). */
    public synchronized void restored(Record record, String by) throws IOException {
        write(new File(_folder, record.key()), new Record(record.key(), record.at(), record.owner(), record.path(), record.id(),
                record.title(), record.reason(), record.by(), record.meta(), System.currentTimeMillis(), by));
    }

    private static void write(File folder, Record record) throws IOException {
        JSONObject json = new JSONObject();
        json.put("at", record.at());
        json.put("owner", record.owner());
        json.put("path", record.path());
        json.put("id", record.id());
        json.put("title", record.title());
        json.put("reason", record.reason());
        json.put("by", record.by());
        json.put("meta", record.meta() == null ? null : JSON.toJSON(record.meta()));
        json.put("restoredAt", record.restoredAt());
        json.put("restoredBy", record.restoredBy());
        Files.writeString(new File(folder, RECORD).toPath(), json.toJSONString(), StandardCharsets.UTF_8);
    }

    private static Record read(File folder) {
        File file = new File(folder, RECORD);
        if (!file.isFile() || !new File(folder, FILE).isFile())
            return null;
        try {
            JSONObject json = JSON.parseObject(Files.readString(file.toPath(), StandardCharsets.UTF_8));
            JSONObject meta = json.getJSONObject("meta");
            return new Record(folder.getName(), json.getLongValue("at"), json.getString("owner"), json.getString("path"),
                    json.getString("id"), json.getString("title"), json.getString("reason"), json.getString("by"),
                    meta == null ? null : meta.toJavaObject(ScenarioLibrary.Meta.class),
                    json.getLong("restoredAt"), json.getString("restoredBy"));
        } catch (IOException | RuntimeException exp) {
            LOG.warn("The archived scenario in " + folder + " cannot be read: " + exp.getMessage());
            return null;
        }
    }
}
