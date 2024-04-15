package com.gempukku.lotro.cards.set4.rohan;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractAlly;
import com.gempukku.lotro.logic.effects.AddUntilEndOfPhaseModifierEffect;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.SelfDiscardEffect;
import com.gempukku.lotro.logic.effects.SelfExertEffect;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Two Towers
 * Side: Free
 * Culture: Rohan
 * Twilight Cost: 1
 * Type: Ally • Home 4T • Man
 * Strength: 4
 * Vitality: 2
 * Site: 4T
 * Game Text: Villager. Discard Hlafwine if an opponent controls his home site. Skirmish: Exert Hlafwine to make
 * a [ROHAN] Man strength +2.
 */
public class Card4_281 extends AbstractAlly {
    public Card4_281() {
        super(1, SitesBlock.TWO_TOWERS, 4, 4, 2, Race.MAN, Culture.ROHAN, "Hlafwine", "Village Farmhand", true);
        addKeyword(Keyword.VILLAGER);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.SKIRMISH, self)
                && PlayConditions.canSelfExert(self, game)) {
            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendEffect(
                    new ChooseActiveCardEffect(self, playerId, "Choose ROHAN Man", Culture.ROHAN, Race.MAN) {
                        @Override
                        protected void cardSelected(LotroGame game, PhysicalCard card) {
                            action.insertEffect(
                                    new AddUntilEndOfPhaseModifierEffect(
                                            new StrengthModifier(self, Filters.sameCard(card), 2)));
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (Filters.countActive(game,
                Filters.siteControlledByOtherPlayer(self.getOwner()),
                Filters.siteNumber(4),
                Filters.siteBlock(SitesBlock.TWO_TOWERS)) > 0) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new SelfDiscardEffect(self));
            return Collections.singletonList(action);
        }
        return null;
    }
}
