package com.gempukku.lotro.cards.set13.gondor;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.effects.AddUntilStartOfPhaseModifierEffect;
import com.gempukku.lotro.logic.effects.ChoiceEffect;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.SpotEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndRemoveCultureTokensFromCardEffect;
import com.gempukku.lotro.logic.modifiers.AbstractExtraPlayCostModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.ResistanceModifier;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Bloodlines
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 5
 * Type: Companion • Man
 * Strength: 8
 * Vitality: 5
 * Resistance: 6
 * Game Text: Knight. To play, spot 2 [GONDOR] knights or exert 2 [GONDOR] Men. Each of your [GONDOR] companions is
 * resistance +1. Maneuver: Remove a [GONDOR] token to make an unbound companion resistance +1 until the regroup phase.
 */
public class Card13_065 extends AbstractCompanion {
    public Card13_065() {
        super(5, 8, 5, 6, Culture.GONDOR, Race.MAN, null, "Elendil", "High-king of Gondor", true);
        addKeyword(Keyword.KNIGHT);
    }

    @Override
    public List<? extends AbstractExtraPlayCostModifier> getExtraCostToPlay(LotroGame game, final PhysicalCard self) {
        return Collections.singletonList(
                new AbstractExtraPlayCostModifier(self, "Extra cost to play", self) {
                    @Override
                    public boolean canPayExtraCostsToPlay(LotroGame game, PhysicalCard card) {
                        return (PlayConditions.canSpot(game, 2, Culture.GONDOR, Keyword.KNIGHT) || PlayConditions.canExert(self, game, 1, 2, Culture.GONDOR, Race.MAN));
                    }

                    @Override
                    public void appendExtraCosts(LotroGame game, CostToEffectAction action, PhysicalCard card) {
                        List<Effect> possibleCosts = new LinkedList<>();
                        possibleCosts.add(
                                new SpotEffect(2, Culture.GONDOR, Keyword.KNIGHT) {
                                    @Override
                                    public String getText(LotroGame game) {
                                        return "Spot 2 GONDOR knights";
                                    }
                                });
                        possibleCosts.add(
                                new ChooseAndExertCharactersEffect(action, card.getOwner(), 2, 2, Culture.GONDOR, Race.MAN) {
                                    @Override
                                    public String getText(LotroGame game) {
                                        return "Exert 2 GONDOR Men";
                                    }
                                });
                        action.appendCost(
                                new ChoiceEffect(action, card.getOwner(), possibleCosts));

                    }
                });
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
return Collections.singletonList(new ResistanceModifier(self, Filters.and(Filters.owner(self.getOwner()), Culture.GONDOR, CardType.COMPANION), 1));
}

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.MANEUVER, self)
                && PlayConditions.canRemoveTokens(game, Token.GONDOR, 1, Filters.any)) {
            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new ChooseAndRemoveCultureTokensFromCardEffect(self, playerId, Token.GONDOR, 1, Filters.any));
            action.appendEffect(
                    new ChooseActiveCardEffect(self, playerId, "Choose an unbound companion", Filters.unboundCompanion) {
                        @Override
                        protected void cardSelected(LotroGame game, PhysicalCard card) {
                            action.appendEffect(
                                    new AddUntilStartOfPhaseModifierEffect(
                                            new ResistanceModifier(self, card, 1), Phase.REGROUP));
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
