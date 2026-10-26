package com.gempukku.lotro.cards.official.set19;

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

public class Card_19_027_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("might", "19_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTokenScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("might", "19_27");
					// Chamber of Records: a [dwarven] condition in the support area that holds [dwarven] tokens
					// (two as it is played beside a Dwarf); its fellowship ability is never used
					put("chamber", "15_3");
					// Rank and File: a [sauron] condition in the support area that holds [sauron] tokens (one each
					// time a roaming [sauron] minion takes a wound); its two abilities are never used
					put("rank", "10_96");
					// Garrison of Osgiliath: a unique [gondor] support-area condition that holds [gondor] tokens (two as
					// it is played beside a [gondor] Man); its skirmish ability is never used
					put("garrison", "6_52");
					put("might2", "19_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SauronsMightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Sauron's Might
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Remove a Free Peoples culture token to add a threat. When you play this event, you may remove (1)
		 * 	to place this on the bottom of your draw deck instead of in your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("might");

		assertEquals("Sauron's Might", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsNotOfferedWithNoFreePeoplesCultureTokenToRemoveAndIsWithOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTokenScenario();

		var might = scn.GetShadowCard("might");
		var rank = scn.GetShadowCard("rank");
		var chamber = scn.GetFreepsCard("chamber");
		scn.MoveCardsToSupportArea(chamber, rank);
		scn.MoveCardsToHand(might);

		scn.StartGame();
		scn.AddTokensToCard(rank, 1);
		assertEquals(1, scn.GetCultureTokensOn(rank));
		scn.FreepsPass();

		// The Shadow phase, with an empty pool: the event costs nothing, and its second sentence, which
		// costs (1), cannot be used.
		scn.SetTwilight(0);
		scn.ShadowRefreshDecision();

		// The only culture token in play is a [sauron] one on a Shadow card.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetCultureTokensOn(chamber));
		assertEquals(1, scn.GetCultureTokensOn(rank));
		assertFalse(scn.ShadowPlayAvailable(might));

		// With one token on a Free Peoples card it is offered.
		scn.AddTokensToCard(chamber, 1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(might));
		assertEquals(1, scn.GetCultureTokensOn(chamber));
		assertEquals(0, scn.GetThreats());

		scn.ShadowPlayCard(might);
		assertEquals(0, scn.GetCultureTokensOn(chamber));
		assertEquals(1, scn.GetCultureTokensOn(rank));
		assertEquals(1, scn.GetThreats());
		assertInDiscard(might);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RemovesATokenFromAFreePeoplesCardOfEitherCultureToAddAThreatAndTheBottomOfTheDeckMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTokenScenario();

		var might = scn.GetShadowCard("might");
		var rank = scn.GetShadowCard("rank");
		var chamber = scn.GetFreepsCard("chamber");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCardsToSupportArea(chamber, garrison, rank);
		scn.MoveCardsToHand(might);

		scn.StartGame();
		scn.AddTokensToCard(chamber, 1);
		scn.AddTokensToCard(garrison, 2);
		scn.AddTokensToCard(rank, 1);
		assertEquals(1, scn.GetCultureTokensOn(chamber));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(rank));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetThreats());
		int pool = scn.GetTwilight();
		assertTrue(pool >= 1);

		scn.ShadowPlayCard(might);

		// A [dwarven] and a [gondor] token on Free Peoples cards; not the [sauron] token on the Shadow player's own card.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(chamber, garrison));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(rank));
		scn.ShadowChooseCard(garrison);

		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(chamber));
		assertEquals(1, scn.GetCultureTokensOn(rank));
		assertEquals(1, scn.GetThreats());

		// The (1) is asked for, and declined: the event goes to the discard pile and the pool is as it was.
		assertTrue(scn.ShadowDecisionAvailable("Would you like to remove (1)"));
		scn.ShadowChooseNo();

		assertInDiscard(might);
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RemovingOneTwilightPlacesItOnTheBottomOfItsOwnersDrawDeckInsteadOfInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTokenScenario();

		var might = scn.GetShadowCard("might");
		var might2 = scn.GetShadowCard("might2");
		var chamber = scn.GetFreepsCard("chamber");
		scn.MoveCardsToSupportArea(chamber);
		// A second copy in hand, which the first's text does not reach.
		scn.MoveCardsToHand(might, might2);
		assertInHand(might, might2);

		scn.StartGame();
		scn.AddTokensToCard(chamber, 1);
		assertEquals(1, scn.GetCultureTokensOn(chamber));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(might, might2);
		int pool = scn.GetTwilight();
		int deck = scn.GetShadowDeckCount();
		assertTrue(pool >= 1);
		assertNotSame(might, scn.GetShadowBottomOfDeck());

		// The one token in play is removed unasked.
		scn.ShadowPlayCard(might);
		assertEquals(0, scn.GetCultureTokensOn(chamber));
		assertEquals(1, scn.GetThreats());

		assertTrue(scn.ShadowDecisionAvailable("Would you like to remove (1)"));
		scn.ShadowChooseYes();

		assertEquals(pool - 1, scn.GetTwilight());
		assertEquals(deck + 1, scn.GetShadowDeckCount());
		assertSame(might, scn.GetShadowBottomOfDeck());
		assertInZone(Zone.DECK, might);
		assertInHand(might2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
