package com.gempukku.lotro.logic.decisions;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * Tests of MultipleChoiceAwaitingDecision by itself: no game is played and no card is on a table.
 *
 * An answer to a multiple-choice decision is the number of one of its choices, counted from 0.  Anything else is
 * an invalid answer and is refused as one, with DecisionResultInvalidException: that is the exception the server
 * answers by warning the player and asking again, where any other exception cancels the game.
 */
public class MultipleChoiceAwaitingDecisionTest {
    // The choice of seat that the higher bidder is given before a game, recording each answer it accepts.
    private static class SeatingDecision extends MultipleChoiceAwaitingDecision {
        private final List<String> accepted = new ArrayList<>();

        private SeatingDecision() {
            super(1, "Choose one", new String[]{"Go first", "Go second"});
        }

        @Override
        protected void validDecisionMade(int index, String result) {
            accepted.add(index + ": " + result);
        }
    }

    @Test
    public void AnAnswerThatIsNoneOfTheChoicesIsRejectedAsInvalid() throws DecisionResultInvalidException {
        // #1023: the decisions before a game all carry the same decision id, so a late or repeated answer to
        // the bid ("2") can arrive while the same player is being asked to go first or go second.  It passed
        // the id check, was used as an index into the two choices, and the ArrayIndexOutOfBoundsException
        // cancelled the game.
        var seating = new SeatingDecision();

        // The stale bid: one past the last choice.
        assertThrows(DecisionResultInvalidException.class, () -> seating.decisionMade("2"));
        // One before the first choice.
        assertThrows(DecisionResultInvalidException.class, () -> seating.decisionMade("-1"));
        // A choice answered by its words and not its number, and no answer at all.
        assertThrows(DecisionResultInvalidException.class, () -> seating.decisionMade("Go second"));
        assertThrows(DecisionResultInvalidException.class, () -> seating.decisionMade(null));
        assertEquals(List.of(), seating.accepted);

        // A refused answer leaves the decision as it was: it is asked again and takes a proper answer.  The
        // last choice, beside the "2" refused above:
        seating.decisionMade("1");
        assertEquals(List.of("1: Go second"), seating.accepted);

        // And the first choice, beside the "-1", on a decision of its own.
        var other = new SeatingDecision();
        other.decisionMade("0");
        assertEquals(List.of("0: Go first"), other.accepted);
    }
}
