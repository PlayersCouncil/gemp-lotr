package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_076_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "15_76");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRevealScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("homestead", "15_76");
					put("scout1", "15_77");     // Easterling Scout: a [men] Man; its ability is a skirmish one
					put("scout2", "15_77");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DestroyedHomesteadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Destroyed Homestead
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When you play this, spot 2 [men] Men to randomly reveal a site from the Free Peoples player's adventure deck. Note its card title. Each site on the adventure path with the same card title as the selected site loses its game text.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Destroyed Homestead", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenPlayedWithTwoMenMenToSpotOneSiteOfTheFreePeoplesAdventureDeckIsRevealedToBothPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRevealScenario();

		var homestead = scn.GetShadowCard("homestead");
		var scout1 = scn.GetShadowCard("scout1");
		var scout2 = scn.GetShadowCard("scout2");
		scn.MoveCardsToHand(homestead);

		scn.StartGame();
		scn.MoveMinionsToTable(scout1, scout2);
		// 7, and for the move 1 companion and the 2 of Ettenmoors
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(homestead));

		scn.ShadowPlayCard(homestead);
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, homestead);

		// Each player is shown one card.  Which site is chance: it is one of the eight still in the Free
		// Peoples player's adventure deck, sites 2 to 9, and not his site 1, which is on the adventure path.
		// A card shown is read by its blueprint, and the Shadow player's adventure deck holds the same
		// sites but for his site 2, which is on the path, and his site 1, which is not: a site revealed
		// from the wrong deck would be told from this only when it is site 1, one time in eight.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		int shownToFreeps = 0;
		int shownToShadow = 0;
		for (int number = 2; number <= 9; number++) {
			var site = scn.GetFreepsSite(number);
			assertInZone(Zone.ADVENTURE_DECK, site);
			if (scn.FreepsHasCardChoiceAvailable(site, false))
				shownToFreeps++;
			if (scn.ShadowHasCardChoiceAvailable(site, false))
				shownToShadow++;
		}
		assertEquals(1, shownToFreeps);
		assertEquals(1, shownToShadow);
		assertFalse(scn.FreepsHasCardChoiceAvailable(scn.GetSite(1), false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(scn.GetSite(1), false));

		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
