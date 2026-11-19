package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_030_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  The Shadow phase of
	// turn 1 is at The Troll Hoard (site 2), where the Runner (site 4) is roaming.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("runner", "31_30");
					put("runner2", "31_30");
					// [moria] cards to spot, of three kinds
					put("scimitar", "31_31");   // Goblin Scimitar: [moria] hand weapon on an Orc; a text on its own play only
					put("power", "31_35");      // Great Goblin's Power: [moria] support-area condition; adds (1) when a weapon is played, a response to an Orc's wound
					put("sneak", "31_32");      // Goblin Sneak: [moria] Orc; when played, may put an Orc from the discard pile under the deck (none there)
					// Shadow cards of another culture
					put("yazneg", "30_41");     // Yazneg, Orkish Assassin: [gundabad] Orc; a maneuver ability not used
					put("hatred", "30_34");     // Hatred Rekindled: [gundabad] support-area condition; a Shadow ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GoblinRunnerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Goblin Runner
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: When you play this minion, you may spot another [moria] card to add (2).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("runner");

		assertEquals("Goblin Runner", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SpottingAMoriaPossessionLetsTheRunnerAdd2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar");
		var yazneg = scn.GetShadowCard("yazneg");
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(yazneg);
		// The only [moria] card in play: a possession, on an Orc of another culture.
		scn.AttachCardsTo(yazneg, scimitar);
		assertAttachedTo(scimitar, yazneg);
		assertInHand(runner);
		scn.FreepsPass();

		scn.ShadowPlayCard(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(runner));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingAMoriaConditionOffersTheTwilightAndDecliningAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var power = scn.GetShadowCard("power");
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveCardsToSupportArea(power);
		assertInZone(Zone.SUPPORT, power);
		assertInHand(runner);
		scn.FreepsPass();

		scn.ShadowPlayCard(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(runner));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheRunnerIsNotAnotherMoriaCardForItselfButIsForASecondCopyAndAnswersNoOtherPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var sneak = scn.GetShadowCard("sneak");
		var yazneg = scn.GetShadowCard("yazneg");
		var hatred = scn.GetShadowCard("hatred");
		// The Free Peoples player's own [moria] condition, in his support area: his Shadow cards are not active on
		// his own turn, so it cannot be spotted.
		var fpPower = scn.GetFreepsCard("power");
		scn.MoveCardsToHand(runner, runner2, sneak);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.MoveMinionsToTable(yazneg);
		scn.MoveCardsToSupportArea(hatred, fpPower);
		assertInZone(Zone.SHADOW_CHARACTERS, yazneg);
		assertInZone(Zone.SUPPORT, hatred);
		assertInZone(Zone.SUPPORT, fpPower);
		assertInHand(runner, runner2, sneak);
		scn.FreepsPass();

		// Shadow cards of another culture, and the Runner itself: nothing to spot.
		scn.ShadowPlayCard(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The first Runner is another [moria] card for the second; the first does not answer the second's play.
		scn.ShadowPlayCard(runner2);
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(runner2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(runner));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [moria] minion played: neither Runner answers it.
		scn.ShadowPlayCard(sneak);
		assertInZone(Zone.SHADOW_CHARACTERS, sneak);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(runner));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(runner2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
