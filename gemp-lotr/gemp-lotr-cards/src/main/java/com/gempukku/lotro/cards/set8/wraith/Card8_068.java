package com.gempukku.lotro.cards.set8.wraith;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.AddUntilEndOfPhaseActionProxyEffect;
import com.gempukku.lotro.logic.effects.AddUntilEndOfPhaseModifierEffect;
import com.gempukku.lotro.logic.effects.PutCardsFromHandBeneathDrawDeckEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Set: Siege of Gondor
 * Side: Shadow
 * Culture: Wraith
 * Twilight Cost: 3
 * Type: Event • Skirmish
 * Game Text: Exert a Nazgul to make him strength +2 and damage +1. If the character he is skirmishing is killed,
 * the Free Peoples player must place his or her hand beneath his or her draw deck.
 */
public class Card8_068 extends AbstractEvent {
    public Card8_068() {
        super(Side.SHADOW, 3, Culture.WRAITH, "Beyond All Darkness", Phase.SKIRMISH);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canExert(self, game, Race.NAZGUL);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, final LotroGame game, final PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendCost(
                new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Race.NAZGUL) {
                    @Override
                    protected void forEachCardExertedCallback(final PhysicalCard character) {
                        action.appendEffect(
                                new AddUntilEndOfPhaseModifierEffect(
                                        new StrengthModifier(self, character, 2)));
                        action.appendEffect(
                                new AddUntilEndOfPhaseModifierEffect(
                                        new KeywordModifier(self, character, Keyword.DAMAGE, 1)));

                        final Collection<PhysicalCard> againstNazgulCollection = Filters.filterActive(game, Filters.inSkirmishAgainst(character));
                        if (againstNazgulCollection.size() > 0) {
                            final PhysicalCard againstNazgul = againstNazgulCollection.iterator().next();
                            action.appendEffect(
                                    new AddUntilEndOfPhaseActionProxyEffect(
                                            new AbstractActionProxy() {
                                                @Override
                                                public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult) {
                                                    if (TriggerConditions.forEachKilled(game, effectResult, againstNazgul)) {
                                                        RequiredTriggerAction action = new RequiredTriggerAction(self);
                                                        action.appendEffect(
                                                                new PutCardsFromHandBeneathDrawDeckEffect(action, game.getGameState().getCurrentPlayerId(), false));
                                                        return Collections.singletonList(action);
                                                    }
                                                    return null;
                                                }
                                            }));
                        }
                    }
                });
        return action;
    }
}
