package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_130_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_130");
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man; stronger and fierce at 2 burdens
					put("axeman2", "4_224");
					put("aragorn", "1_89");     // with Frodo, two companions: room for two threats
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DarkTidingsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Dark Tidings
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Regroup:</b> Spot 2 [raider] Men and remove (3) to add a threat (limit 1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Dark Tidings", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void DarkTidingsTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}

	@Test
	public void RegroupAbilityAtItsLimitIsStillOfferedAndPaidForAndAddsNoThreat() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetShadowCard("card");
		scn.MoveCardsToSupportArea(card);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("axeman"), scn.GetShadowCard("axeman2"));
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(6);
		scn.FreepsPass();

		assertEquals("false", scn.ShadowActionNoEffect(card));
		scn.ShadowUseCardAction(card);
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		scn.FreepsPass();

		// Limit 1: (3) can still be removed, so the ability is still offered, flagged as doing nothing
		// (Ketura on Q-0041).
		assertTrue(scn.ShadowActionAvailable(card));
		assertEquals("limit", scn.ShadowActionNoEffect(card));

		scn.ShadowUseCardAction(card);
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetThreatLimit());
	}
}
