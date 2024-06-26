package com.gempukku.lotro.cards.set2.moria;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.DiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.effects.SelfDiscardEffect;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.MayNotBearModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Mines of Moria
 * Side: Shadow
 * Culture: Moria
 * Twilight Cost: 4
 * Type: Minion • Creature
 * Strength: 11
 * Vitality: 4
 * Site: 4
 * Game Text: Damage +1. While you can spot Watcher in the Water, discard all other minions (except tentacles). Each
 * tentacle is strength +2 and damage +1. This minion may not bear possessions and is discarded if not at a marsh.
 */
public class Card2_073 extends AbstractMinion {
    public Card2_073() {
        super(4, 11, 4, 4, Race.CREATURE, Culture.MORIA, "Watcher in the Water", "Keeper of Westgate", true);
        addKeyword(Keyword.DAMAGE);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new StrengthModifier(self, Keyword.TENTACLE, 2));
        modifiers.add(
                new KeywordModifier(self, Keyword.TENTACLE, Keyword.DAMAGE));
        modifiers.add(
                new MayNotBearModifier(self, self, CardType.POSSESSION));
        return modifiers;
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        List<RequiredTriggerAction> actions = new LinkedList<>();
        if (Filters.canSpot(game, CardType.MINION, Filters.not(self), Filters.not(Keyword.TENTACLE))) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new DiscardCardsFromPlayEffect(self.getOwner(), self, Filters.and(CardType.MINION, Filters.not(self), Filters.not(Keyword.TENTACLE))));
            actions.add(action);
        }
        if (!game.getModifiersQuerying().hasKeyword(game, game.getGameState().getCurrentSite(), Keyword.MARSH)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new SelfDiscardEffect(self));
            actions.add(action);
        }
        return actions;
    }
}
