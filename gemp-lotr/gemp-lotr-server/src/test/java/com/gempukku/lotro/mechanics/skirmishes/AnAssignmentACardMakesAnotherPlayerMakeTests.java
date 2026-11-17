package com.gempukku.lotro.mechanics.skirmishes;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * "Make the Free Peoples player assign this minion to an unwounded companion" (Barbaric Uruk 12_135): who is the
 * one assigning, for a text that reads "each time the Free Peoples player assigns ..."?
 *
 * Ketura's rule, from Q-0251 with Q-0171 (rulings log, "Reveals", 2026-10-11): where a card makes an opponent do a
 * thing and he has a choice in it, he is the one doing it; where he has none, the card's player is.  So the Free
 * Peoples player made to assign, with two unwounded companions to choose between, is the one assigning; with one,
 * the Shadow player is (F-0513, Wold of Rohan 4_336).
 *
 * AssignFpCharacterToSkirmish credits the assignment (AssignmentEffect's doer: the game log, AssignedToSkirmishResult
 * and AssignAgainstResult) to the action's player where the player it names to assign is another and had no choice
 * of the character or the minion.  What may be assigned, and what is paid to assign it, are still judged for the
 * player made to assign.  The reader here is Rohan Is Mine 4_172 ("Each time the Free Peoples player assigns bearer
 * to a skirmish, exert each ally"), on Boromir; Wold of Rohan has the same two boards in its own tests.
 */
public class AnAssignmentACardMakesAnotherPlayerMakeTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("boromir", "1_97");     // Boromir, Son of Denethor: a Man companion; an ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion; an ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally; a maneuver ability used only on purpose
					put("mine", "4_172");       // Rohan Is Mine: on a Free Peoples Man; "Each time the Free Peoples player assigns bearer to a skirmish, exert each ally."
					put("uruk", "12_135");      // Barbaric Uruk: damage +1; "Assignment: Make the Free Peoples player assign this minion to an unwounded companion."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MadeToAssignTheOneUnwoundedCompanionTheFreePeoplesPlayerIsNotTheOneAssigning() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var mine = scn.GetShadowCard("mine");
		var uruk = scn.GetShadowCard("uruk");
		scn.MoveCompanionsToTable(boromir, gimli);
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(boromir, mine);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk);
		// Frodo and Gimli wounded: Boromir is the one unwounded companion.
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(gimli, 1);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(0, scn.GetWoundsOn(thrarin));

		scn.FreepsPass();
		scn.MarkGameLog();
		scn.ShadowUseCardAction(uruk);

		// Nothing to choose: the assignment is the Shadow player's, and Rohan Is Mine does not answer it.
		assertTrue(scn.IsCharAssigned(boromir));
		assertTrue(scn.GameLogSinceMarkSays(VirtualTableScenario.P2 + " assigns"));
		assertFalse(scn.GameLogSinceMarkSays(VirtualTableScenario.P1 + " assigns"));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void MadeToAssignOneOfTwoUnwoundedCompanionsTheFreePeoplesPlayerIsTheOneAssigning() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var mine = scn.GetShadowCard("mine");
		var uruk = scn.GetShadowCard("uruk");
		scn.MoveCompanionsToTable(boromir, gimli);
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(boromir, mine);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk);
		// Frodo wounded: Boromir and Gimli are the unwounded companions.
		scn.AddWoundsToChar(frodo, 1);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(0, scn.GetWoundsOn(thrarin));

		scn.FreepsPass();
		scn.MarkGameLog();
		scn.ShadowUseCardAction(uruk);
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, gimli));
		scn.FreepsChooseCard(boromir);

		// He chose: the assignment is his, and Rohan Is Mine exerts the ally.
		assertTrue(scn.IsCharAssigned(boromir));
		assertTrue(scn.GameLogSinceMarkSays(VirtualTableScenario.P1 + " assigns"));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
