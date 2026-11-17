package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_324_Tests
{

	// The Prancing Pony is site 1; the rest of the path is that of FellowshipSites.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Aragorn, cost 4; a maneuver ability used only on purpose
					// Aragorn, Defender of Rohan: the only Aragorn who is not [gondor]; his hunter needs the Ring-bearer assigned
					put("rohanaragorn", "17_93");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion who is not Aragorn; a skirmish ability never used
				}},
				new HashMap<>()
				{{
					put("site1", "1_324");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThePrancingPonyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Prancing Pony
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 1
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: Fellowship: Add a burden to play Aragorn from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("The Prancing Pony", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void FellowshipActionAddsABurdenToPlayAnAragornOfEitherCultureFromTheDrawDeckAndShufflesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var rohanaragorn = scn.GetFreepsCard("rohanaragorn");
		var boromir = scn.GetFreepsCard("boromir");

		scn.StartGame();

		var pony = scn.GetCurrentSite();
		assertEquals("The Prancing Pony", pony.getBlueprint().getTitle());
		// Each player's two Aragorns and his Boromir lie in his own draw deck.
		assertInZone(Zone.DECK, aragorn, rohanaragorn, boromir);
		assertInZone(Zone.DECK, scn.GetShadowCard("aragorn"), scn.GetShadowCard("rohanaragorn"));
		assertEquals(0, scn.GetBurdens());
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertTrue(scn.FreepsActionAvailable(pony));

		scn.FreepsUseCardAction(pony);
		assertEquals(1, scn.GetBurdens());

		// His own draw deck is shown to him, and no other.
		assertEquals(scn.GetFreepsDeckCount(), scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// Either Aragorn; the [gondor] Man who is no Aragorn is not offered.  He need not play one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(rohanaragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(rohanaragorn);

		assertInZone(Zone.FREE_CHARACTERS, rohanaragorn);
		assertInZone(Zone.DECK, aragorn, boromir);
		assertTrue(scn.GetFreepsDeckShuffleCount() > freepsShuffles);
		// The Shadow player's deck was not looked through.
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoAragornInTheDrawDeckTheActionIsStillOfferedAndOnlyLooksThroughTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var rohanaragorn = scn.GetFreepsCard("rohanaragorn");
		var boromir = scn.GetFreepsCard("boromir");

		scn.StartGame();
		scn.MoveCardsToDiscard(aragorn, rohanaragorn);

		var pony = scn.GetCurrentSite();
		assertEquals(0, scn.GetBurdens());
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		// No penalty for failing to play from a draw deck (Q-0019): the action may be used for a look.
		assertTrue(scn.FreepsActionAvailable(pony));
		scn.FreepsUseCardAction(pony);
		assertEquals(1, scn.GetBurdens());

		// The deck is shown, with nothing in it that may be chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, false));
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DECK, boromir);
		assertInZone(Zone.DISCARD, aragorn, rohanaragorn);
		assertTrue(scn.GetFreepsDeckShuffleCount() > freepsShuffles);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
