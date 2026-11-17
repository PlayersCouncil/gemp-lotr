package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_92_010_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("runner", "1_178");     // Goblin Runner: [moria] Orc minion, cost 1, site 4; when played it adds (2)
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion, cost 2, site 5; Damage +1 and nothing else
					put("troll", "1_165");      // Cave Troll of Moria, Scourge of the Black Pit: [moria] Troll minion, cost 10, site 4; to play, spot a [moria] Orc
					put("swarms", "1_183");     // Goblin Swarms: [moria] support-area condition, cost 1; a Shadow card that is no minion
					// Cards to discard from hand
					put("fodder1", "1_179");    // Goblin Scavengers: [moria] Orc minion, cost 3, site 4
					put("fodder2", "1_183");    // Goblin Swarms again
					put("fodder3", "1_191");    // Moria Scout: [moria] Orc minion, cost 2, site 4
					put("fodder4", "1_192");    // Pinned Down: [moria] support-area condition
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Free Peoples card, which a hand holds and discards like any other
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_10"
		);
	}

	protected VirtualTableScenario GetOpponentScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("runner", "1_178");     // Goblin Runner: [moria] Orc minion, cost 1, site 4; when played it adds (2)
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion, cost 2, site 5; Damage +1 and nothing else
					put("troll", "1_165");      // Cave Troll of Moria, Scourge of the Black Pit: [moria] Troll minion, cost 10, site 4; to play, spot a [moria] Orc
					put("swarms", "1_183");     // Goblin Swarms: [moria] support-area condition, cost 1; a Shadow card that is no minion
					// Cards to discard from hand
					put("fodder1", "1_179");    // Goblin Scavengers: [moria] Orc minion, cost 3, site 4
					put("fodder2", "1_183");    // Goblin Swarms again
					put("fodder3", "1_191");    // Moria Scout: [moria] Orc minion, cost 2, site 4
					put("fodder4", "1_192");    // Pinned Down: [moria] support-area condition
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Free Peoples card, which a hand holds and discards like any other
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_10", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_10
		 * Type: MetaSite
		 * Intensity: -2
		 * Game Text: Shadow: Discard 3 cards from hand to play a minion from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_10", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-2, card.getBlueprint().getIntensity());
	}

	@Test
	public void PlaysMinionFromDiscardAfterDiscarding3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mod = scn.GetShadowCard("mod");
		var runner = scn.GetShadowCard("runner");
		var fodder1 = scn.GetShadowCard("fodder1");
		var fodder2 = scn.GetShadowCard("fodder2");
		var fodder3 = scn.GetShadowCard("fodder3");
		var fodder4 = scn.GetShadowCard("fodder4");

		scn.MoveCardsToHand(fodder1, fodder2, fodder3, fodder4);
		scn.MoveCardsToDiscard(runner);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(4, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// Discard 3 cards
		scn.ShadowChooseCards(fodder1, fodder2, fodder3);
		assertInZone(Zone.DISCARD, fodder1);
		assertInZone(Zone.DISCARD, fodder2);
		assertInZone(Zone.DISCARD, fodder3);
		assertInZone(Zone.HAND, fodder4);
		assertEquals(1, scn.GetShadowHandCount());

		// The runner is the only minion in the discard pile that was not just discarded there: no choice is asked.
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OffersEveryMinionInYourDiscardPileAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mod = scn.GetShadowCard("mod");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var swarms = scn.GetShadowCard("swarms");
		var scavengers = scn.GetShadowCard("fodder1");
		var swarmsInHand = scn.GetShadowCard("fodder2");
		var pinned = scn.GetShadowCard("fodder4");
		var gimli = scn.GetShadowCard("gimli");
		// The other player's Moria Scout lies in his own discard pile; the Shadow player's stays in his deck.
		var theirScout = scn.GetFreepsCard("fodder3");

		scn.MoveCardsToHand(gimli, pinned, scavengers, swarmsInHand);
		scn.MoveCardsToDiscard(runner, savage, swarms);
		scn.MoveCardsToDiscard(theirScout);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for site 2
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// Any 3 cards of the hand, whatever they are: a Free Peoples card, a condition, a minion.
		assertEquals(3, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, pinned, scavengers, swarmsInHand));
		scn.ShadowChooseCards(gimli, pinned, scavengers);
		assertInZone(Zone.DISCARD, gimli);
		assertInZone(Zone.DISCARD, pinned);
		assertInZone(Zone.DISCARD, scavengers);
		assertInZone(Zone.HAND, swarmsInHand);

		// The two minions that were in the discard pile, of two cultures and races, and nothing else:
		// not the condition there, not the cards just discarded (a minion among them), and not the
		// minion in the other player's discard pile.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(swarms, scavengers, pinned, gimli, theirScout));
		assertEquals(2, scn.ShadowGetCardChoiceCount());

		scn.ShadowChooseCards(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.DISCARD, runner);
		assertInZone(Zone.DISCARD, theirScout);
		// 13 - 2 to play - 2 roaming (site number 5 at site 2)
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBeUsedWithOnlyTwoCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mod = scn.GetShadowCard("mod");
		var runner = scn.GetShadowCard("runner");
		var fodder2 = scn.GetShadowCard("fodder2");
		var fodder4 = scn.GetShadowCard("fodder4");

		scn.MoveCardsToHand(fodder2, fodder4);
		scn.MoveCardsToDiscard(runner);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A minion to play and the twilight to play it, and one card too few to discard.
		assertEquals(2, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.ShadowActionAvailable(mod));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotChooseDiscardedCardsAsTarget() throws DecisionResultInvalidException, CardNotFoundException {
		// Per ruling on They Are Coming: the cards you discarded cannot be the minion you play.
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var fodder1 = scn.GetShadowCard("fodder1");
		var fodder2 = scn.GetShadowCard("fodder2");
		var fodder3 = scn.GetShadowCard("fodder3");
		var fodder4 = scn.GetShadowCard("fodder4");

		// Put runner in hand as one of the 3 to discard, plus 2 others
		// Runner will end up in discard but should NOT be selectable
		scn.MoveCardsToHand(runner, fodder2, fodder3, fodder4);
		// fodder1 is already a minion in discard as the intended target
		scn.MoveCardsToDiscard(fodder1);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowUseCardAction(scn.GetShadowCard("mod"));

		// Discard 3 cards from hand (including runner)
		scn.ShadowChooseCards(runner, fodder2, fodder3);

		// Now selecting target: runner is in discard but was one of the discarded cards,
		// so only fodder1 should be available.  Since there's only one valid choice,
		// it should auto-select fodder1.
		assertInZone(Zone.SHADOW_CHARACTERS, fodder1);
		// The runner and the Moria Scout (fodder3), minions both, lie where they were discarded.
		assertInZone(Zone.DISCARD, runner);
		assertInZone(Zone.DISCARD, fodder3);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotPlayMinionYouCantAfford() throws DecisionResultInvalidException, CardNotFoundException {
		// Cave Troll costs 10 twilight — if pool is insufficient, it shouldn't be playable.
		var scn = GetScenario();

		var mod = scn.GetShadowCard("mod");
		var runner = scn.GetShadowCard("runner");
		var troll = scn.GetShadowCard("troll");
		var fodder1 = scn.GetShadowCard("fodder1");
		var fodder2 = scn.GetShadowCard("fodder2");
		var fodder3 = scn.GetShadowCard("fodder3");

		scn.MoveCardsToHand(fodder1, fodder2, fodder3);
		scn.MoveCardsToDiscard(troll);

		scn.StartGame();
		// The [moria] Orc the troll must spot is in play, so the pool is all that stands in its way.
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(8);
		scn.FreepsPass();

		// 8 set + 1 companion + 2 for site 2: one short of 10 to play + 2 roaming (site number 4 at site 2)
		assertEquals(11, scn.GetTwilight());
		assertEquals(3, scn.GetShadowHandCount());
		// The action shouldn't be available at all since the only minion in discard
		// can't be afforded (and you can't take the action if the effect can't resolve).
		assertFalse(scn.ShadowActionAvailable(mod));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OpponentCannotUseAbility() throws DecisionResultInvalidException, CardNotFoundException {
		// "Your discard pile": the modifier is the Free Peoples player's, so the Shadow player is not offered it.

		var scn = GetOpponentScenario();
		var mod = scn.GetFreepsCard("mod");

		scn.MoveCardsToShadowDiscard("runner");
		scn.MoveCardsToShadowHand("fodder1", "fodder2", "fodder3");

		scn.StartGame();
		scn.SetTwilight(10);

		scn.FreepsPass();
		// He has the three cards to discard, a minion in his discard pile and the twilight to play it.
		assertEquals(3, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, scn.GetShadowCard("runner"));
		assertFalse(scn.ShadowActionAvailable(mod));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
