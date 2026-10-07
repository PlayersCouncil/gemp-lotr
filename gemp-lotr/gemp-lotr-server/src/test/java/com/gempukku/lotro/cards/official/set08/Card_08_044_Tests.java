package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_044_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "8_44");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, vitality 4
					put("first", "7_92");       // First Level: a [gondor] fortification, cost 1
					put("third", "7_125");      // Third Level: a [gondor] fortification, cost 1
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SixthLevelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Sixth Level
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Fortification</b>.<br><b>Regroup:</b> Exert a [gondor] Man to play a [gondor] fortification from your discard pile (limit 1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Sixth Level", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void SixthLevelTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}

	@Test
	public void RegroupAbilityAtItsLimitStillExertsAndPlaysNoFortification() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		var aragorn = scn.GetFreepsCard("aragorn");
		var first = scn.GetFreepsCard("first");
		var third = scn.GetFreepsCard("third");
		scn.MoveCardsToSupportArea(card);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDiscard(first, third);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals("false", scn.FreepsActionNoEffect(card));
		scn.FreepsUseCardAction(card);
		scn.FreepsChooseCardBPFromSelection(first);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(Zone.SUPPORT, first.getZone());
		scn.ShadowPass();

		// Limit 1: Aragorn can still exert and a fortification is still in the discard pile, so the ability
		// is still offered, flagged as doing nothing (Ketura on Q-0041).
		assertTrue(scn.FreepsActionAvailable(card));
		assertEquals("limit", scn.FreepsActionNoEffect(card));

		scn.FreepsUseCardAction(card);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(Zone.DISCARD, third.getZone());
	}
}
