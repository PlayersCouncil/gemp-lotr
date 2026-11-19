package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_293_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("deftness", "1_293");
					// Sam, Son of Hamfast: Hobbit companion, strength 3; his fellowship ability is never used
					put("sam", "1_311");
					// Pippin, Trollslayer: a Hobbit companion who is a [gondor] card; a skirmish ability never used here
					put("trollslayer", "103_31");
					// Merry, From O'er the Brandywine: a Hobbit companion, laid exhausted; strength +2 only with a weapon
					put("merry", "1_303");
					put("bounder", "1_286");       // Bounder: [shire] Hobbit ally; a skirmish ability never used here
					// Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability never used here
					put("smeagol", "103_116");
					put("aragorn", "1_89");        // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability never used here

					put("runner", "1_178");        // Goblin Runner: [moria] Orc, strength 5, vitality 1; "when played" only
					// Ted Sandyman, Chief's Men's Ally: the opponent's Hobbit, a [men] minion of strength 3 and vitality 4;
					// a maneuver and a regroup ability never used here
					put("ted", "18_75");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HalflingDeftnessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Halfling Deftness
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Exert a Hobbit to make him strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("deftness");

		assertEquals("Halfling Deftness", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAHobbitOfEitherSideToMakeHimStrengthPlus3ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deftness = scn.GetFreepsCard("deftness");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(sam, trollslayer, merry, smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(deftness);
		// Merry, vitality 4, is exhausted.
		scn.AddWoundsToChar(merry, 3);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(merry));
		scn.MoveMinionsToTable(runner, ted);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{sam, runner},
				new PhysicalCardImpl[]{aragorn, ted});
		scn.FreepsResolveSkirmish(sam);

		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(trollslayer));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(3, scn.GetStrength(sam));
		// 3 + 1 for The Ruling Ring
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(trollslayer));
		assertTrue(scn.FreepsPlayAvailable(deftness));
		scn.FreepsPlayCard(deftness);

		// The Hobbit skirmishing, the Ring-bearer, a Hobbit who is a [gondor] card, a Hobbit ally, and the
		// opponent's Hobbit minion (a race word names no side, Q-0058), none of the last four in this skirmish.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, frodo, trollslayer, bounder, ted));
		// An exhausted Hobbit, a [shire] companion who is no Hobbit, a Man
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, smeagol, aragorn, runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(sam);

		// Sam, and only Sam, exerts and is +3.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(trollslayer));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(6, scn.GetStrength(sam));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(trollslayer));
		assertEquals(3, scn.GetStrength(ted));
		assertInDiscard(deftness);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Sam (6) beats the Runner (5). In the next skirmish, a phase of its own, the +3 is gone.
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenNoHobbitCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deftness = scn.GetFreepsCard("deftness");
		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToHand(deftness);
		// Frodo, vitality 4, the only Hobbit, is exhausted; the others can exert.
		scn.AddWoundsToChar(frodo, 3);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertInHand(deftness);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, runner);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInHand(deftness);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(deftness));
	}
}
