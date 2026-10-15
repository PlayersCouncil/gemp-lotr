package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_153_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("slayer1", "1_153");
					put("slayer2", "1_153");    // not unique: "this minion" is read beside other copies
					put("slayer3", "1_153");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: companion, strength 4, vitality 2, no text in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukSlayerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk Slayer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Skirmish:</b> Remove (1) to make this minion strength +1 (limit +3).
		*/

		var scn = GetScenario();

		var slayer = scn.GetFreepsCard("slayer1");

		assertEquals("Uruk Slayer", slayer.getBlueprint().getTitle());
		assertNull(slayer.getBlueprint().getSubtitle());
		assertFalse(slayer.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, slayer.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, slayer.getBlueprint().getCulture());
		assertEquals(CardType.MINION, slayer.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, slayer.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(slayer, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(slayer, Keyword.DAMAGE));
		assertEquals(4, slayer.getBlueprint().getTwilightCost());
		assertEquals(9, slayer.getBlueprint().getStrength());
		assertEquals(2, slayer.getBlueprint().getVitality());
		assertEquals(5, slayer.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishRemovesOneTwilightToMakeThisMinionAndNoOtherStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var slayer1 = scn.GetShadowCard("slayer1");
		var slayer2 = scn.GetShadowCard("slayer2");
		var slayer3 = scn.GetShadowCard("slayer3");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer1, slayer2, slayer3);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		// Two Slayers in one skirmish, the second put there by the Shadow player; the third is left out of the skirmishes.
		scn.FreepsAssignToMinions(aragorn, slayer1);
		scn.ShadowAssignToMinions(aragorn, slayer2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertEquals(9, scn.GetStrength(slayer1));
		assertEquals(9, scn.GetStrength(slayer2));
		assertEquals(9, scn.GetStrength(slayer3));
		assertTrue(scn.ShadowActionAvailable(slayer1));
		scn.ShadowUseCardAction(slayer1);
		assertEquals(twilight - 1, scn.GetTwilight());
		assertEquals(10, scn.GetStrength(slayer1));
		assertEquals(9, scn.GetStrength(slayer2));
		assertEquals(9, scn.GetStrength(slayer3));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The text names no skirmish: the Slayer in none is offered it too, and strengthens itself alone.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(slayer3));
		scn.ShadowUseCardAction(slayer3);
		assertEquals(twilight - 2, scn.GetTwilight());
		assertEquals(10, scn.GetStrength(slayer3));
		assertEquals(10, scn.GetStrength(slayer1));
		assertEquals(9, scn.GetStrength(slayer2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWithNoTwilightInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var slayer1 = scn.GetShadowCard("slayer1");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, slayer1);
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(slayer1));
		assertEquals(9, scn.GetStrength(slayer1));
	}

	@Test
	public void AtPlusThreeItIsStillOfferedButAddsNothingAndAnotherCopyHasItsOwnLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var slayer1 = scn.GetShadowCard("slayer1");
		var slayer2 = scn.GetShadowCard("slayer2");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer1, slayer2);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, slayer1);
		scn.ShadowAssignToMinions(aragorn, slayer2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(slayer1));
		scn.ShadowUseCardAction(slayer1);
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer1);
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer1);
		assertEquals(12, scn.GetStrength(slayer1));
		scn.FreepsPass();

		// A limit on what the effect gives, with a cost: still offered, the (1) paid, and no more strength.
		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(slayer1));
		scn.ShadowUseCardAction(slayer1);
		assertEquals(twilight - 1, scn.GetTwilight());
		assertEquals(12, scn.GetStrength(slayer1));
		scn.FreepsPass();

		// The limit is each copy's own (CR 4.2 "limit").
		assertEquals(9, scn.GetStrength(slayer2));
		scn.ShadowUseCardAction(slayer2);
		assertEquals(twilight - 2, scn.GetTwilight());
		assertEquals(10, scn.GetStrength(slayer2));
		assertEquals(12, scn.GetStrength(slayer1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheBonusAndTheLimitLastOneSkirmishPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);

		var slayer1 = scn.GetShadowCard("slayer1");
		var slayer2 = scn.GetShadowCard("slayer2");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer1, slayer2);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, slayer1}, new PhysicalCardImpl[]{guard, slayer2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The second Slayer, waiting for its own skirmish, takes its +3 in the first.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(9, scn.GetStrength(slayer2));
		scn.ShadowUseCardAction(slayer2);
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer2);
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer2);
		assertEquals(12, scn.GetStrength(slayer2));
		assertEquals(9, scn.GetStrength(slayer1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsPass();
		scn.ShadowPass();

		// The first Slayer, 9, beats Aragorn, 8: two wounds with damage +1.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(guard);

		// A skirmish is a phase of its own: the +3 is gone, and the limit is fresh.
		assertEquals(9, scn.GetStrength(slayer2));
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(slayer2));
		scn.ShadowUseCardAction(slayer2);
		assertEquals(10, scn.GetStrength(slayer2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
