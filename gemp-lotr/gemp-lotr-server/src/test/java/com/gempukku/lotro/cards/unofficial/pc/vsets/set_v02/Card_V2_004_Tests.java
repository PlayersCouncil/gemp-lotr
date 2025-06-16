package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horsemen", "102_4");
					put("band", "4_4");
					put("chief", "4_20");
					put("club", "4_36");
					put("hides", "4_19");

					put("card1", "1_3");
					put("card2", "1_4");
					put("card3", "1_5");
					put("card4", "1_6");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HorsemenTookYourLandsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Horsemen Took Your Lands
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Play a non-unique [Dunland] minion from your discard pile. If you have initiative, you may play a [dunland] possession on that minion from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horsemen");

		assertEquals("Horsemen Took Your Lands", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void HorsemenTookYourLandsPlaysNonUniqueDunlendingFromDiscardWithPossessionWithInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var horsemen = scn.GetShadowCard("horsemen");
		var band = scn.GetShadowCard("band");
		var chief = scn.GetShadowCard("chief");
		var club = scn.GetShadowCard("club");
		var hides = scn.GetShadowCard("hides");
		scn.MoveCardsToHand(horsemen);
		scn.MoveCardsToDiscard(band, chief, club, hides);

		var card1 = scn.GetFreepsCard("card1");
		var card2 = scn.GetFreepsCard("card2");
		var card3 = scn.GetFreepsCard("card3");
		var card4 = scn.GetFreepsCard("card4");
		scn.MoveCardsToHand(card1, card2, card3);
		scn.MoveCardsToDiscard(card4);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Zone.DISCARD, band.getZone());
		assertEquals(Zone.DISCARD, chief.getZone());
		assertEquals(Zone.DISCARD, club.getZone());
		assertEquals(Zone.DISCARD, hides.getZone());
		assertTrue(scn.ShadowPlayAvailable(horsemen));
		scn.ShadowPlayCard(horsemen);
		assertEquals(Zone.SHADOW_CHARACTERS, band.getZone());
		assertEquals(Zone.DISCARD, chief.getZone());
		assertEquals(Zone.ATTACHED, club.getZone());
		assertEquals(band, club.getAttachedTo());
		assertEquals(Zone.DISCARD, hides.getZone());
	}

	@Test
	public void HorsemenTookYourLandsPlaysNonUniqueDunlendingFromDiscardWithNoPossessionWithNoInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var horsemen = scn.GetShadowCard("horsemen");
		var band = scn.GetShadowCard("band");
		var chief = scn.GetShadowCard("chief");
		var club = scn.GetShadowCard("club");
		var hides = scn.GetShadowCard("hides");
		scn.MoveCardsToHand(horsemen);
		scn.MoveCardsToDiscard(band, chief, club, hides);

		var card1 = scn.GetFreepsCard("card1");
		var card2 = scn.GetFreepsCard("card2");
		var card3 = scn.GetFreepsCard("card3");
		var card4 = scn.GetFreepsCard("card4");
		scn.MoveCardsToHand(card1, card2, card3, card4);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Zone.DISCARD, band.getZone());
		assertEquals(Zone.DISCARD, chief.getZone());
		assertEquals(Zone.DISCARD, club.getZone());
		assertEquals(Zone.DISCARD, hides.getZone());
		assertTrue(scn.ShadowPlayAvailable(horsemen));
		scn.ShadowPlayCard(horsemen);
		assertEquals(Zone.SHADOW_CHARACTERS, band.getZone());
		assertEquals(Zone.DISCARD, chief.getZone());
		assertEquals(Zone.DISCARD, club.getZone());
		assertEquals(Zone.DISCARD, hides.getZone());
	}
}
