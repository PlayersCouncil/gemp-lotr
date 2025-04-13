
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

public class Card_V1_024_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>() {{
					put("mysword", "101_24");
					put("aragorn", "1_365");
					put("arwen", "1_30");

					put("nazgul", "1_233");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void YouHaveMySwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: *You Have My Sword
		* Side: Free Peoples
		* Culture: gondor
		* Twilight Cost: 1
		* Type: condition
		* Subtype: 
		* Strength: 2
		* Game Text: To play, spot Aragorn. Plays on a companion with the Aragorn signet (except Aragorn).
		* 	Each time bearer wins a skirmish, exert Aragorn or discard this condition.
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl mysword = scn.GetFreepsCard("mysword");

		assertTrue(mysword.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, mysword.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, mysword.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, mysword.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, mysword.getBlueprint().getRace());
		//assertTrue(scn.HasKeyword(mysword, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(1, mysword.getBlueprint().getTwilightCost());
		assertEquals(2, mysword.getBlueprint().getStrength());
		//assertEquals(, mysword.getBlueprint().getVitality());
		//assertEquals(, mysword.getBlueprint().getResistance());
		//assertEquals(Signet., mysword.getBlueprint().getSignet()); 
		//assertEquals(, mysword.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void SpotsAragornToPlayOnGornSignetBesidesAragorn() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl mysword = scn.GetFreepsCard("mysword");
		PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
		PhysicalCardImpl arwen = scn.GetFreepsCard("arwen");
		scn.FreepsMoveCardToHand(mysword, aragorn, arwen);

		scn.StartGame();
		assertFalse(scn.FreepsPlayAvailable(mysword));
		scn.FreepsPlayCard(aragorn);
		assertFalse(scn.FreepsPlayAvailable(mysword));
		scn.FreepsPlayCard(arwen);
		assertTrue(scn.FreepsPlayAvailable(mysword));
		scn.FreepsPlayCard(mysword);

		assertEquals(Zone.ATTACHED, mysword.getZone());
		assertEquals(arwen, mysword.getAttachedTo());
	}

	@Test
	public void BearerWinningSkirmishExertsAragornOrSelfDiscards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl mysword = scn.GetFreepsCard("mysword");
		PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
		PhysicalCardImpl arwen = scn.GetFreepsCard("arwen");
		scn.FreepsMoveCharToTable(aragorn, arwen);
		scn.FreepsAttachCardsTo(arwen, mysword);

		PhysicalCardImpl nazgul = scn.GetShadowCard("nazgul");
		scn.ShadowMoveCharToTable(nazgul);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(arwen, nazgul);
		scn.FreepsResolveSkirmish(arwen);
		// 6 base + 3 against nazgul + 2 from You Have My Sword
		assertEquals(11, scn.GetStrength(arwen));
		scn.PassCurrentPhaseActions();

		//Freeps tiebreaker between resolving the skirmish or processing the required trigger for You Have My Sword.
		scn.FreepsResolveRuleFirst();

		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		String[] choices = scn.FreepsGetMultipleChoices().toArray(new String[0]);
		assertEquals(2, choices.length);
		assertEquals("Exert Aragorn", choices[0]);
		assertEquals("Discard You Have My Sword", choices[1]);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseMultipleChoiceOption("Exert");
		assertEquals(1, scn.GetWoundsOn(aragorn));

		//fierce skirmish
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(arwen, nazgul);
		scn.FreepsResolveSkirmish(arwen);
		// 6 base + 3 against nazgul + 2 from You Have My Sword
		assertEquals(11, scn.GetStrength(arwen));
		scn.PassCurrentPhaseActions();

		//Freeps tiebreaker between resolving the skirmish or processing the required trigger for You Have My Sword.
		scn.FreepsResolveRuleFirst();

		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		choices = scn.FreepsGetMultipleChoices().toArray(new String[0]);
		assertEquals(2, choices.length);
		assertEquals("Exert Aragorn", choices[0]);
		assertEquals("Discard You Have My Sword", choices[1]);

		scn.FreepsChooseMultipleChoiceOption("Discard");
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(Zone.DISCARD, mysword.getZone());
	}
}
