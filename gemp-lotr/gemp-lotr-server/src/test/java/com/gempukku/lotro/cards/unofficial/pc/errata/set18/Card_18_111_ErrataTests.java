package com.gempukku.lotro.cards.unofficial.pc.errata.set18;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_111_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("robin", "68_111");
					// Radagast, Tender of Beasts: another follower with Aid (2); its text needs 2 [gandalf] companions
					put("radagast", "13_38");

					put("sam", "1_311");          // Sam, Son of Hamfast: a [shire] Hobbit
					put("smeagol", "103_116");    // Smeagol, Once Like Us: a [shire] companion who has no race
					put("pippin", "103_31");      // Pippin, Trollslayer: a Hobbit who is [gondor]
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] Man

					// Cards for the hand. None does anything while it is held.
					put("sword", "1_299");        // Hobbit Sword
					put("toby", "1_305");         // Old Toby
					put("promise", "2_112");      // A Promise
					put("cloak", "1_42");         // Elven Cloak
					put("armor", "1_8");          // Dwarven Armor

					// Cards laid on top of the draw deck, to be drawn.
					put("tale", "1_317");         // There and Back Again
					put("coat", "13_153");        // Mithril-coat
					put("pony", "13_143");        // Bill the Pony

					put("runner", "1_178");       // Goblin Runner: a minion on the table, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RobinSmallburrowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Robin Smallburrow, Shirriff Cock-Robin
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Follower
		 * Resistance: +1
		 * Game Text: <b>Aid</b> - (2). <helper>(At the start of the maneuver phase, you may add (2) to transfer this to a companion.)</helper>
		 * 	Each time you attach this follower to a [shire] companion, you may discard up to 3 cards from hand to draw an equal amount.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("robin");

		assertEquals("Robin Smallburrow", card.getBlueprint().getTitle());
		assertEquals("Shirriff Cock-Robin", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(0, card.getBlueprint().getStrength());
		assertEquals(0, card.getBlueprint().getVitality());
		assertEquals(1, card.getBlueprint().getResistance());
		// The cost of his Aid is not a field of the card: the tests that transfer him read the (2) as it is paid.
	}

	@Test
	public void PlayedFromHandForTwoToTheSupportAreaWithNothingOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var robin = scn.GetFreepsCard("robin");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(robin, scn.GetFreepsCard("sword"), scn.GetFreepsCard("toby"));

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(robin));

		scn.FreepsPlayCard(robin);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, robin);
		assertNotAttachedTo(robin, frodo);
		assertNotAttachedTo(robin, sam);

		// He was played, not attached to anyone: with two [shire] companions in play and cards in hand, no offer.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(robin));
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AttachedToSamForTwoHeLetsTwoCardsOfFourBeDiscardedToDrawTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var robin = scn.GetFreepsCard("robin");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var toby = scn.GetFreepsCard("toby");
		var promise = scn.GetFreepsCard("promise");
		var cloak = scn.GetFreepsCard("cloak");
		var tale = scn.GetFreepsCard("tale");
		var coat = scn.GetFreepsCard("coat");
		var pony = scn.GetFreepsCard("pony");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToSupportArea(robin);
		scn.MoveCardsToHand(sword, toby, promise, cloak);

		scn.StartGame();
		// The Tale is the top card, the Mithril-coat next, the Pony third.
		scn.MoveCardsToTopOfDeck(pony, coat, tale);
		scn.MoveMinionsToTable("runner");

		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		assertInZone(Zone.SUPPORT, robin);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(robin, "Aid"));

		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, aragorn));
		scn.FreepsChooseCard(sam);
		// Aid - (2).
		assertEquals(pool + 2, scn.GetTwilight());
		assertAttachedTo(robin, sam);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(robin, "Optional trigger"));
		int deck = scn.GetFreepsDeckCount();
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.FreepsAcceptOptionalTrigger();
		// The cards in hand are what may be discarded, not one in the draw deck.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, toby, promise, cloak));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(tale));

		scn.FreepsChooseCards(sword, promise);
		assertInZone(Zone.DISCARD, sword, promise);
		assertEquals(2, scn.GetFreepsDiscardCount());
		// Two discarded, two drawn: the two top cards of the deck, and not the third.
		assertInZone(Zone.HAND, toby, cloak, tale, coat);
		assertInZone(Zone.DECK, pony);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertEquals(pool + 2, scn.GetTwilight());
		assertAttachedTo(robin, sam);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AttachedToPippinAHobbitWhoIsNotOfTheShireCultureNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var robin = scn.GetFreepsCard("robin");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var sword = scn.GetFreepsCard("sword");
		var toby = scn.GetFreepsCard("toby");
		var promise = scn.GetFreepsCard("promise");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(sam, pippin);
		scn.MoveCardsToSupportArea(robin);
		scn.MoveCardsToHand(sword, toby, promise, cloak);

		scn.StartGame();
		scn.MoveMinionsToTable("runner");

		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(robin, "Aid"));

		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, pippin));
		scn.FreepsChooseCard(pippin);
		assertEquals(pool + 2, scn.GetTwilight());
		assertAttachedTo(robin, pippin);

		// Pippin, Trollslayer is a Hobbit and [gondor]: no offer, with Sam beside him and four cards in hand.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(robin));
		assertInZone(Zone.HAND, sword, toby, promise, cloak);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnotherFollowerAttachedToSamAndRobinAttachedToAragornBringNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var robin = scn.GetFreepsCard("robin");
		var radagast = scn.GetFreepsCard("radagast");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var toby = scn.GetFreepsCard("toby");
		var promise = scn.GetFreepsCard("promise");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToSupportArea(robin, radagast);
		scn.MoveCardsToHand(sword, toby, promise, cloak);

		scn.StartGame();
		scn.MoveMinionsToTable("runner");

		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(radagast, "Aid"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(robin, "Aid"));

		// Radagast goes onto Sam, a [shire] companion, while Robin lies in the support area: Robin's Aid is still
		// on offer and nothing else of his is.
		scn.FreepsChooseOptionalTrigger(radagast);
		scn.FreepsChooseCard(sam);
		assertEquals(pool + 2, scn.GetTwilight());
		assertAttachedTo(radagast, sam);
		assertInZone(Zone.SUPPORT, robin);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(robin, "Optional trigger"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(robin, "Aid"));

		// Robin goes onto Aragorn, a [gondor] Man: no offer.
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);
		assertEquals(pool + 4, scn.GetTwilight());
		assertAttachedTo(robin, aragorn);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(robin));
		assertInZone(Zone.HAND, sword, toby, promise, cloak);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AttachedToSmeagolTheOfferMayBeDeclinedAndNothingIsDiscardedOrDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var robin = scn.GetFreepsCard("robin");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sword = scn.GetFreepsCard("sword");
		var toby = scn.GetFreepsCard("toby");
		var promise = scn.GetFreepsCard("promise");
		var cloak = scn.GetFreepsCard("cloak");
		var tale = scn.GetFreepsCard("tale");
		scn.MoveCompanionsToTable(sam, smeagol);
		scn.MoveCardsToSupportArea(robin);
		scn.MoveCardsToHand(sword, toby, promise, cloak);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(tale);
		scn.MoveMinionsToTable("runner");

		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		int deck = scn.GetFreepsDeckCount();
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, smeagol));
		scn.FreepsChooseCard(smeagol);
		assertAttachedTo(robin, smeagol);

		// Smeagol, Once Like Us is a [shire] companion and no Hobbit: the offer is made.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(robin, "Optional trigger"));

		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.HAND, sword, toby, promise, cloak);
		assertEquals(4, scn.GetFreepsHandCount());
		assertInZone(Zone.DECK, tale);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(pool + 2, scn.GetTwilight());
		assertAttachedTo(robin, smeagol);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void UpToThreeOfFiveCardsInHandMayBeDiscardedAndThreeDrawThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var robin = scn.GetFreepsCard("robin");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var toby = scn.GetFreepsCard("toby");
		var promise = scn.GetFreepsCard("promise");
		var cloak = scn.GetFreepsCard("cloak");
		var armor = scn.GetFreepsCard("armor");
		var tale = scn.GetFreepsCard("tale");
		var coat = scn.GetFreepsCard("coat");
		var pony = scn.GetFreepsCard("pony");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToSupportArea(robin);
		scn.MoveCardsToHand(sword, toby, promise, cloak, armor);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(pony, coat, tale);
		scn.MoveMinionsToTable("runner");

		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(sam);
		assertAttachedTo(robin, sam);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(robin, "Optional trigger"));
		int deck = scn.GetFreepsDeckCount();
		assertEquals(5, scn.GetFreepsHandCount());

		scn.FreepsAcceptOptionalTrigger();
		// Five cards to choose from, and no more than three of them may go.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, toby, promise, cloak, armor));
		assertEquals(3, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCards(sword, toby, armor);
		assertInZone(Zone.DISCARD, sword, toby, armor);
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertInZone(Zone.HAND, promise, cloak, tale, coat, pony);
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void EachTimeHeIsAttachedInATurnTheOfferIsMadeAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var robin = scn.GetFreepsCard("robin");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var toby = scn.GetFreepsCard("toby");
		var promise = scn.GetFreepsCard("promise");
		var cloak = scn.GetFreepsCard("cloak");
		var armor = scn.GetFreepsCard("armor");
		var tale = scn.GetFreepsCard("tale");
		var coat = scn.GetFreepsCard("coat");
		var pony = scn.GetFreepsCard("pony");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToSupportArea(robin);
		scn.MoveCardsToHand(sword, toby, promise, cloak);

		scn.StartGame();
		// Drawn in this order: the Tale, the Mithril-coat, the Pony, the Armor.
		scn.MoveCardsToTopOfDeck(armor, pony, coat, tale);
		scn.MoveMinionsToTable("runner");

		scn.SkipToPhase(Phase.MANEUVER);

		// The first maneuver phase of the turn: onto Sam, and one card is discarded to draw one.
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(sam);
		assertAttachedTo(robin, sam);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(robin, "Optional trigger"));
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCards(sword);
		assertInZone(Zone.DISCARD, sword);
		assertInZone(Zone.HAND, toby, promise, cloak, tale);
		assertInZone(Zone.DECK, coat, pony, armor);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The fellowship moves again. Nobody fights the Goblin Runner, which is still there.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.MANEUVER);

		// The second maneuver phase: his Aid is paid again and he goes from Sam to Frodo.
		int pool = scn.GetTwilight();
		assertAttachedTo(robin, sam);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(robin, "Aid"));

		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(frodo);
		assertEquals(pool + 2, scn.GetTwilight());
		assertAttachedTo(robin, frodo);
		assertNotAttachedTo(robin, sam);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(robin, "Optional trigger"));

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCards(toby, promise, cloak);
		assertInZone(Zone.DISCARD, sword, toby, promise, cloak);
		assertInZone(Zone.HAND, tale, coat, pony, armor);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(deck - 4, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoCardInHandToDiscardNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var robin = scn.GetFreepsCard("robin");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var tale = scn.GetFreepsCard("tale");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToSupportArea(robin);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(tale);
		scn.MoveMinionsToTable("runner");

		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(robin, "Aid"));

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(sam);
		assertEquals(pool + 2, scn.GetTwilight());
		assertAttachedTo(robin, sam);

		// Nothing to discard, so nothing to draw: the offer would do nothing and is not made, though the draw
		// deck has cards in it.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(robin));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void OnceTheOfferIsTakenAtLeastOneCardIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var robin = scn.GetFreepsCard("robin");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var toby = scn.GetFreepsCard("toby");
		var tale = scn.GetFreepsCard("tale");
		var coat = scn.GetFreepsCard("coat");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToSupportArea(robin);
		scn.MoveCardsToHand(sword, toby);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(coat, tale);
		scn.MoveMinionsToTable("runner");

		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(sam);
		assertAttachedTo(robin, sam);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(robin, "Optional trigger"));

		// Declining was the way to discard nothing. With two cards in hand the choice is of one or two.
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, toby));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCards(toby);
		assertInZone(Zone.DISCARD, toby);
		assertInZone(Zone.HAND, sword, tale);
		assertInZone(Zone.DECK, coat);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
