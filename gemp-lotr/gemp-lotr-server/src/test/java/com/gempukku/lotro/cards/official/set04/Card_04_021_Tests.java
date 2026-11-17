package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_021_Tests
{

	// Site 3 is Rivendell Waterfall: "While you can spot a ranger at Rivendell Waterfall, the move limit is +1
	// for this turn."  Aragorn is a ranger, so a turn begun there has three moves.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("band1", "4_21");
					put("band2", "4_21");

					put("brigand", "4_10");     // Dunlending Brigand: the other [dunland] Man, with no text
					// Saruman, Rabble-rouser: a [dunland] minion who is no Man.  "Each time the fellowship moves,
					// the Free Peoples player must exert a companion for each [dunland] Man you can spot."
					put("saruman", "4_33");
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man; strength +3 and fierce at 2 burdens, and there are none

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a ranger; a maneuver ability used only on purpose
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "1_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSiteControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("band", "4_21");
					put("brigand", "4_10");     // Dunlending Brigand: the other [dunland] Man, with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HillmanBandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Hillman Band
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: Each time the fellowship moves, you may spot another [dunland] Man to take control of a site.
		*/

		var scn = GetScenario();

		var band = scn.GetFreepsCard("band1");

		assertEquals("Hillman Band", band.getBlueprint().getTitle());
		assertNull(band.getBlueprint().getSubtitle());
		assertFalse(band.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, band.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, band.getBlueprint().getCulture());
		assertEquals(CardType.MINION, band.getBlueprint().getCardType());
		assertEquals(Race.MAN, band.getBlueprint().getRace());
		assertEquals(3, band.getBlueprint().getTwilightCost());
		assertEquals(9, band.getBlueprint().getStrength());
		assertEquals(1, band.getBlueprint().getVitality());
		assertEquals(3, band.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithASiteThatCanBeControlledTheMoveOffersItAndAYesTakesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var band = scn.GetShadowCard("band");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(band, brigand);

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.FreepsPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(band));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoSiteThatCanBeControlledTheMoveOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var band = scn.GetShadowCard("band");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 is the only site every marker has passed, and player 1 took it on player 2's turn.  Site 2 has
		// both markers on it as the move is answered (Q-0184), so there is nothing a yes could take (Q-0034).
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.MoveMinionsToTable(band, brigand);

		assertTrue(scn.FreepsControls(site1));

		scn.FreepsPass();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(band));
		assertTrue(scn.AwaitingShadowPhaseActions());

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertFalse(scn.IsSiteControlled(site2));
		assertEquals(3, scn.GetCurrentSiteNumber());
	}

	@Test
	public void EachRegroupMoveOffersAgainAndTakesTheNextSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var band = scn.GetShadowCard("band1");
		var brigand = scn.GetShadowCard("brigand");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		// Both markers are on site 3, the Waterfall; the other player's stays there all turn.
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		assertEquals(3, scn.GetMoveLimit());
		scn.FreepsPass();
		scn.MoveMinionsToTable(band, brigand);
		assertEquals(0, scn.GetShadowControlledSiteCount());

		// The regroup move from site 4: site 1, the lowest behind every marker, is taken.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(band));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, site1);
		assertFalse(scn.IsSiteControlled(site2));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The third move of the turn, from site 5: the same Band offers again, and site 2 is taken.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(band));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertTrue(scn.ShadowControls(site2));
		assertInZone(Zone.SUPPORT, site2);
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TwoBandsSpotEachOtherAndEachMakesItsOwnOfferWhichMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var band1 = scn.GetShadowCard("band1");
		var band2 = scn.GetShadowCard("band2");

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.FreepsPass();
		scn.MoveMinionsToTable(band1, band2);
		assertEquals(0, scn.GetShadowControlledSiteCount());

		// The regroup move from site 4.  Each Band is the other [dunland] Man the other spots.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(band1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(band2));
		scn.ShadowChooseOptionalTrigger(band1);
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());

		// The second Band's offer is still there, and declining it leaves site 2 on the path.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(band2));
		scn.ShadowDeclineOptionalTrigger();
		assertFalse(scn.IsSiteControlled(site2));
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithOnlyADunlandMinionWhoIsNoManAndAManOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var band = scn.GetShadowCard("band1");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(band, saruman, axeman);
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.SkipToMovementDecision();
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsChooseToMove();

		// Saruman's own move trigger spots the Band, the one [dunland] Man, and Frodo, the one companion, exerts.
		// The Band has no other [dunland] Man to spot, and makes no offer.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingAtTheRegroupDecisionOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var band = scn.GetShadowCard("band1");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(band, brigand);

		scn.SkipToMovementDecision();
		// A card in hand, so that the stay is followed by the reconcile and the turn does not end at once.
		scn.SetFreepsHand(scn.GetFreepsCard("brigand"));
		assertFalse(scn.IsSiteControlled(site1));
		scn.FreepsChooseToStay();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(band));
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
	}
}
