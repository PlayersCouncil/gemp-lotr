package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_059_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("campfire", "31_59");
					// For the hand: a [moria] Orc and a [moria] condition
					put("runner", "1_178");     // Goblin Runner: [moria] Orc; a text on being played, never reached
					put("swarms", "1_183");     // Goblin Swarms: [moria] condition
					// For the discard pile.  A choice from a pile is read by blueprint, so no card here shares
					// one with a card of the hand.
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6, site 4, no text
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc, cost 3, site 4; Archer is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TrollCampfireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Troll Campfire
		 * Unique: True
		 * Side: Shadow
		 * Culture: Troll
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a troll.  <b>Shadow:</b> Discard 2 cards from hand to play a minion from your discard pile.  (No sheet has the Hobbit sets and the definition carries no game text: these
		 * 	are the definition's own fields.  It has no side line; the game takes Shadow from the culture.)
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("campfire");

		assertEquals("Troll Campfire", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.TROLL, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// From the definition, there being no text to go by: "Shadow: Discard 2 cards from hand to play a minion from
	// your discard pile."
	@Test
	public void TheMinionPlayedIsNotOneOfTheCardsDiscardedToPayForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var campfire = scn.GetShadowCard("campfire");
		var runner = scn.GetShadowCard("runner");
		var swarms = scn.GetShadowCard("swarms");
		var troop = scn.GetShadowCard("troop");
		var marksman = scn.GetShadowCard("marksman");

		scn.MoveCardsToSupportArea(campfire);
		scn.MoveCardsToHand(runner, swarms);
		scn.MoveCardsToDiscard(troop, marksman);

		scn.StartGame();
		assertInHand(runner, swarms);
		assertInDiscard(troop, marksman);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertInHand(runner, swarms);
		assertInDiscard(troop, marksman);
		// 20 set + 1 companion + 2 for Ettenmoors
		assertEquals(23, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(campfire));
		scn.ShadowUseCardAction(campfire);
		// 2 cards in hand pay the discard of 2 without a choice
		assertEquals(0, scn.GetShadowHandCount());
		assertInDiscard(runner, swarms);

		// The two minions that lay in the discard pile before the cost was paid, and not the Goblin Runner that lies there now
		// because it paid for it
		// (CR 4.2 "effect", the They Are Coming example; Q-0320).
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, marksman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(marksman);

		assertInZone(Zone.SHADOW_CHARACTERS, marksman);
		// 3, and 2 for roaming (a site 4 minion at site 2)
		assertEquals(18, scn.GetTwilight());
		assertInDiscard(troop, runner, swarms);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
