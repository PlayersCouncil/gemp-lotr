package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_360_Tests
{

	// The Towers path with Fortress of Orthanc as site 9 in place of Palantir Chamber.  Site 8 is Ring of Isengard
	// (Shadow number 7; a Shadow ability that needs a [dunland] minion in hand, and there is none).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Aragorn, Ranger of the North: the ranger Pathfinder spots; a maneuver ability used only on purpose
					put("aragorn", "1_89");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a skirmish ability used only on purpose
					put("gandalf", "0_64");     // Gandalf, Stormcrow: no text
					// Ent Avenger: strength 6, +2 only beside 3 Ents
					put("avenger", "6_27");
					// Guma, Plains Farmer: a [rohan] Man ally, home 4T; an ability used only on purpose
					put("guma", "4_277");
					// Pathfinder: "Fellowship or Regroup: Spot a ranger to play the fellowship's next site."  It lays
					// site 9 while the fellowship is still at site 8.
					put("pathfinder", "1_110");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");
					put("site2", "4_331");
					put("site3", "4_337");
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FortressofOrthancStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Fortress of Orthanc
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 9
		 * Shadow Number: 9
		 * Direction: Left
		 * Game Text: <b>Battleground</b>. The Shadow number of Fortress of Orthanc is +2 for each companion over 4.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Fortress of Orthanc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
	}

	@Test
	public void WithFiveCompanionsAndAnAllyItIsElevenOnceTheFellowshipIsThereAndNineAhead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var guma = scn.GetFreepsCard("guma");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		var fortress = scn.GetFreepsSite(9);
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, gandalf);
		scn.MoveAlliesToTable(guma);

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToHand(pathfinder);
		assertInHand(pathfinder);

		// To site 8, and with no minion about straight to the regroup phase, where Pathfinder lays site 9.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPlayCard(pathfinder);

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertSame(fortress, scn.GetSite(9));
		// CR 4.2 "active": the fellowship is not there, so its text does not apply.
		assertEquals(9, scn.GetTwilightCost(fortress));

		scn.SkipToMovementDecision();
		scn.SetTwilight(0);
		scn.FreepsChooseToMove();

		assertSame(fortress, scn.GetCurrentSite());
		// Frodo, Aragorn, Gimli, Boromir and Gandalf: one over 4.  The ally is no companion.
		assertEquals(11, scn.GetTwilightCost(fortress));
		// 0 set + 5 companions + 9 and 2 for Fortress of Orthanc
		assertEquals(16, scn.GetTwilight());
		// Ring of Isengard, site 8, keeps its own number.
		assertEquals(7, scn.GetTwilightCost(scn.GetSite(8)));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSixCompanionsItIsThirteenAndElevenOnceOneOfThemIsKilledThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, gandalf, avenger);

		scn.StartGame();
		scn.SkipToSite(8);
		scn.SetTwilight(0);
		scn.FreepsPass();

		var fortress = scn.GetCurrentSite();
		assertEquals("Fortress of Orthanc", fortress.getBlueprint().getTitle());
		// Two over 4.
		assertEquals(13, scn.GetTwilightCost(fortress));
		// 0 set + 6 companions + 9 and 4 for Fortress of Orthanc
		assertEquals(19, scn.GetTwilight());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(avenger, troop);
		scn.FreepsResolveSkirmish(avenger);
		// 13 against 6: Ent Avenger is overwhelmed, and five companions are left.
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, avenger);
		assertEquals(11, scn.GetTwilightCost(fortress));
		assertTrue(scn.GameIsFinished());
	}

	@Test
	public void WithFourCompanionsAndAnAllyItIsItsPrintedNine() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);
		scn.MoveAlliesToTable(guma);

		scn.StartGame();
		scn.SkipToSite(8);
		scn.SetTwilight(0);
		scn.FreepsPass();

		var fortress = scn.GetCurrentSite();
		assertEquals("Fortress of Orthanc", fortress.getBlueprint().getTitle());
		// Four companions and an ally: none over 4.
		assertEquals(9, scn.GetTwilightCost(fortress));
		// 0 set + 4 companions + 9 for Fortress of Orthanc
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
