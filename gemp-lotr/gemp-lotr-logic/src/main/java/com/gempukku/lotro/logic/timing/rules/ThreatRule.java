package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.effects.ChooseAndWoundCharactersEffect;
import com.gempukku.lotro.logic.effects.RemoveThreatsEffect;
import com.gempukku.lotro.logic.effects.ThreatWoundsEffect;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.results.KilledResult;

import java.util.Collections;
import java.util.List;

public class ThreatRule {
    private final DefaultActionsEnvironment _actionsEnvironment;

    public ThreatRule(DefaultActionsEnvironment actionsEnvironment) {
        _actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        _actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult) {
                        if (effectResult.getType() == EffectResult.Type.ANY_NUMBER_KILLED) {
                            KilledResult killResult = (KilledResult) effectResult;
                            if (Filters.acceptsAny(game, killResult.getKilledCards(), Filters.or(CardType.COMPANION, CardType.ALLY))) {
                                RequiredTriggerAction action = new RequiredTriggerAction(null);
                                action.appendEffect(
                                        new ThreatWoundsEffect(killResult));
                                return Collections.singletonList(action);
                            }
                        }
                        if (effectResult.getType() == EffectResult.Type.THREAT_WOUND_TRIGGER) {
                            int threats = game.getGameState().getThreats();
                            if (threats > 0) {
                                RequiredTriggerAction action = new RequiredTriggerAction(null);
                                action.setText("Threat damage assignment");
                                action.setMessage(game.getGameState().getCurrentPlayerId() + " assigns " + threats + " threat damage");
                                action.appendEffect(
                                        new RemoveThreatsEffect(null, threats));
                                for (int i = 0; i < threats; i++) {
                                    Filterable filter = CardType.COMPANION;

                                    if (game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.RING_BEARER_CANT_TAKE_THREAT_WOUNDS))
                                        filter = Filters.and(filter, Filters.not(Filters.ringBearer));

                                    ChooseAndWoundCharactersEffect woundCharacter = new ChooseAndWoundCharactersEffect(action, game.getGameState().getCurrentPlayerId(), 1, 1, filter);
                                    woundCharacter.setSourceText("Threat Rule");
                                    action.appendEffect(woundCharacter);
                                }
                                return Collections.singletonList(action);
                            }
                        }
                        return null;
                    }
                });
    }
}
