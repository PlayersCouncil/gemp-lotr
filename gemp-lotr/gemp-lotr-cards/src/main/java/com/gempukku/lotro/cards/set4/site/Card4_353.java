package com.gempukku.lotro.cards.set4.site;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractSite;
import com.gempukku.lotro.logic.modifiers.AllyParticipatesInArcheryFireAndSkirmishesModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Two Towers
 * Twilight Cost: 3
 * Type: Site
 * Site: 6T
 * Game Text: Sanctuary. Battleground. Each [ROHAN] ally may participate in archery fire and skirmishes at Great Hall.
 */
public class Card4_353 extends AbstractSite {
    public Card4_353() {
        super("Great Hall", SitesBlock.TWO_TOWERS, 6, 3, Direction.LEFT);
        addKeyword(Keyword.SANCTUARY);
        addKeyword(Keyword.BATTLEGROUND);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new AllyParticipatesInArcheryFireAndSkirmishesModifier(self, Filters.and(Culture.ROHAN, CardType.ALLY)));
    }
}
