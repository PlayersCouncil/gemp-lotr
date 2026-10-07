package com.gempukku.lotro.cards.unofficial.pc.errata.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_05_057_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ladder", "55_57");
					put("troop", "1_143");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ScalingLadderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Scaling Ladder
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Game Text: <b>Machine</b>. Plays to your support area.
		 * <b>Shadow:</b> Exert an Uruk-hai to place an [isengard] token on this card (limit 2 per phase).
		 * <b>Skirmish:</b> For each [isengard] token here, heal an Uruk-hai.  Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ladder");

		assertEquals("Scaling Ladder", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.MACHINE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ScalingLadderExertsUruksToAddTokensLimit2() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var ladder = scn.GetShadowCard("ladder");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(ladder);
		scn.MoveCardsToHand(troop);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertFalse(scn.ShadowActionAvailable(ladder));
		scn.ShadowPlayCard(troop);

		assertEquals(0, scn.GetCultureTokensOn(ladder));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowActionAvailable(ladder));
		assertEquals("false", scn.ShadowActionNoEffect(ladder));

		scn.ShadowUseCardAction(ladder);
		assertEquals(1, scn.GetCultureTokensOn(ladder));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowActionAvailable(ladder));

		scn.ShadowUseCardAction(ladder);
		assertEquals(2, scn.GetCultureTokensOn(ladder));
		assertEquals(2, scn.GetWoundsOn(troop));

		// At its limit the ability is still offered, as the Uruk-hai can still exert, and is flagged as
		// doing nothing (Ketura on Q-0041).
		assertTrue(scn.ShadowActionAvailable(ladder));
		assertEquals("limit", scn.ShadowActionNoEffect(ladder));

		scn.ShadowUseCardAction(ladder);
		// The Uruk-hai exerts and no token is placed.
		assertEquals(3, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetCultureTokensOn(ladder));

		// Vitality 4 with 3 wounds: the Uruk-hai cannot pay again.
		assertFalse(scn.ShadowActionAvailable(ladder));
	}
}
