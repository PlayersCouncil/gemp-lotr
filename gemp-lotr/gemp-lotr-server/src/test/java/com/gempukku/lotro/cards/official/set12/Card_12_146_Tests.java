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

public class Card_12_146_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("device", "12_146");

					// Hounding Uruk: an [uruk-hai] Uruk-hai minion; strength +3 only while a companion is exhausted.
					put("hounding", "11_188");
					put("hounding2", "11_188");
					// Saruman, Curunir: an [uruk-hai] minion who is a Wizard and no Uruk-hai; a trigger on a hunter's
					// win and a skirmish ability, neither reached.
					put("saruman", "17_115");
					// Cirith Ungol Sentry: a [sauron] Uruk-hai minion, whose whole text is Damage +1.
					put("sentry", "10_84");
					// Worthy of Mordor: an [uruk-hai] condition (support area), with a trigger at the start of the
					// regroup phase, never reached.
					put("worthy", "12_160");
					// Broken in Defeat: an [uruk-hai] skirmish event, only ever held in hand here.
					put("broken", "12_139");
					// Aragorn, Ranger of the North: a Free Peoples companion, only ever held in the Shadow hand here.
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrangeDeviceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Strange Device
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Spot an [uruk-hai] minion to draw 3 cards. Then discard 2 cards from hand or discard an [uruk-hai] minion from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("device");

		assertEquals("Strange Device", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DrawsThreeThenDiscardsAnyTwoCardsFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var device = scn.GetShadowCard("device");
		var hounding = scn.GetShadowCard("hounding");
		var hounding2 = scn.GetShadowCard("hounding2");
		var sentry = scn.GetShadowCard("sentry");
		var worthy = scn.GetShadowCard("worthy");
		var broken = scn.GetShadowCard("broken");
		var aragorn = scn.GetShadowCard("aragorn");

		scn.StartGame();
		scn.SetShadowHand(device, aragorn, worthy);
		scn.MoveCardsToTopOfDeck(sentry, broken, hounding2);
		scn.MoveMinionsToTable(hounding);
		assertInHand(device, aragorn, worthy);
		assertInZone(Zone.DECK, sentry, broken, hounding2);

		scn.FreepsPass();
		int deck = scn.GetShadowDeckCount();
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(device));

		scn.ShadowPlayCard(device);
		// The three drawn come in ahead of the discard.
		assertEquals(deck - 3, scn.GetShadowDeckCount());
		assertInHand(sentry, broken, hounding2);
		// Both can be done: asked which.
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowChoiceAvailable("2 cards"));
		assertTrue(scn.ShadowChoiceAvailable("minion"));
		scn.ShadowChooseOption("2 cards");

		// Any two cards: a companion, a condition, two minions and an event are all offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, worthy, sentry, broken, hounding2));
		assertEquals(2, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(worthy, hounding2);

		assertInDiscard(device, worthy, hounding2);
		assertInHand(aragorn, sentry, broken);
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOtherChoiceDiscardsOneUrukHaiMinionFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var device = scn.GetShadowCard("device");
		var hounding = scn.GetShadowCard("hounding");
		var hounding2 = scn.GetShadowCard("hounding2");
		var saruman = scn.GetShadowCard("saruman");
		var sentry = scn.GetShadowCard("sentry");
		var worthy = scn.GetShadowCard("worthy");
		var broken = scn.GetShadowCard("broken");
		var aragorn = scn.GetShadowCard("aragorn");

		scn.StartGame();
		scn.SetShadowHand(device, saruman, sentry, worthy);
		scn.MoveCardsToTopOfDeck(aragorn, broken, hounding2);
		scn.MoveMinionsToTable(hounding);
		assertInHand(device, saruman, sentry, worthy);
		assertInZone(Zone.DECK, aragorn, broken, hounding2);

		scn.FreepsPass();
		scn.ShadowPlayCard(device);
		assertEquals(6, scn.GetShadowHandCount());
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("minion");

		// [uruk-hai] minions of two races; not a [sauron] Uruk-hai, nor an [uruk-hai] card that is no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hounding2, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, worthy, broken, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(saruman);

		assertInDiscard(device, saruman);
		assertInHand(hounding2, sentry, worthy, broken, aragorn);
		assertEquals(5, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoUrukHaiMinionInHandTwoCardsAreDiscardedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var device = scn.GetShadowCard("device");
		var hounding = scn.GetShadowCard("hounding");
		var sentry = scn.GetShadowCard("sentry");
		var worthy = scn.GetShadowCard("worthy");
		var broken = scn.GetShadowCard("broken");
		var aragorn = scn.GetShadowCard("aragorn");

		scn.StartGame();
		scn.SetShadowHand(device, worthy);
		scn.MoveCardsToTopOfDeck(aragorn, broken, sentry);
		scn.MoveMinionsToTable(hounding);
		assertInHand(device, worthy);
		assertInZone(Zone.DECK, aragorn, broken, sentry);

		scn.FreepsPass();
		scn.ShadowPlayCard(device);
		// No [uruk-hai] minion in hand (the [sauron] Sentry is none): the choice of two cards comes at once.
		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertEquals(4, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCards(worthy, sentry);

		assertInDiscard(device, worthy, sentry);
		assertInHand(aragorn, broken);
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneCardLeftToDrawTheUrukHaiMinionIsDiscardedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var device = scn.GetShadowCard("device");
		var hounding = scn.GetShadowCard("hounding");
		var hounding2 = scn.GetShadowCard("hounding2");

		scn.StartGame();
		scn.SetShadowHand(device);
		// Every other card of the Shadow deck goes to his discard pile: the deck holds the one Uruk-hai.
		scn.MoveCardsToShadowDiscard("saruman", "sentry", "worthy", "broken", "aragorn");
		scn.MoveMinionsToTable(hounding);
		assertInHand(device);
		assertInZone(Zone.DECK, hounding2);
		assertEquals(1, scn.GetShadowDeckCount());

		scn.FreepsPass();
		scn.ShadowPlayCard(device);
		// One card drawn: two cannot be discarded, so the minion is what is left to do.
		assertEquals(0, scn.GetShadowDeckCount());
		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));

		assertInDiscard(device, hounding2);
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAnUrukHaiMinionAndSarumanIsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var device = scn.GetShadowCard("device");
		var saruman = scn.GetShadowCard("saruman");
		var sentry = scn.GetShadowCard("sentry");
		var worthy = scn.GetShadowCard("worthy");
		var broken = scn.GetShadowCard("broken");
		var aragorn = scn.GetShadowCard("aragorn");
		var hounding2 = scn.GetShadowCard("hounding2");

		scn.StartGame();
		scn.SetShadowHand(device);
		scn.MoveCardsToTopOfDeck(hounding2, broken, aragorn);
		// A [sauron] Uruk-hai minion, and an [uruk-hai] card that is no minion.
		scn.MoveMinionsToTable(sentry);
		scn.MoveCardsToSupportArea(worthy);
		assertInZone(Zone.SHADOW_CHARACTERS, sentry);
		assertInZone(Zone.SUPPORT, worthy);
		assertInZone(Zone.DECK, hounding2, broken, aragorn);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(device));

		// Saruman, an [uruk-hai] minion who is no Uruk-hai.
		scn.MoveMinionsToTable(saruman);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(device));

		scn.ShadowPlayCard(device);
		assertInHand(hounding2, broken, aragorn);
		scn.ShadowChooseOption("2 cards");
		scn.ShadowChooseCards(broken, aragorn);

		assertInDiscard(device, broken, aragorn);
		assertInHand(hounding2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
