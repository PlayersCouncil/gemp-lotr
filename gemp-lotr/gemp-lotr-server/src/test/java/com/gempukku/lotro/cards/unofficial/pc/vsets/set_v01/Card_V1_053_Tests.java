
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_V1_053_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("pippin", "101_53");
					put("sam", "1_310");

					put("nazgul", "1_232");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PippinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: *Pippin, Of Tuckborough
		* Side: Free Peoples
		* Culture: shire
		* Twilight Cost: 1
		* Type: companion
		* Subtype: Hobbit
		* Strength: 3
		* Vitality: 4
		* Signet: Frodo
		* Game Text: Skirmish: Exert Pippin to prevent a [shire] companion from being overwhelmed unless their strength is tripled (and make them strength +1 if you can spot 3 companions with the Frodo signet).
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl pippin = scn.GetFreepsCard("pippin");

		assertTrue(pippin.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, pippin.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, pippin.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, pippin.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, pippin.getBlueprint().getRace());
		//assertTrue(scn.HasKeyword(pippin, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(1, pippin.getBlueprint().getTwilightCost());
		assertEquals(3, pippin.getBlueprint().getStrength());
		assertEquals(4, pippin.getBlueprint().getVitality());
		//assertEquals(, pippin.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, pippin.getBlueprint().getSignet());
		//assertEquals(, pippin.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void SkirmishAbilityExertsPippinToPreventShireOverwhelmUnlessTripled() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();
		PhysicalCardImpl pippin = scn.GetFreepsCard("pippin");
		//PhysicalCardImpl sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(pippin);

		PhysicalCardImpl nazgul = scn.GetShadowCard("nazgul");
		scn.MoveCompanionsToTable(nazgul);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(frodo, nazgul);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(2, scn.GetOverwhelmMultiplier(frodo)); // 2x is the default
		assertTrue(scn.FreepsActionAvailable(pippin));
		assertEquals(0, scn.GetWoundsOn(pippin));

		scn.FreepsUseCardAction(pippin);
		assertEquals(3, scn.GetOverwhelmMultiplier(frodo));
		assertEquals(1, scn.GetWoundsOn(pippin));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		scn.FreepsDeclineOptionalTrigger(); //ring response

		assertEquals(1, scn.GetWoundsOn(frodo));

	}

	@Test
	public void SkirmishAbilityPumpsIfThreeFrodoSignets() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();
		PhysicalCardImpl pippin = scn.GetFreepsCard("pippin");
		PhysicalCardImpl sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(pippin, sam);

		PhysicalCardImpl nazgul = scn.GetShadowCard("nazgul");
		scn.MoveCompanionsToTable(nazgul);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(sam, nazgul);
		scn.FreepsResolveSkirmish(sam);

		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.FreepsActionAvailable(pippin));
		assertEquals(0, scn.GetWoundsOn(pippin));

		scn.FreepsUseCardAction(pippin);
		assertEquals(4, scn.GetStrength(sam));

	}
}
