package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_079_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("resistance", "2_79");
					put("twigul", "2_82");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void ResistanceBecomesbrUnbearableStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Resistance Becomes Unbearable
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert a twilight Nazgûl to exert the Ring-bearer. If the Ring-bearer is then exhausted, he puts on The One Ring until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("resistance");

		assertEquals("Resistance Becomes Unbearable", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.hasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ResistanceBecomesbrUnbearablePutsOnRingWhenRBWasAlreadyExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var resistance = scn.GetShadowCard("resistance");
		var twigul = scn.GetShadowCard("twigul");
		scn.ShadowMoveCardToHand(resistance);
		scn.ShadowMoveCharToTable(twigul);

		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(twigul));
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.ShadowPlayAvailable(resistance));

		scn.ShadowPlayCard(resistance);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(twigul));
		assertTrue(scn.RBWearingOneRing());
	}
}
