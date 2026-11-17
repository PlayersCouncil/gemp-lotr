package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_187_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("host", "1_187");

					// For the discard pile.  A choice from a pile is read by blueprint, so each is of its own.
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6, site 4, no text
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc, cost 3, site 4; Archer is its whole text
					// Cave Troll of Moria: [moria] and no Orc.  Cost 10, site 4; to play, spot a [moria] Orc.
					put("troll", "1_165");
					// Orc Soldier: an Orc and not [moria] ([sauron]), cost 2, site 6; a skirmish ability only
					put("soldier", "1_271");

					// Guard Commander: a [moria] Orc in play, so that the Cave Troll could be played if it were
					// offered.  A skirmish ability only.
					put("commander", "1_186");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HostofThousandsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Host of Thousands
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Shadow:</b> Play a [moria] Orc from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("host");

		assertEquals("Host of Thousands", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysTheMoriaOrcChosenFromYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var troop = scn.GetShadowCard("troop");
		var marksman = scn.GetShadowCard("marksman");
		var troll = scn.GetShadowCard("troll");
		var soldier = scn.GetShadowCard("soldier");
		var commander = scn.GetShadowCard("commander");
		scn.MoveCardsToHand(host);
		scn.MoveCardsToDiscard(troop, marksman, troll, soldier);

		scn.StartGame();
		scn.MoveMinionsToTable(commander);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for Ettenmoors: enough for any card of the discard pile, the Cave Troll
		// included (10 + 2 roaming)
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(host));

		scn.ShadowPlayCard(host);
		// The two [moria] Orcs, and neither the [moria] Troll nor the [sauron] Orc.  One is to be played.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, marksman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troll, soldier));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCards(marksman);
		assertInZone(Zone.SHADOW_CHARACTERS, marksman);
		assertInDiscard(host, troop, troll, soldier);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsPlayableOnlyWithAMoriaOrcInYourOwnDiscardPileThatThePoolCanPayFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		var soldier = scn.GetShadowCard("soldier");
		var commander = scn.GetShadowCard("commander");
		// A [moria] Orc in the other player's discard pile.
		var freepsTroop = scn.GetFreepsCard("troop");
		scn.MoveCardsToHand(host);
		scn.MoveCardsToDiscard(troll, soldier, freepsTroop);

		scn.StartGame();
		scn.MoveMinionsToTable(commander);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// (13) in the pool, and in your discard pile a [moria] card that is no Orc and an Orc that is not [moria],
		// each of which the pool could pay for; the only [moria] Orc in a discard pile is not in yours.
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(host));

		// A Goblin Patrol Troop in your own discard pile costs 6, and 2 more for roaming (a site 4 minion at site 2).
		// Playing it is the event's cost.
		scn.MoveCardsToDiscard(troop);
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(host));

		scn.SetTwilight(8);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(host));

		scn.ShadowPlayCard(host);

		// The Troop is the one [moria] Orc there: it is played with nothing asked.
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInDiscard(host, troll, soldier, freepsTroop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
