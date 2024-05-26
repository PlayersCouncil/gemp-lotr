package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.effects.ChooseAndHealCharactersEffect;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.ModifiersLogic;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.List;

public class SanctuaryRule {
    private final DefaultActionsEnvironment _actionsEnvironment;
    private final ModifiersLogic _modifiersLogic;

    public SanctuaryRule(DefaultActionsEnvironment actionsEnvironment, ModifiersLogic modifiersLogic) {
        _actionsEnvironment = actionsEnvironment;
        _modifiersLogic = modifiersLogic;
    }

    public void applyRule() {
        _modifiersLogic.addAlwaysOnModifier(
                new AddKeywordModifier(null, Filters.and(CardType.SITE, Filters.or(Filters.siteNumber(3), Filters.siteNumber(6))), Keyword.SANCTUARY));

        _actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult) {
                        if (effectResult.getType() == EffectResult.Type.START_OF_TURN
                                && game.getModifiersQuerying().hasKeyword(game, game.getGameState().getCurrentSite(), Keyword.SANCTUARY)) {
                            RequiredTriggerAction action = new RequiredTriggerAction(game.getGameState().getCurrentSite());
                            action.setText("Sanctuary healing");
                            int healCount = 5 + game.getModifiersQuerying().getSanctuaryHealModifier(game);
                            game.getGameState().sendMessage("Sanctuary healing. " + game.getGameState().getCurrentPlayerId() + " has " + healCount + " heals available.");
                            for (int i = 0; i < healCount; i++) {
                                final int remainingHeals = healCount - i;
                                ChooseAndHealCharactersEffect healEffect = new ChooseAndHealCharactersEffect(action, game.getGameState().getCurrentPlayerId(), 0, 1, CardType.COMPANION);
                                healEffect.setChoiceText("Sanctuary healing - Choose companion to heal - remaining heals: " + remainingHeals);
                                action.appendEffect(healEffect);
                            }
                            return Collections.singletonList(action);
                        }
                        return null;
                    }
                });
    }
}
