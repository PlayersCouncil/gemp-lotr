package com.gempukku.lotro.cards.set11.orc;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.PreventableEffect;
import com.gempukku.lotro.logic.effects.SelfExertEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndAssignCharacterToMinionEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndDiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Shadows
 * Side: Shadow
 * Culture: Orc
 * Twilight Cost: 6
 * Type: Minion • Troll
 * Strength: 13
 * Vitality: 3
 * Site: 5
 * Game Text: Damage +1. Fierce. To play, spot an [ORC] minion. Assignment: Exert this minion to assign it to
 * a companion bearing an artifact. The Free Peoples player may discard an artifact from play to prevent this.
 */
public class Card11_108 extends AbstractMinion {
    public Card11_108() {
        super(6, 13, 3, 5, Race.TROLL, Culture.ORC, "Beastly Olog-hai");
        addKeyword(Keyword.DAMAGE, 1);
        addKeyword(Keyword.FIERCE);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Culture.ORC, CardType.MINION);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.ASSIGNMENT, self, 0)
                && PlayConditions.canSelfExert(self, game)) {
            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendEffect(
                    new PreventableEffect(action,
                            new ChooseAndAssignCharacterToMinionEffect(action, playerId, self, CardType.COMPANION, Filters.hasAttached(CardType.ARTIFACT)) {
                                @Override
                                public String getText(LotroGame game) {
                                    return "Assign the minion to a companion bearing an artifact";
                                }
                            }, game.getGameState().getCurrentPlayerId(),
                            new PreventableEffect.PreventionCost() {
                                @Override
                                public Effect createPreventionCostForPlayer(CostToEffectAction subAction, String playerId) {
                                    return new ChooseAndDiscardCardsFromPlayEffect(subAction, playerId, 1, 1, CardType.ARTIFACT) {
                                        @Override
                                        public String getText(LotroGame game) {
                                            return "Discard an artifact from play";
                                        }
                                    };
                                }
                            }
                    ));
            return Collections.singletonList(action);
        }
        return null;
    }
}
