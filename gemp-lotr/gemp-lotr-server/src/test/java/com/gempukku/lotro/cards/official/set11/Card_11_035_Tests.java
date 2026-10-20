package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_035_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("glamdring", "11_35");
					// Gandalf, Friend of the Shirefolk: cost 4, strength 6, +1 for each race he can spot in the
					// fellowship (Frodo's Hobbit: 7).
					put("gandalf", "1_72");
					put("avenger", "6_27");       // Ent Avenger: a [gandalf] companion who is not Gandalf, strength 6

					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3, damage +1 and nothing else
					put("pursuer", "4_190");      // Uruk Pursuer: strength 5, vitality 3, fierce; its text is about unbound Hobbits
					put("brute", "4_17");         // Dunlending Savage: strength 11, vitality 1 and no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GlamdringStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Glamdring, Foe-hammer
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be Gandalf.
		 * 	Each time Gandalf wins a skirmish, you may remove a burden.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("glamdring");

		assertEquals("Glamdring", card.getBlueprint().getTitle());
		assertEquals("Foe-hammer", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void CanBePlayedOnlyOnGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(gandalf, glamdring);

		scn.StartGame();

		// Frodo and a [gandalf] companion of another title: nobody may bear it.
		assertFalse(scn.FreepsPlayAvailable(glamdring));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(glamdring));

		// Gandalf is the only bearer it may have, so nobody is asked.
		scn.FreepsPlayCard(glamdring);
		assertAttachedTo(glamdring, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeGandalfWinsASkirmishABurdenMayBeRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring);

		scn.StartGame();
		scn.AddBurdens(2);
		// A fierce minion that loses and lives gives Gandalf two skirmishes to win in one turn.
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, pursuer);
		scn.FreepsResolveSkirmish(gandalf);

		assertEquals(2, scn.GetBurdens());
		scn.PassSkirmishActions();

		// 7 + 2 against 5: he wins.
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glamdring));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gandalf, pursuer);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassFierceSkirmishActions();

		// He wins again, and the offer is made again; declined, the burden stays.
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glamdring));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenAnotherCompanionWinsASkirmishNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.AttachCardsTo(gandalf, glamdring);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(avenger, savage);
		scn.FreepsResolveSkirmish(avenger);

		assertEquals(2, scn.GetBurdens());
		scn.PassSkirmishActions();

		// A [gandalf] companion who is no Gandalf wins, 6 against 5, while Gandalf bears Glamdring.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenGandalfLosesASkirmishNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var brute = scn.GetShadowCard("brute");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(brute);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, brute);
		scn.FreepsResolveSkirmish(gandalf);

		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.PassSkirmishActions();

		// 7 + 2 against 11: he loses, and takes one wound from a minion with no damage bonus.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(brute));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoBurdenToRemoveNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, savage);
		scn.FreepsResolveSkirmish(gandalf);

		assertEquals(0, scn.GetBurdens());
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
