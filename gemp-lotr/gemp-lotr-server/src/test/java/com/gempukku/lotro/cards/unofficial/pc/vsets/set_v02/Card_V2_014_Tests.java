package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_014_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("remember", "102_14");
					put("gandalf", "102_13");
					put("theoden", "4_292");
					put("vcompanion", "5_122");
					put("shadowfax", "8_21");
					put("snowmane", "7_250");

					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RememberYourOldStrengthStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Remember Your Old Strength
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert Gandalf to make Theoden and each valiant companion strength +1 until the regroup phase (or strength +2 if every unbound companion is valiant).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("remember");

		assertEquals("Remember Your Old Strength", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RememberYourOldStrengthPumpsTheodenAndValiantCompanionsBy1WhenNotEveryUnboundCompanionIsValiant() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var remember = scn.GetFreepsCard("remember");
		var gandalf = scn.GetFreepsCard("gandalf");
		var theoden = scn.GetFreepsCard("theoden");
		var vcompanion = scn.GetFreepsCard("vcompanion");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		var snowmane = scn.GetFreepsCard("snowmane");
		scn.MoveCardsToHand(remember);
		scn.MoveCompanionToTable(gandalf, theoden, vcompanion);

		scn.MoveMinionsToTable("runner");

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(4, scn.GetStrength(frodo));
		assertFalse(scn.HasKeyword(frodo, Keyword.VALIANT));
		assertEquals(7, scn.GetStrength(gandalf));
		assertFalse(scn.HasKeyword(gandalf, Keyword.VALIANT));
		assertEquals(6, scn.GetStrength(theoden));
		assertFalse(scn.HasKeyword(theoden, Keyword.VALIANT));
		assertEquals(6, scn.GetStrength(vcompanion));
		assertTrue(scn.HasKeyword(vcompanion, Keyword.VALIANT));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(remember));

		scn.FreepsPlayCard(remember);
		assertEquals(1, scn.GetWoundsOn(gandalf));

		//Only Theoden and the valiant companion are pumped
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(7, scn.GetStrength(theoden));
		assertEquals(7, scn.GetStrength(vcompanion));

		//Lasts until regroup
		scn.SkipToAssignments();
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(7, scn.GetStrength(theoden));
		assertEquals(7, scn.GetStrength(vcompanion));

		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(6, scn.GetStrength(theoden));
		assertEquals(6, scn.GetStrength(vcompanion));
	}

	@Test
	public void RememberYourOldStrengthPumpsTheodenAndValiantCompanionsBy2WhenEveryUnboundCompanionIsValiant() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var remember = scn.GetFreepsCard("remember");
		var gandalf = scn.GetFreepsCard("gandalf");
		var theoden = scn.GetFreepsCard("theoden");
		var vcompanion = scn.GetFreepsCard("vcompanion");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		var snowmane = scn.GetFreepsCard("snowmane");
		scn.MoveCardsToHand(remember);
		scn.MoveCompanionToTable(gandalf, theoden, vcompanion);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.AttachCardsTo(theoden, snowmane);

		scn.MoveMinionsToTable("runner");

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		//Everyone unbound should be mounted and valiant due to Gandalf's game text
		assertEquals(4, scn.GetStrength(frodo));
		assertFalse(scn.HasKeyword(frodo, Keyword.VALIANT));
		assertEquals(7, scn.GetStrength(gandalf));
		assertTrue(scn.HasKeyword(gandalf, Keyword.VALIANT));
		assertEquals(6, scn.GetStrength(theoden));
		assertTrue(scn.HasKeyword(theoden, Keyword.VALIANT));
		assertEquals(6, scn.GetStrength(vcompanion));
		assertTrue(scn.HasKeyword(vcompanion, Keyword.VALIANT));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(remember));

		scn.FreepsPlayCard(remember);
		assertEquals(1, scn.GetWoundsOn(gandalf));

		//Only Theoden and the valiant companion are pumped
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(9, scn.GetStrength(gandalf));
		assertEquals(8, scn.GetStrength(theoden));
		assertEquals(8, scn.GetStrength(vcompanion));

		//Lasts until regroup
		scn.SkipToAssignments();
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(9, scn.GetStrength(gandalf));
		assertEquals(8, scn.GetStrength(theoden));
		assertEquals(8, scn.GetStrength(vcompanion));

		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(6, scn.GetStrength(theoden));
		assertEquals(6, scn.GetStrength(vcompanion));
	}
}
