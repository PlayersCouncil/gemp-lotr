
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

public class Card_V1_010_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
                new HashMap<>() {{
                    put("darts", "101_10");
                    put("galadriel", "1_45");
                    put("greenleaf", "1_50");
                    put("lorien", "51_53");
                    put("bow1", "1_41");
                    put("bow2", "1_41");
                    put("aragorn", "1_89");
                    put("gornbow", "1_90");

                    put("archer", "1_172");
                    put("runner", "1_178");
                }},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LetFlytheDartsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: *Let Fly the Darts of Lindon
		* Side: Free Peoples
		* Culture: elven
		* Twilight Cost: 1
		* Type: condition
		* Subtype: 
		* Game Text: Tale.
		* 	 Bearer must be a unique [elven] companion.
		* 	Archery: Exert bearer to make all Free Peoples archers lose archer and gain damage +1 until the regroup phase.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var darts = scn.GetFreepsCard("darts");

		assertFalse(darts.getBlueprint().isUnique());
		assertTrue(scn.HasKeyword(darts, Keyword.TALE)); // test for keywords as needed
		assertEquals(1, darts.getBlueprint().getTwilightCost());
		assertEquals(CardType.CONDITION, darts.getBlueprint().getCardType());
		assertEquals(Culture.ELVEN, darts.getBlueprint().getCulture());
		assertEquals(Side.FREE_PEOPLE, darts.getBlueprint().getSide());
	}

	@Test
	public void LetFlytheDartsOnlyPlaysOnUniqueElvenCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var darts = scn.GetFreepsCard("darts");
		var galadriel = scn.GetFreepsCard("galadriel");
		var greenleaf = scn.GetFreepsCard("greenleaf");
		var lorien = scn.GetFreepsCard("lorien");
		scn.MoveCardsToHand(darts, galadriel, greenleaf, lorien);

		scn.StartGame();
		assertFalse(scn.FreepsActionAvailable("let fly the darts"));
		scn.FreepsPlayCard(galadriel);
		assertFalse(scn.FreepsActionAvailable("let fly the darts"));
		scn.FreepsPlayCard(lorien);
		assertFalse(scn.FreepsActionAvailable("let fly the darts"));
		scn.FreepsPlayCard(greenleaf);
		assertTrue(scn.FreepsActionAvailable("let fly the darts"));
	}

	@Test
	public void LetFlytheDartsExertsToMakeArchersLoseArcherAndGainDamage() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var darts = scn.GetFreepsCard("darts");
		var galadriel = scn.GetFreepsCard("galadriel");
		var greenleaf = scn.GetFreepsCard("greenleaf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lorien = scn.GetFreepsCard("lorien");
		scn.MoveCompanionsToTable(greenleaf, galadriel, aragorn, lorien);
		scn.AttachCardsTo(galadriel, scn.GetFreepsCard("bow1"));
		scn.AttachCardsTo(lorien, scn.GetFreepsCard("bow2"));
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("gornbow"));
		scn.MoveCardsToHand(darts);

		PhysicalCardImpl archer = scn.GetShadowCard("archer");
		scn.MoveMinionsToTable(archer);

		scn.StartGame();
		scn.FreepsPlayCard(darts);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetWoundsOn(greenleaf));
		//1 each from greenleaf, lorien elf + bow, aragorn + bow (galadriel doesn't count)
		assertEquals(3, scn.GetFreepsArcheryTotal());
		assertTrue(scn.HasKeyword(greenleaf, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(galadriel, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(lorien, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(greenleaf, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(galadriel, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(aragorn, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(lorien, Keyword.DAMAGE));

		assertTrue(scn.FreepsActionAvailable("let fly the darts"));
		scn.FreepsUseCardAction(darts);

		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertFalse(scn.HasKeyword(greenleaf, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(galadriel, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(lorien, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(greenleaf, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(galadriel, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(aragorn, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(lorien, Keyword.DAMAGE));

		assertEquals(1, scn.GetWoundsOn(greenleaf));

		//Archery
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		scn.FreepsChooseCard(aragorn);

		//Assignment
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(lorien, archer);
		scn.FreepsResolveSkirmish(lorien);
		scn.PassCurrentPhaseActions();

		assertEquals(2, scn.GetWoundsOn(archer));

		//Regroup
		assertTrue(scn.HasKeyword(greenleaf, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(galadriel, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(lorien, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(greenleaf, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(galadriel, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(aragorn, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(lorien, Keyword.DAMAGE));
	}


}
