package com.gempukku.lotro.cards.set10.dwarven;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.PlayUtils;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.actions.ResolveSkirmishDamageAction;
import com.gempukku.lotro.logic.cardtype.AbstractResponseEvent;
import com.gempukku.lotro.logic.effects.ChooseAndWoundCharactersEffect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;

import java.util.Collections;
import java.util.List;

/**
 * Set: Mount Doom
 * Side: Free
 * Culture: Dwarven
 * Twilight Cost: 0
 * Type: Event • Response
 * Game Text: If a Dwarf kills a minion in a skirmish and that minion did not take all wounds caused by that Dwarf's
 * damage bonus, assign those remaining wounds to minions not assigned to a skirmish.
 */
public class Card10_003 extends AbstractResponseEvent {
    public Card10_003() {
        super(Side.FREE_PEOPLE, 0, Culture.DWARVEN, "More Yet to Come");
    }

    @Override
    public List<PlayEventAction> getPlayResponseEventAfterActions(final String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.forEachKilledInASkirmish(game, effectResult, Race.DWARF, CardType.MINION)
                && PlayUtils.checkPlayRequirements(game, self, Filters.any, 0, 0, false, false)) {
            final ResolveSkirmishDamageAction resolveSkirmishDamageAction = game.getActionsEnvironment().findTopmostActionOfType(ResolveSkirmishDamageAction.class);
            if (resolveSkirmishDamageAction != null
                    && resolveSkirmishDamageAction.getRemainingDamage() > 0) {
                final PlayEventAction action = new PlayEventAction(self);
                action.appendEffect(
                        new UnrespondableEffect() {
                            @Override
                            protected void doPlayEffect(LotroGame game) {
                                int remainingDamage = resolveSkirmishDamageAction.getRemainingDamage();
                                resolveSkirmishDamageAction.consumeRemainingDamage();
                                for (int i = 0; i < remainingDamage; i++)
                                    action.appendEffect(
                                            new ChooseAndWoundCharactersEffect(action, playerId, 1, 1, CardType.MINION, Filters.notAssignedToSkirmish));
                            }
                        });
                return Collections.singletonList(action);
            }
        }
        return null;
    }
}
