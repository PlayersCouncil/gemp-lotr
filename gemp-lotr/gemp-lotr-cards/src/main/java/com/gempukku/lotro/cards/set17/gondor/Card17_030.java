package com.gempukku.lotro.cards.set17.gondor;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.effects.AddUntilStartOfPhaseModifierEffect;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.modifiers.CantDiscardFromPlayModifier;
import com.gempukku.lotro.logic.modifiers.MinionSiteNumberModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.SpotCondition;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Rise of Saruman
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 3
 * Type: Companion • Man
 * Strength: 6
 * Vitality: 3
 * Resistance: 6
 * Game Text: Ranger. Hunter 1. While you can spot a roaming minion, your [GONDOR] conditions cannot be discarded from
 * play by Shadow cards. Maneuver: Exert 2 rangers to make a minion site number +2 until the regroup phase.
 */
public class Card17_030 extends AbstractCompanion {
    public Card17_030() {
        super(3, 6, 3, 6, Culture.GONDOR, Race.MAN, null, "Madril", "Loyal Lieutenant", true);
        addKeyword(Keyword.RANGER);
        addKeyword(Keyword.HUNTER, 1);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(new CantDiscardFromPlayModifier(self, "Cannot be discarded from play by Shadow cards",
                new SpotCondition(CardType.MINION, Keyword.ROAMING), null,
                Filters.and(Filters.owner(self.getOwner()), Culture.GONDOR, CardType.CONDITION), Side.SHADOW));
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.MANEUVER, self)
                && PlayConditions.canExert(self, game, 1, 2, Keyword.RANGER)) {
            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new ChooseAndExertCharactersEffect(action, playerId, 2, 2, Keyword.RANGER));
            action.appendEffect(
                    new ChooseActiveCardEffect(self, playerId, "Choose a minion", CardType.MINION) {
                        @Override
                        protected void cardSelected(LotroGame game, PhysicalCard card) {
                            action.appendEffect(
                                    new AddUntilStartOfPhaseModifierEffect(
                                            new MinionSiteNumberModifier(self, card, null, 2), Phase.REGROUP));
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
