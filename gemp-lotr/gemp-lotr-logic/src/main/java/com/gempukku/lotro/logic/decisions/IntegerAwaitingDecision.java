package com.gempukku.lotro.logic.decisions;

import com.alibaba.fastjson2.JSONObject;

public abstract class IntegerAwaitingDecision extends AbstractAwaitingDecision {
    private final Integer _min;
    private final Integer _max;

    public IntegerAwaitingDecision(int id, String text, Integer min) {
        this(id, text, min, null);
    }

    public IntegerAwaitingDecision(int id, String text, Integer min, Integer max) {
        this(id, text, min, max, null);
    }

    public IntegerAwaitingDecision(int id, String text, Integer min, Integer max, Integer defaultValue) {
        super(id, text, AwaitingDecisionType.INTEGER);
        _min = min;
        _max = max;
        if (min != null)
            setParam("min", min.toString());
        if (max != null)
            setParam("max", max.toString());
        if (defaultValue != null)
            setParam("defaultValue", defaultValue.toString());
    }

    protected int getValidatedResult(String result) throws DecisionResultInvalidException {
        try {
            int value = Integer.parseInt(result);
            if (_min != null && _min > value)
                throw new DecisionResultInvalidException();
            if (_max != null && _max < value)
                throw new DecisionResultInvalidException();

            return value;
        } catch (NumberFormatException exp) {
            throw new DecisionResultInvalidException();
        }
    }

    @Override
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("type", "IntegerAwaitingDecision");
        obj.put("id", getAwaitingDecisionId());
        obj.put("text", getText());
        if (_min != null)
            obj.put("min", _min);
        if (_max != null)
            obj.put("max", _max);
        return obj;
    }

    public static IntegerAwaitingDecision fromJson(JSONObject obj) {
        return new IntegerAwaitingDecision(obj.getInteger("id"), obj.getString("text"), obj.getInteger("min"), obj.getInteger("max")) {
            @Override
            public void decisionMade(String result) throws DecisionResultInvalidException {
                throw new UnsupportedOperationException("Not implemented in training context");
            }
        };
    }
}
