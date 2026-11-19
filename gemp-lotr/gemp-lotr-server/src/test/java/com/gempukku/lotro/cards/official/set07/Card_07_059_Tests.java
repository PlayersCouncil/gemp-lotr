package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class Card_07_059_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gollum", "7_59");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					// Goblin Runner: a [moria] Orc, strength 5, with a play trigger only; it stands in Gollum's skirmish
					put("runner", "1_178");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GollumStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gollum, Vile Creature
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: 
		 * Strength: 5
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: <b>Skirmish:</b> Exert Gollum twice or remove a threat to make him strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gollum");

		assertEquals("Gollum", card.getBlueprint().getTitle());
		assertEquals("Vile Creature", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ExertingGollumTwiceMakesHimStrengthPlusTwoForHisSkirmishAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, runner, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, gollum}, new PhysicalCardImpl[]{frodo, troop});
		// The Runner is left over: the Shadow player puts it in Gollum's skirmish too.
		scn.ShadowAssignToMinions(aragorn, runner);

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// No threat, so exerting twice is the only price he can pay, and it is paid without a question.
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.ShadowActionAvailable(gollum));
		scn.ShadowUseCardAction(gollum);

		assertEquals(2, scn.GetWoundsOn(gollum));
		assertEquals(7, scn.GetStrength(gollum));
		// The minion beside him in his skirmish is not made stronger.
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn, 8, loses to 7 + 5 and lives.  In the next skirmish Gollum is 5 again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AbilityIsOfferedToAGollumWhoIsNotInTheSkirmishBeingFought() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, runner);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		// The Shadow player leaves Gollum unassigned: Aragorn and the Runner are the only skirmish.
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The text says nothing of his skirmish, so he may strengthen himself from outside it.
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(gollum));
		scn.ShadowUseCardAction(gollum);
		scn.ShadowChoose("Remove a threat");

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(7, scn.GetStrength(gollum));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTooFewVitalityToExertTwiceTheThreatIsRemovedInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		// Vitality 4 with 2 wounds: 2 left, and exerting twice wants 3.
		scn.AddWoundsToChar(gollum, 2);
		scn.AddThreats(1);
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertEquals(1, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.ShadowActionAvailable(gollum));
		scn.ShadowUseCardAction(gollum);

		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertEquals(7, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithBothPricesPayableTheShadowPlayerChoosesWhichAndTheAbilityIsNotOfferedOutsideASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());

		// "Skirmish:" is not the assignment phase, though both prices can be paid there.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gollum));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.ShadowActionAvailable(gollum));
		scn.ShadowUseCardAction(gollum);

		assertEquals(List.of("Exert Gollum twice", "Remove a threat"), scn.ShadowGetMultipleChoices());
		scn.ShadowChoose("Remove a threat");

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(7, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNeitherPricePayableTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.AddWoundsToChar(gollum, 2);
		assertEquals(2, scn.GetWoundsOn(gollum));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// 2 vitality left and no threat.
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gollum));
	}
}
