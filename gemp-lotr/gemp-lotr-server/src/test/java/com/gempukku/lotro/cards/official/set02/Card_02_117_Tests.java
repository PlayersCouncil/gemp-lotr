package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_117_Tests
{

	// Town Center is site 1; the rest of the path is that of FellowshipSites.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, not unique, home 2F; a skirmish ability never used
					// Goldberry, River-daughter: a unique [shire] ally with no race, home 2F; her texts need a river
					put("goldberry", "9_51");
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a [shire] companion
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a [dwarven] ally; a fellowship ability used only on purpose
					put("axe", "1_3");          // Axe Strike: a [dwarven] skirmish event, only ever a card in the draw deck
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site1", "2_117");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TownCenterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Town Center
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 1
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Fellowship:</b> Play a [shire] ally to draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Town Center", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void FellowshipActionPlaysAShireAllyFromHandToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bounder = scn.GetFreepsCard("bounder");
		var goldberry = scn.GetFreepsCard("goldberry");
		var merry = scn.GetFreepsCard("merry");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCardsToHand(bounder, goldberry, merry, grimir);

		scn.StartGame();

		var center = scn.GetCurrentSite();
		assertEquals("Town Center", center.getBlueprint().getTitle());
		assertInZone(Zone.HAND, bounder, goldberry, merry, grimir);
		assertEquals(4, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck >= 1);
		assertTrue(scn.FreepsActionAvailable(center));

		scn.FreepsUseCardAction(center);

		// A [shire] Hobbit ally and a [shire] ally of no race may be played; a [shire] companion and an ally of
		// another culture, each of which could be played from hand, may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bounder, goldberry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, grimir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(goldberry);

		assertInZone(Zone.SUPPORT, goldberry);
		assertInZone(Zone.HAND, bounder, merry, grimir);
		// 4 - Goldberry + 1
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoShireAllyInHandTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bounder = scn.GetFreepsCard("bounder");
		var goldberry = scn.GetFreepsCard("goldberry");
		var merry = scn.GetFreepsCard("merry");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCardsToHand(merry, grimir);

		scn.StartGame();

		// The [shire] allies are in the draw deck; the hand holds a [shire] companion and a [dwarven] ally, both
		// playable.
		assertInZone(Zone.DECK, bounder, goldberry);
		assertInZone(Zone.HAND, merry, grimir);
		assertTrue(scn.FreepsPlayAvailable(merry));
		assertTrue(scn.FreepsPlayAvailable(grimir));

		assertFalse(scn.FreepsActionAvailable(scn.GetCurrentSite()));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
