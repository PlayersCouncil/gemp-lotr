package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_024_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "101_24");
					// Aragorn, King in Exile: strength 8, vitality 4, and himself a companion with the Aragorn
					// signet.  His own text is only an offer to heal at the start of his owner's turn.
					put("aragorn", "1_365");
					// Aragorn, Defender of Rohan: a [rohan] companion with no signet, strength 8, vitality 4.
					// His text gives [rohan] companions hunter while the Ring-bearer is assigned to a skirmish,
					// which he never is here.
					put("defender", "17_93");
					// Arwen, Daughter of Elrond: [elven] Elf, strength 6, vitality 3, Aragorn signet.  She is
					// strength +3 while skirmishing a Nazgul and has no other text.
					put("arwen", "1_30");
					// Boromir, Lord of Gondor: [gondor] Man, strength 7, vitality 3, Aragorn signet.  Not
					// overwhelmed unless his strength is tripled.
					put("boromir", "1_96");
					// Sam, Son of Hamfast: [shire] Hobbit, Ring-bound, Aragorn signet.  A fellowship ability and
					// a response to Frodo's death, neither used here.
					put("sam", "1_311");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion with the Gandalf signet
					put("guard", "1_7");        // Dwarf Guard: a companion with no signet

					// Ulaire Nelya, Lieutenant of Morgul: Nazgul, strength 10, vitality 3, fierce.  A Shadow
					// phase ability, never used here.
					put("nelya", "1_233");
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: a companion with the Frodo signet
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void YouHaveMySwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: You Have My Sword
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Strength: +2
		 * Game Text: To play, spot Aragorn. Plays on a companion with the Aragorn signet (except Aragorn).
		 * Each time bearer wins a skirmish, exert Aragorn or discard this condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sword");

		assertEquals("You Have My Sword", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void CannotBePlayedUntilAragornIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		// "Aragorn" is a name: this one is a [rohan] companion with no signet.
		var aragorn = scn.GetFreepsCard("defender");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(sword, aragorn);

		scn.StartGame();

		// Boromir could bear it, and is a [gondor] Man with the Aragorn signet.  He is not Aragorn.
		assertInPlay(boromir);
		assertInZone(Zone.HAND, aragorn);
		assertFalse(scn.FreepsPlayAvailable(sword));

		scn.FreepsPlayCard(aragorn);

		assertInPlay(aragorn);
		assertEquals(Culture.ROHAN, aragorn.getBlueprint().getCulture());
		assertTrue(scn.FreepsPlayAvailable(sword));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysForOneTwilightOnAChosenCompanionWithTheAragornSignetAndNeverOnAragorn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(gimli, guard);
		scn.MoveCardsToHand(sword, aragorn, arwen);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(sword));

		// Aragorn has the Aragorn signet himself, and is the one such companion who may not bear it: with him
		// spotted there is still nobody to play it on.
		scn.FreepsPlayCard(aragorn);
		assertInPlay(aragorn);
		assertEquals(Signet.ARAGORN, aragorn.getBlueprint().getSignet());
		assertFalse(scn.FreepsPlayAvailable(sword));

		// Boromir and Sam are put down beside him without being played.  Playing Arwen has the game ask again.
		scn.MoveCompanionsToTable(boromir, sam);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(arwen);
		assertTrue(scn.FreepsPlayAvailable(sword));
		// 4 for Aragorn + 2 for Arwen
		assertEquals(6, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(boromir));

		scn.FreepsPlayCard(sword);

		// A Man, an Elf and a Ring-bound Hobbit, each with the Aragorn signet
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, arwen, sam));
		// Aragorn; a companion with the Gandalf signet, one with the Frodo signet and one with none
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, gimli, frodo, guard));
		scn.FreepsChooseCard(arwen);

		assertAttachedTo(sword, arwen);
		assertNotAttachedTo(sword, boromir);
		assertNotAttachedTo(sword, sam);
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachSkirmishBearerWinsHasItsOwnerExertAragornOrDiscardIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		// The Aragorn who pays is the [rohan] one: the text names him, not his culture or his signet.
		var aragorn = scn.GetFreepsCard("defender");
		var arwen = scn.GetFreepsCard("arwen");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.AttachCardsTo(arwen, sword);

		scn.StartGame();

		scn.MoveMinionsToTable(nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(arwen, nelya);

		// 6 + 3 against a Nazgul + 2 from the condition, against 10
		assertEquals(11, scn.GetStrength(arwen));
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(nelya));

		scn.PassSkirmishActions();
		// The text has no "may": the game does not offer the condition's text, it asks which comes first,
		// the wound to the loser or the condition's text.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Required responses"));
		scn.FreepsResolveRuleFirst();

		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		assertTrue(scn.FreepsChoiceAvailable("Discard"));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertAttachedTo(sword, arwen);

		scn.FreepsChooseOption("Exert");

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertAttachedTo(sword, arwen);

		// Nelya is fierce, so the same bearer fights and wins a second time this turn.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(arwen, nelya);
		assertEquals(11, scn.GetStrength(arwen));

		scn.PassFierceSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Required responses"));
		scn.FreepsResolveRuleFirst();

		assertEquals(2, scn.GetWoundsOn(nelya));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		assertTrue(scn.FreepsChoiceAvailable("Discard"));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertAttachedTo(sword, arwen);

		scn.FreepsChooseOption("Discard");

		assertInZone(Zone.DISCARD, sword);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsDiscardedWithoutAChoiceWhenAragornIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, sword);

		scn.StartGame();

		// Aragorn is put down once the turn has started, so that his own start-of-turn text is not offered.
		scn.MoveCompanionsToTable(aragorn);
		// Vitality 4 with 3 wounds: Aragorn is exhausted and cannot exert.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(arwen, savage);

		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.IsExhausted(aragorn));
		assertAttachedTo(sword, arwen);

		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Required responses"));
		scn.FreepsResolveRuleFirst();

		// Only one of the two can be carried out, so it is what happens.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInZone(Zone.DISCARD, sword);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInPlay(aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsDiscardedWithoutAChoiceWhenAragornIsNoLongerInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, sword);
		// Aragorn was there when the condition was played and has been killed since.
		scn.MoveCardsToDeadPile(aragorn);

		scn.StartGame();

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(arwen, savage);

		assertInZone(Zone.DEAD, aragorn);
		assertAttachedTo(sword, arwen);

		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Required responses"));
		scn.FreepsResolveRuleFirst();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertInZone(Zone.DISCARD, sword);
		assertInZone(Zone.DEAD, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsAskedWhenBearerLosesOrWhenAnotherCompanionWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(arwen, boromir);
		scn.AttachCardsTo(boromir, sword);

		scn.StartGame();

		// Aragorn is put down once the turn has started, so that his own start-of-turn text is not offered.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{arwen, savage},
				new PhysicalCardImpl[]{boromir, troop});

		// Arwen has the Aragorn signet and does not bear the condition: 6 against 5, and she wins.
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(6, scn.GetStrength(arwen));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertAttachedTo(sword, boromir);

		// The next thing asked is which skirmish to fight.  Boromir bears it: 7 + 2 against 13, and he loses.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(13, scn.GetStrength(troop));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertAttachedTo(sword, boromir);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
