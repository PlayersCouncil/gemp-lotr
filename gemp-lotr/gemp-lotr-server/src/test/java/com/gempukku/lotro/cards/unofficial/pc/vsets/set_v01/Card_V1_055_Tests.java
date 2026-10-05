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

public class Card_V1_055_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("yet1", "101_55");
					put("yet2", "101_55");
					// With the Frodo signet:
					// Merry, Friend to Sam: strength 3, vitality 4.  His skirmish ability needs him unassigned
					// and is never used here.
					put("merry", "1_302");
					put("pippin", "1_306");         // Pippin, Friend to Frodo: his text is about [shire] tales
					// Boromir, Son of Denethor: [gondor] Man, strength 7, vitality 3.  His skirmish ability is
					// never used here.
					put("boromir", "1_97");
					// Gimli, Dwarf of the Mountain-race: [dwarven] Dwarf; his text is about underground sites
					put("gimli", "2_121");
					// With another signet, or none:
					put("sam", "1_311");            // Sam, Son of Hamfast: [shire] Hobbit with the Aragorn signet
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: the Gandalf signet
					put("guard", "1_7");            // Dwarf Guard: no signet

					put("runner1", "1_178");        // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: Frodo signet, strength 3
				VirtualTableScenario.RulingRing     // strength +1 to its bearer
		);
	}

	protected VirtualTableScenario GetFrodoWithTheAragornSignetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("yet1", "101_55");
					put("merry", "1_302");          // Merry, Friend to Sam: Frodo signet, strength 3
					put("pippin", "1_306");         // Pippin, Friend to Frodo: Frodo signet
					put("sam", "1_311");            // Sam, Son of Hamfast: [shire] Hobbit with the Aragorn signet
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: the Gandalf signet
					put("guard", "1_7");            // Dwarf Guard: no signet

					put("runner1", "1_178");        // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				// Frodo, Reluctant Adventurer: a Frodo with the Aragorn signet; his text is about what is played on him
				"2_102",
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WeMayYetStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: We May Yet
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert a skirmishing companion with the Frodo signet to make that companion strength +1 for
		 * 	each companion with the Frodo signet you can spot (limit +4).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("yet1");

		assertEquals("We May Yet", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsTheSkirmishingCompanionToMakeHimStrongerByOneForEachCompanionWithTheFrodoSignet() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var yet = scn.GetFreepsCard("yet1");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		// Three with the Frodo signet: Frodo, Merry and Boromir.
		scn.MoveCompanionsToTable(merry, boromir, sam, aragorn, guard);
		scn.MoveCardsToHand(yet);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, runner1}, new PhysicalCardImpl[]{aragorn, runner2});

		scn.FreepsResolveSkirmish(merry);
		var twilight = scn.GetTwilight();
		assertInHand(yet);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(4, scn.GetStrength(frodo));     // 3 and 1 for the Ring
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.FreepsPlayAvailable(yet));

		scn.FreepsPlayCard(yet);
		assertEquals(twilight + 1, scn.GetTwilight());
		assertInDiscard(yet);
		assertEquals(1, scn.GetWoundsOn(merry));
		// 3 + 3: Sam with the Aragorn signet, Aragorn with Gandalf's and the Dwarf Guard with none add nothing.
		assertEquals(6, scn.GetStrength(merry));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassCurrentPhaseActions();
		// 6 against 5: Merry wins a skirmish he would have lost at 3, and the Runner's one vitality is gone.
		assertInDiscard(runner1);
		assertEquals(1, scn.GetWoundsOn(merry));

		// Aragorn's skirmish is still to come.  Merry's is over, and so is his bonus.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsStrengthPlusTwoWithTwoSuchCompanionsBesideAFrodoWhoHasAnotherSignet() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrodoWithTheAragornSignetScenario();

		var yet = scn.GetFreepsCard("yet1");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetShadowCard("runner1");
		// Two with the Frodo signet: Merry and Pippin.
		scn.MoveCompanionsToTable(merry, pippin, sam, aragorn, guard);
		scn.MoveCardsToHand(yet);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(merry, runner);

		scn.FreepsResolveSkirmish(merry);
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.FreepsPlayAvailable(yet));

		scn.FreepsPlayCard(yet);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(5, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void GivesNoMoreThanPlusFourWithFiveSuchCompanionsAndASecondCopyGivesFourOfItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var yet1 = scn.GetFreepsCard("yet1");
		var yet2 = scn.GetFreepsCard("yet2");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var runner = scn.GetShadowCard("runner1");
		// Five with the Frodo signet: Frodo, Boromir, Merry, Pippin and Gimli.
		scn.MoveCompanionsToTable(boromir, merry, pippin, gimli, sam);
		scn.MoveCardsToHand(yet1, yet2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(boromir, runner);

		scn.FreepsResolveSkirmish(boromir);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.FreepsPlayAvailable(yet1));

		scn.FreepsPlayCard(yet1);
		assertEquals(1, scn.GetWoundsOn(boromir));
		// 7 + 4, not 7 + 5.
		assertEquals(11, scn.GetStrength(boromir));

		// The limit is each card's own: the second copy is not held to what the first gave.
		scn.ShadowPass();
		assertTrue(scn.FreepsPlayAvailable(yet2));
		scn.FreepsPlayCard(yet2);
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(15, scn.GetStrength(boromir));
		assertInDiscard(yet1, yet2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheSkirmishOfACompanionWithAnotherSignetWhileOneWithTheFrodoSignetWaitsForHis() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var yet = scn.GetFreepsCard("yet1");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(merry, sam);
		scn.MoveCardsToHand(yet);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, runner1}, new PhysicalCardImpl[]{merry, runner2});

		// Sam, a [shire] Hobbit with the Aragorn signet, is the one skirmishing.  Merry is assigned to the other
		// Runner and Frodo is assigned to nothing.
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishing(sam));
		assertTrue(scn.IsCharAssigned(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(yet));

		scn.PassCurrentPhaseActions();
		assertEquals(0, scn.GetWoundsOn(merry));

		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishing(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(yet));
	}

	@Test
	public void CannotBePlayedWhenTheSkirmishingCompanionWithTheFrodoSignetIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var yet = scn.GetFreepsCard("yet1");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(merry, boromir);
		// Vitality 4 with 3 wounds: he cannot exert.
		scn.AddWoundsToChar(merry, 3);
		scn.MoveCardsToHand(yet);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, runner1}, new PhysicalCardImpl[]{boromir, runner2});

		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishing(merry));
		assertEquals(3, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(yet));

		scn.PassCurrentPhaseActions();

		// Boromir can exert, and in his skirmish the event can be played.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(yet));
	}
}
