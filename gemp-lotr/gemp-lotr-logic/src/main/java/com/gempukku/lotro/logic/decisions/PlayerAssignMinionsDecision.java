package com.gempukku.lotro.logic.decisions;

import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.game.PhysicalCard;

import java.util.*;

public abstract class PlayerAssignMinionsDecision extends AbstractAwaitingDecision {
    private final List<PhysicalCard> _freeCharacters;
    private final List<PhysicalCard> _minions;

    public PlayerAssignMinionsDecision(int id, String text, Collection<PhysicalCard> freeCharacters, Collection<PhysicalCard> minions) {
        super(id, text, AwaitingDecisionType.ASSIGN_MINIONS);
        _freeCharacters = new LinkedList<>(freeCharacters);
        _minions = new LinkedList<>(minions);
        setParam("freeCharacters", getCardIds(_freeCharacters));
        setParam("minions", getCardIds(_minions));
    }

    public PlayerAssignMinionsDecision(int id, String text, List<String> freeCharacterIds, List<String> minionIds) {
        super(id, text, AwaitingDecisionType.ASSIGN_MINIONS);
        this._freeCharacters = null; // not used in training context
        this._minions = null;
        setParam("freeCharacters", freeCharacterIds.toArray(new String[0]));
        setParam("minions", minionIds.toArray(new String[0]));
    }

    protected Map<PhysicalCard, Set<PhysicalCard>> getAssignmentsBasedOnResponse(String response) throws DecisionResultInvalidException {
        Map<PhysicalCard, Set<PhysicalCard>> assignments = new HashMap<>();
        if (response.equals(""))
            return assignments;

        Set<PhysicalCard> assignedMinions = new HashSet<>();

        try {
            String[] groups = response.split(",");
            for (String group : groups) {
                String[] cardIds = group.split(" ");
                PhysicalCard freeCard = getCardId(_freeCharacters, Integer.parseInt(cardIds[0]));
                if (assignments.containsKey(freeCard))
                    throw new DecisionResultInvalidException();

                Set<PhysicalCard> minions = new HashSet<>();
                for (int i = 1; i < cardIds.length; i++) {
                    PhysicalCard minion = getCardId(_minions, Integer.parseInt(cardIds[i]));
                    if (assignedMinions.contains(minion))
                        throw new DecisionResultInvalidException();
                    minions.add(minion);
                    assignedMinions.add(minion);
                }

                assignments.put(freeCard, minions);
            }
        } catch (NumberFormatException exp) {
            throw new DecisionResultInvalidException();
        }

        return assignments;
    }

    private PhysicalCard getCardId(List<PhysicalCard> physicalCards, int cardId) throws DecisionResultInvalidException {
        for (PhysicalCard physicalCard : physicalCards) {
            if (physicalCard.getCardId() == cardId)
                return physicalCard;
        }
        throw new DecisionResultInvalidException();
    }

    private String[] getCardIds(List<PhysicalCard> cards) {
        String[] result = new String[cards.size()];
        for (int i = 0; i < cards.size(); i++)
            result[i] = String.valueOf(cards.get(i).getCardId());
        return result;
    }

    @Override
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("type", "PlayerAssignMinionsDecision");
        obj.put("id", getAwaitingDecisionId());
        obj.put("text", getText());
        obj.put("freeCharacters", getDecisionParameters().get("freeCharacters"));
        obj.put("minions", getDecisionParameters().get("minions"));
        return obj;
    }

    public static PlayerAssignMinionsDecision fromJson(JSONObject obj) {
        return new PlayerAssignMinionsDecision(
                obj.getInteger("id"),
                obj.getString("text"),
                Arrays.asList(obj.getObject("freeCharacters", String[].class)),
                Arrays.asList(obj.getObject("minions", String[].class))) {
            @Override
            public void decisionMade(String result) throws DecisionResultInvalidException {
                throw new UnsupportedOperationException("Not implemented in training context");
            }
        };
    }
}
