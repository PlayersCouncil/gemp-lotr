package com.gempukku.lotro.cards.unofficial.pc.errata.set05;

import com.gempukku.lotro.cards.GenericCardTestHelper;
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

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("ladder", "55_57");
					put("troop", "1_143");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
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
		assertTrue(scn.hasKeyword(card, Keyword.MACHINE));
		assertTrue(scn.hasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ScalingLadderExertsUruksToAddTokensLimit2() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var ladder = scn.GetShadowCard("ladder");
		var troop = scn.GetShadowCard("troop");
		scn.ShadowMoveCardToSupportArea(ladder);
		scn.ShadowMoveCardToHand(troop);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowActionAvailable(ladder));
		scn.ShadowPlayCard(troop);

		assertEquals(0, scn.GetCultureTokensOn(ladder));
		assertEquals(0, scn.getWounds(troop));
		assertTrue(scn.ShadowActionAvailable(ladder));

		scn.ShadowUseCardAction(ladder);
		assertEquals(1, scn.GetCultureTokensOn(ladder));
		assertEquals(1, scn.getWounds(troop));
		assertTrue(scn.ShadowActionAvailable(ladder));

		scn.ShadowUseCardAction(ladder);
		assertEquals(2, scn.GetCultureTokensOn(ladder));
		assertEquals(2, scn.getWounds(troop));

		assertFalse(scn.ShadowActionAvailable(ladder));
	}
}
