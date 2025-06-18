package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_311_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("sam", "51_311");
					put("rosie", "1_309");
					put("proudfoot", "1_301");
					put("gaffer", "1_291");
					put("stealth", "1_298");

					put("twk", "1_237");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: Sam, Son of Hamfast
		* Unique: True
		* Side: FREE_PEOPLE
		* Culture: Shire
		* Twilight Cost: 2
		* Type: companion
		* Subtype: Hobbit
		* Strength: 3
		* Vitality: 4
		* Signet: Aragorn
		* Game Text: <b>Fellowship:</b> Exert Sam and another companion to remove a burden. Then, exert Sam again unless you can spot 2 [shire] allies (or Rosie Cotton).
		* 	<b>Response:</b> If Frodo dies, make Sam the <b>Ring-bearer (resistance 5).</b>
		*/

		//Pre-game setup
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");

		assertTrue(sam.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, sam.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, sam.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, sam.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, sam.getBlueprint().getRace());
		assertEquals(2, sam.getBlueprint().getTwilightCost());
		assertEquals(3, sam.getBlueprint().getStrength());
		assertEquals(4, sam.getBlueprint().getVitality());
		assertEquals(5, sam.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, sam.getBlueprint().getSignet());
	}

	@Test
	public void FellowshipActionExertsTwicePlusOneToRemoveABurden() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsActionAvailable(sam));

		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());

		scn.FreepsUseCardAction(sam);

		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
	}

	@Test
	public void FellowshipActionExertsTwiceToRemoveABurdenIf2ShireAllies() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var proudfoot = scn.GetFreepsCard("proudfoot");
		var gaffer = scn.GetFreepsCard("gaffer");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(proudfoot, gaffer);

		scn.StartGame();

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsActionAvailable(sam));

		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());

		scn.FreepsUseCardAction(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
	}

	@Test
	public void FellowshipActionExertsTwiceToRemoveABurdenIfRosie() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var rosie = scn.GetFreepsCard("rosie");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(rosie);

		scn.StartGame();

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsActionAvailable(sam));

		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());

		scn.FreepsUseCardAction(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
	}


	@Test
	public void RBDeathMakesSamTheRB() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		assertNotSame(scn.GetRingBearer(), sam);
		scn.AddWoundsToChar(frodo, 4);

		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertSame(scn.GetRingBearer(), sam);
		assertFalse(scn.GameIsFinished());
	}

	@Test
	public void RBOverwhelmMakesSamTheRB() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		var twk = scn.GetShadowCard("twk");
		scn.MoveMinionsToTable(twk);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, twk);
		scn.FreepsResolveSkirmish(frodo);
		scn.PassCurrentPhaseActions();

		assertNotSame(scn.GetRingBearer(), sam);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertSame(scn.GetRingBearer(), sam);
		assertFalse(scn.GameIsFinished());
	}

	@Test
	public void RBFierceOverwhelmMakesSamTheRB() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var stealth = scn.GetFreepsCard("stealth");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(stealth);

		var twk = scn.GetShadowCard("twk");
		scn.MoveMinionsToTable(twk);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, twk);
		scn.FreepsResolveSkirmish(sam);
		scn.FreepsPlayCard(stealth);

		//Fierce
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(frodo, twk);
		scn.FreepsResolveSkirmish(frodo);
		scn.PassCurrentPhaseActions();

		assertNotSame(scn.GetRingBearer(), sam);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertSame(scn.GetRingBearer(), sam);
		assertFalse(scn.GameIsFinished());
	}
}
