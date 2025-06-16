package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_025_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foreman", "102_25");
					put("isenorc", "3_58");

					put("comp2", "1_7");
					put("comp3", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsengardForemanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Isengard Foreman
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: When you play this minion, you may play an [isengard] Orc from your discard pile; it is strength +1 until the regroup phase.
		* 	Regroup: Discard this minion to add two threats.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("foreman");

		assertEquals("Isengard Foreman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsengardForemanPlaysIsenorcFromDiscardOnPlayAndPumpsPlus1UntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var foreman = scn.GetShadowCard("foreman");
		var isenorc = scn.GetShadowCard("isenorc");
		scn.MoveCardsToDiscard(isenorc);
		scn.MoveCardsToHand(foreman);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(foreman));
		assertEquals(Zone.DISCARD, isenorc.getZone());
		scn.ShadowPlayCard(foreman);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(Zone.SHADOW_CHARACTERS, isenorc.getZone());
		//Base 7 +1 from the foreman
		assertEquals(8, scn.GetStrength(isenorc));

		scn.SkipToAssignments();
		assertEquals(8, scn.GetStrength(isenorc));
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(7, scn.GetStrength(isenorc));
	}

	@Test
	public void IsengardForemanRegroupActionSelfDiscardsToAdd2Threats() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var foreman = scn.GetShadowCard("foreman");

		scn.MoveCompanionToTable("comp2", "comp3");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		scn.MoveMinionsToTable(foreman);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetThreats());
		assertEquals(Zone.SHADOW_CHARACTERS, foreman.getZone());
		assertTrue(scn.ShadowActionAvailable(foreman));

		scn.ShadowUseCardAction(foreman);
		assertEquals(2, scn.GetThreats());
		assertEquals(Zone.DISCARD, foreman.getZone());
	}
}
