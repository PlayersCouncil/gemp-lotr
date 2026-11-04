package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_13_174_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rogue", "13_174");
					put("rogue2", "13_174");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("commander", "13_158"); // Assault Commander: [uruk-hai] Uruk-hai, strength 9; a Shadow ability used only on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukRogueStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Uruk Rogue
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Skirmish:</b> If no other minions are assigned to a skirmish, exert this minion twice to make it <b>fierce</b> and strength +4 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rogue");

		assertEquals("Uruk Rogue", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AloneInItsSkirmishItExertsTwiceToBeFierceAndStrengthPlus4UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var rogue = scn.GetShadowCard("rogue");
		var rogue2 = scn.GetShadowCard("rogue2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(rogue, rogue2, troop);
		scn.SkipToAssignments();
		// The Troop fights Boromir first; the Rogue fights Aragorn after; the other Rogue is assigned to nobody.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, rogue}, new PhysicalCardImpl[]{boromir, troop});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		// While the Troop is in its skirmish it is a minion assigned to a skirmish.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(rogue));
		scn.ShadowPass();

		// The Troop's skirmish is over, so it is no longer assigned (CR 4.2 "skirmish phase(s)"), and the other
		// Rogue was never assigned: neither stops it.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(rogue));
		assertEquals(12, scn.GetStrength(rogue));
		assertFalse(scn.HasKeyword(rogue, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(rogue));
		scn.ShadowUseCardAction(rogue);

		assertEquals(2, scn.GetWoundsOn(rogue));
		assertEquals(16, scn.GetStrength(rogue));
		assertTrue(scn.HasKeyword(rogue, Keyword.FIERCE));
		// The other copy pays nothing and gains nothing.
		assertEquals(0, scn.GetWoundsOn(rogue2));
		assertEquals(12, scn.GetStrength(rogue2));
		assertFalse(scn.HasKeyword(rogue2, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Still fierce and +4 in the fierce round, which comes only because it is fierce.
		scn.PassSkirmishActions();
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(16, scn.GetStrength(rogue));
		assertTrue(scn.HasKeyword(rogue, Keyword.FIERCE));

		// Nobody assigned to it there; at the regroup phase both are gone.
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(12, scn.GetStrength(rogue));
		assertFalse(scn.HasKeyword(rogue, Keyword.FIERCE));
	}

	@Test
	public void IsNotOfferedWhileAnotherMinionWaitsForItsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var rogue = scn.GetShadowCard("rogue");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(rogue, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, rogue}, new PhysicalCardImpl[]{boromir, troop});
		// The Rogue's skirmish first; the Troop waits for Boromir's.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(rogue));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(rogue));
	}

	@Test
	public void IsNotOfferedWithAnotherMinionInItsOwnSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var rogue = scn.GetShadowCard("rogue");
		var commander = scn.GetShadowCard("commander");

		scn.StartGame();
		scn.MoveMinionsToTable(rogue, commander);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, rogue);
		// The Shadow player puts the [uruk-hai] Uruk-hai left over on Aragorn beside the Rogue.
		scn.ShadowAssignToMinions(aragorn, commander);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(rogue));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(rogue));
	}

	@Test
	public void IsNotOfferedWithTwoVitalityLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var rogue = scn.GetShadowCard("rogue");

		scn.StartGame();
		scn.MoveMinionsToTable(rogue);
		// Vitality 3 with 1 wound: it cannot exert twice (CR 4.2 "exert": X times wants X+1 vitality).
		scn.AddWoundsToChar(rogue, 1);
		assertEquals(1, scn.GetWoundsOn(rogue));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, rogue);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(1, scn.GetWoundsOn(rogue));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(rogue));
	}
}
