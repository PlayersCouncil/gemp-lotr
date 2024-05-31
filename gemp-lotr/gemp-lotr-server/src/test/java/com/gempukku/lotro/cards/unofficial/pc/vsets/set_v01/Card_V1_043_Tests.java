
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_043_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>() {{
					put("faces", "101_43");
					put("twigul", "2_82");
					put("wk", "2_85");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void WhiteFacesBurnedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: White Faces Burned
		* Side: Free Peoples
		* Culture: ringwraith
		* Twilight Cost: 1
		* Type: event
		* Subtype: Skirmish
		* Game Text: Make a twilight Nazgul strength +1 for each wound on the Ring-bearer_(and damage +1 if that minion is The Witch King.)
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl faces = scn.GetFreepsCard("faces");

		assertFalse(faces.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, faces.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, faces.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, faces.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, faces.getBlueprint().getRace());
        assertTrue(scn.hasTimeword(faces, Timeword.SKIRMISH)); // test for keywords as needed
		assertEquals(1, faces.getBlueprint().getTwilightCost());
		//assertEquals(, faces.getBlueprint().getStrength());
		//assertEquals(, faces.getBlueprint().getVitality());
		//assertEquals(, faces.getBlueprint().getResistance());
		//assertEquals(Signet., faces.getBlueprint().getSignet());
		//assertEquals(, faces.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void FacesAddsOneStrengthPerWoundOnTheRB() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();

		PhysicalCardImpl faces = scn.GetShadowCard("faces");
		PhysicalCardImpl twigul = scn.GetShadowCard("twigul");
		scn.ShadowMoveCharToTable(twigul);
		scn.ShadowMoveCardToHand(faces);

		scn.StartGame();

		scn.AddWoundsToChar(frodo, 3);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(frodo, twigul);
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(12, scn.GetStrength(twigul));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowPlayAvailable(faces));
		scn.ShadowPlayCard(faces);
		assertEquals(15, scn.GetStrength(twigul));
	}

	@Test
	public void FacesMakesWitchKingDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();

		PhysicalCardImpl faces = scn.GetShadowCard("faces");
		PhysicalCardImpl wk = scn.GetShadowCard("wk");
		scn.ShadowMoveCharToTable(wk);
		scn.ShadowMoveCardToHand(faces);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(frodo, wk);
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(14, scn.GetStrength(wk));
		assertFalse(scn.HasKeyword(wk, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowPlayAvailable(faces));

		scn.ShadowPlayCard(faces);
		assertEquals(14, scn.GetStrength(wk));
		assertTrue(scn.HasKeyword(wk, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(wk, Keyword.DAMAGE));
	}
}
