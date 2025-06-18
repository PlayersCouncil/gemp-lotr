package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_006_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fror", "52_6");
					put("gimli", "1_13");

					put("nazgul", "1_230");
					put("uruk", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 2
		* Title: *Fror, Gimli's Kinsman
		* Side: Free Peoples
		* Culture: Dwarven
		* Twilight Cost: 2
		* Type: companion
		* Subtype: Dwarf
		* Strength: 6
		* Vitality: 3
		* Game Text: To play, spot a Dwarf.
		* 	While skirmishing an Uruk-hai, Fror is strength +3.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl fror = scn.GetFreepsCard("fror");

		assertTrue(fror.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, fror.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, fror.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, fror.getBlueprint().getCardType());
		assertEquals(Race.DWARF, fror.getBlueprint().getRace());
		assertEquals(2, fror.getBlueprint().getTwilightCost());
		assertEquals(6, fror.getBlueprint().getStrength());
		assertEquals(3, fror.getBlueprint().getVitality());
		assertEquals(6, fror.getBlueprint().getResistance());
	}

	@Test
	public void FrorRequiresDwarfSpotToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var fror = scn.GetFreepsCard("fror");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(gimli, fror);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(fror));
		scn.FreepsPlayCard(gimli);
		assertTrue(scn.FreepsPlayAvailable(fror));
	}

	@Test
	public void FrorIsStrengthPlus3AgainstUruks() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var fror = scn.GetFreepsCard("fror");
		scn.MoveCompanionsToTable(fror);

		var uruk = scn.GetShadowCard("uruk");
		var nazgul = scn.GetShadowCard("nazgul");
		scn.MoveMinionsToTable(uruk, nazgul);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(fror, uruk);
		scn.ShadowDeclineAssignments();

		assertEquals(6, scn.GetStrength(fror));
		scn.FreepsResolveSkirmish(fror);
		assertEquals(9, scn.GetStrength(fror));
		scn.PassCurrentPhaseActions();

		//fierce
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(fror, nazgul);
		scn.FreepsResolveSkirmish(fror);
		assertEquals(6, scn.GetStrength(fror));
	}
}
