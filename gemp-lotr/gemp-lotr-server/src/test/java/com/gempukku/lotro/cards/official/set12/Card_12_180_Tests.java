package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_12_180_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("toldea", "12_180");

					put("approach", "12_162");    // Dark Approach: [wraith] Shadow event
					put("riders", "11_215");      // Riders in Black: [wraith] Skirmish event
					put("bladetip", "1_209");     // Blade Tip: a [wraith] card that is no event (a condition)
					put("forces", "1_248");       // Forces of Mordor: a [sauron] event
					// In the Ringwraith's Wake: a [wraith] event, laid only in the Free Peoples player's discard pile
					put("wake", "12_165");

					put("blackrider", "12_161");  // Black Rider: a Nazgul, cost 5, fierce; strength +1 for each companion
					// Peril: [sauron] support-area condition, "Regroup: Exert a [sauron] Orc to play a Nazgûl or
					// [sauron] minion. Its twilight cost is -4 and it comes into play exhausted."
					put("peril", "6_105");
					// Orc Chieftain: a [sauron] Orc, vitality 2; its text answers a weapon played on it
					put("chieftain", "1_266");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireToldeaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Úlairë Toldëa, Black Shadow
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: Fierce.
		 * 	When you play Úlairë Toldëa, you may exert him to take a [wraith] event from your discard pile into hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("toldea");

		assertEquals("Úlairë Toldëa", card.getBlueprint().getTitle());
		assertEquals("Black Shadow", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedHeMayExertToTakeAWraithEventFromTheDiscardPileIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toldea = scn.GetShadowCard("toldea");
		var approach = scn.GetShadowCard("approach");
		var riders = scn.GetShadowCard("riders");
		var bladetip = scn.GetShadowCard("bladetip");
		var forces = scn.GetShadowCard("forces");
		var freepsWake = scn.GetFreepsCard("wake");
		scn.MoveCardsToHand(toldea);
		scn.MoveCardsToDiscard(approach, riders, bladetip, forces, freepsWake);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(toldea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(toldea));
		assertEquals(0, scn.GetWoundsOn(toldea));
		assertInDiscard(approach, riders, bladetip, forces, freepsWake);
		assertEquals(0, scn.GetShadowHandCount());

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(toldea));
		// [wraith] events of two phases are offered; a [wraith] card that is no event, a [sauron] event and the
		// other player's [wraith] event are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(approach, riders));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bladetip, forces, freepsWake));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(riders);
		assertInHand(riders);
		assertEquals(1, scn.GetShadowHandCount());
		assertInDiscard(approach, bladetip, forces, freepsWake);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DeclinedNothingChangesAndAnotherNazgulPlayedAfterHimSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toldea = scn.GetShadowCard("toldea");
		var blackrider = scn.GetShadowCard("blackrider");
		var approach = scn.GetShadowCard("approach");
		var riders = scn.GetShadowCard("riders");
		scn.MoveCardsToHand(toldea, blackrider);
		scn.MoveCardsToDiscard(approach, riders);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(toldea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(toldea));
		assertEquals(0, scn.GetWoundsOn(toldea));

		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(toldea));
		assertInDiscard(approach, riders);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another Nazgûl played while he is in play, able to exert, with [wraith] events in the discard pile.
		scn.ShadowPlayCard(blackrider);
		assertInZone(Zone.SHADOW_CHARACTERS, blackrider);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(toldea));
		assertInDiscard(approach, riders);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedExhaustedByPerilHeCannotExertAndIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toldea = scn.GetShadowCard("toldea");
		var approach = scn.GetShadowCard("approach");
		var peril = scn.GetShadowCard("peril");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveCardsToSupportArea(peril);
		scn.SetShadowHand(toldea);
		scn.MoveCardsToDiscard(approach);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A [sauron] Orc for Peril to exert.
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(peril));
		scn.ShadowUseCardAction(peril);
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertInZone(Zone.SHADOW_CHARACTERS, toldea);
		// Vitality 3 and played exhausted: 2 wounds, and he cannot exert.
		assertEquals(2, scn.GetWoundsOn(toldea));

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(approach);
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
