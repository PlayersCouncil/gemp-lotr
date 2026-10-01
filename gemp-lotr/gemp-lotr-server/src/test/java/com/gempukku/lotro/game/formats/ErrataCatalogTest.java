package com.gempukku.lotro.game.formats;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.common.AppConfig;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.util.JsonUtils;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import java.util.*;

import static org.junit.Assert.*;

/** The rows behind Help › PC Errata (GET /hall/errata/json: entries, formats, counts). */
public class ErrataCatalogTest extends AbstractAtTest {

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> entries(Map<String, Object> catalog) {
        return (List<Map<String, Object>>) catalog.get("entries");
    }

    private static Map<String, Object> entry(Map<String, Object> catalog, String id) {
        return entries(catalog).stream().filter(e -> id.equals(e.get("id"))).findFirst().orElse(null);
    }

    @Test
    public void errataBaseFollowsTheErrataSetRanges() {
        assertEquals("1_45", ErrataCatalog.errataBase("51_45"));
        assertEquals("0_7", ErrataCatalog.errataBase("50_7"));
        assertEquals("19_3", ErrataCatalog.errataBase("69_3"));
        assertEquals("3_68", ErrataCatalog.errataBase("73_68"));       // playtest errata
        assertEquals("101_2", ErrataCatalog.errataBase("151_2"));
        assertNull(ErrataCatalog.errataBase("1_45"));
        assertNull(ErrataCatalog.errataBase("103_19"));
        assertNull(ErrataCatalog.errataBase("49_1"));
        assertNull(ErrataCatalog.errataBase("90_1"));
        assertNull(ErrataCatalog.errataBase("x_1"));
        assertNull(ErrataCatalog.errataBase(null));
    }

    @Test
    public void everyErrataOfTheLibraryIsAnEntryWithBothVersions() {
        var catalog = ErrataCatalog.build(_cardLibrary, _formatLibrary);
        var byBase = new HashMap<String, Map<String, Object>>();
        for (var e : entries(catalog)) {
            if (!"revision".equals(e.get("kind")))
                byBase.put((String) e.get("base"), e);
        }
        // the legacy map (one per base card) and the rows agree
        for (var legacy : _cardLibrary.getErrata().entrySet()) {
            var e = byBase.get(legacy.getKey());
            assertNotNull("no row for " + legacy.getKey(), e);
            // the legacy map names the original card (an errata may respell it: Círdan / Cirdan)
            @SuppressWarnings("unchecked") var before = (Map<String, Object>) e.get("before");
            assertEquals(legacy.getValue().Name, before.get("name"));
        }
        for (var e : entries(catalog)) {
            String id = (String) e.get("id");
            assertNotNull(id + " after", e.get("after"));
            if ("revision".equals(e.get("kind"))) {
                assertEquals(id, e.get("base"));
                // the version it replaced, from the commented-out block in the card file
                assertNotNull(id + " has its earlier version", e.get("before"));
            } else {
                assertEquals(ErrataCatalog.errataBase(id), e.get("base"));
                assertNotNull(id + " before", e.get("before"));
            }
            assertNotNull(id + " type", e.get("type"));
            assertNotNull(id + " name", e.get("name"));
        }
    }

    @Test
    public void cleavingBlowRow() {
        var catalog = ErrataCatalog.build(_cardLibrary, _formatLibrary);
        var e = entry(catalog, "51_5");
        assertNotNull(e);
        assertEquals("1_5", e.get("base"));
        assertEquals("errata", e.get("kind"));
        assertEquals("Cleaving Blow", e.get("name"));
        assertEquals("1", e.get("set"));
        assertEquals("The Fellowship of the Ring", e.get("setName"));
        assertEquals(5, e.get("cardNum"));
        assertEquals("Dwarven", e.get("culture"));
        assertEquals("dwarven", e.get("cultureCode"));
        assertEquals("Free Peoples", e.get("side"));
        assertEquals("Event", e.get("type"));
        assertEquals(Boolean.FALSE, e.get("recent"));
        @SuppressWarnings("unchecked") var formats = (List<String>) e.get("formats");
        assertTrue(formats.containsAll(List.of("pc_fotr_block", "pc_movie", "pc_expanded")));
        assertFalse("not in the Decipher Fellowship Block", formats.contains("fotr_block"));
        assertFalse(formats.contains("movie"));
        @SuppressWarnings("unchecked") var before = (Map<String, Object>) e.get("before");
        @SuppressWarnings("unchecked") var after = (Map<String, Object>) e.get("after");
        assertTrue(((String) after.get("gametext")).contains("discard a Shadow item"));
        assertFalse(((String) before.get("gametext")).contains("discard a Shadow item"));
        @SuppressWarnings("unchecked") var stats = (Map<String, Object>) after.get("stats");
        assertEquals(1, stats.get("Twilight"));
        assertEquals(Boolean.FALSE, stats.get("Unique"));
    }

    @Test
    public void theWraithCultureIsLabelledRingwraith() {
        assertEquals("Ringwraith", ErrataCatalog.cultureLabel(com.gempukku.lotro.common.Culture.WRAITH));
        assertEquals("Dwarven", ErrataCatalog.cultureLabel(com.gempukku.lotro.common.Culture.DWARVEN));

        var catalog = ErrataCatalog.build(_cardLibrary, _formatLibrary);
        int wraith = 0;
        for (var e : entries(catalog)) {
            assertNotEquals(e.get("id") + " culture", "Wraith", e.get("culture"));
            if ("wraith".equals(e.get("cultureCode"))) {
                assertEquals(e.get("id") + " culture", "Ringwraith", e.get("culture"));
                wraith++;
            }
        }
        assertTrue("the library has errata'd Ringwraith cards", wraith > 0);
    }

    @Test
    public void formatsAreWhereTheErrataIsInEffect() {
        var catalog = ErrataCatalog.build(_cardLibrary, _formatLibrary);
        var hall = _formatLibrary.getHallFormats();
        for (var e : entries(catalog)) {
            String id = (String) e.get("id");
            String base = (String) e.get("base");
            @SuppressWarnings("unchecked") var formats = (List<String>) e.get("formats");
            for (LotroFormat format : hall.values()) {
                boolean expected = "revision".equals(e.get("kind"))
                        ? format.getValidSets().contains(base.split("_")[0]) || format.getValidCards().contains(id)
                        : id.equals(format.getErrataCardMap().get(base));
                assertEquals(id + " in " + format.getCode(), expected, formats.contains(format.getCode()));
            }
        }
        @SuppressWarnings("unchecked") var formatList = (List<Map<String, Object>>) catalog.get("formats");
        assertEquals(hall.size(), formatList.size());
        for (var f : formatList) {
            long count = entries(catalog).stream().filter(e -> ((List<?>) e.get("formats")).contains(f.get("code"))).count();
            assertEquals(f.get("code").toString(), (int) count, f.get("count"));
        }
    }

    @Test
    public void recentIsTheLatestPcErrataList() {
        var catalog = ErrataCatalog.build(_cardLibrary, _formatLibrary);
        var valid = new HashSet<>(_formatLibrary.getFormat("pc_errata").getValidCards());
        int recent = 0;
        for (var e : entries(catalog)) {
            boolean isRecent = (Boolean) e.get("recent");
            assertEquals(e.get("id").toString(), valid.contains(e.get("id")), isRecent);
            if (isRecent)
                recent++;
        }
        @SuppressWarnings("unchecked") var counts = (Map<String, Integer>) catalog.get("counts");
        assertEquals(recent, (int) counts.get("recent"));
        assertEquals(entries(catalog).size(), (int) counts.get("total"));
        assertEquals((int) counts.get("total"), counts.get("errata") + counts.get("playtest") + counts.get("revision"));
        assertEquals(_cardLibrary.getErrata().size(), counts.get("errata") + counts.get("playtest"));

        // the pre-WC 2026 batch, exactly
        var recentIds = new TreeSet<String>();
        for (var e : entries(catalog)) {
            if ((Boolean) e.get("recent"))
                recentIds.add((String) e.get("id"));
        }
        assertEquals(new TreeSet<>(List.of("52_46", "53_66", "60_20", "102_5", "103_35", "103_65", "103_83", "103_97")), recentIds);

        // a V-set card of the batch, changed in place
        var gloom = entry(catalog, "103_65");
        assertNotNull(gloom);
        assertEquals("revision", gloom.get("kind"));
        assertEquals(Boolean.TRUE, gloom.get("recent"));

        // a V-set revision of an earlier batch is still listed, just no longer "latest"
        var aragorn = entry(catalog, "103_19");
        assertNotNull(aragorn);
        assertEquals("revision", aragorn.get("kind"));
        assertEquals(Boolean.FALSE, aragorn.get("recent"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void preWcBatchComparesWithTheVersionEachReplaced() {
        var catalog = ErrataCatalog.build(_cardLibrary, _formatLibrary);
        // errata-set cards compare with the original (Decipher) card, as every errata row does
        var captain = entry(catalog, "52_46");
        assertEquals("errata", captain.get("kind"));
        assertEquals(2, captain.get("revision"));
        assertTrue(((String) ((Map<String, Object>) captain.get("after")).get("gametext")).contains("exert a unique Uruk-hai"));
        var strike = entry(catalog, "60_20");
        assertEquals("errata", strike.get("kind"));
        assertEquals("10_20", strike.get("base"));
        assertTrue(((String) ((Map<String, Object>) strike.get("after")).get("gametext")).contains("(limit once per phase)"));
        assertFalse(((String) ((Map<String, Object>) strike.get("before")).get("gametext")).contains("(limit once per phase)"));
        // in-place revisions compare with the revision they replaced
        var oath = entry(catalog, "102_5");
        assertEquals("revision", oath.get("kind"));
        assertTrue(((String) ((Map<String, Object>) oath.get("before")).get("gametext")).contains("(or 25 if in region 3)"));
        assertTrue(((String) ((Map<String, Object>) oath.get("after")).get("gametext")).contains("(or 25 if in regions 2 or 3)"));
        var sky = entry(catalog, "103_97");
        assertTrue(((String) ((Map<String, Object>) sky.get("before")).get("gametext")).contains("you may hinder this to add (1)"));
        assertFalse(((String) ((Map<String, Object>) sky.get("after")).get("gametext")).contains("hinder this"));
        var gloom = entry(catalog, "103_65");
        assertTrue(((String) ((Map<String, Object>) gloom.get("before")).get("gametext")).contains("remove (1) to take a twilight card"));
    }

    @Test
    public void sortedBySetThenNumberAndSerializable() {
        var catalog = ErrataCatalog.build(_cardLibrary, _formatLibrary);
        var list = entries(catalog);
        for (int i = 1; i < list.size(); i++) {
            var a = list.get(i - 1);
            var b = list.get(i);
            int sa = Integer.parseInt((String) a.get("set")), sb = Integer.parseInt((String) b.get("set"));
            assertTrue(sa < sb || (sa == sb && (Integer) a.get("cardNum") <= (Integer) b.get("cardNum")));
        }
        String json = JsonUtils.Serialize(catalog);
        assertTrue(json.contains("\"id\":\"51_5\""));
        assertTrue(json.contains("\"formats\":["));
    }

    @Test
    public void collectorInfoIsTheOriginalsPrintedNumber() {
        var catalog = ErrataCatalog.build(_cardLibrary, _formatLibrary);
        assertEquals("1U29", entry(catalog, "51_29").get("collInfo"));
        assertEquals("V3_19", entry(catalog, "103_19").get("collInfo"));
        for (var e : entries(catalog))
            assertNotNull(e.get("id") + " collInfo", e.get("collInfo"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void vSetRevisionsCompareWithTheVersionTheyReplaced() {
        var catalog = ErrataCatalog.build(_cardLibrary, _formatLibrary);
        int revisions = 0, textChanged = 0;
        for (var e : entries(catalog)) {
            if (!"revision".equals(e.get("kind")))
                continue;
            revisions++;
            var before = (Map<String, Object>) e.get("before");
            var after = (Map<String, Object>) e.get("after");
            assertNotNull(e.get("id") + " before", before);
            assertFalse(e.get("id") + " before text", ((String) before.get("gametext")).isBlank());
            assertNotNull(before.get("stats"));
            if (!before.get("gametext").equals(after.get("gametext")))
                textChanged++;
        }
        // 38 from the V3 errata batch, plus War-beacon, Gondor Calls For Aid! and Ritual Oath of Enmity from the pre-WC 2026 batch
        assertEquals(41, revisions);
        assertTrue("most revisions change the game text: " + textChanged, textChanged >= 30);

        var aragorn = entry(catalog, "103_19");
        var before = (Map<String, Object>) aragorn.get("before");
        assertTrue(((String) before.get("gametext")).startsWith("While you can spot another [Gondor] Man, Aragorn is strength +1."));
        assertEquals("Aragorn, King of Gondor and Arnor", before.get("name"));
        var stats = (Map<String, Object>) before.get("stats");
        assertEquals(5, stats.get("Twilight"));
        assertEquals(8, stats.get("Strength"));
        assertEquals(Boolean.TRUE, stats.get("Unique"));

        // without the card files there is nothing to compare with, and nothing breaks; the revisions of earlier
        // batches are only found through their card files, so just the current batch's 5 V-set revisions are listed
        var bare = ErrataCatalog.build(_cardLibrary, _formatLibrary, null);
        assertNull(entry(bare, "103_65").get("before"));
        assertNull(entry(bare, "103_19"));
        assertEquals(entries(catalog).size() - (41 - 5), entries(bare).size());
    }

    // describe(JsonObject) reads a card file's block the way the blueprint is built: check it on every live V-set card
    @Test
    public void describingACardBlockMatchesItsBlueprint() throws Exception {
        File dir = new File(AppConfig.getCardsPath(), "unofficial/pc/setv03");
        File[] files = dir.listFiles((d, name) -> name.endsWith(".hjson"));
        assertNotNull(files);
        int compared = 0;
        for (File file : files) {
            JsonObject cards = JsonValue.readHjson(Files.readString(file.toPath(), StandardCharsets.UTF_8)).asObject();
            for (var member : cards) {
                var blueprint = _cardLibrary.getLotroCardBlueprint(member.getName());
                var fromBlock = ErrataCatalog.describe(member.getValue().asObject());
                var built = ErrataCatalog.describe(blueprint);
                assertEquals(member.getName(), built, fromBlock);
                compared++;
            }
        }
        assertTrue("compared " + compared, compared > 100);
    }

    @Test
    public void commentedBlocksAreFoundAtTheirOwnIndentation() {
        var lines = List.of(
                "{",
                "//\t9_1: {",
                "//\t\tcardInfo: {",
                "//\t\t\t//id: 9_1.0",
                "//\t\t\trevision: 0",
                "//\t\t}",
                "//\t\ttitle: Old",
                "//\t\ttwilight: 2",
                "//\t\ttype: Condition",
                "//\t\tgametext: The old {text}.",
                "//\t}",
                "",
                "// a note: 9_2: {",
                "//\t9_3: {",
                "//\t\ttitle: Never closed",
                "\t9_1: {",
                "\t\ttitle: New",
                "\t}",
                "}");
        var parsed = ErrataCatalog.parseCommented(lines, "test");
        assertEquals(Set.of("9_1"), parsed.keySet());
        var old = ErrataCatalog.describe(parsed.get("9_1").get(0));
        assertEquals("Old", old.get("name"));
        assertEquals("Condition", old.get("type"));
        assertEquals("The old {text}.", old.get("gametext"));
        @SuppressWarnings("unchecked") var stats = (Map<String, Object>) old.get("stats");
        assertEquals(2, stats.get("Twilight"));
        assertEquals(Boolean.FALSE, stats.get("Unique"));
    }

    // an errata can change the card type: Still Draws Breath went from an Event to a Condition
    @Test
    @SuppressWarnings("unchecked")
    public void theCardTypeIsPartOfBothVersions() {
        var catalog = ErrataCatalog.build(_cardLibrary, _formatLibrary);
        var breath = entry(catalog, "51_25");
        assertEquals("Event", ((Map<String, Object>) breath.get("before")).get("type"));
        assertEquals("Condition", ((Map<String, Object>) breath.get("after")).get("type"));
        assertEquals("Condition", breath.get("type"));
        var cleaving = entry(catalog, "51_5");
        assertEquals("Event", ((Map<String, Object>) cleaving.get("before")).get("type"));
        assertEquals("Event", ((Map<String, Object>) cleaving.get("after")).get("type"));
        for (var e : entries(catalog)) {
            assertNotNull(e.get("id") + " after type", ((Map<String, Object>) e.get("after")).get("type"));
            var before = (Map<String, Object>) e.get("before");
            if (before != null)
                assertNotNull(e.get("id") + " before type", before.get("type"));
        }
    }

    // Beneath the Mountains (52_1) and Gimli, Bearer of Grudges (59_4) had no gametext in their errata definitions,
    // so the page showed their whole original text as removed
    @Test
    @SuppressWarnings("unchecked")
    public void everyErrataHasGameTextWhenTheOriginalDid() {
        var catalog = ErrataCatalog.build(_cardLibrary, _formatLibrary);
        List<String> missing = new ArrayList<>();
        for (var e : entries(catalog)) {
            var before = (Map<String, Object>) e.get("before");
            var after = (Map<String, Object>) e.get("after");
            if (before != null && !((String) before.get("gametext")).isBlank() && ((String) after.get("gametext")).isBlank())
                missing.add((String) e.get("id"));
        }
        assertEquals("errata without game text", List.of(), missing);
        var beneath = (Map<String, Object>) entry(catalog, "52_1").get("after");
        assertTrue((String) beneath.get("gametext"), ((String) beneath.get("gametext")).contains("Spot a Dwarf to discard the top 3 cards"));
    }
}
