
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_V1_033_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("ttent", "101_33");
                    put("spear", "1_182");
                    put("ftentacle1", "2_58");
                    put("ftentacle2", "2_58");
                    put("ftentacle3", "2_58");
                }},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void ThrashingTentacleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Title: Thrashing Tentacle
		 * Side: Free Peoples
		 * Culture: moria
		 * Twilight Cost: 2
		 * Type: minion
		 * Subtype: Creature
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: Tentacle. This minion may not bear possessions and is discarded if not at a marsh.
		 * 	Shadow: Spot 3 [moria] creatures to play this minion from your discard pile.
		 */

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl ttent = scn.GetFreepsCard("ttent");

		assertFalse(ttent.getBlueprint().isUnique());
		assertTrue(scn.HasKeyword(ttent, Keyword.TENTACLE)); // test for keywords as needed
		assertEquals(2, ttent.getBlueprint().getTwilightCost());
		assertEquals(5, ttent.getBlueprint().getStrength());
		assertEquals(2, ttent.getBlueprint().getVitality());
		assertEquals(4, ttent.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies
		assertEquals(CardType.MINION, ttent.getBlueprint().getCardType());
		assertEquals(Race.CREATURE, ttent.getBlueprint().getRace());
		assertEquals(Culture.MORIA, ttent.getBlueprint().getCulture());
		assertEquals(Side.SHADOW, ttent.getBlueprint().getSide());
	}

	@Test
	public void ThrashingTentacleCannotBearPossessions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl ttent = scn.GetShadowCard("ttent");
		PhysicalCardImpl spear = scn.GetShadowCard("spear");
		scn.ShadowMoveCardToHand(ttent, spear);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.ApplyAdHocModifier(new KeywordModifier(null, Filters.siteNumber(2), Keyword.MARSH));
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(ttent);

		assertFalse(scn.ShadowActionAvailable("Goblin Spear"));
	}

	@Test
	public void ThrashingTentacleSelfDiscardsIfNotAtAMarsh() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl ttent = scn.GetShadowCard("ttent");
		scn.ShadowMoveCardToHand(ttent);

		scn.StartGame();
		scn.SetTwilight(2);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(ttent);

		assertEquals(1, scn.GetShadowDiscardCount());
	}

	@Test
	public void ThrashingTentaclePlaysFromDiscardIfThreeCreatures() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl ttent = scn.GetShadowCard("ttent");
		scn.ShadowMoveCardToHand(ttent);

		PhysicalCardImpl ftentacle1 = scn.GetShadowCard("ftentacle1");
		PhysicalCardImpl ftentacle2 = scn.GetShadowCard("ftentacle2");
		PhysicalCardImpl ftentacle3 = scn.GetShadowCard("ftentacle3");
		scn.ShadowMoveCardToHand(ftentacle1, ftentacle2, ftentacle3);
		scn.ShadowMoveCardToDiscard(ttent);

		scn.StartGame();
		scn.SetTwilight(16);
		scn.ApplyAdHocModifier(new KeywordModifier(null, Filters.siteNumber(2), Keyword.MARSH));
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(ftentacle1);
		//scn.ShadowDeclineOptionalTrigger();
		scn.ShadowPlayCard(ftentacle2);
		//scn.ShadowDeclineOptionalTrigger();

		assertFalse(scn.ShadowActionAvailable("Thrashing Tentacle"));
		scn.ShadowPlayCard(ftentacle3);
		//scn.ShadowDeclineOptionalTrigger();

		assertTrue(scn.ShadowActionAvailable("Thrashing Tentacle"));
		scn.ShadowUseCardAction(ttent);
		assertEquals(0, scn.GetShadowDiscardCount());
	}
}
