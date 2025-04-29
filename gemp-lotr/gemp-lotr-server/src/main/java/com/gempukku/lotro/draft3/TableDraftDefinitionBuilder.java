package com.gempukku.lotro.draft3;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft3.timer.DraftTimer;
import com.gempukku.lotro.game.DefaultCardCollection;
import com.gempukku.lotro.game.MutableCardCollection;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TableDraftDefinitionBuilder {
    private static final int HIGH_ENOUGH_PRIME_NUMBER = 8963;

    public static TableDraftDefinition build(File jsonFile) {
        try {
            final InputStreamReader reader = new InputStreamReader(new FileInputStream(jsonFile), StandardCharsets.UTF_8);
            try {
                JSONParser parser = new JSONParser();
                JSONObject object = (JSONObject) parser.parse(reader);
                return build(object);
            } catch (ParseException e) {
                throw new RuntimeException("Problem loading solo draft " + jsonFile, e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Problem loading solo draft " + jsonFile, e);
        }
    }

    public static TableDraftDefinition build(JSONObject definition) {
        String name = (String) definition.get("name");
        String code = (String) definition.get("code");
        String format = (String) definition.get("format");
        String timer = (String) definition.get("timer");

        int maxPlayers = ((Number) definition.get("max-players")).intValue();

        StartingCollectionProducer startingCollectionProducer = buildStartingCollectionProducer((JSONArray) definition.get("starting-collection"));
        // test if booster producer can be made from this json
        BoosterProducer testBoosterProducer = buildBoosterProducer((JSONArray) definition.get("card-sets"), (JSONArray) definition.get("boosters"), (JSONArray) definition.get("pools"));

        Map<String, Double> cardValues = buildCardValues((JSONArray) definition.get("card-values"));


        return new TableDraftDefinition() {
            @Override
            public TableDraft getTableDraft(CollectionsManager collectionsManager, CollectionType collectionType, DraftTimer draftTimer) {
                BoosterProducer boosterProducer = buildBoosterProducer((JSONArray) definition.get("card-sets"), (JSONArray) definition.get("boosters"), (JSONArray) definition.get("pools"));
                return new TableDraftClassic(collectionsManager, collectionType, startingCollectionProducer, boosterProducer, maxPlayers, boosterProducer.getMaxRound(), draftTimer, cardValues);
            }

            @Override
            public int getMaxPlayers() {
                return maxPlayers;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getCode() {
                return code;
            }

            @Override
            public String getFormat() {
                return format;
            }

            @Override
            public DraftTimer.Type getRecommendedTimer() {
                if (timer == null) {
                    return DraftTimer.Type.CLASSIC;
                } else {
                    return DraftTimer.getTypeFromString(timer);
                }
            }
        };
    }

    private static Map<String, Double> buildCardValues(JSONArray jsonArray) {
        Map<String, Double> tbr = new HashMap<>();

        for (JSONObject cardValuePair : (Iterable<JSONObject>) jsonArray) {
            String name = (String) cardValuePair.get("card");
            double value = ((Number) cardValuePair.get("value")).doubleValue();
            tbr.put(name, value);
        }

        return tbr;
    }

    private static BoosterProducer buildBoosterProducer(JSONArray cardSets, JSONArray boosters, JSONArray pools) {
        Map<String, List<String>> cardPools = new HashMap<>();
        Map<String, List<String>> setPools = new HashMap<>();
        Map<String, List<String>> draftPools = new HashMap<>();

        for (JSONObject cardSet : (Iterable<JSONObject>) cardSets) {
            String name = (String) cardSet.get("set-name");
            if (cardSet.containsKey("card-set")) {
                List<String> cards = (List<String>) cardSet.get("card-set");
                cardPools.put(name, cards);
            } else if (cardSet.containsKey("set-set") && cardSet.containsKey("choose")) {
                List<String> setNames = (List<String>) cardSet.get("set-set");
                Collections.shuffle(setNames);
                int howMany = ((Number) cardSet.get("choose")).intValue();
                List<String> chosenSetNames = new ArrayList<>();
                if (howMany <= setNames.size()) {
                    for (int i = 0; i < howMany; i++) {
                        chosenSetNames.add(setNames.get(i));
                    }
                    setPools.put(name, chosenSetNames);
                } else {
                    throw new IllegalArgumentException("Invalid JSON content");
                }
            } else {
                throw new IllegalArgumentException("Invalid JSON content");
            }
        }

        if (pools != null) {
            for (JSONObject cardSet : (Iterable<JSONObject>) pools) {
                String name = (String) cardSet.get("pool-name");
                draftPools.put(name, new ArrayList<>());
                List<String> sets = (List<String>) cardSet.get("set-set");
                for (String set : sets) {
                    addSetToPool(cardPools, setPools, draftPools, name, set);
                }
            }
        }

        Map<Integer, List<Pair<String, Integer>>> boosterPlan = new HashMap<>();
        for (JSONObject booster : (Iterable<JSONObject>) boosters) {
            JSONArray cards = (JSONArray) booster.get("cards");
            JSONArray rounds = (JSONArray) booster.get("rounds");
            for (Object round : rounds) {
                List<Pair<String, Integer>> cardsList = new ArrayList<>();

                for (JSONObject countSetPair : (Iterable<JSONObject>) cards) {
                    String name = (String) countSetPair.get("set-name");
                    int count = ((Number) countSetPair.get("count")).intValue();
                    cardsList.add(new Pair<>() {
                        @Override
                        public String getLeft() {
                            return name;
                        }

                        @Override
                        public Integer getRight() {
                            return count;
                        }

                        @Override
                        public Integer setValue(Integer value) {
                            return null;
                        }
                    });
                }
                boosterPlan.put(((Number) round).intValue(), cardsList);

            }
        }

        return new BoosterProducer() {
            Lock poolLock = new ReentrantLock();

            @Override
            public Booster getBooster(int round) {
                List<String> pickedCards = new ArrayList<>();
                boosterPlan.get(round).forEach(pair -> {
                    if (draftPools.get(pair.getLeft()) == null) {
                        // opening boosters
                        pickedCards.addAll(pickRandom(cardPools.get(pair.getLeft()), pair.getRight(), false));
                    } else {
                        // opening cards from draft pool
                        poolLock.lock();
                        try {
                            pickedCards.addAll(pickRandom(draftPools.get(pair.getLeft()), pair.getRight(), true));
                        } finally {
                            poolLock.unlock();
                        }
                    }
                });
                return new Booster(pickedCards);
            }

            @Override
            public int getMaxRound() {
                return boosterPlan.keySet().stream().max(Comparator.naturalOrder()).orElse(-1);
            }

            private List<String> pickRandom(List<String> source, int count, boolean remove) {
                if (source.isEmpty() || count <= 0) return Collections.emptyList();

                int size = source.size();
                count = Math.min(count, size);

                Set<Integer> selectedIndexes = new HashSet<>();
                List<String> result = new ArrayList<>();

                Random rand = new Random();
                while (result.size() < count) {
                    int chosenIndex;
                    if (size > 0) {
                        chosenIndex = rand.nextInt(size);
                    } else {
                        chosenIndex = 0;
                    }
                    if (remove) {
                        result.add(source.remove(chosenIndex));
                        size--;
                    } else if (selectedIndexes.add(chosenIndex)) {
                        result.add(source.get(chosenIndex));
                    }
                }
                return result;
            }
        };
    }

    private static void addSetToPool(Map<String, List<String>> cardPools, Map<String, List<String>> setPools, Map<String, List<String>> draftPools, String poolName, String setToAdd) {
        if (cardPools.containsKey(setToAdd)) {
            draftPools.get(poolName).addAll(cardPools.get(setToAdd));
        } else if (setPools.containsKey(setToAdd)) {
            for (String setName : setPools.get(setToAdd)) {
                addSetToPool(cardPools, setPools, draftPools, poolName, setName);
            }
        } else {
            throw new IllegalArgumentException("Invalid JSON content");
        }
    }

    private static StartingCollectionProducer buildStartingCollectionProducer(JSONArray jsonArray) {

        return new StartingCollectionProducer() {
            @Override
            public MutableCardCollection getStartingCardCollection(String uniqueEventName, String playerName) {
                long seed = uniqueEventName.hashCode() + (long) playerName.hashCode() * HIGH_ENOUGH_PRIME_NUMBER;
                Random rnd = new Random(seed);

                final DefaultCardCollection startingCollection = new DefaultCardCollection();

                for (JSONObject wheel : (Iterable<JSONObject>) jsonArray) {
                    int size = ((Number) wheel.get("size")).intValue();
                    List<String> cardList = (List<String>) wheel.get("card-list");

                    int start = rnd.nextInt(cardList.size());

                    for (String card : getCyclingIterable(cardList, start, size)) {
                        startingCollection.addItem(card, 1);
                    }
                }

                return startingCollection;
            }

            private Iterable<String> getCyclingIterable(List<String> list, int start, int length) {
                return Iterables.limit(Iterables.skip(Iterables.cycle(list), start), length);
            }
        };
    }
}
