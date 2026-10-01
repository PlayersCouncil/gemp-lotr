package com.gempukku.lotro.game.formats;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.logic.GameUtils;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/** Help › Format Definitions data (GET /hall/formats/json), against the real formats and cards. */
public class FormatDefinitionsTest extends AbstractAtTest {

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> list(Map<String, Object> entry, String name) {
        return (List<Map<String, Object>>) ((Map<String, Object>) entry.get("lists")).get(name);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> errata(Map<String, Object> entry) {
        return (Map<String, Object>) entry.get("errata");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void everyHallFormatInPlayMenuOrderWithItsAnchorAndPlayPopupFacts() {
        var hall = _formatLibrary.getHallFormats();
        var entries = (List<Map<String, Object>>) FormatDefinitions.describeAll(hall.values(), _cardLibrary).get("formats");
        assertEquals(hall.size(), entries.size());
        int previousOrder = Integer.MIN_VALUE;
        for (Map<String, Object> entry : entries) {
            LotroFormat format = hall.get((String) entry.get("code"));
            assertNotNull(entry.get("code") + " is a hall format", format);
            assertTrue("sorted by order", format.getOrder() >= previousOrder);
            previousOrder = format.getOrder();
            assertEquals(format.getName(), entry.get("name"));
            assertEquals(FormatSummary.anchorId(format.getCode()), entry.get("anchor"));
            // the same facts the Play popup's (i) shows
            var facts = format.Serialize();
            assertEquals(facts.setSummary, entry.get("setSummary"));
            assertEquals(facts.sites, entry.get("sites"));
            assertEquals(facts.cancelRingBearerSkirmish, entry.get("cancelRingBearerSkirmish"));
            assertEquals(facts.minimumDeckSize, entry.get("minimumDeckSize"));
            assertEquals(facts.maximumSameName, entry.get("maximumSameName"));
            assertEquals(facts.validateShadowFPCount, entry.get("validateShadowFPCount"));
            assertEquals(format.getBannedCards().size(), list(entry, "banned").size());
            assertEquals(format.getRestrictedCards().size(), list(entry, "restricted").size());
            assertEquals(format.getValidCards().size(), list(entry, "valid").size());
        }
    }

    @Test
    public void errataIsAFlagNotAList() {
        var pcMovie = FormatDefinitions.describe(_formatLibrary.getFormat("pc_movie"), _cardLibrary);
        assertEquals(true, errata(pcMovie).get("pc"));
        assertEquals(false, errata(pcMovie).get("playtest"));
        assertEquals(false, errata(pcMovie).get("pcCardsLegal"));
        assertEquals("only the flags", 3, errata(pcMovie).size());
        assertFalse("no errata card list", pcMovie.toString().contains("51_"));

        var movie = FormatDefinitions.describe(_formatLibrary.getFormat("movie"), _cardLibrary);
        assertEquals(false, errata(movie).get("pc"));
        assertEquals(false, errata(movie).get("playtest"));
        assertEquals(false, errata(movie).get("pcCardsLegal"));

        // Anything Goes: the PC errata sets are legal sets, next to the originals, not errata applied to them
        var anythingGoes = FormatDefinitions.describe(_formatLibrary.getFormat("rev_tow_sta"), _cardLibrary);
        assertEquals(false, errata(anythingGoes).get("pc"));
        assertEquals(true, errata(anythingGoes).get("pcCardsLegal"));

        // no playtest errata cards exist today, so a format stands in: sets 70-89 and 150-199 are playtest errata
        LotroFormat playtest = Mockito.mock(LotroFormat.class);
        Mockito.when(playtest.getValidSets()).thenReturn(List.of("1", "101", "151"));
        Mockito.when(playtest.getErrataCardMap()).thenReturn(Map.of("1_2", "71_2", "101_3", "151_3"));
        assertEquals(Map.of("pc", false, "playtest", true, "pcCardsLegal", false), FormatDefinitions.errata(playtest));
        Mockito.when(playtest.getErrataCardMap()).thenReturn(Map.of("1_2", "51_2", "4_3", "74_3"));
        assertEquals(Map.of("pc", true, "playtest", true, "pcCardsLegal", false), FormatDefinitions.errata(playtest));
    }

    @Test
    public void cardListsCarryNameAndSetInSetOrder() throws Exception {
        var movie = FormatDefinitions.describe(_formatLibrary.getFormat("movie"), _cardLibrary);
        var banned = list(movie, "banned");
        assertEquals(21, banned.size());
        List<String> ids = new ArrayList<>();
        for (var card : banned)
            ids.add((String) card.get("id"));
        assertEquals(List.of("1_40", "1_45", "1_80", "1_108", "1_139", "1_195", "1_234", "1_248", "1_313", "2_32",
                "3_38", "3_42", "3_67", "3_68", "3_106", "3_108", "7_49", "7_96", "8_1", "10_2", "10_91"), ids);

        var first = banned.get(0);
        var blueprint = _cardLibrary.getLotroCardBlueprint("1_40");
        assertEquals(GameUtils.getUniqueDots(blueprint.getUniqueRestriction()) + blueprint.getFullName(), first.get("name"));
        assertEquals(1, first.get("set"));
        assertEquals("1", first.get("setLabel"));
        assertEquals(_cardLibrary.getSetDefinitions().get("1").getSetName(), first.get("setName"));
    }

    @Test
    public void cardListsCarryTheCollectorsInfo() throws Exception {
        var cards = FormatDefinitions.cards(List.of("1_40", "3_38", "101_5", "999_1"), _cardLibrary);
        assertEquals(_cardLibrary.getLotroCardBlueprint("1_40").getCardInfo().collInfo.trim(), cards.get(0).get("collInfo"));
        assertTrue(((String) cards.get(0).get("collInfo")).matches("1[CURPS]+40"));
        assertTrue(((String) cards.get(1).get("collInfo")).matches("3[CURPS]+38"));
        assertEquals(_cardLibrary.getLotroCardBlueprint("101_5").getCardInfo().collInfo.trim(), cards.get(2).get("collInfo"));
        assertTrue(cards.get(3).containsKey("collInfo"));
        assertNull(cards.get(3).get("collInfo"));
        for (var format : _formatLibrary.getHallFormats().values())
            for (var list : List.of(format.getBannedCards(), format.getRestrictedCards()))
                for (var card : FormatDefinitions.cards(list, _cardLibrary))
                    assertNotNull(card.get("id") + " collInfo", card.get("collInfo"));
    }

    @Test
    public void vSetsAreLabelledAndUnknownCardsKeepTheirId() {
        var cards = FormatDefinitions.cards(List.of("999_1", "101_5"), _cardLibrary);
        assertEquals("101_5", cards.get(0).get("id"));
        assertEquals("V1", cards.get(0).get("setLabel"));
        assertNotEquals("101_5", cards.get(0).get("name"));
        assertEquals("999_1", cards.get(1).get("name"));
        assertEquals(999, cards.get(1).get("set"));
        assertNull(cards.get(1).get("setName"));
        assertTrue(FormatDefinitions.cards(null, _cardLibrary).isEmpty());
    }
}
