package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import com.gempukku.lotro.game.PhysicalCardImpl;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_109_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gorbag", "15_109");
					put("tracking", "15_171");    // Tracking Uruk: [uruk-hai] Uruk-hai minion, Hunter 1 is its whole text
					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc minion, no hunter; his text answers only a weapon played on him
					put("goblin", "11_137");      // Scurrying Goblin: [orc] Orc minion, cost 1; adds (2) only at an underground site
					put("guardian", "17_7");      // Elven Guardian: [elven] Elf companion, Hunter 1; a maneuver heal never used

					// Cards of the Free Peoples draw deck, laid in a known order.
					put("deck1", "1_53");         // Lorien Elf, here only as a card of the draw deck
					put("deck2", "1_53");
					put("deck3", "1_53");
					put("deck4", "1_53");
					put("deck5", "1_53");
					put("deck6", "1_53");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorbagStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Gorbag, Filthy Rebel
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Hunter 3</b>. <helper>(While skirmishing a non-hunter character, this character is strength +3.)</helper><br>When you play Gorbag, you may discard a card from the top of the Free Peoples player's deck for each hunter character you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gorbag");

		assertEquals("Gorbag", card.getBlueprint().getTitle());
		assertEquals("Filthy Rebel", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(3, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithThreeHuntersInPlayUpToThreeCardsAreDiscardedFromTheTopOfTheFreePeoplesDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guardian = scn.GetFreepsCard("guardian");
		var deck1 = scn.GetFreepsCard("deck1");
		var deck2 = scn.GetFreepsCard("deck2");
		var deck3 = scn.GetFreepsCard("deck3");
		var deck4 = scn.GetFreepsCard("deck4");
		var deck5 = scn.GetFreepsCard("deck5");
		var deck6 = scn.GetFreepsCard("deck6");
		scn.MoveCompanionsToTable(guardian);

		var gorbag = scn.GetShadowCard("gorbag");
		var tracking = scn.GetShadowCard("tracking");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SetFreepsDrawDeck(deck1, deck2, deck3, deck4, deck5, deck6);
		// Hunters of each side, culture and race: Gorbag himself, the [uruk-hai] Tracking Uruk and the [elven]
		// companion.  Frodo and the Orc Chieftain are characters who are no hunters.  Their hunter numbers add up
		// to 3 + 1 + 1 = 5, and there are 5 characters in all: only a count of hunter characters reads 3.
		scn.MoveMinionsToTable(tracking, chieftain);
		scn.MoveCardsToHand(gorbag);
		scn.SetTwilight(10);
		scn.FreepsPass();

		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(6, scn.GetFreepsDeckCount());
		assertEquals(deck1, scn.GetFreepsTopOfDeck());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.ShadowPlayAvailable(gorbag));
		scn.ShadowPlayCard(gorbag);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gorbag));
		scn.ShadowAcceptOptionalTrigger();

		// Q-0399: the yes leads to a choice of 0 up to the 3 hunters spotted.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("3");

		assertInDiscard(deck1, deck2, deck3);
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(deck4, scn.GetFreepsTopOfDeck());
		// The Shadow player's own deck is not the one discarded from.
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithGorbagTheOnlyHunterAtMostOneCardIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deck1 = scn.GetFreepsCard("deck1");
		var deck2 = scn.GetFreepsCard("deck2");
		var deck3 = scn.GetFreepsCard("deck3");

		var gorbag = scn.GetShadowCard("gorbag");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SetFreepsDrawDeck(deck1, deck2, deck3);
		scn.MoveMinionsToTable(chieftain);
		scn.MoveCardsToHand(gorbag);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(deck1, scn.GetFreepsTopOfDeck());
		scn.ShadowPlayCard(gorbag);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gorbag));
		scn.ShadowAcceptOptionalTrigger();

		// Gorbag, a hunter himself, is the one hunter character: one card at most.
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");

		assertInDiscard(deck1);
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(deck2, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningDiscardsNothingAndAnotherOrcPlayedAfterGorbagIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deck1 = scn.GetFreepsCard("deck1");
		var deck2 = scn.GetFreepsCard("deck2");
		var deck3 = scn.GetFreepsCard("deck3");

		var gorbag = scn.GetShadowCard("gorbag");
		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		scn.SetFreepsDrawDeck(deck1, deck2, deck3);
		scn.MoveCardsToHand(gorbag, goblin);
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.ShadowPlayCard(gorbag);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gorbag));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(deck1, scn.GetFreepsTopOfDeck());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [orc] Orc played while Gorbag is in play sets nothing off.
		scn.ShadowPlayCard(goblin);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(gorbag));
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheFreePeoplesDrawDeckIsEmpty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gorbag = scn.GetShadowCard("gorbag");
		var tracking = scn.GetShadowCard("tracking");

		scn.StartGame();
		scn.MoveCardsToDiscard(new ArrayList<>(scn.GetFreepsDrawDeck()).toArray(new PhysicalCardImpl[0]));
		scn.MoveMinionsToTable(tracking);
		scn.MoveCardsToHand(gorbag);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetFreepsDeckCount());
		scn.ShadowPlayCard(gorbag);

		// Nothing to discard: the offer is not made (Q-0034).
		assertInZone(Zone.SHADOW_CHARACTERS, gorbag);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(gorbag));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
