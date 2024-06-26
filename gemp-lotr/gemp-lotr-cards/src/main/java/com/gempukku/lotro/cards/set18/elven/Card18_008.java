package com.gempukku.lotro.cards.set18.elven;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractAttachableFPPossession;
import com.gempukku.lotro.logic.modifiers.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Set: Treachery & Deceit
 * Side: Free
 * Culture: Elven
 * Twilight Cost: 2
 * Type: Possession • Hand Weapon/Shield
 * Strength: +2
 * Vitality: +1
 * Game Text: To play, bearer must not be bearing any possessions. Bearer must be an [ELVEN] companion. Bearer cannot
 * bear any other possessions. While a unique companion bears this possession, the Free Peoples player may not use
 * archery special abilities and the minion archery total is -1.
 */
public class Card18_008 extends AbstractAttachableFPPossession {
    public Card18_008() {
        super(2, 2, 1, Culture.ELVEN, null, "Elven Armaments");
    }

    @Override
    public Set<PossessionClass> getPossessionClasses() {
        Set<PossessionClass> result = new HashSet<>();
        result.add(PossessionClass.HAND_WEAPON);
        result.add(PossessionClass.SHIELD);
        return result;
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.and(Culture.ELVEN, CardType.COMPANION, Filters.not(Filters.hasAttached(CardType.POSSESSION)));
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new MayNotBearModifier(self, Filters.hasAttached(self), Filters.not(self), CardType.POSSESSION));
        modifiers.add(
                new PlayerCantUsePhaseSpecialAbilitiesModifier(self, new SpotCondition(Filters.hasAttached(self), Filters.unique, CardType.COMPANION), self.getOwner(), Phase.ARCHERY));
        modifiers.add(
                new ArcheryTotalModifier(self, Side.SHADOW, new SpotCondition(Filters.hasAttached(self), Filters.unique, CardType.COMPANION), -1));
        return modifiers;
    }
}
