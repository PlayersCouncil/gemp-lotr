package com.gempukku.lotro.cards.set4.site;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractSite;
import com.gempukku.lotro.logic.modifiers.ArcheryTotalModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: The Two Towers
 * Twilight Cost: 8
 * Type: Site
 * Site: 5T
 * Game Text: Battleground. The fellowship archery total is +1. The minion archery total is -1.
 */
public class Card4_351 extends AbstractSite {
    public Card4_351() {
        super("Hornburg Parapet", SitesBlock.TWO_TOWERS, 5, 8, Direction.LEFT);
        addKeyword(Keyword.BATTLEGROUND);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new ArcheryTotalModifier(self, Side.FREE_PEOPLE, 1));
        modifiers.add(
                new ArcheryTotalModifier(self, Side.SHADOW, -1));
        return modifiers;
    }
}
