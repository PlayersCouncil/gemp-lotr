package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_279_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_279");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetEndOfTurnScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thin", "1_279");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the other companion; strength 8
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; cheated in, it adds nothing
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// The Great Ring answers no wound while it is not worn, so the Ring-bearer's own skirmish asks
				// nothing and no burden comes of it.
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void ThinandStretchedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Thin and Stretched
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Game Text: To play, exert a [sauron] Orc. Plays on the Ring-bearer.<br>Add a burden at the end of each turn during which bearer was not assigned to a skirmish (and another companion was).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Thin and Stretched", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AddsABurdenAtTheEndOfATurnInWhichAnotherCompanionWasAssignedToASkirmishAndItsBearerWasNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEndOfTurnScenario();

		var thin = scn.GetShadowCard("thin");
		var runner = scn.GetShadowCard("runner1");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AttachCardsTo(frodo, thin);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, runner));
		assertFalse(scn.IsCharAssigned(frodo));
		scn.FreepsResolveSkirmish(aragorn);

		// The last thing asked in the turn: no burden yet.
		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertAttachedTo(thin, frodo);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("another move"));

		scn.FreepsChooseToStay();
		// The turn has ended and the other player's has begun: the burden came as it ended, and it is the first
		// player's.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());
		// Nothing is played in the other player's turn, and nobody is assigned to a skirmish in it: the first
		// player's burdens read the same as his next turn begins.
		scn.SkipToOtherPlayersTurn();
		assertEquals(1, scn.GetBurdens());
		assertAttachedTo(thin, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AddsNoBurdenAtTheEndOfATurnInWhichItsBearerWasAssignedToASkirmishAsWellAsAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEndOfTurnScenario();

		var thin = scn.GetShadowCard("thin");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AttachCardsTo(frodo, thin);
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { frodo, runner1 }, new PhysicalCardImpl[] { aragorn, runner2 });
		assertTrue(scn.IsCharAssignedAgainst(frodo, runner1));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, runner2));
		// Frodo's 3 loses to the Runner's 5 for one wound; Aragorn's 8 kills the other Runner.
		scn.FreepsResolveSkirmish(frodo);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertAttachedTo(thin, frodo);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("another move"));

		scn.FreepsChooseToStay();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		// Another companion was assigned, and so was the bearer: no burden as the turn ended.
		assertEquals(0, scn.FreepsGetBurdens());
		scn.SkipToOtherPlayersTurn();
		assertEquals(0, scn.GetBurdens());
		assertAttachedTo(thin, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AddsNoBurdenAtTheEndOfATurnInWhichNobodyWasAssignedToASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEndOfTurnScenario();

		var thin = scn.GetShadowCard("thin");
		var runner = scn.GetShadowCard("runner1");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AttachCardsTo(frodo, thin);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		// Neither player assigns the Runner to anybody.
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(aragorn));

		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertAttachedTo(thin, frodo);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("another move"));

		scn.FreepsChooseToStay();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		// The bearer was not assigned, and no other companion was either: no burden as the turn ended.
		assertEquals(0, scn.FreepsGetBurdens());
		scn.SkipToOtherPlayersTurn();
		assertEquals(0, scn.GetBurdens());
		assertAttachedTo(thin, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
