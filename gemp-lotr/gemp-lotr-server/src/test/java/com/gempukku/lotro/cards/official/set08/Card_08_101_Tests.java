package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_101_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stormer", "8_101");
					put("stormer2", "8_101");

					// Cards for the Free Peoples player's hand and draw deck: the hand sets who has initiative.
					put("card1", "1_177");     // Goblin Patrol Troop: a minion with no text
					put("card2", "1_177");
					put("card3", "1_177");
					put("card4", "1_177");
					put("drawn", "1_13");      // Gimli, Son of Gloin: the card on top of the Free Peoples draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothStormerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Gorgoroth Stormer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>.<br><b>Shadow:</b> If you have initiative, exert this minion to add a burden and make the Free Peoples player draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stormer");

		assertEquals("Gorgoroth Stormer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityExertsThisMinionToAddABurdenAndMakeTheFreePeoplesPlayerDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stormer = scn.GetShadowCard("stormer");
		var stormer2 = scn.GetShadowCard("stormer2");
		var card1 = scn.GetFreepsCard("card1");
		var card2 = scn.GetFreepsCard("card2");
		var card3 = scn.GetFreepsCard("card3");
		var drawn = scn.GetFreepsCard("drawn");

		scn.StartGame();
		scn.SetFreepsHand(card1, card2, card3);
		scn.MoveCardsToTopOfDeck(drawn);
		scn.MoveMinionsToTable(stormer, stormer2);
		assertInZone(Zone.DECK, drawn);
		assertEquals(drawn, scn.GetFreepsDrawDeck().getFirst());
		scn.FreepsPass();

		// Three cards in the Free Peoples hand: the Shadow has initiative.
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(stormer));
		assertEquals(0, scn.GetWoundsOn(stormer2));
		assertTrue(scn.ShadowActionAvailable(stormer));
		assertTrue(scn.ShadowActionAvailable(stormer2));

		scn.ShadowUseCardAction(stormer);

		// Only the copy used exerts.
		assertEquals(1, scn.GetWoundsOn(stormer));
		assertEquals(0, scn.GetWoundsOn(stormer2));
		assertEquals(1, scn.GetBurdens());
		assertEquals(4, scn.GetFreepsHandCount());
		assertInHand(drawn);

		// The draw has given the Free Peoples player four cards and the initiative: the other copy is not offered.
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(stormer2));
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileTheFreePeoplesPlayerHasFourCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stormer = scn.GetShadowCard("stormer");
		var card1 = scn.GetFreepsCard("card1");
		var card2 = scn.GetFreepsCard("card2");
		var card3 = scn.GetFreepsCard("card3");
		var card4 = scn.GetFreepsCard("card4");

		scn.StartGame();
		scn.SetFreepsHand(card1, card2, card3, card4);
		scn.MoveMinionsToTable(stormer);
		assertEquals(0, scn.GetWoundsOn(stormer));
		scn.FreepsPass();

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(stormer));

		// One card fewer, and the Shadow has initiative.
		scn.MoveCardsToDiscard(card4);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.ShadowActionAvailable(stormer));
	}

	@Test
	public void ShadowAbilityIsNotOfferedOnAnExhaustedCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stormer = scn.GetShadowCard("stormer");
		var stormer2 = scn.GetShadowCard("stormer2");

		scn.StartGame();
		scn.MoveMinionsToTable(stormer, stormer2);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(stormer, 1);
		assertEquals(1, scn.GetWoundsOn(stormer));
		assertEquals(0, scn.GetWoundsOn(stormer2));
		scn.FreepsPass();

		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(stormer));
		assertTrue(scn.ShadowActionAvailable(stormer2));
	}
}
