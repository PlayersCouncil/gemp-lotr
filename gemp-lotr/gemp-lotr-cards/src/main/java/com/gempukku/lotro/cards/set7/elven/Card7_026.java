package com.gempukku.lotro.cards.set7.elven;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.effects.AddThreatsEffect;
import com.gempukku.lotro.logic.effects.AddUntilEndOfPhaseModifierEffect;
import com.gempukku.lotro.logic.modifiers.ArcheryTotalModifier;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Free
 * Culture: Elven
 * Twilight Cost: 2
 * Type: Companion • Elf
 * Strength: 6
 * Vitality: 3
 * Resistance: 6
 * Signet: Theoden
 * Game Text: Archer. Archery: If you cannot spot a threat, add a threat to make the fellowship archery total +1.
 */
public class Card7_026 extends AbstractCompanion {
    public Card7_026() {
        super(2, 6, 3, 6, Culture.ELVEN, Race.ELF, Signet.THEODEN, "Legolas", "Nimble Warrior", true);
        addKeyword(Keyword.ARCHER);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.ARCHERY, self)
                && game.getGameState().getThreats() == 0) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new AddThreatsEffect(playerId, self, 1));
            action.appendEffect(
                    new AddUntilEndOfPhaseModifierEffect(
                            new ArcheryTotalModifier(self, Side.FREE_PEOPLE, 1)));
            return Collections.singletonList(action);
        }
        return null;
    }
}
