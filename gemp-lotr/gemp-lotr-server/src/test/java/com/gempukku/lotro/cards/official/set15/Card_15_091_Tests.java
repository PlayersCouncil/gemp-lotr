package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_091_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "15_91");
					// put other cards in here as needed for the test case
					// Tracking Uruk: [uruk-hai] Uruk-hai; hunter 1 is its whole text
					put("tracking", "15_171");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RavagingWildManStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Ravaging Wild Man
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While you can spot a hunter, this minion gains <b>hunter 1</b>.<br>Each time this minion wins a skirmish, you may exert 2 [men] Men to discard a follower.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Ravaging Wild Man", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ItSpotsItselfAndKeepsHunter1WhenTheOtherHunterLeavesPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wildman = scn.GetShadowCard("card");
		var tracking = scn.GetShadowCard("tracking");

		scn.StartGame();
		// Alone, it has no hunter to spot.
		scn.MoveMinionsToTable(wildman);
		assertEquals(0, scn.GetKeywordCount(wildman, Keyword.HUNTER));

		scn.MoveMinionsToTable(tracking);
		assertEquals(1, scn.GetKeywordCount(wildman, Keyword.HUNTER));

		// The other hunter goes.  Hunter by its own text, the Wild Man is itself a hunter it can spot, and keeps
		// hunter 1 (Q-0290: "A, yeah, same deal" as Sentry Uruk 15_170, Q-0276).
		scn.MoveCardsToDiscard(tracking);
		assertEquals(1, scn.GetKeywordCount(wildman, Keyword.HUNTER));
		assertTrue(scn.HasKeyword(wildman, Keyword.HUNTER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void RavagingWildManTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(4, scn.GetTwilight());
	}
}
