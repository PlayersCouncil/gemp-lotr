package com.gempukku.lotro.logic.decisions;

import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.Action;

import java.util.List;
import java.util.Objects;

/**
 * The wording of the actions a decision offers.  An action is worded as it words itself, with one exception: where
 * one card offers the same action more than once in a decision because more than one card set it off (a response
 * to a won skirmish, with two winners), each offer is told what it is for, " (for <the card that set it off>)", if
 * the action knows.  An action offered once is never reworded.
 */
public class OfferedActionTexts {
    public static String[] of(LotroGame game, List<? extends Action> actions) {
        final String[] texts = new String[actions.size()];
        for (int i = 0; i < texts.length; i++)
            texts[i] = actions.get(i).getText(game);

        final String[] result = texts.clone();
        for (int i = 0; i < texts.length; i++) {
            final String offeredFor = actions.get(i).getOfferedFor();
            if (offeredFor != null && texts[i] != null && hasTwin(actions, texts, i))
                result[i] = texts[i] + " (for " + offeredFor + ")";
        }
        return result;
    }

    // Another offer in the same decision, of the same card, in the same words.
    private static boolean hasTwin(List<? extends Action> actions, String[] texts, int index) {
        final Action action = actions.get(index);
        for (int i = 0; i < texts.length; i++) {
            if (i == index)
                continue;
            final Action other = actions.get(i);
            if (other.getActionAttachedToCard() == action.getActionAttachedToCard()
                    && other.getActionSource() == action.getActionSource()
                    && Objects.equals(texts[i], texts[index]))
                return true;
        }
        return false;
    }
}
