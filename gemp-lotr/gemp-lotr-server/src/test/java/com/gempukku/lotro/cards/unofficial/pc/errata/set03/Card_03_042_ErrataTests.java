package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_042_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horn", "53_42");
					put("boromir", "1_96");     // Boromir, Lord of Gondor: no ability to use
					// A second, different Boromir, cheated in beside the first (each is unique, so legal play never has both).
					put("boromir2", "1_97");    // Boromir, Son of Denethor: his only text is a skirmish ability, never used here
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion who is not Boromir

					// Allies whose homes (3 and 6 of the Fellowship path) are not site 2, where the tests are fought.
					put("dinendal", "3_12");    // Dinendal, Silent Scout: [elven] Elf ally, archer, strength 3, home 6
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] Dwarf ally, strength 3, home 3

					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; cheated in, so it adds nothing
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1 and no other text
					put("marksman", "1_176");   // Goblin Marksman: archer, strength 7, vitality 1, no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HornOfBoromirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Horn of Boromir
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Game Text: Bearer must be Boromir.
		 * Maneuver: Exert Boromir and discard this possession to spot an ally.  Until the regroup phase, that ally
		 * is strength +3 and participates in archery fire and skirmishes.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horn");

		assertEquals("Horn of Boromir", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnBoromirForOneAndCannotBePlayedWithoutHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var boromir = scn.GetFreepsCard("boromir");
		var boromir2 = scn.GetFreepsCard("boromir2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(horn, boromir);

		scn.StartGame();

		// Aragorn and Frodo are companions, and neither is Boromir.
		assertFalse(scn.FreepsPlayAvailable(horn));

		scn.FreepsPlayCard(boromir);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(horn));

		// With a second Boromir beside the first, either may bear the Horn and the others may not.
		scn.MoveCompanionsToTable(boromir2);
		scn.FreepsPlayCard(horn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, boromir2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(boromir);

		assertEquals(4, scn.GetTwilight());
		assertAttachedTo(horn, boromir);
		assertNotAttachedTo(horn, boromir2);
		assertNotAttachedTo(horn, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverAbilityExertsBoromirAndDiscardsHornToMakeAnAllyStrengthPlus3AndSkirmishUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var dinendal = scn.GetFreepsCard("dinendal");
		var grimir = scn.GetFreepsCard("grimir");
		var savage = scn.GetShadowCard("savage");
		var boromir2 = scn.GetFreepsCard("boromir2");
		scn.MoveCompanionsToTable(boromir, boromir2, aragorn);
		scn.AttachCardsTo(boromir, horn);
		scn.MoveAlliesToTable(dinendal, grimir);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(boromir2));
		assertEquals(3, scn.GetStrength(dinendal));
		assertEquals(3, scn.GetStrength(grimir));
		assertTrue(scn.FreepsActionAvailable(horn));

		scn.FreepsUseCardAction(horn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(dinendal, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, boromir2, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(dinendal);

		// The bearer paid the exertion: the other Boromir did not.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(boromir2));
		assertInDiscard(horn);
		assertEquals(6, scn.GetStrength(dinendal));
		assertEquals(3, scn.GetStrength(grimir));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		// Neither ally is at home at site 2: only the one the Horn chose may be assigned.
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(dinendal));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(grimir));
		scn.FreepsAssignToMinions(dinendal, savage);
		scn.FreepsResolveSkirmish(dinendal);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(dinendal));
		assertEquals(3, scn.GetStrength(grimir));

		// Dinendal's arrow in the archery phase was the first wound on the Savage.
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.PassSkirmishActions();
		// Strength 6 against 5: Dinendal wins.
		assertEquals(0, scn.GetWoundsOn(dinendal));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(3, scn.GetStrength(dinendal));

		// A second move in the same turn: the participation has ended with the strength.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("Assign"));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(dinendal));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
	}

	@Test
	public void TheArcherAllyChosenAddsToTheFellowshipArcheryTotalAndMayTakeArcheryWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var dinendal = scn.GetFreepsCard("dinendal");
		var grimir = scn.GetFreepsCard("grimir");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.AttachCardsTo(boromir, horn);
		scn.MoveAlliesToTable(dinendal, grimir);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.MANEUVER);

		// No companion is an archer, and Dinendal away from home does not count.
		assertEquals(0, scn.GetFreepsArcheryTotal());

		scn.FreepsUseCardAction(horn);
		scn.FreepsChooseCard(dinendal);
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.SkipToArcheryWounds();
		assertEquals(0, scn.GetWoundsOn(dinendal));
		assertTrue(scn.FreepsHasCardChoiceAvailable(dinendal, boromir, aragorn, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(grimir));
		scn.FreepsChooseCard(dinendal);
		assertEquals(1, scn.GetWoundsOn(dinendal));
		assertEquals(0, scn.GetWoundsOn(grimir));
		// Dinendal's arrow killed the Marksman, so there is nothing to assign and the game goes to regroup.
		assertInDiscard(marksman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AbilityIsNotOfferedWhileBoromirIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var dinendal = scn.GetFreepsCard("dinendal");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.AttachCardsTo(boromir, horn);
		scn.MoveAlliesToTable(dinendal);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(boromir, 2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(aragorn));
		assertFalse(scn.FreepsActionAvailable(horn));
		assertAttachedTo(horn, boromir);
	}

	@Test
	public void WithNoAllyToSpotTheAbilityIsStillOfferedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var boromir = scn.GetFreepsCard("boromir");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, horn);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.FreepsActionAvailable(horn));

		scn.FreepsUseCardAction(horn);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertInDiscard(horn);
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
