
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

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

public class Card_V1_018_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("council", "101_18");
					put("gandalf", "101_14");
					put("elrond", "3_13");
					put("galadriel", "1_45");
					put("aragorn", "1_89");

					put("saruman", "3_69");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheWhiteCouncilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: *The White Council
		* Side: Free Peoples
		* Culture: gandalf
		* Twilight Cost: 1
		* Type: condition
		* Subtype: Support Area
		* Game Text: While you can spot Gandalf and 2 [elven] allies, the first sentence of Saruman's game text does not apply.
		* 	Regroup: Exert 2 [elven] allies to heal Gandalf or a companion with the Gandalf signet (limit 2 per phase).
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl council = scn.GetFreepsCard("council");

		assertTrue(council.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, council.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, council.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, council.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, council.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(council, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(1, council.getBlueprint().getTwilightCost());
		//assertEquals(, council.getBlueprint().getStrength());
		//assertEquals(, council.getBlueprint().getVitality());
		//assertEquals(, council.getBlueprint().getResistance());
		//assertEquals(Signet., council.getBlueprint().getSignet());
		//assertEquals(, council.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void SpottingGandalfAndTwoAlliesNegatesSarumansFirstSentence() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl council = scn.GetFreepsCard("council");
		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		PhysicalCardImpl elrond = scn.GetFreepsCard("elrond");
		PhysicalCardImpl galadriel = scn.GetFreepsCard("galadriel");
		PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(gandalf, elrond, galadriel);
		scn.MoveCardsToSupportArea(council);
		scn.MoveCompanionsToTable(aragorn);

		PhysicalCardImpl saruman = scn.GetShadowCard("saruman");
		scn.MoveMinionsToTable(saruman);

		scn.StartGame();

		assertFalse(scn.CanBeAssignedViaAction(saruman));
		scn.FreepsPlayCard(elrond);
		assertFalse(scn.CanBeAssignedViaAction(saruman));
		scn.FreepsPlayCard(galadriel);
		assertFalse(scn.CanBeAssignedViaAction(saruman));
		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.CanBeAssignedViaAction(saruman));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(gandalf, saruman);
		scn.FreepsResolveSkirmish(gandalf);
	}

	@Test
	public void RegroupActionUnavailableWithoutHealthyElvenAllies() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl council = scn.GetFreepsCard("council");
		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		PhysicalCardImpl elrond = scn.GetFreepsCard("elrond");
		PhysicalCardImpl galadriel = scn.GetFreepsCard("galadriel");
		PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(council);
		scn.MoveCompanionsToTable(aragorn, gandalf, elrond, galadriel);


		scn.StartGame();

		scn.AddWoundsToChar(galadriel, 2);

		scn.SkipToPhase(Phase.REGROUP);
		assertFalse(scn.FreepsActionAvailable(council));
	}

	@Test
	public void RegroupActionExertsAlliesToHeal() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl council = scn.GetFreepsCard("council");
		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		PhysicalCardImpl elrond = scn.GetFreepsCard("elrond");
		PhysicalCardImpl galadriel = scn.GetFreepsCard("galadriel");
		PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(council);
		scn.MoveCompanionsToTable(aragorn, gandalf, elrond, galadriel);


		scn.StartGame();

		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(gandalf, 3);

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsActionAvailable(council));
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(3, scn.GetWoundsOn(aragorn));

		scn.FreepsUseCardAction(council);
		//the only 2 elven allies are chosen automatically
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(aragorn));

		scn.ShadowPassCurrentPhaseAction();

		assertTrue(scn.FreepsActionAvailable(council));
		scn.FreepsUseCardAction(council);
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.RemoveWoundsFromChar(galadriel, 1);

		scn.ShadowPassCurrentPhaseAction();

		//limit of 2 means it won't let us even with healthy allies
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertFalse(scn.FreepsActionAvailable(council));

		scn.SkipToSite(3);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsActionAvailable(council));
	}
}
