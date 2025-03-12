package com.gempukku.lotro.draft3.format.ttt;

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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TttDraftCardEvaluator {
    private static final boolean READ_NEW_DATA = false;

    private static final int RARES_IN_PACK = 1;
    private static final int UNCOMMONS_IN_PACK = 3;
    private static final int COMMONS_IN_PACK = 7;

    private static final Map<String, Double> AVERAGE_STARTING_COUNT_MAP = new HashMap<>();

    static {
        List<String> allCards = new ArrayList<>();
        allCards.addAll(List.of("4_90", "4_105", "4_97", "4_98", "4_97", "4_105", "4_90", "4_109", "4_115", "4_112",
                "4_115", "4_109", "4_71", "4_83", "4_76", "4_78", "4_83", "4_71", "4_74", "4_87",
                "4_70", "4_87", "4_74", "4_117", "4_135", "4_112", "4_128", "4_130", "4_135", "4_117"));
        allCards.addAll(List.of("4_266", "4_278", "4_277", "4_273", "4_273", "4_281", "4_283", "4_266", "4_270",
                "4_287", "4_265", "4_297", "4_265", "4_287", "4_270", "4_49", "4_44", "4_42", "4_58",
                "4_42", "4_44", "4_49", "4_310", "4_322", "4_308", "4_314", "4_310", "4_308", "4_322", "4_314"));
        allCards.addAll(List.of("4_248", "4_221", "4_222", "4_227", "4_226", "4_224",
                "4_228", "4_258", "4_228", "4_224", "4_226", "4_227", "4_222", "4_221", "4_248", "4_165", "4_191", "4_184",
                "4_206", "4_204", "4_180", "4_198", "4_137", "4_198", "4_180", "4_204", "4_206", "4_184", "4_191", "4_165",
                "4_4", "4_17", "4_16", "4_25", "4_10", "4_14", "4_12", "4_21", "4_12", "4_14", "4_10", "4_25", "4_16",
                "4_17", "4_4", "4_207", "4_181", "4_193", "4_189", "4_187", "4_190", "4_178", "4_153", "4_178", "4_190",
                "4_187", "4_189", "4_193", "4_181", "4_207"));

        for (String card : allCards) {
            AVERAGE_STARTING_COUNT_MAP.merge(card, 14.0/60.0, Double::sum);
        }
        AVERAGE_STARTING_COUNT_MAP.put("4_302", 1.0); // Frodo
        AVERAGE_STARTING_COUNT_MAP.put("4_2", 1.0); // Ruling Ring
    }

    private final LotroCardBlueprintLibrary library;

    private int gamesAnalyzed = 0;

    public TttDraftCardEvaluator(LotroCardBlueprintLibrary library) {
        this.library = library;
    }

    public Map<String, Double> getValuesMap() {
        if (!READ_NEW_DATA) {
            return getCachedValuesMap();
        } else {
            // Data from solo drafts do not contain one ring cards, add some value manually after
            Path summaryDir = Paths.get(AppConfig.getProperty("application.root"), "replay", "summaries");

            System.out.println("Game history reading started at " + new SimpleDateFormat("HH.mm.ss").format(new Date()));
            gamesAnalyzed = 0;
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

            System.out.println("Read data from " + gamesAnalyzed + " games at " + new SimpleDateFormat("HH.mm.ss").format(new Date()));

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

    public Map<String, Double> getPlayRateMap() {
        if (!READ_NEW_DATA) {
            return getCachedPlayRateMap();
        } else {
            // Data from solo drafts do not contain one ring cards, add some value manually after
            Path summaryDir = Paths.get(AppConfig.getProperty("application.root"), "replay", "summaries");

            System.out.println("Game history reading started at " + new SimpleDateFormat("HH.mm.ss").format(new Date()));
            gamesAnalyzed = 0;
            // Load FotR limited decks from the past
            Map<String, Integer> countMap = new HashMap<>(); // How many decks contained what card (ignoring how many times it was including in said deck)
            try (Stream<Path> paths = Files.walk(summaryDir)) {
                paths.filter(Files::isRegularFile) // Keep only regular files
                        .filter(path -> path.toString().endsWith(".json")) // Filter only JSON files
                        .forEach(path -> countOccurrencesInGame(path, countMap)); // Process each JSON file
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Read data from " + gamesAnalyzed + " games at " + new SimpleDateFormat("HH.mm.ss").format(new Date()));

            return convertCountToPlayRate(countMap);
        }
    }

    private Map<String, Double> convertCountToPlayRate(Map<String, Integer> countMap) {
        return countMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().doubleValue() / (gamesAnalyzed * 2))); // Every game has two decks
    }

    private void countOccurrencesInGame(Path path, Map<String, Integer> map) {
        try {
            // Read JSON file into a string
            String json = Files.readString(path);

            // Convert JSON to object
            ReplayMetadata summary = JsonUtils.Convert(json, ReplayMetadata.class);

            // Look at only limited FotR games
            if (summary.GameReplayInfo != null && ("Limited - TTT").equals(summary.GameReplayInfo.format_name) && summary.GameReplayInfo.tournament.toLowerCase().contains("draft")) {
                ReplayMetadata.DeckMetadata winningDeck = summary.Decks.get(summary.GameReplayInfo.winner);
                ReplayMetadata.DeckMetadata losingDeck = summary.Decks.get(summary.GameReplayInfo.loser);

                if (winningDeck != null && losingDeck != null) {
                    gamesAnalyzed++;

                    map.merge(winningDeck.RingBearer.replace("*", "").replace("T", ""), 1, Integer::sum);
                    map.merge(winningDeck.Ring.replace("*", "").replace("T", ""), 1, Integer::sum);
                    Set<String> winningCardSet = new HashSet<>(winningDeck.DrawDeck);
                    winningCardSet.forEach(card -> map.merge(card.replace("*", "").replace("T", ""), 1, Integer::sum));

                    map.merge(losingDeck.RingBearer.replace("*", "").replace("T", ""), 1, Integer::sum);
                    map.merge(losingDeck.Ring.replace("*", "").replace("T", ""), 1, Integer::sum);
                    Set<String> losingCardSet = new HashSet<>(losingDeck.DrawDeck);
                    losingCardSet.forEach(card -> map.merge(card.replace("*", "").replace("T", ""), 1, Integer::sum));
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle file reading exceptions
        }
    }

    private Map<String, Double> convertToDoubleMap(Map<String, Integer> intMap) {
        return intMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().doubleValue()));
    }

    private void countOccurrencesInDeck(Path path, Map<String, Integer> winningMap, Map<String, Integer> losingMap) {
        try {
            // Read JSON file into a string
            String json = Files.readString(path);

            // Convert JSON to object
            ReplayMetadata summary = JsonUtils.Convert(json, ReplayMetadata.class);

            // Look at only limited FotR games
            if (summary.GameReplayInfo != null && ("Limited - TTT").equals(summary.GameReplayInfo.format_name) && summary.GameReplayInfo.tournament.toLowerCase().contains("draft")) {
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
            if (key.startsWith("5_") || key.startsWith("6_")) {
                normalizedMap.put(key, value / 3.0); // BoHD and EoF are 1/3 of TTT set
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
        tbr.put("4_1", 0.6); // Manually added Ring which was not part of solo draft data
        tbr.put("4_267", 1.0);
        tbr.put("4_179", 0.8343093340676018);
        tbr.put("4_299", 0.8193251347137154);
        tbr.put("4_200", 0.7709146444934668);
        tbr.put("5_51", 0.7603488628977776);
        tbr.put("5_116", 0.754345577900227);
        tbr.put("4_289", 0.7369120382673399);
        tbr.put("4_118", 0.7067995607196258);
        tbr.put("4_274", 0.6814416848899718);
        tbr.put("4_225", 0.6801449753305009);
        tbr.put("4_219", 0.6772633985316765);
        tbr.put("4_218", 0.6700594565346157);
        tbr.put("4_244", 0.6700594565346157);
        tbr.put("4_73", 0.6687627469751448);
        tbr.put("6_88", 0.6671778797357915);
        tbr.put("4_100", 0.660838410778378);
        tbr.put("4_303", 0.660838410778378);
        tbr.put("4_257", 0.6599739377387306);
        tbr.put("4_154", 0.6585331493393185);
        tbr.put("6_50", 0.6497443401029043);
        tbr.put("6_60", 0.6470068421440213);
        tbr.put("4_158", 0.6455660537446091);
        tbr.put("4_23", 0.6383621117475483);
        tbr.put("4_249", 0.63500027214892);
        tbr.put("4_284", 0.6338956677093706);
        tbr.put("5_19", 0.6328390895498017);
        tbr.put("5_82", 0.629669355071095);
        tbr.put("4_89", 0.6291410659913105);
        tbr.put("5_46", 0.626835804552251);
        tbr.put("5_72", 0.626835804552251);
        tbr.put("5_47", 0.6253950161528389);
        tbr.put("4_272", 0.6243864642732504);
        tbr.put("4_256", 0.6167502857563659);
        tbr.put("4_6", 0.6167502857563659);
        tbr.put("4_116", 0.6148772608371301);
        tbr.put("6_31", 0.6143489717573457);
        tbr.put("4_315", 0.6117075263584233);
        tbr.put("6_30", 0.6117075263584233);
        tbr.put("4_164", 0.6109871321587172);
        tbr.put("4_132", 0.6088899845995729);
        tbr.put("6_57", 0.6057042413608726);
        tbr.put("6_80", 0.6037831901616565);
        tbr.put("4_33", 0.6023424017622443);
        tbr.put("6_92", 0.602198322922303);
        tbr.put("6_76", 0.6009016133628321);
        tbr.put("6_28", 0.6000851666031652);
        tbr.put("4_186", 0.5936976713657713);
        tbr.put("4_136", 0.5917766201665552);
        tbr.put("6_78", 0.5898555689673389);
        tbr.put("4_19", 0.5893753061675349);
        tbr.put("4_40", 0.5893753061675349);
        tbr.put("4_176", 0.5893753061675349);
        tbr.put("4_237", 0.5850529409692984);
        tbr.put("4_251", 0.5821713641704741);
        tbr.put("6_104", 0.5749674221734133);
        tbr.put("4_41", 0.5736707126139424);
        tbr.put("6_6", 0.5735266337740011);
        tbr.put("5_94", 0.5715575562948045);
        tbr.put("5_89", 0.57102926721502);
        tbr.put("5_70", 0.5706450569751769);
        tbr.put("4_103", 0.5705009781352356);
        tbr.put("4_271", 0.5657463764171755);
        tbr.put("4_199", 0.5648819033775282);
        tbr.put("4_188", 0.5642415529777894);
        tbr.put("6_94", 0.5641615091778222);
        tbr.put("5_4", 0.563441114978116);
        tbr.put("4_133", 0.5609917746991154);
        tbr.put("4_313", 0.559406907459762);
        tbr.put("4_22", 0.5591187497798795);
        tbr.put("4_177", 0.5591187497798795);
        tbr.put("4_240", 0.5576779613804674);
        tbr.put("4_263", 0.557117654780696);
        tbr.put("4_124", 0.5562371729810552);
        tbr.put("4_268", 0.5562371729810552);
        tbr.put("4_91", 0.5546523057417019);
        tbr.put("5_45", 0.5544762093817737);
        tbr.put("6_55", 0.5530674385023485);
        tbr.put("5_49", 0.5523950705826228);
        tbr.put("4_203", 0.5519148077828188);
        tbr.put("4_160", 0.5519148077828188);
        tbr.put("4_65", 0.5483128367842883);
        tbr.put("5_78", 0.5466319169849742);
        tbr.put("4_36", 0.5459915665852355);
        tbr.put("4_111", 0.5451431023055816);
        tbr.put("5_39", 0.5446148132257972);
        tbr.put("4_13", 0.5445507781858233);
        tbr.put("4_61", 0.5435582350662282);
        tbr.put("4_223", 0.5432700773863458);
        tbr.put("5_25", 0.5427898145865417);
        tbr.put("6_7", 0.5413490261871297);
        tbr.put("4_48", 0.5403885005875215);
        tbr.put("5_22", 0.5401750504542753);
        tbr.put("5_29", 0.539860211507737);
        tbr.put("4_259", 0.5389477121881093);
        tbr.put("5_113", 0.5382753442683836);
        tbr.put("6_26", 0.5356338988694613);
        tbr.put("4_166", 0.5346253469898729);
        tbr.put("6_58", 0.5343051717900035);
        tbr.put("4_30", 0.5331845585904608);
        tbr.put("4_300", 0.5324641643907546);
        tbr.put("5_115", 0.531759778951042);
        tbr.put("4_229", 0.5317437701910486);
        tbr.put("4_20", 0.5317437701910486);
        tbr.put("4_150", 0.5317437701910486);
        tbr.put("5_7", 0.5308792971514013);
        tbr.put("4_294", 0.5308792971514013);
        tbr.put("5_71", 0.5307832445914404);
        tbr.put("6_35", 0.5298227189918324);
        tbr.put("6_46", 0.5293424561920282);
        tbr.put("4_301", 0.5292944299120479);
        tbr.put("4_148", 0.5287021057922896);
        tbr.put("4_246", 0.5274214049928121);
        tbr.put("4_173", 0.5274214049928121);
        tbr.put("6_113", 0.52718127359291);
        tbr.put("4_261", 0.5245398281939878);
        tbr.put("4_262", 0.5245398281939878);
        tbr.put("5_80", 0.5238941415409178);
        tbr.put("4_94", 0.5229549609546343);
        tbr.put("5_31", 0.5192569373961432);
        tbr.put("6_18", 0.5192569373961432);
        tbr.put("4_285", 0.5187286483163588);
        tbr.put("4_84", 0.5182003592365743);
        tbr.put("4_202", 0.516855623397123);
        tbr.put("6_23", 0.5166154919972209);
        tbr.put("4_191", 0.5161054986431433);
        tbr.put("5_15", 0.515558913837652);
        tbr.put("6_39", 0.515558913837652);
        tbr.put("4_282", 0.5145023356780831);
        tbr.put("4_32", 0.5144543093981027);
        tbr.put("6_114", 0.5139740465982986);
        tbr.put("4_304", 0.5134457575185142);
        tbr.put("4_280", 0.5132696611585861);
        tbr.put("4_109", 0.5129656254024652);
        tbr.put("4_205", 0.5120529953990823);
        tbr.put("4_120", 0.5118608902791607);
        tbr.put("4_92", 0.5118608902791607);
        tbr.put("4_31", 0.5104521193997356);
        tbr.put("5_44", 0.510398756866424);
        tbr.put("6_51", 0.5102760230398075);
        tbr.put("1_299", 0.5093380403879452);
        tbr.put("4_107", 0.508691155800454);
        tbr.put("4_125", 0.508691155800454);
        tbr.put("5_18", 0.508691155800454);
        tbr.put("5_16", 0.5081628667206696);
        tbr.put("4_276", 0.5079867703607415);
        tbr.put("5_12", 0.5074584812809569);
        tbr.put("4_82", 0.5074584812809569);
        tbr.put("4_127", 0.5072823849210288);
        tbr.put("6_41", 0.5072503674010419);
        tbr.put("6_90", 0.5067540958412443);
        tbr.put("4_288", 0.5062976828267367);
        tbr.put("5_88", 0.5062653385973621);
        tbr.put("4_247", 0.5058095790016297);
        tbr.put("6_101", 0.5058095790016297);
        tbr.put("4_39", 0.5058095790016297);
        tbr.put("4_46", 0.5055214213217473);
        tbr.put("5_63", 0.5054894038017603);
        tbr.put("6_32", 0.5054040237484618);
        tbr.put("4_230", 0.5048490534020216);
        tbr.put("4_312", 0.5046409395221065);
        tbr.put("4_233", 0.5045288782021522);
        tbr.put("4_134", 0.5044864059817614);
        tbr.put("4_215", 0.5043687906022175);
        tbr.put("4_144", 0.5043687906022175);
        tbr.put("4_311", 0.5039365540823939);
        tbr.put("4_119", 0.5039365540823939);
        tbr.put("5_50", 0.5038885278024136);
        tbr.put("4_157", 0.5029280022028054);
        tbr.put("6_15", 0.5028799759228251);
        tbr.put("4_194", 0.502127564203132);
        tbr.put("4_9", 0.5019674766031973);
        tbr.put("4_115", 0.501903898956366);
        tbr.put("4_99", 0.5018233977632561);
        tbr.put("4_302", 0.5015215182890935);
        tbr.put("4_27", 0.5011670386035239);
        tbr.put("4_319", 0.5008315080624363);
        tbr.put("4_317", 0.5007668196036872);
        tbr.put("5_5", 0.5007668196036872);
        tbr.put("6_49", 0.5007668196036872);
        tbr.put("4_2", 0.500585495893557);
        tbr.put("4_8", 0.5005266882037851);
        tbr.put("5_2", 0.5003666006038504);
        tbr.put("4_213", 0.500046425403981);
        tbr.put("5_32", 0.4998396183616165);
        tbr.put("4_117", 0.49976917981764524);
        tbr.put("6_103", 0.499566162604177);
        tbr.put("4_295", 0.4995341450841901);
        tbr.put("4_291", 0.4992466408230829);
        tbr.put("4_269", 0.49918195236433377);
        tbr.put("4_66", 0.49918195236433377);
        tbr.put("5_73", 0.4991741113390309);
        tbr.put("4_192", 0.49884086776365666);
        tbr.put("4_296", 0.4988297596444775);
        tbr.put("4_34", 0.4986056370045689);
        tbr.put("4_287", 0.49782852605517164);
        tbr.put("6_97", 0.4978019319110193);
        tbr.put("4_4", 0.49755167252010096);
        tbr.put("6_81", 0.49728246398470055);
        tbr.put("4_321", 0.4968855120787401);
        tbr.put("5_61", 0.49684140631141116);
        tbr.put("4_25", 0.4966989610184081);
        tbr.put("6_79", 0.4966312232720411);
        tbr.put("4_201", 0.49636441060548336);
        tbr.put("5_1", 0.4962043230055486);
        tbr.put("6_40", 0.4956652525159727);
        tbr.put("4_151", 0.4949595602387096);
        tbr.put("4_16", 0.4946700957212767);
        tbr.put("4_174", 0.4942832718063324);
        tbr.put("6_2", 0.4942734705247038);
        tbr.put("4_265", 0.4942383165945957);
        tbr.put("4_182", 0.4941231842063978);
        tbr.put("4_232", 0.49396309660646304);
        tbr.put("4_88", 0.4938990615664892);
        tbr.put("4_137", 0.4935357607274537);
        tbr.put("4_228", 0.4932293073218645);
        tbr.put("6_16", 0.49301857976684843);
        tbr.put("4_38", 0.4930025710068549);
        tbr.put("4_106", 0.4928424834069203);
        tbr.put("4_149", 0.4928424834069203);
        tbr.put("4_35", 0.4928424834069203);
        tbr.put("4_45", 0.4928424834069203);
        tbr.put("4_293", 0.4928424834069203);
        tbr.put("4_159", 0.4925223082070509);
        tbr.put("6_22", 0.49243159190042124);
        tbr.put("6_24", 0.4923728931137786);
        tbr.put("5_58", 0.49236222060711626);
        tbr.put("4_86", 0.4923141943271358);
        tbr.put("6_95", 0.49223872445859523);
        tbr.put("4_126", 0.4921380979672077);
        tbr.put("5_79", 0.49190330282063677);
        tbr.put("6_52", 0.4918182494767259);
        tbr.put("6_13", 0.49178590524735133);
        tbr.put("4_198", 0.49175911507756637);
        tbr.put("6_25", 0.4916685076740659);
        tbr.put("4_243", 0.49140169500750813);
        tbr.put("4_169", 0.49140169500750813);
        tbr.put("4_171", 0.49140169500750813);
        tbr.put("5_112", 0.49125761616756686);
        tbr.put("4_121", 0.49125761616756686);
        tbr.put("4_79", 0.49125761616756686);
        tbr.put("4_184", 0.49090640357587345);
        tbr.put("4_270", 0.490842172510267);
        tbr.put("6_96", 0.4907293270877825);
        tbr.put("6_106", 0.4904411694079);
        tbr.put("4_309", 0.490201038007998);
        tbr.put("4_217", 0.4901209942080306);
        tbr.put("4_241", 0.4900197142978679);
        tbr.put("4_90", 0.48971012448215745);
        tbr.put("4_53", 0.48967274892821355);
        tbr.put("4_75", 0.48967274892821355);
        tbr.put("4_279", 0.48967274892821355);
        tbr.put("4_196", 0.48963746431435035);
        tbr.put("4_135", 0.4893543379590373);
        tbr.put("5_91", 0.4892199297169697);
        tbr.put("4_220", 0.48916046860842255);
        tbr.put("4_11", 0.48916046860842255);
        tbr.put("4_273", 0.48916027258279);
        tbr.put("4_114", 0.4891444598484291);
        tbr.put("4_226", 0.48908336519294376);
        tbr.put("5_43", 0.48902586434072237);
        tbr.put("5_33", 0.4889396130623902);
        tbr.put("4_236", 0.4888402934085532);
        tbr.put("4_64", 0.48883179896447504);
        tbr.put("4_142", 0.4887847528126575);
        tbr.put("4_24", 0.48868020580861843);
        tbr.put("4_316", 0.4885730451294786);
        tbr.put("4_163", 0.4885201182086838);
        tbr.put("5_30", 0.4882554836047101);
        tbr.put("6_11", 0.48808788168886014);
        tbr.put("5_84", 0.48808788168886014);
        tbr.put("4_7", 0.48784382977630664);
        tbr.put("6_21", 0.48775365798532305);
        tbr.put("4_281", 0.4876537502545883);
        tbr.put("6_12", 0.48752724837970113);
        tbr.put("6_29", 0.48744099710136896);
        tbr.put("6_53", 0.48732240159366225);
        tbr.put("6_36", 0.48726609867586207);
        tbr.put("4_264", 0.4872073998892194);
        tbr.put("4_206", 0.48717211527535625);
        tbr.put("5_76", 0.4870891310909003);
        tbr.put("6_56", 0.4870852105782489);
        tbr.put("4_211", 0.48707932980927166);
        tbr.put("6_4", 0.4870303234011284);
        tbr.put("6_8", 0.48697260474264853);
        tbr.put("4_208", 0.4867591546094023);
        tbr.put("4_297", 0.4867157676027261);
        tbr.put("4_283", 0.4866834233733515);
        tbr.put("4_81", 0.48667911080943493);
        tbr.put("5_100", 0.48659906700946765);
        tbr.put("4_52", 0.48650301444950683);
        tbr.put("4_96", 0.48650301444950683);
        tbr.put("4_204", 0.4864076153083212);
        tbr.put("4_210", 0.4863442336871226);
        tbr.put("4_5", 0.4863148298422366);
        tbr.put("1_41", 0.48627660484388485);
        tbr.put("4_310", 0.48592584964533414);
        tbr.put("5_13", 0.48568123143650876);
        tbr.put("4_238", 0.4856385414098595);
        tbr.put("5_3", 0.4856385414098595);
        tbr.put("4_167", 0.4856385414098595);
        tbr.put("4_130", 0.48558371957461655);
        tbr.put("6_93", 0.48550513507658055);
        tbr.put("4_60", 0.4854464362899379);
        tbr.put("4_18", 0.48531509911611387);
        tbr.put("4_87", 0.4853113092872175);
        tbr.put("4_260", 0.48522688758145605);
        tbr.put("5_87", 0.4851529423567243);
        tbr.put("4_239", 0.4849328491325964);
        tbr.put("6_59", 0.4848838427244531);
        tbr.put("6_1", 0.48483483631630986);
        tbr.put("4_108", 0.48474205085022526);
        tbr.put("4_123", 0.48474205085022526);
        tbr.put("5_62", 0.48469761837350867);
        tbr.put("6_68", 0.48467801581025133);
        tbr.put("4_277", 0.48464573692275437);
        tbr.put("6_86", 0.4846246532769398);
        tbr.put("4_320", 0.48456595449029705);
        tbr.put("4_28", 0.4845179282103167);
        tbr.put("5_17", 0.48450845363807565);
        tbr.put("4_131", 0.4844653279989096);
        tbr.put("6_109", 0.48438985813036894);
        tbr.put("5_20", 0.4843311593437263);
        tbr.put("4_212", 0.4842859645451052);
        tbr.put("4_138", 0.4841977530104473);
        tbr.put("4_168", 0.4841977530104473);
        tbr.put("6_82", 0.4841977530104473);
        tbr.put("4_37", 0.4841389453206754);
        tbr.put("6_37", 0.4838615690505845);
        tbr.put("5_24", 0.4838351055901871);
        tbr.put("6_33", 0.4837537549526693);
        tbr.put("5_77", 0.48350404007739706);
        tbr.put("4_255", 0.48343325304341234);
        tbr.put("4_181", 0.4834084231299531);
        tbr.put("5_11", 0.4833332799708);
        tbr.put("6_74", 0.4832372274108392);
        tbr.put("4_258", 0.483185607327595);
        tbr.put("1_158", 0.4830510030598948);
        tbr.put("4_14", 0.4828791539220057);
        tbr.put("4_26", 0.482874579990579);
        tbr.put("4_139", 0.4827569646110352);
        tbr.put("4_29", 0.4827569646110352);
        tbr.put("6_85", 0.4827569646110352);
        tbr.put("6_91", 0.48268759331773015);
        tbr.put("4_21", 0.48259753042987574);
        tbr.put("5_93", 0.48257858128539366);
        tbr.put("4_224", 0.4825263077833742);
        tbr.put("5_57", 0.4824901519444774);
        tbr.put("4_110", 0.48245279817115927);
        tbr.put("6_110", 0.4823940993845166);
        tbr.put("6_14", 0.4823940993845166);
        tbr.put("4_101", 0.48227670181123117);
        tbr.put("5_41", 0.48227670181123117);
        tbr.put("5_59", 0.48227670181123117);
        tbr.put("6_44", 0.48227670181123117);
        tbr.put("5_28", 0.48202872938602614);
        tbr.put("5_60", 0.4820098891446733);
        tbr.put("6_54", 0.48198320787801757);
        tbr.put("4_143", 0.4819565266113618);
        tbr.put("4_248", 0.48193823088565496);
        tbr.put("5_107", 0.48179643901142705);
        tbr.put("4_308", 0.48175344405601606);
        tbr.put("4_69", 0.4817484127314467);
        tbr.put("5_86", 0.4817484127314467);
        tbr.put("6_9", 0.4816897139448039);
        tbr.put("4_235", 0.4816396185053686);
        tbr.put("4_122", 0.48155434735519925);
        tbr.put("5_114", 0.48151361758487576);
        tbr.put("5_14", 0.4815004403062417);
        tbr.put("4_193", 0.48137955783282166);
        tbr.put("4_314", 0.4813329690741468);
        tbr.put("5_56", 0.48131617621162304);
        tbr.put("5_42", 0.4812788224383049);
        tbr.put("6_3", 0.48116915698719326);
        tbr.put("4_57", 0.48104402729173407);
        tbr.put("4_47", 0.48086793093180585);
        tbr.put("5_10", 0.48069183457187775);
        tbr.put("4_185", 0.4806104839343599);
        tbr.put("6_27", 0.480551676244588);
        tbr.put("4_278", 0.4804733313334363);
        tbr.put("6_48", 0.48041151791729825);
        tbr.put("5_104", 0.48040901314532647);
        tbr.put("5_102", 0.4803556506120149);
        tbr.put("5_96", 0.4803556506120149);
        tbr.put("5_99", 0.48032624676712893);
        tbr.put("6_61", 0.48030228807870334);
        tbr.put("6_87", 0.4801955630120802);
        tbr.put("4_80", 0.4801635454920933);
        tbr.put("5_83", 0.48014198267251024);
        tbr.put("4_292", 0.48006651280396967);
        tbr.put("4_234", 0.48003547541214553);
        tbr.put("4_59", 0.4799874491321651);
        tbr.put("4_74", 0.4799745114404153);
        tbr.put("4_245", 0.47987538781221084);
        tbr.put("4_146", 0.47987538781221084);
        tbr.put("4_140", 0.47987538781221084);
        tbr.put("6_20", 0.4798700515588797);
        tbr.put("4_253", 0.4797153002122762);
        tbr.put("4_93", 0.47964603782210036);
        tbr.put("4_227", 0.47964473098454985);
        tbr.put("5_81", 0.4795921307731428);
        tbr.put("5_23", 0.47951785883902337);
        tbr.put("6_72", 0.4794833365470647);
        tbr.put("5_95", 0.47939512501240683);
        tbr.put("6_83", 0.47939512501240683);
        tbr.put("6_45", 0.4792902512989802);
        tbr.put("4_63", 0.47928306369245255);
        tbr.put("5_35", 0.4792686884793972);
        tbr.put("6_34", 0.47925790706960564);
        tbr.put("4_15", 0.47922850322471966);
        tbr.put("5_92", 0.4792243649058098);
        tbr.put("6_5", 0.47916969553494776);
        tbr.put("5_111", 0.47910696733252434);
        tbr.put("5_26", 0.47907494981253745);
        tbr.put("5_74", 0.4790618814370326);
        tbr.put("4_104", 0.4790314974639837);
        tbr.put("4_156", 0.47896386862074597);
        tbr.put("5_27", 0.47895602759544315);
        tbr.put("4_161", 0.4789148622126027);
        tbr.put("6_47", 0.47890506093097407);
        tbr.put("5_109", 0.4788854583677168);
        tbr.put("5_53", 0.4788854583677168);
        tbr.put("4_197", 0.47884625324120217);
        tbr.put("4_175", 0.4787580417065443);
        tbr.put("4_318", 0.4787547746126681);
        tbr.put("4_250", 0.4787547746126681);
        tbr.put("4_147", 0.4787547746126681);
        tbr.put("4_67", 0.4787080551702381);
        tbr.put("5_37", 0.4787080551702381);
        tbr.put("4_128", 0.4785973660297119);
        tbr.put("4_152", 0.47859468701273333);
        tbr.put("4_55", 0.4785786782527399);
        tbr.put("4_51", 0.4785786782527399);
        tbr.put("5_8", 0.4785786782527399);
        tbr.put("4_77", 0.4785786782527399);
        tbr.put("6_102", 0.4785620160739712);
        tbr.put("5_34", 0.4784612806794545);
        tbr.put("4_214", 0.4784345994127987);
        tbr.put("4_170", 0.4784345994127987);
        tbr.put("4_221", 0.47835096180956754);
        tbr.put("4_183", 0.4783169840332549);
        tbr.put("5_9", 0.47828518431952627);
        tbr.put("5_55", 0.47827451181286407);
        tbr.put("4_3", 0.47825817634348294);
        tbr.put("6_75", 0.47822114927955245);
        tbr.put("6_42", 0.47817976609045365);
        tbr.put("5_110", 0.47816778674624094);
        tbr.put("4_216", 0.4781144242129293);
        tbr.put("5_85", 0.4781042962219131);
        tbr.put("5_75", 0.47806215071090985);
        tbr.put("6_19", 0.4780503891729555);
        tbr.put("6_69", 0.4780425481476525);
        tbr.put("4_83", 0.47800151344856723);
        tbr.put("4_252", 0.47796413789462333);
        tbr.put("6_71", 0.4779249327681087);
        tbr.put("4_113", 0.4778994494358742);
        tbr.put("6_65", 0.47788572764159404);
        tbr.put("4_195", 0.4778759263599654);
        tbr.put("5_66", 0.47785632379670806);
        tbr.put("6_111", 0.47781319815754203);
        tbr.put("4_102", 0.4778024167477505);
        tbr.put("4_68", 0.4778024167477505);
        tbr.put("4_85", 0.47777007251837594);
        tbr.put("5_65", 0.4777485096987929);
        tbr.put("6_38", 0.4777485096987929);
        tbr.put("1_110", 0.47770538405962687);
        tbr.put("5_38", 0.47769819645309913);
        tbr.put("6_105", 0.4776875239464368);
        tbr.put("5_105", 0.4776875239464368);
        tbr.put("5_64", 0.4776875239464368);
        tbr.put("6_66", 0.4776875239464368);
        tbr.put("4_298", 0.4776730398302523);
        tbr.put("5_6", 0.4776514770106692);
        tbr.put("4_254", 0.47764069560087774);
        tbr.put("5_90", 0.47764069560087774);
        tbr.put("5_101", 0.4776341614131253);
        tbr.put("6_43", 0.4775975699617116);
        tbr.put("5_108", 0.47758188791110584);
        tbr.put("5_98", 0.4775426827845912);
        tbr.put("6_63", 0.4775274363465022);
        tbr.put("4_305", 0.47752210009317103);
        tbr.put("4_112", 0.4775163500079489);
        tbr.put("5_103", 0.47747407381319057);
        tbr.put("5_48", 0.47747407381319057);
        tbr.put("5_69", 0.47747407381319057);
        tbr.put("6_10", 0.47743584881483886);
        tbr.put("1_2", 0.47743486868667595);
        tbr.put("5_67", 0.4774250674050473);
        tbr.put("6_84", 0.47742071127987906);
        tbr.put("4_43", 0.4773460037332429);
        tbr.put("5_106", 0.47730745202550345);
        tbr.put("4_50", 0.47728490907775756);
        tbr.put("4_56", 0.477252564848383);
        tbr.put("6_67", 0.47722904177247427);
        tbr.put("6_70", 0.4772072611466328);
        tbr.put("6_112", 0.4771986577994254);
        tbr.put("4_266", 0.4771929077142033);
        tbr.put("6_108", 0.477180035364331);
        tbr.put("4_242", 0.4771538986133212);
        tbr.put("5_52", 0.47714083023781634);
        tbr.put("6_17", 0.47713396934067626);
        tbr.put("5_54", 0.4771005360800097);
        tbr.put("6_73", 0.4771005360800097);
        tbr.put("4_286", 0.4770908437015102);
        tbr.put("4_145", 0.4770820225480444);
        tbr.put("5_68", 0.4770820225480444);
        tbr.put("5_36", 0.4770800622917187);
        tbr.put("5_97", 0.47705261870315846);
        tbr.put("5_117", 0.47704771806234414);
        tbr.put("5_40", 0.4770261552427611);
        tbr.put("6_100", 0.4770036122950152);
        tbr.put("6_99", 0.4770036122950152);
        tbr.put("4_17", 0.4765279234266379);
        tbr.put("4_12", 0.47611626959823433);
        tbr.put("4_78", 0.47526591040413235);
        tbr.put("4_71", 0.4749288116579841);
        tbr.put("4_10", 0.4745284619743924);
        tbr.put("4_222", 0.4743226350601906);
        tbr.put("4_189", 0.4742638273704187);
        tbr.put("4_180", 0.4736463466278136);
        tbr.put("4_76", 0.4735840104766553);
        tbr.put("4_322", 0.4722442406198957);
        tbr.put("4_49", 0.47082109452741516);
        tbr.put("4_70", 0.4703495875391994);
        tbr.put("4_165", 0.47026490446592784);
        tbr.put("4_207", 0.46929457758469106);
        tbr.put("4_153", 0.46904235793744703);
        tbr.put("4_98", 0.4681178357123548);
        tbr.put("4_105", 0.4681041792599523);
        tbr.put("4_178", 0.4652662508353142);
        tbr.put("4_42", 0.465193198616242);
        tbr.put("4_190", 0.46464877009270905);
        tbr.put("4_187", 0.46197302020808645);
        tbr.put("4_97", 0.46021218729256);
        tbr.put("4_44", 0.45804512392446456);
        tbr.put("4_58", 0.007203941997060792);
        return tbr;
    }

    private Map<String, Double> getCachedPlayRateMap() {
        Map<String, Double> tbr = new HashMap<>();
        // Missing rare Ring
        tbr.put("4_2", 0.9938825448613376);
        tbr.put("4_302", 0.9759380097879282);
        tbr.put("4_109", 0.5778955954323002);
        tbr.put("5_32", 0.5738172920065253);
        tbr.put("4_191", 0.5411908646003263);
        tbr.put("6_104", 0.517536704730832);
        tbr.put("6_40", 0.5061174551386624);
        tbr.put("5_88", 0.4685970636215334);
        tbr.put("5_45", 0.46125611745513867);
        tbr.put("6_122", 0.4596247960848287);
        tbr.put("4_117", 0.4469820554649266);
        tbr.put("6_97", 0.44208809135399674);
        tbr.put("6_52", 0.42251223491027734);
        tbr.put("4_4", 0.42088091353996737);
        tbr.put("5_73", 0.41272430668841764);
        tbr.put("1_299", 0.3866231647634584);
        tbr.put("6_81", 0.38499184339314846);
        tbr.put("4_270", 0.383768352365416);
        tbr.put("5_61", 0.38132137030995106);
        tbr.put("6_95", 0.3792822185970636);
        tbr.put("5_22", 0.3637846655791191);
        tbr.put("4_184", 0.36133768352365414);
        tbr.put("5_115", 0.3609298531810767);
        tbr.put("4_90", 0.3592985318107667);
        tbr.put("4_115", 0.35195758564437196);
        tbr.put("4_16", 0.34910277324632955);
        tbr.put("4_287", 0.34828711256117456);
        tbr.put("4_265", 0.3425774877650897);
        tbr.put("4_249", 0.33849918433931486);
        tbr.put("6_2", 0.3368678629690049);
        tbr.put("5_91", 0.32300163132137033);
        tbr.put("6_58", 0.31933115823817293);
        tbr.put("4_112", 0.3099510603588907);
        tbr.put("5_33", 0.3066884176182708);
        tbr.put("4_25", 0.29975530179445353);
        tbr.put("4_288", 0.29893964110929855);
        tbr.put("4_206", 0.2973083197389886);
        tbr.put("4_135", 0.2936378466557912);
        tbr.put("4_198", 0.29078303425774876);
        tbr.put("4_248", 0.28629690048939643);
        tbr.put("4_137", 0.28466557911908646);
        tbr.put("4_310", 0.283442088091354);
        tbr.put("4_204", 0.28099510603588906);
        tbr.put("4_132", 0.27895595432300163);
        tbr.put("4_134", 0.27732463295269166);
        tbr.put("4_74", 0.27446982055464925);
        tbr.put("4_319", 0.2736541598694943);
        tbr.put("4_281", 0.2724306688417618);
        tbr.put("4_181", 0.2724306688417618);
        tbr.put("4_266", 0.27202283849918435);
        tbr.put("5_76", 0.2699836867862969);
        tbr.put("6_56", 0.26753670473083196);
        tbr.put("5_30", 0.2671288743882545);
        tbr.put("6_12", 0.26468189233278955);
        tbr.put("5_43", 0.26305057096247964);
        tbr.put("4_314", 0.2606035889070147);
        tbr.put("4_193", 0.2606035889070147);
        tbr.put("5_80", 0.25570962479608483);
        tbr.put("6_4", 0.25489396411092985);
        tbr.put("4_17", 0.2536704730831974);
        tbr.put("6_21", 0.2536704730831974);
        tbr.put("4_221", 0.2532626427406199);
        tbr.put("4_283", 0.250815660685155);
        tbr.put("4_273", 0.24918433931484502);
        tbr.put("4_277", 0.24755301794453508);
        tbr.put("4_226", 0.24225122349102773);
        tbr.put("6_53", 0.23776508972267538);
        tbr.put("4_136", 0.23368678629690048);
        tbr.put("6_1", 0.233278955954323);
        tbr.put("4_291", 0.23164763458401305);
        tbr.put("5_51", 0.23083197389885807);
        tbr.put("4_228", 0.22960848287112562);
        tbr.put("4_14", 0.22960848287112562);
        tbr.put("4_297", 0.2263458401305057);
        tbr.put("4_364", 0.21900489396411094);
        tbr.put("4_321", 0.21859706362153344);
        tbr.put("4_192", 0.21818923327895595);
        tbr.put("4_130", 0.21492659053833604);
        tbr.put("6_33", 0.21451876019575855);
        tbr.put("5_116", 0.2141109298531811);
        tbr.put("4_308", 0.21044045676998369);
        tbr.put("4_87", 0.2096247960848287);
        tbr.put("4_71", 0.2092169657422512);
        tbr.put("5_44", 0.20799347471451876);
        tbr.put("4_207", 0.20432300163132136);
        tbr.put("4_227", 0.2030995106035889);
        tbr.put("4_165", 0.2030995106035889);
        tbr.put("4_222", 0.199836867862969);
        tbr.put("5_63", 0.1994290375203915);
        tbr.put("4_224", 0.1965742251223491);
        tbr.put("5_62", 0.1965742251223491);
        tbr.put("4_151", 0.19535073409461665);
        tbr.put("6_51", 0.19494290375203915);
        tbr.put("5_15", 0.19371941272430668);
        tbr.put("4_10", 0.19249592169657423);
        tbr.put("4_189", 0.19086460032626426);
        tbr.put("4_21", 0.18923327895595432);
        tbr.put("5_24", 0.18882544861337683);
        tbr.put("4_278", 0.18841761827079934);
        tbr.put("5_12", 0.1867862969004894);
        tbr.put("4_258", 0.18515497553017946);
        tbr.put("6_59", 0.18393148450244698);
        tbr.put("4_263", 0.183115823817292);
        tbr.put("5_28", 0.183115823817292);
        tbr.put("6_29", 0.18270799347471453);
        tbr.put("6_90", 0.18270799347471453);
        tbr.put("4_188", 0.18230016313213704);
        tbr.put("4_49", 0.17944535073409462);
        tbr.put("4_83", 0.17903752039151713);
        tbr.put("6_37", 0.1765905383360522);
        tbr.put("5_93", 0.1729200652528548);
        tbr.put("4_268", 0.16924959216965743);
        tbr.put("4_12", 0.16557911908646003);
        tbr.put("4_196", 0.16557911908646003);
        tbr.put("5_17", 0.16517128874388254);
        tbr.put("4_180", 0.15823817292006526);
        tbr.put("6_32", 0.15456769983686786);
        tbr.put("6_88", 0.15456769983686786);
        tbr.put("4_322", 0.14722675367047308);
        tbr.put("4_36", 0.1468189233278956);
        tbr.put("4_316", 0.14233278955954323);
        tbr.put("4_142", 0.1407014681892333);
        tbr.put("4_13", 0.1407014681892333);
        tbr.put("4_241", 0.13947797716150082);
        tbr.put("6_60", 0.1362153344208809);
        tbr.put("4_267", 0.13213703099510604);
        tbr.put("5_2", 0.13132137030995106);
        tbr.put("4_128", 0.12928221859706363);
        tbr.put("4_78", 0.12438825448613376);
        tbr.put("4_105", 0.12357259380097879);
        tbr.put("5_46", 0.1231647634584013);
        tbr.put("6_121", 0.12194127243066884);
        tbr.put("6_3", 0.12194127243066884);
        tbr.put("5_121", 0.1199021207177814);
        tbr.put("6_50", 0.1199021207177814);
        tbr.put("5_72", 0.11949429037520391);
        tbr.put("5_47", 0.11908646003262642);
        tbr.put("5_19", 0.11867862969004894);
        tbr.put("4_7", 0.11827079934747145);
        tbr.put("4_148", 0.11786296900489396);
        tbr.put("4_64", 0.11623164763458402);
        tbr.put("5_82", 0.1133768352365416);
        tbr.put("4_76", 0.11256117455138662);
        tbr.put("5_1", 0.11052202283849918);
        tbr.put("4_210", 0.10970636215334421);
        tbr.put("6_48", 0.1068515497553018);
        tbr.put("6_79", 0.10236541598694943);
        tbr.put("6_76", 0.10195758564437195);
        tbr.put("6_31", 0.10154975530179446);
        tbr.put("6_80", 0.10114192495921696);
        tbr.put("6_57", 0.0999184339314845);
        tbr.put("4_179", 0.09951060358890701);
        tbr.put("5_83", 0.09910277324632953);
        tbr.put("5_122", 0.09869494290375204);
        tbr.put("6_30", 0.09869494290375204);
        tbr.put("4_239", 0.09828711256117455);
        tbr.put("4_260", 0.09747145187601958);
        tbr.put("4_18", 0.0966557911908646);
        tbr.put("4_285", 0.0966557911908646);
        tbr.put("5_81", 0.0966557911908646);
        tbr.put("5_14", 0.09543230016313213);
        tbr.put("6_13", 0.09543230016313213);
        tbr.put("4_5", 0.09502446982055465);
        tbr.put("6_92", 0.09420880913539967);
        tbr.put("5_79", 0.0933931484502447);
        tbr.put("6_16", 0.09257748776508973);
        tbr.put("6_78", 0.09216965742251224);
        tbr.put("4_202", 0.09176182707993474);
        tbr.put("1_41", 0.09053833605220228);
        tbr.put("5_99", 0.08931484502446982);
        tbr.put("4_205", 0.08768352365415986);
        tbr.put("4_42", 0.08727569331158239);
        tbr.put("6_45", 0.0868678629690049);
        tbr.put("6_22", 0.08605220228384992);
        tbr.put("4_131", 0.08523654159869494);
        tbr.put("6_24", 0.08523654159869494);
        tbr.put("4_31", 0.08442088091353997);
        tbr.put("4_190", 0.08442088091353997);
        tbr.put("4_280", 0.08401305057096248);
        tbr.put("4_37", 0.08360522022838499);
        tbr.put("6_25", 0.08360522022838499);
        tbr.put("4_200", 0.08319738988580751);
        tbr.put("6_27", 0.08197389885807504);
        tbr.put("6_28", 0.08115823817292006);
        tbr.put("4_299", 0.08034257748776509);
        tbr.put("4_282", 0.0799347471451876);
        tbr.put("4_178", 0.0799347471451876);
        tbr.put("5_70", 0.07952691680261012);
        tbr.put("6_6", 0.07626427406199021);
        tbr.put("1_158", 0.07259380097879282);
        tbr.put("6_34", 0.07218597063621533);
        tbr.put("5_89", 0.07218597063621533);
        tbr.put("4_255", 0.07177814029363784);
        tbr.put("5_94", 0.07177814029363784);
        tbr.put("4_276", 0.06973898858075041);
        tbr.put("4_212", 0.06933115823817292);
        tbr.put("4_26", 0.06933115823817292);
        tbr.put("5_4", 0.06892332789559544);
        tbr.put("4_127", 0.06729200652528548);
        tbr.put("4_70", 0.06729200652528548);
        tbr.put("5_35", 0.0664763458401305);
        tbr.put("4_82", 0.0664763458401305);
        tbr.put("6_5", 0.06566068515497553);
        tbr.put("4_187", 0.06566068515497553);
        tbr.put("6_94", 0.06525285481239804);
        tbr.put("6_8", 0.06443719412724307);
        tbr.put("4_312", 0.06280587275693311);
        tbr.put("5_49", 0.06280587275693311);
        tbr.put("5_74", 0.06158238172920065);
        tbr.put("4_289", 0.06076672104404568);
        tbr.put("4_194", 0.06076672104404568);
        tbr.put("4_233", 0.06035889070146819);
        tbr.put("5_37", 0.059135399673735725);
        tbr.put("5_78", 0.059135399673735725);
        tbr.put("6_55", 0.05872756933115824);
        tbr.put("4_118", 0.05831973898858075);
        tbr.put("4_97", 0.05791190864600326);
        tbr.put("4_27", 0.05750407830342578);
        tbr.put("4_219", 0.0566884176182708);
        tbr.put("4_225", 0.0566884176182708);
        tbr.put("6_47", 0.0566884176182708);
        tbr.put("5_87", 0.0566884176182708);
        tbr.put("5_25", 0.05587275693311582);
        tbr.put("4_235", 0.05546492659053834);
        tbr.put("4_365", 0.05546492659053834);
        tbr.put("4_9", 0.05546492659053834);
        tbr.put("5_13", 0.055057096247960846);
        tbr.put("4_218", 0.05464926590538336);
        tbr.put("4_244", 0.05464926590538336);
        tbr.put("6_7", 0.05464926590538336);
        tbr.put("4_8", 0.05464926590538336);
        tbr.put("5_109", 0.054241435562805876);
        tbr.put("4_99", 0.053833605220228384);
        tbr.put("6_93", 0.0534257748776509);
        tbr.put("4_44", 0.05301794453507341);
        tbr.put("4_230", 0.05261011419249592);
        tbr.put("4_274", 0.05261011419249592);
        tbr.put("4_295", 0.052202283849918436);
        tbr.put("6_86", 0.051794453507340944);
        tbr.put("4_122", 0.05138662316476346);
        tbr.put("4_257", 0.05138662316476346);
        tbr.put("5_39", 0.05138662316476346);
        tbr.put("5_53", 0.05138662316476346);
        tbr.put("4_154", 0.05138662316476346);
        tbr.put("6_36", 0.05138662316476346);
        tbr.put("4_34", 0.050978792822185974);
        tbr.put("4_296", 0.050163132137031);
        tbr.put("4_185", 0.050163132137031);
        tbr.put("4_66", 0.04934747145187602);
        tbr.put("5_29", 0.04853181076672104);
        tbr.put("4_73", 0.04812398042414356);
        tbr.put("4_201", 0.047716150081566065);
        tbr.put("5_113", 0.04730831973898858);
        tbr.put("4_303", 0.04730831973898858);
        tbr.put("5_27", 0.04730831973898858);
        tbr.put("6_72", 0.0464926590538336);
        tbr.put("5_71", 0.04567699836867863);
        tbr.put("4_100", 0.04526916802610114);
        tbr.put("6_46", 0.0432300163132137);
        tbr.put("4_232", 0.042822185970636216);
        tbr.put("4_158", 0.042822185970636216);
        tbr.put("5_20", 0.040783034257748776);
        tbr.put("6_26", 0.040783034257748776);
        tbr.put("6_35", 0.040783034257748776);
        tbr.put("4_284", 0.04037520391517129);
        tbr.put("5_75", 0.04037520391517129);
        tbr.put("4_23", 0.0399673735725938);
        tbr.put("4_38", 0.039559543230016314);
        tbr.put("4_6", 0.039559543230016314);
        tbr.put("6_102", 0.03915171288743882);
        tbr.put("4_88", 0.03915171288743882);
        tbr.put("6_113", 0.03874388254486134);
        tbr.put("4_89", 0.03874388254486134);
        tbr.put("5_57", 0.03874388254486134);
        tbr.put("4_98", 0.03874388254486134);
        tbr.put("5_85", 0.03874388254486134);
        tbr.put("4_153", 0.03833605220228385);
        tbr.put("4_272", 0.03792822185970636);
        tbr.put("4_164", 0.03792822185970636);
        tbr.put("4_292", 0.03792822185970636);
        tbr.put("5_7", 0.037520391517128875);
        tbr.put("6_91", 0.037520391517128875);
        tbr.put("6_14", 0.0367047308319739);
        tbr.put("4_33", 0.035481239804241435);
        tbr.put("4_159", 0.035481239804241435);
        tbr.put("4_126", 0.03507340946166395);
        tbr.put("4_315", 0.03466557911908646);
        tbr.put("4_116", 0.03466557911908646);
        tbr.put("4_58", 0.03466557911908646);
        tbr.put("5_77", 0.03466557911908646);
        tbr.put("6_42", 0.03425774877650897);
        tbr.put("4_182", 0.03425774877650897);
        tbr.put("6_110", 0.03303425774877651);
        tbr.put("5_60", 0.03303425774877651);
        tbr.put("4_186", 0.03303425774877651);
        tbr.put("6_18", 0.03262642740619902);
        tbr.put("4_19", 0.03181076672104405);
        tbr.put("4_40", 0.03181076672104405);
        tbr.put("6_69", 0.03181076672104405);
        tbr.put("6_54", 0.031402936378466556);
        tbr.put("4_217", 0.03099510603588907);
        tbr.put("4_11", 0.03099510603588907);
        tbr.put("5_114", 0.030587275693311582);
        tbr.put("4_220", 0.030587275693311582);
        tbr.put("4_237", 0.030587275693311582);
        tbr.put("6_111", 0.030587275693311582);
        tbr.put("4_309", 0.030587275693311582);
        tbr.put("6_23", 0.030587275693311582);
        tbr.put("4_256", 0.030179445350734094);
        tbr.put("5_107", 0.030179445350734094);
        tbr.put("4_251", 0.029771615008156605);
        tbr.put("6_39", 0.029771615008156605);
        tbr.put("4_176", 0.029771615008156605);
        tbr.put("4_93", 0.02936378466557912);
        tbr.put("5_31", 0.028548123980424143);
        tbr.put("4_86", 0.028548123980424143);
        tbr.put("4_114", 0.028140293637846654);
        tbr.put("4_236", 0.02773246329526917);
        tbr.put("6_114", 0.02732463295269168);
        tbr.put("4_15", 0.02732463295269168);
        tbr.put("4_24", 0.026916802610114192);
        tbr.put("5_42", 0.026101141924959218);
        tbr.put("6_65", 0.026101141924959218);
        tbr.put("4_104", 0.02569331158238173);
        tbr.put("6_9", 0.02569331158238173);
        tbr.put("6_41", 0.02569331158238173);
        tbr.put("4_41", 0.024877650897226752);
        tbr.put("6_101", 0.024469820554649267);
        tbr.put("5_18", 0.024469820554649267);
        tbr.put("5_90", 0.024469820554649267);
        tbr.put("6_87", 0.024469820554649267);
        tbr.put("4_103", 0.02406199021207178);
        tbr.put("5_10", 0.02406199021207178);
        tbr.put("5_16", 0.02406199021207178);
        tbr.put("4_53", 0.02406199021207178);
        tbr.put("4_264", 0.02365415986949429);
        tbr.put("4_22", 0.0232463295269168);
        tbr.put("4_156", 0.0232463295269168);
        tbr.put("4_199", 0.0232463295269168);
        tbr.put("5_50", 0.022838499184339316);
        tbr.put("4_271", 0.022838499184339316);
        tbr.put("6_38", 0.022430668841761828);
        tbr.put("4_208", 0.02202283849918434);
        tbr.put("4_177", 0.02202283849918434);
        tbr.put("4_133", 0.02161500815660685);
        tbr.put("5_104", 0.02161500815660685);
        tbr.put("4_67", 0.02161500815660685);
        tbr.put("4_203", 0.021207177814029365);
        tbr.put("5_65", 0.021207177814029365);
        tbr.put("4_160", 0.021207177814029365);
        tbr.put("4_81", 0.020799347471451877);
        tbr.put("4_313", 0.0199836867862969);
        tbr.put("6_15", 0.0199836867862969);
        tbr.put("4_91", 0.0199836867862969);
        tbr.put("4_197", 0.0199836867862969);
        tbr.put("6_43", 0.01957585644371941);
        tbr.put("6_61", 0.01957585644371941);
        tbr.put("6_103", 0.019168026101141926);
        tbr.put("5_108", 0.019168026101141926);
        tbr.put("4_28", 0.019168026101141926);
        tbr.put("4_96", 0.019168026101141926);
        tbr.put("6_71", 0.019168026101141926);
        tbr.put("4_223", 0.018760195758564437);
        tbr.put("4_240", 0.018760195758564437);
        tbr.put("4_138", 0.01835236541598695);
        tbr.put("4_65", 0.01835236541598695);
        tbr.put("6_83", 0.01835236541598695);
        tbr.put("4_51", 0.01794453507340946);
        tbr.put("5_5", 0.01794453507340946);
        tbr.put("4_111", 0.017536704730831975);
        tbr.put("5_23", 0.017536704730831975);
        tbr.put("5_6", 0.017536704730831975);
        tbr.put("4_175", 0.017536704730831975);
        tbr.put("4_108", 0.017128874388254486);
        tbr.put("5_66", 0.017128874388254486);
        tbr.put("5_98", 0.017128874388254486);
        tbr.put("4_61", 0.016721044045676998);
        tbr.put("4_124", 0.01631321370309951);
        tbr.put("4_259", 0.01631321370309951);
        tbr.put("4_48", 0.01631321370309951);
        tbr.put("4_166", 0.01631321370309951);
        tbr.put("4_123", 0.015905383360522024);
        tbr.put("4_30", 0.015905383360522024);
        tbr.put("5_26", 0.015905383360522024);
        tbr.put("4_60", 0.015905383360522024);
        tbr.put("4_229", 0.015497553017944535);
        tbr.put("4_20", 0.015497553017944535);
        tbr.put("5_92", 0.015497553017944535);
        tbr.put("4_183", 0.015089722675367047);
        tbr.put("5_111", 0.01468189233278956);
        tbr.put("6_20", 0.01468189233278956);
        tbr.put("6_49", 0.01468189233278956);
        tbr.put("4_246", 0.014274061990212071);
        tbr.put("4_300", 0.014274061990212071);
        tbr.put("6_10", 0.014274061990212071);
        tbr.put("4_173", 0.014274061990212071);
        tbr.put("4_294", 0.013866231647634585);
        tbr.put("4_320", 0.013458401305057096);
        tbr.put("4_252", 0.013458401305057096);
        tbr.put("4_261", 0.013458401305057096);
        tbr.put("4_301", 0.013458401305057096);
        tbr.put("4_3", 0.013458401305057096);
        tbr.put("4_150", 0.013458401305057096);
        tbr.put("5_58", 0.013050570962479609);
        tbr.put("4_143", 0.01264274061990212);
        tbr.put("4_110", 0.012234910277324634);
        tbr.put("4_262", 0.012234910277324634);
        tbr.put("4_195", 0.012234910277324634);
        tbr.put("4_101", 0.011827079934747145);
        tbr.put("4_94", 0.011827079934747145);
        tbr.put("4_113", 0.011419249592169658);
        tbr.put("6_106", 0.011419249592169658);
        tbr.put("5_112", 0.01101141924959217);
        tbr.put("5_8", 0.01101141924959217);
        tbr.put("5_67", 0.01101141924959217);
        tbr.put("4_32", 0.010603588907014683);
        tbr.put("4_84", 0.010603588907014683);
        tbr.put("6_96", 0.010603588907014683);
        tbr.put("4_102", 0.010195758564437194);
        tbr.put("4_68", 0.010195758564437194);
        tbr.put("5_34", 0.009787928221859706);
        tbr.put("4_85", 0.009787928221859706);
        tbr.put("4_304", 0.009380097879282219);
        tbr.put("4_57", 0.009380097879282219);
        tbr.put("5_110", 0.00897226753670473);
        tbr.put("4_120", 0.00897226753670473);
        tbr.put("5_9", 0.00897226753670473);
        tbr.put("4_92", 0.00897226753670473);
        tbr.put("1_110", 0.00897226753670473);
        tbr.put("6_11", 0.008564437194127243);
        tbr.put("5_55", 0.008564437194127243);
        tbr.put("5_84", 0.008564437194127243);
        tbr.put("4_107", 0.008156606851549755);
        tbr.put("4_125", 0.008156606851549755);
        tbr.put("4_247", 0.008156606851549755);
        tbr.put("5_100", 0.008156606851549755);
        tbr.put("4_39", 0.008156606851549755);
        tbr.put("4_215", 0.007748776508972268);
        tbr.put("4_234", 0.007748776508972268);
        tbr.put("4_254", 0.007748776508972268);
        tbr.put("4_144", 0.007748776508972268);
        tbr.put("6_112", 0.007748776508972268);
        tbr.put("4_298", 0.007748776508972268);
        tbr.put("5_3", 0.00734094616639478);
        tbr.put("6_19", 0.00734094616639478);
        tbr.put("4_80", 0.00734094616639478);
        tbr.put("4_157", 0.00734094616639478);
        tbr.put("4_311", 0.006933115823817292);
        tbr.put("4_253", 0.006933115823817292);
        tbr.put("4_59", 0.006933115823817292);
        tbr.put("4_213", 0.0065252854812398045);
        tbr.put("5_106", 0.0065252854812398045);
        tbr.put("4_47", 0.0065252854812398045);
        tbr.put("6_68", 0.0065252854812398045);
        tbr.put("4_317", 0.006117455138662317);
        tbr.put("4_119", 0.006117455138662317);
        tbr.put("6_67", 0.006117455138662317);
        tbr.put("1_2", 0.006117455138662317);
        tbr.put("6_82", 0.006117455138662317);
        tbr.put("4_269", 0.005709624796084829);
        tbr.put("6_109", 0.005709624796084829);
        tbr.put("4_46", 0.005301794453507341);
        tbr.put("4_63", 0.005301794453507341);
        tbr.put("5_52", 0.005301794453507341);
        tbr.put("5_64", 0.005301794453507341);
        tbr.put("6_75", 0.005301794453507341);
        tbr.put("6_74", 0.005301794453507341);
        tbr.put("5_105", 0.004893964110929853);
        tbr.put("5_101", 0.004893964110929853);
        tbr.put("5_11", 0.004893964110929853);
        tbr.put("5_38", 0.004893964110929853);
        tbr.put("4_161", 0.004893964110929853);
        tbr.put("4_174", 0.004893964110929853);
        tbr.put("6_66", 0.004893964110929853);
        tbr.put("6_85", 0.004893964110929853);
        tbr.put("4_250", 0.004486133768352365);
        tbr.put("4_147", 0.004486133768352365);
        tbr.put("4_149", 0.004486133768352365);
        tbr.put("4_35", 0.004486133768352365);
        tbr.put("6_17", 0.004486133768352365);
        tbr.put("5_59", 0.004486133768352365);
        tbr.put("4_318", 0.004078303425774877);
        tbr.put("4_106", 0.004078303425774877);
        tbr.put("4_243", 0.004078303425774877);
        tbr.put("4_45", 0.004078303425774877);
        tbr.put("5_41", 0.004078303425774877);
        tbr.put("4_152", 0.004078303425774877);
        tbr.put("4_169", 0.004078303425774877);
        tbr.put("6_44", 0.004078303425774877);
        tbr.put("4_171", 0.004078303425774877);
        tbr.put("4_293", 0.004078303425774877);
        tbr.put("4_121", 0.00367047308319739);
        tbr.put("4_50", 0.00367047308319739);
        tbr.put("4_77", 0.00367047308319739);
        tbr.put("4_79", 0.00367047308319739);
        tbr.put("5_48", 0.00367047308319739);
        tbr.put("5_56", 0.00367047308319739);
        tbr.put("4_170", 0.00367047308319739);
        tbr.put("5_86", 0.00367047308319739);
        tbr.put("6_63", 0.00367047308319739);
        tbr.put("6_105", 0.0032626427406199023);
        tbr.put("4_56", 0.0032626427406199023);
        tbr.put("5_36", 0.0032626427406199023);
        tbr.put("4_75", 0.0032626427406199023);
        tbr.put("4_279", 0.0032626427406199023);
        tbr.put("4_163", 0.0032626427406199023);
        tbr.put("6_84", 0.0032626427406199023);
        tbr.put("4_216", 0.0028548123980424145);
        tbr.put("4_211", 0.0028548123980424145);
        tbr.put("5_102", 0.0028548123980424145);
        tbr.put("5_68", 0.0028548123980424145);
        tbr.put("5_96", 0.0028548123980424145);
        tbr.put("4_238", 0.0024469820554649264);
        tbr.put("4_52", 0.0024469820554649264);
        tbr.put("4_167", 0.0024469820554649264);
        tbr.put("5_117", 0.0020391517128874386);
        tbr.put("6_108", 0.0020391517128874386);
        tbr.put("4_168", 0.0020391517128874386);
        tbr.put("5_95", 0.0020391517128874386);
        tbr.put("4_139", 0.0016313213703099511);
        tbr.put("5_97", 0.0016313213703099511);
        tbr.put("4_145", 0.0012234910277324632);
        tbr.put("4_305", 0.0012234910277324632);
        tbr.put("4_29", 0.0012234910277324632);
        tbr.put("4_69", 0.0012234910277324632);
        tbr.put("5_40", 0.0012234910277324632);
        tbr.put("4_286", 0.0012234910277324632);
        tbr.put("6_70", 0.0012234910277324632);
        tbr.put("4_245", 8.156606851549756E-4);
        tbr.put("4_146", 8.156606851549756E-4);
        tbr.put("4_140", 8.156606851549756E-4);
        tbr.put("4_43", 8.156606851549756E-4);
        tbr.put("5_54", 8.156606851549756E-4);
        tbr.put("4_214", 4.078303425774878E-4);
        tbr.put("4_242", 4.078303425774878E-4);
        tbr.put("6_100", 4.078303425774878E-4);
        tbr.put("5_103", 4.078303425774878E-4);
        tbr.put("4_55", 4.078303425774878E-4);
        tbr.put("5_69", 4.078303425774878E-4);
        tbr.put("6_73", 4.078303425774878E-4);
        tbr.put("6_99", 4.078303425774878E-4);
        return tbr;
    }
}
