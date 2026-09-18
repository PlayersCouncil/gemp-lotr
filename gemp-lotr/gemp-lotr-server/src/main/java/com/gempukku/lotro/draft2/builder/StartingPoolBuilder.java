package com.gempukku.lotro.draft2.builder;

import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultCardCollection;
import com.google.common.collect.Iterables;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class StartingPoolBuilder {
    public static CardCollectionProducer buildCardCollectionProducer(JSONObject startingPool) {
        String cardCollectionProducerType = (String) startingPool.get("type");
        if (cardCollectionProducerType.equals("randomCardPool")) {
            return buildRandomCardPool((JSONObject) startingPool.get("data"));
        } else if (cardCollectionProducerType.equals("boosterDraftRun")) {
            return buildBoosterDraftRun((JSONObject) startingPool.get("data"));
        }
        throw new RuntimeException("Unknown cardCollectionProducer type: " + cardCollectionProducerType);
    }

    private static CardCollectionProducer buildRandomCardPool(JSONObject randomCardPool) {
        JSONArray cardPools = (JSONArray) randomCardPool.get("randomResult");

        final List<CardCollection> cardCollections = new ArrayList<>();
        for (JSONArray cards : (Iterable<JSONArray>) cardPools) {
            DefaultCardCollection cardCollection = new DefaultCardCollection();
            for (String card : (Iterable<String>) cards) {
                cardCollection.addItem(card, 1);
            }
            cardCollections.add(cardCollection);
        }

        return seed -> {
            Random rnd = new Random(seed);
            float thisFixesARandomnessBug = rnd.nextFloat();
            return cardCollections.get(rnd.nextInt(cardCollections.size()));
        };
    }

    private static CardCollectionProducer buildBoosterDraftRun(JSONObject boosterDraftRun) {
        final int runLength = ((Number) boosterDraftRun.get("runLength")).intValue();
        final JSONArray coreCards = (JSONArray) boosterDraftRun.get("coreCards");
        final JSONArray freePeoplesRuns = (JSONArray) boosterDraftRun.get("freePeoplesRuns");
        final JSONArray shadowRuns = (JSONArray) boosterDraftRun.get("shadowRuns");
        final int fpRunLength = runLength / freePeoplesRuns.size();
        final int shadowRunLength = runLength / shadowRuns.size();

        return seed -> {
            Random rnd = new Random(seed);

            final DefaultCardCollection startingCollection = new DefaultCardCollection();

            for (String card : (List<String>) coreCards)
                startingCollection.addItem(card, 1);

            for (Object run : freePeoplesRuns) {
                List<String> freePeoplesRun = (List<String>) run;

                int freePeopleLength = freePeoplesRun.size();
                int freePeopleStart = rnd.nextInt(freePeopleLength);

                Iterable<String> freePeopleIterable = getCyclingIterable(freePeoplesRun, freePeopleStart, fpRunLength);
                for (String card : freePeopleIterable)
                    startingCollection.addItem(card, 1);
            }

            for (Object run : shadowRuns) {
                List<String> shadowRun = (List<String>) run;

                int shadowLength = shadowRun.size();
                int shadowStart = rnd.nextInt(shadowLength);

                Iterable<String> shadowIterable = getCyclingIterable(shadowRun, shadowStart, shadowRunLength);
                for (String card : shadowIterable)
                    startingCollection.addItem(card, 1);
            }

            return startingCollection;
        };
    }

    private static Iterable<String> getCyclingIterable(List<String> list, int start, int length) {
        return Iterables.limit(Iterables.skip(Iterables.cycle(list), start), length);
    }
}
