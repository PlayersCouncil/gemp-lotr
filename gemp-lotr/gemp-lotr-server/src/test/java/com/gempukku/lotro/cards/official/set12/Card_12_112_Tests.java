package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_112_Tests
{

	// Site 1 is Bag End, which changes no twilight cost.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eomer", "12_112");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, valiant and nothing else
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ALLY, cost 1; a fellowship ability used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit companion, a Man only while 3 [rohan] companions or Eowyn can be spotted
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, cost 4; a maneuver ability used only on purpose
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally; a skirmish ability used only on purpose
					put("runner1", "1_178");    // Goblin Runner: a minion whose only text is on being played
					put("runner2", "1_178");
					put("runner3", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EomerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Éomer, Éored Leader
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: While you can spot a [rohan] Man, Éomer's twilight cost is -1.<br><b>Maneuver:</b> If you can spot more minions than companions, exert Éomer to play a [rohan] companion from your draw deck.
		*/

		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");

		assertEquals("Éomer", eomer.getBlueprint().getTitle());
		assertEquals("Éored Leader", eomer.getBlueprint().getSubtitle());
		assertTrue(eomer.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, eomer.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, eomer.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, eomer.getBlueprint().getCardType());
		assertEquals(Race.MAN, eomer.getBlueprint().getRace());
		assertEquals(3, eomer.getBlueprint().getTwilightCost());
		assertEquals(7, eomer.getBlueprint().getStrength());
		assertEquals(3, eomer.getBlueprint().getVitality());
		assertEquals(7, eomer.getBlueprint().getResistance());
	}

	@Test
	public void WithARohanManCompanionToSpotEomerIsPlayedForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(eomer, aragorn);
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();

		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertEquals(2, scn.GetTwilightCost(eomer));    // 3 - 1
		// Another companion in the hand, beside the [rohan] Man he spots, costs what it is printed at.
		assertEquals(4, scn.GetTwilightCost(aragorn));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(eomer);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanHobbitAndAGondorManDoNotLowerHisCostAndARohanManAllyPlayedThenDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guma = scn.GetFreepsCard("guma");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(eomer, guma);
		scn.MoveCompanionsToTable(merry, aragorn);

		scn.StartGame();

		// A [rohan] companion who is no Man, and a Man who is not [rohan].
		assertInZone(Zone.FREE_CHARACTERS, merry, aragorn);
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertEquals(3, scn.GetTwilightCost(eomer));

		scn.FreepsPlayCard(guma);

		assertInZone(Zone.SUPPORT, guma);
		assertEquals(2, scn.GetTwilightCost(eomer));    // 3 - 1
		int pool = scn.GetTwilight();

		scn.FreepsPlayCard(eomer);

		assertEquals(pool + 2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		// Two [rohan] companions: Merry is still no Man.
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverAbilityNeedsMoreMinionsThanCompanionsAndPlaysARohanCompanionFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var bounder = scn.GetFreepsCard("bounder");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var guma = scn.GetFreepsCard("guma");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		scn.MoveCompanionsToTable(eomer);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.SetFreepsDrawDeck(guard, merry, guma, aragorn);
		assertEquals(4, scn.GetFreepsDeckCount());
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.MANEUVER);

		// Two companions (Frodo and Éomer) and an ally against two minions: no more minions than companions.
		assertInZone(Zone.SUPPORT, bounder);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(eomer));

		// A third minion: three against two companions. The ally is no companion (counted as one, it would be three
		// against three).
		scn.MoveMinionsToTable(runner3);
		scn.FreepsRefreshDecision();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertTrue(scn.FreepsActionAvailable(eomer));

		scn.FreepsUseCardAction(eomer);
		assertEquals(1, scn.GetWoundsOn(eomer));

		scn.FreepsDismissRevealedCards();
		// [rohan] companions of two races; not a [rohan] ally, nor a companion of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guma, aragorn));
		// A play from the draw deck may always find nothing (Q-0019).
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(guard);

		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertEquals(3, scn.GetFreepsDeckCount());
		// The deck looked through is shuffled: once as the card leaves it and once more after.
		assertEquals(freepsShuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoRohanCompanionInTheDrawDeckEomerExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guma = scn.GetFreepsCard("guma");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		scn.MoveCompanionsToTable(eomer);

		scn.StartGame();
		scn.SetFreepsDrawDeck(guma, aragorn);
		assertEquals(2, scn.GetFreepsDeckCount());
		scn.MoveMinionsToTable(runner1, runner2, runner3);
		scn.SkipToPhase(Phase.MANEUVER);

		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertTrue(scn.FreepsActionAvailable(eomer));
		scn.FreepsUseCardAction(eomer);

		assertEquals(1, scn.GetWoundsOn(eomer));
		// Shown, and nothing can be chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guma, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, false));
		scn.FreepsDismissRevealedCards();

		assertEquals(2, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, guma, aragorn);
		// Looked through and shuffled once.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhileEomerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		scn.MoveCompanionsToTable(eomer);

		scn.StartGame();
		scn.SetFreepsDrawDeck(guard);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(eomer, 2);
		assertEquals(2, scn.GetWoundsOn(eomer));
		scn.MoveMinionsToTable(runner1, runner2, runner3);
		scn.SkipToPhase(Phase.MANEUVER);

		// Three minions against two companions, and a [rohan] companion in the deck.
		assertInZone(Zone.DECK, guard);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(eomer));
		assertEquals(2, scn.GetWoundsOn(eomer));
	}
}
