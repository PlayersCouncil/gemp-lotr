package com.gempukku.lotro.cards.unofficial.pc.errata.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_15_112_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<String, String>()
				{{
					put("troll", "65_112");
					put("orc1", "13_118");
					put("orc2", "13_118");
					put("orc3", "13_118");
					put("orc4", "13_118");
					put("orc5", "13_118");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MountaintrollStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 15
		* Title: Mountain-troll
		* Side: Free Peoples
		* Culture: Orc
		* Twilight Cost: 8
		* Type: minion
		* Subtype: Troll
		* Strength: 13
		* Vitality: 4
		* Site Number: 5
		* Game Text: When you play this minion, you may discard 5 [orc] minions from play to make it strength +10 and
		 * <b>fierce</b> until the end of the turn.
		 * Shadow: Remove (2) to play an [orc] Orc from your discard pile. It comes into play exhausted.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl troll = scn.GetFreepsCard("troll");

		assertFalse(troll.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, troll.getBlueprint().getSide());
		assertEquals(Culture.ORC, troll.getBlueprint().getCulture());
		assertEquals(CardType.MINION, troll.getBlueprint().getCardType());
		assertEquals(Race.TROLL, troll.getBlueprint().getRace());
		assertEquals(8, troll.getBlueprint().getTwilightCost());
		assertEquals(13, troll.getBlueprint().getStrength());
		assertEquals(4, troll.getBlueprint().getVitality());
		//assertEquals(, troll.getBlueprint().getResistance());
		//assertEquals(Signet., troll.getBlueprint().getSignet());
		assertEquals(5, troll.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void OnPlayTrollDoesNothingIfLessThan5OrcMinionsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var orc1 = scn.GetShadowCard("orc1");
		var orc2 = scn.GetShadowCard("orc2");
		var orc3 = scn.GetShadowCard("orc3");
		var orc4 = scn.GetShadowCard("orc4");
		var orc5 = scn.GetShadowCard("orc5");
		scn.MoveCardsToHand(troll);
		scn.MoveMinionsToTable(orc1, orc2, orc3, orc4);

		scn.StartGame();

		scn.SetTwilight(20);

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(troll);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void OnPlayTrollOptionallDiscards5OrcMinionsForFierceAndStrengthPluse10UntilEndOfTurn() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var orc1 = scn.GetShadowCard("orc1");
		var orc2 = scn.GetShadowCard("orc2");
		var orc3 = scn.GetShadowCard("orc3");
		var orc4 = scn.GetShadowCard("orc4");
		var orc5 = scn.GetShadowCard("orc5");
		scn.MoveCardsToHand(troll);
		scn.MoveMinionsToTable(orc1, orc2, orc3, orc4, orc5);

		scn.StartGame();

		scn.SetTwilight(17);

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(20, scn.GetTwilight());
		scn.ShadowPlayCard(troll);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(13, scn.GetStrength(troll));
		assertFalse(scn.HasKeyword(troll, Keyword.FIERCE));

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(23, scn.GetStrength(troll));
		assertTrue(scn.HasKeyword(troll, Keyword.FIERCE));
		assertEquals(Zone.DISCARD, orc1.getZone());
		assertEquals(Zone.DISCARD, orc2.getZone());
		assertEquals(Zone.DISCARD, orc3.getZone());
		assertEquals(Zone.DISCARD, orc4.getZone());
		assertEquals(Zone.DISCARD, orc5.getZone());
		assertEquals(10, scn.GetTwilight()); //20 initial -8 for troll -2 for roaming

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(23, scn.GetStrength(troll));
		assertTrue(scn.HasKeyword(troll, Keyword.FIERCE));

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(23, scn.GetStrength(troll));
		assertTrue(scn.HasKeyword(troll, Keyword.FIERCE));

		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();
		assertEquals(23, scn.GetStrength(troll));
		assertTrue(scn.HasKeyword(troll, Keyword.FIERCE));
	}

	@Test
	public void ShadowAbilityRemoves2AndPlaysExhaustedOrcFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var orc1 = scn.GetShadowCard("orc1");
		var orc2 = scn.GetShadowCard("orc2");
		var orc3 = scn.GetShadowCard("orc3");
		var orc4 = scn.GetShadowCard("orc4");
		var orc5 = scn.GetShadowCard("orc5");
		scn.MoveMinionsToTable(troll);
		scn.MoveCardsToDiscard(orc1, orc2, orc3, orc4, orc5);

		scn.StartGame();

		scn.SetTwilight(17);

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(20, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(troll));
		assertEquals(Zone.DISCARD, orc1.getZone());

		scn.ShadowUseCardAction(troll);
		assertEquals(5, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(orc1);

		assertEquals(13, scn.GetTwilight()); // -2 for ability, -3 for orc, -2 for roaming
		assertEquals(Zone.SHADOW_CHARACTERS, orc1.getZone());
		assertEquals(1, scn.GetWoundsOn(orc1));
	}
}
