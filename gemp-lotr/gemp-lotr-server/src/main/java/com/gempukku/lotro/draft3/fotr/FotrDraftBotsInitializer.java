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
            if (summary.GameReplayInfo != null && ("Limited - FOTR").equals(summary.GameReplayInfo.format_name)) {
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
        tbr.put("1_97", 0.6977841195232499);
        tbr.put("1_231", 0.6821565308254964);
        tbr.put("1_153", 0.6277018315018316);
        tbr.put("1_100", 0.5679142857142857);
        tbr.put("1_178", 0.5483889833940867);
        tbr.put("1_296", 0.5028331391284083);
        tbr.put("1_170", 0.49544);
        tbr.put("2_108", 0.49245328621908135);
        tbr.put("3_38", 0.48413408825978355);
        tbr.put("2_80", 0.47947103448275863);
        tbr.put("1_150", 0.4672906532220563);
        tbr.put("2_84", 0.45716049661399555);
        tbr.put("1_299", 0.4546521984040056);
        tbr.put("1_89", 0.4512732974427994);
        tbr.put("1_183", 0.4483355555555556);
        tbr.put("3_13", 0.44607627530364374);
        tbr.put("1_94", 0.44423063365505666);
        tbr.put("1_234", 0.4409267630955299);
        tbr.put("1_112", 0.4406369630369631);
        tbr.put("1_252", 0.44044);
        tbr.put("1_50", 0.4400294267515924);
        tbr.put("2_32", 0.43686300518134713);
        tbr.put("1_90", 0.4354264596273293);
        tbr.put("1_81", 0.4345404081632653);
        tbr.put("3_68", 0.4304412927756654);
        tbr.put("1_51", 0.4304257133897773);
        tbr.put("2_93", 0.4299898818474759);
        tbr.put("2_67", 0.42928288288288297);
        tbr.put("2_85", 0.4272193501805055);
        tbr.put("1_308", 0.4266001593625498);
        tbr.put("2_105", 0.42416000000000004);
        tbr.put("1_312", 0.4239294117647059);
        tbr.put("1_127", 0.4225444171779142);
        tbr.put("3_60", 0.4220641305416298);
        tbr.put("3_64", 0.4208387096774194);
        tbr.put("2_10", 0.41956934691783326);
        tbr.put("2_46", 0.41953760869565215);
        tbr.put("3_66", 0.41941146067415735);
        tbr.put("1_228", 0.41816210526315784);
        tbr.put("1_158", 0.41663809523809525);
        tbr.put("1_121", 0.41596635950277483);
        tbr.put("1_69", 0.4150997183098592);
        tbr.put("1_13", 0.4145149379652605);
        tbr.put("1_133", 0.41450494159928125);
        tbr.put("1_230", 0.4133800000000001);
        tbr.put("1_2", 0.41302935569958066);
        tbr.put("1_154", 0.4127961437335671);
        tbr.put("1_313", 0.41264164383561647);
        tbr.put("3_63", 0.412406159489028);
        tbr.put("1_152", 0.41229308755760363);
        tbr.put("1_14", 0.411964347826087);
        tbr.put("1_262", 0.4107942986169913);
        tbr.put("3_65", 0.409819766536965);
        tbr.put("3_108", 0.40739907220173666);
        tbr.put("1_125", 0.4068585714285714);
        tbr.put("1_27", 0.40685641410352585);
        tbr.put("2_5", 0.40635082799022093);
        tbr.put("3_21", 0.40448019169329075);
        tbr.put("3_29", 0.4043785542168675);
        tbr.put("1_307", 0.40358535211267604);
        tbr.put("3_57", 0.4035673469387755);
        tbr.put("1_9", 0.4030994584036225);
        tbr.put("1_143", 0.40250648501362396);
        tbr.put("1_75", 0.40209814946619216);
        tbr.put("3_42", 0.40195337016574595);
        tbr.put("1_237", 0.4017200000000001);
        tbr.put("1_131", 0.4012321739130435);
        tbr.put("1_44", 0.40060441367373434);
        tbr.put("1_18", 0.4000285714285714);
        tbr.put("1_181", 0.3996780299521383);
        tbr.put("3_7", 0.39911879622545265);
        tbr.put("1_111", 0.39886);
        tbr.put("1_229", 0.3988429925187032);
        tbr.put("1_236", 0.3987278485370052);
        tbr.put("1_270", 0.39758042360682705);
        tbr.put("3_67", 0.39624156862745097);
        tbr.put("2_75", 0.3959128859060403);
        tbr.put("2_3", 0.39578902195608784);
        tbr.put("3_43", 0.3953002140352475);
        tbr.put("2_12", 0.39357039301310043);
        tbr.put("1_302", 0.3924381314878893);
        tbr.put("1_49", 0.39199017421602794);
        tbr.put("1_128", 0.39038318584070797);
        tbr.put("1_176", 0.38950842862316964);
        tbr.put("1_15", 0.38914427350427355);
        tbr.put("1_66", 0.38705652173913047);
        tbr.put("1_139", 0.3868404878048781);
        tbr.put("3_10", 0.3867765266882018);
        tbr.put("1_180", 0.3863351346639937);
        tbr.put("1_74", 0.3859809523809524);
        tbr.put("1_56", 0.3857268344938404);
        tbr.put("1_114", 0.38566000000000006);
        tbr.put("3_85", 0.38525098591549295);
        tbr.put("1_72", 0.3850091124260356);
        tbr.put("3_69", 0.3849246227587432);
        tbr.put("2_20", 0.3848494736842106);
        tbr.put("2_48", 0.384089552238806);
        tbr.put("1_147", 0.38367128712871296);
        tbr.put("3_41", 0.3833526829268293);
        tbr.put("3_58", 0.3829122807017544);
        tbr.put("1_40", 0.38141876288659793);
        tbr.put("3_80", 0.38103958254269454);
        tbr.put("2_52", 0.379382765598651);
        tbr.put("1_47", 0.3793769491525424);
        tbr.put("1_95", 0.3792906344410876);
        tbr.put("1_108", 0.37909023941068143);
        tbr.put("1_148", 0.3783354016620499);
        tbr.put("1_196", 0.3782432321854756);
        tbr.put("1_179", 0.3779739213806328);
        tbr.put("2_60", 0.3776315431967735);
        tbr.put("3_75", 0.3771824039653036);
        tbr.put("3_74", 0.377176148954304);
        tbr.put("1_187", 0.376915602013163);
        tbr.put("1_116", 0.3765142857142857);
        tbr.put("2_47", 0.3761561395720501);
        tbr.put("1_311", 0.37586748025843497);
        tbr.put("1_365", 0.3756848429009664);
        tbr.put("1_3", 0.374369889999281);
        tbr.put("2_102", 0.37337936507936503);
        tbr.put("1_286", 0.3732469490308686);
        tbr.put("1_12", 0.3729991561181434);
        tbr.put("2_51", 0.37283020667726546);
        tbr.put("1_186", 0.3716432876712329);
        tbr.put("1_317", 0.3707970695970696);
        tbr.put("1_96", 0.36960709677419357);
        tbr.put("1_191", 0.36929943230422274);
        tbr.put("1_57", 0.3689846582984659);
        tbr.put("1_289", 0.36891090909090907);
        tbr.put("2_50", 0.3668824713958811);
        tbr.put("2_49", 0.36682407643312104);
        tbr.put("1_298", 0.36668235294117646);
        tbr.put("1_156", 0.3657917322398315);
        tbr.put("2_44", 0.3654720640150623);
        tbr.put("1_45", 0.36517105263157895);
        tbr.put("1_34", 0.36327176470588235);
        tbr.put("1_80", 0.36235047619047617);
        tbr.put("2_98", 0.3612666666666667);
        tbr.put("1_204", 0.36102);
        tbr.put("1_314", 0.3607617391304348);
        tbr.put("1_33", 0.36058);
        tbr.put("1_240", 0.3605496551724138);
        tbr.put("1_285", 0.36006666666666665);
        tbr.put("2_22", 0.35979109057301295);
        tbr.put("3_1", 0.3579863157894737);
        tbr.put("1_92", 0.35789114149292683);
        tbr.put("1_19", 0.35785602722889287);
        tbr.put("1_21", 0.3573437229437229);
        tbr.put("1_162", 0.3567604519774011);
        tbr.put("1_165", 0.35604186046511627);
        tbr.put("1_261", 0.35586347009391994);
        tbr.put("3_12", 0.3557194508009153);
        tbr.put("1_117", 0.3549277244494636);
        tbr.put("3_71", 0.35486);
        tbr.put("1_16", 0.3541685714285714);
        tbr.put("2_101", 0.3529507165016989);
        tbr.put("1_173", 0.3526239344262296);
        tbr.put("2_26", 0.35254526678141135);
        tbr.put("3_106", 0.35248333333333326);
        tbr.put("1_30", 0.3522705058365759);
        tbr.put("2_6", 0.3519342156481121);
        tbr.put("1_11", 0.35157891816920944);
        tbr.put("3_62", 0.35106278407564606);
        tbr.put("1_120", 0.3505077108433735);
        tbr.put("1_260", 0.34982413087934566);
        tbr.put("2_37", 0.34967990048448344);
        tbr.put("3_56", 0.3493346653346653);
        tbr.put("1_166", 0.3491284210526316);
        tbr.put("3_5", 0.3489641577060932);
        tbr.put("2_61", 0.34840817333098467);
        tbr.put("1_303", 0.34779532794249773);
        tbr.put("2_112", 0.3475073684210527);
        tbr.put("1_175", 0.34748357976653693);
        tbr.put("1_145", 0.3472149890224389);
        tbr.put("3_102", 0.3466466666666666);
        tbr.put("3_3", 0.3465478260869565);
        tbr.put("1_264", 0.3457200947867299);
        tbr.put("1_102", 0.34549057750759876);
        tbr.put("1_195", 0.34536051282051283);
        tbr.put("1_256", 0.3448535483870968);
        tbr.put("1_36", 0.34474000000000005);
        tbr.put("1_267", 0.3445846153846154);
        tbr.put("1_246", 0.3445503867403315);
        tbr.put("1_310", 0.34423931034482763);
        tbr.put("2_109", 0.34423931034482763);
        tbr.put("1_309", 0.34375531135531134);
        tbr.put("3_27", 0.34364000000000006);
        tbr.put("3_2", 0.3429904761904761);
        tbr.put("1_138", 0.3419304971809328);
        tbr.put("1_142", 0.34119552414605414);
        tbr.put("2_114", 0.34105690276110445);
        tbr.put("1_250", 0.34036391304347824);
        tbr.put("2_4", 0.33961174524400334);
        tbr.put("3_61", 0.33904818481848187);
        tbr.put("2_103", 0.3387425149700599);
        tbr.put("2_121", 0.33837615520744574);
        tbr.put("3_34", 0.3381514498141264);
        tbr.put("2_17", 0.3379);
        tbr.put("1_301", 0.3379);
        tbr.put("1_364", 0.337641699543042);
        tbr.put("1_26", 0.33666833578792343);
        tbr.put("1_227", 0.3362);
        tbr.put("2_96", 0.33599773371104813);
        tbr.put("1_244", 0.33566500000000005);
        tbr.put("1_290", 0.3349337662337662);
        tbr.put("1_82", 0.3345619047619047);
        tbr.put("2_31", 0.3340666666666666);
        tbr.put("1_5", 0.33366784599375643);
        tbr.put("3_50", 0.33297932203389835);
        tbr.put("3_121", 0.3325518187371578);
        tbr.put("1_190", 0.3312721739130435);
        tbr.put("1_48", 0.3308593272171254);
        tbr.put("3_8", 0.33084206896551727);
        tbr.put("1_123", 0.3307542857142857);
        tbr.put("2_38", 0.33066000000000006);
        tbr.put("1_295", 0.3302968641114983);
        tbr.put("3_59", 0.3302471677093153);
        tbr.put("1_377", 0.33022);
        tbr.put("3_17", 0.33020264984227127);
        tbr.put("3_99", 0.3292604819277109);
        tbr.put("2_110", 0.32884240871236387);
        tbr.put("1_42", 0.3284462524023062);
        tbr.put("1_1", 0.3282773584905661);
        tbr.put("2_122", 0.3275256280052128);
        tbr.put("3_19", 0.3267275);
        tbr.put("1_78", 0.3264552166934189);
        tbr.put("1_199", 0.3256115789473684);
        tbr.put("1_146", 0.3248116350341416);
        tbr.put("2_82", 0.32395079594790155);
        tbr.put("2_18", 0.3231251992642551);
        tbr.put("3_35", 0.3227965899403239);
        tbr.put("3_25", 0.3220939393939394);
        tbr.put("1_209", 0.3214739336492891);
        tbr.put("1_280", 0.32128599882835385);
        tbr.put("1_219", 0.3212215053763441);
        tbr.put("1_58", 0.321026168224299);
        tbr.put("1_159", 0.3210089238845144);
        tbr.put("1_151", 0.31976092715231785);
        tbr.put("2_74", 0.3194956626506024);
        tbr.put("1_259", 0.3189057142857143);
        tbr.put("3_82", 0.3186371747211896);
        tbr.put("1_212", 0.3179);
        tbr.put("1_10", 0.31784705882352937);
        tbr.put("1_318", 0.31748);
        tbr.put("1_281", 0.3160423198787712);
        tbr.put("3_39", 0.3158000000000001);
        tbr.put("2_33", 0.315552380952381);
        tbr.put("1_291", 0.3155188235294118);
        tbr.put("1_46", 0.31488571428571427);
        tbr.put("1_208", 0.31482);
        tbr.put("2_94", 0.31449000000000005);
        tbr.put("1_70", 0.3141692410565028);
        tbr.put("3_20", 0.31408682170542634);
        tbr.put("1_155", 0.3137357142857143);
        tbr.put("2_2", 0.31353743683968766);
        tbr.put("2_43", 0.31306);
        tbr.put("1_17", 0.3129840375586854);
        tbr.put("1_118", 0.3121443243243243);
        tbr.put("1_98", 0.311565690376569);
        tbr.put("1_213", 0.3115646017699115);
        tbr.put("1_122", 0.31151625615763545);
        tbr.put("1_164", 0.31083904235727444);
        tbr.put("2_42", 0.30967027914614126);
        tbr.put("2_89", 0.3086398200224972);
        tbr.put("1_157", 0.308475664034458);
        tbr.put("1_84", 0.30827069486404834);
        tbr.put("1_62", 0.30824);
        tbr.put("1_188", 0.30823962703962704);
        tbr.put("1_205", 0.308);
        tbr.put("1_103", 0.30795817286162114);
        tbr.put("1_247", 0.30770000000000003);
        tbr.put("3_122", 0.30740045032367014);
        tbr.put("2_7", 0.3072709803921569);
        tbr.put("3_40", 0.3067565217391305);
        tbr.put("1_59", 0.30674285714285715);
        tbr.put("2_15", 0.30638000000000004);
        tbr.put("3_96", 0.3062678328474247);
        tbr.put("1_185", 0.3055411036616813);
        tbr.put("3_77", 0.30540150943396227);
        tbr.put("2_106", 0.30519176319176317);
        tbr.put("1_71", 0.30514);
        tbr.put("2_90", 0.3050600908788651);
        tbr.put("1_233", 0.30505281385281385);
        tbr.put("1_161", 0.30453846153846154);
        tbr.put("2_76", 0.3044918918918919);
        tbr.put("1_7", 0.3044491862567812);
        tbr.put("2_14", 0.30386234199856904);
        tbr.put("1_294", 0.30178539107950875);
        tbr.put("2_100", 0.30141999999999997);
        tbr.put("1_220", 0.3012666666666667);
        tbr.put("1_274", 0.3012666666666667);
        tbr.put("1_28", 0.3011);
        tbr.put("1_258", 0.3001260306242638);
        tbr.put("1_273", 0.30005714285714286);
        tbr.put("1_52", 0.3000285714285714);
        tbr.put("2_72", 0.2997032967032967);
        tbr.put("2_113", 0.2992);
        tbr.put("1_268", 0.29913926119646944);
        tbr.put("2_8", 0.2988485875706215);
        tbr.put("2_13", 0.2974077922077922);
        tbr.put("3_91", 0.29674901408450705);
        tbr.put("1_288", 0.29669323943661974);
        tbr.put("1_136", 0.29639357021996615);
        tbr.put("1_67", 0.29542990033222594);
        tbr.put("1_160", 0.2947986710963455);
        tbr.put("2_29", 0.2947362821279139);
        tbr.put("1_113", 0.29472880258899675);
        tbr.put("3_31", 0.29446285714285714);
        tbr.put("1_106", 0.29408872180451123);
        tbr.put("1_197", 0.2939142857142857);
        tbr.put("2_83", 0.2937495112908662);
        tbr.put("2_39", 0.293642962962963);
        tbr.put("3_114", 0.29348374384236453);
        tbr.put("1_184", 0.2925859912940245);
        tbr.put("1_306", 0.2925020408163265);
        tbr.put("2_71", 0.2915720430107527);
        tbr.put("2_104", 0.2905262580491295);
        tbr.put("2_53", 0.29051000000000005);
        tbr.put("1_76", 0.28851064797271697);
        tbr.put("1_167", 0.2870371428571429);
        tbr.put("1_193", 0.2863980037429819);
        tbr.put("1_79", 0.28615714285714283);
        tbr.put("3_36", 0.286);
        tbr.put("2_62", 0.2858835164835165);
        tbr.put("2_19", 0.28534000000000004);
        tbr.put("1_86", 0.2851454049135578);
        tbr.put("2_36", 0.28439714285714285);
        tbr.put("1_257", 0.28403236245954694);
        tbr.put("3_49", 0.2836467559837423);
        tbr.put("1_6", 0.28364130931235726);
        tbr.put("1_243", 0.28351714285714286);
        tbr.put("1_41", 0.28346014669926645);
        tbr.put("1_53", 0.2827456953642384);
        tbr.put("1_31", 0.28136850393700785);
        tbr.put("1_232", 0.2799452736318408);
        tbr.put("2_59", 0.2798583333333333);
        tbr.put("1_37", 0.27964764166949435);
        tbr.put("3_111", 0.27916076759061836);
        tbr.put("1_271", 0.2787041148632389);
        tbr.put("2_111", 0.2777897435897436);
        tbr.put("1_304", 0.2776585850711362);
        tbr.put("3_70", 0.2772384615384615);
        tbr.put("1_202", 0.2766666666666666);
        tbr.put("1_200", 0.27662344827586205);
        tbr.put("1_163", 0.27613844660194176);
        tbr.put("1_109", 0.2755558685446009);
        tbr.put("1_85", 0.2752659340659341);
        tbr.put("1_101", 0.2747055016937273);
        tbr.put("2_64", 0.27397630121816163);
        tbr.put("1_68", 0.27368021518230723);
        tbr.put("1_263", 0.27274851063829786);
        tbr.put("1_223", 0.27269256594724217);
        tbr.put("3_73", 0.271694498381877);
        tbr.put("1_316", 0.2715845730027548);
        tbr.put("1_207", 0.2715278538812785);
        tbr.put("3_55", 0.27132973342447025);
        tbr.put("1_210", 0.27104);
        tbr.put("1_235", 0.27102150537634406);
        tbr.put("1_272", 0.2709027067669173);
        tbr.put("1_126", 0.26956936936936937);
        tbr.put("1_110", 0.2694457831325301);
        tbr.put("1_224", 0.2692041379310345);
        tbr.put("1_135", 0.2687298245614035);
        tbr.put("1_194", 0.2686);
        tbr.put("2_68", 0.26822018348623855);
        tbr.put("1_177", 0.2682130732735899);
        tbr.put("1_65", 0.2680597701149425);
        tbr.put("3_93", 0.26708);
        tbr.put("1_211", 0.26693333333333336);
        tbr.put("1_218", 0.2647784200385356);
        tbr.put("1_192", 0.26415655577299413);
        tbr.put("2_24", 0.26324285714285717);
        tbr.put("2_11", 0.2628057142857143);
        tbr.put("1_88", 0.2628057142857143);
        tbr.put("1_174", 0.2626728370221328);
        tbr.put("3_52", 0.2622983673469388);
        tbr.put("1_217", 0.2613729411764706);
        tbr.put("1_283", 0.2610738493010569);
        tbr.put("3_26", 0.2608615384615385);
        tbr.put("1_305", 0.2605449735449735);
        tbr.put("1_63", 0.2599181102362205);
        tbr.put("2_1", 0.25962972972972975);
        tbr.put("1_266", 0.2579721824349792);
        tbr.put("1_132", 0.25793565217391307);
        tbr.put("2_45", 0.25714615384615386);
        tbr.put("2_97", 0.2569285714285714);
        tbr.put("1_83", 0.2568422535211268);
        tbr.put("2_69", 0.2562129032258064);
        tbr.put("1_24", 0.25579830009807125);
        tbr.put("3_30", 0.255502656137832);
        tbr.put("1_278", 0.2548007279344859);
        tbr.put("1_238", 0.2547794871794872);
        tbr.put("1_149", 0.25477593984962404);
        tbr.put("1_198", 0.2538666666666667);
        tbr.put("1_129", 0.2534978947368421);
        tbr.put("3_107", 0.253298245614035);
        tbr.put("2_41", 0.2530857808857809);
        tbr.put("2_16", 0.25295337262606415);
        tbr.put("3_94", 0.2520600305758451);
        tbr.put("1_32", 0.2513088052612349);
        tbr.put("2_9", 0.2506758620689655);
        tbr.put("3_83", 0.2504);
        tbr.put("2_88", 0.2501607535321821);
        tbr.put("3_28", 0.24988553515450068);
        tbr.put("3_100", 0.2498518337408313);
        tbr.put("1_8", 0.24903665184612409);
        tbr.put("2_57", 0.24838);
        tbr.put("1_38", 0.2481117073170732);
        tbr.put("3_79", 0.24705771144278607);
        tbr.put("1_315", 0.247);
        tbr.put("3_37", 0.2465142857142857);
        tbr.put("1_297", 0.24644090441932168);
        tbr.put("1_226", 0.24593333333333334);
        tbr.put("1_73", 0.2445078651685393);
        tbr.put("1_23", 0.24286);
        tbr.put("3_104", 0.2428152941176471);
        tbr.put("1_4", 0.24235441412520062);
        tbr.put("3_14", 0.24210886699507386);
        tbr.put("1_201", 0.2417597474348855);
        tbr.put("3_18", 0.2414968591691996);
        tbr.put("3_97", 0.2413992673992674);
        tbr.put("2_28", 0.24103058103975536);
        tbr.put("2_55", 0.24027686116700198);
        tbr.put("2_35", 0.23595403726708075);
        tbr.put("1_119", 0.23481952983725135);
        tbr.put("1_107", 0.23407301587301585);
        tbr.put("1_255", 0.2339164021164021);
        tbr.put("1_279", 0.23385714285714287);
        tbr.put("3_53", 0.23354509803921564);
        tbr.put("1_182", 0.23209804659919983);
        tbr.put("1_124", 0.2311257142857143);
        tbr.put("1_248", 0.23112087912087909);
        tbr.put("1_29", 0.23002702702702704);
        tbr.put("3_98", 0.22911255660765745);
        tbr.put("2_63", 0.22891475282210974);
        tbr.put("1_61", 0.22511428571428568);
        tbr.put("1_141", 0.22493414387031407);
        tbr.put("3_51", 0.2244151260504202);
        tbr.put("3_101", 0.22401158301158303);
        tbr.put("1_144", 0.22399867109634553);
        tbr.put("2_79", 0.22328888888888887);
        tbr.put("1_221", 0.22197999999999998);
        tbr.put("2_70", 0.2217859649122807);
        tbr.put("3_16", 0.22170977443609022);
        tbr.put("1_140", 0.22154000000000001);
        tbr.put("1_251", 0.22119999999999998);
        tbr.put("2_73", 0.2211);
        tbr.put("3_23", 0.22066);
        tbr.put("2_66", 0.22066);
        tbr.put("2_65", 0.22045714285714285);
        tbr.put("1_277", 0.2203857142857143);
        tbr.put("3_54", 0.22022000000000003);
        tbr.put("1_55", 0.22022);
        tbr.put("1_378", 0.22022);
        tbr.put("2_54", 0.2198986301369863);
        tbr.put("1_87", 0.21956);
        tbr.put("2_107", 0.21895460992907803);
        tbr.put("1_282", 0.2183694117647059);
        tbr.put("2_99", 0.21825714285714284);
        tbr.put("1_275", 0.21813333333333332);
        tbr.put("1_293", 0.21669230769230768);
        tbr.put("3_95", 0.2161674550614948);
        tbr.put("3_72", 0.2141698924731183);
        tbr.put("1_168", 0.2141155119558553);
        tbr.put("2_95", 0.21401446654611211);
        tbr.put("3_89", 0.21262898550724638);
        tbr.put("3_22", 0.2093012987012987);
        tbr.put("1_115", 0.20879692307692313);
        tbr.put("1_269", 0.2086018379281537);
        tbr.put("3_76", 0.20682448979591833);
        tbr.put("1_245", 0.20681692307692312);
        tbr.put("2_78", 0.20613333333333334);
        tbr.put("2_92", 0.20475151515151516);
        tbr.put("2_56", 0.20299999999999999);
        tbr.put("3_78", 0.20263896103896106);
        tbr.put("3_44", 0.20240000000000002);
        tbr.put("3_24", 0.20200567375886522);
        tbr.put("1_171", 0.2007142857142857);
        tbr.put("1_203", 0.20046666666666665);
        tbr.put("1_292", 0.20046666666666665);
        tbr.put("2_58", 0.20037142857142856);
        tbr.put("3_6", 0.20012527472527472);
        tbr.put("1_241", 0.20006666666666664);
        tbr.put("1_375", 0.20005454545454543);
        tbr.put("3_86", 0.1986625850340136);
        tbr.put("3_11", 0.19710036630036631);
        tbr.put("3_45", 0.1961945945945946);
        tbr.put("1_253", 0.19602000000000006);
        tbr.put("1_172", 0.19495142857142855);
        tbr.put("1_239", 0.19306666666666666);
        tbr.put("3_48", 0.19173766233766232);
        tbr.put("3_90", 0.19138770343580472);
        tbr.put("3_15", 0.19055142857142857);
        tbr.put("2_81", 0.18816666666666668);
        tbr.put("1_134", 0.18587252747252747);
        tbr.put("3_33", 0.1821809523809524);
        tbr.put("3_109", 0.18137142857142857);
        tbr.put("1_64", 0.17436190476190475);
        tbr.put("1_91", 0.1720285714285714);
        tbr.put("1_242", 0.17169523809523807);
        tbr.put("2_124", 0.1715428571428571);
        tbr.put("1_104", 0.17127226890756303);
        tbr.put("3_112", 0.16856236626809315);
        tbr.put("1_105", 0.16785022831050228);
        tbr.put("1_43", 0.1670589861751152);
        tbr.put("1_137", 0.16676);
        tbr.put("1_93", 0.16544000000000003);
        tbr.put("2_86", 0.16544000000000003);
        tbr.put("2_40", 0.1652077922077922);
        tbr.put("2_87", 0.16101212121212122);
        tbr.put("3_88", 0.16066666666666668);
        tbr.put("1_284", 0.15998631578947367);
        tbr.put("2_34", 0.15758840579710143);
        tbr.put("1_300", 0.15697888198757762);
        tbr.put("1_214", 0.15352000000000002);
        tbr.put("2_21", 0.15134285714285714);
        tbr.put("1_216", 0.15106666666666668);
        tbr.put("1_25", 0.14936488549618318);
        tbr.put("2_30", 0.14713470790378005);
        tbr.put("3_84", 0.1444496894409938);
        tbr.put("1_60", 0.14067619047619045);
        tbr.put("2_23", 0.13682672811059907);
        tbr.put("2_27", 0.13582048780487804);
        tbr.put("1_39", 0.13575102040816328);
        tbr.put("2_25", 0.1353);
        tbr.put("1_249", 0.13373333333333334);
        tbr.put("1_35", 0.13266);
        tbr.put("3_113", 0.13266);
        tbr.put("1_169", 0.13266);
        tbr.put("2_91", 0.12948571428571426);
        tbr.put("3_47", 0.12923809523809524);
        tbr.put("3_92", 0.12758231292517005);
        tbr.put("3_46", 0.1252627450980392);
        tbr.put("3_87", 0.12464438280166434);
        tbr.put("3_32", 0.12392409381663114);
        tbr.put("1_77", 0.12046666666666667);
        tbr.put("1_130", 0.12046666666666667);
        tbr.put("1_370", 0.1202);
        tbr.put("3_81", 0.11528);
        tbr.put("3_110", 0.11308);
        tbr.put("1_189", 0.11087999999999999);
        tbr.put("1_54", 0.10026666666666666);
        tbr.put("1_20", 0.10025714285714285);
        tbr.put("1_99", 0.08382);
        tbr.put("1_287", 0.07537142857142858);
        tbr.put("1_276", 0.07299368421052631);
        tbr.put("3_4", 0.04688717948717949);
        return tbr;
    }
}
