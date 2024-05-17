package com.gempukku.lotro.cards.set15.men;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.StringWhileInZoneData;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.RevealCardEffect;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.RemoveGameTextModifier;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Hunters
 * Side: Shadow
 * Culture: Men
 * Twilight Cost: 3
 * Type: Condition • Support Area
 * Game Text: When you play this, spot 2 [MEN] Men to randomly reveal a site from the Free Peoples player’s
 * adventure deck. Note its cardtitle. Each site on the adventure path with the same card title as the selected site
 * loses its gametext.
 */
public class Card15_076 extends AbstractPermanent {
    public Card15_076() {
        super(Side.SHADOW, 3, CardType.CONDITION, Culture.MEN, "Destroyed Homestead");
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(new RemoveGameTextModifier(self, Filters.and(CardType.SITE, Zone.ADVENTURE_PATH, Filters.name(fromData(self.getWhileInZoneData())))));
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, self)
                && PlayConditions.canSpot(game, 2, Culture.MEN, Race.MAN)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            final List<PhysicalCard> randomCards = GameUtils.getRandomCards(game.getGameState().getAdventureDeck(game.getGameState().getCurrentPlayerId()), 1);
            if (randomCards.size() == 1) {
                PhysicalCard randomSite = randomCards.get(0);
                action.appendEffect(
                        new RevealCardEffect(self, randomCards.get(0)));
                self.setWhileInZoneData(toData(randomSite.getBlueprint().getTitle()));
            }
            return Collections.singletonList(action);
        }
        return null;
    }

    private String fromData(PhysicalCard.WhileInZoneData data) {
        if (data == null)
            return null;
        return data.getValue();
    }

    private PhysicalCard.WhileInZoneData toData(String cardTitle) {
        return new StringWhileInZoneData(cardTitle);
    }

    @Override
    public String getDisplayableInformation(PhysicalCard self) {
        if (self.getWhileInZoneData() != null)
            return "Random site name is: " + self.getWhileInZoneData();
        return null;
    }
}
