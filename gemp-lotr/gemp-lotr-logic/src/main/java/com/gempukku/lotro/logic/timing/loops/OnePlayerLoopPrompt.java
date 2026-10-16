package com.gempukku.lotro.logic.timing.loops;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.SystemQueueAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.decisions.IntegerAwaitingDecision;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;

import java.util.List;
import java.util.function.IntConsumer;

/**
 * The prompt of a loop of one player's actions (CR 4.2 "loops"; OnePlayerLoopTracker), in Ketura's words
 * (answers/2026-10-16-c.md): "LOOP DETECTED: Choose a number of times to repeat this action."  The player names a
 * number from 0 to 99 ({@link LoopNegotiation#MOST_ITERATIONS}), filled in with 3 as the two players' prompt is;
 * 0 repeats nothing and only bars the loop's abilities.  Her words name no card, so the game log names them as the
 * prompt is asked, each as GameUtils.getCardLink gives it (the card hint), and says the number chosen.
 * <p>
 * The number is handed to the window that found the loop (OnePlayerLoopTracker.agreed), which carries it out
 * (LoopRun).
 */
public class OnePlayerLoopPrompt extends SystemQueueAction {
    /**
     * @param game The game.
     * @param playerId The player whose loop it is.
     * @param cards The loop's cards (OnePlayerLoopTracker.findLoop).
     * @param onChosen Told the number chosen, once it is.
     */
    public OnePlayerLoopPrompt(LotroGame game, String playerId, List<PhysicalCard> cards, IntConsumer onChosen) {
        final String names = namesOf(cards);
        game.getGameState().sendMessage("Loop detected: " + names + " can be used forever");
        appendEffect(
                new PlayoutDecisionEffect(playerId,
                        new IntegerAwaitingDecision(1, OnePlayerLoopTracker.PROMPT, 0, LoopNegotiation.MOST_ITERATIONS,
                                LoopNegotiation.DEFAULT_ITERATIONS) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                final int iterations = getValidatedResult(result);
                                game.getGameState().sendMessage(playerId + " chose to repeat " + names + " "
                                        + iterations + (iterations == 1 ? " time" : " times"));
                                onChosen.accept(iterations);
                            }
                        }));
    }

    private static String namesOf(List<PhysicalCard> cards) {
        final StringBuilder sb = new StringBuilder();
        for (PhysicalCard card : cards) {
            if (!sb.isEmpty())
                sb.append(" and ");
            sb.append(GameUtils.getCardLink(card));
        }
        return sb.toString();
    }
}
