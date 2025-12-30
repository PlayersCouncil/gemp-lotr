package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_103_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("accounted", "103_103");  // They Are Not All Accounted For
					put("slayer", "3_93");        // Morgul Slayer - [Sauron] minion
					put("stone", "9_47");         // Ithil Stone - [Sauron] artifact
					put("bladetip", "1_209");     // Blade Tip - [Ringwraith] condition
					put("runner", "1_178");       // Goblin Runner - [Moria], not [Sauron]

					put("halls", "1_18");         // Halls of My Home - reveals/discards from deck
					put("aragorn", "1_89");       // Unbound companion
					put("sam", "1_311");          // Ring-bound companion
					put("bilbo", "1_284");        // Ally
					put("gimli", "1_13");         // Dwarf for Halls
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}
	@Test
	public void TheyAreNotAllAccountedForStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: They Are Not All Accounted For
		 * Unique: false
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 2 [sauron] or [ringwraith] cards.
		* 	Each time a Free Peoples card reveals or discards a card from any draw deck, you may exert an unbound companion or ally to draw a card (limit once per site).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("accounted");

		assertEquals("They Are Not All Accounted For", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}



	@Test
	public void AccountedRequiresTwoSauronOrRingwraithToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var accounted = scn.GetShadowCard("accounted");
		var slayer = scn.GetShadowCard("slayer");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToHand(accounted);
		scn.MoveMinionsToTable(slayer, runner);  // Only 1 [Sauron], runner is [Moria]

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		assertFalse(scn.ShadowPlayAvailable(accounted));
	}

	@Test
	public void AccountedCanPlayWithMixedSauronAndRingwraith() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var accounted = scn.GetShadowCard("accounted");
		var slayer = scn.GetShadowCard("slayer");
		var bladetip = scn.GetShadowCard("bladetip");

		scn.MoveCardsToHand(accounted);
		scn.MoveMinionsToTable(slayer);
		scn.MoveCardsToSupportArea(bladetip);  // 1 [Sauron] + 1 [Ringwraith]

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.ShadowPlayAvailable(accounted));
		scn.ShadowPlayCard(accounted);

		assertInZone(Zone.SUPPORT, accounted);
	}

	@Test
	public void AccountedTriggersOnRevealFromFreepsDeckAndCanExertUnboundCompanionToDrawCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var accounted = scn.GetShadowCard("accounted");
		var halls = scn.GetFreepsCard("halls");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bilbo = scn.GetFreepsCard("bilbo");
		var sam = scn.GetFreepsCard("sam");
		//a shadow card owned by player 1
		var runner = scn.GetFreepsCard("runner");

		scn.MoveCardsToSupportArea(accounted, bilbo);
		scn.MoveCardsToHand(halls);
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(runner);

		int shadowHand = scn.GetShadowHandCount();

		// Halls reveals top 3 cards
		scn.FreepsPlayCard(halls);
		// Gimli auto-selected as only Dwarf to exert
		scn.FreepsChoose("your deck");
		scn.DismissRevealedCards();

		// Accounted trigger fires on reveal
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Choose unbound companion to exert
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn));
		assertTrue(scn.ShadowHasCardChoiceAvailable(bilbo));
		assertFalse(scn.ShadowHasCardChoiceAvailable(sam));
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(shadowHand + 1, scn.GetShadowHandCount());
	}

	@Test
	public void AccountedTriggersOnDiscardFromFreepsDeckAndCanExertUnboundCompanionToDrawCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var accounted = scn.GetShadowCard("accounted");
		var halls = scn.GetFreepsCard("halls");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bilbo = scn.GetFreepsCard("bilbo");
		var sam = scn.GetFreepsCard("sam");
		//a shadow card owned by player 1
		var runner = scn.GetFreepsCard("runner");
		var bladetip = scn.GetFreepsCard("bladetip");

		scn.MoveCardsToSupportArea(accounted, bilbo);
		scn.MoveCardsToHand(halls);
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();

		scn.MoveCardsToTopOfDeck(runner, bladetip);

		int shadowHand = scn.GetShadowHandCount();

		// Halls reveals top 3 cards
		scn.FreepsPlayCard(halls);
		// Gimli auto-selected as only Dwarf to exert
		scn.FreepsChoose("your deck");
		scn.DismissRevealedCards();

		// Accounted trigger fires on reveal
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();

		scn.FreepsChooseYes();
		scn.FreepsChooseCardBPFromSelection(runner); //discarded

		// Accounted trigger fires on discard
		assertInDiscard(runner);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Choose unbound companion to exert
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn));
		assertTrue(scn.ShadowHasCardChoiceAvailable(bilbo));
		assertFalse(scn.ShadowHasCardChoiceAvailable(sam));
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(shadowHand + 1, scn.GetShadowHandCount());
	}

	@Test
	public void AccountedTriggersOnRevealFromShadowDeckAndCanExertUnboundCompanionToDrawCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var accounted = scn.GetShadowCard("accounted");
		var halls = scn.GetFreepsCard("halls");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bilbo = scn.GetFreepsCard("bilbo");
		var sam = scn.GetFreepsCard("sam");
		//a shadow card owned by player 1
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToSupportArea(accounted, bilbo);
		scn.MoveCardsToHand(halls);
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(runner);

		int shadowHand = scn.GetShadowHandCount();

		// Halls reveals top 3 cards
		scn.FreepsPlayCard(halls);
		// Gimli auto-selected as only Dwarf to exert
		scn.FreepsChoose("opponent's deck");
		scn.DismissRevealedCards();

		// Accounted trigger fires on reveal
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Choose unbound companion to exert
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn));
		assertTrue(scn.ShadowHasCardChoiceAvailable(bilbo));
		assertFalse(scn.ShadowHasCardChoiceAvailable(sam));
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(shadowHand + 1, scn.GetShadowHandCount());
	}

	@Test
	public void AccountedTriggersOnDiscardFromShadowDeckAndCanExertUnboundCompanionToDrawCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var accounted = scn.GetShadowCard("accounted");
		var halls = scn.GetFreepsCard("halls");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bilbo = scn.GetFreepsCard("bilbo");
		var sam = scn.GetFreepsCard("sam");
		//shadow cards owned by player 2
		var runner = scn.GetShadowCard("runner");
		var bladetip = scn.GetShadowCard("bladetip");

		scn.MoveCardsToSupportArea(accounted, bilbo);
		scn.MoveCardsToHand(halls);
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();

		scn.MoveCardsToTopOfDeck(runner, bladetip);

		int shadowHand = scn.GetShadowHandCount();

		// Halls reveals top 3 cards
		scn.FreepsPlayCard(halls);
		// Gimli auto-selected as only Dwarf to exert
		scn.FreepsChoose("opponent's deck");
		scn.DismissRevealedCards();

		// Accounted trigger fires on reveal
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();

		scn.FreepsChooseYes();
		scn.FreepsChooseCardBPFromSelection(runner); //discarded

		// Accounted trigger fires on discard
		assertInDiscard(runner);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Choose unbound companion to exert
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn));
		assertTrue(scn.ShadowHasCardChoiceAvailable(bilbo));
		assertFalse(scn.ShadowHasCardChoiceAvailable(sam));
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(shadowHand + 1, scn.GetShadowHandCount());
	}


	@Test
	public void AccountedLimitedOncePerSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var accounted = scn.GetShadowCard("accounted");
		var halls = scn.GetFreepsCard("halls");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		//a shadow card owned by player 1
		var runner = scn.GetFreepsCard("runner");

		scn.MoveCardsToSupportArea(accounted);
		scn.MoveCardsToHand(halls);
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(runner);

		// First reveal - trigger available
		scn.FreepsPlayCard(halls);
		scn.FreepsChoose("your deck");
		scn.DismissRevealedCards();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(aragorn);

		//Choose to discard a card
		scn.FreepsChooseYes();
		scn.FreepsChooseCardBPFromSelection(runner); //discarded
		assertInDiscard(runner);

		// The discard also triggers, but limit already used
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

}
