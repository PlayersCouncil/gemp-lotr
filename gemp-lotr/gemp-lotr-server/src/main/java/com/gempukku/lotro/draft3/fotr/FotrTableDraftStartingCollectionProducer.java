package com.gempukku.lotro.draft3.fotr;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.draft3.StartingCollectionProducer;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.google.common.collect.Iterables;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class FotrTableDraftStartingCollectionProducer implements StartingCollectionProducer {
    private static final int HIGH_ENOUGH_PRIME_NUMBER = 8963;


    private static final List<String> CORE_CARDS = List.of("1_2", "1_290", "Fellowship Site Pack");
    private static final int FP_WHEEL_LENGTH = 7;
    private static final List<String> FP1_CARDS = List.of("1_296", "2_110", "1_12", "1_365", "1_112", "1_365", "1_311", "1_317",
            "2_104", "3_122", "1_364", "3_35", "1_78", "1_364", "2_114", "1_299", "1_303", "3_121", "1_365", "1_112",
            "1_365", "2_110", "1_298", "2_104", "1_51", "1_364", "3_35", "1_76", "1_364", "2_114");
    private static final List<String> FP2_CARDS = List.of("1_26", "1_7", "1_9", "1_11", "1_5", "2_121", "1_97", "2_37", "1_97",
            "1_51", "1_37", "1_48", "1_32", "1_51", "1_97", "2_121", "1_5", "2_6", "1_9", "1_7", "1_6", "2_121", "1_97",
            "3_7", "1_37", "1_48", "2_18", "3_7", "1_97", "2_121");
    private static final int SHADOW_WHEEL_LENGTH = 14;
    private static final List<String> SHADOW_CARDS = List.of("3_96", "1_270", "2_89", "1_262", "1_262", "2_89", "1_270", "3_96",
            "1_231", "2_61", "1_179", "2_63", "1_184", "2_62", "2_62", "1_184", "2_63", "1_179", "2_61", "1_234", "1_158",
            "1_152", "1_154", "1_151", "1_145", "1_151", "1_154", "1_152", "1_158", "1_231", "3_100", "1_267", "1_270",
            "1_271", "1_271", "1_270", "1_267", "3_100", "1_234", "1_177", "2_67", "1_178", "1_176", "2_60", "2_60", "1_176",
            "1_178", "2_67", "1_177", "1_231", "3_57", "3_58", "3_59", "3_62", "3_69", "3_62", "3_59", "3_58", "3_57", "1_234");
    private static final String RARE_FOTR_FILTER = "set:1 rarity:R";
    private final List<String> possibleRareCards;

    public FotrTableDraftStartingCollectionProducer(CollectionsManager collectionsManager, LotroCardBlueprintLibrary cardLibrary,
                                                    LotroFormatLibrary formatLibrary){
        possibleRareCards = new SortAndFilterCards().process(RARE_FOTR_FILTER, collectionsManager.getCompleteCardCollection().getAll(), cardLibrary, formatLibrary).stream().map(CardCollection.Item::getBlueprintId).collect(Collectors.toList());
        possibleRareCards.removeIf(cardId -> !cardId.startsWith("1_"));
    }

    @Override
    public MutableCardCollection getStartingCardCollection(String uniqueEventName, String playerName) {
       long seed = uniqueEventName.hashCode() + (long) playerName.hashCode() * HIGH_ENOUGH_PRIME_NUMBER;
        Random rnd = new Random(seed);

        final DefaultCardCollection startingCollection = new DefaultCardCollection();

        // Add Frodo SoD, the Ruling Ring, and all fellowship site cards
        for (String coreCard : CORE_CARDS) {
            startingCollection.addItem(coreCard, 1);
        }
        // Add 1 random FotR rare card
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
