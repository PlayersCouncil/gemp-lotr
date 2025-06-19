package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_040_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "103_40");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BladetuskRearguardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Bladetusk Rearguard
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Artifact
		 * Subtype: Support area
		 * Strength: 6
		 * Vitality: 3
		 * Game Text: Regroup: Stack a Southron from play here.
		* 	Maneuver: Spot a mounted Southron and remove (4) to make this artifact a <b>fierce</b> mounted Southron minion until the end of the turn that is strength +3 and <b>ambush (1)</b> for each Southron stacked on it. 
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Bladetusk Rearguard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void BladetuskRearguardTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);
		scn.MoveCompanionsToTable(card);
		scn.MoveCardsToSupportArea(card);
		scn.MoveCardsToDiscard(card);
		scn.MoveCardsToTopOfDeck(card);

		//var card = scn.GetShadowCard("card");
		scn.MoveCardsToHand(card);
		scn.MoveMinionsToTable(card);
		scn.MoveCardsToSupportArea(card);
		scn.MoveCardsToDiscard(card);
		scn.MoveCardsToTopOfDeck(card);

		scn.StartGame();
		
		assertFalse(true);
	}
}
