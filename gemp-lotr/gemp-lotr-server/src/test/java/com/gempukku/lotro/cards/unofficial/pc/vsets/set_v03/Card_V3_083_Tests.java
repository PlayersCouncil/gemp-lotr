package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_083_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aid", "103_83");

					put("uruk", "1_151");
					put("power", "51_136");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GondorCallsForAidStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Gondor Calls For Aid!
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Beacon. To play, hinder 4 beacons.
		* 	This cannot be discarded or hindered.
		* 	Your [Gondor] Men are considered [Rohan] Men.  Your [Rohan] Men are considered [Gondor] Men.
		* 	Maneuver: Hinder a beacon to play an item from your discard pile on your Man.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aid");

		assertEquals("Gondor Calls For Aid!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.BEACON));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void GondorCallsForAidCannotBeDiscardedOrHindered() throws DecisionResultInvalidException, CardNotFoundException {

		//Pre-game setup
		var scn = GetScenario();

		var aid = scn.GetFreepsCard("aid");
		scn.MoveCardsToSupportArea(aid);

		var uruk = scn.GetShadowCard("uruk");
		//Errata'd Saruman's Power, which discards 1 condition and then hinders all others
		var power = scn.GetShadowCard("power");
		scn.MoveMinionsToTable(uruk);
		scn.MoveCardsToHand(power);

		scn.StartGame();
		scn.FreepsPass();

		assertInZone(Zone.SUPPORT, aid);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(power));
		
		scn.ShadowPlayCard(power);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SUPPORT, aid);
	}
}
