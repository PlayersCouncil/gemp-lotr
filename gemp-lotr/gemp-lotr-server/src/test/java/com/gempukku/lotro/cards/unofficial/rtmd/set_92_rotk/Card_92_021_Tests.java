package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_92_021_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] companion, cost 4
					put("aragorn", "10_25");    // Aragorn, Elessar Telcontar: [gondor] companion, cost 5; when played, may heal another companion (nobody here is wounded: no offer)
					put("guard", "1_7");        // Dwarf Guard: [dwarven] companion, cost 1; to play, spot a Dwarf
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, cost 1; a Free Peoples character who is no companion
					put("sword", "1_299");      // Hobbit Sword: [shire] possession, cost 1; bearer must be a Hobbit (Frodo)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_21", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] companion, cost 4
					put("aragorn", "10_25");    // Aragorn, Elessar Telcontar: [gondor] companion, cost 5; when played, may heal another companion (nobody here is wounded: no offer)
					put("guard", "1_7");        // Dwarf Guard: [dwarven] companion, cost 1; to play, spot a Dwarf
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, cost 1; a Free Peoples character who is no companion
					put("sword", "1_299");      // Hobbit Sword: [shire] possession, cost 1; bearer must be a Hobbit (Frodo)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_21"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_21
		 * Type: MetaSite
		 * Intensity: -3
		 * Game Text: Fellowship: Play a companion; their twilight cost is -4.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_21", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-3, card.getBlueprint().getIntensity());
	}

	@Test
	public void PlayCompanionWithDiscount() throws DecisionResultInvalidException, CardNotFoundException {
		// Gandalf costs 4, discount of 4 means he should cost 0.
		// Aragorn costs 5, discount of 4 means he should cost 1.

		var scn = GetFreepsScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToHand(gandalf, aragorn);

		scn.StartGame();

		var mod = scn.GetFreepsCard("mod");
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		// Both should be offered
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsChooseCard(gandalf);

		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(0, scn.GetTwilight());

		// The action has no limit: it is used again, and Aragorn, the one companion left in hand, is not asked for.
		scn.FreepsUseCardAction(mod);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(1, scn.GetTwilight());

		// Aragorn's own "when you play Aragorn, you may heal another companion" is not offered: nobody is wounded.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOwnerGated() throws DecisionResultInvalidException, CardNotFoundException {
		// Shadow owns the modifier, but Freeps should still be able to use it
		var scn = GetShadowScenario();

		var gandalf = scn.GetFreepsCard("gandalf");

		scn.MoveCardsToHand(gandalf);

		scn.StartGame();

		var mod = scn.GetShadowCard("mod");
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(mod));

		// Gandalf is the one companion in hand: he is played for 4 - 4.
		scn.FreepsUseCardAction(mod);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWithNoCompanionInHandThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var guard = scn.GetFreepsCard("guard");
		var bounder = scn.GetFreepsCard("bounder");
		var sword = scn.GetFreepsCard("sword");

		scn.MoveCardsToHand(guard, bounder, sword);

		scn.StartGame();

		// An ally and a possession that could be played, and a companion who cannot: there is no Dwarf to spot.
		assertTrue(scn.FreepsPlayAvailable(bounder));
		assertTrue(scn.FreepsPlayAvailable(sword));
		assertFalse(scn.FreepsPlayAvailable(guard));
		assertFalse(scn.FreepsActionAvailable(mod));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OffersEachCompanionInHandAndNoOtherCardAndDiscountsOnlyThePlayItMakes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var bounder = scn.GetFreepsCard("bounder");
		var sword = scn.GetFreepsCard("sword");

		scn.MoveCardsToHand(gandalf, aragorn, guard, bounder, sword);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		// The two companions who can be played, of two cultures, and nothing else: not the ally, not the
		// possession, and not the companion who has no Dwarf to spot.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, bounder, sword));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(aragorn);

		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertInZone(Zone.HAND, gandalf);
		// 5 to play - 4
		assertEquals(1, scn.GetTwilight());

		// The discount is for the companion the action plays.  Gandalf, played from hand in the ordinary way
		// straight after, pays all of his 4.
		scn.FreepsPlayCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
