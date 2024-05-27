package com.gempukku.lotro.logic.decisions;

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
}
