package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_197_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("threat", "1_197");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("scout", "1_191");      // Moria Scout: [moria] Orc, vitality 2; its "when you play" is not set off by a cheat
					put("troll", "1_165");      // Cave Troll of Moria: [moria] Troll (no Orc), vitality 4; its play text is not reached
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability used only on purpose

					put("deck1", "1_177");      // Goblin Patrol Troop, laid on the draw deck to be drawn
					put("deck2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThreatoftheUnknownStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Threat of the Unknown
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Shadow:</b> Exert your [moria] Orc to draw 2 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("threat");

		assertEquals("Threat of the Unknown", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAMoriaOrcOfTheShadowPlayersChoiceToDrawTwoCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threat = scn.GetShadowCard("threat");
		var troop = scn.GetShadowCard("troop");
		var scout = scn.GetShadowCard("scout");
		var troll = scn.GetShadowCard("troll");
		var soldier = scn.GetShadowCard("soldier");
		var deck1 = scn.GetShadowCard("deck1");
		var deck2 = scn.GetShadowCard("deck2");
		scn.MoveCardsToHand(threat);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, scout, troll, soldier);
		// deck1 on top, deck2 under it.
		scn.MoveCardsToTopOfDeck(deck2, deck1);
		assertEquals(deck1, scn.GetShadowTopOfDeck());
		assertInZone(Zone.DECK, deck1, deck2);
		assertInHand(threat);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(1, scn.GetShadowHandCount());
		int shadowDeck = scn.GetShadowDeckCount();
		int freepsHand = scn.GetFreepsHandCount();
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertTrue(scn.ShadowPlayAvailable(threat));

		scn.ShadowPlayCard(threat);
		// Two [moria] Orcs of different titles; not the [moria] Troll, nor the [sauron] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, scout));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troll, soldier));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(scout);

		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(soldier));
		// The two top cards are drawn by the Shadow player; the event is gone from his hand.
		assertInDiscard(threat);
		assertInHand(deck1, deck2);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 2, scn.GetShadowDeckCount());
		assertEquals(freepsHand, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotPlayableWithItsOnlyMoriaOrcExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threat = scn.GetShadowCard("threat");
		var scout = scn.GetShadowCard("scout");
		var troll = scn.GetShadowCard("troll");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(threat);

		scn.StartGame();
		scn.MoveMinionsToTable(scout, troll, soldier);
		// Vitality 2 with 1 wound: the only [moria] Orc is exhausted.  The [moria] Troll and the [sauron] Orc
		// beside it can each exert.
		scn.AddWoundsToChar(scout, 1);
		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertInHand(threat);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(threat));
		assertInHand(threat);
	}
}
