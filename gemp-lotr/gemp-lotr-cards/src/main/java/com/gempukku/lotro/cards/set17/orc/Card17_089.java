package com.gempukku.lotro.cards.set17.orc;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.ReadableStringWhileInZoneData;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Rise of Saruman
 * Side: Shadow
 * Culture: Orc
 * Twilight Cost: 3
 * Type: Possession • Mount
 * Strength: +2
 * Vitality: +2
 * Game Text: Bearer must be an [ORC] Orc with strength 10 or less. Bearer is fierce. When you play this possession,
 * spot a race. While bearer is skirmishing a companion of that race, bearer is strength +3 and damage +1.
 */
public class Card17_089 extends AbstractAttachable {
    public Card17_089() {
        super(Side.SHADOW, CardType.POSSESSION, 3, Culture.ORC, PossessionClass.MOUNT, "Relentless Warg");
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.and(Culture.ORC, Race.ORC, Filters.lessStrengthThan(11));
    }

    @Override
    public int getStrength() {
        return 2;
    }

    @Override
    public int getVitality() {
        return 2;
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new KeywordModifier(self, Filters.hasAttached(self), Keyword.FIERCE));
        modifiers.add(
                new StrengthModifier(self, Filters.and(Filters.hasAttached(self), Filters.inSkirmishAgainst(CardType.COMPANION,
                        new Filter() {
                            @Override
                            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                                return self.getWhileInZoneData() != null && physicalCard.getBlueprint().getRace() == fromData(self.getWhileInZoneData());
                            }
                        })), 3));
        modifiers.add(
                new KeywordModifier(self, Filters.and(Filters.hasAttached(self), Filters.inSkirmishAgainst(CardType.COMPANION,
                        new Filter() {
                            @Override
                            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                                return self.getWhileInZoneData() != null && physicalCard.getBlueprint().getRace() == fromData(self.getWhileInZoneData());
                            }
                        })), Keyword.DAMAGE, 1));
        return modifiers;
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, final PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, self)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new ChooseActiveCardEffect(self, self.getOwner(), "Choose character with race", Filters.character,
                            new Filter() {
                                @Override
                                public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                                    return physicalCard.getBlueprint().getRace() != null;
                                }
                            }) {
                        @Override
                        protected void cardSelected(LotroGame game, PhysicalCard card) {
                            Race race = card.getBlueprint().getRace();
                            self.setWhileInZoneData(toData(race));

                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }

    private Race fromData(PhysicalCard.WhileInZoneData data) {
        if (data == null)
            return null;
        return Race.valueOf(data.getValue());
    }

    private PhysicalCard.WhileInZoneData toData(Race race) {
        return new ReadableStringWhileInZoneData(race.name(), race.getHumanReadable());
    }

    @Override
    public String getDisplayableInformation(PhysicalCard self) {
        if (self.getWhileInZoneData() != null)
            return "Selected race is: " + self.getWhileInZoneData().getHumanReadable();
        return null;
    }
}
