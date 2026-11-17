package com.gempukku.lotro.mechanics.choices;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * "Choose one: assign <a minion> to <a character>; or <do something else>": when the assignment can be done in
 * full.
 *
 * Comprehensive Rules 4.2, "effect": of two actions a player must choose one he is fully capable of performing,
 * if he can (ChoiceOfWhatCanBeDoneInFullTests).  An assignment can be performed in full while some character the
 * text names can be assigned against some minion it names: the pair, not the two halves apart.  Before F-0624 the
 * game asked the minion's half against a character not chosen yet, and an assignment as a branch was never
 * doable in full on any board.
 *
 * The card is Pull of the Ring 18_133, the one card with an assignment as a branch of a choice: "Remove a burden
 * to choose one: assign a [wraith] minion to a companion who has resistance 0; or have a [wraith] minion lose
 * fierce and gain hunter 2 until the regroup phase."  Úlairë Cantëa 12_174 ("Assignment: Assign Úlairë Cantëa to
 * a companion who has resistance 0") is the [wraith] minion, and is the means of having it assigned beforehand.
 * Six burdens take Sam and Boromir (resistance 5) to 0, and they stay at 0 once Pull of the Ring removes one.
 */
public class ChoiceWithAnAssignmentBranchTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pull", "18_133");
					put("cantea", "12_174");    // Úlairë Cantëa, Black Assassin: a fierce [wraith] Nazgûl
					put("sam", "1_311");        // Sam, Son of Hamfast: resistance 5
					put("boromir", "11_57");    // Boromir, Hero of Osgiliath: resistance 5
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WithACompanionAndAMinionThatCanBeAssignedToEachOtherTheAssignmentIsOfferedBesideTheOtherBranch() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(sam, boromir);

		var pull = scn.GetShadowCard("pull");
		var cantea = scn.GetShadowCard("cantea");
		scn.MoveCardsToHand(pull);

		scn.StartGame();
		scn.MoveMinionsToTable(cantea);
		scn.AddBurdens(6);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.FreepsPass();
		scn.ShadowPlayCard(pull);
		assertEquals(5, scn.GetBurdens());
		assertEquals(0, scn.GetResistance(sam));
		assertEquals(0, scn.GetResistance(boromir));

		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChoose("Assign");
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, boromir));
		scn.ShadowChooseCard(boromir);

		// The one [wraith] minion is taken unasked.
		assertTrue(scn.IsCharAssignedAgainst(boromir, cantea));
		assertTrue(scn.HasKeyword(cantea, Keyword.FIERCE));
		assertInZone(Zone.DISCARD, pull);
	}

	@Test
	public void WithACompanionAtResistanceZeroAndOnlyAnAssignedMinionTheAssignmentCannotBeDoneAndTheOtherBranchIsTakenUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(sam, boromir);

		var pull = scn.GetShadowCard("pull");
		var cantea = scn.GetShadowCard("cantea");
		scn.MoveCardsToHand(pull);

		scn.StartGame();
		scn.MoveMinionsToTable(cantea);
		scn.AddBurdens(6);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Úlairë Cantëa assigns himself to Sam.
		scn.FreepsPass();
		scn.ShadowUseCardAction(cantea);
		scn.ShadowChooseCard(sam);
		assertTrue(scn.IsCharAssignedAgainst(sam, cantea));

		scn.FreepsPass();
		scn.ShadowPlayCard(pull);
		assertEquals(5, scn.GetBurdens());

		// Boromir is at resistance 0 and unassigned, and there is a [wraith] minion, but that minion is assigned
		// already: no pair can be assigned, so only the second branch can be done in full, and it is carried out
		// with no question, on the one [wraith] minion.
		assertEquals(0, scn.GetResistance(boromir));
		assertFalse(scn.IsCharAssigned(boromir));
		assertEquals(0, scn.ShadowGetMultipleChoiceCount());
		assertFalse(scn.HasKeyword(cantea, Keyword.FIERCE));
		assertEquals(2, scn.GetKeywordCount(cantea, Keyword.HUNTER));
		assertFalse(scn.IsCharAssigned(boromir));
		assertTrue(scn.IsCharAssignedAgainst(sam, cantea));
		assertInZone(Zone.DISCARD, pull);
	}
}
