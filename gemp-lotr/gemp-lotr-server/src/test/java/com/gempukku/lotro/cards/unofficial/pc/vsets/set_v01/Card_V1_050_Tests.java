
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_050_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("bilbo", "101_50");
                    put("sam", "1_311");
                    put("coat", "13_153");
                    put("sting", "1_313");

                    put("phial", "3_24");
                    put("greenleaf", "1_50");
                }},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void BilboStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: *Bilbo, Of Bag End
		* Side: Free Peoples
		* Culture: shire
		* Twilight Cost: 2
		* Type: ally
		* Subtype: Hobbit
		* Strength: 2
		* Vitality: 3
		* Site Number: 3
		* Game Text: Fellowship: If the fellowship is at a sanctuary, exert Bilbo twice to play 2 [shire] items from your draw deck on companions with the Frodo signet.
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl bilbo = scn.GetFreepsCard("bilbo");

		assertTrue(bilbo.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, bilbo.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, bilbo.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, bilbo.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, bilbo.getBlueprint().getRace());
		//assertTrue(scn.HasKeyword(bilbo, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(2, bilbo.getBlueprint().getTwilightCost());
		assertEquals(2, bilbo.getBlueprint().getStrength());
		assertEquals(3, bilbo.getBlueprint().getVitality());
		//assertEquals(, bilbo.getBlueprint().getResistance());
		//assertEquals(Signet., bilbo.getBlueprint().getSignet());
		assertTrue(bilbo.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void FellowshipAbilityOnlyWorksAtSanctuary() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl bilbo = scn.GetFreepsCard("bilbo");
		scn.FreepsMoveCardToSupportArea(bilbo);

		scn.FreepsMoveCardToDiscard("sam", "sting", "coat");
		scn.ShadowMoveCardToDiscard("bilbo", "sam", "sting", "coat");

		//Max out the move limit so we don't have to juggle play back and forth
		scn.ApplyAdHocModifier(new MoveLimitModifier(null, 10));

		scn.StartGame();

		for(int i = 1; i < 8; i++)
		{
			PhysicalCardImpl site = scn.GetCurrentSite();
			if(scn.hasKeyword(site, Keyword.SANCTUARY)) {
				assertTrue(scn.FreepsActionAvailable(bilbo));
			}
			else {
				assertFalse(scn.FreepsActionAvailable(bilbo));
			}

			scn.SkipToSite(i+1);
		}
	}

	@Test
	public void FellowshipAbilityExertsTwiceToTutor2ItemsOnFrodoSignetShireCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();
		PhysicalCardImpl bilbo = scn.GetFreepsCard("bilbo");
		PhysicalCardImpl greenleaf = scn.GetFreepsCard("greenleaf");
		PhysicalCardImpl sam = scn.GetFreepsCard("sam");
		PhysicalCardImpl coat = scn.GetFreepsCard("coat");
		PhysicalCardImpl sting = scn.GetFreepsCard("sting");
		PhysicalCardImpl phial = scn.GetFreepsCard("phial");
		scn.FreepsMoveCardToSupportArea(bilbo, greenleaf);
		scn.FreepsMoveCharToTable(sam);

		//Cheat the sanctuary so we don't have to move and swap
        scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.siteNumber(1), null, Keyword.SANCTUARY));

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(bilbo));
		assertEquals(0, scn.GetWoundsOn(bilbo));

		scn.FreepsUseCardAction(bilbo);
		assertEquals(2, scn.GetWoundsOn(bilbo));
        assertTrue(scn.FreepsDecisionAvailable("Choose cards from deck"));
		assertEquals(3, scn.GetFreepsCardChoiceCount()); // coat, sting, and phial

		scn.FreepsChooseCardBPFromSelection(sting);
		scn.FreepsDismissRevealedCards();
		assertEquals(Zone.ATTACHED, sting.getZone());
		assertEquals(frodo, sting.getAttachedTo());

        assertTrue(scn.FreepsDecisionAvailable("Choose cards from deck"));
		assertEquals(2, scn.GetFreepsCardChoiceCount());
		scn.FreepsChooseCardBPFromSelection(phial);
		scn.FreepsDismissRevealedCards();
		assertEquals(Zone.ATTACHED, phial.getZone());
		assertEquals(frodo, phial.getAttachedTo());

	}
}
