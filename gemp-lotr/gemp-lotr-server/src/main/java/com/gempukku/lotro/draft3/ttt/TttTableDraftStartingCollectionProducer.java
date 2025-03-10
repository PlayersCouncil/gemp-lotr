package com.gempukku.lotro.draft3.ttt;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.draft3.StartingCollectionProducer;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.google.common.collect.Iterables;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TttTableDraftStartingCollectionProducer implements StartingCollectionProducer {
    private static final int HIGH_ENOUGH_PRIME_NUMBER = 8963;


    private static final List<String> CORE_CARDS = List.of("4_2", "4_302", "Towers Site Pack");
    private static final int FP_WHEEL_LENGTH = 7;
    private static final List<String> FP1_CARDS = List.of("4_90", "4_105", "4_97", "4_98", "4_97", "4_105", "4_90",
            "4_109", "4_115", "4_112", "4_115", "4_109", "4_71", "4_83", "4_76", "4_78", "4_83", "4_71", "4_74", "4_87",
            "4_70", "4_87", "4_74", "4_117", "4_135", "4_112", "4_128", "4_130", "4_135", "4_117");
    private static final List<String> FP2_CARDS = List.of("4_266", "4_278", "4_277", "4_273", "4_273", "4_281", "4_283",
            "4_266", "4_270", "4_287", "4_265", "4_297", "4_265", "4_287", "4_270", "4_49", "4_44", "4_42", "4_58",
            "4_42", "4_44", "4_49", "4_310", "4_322", "4_308", "4_314", "4_310", "4_308", "4_322", "4_314");
    private static final int SHADOW_WHEEL_LENGTH = 14;
    private static final List<String> SHADOW_CARDS = List.of("4_248", "4_221", "4_222", "4_227", "4_226", "4_224",
            "4_228", "4_258", "4_228", "4_224", "4_226", "4_227", "4_222", "4_221", "4_248", "4_165", "4_191", "4_184",
            "4_206", "4_204", "4_180", "4_198", "4_137", "4_198", "4_180", "4_204", "4_206", "4_184", "4_191", "4_165",
            "4_4", "4_17", "4_16", "4_25", "4_10", "4_14", "4_12", "4_21", "4_12", "4_14", "4_10", "4_25", "4_16",
            "4_17", "4_4", "4_207", "4_181", "4_193", "4_189", "4_187", "4_190", "4_178", "4_153", "4_178", "4_190",
            "4_187", "4_189", "4_193", "4_181", "4_207");
    private static final String RARE_TTT_FILTER = "set:4 rarity:R";
    private final List<String> possibleRareCards;

    public TttTableDraftStartingCollectionProducer(CollectionsManager collectionsManager, LotroCardBlueprintLibrary cardLibrary,
                                                   LotroFormatLibrary formatLibrary){
        possibleRareCards = new SortAndFilterCards().process(RARE_TTT_FILTER, collectionsManager.getCompleteCardCollection().getAll(), cardLibrary, formatLibrary).stream().map(CardCollection.Item::getBlueprintId).collect(Collectors.toList());
        possibleRareCards.removeIf(cardId -> !cardId.startsWith("4_"));
    }

    @Override
    public MutableCardCollection getStartingCardCollection(String uniqueEventName, String playerName) {
       long seed = uniqueEventName.hashCode() + (long) playerName.hashCode() * HIGH_ENOUGH_PRIME_NUMBER;
        Random rnd = new Random(seed);

        final DefaultCardCollection startingCollection = new DefaultCardCollection();

        // Add Frodo, the Ruling Ring, and all ttt site cards
        for (String coreCard : CORE_CARDS) {
            startingCollection.addItem(coreCard, 1);
        }
        // Add 1 random TTT rare card
        startingCollection.addItem(possibleRareCards.get(rnd.nextInt(possibleRareCards.size())), 1);
        // 7 cards from FP wheel 1
        int fp1Start = rnd.nextInt(FP1_CARDS.size());
        for (String fp1Card : getCyclingIterable(FP1_CARDS, fp1Start, FP_WHEEL_LENGTH)) {
            startingCollection.addItem(fp1Card, 1);
        }
        // 7 cards from FP wheel 2
        int fp2Start = rnd.nextInt(FP2_CARDS.size());
        for (String fp2Card : getCyclingIterable(FP2_CARDS, fp2Start, FP_WHEEL_LENGTH)) {
            startingCollection.addItem(fp2Card, 1);
        }
        // 14 cards from shadow wheel
        int shadowStart = rnd.nextInt(SHADOW_CARDS.size());
        for (String shadowCard : getCyclingIterable(SHADOW_CARDS, shadowStart, SHADOW_WHEEL_LENGTH)) {
            startingCollection.addItem(shadowCard, 1);
        }

        return startingCollection;
    }

    private static Iterable<String> getCyclingIterable(List<String> list, int start, int length) {
        return Iterables.limit(Iterables.skip(Iterables.cycle(list), start), length);
    }
}
