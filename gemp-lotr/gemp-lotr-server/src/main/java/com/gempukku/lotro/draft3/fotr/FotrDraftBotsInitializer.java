package com.gempukku.lotro.draft3.fotr;

import com.gempukku.lotro.common.AppConfig;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.ReplayMetadata;
import com.gempukku.util.JsonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class FotrDraftBotsInitializer {
    private static final boolean READ_NEW_DATA = false;

    private static final int RARES_IN_PACK = 1;
    private static final int UNCOMMONS_IN_PACK = 3;
    private static final int COMMONS_IN_PACK = 7;

    private static final Map<String, Double> AVERAGE_STARTING_COUNT_MAP = new HashMap<>();

    static {
        List<String> allCards = new ArrayList<>();
        allCards.addAll(List.of("1_296", "2_110", "1_12", "1_365", "1_112", "1_365", "1_311", "1_317",
                "2_104", "3_122", "1_364", "3_35", "1_78", "1_364", "2_114", "1_299", "1_303", "3_121", "1_365", "1_112",
                "1_365", "2_110", "1_298", "2_104", "1_51", "1_364", "3_35", "1_76", "1_364", "2_114"));
        allCards.addAll(List.of("1_26", "1_7", "1_9", "1_11", "1_5", "2_121", "1_97", "2_37", "1_97",
                "1_51", "1_37", "1_48", "1_32", "1_51", "1_97", "2_121", "1_5", "2_6", "1_9", "1_7", "1_6", "2_121", "1_97",
                "3_7", "1_37", "1_48", "2_18", "3_7", "1_97", "2_121"));
        allCards.addAll(List.of("3_96", "1_270", "2_89", "1_262", "1_262", "2_89", "1_270", "3_96",
                "1_231", "2_61", "1_179", "2_63", "1_184", "2_62", "2_62", "1_184", "2_63", "1_179", "2_61", "1_234", "1_158",
                "1_152", "1_154", "1_151", "1_145", "1_151", "1_154", "1_152", "1_158", "1_231", "3_100", "1_267", "1_270",
                "1_271", "1_271", "1_270", "1_267", "3_100", "1_234", "1_177", "2_67", "1_178", "1_176", "2_60", "2_60", "1_176",
                "1_178", "2_67", "1_177", "1_231", "3_57", "3_58", "3_59", "3_62", "3_69", "3_62", "3_59", "3_58", "3_57", "1_234"));

        for (String card : allCards) {
            AVERAGE_STARTING_COUNT_MAP.merge(card, 14.0/60.0, Double::sum);
        }
        AVERAGE_STARTING_COUNT_MAP.put("1_290", 1.0); // Frodo SoD
        AVERAGE_STARTING_COUNT_MAP.put("1_2", 1.0); // Ruling Ring
    }

    private final LotroCardBlueprintLibrary library;

    private int gamesAnalyzed = 0;

    public FotrDraftBotsInitializer(LotroCardBlueprintLibrary library) {
        this.library = library;
    }

    public Map<String, Double> getValuesMap() {
        if (!READ_NEW_DATA) {
            return getCachedValuesMap();
        } else {
            Path summaryDir = Paths.get(AppConfig.getProperty("application.root"), "replay", "summaries");

            System.out.println("Game history reading started at " + new SimpleDateFormat("HH.mm.ss").format(new java.util.Date()));
            // Load FotR limited decks from the past
            Map<String, Integer> winningMap = new HashMap<>();
            Map<String, Integer> losingMap = new HashMap<>();
            try (Stream<Path> paths = Files.walk(summaryDir)) {
                paths.filter(Files::isRegularFile) // Keep only regular files
                        .filter(path -> path.toString().endsWith(".json")) // Filter only JSON files
                        .forEach(path -> countOccurrencesInDeck(path, winningMap, losingMap)); // Process each JSON file
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Read data from " + gamesAnalyzed + " games at " + new SimpleDateFormat("HH.mm.ss").format(new java.util.Date()));

            // Merge win and lose maps (they are kept separate for potential wr info)
            Map<String, Integer> mergedMap = mergeMaps(winningMap, losingMap);

            // Lower count of cards from Draft Packs
            Map<String, Double> startingCollectionNormalizedCardCountMap = normalizeCountByDraftPackChance(mergedMap);

            // Normalize for uneven set size
            Map<String, Double> setNormalizedCardCountMap = normalizeCountBySetSize(startingCollectionNormalizedCardCountMap);

            // Normalize count based on rarities
            Map<String, Double> normalizedCardCountMap = normalizeCountByRarity(setNormalizedCardCountMap, library);

            // Boost rarities (more likely to pick rares and uncommons)
            Map<String, Double> rareInflatedMap = inflateRarity(normalizedCardCountMap, library);

            // Boost FP cards (more likely to pick FP card in mixed draft)
            Map<String, Double> fpInflatedMap = inflateFp(rareInflatedMap, library);

            // Make sure all values are positive and in 0-1 range
            Map<String, Double> shiftedMap = shift(fpInflatedMap);

            return shiftedMap;
        }
    }

    private void countOccurrencesInDeck(Path path, Map<String, Integer> winningMap, Map<String, Integer> losingMap) {
        try {
            // Read JSON file into a string
            String json = Files.readString(path);

            // Convert JSON to object
            ReplayMetadata summary = JsonUtils.Convert(json, ReplayMetadata.class);

            // Look at only limited FotR games
            if (summary.GameReplayInfo != null && ("Limited - FOTR").equals(summary.GameReplayInfo.format_name) && summary.GameReplayInfo.tournament.toLowerCase().contains("draft")) {
                ReplayMetadata.DeckMetadata winningDeck = summary.Decks.get(summary.GameReplayInfo.winner);
                ReplayMetadata.DeckMetadata losingDeck = summary.Decks.get(summary.GameReplayInfo.loser);

                if (winningDeck != null && losingDeck != null) {
                    gamesAnalyzed++;

                    winningMap.merge(winningDeck.RingBearer.replace("*", "").replace("T", ""), 1, Integer::sum);
                    winningMap.merge(winningDeck.Ring.replace("*", "").replace("T", ""), 1, Integer::sum);
                    winningDeck.DrawDeck.forEach(card -> winningMap.merge(card.replace("*", "").replace("T", ""), 1, Integer::sum));

                    losingMap.merge(losingDeck.RingBearer.replace("*", "").replace("T", ""), 1, Integer::sum);
                    losingMap.merge(losingDeck.Ring.replace("*", "").replace("T", ""), 1, Integer::sum);
                    losingDeck.DrawDeck.forEach(card -> losingMap.merge(card.replace("*", "").replace("T", ""), 1, Integer::sum));
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle file reading exceptions
        }
    }

    private static Map<String, Integer> mergeMaps(Map<String, Integer> map1, Map<String, Integer> map2) {
        Map<String, Integer> result = new HashMap<>(map1);

        map2.forEach((key, value) -> result.merge(key, value, Integer::sum));

        return result;
    }

    private Map<String, Double> normalizeCountByDraftPackChance(Map<String, Integer> map) {
        Map<String, Double> normalizedMap = new HashMap<>();

        map.forEach((key, value) -> {
            double cardsGiven = gamesAnalyzed * 2 * AVERAGE_STARTING_COUNT_MAP.getOrDefault(key, 0.0);
            normalizedMap.put(key, value.doubleValue() - (cardsGiven / 1.5)); // Assume 75 % of assigned cards are played, rest is drafted
        });

        return normalizedMap;
    }

    private Map<String, Double> normalizeCountBySetSize(Map<String, Double> map) {
        Map<String, Double> normalizedMap = new HashMap<>();

        map.forEach((key, value) -> {
            if (key.startsWith("2_") || key.startsWith("3_")) {
                normalizedMap.put(key, value / 3.0); // MoM and RotEL are 1/3 of FotR set
            } else {
                normalizedMap.put(key, value);
            }
        });

        return normalizedMap;
    }

    private Map<String, Double> normalizeCountByRarity(Map<String, Double> map, LotroCardBlueprintLibrary library) {
        Map<String, Double> normalizedMap = new HashMap<>();

        map.entrySet().forEach(entry -> {
            // Do not add P cards
            try {
                if (library.getLotroCardBlueprint(entry.getKey()).getCardInfo().rarity.equals("P")) {
                    return;
                }
            } catch (CardNotFoundException ignored) {

            }
            normalizedMap.put(entry.getKey(), getRarityNormalizedValue(library, entry));
        });

        return normalizedMap;
    }

    private double getRarityNormalizedValue(LotroCardBlueprintLibrary library, Map.Entry<String, Double> entry) {
        double rarityInPack = 1.0;
        try {
            rarityInPack = switch (library.getLotroCardBlueprint(entry.getKey()).getCardInfo().rarity) {
                case "R" -> RARES_IN_PACK;
                case "U" -> UNCOMMONS_IN_PACK;
                case "C" -> COMMONS_IN_PACK;
                default -> 1.0; // Fallback for unknown cards and promos
            };
        } catch (CardNotFoundException ignore) {

        }

        // Normalize by expected rarity frequency
        return entry.getValue() / rarityInPack;
    }

    private Map<String, Double> inflateRarity(Map<String, Double> map, LotroCardBlueprintLibrary library) {
        Map<String, Double> rarityInflatedMap = new HashMap<>();

        map.forEach((key, value) -> {
            try {
                int rarityInPack = switch (library.getLotroCardBlueprint(key).getCardInfo().rarity) {
                    case "R" -> RARES_IN_PACK;
                    case "U" -> UNCOMMONS_IN_PACK;
                    case "C" -> COMMONS_IN_PACK;
                    default -> 1; // Fallback for unknown cards and promos
                };
                rarityInflatedMap.put(key, value / rarityInPack);
            } catch (CardNotFoundException e) {
                rarityInflatedMap.put(key, value);
            }
        });
        return rarityInflatedMap;
    }

    private Map<String, Double> inflateFp(Map<String, Double> map, LotroCardBlueprintLibrary library) {
        Map<String, Double> fpInflatedMap = new HashMap<>();

        map.forEach((key, value) -> {
            try {
                double sideFactor = Side.FREE_PEOPLE.equals(library.getLotroCardBlueprint(key).getSide()) ? 1.1 : 1.0;
                fpInflatedMap.put(key, value * sideFactor);
            } catch (CardNotFoundException e) {
                fpInflatedMap.put(key, value);
            }
        });
        return fpInflatedMap;
    }

    private Map<String, Double> shift(Map<String, Double> map) {
        double min = map.values().stream().min(Double::compareTo).orElse(0.0);

        double positiveShift;
        if (min < 0) {
            positiveShift = -min + 5;
        } else {
            positiveShift = 5;
        }

        Map<String, Double> positiveMap = new HashMap<>();
        map.forEach((key, value) -> positiveMap.put(key, value + positiveShift));

        double max = positiveMap.values().stream().max(Double::compareTo).orElse(1.0);
        Map<String, Double> tbr = new HashMap<>();
        positiveMap.forEach((key, value) -> tbr.put(key, value / max));

        return tbr;
    }

    private Map<String, Double> getCachedValuesMap(){
        Map<String, Double> tbr = new HashMap<>();
        tbr.put("1_50", 1.0);
        tbr.put("1_90", 0.9832224243314389);
        tbr.put("1_313", 0.932889697325756);
        tbr.put("1_49", 0.9098205307814846);
        tbr.put("1_89", 0.8720709855272226);
        tbr.put("1_95", 0.8615850007343719);
        tbr.put("1_14", 0.846904622024381);
        tbr.put("2_108", 0.7762989910858537);
        tbr.put("3_38", 0.7553270215001525);
        tbr.put("1_127", 0.7298429008823761);
        tbr.put("1_230", 0.7260298155031577);
        tbr.put("1_308", 0.6917120470901921);
        tbr.put("2_32", 0.6714391431573477);
        tbr.put("1_165", 0.6669269921252725);
        tbr.put("1_40", 0.6560596987945);
        tbr.put("1_30", 0.6518653048773598);
        tbr.put("1_75", 0.6350877292087987);
        tbr.put("1_139", 0.6173568821954333);
        tbr.put("1_302", 0.6141157596230975);
        tbr.put("1_236", 0.6078241687473873);
        tbr.put("1_229", 0.5982914552993412);
        tbr.put("1_112", 0.5967116139990133);
        tbr.put("1_131", 0.5715998576448125);
        tbr.put("1_231", 0.5714868773372802);
        tbr.put("1_237", 0.567786772265594);
        tbr.put("1_47", 0.5511998508659939);
        tbr.put("3_13", 0.5386166691145733);
        tbr.put("1_72", 0.534422275197433);
        tbr.put("1_250", 0.5239362904045824);
        tbr.put("2_85", 0.5112260058071877);
        tbr.put("2_84", 0.5074129204279694);
        tbr.put("1_13", 0.5071587147360215);
        tbr.put("2_93", 0.4978802069799233);
        tbr.put("2_105", 0.49038113906746045);
        tbr.put("3_68", 0.48771197930200755);
        tbr.put("3_65", 0.4775437516240918);
        tbr.put("1_114", 0.4631175786060489);
        tbr.put("1_143", 0.44195495475138674);
        tbr.put("1_125", 0.44195495475138674);
        tbr.put("3_21", 0.43865028075606416);
        tbr.put("1_183", 0.4305156986137315);
        tbr.put("3_64", 0.4298801843838618);
        tbr.put("2_46", 0.4292446701539921);
        tbr.put("1_234", 0.42828433753996675);
        tbr.put("1_97", 0.4225199880994075);
        tbr.put("1_45", 0.41907644247607634);
        tbr.put("3_42", 0.4169792455175062);
        tbr.put("1_15", 0.40649326072465564);
        tbr.put("3_80", 0.39746895866050536);
        tbr.put("1_173", 0.39619793020076594);
        tbr.put("1_96", 0.3918128820146648);
        tbr.put("3_66", 0.3911138163618081);
        tbr.put("1_16", 0.38971568505609466);
        tbr.put("3_67", 0.38221661714363175);
        tbr.put("1_240", 0.38094558868389233);
        tbr.put("2_22", 0.37573437199896054);
        tbr.put("1_56", 0.3713069561975347);
        tbr.put("1_264", 0.3618801617878003);
        tbr.put("1_246", 0.3618801617878003);
        tbr.put("1_57", 0.3533642711075459);
        tbr.put("1_83", 0.3519661398018325);
        tbr.put("3_41", 0.3407810893561252);
        tbr.put("1_33", 0.3372857610918416);
        tbr.put("1_128", 0.3370951068228807);
        tbr.put("1_148", 0.3313754787540531);
        tbr.put("1_256", 0.3294689360644439);
        tbr.put("2_49", 0.32756239337483467);
        tbr.put("1_175", 0.3218427653060071);
        tbr.put("2_20", 0.319809119770424);
        tbr.put("1_27", 0.318177966580425);
        tbr.put("3_60", 0.30517816994497854);
        tbr.put("1_289", 0.3037306097547197);
        tbr.put("3_71", 0.3021418241800453);
        tbr.put("2_52", 0.29324462496186904);
        tbr.put("2_75", 0.2805343403644744);
        tbr.put("1_69", 0.27227265537616785);
        tbr.put("2_50", 0.27100162691642843);
        tbr.put("2_3", 0.26613641242331454);
        tbr.put("1_178", 0.2649242278737482);
        tbr.put("2_67", 0.26198517070696786);
        tbr.put("1_38", 0.25549507970760693);
        tbr.put("1_270", 0.24613654423367334);
        tbr.put("1_244", 0.24558105772163905);
        tbr.put("3_34", 0.2422128323033295);
        tbr.put("3_10", 0.23708635084904695);
        tbr.put("1_272", 0.23414180158398384);
        tbr.put("3_75", 0.23322383658528312);
        tbr.put("1_44", 0.2291636067833376);
        tbr.put("1_153", 0.22651563082554702);
        tbr.put("3_1", 0.2254352566347685);
        tbr.put("1_298", 0.22430058272475756);
        tbr.put("3_17", 0.22333805967619838);
        tbr.put("1_34", 0.22193992837048496);
        tbr.put("1_31", 0.2217069064861994);
        tbr.put("1_163", 0.21888946006711021);
        tbr.put("1_94", 0.21751251256905912);
        tbr.put("1_195", 0.2150763746878918);
        tbr.put("2_48", 0.21429963507360664);
        tbr.put("1_36", 0.21145394357763433);
        tbr.put("1_62", 0.20935674661906423);
        tbr.put("1_279", 0.20935674661906423);
        tbr.put("2_7", 0.20935674661906423);
        tbr.put("3_57", 0.20591869309401584);
        tbr.put("1_147", 0.20554366123984583);
        tbr.put("1_299", 0.20431301171144636);
        tbr.put("1_162", 0.20406079470348312);
        tbr.put("1_66", 0.20306515574335385);
        tbr.put("1_190", 0.20173057586062743);
        tbr.put("2_12", 0.19956982747907034);
        tbr.put("1_262", 0.19904258604391917);
        tbr.put("1_108", 0.19863773994192804);
        tbr.put("2_4", 0.19483171583193043);
        tbr.put("3_8", 0.1946763679090734);
        tbr.put("3_29", 0.19397730225621668);
        tbr.put("2_38", 0.19257917095050325);
        tbr.put("1_296", 0.19245742890483572);
        tbr.put("2_74", 0.19156234818271167);
        tbr.put("1_217", 0.1902913197229722);
        tbr.put("2_112", 0.18908384268621972);
        tbr.put("1_280", 0.1869019104970003);
        tbr.put("1_120", 0.1864782343437538);
        tbr.put("3_106", 0.184967122730508);
        tbr.put("3_108", 0.18411904988633945);
        tbr.put("1_159", 0.18372433934765164);
        tbr.put("2_82", 0.18280637434895092);
        tbr.put("1_233", 0.18245331088791217);
        tbr.put("3_39", 0.18209318615765266);
        tbr.put("3_7", 0.18102991593009776);
        tbr.put("3_50", 0.1807586062749262);
        tbr.put("1_9", 0.18013770454911743);
        tbr.put("3_40", 0.17999598919908252);
        tbr.put("1_317", 0.17961744752583497);
        tbr.put("3_43", 0.1787262577071591);
        tbr.put("1_87", 0.17789879224051242);
        tbr.put("3_61", 0.17433285128402112);
        tbr.put("1_209", 0.17419162589960563);
        tbr.put("1_186", 0.1731324355164894);
        tbr.put("1_136", 0.1731324355164894);
        tbr.put("3_58", 0.172518889679751);
        tbr.put("1_199", 0.1712258928268802);
        tbr.put("1_259", 0.1712258928268802);
        tbr.put("3_63", 0.17039583342460135);
        tbr.put("3_104", 0.16995486436714075);
        tbr.put("1_263", 0.169319350137271);
        tbr.put("3_85", 0.16804832167753156);
        tbr.put("1_92", 0.16647120881320177);
        tbr.put("1_150", 0.16632335448217084);
        tbr.put("1_286", 0.16561521005460167);
        tbr.put("1_155", 0.1655062647580526);
        tbr.put("1_123", 0.1655062647580526);
        tbr.put("2_10", 0.16474494465002498);
        tbr.put("1_133", 0.16422226662015252);
        tbr.put("2_83", 0.1619756301476652);
        tbr.put("1_207", 0.16042215091909473);
        tbr.put("1_132", 0.159786636689225);
        tbr.put("1_247", 0.159786636689225);
        tbr.put("1_176", 0.1590594700670203);
        tbr.put("1_288", 0.1590240196133813);
        tbr.put("1_284", 0.1590240196133813);
        tbr.put("3_93", 0.15851560822948552);
        tbr.put("1_291", 0.1569268226548112);
        tbr.put("3_12", 0.15684914869338265);
        tbr.put("1_156", 0.1563626416548248);
        tbr.put("1_129", 0.1559735513100066);
        tbr.put("3_69", 0.15479532810288302);
        tbr.put("3_27", 0.15413056004338435);
        tbr.put("1_2", 0.1540929479767186);
        tbr.put("3_77", 0.15343149439052767);
        tbr.put("2_100", 0.15343149439052767);
        tbr.put("3_99", 0.1527959801606579);
        tbr.put("1_318", 0.15273242873767096);
        tbr.put("3_5", 0.1516449932776716);
        tbr.put("2_39", 0.15088943747104872);
        tbr.put("1_158", 0.15007148310171978);
        tbr.put("2_94", 0.14961840901130927);
        tbr.put("2_60", 0.14942789884988344);
        tbr.put("1_181", 0.14855921862819305);
        tbr.put("1_118", 0.14853803482053068);
        tbr.put("1_208", 0.1483473805515698);
        tbr.put("1_204", 0.1483473805515698);
        tbr.put("1_146", 0.14815283537916069);
        tbr.put("3_25", 0.14737292539910288);
        tbr.put("1_196", 0.14721901855159697);
        tbr.put("1_282", 0.14644083786196058);
        tbr.put("2_51", 0.14487150680452718);
        tbr.put("2_37", 0.14421746034564129);
        tbr.put("1_281", 0.14383393255167862);
        tbr.put("3_102", 0.14326326671261191);
        tbr.put("2_18", 0.14311294013675632);
        tbr.put("1_311", 0.14310910047154285);
        tbr.put("1_218", 0.14241591440611895);
        tbr.put("3_74", 0.14239429827585126);
        tbr.put("2_101", 0.14147604506208034);
        tbr.put("1_260", 0.14093304786975624);
        tbr.put("2_47", 0.14039696783911781);
        tbr.put("2_96", 0.14014924698625023);
        tbr.put("1_180", 0.1397873929655693);
        tbr.put("1_224", 0.13881466710352378);
        tbr.put("1_205", 0.13881466710352378);
        tbr.put("1_73", 0.13851809379625124);
        tbr.put("1_80", 0.1380520500276801);
        tbr.put("1_102", 0.13693925164150003);
        tbr.put("3_91", 0.1369081244139146);
        tbr.put("1_172", 0.1369081244139146);
        tbr.put("1_267", 0.13676219151668523);
        tbr.put("3_20", 0.1366539187219667);
        tbr.put("1_145", 0.13617490527523493);
        tbr.put("1_235", 0.1360607721074216);
        tbr.put("2_5", 0.13521298747832328);
        tbr.put("2_71", 0.13486035633988988);
        tbr.put("1_303", 0.13459191282347235);
        tbr.put("1_164", 0.1341542294178124);
        tbr.put("1_316", 0.13409067799482544);
        tbr.put("1_121", 0.13391212875881442);
        tbr.put("1_187", 0.13360085648295983);
        tbr.put("1_228", 0.1330950390346962);
        tbr.put("1_253", 0.1330950390346962);
        tbr.put("2_1", 0.13245952480482645);
        tbr.put("1_258", 0.13245952480482645);
        tbr.put("1_3", 0.13206005871747975);
        tbr.put("1_307", 0.13176045915196974);
        tbr.put("1_138", 0.13153867765542337);
        tbr.put("1_191", 0.13134413248301424);
        tbr.put("1_142", 0.130341144038594);
        tbr.put("1_161", 0.13012930596197075);
        tbr.put("1_79", 0.12966326219339963);
        tbr.put("2_102", 0.1295919289635163);
        tbr.put("3_96", 0.12930392204860908);
        tbr.put("1_276", 0.1292819536554778);
        tbr.put("1_245", 0.1292819536554778);
        tbr.put("1_188", 0.12907011557885453);
        tbr.put("3_19", 0.1289641965405429);
        tbr.put("1_257", 0.12885827750223128);
        tbr.put("1_283", 0.12881504524169593);
        tbr.put("2_16", 0.12842047881054325);
        tbr.put("2_44", 0.12836110650607468);
        tbr.put("3_52", 0.12737541096586857);
        tbr.put("1_117", 0.12671006647622945);
        tbr.put("1_152", 0.12610351786091842);
        tbr.put("1_314", 0.1254688682762594);
        tbr.put("1_268", 0.12496305082799572);
        tbr.put("2_43", 0.1241978398165199);
        tbr.put("2_59", 0.1241978398165199);
        tbr.put("1_116", 0.1241420702004293);
        tbr.put("2_6", 0.12401588964268012);
        tbr.put("1_41", 0.1240136703866393);
        tbr.put("1_309", 0.12360469320197483);
        tbr.put("1_216", 0.12356232558665017);
        tbr.put("1_214", 0.12356232558665017);
        tbr.put("1_212", 0.12356232558665017);
        tbr.put("2_15", 0.12337167131768927);
        tbr.put("2_29", 0.1233146047337826);
        tbr.put("2_76", 0.12264436058794945);
        tbr.put("2_61", 0.1220878172873243);
        tbr.put("1_220", 0.12165578289704097);
        tbr.put("1_170", 0.12165578289704097);
        tbr.put("2_92", 0.12109088135937898);
        tbr.put("3_81", 0.12102026866717124);
        tbr.put("2_114", 0.12087944678385408);
        tbr.put("1_154", 0.12081188917139082);
        tbr.put("1_293", 0.1198763430534057);
        tbr.put("1_210", 0.11974924020743177);
        tbr.put("1_221", 0.11974924020743177);
        tbr.put("1_26", 0.11969753442383149);
        tbr.put("1_98", 0.11964332116912015);
        tbr.put("2_106", 0.11941029928483458);
        tbr.put("1_185", 0.11936014986261356);
        tbr.put("1_71", 0.11917727740054902);
        tbr.put("2_80", 0.11911372597756203);
        tbr.put("1_223", 0.11869004982431555);
        tbr.put("1_84", 0.11815007889022895);
        tbr.put("2_64", 0.11786863687414378);
        tbr.put("1_126", 0.11784269751782257);
        tbr.put("1_124", 0.11784269751782257);
        tbr.put("3_62", 0.11778188413800283);
        tbr.put("2_103", 0.11754612421055004);
        tbr.put("1_261", 0.11718124393163162);
        tbr.put("2_90", 0.11710342586266799);
        tbr.put("3_82", 0.11706595790353735);
        tbr.put("1_59", 0.11690888069025888);
        tbr.put("1_226", 0.11678350713470635);
        tbr.put("1_5", 0.11645139690927375);
        tbr.put("1_65", 0.11638101478912219);
        tbr.put("1_11", 0.11635913926529129);
        tbr.put("2_14", 0.11635248149716886);
        tbr.put("1_200", 0.11593615482821337);
        tbr.put("1_166", 0.11593615482821337);
        tbr.put("1_68", 0.11575328236614882);
        tbr.put("3_86", 0.11551247867496689);
        tbr.put("1_160", 0.11535251931098606);
        tbr.put("3_30", 0.11506848335926877);
        tbr.put("3_18", 0.11498288348340877);
        tbr.put("3_56", 0.11489858057536484);
        tbr.put("1_182", 0.11476888379375876);
        tbr.put("3_97", 0.11445328829185066);
        tbr.put("1_19", 0.11438368435238873);
        tbr.put("1_113", 0.11405079594626649);
        tbr.put("1_227", 0.11402961213860417);
        tbr.put("3_98", 0.11364052179378596);
        tbr.put("2_19", 0.11358475217769536);
        tbr.put("3_79", 0.11353532329314993);
        tbr.put("1_269", 0.11321252241448594);
        tbr.put("3_111", 0.11318528609034867);
        tbr.put("1_213", 0.11318225983211119);
        tbr.put("1_17", 0.11311870840912423);
        tbr.put("1_103", 0.11301408633862865);
        tbr.put("1_23", 0.11288568652483864);
        tbr.put("2_11", 0.11288568652483864);
        tbr.put("3_44", 0.11288568652483864);
        tbr.put("2_109", 0.11288568652483864);
        tbr.put("2_54", 0.11275858367886471);
        tbr.put("3_54", 0.11275858367886471);
        tbr.put("1_255", 0.11270670496622227);
        tbr.put("2_89", 0.11260698255192071);
        tbr.put("2_41", 0.11233490752561823);
        tbr.put("1_194", 0.11233490752561823);
        tbr.put("2_28", 0.11226429483341047);
        tbr.put("3_94", 0.11222682687427983);
        tbr.put("2_27", 0.11218662087198195);
        tbr.put("1_63", 0.11218662087198195);
        tbr.put("2_53", 0.11212306944899497);
        tbr.put("2_95", 0.11201931202371011);
        tbr.put("3_31", 0.11195835453635526);
        tbr.put("2_13", 0.11187592502626786);
        tbr.put("1_101", 0.11173008820072858);
        tbr.put("1_8", 0.11164448832486858);
        tbr.put("1_109", 0.11148755521912523);
        tbr.put("3_49", 0.11146161586280402);
        tbr.put("2_8", 0.11125453333483967);
        tbr.put("2_88", 0.11082610163293428);
        tbr.put("1_115", 0.11078848956626852);
        tbr.put("3_24", 0.11071081560484);
        tbr.put("3_55", 0.11067046549500702);
        tbr.put("1_198", 0.11064020291263225);
        tbr.put("2_87", 0.11064020291263225);
        tbr.put("1_29", 0.11055546768198296);
        tbr.put("1_135", 0.11042836483600901);
        tbr.put("1_105", 0.1103224457976974);
        tbr.put("1_167", 0.11021652675938577);
        tbr.put("1_251", 0.11021652675938577);
        tbr.put("2_97", 0.11008942391341182);
        tbr.put("2_30", 0.10993407599055478);
        tbr.put("3_89", 0.10993407599055478);
        tbr.put("2_72", 0.10986346329834704);
        tbr.put("1_202", 0.10979285060613928);
        tbr.put("3_14", 0.10967569118008846);
        tbr.put("1_304", 0.10946169149043845);
        tbr.put("3_3", 0.10939035826055513);
        tbr.put("3_53", 0.10929856176068505);
        tbr.put("2_42", 0.10916598282837661);
        tbr.put("2_68", 0.10915733637626954);
        tbr.put("3_73", 0.10915733637626954);
        tbr.put("1_219", 0.10894549829964631);
        tbr.put("1_274", 0.10894549829964631);
        tbr.put("2_2", 0.10891955894332508);
        tbr.put("1_106", 0.10886249235941842);
        tbr.put("1_119", 0.10877689248355842);
        tbr.put("1_93", 0.10869129260769841);
        tbr.put("1_100", 0.10869129260769841);
        tbr.put("1_310", 0.10869129260769841);
        tbr.put("1_377", 0.10869129260769841);
        tbr.put("1_248", 0.10854343827666749);
        tbr.put("1_232", 0.10852182214639981);
        tbr.put("2_56", 0.10845120945419207);
        tbr.put("2_57", 0.10830998406977657);
        tbr.put("1_157", 0.10830998406977657);
        tbr.put("3_28", 0.10827755987437504);
        tbr.put("1_64", 0.10822524883912726);
        tbr.put("1_70", 0.10822524883912726);
        tbr.put("3_78", 0.10808949954104626);
        tbr.put("1_201", 0.10803762082840382);
        tbr.put("1_4", 0.10800649360081838);
        tbr.put("2_113", 0.1079922269548417);
        tbr.put("1_42", 0.10792089372495837);
        tbr.put("3_101", 0.10792089372495836);
        tbr.put("1_174", 0.10792089372495836);
        tbr.put("1_107", 0.10779249391116835);
        tbr.put("1_275", 0.10767446983990683);
        tbr.put("1_197", 0.10753180338014016);
        tbr.put("3_95", 0.10750586402381894);
        tbr.put("2_78", 0.10739201907107584);
        tbr.put("2_36", 0.107293161301985);
        tbr.put("2_98", 0.107293161301985);
        tbr.put("3_6", 0.107293161301985);
        tbr.put("1_238", 0.10725079368666034);
        tbr.put("1_21", 0.10723609471807834);
        tbr.put("2_107", 0.10721548734055647);
        tbr.put("1_168", 0.10718162206980378);
        tbr.put("1_277", 0.10714271303532195);
        tbr.put("3_114", 0.1070648949663583);
        tbr.put("2_33", 0.10706013941769942);
        tbr.put("1_266", 0.10690925882843103);
        tbr.put("3_26", 0.10690479149484239);
        tbr.put("1_24", 0.10685089527670831);
        tbr.put("1_301", 0.10682711753341385);
        tbr.put("1_239", 0.10682711753341385);
        tbr.put("1_149", 0.10667580462154011);
        tbr.put("2_69", 0.1066628349433795);
        tbr.put("3_92", 0.10661527945679061);
        tbr.put("2_65", 0.10659798655257646);
        tbr.put("1_28", 0.10659409564912829);
        tbr.put("1_35", 0.10659409564912829);
        tbr.put("1_55", 0.10659409564912829);
        tbr.put("1_60", 0.10659409564912829);
        tbr.put("1_378", 0.10659409564912829);
        tbr.put("1_111", 0.10659409564912829);
        tbr.put("1_271", 0.10657118255104456);
        tbr.put("3_112", 0.10652276241924495);
        tbr.put("2_110", 0.10649866763937324);
        tbr.put("2_31", 0.10643874772627124);
        tbr.put("2_73", 0.10640344138016737);
        tbr.put("1_169", 0.10640344138016737);
        tbr.put("1_252", 0.10640344138016737);
        tbr.put("1_243", 0.10640344138016737);
        tbr.put("1_278", 0.10636453234568555);
        tbr.put("1_46", 0.10636107376484272);
        tbr.put("3_76", 0.10635156266752493);
        tbr.put("2_55", 0.10631265363304311);
        tbr.put("1_192", 0.10616998717327644);
        tbr.put("1_141", 0.10616998717327644);
        tbr.put("3_37", 0.10599489651810826);
        tbr.put("1_249", 0.10597976522692087);
        tbr.put("1_297", 0.10590929664224825);
        tbr.put("3_23", 0.10589502999627158);
        tbr.put("3_45", 0.10589502999627158);
        tbr.put("3_110", 0.10589502999627158);
        tbr.put("1_58", 0.10586649670431825);
        tbr.put("3_59", 0.10586274990840518);
        tbr.put("1_306", 0.10582369676638825);
        tbr.put("3_70", 0.10580683618477946);
        tbr.put("3_32", 0.10578089682845825);
        tbr.put("1_193", 0.10578089682845825);
        tbr.put("1_104", 0.10573809689052824);
        tbr.put("1_67", 0.10565249701466824);
        tbr.put("1_110", 0.10565249701466824);
        tbr.put("2_79", 0.10562670176588214);
        tbr.put("3_72", 0.10562670176588214);
        tbr.put("3_22", 0.1056239637227149);
        tbr.put("1_85", 0.10560969707673823);
        tbr.put("2_111", 0.10558433415055749);
        tbr.put("1_43", 0.10556689713880822);
        tbr.put("2_81", 0.10548547638146664);
        tbr.put("1_134", 0.10546962455260367);
        tbr.put("2_26", 0.10545276397099489);
        tbr.put("1_74", 0.10542898622770044);
        tbr.put("1_91", 0.10542898622770044);
        tbr.put("3_46", 0.10542898622770044);
        tbr.put("3_47", 0.10542898622770044);
        tbr.put("2_70", 0.1054148636892589);
        tbr.put("1_294", 0.1053956973870882);
        tbr.put("3_87", 0.10536586712731882);
        tbr.put("3_4", 0.10535131226627192);
        tbr.put("1_211", 0.10534425099705114);
        tbr.put("2_99", 0.10533863080318154);
        tbr.put("1_171", 0.1053139884146764);
        tbr.put("3_11", 0.10529583086525154);
        tbr.put("2_35", 0.10528156421927487);
        tbr.put("2_9", 0.1052244976353682);
        tbr.put("2_40", 0.10522320066755214);
        tbr.put("2_21", 0.10521023098939154);
        tbr.put("3_33", 0.10521023098939154);
        tbr.put("3_83", 0.10520302561263566);
        tbr.put("1_77", 0.10519596434341487);
        tbr.put("1_285", 0.10519596434341487);
        tbr.put("1_370", 0.10519596434341487);
        tbr.put("2_45", 0.10513241292042791);
        tbr.put("2_34", 0.10511829038198636);
        tbr.put("3_109", 0.10511036446755487);
        tbr.put("3_84", 0.10506756452962487);
        tbr.put("3_107", 0.10504061642055783);
        tbr.put("3_48", 0.10502476459169485);
        tbr.put("1_53", 0.10501049794571819);
        tbr.put("3_88", 0.1049911875360124);
        tbr.put("1_39", 0.10496769800778818);
        tbr.put("3_90", 0.10492489806985819);
        tbr.put("1_122", 0.10492489806985819);
        tbr.put("2_23", 0.10491063142388152);
        tbr.put("1_305", 0.10488209813192818);
        tbr.put("1_315", 0.10483929819399819);
        tbr.put("3_2", 0.10480759453627227);
        tbr.put("1_25", 0.10479649825606818);
        tbr.put("1_86", 0.10479649825606818);
        tbr.put("3_16", 0.10475369831813819);
        tbr.put("2_91", 0.1047433225756097);
        tbr.put("1_54", 0.10472992057484373);
        tbr.put("1_292", 0.10472992057484373);
        tbr.put("1_61", 0.10466809844227817);
        tbr.put("1_82", 0.10466809844227817);
        tbr.put("3_36", 0.10466809844227817);
        tbr.put("1_20", 0.10462529850434817);
        tbr.put("2_58", 0.10461362579400363);
        tbr.put("3_51", 0.10460065611584303);
        tbr.put("1_300", 0.10458249856641817);
        tbr.put("2_17", 0.10457457265198668);
        tbr.put("2_124", 0.1045682319204415);
        tbr.put("2_24", 0.10455396527446484);
        tbr.put("1_18", 0.10453969862848816);
        tbr.put("1_10", 0.10453969862848816);
        tbr.put("1_312", 0.10453969862848816);
        tbr.put("1_273", 0.10453580772504);
        tbr.put("1_78", 0.10155036074151044);
        tbr.put("2_62", 0.1005711212188776);
        tbr.put("3_35", 0.09791877720157814);
        tbr.put("1_32", 0.09722756701058019);
        tbr.put("1_179", 0.09489847220649843);
        tbr.put("1_6", 0.09448837098306004);
        tbr.put("2_104", 0.09363015296841916);
        tbr.put("2_63", 0.09285416271331655);
        tbr.put("1_37", 0.09167023284780232);
        tbr.put("1_76", 0.09072197644521982);
        tbr.put("1_184", 0.09042393324108908);
        tbr.put("1_151", 0.08466539613777965);
        tbr.put("1_290", 0.08115666587273014);
        tbr.put("1_177", 0.07898467710343386);
        tbr.put("1_12", 0.06794871070639047);
        tbr.put("1_7", 0.06714586841391089);
        tbr.put("3_100", 0.046334008704504964);
        tbr.put("1_48", 0.03582793852364858);
        tbr.put("1_51", 0.009532713448046005);
        return tbr;
    }
}
