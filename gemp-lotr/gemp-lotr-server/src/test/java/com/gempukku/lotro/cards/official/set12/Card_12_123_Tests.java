package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_123_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hope", "12_123");
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] Hobbit companion, strength 3, vitality 4; +2 while he bears a weapon
					put("sword", "1_299");      // Hobbit Sword: hand weapon, strength +2, on a Hobbit
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ALLY; a skirmish ability used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; its text is on being played
					put("whelp", "7_202");      // Morgul Whelp: [wraith] Orc, strength 3 while the Ring-bearer has no wound
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion whose race is Hobbit, strength 3; abilities used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HopeisKindledStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Hope is Kindled
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a Hobbit strength +2. If that Hobbit wins this skirmish, you may remove a burden.
		*/

		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");

		assertEquals("Hope is Kindled", hope.getBlueprint().getTitle());
		assertNull(hope.getBlueprint().getSubtitle());
		assertFalse(hope.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, hope.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, hope.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, hope.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(hope, Timeword.SKIRMISH));
		assertEquals(2, hope.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAHobbitStrengthPlus2AndWhenHeWinsTheSkirmishABurdenMayBeRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCardsToHand(hope);
		scn.MoveCompanionsToTable(pippin, smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.MoveMinionsToTable(runner, ted);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, runner);
		// Ted is left over and stays out of the skirmishes.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(2, scn.GetBurdens());
		// 3, + 1 for The Ruling Ring.
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(2, scn.GetStrength(bounder));
		assertEquals(3, scn.GetStrength(ted));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(hope));
		scn.FreepsPlayCard(hope);

		// Hobbits of each side, culture and kind, the one fighting and others in no skirmish; not a [shire]
		// companion who is no Hobbit, nor a Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin, bounder, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(frodo);
		assertEquals(6, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(2, scn.GetStrength(bounder));
		assertEquals(3, scn.GetStrength(ted));
		assertInDiscard(hope);

		// 6 against the Runner's 5.
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheBurdenMayBeKept() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(hope);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, runner);

		// Frodo, the only Hobbit, is made stronger with nothing asked.
		scn.FreepsPlayCard(hope);
		assertEquals(6, scn.GetStrength(frodo));
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsAskedWhenThatHobbitLosesTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var merry = scn.GetFreepsCard("merry");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(hope);
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(merry, runner);

		scn.FreepsPlayCard(hope);
		scn.FreepsChooseCard(merry);
		assertEquals(5, scn.GetStrength(merry));
		assertEquals(0, scn.GetWoundsOn(merry));

		// 5 against 5: a tie goes to the Shadow side, and Merry takes the wound and lives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(merry));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PlayedOnAHobbitWaitingForHisSkirmishItLastsThatSkirmishAndNeitherWinIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var runner = scn.GetShadowCard("runner");
		var whelp = scn.GetShadowCard("whelp");
		scn.MoveCardsToHand(hope);
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, sword);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		assertAttachedTo(sword, merry);
		scn.MoveMinionsToTable(runner, whelp);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, runner}, new PhysicalCardImpl[]{frodo, whelp});

		// Played on Frodo, whose skirmish is still to come, while Merry fights.
		scn.FreepsResolveSkirmish(merry);
		scn.FreepsPlayCard(hope);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry));
		scn.FreepsChooseCard(frodo);
		assertEquals(6, scn.GetStrength(frodo));
		// 3, + 2 for the Sword, + 2 while he bears a weapon.
		assertEquals(7, scn.GetStrength(merry));

		// Merry, 7 against 5, wins: he is not that Hobbit, and nothing is asked.
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetBurdens());

		// Frodo's own skirmish is not the one the event was played in: the bonus is gone, and his win, 4 against
		// the Whelp's 3, is answered by nothing.
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(4, scn.GetStrength(frodo));
		scn.PassSkirmishActions();
		assertInDiscard(whelp);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhaseAndWithNoBurdenAWinAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(hope);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(hope));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignAndResolve(frodo, runner);
		assertTrue(scn.FreepsPlayAvailable(hope));
		scn.FreepsPlayCard(hope);
		assertEquals(6, scn.GetStrength(frodo));

		// Frodo wins with no burden to remove: nothing is asked.
		assertEquals(0, scn.GetBurdens());
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
