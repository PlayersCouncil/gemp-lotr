package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_354_Tests
{

	// The Fellowship sites, with Anduin Wilderland as site 7 in place of Anduin Confluence.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, vitality 1; Archer is its whole text
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site7", "1_354");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AnduinWilderlandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Anduin Wilderland
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 7
		 * Shadow Number: 6
		 * Direction: Right
		 * Game Text: <b>Forest</b>. <b>River</b>. While the fellowship is at Anduin Wilderland, skip the archery phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(7);

		assertEquals("Anduin Wilderland", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(7, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void AtAnduinWilderlandTheArcheryPhaseIsSkipped() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveMinionsToTable(marksman);
		// The fellowship moves from site 6 to Anduin Wilderland.
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.PassCurrentPhaseActions();

		// From the maneuver phase straight to the assignment phase: the archer's wound is never placed.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AtTheNextSiteWithAnduinWilderlandBehindTheArcheryPhaseIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveMinionsToTable(marksman);
		// The fellowship moves on from Anduin Wilderland to site 8.
		scn.SkipToPhase(Phase.MANEUVER);

		// Anduin Wilderland lies on the adventure path, behind the fellowship.
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals("Anduin Wilderland", scn.GetSite(7).getBlueprint().getTitle());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.PassCurrentPhaseActions();

		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		scn.PassCurrentPhaseActions();

		// Minion archery total 1 (the Marksman); Frodo is the only companion and takes it.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
