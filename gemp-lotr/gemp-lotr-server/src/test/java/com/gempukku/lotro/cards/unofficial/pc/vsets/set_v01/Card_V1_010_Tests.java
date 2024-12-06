
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
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
	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
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
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
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
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl darts = scn.GetFreepsCard("darts");

		assertFalse(darts.getBlueprint().isUnique());
		assertTrue(scn.hasKeyword(darts, Keyword.TALE)); // test for keywords as needed
		assertEquals(1, darts.getBlueprint().getTwilightCost());
		assertEquals(CardType.CONDITION, darts.getBlueprint().getCardType());
		assertEquals(Culture.ELVEN, darts.getBlueprint().getCulture());
		assertEquals(Side.FREE_PEOPLE, darts.getBlueprint().getSide());
	}

	@Test
	public void LetFlytheDartsOnlyPlaysOnUniqueElvenCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl darts = scn.GetFreepsCard("darts");
		PhysicalCardImpl galadriel = scn.GetFreepsCard("galadriel");
		PhysicalCardImpl greenleaf = scn.GetFreepsCard("greenleaf");
		PhysicalCardImpl lorien = scn.GetFreepsCard("lorien");
		scn.FreepsMoveCardToHand(darts, galadriel, greenleaf, lorien);

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
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl darts = scn.GetFreepsCard("darts");
		PhysicalCardImpl galadriel = scn.GetFreepsCard("galadriel");
		PhysicalCardImpl greenleaf = scn.GetFreepsCard("greenleaf");
		PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
		PhysicalCardImpl lorien = scn.GetFreepsCard("lorien");
		scn.FreepsMoveCharToTable(greenleaf, galadriel, aragorn, lorien);
		scn.FreepsAttachCardsTo(galadriel, "bow1");
		scn.FreepsAttachCardsTo(lorien, "bow2");
		scn.FreepsAttachCardsTo(aragorn, "gornbow");
		scn.FreepsMoveCardToHand(darts);

		PhysicalCardImpl archer = scn.GetShadowCard("archer");
		scn.ShadowMoveCharToTable(archer);

		scn.StartGame();
		scn.FreepsPlayCard(darts);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetWoundsOn(greenleaf));
		//1 each from greenleaf, lorien elf + bow, aragorn + bow (galadriel doesn't count)
		assertEquals(3, scn.GetFreepsArcheryTotal());
		assertTrue(scn.hasKeyword(greenleaf, Keyword.ARCHER));
		assertTrue(scn.hasKeyword(galadriel, Keyword.ARCHER));
		assertTrue(scn.hasKeyword(aragorn, Keyword.ARCHER));
		assertTrue(scn.hasKeyword(lorien, Keyword.ARCHER));
		assertFalse(scn.hasKeyword(greenleaf, Keyword.DAMAGE));
		assertFalse(scn.hasKeyword(galadriel, Keyword.DAMAGE));
		assertFalse(scn.hasKeyword(aragorn, Keyword.DAMAGE));
		assertFalse(scn.hasKeyword(lorien, Keyword.DAMAGE));

		assertTrue(scn.FreepsActionAvailable("let fly the darts"));
		scn.FreepsUseCardAction(darts);

		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertFalse(scn.hasKeyword(greenleaf, Keyword.ARCHER));
		assertFalse(scn.hasKeyword(galadriel, Keyword.ARCHER));
		assertFalse(scn.hasKeyword(aragorn, Keyword.ARCHER));
		assertFalse(scn.hasKeyword(lorien, Keyword.ARCHER));
		assertTrue(scn.hasKeyword(greenleaf, Keyword.DAMAGE));
		assertTrue(scn.hasKeyword(galadriel, Keyword.DAMAGE));
		assertTrue(scn.hasKeyword(aragorn, Keyword.DAMAGE));
		assertTrue(scn.hasKeyword(lorien, Keyword.DAMAGE));

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
		assertTrue(scn.hasKeyword(greenleaf, Keyword.ARCHER));
		assertTrue(scn.hasKeyword(galadriel, Keyword.ARCHER));
		assertTrue(scn.hasKeyword(aragorn, Keyword.ARCHER));
		assertTrue(scn.hasKeyword(lorien, Keyword.ARCHER));
		assertFalse(scn.hasKeyword(greenleaf, Keyword.DAMAGE));
		assertFalse(scn.hasKeyword(galadriel, Keyword.DAMAGE));
		assertFalse(scn.hasKeyword(aragorn, Keyword.DAMAGE));
		assertFalse(scn.hasKeyword(lorien, Keyword.DAMAGE));
	}


}
