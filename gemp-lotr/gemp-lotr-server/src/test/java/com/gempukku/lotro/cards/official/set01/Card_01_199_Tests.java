package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_199_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("keyward", "1_199");
					put("troll", "1_165");    // Cave Troll of Moria, Scourge of the Black Pit: [moria], cost 10, site 4; to play, spot a [moria] Orc
					put("menace", "12_85");   // Cave Troll of Moria, Savage Menace: the [orc] Cave Troll of Moria, cost 12
					put("deep", "19_23");     // Troll of the Deep, Cave Troll: a Troll called a Cave Troll, of another title, cost 6
					put("troop", "1_177");    // Goblin Patrol Troop: another [moria] minion, cost 6, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TrollsKeywardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Troll's Keyward
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: Cave Troll of Moria's twilight cost is -2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("keyward");

		assertEquals("Troll's Keyward", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void OnceTheKeywardIsInPlayEachCaveTrollOfMoriaCostsTwoLessAndNoOtherCardDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var keyward = scn.GetShadowCard("keyward");
		var troll = scn.GetShadowCard("troll");
		var menace = scn.GetShadowCard("menace");
		var deep = scn.GetShadowCard("deep");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SetShadowHand(keyward, troll, menace, deep, troop);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Ettenmoors, site 2, which is not underground.  The Keyward in hand changes nothing.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(10, scn.GetTwilightCost(troll));
		assertEquals(12, scn.GetTwilightCost(menace));
		assertEquals(6, scn.GetTwilightCost(deep));
		assertEquals(6, scn.GetTwilightCost(troop));

		scn.ShadowPlayCard(keyward);
		assertInPlay(keyward);

		// Both cards titled Cave Troll of Moria, of [moria] and of [orc]; not the Troll of another title, nor another
		// [moria] minion.
		assertEquals(8, scn.GetTwilightCost(troll));
		assertEquals(10, scn.GetTwilightCost(menace));
		assertEquals(6, scn.GetTwilightCost(deep));
		assertEquals(6, scn.GetTwilightCost(troop));

		// 10 - 2 + 2 roaming: one short of it does not play him (the Keyward is the [moria] Orc he must spot).
		scn.SetTwilight(9);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(troll));
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(troll));

		scn.ShadowPlayCard(troll);
		assertInPlay(troll);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
