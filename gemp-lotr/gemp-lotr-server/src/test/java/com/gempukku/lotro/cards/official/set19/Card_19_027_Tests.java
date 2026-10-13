package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
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
					put("card", "19_27");
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
		 * Game Text: Remove a Free Peoples culture token to add a threat. When you play this event, you may remove (1) to place this on the bottom of your draw deck instead of in your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

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
}
