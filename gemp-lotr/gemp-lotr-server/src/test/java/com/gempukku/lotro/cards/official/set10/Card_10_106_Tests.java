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

public class Card_10_106_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chance", "10_106");
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit companion, strength 3,
					                            // enduring (kept unwounded here)
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit, strength 8
					put("bounder", "1_286");    // Bounder: a Hobbit ally; a skirmish ability used only on purpose
					put("goldberry", "9_51");   // Goldberry: a [shire] ally who is no Hobbit; her texts want a river
					put("ted", "18_75");        // Ted Sandyman: a Hobbit minion, strength 3; maneuver and regroup abilities only
					put("savage1", "1_151");    // Uruk Savage: strength 5, vitality 3, damage +1 and nothing else

					// For the discard pile: [shire] cards of four types, and a [gondor] card.
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession
					put("appetite", "1_294");   // Hobbit Appetite: a [shire] event
					put("gaffer", "1_291");     // The Gaffer: a [shire] ally
					put("talent", "1_316");     // A Talent for Not Being Seen: a [shire] condition
					put("swordarm", "1_116");   // Swordarm of the White Tower: a [gondor] event
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ChanceObservationStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Chance Observation
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: If there are 4 or more [shire] cards in your discard pile, make a Hobbit strength +1 for each Hobbit you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("chance");

		assertEquals("Chance Observation", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithFourShireCardsInTheDiscardPileAHobbitIsStrengthPlusOneForEachHobbitUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chance = scn.GetFreepsCard("chance");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var goldberry = scn.GetFreepsCard("goldberry");
		var ted = scn.GetShadowCard("ted");
		var savage1 = scn.GetShadowCard("savage1");
		var sword = scn.GetFreepsCard("sword");
		var appetite = scn.GetFreepsCard("appetite");
		var gaffer = scn.GetFreepsCard("gaffer");
		var talent = scn.GetFreepsCard("talent");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveAlliesToTable(bounder, goldberry);
		scn.MoveCardsToDiscard(sword, appetite, gaffer, talent);
		scn.MoveCardsToHand(chance);

		scn.StartGame();
		assertInDiscard(sword, appetite, gaffer, talent);
		scn.MoveMinionsToTable(ted, savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage1}, new PhysicalCardImpl[]{merry, ted});
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(3, scn.GetStrength(merry));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.FreepsPlayAvailable(chance));
		scn.FreepsPlayCard(chance);

		// "A Hobbit": the Ring-bearer, a [rohan] Hobbit companion, a Hobbit ally and the opponent's Hobbit minion,
		// none of them in this skirmish; not Aragorn, who is, nor the [shire] ally who is no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry, bounder, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, goldberry));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);

		// Four Hobbits (Frodo, Merry, Bounder, Ted Sandyman): 3 + 4.  [shire] cards would count 3 (Frodo, Bounder,
		// Goldberry), Hobbit companions 2.
		assertEquals(7, scn.GetStrength(merry));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(merry);

		// Merry's own skirmish is another skirmish phase: the bonus is gone.
		assertTrue(scn.IsCharSkirmishingAgainst(merry, ted));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableWithThreeShireCardsInTheDiscardPileAndPlayableWithAFourth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chance = scn.GetFreepsCard("chance");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage1 = scn.GetShadowCard("savage1");
		var sword = scn.GetFreepsCard("sword");
		var appetite = scn.GetFreepsCard("appetite");
		var gaffer = scn.GetFreepsCard("gaffer");
		var talent = scn.GetFreepsCard("talent");
		var swordarm = scn.GetFreepsCard("swordarm");
		var shadowTalent = scn.GetShadowCard("talent");
		scn.MoveCompanionsToTable(merry, aragorn);
		// Three [shire] cards and a [gondor] one in his own discard pile, and a [shire] card in his opponent's.
		scn.MoveCardsToDiscard(sword, appetite, gaffer, swordarm, shadowTalent);
		scn.MoveCardsToHand(chance);

		scn.StartGame();
		assertInDiscard(sword, appetite, gaffer, swordarm, shadowTalent);
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage1);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(chance));

		scn.MoveCardsToDiscard(talent);
		assertInDiscard(talent);
		assertTrue(scn.FreepsPlayAvailable(chance));

		assertEquals(3, scn.GetStrength(merry));
		scn.FreepsPlayCard(chance);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry));
		scn.FreepsChooseCard(merry);

		// Two Hobbits: 3 + 2.
		assertEquals(5, scn.GetStrength(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
