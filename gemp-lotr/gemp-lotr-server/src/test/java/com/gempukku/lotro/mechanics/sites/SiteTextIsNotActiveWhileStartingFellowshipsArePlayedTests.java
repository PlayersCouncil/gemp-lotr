package com.gempukku.lotro.mechanics.sites;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A site's game text is not in force while the starting fellowships are played.
 *
 * Comprehensive Rules 4.2, "starting fellowship": "Don't place any tokens into the twilight pool for the cards in
 * your starting fellowship.  Site text is not active when the starting fellowships are played."  And under
 * "active", as Legacy Ruling 3 has it: "Exception: Site text does not apply when the starting fellowships are
 * played."  Ketura, 2026-10-10, named the rule for the campaign.
 *
 * Site 1 is on the table before either player chooses a fellowship, and both fellowships begin there.  Its text
 * says nothing until the first turn begins: a cost it raises is not raised, a cost it lowers is not lowered, and
 * a companion played into a starting fellowship sets off no trigger of it.  The game has kept this since 2011
 * (F-0470); these tests hold it.
 *
 * Each board reads the same thing twice in one game: while a fellowship is chosen, where the site says nothing,
 * and in the first fellowship phase at that same site, where it speaks.  The second reading is the control: it
 * shows that the site, the card read and the board are ones the text reaches.  The game deals the opening hands
 * when the last player has chosen, and a test table's draw deck is the few cards its scenario names: so the card
 * left in the draw deck for the second reading is in its owner's opening hand when the first turn begins, and
 * is played from there.
 *
 * While a player chooses, the game keeps the running total of his fellowship in the twilight pool and offers a
 * companion only while the total with its cost is 4 or less (StartingFellowshipTests).  So a site whose text
 * changed a cost would show in three places at once: in the cost read from a companion still in the draw deck,
 * in the total, and in who is still offered.
 *
 * A meta-site (Race to Mount Doom) is not a site, and five of them are about the starting fellowship: this rule
 * does not reach them.
 */
public class SiteTextIsNotActiveWhileStartingFellowshipsArePlayedTests
{
	// Site 1 is East Road: "Each companion's twilight cost is +2."
	protected VirtualTableScenario GetCostRaisedScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Three companions whose texts touch no card's cost
					put("gimli", "2_121");      // Gimli, Dwarf of the Mountain-race: cost 2
					put("legolas", "3_121");    // Legolas, Son of Thranduil: cost 2
					put("pippin", "1_306");     // Pippin, Friend to Frodo: cost 1
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site1", "1_320");      // East Road
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Site 1 is Green Hill Country: "The twilight cost of each Hobbit is -1."
	protected VirtualTableScenario GetCostLoweredScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Three Hobbits whose texts touch no card's cost
					put("sam", "1_311");        // Sam, Son of Hamfast: cost 2
					put("merry", "1_303");      // Merry, From O'er the Brandywine: cost 1
					put("pippin", "1_306");     // Pippin, Friend to Frodo: cost 1
					// Aragorn, Ranger of the North: a Man of cost 4, who fits a starting fellowship only while
					// its total is 0
					put("aragorn", "1_89");
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site1", "1_323");      // Green Hill Country
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Site 1 is Eastemnet Downs: "Each time you play a companion here, you must discard 2 cards from hand."
	protected VirtualTableScenario GetTriggerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "2_121");      // Gimli, Dwarf of the Mountain-race: cost 2
					put("legolas", "3_121");    // Legolas, Son of Thranduil: cost 2
					// Hobbit Sword: a possession, so never offered for a starting fellowship; three of them are
					// a hand of which 2 must be chosen
					put("sword1", "1_299");
					put("sword2", "1_299");
					put("sword3", "1_299");
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.TowersSites);
					put("site1", "4_324");      // Eastemnet Downs
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ACostThatSiteOneRaisesIsNotRaisedWhileAStartingFellowshipIsChosenAndIsRaisedInTheFirstFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCostRaisedScenario();

		var eastRoad = scn.GetFreepsSite(1);
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var pippin = scn.GetFreepsCard("pippin");

		// East Road is on the table as site 1 and the first player is choosing
		assertEquals("East Road", eastRoad.getBlueprint().getTitle());
		assertEquals(eastRoad, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_PATH, eastRoad);
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertEquals(0, scn.GetTwilight());

		// Each companion costs what is printed on him.  Were the site's text in force they would cost 4, 4 and 3.
		assertEquals(2, scn.GetTwilightCost(gimli));
		assertEquals(2, scn.GetTwilightCost(legolas));
		assertEquals(1, scn.GetTwilightCost(pippin));

		scn.FreepsChooseCard(gimli);

		// The total is 2 and there is room for another.  At 4 for Gimli the choice would have ended with him.
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, pippin));

		scn.FreepsChooseCard(legolas);

		// 2 + 2 fills the 4: nobody else can be played, so the first player is asked nothing more and the second
		// player is choosing
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertInZone(Zone.DECK, pippin);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));

		// The second player takes nobody, the opening hands are dealt, and neither player takes a mulligan
		scn.ShadowDeclineChoosing();
		scn.StartGame(false, false);

		// The control.  The first fellowship phase, at the same East Road, with nothing in the twilight pool.
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(eastRoad, scn.GetCurrentSite());
		assertEquals(0, scn.GetTwilight());

		// The same Pippin, dealt to his owner's opening hand, now costs 1 + 2
		assertInHand(pippin);
		assertEquals(3, scn.GetTwilightCost(pippin));

		// and that is what playing him adds to the twilight pool
		scn.FreepsPlayCard(pippin);
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACostThatSiteOneRaisesIsNotRaisedWhileTheSecondPlayerChoosesEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCostRaisedScenario();

		var eastRoad = scn.GetFreepsSite(1);
		var gimli = scn.GetShadowCard("gimli");
		var legolas = scn.GetShadowCard("legolas");
		var pippin = scn.GetShadowCard("pippin");

		// The first player takes nobody
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		scn.FreepsDeclineChoosing();

		// The second player's fellowship begins at the same site 1, which the first player laid
		assertEquals(eastRoad, scn.GetCurrentSite());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
		assertEquals(0, scn.GetTwilight());

		// His companions cost what is printed on them as well
		assertEquals(2, scn.GetTwilightCost(gimli));
		assertEquals(2, scn.GetTwilightCost(legolas));
		assertEquals(1, scn.GetTwilightCost(pippin));

		scn.ShadowChooseCard(gimli);

		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(legolas, pippin));

		scn.ShadowChooseCard(pippin);

		// 2 + 1 leaves 1 of the 4: Legolas at 2 does not fit, nobody else can be played, and the game goes on to
		// the mulligans
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertNotInZone(Zone.FREE_CHARACTERS, legolas);
		assertFalse(scn.ShadowDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsDecisionAvailable("Do you wish to mulligan"));
	}

	@Test
	public void ACostThatSiteOneLowersIsNotLoweredWhileAStartingFellowshipIsChosenAndIsLoweredInTheFirstFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCostLoweredScenario();

		var greenHillCountry = scn.GetFreepsSite(1);
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");

		assertEquals("Green Hill Country", greenHillCountry.getBlueprint().getTitle());
		assertEquals(greenHillCountry, scn.GetCurrentSite());
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertEquals(0, scn.GetTwilight());

		// Each Hobbit costs what is printed on him.  Were the site's text in force they would cost 1, 0 and 0.
		assertEquals(2, scn.GetTwilightCost(sam));
		assertEquals(1, scn.GetTwilightCost(merry));
		assertEquals(1, scn.GetTwilightCost(pippin));
		// With nobody down Aragorn's 4 fits
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, merry, pippin, aragorn));

		scn.FreepsChooseCard(merry);

		// Merry has taken 1 of the 4, and Aragorn no longer fits.  Had Merry cost 0 he still would.
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));

		scn.FreepsChooseCard(pippin);

		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertEquals(2, scn.GetTwilightCost(sam));
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));

		// Sam is left in the draw deck to be read again.  The second player takes nobody, the opening hands are
		// dealt, and neither player takes a mulligan.
		assertInZone(Zone.DECK, sam);
		scn.FreepsDeclineChoosing();
		scn.ShadowDeclineChoosing();
		scn.StartGame(false, false);

		// The control.  The first fellowship phase, at the same Green Hill Country.
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(greenHillCountry, scn.GetCurrentSite());
		assertEquals(0, scn.GetTwilight());

		// The same Sam, dealt to his owner's opening hand, now costs 2 - 1
		assertInHand(sam);
		assertEquals(1, scn.GetTwilightCost(sam));

		// and that is what playing him adds to the twilight pool
		scn.FreepsPlayCard(sam);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// No player has a hand while the starting fellowships are played: the hands are dealt afterwards.  The three
	// cards are put in the first player's hand by the test so that the site's trigger, were it set off, would
	// have something to take and a choice to ask; nothing else here is cheated.
	@Test
	public void ATriggerOfSiteOneIsNotSetOffByACompanionPlayedIntoAStartingFellowshipAndIsSetOffInTheFirstFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTriggerScenario();

		var eastemnetDowns = scn.GetFreepsSite(1);
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var sword3 = scn.GetFreepsCard("sword3");

		assertEquals("Eastemnet Downs", eastemnetDowns.getBlueprint().getTitle());
		assertEquals(eastemnetDowns, scn.GetCurrentSite());
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));

		scn.MoveCardsToHand(sword1, sword2, sword3);
		assertEquals(3, scn.GetFreepsHandCount());

		scn.FreepsChooseCard(gimli);

		// A companion has been played at Eastemnet Downs by the player whose fellowship is there.  No discard is
		// asked for and none is made: the next thing asked is the next companion.
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas));
		assertEquals(3, scn.GetFreepsHandCount());
		assertInHand(sword1, sword2, sword3);
		assertEquals(0, scn.GetFreepsDiscardCount());

		// Legolas is left in the draw deck to be played in the game.  The second player takes nobody, the opening
		// hands are dealt, and neither player takes a mulligan.
		scn.FreepsDeclineChoosing();
		scn.ShadowDeclineChoosing();
		scn.StartGame(false, false);

		// The control.  The first fellowship phase, at the same Eastemnet Downs, with the same three cards in hand
		// and Legolas dealt to them.
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(eastemnetDowns, scn.GetCurrentSite());
		assertEquals(4, scn.GetFreepsHandCount());
		assertInHand(sword1, sword2, sword3, legolas);

		scn.FreepsPlayCard(legolas);

		// A companion played here now sets the trigger off: 2 of the 3 cards left in hand must be chosen
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertTrue(scn.FreepsDecisionAvailable("Choose cards from hand to discard"));
		assertEquals(3, scn.FreepsGetCardChoiceCount());

		scn.FreepsChooseCards(sword1, sword2);

		assertInDiscard(sword1, sword2);
		assertInHand(sword3);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
