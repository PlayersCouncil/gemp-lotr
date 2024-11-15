
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_V1_025_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("flock", "101_25");
                    put("flock2", "101_25");
                    put("flock3", "101_25");
                    put("flock4", "101_25");

                    put("weather1", "1_134");
                    put("weather2", "1_134");
                    put("weather3", "1_134");

                    put("saruman", "3_69");

                    put("aragorn", "1_89");
                    put("arwen", "1_30");
                    put("gimli", "1_12");
                    put("gandalf", "1_364");

                    //Since we will go multiple rounds and need to manipulate the draw deck,
                    // these are here to ensure reconciliation doesn't screw us over.
                    put("filler1", "1_12");
                    put("filler2", "1_12");
                    put("filler3", "1_12");
                    put("filler4", "1_12");
                    put("filler5", "1_12");
                    put("filler6", "1_12");
                    put("filler7", "1_12");
                    put("filler8", "1_12");
                    put("filler9", "1_12");

                }},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void CrebainFlockStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: Crebain Flock
		* Side: Free Peoples
		* Culture: isengard
		* Twilight Cost: 2
		* Type: minion
		* Subtype: Crow
		* Strength: 2
		* Vitality: 3
		* Site Number: 4
		* Game Text: This minion is twilight cost -1 for each companion you can spot over 3.
		* 	Maneuver: Spot 2 wounded companions (or spot Saruman) and discard this minion to place an [isengard] weather flock from your draw deck or discard pile on top of your draw deck.
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl flock = scn.GetFreepsCard("flock");

		assertFalse(flock.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, flock.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, flock.getBlueprint().getCulture());
		assertEquals(CardType.MINION, flock.getBlueprint().getCardType());
		assertEquals(Race.CROW, flock.getBlueprint().getRace());
		//assertTrue(scn.HasKeyword(flock, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(2, flock.getBlueprint().getTwilightCost());
		assertEquals(2, flock.getBlueprint().getStrength());
		assertEquals(3, flock.getBlueprint().getVitality());
		//assertEquals(, flock.getBlueprint().getResistance());
		//assertEquals(Signet., flock.getBlueprint().getSignet());
		assertEquals(4, flock.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void FlockTwilightDiscountScalesBasedOnCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
		PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
		PhysicalCardImpl arwen = scn.GetFreepsCard("arwen");
		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");

		scn.FreepsMoveCharToTable(aragorn, gimli);
		scn.FreepsMoveCardToHand(arwen, gandalf);

		PhysicalCardImpl flock = scn.GetShadowCard("flock");
		PhysicalCardImpl flock2 = scn.GetShadowCard("flock2");
		PhysicalCardImpl flock3 = scn.GetShadowCard("flock3");
		PhysicalCardImpl flock4 = scn.GetShadowCard("flock4");
		scn.ShadowMoveCardToHand(flock, flock2, flock3, flock4);

		scn.StartGame();

		assertEquals(Zone.FREE_CHARACTERS, scn.GetRingBearer().getZone());
		assertEquals(Zone.FREE_CHARACTERS, aragorn.getZone());
		assertEquals(Zone.FREE_CHARACTERS, gimli.getZone());
		assertEquals(Zone.HAND, arwen.getZone());
		assertEquals(Zone.HAND, gandalf.getZone());

		scn.SetTwilight(15);

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(20, scn.GetTwilight()); //base after moving
		scn.ShadowPlayCard(flock);
		assertEquals(16, scn.GetTwilight()); //-2 cost, -2 roaming
		scn.FreepsMoveCharToTable(arwen);
		scn.ShadowPlayCard(flock);
		assertEquals(13, scn.GetTwilight()); //-1 cost, -2 roaming
		scn.FreepsMoveCharToTable(gandalf);
		scn.ShadowPlayCard(flock);
		assertEquals(11, scn.GetTwilight()); //-0 cost, -2 roaming
	}

	@Test
	public void ManeuverActionRequires2WoundedCompsOrSaruman() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
		PhysicalCardImpl arwen = scn.GetFreepsCard("arwen");

		scn.FreepsMoveCharToTable(aragorn, arwen);

		PhysicalCardImpl flock = scn.GetShadowCard("flock");
		PhysicalCardImpl weather1 = scn.GetShadowCard("weather1");
		PhysicalCardImpl weather2 = scn.GetShadowCard("weather2");
		PhysicalCardImpl weather3 = scn.GetShadowCard("weather3");
		PhysicalCardImpl saruman = scn.GetShadowCard("saruman");

		scn.ShadowMoveCardToHand(saruman);
		scn.ShadowMoveCharToTable(flock);
		scn.ShadowMoveCardsToBottomOfDeck(weather1);
		scn.ShadowMoveCardToDiscard(weather2);
		scn.ShadowMoveCardToDiscard(weather3);

		//Max out the move limit so we don't have to juggle play back and forth
		scn.ApplyAdHocModifier(new MoveLimitModifier(null, 10));

		scn.StartGame();

		scn.AddWoundsToChar(aragorn, 1);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		// 1 wounded companion, no saruman
		assertFalse(scn.ShadowActionAvailable(flock));
		scn.ShadowPassCurrentPhaseAction();

		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();

		scn.AddWoundsToChar(arwen, 1);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		// 2 wounded companions, no saruman
		assertTrue(scn.ShadowActionAvailable(flock));
		scn.ShadowPassCurrentPhaseAction();

		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();

		scn.RemoveWoundsFromChar(arwen, 1);
		scn.RemoveWoundsFromChar(aragorn, 1);
		scn.ShadowMoveCharToTable(saruman);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		// 0 wounded companions, yes saruman
		assertTrue(scn.ShadowActionAvailable(flock));

	}

	@Test
	public void ManeuverActionSelfDiscardsToTopDecksWeatherFromDeckOrDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();


		PhysicalCardImpl flock = scn.GetShadowCard("flock");
		PhysicalCardImpl flock2 = scn.GetShadowCard("flock2");
		PhysicalCardImpl flock3 = scn.GetShadowCard("flock3");
		PhysicalCardImpl weather1 = scn.GetShadowCard("weather1");
		PhysicalCardImpl weather2 = scn.GetShadowCard("weather2");
		PhysicalCardImpl weather3 = scn.GetShadowCard("weather3");
		PhysicalCardImpl saruman = scn.GetShadowCard("saruman");

		scn.ShadowMoveCharToTable(flock, flock2, flock3, saruman);
		scn.ShadowMoveCardsToBottomOfDeck(weather1);
		scn.ShadowMoveCardToDiscard(weather2);
		scn.ShadowMoveCardToDiscard(weather3);

		//Max out the move limit so we don't have to juggle play back and forth
		scn.ApplyAdHocModifier(new MoveLimitModifier(null, 10));

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(flock));
		assertEquals(Zone.DECK, weather1.getZone());
		assertEquals(weather1, scn.GetShadowBottomOfDeck());
		assertEquals(Zone.DISCARD, weather2.getZone());
		assertEquals(Zone.DISCARD, weather3.getZone());

		// When the ability is invoked and there are weathers in both draw deck and discard, the player
		// must first decide between deck and discard, and then choose among the revealed copies.
		scn.ShadowUseCardAction(flock);
		assertTrue(scn.ShadowDecisionAvailable("Choose action to perform"));
		scn.ShadowChooseMultipleChoiceOption("discard");
		assertEquals(2, scn.GetShadowCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(weather2);
		assertEquals(Zone.DISCARD, flock.getZone());
		assertEquals(Zone.DECK, weather2.getZone());
		assertEquals(weather2, scn.GetShadowTopOfDeck());

		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		//scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(flock2));
		assertEquals(Zone.DECK, weather1.getZone());
		assertEquals(weather1, scn.GetShadowBottomOfDeck());
		assertEquals(Zone.HAND, weather2.getZone()); // it was drawn during regroup
		assertEquals(Zone.DISCARD, weather3.getZone());

		// When the ability is invoked and there is a single weather in both draw deck and discard, the player
		// chooses between deck/discard, but then does not have to choose among copies (as there is only 1)
		scn.ShadowUseCardAction(flock2);
		assertTrue(scn.ShadowDecisionAvailable("Choose action to perform"));
		scn.ShadowChooseMultipleChoiceOption("discard");
//		assertEquals(1, scn.GetShadowCardChoiceCount());
//		scn.ShadowChooseCardBPFromSelection(weather1);
		assertEquals(Zone.DISCARD, flock2.getZone());
		assertEquals(Zone.DECK, weather3.getZone());
		assertEquals(weather3, scn.GetShadowTopOfDeck());

		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		//scn.ShadowDeclineReconciliation();
		scn.ShadowChooseCard("arwen"); // reconciling a card away so we draw the weather from the top
		scn.FreepsChooseToMove();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(flock3));
		assertEquals(Zone.DECK, weather1.getZone());
		assertEquals(weather1, scn.GetShadowBottomOfDeck());
		assertEquals(Zone.HAND, weather2.getZone());
		assertEquals(Zone.HAND, weather3.getZone()); // it was drawn during regroup

		// When the ability is invoked and there is a single weather between both draw deck and discard, the player
		// does not need to choose either pile source nor which copy
		scn.ShadowUseCardAction(flock3);
		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
//		scn.ShadowChooseMultipleChoiceOption("draw deck");
//		assertEquals(1, scn.GetShadowCardChoiceCount());
//		scn.ShadowChooseCardBPFromSelection(weather1);
		assertEquals(Zone.DISCARD, flock3.getZone());
		assertEquals(Zone.DECK, weather1.getZone());
		assertEquals(weather1, scn.GetShadowTopOfDeck());

	}
}
