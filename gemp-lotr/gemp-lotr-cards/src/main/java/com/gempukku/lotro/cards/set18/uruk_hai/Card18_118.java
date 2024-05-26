package com.gempukku.lotro.cards.set18.uruk_hai;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.*;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Treachery & Deceit
 * Side: Shadow
 * Culture: Uruk-Hai
 * Twilight Cost: 7
 * Type: Minion • Uruk-Hai
 * Strength: 13
 * Vitality: 3
 * Site: 5
 * Game Text: Archer. Damage +1. Maneuver: Spot 6 companions to make Lurtz fierce until the regroup phase.
 * Assignment: Exert Lurtz twice to assign it to an unbound companion. The Free Peoples player may exert that companion
 * or add a burden to prevent this.
 */
public class Card18_118 extends AbstractMinion {
    public Card18_118() {
        super(7, 13, 3, 5, Race.URUK_HAI, Culture.URUK_HAI, "Lurtz", "Halfling Hunter", true);
        addKeyword(Keyword.ARCHER);
        addKeyword(Keyword.DAMAGE, 1);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(final String playerId, LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.MANEUVER, self, 0)
                && PlayConditions.canSpot(game, 6, CardType.COMPANION)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendEffect(
                    new AddUntilStartOfPhaseModifierEffect(
                            new AddKeywordModifier(self, self, Keyword.FIERCE), Phase.REGROUP));
            return Collections.singletonList(action);
        }
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.ASSIGNMENT, self, 0)
                && PlayConditions.canSelfExert(self, 2, game)) {
            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendEffect(
                    new ChooseActiveCardEffect(self, playerId, "Choose an unbound companion to assign to", Filters.unboundCompanion, Filters.assignableToSkirmishAgainst(Side.SHADOW, self)) {
                        @Override
                        protected void cardSelected(final LotroGame game, final PhysicalCard companion) {
                            action.appendEffect(
                                    new PreventableEffect(action,
                                            new AssignmentEffect(playerId, companion, self), game.getGameState().getCurrentPlayerId(),
                                            new PreventableEffect.PreventionCost() {
                                                @Override
                                                public Effect createPreventionCostForPlayer(CostToEffectAction subAction, String playerId) {
                                                    List<Effect> possibleCosts = new LinkedList<>();
                                                    possibleCosts.add(
                                                            new ExertCharactersEffect(action, self, companion));
                                                    possibleCosts.add(
                                                            new AddBurdenEffect(game.getGameState().getCurrentPlayerId(), self, 1));
                                                    return new ChoiceEffect(subAction, playerId, possibleCosts) {
                                                        @Override
                                                        public String getText(LotroGame game) {
                                                            return "Exert that companion or add a burden";
                                                        }
                                                    };
                                                }
                                            }));
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
