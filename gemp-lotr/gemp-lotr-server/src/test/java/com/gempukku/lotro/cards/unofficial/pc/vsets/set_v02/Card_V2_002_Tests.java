package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.InitiativeHandSizeModifier;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_002_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("raider", "102_2");
					put("raider2", "102_2");
					// Dunlending Brigand: another [dunland] Man minion of site 3, cost 2, no text. The spare
					// copies fill out hands.
					put("brigand", "4_10");
					put("brigand2", "4_10");
					put("brigand3", "4_10");
					put("brigand4", "4_10");

					put("pippin", "1_306");     // Pippin, Friend to Frodo: cost 1, nothing to spot, protects [shire] tales
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DunlendingRaiderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Dunlending Raider
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: While you have initiative, this minion's twilight cost is -2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("raider");

		assertEquals("Dunlending Raider", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CostsItsPrintedThreeWhileTheFreePeoplesPlayerHasFourCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var raider = scn.GetShadowCard("raider");
		var raider2 = scn.GetShadowCard("raider2");
		scn.MoveCardsToHand(raider, raider2);
		// The Shadow player's own hand is the larger, and is not what decides who has initiative.
		scn.MoveCardsToShadowHand("brigand", "brigand2", "brigand3", "brigand4");
		scn.MoveCardsToFreepsHand("brigand", "brigand2", "brigand3", "brigand4");

		scn.StartGame();
		scn.SetTwilight(6);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(6, scn.GetShadowHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 6 set + 1 companion + 2 for the site
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(raider));

		scn.ShadowPlayCard(raider);
		// 3 to play + 2 roaming: site number 3, played at site 2
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, raider);
		// The second copy needs 5 as well. At 1 + 2 it would be offered.
		assertFalse(scn.ShadowPlayAvailable(raider2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CostsTwoLessOnceTheFreePeoplesPlayerIsDownToThreeCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var raider = scn.GetShadowCard("raider");
		var raider2 = scn.GetShadowCard("raider2");
		var brigand = scn.GetShadowCard("brigand");
		var pippin = scn.GetFreepsCard("pippin");
		// The Shadow player's own hand is 3 cards from first to last.
		scn.MoveCardsToHand(raider, raider2, brigand);
		scn.MoveCardsToFreepsHand("pippin", "brigand", "brigand2", "brigand3");

		scn.StartGame();
		scn.SetTwilight(5);

		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(3, scn.GetTwilightCost(raider));
		assertEquals(3, scn.GetTwilightCost(raider2));
		assertEquals(2, scn.GetTwilightCost(brigand));

		// The Free Peoples player plays one of his 4 cards, and the Shadow has initiative.
		scn.FreepsPlayCard(pippin);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(1, scn.GetTwilightCost(raider));
		assertEquals(1, scn.GetTwilightCost(raider2));
		assertEquals(2, scn.GetTwilightCost(brigand));

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 5 set + 1 for Pippin + 2 companions + 2 for the site
		assertEquals(10, scn.GetTwilight());

		scn.ShadowPlayCard(raider);
		// 1 to play + 2 roaming: site number 3, played at site 2
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, raider);

		// The Brigand beside it in hand pays its full 2 + 2 roaming.
		scn.ShadowPlayCard(brigand);
		assertEquals(3, scn.GetTwilight());

		// A Raider in play takes nothing more off the next one: still 1 + 2, with exactly that in the pool.
		assertEquals(1, scn.GetTwilightCost(raider2));
		assertTrue(scn.ShadowPlayAvailable(raider2));
		scn.ShadowPlayCard(raider2);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, raider2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CostsTwoLessAgainstFourCardsInHandWhenInitiativeTakesFive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var raider = scn.GetShadowCard("raider");
		scn.MoveCardsToHand(raider);
		scn.MoveCardsToFreepsHand("brigand", "brigand2", "brigand3", "brigand4");

		scn.StartGame();

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(3, scn.GetTwilightCost(raider));

		// Initiative is made to take one card more, as Streaming to the Field (8_78) and Driven into the Hills
		// (V2_1) make it. The hand is the same 4 cards and the Shadow now has initiative: the discount goes by
		// initiative, not by a count of the cards in hand.
		scn.ApplyAdHocModifier(new InitiativeHandSizeModifier(null, null, new ConstantEvaluator(1)));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(1, scn.GetTwilightCost(raider));

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 1 companion + 2 for the site
		assertEquals(3, scn.GetTwilight());
		// Exactly 1 to play + 2 roaming. At its printed 3 + 2 it would not be offered.
		assertTrue(scn.ShadowPlayAvailable(raider));

		scn.ShadowPlayCard(raider);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, raider);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
