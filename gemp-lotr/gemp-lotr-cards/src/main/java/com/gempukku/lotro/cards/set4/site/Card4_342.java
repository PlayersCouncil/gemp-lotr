package com.gempukku.lotro.cards.set4.site;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractSite;
import com.gempukku.lotro.logic.modifiers.CantTakeWoundsModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.condition.PhaseCondition;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Two Towers
 * Twilight Cost: 1
 * Type: Site
 * Site: 3T
 * Game Text: Plains. Sanctuary. Each [DUNLAND] Man may not take wounds during the archery phase.
 */
public class Card4_342 extends AbstractSite {
    public Card4_342() {
        super("Westemnet Plains", SitesBlock.TWO_TOWERS, 3, 1, Direction.RIGHT);
        addKeyword(Keyword.PLAINS);
        addKeyword(Keyword.SANCTUARY);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new CantTakeWoundsModifier(self, new PhaseCondition(Phase.ARCHERY), Filters.and(Culture.DUNLAND, Race.MAN)));
    }
}
