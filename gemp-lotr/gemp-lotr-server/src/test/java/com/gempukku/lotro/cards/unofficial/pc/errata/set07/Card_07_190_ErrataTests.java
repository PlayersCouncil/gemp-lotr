package com.gempukku.lotro.cards.unofficial.pc.errata.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_190_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("destroyer", "57_190");
					put("destroyer2", "57_190");
					put("nazgul", "1_229");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulDestroyerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 7
		* Title: Morgul Destroyer
		* Unique: False
		* Side: SHADOW
		* Culture: Wraith
		* Twilight Cost: 3
		* Type: minion
		* Subtype: Orc
		* Strength: 6
		* Vitality: 2
		* Site Number: 4
		* Game Text: When you play this minion, you may spot a Nazgûl to add a threat. The
		* 	Free Peoples player may make you wound a companion to prevent this. <br><b>Skirmish:</b> Remove a threat to make this minion strength +4.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var destroyer = scn.GetFreepsCard("destroyer");

		assertFalse(destroyer.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, destroyer.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, destroyer.getBlueprint().getCulture());
		assertEquals(CardType.MINION, destroyer.getBlueprint().getCardType());
		assertEquals(Race.ORC, destroyer.getBlueprint().getRace());
		assertEquals(3, destroyer.getBlueprint().getTwilightCost());
		assertEquals(6, destroyer.getBlueprint().getStrength());
		assertEquals(2, destroyer.getBlueprint().getVitality());
		assertEquals(4, destroyer.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayingDestroyerWithNoNazgulDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var destroyer = scn.GetShadowCard("destroyer");
		scn.MoveCardsToHand(destroyer);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowPlayAvailable(destroyer));
		scn.ShadowPlayCard(destroyer);
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void PlayingDestroyerWithNazgulAddsAThreatIfNotPrevented() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var destroyer = scn.GetShadowCard("destroyer");
		var nazgul = scn.GetShadowCard("nazgul");
		scn.MoveCardsToHand(destroyer);
		scn.MoveMinionsToTable(nazgul);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowPlayAvailable(destroyer));
		scn.ShadowPlayCard(destroyer);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDecisionAvailable("Would you like to spot a Nazgul to add 1 threat?");
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.FreepsDecisionAvailable("Make the Shadow player wound a companion to prevent adding a threat?"));
		scn.FreepsChooseNo();

		assertEquals(1, scn.GetThreats());
	}

	@Test
	public void DestroyerAddingAThreatCanBePreventedByFreepsLettingShadowWoundSomeone() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var destroyer = scn.GetShadowCard("destroyer");
		var nazgul = scn.GetShadowCard("nazgul");
		scn.MoveCardsToHand(destroyer);
		scn.MoveMinionsToTable(nazgul);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowPlayAvailable(destroyer));
		scn.ShadowPlayCard(destroyer);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Make the Shadow player wound a companion to prevent adding a threat?"));
		scn.FreepsChooseYes();

		//Wound automatically applied since there's only one choice
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetThreats());
	}

	@Test
	public void DestroyerSkirmishActionRemovesAThreatToPump3() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var destroyer = scn.GetShadowCard("destroyer");
		scn.MoveMinionsToTable(destroyer);

		scn.StartGame();
		scn.AddThreats(2);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(frodo, destroyer);
		scn.FreepsResolveSkirmish(frodo);

		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(destroyer));
		assertEquals(2, scn.GetThreats());
		assertEquals(6, scn.GetStrength(destroyer));
		scn.ShadowUseCardAction(destroyer);
		assertEquals(1, scn.GetThreats());
		assertEquals(9, scn.GetStrength(destroyer));
	}
}
