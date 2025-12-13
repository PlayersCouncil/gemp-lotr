package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_011_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("farin", "51_11");
					put("gimli", "1_13");

					put("runner", "1_178");
					put("nazgul", "1_230");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FarinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: *Farin, Dwarven Emissary
		* Side: Free Peoples
		* Culture: Dwarven
		* Twilight Cost: 2
		* Type: companion
		* Subtype: Dwarf
		* Strength: 6
		* Vitality: 3
		* Game Text: To play, spot a Dwarf.
		* 	While skirmishing an Orc, Farin is strength +2.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl farin = scn.GetFreepsCard("farin");

		assertTrue(farin.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, farin.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, farin.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, farin.getBlueprint().getCardType());
		assertEquals(Race.DWARF, farin.getBlueprint().getRace());
		assertEquals(2, farin.getBlueprint().getTwilightCost());
		assertEquals(6, farin.getBlueprint().getStrength());
		assertEquals(3, farin.getBlueprint().getVitality());
	}

	@Test
	public void FarinRequiresDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl farin = scn.GetFreepsCard("farin");
		PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(farin, gimli);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(farin));
		scn.FreepsPlayCard(gimli);
		assertTrue(scn.FreepsPlayAvailable(farin));
	}

	@Test
	public void FarinStrengthBonusAgainstOrcs() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(farin);

		PhysicalCardImpl orc = scn.GetShadowCard("runner");
		PhysicalCardImpl nazgul = scn.GetShadowCard("nazgul");
		scn.MoveMinionsToTable(orc, nazgul);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(farin, orc);
		scn.ShadowDeclineAssignments();

		assertEquals(6, scn.GetStrength(farin));
		scn.FreepsResolveSkirmish(farin);
		assertEquals(8, scn.GetStrength(farin));
		scn.PassCurrentPhaseActions();

		//fierce
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(farin, nazgul);
		scn.FreepsResolveSkirmish(farin);
		assertEquals(6, scn.GetStrength(farin));
	}
}
