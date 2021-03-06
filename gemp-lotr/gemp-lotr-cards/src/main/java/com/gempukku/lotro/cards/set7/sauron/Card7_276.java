package com.gempukku.lotro.cards.set7.sauron;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.DiscardStackedCardsEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndAddUntilEOPStrengthBonusEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Shadow
 * Culture: Sauron
 * Twilight Cost: 4
 * Type: Minion • Orc
 * Strength: 10
 * Vitality: 2
 * Site: 5
 * Game Text: Besieger. Skirmish: If this minion is stacked on a site you control, discard him to make a [SAURON] Orc
 * strength +5 (or +10 if you have initiative).
 */
public class Card7_276 extends AbstractMinion {
    public Card7_276() {
        super(4, 10, 2, 5, Race.ORC, Culture.SAURON, "Gorgoroth Ransacker");
        addKeyword(Keyword.BESIEGER);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsFromStacked(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseStackedShadowCardDuringPhase(game, Phase.SKIRMISH, self, 0)
                && Filters.siteControlled(playerId).accepts(game, self.getStackedOn())) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new DiscardStackedCardsEffect(self, self));
            boolean hasInitiative = PlayConditions.hasInitiative(game, Side.SHADOW);
            action.appendEffect(
                    new ChooseAndAddUntilEOPStrengthBonusEffect(action, self, playerId, hasInitiative ? 10 : 5, Culture.SAURON, Race.ORC));
            return Collections.singletonList(action);
        }
        return null;
    }
}
