package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_097_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("soldier", "5_97");
					put("soldier2", "5_97");
					put("strength", "1_278");   // Strength Born of Fear: [sauron] condition, support area, cost 1; a skirmish ability only
					put("evil", "1_283");       // You Bring Great Evil: [sauron] condition on a companion, cost 1; to play, spot a [sauron] Orc
					put("few", "1_260");        // The Number Must Be Few: [sauron] condition, support area, cost 2; reads 7 companions
					put("banner", "6_98");      // Banner of the Eye: [sauron] POSSESSION on a [sauron] Orc, cost 0; a trigger on a skirmish won
					put("chasm", "13_44");      // Chasm's Edge: [gollum] condition, support area, cost 1; pumps Gollum only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GateSoldierStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Gate Soldier
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: When you play a [sauron] condition, you may exert this minion and remove (2) to draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("soldier");

		assertEquals("Gate Soldier", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayingASauronConditionLetsEachCopyExertAndRemoveTwoToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetShadowCard("soldier");
		var soldier2 = scn.GetShadowCard("soldier2");
		var strength = scn.GetShadowCard("strength");
		scn.MoveCardsToHand(strength);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, soldier2);
		scn.SetTwilight(2);

		// 2 + 2 for Ettenmoors + 1 for the Ring-bearer
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(soldier2));
		assertEquals(1, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(strength);
		assertInZone(Zone.SUPPORT, strength);
		// Each copy makes its own offer.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier2));
		// 5 - 1 for the condition
		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetShadowHandCount());

		scn.ShadowChooseOptionalTrigger(soldier);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(soldier2));
		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());

		// The other copy's offer still stands; it is declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier2));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(soldier2));
		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningChangesNothingAndASauronConditionOnACompanionIsAnsweredAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var soldier = scn.GetShadowCard("soldier");
		var strength = scn.GetShadowCard("strength");
		var evil = scn.GetShadowCard("evil");
		scn.MoveCardsToHand(strength, evil);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SetTwilight(2);

		// 2 + 2 for Ettenmoors + 1 for the Ring-bearer
		scn.FreepsPass();
		assertEquals(5, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(strength);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(soldier));
		// 5 - 1 for the condition, and nothing more
		assertEquals(4, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Frodo is the only companion it can go on, so it goes on him unasked.
		scn.ShadowPlayCard(evil);
		assertAttachedTo(evil, frodo);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(soldier));
		// 4 - 1 for the condition - 2 for the cost
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayingASauronPossessionOrAConditionOfAnotherCultureIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetShadowCard("soldier");
		var banner = scn.GetShadowCard("banner");
		var chasm = scn.GetShadowCard("chasm");
		scn.MoveCardsToHand(banner, chasm);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SetTwilight(2);

		scn.FreepsPass();
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(soldier));

		// The Gate Soldier is the only [sauron] Orc, so the Banner goes on him unasked.
		scn.ShadowPlayCard(banner);
		assertAttachedTo(banner, soldier);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(soldier));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(chasm);
		assertInZone(Zone.SUPPORT, chasm);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(soldier));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(soldier));
		// 5 - 0 for the Banner - 1 for Chasm's Edge
		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetShadowHandCount());
	}

	@Test
	public void AnExhaustedGateSoldierMakesNoOfferButAFreshCopyDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetShadowCard("soldier");
		var soldier2 = scn.GetShadowCard("soldier2");
		var strength = scn.GetShadowCard("strength");
		scn.MoveCardsToHand(strength);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, soldier2);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(soldier, 1);
		assertEquals(1, scn.GetWoundsOn(soldier));
		scn.SetTwilight(2);

		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(5, scn.GetTwilight());

		scn.ShadowPlayCard(strength);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(soldier));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier2));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOnlyOneTwilightLeftAfterTheConditionNoOfferIsMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetShadowCard("soldier");
		var few = scn.GetShadowCard("few");
		scn.MoveCardsToHand(few);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);

		// 0 + 2 for Ettenmoors + 1 for the Ring-bearer
		scn.FreepsPass();
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(soldier));

		scn.ShadowPlayCard(few);
		// 3 - 2 for the condition: one short of the (2) to remove.
		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(soldier));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(soldier));
	}
}
