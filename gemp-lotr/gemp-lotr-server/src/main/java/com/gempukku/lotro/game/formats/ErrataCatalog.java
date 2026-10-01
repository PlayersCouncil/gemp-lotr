package com.gempukku.lotro.game.formats;

import com.gempukku.lotro.common.AppConfig;
import com.gempukku.lotro.common.CardInfo;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.GameText;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.packs.SetDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The rows behind Help › PC Errata (GET /hall/errata/json, key "entries"), one per errata'd card, with what the page
 * filters and compares on: the card's set, culture, side and type, the hall formats the errata is in effect in, whether
 * it is in the current batch (the {@code pc_errata} format's valid list), and the stats and game text before and after.
 * <p>
 * Which blueprints count follows {@link LotroCardBlueprintLibrary#getErrata()}: sets 50-69 (and 150-199) are the live
 * PC errata of sets 0-19 (100-149), 70-89 the playtest errata of 0-19.  A card whose id is its own base (the V-sets
 * revise their cards in place, e.g. 103_19) is a "revision": Gemp only plays its current version, but the card files
 * keep the one it replaced as a commented-out block right above it ({@code //\t103_19: { ... //\t}}), which is read
 * (see {@link #commentedVersions}) for its "before".  Such a card is listed while it is in the current batch, and
 * afterwards for as long as its card file keeps that earlier version (so replacing the batch does not drop it).
 */
public final class ErrataCatalog {
    public static final String KIND_ERRATA = "errata";
    public static final String KIND_PLAYTEST = "playtest";
    public static final String KIND_REVISION = "revision";

    public static final String RECENT_FORMAT = "pc_errata";

    private static final Logger LOGGER = LogManager.getLogger(ErrataCatalog.class);

    /**
     * The culture's English name as this page shows it (tooltip, sort, frame heading): the Culture enum's label, except
     * that WRAITH reads "Ringwraith", the culture's name in play.  The enum's "Wraith" is left alone: it is shared with
     * card parsing and the rest of the server.
     */
    static String cultureLabel(Culture culture) {
        return culture == Culture.WRAITH ? "Ringwraith" : culture.getHumanReadable();
    }

    private ErrataCatalog() {
    }

    /**
     * @return the base id an errata blueprint id stands for ("51_45" -> "1_45"), or null when the id is not in an
     * errata set.
     */
    public static String errataBase(String blueprintId) {
        if (blueprintId == null)
            return null;
        var parts = blueprintId.split("_");
        if (parts.length != 2)
            return null;
        int set;
        try {
            set = Integer.parseInt(parts[0]);
        } catch (NumberFormatException exp) {
            return null;
        }
        int baseSet = baseSet(set);
        if (baseSet < 0)
            return null;
        return baseSet + "_" + parts[1];
    }

    private static int baseSet(int set) {
        if (set >= 50 && set <= 69)
            return set - 50;
        if (set >= 70 && set <= 89)
            return set - 70;
        if (set >= 150 && set <= 199)
            return set - 50;
        return -1;
    }

    private static String kindOf(String blueprintId) {
        int set = Integer.parseInt(blueprintId.split("_")[0]);
        if (set >= 70 && set <= 89)
            return KIND_PLAYTEST;
        return KIND_ERRATA;
    }

    /**
     * Everything the page needs besides the legacy "all"/"recent" maps: {@code entries} (sorted by base set, then card
     * number), {@code formats} (every hall format, in hall order, with how many entries apply in it) and
     * {@code counts} (by kind).
     */
    public static Map<String, Object> build(LotroCardBlueprintLibrary library, LotroFormatLibrary formatLibrary) {
        return build(library, formatLibrary, AppConfig.getCardsPath());
    }

    /**
     * @param cardsPath the card definitions the library was loaded from, searched for the earlier versions of in-place
     *                  revisions (null: none)
     */
    public static Map<String, Object> build(LotroCardBlueprintLibrary library, LotroFormatLibrary formatLibrary, File cardsPath) {
        LotroFormat recentFormat = formatLibrary.getFormat(RECENT_FORMAT);
        Set<String> recent = recentFormat == null ? Set.of() : new HashSet<>(recentFormat.getValidCards());

        List<LotroFormat> hallFormats = new ArrayList<>(formatLibrary.getHallFormats().values());
        hallFormats.sort(Comparator.comparingInt(LotroFormat::getOrder).thenComparing(LotroFormat::getName));

        Map<String, SetDefinition> setDefinitions = library.getSetDefinitions();

        // errata ids, then the in-place revisions: those of the current batch, and any earlier ones whose card file
        // still keeps the version they replaced (so a revision stays listed after it drops out of the batch)
        Map<String, String> idToBase = new TreeMap<>();
        Map<String, LotroCardBlueprint> baseCards = library.getBaseCards();
        for (String id : new ArrayList<>(baseCards.keySet())) {
            String base = errataBase(id);
            if (base != null)
                idToBase.put(id, base);
        }
        for (String id : recent) {
            if (!idToBase.containsKey(id) && errataBase(id) == null)
                idToBase.put(id, id);
        }
        Map<String, List<JsonObject>> earlierVersions = commentedVersions(cardsPath);
        for (String id : earlierVersions.keySet()) {
            if (idToBase.containsKey(id) || errataBase(id) != null)
                continue;
            LotroCardBlueprint card = baseCards.get(id);
            if (card != null && card.getCardInfo() != null && card.getCardInfo().revision > 0
                    && previousVersion(earlierVersions.get(id), card.getCardInfo().revision) != null)
                idToBase.put(id, id);
        }

        List<Map<String, Object>> entries = new ArrayList<>();
        for (Map.Entry<String, String> idAndBase : idToBase.entrySet()) {
            String id = idAndBase.getKey();
            String base = idAndBase.getValue();
            LotroCardBlueprint card;
            LotroCardBlueprint original = null;
            try {
                card = library.getLotroCardBlueprint(id);
                if (!base.equals(id))
                    original = library.getLotroCardBlueprint(base);
            } catch (CardNotFoundException exp) {
                // an errata candidate without an official counterpart (see getErrata()), or a stale batch entry
                continue;
            }

            String kind = base.equals(id) ? KIND_REVISION : kindOf(id);
            String[] baseParts = base.split("_");

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", id);
            entry.put("base", base);
            entry.put("kind", kind);
            entry.put("name", card.getFullName());
            entry.put("title", card.getTitle());
            if (card.getSubtitle() != null)
                entry.put("subtitle", card.getSubtitle());
            entry.put("set", baseParts[0]);
            SetDefinition setDefinition = setDefinitions.get(baseParts[0]);
            if (setDefinition != null)
                entry.put("setName", setDefinition.getSetName());
            entry.put("cardNum", parseIntOr(baseParts[1], 0));
            // the collector's info printed on the card ("1U29"): the original's, as the table lists originals
            String collInfo = collectorInfo(original);
            if (collInfo == null)
                collInfo = collectorInfo(card);
            if (collInfo != null)
                entry.put("collInfo", collInfo);
            Culture culture = card.getCulture();
            if (culture != null) {
                entry.put("culture", cultureLabel(culture));
                entry.put("cultureCode", culture.name().toLowerCase(Locale.ROOT));
            }
            entry.put("side", humanReadable(card.getSide()));
            entry.put("type", humanReadable(card.getCardType()));
            if (card.getCardInfo() != null)
                entry.put("revision", card.getCardInfo().revision);
            entry.put("recent", recent.contains(id));

            List<String> formats = new ArrayList<>();
            for (LotroFormat format : hallFormats) {
                if (appliesIn(format, id, base, kind))
                    formats.add(format.getCode());
            }
            entry.put("formats", formats);

            entry.put("after", describe(card));
            if (original != null) {
                entry.put("before", describe(original));
            } else if (KIND_REVISION.equals(kind)) {
                JsonObject earlier = previousVersion(earlierVersions.get(id), entry.get("revision"));
                if (earlier != null)
                    entry.put("before", describe(earlier));
            }

            entries.add(entry);
        }

        entries.sort(Comparator
                .comparingInt((Map<String, Object> e) -> parseIntOr((String) e.get("set"), 0))
                .thenComparingInt(e -> (Integer) e.get("cardNum"))
                .thenComparing(e -> (String) e.get("id")));

        List<Map<String, Object>> formatList = new ArrayList<>();
        for (LotroFormat format : hallFormats) {
            int count = 0;
            for (Map<String, Object> entry : entries) {
                if (((List<?>) entry.get("formats")).contains(format.getCode()))
                    count++;
            }
            Map<String, Object> formatInfo = new LinkedHashMap<>();
            formatInfo.put("code", format.getCode());
            formatInfo.put("name", format.getName());
            formatInfo.put("count", count);
            formatList.add(formatInfo);
        }

        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("total", entries.size());
        for (String kind : List.of(KIND_ERRATA, KIND_PLAYTEST, KIND_REVISION))
            counts.put(kind, 0);
        int recentCount = 0;
        for (Map<String, Object> entry : entries) {
            counts.merge((String) entry.get("kind"), 1, Integer::sum);
            if (Boolean.TRUE.equals(entry.get("recent")))
                recentCount++;
        }
        counts.put("recent", recentCount);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("entries", entries);
        result.put("formats", formatList);
        result.put("counts", counts);
        return result;
    }

    /**
     * An errata is in effect in a format that maps its base card to it; an in-place revision wherever its set is legal.
     */
    static boolean appliesIn(LotroFormat format, String id, String base, String kind) {
        if (KIND_REVISION.equals(kind))
            return format.getValidSets().contains(base.split("_")[0]) || format.getValidCards().contains(id);
        return id.equals(format.getErrataCardMap().get(base));
    }

    static Map<String, Object> describe(LotroCardBlueprint card) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", card.getFullName());
        CardType type = card.getCardType();
        // the card type too: an errata can change it (Still Draws Breath: Event -> Condition)
        result.put("type", humanReadable(type));
        Map<String, Object> stats = new LinkedHashMap<>();
        if (type != CardType.THE_ONE_RING)
            stats.put("Twilight", card.getTwilightCost());
        if (card.getStrength() != 0)
            stats.put("Strength", card.getStrength());
        if (card.getVitality() != 0)
            stats.put("Vitality", card.getVitality());
        if (card.getResistance() != 0)
            stats.put("Resistance", card.getResistance());
        if (card.getSiteNumber() != 0)
            stats.put("Site", card.getSiteNumber());
        stats.put("Unique", card.isUnique());
        result.put("stats", stats);
        result.put("gametext", card.getGameText() == null ? "" : card.getGameText());
        return result;
    }

    private static String collectorInfo(LotroCardBlueprint card) {
        if (card == null)
            return null;
        CardInfo info = card.getCardInfo();
        if (info == null || info.collInfo == null || info.collInfo.isBlank())
            return null;
        return info.collInfo.trim();
    }

    /**
     * {@link #describe(LotroCardBlueprint)} for a card definition that is not loaded (an earlier version kept in a
     * comment): the same keys, read the way the blueprint builder reads them.
     */
    static Map<String, Object> describe(JsonObject card) {
        Map<String, Object> result = new LinkedHashMap<>();
        String title = string(card.get("title"));
        String subtitle = string(card.get("subtitle"));
        result.put("name", subtitle == null ? title : title + ", " + subtitle);
        String type = string(card.get("type"));
        boolean ring = type != null && type.trim().replace(' ', '_').equalsIgnoreCase(CardType.THE_ONE_RING.name());
        if (type != null && !type.isBlank())
            result.put("type", cardTypeName(type));
        Map<String, Object> stats = new LinkedHashMap<>();
        if (!ring)
            stats.put("Twilight", integer(card.get("twilight")));
        for (String[] stat : new String[][]{{"strength", "Strength"}, {"vitality", "Vitality"}, {"resistance", "Resistance"}, {"site", "Site"}}) {
            int value = integer(card.get(stat[0]));
            if (value != 0)
                stats.put(stat[1], value);
        }
        JsonValue unique = card.get("unique");
        stats.put("Unique", unique != null && (unique.isBoolean() ? unique.asBoolean() : "true".equalsIgnoreCase(string(unique))));
        result.put("stats", stats);
        // as GameTextFieldProcessor and BuiltLotroCardBlueprint.setGameText do: lines joined with <br>, then plain text
        JsonValue text = card.get("gametext");
        String gametext;
        if (text == null || text.isNull()) {
            gametext = "";
        } else if (text.isArray()) {
            List<String> lines = new ArrayList<>();
            for (JsonValue line : text.asArray())
                lines.add(string(line));
            gametext = String.join("<br>", lines);
        } else {
            gametext = string(text);
        }
        result.put("gametext", gametext.isEmpty() ? "" : GameText.SanitizeHTMLToSearchText(GameText.ConvertTextToHTML(gametext.trim())));
        return result;
    }

    private static String string(JsonValue value) {
        if (value == null || value.isNull())
            return null;
        return value.isString() ? value.asString() : value.toString();
    }

    private static int integer(JsonValue value) {
        if (value == null || value.isNull())
            return 0;
        if (value.isNumber())
            return value.asInt();
        return parseIntOr(string(value).trim(), 0);
    }

    // the latest version older than the current one (revision 1 replaced revision 0, 2 replaced 1, ...)
    private static JsonObject previousVersion(List<JsonObject> versions, Object currentRevision) {
        if (versions == null)
            return null;
        int current = currentRevision instanceof Integer ? (Integer) currentRevision : Integer.MAX_VALUE;
        JsonObject best = null;
        int bestRevision = Integer.MIN_VALUE;
        for (JsonObject version : versions) {
            JsonValue info = version.get("cardInfo");
            int revision = info != null && info.isObject() ? integer(info.asObject().get("revision")) : 0;
            if (revision < current && revision > bestRevision) {
                best = version;
                bestRevision = revision;
            }
        }
        return best;
    }

    // "//\t103_19: {" (a commented-out card), up to the "//\t}" at the same indentation
    private static final Pattern COMMENTED_CARD = Pattern.compile("^\\s*//(\\s*)(\\d+_\\d+)\\s*:\\s*\\{\\s*$");
    private static final Pattern COMMENTED_LINE = Pattern.compile("^\\s*//(.*)$");

    private record ParsedFile(long modified, long size, Map<String, List<JsonObject>> cards) {
    }

    private static final Map<String, ParsedFile> COMMENTED_CACHE = new ConcurrentHashMap<>();

    /**
     * The commented-out card definitions under cardsPath, by blueprint id: the earlier versions of cards the PC revised
     * in place.  Files are re-read only when they change.
     */
    static Map<String, List<JsonObject>> commentedVersions(File cardsPath) {
        Map<String, List<JsonObject>> result = new HashMap<>();
        if (cardsPath == null)
            return result;
        List<File> files = new ArrayList<>();
        collectHjson(cardsPath, files);
        for (File file : files) {
            String key = file.getAbsolutePath();
            ParsedFile parsed = COMMENTED_CACHE.get(key);
            if (parsed == null || parsed.modified() != file.lastModified() || parsed.size() != file.length()) {
                parsed = new ParsedFile(file.lastModified(), file.length(), parseCommented(file));
                COMMENTED_CACHE.put(key, parsed);
            }
            parsed.cards().forEach((id, versions) -> result.computeIfAbsent(id, k -> new ArrayList<>()).addAll(versions));
        }
        return result;
    }

    private static void collectHjson(File path, List<File> into) {
        if (path.isDirectory()) {
            File[] children = path.listFiles();
            if (children == null)
                return;
            Arrays.sort(children);
            for (File child : children)
                collectHjson(child, into);
        } else if (path.isFile()) {
            String name = path.getName().toLowerCase(Locale.ROOT);
            if (name.endsWith(".hjson") || name.endsWith(".json"))
                into.add(path);
        }
    }

    static Map<String, List<JsonObject>> parseCommented(File file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException exp) {
            return Map.of();
        }
        return parseCommented(lines, file.getName());
    }

    static Map<String, List<JsonObject>> parseCommented(List<String> lines, String source) {
        Map<String, List<JsonObject>> result = new HashMap<>();
        for (int i = 0; i < lines.size(); i++) {
            Matcher start = COMMENTED_CARD.matcher(lines.get(i));
            if (!start.matches())
                continue;
            String indent = start.group(1);
            String id = start.group(2);
            StringBuilder body = new StringBuilder("{\n");
            int end = -1;
            for (int j = i + 1; j < lines.size(); j++) {
                Matcher line = COMMENTED_LINE.matcher(lines.get(j));
                if (!line.matches())
                    break;
                String text = line.group(1);
                if (text.equals(indent + "}") || (text.trim().equals("}") && leading(text) <= indent.length())) {
                    end = j;
                    break;
                }
                body.append(text).append('\n');
            }
            if (end < 0)
                continue;
            body.append("}\n");
            try {
                JsonObject card = JsonValue.readHjson(body.toString()).asObject();
                result.computeIfAbsent(id, k -> new ArrayList<>()).add(card);
            } catch (RuntimeException exp) {
                LOGGER.warn("Could not read the commented-out " + id + " in " + source + ": " + exp.getMessage());
            }
            i = end;
        }
        return result;
    }

    private static int leading(String text) {
        int count = 0;
        while (count < text.length() && Character.isWhitespace(text.charAt(count)))
            count++;
        return count;
    }

    // "condition" / "the one ring" as the blueprint builder reads them, in humanReadable's words
    private static String cardTypeName(String type) {
        String name = type.trim().replace(' ', '_').toUpperCase(Locale.ROOT);
        try {
            return humanReadable(CardType.valueOf(name));
        } catch (IllegalArgumentException exp) {
            String lower = type.trim().toLowerCase(Locale.ROOT);
            return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
        }
    }

    private static String humanReadable(Enum<?> value) {
        if (value == null)
            return null;
        if (value == Side.FREE_PEOPLE)
            return "Free Peoples";
        if (value == CardType.THE_ONE_RING)
            return "The One Ring";
        String lower = value.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private static int parseIntOr(String text, int fallback) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException exp) {
            return fallback;
        }
    }
}
