package com.gempukku.lotro.logic.decisions;

import com.alibaba.fastjson2.JSONObject;

public abstract class MultipleChoiceAwaitingDecision extends AbstractAwaitingDecision {
    private final String[] _possibleResults;

    public MultipleChoiceAwaitingDecision(int id, String text, String[] possibleResults) {
        super(id, text, AwaitingDecisionType.MULTIPLE_CHOICE);
        _possibleResults = possibleResults;
        setParam("results", _possibleResults);
    }

    protected abstract void validDecisionMade(int index, String result);

    @Override
    public final void decisionMade(String result) throws DecisionResultInvalidException {
        if (result == null)
            throw new DecisionResultInvalidException();

        int index;
        try {
            index = Integer.parseInt(result);
        } catch (NumberFormatException exp) {
            throw new DecisionResultInvalidException("Unknown response number");
        }
        validDecisionMade(index, _possibleResults[index]);
    }

    @Override
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("type", "MultipleChoiceAwaitingDecision");
        obj.put("id", getAwaitingDecisionId());
        obj.put("text", getText());
        obj.put("results", getDecisionParameters().get("results"));
        return obj;
    }

    public static MultipleChoiceAwaitingDecision fromJson(JSONObject obj) {
        return new MultipleChoiceAwaitingDecision(obj.getInteger("id"), obj.getString("text"), obj.getObject("results", String[].class)) {
            @Override
            protected void validDecisionMade(int index, String result) {
                throw new UnsupportedOperationException("Not implemented in training context");
            }
        };
    }
}
