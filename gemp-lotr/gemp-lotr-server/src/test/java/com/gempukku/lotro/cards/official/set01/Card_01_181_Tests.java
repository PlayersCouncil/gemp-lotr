package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_181_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sneak", "1_181");
					put("sneak2", "1_181");
					// Goblin Runner: [moria] Orc, cost 1; its play adds (2) unasked
					put("runner", "1_178");
					// Goblin Scavengers: [moria] Orc of another title; its play offers a weapon from the discard
					// pile, and none is there
					put("scavengers", "1_179");
					// Goblin Marksman: a third [moria] Orc title, archer, no other text
					put("marksman", "1_176");
					// Cave Troll of Moria, Scourge of the Black Pit: a [moria] minion who is no Orc
					put("troll", "1_165");
					// Orc Chieftain: an Orc who is not [moria] ([sauron]); his text answers only a weapon played on him
					put("chieftain", "1_266");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinSneakStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Goblin Sneak
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: When you play this minion, you may place a [moria] Orc from your discard pile beneath your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sneak");

		assertEquals("Goblin Sneak", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedYouMayPlaceAMoriaOrcFromYourDiscardPileBeneathYourDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sneak = scn.GetShadowCard("sneak");
		var runner = scn.GetShadowCard("runner");
		var scavengers = scn.GetShadowCard("scavengers");
		var troll = scn.GetShadowCard("troll");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(sneak);
		scn.MoveCardsToShadowDiscard("runner", "scavengers", "troll", "chieftain");
		assertInDiscard(runner, scavengers, troll, chieftain);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertNotSame(runner, scn.GetShadowBottomOfDeck());
		scn.ShadowPlayCard(sneak);
		assertInZone(Zone.SHADOW_CHARACTERS, sneak);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak));
		scn.ShadowAcceptOptionalTrigger();

		// Two [moria] Orcs of different titles are offered; a [moria] minion who is no Orc, and an Orc who is not
		// [moria], are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, scavengers));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troll, chieftain));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(runner);

		assertSame(runner, scn.GetShadowBottomOfDeck());
		assertInZone(Zone.DECK, runner);
		assertInDiscard(scavengers, troll, chieftain);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesTheDiscardPileAndTheDrawDeckAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sneak = scn.GetShadowCard("sneak");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(sneak);
		scn.MoveCardsToShadowDiscard("runner");
		assertInDiscard(runner);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		var bottom = scn.GetShadowBottomOfDeck();
		int deck = scn.GetShadowDeckCount();
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(sneak);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak));
		scn.ShadowDeclineOptionalTrigger();

		assertInDiscard(runner);
		assertSame(bottom, scn.GetShadowBottomOfDeck());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnswersItsOwnPlayOnlyAndNotAnotherMoriaOrcsOrAnotherCopys() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sneak = scn.GetShadowCard("sneak");
		var sneak2 = scn.GetShadowCard("sneak2");
		var runner = scn.GetShadowCard("runner");
		var scavengers = scn.GetShadowCard("scavengers");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(sneak2, runner);
		scn.MoveCardsToShadowDiscard("scavengers", "marksman");
		assertInDiscard(scavengers, marksman);

		scn.StartGame();
		scn.MoveMinionsToTable(sneak);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, sneak);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [moria] Orc played with this Sneak in play: no offer.
		scn.ShadowPlayCard(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second Sneak played: its own offer, and none from the Sneak already in play.
		scn.ShadowPlayCard(sneak2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(sneak));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(scavengers);

		assertSame(scavengers, scn.GetShadowBottomOfDeck());
		assertInDiscard(marksman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoMoriaOrcInYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sneak = scn.GetShadowCard("sneak");
		var troll = scn.GetShadowCard("troll");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(sneak);
		scn.MoveCardsToShadowDiscard("troll", "chieftain");
		assertInDiscard(troll, chieftain);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(sneak);
		assertInZone(Zone.SHADOW_CHARACTERS, sneak);

		// Only a [moria] minion who is no Orc and an Orc who is not [moria] are there: nothing to place, no offer.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(troll, chieftain);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
