package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_017_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("die", "102_17");
					put("aragorn", "1_89");
					put("theoden", "4_292");
					put("vcompanion", "5_122");

					put("sauron", "9_48");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IWillDieasOneofThemStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: I Will Die as One of Them
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert Aragorn three times to make each valiant companion strength +2 until the regroup phase. 
		* 	If Aragorn dies during this turn, make each valiant companion strength +1 for the rest of the turn. 
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("die");

		assertEquals("I Will Die as One of Them", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IWillDieasPumpsValiantCompanionsUntilRegroupAndEnddOfTurnIfAragornDies() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var die = scn.GetFreepsCard("die");
		var aragorn = scn.GetFreepsCard("aragorn");
		var theoden = scn.GetFreepsCard("theoden");
		var vcompanion = scn.GetFreepsCard("vcompanion");
		scn.MoveCardsToHand(die);
		scn.MoveCompanionToTable(aragorn, theoden, vcompanion);

		var sauron = scn.GetShadowCard("sauron");
		scn.MoveMinionsToTable(sauron);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(theoden));
		assertEquals(6, scn.GetStrength(vcompanion));
		assertFalse(scn.HasKeyword(frodo, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(aragorn, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(theoden, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(vcompanion, Keyword.VALIANT));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsPlayAvailable(die));

		scn.FreepsPlayCard(die);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(theoden));
		assertEquals(8, scn.GetStrength(vcompanion));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, sauron);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(Zone.DEAD, aragorn.getZone());

		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(theoden));
		//+2 until regroup, +1 until end of turn
		assertEquals(9, scn.GetStrength(vcompanion));

		scn.AddWoundsToChar(sauron, 5); //we're done with him
		scn.PassCurrentPhaseActions();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());

		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(theoden));
		//+2 until regroup wore off, +1 until end of turn
		assertEquals(7, scn.GetStrength(vcompanion));

		scn.PassCurrentPhaseActions();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(theoden));
		//+2 until regroup wore off, +1 until end of turn
		assertEquals(7, scn.GetStrength(vcompanion));

	}
}
