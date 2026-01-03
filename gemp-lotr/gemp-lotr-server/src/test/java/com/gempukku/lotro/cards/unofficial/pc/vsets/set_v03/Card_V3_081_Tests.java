package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_081_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nazgul1", "103_81");
					put("nazgul2", "103_81");

					put("aragorn", "1_89");
					put("boromir", "1_96");
					put("sam", "1_311");       // Ring-bound
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WingedNazgulStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Winged Nazgul
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: Enduring. Fierce.
		* 	Assignment: Return another Winged Nazgul to hand to assign this minion to an unbound companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nazgul1");

		assertEquals("Winged Nazgul", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}



// ======== BASIC FUNCTIONALITY ========

	@Test
	public void WingedNazgulCanBounceAnotherToAssignSelf() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var nazgul1 = scn.GetShadowCard("nazgul1");
		var nazgul2 = scn.GetShadowCard("nazgul2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();

		scn.MoveMinionsToTable(nazgul1, nazgul2);
		scn.MoveCompanionsToTable(aragorn, boromir, sam);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.FreepsPassCurrentPhaseAction();

		// Use nazgul1's ability, bouncing nazgul2
		scn.ShadowUseCardAction(nazgul1);

		// Choose which Winged Nazgul to return (nazgul2 is only valid "another")
		// Auto-selected since only one valid target

		// Choose unbound companion to assign to
		assertTrue(scn.ShadowHasCardChoicesAvailable(aragorn, boromir));
		assertFalse(scn.ShadowHasCardChoicesAvailable(frodo, sam));
		scn.ShadowChooseCard(aragorn);

		// Nazgul2 returned to hand
		assertInHand(nazgul2);

		assertTrue(scn.IsCharAssignedAgainst(nazgul1, aragorn));

		// Nazgul1 assigned to Aragorn
		// Skip to skirmish to verify assignment
		scn.BothPass();
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishing(nazgul1));
		assertTrue(scn.IsCharSkirmishing(aragorn));
	}

	@Test
	public void WingedNazgulAbilityNotAvailableWithOnlyOneCopy() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var nazgul1 = scn.GetShadowCard("nazgul1");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(nazgul1);  // Only one Winged Nazgul
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.FreepsPassCurrentPhaseAction();

		// Can't use ability - no "another Winged Nazgul" to return
		assertFalse(scn.ShadowActionAvailable(nazgul1));
	}


	@Test
	public void WingedNazgulAbilityFizzlesWithNoUnboundCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var nazgul1 = scn.GetShadowCard("nazgul1");
		var nazgul2 = scn.GetShadowCard("nazgul2");
		var sam = scn.GetFreepsCard("sam");
		// Only Frodo (RB) and Sam (Ring-bound) as companions

		scn.MoveMinionsToTable(nazgul1, nazgul2);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowUseCardAction(nazgul1);
		//No companion to select since there's no unbound companions
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
