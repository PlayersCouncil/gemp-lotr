package com.gempukku.lotro.logic.decisions;

import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.Action;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public abstract class CardActionSelectionDecision extends AbstractAwaitingDecision {
    private final LotroGame _game;
    private final List<Action> _actions;

    public CardActionSelectionDecision(LotroGame game, int decisionId, String text, List<? extends Action> actions) {
        super(decisionId, text, AwaitingDecisionType.CARD_ACTION_CHOICE);
        _game = game;
        _actions = new LinkedList<>(actions);

        setParam("actionId", getActionIds(actions));
        setParam("cardId", getCardIds(actions));
        setParam("blueprintId", getBlueprintIdsForVirtualActions(actions));
        setParam("actionText", getActionTexts(actions));
    }

    public CardActionSelectionDecision(int id, String text, List<String> cardIds, List<String> blueprintIds, List<String> actionTexts) {
        super(id, text, AwaitingDecisionType.CARD_ACTION_CHOICE);
        _game = null;
        _actions = null;

        setParam("cardId", cardIds.toArray(new String[0]));
        setParam("blueprintId", blueprintIds.toArray(new String[0]));
        setParam("actionText", actionTexts.toArray(new String[0]));

        // Generate fake actionId array
        String[] actionIds = new String[actionTexts.size()];
        for (int i = 0; i < actionIds.length; i++) {
            actionIds[i] = String.valueOf(i);
        }
        setParam("actionId", actionIds);
    }

    /**
     * For testing, being able to inject an extra action at any point
     *
     * @param action
     */
    public void addAction(Action action) {
        _actions.add(action);
    }

    private String[] getActionIds(List<? extends Action> actions) {
        String[] result = new String[actions.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = String.valueOf(i);
        return result;
    }

    private String[] getBlueprintIdsForVirtualActions(List<? extends Action> actions) {
        String[] result = new String[actions.size()];
        for (int i = 0; i < result.length; i++) {
            Action action = actions.get(i);
            if (action.isVirtualCardAction())
                result[i] = String.valueOf(action.getActionSource().getBlueprintId());
            else
                result[i] = "inPlay";
        }
        return result;
    }

    private String[] getCardIds(List<? extends Action> actions) {
        String[] result = new String[actions.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = String.valueOf(actions.get(i).getActionAttachedToCard().getCardId());
        return result;
    }

    private String[] getActionTexts(List<? extends Action> actions) {
        String[] result = new String[actions.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = actions.get(i).getText(_game);
        return result;
    }

    protected Action getSelectedAction(String result) throws DecisionResultInvalidException {
        if (result.equals(""))
            return null;
        try {
            int actionIndex = Integer.parseInt(result);
            if (actionIndex < 0 || actionIndex >= _actions.size())
                throw new DecisionResultInvalidException();

            return _actions.get(actionIndex);
        } catch (NumberFormatException exp) {
            throw new DecisionResultInvalidException();
        }
    }

    @Override
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("type", "CardActionSelectionDecision");
        obj.put("id", getAwaitingDecisionId());
        obj.put("text", getText());

        obj.put("cardId", getDecisionParameters().get("cardId"));
        obj.put("blueprintId", getDecisionParameters().get("blueprintId"));
        obj.put("actionText", getDecisionParameters().get("actionText"));

        return obj;
    }

    public static CardActionSelectionDecision fromJson(JSONObject obj) {
        int id = obj.getInteger("id");
        String text = obj.getString("text");
        List<String> cardIds = Arrays.asList(obj.getObject("cardId", String[].class));
        List<String> blueprintIds = Arrays.asList(obj.getObject("blueprintId", String[].class));
        List<String> actionTexts = Arrays.asList(obj.getObject("actionText", String[].class));

        return new CardActionSelectionDecision(id, text, cardIds, blueprintIds, actionTexts) {
            @Override
            public void decisionMade(String result) throws DecisionResultInvalidException {
                throw new UnsupportedOperationException("Not implemented in training context");
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CardActionSelectionDecision that = (CardActionSelectionDecision) o;

        String[] actions = getDecisionParameters().get("actionText");
        String[] otherActions = that.getDecisionParameters().get("actionText");
        return Arrays.equals(actions, otherActions);
    }
}
