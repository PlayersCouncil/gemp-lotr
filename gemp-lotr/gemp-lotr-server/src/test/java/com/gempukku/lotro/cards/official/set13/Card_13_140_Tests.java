package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_140_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "13_140");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDiscountScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sauron", "13_140");
					put("troop1", "1_177");     // Goblin Patrol Troop: a minion of vitality 3 with no text at all
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SauronStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Sauron, Dark Lord of Mordor
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 16
		 * Type: Minion
		 * Subtype: Maia
		 * Strength: 24
		 * Vitality: 5
		 * Site Number: 6
		 * Game Text: <b>Damage +1</b>. <b>Fierce</b>.<br>When you play Sauron, you may exert any number of minions. For each minion you exert, Sauron is twilight cost -X, where X is the current region number.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Sauron", card.getBlueprint().getTitle());
		assertEquals("Dark Lord of Mordor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAIA, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(16, card.getBlueprint().getTwilightCost());
		assertEquals(24, card.getBlueprint().getStrength());
		assertEquals(5, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ExertingAMinionAsHeIsPlayedTakesTheRegionNumberOffHisCostAndAPoolOneShortThenPlaysHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscountScenario();

		var sauron = scn.GetShadowCard("sauron");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCardsToHand(sauron);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		// 14, and for the move 1 companion and the 2 of Ettenmoors
		scn.SetTwilight(14);
		scn.FreepsPass();

		// Site 2 is in region 1.  16 to play + 2 roaming (his site number is 6) = 18: the pool is one short,
		// and one minion exerted takes 1 off.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(17, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertTrue(scn.ShadowPlayAvailable(sauron));

		scn.ShadowPlayCard(sauron);
		// "Any number of minions", but no fewer than the pool needs: he cannot be played for less than he
		// costs by exerting nobody.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop1, troop2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(troop2);

		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(1, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, sauron);
		assertEquals(0, scn.GetWoundsOn(sauron));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThePoolOneShortAndNoMinionAbleToExertHeIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscountScenario();

		var sauron = scn.GetShadowCard("sauron");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCardsToHand(sauron);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		// Vitality 3 with 2 wounds: the one minion in play is exhausted, and can be spotted but not exerted.
		scn.AddWoundsToChar(troop1, 2);
		// 14, and for the move 1 companion and the 2 of Ettenmoors
		scn.SetTwilight(14);
		scn.FreepsPass();

		// 16 to play + 2 roaming = 18, with no exertion to take anything off it.
		assertEquals(17, scn.GetTwilight());
		assertInZone(Zone.HAND, sauron);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(sauron));

		// One more in the pool, and he is offered at his whole cost.
		scn.SetTwilight(18);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(sauron));
		assertEquals(2, scn.GetWoundsOn(troop1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
