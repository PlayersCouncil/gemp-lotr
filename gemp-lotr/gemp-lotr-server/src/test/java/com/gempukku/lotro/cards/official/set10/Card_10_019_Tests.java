package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_019_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("sprang", "10_19");
					put("gollum", "9_28");

					put("sword", "1_299");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void ADarkShapeSprangStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: A Dark Shape Sprang
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If an opponent plays a possession on a companion, play Gollum at twilight cost -2 from your discard pile or hand to suspend the current phase. Begin a skirmish phase involving Gollum and that companion. When it ends, resume the suspended phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sprang");

		assertEquals("A Dark Shape Sprang", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.hasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ADarkShapeSprangTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sprang = scn.GetShadowCard("sprang");
		var gollum = scn.GetShadowCard("gollum");
		scn.ShadowMoveCardToHand(sprang, gollum);

		var frodo = scn.GetRingBearer();
		var sword = scn.GetFreepsCard("sword");
		scn.FreepsMoveCardToHand(sword);

		scn.StartGame();

		scn.SetTwilight(9);
		scn.FreepsPlayCard(sword);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(Zone.HAND, gollum.getZone());
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(Zone.SHADOW_CHARACTERS, gollum.getZone());
		//9 start, +1 from sword = 10; gollum costs 2 + 2 roaming -2 discount
		assertEquals(8, scn.GetTwilight());

		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPassCurrentPhaseAction();
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());

		assertEquals(1, scn.GetWoundsOn(gollum)); //lost skirmish
	}
}
