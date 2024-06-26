package com.gempukku.lotro.cards.set5.gandalf;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.effects.*;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.modifiers.UnhastyCompanionParticipatesInSkirmishedModifier;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Battle of Helm's Deep
 * Side: Free
 * Culture: Gandalf
 * Twilight Cost: 4
 * Type: Companion • Ent
 * Strength: 10
 * Vitality: 4
 * Resistance: 6
 * Game Text: Unhasty. Assignment: Exert an unbound Hobbit or discard 2 cards from hand to allow Lidenroot to skirmish.
 * Skirmish: Exert Lidenroot to wound an [ISENGARD] minion he is skirmishing.
 */
public class Card5_019 extends AbstractCompanion {
    public Card5_019() {
        super(4, 10, 4, 6, Culture.GANDALF, Race.ENT, null, "Lindenroot", "Elder Shepherd", true);
        addKeyword(Keyword.UNHASTY);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.ASSIGNMENT, self)
                && (
                PlayConditions.canExert(self, game, Race.HOBBIT, Filters.unboundCompanion)
                        || game.getGameState().getHand(playerId).size() >= 2)) {
            ActivateCardAction action = new ActivateCardAction(self);
            List<Effect> possibleCosts = new LinkedList<>();
            possibleCosts.add(
                    new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Race.HOBBIT, Filters.unboundCompanion) {
                        @Override
                        public String getText(LotroGame game) {
                            return "Exert an unbound Hobbit";
                        }
                    });
            possibleCosts.add(
                    new ChooseAndDiscardCardsFromHandEffect(action, playerId, false, 2) {
                        @Override
                        public String getText(LotroGame game) {
                            return "Discard 2 cards from hand";
                        }
                    });
            action.appendCost(
                    new ChoiceEffect(action, playerId, possibleCosts));
            action.appendEffect(
                    new AddUntilEndOfPhaseModifierEffect(
                            new UnhastyCompanionParticipatesInSkirmishedModifier(self, self)));
            return Collections.singletonList(action);
        }
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.SKIRMISH, self)
                && PlayConditions.canSelfExert(self, game)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendEffect(
                    new ChooseAndWoundCharactersEffect(action, playerId, 1, 1, Culture.ISENGARD, CardType.MINION, Filters.inSkirmishAgainst(self)));
            return Collections.singletonList(action);
        }
        return null;

    }
}
