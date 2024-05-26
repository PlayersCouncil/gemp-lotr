package com.gempukku.lotro.cards.set18.uruk_hai;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.CantTakeWoundsModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.SpotCondition;
import com.gempukku.lotro.logic.modifiers.condition.AndCondition;
import com.gempukku.lotro.logic.modifiers.condition.NotCondition;
import com.gempukku.lotro.logic.modifiers.condition.PhaseCondition;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Treachery & Deceit
 * Side: Shadow
 * Culture: Uruk-Hai
 * Twilight Cost: 5
 * Type: Minion • Uruk-Hai
 * Strength: 13
 * Vitality: 3
 * Site: 5
 * Game Text: Damage +1. Hunter 1. (While skirmishing a non-hunter character, this character is strength +1.)
 * While you can spot 7 possessions, Shagrat is fierce and cannot take wounds (except during skirmish phases).
 */
public class Card18_122 extends AbstractMinion {
    public Card18_122() {
        super(5, 13, 3, 5, Race.URUK_HAI, Culture.URUK_HAI, "Shagrat", "Tower Captain", true);
        addKeyword(Keyword.DAMAGE, 1);
        addKeyword(Keyword.HUNTER, 1);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new AddKeywordModifier(self, self, new SpotCondition(7, CardType.POSSESSION), Keyword.FIERCE, 1));
        modifiers.add(
                new CantTakeWoundsModifier(self,
                        new AndCondition(
                                new SpotCondition(7, CardType.POSSESSION),
                                new NotCondition(new PhaseCondition(Phase.SKIRMISH))
                        ), self));
        return modifiers;
    }
}
