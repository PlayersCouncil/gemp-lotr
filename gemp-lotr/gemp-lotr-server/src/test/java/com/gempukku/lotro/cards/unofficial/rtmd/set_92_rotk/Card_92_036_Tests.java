package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInDiscard;
import static com.gempukku.lotro.framework.Assertions.assertInPlay;
import static org.junit.Assert.*;

public class Card_92_036_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");        // Gimli, Son of Gloin: unique [dwarven] Dwarf companion, twilight 2
					put("guard1", "1_7");        // Dwarf Guard: [dwarven] Dwarf companion, not unique, twilight 1; to play, spot a Dwarf
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
					put("guard5", "1_7");
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: unique [gondor] Man companion, twilight 4; no requirement to play
					put("bounder1", "1_286");    // Bounder: [shire] Hobbit ally, not unique, twilight 1; a skirmish ability only
					put("bounder2", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_36", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");        // Gimli, Son of Gloin: unique [dwarven] Dwarf companion, twilight 2
					put("guard1", "1_7");        // Dwarf Guard: [dwarven] Dwarf companion, not unique, twilight 1; to play, spot a Dwarf
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
					put("guard5", "1_7");
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: unique [gondor] Man companion, twilight 4; no requirement to play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_36"
		);
	}

	// Galadriel bears the Ring, and Frodo is a companion like any other.
	protected VirtualTableScenario GetGaladrielScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("frodo", "1_290");       // Frodo, Son of Drogo: [shire] Hobbit companion; here he is not the Ring-bearer
					put("sam", "1_311");         // Sam, Son of Hamfast: [shire] Hobbit companion; Ring-bound, and not the Ring-bearer
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man companion
					put("bounder", "1_286");     // Bounder: [shire] Hobbit ally
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing,
				"92_36", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_36
		 * Type: MetaSite
		 * Intensity: 5
		 * Game Text: You cannot have more than 5 companions in your fellowship.
		 *   Fellowship: Exert the Ring-bearer to discard another companion.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_36", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(5, card.getBlueprint().getIntensity());
	}

	@Test
	public void CanPlayUpTo5CompanionsButNot6th() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var guard4 = scn.GetFreepsCard("guard4");
		var guard5 = scn.GetFreepsCard("guard5");
		var aragorn = scn.GetFreepsCard("aragorn");

		// Frodo is companion #1. Gimli + 3 guards on table = 5 total.
		scn.MoveCompanionsToTable(gimli, guard1, guard2, guard3);
		scn.MoveCardsToHand(guard4, guard5, aragorn);

		scn.StartGame();

		// Frodo + Gimli + guard1 + guard2 + guard3 = 5. Already at 5.
		// guard4 should NOT be playable — already spotting 5 companions. Nor should a companion of
		// another race and culture (Aragorn, a Man): the limit is on companions, not on Dwarves.
		assertFalse(scn.FreepsPlayAvailable(guard4));
		assertFalse(scn.FreepsPlayAvailable(guard5));
		assertFalse(scn.FreepsPlayAvailable(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanPlayThe5thCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");

		// Frodo + Gimli + 2 guards on table = 4, guard3 in hand to play as #5
		scn.MoveCompanionsToTable(gimli, guard1, guard2);
		scn.MoveCardsToHand(guard3);

		scn.StartGame();

		// 4 companions in play, should be able to play the 5th
		assertTrue(scn.FreepsPlayAvailable(guard3));
		scn.FreepsPlayCard(guard3);

		assertInPlay(guard3);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionExertsRingBearerToDiscardCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var mod = scn.GetFreepsCard("mod");

		scn.MoveCompanionsToTable(gimli, guard1, guard2);

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(mod));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsUseCardAction(mod);

		// Ring-bearer should be exerted
		assertEquals(1, scn.GetWoundsOn(frodo));

		// Should be choosing a companion to discard — ring-bearer should NOT be a choice
		assertFalse(scn.FreepsHasCardChoiceAvailable(frodo));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli));
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard1));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(guard1);

		assertInDiscard(guard1);
		assertInPlay(gimli);
		assertInPlay(guard2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OpponentNotAffectedByCompanionLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var guard4 = scn.GetFreepsCard("guard4");
		var guard5 = scn.GetFreepsCard("guard5");
		var aragorn = scn.GetFreepsCard("aragorn");

		// Frodo + Gimli + 3 guards = 5 companions already
		scn.MoveCompanionsToTable(gimli, guard1, guard2, guard3);
		scn.MoveCardsToHand(guard4, aragorn);

		scn.StartGame();

		// Shadow owns the mod, so FP should not be restricted — can play 6th companion, Aragorn as well
		// as a Guard
		assertTrue(scn.FreepsPlayAvailable(aragorn));
		assertTrue(scn.FreepsPlayAvailable(guard4));
		scn.FreepsPlayCard(guard4);
		assertInPlay(guard4);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OpponentCantUseFellowshipAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var mod = scn.GetShadowCard("mod");

		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();

		// Frodo could exert and Gimli could be discarded: it is the modifier's owner who is asked for.
		assertFalse(scn.FreepsActionAvailable(mod));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnAllyIsNotCountedAsACompanionAndMayStillBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var guard4 = scn.GetFreepsCard("guard4");
		var bounder1 = scn.GetFreepsCard("bounder1");
		var bounder2 = scn.GetFreepsCard("bounder2");

		// Frodo + Gimli + 2 guards = 4 companions, and a Bounder who is a fifth character but no companion.
		scn.MoveCompanionsToTable(gimli, guard1, guard2);
		scn.MoveAlliesToTable(bounder1);
		scn.MoveCardsToHand(guard3, guard4, bounder2);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(guard3));
		scn.FreepsPlayCard(guard3);
		assertInPlay(guard3);

		// 5 companions now: no sixth, but another ally is no companion and is played.
		assertFalse(scn.FreepsPlayAvailable(guard4));
		assertTrue(scn.FreepsPlayAvailable(bounder2));
		scn.FreepsPlayCard(bounder2);
		assertInPlay(bounder2);

		assertFalse(scn.FreepsPlayAvailable(guard4));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardingACompanionMakesRoomForAnotherAndAnExhaustedRingBearerCannotPay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var guard4 = scn.GetFreepsCard("guard4");
		var mod = scn.GetFreepsCard("mod");

		// Frodo + Gimli + 3 guards = 5 companions. Frodo has vitality 4: with 2 wounds he can exert once more.
		scn.MoveCompanionsToTable(gimli, guard1, guard2, guard3);
		scn.MoveCardsToHand(guard4);
		scn.AddWoundsToChar(frodo, 2);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(guard4));
		assertTrue(scn.FreepsActionAvailable(mod));

		scn.FreepsUseCardAction(mod);
		assertEquals(3, scn.GetWoundsOn(frodo));
		scn.FreepsChooseCard(guard1);
		assertInDiscard(guard1);

		// 4 companions again: the Guard in hand may now be played.
		assertTrue(scn.FreepsPlayAvailable(guard4));

		// Frodo is exhausted. Gimli and two Guards are still there to be discarded, but the
		// exertion cannot be paid.
		assertFalse(scn.FreepsActionAvailable(mod));
		assertInPlay(gimli);
		assertInPlay(guard2);
		assertInPlay(guard3);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARingBearerWhoIsNotFrodoExertsAndFrodoMayBeTheCompanionDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielScenario();

		var galadriel = scn.GetRingBearer();
		var frodo = scn.GetFreepsCard("frodo");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var mod = scn.GetFreepsCard("mod");

		scn.MoveCompanionsToTable(frodo, sam, aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		// Sam is Ring-bound and no Ring-bearer: it is Galadriel who exerts, not he and not Frodo.
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Any other companion may go, of whatever culture, Frodo among them; the Ring-bearer
		// herself and an ally may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(galadriel, bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(frodo);

		assertInDiscard(frodo);
		assertInPlay(sam);
		assertInPlay(aragorn);
		assertInPlay(bounder);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
