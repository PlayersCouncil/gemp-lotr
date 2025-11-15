package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_026_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blind", "103_24");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KeepHimBlindtoAllElseThatMovesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Keep Him Blind to All Else That Moves
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When you play this, heal each Free Peoples Man. 
		* 	Shadow: The Shadow player may discard this to reconcile their hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blind");

		assertEquals("Keep Him Blind to All Else That Moves", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void KeepHimBlindGrantsActionToShadowPlayer() throws DecisionResultInvalidException, CardNotFoundException {

		//Pre-game setup
		var scn = GetScenario();

		var blind = scn.GetFreepsCard("blind");
		scn.MoveCardsToSupportArea(blind);

		scn.StartGame();

		scn.SkipToPhase(Phase.SHADOW);
		
		assertTrue(scn.ShadowActionAvailable(blind));
	}
}
