package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_045_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "10_45");
					// Mûmak: a [raider] mount, cost 2, strength +3; "Bearer must be a Southron."  Bearer is fierce.
					put("mumak", "5_73");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MumakChieftainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Mûmak Chieftain
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Southron</b>.<br>When you play this minion, you may play a mount on him from your discard pile. Its twilight cost is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Mûmak Chieftain", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AMountCostingTwoIsPlayedOnHimFromTheDiscardPileForNothingWithAnEmptyPool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chieftain = scn.GetShadowCard("card");
		var mumak = scn.GetShadowCard("mumak");
		scn.MoveCardsToHand(chieftain);
		scn.MoveCardsToDiscard(mumak);

		scn.StartGame();
		// 4 set + 1 companion + 2 for the site make 7: the 5 he costs and 2 for roaming, a site 4 minion at site 2.
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(chieftain));
		scn.ShadowPlayCard(chieftain);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertInZone(Zone.DISCARD, mumak);
		// 11 in print
		assertEquals(11, scn.GetStrength(chieftain));

		// The pool is empty and the mount costs 2 - 2: it is offered, and played for nothing.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(chieftain));
		scn.ShadowAcceptOptionalTrigger();
		assertAttachedTo(mumak, chieftain);
		// 11 + 3 for the Mûmak
		assertEquals(14, scn.GetStrength(chieftain));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
