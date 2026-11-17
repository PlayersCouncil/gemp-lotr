package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_177_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troop", "51_177");
					put("troop2", "51_177");
					put("keyward", "1_199");    // Troll's Keyward: unique [moria] Orc; lowers Cave Troll of Moria's cost
					put("chieftain", "1_163");  // Ancient Chieftain: unique [moria] Orc; strength +1 for each other [moria] Orc
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, not unique; its (2) only when played
					put("warden", "1_259");     // Morgul Warden: unique [sauron] Orc; strength +1 for each other [sauron] Orc
					put("troll", "1_165");      // Cave Troll of Moria: unique [moria] Troll; damage +1, fierce
					// Goblin Marksman: left in the draw deck, to be drawn
					put("marksman1", "1_176");
					put("marksman2", "1_176");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinPatrolTroopStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Goblin Patrol Troop
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: Shadow: Exert this minion or a unique [moria] Orc to draw a card.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("troop");

		assertEquals("Goblin Patrol Troop", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityExertsThisMinionOrAUniqueMoriaOrcToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var keyward = scn.GetShadowCard("keyward");
		var chieftain = scn.GetShadowCard("chieftain");
		var runner = scn.GetShadowCard("runner");
		var warden = scn.GetShadowCard("warden");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCardsToHand(troop);

		scn.StartGame();
		scn.MoveMinionsToTable(troop2, keyward, chieftain, runner, warden, troll);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for the site
		assertEquals(13, scn.GetTwilight());
		scn.ShadowPlayCard(troop);
		// 6 to play + 2 roaming
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, troop);

		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		assertTrue(scn.ShadowActionAvailable(troop));
		scn.ShadowUseCardAction(troop);

		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, keyward, chieftain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop2, runner, warden, troll));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(keyward);

		assertEquals(1, scn.GetWoundsOn(keyward));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());

		// Again, exerting this minion this time.
		assertTrue(scn.ShadowActionAvailable(troop));
		scn.ShadowUseCardAction(troop);
		scn.ShadowChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertEquals(1, scn.GetWoundsOn(keyward));
		assertEquals(hand + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AbilityIsNotOfferedUntilThisMinionOrAUniqueMoriaOrcCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var keyward = scn.GetShadowCard("keyward");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, keyward, runner);
		// Both exhausted (vitality 3 with 2 wounds); the [moria] Orc who is not unique is fresh.
		scn.AddWoundsToChar(troop, 2);
		scn.AddWoundsToChar(keyward, 2);
		scn.FreepsPass();

		assertFalse(scn.ShadowActionAvailable(troop));

		// With Troll's Keyward able to exert again, the ability is offered and it is Troll's Keyward that pays.
		scn.RemoveWoundsFromChar(keyward, 1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(troop));

		int hand = scn.GetShadowHandCount();
		scn.ShadowUseCardAction(troop);
		assertEquals(2, scn.GetWoundsOn(keyward));
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
