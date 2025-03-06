package com.gempukku.lotro.draft3.bot;

import com.gempukku.lotro.draft3.DraftPlayer;
import com.gempukku.lotro.draft3.TableDraft;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class WeightDraftBot extends DraftPlayer implements DraftBot {
    private final Map<String, Double> cardValues;

    private final Random rand = new Random();

    public WeightDraftBot(TableDraft table, String name, Map<String, Double> cardValues) {
        super(table, name);
        this.cardValues = cardValues;
    }

    @Override
    public String chooseCard(List<String> cardsToPickFrom) {
        List<String> topCards = getTopCards(cardsToPickFrom);
        double totalValue = getTotalValue(topCards);

        // Generate a random number between 0 and the total value sum
        double randomValue = rand.nextDouble() * totalValue;

        // Determine which card to choose based on the random number
        return getCardFromRandomValue(topCards, randomValue);
    }

    private List<String> getTopCards(List<String> cardsToPickFrom) {
        // Sort the cards by value in descending order
        return cardsToPickFrom.stream()
                .sorted((card1, card2) -> Double.compare(cardValues.getOrDefault(card2, 0.0), cardValues.getOrDefault(card1, 0.0)))
                .limit(getTopCardCount(cardsToPickFrom.size()))
                .collect(Collectors.toList());
    }

    private int getTopCardCount(int packSize) {
        // Choose top 3 cards if the pack has more than 5, otherwise choose half of the cards
        return packSize <= 5 ? packSize / 2 : 3;
    }

    private double getTotalValue(List<String> topCards) {
        // Sum up the values of the top N cards
        return topCards.stream()
                .mapToDouble(cardId -> cardValues.getOrDefault(cardId, 0.0))
                .sum();
    }

    private String getCardFromRandomValue(List<String> topCards, double randomValue) {
        double cumulativeValue = 0.0;

        // Go through the top cards and add their values to the cumulative sum
        for (String cardId : topCards) {
            cumulativeValue += cardValues.getOrDefault(cardId, 0.1);
            if (randomValue <= cumulativeValue) {
                return cardId; // Return the card when we reach the random value
            }
        }

        return topCards.get(topCards.size() - 1);  // Fallback, should never happen
    }
}
