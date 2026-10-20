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
    public static String ThreatRuleSource = "Threat Rule";
    /** The text of the rule's entry among the required responses to the kill of a companion or ally. */
    public static final String ThreatWoundsText = "Threat wounds: remove every threat, then wound a companion for each";
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
                            // CR 4.2 "threats": the threats on the dead pile are counted, removed and that many wounds
                            // assigned.  With none there is nothing to count, remove or assign, and nothing is raised:
                            // no entry is put to the Free Peoples player to order among the kill's other required
                            // triggers, and no "threat wounds are about to be placed" window opens (Mordor Assassin
                            // 7_284, the one card that answers it, pays a threat).  Where there are threats the entry
                            // says what it does (F-0567; F-0116, "the prompt should be smarter").
                            if (game.getGameState().getThreats() > 0
                                    && Filters.acceptsAny(game, killResult.getKilledCards(), Filters.or(CardType.COMPANION, CardType.ALLY))) {
                                RequiredTriggerAction action = new RequiredTriggerAction(null);
                                action.setText(ThreatWoundsText);
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
                                    woundCharacter.setSourceText(ThreatRuleSource);
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
