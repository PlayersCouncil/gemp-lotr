package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_022_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horde", "4_22");

					put("savage", "4_17");      // Dunlending Savage: a [dunland] Man of another title, with no text
					put("saruman", "4_33");     // Saruman, Rabble-rouser: a [dunland] minion who is no Man; laid after the last move, so his move trigger never fires
					// Southron Explorer: a [raider] Man, vitality 3.  "Regroup: Exert this minion and remove (5) to
					// take control of a site."
					put("explorer", "4_250");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HillmanHordeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Hillman Horde
		 * Unique: True
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 14
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: While you control 2 sites, skip the archery phase.<br>While you control 3 sites, each of your [dunland] Men is <b>fierce</b>.<br>While you control 4 sites, each of your [dunland] Men is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var horde = scn.GetFreepsCard("horde");

		assertEquals("Hillman Horde", horde.getBlueprint().getTitle());
		assertNull(horde.getBlueprint().getSubtitle());
		assertTrue(horde.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, horde.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, horde.getBlueprint().getCulture());
		assertEquals(CardType.MINION, horde.getBlueprint().getCardType());
		assertEquals(Race.MAN, horde.getBlueprint().getRace());
		assertEquals(6, horde.getBlueprint().getTwilightCost());
		assertEquals(14, horde.getBlueprint().getStrength());
		assertEquals(2, horde.getBlueprint().getVitality());
		assertEquals(3, horde.getBlueprint().getSiteNumber());
	}

	@Test
	public void ArcheryPhaseIsPlayedWithOneSiteAndSkippedOnceASecondIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horde = scn.GetShadowCard("horde");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		// Both markers are on site 4: sites 1 to 3 are behind every marker.
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		// One site of the Shadow player's, and one the other player took on his own Shadow turn.
		scn.ShadowTakeControlOfSite(site1);
		scn.FreepsTakeControlOfSite(site2);
		scn.MoveMinionsToTable(horde, explorer);
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());

		// The move to site 5, and its maneuver phase: the archery phase follows.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.PassManeuverActions();
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(5);
		assertEquals(5, scn.FreepsGetSiteNumber());
		assertEquals(4, scn.ShadowGetSiteNumber());
		assertFalse(scn.IsSiteControlled(site3));
		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(site3));
		assertEquals(2, scn.GetShadowControlledSiteCount());

		// The move to site 6: from the maneuver phase the game goes straight on to the assignment phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.PassManeuverActions();
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void EachOfYourDunlandMenIsFierceAtThreeSitesAndDamagePlusOneAtFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horde = scn.GetShadowCard("horde");
		var savage = scn.GetShadowCard("savage");
		var saruman = scn.GetShadowCard("saruman");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		// Both markers are on site 6: sites 1 to 5 are behind every marker.
		scn.SkipToSite(6);
		var site4 = scn.GetSite(4);
		var site5 = scn.GetSite(5);
		// One site of the other player's, and two of the Shadow player's.
		scn.FreepsTakeControlOfSite(scn.GetSite(1));
		scn.ShadowTakeControlOfSite(scn.GetSite(2));
		scn.ShadowTakeControlOfSite(scn.GetSite(3));

		// The move to site 7, with no minion in play: straight on to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(horde, savage, saruman, explorer);
		scn.SetTwilight(10);
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertFalse(scn.HasKeyword(horde, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(horde, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(savage, Keyword.DAMAGE));

		// A third site, by the Explorer: the Horde and the Savage are fierce, the [dunland] Wizard and the
		// [raider] Man are not, and nobody is damage +1.
		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(site4));
		assertEquals(3, scn.GetShadowControlledSiteCount());
		assertTrue(scn.HasKeyword(horde, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(saruman, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(explorer, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(horde, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(savage, Keyword.DAMAGE));

		// A fourth: the same two are damage +1 as well, and still fierce.
		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(site5));
		assertEquals(4, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetKeywordCount(horde, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(saruman, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(explorer, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(horde, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
