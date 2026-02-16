package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInDiscard;
import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_072_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("attea", "103_72");
					put("savage", "1_151");     // Uruk Savage, cost 2
					put("soldier", "1_271");    // Orc Soldier, cost 2
					put("runner", "1_178");     // Goblin Runner, cost 1
					put("host", "1_187");       // Host of Thousands - play Moria Orc from discard

					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireAtteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ulaire Attea, Bestowed with Dominion
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: Fierce.
		* 	Each time you play a minion from hand, you may exert Ulaire Attea to play another minion with the same twilight cost from your discard pile.
		* 	Skirmish: Hinder another minion to make Ulaire Attea strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("attea");

		assertEquals("Úlairë Attëa", card.getBlueprint().getTitle());
		assertEquals("Bestowed with Dominion", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}



// ======== PLAY FROM DISCARD TRIGGER TESTS ========

	@Test
	public void AtteaCanPlaySameCostMinionFromDiscardWhenPlayingFromHandAndDoesNotSelfTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var savage = scn.GetShadowCard("savage");   // Cost 2
		var soldier = scn.GetShadowCard("soldier"); // Cost 2
		var runner = scn.GetShadowCard("runner");   // Cost 1

		scn.MoveMinionsToTable(attea);
		scn.MoveCardsToHand(savage);
		scn.MoveCardsToDiscard(soldier, runner);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetWoundsOn(attea));

		// Play Savage (cost 2) from hand
		scn.ShadowPlayCard(savage);

		// Attëa's trigger should fire
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Attëa exerts
		assertEquals(1, scn.GetWoundsOn(attea));

		// Only Soldier (cost 2) should be valid, not Runner (cost 1)
		// Soldier now in play
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);

		// Soldier was played from discard via Attëa's effect
		// This should NOT trigger Attëa again
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}


	@Test
	public void AtteaDoesNotTriggerWhenOtherEffectPlaysMinionFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var host = scn.GetShadowCard("host");     // Host of Thousands
		var runner = scn.GetShadowCard("runner"); // Moria Orc, cost 1
		var soldier = scn.GetShadowCard("soldier"); // Cost 2

		scn.MoveMinionsToTable(attea);
		scn.MoveCardsToHand(host);
		scn.MoveCardsToDiscard(runner, soldier);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		// Play Host of Thousands (event that plays Moria Orc from discard)
		scn.ShadowPlayCard(host);
		// Auto-selects runner as only valid Moria Orc

		// Runner was played from discard, not hand
		// Attëa should NOT trigger
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// Runner should be in play
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
	}

	@Test
	public void AtteaTriggerCanBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var savage = scn.GetShadowCard("savage");
		var soldier = scn.GetShadowCard("soldier");

		scn.MoveMinionsToTable(attea);
		scn.MoveCardsToHand(savage);
		scn.MoveCardsToDiscard(soldier);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(savage);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();

		// Attëa not exerted, Soldier still in discard
		assertEquals(0, scn.GetWoundsOn(attea));
		assertInDiscard(soldier);
	}

	@Test
	public void AtteaTriggerNotAvailableWithNoMatchingCostInDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var savage = scn.GetShadowCard("savage");   // Cost 2
		var runner = scn.GetShadowCard("runner");   // Cost 1 - no match

		scn.MoveMinionsToTable(attea);
		scn.MoveCardsToHand(savage);
		scn.MoveCardsToDiscard(runner);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(savage);

		// No cost-2 minion in discard, trigger shouldn't be available
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

// ======== SKIRMISH ABILITY TESTS ========

	@Test
	public void AtteaCanHinderAnotherMinionForStrengthBonus() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var savage = scn.GetShadowCard("savage");
		var soldier = scn.GetShadowCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(attea, savage, soldier);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, attea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		int atteaStrength = scn.GetStrength(attea);

		// First use - hinder savage
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(attea);
		scn.ShadowChooseCard(savage);

		assertEquals(atteaStrength + 1, scn.GetStrength(attea));

		// Second use - hinder soldier
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(attea);
		// Soldier is only remaining unhindere minion, auto-selected

		assertEquals(atteaStrength + 2, scn.GetStrength(attea));
		assertTrue(scn.IsHindered(savage));
		assertTrue(scn.IsHindered(soldier));
	}

	@Test
	public void AtteaSkirmishAbilityNotAvailableWithNoOtherMinions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(attea);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, attea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		scn.FreepsPassCurrentPhaseAction();

		// No other minions to hinder - ability not available
		assertFalse(scn.ShadowActionAvailable(attea));
	}
}
