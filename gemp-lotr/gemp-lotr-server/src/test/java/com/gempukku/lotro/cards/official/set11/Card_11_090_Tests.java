package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_090_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bree", "11_90");
					// A second copy: each counts the hand for itself.
					put("bree2", "11_90");
					// Harad Standard-bearer: another [men] Man minion, strength 10.  Its skirmish ability is never used.
					put("bearer", "11_84");
					// Cards for the hands.  Gimli, Son of Glóin: a companion the Free Peoples player plays from hand.
					// His skirmish ability is never used.
					put("gimli", "1_13");
					// Legion of Harad: a [men] minion with no text but toil, only ever held in a hand.
					put("legion1", "11_88");
					put("legion2", "11_88");
					put("legion3", "11_88");
					put("legion4", "11_88");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ManofBreeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Man of Bree
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: This minion is strength +1 for each card in the Free Peoples player's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bree");

		assertEquals("Man of Bree", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlus1ForEachCardInTheFreePeoplesHandAndFollowsTheHandAsItChanges() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legion1 = scn.GetFreepsCard("legion1");
		var legion2 = scn.GetFreepsCard("legion2");

		var bree = scn.GetShadowCard("bree");
		var bree2 = scn.GetShadowCard("bree2");
		var bearer = scn.GetShadowCard("bearer");

		scn.StartGame();
		scn.MoveCardsToHand(gimli, legion1, legion2);
		// Laid early, in the fellowship phase, to be read before and after a Free Peoples card is played (Q-0228).
		scn.MoveMinionsToTable(bree, bree2, bearer);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowHandCount());
		// 4 printed + 3 cards in hand, on each copy; the other minion is left at its printed 10.
		assertEquals(7, scn.GetStrength(bree));
		assertEquals(7, scn.GetStrength(bree2));
		assertEquals(10, scn.GetStrength(bearer));

		scn.FreepsPlayCard(gimli);

		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(2, scn.GetFreepsHandCount());
		// 4 + 2.
		assertEquals(6, scn.GetStrength(bree));
		assertEquals(6, scn.GetStrength(bree2));
		assertEquals(10, scn.GetStrength(bearer));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CardsInTheShadowPlayersHandAreNotCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bree = scn.GetShadowCard("bree");
		var legion1 = scn.GetShadowCard("legion1");
		var legion2 = scn.GetShadowCard("legion2");
		var legion3 = scn.GetShadowCard("legion3");
		var legion4 = scn.GetShadowCard("legion4");

		scn.StartGame();
		scn.MoveCardsToHand(legion1, legion2, legion3, legion4);
		scn.MoveMinionsToTable(bree);

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetShadowHandCount());
		// Counting the Shadow player's four cards would read 8.
		assertEquals(4, scn.GetStrength(bree));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
