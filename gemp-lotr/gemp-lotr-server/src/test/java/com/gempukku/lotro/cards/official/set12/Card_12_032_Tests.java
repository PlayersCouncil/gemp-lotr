package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_032_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("salve", "12_32");
					put("salve2", "12_32");
					put("salve3", "12_32");
					put("salve4", "12_32");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion, cost 4
					// Saruman, The White: a [gandalf] Wizard who is an ally
					put("saruman", "32_24");
					// Ent Avenger: a [gandalf] companion who is no Wizard
					put("avenger", "6_27");
					put("bounder", "1_286");    // Bounder: a [shire] ally
					put("guard", "5_83");       // Household Guard: strength 6, vitality 3, no text but valiant
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3, vitality 4
					// Ent Draught: another [gandalf] condition a companion bears (vitality +1), with a limit of
					// 1 per bearer of its own
					put("draught", "9_24");

					put("marksman", "1_176");
					// Uruk-hai Raiding Party: strength 9, vitality 3, damage +1, so the companion it beats
					// takes two wounds
					put("raiders", "1_158");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text

				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SalveStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Salve
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Game Text: <b>Spell</b>. To play, spot a [gandalf] Wizard. Bearer must be a companion. Limit 1 per bearer.
		 * <b>Response:</b> If bearer is about to take a wound that would kill him or her, discard this condition to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("salve");

		assertEquals("Salve", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAGandalfWizardIsSpottedAndThenPlaysForNothingOnAChosenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var salve = scn.GetFreepsCard("salve");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var avenger = scn.GetFreepsCard("avenger");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(salve, gandalf);
		scn.MoveCompanionsToTable(aragorn, avenger);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();

		// A [gandalf] companion who is no Wizard is not enough.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.HAND, salve);
		assertFalse(scn.FreepsPlayAvailable(salve));

		scn.FreepsPlayCard(gandalf);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(salve));

		scn.FreepsPlayCard(salve);
		// Any companion, the Ring-bearer among them; not the ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, avenger, gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(aragorn);

		assertAttachedTo(salve, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGandalfWizardWhoIsAnAllyIsSpottedAndBearsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var salve = scn.GetFreepsCard("salve");
		var saruman = scn.GetFreepsCard("saruman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(salve);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(saruman);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(salve));

		scn.FreepsPlayCard(salve);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(saruman));
		scn.FreepsChooseCard(frodo);

		assertAttachedTo(salve, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionWhoBearsOneIsNotOfferedASecondAndWithEveryCompanionBearingOneNoneCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var salve = scn.GetFreepsCard("salve");
		var salve2 = scn.GetFreepsCard("salve2");
		var salve3 = scn.GetFreepsCard("salve3");
		var salve4 = scn.GetFreepsCard("salve4");
		var draught = scn.GetFreepsCard("draught");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(salve2, salve3, salve4);
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(frodo, salve);
		// Aragorn already bears a different [gandalf] condition, which is not a copy of this card.
		scn.AttachCardsTo(aragorn, draught);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(salve2));

		scn.FreepsPlayCard(salve2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		scn.FreepsChooseCard(gandalf);
		assertAttachedTo(salve2, gandalf);

		// Aragorn is now the only companion bearing none, and is given the third without a choice.
		scn.FreepsPlayCard(salve3);
		assertAttachedTo(salve3, aragorn);
		assertAttachedTo(draught, aragorn);

		// Gandalf is still there to spot, and the card costs nothing; there is no bearer left.
		assertInZone(Zone.HAND, salve4);
		assertFalse(scn.FreepsPlayAvailable(salve4));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SalveProtectsBearerFromLethalDamage() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var salve = scn.GetFreepsCard("salve");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, salve);

		scn.MoveMinionsToTable("marksman");

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 3);

		// The Marksman's one archery wound is placed on Aragorn.
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetVitality(aragorn));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.ATTACHED, salve);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, salve);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(1, scn.GetVitality(aragorn));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AWoundThatLeavesBearerAliveIsNotAnsweredAndTheNextOneIsByBearersOwnCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var salve = scn.GetFreepsCard("salve");
		var salve2 = scn.GetFreepsCard("salve2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, salve);
		scn.AttachCardsTo(frodo, salve2);

		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		// Vitality 4: two wounds can still be taken, and the second of them would kill.
		scn.AddWoundsToChar(aragorn, 2);
		scn.MoveMinionsToTable(raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, raiders);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// Aragorn (8) loses to the Raiding Party (9, damage +1) and is to take two wounds.
		scn.PassCurrentPhaseActions();

		// The first has landed with nothing asked; the game stops before the second.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(salve));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(salve2));
		assertAttachedTo(salve, aragorn);

		scn.FreepsAcceptOptionalTrigger();

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertInZone(Zone.DISCARD, salve);
		assertAttachedTo(salve2, frodo);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnlyThatWoundIsPreventedSoASecondWoundOfTheSameSkirmishKillsBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var salve = scn.GetFreepsCard("salve");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, salve);

		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		// Vitality 4 with 3 wounds: either of the two wounds to come would kill.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, raiders);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(salve));

		scn.FreepsAcceptOptionalTrigger();

		// The first wound was prevented and the condition is gone; the second kills him.
		assertInZone(Zone.DISCARD, salve);
		assertInZone(Zone.DEAD, aragorn);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AWoundThatWouldKillAnotherCompanionIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var salve = scn.GetFreepsCard("salve");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.AttachCardsTo(aragorn, salve);

		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		// Both are exhausted (vitality 4 and 3); Aragorn bears the Salve and the Guard takes the wounds.
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(guard, 2);
		scn.MoveMinionsToTable(raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, raiders);
		scn.FreepsResolveSkirmish(guard);
		assertInZone(Zone.FREE_CHARACTERS, guard);

		scn.PassCurrentPhaseActions();

		assertInZone(Zone.DEAD, guard);
		assertAttachedTo(salve, aragorn);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ABearerWhoIsOverwhelmedIsKilledWithNoWoundToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var salve = scn.GetFreepsCard("salve");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, salve);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Vitality 4 with 3 wounds: a wound would kill him.
		scn.AddWoundsToChar(sam, 3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, troop);
		scn.FreepsResolveSkirmish(sam);
		// 3 against 13: more than doubled.
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(13, scn.GetStrength(troop));
		assertAttachedTo(salve, sam);

		scn.PassCurrentPhaseActions();

		assertInZone(Zone.DEAD, sam);
		assertInZone(Zone.DISCARD, salve);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
