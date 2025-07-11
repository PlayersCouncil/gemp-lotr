package com.gempukku.lotro.bots.rl.semanticaction;

import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CardActionChoiceAction implements SemanticAction {
    private final String actionText;
    private final String sourceBlueprint;
    private int woundsOnSource;
    private String holderBlueprint;
    private final List<String> notChosenActions = new ArrayList<>();
    private final List<String> notChosenBlueprints = new ArrayList<>();
    private final String skirmishingFpBlueprintId;
    private final List<String> skirmishingMinionBlueprintIds = new ArrayList<>();
    private final boolean sourceInSkirmish;

    public CardActionChoiceAction(String answer, AwaitingDecision decision, GameState gameState) {
        if (answer == null || answer.isEmpty()) {
            actionText = null;
            sourceBlueprint = null;
            woundsOnSource = 0;
            holderBlueprint = null;
            skirmishingFpBlueprintId = null;
            sourceInSkirmish = false;
            String[] actionTexts = decision.getDecisionParameters().get("actionText");
            for (int i = 0; i < actionTexts.length; i++) {
                notChosenActions.add(actionTexts[i]);
                notChosenBlueprints.add(gameState.getBlueprintId(Integer.parseInt(decision.getDecisionParameters().get("cardId")[i])));
            }
        } else {
            String[] actionTexts = decision.getDecisionParameters().get("actionText");
            actionText = actionTexts[Integer.parseInt(answer)];
            sourceBlueprint = gameState.getBlueprintId(Integer.parseInt(decision.getDecisionParameters().get("cardId")[Integer.parseInt(answer)]));
            woundsOnSource = 0;
            holderBlueprint = null;
            for (PhysicalCard physicalCard : gameState.getInPlay()) {
                if (physicalCard.getCardId() == Integer.parseInt(decision.getDecisionParameters().get("cardId")[Integer.parseInt(answer)])) {
                    woundsOnSource = gameState.getWounds(physicalCard);
                    if (physicalCard.getAttachedTo() != null) {
                        holderBlueprint = physicalCard.getAttachedTo().getBlueprintId();
                        woundsOnSource = gameState.getWounds(physicalCard.getAttachedTo());
                    }
                    break;
                }
            }
            for (int i = 0; i < actionTexts.length; i++) {
                if (!actionText.equals(actionTexts[i])) {
                    notChosenActions.add(actionTexts[i]);
                    notChosenBlueprints.add(gameState.getBlueprintId(Integer.parseInt(decision.getDecisionParameters().get("cardId")[i])));
                }
            }

            if (gameState.getSkirmish() != null && gameState.getSkirmish().getFellowshipCharacter() != null && !gameState.getSkirmish().getShadowCharacters().isEmpty()) {
                int sourceId = Integer.parseInt(decision.getDecisionParameters().get("cardId")[Integer.parseInt(answer)]);
                boolean tmpSourceInSkirmish = gameState.getSkirmish().getFellowshipCharacter().getCardId() == sourceId;

                skirmishingFpBlueprintId = gameState.getSkirmish().getFellowshipCharacter().getBlueprintId();
                for (PhysicalCard shadowCharacter : gameState.getSkirmish().getShadowCharacters()) {
                    skirmishingMinionBlueprintIds.add(shadowCharacter.getBlueprintId());
                    if (shadowCharacter.getCardId() == sourceId) {
                        tmpSourceInSkirmish = true;
                    }
                }

                sourceInSkirmish = tmpSourceInSkirmish;
            } else {
                skirmishingFpBlueprintId = null;
                sourceInSkirmish = false;
            }
        }
    }

    public CardActionChoiceAction(String actionText, String sourceBlueprint, int woundsOnSource, String holderBlueprint,
                                  String[] notChosenActions, String[] notChosenBlueprintIds,
                                  String skirmishingFpBlueprintId, String[] skirmishingMinionBlueprintIds,
                                  boolean sourceInSkirmish) {
        this.actionText = actionText;
        this.sourceBlueprint = sourceBlueprint;
        this.woundsOnSource = woundsOnSource;
        this.holderBlueprint = holderBlueprint;
        this.notChosenActions.addAll(Arrays.asList(notChosenActions));
        this.notChosenBlueprints.addAll(Arrays.asList(notChosenBlueprintIds));
        this.skirmishingFpBlueprintId = skirmishingFpBlueprintId;
        this.skirmishingMinionBlueprintIds.addAll(Arrays.asList(skirmishingMinionBlueprintIds));
        this.sourceInSkirmish = sourceInSkirmish;
    }

    public String getActionText() {
        return actionText;
    }

    public String getSourceBlueprint() {
        return sourceBlueprint;
    }

    public List<String> getNotChosenActions() {
        return notChosenActions;
    }

    public List<String> getNotChosenBlueprints() {
        return notChosenBlueprints;
    }

    public int getWoundsOnSource() {
        return woundsOnSource;
    }

    public String getHolderBlueprint() {
        return holderBlueprint;
    }

    public String getSkirmishingFpBlueprintId() {
        return skirmishingFpBlueprintId;
    }

    public List<String> getSkirmishingMinionBlueprintIds() {
        return skirmishingMinionBlueprintIds;
    }

    public boolean isSourceInSkirmish() {
        return sourceInSkirmish;
    }

    @Override
    public String toDecisionString(AwaitingDecision decision, GameState gameState) {
        if (decision.getDecisionType() != AwaitingDecisionType.CARD_ACTION_CHOICE) {
            throw new IllegalArgumentException("Wrong decision type.");
        }

        if (actionText == null) {
            return "";
        }

        String[] actionTexts = decision.getDecisionParameters().get("actionText");
        for (int i = 0; i < actionTexts.length; i++) {
            if (actionTexts[i].equals(actionText)) {
                return String.valueOf(i);
            }
        }
        throw new IllegalArgumentException("Option not found.");
    }

    @Override
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("type", "CardActionChoiceAction");
        obj.put("actionText", actionText);
        obj.put("sourceBlueprint", sourceBlueprint);
        obj.put("woundsOnSource", woundsOnSource);
        obj.put("holderBlueprint", holderBlueprint);
        obj.put("notChosenActions", notChosenActions.toArray(new String[0]));
        obj.put("notChosenBlueprints", notChosenBlueprints.toArray(new String[0]));
        obj.put("skirmishingFpBlueprintId", skirmishingFpBlueprintId);
        obj.put("skirmishingMinionBlueprintIds", skirmishingMinionBlueprintIds.toArray(new String[0]));
        obj.put("sourceInSkirmish", sourceInSkirmish);
        return obj;
    }

    public static CardActionChoiceAction fromJson(JSONObject obj) {
        String actionText = obj.getString("actionText");
        String sourceBlueprint = obj.getString("sourceBlueprint");
        int woundsOnSource = obj.getIntValue("woundsOnSource");
        String holderBlueprint = obj.getString("holderBlueprint");
        String[] notChosenActions = obj.getObject("notChosenActions", String[].class);
        String[] notChosenBlueprints = obj.getObject("notChosenBlueprints", String[].class);
        String skirmishingFpBlueprintId = obj.getString("skirmishingFpBlueprintId");
        String[] skirmishingMinionBlueprintIds = obj.getObject("skirmishingMinionBlueprintIds", String[].class);
        boolean sourceInSkirmish = obj.getBoolean("sourceInSkirmish");
        return new CardActionChoiceAction(actionText, sourceBlueprint, woundsOnSource, holderBlueprint, notChosenActions,
                notChosenBlueprints, skirmishingFpBlueprintId, skirmishingMinionBlueprintIds, sourceInSkirmish);
    }
}
