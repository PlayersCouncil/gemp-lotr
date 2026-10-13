package com.gempukku.lotro.logic.decisions;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.Action;

import java.util.List;

public abstract class ActionSelectionDecision extends AbstractAwaitingDecision {
    private final LotroGame _game;
    private final List<? extends Action> _actions;

    public ActionSelectionDecision(LotroGame game, int decisionId, String text, List<? extends Action> actions) {
        super(decisionId, text, AwaitingDecisionType.ACTION_CHOICE);
        _game = game;
        _actions = actions;

        setParam("actionId", getActionIds(actions));
        setParam("blueprintId", getBlueprintIds(actions));
        setParam("actionText", getActionTexts(actions));
    }

    private String[] getActionIds(List<? extends Action> actions) {
        String[] result = new String[actions.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = String.valueOf(i);
        return result;
    }

    private String[] getBlueprintIds(List<? extends Action> actions) {
        String[] result = new String[actions.size()];
        for (int i = 0; i < result.length; i++) {
            PhysicalCard physicalCard = actions.get(i).getActionAttachedToCard();
            if (physicalCard != null)
                result[i] = String.valueOf(physicalCard.getBlueprintId());
            else
                result[i] = "rules";
        }
        return result;
    }

    private String[] getActionTexts(List<? extends Action> actions) {
        // Each as it words itself, and two offers of one action told apart by what each is for.
        return OfferedActionTexts.of(_game, actions);
    }

    protected Action getSelectedAction(String result) throws DecisionResultInvalidException {
        if (result.equals(""))
            throw new DecisionResultInvalidException();

        try {
            int actionIndex = Integer.parseInt(result);
            if (actionIndex < 0 || actionIndex >= _actions.size())
                throw new DecisionResultInvalidException();

            return _actions.get(actionIndex);
        } catch (NumberFormatException exp) {
            throw new DecisionResultInvalidException();
        }
    }
}
