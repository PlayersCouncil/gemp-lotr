package com.gempukku.lotro.mechanics.choices;

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
 * "<do this> or <do that>": the player is asked which only when there is something to choose between.
 *
 * Ketura on F-0116 (2026-10-06): "Yeah the prompt should be smarter."  A choice whose every branch has nothing
 * left to act on is not put to the player.  F-0293 found the same pointless question on Sword-wall, a card whose
 * definition had no guard of its own, so the rule is now the definition word `choice`'s.
 *
 * As the word stood, it asked in two cases and still does.  Two or more branches that can be carried out in full
 * are put to the player (exactly one is taken without asking).  With none, the player is asked to pick among all
 * of them, so that a branch that can be done in part still is (Dwarrowdelf Chamber with one companion to exert
 * where it wants two).  What has gone is the question in that second case when each branch says for itself that
 * it is certain to do nothing: nobody to exert, heal or wound, no burden to remove, no site to liberate.  A
 * branch that cannot tell keeps the question.  So does a branch whose first effect only remembers something for
 * the next, since the later effect cannot be asked before that memory is filled: no printed card's choice has
 * such a branch, and that is built and not tested (Ketura, 2026-10-11: a test shows what a card that exists
 * does).  (Until engine pass 26 every branch of several effects, and every one that holds others, kept the
 * question: ChoiceWithABranchThatHoldsOthersTests.)
 *
 * A reveal from a draw deck with nothing in it is certain to do nothing as well (engine pass 21, Q-0163), so a
 * choice of which deck to reveal from is not put while every such deck is empty.
 */
public class ChoiceWithNothingOnAnyBranchTests
{
	protected VirtualTableScenario GetEventScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Sword-wall: "Skirmish or Regroup: Spot 3 Elf companions to liberate a site or exert a
					// minion."
					put("swordwall", "4_84");
					put("elf1", "1_53");        // Lorien Elf: an Elf companion; "To play, spot an Elf" is all
					put("elf2", "1_53");
					put("elf3", "1_53");
					put("savage", "1_151");     // Uruk Savage: vitality 3; Damage +1 is its whole text
					put("runner", "1_178");     // Goblin Runner: vitality 1, so it can never exert
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
					put("halls2", "1_18");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf, vitality 3
					put("sword", "1_299");      // Hobbit Sword: a Free Peoples card
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTriggerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Pippin, Hobbit of Some Intelligence: "When you play Pippin, remove a burden or wound from
					// a companion."
					put("pippin", "1_307");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSiteScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>(),
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					// Dwarrowdelf Chamber: "When the fellowship moves to Dwarrowdelf Chamber, Gimli or 2 other
					// companions must exert."
					put("site4", "1_344");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AChoiceWithNothingOnEitherBranchIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEventScenario();

		var swordwall = scn.GetFreepsCard("swordwall");
		scn.MoveCardsToHand(swordwall);
		scn.MoveCompanionsToTable("elf1", "elf2", "elf3");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// Nobody controls a site and there is no minion: the event may be played, and does nothing.
		assertTrue(scn.FreepsPlayAvailable(swordwall));
		scn.FreepsPlayCard(swordwall);

		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertInZone(Zone.DISCARD, swordwall);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AChoiceIsNotAskedWhenWhatABranchWouldActOnCannotBeActedOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEventScenario();

		var swordwall = scn.GetFreepsCard("swordwall");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(swordwall);
		scn.MoveCompanionsToTable("elf1", "elf2", "elf3");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.REGROUP);

		// The only minion has 1 vitality and cannot exert.
		scn.FreepsPlayCard(swordwall);

		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInZone(Zone.DISCARD, swordwall);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheOneBranchWithSomethingToActOnIsCarriedOutUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEventScenario();

		var swordwall = scn.GetFreepsCard("swordwall");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(swordwall);
		scn.MoveCompanionsToTable("elf1", "elf2", "elf3");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPlayCard(swordwall);

		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ASiteToLiberateAndNoMinionLiberatesUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEventScenario();

		var swordwall = scn.GetFreepsCard("swordwall");
		scn.MoveCardsToHand(swordwall);
		scn.MoveCompanionsToTable("elf1", "elf2", "elf3");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.IsSiteControlled(site1));
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPlayCard(swordwall);

		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertFalse(scn.IsSiteControlled(site1));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TwoBranchesWithSomethingToActOnAreAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEventScenario();

		var swordwall = scn.GetFreepsCard("swordwall");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(swordwall);
		scn.MoveCompanionsToTable("elf1", "elf2", "elf3");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPlayCard(swordwall);

		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertTrue(scn.FreepsChoiceAvailable("Liberate a site"));
		assertTrue(scn.FreepsChoiceAvailable("Exert a minion"));
		scn.FreepsChoose("Liberate a site");

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ARequiredTriggersChoiceWithNothingOnEitherBranchIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTriggerScenario();

		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCardsToHand(pippin);

		scn.StartGame();

		// No burden and no wounded companion.
		assertEquals(0, scn.GetBurdens());
		scn.FreepsPlayCard(pippin);

		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARequiredTriggersChoiceWithSomethingOnBothBranchesIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTriggerScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(pippin);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.AddWoundsToChar(frodo, 1);

		scn.FreepsPlayCard(pippin);

		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsChoose("Remove a burden");

		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ABranchThatCanBeDoneInPartKeepsTheQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteScenario();

		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToSite(3);

		// There is no Gimli, and one companion where "2 other companions" are wanted: neither branch can be
		// carried out in full, and one of them can be carried out in part.
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsPass();

		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertTrue(scn.FreepsChoiceAvailable("Exert Gimli"));
		scn.FreepsChoose("Exert 2 other companions");

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheSameChoiceWithNobodyAbleToExertIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteScenario();

		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToSite(3);

		// Frodo is exhausted: nobody can exert on either branch.
		scn.AddWoundsToChar(frodo, 3);
		scn.FreepsPass();

		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(4, scn.GetCurrentSiteNumber());
	}

	@Test
	public void AChoiceOfWhichDrawDeckToRevealFromIsNotAskedWhileBothAreEmpty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRevealScenario();

		var halls = scn.GetFreepsCard("halls");
		var halls2 = scn.GetFreepsCard("halls2");
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(halls, halls2);

		scn.StartGame();
		scn.MoveCardsToDiscard(new ArrayList<>(scn.GetFreepsDrawDeck()).toArray(new PhysicalCardImpl[0]));
		scn.MoveCardsToDiscard(new ArrayList<>(scn.GetShadowDrawDeck()).toArray(new PhysicalCardImpl[0]));
		scn.FreepsRefreshDecision();

		// Neither deck has a card to reveal: the event is paid for and nobody is asked which deck.
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetShadowDeckCount());
		scn.FreepsPlayCard(halls);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInDiscard(halls);
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// One card in the Shadow player's deck, fewer than the 3 to be revealed.  Neither branch can be carried
		// out in full, and one of them will show a card: the question is put, as it always was.
		scn.MoveCardsToTopOfDeck(sword);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(halls2);

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsChoose("opponent's deck");
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, false));
		// The card is being shown to both players
		assertTrue(scn.FreepsDecisionAvailable("revealed card(s) from top of deck"));
	}

	protected VirtualTableScenario GetTaughtBranchScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Duality: a [gollum] Fellowship event, cost 0.  "Spot Sméagol to choose a Shadow player who must
					// discard a Shadow card from hand or remove (2)."  The Shadow player chooses.
					put("duality", "13_47");
					put("duality2", "13_47");
					put("smeagol", "5_28");     // Sméagol, Old Noser: a regroup ability, not used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A branch that says it will do nothing only because it was taught to say so for the rule of Q-0224 (twilight
	// removed from an empty pool, a card discarded from a hand with none to take) is heard by the choice as well
	// (engine pass 32).  Until then it kept the question, and a definition wanted an If round the choice (Seeing
	// Stone of Orthanc 9_38, F-0471).  The whole-hand discard is not heard (Q-0255): Pelennor Flat 7_345's file.
	@Test
	public void AChoiceWhoseBranchesWereTaughtToSayTheyDoNothingIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTaughtBranchScenario();

		var duality = scn.GetFreepsCard("duality");
		var duality2 = scn.GetFreepsCard("duality2");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(duality, duality2);

		scn.StartGame();

		// No card in the Shadow player's hand and nothing in the pool: the event is played and nobody is asked.
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(duality);

		assertInDiscard(duality);
		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// With (1) in the pool, removing (2) cannot be done in full and would still remove something: the
		// question is put, as it always was, and the Shadow player may take either.
		scn.SetTwilight(1);
		scn.FreepsPlayCard(duality2);

		assertTrue(scn.ShadowDecisionAvailable("Choose action to perform"));
		scn.ShadowChoose("Remove");
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
