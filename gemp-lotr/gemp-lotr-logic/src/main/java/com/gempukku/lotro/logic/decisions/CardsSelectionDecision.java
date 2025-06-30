package com.gempukku.lotro.logic.decisions;

import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.game.PhysicalCard;

import java.util.*;

public abstract class CardsSelectionDecision extends AbstractAwaitingDecision {
    private final List<? extends PhysicalCard> _physicalCards;
    private final int _minimum;
    private final int _maximum;

    public CardsSelectionDecision(int id, String text, Collection<? extends PhysicalCard> physicalCards, int minimum, int maximum) {
        super(id, text, AwaitingDecisionType.CARD_SELECTION);
        _physicalCards = new LinkedList<PhysicalCard>(physicalCards);
        _minimum = minimum;
        _maximum = maximum;
        setParam("min", String.valueOf(minimum));
        setParam("max", String.valueOf(maximum));
        setParam("cardId", getCardIds(_physicalCards));
    }

    public CardsSelectionDecision(int id, String text, List<String> physicalCardIds, int minimum, int maximum) {
        super(id, text, AwaitingDecisionType.CARD_SELECTION);
        _physicalCards = null;
        _minimum = minimum;
        _maximum = maximum;
        setParam("min", String.valueOf(minimum));
        setParam("max", String.valueOf(maximum));
        setParam("cardId", physicalCardIds.toArray(new String[0]));
    }

    private String[] getCardIds(List<? extends PhysicalCard> physicalCards) {
        String[] result = new String[physicalCards.size()];
        for (int i = 0; i < physicalCards.size(); i++)
            result[i] = String.valueOf(physicalCards.get(i).getCardId());
        return result;
    }

    protected Set<PhysicalCard> getSelectedCardsByResponse(String response) throws DecisionResultInvalidException {
        if (response.equals("")) {
            if (_minimum == 0)
                return Collections.emptySet();
            else
                throw new DecisionResultInvalidException();
        }
        String[] cardIds = response.split(",");
        if (cardIds.length < _minimum || cardIds.length > _maximum)
            throw new DecisionResultInvalidException();

        Set<PhysicalCard> result = new HashSet<>();
        try {
            for (String cardId : cardIds) {
                PhysicalCard card = getSelectedCardById(Integer.parseInt(cardId));
                if (result.contains(card))
                    throw new DecisionResultInvalidException();
                result.add(card);
            }
        } catch (NumberFormatException e) {
            throw new DecisionResultInvalidException();
        }

        return result;
    }

    private PhysicalCard getSelectedCardById(int cardId) throws DecisionResultInvalidException {
        for (PhysicalCard physicalCard : _physicalCards)
            if (physicalCard.getCardId() == cardId)
                return physicalCard;

        throw new DecisionResultInvalidException();
    }

    @Override
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("type", "CardsSelectionDecision");
        obj.put("id", getAwaitingDecisionId());
        obj.put("text", getText());
        obj.put("min", _minimum);
        obj.put("max", _maximum);
        if (_physicalCards != null) {
            obj.put("physicalCards", getCardIds(_physicalCards));
        } else {
            obj.put("physicalCards", getDecisionParameters().get("cardId"));
        }
        return obj;
    }

    public static CardsSelectionDecision fromJson(JSONObject obj) {
        return new CardsSelectionDecision(obj.getInteger("id"), obj.getString("text"), Arrays.asList(obj.getObject("physicalCards", String[].class)), obj.getInteger("min"), obj.getInteger("max")) {
            @Override
            public void decisionMade(String result) throws DecisionResultInvalidException {
                throw new UnsupportedOperationException("Not implemented in training context");
            }
        };
    }
}
