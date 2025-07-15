package com.gempukku.lotro.logic.decisions;

import com.gempukku.util.JsonSerializable;

import java.util.Map;

public interface AwaitingDecision extends JsonSerializable {
    int getAwaitingDecisionId();

    String getText();

    AwaitingDecisionType getDecisionType();

    Map<String, String[]> getDecisionParameters();

    void decisionMade(String result) throws DecisionResultInvalidException;
}
