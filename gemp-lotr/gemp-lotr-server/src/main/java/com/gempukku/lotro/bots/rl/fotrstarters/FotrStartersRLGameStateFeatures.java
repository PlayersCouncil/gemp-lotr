package com.gempukku.lotro.bots.rl.fotrstarters;

import com.gempukku.lotro.bots.rl.RLGameStateFeatures;
import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.Assignment;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.Skirmish;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.timing.GameStats;
import com.gempukku.lotro.logic.vo.LotroDeck;

import java.util.*;

public class FotrStartersRLGameStateFeatures implements RLGameStateFeatures {
    private static final List<String> BLUEPRINTS = List.of(
            "1_2", "1_12", "1_26", "1_37", "1_51", "1_70", "1_76", "1_78", "1_84", "1_86",
            "1_92", "1_94", "1_97", "1_101", "1_104", "1_106", "1_107", "1_108", "1_110", "1_116",
            "1_117", "1_121", "1_133", "1_141", "1_145", "1_150", "1_151", "1_152", "1_153", "1_154",
            "1_157", "1_158", "1_168", "1_176", "1_177", "1_178", "1_179", "1_180", "1_181", "1_187",
            "1_191", "1_196", "1_286", "1_290", "1_296", "1_299", "1_304", "1_309", "1_311", "1_312", "1_320",
            "1_326", "1_327", "1_331", "1_337", "1_340", "1_345", "1_346", "1_349", "1_350", "1_351",
            "1_353", "1_355", "1_358", "1_359", "1_360", "1_361", "1_364", "1_365"
    );
    private static final List<String> COMPANIONS_ALLIES = List.of(
            "1_290", "1_311", "1_97", "1_365", "1_51", "1_12", "1_364", "1_70", "1_286", "1_309"
    );
    private static final List<String> MINIONS_MORIA = List.of(
            "1_176", "1_177", "1_178", "1_179", "1_181", "1_191"
    );
    private static final List<String> MINIONS_URUKS = List.of(
            "1_145", "1_150", "1_151", "1_152", "1_153", "1_154", "1_158"
    );
    private static final List<String> ATTACHED_CARDS = List.of(
            // Ring on Frodo
            "1_2|1_290",
            // Ring on Sam
            "1_2|1_311",
            // Sword on Frodo
            "1_299|1_290",
            // Sword on Sam
            "1_299|1_311",
            // Armor on Aragorn
            "1_92|1_365",
            // Armor on Boromir
            "1_92|1_97",
            // Coat of Mail on Aragorn
            "1_101|1_365",
            // Coat of Mail on Boromir
            "1_101|1_97",
            // Shield on Aragorn
            "1_107|1_365",
            // Shield on Boromir
            "1_107|1_97",
            // Athelas on Aragorn
            "1_94|1_365",
            // Athelas on Boromir
            "1_94|1_97",
            // No Stranger on Aragorn
            "1_108|1_365",
            // Scimitars on Marksmen
            "1_180|1_176",
            // Scimitars on Patrol Troop
            "1_180|1_177",
            // Scimitars on Runners
            "1_180|1_178",
            // Scimitars on Scavangers
            "1_180|1_179",
            // Scimitars on Sneaks
            "1_180|1_181",
            // Scimitar on Scouts
            "1_180|1_191"
    );
    private static final List<String> WOUND_LIST = List.of(
            "1_290", "1_12", "1_51", "1_364", "1_97", "1_70", "1_286", "1_286", "1_286", // Gandalf
            "1_365", "1_311", "1_309", // Aragorn
            "1_177", "1_191", "1_191", "1_191", // Moria
            "1_145", "1_150", "1_150", "1_150", "1_150", "1_151", "1_151", "1_151", "1_151", "1_152", "1_152", "1_152", "1_152", "1_153", "1_153", "1_153", "1_158", "1_158" // Uruks
    );

    public static List<String> getBlueprints() {
        return BLUEPRINTS;
    }

    @Override
    public double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerId) {
        // Game state
        String opponent = gameState.getPlayerNames().stream()
                .filter(s -> !s.equals(playerId)).findFirst()
                .orElseThrow(() -> new IllegalStateException("Unknown second player"));
        GameStats gameStats = gameState.getMostRecentGameStats();

        List<Double> features = new ArrayList<>();

        features.add(getDeckIndex(gameState, playerId));
        features.add(getPhaseIndex(gameState));
        if (gameState.isInit()) {
            features.add(getTurnIndicator(gameState, playerId));
            features.add((double) gameState.getTwilightPool());
            features.add((double) gameState.getPlayerPosition(playerId));
            features.add((double) gameState.getPlayerPosition(opponent));

            features.addAll(getActiveCardCounts(gameState, playerId));
            features.addAll(getActiveCardCounts(gameState, opponent));

            features.addAll(getAttachmentCounts(gameState, playerId));
            features.addAll(getAttachmentCounts(gameState, opponent));

            features.addAll(getCardInHandCounts(gameState, playerId));
            features.add((double) gameState.getHand(opponent).size());

            features.addAll(getDeadPileCounts(gameState, playerId));
            features.addAll(getDeadPileCounts(gameState, opponent));

            features.addAll(getDiscardPileCounts(gameState, playerId));
            features.addAll(getDiscardPileCounts(gameState, opponent));

            features.addAll(getAssignments(gameState));
            features.addAll(getSkirmish(gameState));

            features.add((double) gameState.getPlayerBurdens(playerId));
            features.add((double) gameState.getPlayerBurdens(opponent));

            features.addAll(getWoundsOnCards(gameState, playerId));
            features.addAll(getWoundsOnCards(gameState, opponent));

            features.add(gameState.isWearingRing() ? 1.0 : 0.0);
            features.add(((double) gameState.getMoveCount()));

            features.add(((double) gameStats.getFellowshipSkirmishStrength()));
            features.add(((double) gameStats.getShadowSkirmishStrength()));
            features.add(((double) gameStats.getFellowshipSkirmishDamageBonus()));
            features.add(((double) gameStats.getShadowSkirmishDamageBonus()));
            features.add(gameStats.isFpOverwhelmed() ? 1.0 : 0.0);
        } else {
            for (int i = 0;
                 i <     14
                         + (BLUEPRINTS.size() * 5)
                         + (ATTACHED_CARDS.size() * 2)
                         + (COMPANIONS_ALLIES.size() * 2)
                         + (COMPANIONS_ALLIES.size() * (1 + MINIONS_URUKS.size() + (MINIONS_MORIA.size() * 2)))
                         + (COMPANIONS_ALLIES.size() + MINIONS_URUKS.size() + (MINIONS_MORIA.size() * 2))
                         + (WOUND_LIST.size() * 2);
                 i++) {
                features.add(0.0);
            }
        }

        // Decision
        features.addAll(getIntegerDecision(decision));
        features.addAll(getMultipleChoiceDecision(decision));
        features.addAll(getArbitraryCardsDecision(decision));
        features.addAll(getCardActionChoiceDecision(decision, gameState));
        features.addAll(getActionChoiceDecision(decision));
        features.addAll(getCardsSelectionDecision(decision, gameState));
        features.addAll(getAssignMinionsDecision(decision, gameState));

        return features.stream().mapToDouble(Double::doubleValue).toArray();
    }

    private Collection<Double> getAssignMinionsDecision(AwaitingDecision decision, GameState gameState) {
        List<Double> result = new ArrayList<>();
        if (decision.getDecisionType() == AwaitingDecisionType.ASSIGN_MINIONS) {
            result.add(1.0);
            String[] fp = decision.getDecisionParameters().get("freeCharacters");
            String[] minions = decision.getDecisionParameters().get("minions");
            for (String blueprint : COMPANIONS_ALLIES) {
                int count = 0;
                for (String id : fp) {
                    if (gameState.getBlueprintId(Integer.parseInt(id)).equals(blueprint)) {
                        count++;
                    }
                }
                result.add((double) count);
            }
            for (String blueprint : MINIONS_URUKS) {
                int count = 0;
                for (String id : minions) {
                    if (gameState.getBlueprintId(Integer.parseInt(id)).equals(blueprint)) {
                        count++;
                    }
                }
                result.add((double) count);
            }
            for (String blueprint : MINIONS_MORIA) {
                int count = 0;
                for (String id : minions) {
                    if (gameState.getBlueprintId(Integer.parseInt(id)).equals(blueprint)) {
                        count++;
                    }
                }
                result.add((double) count);
            }
        } else {
            for (int i = 0; i < 1 + COMPANIONS_ALLIES.size() + MINIONS_URUKS.size() + MINIONS_MORIA.size(); i++) {
                result.add(0.0);
            }
        }
        return result;

    }

    private Collection<Double> getCardsSelectionDecision(AwaitingDecision decision, GameState gameState) {
        List<Double> result = new ArrayList<>();
        if (decision.getDecisionType() == AwaitingDecisionType.CARD_SELECTION) {
            result.add(1.0);
            String[] ids = decision.getDecisionParameters().get("cardId");
            for (String blueprint : BLUEPRINTS) {
                int count = 0;
                for (String id : ids) {
                    if (gameState.getBlueprintId(Integer.parseInt(id)).equals(blueprint)) {
                        count++;
                    }
                }
                result.add((double) count);
            }
        } else {
            for (int i = 0; i < 1 + BLUEPRINTS.size(); i++) {
                result.add(0.0);
            }
        }
        return result;
    }

    private Collection<Double> getActionChoiceDecision(AwaitingDecision decision) {
        List<Double> result = new ArrayList<>();
        result.add(decision.getDecisionType() == AwaitingDecisionType.ACTION_CHOICE ? 1.0 : 0.0);
        return result;
    }

    private Collection<Double> getCardActionChoiceDecision(AwaitingDecision decision, GameState gameState) {
        List<Double> result = new ArrayList<>();
        if (decision.getDecisionType() == AwaitingDecisionType.CARD_ACTION_CHOICE) {
            List<String> actionText = Arrays.asList(decision.getDecisionParameters().get("actionText"));
            String[] cardIds = decision.getDecisionParameters().get("cardId");

            List<String> playCardIds = new ArrayList<>();
            List<String> otherCardIds = new ArrayList<>();

            for (int i = 0; i < actionText.size(); i++) {
                String text = actionText.get(i);
                String cardId = cardIds[i];

                if (text.contains("Play")) {
                    playCardIds.add(cardId);
                } else {
                    otherCardIds.add(cardId);
                }
            }

            result.add(1.0);
            for (String blueprint : BLUEPRINTS) {
                int count = 0;
                for (String id : playCardIds) {
                    if (gameState.getBlueprintId(Integer.parseInt(id)).equals(blueprint)) {
                        count++;
                    }
                }
                result.add((double) count);
            }
            for (String blueprint : BLUEPRINTS) {
                int count = 0;
                for (String id : otherCardIds) {
                    if (gameState.getBlueprintId(Integer.parseInt(id)).equals(blueprint)) {
                        count++;
                    }
                }
                result.add((double) count);
            }
        } else {
            for (int i = 0; i < 1 + (BLUEPRINTS.size() * 2); i++) {
                result.add(0.0);
            }
        }
        return result;
    }

    private Collection<Double> getArbitraryCardsDecision(AwaitingDecision decision) {
        List<Double> result = new ArrayList<>();
        if (decision.getDecisionType() == AwaitingDecisionType.ARBITRARY_CARDS) {
            result.add(1.0);
            result.add(decision.getText().contains("Starting fellowship") ? 1.0 : 0.0);
            result.add(decision.getText().contains("You may inspect the contents of your deck while retrieving cards") ? 1.0 : 0.0);
            result.add(decision.getText().contains("Choose card from discard") ? 1.0 : 0.0);
            String[] ids = decision.getDecisionParameters().get("blueprintId");
            for (String blueprint : BLUEPRINTS) {
                int count = 0;
                for (String id : ids) {
                    if (id.equals(blueprint)) {
                        count++;
                    }
                }
                result.add((double) count);
            }
        } else {
            for (int i = 0; i < 4 + BLUEPRINTS.size(); i++) {
                result.add(0.0);
            }
        }
        return result;
    }

    private Collection<Double> getMultipleChoiceDecision(AwaitingDecision decision) {
        List<Double> result = new ArrayList<>();
        if (decision.getDecisionType() == AwaitingDecisionType.MULTIPLE_CHOICE) {
            result.add(1.0);
            result.add(decision.getDecisionParameters().get("results")[0].equals("Go first") ? 1.0 : 0.0);
            result.add(decision.getText().contains("mulligan") ? 1.0 : 0.0);
            result.add(decision.getText().contains("another move") ? 1.0 : 0.0);
        } else {
            for (int i = 0; i < 4; i++) {
                result.add(0.0);
            }
        }
        return result;
    }

    private Collection<Double> getIntegerDecision(AwaitingDecision decision) {
        List<Double> result = new ArrayList<>();
        result.add(decision.getDecisionType() == AwaitingDecisionType.INTEGER ? 1.0 : 0.0);
        return result;
    }

    private double getTurnIndicator(GameState gameState, String playerId) {
        String current = gameState.getCurrentPlayerId();
        return current == null ? -1.0 : (current.equals(playerId) ? 1.0 : 0.0);
    }

    private double getPhaseIndex(GameState gameState) {
        var phase = gameState.getCurrentPhase();
        if (phase == null) return -1.0;
        return switch (phase) {
            case PUT_RING_BEARER -> 0.0;
            case PLAY_STARTING_FELLOWSHIP -> 1.0;
            case FELLOWSHIP -> 2.0;
            case SHADOW -> 3.0;
            case MANEUVER -> 4.0;
            case ARCHERY -> 5.0;
            case ASSIGNMENT -> 6.0;
            case SKIRMISH -> 7.0;
            case REGROUP -> 8.0;
            case BETWEEN_TURNS -> 9.0;
        };
    }

    private double getDeckIndex(GameState gameState, String playerId) {
        LotroDeck deck = gameState.getLotroDeck(playerId);
        if (deck == null) {
            return 0.0;
        } else if (deck.getDrawDeckCards().contains("1_365")) {
            return 1.0;
        } else if (deck.getDrawDeckCards().contains("1_364")) {
            return 2.0;
        } else {
            return 0.0;
        }
    }

    private List<Double> getActiveCardCounts(GameState gameState, String owner) {
        List<Double> result = new ArrayList<>();
        List<? extends PhysicalCard> inPlay = gameState.getInPlay();

        for (String blueprintId : BLUEPRINTS) {
            long count = inPlay.stream()
                    .filter(card -> blueprintId.equals(card.getBlueprintId()) && owner.equals(card.getOwner()))
                    .count();
            result.add((double) count);
        }

        return result;
    }

    private List<Double> getAttachmentCounts(GameState gameState, String owner) {
        List<Double> result = new ArrayList<>();
        List<? extends PhysicalCard> inPlay = gameState.getInPlay();

        for (String combo : ATTACHED_CARDS) {
            String[] split = combo.split("\\|");
            String itemId = split[0];
            String targetId = split[1];

            long count = inPlay.stream()
                    .filter(card -> itemId.equals(card.getBlueprintId())
                            && owner.equals(card.getOwner())
                            && isAttachedTo(card, targetId))
                    .count();

            result.add((double) count);
        }

        return result;
    }

    private boolean isAttachedTo(PhysicalCard item, String expectedBlueprint) {
        PhysicalCard attachedTo = item.getAttachedTo();
        return attachedTo != null && expectedBlueprint.equals(attachedTo.getBlueprintId());
    }

    private List<Double> getCardInHandCounts(GameState gameState, String owner) {
        List<Double> result = new ArrayList<>();
        List<? extends PhysicalCard> inHand = gameState.getHand(owner);

        for (String blueprintId : BLUEPRINTS) {
            long count = inHand.stream()
                    .filter(card -> blueprintId.equals(card.getBlueprintId()))
                    .count();
            result.add((double) count);
        }

        return result;
    }

    private List<Double> getDeadPileCounts(GameState gameState, String owner) {
        List<Double> result = new ArrayList<>();
        List<? extends PhysicalCard> inDeadPile = gameState.getDeadPile(owner);

        for (String blueprintId : COMPANIONS_ALLIES) {
            long count = inDeadPile.stream()
                    .filter(card -> blueprintId.equals(card.getBlueprintId()))
                    .count();
            result.add((double) count);
        }

        return result;
    }

    private List<Double> getDiscardPileCounts(GameState gameState, String owner) {
        List<Double> result = new ArrayList<>();
        List<? extends PhysicalCard> inDiscard = gameState.getDiscard(owner);

        for (String blueprintId : BLUEPRINTS) {
            long count = inDiscard.stream()
                    .filter(card -> blueprintId.equals(card.getBlueprintId()))
                    .count();
            result.add((double) count);
        }

        return result;
    }

    private List<Double> getAssignments(GameState gameState) {
        List<Double> result = new ArrayList<>();
        List<Assignment> assignments = gameState.getAssignments();

        for (String fpCharacter : COMPANIONS_ALLIES) {
            // Does not work best with multiple Bounders assigned, edge case
            Assignment forThisCharacter = assignments.stream().filter(assignment -> assignment.getFellowshipCharacter().getBlueprintId().equals(fpCharacter)).findFirst().orElse(null);

            if (forThisCharacter != null) {
                result.add(1.0); // Character is assigned

                // Count Uruks
                for (String uruk : MINIONS_URUKS) {
                    result.add((double) forThisCharacter.getShadowCharacters().stream()
                            .filter(minion -> minion.getBlueprintId().equals(uruk)).count());
                }

                // Determine which goblins have scimitars
                List<Integer> idsWithScimitar = gameState.getInPlay().stream()
                        .filter(pc -> pc.getBlueprintId().equals("1_180") && pc.getAttachedTo() != null)
                        .map(pc -> pc.getAttachedTo().getCardId())
                        .toList();

                // Goblins without scimitars
                for (String goblin : MINIONS_MORIA) {
                    result.add((double) forThisCharacter.getShadowCharacters().stream()
                            .filter(minion -> minion.getBlueprintId().equals(goblin) && !idsWithScimitar.contains(minion.getCardId()))
                            .count());
                }

                // Goblins with scimitars
                for (String goblin : MINIONS_MORIA) {
                    result.add((double) forThisCharacter.getShadowCharacters().stream()
                            .filter(minion -> minion.getBlueprintId().equals(goblin) && idsWithScimitar.contains(minion.getCardId()))
                            .count());
                }
            } else {
                for (int i = 0; i < 1 + MINIONS_URUKS.size() + (MINIONS_MORIA.size() * 2); i++) {
                    result.add(0.0);
                }
            }
        }

        return result;
    }

    private List<Double> getSkirmish(GameState gameState) {
        List<Double> result = new ArrayList<>();
        Skirmish skirmish = gameState.getSkirmish();

        if (skirmish != null) {
            // Who is skirmishing
            for (String fpCharacter : COMPANIONS_ALLIES) {
                if (skirmish.getFellowshipCharacter() == null) {
                    result.add(0.0);
                } else {
                    result.add(skirmish.getFellowshipCharacter().getBlueprintId().equals(fpCharacter) ? 1.0 : 0.0);
                }
            }

            // Count Uruks
            for (String uruk : MINIONS_URUKS) {
                result.add((double) skirmish.getShadowCharacters().stream()
                        .filter(minion -> minion.getBlueprintId().equals(uruk)).count());
            }

            // Determine which goblins have scimitars
            List<Integer> idsWithScimitar = gameState.getInPlay().stream()
                    .filter(pc -> pc.getBlueprintId().equals("1_180") && pc.getAttachedTo() != null)
                    .map(pc -> pc.getAttachedTo().getCardId())
                    .toList();

            // Goblins without scimitars
            for (String goblin : MINIONS_MORIA) {
                result.add((double) skirmish.getShadowCharacters().stream()
                        .filter(minion -> minion.getBlueprintId().equals(goblin) && !idsWithScimitar.contains(minion.getCardId()))
                        .count());
            }

            // Goblins with scimitars
            for (String goblin : MINIONS_MORIA) {
                result.add((double) skirmish.getShadowCharacters().stream()
                        .filter(minion -> minion.getBlueprintId().equals(goblin) && idsWithScimitar.contains(minion.getCardId()))
                        .count());
            }
        } else {
            for (int i = 0; i < COMPANIONS_ALLIES.size() + MINIONS_URUKS.size() + (MINIONS_MORIA.size() * 2); i++) {
                result.add(0.0);
            }
        }

        return result;
    }

    private List<Double> getWoundsOnCards(GameState gameState, String owner) {
        List<Double> result = new ArrayList<>();
        List<? extends PhysicalCard> inPlay = new ArrayList<>(gameState.getInPlay());
        List<PhysicalCard> usedCards = new ArrayList<>();
        inPlay.sort(Comparator.comparingInt(PhysicalCard::getCardId));

        for (String characterId : WOUND_LIST) {
            double toAdd = 0.0;
            for (PhysicalCard physicalCard : inPlay) {
                if (physicalCard.getBlueprintId().equals(characterId) && !usedCards.contains(physicalCard)) {
                    toAdd = gameState.getTokenCount(physicalCard, Token.WOUND);
                    usedCards.add(physicalCard);
                    break;
                }
            }
            result.add(toAdd);
        }

        return result;
    }
}
