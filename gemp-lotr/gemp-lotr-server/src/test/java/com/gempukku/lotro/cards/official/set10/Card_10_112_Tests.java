package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_112_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tale", "10_112");

					// Sam, Son of Hamfast: [shire] Hobbit companion; his fellowship ability is never used
					put("sam", "1_311");
					// Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability never used here
					put("smeagol", "103_116");
					// Merry, From O'er the Brandywine: a [shire] Hobbit companion, laid exhausted; strength +2 only with a weapon
					put("merry", "1_303");
					put("bounder", "1_286");       // Bounder: [shire] Hobbit ally; a skirmish ability never used here
					// Pippin, Trollslayer: a Hobbit companion who is a [gondor] card; a skirmish ability never used here
					put("trollslayer", "103_31");
					put("aragorn", "1_89");        // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability never used here

					put("runner", "1_178");        // Goblin Runner: [moria] Orc, strength 5, vitality 1; "when played" only
					// Ted Sandyman, Chief's Men's Ally: the opponent's Hobbit, a [men] minion; a maneuver and a regroup
					// ability never used here
					put("ted", "18_75");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NinefingeredFrodoandtheRingofDoomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Nine-fingered Frodo and the Ring of Doom
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Tale</b>. Exert a [shire] character to remove a burden or 2 threats.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tale");

		assertEquals("Nine-fingered Frodo and the Ring of Doom", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAShireCharacterAndRemovesABurdenWhenThatIsChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale = scn.GetFreepsCard("tale");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(sam, smeagol, merry, trollslayer, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(tale);
		// Merry, vitality 4, is exhausted.
		scn.AddWoundsToChar(merry, 3);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(merry));
		scn.AddBurdens(2);
		scn.AddThreats(3);
		assertEquals(2, scn.FreepsGetBurdens());
		assertEquals(3, scn.GetThreats());
		scn.MoveMinionsToTable(runner, ted);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{sam, runner},
				new PhysicalCardImpl[]{aragorn, ted});
		scn.FreepsResolveSkirmish(sam);

		assertEquals(2, scn.FreepsGetBurdens());
		assertEquals(3, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertTrue(scn.FreepsPlayAvailable(tale));
		scn.FreepsPlayCard(tale);

		// [shire] characters: Hobbit companions, a companion who is no Hobbit, and an ally.  Not an exhausted one,
		// a Hobbit of [gondor], a Man, nor the opponent's [men] Hobbit minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, smeagol, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, trollslayer, aragorn, ted, runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(smeagol);

		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(bounder));

		// Both can be done in full: the Free Peoples player is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Remove a burden"));
		assertTrue(scn.FreepsChoiceAvailable("Remove 2 threats"));
		scn.FreepsChoose("Remove a burden");

		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(3, scn.GetThreats());
		assertInDiscard(tale);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayChooseToRemoveTwoThreatsInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale = scn.GetFreepsCard("tale");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(tale);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.AddThreats(3);
		assertEquals(2, scn.FreepsGetBurdens());
		assertEquals(3, scn.GetThreats());
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner);
		scn.FreepsResolveSkirmish(sam);

		assertEquals(0, scn.GetWoundsOn(sam));
		scn.FreepsPlayCard(tale);
		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		scn.FreepsChoose("Remove 2 threats");

		// 3 - 2
		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.FreepsGetBurdens());
		assertInDiscard(tale);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithOneThreatABurdenIsRemovedWithNothingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale = scn.GetFreepsCard("tale");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(tale);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.AddThreats(1);
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(1, scn.GetThreats());
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner);
		scn.FreepsResolveSkirmish(sam);

		scn.FreepsPlayCard(tale);
		scn.FreepsChooseCard(sam);

		// 2 threats cannot be removed in full from 1, so the burden is removed: no choice is asked.
		assertEquals(0, scn.FreepsGetMultipleChoiceCount());
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(1, scn.GetThreats());
		assertInDiscard(tale);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoBurdenTwoThreatsAreRemovedWithNothingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale = scn.GetFreepsCard("tale");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(tale);

		scn.StartGame();
		scn.AddThreats(3);
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(3, scn.GetThreats());
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner);
		scn.FreepsResolveSkirmish(sam);

		scn.FreepsPlayCard(tale);
		scn.FreepsChooseCard(sam);

		assertEquals(0, scn.FreepsGetMultipleChoiceCount());
		// 3 - 2
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.FreepsGetBurdens());
		assertInDiscard(tale);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoBurdenAndOneThreatBothAreOfferedAndTheOneThreatIsRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale = scn.GetFreepsCard("tale");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(tale);

		scn.StartGame();
		scn.AddThreats(1);
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(1, scn.GetThreats());
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner);
		scn.FreepsResolveSkirmish(sam);

		scn.FreepsPlayCard(tale);
		scn.FreepsChooseCard(sam);

		// Neither can be done in full: both are offered, and as much of the one chosen as can be done is done
		// (CR 4.2 "effect").
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChoose("Remove 2 threats");

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.FreepsGetBurdens());
		assertInDiscard(tale);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoBurdenAndNoThreatTheExertionIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale = scn.GetFreepsCard("tale");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(tale);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner);
		scn.FreepsResolveSkirmish(sam);

		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(sam));
		// Nothing to remove; an event may be played for no effect (CR 4.2 "effect").
		assertTrue(scn.FreepsPlayAvailable(tale));
		scn.FreepsPlayCard(tale);
		scn.FreepsChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.FreepsGetMultipleChoiceCount());
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetThreats());
		assertInDiscard(tale);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenNoShireCharacterCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale = scn.GetFreepsCard("tale");
		var frodo = scn.GetRingBearer();
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(trollslayer, aragorn);
		scn.MoveCardsToHand(tale);
		// Frodo, vitality 4, the only [shire] character, is exhausted; a [gondor] Hobbit and a Man can exert.
		scn.AddWoundsToChar(frodo, 3);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertInHand(tale);
		scn.AddBurdens(1);
		scn.AddThreats(2);
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(2, scn.GetThreats());
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(trollslayer));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInHand(tale);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(tale));
	}
}
