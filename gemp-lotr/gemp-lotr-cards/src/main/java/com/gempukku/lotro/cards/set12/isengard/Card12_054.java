package com.gempukku.lotro.cards.set12.isengard;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.ReadableStringWhileInZoneData;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Set: Black Rider
 * Side: Shadow
 * Culture: Isengard
 * Twilight Cost: 4
 * Type: Minion • Wizard
 * Strength: 8
 * Vitality: 4
 * Site: 4
 * Game Text: Damage +1. Fierce. Lurker. (Skirmishes involving lurker minions must be resolved after any others.) When
 * you play Saruman, name a culture. Each companion of the named culture is strength -1.
 */
public class Card12_054 extends AbstractMinion {
    public Card12_054() {
        super(4, 8, 4, 4, Race.WIZARD, Culture.ISENGARD, "Saruman", "Of Many Colours", true);
        addKeyword(Keyword.DAMAGE, 1);
        addKeyword(Keyword.FIERCE);
        addKeyword(Keyword.LURKER);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, final PhysicalCard self) {
        return Collections.singletonList(new StrengthModifier(self, Filters.and(CardType.COMPANION,
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        return self.getWhileInZoneData() != null && physicalCard.getBlueprint().getCulture() == fromData(self.getWhileInZoneData());
                    }
                }), -1));
    }

    @Override
    public String getDisplayableInformation(PhysicalCard self) {
        if (self.getWhileInZoneData() != null)
            return "Chosen culture is " + self.getWhileInZoneData().getHumanReadable();
        return null;
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(final LotroGame game, EffectResult effectResult, final PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, self)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);

            final Set<String> possibleCultures = new LinkedHashSet<>();
            for (Culture culture : Culture.values())
                if (culture.isOfficial())
                    possibleCultures.add(culture.getHumanReadable());

            action.appendEffect(
                    new PlayoutDecisionEffect(self.getOwner(),
                            new MultipleChoiceAwaitingDecision(1, "Choose a culture", possibleCultures.toArray(new String[possibleCultures.size()])) {
                                @Override
                                protected void validDecisionMade(int index, String result) {
                                    self.setWhileInZoneData(toData(Culture.findCultureByHumanReadable(result)));
                                    game.getGameState().sendMessage(self.getOwner() +" has chosen "+ result);
                                }
                            }));
            return Collections.singletonList(action);
        }
        return null;
    }

    private Culture fromData(PhysicalCard.WhileInZoneData data) {
        if (data == null)
            return null;
        return Culture.valueOf(data.getValue());
    }

    private PhysicalCard.WhileInZoneData toData(Culture culture) {
        return new ReadableStringWhileInZoneData(culture.name(), culture.getHumanReadable());
    }
}
