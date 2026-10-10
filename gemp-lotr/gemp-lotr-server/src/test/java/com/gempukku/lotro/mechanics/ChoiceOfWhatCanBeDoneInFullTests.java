package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * "<do this> or <do that>": which of the two a player may choose.
 *
 * Comprehensive Rules 4.2, "effect": "If the effect of a card or special ability requires you to choose one of two
 * different actions, you must choose an action that you are fully capable of performing (if possible)", and "If
 * the effect of a card or special ability requires you to perform an action and you cannot, you must perform as
 * much as you can and ignore the rest."
 *
 * Ketura on F-0310 (2026-10-09): "Here Lies Balin is an infamous rules issue.  To wit: when you make a choice you
 * must make one you are 'fully capable' of performing, which means that a 1-vitality Orc cannot be hit by either
 * branch.  However, since you then must 'do what you can and ignore the rest', the player should be given a choice
 * and attempt to perform either branch.  In the zero Orc case, I guess we just fizzle and offer neither option."
 *
 * So, for a choice that comes up as an effect:
 * 1. a branch that can be performed in full may be chosen, and while at least one can, a branch that cannot is
 *    not offered (a lone branch that can is carried out without a question);
 * 2. where no branch can be performed in full but something can be done, every branch is offered and what can
 *    be done of the one chosen is done;
 * 3. where nothing can be done on any branch, nothing is asked (F-0116; ChoiceWithNothingOnAnyBranchTests).
 *
 * The card is Here Lies Balin, Son of Fundin: "Maneuver: Exert a Dwarf to wound 2 Orcs or to wound 1 Orc twice."
 * "Wound 2 Orcs" can be performed in full with two Orcs in play; "wound 1 Orc twice" with an Orc that has 2
 * vitality left.
 *
 * Ketura on Q-0209 (2026-10-10): "while the choice is valid (i.e. both sides are 'fully capable' of happening),
 * the eventual secondary choices (who to wound etc) are an extension of that choice and thus must be restricted
 * to only 'fully executed' choices; picking 'wound one Orc twice' means you can't pick a 1-vitality Orc.  However
 * once a choice is broken by having no 'fully capable' branches, then we are free of that restriction and
 * instead fall back on 'do what you can and ignore the rest', which *does* permit us to wound a 1-vitality Orc
 * twice."
 *
 * So, for what is picked once a branch has been taken:
 * 4. under rule 1, "wound X twice" is aimed only at an X able to take both wounds; "wound 2 Orcs" on the same
 *    board may still take an Orc of 1 vitality, which takes the whole of its one wound;
 * 5. under rule 2 any X may be picked, and takes the wound it can;
 * 6. a "wound X twice" that is no branch of a choice is as it was: any X may be picked (Fury of the White
 *    Rider: "Maneuver: Spot 3 twilight tokens and exert Gandalf to wound a minion twice.").
 *
 * The last test is of the branch the game takes without a question: it is the one remembered as chosen, which
 * what follows the choice may read (Halls of My Home).
 */
public class ChoiceOfWhatCanBeDoneInFullTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Here Lies Balin, Son of Fundin: "Maneuver: Exert a Dwarf to wound 2 Orcs or to wound 1 Orc
					// twice."
					put("balin", "1_19");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf, vitality 3
					put("runner1", "1_178");    // Goblin Runner: an Orc, vitality 1
					put("runner2", "1_178");
					put("troop1", "1_177");     // Goblin Patrol Troop: an Orc, vitality 3, no text
					put("troop2", "1_177");
					put("savage", "1_151");     // Uruk Savage: an Uruk-hai and no Orc, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetNoChoiceScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Fury of the White Rider: "Spell.  Maneuver: Spot 3 twilight tokens and exert Gandalf to
					// wound a minion twice."
					put("fury", "5_18");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: vitality 4
					put("runner", "1_178");     // Goblin Runner: vitality 1
					put("troop", "1_177");      // Goblin Patrol Troop: vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRevealScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Halls of My Home: "Fellowship: Exert a Dwarf to reveal the top 3 cards of any draw deck.
					// You may discard 1 Shadow card revealed.  Return the rest in any order."
					put("halls", "1_18");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf, vitality 3
					put("sword", "1_299");      // Hobbit Sword: a Free Peoples card
					put("runner", "1_178");     // Goblin Runner: a Shadow card
					put("savage", "1_151");     // Uruk Savage: a Shadow card
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WithNoOrcNothingCanBeDoneOnEitherBranchAndNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// Its cost can be paid, so the event may be played; with no Orc in play it does nothing.
		assertTrue(scn.FreepsPlayAvailable(balin));
		scn.FreepsPlayCard(balin);

		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInZone(Zone.DISCARD, balin);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithOneOrcOfOneVitalityNeitherCanBeDoneInFullSoBothAreOfferedAndWoundTwoOrcsWoundsTheOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var gimli = scn.GetFreepsCard("gimli");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(balin);

		// There are not two Orcs, and the one there is cannot take two wounds.
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(List.of("Wound 2 Orcs", "Wound 1 Orc twice"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Wound 2 Orcs");

		// As much as can be done is done: the one Orc is wounded.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInZone(Zone.DISCARD, runner1);
		assertInZone(Zone.DISCARD, balin);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithOneOrcOfOneVitalityWoundOneOrcTwiceMayBeChosenTooAndPlacesTheOneWoundItCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var gimli = scn.GetFreepsCard("gimli");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(balin);

		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsChoose("Wound 1 Orc twice");

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInZone(Zone.DISCARD, runner1);
		assertInZone(Zone.DISCARD, balin);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithTwoOrcsOfOneVitalityOnlyWoundTwoOrcsCanBeDoneInFullAndIsDoneWithoutAQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var gimli = scn.GetFreepsCard("gimli");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(balin);

		// Neither Orc can take two wounds, so "wound 1 Orc twice" is not a choice the player is fully capable of.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInZone(Zone.DISCARD, runner1);
		assertInZone(Zone.DISCARD, runner2);
	}

	@Test
	public void WithOneOrcOfThreeVitalityOnlyWoundOneOrcTwiceCanBeDoneInFullAndIsDoneWithoutAQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var gimli = scn.GetFreepsCard("gimli");
		var troop1 = scn.GetShadowCard("troop1");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(balin);

		scn.StartGame();
		// The Uruk-hai is no Orc: it does not make a second Orc for "wound 2 Orcs".
		scn.MoveMinionsToTable(troop1, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(balin);

		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithTwoOrcsOfThreeVitalityBothCanBeDoneInFullAndWoundTwoOrcsWoundsEachOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var gimli = scn.GetFreepsCard("gimli");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(balin);

		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(List.of("Wound 2 Orcs", "Wound 1 Orc twice"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Wound 2 Orcs");

		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(1, scn.GetWoundsOn(troop2));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithTwoOrcsOfThreeVitalityWoundOneOrcTwiceWoundsTheOneChosenTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var gimli = scn.GetFreepsCard("gimli");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(balin);

		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsChoose("Wound 1 Orc twice");
		scn.FreepsChooseCard(troop2);

		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(2, scn.GetWoundsOn(troop2));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithAnOrcOfOneVitalityAndAnOrcOfThreeWoundOneOrcTwiceGoesToTheOrcAbleToTakeBoth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var gimli = scn.GetFreepsCard("gimli");
		var runner1 = scn.GetShadowCard("runner1");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, troop1);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(balin);

		// Two Orcs for the one branch, and one Orc able to take two wounds for the other.
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(List.of("Wound 2 Orcs", "Wound 1 Orc twice"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Wound 1 Orc twice");

		// The branch was open because it can be done in full, so it is done in full: the Orc of 1 vitality is
		// no part of it, and the one Orc who is takes both wounds without a question.
		assertEquals(2, scn.GetWoundsOn(troop1));
		assertInZone(Zone.SHADOW_CHARACTERS, runner1);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithBothBranchesOpenWoundOneOrcTwiceOffersOnlyTheOrcsAbleToTakeBothWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var gimli = scn.GetFreepsCard("gimli");
		var runner1 = scn.GetShadowCard("runner1");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, troop1, troop2);
		// One Patrol Troop has 2 vitality left, the fewest that can take both wounds.
		scn.AddWoundsToChar(troop2, 1);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(balin);

		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(List.of("Wound 2 Orcs", "Wound 1 Orc twice"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Wound 1 Orc twice");

		// Whom to wound is part of the choice made: only an Orc able to take both wounds.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, troop2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner1));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(troop2);

		assertInZone(Zone.DISCARD, troop2);
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertInZone(Zone.SHADOW_CHARACTERS, runner1);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithBothBranchesOpenWoundTwoOrcsMayStillTakeAnOrcOfOneVitality() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var gimli = scn.GetFreepsCard("gimli");
		var runner1 = scn.GetShadowCard("runner1");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, troop1, troop2);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(balin);

		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsChoose("Wound 2 Orcs");

		// One wound each is the whole of this branch, and an Orc of 1 vitality can take it.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner1, troop1, troop2));
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(runner1, troop1);

		assertInZone(Zone.DISCARD, runner1);
		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WoundAMinionTwiceThatIsNoBranchOfAChoiceMayBeAimedAtAMinionOfOneVitality() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNoChoiceScenario();

		var fury = scn.GetFreepsCard("fury");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(fury);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(3);

		assertTrue(scn.FreepsPlayAvailable(fury));
		scn.FreepsPlayCard(fury);
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// No choice between two things to do was made here, so there is nothing to hold the player to: the
		// minion of 1 vitality stands beside the one able to take both wounds, and takes what it can.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(runner);

		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInZone(Zone.DISCARD, fury);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheLoneBranchThatCanBeDoneInFullIsTheOneRememberedAsChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRevealScenario();

		var halls = scn.GetFreepsCard("halls");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(halls);

		scn.StartGame();
		// The Free Peoples player's own deck is left with one card, fewer than the 3 to be revealed, so "your
		// deck" cannot be performed in full; the other deck has 5, with these three on top.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("runner"), scn.GetFreepsCard("savage"));
		scn.MoveCardsToTopOfDeck(sword, savage, runner);
		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(5, scn.GetShadowDeckCount());

		scn.FreepsPlayCard(halls);

		// The second of the two branches is the only one that can be, and is taken without a question.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, false));
		scn.DismissRevealedCards();

		// What follows the choice goes by the branch that was taken: a Shadow card revealed from that deck may
		// be discarded, and the rest go back on it.
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard 1 Shadow card revealed?"));
		scn.FreepsChooseYes();
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCardBPFromSelection(runner);

		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.FreepsDecisionAvailable("Choose a card to put on top of the deck"));
		scn.FreepsChooseCardBPFromSelection(savage);

		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, savage, sword);
		assertInZone(Zone.DISCARD, halls);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
