package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_142_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bilbo", "13_142");
					put("sam", "1_311");         // Sam, Son of Hamfast: a Ring-bound Hobbit; a fellowship ability and a response to Frodo's death
					put("merry", "1_303");       // Merry, From O'er the Brandywine: an unbound Hobbit companion; +2 only with a weapon
					// Sméagol, Poor Creature: a Ring-bound companion of no race; a skirmish ability used only on purpose
					put("smeagol", "6_45");
					put("runner1", "1_178");     // Goblin Runner: strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
					// Uruk Pursuer: fierce, strength 5, vitality 3; his texts are about unbound Hobbits discarded or killed
					put("pursuer", "4_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli, Bearer of Grudges is the Ring-bearer: a Ring-bearer who is no Hobbit.  His own text acts only at
	// the start of a skirmish involving him.
	protected VirtualTableScenario GetBurdenScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bilbo", "13_142");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit; a Fellowship ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BilboStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Bilbo, Aged Ring-bearer
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 8
		 * Game Text: <b>Ring-bound</b>.<br>While Bilbo is not assigned to a skirmish, each Ring-bound Hobbit is strength +1.<br>At the start of each skirmish involving Bilbo, you may remove a burden for each other Ring-bound Hobbit you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bilbo");

		assertEquals("Bilbo", card.getBlueprint().getTitle());
		assertEquals("Aged Ring-bearer", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getResistance());
	}

	// Ketura on Q-0034: an optional action with no cost that would do nothing is not offered.  "A burden for each
	// other Ring-bound Hobbit you can spot" is no burden where Bilbo is the only Ring-bound Hobbit.
	@Test
	public void TheBurdenRemovalIsNotOfferedWithNoOtherRingBoundHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBurdenScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCompanionsToTable(bilbo);
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.FreepsResolveSkirmish(bilbo);

		// A burden is there to remove; the Ring-bearer is a Dwarf.
		assertTrue(scn.IsCharSkirmishingAgainst(bilbo, runner));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheBurdenRemovalIsOfferedWithAnotherRingBoundHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBurdenScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(bilbo, sam);
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.FreepsResolveSkirmish(bilbo);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		assertEquals(1, scn.GetBurdens());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WhileBilboIsNotAssignedEachRingBoundHobbitIsStrengthPlusOneAndAtHisSkirmishABurdenIsRemovedForEachOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var smeagol = scn.GetFreepsCard("smeagol");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(bilbo, sam, merry, smeagol);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.AddBurdens(4);

		// Bilbo is assigned to nothing: each Ring-bound Hobbit, Bilbo himself among them, is +1; not Merry, a
		// Hobbit who is not Ring-bound, nor Sméagol, a Ring-bound companion who is no Hobbit.
		assertEquals(4, scn.GetStrength(bilbo));
		assertEquals(5, scn.GetStrength(frodo));    // 3 + 1 for The Ruling Ring + 1
		assertEquals(4, scn.GetStrength(sam));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(smeagol));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{bilbo, runner1},
				new PhysicalCardImpl[]{sam, runner2});

		// Bilbo is assigned: nobody is +1.
		assertTrue(scn.IsCharAssigned(bilbo));
		assertEquals(3, scn.GetStrength(bilbo));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));

		// Sam's skirmish, fought first, does not involve Bilbo: nothing is offered, and Bilbo, still waiting for
		// his own, gives no bonus.
		assertEquals(4, scn.GetBurdens());
		scn.FreepsResolveSkirmish(sam);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(4, scn.GetBurdens());
		scn.PassSkirmishActions();

		// Bilbo's skirmish: a burden for each other Ring-bound Hobbit he can spot, Frodo and Sam. Not Bilbo himself,
		// Merry or Sméagol.
		scn.FreepsResolveSkirmish(bilbo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// His skirmish is over and he is assigned no longer: the bonus is back.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(4, scn.GetStrength(bilbo));
		assertEquals(5, scn.GetStrength(frodo));
		assertEquals(4, scn.GetStrength(sam));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(smeagol));
	}

	@Test
	public void WithFrodoTheOnlyOtherRingBoundHobbitOneBurdenGoesAndTheFierceSkirmishOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(bilbo, merry);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, pursuer);
		scn.FreepsResolveSkirmish(bilbo);

		// Frodo alone is counted, not Merry: one burden.
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// The Pursuer is fierce: Bilbo's second skirmish offers it again, and declining leaves the burdens.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(bilbo, pursuer);
		scn.FreepsResolveSkirmish(bilbo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
