package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_065_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("overseer", "3_65");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, vitality 3

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit, Ring-bound but not the Ring-bearer, vitality 4; used only on purpose
					put("guard", "1_7");        // Dwarf Guard: [dwarven] Dwarf companion, vitality 2; no text but its play requirement
					put("barliman", "1_70");    // Barliman Butterbur: a Man ally, vitality 2; a fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcOverseerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Orc Overseer
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: <b>Regroup:</b> Exert Orc Overseer twice to exhaust a companion (except the Ring-bearer).
		*/

		var scn = GetScenario();

		var overseer = scn.GetFreepsCard("overseer");

		assertEquals("Orc Overseer", overseer.getBlueprint().getTitle());
		assertNull(overseer.getBlueprint().getSubtitle());
		assertTrue(overseer.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, overseer.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, overseer.getBlueprint().getCulture());
		assertEquals(CardType.MINION, overseer.getBlueprint().getCardType());
		assertEquals(Race.ORC, overseer.getBlueprint().getRace());
		assertEquals(5, overseer.getBlueprint().getTwilightCost());
		assertEquals(10, overseer.getBlueprint().getStrength());
		assertEquals(4, overseer.getBlueprint().getVitality());
		assertEquals(4, overseer.getBlueprint().getSiteNumber());
	}

	@Test
	public void RegroupAbilityExertsHimTwiceToExhaustACompanionOtherThanTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, sam, guard);
		scn.MoveAlliesToTable(barliman);

		var overseer = scn.GetShadowCard("overseer");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(overseer, troop);
		// Vitality 4 with 1 wound: exactly the two exertions the cost needs are left in him.
		scn.AddWoundsToChar(overseer, 1);
		// Vitality 2 with 1 wound: the Dwarf Guard is already exhausted.
		scn.AddWoundsToChar(guard, 1);
		assertEquals(1, scn.GetWoundsOn(overseer));
		assertEquals(1, scn.GetWoundsOn(guard));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(1, scn.GetWoundsOn(overseer));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(overseer));

		scn.ShadowUseCardAction(overseer);
		// He exerts twice; the other minion does not.
		assertEquals(3, scn.GetWoundsOn(overseer));
		assertEquals(0, scn.GetWoundsOn(troop));

		// The Shadow player chooses: a companion other than the Ring-bearer, Sam (Ring-bound) among them; not the
		// Ring-bearer, not a companion already exhausted, not an ally, not a minion.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, guard, barliman, troop, overseer));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		// Vitality 4: exhausted at 3 wounds, which one exertion or two would not give.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsExhausted(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhenHeCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var overseer = scn.GetShadowCard("overseer");

		scn.StartGame();
		scn.MoveMinionsToTable(overseer);
		// Vitality 4 with 2 wounds: one exertion left in him, not two.
		scn.AddWoundsToChar(overseer, 2);
		assertEquals(2, scn.GetWoundsOn(overseer));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(2, scn.GetWoundsOn(overseer));
		assertFalse(scn.IsExhausted(overseer));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(overseer));
	}

	@Test
	public void WithOnlyTheRingBearerAsCompanionTheAbilityIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveAlliesToTable(barliman);

		var overseer = scn.GetShadowCard("overseer");

		scn.StartGame();
		scn.MoveMinionsToTable(overseer);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(overseer));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		// The cost can be paid, so the ability is offered though no companion can be chosen.
		assertTrue(scn.ShadowActionAvailable(overseer));

		scn.ShadowUseCardAction(overseer);
		assertEquals(2, scn.GetWoundsOn(overseer));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
