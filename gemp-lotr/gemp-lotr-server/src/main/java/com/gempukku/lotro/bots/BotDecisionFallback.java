package com.gempukku.lotro.bots;

import com.gempukku.lotro.bots.random.RandomDecisionBot;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Safety net for bot answers the engine rejects. A rejected human answer just re-asks the same decision; a bot's
 * model would usually give the same answer again, so the mediator used to concede the game for the bot
 * ("~bot lost due to: Invalid decision", issue #1097). Instead we try, in order:
 * <ol>
 *     <li>ASSIGN_MINIONS: the bot's own assignment with the forbidden pairings removed, then "assign nothing"
 *     (always legal for the Free Peoples player; for Shadow, the minions simply are not assigned);</li>
 *     <li>CARD_ACTION_CHOICE: pass;</li>
 *     <li>anything else: a few random answers built from the decision's own options.</li>
 * </ol>
 * Retrying is safe because every decision validates its answer before acting on it; the human path relies on the
 * same property when it re-sends a decision after DecisionResultInvalidException. Only if every fallback is also
 * rejected does the original exception propagate (and the bot concedes, as before).
 */
public final class BotDecisionFallback {
    private static final int RANDOM_ATTEMPTS = 5;

    private BotDecisionFallback() {
    }

    /**
     * Gives the decision the bot's answer, falling back as described above. {@code answer} may be null when the bot
     * failed to produce one. Returns the answer the engine accepted.
     */
    public static String decide(LotroGame game, String botName, AwaitingDecision decision, String answer) throws DecisionResultInvalidException {
        DecisionResultInvalidException rejection = null;
        if (answer != null) {
            try {
                decision.decisionMade(answer);
                return answer;
            } catch (DecisionResultInvalidException exp) {
                rejection = exp;
            }
        }

        for (String fallback : fallbackAnswers(game, botName, decision, answer)) {
            try {
                decision.decisionMade(fallback);
                return fallback;
            } catch (DecisionResultInvalidException ignored) {
                // try the next one
            }
        }
        throw rejection != null ? rejection : new DecisionResultInvalidException("Bot produced no valid answer");
    }

    static List<String> fallbackAnswers(LotroGame game, String botName, AwaitingDecision decision, String rejected) {
        Set<String> answers = new LinkedHashSet<>();
        AssignmentLegality legality = new AssignmentLegality(game);
        Map<String, String[]> params = decision.getDecisionParameters();

        switch (decision.getDecisionType()) {
            case ASSIGN_MINIONS -> {
                if (rejected != null) {
                    try {
                        answers.add(AssignmentLegality.format(legality.keepLegal(
                                AssignmentLegality.assigningSide(game.getGameState(), botName),
                                params.get("freeCharacters"), params.get("minions"), AssignmentLegality.parse(rejected))));
                    } catch (NumberFormatException ignored) {
                        // unparseable answer: nothing of it to keep
                    }
                }
                answers.add("");
            }
            case CARD_ACTION_CHOICE -> answers.add("");
            default -> {
                RandomDecisionBot random = new RandomDecisionBot(botName);
                for (int i = 0; i < RANDOM_ATTEMPTS; i++) {
                    try {
                        answers.add(random.chooseAction(game.getGameState(), decision, legality));
                    } catch (RuntimeException ignored) {
                        // e.g. a decision with no options; nothing random to offer
                    }
                }
            }
        }
        if (rejected != null)
            answers.remove(rejected);
        return List.copyOf(answers);
    }
}
