package com.gempukku.lotro.bots.random;

import com.gempukku.lotro.bots.BotPlayer;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.*;
import java.util.stream.Collectors;

public class RandomDecisionBot implements BotPlayer {
    private final String botName;
    private final Random random = new Random();

    public RandomDecisionBot(String botName) {
        this.botName = botName;
    }

    @Override
    public String chooseAction(GameState gameState, AwaitingDecision awaitingDecision) {
        AwaitingDecisionType type = awaitingDecision.getDecisionType();
        Map<String, String[]> params = awaitingDecision.getDecisionParameters();

        return switch (type) {
            case INTEGER -> chooseIntegerDecision(params);
            case MULTIPLE_CHOICE -> chooseFromMultipleChoice(params);
            case ARBITRARY_CARDS -> chooseFromArbitraryCards(params);
            case CARD_ACTION_CHOICE -> chooseFromCardActionChoice(params);
            case ACTION_CHOICE -> chooseFromActionChoice(params);
            case CARD_SELECTION -> chooseFromCardSelection(params);
            case ASSIGN_MINIONS -> chooseAssignment(params, gameState.getCurrentPlayerId().equals(botName));
        };
    }

    @Override
    public String getName() {
        return botName;
    }

    private String chooseIntegerDecision(Map<String, String[]> params) {
        int min = 0;
        int max = 10; // a reasonable default

        if (params.containsKey("min")) {
            min = Integer.parseInt(params.get("min")[0]);
        }

        if (params.containsKey("max")) {
            max = Integer.parseInt(params.get("max")[0]);
        }

        if (min > max) {
            // Fallback safety
            return String.valueOf(min);
        }

        int choice = random.nextInt((max - min + 1)) + min;
        return String.valueOf(choice);
    }

    private String chooseFromMultipleChoice(Map<String, String[]> params) {
        String[] options = params.get("results");
        if (options == null || options.length == 0) {
            throw new IllegalArgumentException("No options provided for multiple choice");
        }

        int choiceIndex = random.nextInt(options.length);
        return String.valueOf(choiceIndex);
    }

    private String chooseFromArbitraryCards(Map<String, String[]> params) {
        String[] cardIds = params.get("cardId");
        String[] selectable = params.get("selectable");
        int min = params.containsKey("min") ? Integer.parseInt(params.get("min")[0]) : 0;
        int max = params.containsKey("max") ? Integer.parseInt(params.get("max")[0]) : cardIds.length;

        if (cardIds == null || selectable == null || cardIds.length != selectable.length)
            return ""; // Invalid input

        // Collect all selectable indices
        List<Integer> selectableIndices = new ArrayList<>();
        for (int i = 0; i < selectable.length; i++) {
            if (Boolean.parseBoolean(selectable[i])) {
                selectableIndices.add(i);
            }
        }

        // If nothing is selectable or max == 0, just return empty
        if (selectableIndices.isEmpty() || max == 0)
            return "";

        // Pick how many to select (between min and max)
        int toPick = min + random.nextInt(Math.max(1, Math.min(max, selectableIndices.size()) - min + 1));

        // Randomly pick `toPick` cards from selectableIndices
        Collections.shuffle(selectableIndices, random);
        List<String> picked = new ArrayList<>();
        for (int i = 0; i < toPick; i++) {
            // This temp prefix matches parsing in ArbitraryCardsSelectionDecision class
            picked.add("temp" + selectableIndices.get(i));
        }

        return String.join(",", picked);
    }

    private String chooseFromCardActionChoice(Map<String, String[]> params) {
        String[] actionIds = params.get("actionId");
        if (actionIds == null || actionIds.length == 0) {
            // No actions available: must pass
            return "";
        }

        // Randomly pick an action or choose to pass
        int totalOptions = actionIds.length + 1; // include "pass"
        int choice = random.nextInt(totalOptions);
        if (choice == actionIds.length) {
            return ""; // pass
        } else {
            return String.valueOf(choice); // choose action index
        }
    }

    private String chooseFromActionChoice(Map<String, String[]> params) {
        String[] actionIds = params.get("actionId");
        if (actionIds == null || actionIds.length == 0) {
            throw new IllegalArgumentException("No actions provided for required choice");
        }

        // Randomly pick one of the mandatory actions
        int choice = random.nextInt(actionIds.length);
        return String.valueOf(choice);
    }

    private String chooseFromCardSelection(Map<String, String[]> params) {
        String[] cardIds = params.get("cardId");
        if (cardIds == null || cardIds.length == 0)
            return "";

        int min = parseIntOrDefault(params.get("min"), 0);
        int max = parseIntOrDefault(params.get("max"), cardIds.length);

        int count = min + random.nextInt(max - min + 1); // inclusive
        List<String> selectionPool = new ArrayList<>(Arrays.asList(cardIds));
        Collections.shuffle(selectionPool, random);

        return String.join(",", selectionPool.subList(0, count));
    }

    private int parseIntOrDefault(String[] value, int defaultVal) {
        if (value != null && value.length > 0) {
            try {
                return Integer.parseInt(value[0]);
            } catch (NumberFormatException ignored) {}
        }
        return defaultVal;
    }

    private String chooseAssignment(Map<String, String[]> params, boolean isFreePeoples) {
        String[] freeCharIds = params.get("freeCharacters");
        String[] minionIds = params.get("minions");

        if (freeCharIds == null || minionIds == null || freeCharIds.length == 0 || minionIds.length == 0)
            return "";

        List<String> freeChars = new ArrayList<>(List.of(freeCharIds));
        List<String> minions = new ArrayList<>(List.of(minionIds));

        Collections.shuffle(freeChars, random);
        Collections.shuffle(minions, random);

        Map<String, List<String>> assignments = new HashMap<>();

        if (isFreePeoples) {
            // Assign 0 or 1 minion to each character (simulate defender = 0)
            int assignableCount = Math.min(freeChars.size(), minions.size());

            for (int i = 0; i < assignableCount; i++) {
                assignments.put(freeChars.get(i), List.of(minions.get(i)));
            }
            // Shadow will assign the rest later
        } else {
            // Shadow assigns remaining unassigned minions however they want
            for (String minion : minions) {
                String target = freeChars.get(random.nextInt(freeChars.size()));
                assignments.computeIfAbsent(target, k -> new ArrayList<>()).add(minion);
            }
        }

        return assignments.entrySet().stream()
                .map(entry -> entry.getKey() + " " + String.join(" ", entry.getValue()))
                .collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return botName;
    }
}
