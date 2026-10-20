package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_196_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("coming", "1_196");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDiscardPileScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("coming", "1_196");

					// For the discard pile.  A choice from a pile is read by blueprint, so no card here shares
					// one with a card of the hand.
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6, site 4, no text
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc, cost 3, site 4; Archer is its whole text
					// Cave Troll of Moria: [moria] and no Orc.  Cost 10; to play, spot a [moria] Orc.
					put("troll", "1_165");
					// Orc Soldier: an Orc and not [moria] ([sauron]), cost 2, site 6; a skirmish ability only
					put("soldier", "1_271");

					// For the hand: two [moria] Orcs, each with a text on being played that is never reached
					put("runner", "1_178");     // Goblin Runner
					put("sneak", "1_181");      // Goblin Sneak
					put("scavengers", "1_179"); // Goblin Scavengers: a third [moria] Orc
					put("swarms", "1_183");     // Goblin Swarms: a [moria] condition, a card that is no minion
					put("domain", "1_175");     // Goblin Domain: another
					// Orc Taskmaster: a [sauron] Orc, a card of the hand that is not [moria]; its only text is an
					// ability of a minion in play
					put("taskmaster", "2_90");

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
	public void TheyAreComingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: They Are Coming
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * 	<b>Shadow:</b> Discard 3 cards from hand to play a [moria] Orc from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("coming");

		assertEquals("They Are Coming", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheOrcPlayedIsNotOneOfTheThreeCardsDiscardedToPayForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var coming = scn.GetShadowCard("coming");
		var troop = scn.GetShadowCard("troop");
		var marksman = scn.GetShadowCard("marksman");
		var troll = scn.GetShadowCard("troll");
		var soldier = scn.GetShadowCard("soldier");
		var runner = scn.GetShadowCard("runner");
		var sneak = scn.GetShadowCard("sneak");
		var swarms = scn.GetShadowCard("swarms");
		var domain = scn.GetShadowCard("domain");
		var taskmaster = scn.GetShadowCard("taskmaster");
		var commander = scn.GetShadowCard("commander");
		scn.MoveCardsToSupportArea(coming);
		scn.MoveCardsToDiscard(troop, marksman, troll, soldier);
		scn.MoveCardsToHand(runner, sneak, swarms, domain, taskmaster);

		scn.StartGame();
		scn.MoveMinionsToTable(commander);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for Ettenmoors: enough for any card of the discard pile, the Cave Troll
		// included (10 + 2 roaming)
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(coming));

		scn.ShadowUseCardAction(coming);
		// Any card of the hand pays, of whatever culture and type
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, sneak, swarms, domain, taskmaster));
		assertEquals(3, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(runner, sneak, swarms);
		assertInDiscard(runner, sneak, swarms);
		assertInHand(domain, taskmaster);
		// The two [moria] Orcs that lay in the discard pile before the cost was paid, and neither of the two
		// that lie there now because they paid it.  Nor the [moria] Troll, nor the [sauron] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, marksman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, sneak, troll, soldier));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCards(marksman);
		assertInZone(Zone.SHADOW_CHARACTERS, marksman);
		assertInDiscard(troop, troll, soldier, runner, sneak, swarms);
		assertInHand(domain, taskmaster);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheOnlyOrcsThatCouldBePlayedAreTheCardsThatWouldPayForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var coming = scn.GetShadowCard("coming");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var sneak = scn.GetShadowCard("sneak");
		var scavengers = scn.GetShadowCard("scavengers");
		scn.MoveCardsToSupportArea(coming);
		// Three [moria] Orcs to discard, and nothing in the discard pile
		scn.MoveCardsToHand(runner, sneak, scavengers);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The play is itself a cost of the ability, and the cards that would pay its other cost cannot be the
		// card played.
		assertEquals(13, scn.GetTwilight());
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(coming));

		// The same board with a [moria] Orc in the discard pile beforehand
		scn.MoveCardsToDiscard(troop);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(coming));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsOfferedOnlyWithThreeCardsInHandAndAMoriaOrcInYourOwnDiscardPileThatThePoolCanPayFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var coming = scn.GetShadowCard("coming");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var swarms = scn.GetShadowCard("swarms");
		var domain = scn.GetShadowCard("domain");
		// A [moria] Orc in the other player's discard pile.
		var freepsTroop = scn.GetFreepsCard("troop");
		scn.MoveCardsToSupportArea(coming);
		scn.MoveCardsToHand(swarms, domain, runner);
		scn.MoveCardsToDiscard(freepsTroop);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Three cards in hand and (13) in the pool, but the only [moria] Orc in a discard pile is not in yours.
		assertEquals(13, scn.GetTwilight());
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(coming));

		// A Goblin Patrol Troop in your own discard pile costs 6, and 2 more for roaming (a site 4 minion at site 2).
		scn.MoveCardsToDiscard(troop);
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(coming));

		scn.SetTwilight(8);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(coming));

		// Two cards in hand cannot pay the discard of 3.
		scn.MoveCardsToTopOfDeck(runner);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetShadowHandCount());
		assertFalse(scn.ShadowActionAvailable(coming));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.MoveCardsToHand(runner);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(coming));
		scn.ShadowUseCardAction(coming);

		// The whole hand pays, and the Troop is the one [moria] Orc there that did not pay: both are done unasked.
		assertEquals(0, scn.GetShadowHandCount());
		assertInDiscard(swarms, domain, runner, freepsTroop);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
