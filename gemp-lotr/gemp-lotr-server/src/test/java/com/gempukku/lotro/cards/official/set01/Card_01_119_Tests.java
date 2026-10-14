package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_119_Tests
{

	// The fellowship is at site 2 (Ettenmoors) in the maneuver phase of every test.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("whatarethey", "1_119");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] ranger
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] ranger
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man who is no ranger
					// Goblin Runner: [moria] Orc, site 4, roaming at site 2
					put("runner", "1_178");
					// Ulaire Lemenya: [wraith] Nazgul, site 3, roaming at site 2
					put("lemenya", "1_232");
					// Goblin Man: [isengard] Orc, site 2, not roaming at site 2; his Shadow ability is used only on purpose
					put("goblinman", "2_42");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhatAreTheyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: What Are They?
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Spot a ranger to discard a roaming minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("whatarethey");

		assertEquals("What Are They?", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingARangerDiscardsAChosenRoamingMinionAndOnlyARoamingOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whatarethey = scn.GetFreepsCard("whatarethey");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var lemenya = scn.GetShadowCard("lemenya");
		var goblinman = scn.GetShadowCard("goblinman");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(whatarethey);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(runner, lemenya, goblinman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(runner, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(lemenya, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(goblinman, Keyword.ROAMING));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(whatarethey));

		scn.FreepsPlayCard(whatarethey);

		// The two roaming minions, of different cultures and races, are offered; the minion at his own site is not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, lemenya));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(goblinman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(lemenya);

		assertInZone(Zone.DISCARD, lemenya, whatarethey);
		assertInZone(Zone.SHADOW_CHARACTERS, runner, goblinman);
		// The ranger is spotted, not exerted.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoRangerToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whatarethey = scn.GetFreepsCard("whatarethey");
		var boromir = scn.GetFreepsCard("boromir");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(whatarethey);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// A [gondor] Man who is no ranger, and a roaming minion to discard.
		assertTrue(scn.HasKeyword(runner, Keyword.ROAMING));
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(whatarethey));
	}

	@Test
	public void AnElvenRangerIsSpottedAndALoneRoamingMinionIsDiscardedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whatarethey = scn.GetFreepsCard("whatarethey");
		var arwen = scn.GetFreepsCard("arwen");
		var runner = scn.GetShadowCard("runner");
		var goblinman = scn.GetShadowCard("goblinman");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(whatarethey);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(runner, goblinman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.HasKeyword(runner, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(goblinman, Keyword.ROAMING));
		assertTrue(scn.FreepsPlayAvailable(whatarethey));

		scn.FreepsPlayCard(whatarethey);

		// Goblin Man is no candidate, so the one roaming minion goes without a question.
		assertInZone(Zone.DISCARD, runner, whatarethey);
		assertInZone(Zone.SHADOW_CHARACTERS, goblinman);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoRoamingMinionItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whatarethey = scn.GetFreepsCard("whatarethey");
		var aragorn = scn.GetFreepsCard("aragorn");
		var goblinman = scn.GetShadowCard("goblinman");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(whatarethey);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(goblinman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.HasKeyword(goblinman, Keyword.ROAMING));
		assertInZone(Zone.SHADOW_CHARACTERS, goblinman);
		// CR 4.2 "effect": with the ranger spotted it may be played though it will do nothing.
		assertTrue(scn.FreepsPlayAvailable(whatarethey));

		scn.FreepsPlayCard(whatarethey);

		assertInZone(Zone.DISCARD, whatarethey);
		assertInZone(Zone.SHADOW_CHARACTERS, goblinman);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
