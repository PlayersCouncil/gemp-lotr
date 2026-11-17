package com.gempukku.lotro.cards.official.set12;

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

public class Card_12_038_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadow", "12_38");

					// [gollum] cards to spot, of each side and of two types.
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: a [gollum] minion; a regroup ability only
					put("stole", "6_46");       // They Stole It: a Shadow [gollum] support condition; a skirmish ability only
					put("smeagol", "6_45");     // Smeagol, Poor Creature: a Free Peoples [gollum] companion; a skirmish ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: a [moria] minion, no [gollum] card

					// Cards for the draw deck.
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, cost 2, site 4; text only with 2 wounds on the Ring-bearer
					put("hound2", "7_192");
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, cost 2, site 5; damage +1 only
					put("stole2", "6_46");      // a [gollum] card that is no minion
					put("cloak", "1_42");       // Elven Cloak: a Free Peoples possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FromDeepinShadowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: From Deep in Shadow
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Spot X [gollum] cards to reveal the top X cards of your draw deck. You may play a revealed minion. Its twilight cost is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shadow");

		assertEquals("From Deep in Shadow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingThreeGollumCardsRevealsThreeAndPlaysARevealedMinionAtTwilightCostMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadow = scn.GetShadowCard("shadow");
		var gollum = scn.GetShadowCard("gollum");
		var stole = scn.GetShadowCard("stole");
		var runner = scn.GetShadowCard("runner");
		var hound = scn.GetShadowCard("hound");
		var hound2 = scn.GetShadowCard("hound2");
		var savage = scn.GetShadowCard("savage");
		var stole2 = scn.GetShadowCard("stole2");
		var cloak = scn.GetShadowCard("cloak");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(stole);
		scn.MoveCardsToHand(shadow, hound2);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, runner);
		// Top first: two minions of other cultures and races, a [gollum] card that is no minion, a Free Peoples
		// card, and one more below them.
		scn.SetShadowDrawDeck(hound, savage, stole2, cloak);
		scn.SetTwilight(2);
		scn.FreepsPass();

		int freepsDeck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();
		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(2, scn.GetShadowHandCount());
		// 2 set + 2 companions + 2 for Ettenmoors
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(shadow));
		scn.ShadowPlayCard(shadow);

		// Gollum, They Stole It and the other player's Smeagol are [gollum] cards; the Goblin Runner is not.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(3), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("3");

		// The top 3 are shown to each player; the fourth is not.
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(3, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(hound, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(stole2, false));
		scn.DismissRevealedCards();

		// Either revealed minion may be played; the revealed card that is no minion may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hound, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(stole2, cloak));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(hound);

		// 2 printed + 2 roaming at site 2 - 2
		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		assertEquals(4, scn.GetTwilight());
		// What was not played stays on top of the deck, in its order; the other player's deck is untouched.
		assertEquals(3, scn.GetShadowDeckCount());
		assertSame(savage, scn.GetShadowTopOfDeck());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		// A reveal of the top cards is no search: no shuffle (Q-0013).
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertInDiscard(shadow);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The -2 was that play's alone: a second Morgul Hound from hand pays 2 + 2 roaming in full.
		scn.ShadowPlayCard(hound2);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingFewerThanThereAreRevealsThatManyAndTheMinionNeedNotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadow = scn.GetShadowCard("shadow");
		var gollum = scn.GetShadowCard("gollum");
		var stole = scn.GetShadowCard("stole");
		var hound = scn.GetShadowCard("hound");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(stole);
		scn.MoveCardsToHand(shadow);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SetShadowDrawDeck(hound, savage);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(2, scn.GetShadowDeckCount());
		// 2 set + 1 companion + 2 for Ettenmoors
		assertEquals(5, scn.GetTwilight());
		scn.ShadowPlayCard(shadow);

		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");

		// One card shown; the Savage below it is not.
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(hound, false));
		scn.DismissRevealedCards();

		// "You may": the revealed minion is offered and declined.
		// The Savage is a minion too, but it lies below the one card revealed.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();

		assertEquals(2, scn.GetShadowDeckCount());
		assertSame(hound, scn.GetShadowTopOfDeck());
		assertEquals(5, scn.GetTwilight());
		assertInDiscard(shadow);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsPlayableInTheShadowPhaseAndNotInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadow = scn.GetShadowCard("shadow");
		var gollum = scn.GetShadowCard("gollum");
		var hound = scn.GetShadowCard("hound");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(shadow);

		scn.StartGame();
		assertInHand(shadow);
		scn.MoveMinionsToTable(gollum);
		scn.SetShadowDrawDeck(hound, savage);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(shadow));
		scn.ShadowPass();

		// The next phase in which the Shadow player is asked for actions: the maneuver phase.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(shadow));
		assertEquals(2, scn.GetShadowDeckCount());
	}
}
