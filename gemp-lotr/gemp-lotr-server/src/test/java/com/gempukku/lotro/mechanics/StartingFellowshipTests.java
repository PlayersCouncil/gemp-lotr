package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * The starting fellowship: the companions a player plays from his draw deck before the game begins.
 *
 * Comprehensive Rules 4.2, "starting fellowship": "You may then play other companions (not allies, possessions,
 * artifacts, or conditions) one at a time from your draw deck, as long as the total twilight cost of these
 * companions is 4 or less.  The twilight cost of your Ring-bearer is not included in this total.  Don't place any
 * tokens into the twilight pool for the cards in your starting fellowship.  One companion you play may effect the
 * play of a later companion you play."  And its example: "Some versions of Éomer cost 3, but read, 'While you can
 * spot a [Rohan] Man, Éomer's twilight cost is -1.'  If you play another [Rohan] Man who costs 2 first, you can
 * spot him to play Éomer, as the total cost of your starting companions will be 4 instead of 5."
 *
 * So the 4 is a limit on the sum of what the companions cost as each is played, a companion who would carry the
 * sum past it is not offered, and one who has gone down is on the table for the next: he is spotted for a later
 * companion's "To play, spot ..." and for a later companion's discount.
 *
 * The game asks for one companion at a time and stops asking when no companion in the draw deck can be played.
 * It keeps the running total in the twilight pool while a player chooses, begins the next player's total at
 * nothing, and empties the pool as the first turn begins.  So "don't place any tokens" is read here in the first
 * fellowship phase, after BOTH players have played companions, and nowhere before it.  Both players at a test
 * table have the same deck: when the first has done the second is asked, and the question about a mulligan comes
 * after both.
 *
 * That only Free Peoples cards go into a starting fellowship is StartingFellowshipIsFreePeoplesCardsTests; that
 * a Frodo is kept out of another Ring-bearer's is FrodoCannotJoinAnotherRingBearerTests.
 */
public class StartingFellowshipTests
{
	protected VirtualTableScenario GetLimitScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Three companions whose texts touch no card's cost
					put("gimli", "2_121");      // Gimli, Dwarf of the Mountain-race: cost 2
					put("legolas", "3_121");    // Legolas, Son of Thranduil: cost 2
					put("pippin", "1_306");     // Pippin, Friend to Frodo: cost 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same draw deck behind a Ring-bearer who has a twilight cost: Galadriel, Bearer of Wisdom costs 3.
	protected VirtualTableScenario GetCostlyRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "2_121");      // Gimli, Dwarf of the Mountain-race: cost 2
					put("legolas", "3_121");    // Legolas, Son of Thranduil: cost 2
					put("pippin", "1_306");     // Pippin, Friend to Frodo: cost 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDiscountScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Elite Rider: [rohan] Man, cost 3.  "When this companion is in your starting fellowship, his
					// twilight cost is -1."
					put("rider", "4_265");
					// Éomer, Third Marshal of Riddermark: [rohan] Man, cost 3.  "While you can spot a [rohan] Man,
					// Éomer's twilight cost is -1."
					put("eomer", "4_267");
					put("gimli", "1_13");       // Gimli, Son of Glóin: cost 2, a Dwarf and no [rohan] Man
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSpotScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf, cost 2, nothing needed to play him
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: cost 2.  "To play, spot an Elf."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CompanionsArePlayedFromTheDrawDeckUntilTheirCostsReachFourAndThenTheChoiceEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLimitScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var pippin = scn.GetFreepsCard("pippin");

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertInZone(Zone.DECK, gimli, legolas, pippin);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, legolas, pippin));
		// "One at a time", and "may": none need be taken
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(gimli);

		// 2 of the 4: either of the others still fits
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, pippin));

		scn.FreepsChooseCard(legolas);

		// 2 + 2 is the 4.  Pippin's 1 would make it 5: nobody is left to offer, and the player is asked nothing
		// more.  The other player is choosing now.
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertInZone(Zone.DECK, pippin);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));

		// The other player plays a companion of his own and stops there, so that his total is the last the game
		// kept
		var hisGimli = scn.GetShadowCard("gimli");
		scn.ShadowChooseCard(hisGimli);
		assertInZone(Zone.FREE_CHARACTERS, hisGimli);
		scn.ShadowDeclineChoosing();

		// Both have chosen, and the game goes on to the mulligans
		assertTrue(scn.FreepsDecisionAvailable("Do you wish to mulligan"));

		scn.StartGame(true, false);

		// "Don't place any tokens into the twilight pool for the cards in your starting fellowship": the first
		// turn begins with an empty pool, neither the first player's 4 in it nor the second player's 2
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.FREE_CHARACTERS, gimli, legolas);
		assertEquals(0, scn.GetTwilight());
	}

	@Test
	public void ACompanionWhoseCostWouldCarryTheTotalPastFourIsNotOfferedThoughTheTotalIsBelowFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLimitScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var pippin = scn.GetFreepsCard("pippin");

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, legolas, pippin));

		scn.FreepsChooseCard(gimli);

		// At 2 Legolas is still offered: it is not his own text that keeps him out below
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, pippin));

		scn.FreepsChooseCard(pippin);

		// 2 + 1 is 3, one short of the limit, and Legolas' 2 would make it 5.  He is the only companion left
		// in the draw deck, so the choice is over with the total at 3.
		assertInZone(Zone.FREE_CHARACTERS, gimli, pippin);
		assertInZone(Zone.DECK, legolas);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
	}

	@Test
	public void TheRingBearersTwilightCostIsNotCountedInTheFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCostlyRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var pippin = scn.GetFreepsCard("pippin");

		// The Ring-bearer is down already, and she costs 3
		assertInZone(Zone.FREE_CHARACTERS, galadriel);
		assertEquals(3, scn.GetTwilightCost(galadriel));

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, legolas, pippin));

		scn.FreepsChooseCard(gimli);

		// Were her 3 counted, Gimli's 2 would have made 5 and nobody would be offered after him
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, pippin));

		scn.FreepsChooseCard(legolas);

		// The whole 4 was there to spend on the others
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertInZone(Zone.DECK, pippin);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
	}

	@Test
	public void ACompanionAlreadyPlayedLowersTheCostOfALaterOneAndTheLimitOfFourCountsTheLoweredCosts() throws DecisionResultInvalidException, CardNotFoundException {
		// The rulebook's own example, with a [rohan] Man who costs 2 only in a starting fellowship
		var scn = GetDiscountScenario();

		var rider = scn.GetFreepsCard("rider");
		var eomer = scn.GetFreepsCard("eomer");
		var gimli = scn.GetFreepsCard("gimli");

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, eomer, gimli));
		// The Rider's own text has him at 3 - 1.  Nobody is on the table for Éomer to spot: he costs his 3.
		assertEquals(2, scn.GetTwilightCost(rider));
		assertEquals(3, scn.GetTwilightCost(eomer));

		scn.FreepsChooseCard(rider);

		// The Rider is a [rohan] Man in play, and Éomer costs 3 - 1.  2 + 2 is the 4: he is still offered, which
		// he would not be at the Rider's printed 3 or at his own.
		assertInZone(Zone.FREE_CHARACTERS, rider);
		assertEquals(2, scn.GetTwilightCost(eomer));
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(eomer, gimli));

		scn.FreepsChooseCard(eomer);

		// The 4 is spent, and Gimli's 2 is not offered
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		assertInZone(Zone.DECK, gimli);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));

		scn.ShadowDeclineChoosing();

		assertTrue(scn.FreepsDecisionAvailable("Do you wish to mulligan"));
	}

	@Test
	public void WithNobodyPlayedWhoLowersItALaterCompanionsCostIsCountedInFull() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscountScenario();

		var rider = scn.GetFreepsCard("rider");
		var eomer = scn.GetFreepsCard("eomer");
		var gimli = scn.GetFreepsCard("gimli");

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, eomer, gimli));

		scn.FreepsChooseCard(gimli);

		// Gimli cost 2 and is no [rohan] Man.  Éomer still costs 3 and would make 5: not offered.  The Rider,
		// at 2 by his own text, is.
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(3, scn.GetTwilightCost(eomer));
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(eomer));

		scn.FreepsChooseCard(rider);

		// There is a [rohan] Man to spot now, but 2 + 2 is the 4 and Éomer at any cost is past it
		assertInZone(Zone.FREE_CHARACTERS, rider);
		assertInZone(Zone.DECK, eomer);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
	}

	@Test
	public void ACompanionWhoMustSpotToBePlayedIsOfferedOnlyOnceAnEarlierCompanionMeetsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSpotScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var haldir = scn.GetFreepsCard("haldir");

		// The Ring-bearer is a Hobbit: there is no Elf for Haldir to spot
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertInZone(Zone.DECK, legolas, haldir);
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(haldir));

		scn.FreepsChooseCard(legolas);

		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(haldir));

		scn.FreepsChooseCard(haldir);

		assertInZone(Zone.FREE_CHARACTERS, haldir);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));

		scn.ShadowDeclineChoosing();

		assertTrue(scn.FreepsDecisionAvailable("Do you wish to mulligan"));
	}
}
