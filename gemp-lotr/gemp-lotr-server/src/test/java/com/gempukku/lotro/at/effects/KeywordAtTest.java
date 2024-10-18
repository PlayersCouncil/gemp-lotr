package com.gempukku.lotro.at.effects;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KeywordAtTest extends AbstractAtTest {
    @Test
    public void removeKeyword() throws Exception{
        initializeSimplestGame();

        PhysicalCard blendedRace = addToZone(createCard(P1, "2_16"), Zone.SUPPORT);
        PhysicalCard lurtz = addToZone(createCard(P2, "1_127"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.FELLOWSHIP);
        assertFalse(hasKeyword(lurtz, Keyword.DAMAGE));
    }

    @Test
    public void removeKeywordEffect() throws Exception {
        initializeSimplestGame();

        PhysicalCard merry = addToZone(createCard(P1, "1_302"), Zone.FREE_CHARACTERS);
        PhysicalCard seekAndHide = addToZone(createCard(P1, "3_112"), Zone.HAND);
        PhysicalCard urukHaiRaidingParty = addToZone(createCard(P2, "1_158"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        playerDecided(P1, merry.getCardId()+" "+urukHaiRaidingParty.getCardId());
        selectCard(P1, merry);

        assertTrue(hasKeyword(urukHaiRaidingParty, Keyword.DAMAGE));
        selectCardAction(P1, seekAndHide);
        assertFalse(hasKeyword(urukHaiRaidingParty, Keyword.DAMAGE));
    }

    @Test
    public void removeGameText() throws Exception {
        initializeSimplestGame();

        PhysicalCard ringBearer = getRingBearer(P1);
        PhysicalCard vialOfGaladriel = attachTo(createCard(P1, "10_13"), ringBearer);

        PhysicalCard urukHaiRaidingParty = addToZone(createCard(P2, "1_158"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        playerDecided(P1, ringBearer.getCardId()+" "+urukHaiRaidingParty.getCardId());
        selectCard(P1, ringBearer);

        assertTrue(hasKeyword(urukHaiRaidingParty, Keyword.DAMAGE));
        selectCardAction(P1, vialOfGaladriel);
        assertFalse(hasKeyword(urukHaiRaidingParty, Keyword.DAMAGE));
    }
}
