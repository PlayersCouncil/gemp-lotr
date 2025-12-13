package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.modifiers.*;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.results.HinderedResult;

import java.util.Collections;
import java.util.List;

public class HinderedRule {
    private final ModifiersLogic _modifiersLogic;
    private final DefaultActionsEnvironment _actionsEnvironment;

    public HinderedRule(ModifiersLogic modifiersLogic, DefaultActionsEnvironment actionsEnvironment) {
        _modifiersLogic = modifiersLogic;
        _actionsEnvironment = actionsEnvironment;
    }


    public void applyRule() {
        Filter hinderedFilter = (game, physicalCard) -> physicalCard.isFlipped();

        //Adds the hindered pseudo-keyword to all cards which are flipped
        _modifiersLogic.addAlwaysOnModifier(new AddKeywordModifier(null, hinderedFilter, null, Keyword.HINDERED));

        //Hindered characters cannot be wounded
        _modifiersLogic.addAlwaysOnModifier(new CantTakeWoundsModifier(null, null, Filters.hindered));
        _modifiersLogic.addAlwaysOnModifier(new CantTakeWoundsFromLosingSkirmishModifier(null, null, Filters.hindered));
        //Hindered characters cannot be healed
        _modifiersLogic.addAlwaysOnModifier(new CantHealModifier(null, null, Filters.hindered));
        //Hindered characters cannot be exerted
        _modifiersLogic.addAlwaysOnModifier(new CantExertWithCardModifier(null, Filters.hindered, null, Filters.any));

        Condition rbIsHindered = (LotroGame game) -> game.getGameState().isHindered(game.getGameState().getRingBearer(game.getGameState().getCurrentPlayerId()));
        //If the Ring-bearer is hindered, burdens cannot be added
        _modifiersLogic.addAlwaysOnModifier(new CantAddBurdensModifier(null, rbIsHindered, Filters.any));
        //If the Ring-bearer is hindered, burdens cannot be removed
        _modifiersLogic.addAlwaysOnModifier(new CantRemoveBurdensModifier(null, rbIsHindered, Filters.any));


        _actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult) {
                        if (effectResult.getType() == EffectResult.Type.FOR_EACH_HINDERED) {
                            var hinderedResult = (HinderedResult) effectResult;
                            final PhysicalCard hinderedCard = hinderedResult.getHinderedCard();
                            RequiredTriggerAction trigger = hinderedCard.getBlueprint().getHinderedFromPlayRequiredTrigger(game, hinderedResult);
                            if (trigger != null)
                                return Collections.singletonList(trigger);
                        }
                        return null;
                    }

                    @Override
                    public List<? extends OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult) {
                        if (effectResult.getType() == EffectResult.Type.FOR_EACH_HINDERED) {
                            var hinderedResult = (HinderedResult) effectResult;
                            final PhysicalCard hinderedCard = hinderedResult.getHinderedCard();
                            if (hinderedCard.getOwner().equals(playerId)) {
                                OptionalTriggerAction trigger = hinderedCard.getBlueprint().getHinderedFromPlayOptionalTrigger(game, hinderedResult);
                                if (trigger != null) {
                                    trigger.setVirtualCardAction(true);
                                    return Collections.singletonList(trigger);
                                }
                            }
                        }
                        return null;
                    }
                });

    }
}
