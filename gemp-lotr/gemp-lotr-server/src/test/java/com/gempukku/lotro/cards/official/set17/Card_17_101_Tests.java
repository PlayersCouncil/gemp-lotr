package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_17_101_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("soldier1", "17_101");
					put("soldier2", "17_101");

					put("guard", "7_246");       // Rohirrim Guard: [rohan] Man companion, strength 6; a skirmish ability used only on purpose
					put("sam", "1_311");         // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer

					put("runner1", "1_178");     // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
					put("runner3", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// The Great Ring adds no strength and, not worn, answers no wound: Frodo is strength 3 and takes his wounds
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void SoldierofRohanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Soldier of Rohan
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 5
		 * Game Text: While the Ring-bearer is assigned to a skirmish, this companion is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("soldier1");

		assertEquals("Soldier of Rohan", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
	}

	@Test
	public void IsStrengthPlus2WhileTheRingBearerIsAssignedAndNotOnceHisSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var soldier1 = scn.GetFreepsCard("soldier1");
		var soldier2 = scn.GetFreepsCard("soldier2");
		var guard = scn.GetFreepsCard("guard");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		scn.MoveCompanionsToTable(soldier1, soldier2, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, runner3);
		scn.SkipToAssignments();

		// Nobody is assigned yet.
		assertFalse(scn.IsCharAssigned(frodo));
		assertEquals(5, scn.GetStrength(soldier1));
		assertEquals(5, scn.GetStrength(soldier2));
		assertEquals(6, scn.GetStrength(guard));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{soldier1, runner1}, new PhysicalCardImpl[]{frodo, runner2},
				new PhysicalCardImpl[]{soldier2, runner3});

		// Each copy has its own +2 and no more; the Rohirrim Guard, another [rohan] Man companion, is untouched.
		assertTrue(scn.IsCharAssigned(frodo));
		assertEquals(5 + 2, scn.GetStrength(soldier1));
		assertEquals(5 + 2, scn.GetStrength(soldier2));
		assertEquals(6, scn.GetStrength(guard));

		// A Soldier's own skirmish, with Frodo's still waiting
		scn.FreepsResolveSkirmish(soldier1);
		assertTrue(scn.IsCharAssigned(frodo));
		assertEquals(5 + 2, scn.GetStrength(soldier1));
		scn.PassCurrentPhaseActions();
		assertEquals(0, scn.GetWoundsOn(soldier1));

		// Frodo's skirmish: he is assigned to it while it is fought.
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(5 + 2, scn.GetStrength(soldier2));
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(frodo));

		// His skirmish is over: he is assigned to none.
		scn.FreepsResolveSkirmish(soldier2);
		assertEquals(5, scn.GetStrength(soldier2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ARingBoundCompanionWhoIsNotTheRingBearerBeingAssignedDoesNotStrengthenHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var soldier1 = scn.GetFreepsCard("soldier1");
		var sam = scn.GetFreepsCard("sam");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(soldier1, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{soldier1, runner1}, new PhysicalCardImpl[]{sam, runner2});

		assertTrue(scn.IsCharAssigned(sam));
		assertFalse(scn.IsCharAssigned(frodo));
		assertEquals(5, scn.GetStrength(soldier1));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
