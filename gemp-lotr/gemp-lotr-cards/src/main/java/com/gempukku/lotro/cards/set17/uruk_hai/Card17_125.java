package com.gempukku.lotro.cards.set17.uruk_hai;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.AddTwilightEffect;
import com.gempukku.lotro.logic.effects.LiberateASiteEffect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Rise of Saruman
 * Side: Shadow
 * Culture: Uruk-hai
 * Twilight Cost: 3
 * Type: Minion • Uruk-Hai
 * Strength: 8
 * Vitality: 2
 * Site: 5
 * Game Text: Damage +1. When you play this minion, you may liberate a site to add (X) where X is the Shadow number
 * of the liberated site.
 */
public class Card17_125 extends AbstractMinion {
    public Card17_125() {
        super(3, 8, 2, 5, Race.URUK_HAI, Culture.URUK_HAI, "White Hand Enforcer");
        addKeyword(Keyword.DAMAGE, 1);
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, final LotroGame game, EffectResult effectResult, final PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, self)
                && PlayConditions.canLiberateASite(game, playerId, self)) {
            final OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendCost(
                    new LiberateASiteEffect(self, playerId, null) {
                        @Override
                        public void liberatedSiteCallback(PhysicalCard liberatedSite) {
                            action.appendEffect(
                                    new AddTwilightEffect(self, game.getModifiersQuerying().getTwilightCostToPlay(game, liberatedSite, null, 0, false)));
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
