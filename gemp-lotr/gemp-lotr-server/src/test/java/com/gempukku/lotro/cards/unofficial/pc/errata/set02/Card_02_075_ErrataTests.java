package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_075_ErrataTests
{
    protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("ferny", "52_75");
                    put("sam", "1_310");

                    put("nazgul1", "12_161");
                    put("nazgul2", "12_161");

                }},
                GenericCardTestHelper.KingSites,
                GenericCardTestHelper.GimliRB,
                GenericCardTestHelper.RulingRing
        );
    }

    @Test
    public void BillStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 2E
         * Title: Bill Ferny
         * Subtitle: Swarthy Sneering Fellow
         * Side: Shadow
         * Culture: Ringwraith
         * Twilight Cost: 2
         * Type: Minion
         * Race: Man
         * Strength: 4
         * Vitality: 1
         * Site: 2
         * Errata Game Text: Bearer must be a ranger.  This weapon may be borne in addition to 1 other hand weapon.
         * Skirmish: If bearer is skirmishing a Nazgul, discard this possession to make bearer strength +3 and damage +1.
         */

        //Pre-game setup
        GenericCardTestHelper scn = GetScenario();

        PhysicalCardImpl ferny = scn.GetFreepsCard("ferny");

        assertTrue(ferny.getBlueprint().isUnique());
        assertEquals(2, ferny.getBlueprint().getTwilightCost());
        assertEquals(CardType.MINION, ferny.getBlueprint().getCardType());
        assertEquals(Culture.WRAITH, ferny.getBlueprint().getCulture());
        assertEquals(Race.MAN, ferny.getBlueprint().getRace());
        assertEquals(4, ferny.getBlueprint().getStrength());
        assertEquals(1, ferny.getBlueprint().getVitality());
        assertEquals(2, ferny.getBlueprint().getSiteNumber());
    }

    @Test
    public void NazgulAreNotRoaming() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetScenario();

        PhysicalCardImpl ferny = scn.GetShadowCard("ferny");
        PhysicalCardImpl nazgul1 = scn.GetShadowCard("nazgul1");
        PhysicalCardImpl nazgul2 = scn.GetShadowCard("nazgul2");

        scn.FreepsMoveCardToHand(ferny, nazgul1, nazgul2);

        scn.StartGame();

        scn.SetTwilight(12);

        scn.FreepsPassCurrentPhaseAction();

        //12 manual + 1 companion + 1 site.
        assertEquals(14, scn.GetTwilight());

        scn.ShadowPlayCard(nazgul1);

        //should be -5 for Black Rider, -2 for roaming
        assertEquals(7, scn.GetTwilight());
        assertTrue(scn.HasKeyword(nazgul1, Keyword.ROAMING));

        scn.ShadowPlayCard(ferny);
        assertFalse(scn.HasKeyword(nazgul1, Keyword.ROAMING));
        scn.ShadowPlayCard(nazgul2);

        //Should have been exactly enough twilight to play ferny + another black rider, if there are no roaming penalties.
        assertEquals(0, scn.GetTwilight());

    }

    @Test
    public void FreepsCannotAssignFernyIfHobbitSpotted() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetScenario();

        PhysicalCardImpl ferny = scn.GetShadowCard("ferny");
        PhysicalCardImpl sam = scn.GetFreepsCard("sam");
        PhysicalCardImpl gimli = scn.GetRingBearer();

        scn.ShadowMoveCharToTable(ferny);
        scn.FreepsMoveCharToTable(sam);

        //Make ferny fierce and strength +5 just to make things easier to test
        scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.name("Bill Ferny"), null, Keyword.FIERCE));
        scn.ApplyAdHocModifier(new StrengthModifier(null, Filters.name("Bill Ferny"), null, 5));

        scn.StartGame();

        scn.SkipToPhase(Phase.ASSIGNMENT);
        scn.PassCurrentPhaseActions();

        Boolean exc = false;
        try {
            scn.FreepsAssignToMinions(gimli, ferny);
        }
        catch (DecisionResultInvalidException ex) {
            exc = true;
        }
        assertTrue(exc); // If an exception wasn't thrown, then freeps assigning was permitted even with a hobbit
        scn.FreepsPassCurrentPhaseAction();
        scn.ShadowAssignToMinions(sam, ferny);

        scn.FreepsResolveSkirmish(sam);
        scn.PassCurrentPhaseActions();

        //artificial fierce skirmish
        assertEquals(Zone.DEAD, sam.getZone());
        scn.PassCurrentPhaseActions();

        Boolean exc2 = false;
        try {
            scn.FreepsAssignToMinions(gimli, ferny);
        }
        catch (DecisionResultInvalidException ex) {
            exc2 = true;
        }
        assertFalse(exc2); // If an exception wasn't thrown, then freeps assigning was permitted with no hobbit to spot

    }

    @Test
    public void DiscardFernyIfUnderground() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetScenario();

        PhysicalCardImpl ferny = scn.GetShadowCard("ferny");

        scn.ShadowMoveCharToTable(ferny);

        scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.siteNumber(2), null, Keyword.UNDERGROUND));

        scn.StartGame();

        assertEquals(Zone.SHADOW_CHARACTERS, ferny.getZone());

        scn.FreepsPassCurrentPhaseAction();

        assertEquals(Zone.DISCARD, ferny.getZone());
    }
}
