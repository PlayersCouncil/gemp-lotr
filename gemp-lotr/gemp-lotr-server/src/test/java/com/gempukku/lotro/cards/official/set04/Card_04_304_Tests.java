package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_304_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "4_304");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GetOnandGetAwayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Get On and Get Away
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Add 2 burdens to play the fellowship's next 2 sites (replacing opponent's sites if necessary).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Get On and Get Away", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AddsTwoBurdensToPlayTheFellowshipsNextTwoSites() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var getaway = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(getaway);

		scn.StartGame();

		var site2 = scn.GetFreepsSite(2);
		var site3 = scn.GetFreepsSite(3);
		var site4 = scn.GetFreepsSite(4);
		// Only site 1 is on the adventure path so far.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(2));
		assertNull(scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, site2, site3, site4);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsPlayAvailable(getaway));

		scn.FreepsPlayCard(getaway);

		assertEquals(2, scn.GetBurdens());
		// The fellowship's next 2 sites are the player's own sites 2 and 3, and no further.
		assertEquals(site2, scn.GetSite(2));
		assertEquals(site3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_PATH, site2, site3);
		assertNull(scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, site4);
		// The sites are played, and the fellowship has not moved.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertInDiscard(getaway);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
