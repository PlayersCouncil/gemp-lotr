package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_17_100_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("caves", "17_100");

					put("sauron", "9_48");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IntotheCavesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Into the Caves
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 3 [rohan] companions.
		 * <b>Assignment:</b> Assign a minion to Frodo to make that minion lose all game text keywords of your choice
		 * and unable to gain game text keywords until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("caves");

		assertEquals("Into the Caves", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IntotheCavesAssignsFrodoToMinionToRemoveKeywords() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var caves = scn.GetFreepsCard("caves");
		scn.MoveCardsToSupportArea(caves);

		var sauron = scn.GetShadowCard("sauron");
		scn.MoveMinionsToTable(sauron);

		scn.StartGame();

		scn.SkipToPhase(Phase.ARCHERY);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.HasKeyword(sauron, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(sauron, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(sauron, Keyword.FIERCE));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(sauron));
		assertTrue(scn.FreepsActionAvailable(caves));

		scn.FreepsUseCardAction(caves);
		assertTrue(scn.IsCharAssigned(frodo));
		assertTrue(scn.IsCharAssigned(sauron));

		assertTrue(scn.FreepsDecisionAvailable("keyword"));
		scn.FreepsChooseOption("DAMAGE");
		assertFalse(scn.HasKeyword(sauron, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(sauron, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(sauron, Keyword.FIERCE));
		scn.FreepsChooseYes();
		scn.FreepsChooseOption("ENDURING");
		assertFalse(scn.HasKeyword(sauron, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(sauron, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(sauron, Keyword.FIERCE));
		scn.FreepsChooseNo();
		assertFalse(scn.HasKeyword(sauron, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(sauron, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(sauron, Keyword.FIERCE));

		assertTrue(scn.ShadowAnyDecisionsAvailable());
	}
}
