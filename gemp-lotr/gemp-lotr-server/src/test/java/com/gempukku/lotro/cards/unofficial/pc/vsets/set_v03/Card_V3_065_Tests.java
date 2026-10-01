package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_065_Tests
{

// ----------------------------------------
// COVER OF DARKNESS, OMEN OF GLOOM TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gloom", "103_65");       // Cover of Darkness, Omen of Gloom
					put("sky1", "103_97");        // Ominous Sky - Twilight condition
					put("sky2", "103_97");        // Second copy
					put("sky3", "103_97");        // Third copy (in deck for fetching)
					put("marshwight", "102_61");  // Marsh Wight - Twilight [Sauron] Wraith minion
					put("rwintwilight", "101_40"); // Ringwraith in Twilight - Twilight [Ringwraith] Nazgul minion
					put("orc", "1_271");          // Orc Soldier - non-Twilight minion
					put("hollowing", "3_54");     // Hollowing of Isengard - non-Twilight condition
					put("runner", "1_178");       // Goblin Runner - str 5, vit 1: dies to a single wound
					put("runner2", "1_178");

					put("aragorn", "1_89");       // str 8: beats a Goblin Runner in a skirmish
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
		 * Name: Cover of Darkness, Omen of Gloom
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Twilight.
		 * 	To play, hinder 2 twilight conditions.
		 * 	Each time your minion is killed, you may remove (1) and hinder a twilight condition to shuffle that minion into your draw deck.
		 * 	Regroup: Hinder a twilight card to add (1).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gloom");

		assertEquals("Cover of Darkness", card.getBlueprint().getTitle());
		assertEquals("Omen of Gloom", card.getBlueprint().getSubtitle());
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
	public void OmenOfGloomRequiresAndHinders2TwilightConditionsToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloom = scn.GetShadowCard("gloom");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		scn.MoveCardsToHand(gloom, sky1, sky2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		// No twilight conditions in play - can't play
		assertFalse(scn.ShadowPlayAvailable(gloom));

		// One twilight condition - still can't play
		scn.ShadowPlayCard(sky1);
		assertFalse(scn.ShadowPlayAvailable(gloom));

		// Two twilight conditions - now can play
		scn.ShadowPlayCard(sky2);
		assertTrue(scn.ShadowPlayAvailable(gloom));

		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(sky2));

		scn.ShadowPlayCard(gloom);
		// Only 2 twilight conditions, so auto-selected

		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(sky2));
		assertInZone(Zone.SUPPORT, gloom);
	}

// ========================================
// KILL TRIGGER TESTS - shuffle a killed minion back into the draw deck
// ========================================

	/** Aragorn skirmishes the Goblin Runner and kills it (Runner has 1 vitality). */
	private void KillRunnerInSkirmish(VirtualTableScenario scn, com.gempukku.lotro.game.PhysicalCardImpl aragorn,
			com.gempukku.lotro.game.PhysicalCardImpl runner, int twilight) throws DecisionResultInvalidException {
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);
		scn.SetTwilight(twilight);
		scn.PassSkirmishActions();
	}

	@Test
	public void KilledMinionCanBeShuffledIntoDrawDeckByRemoving1AndHinderingATwilightCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloom = scn.GetShadowCard("gloom");
		var sky1 = scn.GetShadowCard("sky1");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(gloom, sky1);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		KillRunnerInSkirmish(scn, aragorn, runner, 5);

		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// any twilight condition, including this one
		assertTrue(scn.ShadowHasCardChoiceAvailable(sky1, gloom));
		scn.ShadowChooseCard(sky1);

		assertTrue(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(gloom));
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.DECK, runner);
	}

	@Test
	public void KillTriggerCanBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloom = scn.GetShadowCard("gloom");
		var sky1 = scn.GetShadowCard("sky1");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(gloom, sky1);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		KillRunnerInSkirmish(scn, aragorn, runner, 5);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();

		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(gloom));
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.DISCARD, runner);
	}

	@Test
	public void WithNoOtherTwilightConditionItHindersItself() throws DecisionResultInvalidException, CardNotFoundException {
		// "a twilight condition" includes Omen of Gloom itself, so while it is active there is always one to hinder
		var scn = GetScenario();

		var gloom = scn.GetShadowCard("gloom");
		var hollowing = scn.GetShadowCard("hollowing"); // a non-twilight condition does not qualify
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(gloom, hollowing);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		KillRunnerInSkirmish(scn, aragorn, runner, 5);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		// only candidate, auto-selected

		assertTrue(scn.IsHindered(gloom));
		assertFalse(scn.IsHindered(hollowing));
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.DECK, runner);
	}

	@Test
	public void KillTriggerNotOfferedWithoutTwilightToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloom = scn.GetShadowCard("gloom");
		var sky1 = scn.GetShadowCard("sky1");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(gloom, sky1);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		KillRunnerInSkirmish(scn, aragorn, runner, 0);

		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.IsHindered(sky1));
	}

	@Test
	public void HinderedOmenOfGloomDoesNotTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloom = scn.GetShadowCard("gloom");
		var sky1 = scn.GetShadowCard("sky1");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(gloom, sky1);
		scn.HinderCard(gloom);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		KillRunnerInSkirmish(scn, aragorn, runner, 5);

		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void NoLongerHasAShadowAbility() throws DecisionResultInvalidException, CardNotFoundException {
		// Errata: the Shadow "take a twilight card into hand from your draw deck" ability is gone
		var scn = GetScenario();

		var gloom = scn.GetShadowCard("gloom");
		var sky1 = scn.GetShadowCard("sky1");
		var sky3 = scn.GetShadowCard("sky3"); // in deck
		scn.MoveCardsToSupportArea(gloom, sky1);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertInZone(Zone.DECK, sky3);
		assertFalse(scn.ShadowActionAvailable(gloom));
	}

// ========================================
// REGROUP ABILITY TESTS - Hinder Twilight to Add Twilight
// ========================================

	@Test
	public void OmenOfGloomRegroupAbilityCanHinderTwilightConditionsAndMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloom = scn.GetShadowCard("gloom");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var marshwight = scn.GetShadowCard("marshwight");
		scn.MoveCardsToSupportArea(gloom, sky1, sky2);
		scn.MoveMinionsToTable(marshwight);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPassCurrentPhaseAction();

		int twilightBefore = scn.GetTwilight();

		// First use - hinder sky1
		scn.ShadowUseCardAction(gloom);
		scn.ShadowChooseCard(sky1);
		assertEquals(twilightBefore + 1, scn.GetTwilight());

		scn.FreepsPassCurrentPhaseAction();

		// Second use - hinder sky2
		scn.ShadowUseCardAction(gloom);
		scn.ShadowChooseCard(sky2);
		assertEquals(twilightBefore + 2, scn.GetTwilight());

		scn.FreepsPassCurrentPhaseAction();

		// Third use - hinder marshwight (twilight minion)
		scn.ShadowUseCardAction(gloom);
		scn.ShadowChooseCard(marshwight);
		assertEquals(twilightBefore + 3, scn.GetTwilight());

		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowUseCardAction(gloom);
		//gloom auto-selected as only remaining unhindered twilight card
		assertEquals(twilightBefore + 4, scn.GetTwilight());

		// Fourth use - hinder gloom itself

		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(sky2));
		assertTrue(scn.IsHindered(gloom));
		assertTrue(scn.IsHindered(marshwight));
	}
}
