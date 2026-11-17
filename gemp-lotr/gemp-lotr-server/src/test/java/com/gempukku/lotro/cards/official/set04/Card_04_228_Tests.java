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

public class Card_04_228_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lieutenant", "4_228");
					put("lieutenant2", "4_228");
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
	public void EasterlingLieutenantStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Easterling Lieutenant
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Easterling</b>.<br>Each time this minion is assigned to an unbound companion, you may exert him to add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lieutenant");

		assertEquals("Easterling Lieutenant", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.EASTERLING));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignedToAnUnboundCompanionByTheFreePeoplesPlayerItMayExertToAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lieutenant = scn.GetShadowCard("lieutenant");
		var lieutenant2 = scn.GetShadowCard("lieutenant2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.MoveMinionsToTable(lieutenant, lieutenant2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lieutenant}, new PhysicalCardImpl[]{frodo, lieutenant2});

		// Aragorn is an unbound companion; Frodo, the Ring-bearer, is Ring-bound.
		assertTrue(scn.IsCharAssignedAgainst(aragorn, lieutenant));
		assertTrue(scn.IsCharAssignedAgainst(frodo, lieutenant2));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lieutenant));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(lieutenant2));
		assertEquals(0, scn.GetWoundsOn(lieutenant));
		assertEquals(0, scn.GetBurdens());

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(lieutenant));
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(lieutenant2));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, lieutenant));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AssignedToAnUnboundCompanionByTheShadowPlayerItMakesTheSameOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lieutenant = scn.GetShadowCard("lieutenant");
		var lieutenant2 = scn.GetShadowCard("lieutenant2");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(gimli, sam);

		scn.StartGame();

		scn.MoveMinionsToTable(lieutenant, lieutenant2);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(new PhysicalCardImpl[]{gimli, lieutenant}, new PhysicalCardImpl[]{sam, lieutenant2});

		// Gimli is an unbound companion of another culture and race than Aragorn; Sam is Ring-bound.
		assertTrue(scn.IsCharAssignedAgainst(gimli, lieutenant));
		assertTrue(scn.IsCharAssignedAgainst(sam, lieutenant2));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lieutenant));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(lieutenant2));
		assertEquals(0, scn.GetWoundsOn(lieutenant));
		assertEquals(0, scn.GetBurdens());

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(lieutenant));
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(lieutenant2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, lieutenant));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AssignedToAnAllyItMakesNoOfferWhileACopyAssignedToACompanionDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lieutenant = scn.GetShadowCard("lieutenant");
		var lieutenant2 = scn.GetShadowCard("lieutenant2");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();

		scn.MoveMinionsToTable(lieutenant, lieutenant2, runner);
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{bounder, lieutenant},
				new PhysicalCardImpl[]{aragorn, lieutenant2},
				new PhysicalCardImpl[]{gimli, runner});

		// The copy on the ally offers nothing, and neither copy answers the Runner's assignment to Gimli.
		assertTrue(scn.IsCharAssignedAgainst(bounder, lieutenant));
		assertTrue(scn.IsCharAssignedAgainst(gimli, runner));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lieutenant2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(lieutenant));
		assertEquals(0, scn.GetBurdens());

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(lieutenant2));
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(lieutenant));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.FreepsResolveSkirmish(bounder);
		assertTrue(scn.IsCharSkirmishingAgainst(bounder, lieutenant));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DeclinedNothingIsPaidAndItOffersAgainWhenAssignedAfterTheNextMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lieutenant = scn.GetShadowCard("lieutenant");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.MoveMinionsToTable(lieutenant);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lieutenant);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lieutenant));

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(lieutenant));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.IsCharAssignedAgainst(aragorn, lieutenant));

		// 8 against 8: the Lieutenant wins and Aragorn takes a wound.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, lieutenant);

		// The regroup move, and the assignment phase at site 3.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(aragorn, lieutenant);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lieutenant));

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(lieutenant));
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnExhaustedLieutenantMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lieutenant = scn.GetShadowCard("lieutenant");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.MoveMinionsToTable(lieutenant);
		// Vitality 2 with 1 wound: he cannot exert.
		scn.AddWoundsToChar(lieutenant, 1);
		scn.SkipToAssignments();
		assertEquals(1, scn.GetWoundsOn(lieutenant));
		scn.FreepsAssignToMinions(aragorn, lieutenant);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, lieutenant));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(lieutenant));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
