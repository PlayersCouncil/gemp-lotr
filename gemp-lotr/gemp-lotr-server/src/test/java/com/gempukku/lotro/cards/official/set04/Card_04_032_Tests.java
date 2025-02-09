package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_032_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("ravage", "4_32");
					put("brigand", "4_10");
					put("weary", "4_212");

					put("sword", "1_299");
					put("coat", "2_105");
					put("stone", "1_314");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void RavagetheDefeatedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Ravage the Defeated
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: 
		 * Game Text: <b>Response:</b> If your [dunland] Man wins a skirmish, discard all Free Peoples cards borne by the companion or ally he was skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ravage");

		assertEquals("Ravage the Defeated", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.hasTimeword(card, Timeword.RESPONSE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RavagetheDefeatedDiscardsFreepsCardsAttachedToLoser() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		//A freeps possession, artifact, and condition
		var sword = scn.GetFreepsCard("sword");
		var coat = scn.GetFreepsCard("coat");
		var stone = scn.GetFreepsCard("stone");
		var frodo = scn.GetRingBearer();
		scn.AttachCardsTo(frodo, sword, coat, stone);

		var ravage = scn.GetShadowCard("ravage");
		var brigand = scn.GetShadowCard("brigand");
		var weary = scn.GetShadowCard("weary");
		scn.ShadowMoveCardToHand(ravage);
		scn.AttachCardsTo(frodo, weary);
		scn.ShadowMoveCharToTable(brigand);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, brigand);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(Zone.ATTACHED, sword.getZone());
		assertEquals(frodo, sword.getAttachedTo());
		assertEquals(Zone.ATTACHED, coat.getZone());
		assertEquals(frodo, coat.getAttachedTo());
		assertEquals(Zone.ATTACHED, stone.getZone());
		assertEquals(frodo, stone.getAttachedTo());
		assertEquals(Zone.ATTACHED, weary.getZone());
		assertEquals(frodo, weary.getAttachedTo());

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPassCurrentPhaseAction();

		scn.FreepsDeclineOptionalTrigger(); //One Ring converting wound on frodo
		assertTrue(scn.ShadowPlayAvailable(ravage));
		scn.ShadowPlayCard(ravage);

		assertEquals(Zone.DISCARD, sword.getZone());
		assertEquals(Zone.DISCARD, coat.getZone());
		assertEquals(Zone.DISCARD, stone.getZone());
		assertEquals(Zone.ATTACHED, weary.getZone());
		assertEquals(frodo, weary.getAttachedTo());
	}
}
