package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_245_Tests
{

	// The minion archery total is read with GetShadowArcheryTotal, the calculation the archery phase uses; no arrows
	// are fired.  An Archer's own keyword is 1 of it.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("archer", "4_245");
					put("archer2", "4_245");

					put("scout", "4_252");      // Southron Scout: [raider] Southron Man minion, cost 1; Ambush (2) and no other text
					put("explorer", "4_250");   // Southron Explorer: [raider] Southron Man minion; Regroup: exert him and remove (5) to take control of a site
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man minion that is no Southron; his text needs 2 burdens
					// Bladetusk Rearguard: a [raider] artifact; "Maneuver: Remove (5) to make this artifact a fierce mounted
					// Southron minion until the end of the turn": a Southron who is no Man, and no archer
					put("rearguard", "103_43");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronArcherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Southron Archer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. <b>Archer</b>.<br>While you can spot another Southron, the minion archery total is +1 for each site you control.
		*/

		var scn = GetScenario();

		var archer = scn.GetFreepsCard("archer");

		assertEquals("Southron Archer", archer.getBlueprint().getTitle());
		assertNull(archer.getBlueprint().getSubtitle());
		assertFalse(archer.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, archer.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, archer.getBlueprint().getCulture());
		assertEquals(CardType.MINION, archer.getBlueprint().getCardType());
		assertEquals(Race.MAN, archer.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(archer, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(archer, Keyword.ARCHER));
		assertEquals(3, archer.getBlueprint().getTwilightCost());
		assertEquals(6, archer.getBlueprint().getStrength());
		assertEquals(2, archer.getBlueprint().getVitality());
		assertEquals(4, archer.getBlueprint().getSiteNumber());
	}


	@Test
	public void AddsOnlyItselfUntilAnotherSouthronIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		var scout = scn.GetShadowCard("scout");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(scout);

		scn.StartGame();
		assertInHand(scout);
		scn.SkipToSite(4);
		// Every marker is past sites 1 to 3: the Shadow player takes two, the Free Peoples player the third.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.FreepsTakeControlOfSite();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The Easterling is a [raider] Man but no Southron.
		scn.MoveMinionsToTable(archer, axeman);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		// The archer alone: he is no "other" Southron, and the Easterling is none at all.
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.ShadowPlayCard(scout);
		assertInZone(Zone.SHADOW_CHARACTERS, scout);
		// 1 for the archer + 1 for each of the Shadow player's 2 sites; the Free Peoples player's site does not count.
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TotalRisesByOneForEachSiteTheShadowPlayerControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.FreepsPass();
		// No minion in the Shadow phase, so the game goes on to regroup at site 5.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(archer, explorer);
		scn.SetTwilight(5);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.ShadowTakeControlOfSite();
		assertEquals(2, scn.GetShadowArcheryTotal());

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(explorer));
		assertEquals(0, scn.GetWoundsOn(explorer));
		scn.ShadowUseCardAction(explorer);
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertEquals(3, scn.GetShadowArcheryTotal());

		// A site the other player controls adds nothing.
		scn.FreepsTakeControlOfSite();
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASouthronWhoIsNoManIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		var rearguard = scn.GetShadowCard("rearguard");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.MoveCardsToSupportArea(rearguard);
		scn.MoveMinionsToTable(archer);
		assertInZone(Zone.SUPPORT, rearguard);
		assertEquals(2, scn.GetShadowControlledSiteCount());
		// The archer is the minion that gives the turn a maneuver phase.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.FreepsPass();
		scn.ShadowUseCardAction(rearguard);
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertTrue(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		assertFalse(scn.IsRace(rearguard, Race.MAN));
		// 1 for the archer + 1 for each of 2 sites: the Rearguard is no archer itself.
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ASecondCopyIsTheOtherSouthronAndEachAddsItsOwnBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		var archer2 = scn.GetShadowCard("archer2");
		scn.MoveCardsToHand(archer2);

		scn.StartGame();
		assertInHand(archer2);
		scn.SkipToSite(4);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.MoveMinionsToTable(archer);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.ShadowPlayCard(archer2);
		assertInZone(Zone.SHADOW_CHARACTERS, archer2);
		// Each sees the other: 1 + 1 for the two archers, and 2 + 2 for two copies of "+1 for each of 2 sites".
		assertEquals(6, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
