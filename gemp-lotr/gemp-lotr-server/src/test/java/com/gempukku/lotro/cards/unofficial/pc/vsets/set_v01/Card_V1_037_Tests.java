package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_037_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fell", "101_37");
					put("fell2", "101_37");

					put("lemenya", "1_232");      // Ulaire Lemenya: Nazgul; weakens characters bearing [ringwraith] conditions, and none does
					put("nelya", "2_84");         // Ulaire Nelya, Ringwraith in Twilight: Nazgul; fierce and damage +1 only with 2 burdens or a twice-wounded Ring-bearer

					// [ringwraith] items
					put("sword", "1_218");        // Nazgul Sword: possession, cost 1, bearer must be a Nazgul
					put("blade", "1_216");        // Morgul Blade: possession, cost 1, bearer must be a Nazgul
					put("crown", "101_39");       // Pale Crown: artifact, cost 1, plays to the support area
					put("ring", "9_44");          // Ring of Rancor: artifact, cost 0, bearer must be a Nazgul
					put("steed", "1_208");        // Black Steed: possession, cost 1, bearer must be a Nazgul

					// [ringwraith] cards that are not items
					put("breath", "1_207");       // Black Breath: condition, cost 1, plays to the support area
					put("tip", "1_209");          // Blade Tip: condition, cost 0, plays to the support area
					put("otsea", "1_235");        // Ulaire Otsea: Nazgul minion, cost 4

					// Shadow items of other cultures, each of which plays to the support area with nothing to spot
					put("scaffolding", "6_73");   // Scaffolding: [isengard] possession, cost 1
					put("hides", "4_19");         // Hides: [dunland] possession, cost 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetGreatRiverScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fell", "101_37");
					put("lemenya", "1_232");      // Ulaire Lemenya: a Nazgul to bear the items
					put("blade", "1_216");        // Morgul Blade: [ringwraith] possession, cost 1
					put("ring", "9_44");          // Ring of Rancor: [ringwraith] artifact, cost 0
					put("steed", "1_208");        // Black Steed: [ringwraith] possession, cost 1
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "3_118");        // The Great River: "cards may not be played from draw decks or discard piles"
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FellVoicesCallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Fell Voices Call
		 * Unique: False
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Play a [ringwraith] item from your draw deck or discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fell");

		assertEquals("Fell Voices Call", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysAChosenRingwraithItemFromTheDrawDeckOnAChosenNazgulAndReshufflesTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell = scn.GetShadowCard("fell");
		var fell2 = scn.GetShadowCard("fell2");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var sword = scn.GetShadowCard("sword");
		var crown = scn.GetShadowCard("crown");
		var blade = scn.GetShadowCard("blade");
		var ring = scn.GetShadowCard("ring");
		var steed = scn.GetShadowCard("steed");
		var breath = scn.GetShadowCard("breath");
		var tip = scn.GetShadowCard("tip");
		var otsea = scn.GetShadowCard("otsea");
		var scaffolding = scn.GetShadowCard("scaffolding");
		var hides = scn.GetShadowCard("hides");
		// The Nazgul Sword, the Pale Crown, Black Breath, Scaffolding and the second Fell Voices Call stay in the draw deck.
		scn.MoveCardsToHand(fell, steed);
		scn.MoveCardsToDiscard(blade, ring, tip, hides, otsea);
		// The other player's Black Steed, in the other player's discard pile
		scn.MoveCardsToDiscard(scn.GetFreepsCard("steed"));

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, nelya);
		scn.SetTwilight(7);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 7 set + 1 for Frodo + 2 for the shadow number of site 2: enough for any card here
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.DECK, sword, crown, breath, scaffolding, fell2);
		assertInZone(Zone.DISCARD, blade, ring, tip, hides, otsea);
		assertInZone(Zone.HAND, fell, steed);
		int deckBefore = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.ShadowPlayAvailable(fell));

		scn.ShadowPlayCard(fell);
		// Each pile holds an item that can be played, so the player says which pile.
		assertTrue(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowChoose("draw deck");
		scn.ShadowDismissRevealedCards();

		// A possession for a Nazgul and an artifact for the support area
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, crown));
		// In the deck: a [ringwraith] condition, an [isengard] possession, a [ringwraith] event.
		// Elsewhere: the [ringwraith] items in the discard pile, and the Black Steed, which is in hand
		// (and, the other player's copy, in the other player's discard pile).
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(breath, scaffolding, fell2, blade, ring, steed));
		scn.ShadowChooseCardBPFromSelection(sword);

		// The Sword is played as from hand: its bearer is the Shadow player's to choose.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, nelya));
		scn.ShadowChooseCard(nelya);

		assertAttachedTo(sword, nelya);
		assertNotAttachedTo(sword, lemenya);
		// 10 - 0 for the event - 1 for the Sword
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.DISCARD, fell);
		assertInZone(Zone.DECK, crown);
		assertInZone(Zone.HAND, steed);
		assertEquals(deckBefore - 1, scn.GetShadowDeckCount());
		// The draw deck was looked through for the item, so it is reshuffled once the looking is done.
		// (The game counts two shuffles for a card that goes onto a bearer, one as it leaves the deck and one
		// for the event. The order of the deck is lost either way.)
		assertTrue(scn.GetShadowDeckShuffleCount() > shuffles);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlaysAChosenRingwraithItemFromTheDiscardPileAndLeavesTheDeckUnshuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell = scn.GetShadowCard("fell");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var sword = scn.GetShadowCard("sword");
		var crown = scn.GetShadowCard("crown");
		var blade = scn.GetShadowCard("blade");
		var ring = scn.GetShadowCard("ring");
		var steed = scn.GetShadowCard("steed");
		var tip = scn.GetShadowCard("tip");
		var otsea = scn.GetShadowCard("otsea");
		var hides = scn.GetShadowCard("hides");
		// The Nazgul Sword and the Pale Crown stay in the draw deck.
		scn.MoveCardsToHand(fell, steed);
		scn.MoveCardsToDiscard(blade, ring, tip, hides, otsea);
		scn.MoveCardsToDiscard(scn.GetFreepsCard("steed"));

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, nelya);
		scn.SetTwilight(7);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 7 set + 1 for Frodo + 2 for the shadow number of site 2: enough for any card here, the minion included
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.DISCARD, blade, ring, tip, hides, otsea);
		assertInZone(Zone.DECK, sword, crown);
		int deckBefore = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(fell);
		assertTrue(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowChoose("discard pile");

		// A possession and an artifact
		assertTrue(scn.ShadowHasCardChoiceAvailable(blade, ring));
		// In the discard pile: a [ringwraith] condition, a [ringwraith] minion, a [dunland] possession.
		// Elsewhere: the [ringwraith] items in the draw deck and the one in hand.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(tip, otsea, hides, sword, crown, steed));
		scn.ShadowChooseCardBPFromSelection(blade);

		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, nelya));
		scn.ShadowChooseCard(lemenya);

		assertAttachedTo(blade, lemenya);
		assertNotAttachedTo(blade, nelya);
		// 10 - 0 for the event - 1 for the Blade
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.DISCARD, fell, ring, tip, hides, otsea);
		// The draw deck was never looked through: nothing has left it and it has not been shuffled.
		assertEquals(deckBefore, scn.GetShadowDeckCount());
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoRingwraithItemInTheDiscardPileTheDrawDeckIsLookedThroughUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell = scn.GetShadowCard("fell");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var sword = scn.GetShadowCard("sword");
		var crown = scn.GetShadowCard("crown");
		var blade = scn.GetShadowCard("blade");
		var ring = scn.GetShadowCard("ring");
		var steed = scn.GetShadowCard("steed");
		var breath = scn.GetShadowCard("breath");
		var tip = scn.GetShadowCard("tip");
		var otsea = scn.GetShadowCard("otsea");
		var scaffolding = scn.GetShadowCard("scaffolding");
		var hides = scn.GetShadowCard("hides");
		// All four other [ringwraith] items stay in the draw deck. The discard pile has only cards that nearly qualify.
		scn.MoveCardsToHand(fell, steed);
		scn.MoveCardsToDiscard(tip, hides, otsea);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, nelya);
		scn.SetTwilight(7);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 7 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.DECK, sword, crown, blade, ring);
		assertInZone(Zone.DISCARD, tip, hides, otsea);
		int shuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.ShadowPlayAvailable(fell));

		scn.ShadowPlayCard(fell);
		// Nothing in the discard pile can be played, so there is no pile to choose: the deck is shown at once.
		assertFalse(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowDismissRevealedCards();

		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, crown, blade, ring));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(breath, scaffolding, steed, tip, hides, otsea));
		scn.ShadowChooseCardBPFromSelection(crown);

		// The Crown plays to the support area: no Nazgul is asked for and neither bears it.
		assertInZone(Zone.SUPPORT, crown);
		assertNotAttachedTo(crown, lemenya);
		assertNotAttachedTo(crown, nelya);
		// 10 - 0 for the event - 1 for the Crown
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.DISCARD, fell);
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoRingwraithItemInTheDrawDeckTheItemComesFromTheDiscardPileUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell = scn.GetShadowCard("fell");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var sword = scn.GetShadowCard("sword");
		var crown = scn.GetShadowCard("crown");
		var blade = scn.GetShadowCard("blade");
		var ring = scn.GetShadowCard("ring");
		var steed = scn.GetShadowCard("steed");
		var breath = scn.GetShadowCard("breath");
		var tip = scn.GetShadowCard("tip");
		var otsea = scn.GetShadowCard("otsea");
		var scaffolding = scn.GetShadowCard("scaffolding");
		var hides = scn.GetShadowCard("hides");
		// The draw deck is left with Black Breath, Scaffolding and the second Fell Voices Call: no [ringwraith] item.
		scn.MoveCardsToHand(fell, steed, sword, crown);
		scn.MoveCardsToDiscard(blade, ring, tip, hides, otsea);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, nelya);
		scn.SetTwilight(7);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 7 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.DECK, breath, scaffolding);
		assertInZone(Zone.DISCARD, blade, ring);
		assertInZone(Zone.HAND, sword, crown, steed);
		int deckBefore = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();
		assertEquals(2, scn.GetVitality(lemenya));
		assertTrue(scn.ShadowPlayAvailable(fell));

		scn.ShadowPlayCard(fell);
		// Only the discard pile holds an item, and a player may only choose what can be fully performed.
		assertFalse(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(blade, ring));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(tip, otsea, hides, sword, crown, steed));
		scn.ShadowChooseCardBPFromSelection(ring);

		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, nelya));
		scn.ShadowChooseCard(lemenya);

		assertAttachedTo(ring, lemenya);
		// The Ring of Rancor is vitality +1.
		assertEquals(3, scn.GetVitality(lemenya));
		// The event and the Ring both cost 0.
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.DISCARD, fell, blade);
		assertEquals(deckBefore, scn.GetShadowDeckCount());
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell = scn.GetShadowCard("fell");
		var lemenya = scn.GetShadowCard("lemenya");
		var sword = scn.GetShadowCard("sword");
		var ring = scn.GetShadowCard("ring");
		scn.MoveCardsToHand(fell);
		scn.MoveCardsToDiscard(ring);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(7);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertInZone(Zone.DECK, sword);
		assertInZone(Zone.DISCARD, ring);
		assertTrue(scn.ShadowPlayAvailable(fell));

		scn.ShadowPassCurrentPhaseAction();
		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		scn.FreepsPassCurrentPhaseAction();

		// The same piles, the same Nazgul, the same pool, and the phase after the one the card names
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.HAND, fell);
		assertFalse(scn.ShadowPlayAvailable(fell));
	}

	@Test
	public void CannotBePlayedWithNoRingwraithItemInEitherPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell = scn.GetShadowCard("fell");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var sword = scn.GetShadowCard("sword");
		var crown = scn.GetShadowCard("crown");
		var blade = scn.GetShadowCard("blade");
		var ring = scn.GetShadowCard("ring");
		var steed = scn.GetShadowCard("steed");
		var breath = scn.GetShadowCard("breath");
		var tip = scn.GetShadowCard("tip");
		var otsea = scn.GetShadowCard("otsea");
		var scaffolding = scn.GetShadowCard("scaffolding");
		var hides = scn.GetShadowCard("hides");
		// Every [ringwraith] item is in hand. Each pile has a [ringwraith] card that is no item and an item that is not [ringwraith].
		scn.MoveCardsToHand(fell, sword, crown, blade, ring, steed);
		scn.MoveCardsToDiscard(tip, hides, otsea);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, nelya);
		scn.SetTwilight(7);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 7 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.DECK, breath, scaffolding);
		assertInZone(Zone.DISCARD, tip, hides, otsea);
		// The items themselves can be played from hand, so it is not the pool or the want of a bearer.
		assertTrue(scn.ShadowPlayAvailable(sword));
		assertTrue(scn.ShadowPlayAvailable(crown));

		assertFalse(scn.ShadowPlayAvailable(fell));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedAtTheGreatRiverWhereNothingIsPlayedFromADrawDeckOrDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGreatRiverScenario();

		var fell = scn.GetShadowCard("fell");
		var lemenya = scn.GetShadowCard("lemenya");
		var blade = scn.GetShadowCard("blade");
		var ring = scn.GetShadowCard("ring");
		var steed = scn.GetShadowCard("steed");
		scn.MoveCardsToDiscard(ring);

		scn.StartGame();
		scn.SkipToSite(6);
		// Put where they belong after the turns on the way, which draw and discard cards
		scn.MoveCardsToHand(fell, steed);
		scn.MoveCardsToTopOfDeck(blade);
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(4);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals("The Great River", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// An item in each pile and a Nazgul to bear either
		assertInZone(Zone.DISCARD, ring);
		assertInZone(Zone.DECK, blade);
		// An item in hand can still be played here, so it is the two piles that are shut and not the pool or the site.
		assertTrue(scn.ShadowPlayAvailable(steed));

		assertFalse(scn.ShadowPlayAvailable(fell));
	}
}
