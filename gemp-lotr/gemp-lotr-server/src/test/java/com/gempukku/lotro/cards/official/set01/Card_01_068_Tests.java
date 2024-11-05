package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_068_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("arrows", "1_68");
					put("galadriel", "1_45");
					put("legolas", "1_50");

					put("scout", "1_191");
					put("runner", "1_178");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void TheWhiteArrowsofLorienStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The White Arrows of Lórien
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Tale.</b> Bearer must be an Elf companion.
		 * 	Archery: If bearer is an archer, exert bearer to make an opponent discard 2 cards at random from hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arrows");

		assertEquals("The White Arrows of Lórien", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.TALE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhiteArrowsPlaysOnElfCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");
		var legolas = scn.GetFreepsCard("legolas");
		var galadriel = scn.GetFreepsCard("galadriel");
		scn.FreepsMoveCharToTable(legolas, galadriel);
		scn.FreepsMoveCardToHand(arrows);

		scn.StartGame();
		scn.FreepsPlayCard(arrows);

		assertEquals(Zone.ATTACHED, arrows.getZone());
		//Should have automatically gone to Legolas and not had galadriel as a valid choice, where Gemp would ask the user to decide
		assertTrue(scn.IsAttachedTo(arrows, legolas));
	}

	@Test
	public void WhiteArrowsExertsBearerToMakeShadowDiscard2CardsFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");
		var legolas = scn.GetFreepsCard("legolas");
		scn.FreepsMoveCharToTable(legolas);
		scn.FreepsAttachCardsTo(legolas, arrows);

		var scout = scn.GetShadowCard("scout");
		scn.ShadowMoveCardToHand("legolas", "galadriel", "arrows");
		scn.ShadowMoveCharToTable("scout", "runner");

		scn.StartGame();

		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.FreepsActionAvailable(arrows));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		scn.FreepsUseCardAction(arrows);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDiscardCount());
	}

}
