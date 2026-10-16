package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_147_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "1_147");
					put("guard2", "1_147");     // a second copy: not unique, and "this minion" is the one used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf; a skirmish ability used only on purpose
					// Bounder: [shire] Hobbit ally whose home is site 2 of this path, where he may be assigned to a
					// skirmish; a skirmish ability used only on purpose
					put("bounder", "1_286");
					put("hunt", "1_126");       // Hunt Them Down!: Maneuver event, "Make an Uruk-hai fierce until the regroup phase"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetAssignmentScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "1_147");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukGuardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk Guard
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Assignment:</b> Exert this minion and spot a companion to prevent the opponent from assigning that companion to this minion.
		*/

		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");

		assertEquals("Uruk Guard", guard.getBlueprint().getTitle());
		assertNull(guard.getBlueprint().getSubtitle());
		assertFalse(guard.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, guard.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, guard.getBlueprint().getCulture());
		assertEquals(CardType.MINION, guard.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, guard.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(guard, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(4, guard.getBlueprint().getTwilightCost());
		assertEquals(9, guard.getBlueprint().getStrength());
		assertEquals(2, guard.getBlueprint().getVitality());
		assertEquals(5, guard.getBlueprint().getSiteNumber());
	}

	// "Prevent the opponent from assigning that companion to this minion": the opponent of the Guard's owner is
	// the Free Peoples player alone.  The Shadow player may still assign his own Guard to the companion spotted, among
	// the minions left over once the Free Peoples player has assigned (F-0490; Ketura, 2026-10-12).
	@Test
	public void TheShadowPlayerMayAssignTheGuardToTheCompanionSpottedThoughTheFreePeoplesPlayerMayNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAssignmentScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.MoveMinionsToTable(guard);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Aragorn is spotted.
		scn.ShadowUseCardAction(guard);
		scn.ShadowChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(guard));
		scn.FreepsPass();
		scn.ShadowPass();

		// The Free Peoples player may not assign Aragorn to the Guard; Frodo he may.
		assertFalse(scn.FreepsCanAssignAgainst(aragorn, guard));
		assertTrue(scn.FreepsCanAssignAgainst(frodo, guard));
		scn.FreepsDeclineAssignments();

		// The Guard is left over, and the Shadow player assigns it to Aragorn.
		assertTrue(scn.ShadowCanAssignAgainst(aragorn, guard));
		scn.ShadowAssignToMinions(aragorn, guard);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, guard));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignmentAbilityExertsThisMinionToBarTheFreePeoplesPlayerFromAssigningTheCompanionSpottedToIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bounder);

		var guard = scn.GetShadowCard("guard");
		var guard2 = scn.GetShadowCard("guard2");

		scn.StartGame();
		scn.MoveMinionsToTable(guard, guard2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(guard2));
		assertTrue(scn.ShadowActionAvailable(guard));

		scn.ShadowUseCardAction(guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(guard2));

		// A companion of each kind on the table, the Ring-bearer among them; not the ally, who may be assigned to a
		// skirmish at this site, and no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bounder, guard, guard2));
		assertTrue(scn.FreepsCanAssign(bounder));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		scn.FreepsPass();
		scn.ShadowPass();

		// Aragorn may not be assigned to this Guard; he may to the other copy, and Gimli and Frodo may to this one.
		assertFalse(scn.FreepsCanAssignAgainst(aragorn, guard));
		assertTrue(scn.FreepsCanAssignAgainst(aragorn, guard2));
		assertTrue(scn.FreepsCanAssignAgainst(gimli, guard));
		assertTrue(scn.FreepsCanAssignAgainst(frodo, guard));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, guard2}, new PhysicalCardImpl[]{gimli, guard});
		assertTrue(scn.IsCharAssignedAgainst(aragorn, guard2));
		assertTrue(scn.IsCharAssignedAgainst(gimli, guard));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignmentAbilityIsNotOfferedWhileThisMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		var guard = scn.GetShadowCard("guard");
		var guard2 = scn.GetShadowCard("guard2");

		scn.StartGame();
		scn.MoveMinionsToTable(guard, guard2);
		// Vitality 2: with one wound the Guard cannot exert.  The other copy can.
		scn.AddWoundsToChar(guard, 1);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(guard2));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(guard));
		assertTrue(scn.ShadowActionAvailable(guard2));
	}

	@Test
	public void TheBarLastsTheAssignmentPhaseAndIsGoneInTheFierceAssignments() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var guard = scn.GetShadowCard("guard");
		var hunt = scn.GetShadowCard("hunt");
		scn.MoveCardsToHand(hunt);

		scn.StartGame();
		scn.MoveMinionsToTable(guard);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Made fierce by Hunt Them Down!, so that there is an assignment phase after the one the bar is laid in.
		scn.ShadowPlayCard(hunt);
		assertTrue(scn.HasKeyword(guard, Keyword.FIERCE));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(guard));
		scn.ShadowUseCardAction(guard);
		scn.ShadowChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(guard));
		scn.FreepsPass();
		scn.ShadowPass();

		assertFalse(scn.FreepsCanAssignAgainst(aragorn, guard));
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// The fierce assignment phase is another phase: the bar is gone.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsCanAssignAgainst(aragorn, guard));
		scn.FreepsAssignToMinions(aragorn, guard);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, guard));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
