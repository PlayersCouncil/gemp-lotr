
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_047_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("betrayed", "101_47");
                    put("orc", "1_271");
                }},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void ItBetrayedIsildurStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: It Betrayed Isildur
		* Side: Free Peoples
		* Culture: sauron
		* Twilight Cost: 1
		* Type: event
		* Subtype: Regroup
		* Game Text: Discard a [Sauron] Orc and spot 5 burdens to choose one: make the move limit for this turn -1; or make the Free Peoples player choose to move again (if the move limit allows).
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl betrayed = scn.GetFreepsCard("betrayed");

		assertFalse(betrayed.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, betrayed.getBlueprint().getSide());
		assertEquals(Culture.SAURON, betrayed.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, betrayed.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, betrayed.getBlueprint().getRace());
        assertTrue(scn.hasTimeword(betrayed, Timeword.REGROUP)); // test for keywords as needed
		assertEquals(1, betrayed.getBlueprint().getTwilightCost());
		//assertEquals(, betrayed.getBlueprint().getStrength());
		//assertEquals(, betrayed.getBlueprint().getVitality());
		//assertEquals(, betrayed.getBlueprint().getResistance());
		//assertEquals(Signet., betrayed.getBlueprint().getSignet());
		//assertEquals(, betrayed.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void RequiresASauronOrcAndFiveBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl betrayed = scn.GetShadowCard("betrayed");
		PhysicalCardImpl orc = scn.GetShadowCard("orc");
		scn.ShadowMoveCardToHand(betrayed, orc);

		//Max out the move limit so we don't have to juggle play back and forth
		scn.ApplyAdHocModifier(new MoveLimitModifier(null, 10));

		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPassCurrentPhaseAction();
		assertFalse(scn.ShadowPlayAvailable(betrayed));
		scn.ShadowPassCurrentPhaseAction();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();

		scn.AddBurdens(4); // Now at 5 with the starting bid

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPassCurrentPhaseAction();
		assertFalse(scn.ShadowPlayAvailable(betrayed));
		scn.ShadowPassCurrentPhaseAction();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();

		scn.ShadowMoveCharToTable(orc);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowPlayAvailable(betrayed));

	}

	@Test
	public void DiscardsOrcToMakeMoveLimitMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl betrayed = scn.GetShadowCard("betrayed");
		PhysicalCardImpl orc = scn.GetShadowCard("orc");
		scn.ShadowMoveCardToHand(betrayed);
		scn.ShadowMoveCharToTable(orc);

		scn.StartGame();

		scn.AddBurdens(4); // Now at 5 with the starting bid

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPassCurrentPhaseAction();
		assertEquals(2, scn.GetMoveLimit()); // 2 moves: 1 in fellowship, 1 in regroup
		assertTrue(scn.ShadowPlayAvailable(betrayed));
		assertEquals(Zone.SHADOW_CHARACTERS, orc.getZone());

		scn.ShadowPlayCard(betrayed);
		assertTrue(scn.ShadowDecisionAvailable("choose"));
		scn.ShadowChooseMultipleChoiceOption("move limit -1");

		assertEquals(1, scn.GetMoveLimit());
		assertEquals(Zone.DISCARD, orc.getZone());

		scn.PassCurrentPhaseActions();

		// Move prompt entirely skipped
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
	}

	@Test
	public void DiscardsOrcToForceMovementIfAllowed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl betrayed = scn.GetShadowCard("betrayed");
		PhysicalCardImpl orc = scn.GetShadowCard("orc");
		scn.ShadowMoveCardToHand(betrayed);
		scn.ShadowMoveCharToTable(orc);

		scn.StartGame();

		scn.AddBurdens(4); // Now at 5 with the starting bid

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPassCurrentPhaseAction();
		assertEquals(2, scn.GetMoveLimit()); // 2 moves: 1 in fellowship, 1 in regroup
		assertTrue(scn.ShadowPlayAvailable(betrayed));
		assertEquals(Zone.SHADOW_CHARACTERS, orc.getZone());

		scn.ShadowPlayCard(betrayed);
		assertTrue(scn.ShadowDecisionAvailable("choose"));
		scn.ShadowChooseMultipleChoiceOption("move again");

		assertEquals(2, scn.GetMoveLimit());
		assertEquals(Zone.DISCARD, orc.getZone());

		scn.PassCurrentPhaseActions();

		// Move prompt entirely skipped
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
	}
}
