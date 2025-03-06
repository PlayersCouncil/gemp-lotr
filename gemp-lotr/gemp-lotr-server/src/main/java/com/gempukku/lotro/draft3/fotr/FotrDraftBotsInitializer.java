package com.gempukku.lotro.draft3.fotr;

import com.gempukku.lotro.common.AppConfig;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.ReplayMetadata;
import com.gempukku.util.JsonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class FotrDraftBotsInitializer {
    private static final boolean READ_NEW_DATA = false;

    private static final double RARE_FREQUENCY = 1.0 / 11;
    private static final double UNCOMMON_FREQUENCY = 3.0 / 11;
    private static final double COMMON_FREQUENCY = 7.0 / 11;

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

            // Normalize count based on rarities and performance
            Map<String, Double> frequencyMap = normalizeCountByRarityAndPerformance(winningMap, losingMap, library);

            // Calculate win rate
            Map<String, Double> winRateMap = getWinRateMap(winningMap, losingMap);

            // Combine both maps for final value
            Map<String, Double> valuesMap = combineMaps(frequencyMap, winRateMap);

            // Boost rare cards and return the result
            return boostRareCards(valuesMap, library);
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

    private Map<String, Double> normalizeCountByRarityAndPerformance(Map<String, Integer> winningMap, Map<String, Integer> losingMap, LotroCardBlueprintLibrary library) {
        Map<String, Double> normalizedMap = new HashMap<>();

        winningMap.entrySet().forEach(entry -> normalizedMap.put(entry.getKey(), getRarityNormalizedValue(library, entry)));
        losingMap.entrySet().forEach(entry -> normalizedMap.put(entry.getKey(), getRarityNormalizedValue(library, entry) / 2)); // Being in losing deck is less important

        // Make sure nothing is greater than 1
        double max = normalizedMap.values().stream().max(Comparator.comparingDouble(value -> value)).orElse(1.0);
        // Divide all values by max
        normalizedMap.replaceAll((card, value) -> value / max);

        return normalizedMap;
    }

    private double getRarityNormalizedValue(LotroCardBlueprintLibrary library, Map.Entry<String, Integer> entry) {
        double expectedFrequency = 1.0;
        try {
            expectedFrequency = switch (library.getLotroCardBlueprint(entry.getKey()).getCardInfo().rarity) {
                case "R" -> RARE_FREQUENCY;
                case "U" -> UNCOMMON_FREQUENCY;
                case "C" -> COMMON_FREQUENCY;
                default -> 1.0; // Fallback for unknown cards
            };
        } catch (CardNotFoundException ignore) {

        }

        // Normalize by expected rarity frequency
        return entry.getValue() / expectedFrequency;
    }

    private Map<String, Double> getWinRateMap(Map<String, Integer> winningMap, Map<String, Integer> losingMap) {
        double smoothingFactor = 1.0;

        Map<String, Double> winRateMap = new HashMap<>();

        winningMap.forEach((key, value) -> {
            int wins = value;
            int losses = losingMap.getOrDefault(key, 0);
            winRateMap.put(key, ((double) wins + smoothingFactor) / (wins + losses + smoothingFactor * 2));
        });
        losingMap.forEach((key, value) -> {
            if (!winRateMap.containsKey(key)) {
                winRateMap.put(key, smoothingFactor / (value + smoothingFactor * 2));
            }
        });

        return winRateMap;
    }

    private Map<String, Double> combineMaps(Map<String, Double> frequencyMap, Map<String, Double> winRateMap) {
        Map<String, Double> tbr = new HashMap<>();
        frequencyMap.forEach((key, entry) -> tbr.put(key, entry * 0.4 + winRateMap.getOrDefault(key, 0.5) * 0.6));
        return tbr;
    }

    private Map<String, Double> boostRareCards(Map<String, Double> valuesMap, LotroCardBlueprintLibrary library) {
        Map<String, Double> tbr = new HashMap<>();
        valuesMap.forEach((key, entry) -> {
            try {
                if (library.getLotroCardBlueprint(key).getCardInfo().rarity.equals("R")) {
                    tbr.put(key, entry * 1.1);
                } else {
                    tbr.put(key, entry);
                }
            } catch (CardNotFoundException e) {
                tbr.put(key, entry);
            }
        });
        return tbr;
    }


    private Map<String, Double> getCachedValuesMap(){
        Map<String, Double> tbr = new HashMap<>();
        tbr.put("1_97", 0.6970283344851417);
        tbr.put("1_231", 0.6620151159369192);
        tbr.put("2_108", 0.622758552048809);
        tbr.put("3_38", 0.6166906711319767);
        tbr.put("2_67", 0.6116585009447021);
        tbr.put("1_234", 0.6023303064160046);
        tbr.put("1_112", 0.6013394549350579);
        tbr.put("1_270", 0.5771532423999821);
        tbr.put("2_32", 0.568961147667159);
        tbr.put("2_109", 0.5663167503423094);
        tbr.put("3_60", 0.5609304076708128);
        tbr.put("3_57", 0.5408972779886168);
        tbr.put("2_84", 0.5375215934827726);
        tbr.put("2_85", 0.5304489905603638);
        tbr.put("3_68", 0.5221446413011909);
        tbr.put("3_108", 0.5191677139187484);
        tbr.put("2_93", 0.5143912845038001);
        tbr.put("3_13", 0.5095259975692183);
        tbr.put("3_63", 0.5088630923962862);
        tbr.put("3_43", 0.5072316236619774);
        tbr.put("3_65", 0.5060462503264241);
        tbr.put("3_7", 0.5032381813352197);
        tbr.put("3_58", 0.4990714809900447);
        tbr.put("3_64", 0.4978625860657412);
        tbr.put("1_100", 0.4974098585120949);
        tbr.put("1_314", 0.4962049292560475);
        tbr.put("2_105", 0.4909803973261142);
        tbr.put("2_3", 0.48716367507893);
        tbr.put("3_66", 0.4842272874280999);
        tbr.put("2_60", 0.4836894673550722);
        tbr.put("1_89", 0.4831275177107932);
        tbr.put("1_170", 0.4812049292560475);
        tbr.put("3_21", 0.4793967937015061);
        tbr.put("3_69", 0.4781433266686652);
        tbr.put("3_42", 0.47809660699419276);
        tbr.put("2_46", 0.47674681912448846);
        tbr.put("1_178", 0.4757851455848463);
        tbr.put("3_67", 0.47470232853597605);
        tbr.put("3_75", 0.4745222485510463);
        tbr.put("1_50", 0.4740701471052909);
        tbr.put("3_80", 0.470925650092219);
        tbr.put("1_90", 0.4702121939135592);
        tbr.put("1_69", 0.46579332301766657);
        tbr.put("2_10", 0.4641561201148323);
        tbr.put("1_127", 0.463742583295299);
        tbr.put("1_308", 0.4630079532322752);
        tbr.put("3_10", 0.4628127354337215);
        tbr.put("1_14", 0.4554082585038898);
        tbr.put("1_313", 0.4548167854067533);
        tbr.put("2_80", 0.4530172523961662);
        tbr.put("2_48", 0.45279365841518426);
        tbr.put("1_2", 0.4495990089326465);
        tbr.put("1_230", 0.4465282307889259);
        tbr.put("1_49", 0.4434584487174333);
        tbr.put("1_252", 0.4412049292560475);
        tbr.put("1_111", 0.4412049292560475);
        tbr.put("1_23", 0.4406024646280237);
        tbr.put("2_49", 0.44016782536852417);
        tbr.put("3_41", 0.43851008134883346);
        tbr.put("1_262", 0.43482311086144954);
        tbr.put("1_236", 0.43394892983256866);
        tbr.put("1_237", 0.43352493968833544);
        tbr.put("3_74", 0.4305621884685221);
        tbr.put("1_228", 0.4300711466695305);
        tbr.put("2_37", 0.43005689565727667);
        tbr.put("1_13", 0.42983056270497394);
        tbr.put("1_298", 0.42874105692808284);
        tbr.put("1_21", 0.42868108614343214);
        tbr.put("1_56", 0.42750484764294927);
        tbr.put("1_229", 0.42631874386859514);
        tbr.put("1_95", 0.425979834861624);
        tbr.put("1_125", 0.4249613834146292);
        tbr.put("1_139", 0.42477869041584077);
        tbr.put("1_131", 0.42346311835129724);
        tbr.put("2_22", 0.4229015904223572);
        tbr.put("2_50", 0.42220561387494293);
        tbr.put("1_114", 0.4216677791174089);
        tbr.put("1_51", 0.42077163847488686);
        tbr.put("2_51", 0.4194392498716111);
        tbr.put("1_299", 0.41874869844051943);
        tbr.put("3_19", 0.4181023104345788);
        tbr.put("1_9", 0.4168640587430534);
        tbr.put("1_66", 0.41492599595748847);
        tbr.put("1_166", 0.4137049292560475);
        tbr.put("3_29", 0.4133023206825124);
        tbr.put("2_47", 0.41326826491891316);
        tbr.put("1_165", 0.413194381053806);
        tbr.put("2_75", 0.4107966608265485);
        tbr.put("1_176", 0.41073855038553264);
        tbr.put("1_267", 0.40904410086718396);
        tbr.put("1_317", 0.40838250635717543);
        tbr.put("1_47", 0.4077326483606296);
        tbr.put("2_20", 0.40751387641524717);
        tbr.put("1_296", 0.4069269093727439);
        tbr.put("3_96", 0.40652572401147336);
        tbr.put("1_57", 0.4061879310859634);
        tbr.put("1_183", 0.40560495774885696);
        tbr.put("2_61", 0.40557726162472857);
        tbr.put("1_289", 0.40512951295600086);
        tbr.put("1_365", 0.40430491951908687);
        tbr.put("1_75", 0.40323311944586143);
        tbr.put("2_44", 0.402176656948696);
        tbr.put("3_106", 0.4016871436749921);
        tbr.put("1_158", 0.40141199560247937);
        tbr.put("1_45", 0.4013623315477191);
        tbr.put("1_143", 0.4002838937349084);
        tbr.put("1_273", 0.40015648431896716);
        tbr.put("2_101", 0.40002429106364845);
        tbr.put("1_27", 0.39993205958295114);
        tbr.put("2_19", 0.3990123231401187);
        tbr.put("2_112", 0.3974111386937695);
        tbr.put("1_42", 0.39723448952556945);
        tbr.put("3_62", 0.39450722504201047);
        tbr.put("3_61", 0.3904898536854511);
        tbr.put("1_128", 0.39035092239513547);
        tbr.put("2_4", 0.38970497724793);
        tbr.put("3_12", 0.3890197661763157);
        tbr.put("2_103", 0.38896609122882375);
        tbr.put("1_220", 0.38842691624553893);
        tbr.put("1_148", 0.3876810090867599);
        tbr.put("3_17", 0.38767898273062107);
        tbr.put("1_302", 0.3875138553824086);
        tbr.put("2_5", 0.38632957588422945);
        tbr.put("1_227", 0.38589976402497644);
        tbr.put("1_40", 0.3856228309637195);
        tbr.put("3_35", 0.38559096773076357);
        tbr.put("2_114", 0.3855855372377508);
        tbr.put("1_133", 0.3854893458689908);
        tbr.put("2_18", 0.38504526995511806);
        tbr.put("1_156", 0.384256856994871);
        tbr.put("1_152", 0.3840459071427097);
        tbr.put("2_82", 0.38389409913150147);
        tbr.put("1_161", 0.3838251828433391);
        tbr.put("3_71", 0.3837699680511183);
        tbr.put("1_106", 0.38366419067012403);
        tbr.put("1_16", 0.38350802690849917);
        tbr.put("2_102", 0.3831187764632389);
        tbr.put("1_72", 0.3827018302806607);
        tbr.put("1_173", 0.3815719438759736);
        tbr.put("2_6", 0.3789521423768395);
        tbr.put("3_34", 0.3771292335708667);
        tbr.put("2_12", 0.37514773877324237);
        tbr.put("1_311", 0.37353840422966983);
        tbr.put("1_15", 0.3733774532177088);
        tbr.put("1_102", 0.3730389014526274);
        tbr.put("1_196", 0.3728565382586378);
        tbr.put("2_52", 0.3721216469320373);
        tbr.put("3_85", 0.3718594786157275);
        tbr.put("1_240", 0.37173501988654883);
        tbr.put("1_71", 0.3684740605507379);
        tbr.put("1_154", 0.3680942703888273);
        tbr.put("1_150", 0.3673937807974512);
        tbr.put("1_107", 0.3669042268098245);
        tbr.put("1_44", 0.3661290857214251);
        tbr.put("2_98", 0.36571380527997305);
        tbr.put("2_121", 0.36528149839062907);
        tbr.put("1_96", 0.365050221803388);
        tbr.put("1_290", 0.36448443025880956);
        tbr.put("3_102", 0.36318575992697405);
        tbr.put("1_250", 0.36308399225332133);
        tbr.put("1_145", 0.362928835262501);
        tbr.put("1_5", 0.3629079860768671);
        tbr.put("1_30", 0.3618991276098384);
        tbr.put("1_33", 0.3608176651870282);
        tbr.put("1_46", 0.3605476951163852);
        tbr.put("1_244", 0.36037293713517027);
        tbr.put("1_285", 0.36018256503879503);
        tbr.put("1_264", 0.3601287283412234);
        tbr.put("1_246", 0.3601287283412234);
        tbr.put("3_5", 0.35982310077275376);
        tbr.put("2_100", 0.3596504640194736);
        tbr.put("1_153", 0.359422709132516);
        tbr.put("1_286", 0.3592782844722598);
        tbr.put("1_175", 0.35925329325296274);
        tbr.put("1_162", 0.3592321051450851);
        tbr.put("2_110", 0.35835857700303164);
        tbr.put("1_186", 0.35761031972903506);
        tbr.put("3_27", 0.35560680992603616);
        tbr.put("1_159", 0.3553373568855181);
        tbr.put("1_364", 0.35497045644671515);
        tbr.put("3_59", 0.353928891027986);
        tbr.put("1_92", 0.35295626308251626);
        tbr.put("2_122", 0.35209439820981503);
        tbr.put("1_36", 0.351288030794933);
        tbr.put("1_301", 0.35073026015518033);
        tbr.put("1_121", 0.3502616587785105);
        tbr.put("3_121", 0.3501300167530406);
        tbr.put("1_318", 0.34982711090826113);
        tbr.put("1_259", 0.3491608175953224);
        tbr.put("1_303", 0.34886267999458326);
        tbr.put("1_209", 0.3487783723857969);
        tbr.put("2_39", 0.3478911912368781);
        tbr.put("3_50", 0.3469183919312239);
        tbr.put("1_116", 0.346703568150802);
        tbr.put("2_96", 0.34469605990192576);
        tbr.put("2_89", 0.3446736355680159);
        tbr.put("2_106", 0.3437182099215629);
        tbr.put("3_122", 0.34366623791543915);
        tbr.put("1_48", 0.34352084920219383);
        tbr.put("1_260", 0.3418262799225673);
        tbr.put("3_77", 0.3415669395513291);
        tbr.put("2_26", 0.3413994574076331);
        tbr.put("1_256", 0.3410577361935189);
        tbr.put("3_39", 0.3398646918407161);
        tbr.put("1_123", 0.3396394340483797);
        tbr.put("3_25", 0.3395823412812837);
        tbr.put("1_12", 0.3388006721712792);
        tbr.put("2_94", 0.3387295474012617);
        tbr.put("1_146", 0.33842706343392587);
        tbr.put("2_83", 0.33837658066421455);
        tbr.put("1_187", 0.33720190389254745);
        tbr.put("1_34", 0.3360921638678608);
        tbr.put("1_26", 0.33606100587199733);
        tbr.put("2_13", 0.33568534821457974);
        tbr.put("3_8", 0.3355911936757241);
        tbr.put("3_40", 0.33554541305340024);
        tbr.put("1_224", 0.3354221816522136);
        tbr.put("2_31", 0.3353415487600791);
        tbr.put("1_80", 0.3348197170241899);
        tbr.put("1_212", 0.3330123231401187);
        tbr.put("1_138", 0.33227598527917357);
        tbr.put("1_200", 0.3318073938840712);
        tbr.put("3_23", 0.33060246462802373);
        tbr.put("1_377", 0.33060246462802373);
        tbr.put("2_42", 0.3298757527475714);
        tbr.put("3_1", 0.3298252852578731);
        tbr.put("1_142", 0.3297610459209964);
        tbr.put("1_281", 0.3295272818039156);
        tbr.put("3_99", 0.32931643436435776);
        tbr.put("1_258", 0.32922948016648834);
        tbr.put("1_147", 0.32886900958466453);
        tbr.put("2_33", 0.32845276129621176);
        tbr.put("1_309", 0.3283660429027841);
        tbr.put("2_65", 0.3278193814734653);
        tbr.put("1_11", 0.3274227578749823);
        tbr.put("1_3", 0.32689507617407143);
        tbr.put("3_20", 0.32645327914896605);
        tbr.put("2_62", 0.326048632328079);
        tbr.put("3_56", 0.32603089397506174);
        tbr.put("1_188", 0.3252953144218645);
        tbr.put("1_204", 0.3240295755362848);
        tbr.put("1_168", 0.32363050966685547);
        tbr.put("2_74", 0.3235928536951361);
        tbr.put("2_90", 0.32350578428326016);
        tbr.put("1_122", 0.32346813387434104);
        tbr.put("1_19", 0.32276972767195156);
        tbr.put("3_49", 0.32256041539473757);
        tbr.put("2_72", 0.3219569567812379);
        tbr.put("1_94", 0.32170846387639557);
        tbr.put("1_155", 0.3208301339705212);
        tbr.put("2_71", 0.31995030173943917);
        tbr.put("2_14", 0.3193476751723053);
        tbr.put("1_233", 0.3192014013100275);
        tbr.put("1_98", 0.3190923520235427);
        tbr.put("2_29", 0.3187042739063928);
        tbr.put("1_151", 0.3185871018603163);
        tbr.put("2_38", 0.31786363247375626);
        tbr.put("1_108", 0.31744912573380124);
        tbr.put("1_280", 0.3172574155374054);
        tbr.put("1_195", 0.3162788680967595);
        tbr.put("2_7", 0.3153505969396334);
        tbr.put("1_59", 0.315298217847251);
        tbr.put("1_219", 0.3148691286488202);
        tbr.put("1_271", 0.3137087019264382);
        tbr.put("1_68", 0.31356770714796256);
        tbr.put("3_82", 0.31309752015822306);
        tbr.put("1_307", 0.3122172523961661);
        tbr.put("2_69", 0.31174130086463764);
        tbr.put("1_110", 0.31136197565949375);
        tbr.put("1_117", 0.3111800311792252);
        tbr.put("1_194", 0.31097847839061893);
        tbr.put("1_184", 0.31047417419564155);
        tbr.put("1_191", 0.3104285378854491);
        tbr.put("1_217", 0.3099552327218699);
        tbr.put("2_2", 0.3084378651927716);
        tbr.put("1_272", 0.30635326335006846);
        tbr.put("1_103", 0.30633167863492733);
        tbr.put("2_59", 0.3049648269936378);
        tbr.put("2_15", 0.30489903838587323);
        tbr.put("1_119", 0.30391210797418006);
        tbr.put("3_91", 0.30388656855231094);
        tbr.put("1_157", 0.30383386581469646);
        tbr.put("2_76", 0.30318625334599775);
        tbr.put("1_58", 0.30125187455173763);
        tbr.put("3_36", 0.3004694529569016);
        tbr.put("1_74", 0.30036513007759014);
        tbr.put("1_211", 0.30036513007759014);
        tbr.put("3_2", 0.30036513007759014);
        tbr.put("1_61", 0.3001564843189672);
        tbr.put("1_65", 0.2990863136501813);
        tbr.put("1_261", 0.29874526293023246);
        tbr.put("1_180", 0.29853700503388014);
        tbr.put("1_6", 0.29758888056054145);
        tbr.put("1_31", 0.2971457492622836);
        tbr.put("2_64", 0.2968597664563693);
        tbr.put("1_164", 0.296608404420131);
        tbr.put("3_26", 0.293830131529812);
        tbr.put("3_94", 0.29371288921821737);
        tbr.put("3_31", 0.2933126426289365);
        tbr.put("1_179", 0.2926352393992741);
        tbr.put("1_306", 0.29216096546082854);
        tbr.put("1_190", 0.29203620299165545);
        tbr.put("3_114", 0.29094948866742565);
        tbr.put("2_43", 0.2908443633044272);
        tbr.put("3_55", 0.2903739227721513);
        tbr.put("3_111", 0.2899339412929826);
        tbr.put("1_163", 0.2878177588669189);
        tbr.put("1_294", 0.2878171854934501);
        tbr.put("1_232", 0.2875399361022364);
        tbr.put("1_160", 0.2872594059396751);
        tbr.put("2_11", 0.28707439525330897);
        tbr.put("1_79", 0.28707439525330897);
        tbr.put("2_53", 0.28707439525330897);
        tbr.put("1_132", 0.2870160922247906);
        tbr.put("1_316", 0.28701559232802265);
        tbr.put("3_97", 0.28678478071449315);
        tbr.put("1_185", 0.28674335349155633);
        tbr.put("2_8", 0.2849850512053908);
        tbr.put("1_136", 0.28481154079852605);
        tbr.put("2_113", 0.28466453674121406);
        tbr.put("1_62", 0.28461240037917357);
        tbr.put("3_104", 0.2843030579643998);
        tbr.put("2_104", 0.28381615518634823);
        tbr.put("1_218", 0.2834498740452529);
        tbr.put("1_113", 0.28308619829535203);
        tbr.put("1_135", 0.28273847558192605);
        tbr.put("1_83", 0.2822873117298038);
        tbr.put("1_76", 0.2822817784679948);
        tbr.put("2_41", 0.2819329275434904);
        tbr.put("1_192", 0.2817995696681228);
        tbr.put("1_149", 0.28165760961209424);
        tbr.put("1_181", 0.28154267457781834);
        tbr.put("1_37", 0.28132143873179866);
        tbr.put("1_101", 0.2808824891988071);
        tbr.put("2_55", 0.2805942914446974);
        tbr.put("1_17", 0.28057437769897836);
        tbr.put("1_207", 0.2805640298830142);
        tbr.put("1_87", 0.28021932475606603);
        tbr.put("3_73", 0.2801017531613284);
        tbr.put("1_120", 0.2796640803286171);
        tbr.put("2_16", 0.2790332886736061);
        tbr.put("1_214", 0.2786147877681424);
        tbr.put("3_30", 0.27611484327303903);
        tbr.put("1_283", 0.2756830668430755);
        tbr.put("3_93", 0.27529171060293683);
        tbr.put("3_52", 0.27378053760599586);
        tbr.put("1_257", 0.27340518906014116);
        tbr.put("1_305", 0.2731184835246907);
        tbr.put("1_205", 0.2706271109082611);
        tbr.put("3_28", 0.27024153237415866);
        tbr.put("1_235", 0.2700052290979655);
        tbr.put("1_263", 0.2693184238551651);
        tbr.put("1_213", 0.26931527496205404);
        tbr.put("3_54", 0.2688197170241899);
        tbr.put("1_41", 0.26856715201671444);
        tbr.put("1_201", 0.26850647916592174);
        tbr.put("1_70", 0.2683097520158223);
        tbr.put("1_32", 0.26819424150532784);
        tbr.put("1_223", 0.267807036691604);
        tbr.put("3_107", 0.26739692682184696);
        tbr.put("3_100", 0.26680739388407115);
        tbr.put("1_268", 0.26661349262330547);
        tbr.put("1_247", 0.2663282342721691);
        tbr.put("1_118", 0.266092909729526);
        tbr.put("2_73", 0.2652049292560475);
        tbr.put("1_167", 0.2652049292560475);
        tbr.put("1_279", 0.26424415671767276);
        tbr.put("2_111", 0.2639605203103606);
        tbr.put("2_107", 0.2631107602353609);
        tbr.put("1_274", 0.26306034568293213);
        tbr.put("3_14", 0.26175357113828945);
        tbr.put("3_11", 0.26112443875864766);
        tbr.put("3_70", 0.26084872486169935);
        tbr.put("1_4", 0.2608202386385864);
        tbr.put("1_63", 0.26061159287996344);
        tbr.put("3_18", 0.2598606775561604);
        tbr.put("1_78", 0.2581485208082652);
        tbr.put("2_28", 0.25783016081832094);
        tbr.put("1_177", 0.2563386853954781);
        tbr.put("1_282", 0.25593450479233226);
        tbr.put("1_182", 0.2558930415953164);
        tbr.put("1_291", 0.25408387849282416);
        tbr.put("3_79", 0.25405750798722043);
        tbr.put("3_98", 0.2532353972177553);
        tbr.put("1_7", 0.2520130902321368);
        tbr.put("3_37", 0.2515799858506001);
        tbr.put("2_9", 0.25140386855809294);
        tbr.put("1_197", 0.25109913933624567);
        tbr.put("3_83", 0.25109539023277044);
        tbr.put("2_54", 0.2499369076704164);
        tbr.put("2_57", 0.24990985851209493);
        tbr.put("1_67", 0.24952773662070316);
        tbr.put("3_53", 0.24748516659059788);
        tbr.put("1_126", 0.2469374714742127);
        tbr.put("2_88", 0.2468774858186086);
        tbr.put("2_63", 0.2466584077720545);
        tbr.put("1_199", 0.2457485783364378);
        tbr.put("2_35", 0.24468725442085357);
        tbr.put("1_278", 0.24226902262502445);
        tbr.put("2_70", 0.24146052031036055);
        tbr.put("1_238", 0.24146052031036055);
        tbr.put("3_101", 0.24065126302749112);
        tbr.put("1_315", 0.240391210797418);
        tbr.put("1_84", 0.2399474973172811);
        tbr.put("1_73", 0.23976895654211947);
        tbr.put("1_253", 0.2389658227508256);
        tbr.put("1_29", 0.23863532633500684);
        tbr.put("3_95", 0.23698613876614788);
        tbr.put("1_85", 0.23696616026602335);
        tbr.put("2_68", 0.2368969312964802);
        tbr.put("3_89", 0.23661122312798616);
        tbr.put("1_226", 0.23657234139662256);
        tbr.put("1_198", 0.2355442352144403);
        tbr.put("1_8", 0.23535611949753718);
        tbr.put("1_24", 0.23423918079086348);
        tbr.put("1_104", 0.23366642338683385);
        tbr.put("1_248", 0.23150083470605676);
        tbr.put("1_251", 0.2306898125560679);
        tbr.put("1_141", 0.22877920497272392);
        tbr.put("1_266", 0.22805144421986046);
        tbr.put("2_78", 0.2280025049621602);
        tbr.put("1_202", 0.22514326284294334);
        tbr.put("1_255", 0.22472544073202336);
        tbr.put("1_297", 0.2245002282062985);
        tbr.put("1_174", 0.22438156093108166);
        tbr.put("1_216", 0.22421725239616613);
        tbr.put("1_43", 0.22347409677395985);
        tbr.put("3_3", 0.22301232314011865);
        tbr.put("1_288", 0.2229872204472844);
        tbr.put("1_269", 0.22115786855894212);
        tbr.put("1_208", 0.22083943404837975);
        tbr.put("1_28", 0.22060246462802374);
        tbr.put("1_35", 0.22060246462802374);
        tbr.put("1_55", 0.22060246462802374);
        tbr.put("2_45", 0.22060246462802374);
        tbr.put("1_169", 0.22060246462802374);
        tbr.put("1_378", 0.22060246462802374);
        tbr.put("1_243", 0.22060246462802374);
        tbr.put("1_60", 0.2192772084145886);
        tbr.put("3_22", 0.2188051649484785);
        tbr.put("3_48", 0.2172624272122218);
        tbr.put("2_95", 0.21503674491106678);
        tbr.put("3_76", 0.2142513147155965);
        tbr.put("3_86", 0.21302278018822687);
        tbr.put("3_78", 0.2124025040295413);
        tbr.put("2_92", 0.21221914737504982);
        tbr.put("3_45", 0.21219078046554085);
        tbr.put("3_16", 0.2109389059138032);
        tbr.put("1_171", 0.20979104240668345);
        tbr.put("1_277", 0.20923518289104778);
        tbr.put("3_90", 0.20743561322292495);
        tbr.put("3_24", 0.20741948770468993);
        tbr.put("2_56", 0.2034891956121437);
        tbr.put("1_221", 0.2016147877681424);
        tbr.put("2_97", 0.2016147877681424);
        tbr.put("3_6", 0.20139354027989353);
        tbr.put("3_88", 0.20091282519397533);
        tbr.put("1_91", 0.2005476951163852);
        tbr.put("1_25", 0.20039121079741798);
        tbr.put("2_17", 0.20018256503879506);
        tbr.put("1_54", 0.20018256503879506);
        tbr.put("1_292", 0.20018256503879506);
        tbr.put("1_375", 0.20014937139537778);
        tbr.put("1_18", 0.20007824215948358);
        tbr.put("1_10", 0.20007824215948358);
        tbr.put("1_312", 0.20007824215948358);
        tbr.put("1_38", 0.1936760950818459);
        tbr.put("1_109", 0.1913338658146965);
        tbr.put("2_1", 0.1909310816978549);
        tbr.put("1_193", 0.1903709982395514);
        tbr.put("1_304", 0.18938846237579887);
        tbr.put("2_81", 0.1893256503879507);
        tbr.put("2_87", 0.18659993128169888);
        tbr.put("2_34", 0.1810953902327704);
        tbr.put("3_51", 0.18046945295690162);
        tbr.put("2_99", 0.18033535102618012);
        tbr.put("3_112", 0.1788140336006607);
        tbr.put("1_293", 0.1764101806856928);
        tbr.put("2_23", 0.1758366337362524);
        tbr.put("3_109", 0.17575884027732497);
        tbr.put("1_129", 0.17196210201608464);
        tbr.put("2_124", 0.1717415400665058);
        tbr.put("2_79", 0.16885744713220752);
        tbr.put("3_72", 0.16885744713220752);
        tbr.put("1_93", 0.16620492925604746);
        tbr.put("1_310", 0.16620492925604746);
        tbr.put("3_110", 0.16620492925604746);
        tbr.put("3_33", 0.16443342143935477);
        tbr.put("2_58", 0.16418405875274883);
        tbr.put("1_134", 0.15704215658574397);
        tbr.put("1_284", 0.15408032861706983);
        tbr.put("2_21", 0.15297320206037687);
        tbr.put("1_124", 0.1502814544348091);
        tbr.put("1_300", 0.1501564843189672);
        tbr.put("2_40", 0.14819199906469144);
        tbr.put("3_44", 0.1474532177088088);
        tbr.put("3_84", 0.14613849429200768);
        tbr.put("2_91", 0.14403077524939686);
        tbr.put("2_30", 0.14337441046706223);
        tbr.put("1_275", 0.14336725105377615);
        tbr.put("1_239", 0.1401046238106941);
        tbr.put("1_105", 0.13698463410923475);
        tbr.put("1_210", 0.13621725239616614);
        tbr.put("1_249", 0.13442872356610375);
        tbr.put("1_115", 0.1338073938840712);
        tbr.put("1_86", 0.13380278629023495);
        tbr.put("3_81", 0.131111364673665);
        tbr.put("3_46", 0.13039707895937927);
        tbr.put("3_47", 0.13039707895937927);
        tbr.put("3_32", 0.12954647896108018);
        tbr.put("1_53", 0.12935385016626458);
        tbr.put("1_77", 0.1205476951163852);
        tbr.put("1_370", 0.1205476951163852);
        tbr.put("1_20", 0.1202347264784508);
        tbr.put("3_87", 0.11734679703246766);
        tbr.put("1_172", 0.11324749573614548);
        tbr.put("2_36", 0.11240985851209492);
        tbr.put("1_64", 0.10255591054313098);
        tbr.put("2_24", 0.1003129686379344);
        tbr.put("1_82", 0.1003129686379344);
        tbr.put("3_92", 0.09849669100867184);
        tbr.put("1_245", 0.09522957553628482);
        tbr.put("2_27", 0.057396341677491844);
        tbr.put("1_276", 0.05183204016430854);
        tbr.put("3_4", 0.048162061580591935);
        tbr.put("1_39", 0.047014509908165775);
        return tbr;
    }
}
