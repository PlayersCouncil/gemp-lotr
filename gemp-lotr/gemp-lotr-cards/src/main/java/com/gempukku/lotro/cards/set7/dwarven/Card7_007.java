package com.gempukku.lotro.cards.set7.dwarven;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.effects.ChoiceEffect;
import com.gempukku.lotro.logic.effects.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.logic.effects.PreventableCardEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPreventCardEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Free
 * Culture: Dwarven
 * Twilight Cost: 2
 * Type: Companion • Dwarf
 * Strength: 6
 * Vitality: 3
 * Resistance: 6
 * Signet: Aragorn
 * Game Text: Damage +1. Response: if a [DWARVEN] condition is about to be discarded by an opponent, exert a Dwarf
 * or discard a [DWARVEN] card from hand to prevent that.
 */
public class Card7_007 extends AbstractCompanion {
    public Card7_007() {
        super(2, 6, 3, 6, Culture.DWARVEN, Race.DWARF, Signet.ARAGORN, "Gimli", "Feared Axeman", true);
        addKeyword(Keyword.DAMAGE, 1);
    }

    @Override
    public List<? extends ActivateCardAction> getOptionalInPlayBeforeActions(String playerId, LotroGame game, Effect effect, PhysicalCard self) {
        if (TriggerConditions.isGettingDiscardedByOpponent(effect, game, playerId, Culture.DWARVEN, CardType.CONDITION)
                && (PlayConditions.canExert(self, game, Race.DWARF)
                || PlayConditions.canDiscardFromHand(game, playerId, 1, Culture.DWARVEN))) {
            final ActivateCardAction action = new ActivateCardAction(self);
            List<Effect> possibleCosts = new LinkedList<>();
            possibleCosts.add(
                    new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Race.DWARF) {
                        @Override
                        public String getText(LotroGame game) {
                            return "Exert a Dwarf";
                        }
                    });
            possibleCosts.add(
                    new ChooseAndDiscardCardsFromHandEffect(action, playerId, false, 1, Culture.DWARVEN) {
                        @Override
                        public String getText(LotroGame game) {
                            return "Discard a DWARVEN card";
                        }
                    });
            action.appendCost(
                    new ChoiceEffect(action, playerId, possibleCosts));
            action.appendEffect(
                    new ChooseAndPreventCardEffect(self, (PreventableCardEffect) effect, playerId, "Choose DWARVEN condition",
                            Culture.DWARVEN, CardType.CONDITION));
            return Collections.singletonList(action);
        }
        return null;
    }
}
