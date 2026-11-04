package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_041_Tests
{
	// Every skirmish here is fought at site 2 of the Fellowship path, Ettenmoors, where the Bounder is at home.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("controlled", "15_41");
					put("controlled2", "15_41");
					put("gollum", "5_24");       // Gollum, Nasty Treacherous Creature: [gollum] minion, strength 5, vitality 4, site 3; a regroup ability only
					put("wraithgollum", "103_66"); // Gollum, Half a Wraith Himself: the [wraith] Gollum; a trigger on playing a Nazgûl and an assignment ability, neither used
					put("savage", "1_151");      // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("runner", "1_178");      // Goblin Runner: [moria] Orc, strength 5, vitality 1; its trigger is on its play only, and it is cheated in
					put("runner2", "1_178");

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability, never used
					put("gimli", "1_13");        // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("bounder", "1_286");     // Bounder: Hobbit ally, home site 2; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ControlledByTheRingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Controlled By The Ring
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype:
		 * Game Text: Bearer must be Gollum.
		 * <b>Skirmish:</b> If Gollum is not assigned to a skirmish, discard this condition to have him replace a minion skirmishing a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("controlled");

		assertEquals("Controlled By The Ring", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CanBePlayedOnlyOnceGollumIsInPlayAndGoesOnHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var controlled = scn.GetShadowCard("controlled");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(controlled, gollum);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A minion who is no Gollum is no bearer.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(controlled));

		scn.ShadowPlayCard(gollum);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);

		assertTrue(scn.ShadowPlayAvailable(controlled));
		scn.ShadowPlayCard(controlled);

		// Gollum is the one bearer it can have, and it goes on him unasked.
		assertAttachedTo(controlled, gollum);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void UnassignedGollumReplacesAChosenMinionInTheSkirmishAndOnlyThatCopyIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var controlled = scn.GetShadowCard("controlled");
		var controlled2 = scn.GetShadowCard("controlled2");
		var gollum = scn.GetShadowCard("gollum");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, savage, runner, runner2);
		scn.AttachCardsTo(gollum, controlled, controlled2);
		assertAttachedTo(controlled, gollum);
		assertAttachedTo(controlled2, gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{gimli, runner2});
		// The Shadow player puts the first Goblin Runner on Aragorn too, and leaves Gollum out.
		scn.ShadowAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertFalse(scn.IsCharAssigned(gollum));
		assertAttachedTo(controlled, gollum);
		assertAttachedTo(controlled2, gollum);

		assertTrue(scn.ShadowActionAvailable(controlled));
		scn.ShadowUseCardAction(controlled);

		// The cost: this copy, and not the other.
		assertInDiscard(controlled);
		assertAttachedTo(controlled2, gollum);

		// Either minion skirmishing Aragorn may be the one replaced; the Goblin Runner assigned to Gimli, whose
		// skirmish has not begun, is skirmishing nobody.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner2, gollum));
		scn.ShadowChooseCard(savage);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, gollum));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertFalse(scn.IsCharSkirmishing(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.IsCharAssignedAgainst(gimli, runner2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Gollum is now in a skirmish, so the other copy is not on offer.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(controlled2));
	}

	@Test
	public void NotOfferedWhileGollumIsAssignedThoughAnotherMinionIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var controlled = scn.GetShadowCard("controlled");
		var gollum = scn.GetShadowCard("gollum");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, savage, runner);
		scn.AttachCardsTo(gollum, controlled);
		assertAttachedTo(controlled, gollum);
		scn.SkipToAssignments();
		// Gollum waits for Gimli; the Goblin Runner is left out of every skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, savage}, new PhysicalCardImpl[]{gimli, gollum});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, savage));
		assertTrue(scn.IsCharAssignedAgainst(gimli, gollum));
		assertFalse(scn.IsCharAssigned(runner));
		assertAttachedTo(controlled, gollum);

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(controlled));
	}

	@Test
	public void WithOnlyAnAllyInTheSkirmishItIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var controlled = scn.GetShadowCard("controlled");
		var gollum = scn.GetShadowCard("gollum");
		var savage = scn.GetShadowCard("savage");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, savage);
		scn.AttachCardsTo(gollum, controlled);
		assertAttachedTo(controlled, gollum);
		scn.SkipToAssignments();
		// At site 2 the Bounder is at home and may be assigned.
		scn.FreepsAssignToMinions(bounder, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(bounder);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(bounder, savage));
		assertFalse(scn.IsCharAssigned(gollum));

		// An ally is no companion: the condition is paid for, and the Savage goes on fighting the Bounder.
		assertTrue(scn.ShadowActionAvailable(controlled));
		scn.ShadowUseCardAction(controlled);

		assertInDiscard(controlled);
		assertTrue(scn.IsCharSkirmishingAgainst(bounder, savage));
		assertFalse(scn.IsCharSkirmishing(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheWraithGollumBearsItAndReplacesTheMinionSkirmishingTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var controlled = scn.GetShadowCard("controlled");
		var gollum = scn.GetShadowCard("wraithgollum");
		var savage = scn.GetShadowCard("savage");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(controlled);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, savage);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(controlled));
		scn.ShadowPlayCard(controlled);
		assertAttachedTo(controlled, gollum);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, savage));
		assertFalse(scn.IsCharAssigned(gollum));

		assertTrue(scn.ShadowActionAvailable(controlled));
		scn.ShadowUseCardAction(controlled);

		// The Savage is the only minion in the skirmish: he is replaced unasked.
		assertInDiscard(controlled);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, gollum));
		assertFalse(scn.IsCharSkirmishing(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
