package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_064_Tests
{

// ----------------------------------------
// COVER OF DARKNESS, OMEN OF FEAR TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fear", "103_64");        // Cover of Darkness, Omen of Fear
					put("sky1", "103_97");        // Ominous Sky - Twilight condition
					put("sky2", "103_97");        // Second copy
					put("squealer1", "8_77");     // Morgul Squealer - [Ringwraith] Orc
					put("squealer2", "8_77");     // Second copy
					put("witchking", "8_84");     // The Witch-king, Black Captain - [Ringwraith] Nazgul

					put("aragorn", "1_89");       // Unbound companion
					put("gimli", "1_13");       // Another unbound companion
					put("sam", "1_311");          // Ring-bound companion (not unbound)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CoverofDarknessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Cover of Darkness, Omen of Fear
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Twilight.
		 * 	To play, hinder 2 twilight conditions.
		 * 	Assignment: Spot 3 [ringwraith] minions, remove a burden, and hinder this condition to spot a [ringwraith]
		 * 	minion and an unbound companion.  Hinder all other characters.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fear");

		assertEquals("Cover of Darkness", card.getBlueprint().getTitle());
		assertEquals("Omen of Fear", card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}



// ========================================
// EXTRA COST TESTS
// ========================================

	@Test
	public void OmenOfFearRequiresAndHinders2TwilightConditionsToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		scn.MoveCardsToHand(fear, sky1, sky2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		// No twilight conditions in play - can't play
		assertFalse(scn.ShadowPlayAvailable(fear));

		// One twilight condition - still can't play (need 2, fear itself counts but isn't in play yet)
		scn.ShadowPlayCard(sky1);
		assertFalse(scn.ShadowPlayAvailable(fear));
		// Need a second twilight condition

		scn.ShadowPlayCard(sky2);

		assertTrue(scn.ShadowPlayAvailable(fear));

		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(sky2));

		scn.ShadowPlayCard(fear);
		// Only 2 twilight conditions, so auto-selected

		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(sky2));
		assertInZone(Zone.SUPPORT, fear);
	}

// ========================================
// ASSIGNMENT ABILITY TESTS
// ========================================

	@Test
	public void OmenOfFearAbilityRequires3RingwraithMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var squealer1 = scn.GetShadowCard("squealer1");
		var squealer2 = scn.GetShadowCard("squealer2");
		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(fear, sky1, sky2);
		scn.HinderCard(sky1, sky2);
		scn.MoveMinionsToTable(squealer1, squealer2); // Only 2 [ringwraith] minions
		scn.MoveCompanionsToTable(aragorn);
		scn.AddBurdens(1);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Only 2 [ringwraith] minions - ability not available
		assertFalse(scn.FreepsActionAvailable(fear));
		assertFalse(scn.ShadowActionAvailable(fear));

		// Add third [ringwraith] minion
		scn.MoveMinionsToTable(witchking);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowActionAvailable(fear));
	}

	@Test
	public void OmenOfFearAbilityRequiresBurdenToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var squealer1 = scn.GetShadowCard("squealer1");
		var squealer2 = scn.GetShadowCard("squealer2");
		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(fear, sky1, sky2);
		scn.HinderCard(sky1, sky2);
		scn.MoveMinionsToTable(squealer1, squealer2, witchking);
		scn.MoveCompanionsToTable(aragorn);
		// No burdens added

		scn.StartGame();
		scn.RemoveBurdens(1); // Remove the starting burden from bidding
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetBurdens());
		scn.FreepsPassCurrentPhaseAction();

		// No burdens to remove - ability not available
		assertFalse(scn.ShadowActionAvailable(fear));
	}

	@Test
	public void OmenOfFearAbilityRequiresUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var squealer1 = scn.GetShadowCard("squealer1");
		var squealer2 = scn.GetShadowCard("squealer2");
		var witchking = scn.GetShadowCard("witchking");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToSupportArea(fear, sky1, sky2);
		scn.HinderCard(sky1, sky2);
		scn.MoveMinionsToTable(squealer1, squealer2, witchking);
		scn.MoveCompanionsToTable(sam); // Ring-bound, not unbound
		scn.AddBurdens(1);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPassCurrentPhaseAction();

		// Frodo is ring-bound, Sam is ring-bound - no unbound companions
		assertFalse(scn.ShadowActionAvailable(fear));
	}

	@Test
	public void OmenOfFearAbilityCannotBeUsedIfSelfAlreadyHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var squealer1 = scn.GetShadowCard("squealer1");
		var squealer2 = scn.GetShadowCard("squealer2");
		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(fear, sky1, sky2);
		scn.HinderCard(sky1, sky2, fear); // Fear already hindered
		scn.MoveMinionsToTable(squealer1, squealer2, witchking);
		scn.MoveCompanionsToTable(aragorn);
		scn.AddBurdens(1);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPassCurrentPhaseAction();

		// Self already hindered - ability not available
		assertFalse(scn.ShadowActionAvailable(fear));
	}

	@Test
	public void OmenOfFearAbilityHindersAllCharactersExceptChosenMinionAndCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var fear = scn.GetShadowCard("fear");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var squealer1 = scn.GetShadowCard("squealer1");
		var squealer2 = scn.GetShadowCard("squealer2");
		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(fear, sky1, sky2);
		scn.HinderCard(sky1, sky2);
		scn.MoveMinionsToTable(squealer1, squealer2, witchking);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AddBurdens(1);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		int burdensBefore = scn.GetBurdens();
		assertFalse(scn.IsHindered(fear));
		assertFalse(scn.IsHindered(squealer1));
		assertFalse(scn.IsHindered(squealer2));
		assertFalse(scn.IsHindered(witchking));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(gimli));
		assertFalse(scn.IsHindered(frodo));

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(fear);

		// Choose which [ringwraith] minion to spare
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoicesAvailable(squealer1, squealer2, witchking));
		scn.ShadowChooseCard(witchking);

		// Choose which unbound companion to spare
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoicesAvailable(aragorn, gimli));
		assertFalse(scn.ShadowHasCardChoiceAvailable(frodo)); // Ring-bound, not valid
		scn.ShadowChooseCard(aragorn);

		// Verify costs paid
		assertEquals(burdensBefore - 1, scn.GetBurdens());
		assertTrue(scn.IsHindered(fear));

		// Verify chosen minion and companion are NOT hindered
		assertFalse(scn.IsHindered(witchking));
		assertFalse(scn.IsHindered(aragorn));

		// Verify all OTHER characters ARE hindered
		assertTrue(scn.IsHindered(squealer1));
		assertTrue(scn.IsHindered(squealer2));
		assertTrue(scn.IsHindered(gimli));
		assertTrue(scn.IsHindered(frodo));
	}

	@Test
	public void OmenOfFearAbilityOnlyAvailableDuringAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var squealer1 = scn.GetShadowCard("squealer1");
		var squealer2 = scn.GetShadowCard("squealer2");
		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(fear, sky1, sky2);
		scn.HinderCard(sky1, sky2);
		scn.MoveMinionsToTable(squealer1, squealer2, witchking);
		scn.MoveCompanionsToTable(aragorn);
		scn.AddBurdens(1);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		assertFalse(scn.FreepsActionAvailable(fear));
		scn.FreepsPassCurrentPhaseAction();
		assertFalse(scn.ShadowActionAvailable(fear));
	}
}
