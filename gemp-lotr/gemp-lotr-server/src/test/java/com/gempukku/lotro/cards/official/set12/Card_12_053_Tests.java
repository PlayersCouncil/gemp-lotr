package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_053_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("leader", "12_53");
					put("leader2", "12_53");
					put("leader3", "12_53");
					put("leader4", "12_53");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; a skirmish ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] ally; a maneuver ability used only on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] companion; an archery ability used only on purpose
					put("saga", "1_114");       // The Saga of Elendil: another [gondor] condition on a [gondor] companion; a skirmish ability used only on purpose

					put("nelya", "2_84");       // Úlairë Nelya: Nazgûl, strength 10; fierce only at 2 burdens or 2 wounds on Frodo
					put("steed", "1_208");      // Black Steed: [wraith] mount POSSESSION on a Nazgûl (strength +2 at a plains)
					put("ring", "9_42");        // Ring of Asperity: [wraith] ARTIFACT on a Nazgûl; its play trigger is not reached (cheated on)
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai; damage +1 is its whole text
					put("banner", "6_59");      // Banner of Isengard: [isengard] hand weapon possession; answers a win by its bearer only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc; its play trigger is not reached (cheated in)
					put("spear", "1_182");      // Goblin Spear: [moria] hand weapon possession; answers a kill by its bearer only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ValorousLeaderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Valorous Leader
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Bearer must be a [gondor] companion. Limit 1 per bearer.<br><b>Skirmish:</b> Discard this condition to discard a possession borne by a minion skirmishing bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("leader");

		assertEquals("Valorous Leader", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnAGondorCompanionOfAnyRaceButNotOnAnAllyOrACompanionOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leader = scn.GetFreepsCard("leader");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, pippin, legolas);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(leader);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(leader));
		scn.FreepsPlayCard(leader);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant, legolas, scn.GetRingBearer()));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		assertEquals(pippin, leader.getAttachedTo());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OneToABearerAndAnotherGondorConditionDoesNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leader = scn.GetFreepsCard("leader");
		var leader2 = scn.GetFreepsCard("leader2");
		var leader3 = scn.GetFreepsCard("leader3");
		var leader4 = scn.GetFreepsCard("leader4");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var saga = scn.GetFreepsCard("saga");
		scn.MoveCompanionsToTable(aragorn, boromir, pippin);
		scn.AttachCardsTo(aragorn, leader);
		scn.AttachCardsTo(pippin, saga);
		scn.MoveCardsToHand(leader2, leader3, leader4);

		scn.StartGame();
		assertEquals(aragorn, leader.getAttachedTo());
		assertEquals(pippin, saga.getAttachedTo());
		assertInZone(Zone.HAND, leader2, leader3, leader4);

		// Aragorn bears one already; Pippin bears a [gondor] condition of another title; Boromir bears nothing.
		scn.FreepsPlayCard(leader2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(pippin, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(boromir);
		assertEquals(boromir, leader2.getAttachedTo());

		// Pippin is the one bearer left, and takes it unasked.
		scn.FreepsPlayCard(leader3);
		assertEquals(pippin, leader3.getAttachedTo());

		// Every [gondor] companion bears one: the fourth cannot be played.
		assertFalse(scn.FreepsPlayAvailable(leader4));
		assertInZone(Zone.HAND, leader4);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void InBearersSkirmishItDiscardsAPossessionOfAMinionSkirmishingHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leader = scn.GetFreepsCard("leader");
		var leader2 = scn.GetFreepsCard("leader2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, leader);
		scn.AttachCardsTo(boromir, leader2);

		var nelya = scn.GetShadowCard("nelya");
		var steed = scn.GetShadowCard("steed");
		var ring = scn.GetShadowCard("ring");
		var savage = scn.GetShadowCard("savage");
		var banner = scn.GetShadowCard("banner");

		scn.StartGame();
		assertEquals(aragorn, leader.getAttachedTo());
		assertEquals(boromir, leader2.getAttachedTo());
		assertInZone(Zone.ATTACHED, leader, leader2);

		scn.MoveMinionsToTable(nelya, savage);
		scn.AttachCardsTo(nelya, steed, ring);
		scn.AttachCardsTo(savage, banner);
		assertEquals(nelya, steed.getAttachedTo());
		assertEquals(nelya, ring.getAttachedTo());
		assertEquals(savage, banner.getAttachedTo());
		assertInZone(Zone.ATTACHED, steed, ring, banner);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(leader));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.ShadowAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.FreepsActionAvailable(leader));
		scn.FreepsUseCardAction(leader);
		assertInZone(Zone.DISCARD, leader);
		// The other copy, on Boromir, stays.
		assertEquals(boromir, leader2.getAttachedTo());

		// A possession of either minion skirmishing him; not the artifact.
		assertTrue(scn.FreepsHasCardChoiceAvailable(steed, banner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ring));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(banner);

		assertInZone(Zone.DISCARD, banner);
		assertEquals(nelya, steed.getAttachedTo());
		assertEquals(nelya, ring.getAttachedTo());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InAnotherCompanionsSkirmishItIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leader = scn.GetFreepsCard("leader");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, leader);

		var nelya = scn.GetShadowCard("nelya");
		var steed = scn.GetShadowCard("steed");
		var savage = scn.GetShadowCard("savage");
		var banner = scn.GetShadowCard("banner");
		var runner = scn.GetShadowCard("runner");
		var spear = scn.GetShadowCard("spear");

		scn.StartGame();
		assertEquals(aragorn, leader.getAttachedTo());
		assertInZone(Zone.ATTACHED, leader);

		scn.MoveMinionsToTable(nelya, savage, runner);
		scn.AttachCardsTo(nelya, steed);
		scn.AttachCardsTo(savage, banner);
		scn.AttachCardsTo(runner, spear);
		assertEquals(nelya, steed.getAttachedTo());
		assertEquals(savage, banner.getAttachedTo());
		assertEquals(runner, spear.getAttachedTo());
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, nelya}, new PhysicalCardImpl[]{boromir, runner});
		scn.ShadowAssignToMinions(aragorn, savage);
		// Boromir's skirmish first: Aragorn's minions are assigned to him and waiting.
		scn.FreepsResolveSkirmish(boromir);

		assertTrue(scn.FreepsActionAvailable(leader));
		scn.FreepsUseCardAction(leader);

		// No minion is skirmishing Aragorn: nothing is offered and nothing is discarded.
		assertInZone(Zone.DISCARD, leader);
		assertEquals(runner, spear.getAttachedTo());
		assertEquals(nelya, steed.getAttachedTo());
		assertEquals(savage, banner.getAttachedTo());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
