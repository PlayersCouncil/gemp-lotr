package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_050_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "103_50");
					put("initiate", "103_47");   // Desert Wind Initiate - Ambush (3)
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesertWindWhisperStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Desert Wind Whisper
		 * Unique: true
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 15
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. Tracker.
		* 	When you play this minion, remove (4) or hinder this minion.
		* 	Each minion with ambush (X) is strength +X.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Desert Wind Whisper", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(15, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void DesertWindWhisperRemoveTwilightTaxIs4() throws DecisionResultInvalidException, CardNotFoundException {
		// Errata changes the tax from Remove (3) to Remove (4)
		var scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(card);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		int twilightBefore = scn.GetTwilight();

		scn.ShadowPlayCard(card);
		scn.ShadowChoose("Remove");

		// 5 to play + 2 roaming + 4 tax = 11 total
		assertEquals(twilightBefore - 11, scn.GetTwilight());
		assertFalse(scn.IsHindered(card));
	}
}
