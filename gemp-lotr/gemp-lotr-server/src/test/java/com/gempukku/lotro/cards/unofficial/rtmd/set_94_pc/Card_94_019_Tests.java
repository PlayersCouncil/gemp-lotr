package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_019_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");  // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_96");  // Boromir, Lord of Gondor: second companion, for the choice; a skirmish ability used only on purpose
					put("bounder", "1_286"); // Bounder: [shire] Hobbit ally, vitality 2; a skirmish ability used only on purpose
					put("troop", "1_177");   // Goblin Patrol Troop: a minion with no text, so that there is an archery phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_19", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");  // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_96");  // Boromir, Lord of Gondor: second companion, for the choice; a skirmish ability used only on purpose
					put("bounder", "1_286"); // Bounder: [shire] Hobbit ally, vitality 2; a skirmish ability used only on purpose
					put("troop", "1_177");   // Goblin Patrol Troop: a minion with no text, so that there is an archery phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_19"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_19
		 * Type: MetaSite
		 * Intensity: 2
		 * Game Text: At the start of each archery phase, exert one of your companions.
		 */

		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		assertEquals("Race Text 94_19", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(2, mod.getBlueprint().getIntensity());
	}

	@Test
	public void MandatoryTriggerExertsAChosenCompanionEachArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();

		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveMinionsToTable(troop);
		scn.StartGame();
		scn.SkipToPhase(Phase.ARCHERY);

		// Mandatory: a card choice is demanded, not a yes/no
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir));
		// The Ring-bearer is a companion as well; the minion is none.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void NoWoundWhenTheRunnerIsShadowSinceYourCompanionFilterIsEmpty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveMinionsToTable(troop);
		scn.StartGame();
		scn.SkipToPhase(Phase.ARCHERY);

		// CanSpot(your,companion) finds nothing for the Shadow owner - trigger does not fire
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void NothingHappensAsTheManeuverPhaseBeginsAndTheOwnerExertsACompanionAgainInTheArcheryPhaseOfHisNextTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveMinionsToTable(troop);
		scn.StartGame();

		// The phase before: nobody is asked to choose and nobody is exerted.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.PassCurrentPhaseActions();
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		// An ally is no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder, troop));
		scn.FreepsChooseCard(frodo);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		// The minion is taken off the table so that the turn ends with no skirmish; player 2's turn has no
		// minion and so no archery phase.
		scn.MoveCardsToDiscard(troop);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		// The owner's next turn.
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(frodo);

		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void TheOwnerInPlayerTwosSeatExertsOneOfHisCompanionsInTheArcheryPhaseOfHisOwnTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var aragorn = scn.GetShadowCard("aragorn");
		var frodo = scn.GetShadowRingBearer();
		var troop = scn.GetFreepsCard("troop");

		scn.StartGame();

		// Player 2's turn: he owns the modifier and is now the Free Peoples player.
		scn.SkipToOtherPlayersTurn();
		scn.MoveCompanionsToTable(aragorn);
		scn.ShadowPass(); // player 2's fellowship moves
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhaseInverted(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, frodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// Player 2, the Free Peoples player of this turn, is asked first in the archery phase.
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
