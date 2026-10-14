package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_050_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("slimy", "15_50");
					put("smeagol", "15_49");    // Sméagol, Wretched and Hungry: [gollum] companion
					put("shiresmeagol", "103_116"); // Smeagol, Once Like Us: [shire] companion; his ability is an assignment one
					put("herbs", "15_44");      // Herbs and Stewed Rabbit: [gollum] support condition that only reads Sméagol
					// In the draw deck:
					put("nicefish", "15_46");   // Nice Fish: [gollum] support condition
					put("doom", "12_36");       // With Doom We Come: [gandalf] support condition
					put("called", "15_39");     // Called to Mordor: [gollum] condition, bearer must be Sméagol
					put("boat", "13_48");       // Fishing Boat: [gollum] possession, bearer must be Sméagol
					put("notthistime", "15_47"); // Not This Time!: [gollum] Shadow condition
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SomethingSlimyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Something Slimy
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Spot Sméagol to play a condition from your draw deck. If that condition was a [gollum] condition, you may make an opponent place a random card from his or her hand beneath his or her draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("slimy");

		assertEquals("Something Slimy", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithoutSmeagolAndCanOnceHeIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slimy = scn.GetFreepsCard("slimy");
		var smeagol = scn.GetFreepsCard("smeagol");
		var herbs = scn.GetFreepsCard("herbs");
		scn.MoveCardsToHand(slimy);
		// A [gollum] card that is no Sméagol, and the Ring-bound Ring-bearer.
		scn.MoveCardsToSupportArea(herbs);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(slimy));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveCompanionsToTable(smeagol);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(slimy));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShireSmeagolLetsItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slimy = scn.GetFreepsCard("slimy");
		var shiresmeagol = scn.GetFreepsCard("shiresmeagol");
		var doom = scn.GetFreepsCard("doom");
		scn.MoveCardsToHand(slimy);
		scn.MoveCompanionsToTable(shiresmeagol);

		scn.StartGame();

		assertInZone(Zone.DECK, doom);
		assertTrue(scn.FreepsPlayAvailable(slimy));
		scn.FreepsPlayCard(slimy);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(doom);

		assertInZone(Zone.SUPPORT, doom);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysAConditionFromTheDeckAndAsksNothingWhenItIsNotGollum() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slimy = scn.GetFreepsCard("slimy");
		var smeagol = scn.GetFreepsCard("smeagol");
		var herbs = scn.GetFreepsCard("herbs");
		var nicefish = scn.GetFreepsCard("nicefish");
		var doom = scn.GetFreepsCard("doom");
		var called = scn.GetFreepsCard("called");
		var boat = scn.GetFreepsCard("boat");
		var notthistime = scn.GetFreepsCard("notthistime");
		scn.MoveCardsToHand(slimy);
		scn.MoveCompanionsToTable(smeagol);
		// A [gollum] condition already in play is not "that condition".
		scn.MoveCardsToSupportArea(herbs);

		var shadowHerbs = scn.GetShadowCard("herbs");
		var shadowDoom = scn.GetShadowCard("doom");

		scn.StartGame();
		scn.MoveCardsToHand(shadowHerbs, shadowDoom);

		assertInZone(Zone.DECK, nicefish, doom, called, boat, notthistime);
		assertEquals(2, scn.GetShadowHandCount());
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsPlayCard(slimy);
		scn.FreepsDismissRevealedCards();

		// Conditions of two cultures, of the support area and borne; not a possession, nor a Shadow
		// condition, which the Free Peoples player cannot play.
		assertTrue(scn.FreepsHasCardChoiceAvailable(nicefish, doom, called));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boat, notthistime));
		scn.FreepsChooseCardBPFromSelection(doom);

		assertInZone(Zone.SUPPORT, doom, herbs);
		assertFalse(scn.FreepsDecisionAvailable("make an opponent"));
		assertEquals(2, scn.GetShadowHandCount());
		assertInHand(shadowHerbs, shadowDoom);
		assertInDiscard(slimy);
		// The deck is shuffled as the condition leaves it, and again as the look ends (CR 4.2 "draw deck").
		assertEquals(freepsShuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGollumConditionPlayedLetsHimSendARandomShadowCardBeneathTheShadowDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slimy = scn.GetFreepsCard("slimy");
		var smeagol = scn.GetFreepsCard("smeagol");
		var nicefish = scn.GetFreepsCard("nicefish");
		scn.MoveCardsToHand(slimy);
		scn.MoveCompanionsToTable(smeagol);

		var shadowHand = new PhysicalCardImpl[]{
				scn.GetShadowCard("herbs"), scn.GetShadowCard("doom"), scn.GetShadowCard("called"),
				scn.GetShadowCard("boat"), scn.GetShadowCard("notthistime")
		};

		scn.StartGame();
		scn.MoveCardsToHand(shadowHand);

		assertEquals(5, scn.GetShadowHandCount());
		int shadowDeck = scn.GetShadowDeckCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsPlayCard(slimy);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(nicefish);
		assertInZone(Zone.SUPPORT, nicefish);

		assertTrue(scn.FreepsDecisionAvailable("make an opponent"));
		scn.FreepsChooseYes();

		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(shadowDeck + 1, scn.GetShadowDeckCount());
		// Whichever card went is the one at the bottom of his deck; the others are still in his hand.
		var bottom = scn.GetShadowBottomOfDeck();
		int gone = 0;
		for (var card : shadowHand) {
			if (card == bottom) {
				gone++;
				assertInZone(Zone.DECK, card);
			}
			else {
				assertInHand(card);
			}
		}
		assertEquals(1, gone);
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ABorneGollumConditionGoesOnSmeagolAndDecliningLeavesTheShadowHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slimy = scn.GetFreepsCard("slimy");
		var smeagol = scn.GetFreepsCard("smeagol");
		var called = scn.GetFreepsCard("called");
		scn.MoveCardsToHand(slimy);
		scn.MoveCompanionsToTable(smeagol);

		var shadowHerbs = scn.GetShadowCard("herbs");
		var shadowDoom = scn.GetShadowCard("doom");

		scn.StartGame();
		scn.MoveCardsToHand(shadowHerbs, shadowDoom);
		assertEquals(2, scn.GetShadowHandCount());
		int shadowDeck = scn.GetShadowDeckCount();

		scn.FreepsPlayCard(slimy);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(called);
		assertAttachedTo(called, smeagol);

		assertTrue(scn.FreepsDecisionAvailable("make an opponent"));
		scn.FreepsChooseNo();

		assertEquals(2, scn.GetShadowHandCount());
		assertInHand(shadowHerbs, shadowDoom);
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTheShadowHandEmptyNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slimy = scn.GetFreepsCard("slimy");
		var smeagol = scn.GetFreepsCard("smeagol");
		var nicefish = scn.GetFreepsCard("nicefish");
		scn.MoveCardsToHand(slimy);
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();

		assertEquals(0, scn.GetShadowHandCount());
		scn.FreepsPlayCard(slimy);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(nicefish);
		assertInZone(Zone.SUPPORT, nicefish);

		assertFalse(scn.FreepsDecisionAvailable("make an opponent"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
