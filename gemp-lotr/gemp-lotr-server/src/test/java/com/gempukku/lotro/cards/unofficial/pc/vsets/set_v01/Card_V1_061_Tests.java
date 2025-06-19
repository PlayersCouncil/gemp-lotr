package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_061_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("aragorn", "1_89");
					put("orophin", "1_56");
					put("uruviel", "1_67");
					put("pathfinder", "1_110");

					put("runner", "1_178");

				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_337");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "101_61");
					put("site7", "3_118"); //NOT Anduin Confluence!  lol
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LorienThroneRoomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Lorien Throne Room
		 * Unique: False
		 * Side:
		 * Culture:
		 * Shadow Number: 3
		 * Type: Sanctuary
		 * Subtype:
		 * Site Number: 6
		 * Game Text: Forest. Sanctuary. Each time a companion exerts here, you may exert an [elven] ally to heal that companion (limit once per phase).
		 */

		var scn = GetScenario();
		var card = scn.GetFreepsSite(6);

		assertEquals("Lorien Throne Room", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenCompanionsExertAnElvenAllyMayExertToHealThatCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(aragorn, orophin);

		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(runner);

		//Max out the move limit so we don't have to juggle play back and forth
		scn.ApplyAdHocModifier(new MoveLimitModifier(null, 10));

		scn.StartGame();

		scn.SkipToSite(5);

		scn.MoveMinionsToTable(runner);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(scn.GetShadowSite(6), scn.GetCurrentSite());

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(orophin));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		//Limit once per phase
		scn.ShadowPassCurrentPhaseAction();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
	}

	@Test
	public void UruvielDoesNotCopyLorienThroneRoom() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var uruviel = scn.GetFreepsCard("uruviel");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		scn.MoveCompanionsToTable(aragorn, uruviel);
		scn.MoveCardsToHand(pathfinder);

		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(runner);

		//Max out the move limit so we don't have to juggle play back and forth
		scn.ApplyAdHocModifier(new MoveLimitModifier(null, 10));

		scn.StartGame();

		// 1 -> 3
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();

		// 3 -> 4
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();

		// 4 -> 5
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();

		// 5 -> 6
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(5, (long)scn.GetCurrentSite().getSiteNumber());
		scn.FreepsPlayCard(pathfinder); //ensure that it counts as "ours"
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();
		assertEquals("Lorien Throne Room", scn.GetCurrentSite().getBlueprint().getTitle());

		// 6 -> 7
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();
		assertEquals(7, (long)scn.GetCurrentSite().getSiteNumber());

		scn.MoveMinionsToTable(runner);

		assertEquals(VirtualTableScenario.P1, scn.GetFreepsSite(6).getOwner());
		assertEquals(7, scn.GetCurrentSite().getSiteNumber().intValue());

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
	}
}
