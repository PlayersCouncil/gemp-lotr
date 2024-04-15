package com.gempukku.lotro.cards.set18.men;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.ChooseActiveCardsEffect;
import com.gempukku.lotro.logic.effects.ShuffleCardsFromPlayIntoDeckEffect;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.condition.LocationCondition;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Treachery & Deceit
 * Side: Shadow
 * Culture: Men
 * Twilight Cost: 5
 * Type: Minion • Man
 * Strength: 11
 * Vitality: 3
 * Site: 4
 * Game Text: While at a battleground, this minion is strength +2. Each mounted Man is strength +1. Regroup: Shuffle
 * any number of your [MEN] mounts in play into your draw deck.
 */
public class Card18_071 extends AbstractMinion {
    public Card18_071() {
        super(5, 11, 3, 4, Race.MAN, Culture.MEN, "Mûmakil Commander", "Bold and Grim", true);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new StrengthModifier(self, self, new LocationCondition(Keyword.BATTLEGROUND), 2));
        modifiers.add(
                new StrengthModifier(self, Filters.and(Race.MAN, Filters.mounted), 1));
        return modifiers;
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(final String playerId, LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.REGROUP, self, 0)) {
            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendEffect(
                    new ChooseActiveCardsEffect(self, playerId, "Choose your MEN mounts to shuffle into your draw deck", 0, Integer.MAX_VALUE, Filters.owner(playerId), Culture.MEN, PossessionClass.MOUNT) {
                        @Override
                        protected void cardsSelected(LotroGame game, Collection<PhysicalCard> cards) {
                            action.appendEffect(
                                    new ShuffleCardsFromPlayIntoDeckEffect(self, playerId, cards));
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
