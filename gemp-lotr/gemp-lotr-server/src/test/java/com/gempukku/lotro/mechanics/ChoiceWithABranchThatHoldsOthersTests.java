package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * "<do this> or <do that>" where a half is more than one plain effect: "spot 6 burdens to ...", "exert Aragorn
 * to ...", "play a [dwarven] condition from your discard pile".
 *
 * The rules of a choice are in ChoiceOfWhatCanBeDoneInFullTests and ChoiceWithNothingOnAnyBranchTests: a half
 * that can be done in full is what may be chosen (a lone one is carried out with no question); with none, every
 * half is offered and what can be done of it is done; and where no half would do anything, nothing is asked
 * (Ketura on F-0116: "the prompt should be smarter").  That last rule used to be kept only for halves that are
 * a single plain effect.  A half built as an `If`, as a cost and what it buys (`costToEffect`), or as a play
 * could not say that it would do nothing, so the question was put with nothing behind either answer.  Ketura
 * took the proposal to mend that once, in the engine (Q-0220: D, "A-J are all yes").  Each kind now says:
 *
 * - an `If`, by the side its check picks: with the check failed and nothing written for that case, nothing;
 * - a cost and what it buys, by the cost alone.  Comprehensive Rules 4.2, "cost": "you must pay that cost or
 *   you cannot use that card or special ability", so a half whose cost cannot be paid is not one to choose
 *   (F-0222; Ketura on F-0053: "'spot X to Y' should clearly not resolve if X isn't around, even if it's one
 *   branch of a choice"), and neither is a half whose own requirement fails.  A half whose cost can be paid is
 *   never nothing, whatever it buys (Q-0034: either half of "exert A to ..., or exert B to ..." may be used
 *   though it will do nothing);
 * - a play, by whether there is a card that can be played.  A play from a draw deck may always be attempted
 *   (the deck is looked through), unless its definition asks that the card be there.
 *
 * A half of several effects one of which only remembers something for the next still cannot say, and keeps
 * the question (ChoiceWithNothingOnAnyBranchTests, ABranchThatFirstRemembersSomethingKeepsTheQuestion).
 */
public class ChoiceWithABranchThatHoldsOthersTests
{
	protected VirtualTableScenario GetIfScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Dark Whispers: "Skirmish: Spot 3 burdens to make a Nazgul strength +1 and damage +1, or
					// spot 6 burdens to make a Nazgul strength +3 and damage +2."
					put("whispers", "1_210");
					// Savagery to Match Their Numbers: "Skirmish: Make an Uruk-hai strength +2, or spot 5
					// companions to make an Uruk-hai strength +4 and fierce until the regroup phase."
					put("savagery", "1_139");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("lemenya", "1_232");    // Ulaire Lemenya: a Nazgul, strength 9; her text needs a [wraith] condition
					put("savage", "1_151");     // Uruk Savage: an Uruk-hai, strength 5; Damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: an Orc, strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetCostScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Arwen's Fate: "Skirmish: Exert Aragorn to make Arwen strength +3, or exert Arwen to make
					// Aragorn strength +3."
					put("fate", "1_93");
					// Deadly Contest (a landed PC card: no Decipher card reaches a choice of two costs neither of
					// which can be paid without a fault of its own): "Maneuver: Spot an [elven] companion to
					// reinforce a [dwarven] token or spot a [dwarven] companion to reinforce an [elven] token."
					put("contest", "102_7");
					put("elf", "1_53");         // Lorien Elf: an [elven] companion; "To play, spot an Elf" is all
					put("count", "4_69");       // Final Count: an [elven] condition that holds [elven] tokens
					put("troop", "1_177");      // Goblin Patrol Troop: a minion, so that there is a maneuver phase
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: strength 6, vitality 3
					put("savage", "1_151");     // Uruk Savage: strength 5; Damage +1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetPlayScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Defending the Keep: "Fellowship: Spot a Dwarf to draw a card or play a [dwarven] condition
					// from your discard pile."
					put("keep", "5_6");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf
					put("harp", "18_3");        // Thorin's Harp: a [dwarven] support-area condition, cost 0
					put("sword", "1_299");      // Hobbit Sword: a card for the draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AnIfWhoseCheckFailsIsNotOfferedBesideOneThatCanBeDone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetIfScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var whispers = scn.GetShadowCard("whispers");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(whispers);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// 3 burdens and not 6: only the first half can be done, and it is done with no question.
		assertEquals(9, scn.GetStrength(lemenya));
		scn.ShadowPlayCard(whispers);

		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertEquals(10, scn.GetStrength(lemenya));
		assertEquals(1, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TwoIfsWhoseChecksHoldAreBothOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetIfScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var whispers = scn.GetShadowCard("whispers");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(whispers);

		scn.StartGame();
		scn.AddBurdens(6);
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		scn.ShadowPlayCard(whispers);

		assertTrue(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertTrue(scn.ShadowChoiceAvailable("strength +1"));
		scn.ShadowChoose("strength +3");

		assertEquals(12, scn.GetStrength(lemenya));
		assertEquals(2, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IfsWithNobodyToActOnAreNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetIfScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var whispers = scn.GetShadowCard("whispers");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(whispers);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// 3 burdens and no Nazgul: the first half has nobody to strengthen, and the second half's check (6
		// burdens) fails.  The event may be played and nobody is asked which half.
		assertTrue(scn.ShadowPlayAvailable(whispers));
		scn.ShadowPlayCard(whispers);

		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertInDiscard(whispers);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void APlainEffectAndAnIfWithNobodyToActOnAreNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetIfScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var savagery = scn.GetShadowCard("savagery");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(savagery);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// No Uruk-hai, and 2 companions where the second half spots 5.
		assertTrue(scn.ShadowPlayAvailable(savagery));
		scn.ShadowPlayCard(savagery);

		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertEquals(13, scn.GetStrength(troop));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertInDiscard(savagery);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void APlainEffectThatCanBeDoneIsCarriedOutUnaskedBesideAnIfWhoseCheckFails() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetIfScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var savagery = scn.GetShadowCard("savagery");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(savagery);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(savage));
		scn.ShadowPlayCard(savagery);

		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertEquals(7, scn.GetStrength(savage));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TwoCostsThatCanBePaidAreBothOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCostScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var fate = scn.GetFreepsCard("fate");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.MoveCardsToHand(fate);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		scn.FreepsPlayCard(fate);

		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertTrue(scn.FreepsChoiceAvailable("Exert Aragorn"));
		scn.FreepsChoose("Exert Arwen");

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(11, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheOneCostThatCanBePaidIsPaidUnaskedWhateverItBuys() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCostScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var fate = scn.GetFreepsCard("fate");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(fate);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn and no Arwen.  "Exert Arwen to ..." cannot be paid.  "Exert Aragorn to make Arwen strength
		// +3" can: it goes by its cost, and is paid though there is no Arwen to strengthen.
		scn.FreepsPlayCard(fate);

		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TwoCostsThatCannotBePaidForAreNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCostScenario();

		var contest = scn.GetFreepsCard("contest");
		var count = scn.GetFreepsCard("count");
		scn.MoveCardsToHand(contest);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("elf"));
		scn.MoveCardsToSupportArea(count);
		scn.AddTokensToCard(count, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		// An [elven] companion and an [elven] token, and nothing [dwarven].  "Spot an [elven] companion to
		// reinforce a [dwarven] token" has no such token, which its definition requires; "spot a [dwarven]
		// companion to reinforce an [elven] token" has no such companion to spot.  The event may be played (a
		// spot is no cost of playing it), and nobody is asked which half.
		assertTrue(scn.FreepsPlayAvailable(contest));
		scn.FreepsPlayCard(contest);

		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertInDiscard(contest);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void APlayWithNoCardToPlayAndADrawWithNoCardToDrawAreNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var keep = scn.GetFreepsCard("keep");
		var harp = scn.GetFreepsCard("harp");
		scn.MoveCompanionsToTable("gimli");
		// The [dwarven] condition is in hand, where a play from the discard pile does not look.
		scn.MoveCardsToHand(keep, harp);

		scn.StartGame();
		scn.MoveCardsToDiscard(new ArrayList<>(scn.GetFreepsDrawDeck()).toArray(new PhysicalCardImpl[0]));
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsPlayAvailable(keep));
		scn.FreepsPlayCard(keep);

		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, harp);
		assertInDiscard(keep);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void APlayWithACardToPlayIsCarriedOutUnaskedBesideADrawWithNoCardToDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var keep = scn.GetFreepsCard("keep");
		var harp = scn.GetFreepsCard("harp");
		scn.MoveCompanionsToTable("gimli");
		scn.MoveCardsToHand(keep);

		scn.StartGame();
		// The whole draw deck, Thorin's Harp with it, goes to the discard pile.
		scn.MoveCardsToDiscard(new ArrayList<>(scn.GetFreepsDrawDeck()).toArray(new PhysicalCardImpl[0]));
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetFreepsDeckCount());
		assertInDiscard(harp);
		scn.FreepsPlayCard(keep);

		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertInZone(Zone.SUPPORT, harp);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void APlayWithACardToPlayAndADrawWithACardToDrawAreBothOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var keep = scn.GetFreepsCard("keep");
		var harp = scn.GetFreepsCard("harp");
		scn.MoveCompanionsToTable("gimli");
		scn.MoveCardsToHand(keep);
		scn.MoveCardsToDiscard(harp);

		scn.StartGame();

		assertTrue(scn.GetFreepsDeckCount() > 0);
		scn.FreepsPlayCard(keep);

		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertTrue(scn.FreepsChoiceAvailable("Draw a card"));
		scn.FreepsChoose("from your discard pile");

		assertInZone(Zone.SUPPORT, harp);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
