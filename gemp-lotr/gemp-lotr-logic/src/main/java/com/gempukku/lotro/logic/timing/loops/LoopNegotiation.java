package com.gempukku.lotro.logic.timing.loops;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.SystemQueueAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.decisions.IntegerAwaitingDecision;
import com.gempukku.lotro.logic.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;

import java.util.function.Consumer;

/**
 * The two prompts of CR 4.2 "loops" for a loop of both players' actions, in Ketura's words (Q-0329,
 * answers/2026-10-15-c.md).  The Free Peoples player names how many more times the loop runs (1 to 99, filled in
 * with 3), whichever player opened it.  The Shadow player then agrees, and the loop goes that many times with the
 * Free Peoples player taking the next action, or chooses a lower number (0 to one less than the number named),
 * and the Shadow player takes the next action.  Where the number named is 1, the lower number can only be 0 and
 * is not asked (PlayoutDecisionEffect, F-0147).
 * <p>
 * The texts are Ketura's words, sent as HTML, since the client puts a decision's text in with jQuery's .html(): the
 * rules link as an anchor, each card as GameUtils.getCardLink gives it (the cardHint div the game log uses; Ketura's
 * [[...]], Q-0329, answers/2026-10-16-a.md), the number in &lt;b&gt;, and Ketura's line breaks as &lt;br&gt;.
 * <p>
 * What is settled is handed to the window that found the loop (LoopTracker.agreed), which carries it out (LoopRun).
 */
public class LoopNegotiation extends SystemQueueAction {
    /** The first line of the Free Peoples player's prompt and of the Shadow player's. */
    public static final String HEADER = "LOOP DETECTED: <a href=\"https://wiki.lotrtcgpc.net/wiki/Comprehensive_Rules_4.2#loops\""
            + " target=\"_blank\">See loop handling rules here.</a>";
    /** The most iterations the Free Peoples player may name: it bounds the work the server does in one go. */
    public static final int MOST_ITERATIONS = 99;
    /** The number the Free Peoples player's prompt is filled in with. */
    public static final int DEFAULT_ITERATIONS = 3;

    /**
     * What the players settled.
     * @param named The number of iterations the Free Peoples player named.
     * @param shadowAgreed Whether the Shadow player agreed to it.
     * @param iterations The number of iterations the loop runs: the number named if the Shadow player agreed,
     *                   his lower number if not.
     * @param nextActor The player who takes the next action once the loop has run: the Free Peoples player if the
     *                  Shadow player agreed (the Shadow player having taken the last), the Shadow player if he
     *                  chose a lower number.
     */
    public record Agreement(int named, boolean shadowAgreed, int iterations, String nextActor) {
    }

    /**
     * @param game The game.
     * @param loop The loop found.
     * @param onAgreed Told what the players settled, once they have.
     */
    public LoopNegotiation(LotroGame game, LoopTracker.Loop loop, Consumer<Agreement> onAgreed) {
        final String freePeoplesPlayer = GameUtils.getFreePeoplePlayer(game);
        final String shadowPlayer = freePeoplesPlayer.equals(loop.opener()) ? loop.other() : loop.opener();
        final String cards = namesOf(loop.openerCard(), loop.otherCard());

        appendEffect(
                new PlayoutDecisionEffect(freePeoplesPlayer,
                        new IntegerAwaitingDecision(1, freePeoplesPrompt(cards), 1, MOST_ITERATIONS, DEFAULT_ITERATIONS) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                final int named = getValidatedResult(result);
                                appendEffect(
                                        new PlayoutDecisionEffect(shadowPlayer,
                                                new MultipleChoiceAwaitingDecision(1, shadowPrompt(cards, named),
                                                        new String[]{agreeChoice(named), lowerChoice()}) {
                                                    @Override
                                                    protected void validDecisionMade(int index, String choice) {
                                                        if (index == 0) {
                                                            settle(game, onAgreed, cards, new Agreement(named, true, named, freePeoplesPlayer));
                                                        } else {
                                                            appendEffect(
                                                                    new PlayoutDecisionEffect(shadowPlayer,
                                                                            new IntegerAwaitingDecision(1, lowerPrompt(cards, named), 0, named - 1) {
                                                                                @Override
                                                                                public void decisionMade(String result) throws DecisionResultInvalidException {
                                                                                    final int lower = getValidatedResult(result);
                                                                                    settle(game, onAgreed, cards, new Agreement(named, false, lower, shadowPlayer));
                                                                                }
                                                                            }));
                                                        }
                                                    }
                                                }));
                            }
                        }));
    }

    private static void settle(LotroGame game, Consumer<Agreement> onAgreed, String cards, Agreement agreement) {
        if (agreement.shadowAgreed())
            game.getGameState().sendMessage("Loop between " + cards + ": the Free Peoples player named " + agreement.named()
                    + " more times, and the Shadow player agreed");
        else
            game.getGameState().sendMessage("Loop between " + cards + ": the Free Peoples player named " + agreement.named()
                    + " more times, and the Shadow player chose " + agreement.iterations());
        onAgreed.accept(agreement);
    }

    /**
     * @param cards The loop's cards as {@link #namesOf} gives them.
     * @return The Free Peoples player's prompt.
     */
    public static String freePeoplesPrompt(String cards) {
        return HEADER + "<br>" + cards + " can be used forever.<br>Choose how many more times this loop will execute.";
    }

    /**
     * @param cards The loop's cards as {@link #namesOf} gives them.
     * @param named The number the Free Peoples player named.
     * @return The Shadow player's prompt.
     */
    public static String shadowPrompt(String cards, int named) {
        return HEADER + "<br>The Free Peoples player has decided that the loop between " + cards + " will be performed <b>"
                + named + "</b> more times.";
    }

    /**
     * @param named The number the Free Peoples player named.
     * @return The Shadow player's first choice.
     */
    public static String agreeChoice(int named) {
        return "Agree: the loop goes <b>" + named + "</b> more times, and Free Peoples takes the next action.";
    }

    /**
     * @return The Shadow player's second choice.
     */
    public static String lowerChoice() {
        return "Choose a lower number: Shadow will take the next action.";
    }

    /**
     * @param cards The loop's cards as {@link #namesOf} gives them.
     * @param named The number the Free Peoples player named.
     * @return The Shadow player's prompt for a lower number.
     */
    public static String lowerPrompt(String cards, int named) {
        return "Choose a number lower than <b>" + named + "</b>. The loop between " + cards
                + " will be performed that many times, ending so that you will take the next action.";
    }

    /**
     * The loop's cards as the prompts and the game log name them: each as GameUtils.getCardLink gives it (a
     * cardHint div), the two joined by " and ", in the order the round takes them, and one card where both actions
     * are of one card.
     * @param first The card of the round's first action.
     * @param second The card of its second.
     * @return The names.
     */
    public static String namesOf(PhysicalCard first, PhysicalCard second) {
        final String firstName = GameUtils.getCardLink(first);
        if (second == null || second == first)
            return firstName;
        return firstName + " and " + GameUtils.getCardLink(second);
    }
}
