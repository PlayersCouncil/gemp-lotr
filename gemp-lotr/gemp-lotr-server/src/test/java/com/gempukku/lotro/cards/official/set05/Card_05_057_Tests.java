package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_05_057_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ladder", "5_57");
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
		 * Game Text: <b>Machine</b>. Plays to your support area.<br><b>Shadow:</b> Exert an Uruk-hai to place an [isengard] token on this card.<br><b>Skirmish:</b> Spot an [isengard] token here to heal an Uruk-hai. Discard this condition.
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
	public void ScalingLadderExertsUruksToAddTokens() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var ladder = scn.GetShadowCard("ladder");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(ladder);
		scn.MoveCardsToHand(troop);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowActionAvailable(ladder));
		scn.ShadowPlayCard(troop);

		assertEquals(0, scn.GetCultureTokensOn(ladder));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowActionAvailable(ladder));

		scn.ShadowUseCardAction(ladder);
		assertEquals(1, scn.GetCultureTokensOn(ladder));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowActionAvailable(ladder));

		scn.ShadowUseCardAction(ladder);
		assertEquals(2, scn.GetCultureTokensOn(ladder));
		assertEquals(2, scn.GetWoundsOn(troop));

		assertTrue(scn.ShadowActionAvailable(ladder));
	}
}
