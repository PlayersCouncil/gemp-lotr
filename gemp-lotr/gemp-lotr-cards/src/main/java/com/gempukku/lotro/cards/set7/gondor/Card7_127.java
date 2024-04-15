package com.gempukku.lotro.cards.set7.gondor;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.effects.ChoiceEffect;
import com.gempukku.lotro.logic.effects.LiberateASiteEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.modifiers.Condition;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 2
 * Type: Companion • Man
 * Strength: 5
 * Vitality: 3
 * Resistance: 6
 * Game Text: While no opponent controls a site, Vorondil is strength +2. Regroup: Exert 3 [GONDOR] Men or 2 knights
 * to liberate a site.
 */
public class Card7_127 extends AbstractCompanion {
    public Card7_127() {
        super(2, 5, 3, 6, Culture.GONDOR, Race.MAN, null, "Vorondil", null, true);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, final PhysicalCard self) {
        return Collections.singletonList(
                new StrengthModifier(self, self,
                        new Condition() {
                            @Override
                            public boolean isFullfilled(LotroGame game) {
                                return Filters.countActive(game, Filters.siteControlledByOtherPlayer(self.getOwner())) == 0;
                            }
                        }, 2));
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.REGROUP, self)
                && (PlayConditions.canExert(self, game, 1, 3, Culture.GONDOR, Race.MAN) || PlayConditions.canExert(self, game, 1, 2, Keyword.KNIGHT))) {
            ActivateCardAction action = new ActivateCardAction(self);
            List<Effect> possibleCosts = new LinkedList<>();
            possibleCosts.add(
                    new ChooseAndExertCharactersEffect(action, playerId, 3, 3, Culture.GONDOR, Race.MAN) {
                        @Override
                        public String getText(LotroGame game) {
                            return "Exert 3 GONDOR Men";
                        }
                    });
            possibleCosts.add(
                    new ChooseAndExertCharactersEffect(action, playerId, 2, 2, Keyword.KNIGHT) {
                        @Override
                        public String getText(LotroGame game) {
                            return "Exert 2 knights";
                        }
                    });
            action.appendCost(
                    new ChoiceEffect(action, playerId, possibleCosts));
            action.appendEffect(
                    new LiberateASiteEffect(self, playerId, null));
            return Collections.singletonList(action);
        }
        return null;
    }
}