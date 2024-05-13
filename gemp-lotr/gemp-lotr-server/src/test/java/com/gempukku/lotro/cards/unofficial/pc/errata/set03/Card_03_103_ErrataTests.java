package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_103_ErrataTests
{

	protected GenericCardTestHelper GetUnboundScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("terrible", "53_103");
					put("troll", "6_106");
					put("din", "7_267");
					put("runner", "1_178");

					put("galadriel", "1_45");
					put("greenleaf", "1_50");
					put("arwen", "1_30");
					put("hosts", "2_18");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	protected GenericCardTestHelper GetRBScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("terrible", "53_103");
					put("troll", "6_106");
					put("din", "7_267");
					put("runner", "1_178");

					put("galadriel", "1_45");
					put("greenleaf", "1_50");
					put("arwen", "1_30");
					put("hosts", "2_18");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.GaladrielRB,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void TerribleastheDawnStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 3
		* Title: Terrible as the Dawn
		* Unique: False
		* Side: SHADOW
		* Culture: Sauron
		* Twilight Cost: 0
		* Type: event
		* Subtype: 
		* Game Text: <b>Maneuver:</b> Spot a [sauron] minion to wound Galadriel 3 times.
		 * The Free Peoples player may discard 2 Elves to prevent this.  If Galadriel is
		 * Ring-bound, 2 burdens may be added instead.
		*/

		//Pre-game setup
		var scn = GetUnboundScenario();

		var terrible = scn.GetFreepsCard("terrible");

		assertFalse(terrible.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, terrible.getBlueprint().getSide());
		assertEquals(Culture.SAURON, terrible.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, terrible.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(terrible, Keyword.MANEUVER));
		assertEquals(0, terrible.getBlueprint().getTwilightCost());
	}

	@Test
	public void TerribleastheDawnRequiresSauronMinion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetUnboundScenario();

		var terrible = scn.GetShadowCard("terrible");
		var din = scn.GetShadowCard("din");
		var troll = scn.GetShadowCard("troll");
		scn.ShadowMoveCardToHand(terrible, din, troll);
		scn.ShadowMoveCharToTable("runner"); //needed so that the maneuver phase is accessible

		var galadriel = scn.GetFreepsCard("galadriel");
		scn.FreepsMoveCharToTable(galadriel);

		scn.StartGame();
		scn.SetTwilight(20);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		assertFalse(scn.ShadowPlayAvailable(terrible));
		scn.ShadowMoveCharToTable(troll);

		scn.ShadowPlayCard(din); // we only do this to perform a maneuver action and refresh what actions are available
		scn.ShadowChoose("0");
		scn.FreepsPassCurrentPhaseAction();
		assertEquals(Zone.FREE_CHARACTERS, galadriel.getZone());
		assertTrue(scn.ShadowPlayAvailable(terrible));
		scn.ShadowPlayCard(terrible);
		assertEquals(Zone.DEAD, galadriel.getZone());
	}

	@Test
	public void UnboundGaladrielIsWounded3Times() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetUnboundScenario();

		var terrible = scn.GetShadowCard("terrible");
		var troll = scn.GetShadowCard("troll");
		scn.ShadowMoveCardToHand(terrible);
		scn.ShadowMoveCharToTable(troll);

		var galadriel = scn.GetFreepsCard("galadriel");
		scn.FreepsMoveCharToTable(galadriel);
		scn.FreepsMoveCardToSupportArea("hosts");

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(terrible);

		assertEquals(Zone.FREE_CHARACTERS, galadriel.getZone());
		assertEquals(0, scn.GetWoundsOn(galadriel));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(galadriel));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(galadriel));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(Zone.DEAD, galadriel.getZone());
	}

	@Test
	public void WoundedUnboundGaladrielIsWounded3Times() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetUnboundScenario();

		var terrible = scn.GetShadowCard("terrible");
		var troll = scn.GetShadowCard("troll");
		scn.ShadowMoveCardToHand(terrible);
		scn.ShadowMoveCharToTable(troll);

		var galadriel = scn.GetFreepsCard("galadriel");
		scn.FreepsMoveCharToTable(galadriel);
		scn.FreepsMoveCardToSupportArea("hosts");

		scn.StartGame();

		scn.AddWoundsToChar(galadriel, 2);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(terrible);

		assertEquals(Zone.FREE_CHARACTERS, galadriel.getZone());
		assertEquals(2, scn.GetWoundsOn(galadriel));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(Zone.DEAD, galadriel.getZone());
	}

	@Test
	public void UnboundGaladrielCanDiscard2ElvesToSaveGaladriel() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetUnboundScenario();

		var terrible = scn.GetShadowCard("terrible");
		scn.ShadowMoveCardToHand(terrible);
		scn.ShadowMoveCharToTable("troll");

		var galadriel = scn.GetFreepsCard("galadriel");
		var greenleaf = scn.GetFreepsCard("greenleaf");
		var arwen = scn.GetFreepsCard("arwen");
		scn.FreepsMoveCharToTable(galadriel, greenleaf, arwen);
		scn.FreepsMoveCardToSupportArea("hosts");

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(terrible);

		assertEquals(Zone.FREE_CHARACTERS, galadriel.getZone());
		assertEquals(Zone.FREE_CHARACTERS, greenleaf.getZone());
		assertEquals(Zone.FREE_CHARACTERS, arwen.getZone());

		assertTrue(scn.FreepsDecisionAvailable("Discard 2 Elves to prevent wounding Galadriel"));
		scn.FreepsChooseYes();
		scn.FreepsChooseCards(greenleaf, arwen);

		assertEquals(Zone.FREE_CHARACTERS, galadriel.getZone());
		assertEquals(Zone.DISCARD, greenleaf.getZone());
		assertEquals(Zone.DISCARD, arwen.getZone());
	}

	@Test
	public void RBGaladrielIsWounded3Times() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetRBScenario();

		var terrible = scn.GetShadowCard("terrible");
		var troll = scn.GetShadowCard("troll");
		scn.ShadowMoveCardToHand(terrible);
		scn.ShadowMoveCharToTable(troll);

		var galadriel = scn.GetRingBearer();
		scn.FreepsMoveCardToSupportArea("hosts");

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(terrible);

		scn.FreepsChooseNo();

		assertEquals(Zone.FREE_CHARACTERS, galadriel.getZone());
		assertEquals(0, scn.GetWoundsOn(galadriel));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(galadriel));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(galadriel));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(Zone.DEAD, galadriel.getZone());
	}

	@Test
	public void WoundedRBGaladrielIsWounded3Times() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetRBScenario();

		var terrible = scn.GetShadowCard("terrible");
		var troll = scn.GetShadowCard("troll");
		scn.ShadowMoveCardToHand(terrible);
		scn.ShadowMoveCharToTable(troll);

		var galadriel = scn.GetRingBearer();
		scn.FreepsMoveCardToSupportArea("hosts");

		scn.StartGame();

		scn.AddWoundsToChar(galadriel, 2);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(terrible);

		scn.FreepsChooseNo();

		assertEquals(Zone.FREE_CHARACTERS, galadriel.getZone());
		assertEquals(2, scn.GetWoundsOn(galadriel));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(Zone.DEAD, galadriel.getZone());
	}

	@Test
	public void RBGaladrielCanDiscard2ElvesToSaveGaladriel() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetRBScenario();

		var terrible = scn.GetShadowCard("terrible");
		scn.ShadowMoveCardToHand(terrible);
		scn.ShadowMoveCharToTable("troll");

		var galadriel = scn.GetRingBearer();
		var greenleaf = scn.GetFreepsCard("greenleaf");
		var arwen = scn.GetFreepsCard("arwen");
		scn.FreepsMoveCharToTable(greenleaf, arwen);
		scn.FreepsMoveCardToSupportArea("hosts");

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(terrible);

		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(Zone.FREE_CHARACTERS, galadriel.getZone());
		assertEquals(Zone.FREE_CHARACTERS, greenleaf.getZone());
		assertEquals(Zone.FREE_CHARACTERS, arwen.getZone());

		assertTrue(scn.FreepsDecisionAvailable("Discard 2 Elves or add 2 burdens to prevent wounding Galadriel 3 times"));
		scn.FreepsChooseYes();
		assertTrue(scn.FreepsChoiceAvailable("Discard 2 Elves"));
		assertTrue(scn.FreepsChoiceAvailable("Add 2 burdens"));
		scn.FreepsChooseMultipleChoiceOption("Discard");

		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(Zone.FREE_CHARACTERS, galadriel.getZone());
		assertEquals(Zone.DISCARD, greenleaf.getZone());
		assertEquals(Zone.DISCARD, arwen.getZone());
	}

	@Test
	public void RBGaladrielCanAdd2BurdensToSaveGaladriel() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetRBScenario();

		var terrible = scn.GetShadowCard("terrible");
		scn.ShadowMoveCardToHand(terrible);
		scn.ShadowMoveCharToTable("troll");

		var galadriel = scn.GetRingBearer();
		var greenleaf = scn.GetFreepsCard("greenleaf");
		var arwen = scn.GetFreepsCard("arwen");
		scn.FreepsMoveCharToTable(greenleaf, arwen);
		scn.FreepsMoveCardToSupportArea("hosts");

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(terrible);

		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(1, scn.GetBurdens());
		assertEquals(Zone.FREE_CHARACTERS, galadriel.getZone());
		assertEquals(Zone.FREE_CHARACTERS, greenleaf.getZone());
		assertEquals(Zone.FREE_CHARACTERS, arwen.getZone());

		assertTrue(scn.FreepsDecisionAvailable("Discard 2 Elves or add 2 burdens to prevent wounding Galadriel 3 times"));
		scn.FreepsChooseYes();
		assertTrue(scn.FreepsChoiceAvailable("Discard 2 Elves"));
		assertTrue(scn.FreepsChoiceAvailable("Add 2 burdens"));
		scn.FreepsChooseMultipleChoiceOption("Burdens");

		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(3, scn.GetBurdens());
		assertEquals(Zone.FREE_CHARACTERS, galadriel.getZone());
		assertEquals(Zone.FREE_CHARACTERS, greenleaf.getZone());
		assertEquals(Zone.FREE_CHARACTERS, arwen.getZone());
	}
}
