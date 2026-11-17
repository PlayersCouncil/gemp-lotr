package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_192_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Free Peoples cards of three types, as the Shadow player holds his own copies of them.
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: a companion
					put("axe", "1_9");              // Dwarven Axe: a possession
					put("swordarm", "1_116");       // Swordarm of the White Tower: a Skirmish event
					// Shadow cards.
					put("skulker", "12_95");        // Orc Skulker: an [orc] minion
					put("beckoning", "12_83");      // The Beckoning Shadow: an [orc] condition; nothing below 5 burdens
					// Left in the draw deck, so that a card can be drawn.
					put("filler1", "12_95");
					put("filler2", "12_95");
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");         // Fangorn Glade: its text answers a move to it; the game starts there
					put("slopes", "12_192");
					// Never laid in these tests.
					put("site3", "11_234");
					put("site5", "18_138");
					put("site6", "12_187");
					put("site7", "13_185");
					put("site8", "11_230");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void SlopesofOrodruinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Slopes of Orodruin
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 1
		 * Direction: Right
		 * Game Text: <b>Mountain</b>. <b>Shadow:</b> Remove (2) and discard 2 Free Peoples cards from hand to draw a card.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("slopes");

		assertEquals("Slopes of Orodruin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
	}

	@Test
	public void ShadowAbilityRemovesTwoAndDiscardsTwoFreePeoplesCardsFromHandToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetShadowCard("aragorn");
		var axe = scn.GetShadowCard("axe");
		var swordarm = scn.GetShadowCard("swordarm");
		var skulker = scn.GetShadowCard("skulker");
		var beckoning = scn.GetShadowCard("beckoning");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("slopes"));

		var slopes = scn.GetCurrentSite();
		assertEquals("Slopes of Orodruin", slopes.getBlueprint().getTitle());
		scn.MoveCardsToHand(aragorn, axe, swordarm, skulker, beckoning);
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(aragorn, axe, swordarm, skulker, beckoning);
		assertEquals(5, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(4, scn.GetTwilight());
		int deck = scn.GetShadowDeckCount();
		assertTrue(scn.ShadowActionAvailable(slopes));

		scn.ShadowUseCardAction(slopes);
		// Free Peoples cards of each type the hand holds; no Shadow card; exactly two.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, axe, swordarm));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(skulker, beckoning));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(aragorn, swordarm);

		assertInDiscard(aragorn, swordarm);
		assertInHand(axe, skulker, beckoning);
		assertEquals(2, scn.GetShadowDiscardCount());
		// 4 - 2 removed.
		assertEquals(2, scn.GetTwilight());
		// Two discarded and one drawn.
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithOneTwilightInThePoolUntilThereAreTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetShadowCard("aragorn");
		var axe = scn.GetShadowCard("axe");
		var swordarm = scn.GetShadowCard("swordarm");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("slopes"));

		var slopes = scn.GetCurrentSite();
		assertEquals("Slopes of Orodruin", slopes.getBlueprint().getTitle());
		scn.MoveCardsToHand(aragorn, axe, swordarm);
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(aragorn, axe, swordarm);
		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(slopes));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(slopes));
	}

	@Test
	public void NotOfferedWithOneFreePeoplesCardInHandBesideShadowCardsUntilThereAreTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetShadowCard("axe");
		var swordarm = scn.GetShadowCard("swordarm");
		var skulker = scn.GetShadowCard("skulker");
		var beckoning = scn.GetShadowCard("beckoning");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("slopes"));

		var slopes = scn.GetCurrentSite();
		assertEquals("Slopes of Orodruin", slopes.getBlueprint().getTitle());
		scn.MoveCardsToHand(axe, skulker, beckoning);
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();

		// Three cards in hand, one of them Free Peoples.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(axe, skulker, beckoning);
		assertEquals(3, scn.GetShadowHandCount());
		assertFalse(scn.ShadowActionAvailable(slopes));

		scn.MoveCardsToHand(swordarm);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(slopes));
	}
}
