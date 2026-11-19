package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_238_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("menofharad", "4_238");

					put("explorer", "4_250");   // Southron Explorer: [raider] Southron Man; a regroup ability used only on purpose
					put("explorer2", "4_250");
					put("bowman", "4_248");     // Southron Bowman: [raider] Southron Man, an archer; the tests leave before archery
					put("soldier", "4_220");    // Desert Soldier: [raider] Southron Man; a start-of-skirmish text the tests never reach
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man who is no Southron; his text needs 2 burdens
					// Bladetusk Rearguard: a [raider] artifact; "Maneuver: Remove (5) to make this artifact a fierce mounted
					// Southron minion until the end of the turn": a Southron who is no Man
					put("rearguard", "103_43");

					put("aragorn", "1_89");

					// Dunlending Brigands, no text: filler, so that a draw deck still holds cards after the hands are drawn.
					put("chaff1", "4_10");
					put("chaff2", "4_10");
					put("chaff3", "4_10");
					put("chaff4", "4_10");
					put("chaff5", "4_10");
					put("chaff6", "4_10");
					put("chaff7", "4_10");
					put("chaff8", "4_10");
					put("chaff9", "4_10");
					put("chaff10", "4_10");
					put("chaff11", "4_10");
					put("chaff12", "4_10");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MenofHaradStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Men of Harad
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br><b>Shadow:</b> Spot a site you control, spot 3 Southrons and remove (2) to draw a card.
		*/

		var scn = GetScenario();

		var menofharad = scn.GetFreepsCard("menofharad");

		assertEquals("Men of Harad", menofharad.getBlueprint().getTitle());
		assertNull(menofharad.getBlueprint().getSubtitle());
		assertFalse(menofharad.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, menofharad.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, menofharad.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, menofharad.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(menofharad, Keyword.SUPPORT_AREA));
		assertEquals(0, menofharad.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithASiteHeControlsAndThreeSouthronsItRemovesTwoToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var menofharad = scn.GetShadowCard("menofharad");
		var explorer = scn.GetShadowCard("explorer");
		var bowman = scn.GetShadowCard("bowman");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		// Both markers at site 2, so site 1 may be taken.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		scn.MoveCardsToSupportArea(menofharad);
		scn.MoveMinionsToTable(explorer, bowman, soldier);
		assertInZone(Zone.SUPPORT, menofharad);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(menofharad));

		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		int freepsHand = scn.GetFreepsHandCount();
		scn.ShadowUseCardAction(menofharad);

		assertEquals(0, scn.GetTwilight());
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertEquals(freepsHand, scn.GetFreepsHandCount());
		assertInZone(Zone.SUPPORT, menofharad);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileOnlyTheOtherPlayerControlsASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var menofharad = scn.GetShadowCard("menofharad");
		var explorer = scn.GetShadowCard("explorer");
		var bowman = scn.GetShadowCard("bowman");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.FreepsControls(site1));
		scn.MoveCardsToSupportArea(menofharad);
		scn.MoveMinionsToTable(explorer, bowman, soldier);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(menofharad));
	}

	@Test
	public void IsNotOfferedWithTwoSouthronsAndARaiderManWhoIsNoSouthronUntilAThirdSouthronIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var menofharad = scn.GetShadowCard("menofharad");
		var explorer = scn.GetShadowCard("explorer");
		var bowman = scn.GetShadowCard("bowman");
		var axeman = scn.GetShadowCard("axeman");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		scn.MoveCardsToSupportArea(menofharad);
		scn.MoveMinionsToTable(explorer, bowman, axeman);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(menofharad));

		scn.MoveMinionsToTable(soldier);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(menofharad));
	}

	@Test
	public void IsNotOfferedWithOneTwilightInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var menofharad = scn.GetShadowCard("menofharad");
		var explorer = scn.GetShadowCard("explorer");
		var bowman = scn.GetShadowCard("bowman");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		scn.MoveCardsToSupportArea(menofharad);
		scn.MoveMinionsToTable(explorer, bowman, soldier);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(menofharad));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(menofharad));
	}

	@Test
	public void ASouthronWhoIsNoManCountsAmongTheThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var menofharad = scn.GetShadowCard("menofharad");
		var explorer = scn.GetShadowCard("explorer");
		var explorer2 = scn.GetShadowCard("explorer2");
		var axeman = scn.GetShadowCard("axeman");
		var rearguard = scn.GetShadowCard("rearguard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		scn.MoveCardsToSupportArea(menofharad, rearguard);
		// The Easterling is no Southron; he is there so that the turn has a maneuver phase.
		scn.MoveMinionsToTable(explorer, explorer2, axeman);

		// The first Shadow phase: two Southron Men, and the Rearguard is still an artifact.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(menofharad));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowUseCardAction(rearguard);
		assertTrue(scn.HasKeyword(rearguard, Keyword.SOUTHRON));

		// Nobody is assigned, so every minion lives on into the second move.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		// The second Shadow phase of the turn: the Rearguard is a Southron minion until the end of the turn.
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(menofharad));
		int hand = scn.GetShadowHandCount();
		scn.ShadowUseCardAction(menofharad);
		assertEquals(0, scn.GetTwilight());
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
