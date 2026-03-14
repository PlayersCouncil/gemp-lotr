package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_068_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "103_68");
					put("orc1", "7_188"); // Morgul Brute (Wraith Orc, twilight 2)
					put("orc2", "7_188"); // Second Morgul Brute
					put("guard", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulLegionsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Morgul Legions
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: The second time you play a [ringwraith] Orc each Shadow phase, you may hinder a Free Peoples possession.
		* 	Shadow: Spot a Nazgul to play any number of possessions on your [ringwraith] Orcs from your discard pile.
		*   Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Morgul Legions", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MorgulLegionsDoesNotTriggerOnFirstOrcPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var orc1 = scn.GetShadowCard("orc1");
		var orc2 = scn.GetShadowCard("orc2");
		scn.MoveCardsToSupportArea(card);
		scn.MoveCardsToHand(orc1, orc2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		// Play first Ringwraith Orc -- should NOT trigger Morgul Legions
		scn.ShadowPlayCard(orc1);
		// The errata added playedCardThisPhase requirement (min:2, max:2)
		// So only the second orc played triggers it
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}
}
