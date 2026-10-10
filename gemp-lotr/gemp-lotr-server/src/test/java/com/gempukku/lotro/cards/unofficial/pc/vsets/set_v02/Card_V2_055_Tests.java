package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_055_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("theoden", "102_55");

					put("guard", "5_83");       // Household Guard: valiant [rohan] Man, strength 6, vitality 3; no other text
					put("guard2", "5_83");
					put("guardian", "54_76");   // Lorien Guardian: valiant [elven] Elf, strength 5, vitality 3; a regroup ability
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, not valiant
					put("bounder", "1_286");    // Bounder: Hobbit ally whose home is site 2, strength 2, vitality 2; a skirmish ability
					// Royal Guard: [rohan] ally, strength 3, vitality 3, not valiant.  "Skirmish: Exert this ally to add his
					// strength to a skirmishing Hobbit and make that Hobbit gain valiant until the regroup phase."
					put("royalguard", "102_54");

					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
					put("runner5", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheodenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Theoden, Last of His House
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Theoden
		 * Game Text: Valiant.
		 * 	Theoden is strength +1 for each other valiant companion assigned to a skirmish.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("theoden");

		assertEquals("Theoden", card.getBlueprint().getTitle());
		assertEquals("Last of His House", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.THEODEN, card.getBlueprint().getSignet());
	}

	@Test
	public void PlayedForThreeToTheFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		scn.MoveCardsToHand(theoden);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(theoden));

		scn.FreepsPlayCard(theoden);

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, theoden);
		assertEquals(6, scn.GetStrength(theoden));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsStrengthPlusOneForEachOtherValiantCompanionForAsLongAsThatCompanionIsAssignedToASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var guardian = scn.GetFreepsCard("guardian");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var royalguard = scn.GetFreepsCard("royalguard");
		scn.MoveCompanionsToTable(theoden, guard, guard2, guardian, aragorn);
		scn.MoveAlliesToTable(bounder, royalguard);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var runner4 = scn.GetShadowCard("runner4");
		var runner5 = scn.GetShadowCard("runner5");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, runner3, runner4, runner5);

		// The fellowship moves to site 2, the Bounder's home, where he may be assigned.
		scn.SkipToAssignments();

		assertEquals(6, scn.GetStrength(theoden));

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{theoden, runner1},
				new PhysicalCardImpl[]{guard, runner2},
				new PhysicalCardImpl[]{guardian, runner3},
				new PhysicalCardImpl[]{aragorn, runner4},
				new PhysicalCardImpl[]{bounder, runner5});

		// Assigned: Theoden himself, two other valiant companions, a companion who is not valiant and an ally.
		// Not assigned: a third valiant companion.
		assertTrue(scn.IsCharAssigned(theoden));
		assertTrue(scn.IsCharAssigned(guard));
		assertTrue(scn.IsCharAssigned(guardian));
		assertTrue(scn.IsCharAssigned(aragorn));
		assertFalse(scn.HasKeyword(aragorn, Keyword.VALIANT));
		assertTrue(scn.IsCharAssigned(bounder));
		assertFalse(scn.HasKeyword(bounder, Keyword.VALIANT));
		assertFalse(scn.IsCharAssigned(guard2));
		assertTrue(scn.HasKeyword(guard2, Keyword.VALIANT));
		// 6 + 1 for the Household Guard + 1 for the Lorien Guardian
		assertEquals(8, scn.GetStrength(theoden));
		// Only Theoden: the valiant companions beside him are as printed.
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(guard2));

		// No ally is valiant in print.  In the Bounder's own skirmish the Royal Guard makes him valiant until the
		// regroup phase: valiant and in a skirmish as the two are, and no companion.
		scn.FreepsResolveSkirmish(bounder);
		scn.FreepsUseCardAction(royalguard);
		assertTrue(scn.IsCharSkirmishing(bounder));
		assertTrue(scn.HasKeyword(bounder, Keyword.VALIANT));
		assertEquals(8, scn.GetStrength(theoden));
		// The Bounder's 2 + 3 does not beat the Runner's 5: one wound of his two.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.HasKeyword(bounder, Keyword.VALIANT));
		assertEquals(8, scn.GetStrength(theoden));

		// A companion whose skirmish is being fought is still assigned to it.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.IsCharSkirmishing(guard));
		assertEquals(8, scn.GetStrength(theoden));

		// Once it is over he is not (the Guard has won and is alive and unwounded).
		scn.PassSkirmishActions();
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(7, scn.GetStrength(theoden));

		// In his own skirmish Theoden does not count himself.
		scn.FreepsResolveSkirmish(theoden);
		assertTrue(scn.IsCharSkirmishing(theoden));
		assertEquals(7, scn.GetStrength(theoden));
		scn.PassSkirmishActions();
		assertEquals(7, scn.GetStrength(theoden));

		scn.FreepsResolveSkirmish(guardian);
		assertEquals(7, scn.GetStrength(theoden));
		scn.PassSkirmishActions();

		// Aragorn is still waiting for his skirmish.
		assertTrue(scn.IsCharAssigned(aragorn));
		assertEquals(6, scn.GetStrength(theoden));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
