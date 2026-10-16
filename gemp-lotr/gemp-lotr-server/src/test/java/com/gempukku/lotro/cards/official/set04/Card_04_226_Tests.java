package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_226_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "4_226");
					put("guard2", "4_226");
					// Goblin Runner: a [moria] Orc, strength 5, vitality 1; its "may add (2)" is a play trigger,
					// and it is cheated in
					put("runner", "1_178");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man, strength 8
					// Gimli, Son of Gloin: unbound [dwarven] Dwarf, strength 6; a skirmish ability never used
					put("gimli", "1_13");
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, and not the Ring-bearer
					// Bounder: a Hobbit ally whose home is site 2, where the assignments here are made, so he
					// may be assigned there; only companions are unbound
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EasterlingGuardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Easterling Guard
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Easterling</b>.<br>Each time this minion is assigned to an unbound companion, you may exert him to add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("guard");

		assertEquals("Easterling Guard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.EASTERLING));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignedToAnUnboundCompanionByTheFreePeoplesPlayerItMayExertToAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetShadowCard("guard");
		var guard2 = scn.GetShadowCard("guard2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.MoveMinionsToTable(guard, guard2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, guard}, new PhysicalCardImpl[]{frodo, guard2});

		// Aragorn is an unbound companion; Frodo, the Ring-bearer, is Ring-bound.
		assertTrue(scn.IsCharAssignedAgainst(aragorn, guard));
		assertTrue(scn.IsCharAssignedAgainst(frodo, guard2));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(guard));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(guard2));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetBurdens());

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(guard2));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AssignedToAnUnboundCompanionByTheShadowPlayerItMakesTheSameOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetShadowCard("guard");
		var guard2 = scn.GetShadowCard("guard2");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(gimli, sam);

		scn.StartGame();

		scn.MoveMinionsToTable(guard, guard2);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(new PhysicalCardImpl[]{gimli, guard}, new PhysicalCardImpl[]{sam, guard2});

		// Gimli is an unbound companion of another culture and race than Aragorn; Sam is Ring-bound.
		assertTrue(scn.IsCharAssignedAgainst(gimli, guard));
		assertTrue(scn.IsCharAssignedAgainst(sam, guard2));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(guard));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(guard2));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetBurdens());

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(guard2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AssignedToAnAllyItMakesNoOfferWhileACopyAssignedToACompanionDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetShadowCard("guard");
		var guard2 = scn.GetShadowCard("guard2");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();

		scn.MoveMinionsToTable(guard, guard2, runner);
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{bounder, guard},
				new PhysicalCardImpl[]{aragorn, guard2},
				new PhysicalCardImpl[]{gimli, runner});

		// The copy on the ally offers nothing, and neither copy answers the Runner's assignment to Gimli.
		assertTrue(scn.IsCharAssignedAgainst(bounder, guard));
		assertTrue(scn.IsCharAssignedAgainst(gimli, runner));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(guard2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(guard));
		assertEquals(0, scn.GetBurdens());

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(guard2));
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.FreepsResolveSkirmish(bounder);
		assertTrue(scn.IsCharSkirmishingAgainst(bounder, guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DeclinedNothingIsPaidAndItOffersAgainWhenAssignedAfterTheNextMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetShadowCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.MoveMinionsToTable(guard);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, guard);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(guard));

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.IsCharAssignedAgainst(aragorn, guard));

		// 9 against 8: the Guard wins and Aragorn takes a wound.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, guard);

		// The regroup move, and the assignment phase at site 3.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(aragorn, guard);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(guard));

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnExhaustedGuardMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetShadowCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.MoveMinionsToTable(guard);
		// Vitality 2 with 1 wound: he cannot exert.
		scn.AddWoundsToChar(guard, 1);
		scn.SkipToAssignments();
		assertEquals(1, scn.GetWoundsOn(guard));
		scn.FreepsAssignToMinions(aragorn, guard);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, guard));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(guard));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
