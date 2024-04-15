package com.gempukku.lotro.cards.set4.gondor;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.ChooseAndWoundCharactersEffect;
import com.gempukku.lotro.logic.effects.SelfDiscardEffect;
import com.gempukku.lotro.logic.modifiers.Condition;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Two Towers
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 0
 * Type: Condition
 * Game Text: Plays to your support area. While the fellowship is at site 6T, each roaming minion skirmishing
 * a Ring-bound companion is strength -2. Skirmish: Spot a [GONDOR] Man and discard this condition to wound
 * a roaming minion.
 */
public class Card4_125 extends AbstractPermanent {
    public Card4_125() {
        super(Side.FREE_PEOPLE, 0, CardType.CONDITION, Culture.GONDOR, "Henneth Annûn", null, true);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new StrengthModifier(self,
                        Filters.and(
                                CardType.MINION,
                                Keyword.ROAMING,
                                Filters.inSkirmishAgainst(
                                        Filters.and(
                                                CardType.COMPANION,
                                                Keyword.RING_BOUND
                                        )
                                )
                        ),
                        new Condition() {
                            @Override
                            public boolean isFullfilled(LotroGame game) {
                                return game.getGameState().getCurrentSiteNumber() == 6 && game.getGameState().getCurrentSiteBlock() == SitesBlock.TWO_TOWERS;
                            }
                        }, -2));
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.SKIRMISH, self)
                && Filters.canSpot(game, Culture.GONDOR, Race.MAN)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfDiscardEffect(self));
            action.appendEffect(
                    new ChooseAndWoundCharactersEffect(action, playerId, 1, 1, CardType.MINION, Keyword.ROAMING));
            return Collections.singletonList(action);
        }
        return null;
    }
}
