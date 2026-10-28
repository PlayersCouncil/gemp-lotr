package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_056_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("city", "7_56");
					put("smeagol", "6_45");     // Sméagol, Poor Creature: [gollum] companion, strength 3, vitality 4; a skirmish ability used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: strength 7, +1 for each race he spots (Hobbit, Man here)
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose

					put("runner", "1_178");     // Goblin Runner: strength 5; "When you play this minion, you may add (2)", cheated in
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					put("marksman", "1_176");   // Goblin Marksman: Archer and nothing else
					put("skirmisher", "18_90"); // Orkish Skirmisher: strength 9; each time he wins a skirmish, may exert twice to wound a companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetShireSmeagolScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("city", "7_56");
					put("smeagol", "103_116");  // Smeagol, Once Like Us: [shire] companion, strength 3, vitality 4; an assignment ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					put("runner", "1_178");     // Goblin Runner: strength 5
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheDeadCityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: The Dead City
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype:
		 * Game Text: <b>Stealth</b>.
		 * Bearer must be Sméagol.
		 * If Sméagol is about to be killed in a skirmish, he is discarded instead.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("city");

		assertEquals("The Dead City", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.STEALTH));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnSmeagolAndNotOnAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var city = scn.GetFreepsCard("city");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToHand(city);

		scn.StartGame();

		assertInHand(city);
		assertTrue(scn.FreepsPlayAvailable(city));
		scn.FreepsPlayCard(city);

		// Sméagol is the one bearer it may have, and it goes on him unasked; Aragorn and Frodo are none.
		assertAttachedTo(city, smeagol);
		assertNotAttachedTo(city, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysOnTheShireSmeagolAsOnTheGollumOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShireSmeagolScenario();

		var city = scn.GetFreepsCard("city");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToHand(city);

		scn.StartGame();

		// This Smeagol is [shire]: the text names only the title.
		assertEquals(Culture.SHIRE, smeagol.getBlueprint().getCulture());
		assertInHand(city);
		assertTrue(scn.FreepsPlayAvailable(city));
		scn.FreepsPlayCard(city);

		assertAttachedTo(city, smeagol);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoSmeagolInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var city = scn.GetFreepsCard("city");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToHand(city);

		scn.StartGame();

		assertInHand(city);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(city));
	}

	@Test
	public void KilledByTheWoundOfALostSkirmishSmeagolIsDiscardedInsteadAndAnotherCompanionIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var city = scn.GetFreepsCard("city");
		var smeagol = scn.GetFreepsCard("smeagol");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(smeagol, gandalf);
		scn.AttachCardsTo(smeagol, city);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);
		// Vitality 4 each: one more wound kills.
		scn.AddWoundsToChar(smeagol, 3);
		scn.AddWoundsToChar(gandalf, 3);

		assertEquals(3, scn.GetWoundsOn(smeagol));
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertAttachedTo(city, smeagol);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{smeagol, runner}, new PhysicalCardImpl[]{gandalf, troop});

		// Gandalf (7 + 1 for a Hobbit spotted) loses to the Troop (13), not overwhelmed, and the wound kills him.
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, gandalf);

		// Sméagol (3) loses to the Runner (5), not overwhelmed: the wound would kill him, and he is discarded instead.
		scn.FreepsResolveSkirmish(smeagol);
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, runner));
		scn.PassSkirmishActions();

		assertInDiscard(smeagol, city);
		assertNotInZone(Zone.DEAD, smeagol);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OverwhelmedInASkirmishSmeagolIsDiscardedInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var city = scn.GetFreepsCard("city");
		var smeagol = scn.GetFreepsCard("smeagol");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, city);

		assertAttachedTo(city, smeagol);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, troop);
		scn.FreepsResolveSkirmish(smeagol);

		// 3 against 13, unwounded: overwhelmed, and so about to be killed.
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(3, scn.GetStrength(smeagol));
		scn.PassSkirmishActions();

		assertInDiscard(smeagol, city);
		assertNotInZone(Zone.DEAD, smeagol);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void KilledByAnArcheryWoundSmeagolIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var city = scn.GetFreepsCard("city");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.AttachCardsTo(smeagol, city);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.AddWoundsToChar(smeagol, 3);

		assertEquals(3, scn.GetWoundsOn(smeagol));
		assertAttachedTo(city, smeagol);

		scn.SkipToArcheryWounds();
		// The minion archery total of 1 is placed by the Free Peoples player.
		scn.FreepsChooseCard(smeagol);

		// Killed in the archery phase, in no skirmish: the dead pile, and The Dead City goes with its bearer.
		assertInZone(Zone.DEAD, smeagol);
		assertInDiscard(city);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
	}

	@Test
	public void KilledDuringASkirmishPhaseWhileInNoSkirmishSmeagolIsDiscardedInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var city = scn.GetFreepsCard("city");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var skirmisher = scn.GetShadowCard("skirmisher");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.AttachCardsTo(smeagol, city);

		scn.StartGame();
		scn.MoveMinionsToTable(skirmisher);
		scn.AddWoundsToChar(smeagol, 3);

		assertEquals(3, scn.GetWoundsOn(smeagol));
		assertAttachedTo(city, smeagol);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, skirmisher);
		scn.FreepsResolveSkirmish(aragorn);
		assertFalse(scn.IsCharAssigned(smeagol));
		scn.PassSkirmishActions();

		// The Skirmisher (9) beats Aragorn (8) and, in that skirmish phase, wounds Sméagol, who is in no skirmish.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(skirmisher));
		scn.ShadowChooseCard(smeagol);

		// "In a skirmish" is any kill while a skirmish phase is going on, his own skirmish or not (Ketura, Q-0358: B):
		// he is discarded, with The Dead City, and not killed.
		assertInDiscard(smeagol, city);
		assertNotInZone(Zone.DEAD, smeagol);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheShireSmeagolKilledInASkirmishIsDiscardedInsteadAsTheGollumOneIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShireSmeagolScenario();

		var city = scn.GetFreepsCard("city");
		var smeagol = scn.GetFreepsCard("smeagol");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, city);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.AddWoundsToChar(smeagol, 3);

		assertEquals(Culture.SHIRE, smeagol.getBlueprint().getCulture());
		assertEquals(3, scn.GetWoundsOn(smeagol));
		assertAttachedTo(city, smeagol);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, runner);
		scn.FreepsResolveSkirmish(smeagol);
		// 3 against 5: the wound of the loss would kill him.
		scn.PassSkirmishActions();

		assertInDiscard(smeagol, city);
		assertNotInZone(Zone.DEAD, smeagol);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
