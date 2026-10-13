package com.gempukku.lotro.mechanics.choices;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Choosing how many to spot.
 *
 * Comprehensive Rules 4.2, "spot": "Normally, you don't have to spot all the cards in play that meet the
 * requirement if you don't want to.  If a card says, 'for each Elf you spot' and there are 2 Elves in play (and
 * active), you may choose to spot 2 Elves, 1 Elf, or none."  And: "To spot a 'Free Peoples culture' means to spot
 * any Free Peoples card of that culture."
 *
 * So where a card counts what its player spots ("spot X burdens", "for each threat you spot"), the player names a
 * number from none to all there are, and the card goes by the number named.  Each test here puts more than one of
 * the thing on the table and names fewer than there are, since a card that counted everything would read the same
 * as one that counted the number named when the player names them all.  The four are burdens, Free Peoples
 * cultures, threats and twilight tokens: each is counted by the game in a way of its own.
 *
 * ("You can spot" is not the player's to count short, and is each card's own to show.)
 */
public class ChoosingHowManyToSpotTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Enduring Evil: [sauron] Skirmish event, cost 0.  "Spot X burdens to make a character
					// skirmishing a [sauron] Orc strength -X."
					put("evil", "1_246");
					// Orc Inquisitor: [sauron] Orc, strength 9.  Its text answers its being played, and it
					// is put on the table by the cheat.
					put("inquisitor", "1_268");

					// Merciless Berserker: [uruk-hai] minion, vitality 2, damage +1.  "Shadow: Exert this
					// minion to exert X companions and add X threats, where X is the number of Free Peoples
					// cultures you spot over 2."
					put("berserker", "15_165");

					// So Polite: [gollum] Shadow event, cost 0.  "Play Gollum from your discard pile to add
					// (1) for each threat you spot."
					put("polite", "7_74");
					// Gollum, Plotting Deceiver: cost 2, site 3.  His text plays him from the discard pile
					// when its owner has initiative, and is not used.
					put("gollum", "7_58");

					// Under the Living Earth: [gandalf] Skirmish event, cost 1.  "Make Gandalf strength +1
					// for each twilight token you spot (limit +5)."
					put("living", "4_105");
					// Gandalf, Friend of the Shirefolk: strength 6, and +1 for each of Hobbit, Dwarf, Elf
					// and Man among the companions.
					put("gandalf", "1_72");
					put("runner", "1_178");     // Goblin Runner: strength 5; cheated in, so its text never comes up

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor], strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven]
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven]
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire], as the Ring-bearer is
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SpottingBurdensThePlayerNamesAnyNumberFromNoneToAllThereAreAndTheCardCountsTheNumberNamed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var evil = scn.GetShadowCard("evil");
		var inquisitor = scn.GetShadowCard("inquisitor");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(evil);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(inquisitor);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, inquisitor);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(3, scn.GetBurdens());
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowPlayAvailable(evil));
		scn.ShadowPlayCard(evil);

		// None, or any number up to the 3 there are.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());

		// Two are named, and Aragorn is 2 weaker and not 3.
		scn.ShadowChoose("2");
		assertEquals(6, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetBurdens());
		assertInZone(Zone.DISCARD, evil);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SpottingFreePeoplesCulturesThePlayerNamesAnyNumberFromNoneToAllThereAreAndTheCardCountsTheNumberNamed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		// Five Free Peoples cards of four cultures: [shire] twice, [gondor], [dwarven] and [elven].  The One
		// Ring has no culture.
		scn.MoveCompanionsToTable(sam, aragorn, gimli, legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(berserker);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(berserker));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(berserker));
		scn.ShadowUseCardAction(berserker);
		assertEquals(1, scn.GetWoundsOn(berserker));

		// A culture is spotted once, however many cards show it: 4 and not 5.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());

		// Three are named, one over 2: one companion is exerted and one threat added, where all four would
		// have made it two of each.
		scn.ShadowChoose("3");
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, sam, aragorn, gimli, legolas));

		scn.ShadowChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingThreatsThePlayerNamesAnyNumberFromNoneToAllThereAreAndTheCardCountsTheNumberNamed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var polite = scn.GetShadowCard("polite");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Two companions, so that there may be two threats.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(polite);
		scn.MoveCardsToDiscard(gollum);

		scn.StartGame();
		scn.AddThreats(2);
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetThreats());

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(polite));
		scn.ShadowPlayCard(polite);

		// The event's cost: Gollum is played from the discard pile, for his 2 and 2 more for roaming at
		// site 2 (his site number is 3).
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertEquals(1, scn.GetTwilight());

		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());

		// One threat is named, and one token added and not two.
		scn.ShadowChoose("1");
		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.DISCARD, polite);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingTwilightTokensThePlayerNamesAnyNumberUpToThePoolWithTheEventsOwnCostInIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var living = scn.GetFreepsCard("living");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(living);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, runner);
		scn.FreepsResolveSkirmish(gandalf);

		scn.SetTwilight(3);
		// 6, and 1 for the Hobbit beside him
		assertEquals(7, scn.GetStrength(gandalf));
		assertTrue(scn.FreepsPlayAvailable(living));
		scn.FreepsPlayCard(living);

		// The rules on this card (CR 4.2, individual rulings): "The twilight token added for the cost of this
		// card adds to the strength bonus provided by its effect."  So there are 3 + 1 to spot.
		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());

		// Two are named, and Gandalf is 2 stronger and not 4.
		scn.FreepsChoose("2");
		assertEquals(9, scn.GetStrength(gandalf));
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.DISCARD, living);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
