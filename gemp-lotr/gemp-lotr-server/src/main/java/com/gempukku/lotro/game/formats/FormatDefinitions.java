package com.gempukku.lotro.game.formats;

import com.gempukku.lotro.common.CardInfo;
import com.gempukku.lotro.common.JSONDefs;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.packs.SetDefinition;
import com.gempukku.lotro.logic.GameUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The data behind Help › Format Definitions (GET /hall/formats/json): one entry per hall format, in the Play menu's
 * order.  Each entry carries the same facts the Play popup's format (i) shows (the format JSON from
 * {@link LotroFormat#Serialize()}: setSummary, sites, cancelRingBearerSkirmish, ...), the id of its section on the
 * page ({@link FormatSummary#anchorId}), which errata it plays with, and its card lists (X-list, R-list, limits,
 * additional valid cards) with each card's name and set, so the page can group and draw them without a card lookup
 * per card.  The errata themselves are not listed: they live on Help › PC Errata.
 */
public final class FormatDefinitions {
    private FormatDefinitions() {
    }

    /** Live Player's Council errata of sets 0-19 are sets 50-69. */
    private static final int FIRST_PC_ERRATA_SET = 50;
    private static final int LAST_PC_ERRATA_SET = 69;

    /** The page's JSON: {"formats": [entry, ...]} for the given formats, sorted by their Play menu order. */
    public static Map<String, Object> describeAll(Collection<LotroFormat> formats, LotroCardBlueprintLibrary library) {
        List<LotroFormat> sorted = new ArrayList<>(formats);
        sorted.sort(Comparator.comparingInt(LotroFormat::getOrder).thenComparing(LotroFormat::getName));
        List<Map<String, Object>> entries = new ArrayList<>();
        for (LotroFormat format : sorted)
            entries.add(describe(format, library));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("formats", entries);
        return result;
    }

    /** One format's entry; see the class comment. */
    public static Map<String, Object> describe(LotroFormat format, LotroCardBlueprintLibrary library) {
        JSONDefs.Format facts = format.Serialize();
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("code", format.getCode());
        entry.put("name", format.getName());
        entry.put("anchor", FormatSummary.anchorId(format.getCode()));
        entry.put("description", facts.description);
        entry.put("setSummary", facts.setSummary);
        entry.put("sites", facts.sites);
        entry.put("cancelRingBearerSkirmish", facts.cancelRingBearerSkirmish);
        entry.put("minimumDeckSize", facts.minimumDeckSize);
        entry.put("maximumSameName", facts.maximumSameName);
        entry.put("validateShadowFPCount", facts.validateShadowFPCount);
        entry.put("winAtEndOfRegroup", facts.winAtEndOfRegroup);
        entry.put("discardPileIsPublic", facts.discardPileIsPublic);
        entry.put("winOnControlling5Sites", facts.winOnControlling5Sites);
        entry.put("ruleOfFour", facts.ruleOfFour);
        entry.put("mulliganRule", facts.mulliganRule);
        entry.put("usesMaps", facts.usesMaps);
        entry.put("playtest", facts.playtest);
        entry.put("errata", errata(format));

        Map<String, Object> lists = new LinkedHashMap<>();
        lists.put("banned", cards(format.getBannedCards(), library));
        lists.put("restricted", cards(format.getRestrictedCards(), library));
        lists.put("limit2", cards(format.getLimit2Cards(), library));
        lists.put("limit3", cards(format.getLimit3Cards(), library));
        lists.put("valid", cards(format.getValidCards(), library));
        entry.put("lists", lists);
        entry.put("restrictedNames", new ArrayList<>(format.getRestrictedCardNames()));
        return entry;
    }

    /**
     * Which errata the format plays with: {"pc": true} when it uses the live Player's Council errata (sets 50-69) in
     * place of the originals, {"playtest": true} when it uses playtest errata (sets 70-89 and 150-199), and
     * {"pcCardsLegal": true} when the PC errata sets are simply legal sets (alongside the originals, as in Anything
     * Goes).  All false for a format without errata.
     */
    public static Map<String, Object> errata(LotroFormat format) {
        boolean pc = false;
        boolean playtest = false;
        for (String errataId : format.getErrataCardMap().values()) {
            Integer set = setNumber(errataId);
            if (set == null)
                continue;
            if (set >= FIRST_PC_ERRATA_SET && set <= LAST_PC_ERRATA_SET)
                pc = true;
            else
                playtest = true;
        }
        boolean pcCardsLegal = false;
        for (String set : format.getValidSets() == null ? List.<String>of() : format.getValidSets()) {
            Integer number = numberPart(set, 0);
            if (number != null && number >= FIRST_PC_ERRATA_SET && number <= LAST_PC_ERRATA_SET)
                pcCardsLegal = true;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pc", pc);
        result.put("playtest", playtest);
        result.put("pcCardsLegal", pcCardsLegal);
        return result;
    }

    /**
     * A card list as {id, name, collInfo, set, setLabel, setName} entries in set and card-number order.  name has the
     * uniqueness dots and subtitle ("·Aragorn, Heir to the White City"); set is the set number, setLabel how players
     * write it ("1", "V1"), setName the set's name when known, collInfo the collector's info as printed ("1R40"; null
     * when the card has none).  A card the library does not know keeps its id as its name.
     */
    public static List<Map<String, Object>> cards(Collection<String> blueprintIds, LotroCardBlueprintLibrary library) {
        List<String> ids = new ArrayList<>(blueprintIds == null ? List.of() : blueprintIds);
        ids.sort(Comparator.comparing((String id) -> sortKey(id, 0)).thenComparing(id -> sortKey(id, 1)).thenComparing(id -> id));
        Map<String, SetDefinition> setDefinitions = library.getSetDefinitions();
        List<Map<String, Object>> result = new ArrayList<>();
        for (String id : ids) {
            Map<String, Object> card = new LinkedHashMap<>();
            card.put("id", id);
            String name = id;
            String collInfo = null;
            try {
                LotroCardBlueprint blueprint = library.getLotroCardBlueprint(id);
                name = GameUtils.getUniqueDots(blueprint.getUniqueRestriction()) + blueprint.getFullName();
                collInfo = collectorInfo(blueprint);
            } catch (CardNotFoundException | RuntimeException ignored) {
                // an id the library does not know is still listed, by id
            }
            card.put("name", name);
            card.put("collInfo", collInfo);
            Integer set = setNumber(id);
            card.put("set", set);
            card.put("setLabel", set == null ? null : FormatSummary.setLabel(set));
            SetDefinition setDefinition = set == null || setDefinitions == null ? null : setDefinitions.get(String.valueOf(set));
            card.put("setName", setDefinition == null ? null : setDefinition.getSetName());
            result.add(card);
        }
        return result;
    }

    /** The collector's info printed on the card ("1R29"; the V-sets' "V1_5"), or null when the card has none. */
    static String collectorInfo(LotroCardBlueprint blueprint) {
        CardInfo info = blueprint == null ? null : blueprint.getCardInfo();
        if (info == null || info.collInfo == null || info.collInfo.isBlank())
            return null;
        return info.collInfo.trim();
    }

    /** The set number of "1_45" (1), or null when the id has none. */
    static Integer setNumber(String blueprintId) {
        return numberPart(blueprintId, 0);
    }

    private static int sortKey(String blueprintId, int part) {
        Integer number = numberPart(blueprintId, part);
        return number == null ? Integer.MAX_VALUE : number;
    }

    private static Integer numberPart(String blueprintId, int part) {
        if (blueprintId == null)
            return null;
        String[] parts = blueprintId.split("_");
        if (parts.length <= part)
            return null;
        String digits = parts[part].replaceAll("[^0-9]", "");
        if (digits.isEmpty())
            return null;
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
