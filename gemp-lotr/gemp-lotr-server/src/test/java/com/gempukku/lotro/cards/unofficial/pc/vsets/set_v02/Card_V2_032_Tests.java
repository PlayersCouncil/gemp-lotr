package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_032_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("alliance", "102_32");
					put("uruk", "2_47");
					put("orc", "1_261");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UneasyAllianceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Uneasy Alliance
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Search.
		* 	While you can spot an [Isengard] tracker and a [Sauron] tracker, trackers are strength +1.
		* 	Shadow: Discard a [sauron] tracker from hand to play an [isengard] tracker from your discard pile. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("alliance");

		assertEquals("Uneasy Alliance", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SEARCH));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void UneasyAlliancePumpsIsengardAndSauronTrackersIfOneOfEach() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var alliance = scn.GetShadowCard("alliance");
		var uruk = scn.GetShadowCard("uruk");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToHand(alliance, orc);
		scn.MoveMinionsToTable(uruk);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(7, scn.GetStrength(uruk));
		scn.ShadowPlayCard(alliance);
		assertEquals(7, scn.GetStrength(uruk));

		scn.ShadowPlayCard(orc);
		assertTrue(scn.HasKeyword(uruk, Keyword.TRACKER));
		assertEquals(8, scn.GetStrength(uruk));
		assertEquals(5, orc.getBlueprint().getStrength());
		assertTrue(scn.HasKeyword(orc, Keyword.TRACKER));
		assertEquals(6, scn.GetStrength(orc));
	}

	@Test
	public void UneasyAllianceTradesSauronTrackerInHandForIsengardTrackerInDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var alliance = scn.GetShadowCard("alliance");
		var uruk = scn.GetShadowCard("uruk");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToSupportArea(alliance);
		scn.MoveCardsToHand(orc);
		scn.MoveCardsToDiscard(uruk);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Zone.HAND, orc.getZone());
		assertEquals(Zone.DISCARD, uruk.getZone());
		assertEquals(Zone.SUPPORT, alliance.getZone());
		assertTrue(orc.getBlueprint().hasKeyword(Keyword.TRACKER));
		assertTrue(uruk.getBlueprint().hasKeyword(Keyword.TRACKER));
		assertTrue(scn.ShadowActionAvailable(alliance));

		scn.ShadowUseCardAction(alliance);

		assertEquals(Zone.DISCARD, orc.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, uruk.getZone());
		assertEquals(Zone.DISCARD, alliance.getZone());
	}
}
