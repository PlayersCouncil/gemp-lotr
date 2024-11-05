package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_311_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>() {{
					put("sam", "51_311");
					put("rosie", "1_309");
					put("proudfoot", "1_301");
					put("gaffer", "1_291");

					put("orc", "1_272");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
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
		scn.FreepsMoveCharToTable(sam);

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
		scn.FreepsMoveCharToTable(sam);
		scn.FreepsMoveCardToSupportArea(proudfoot, gaffer);

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
		scn.FreepsMoveCharToTable(sam);
		scn.FreepsMoveCardToSupportArea(rosie);

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
		scn.FreepsMoveCharToTable(sam);

		scn.StartGame();

		assertNotSame(scn.GetRingBearer(), sam);
		scn.AddWoundsToChar(frodo, 4);

		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertSame(scn.GetRingBearer(), sam);
	}
}
