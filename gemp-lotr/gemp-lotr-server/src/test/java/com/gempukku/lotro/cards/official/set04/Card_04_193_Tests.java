package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_193_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("runner", "4_193");
					put("runner2", "4_193");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("pursuer", "4_190");    // Uruk Pursuer: printed fierce, strength 5; its texts read only an unbound Hobbit discarded or killed
					put("seeker", "4_195");     // Uruk Seeker: printed fierce, strength 6; +2 only against a bearer of a search card
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukRunnerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk Runner
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Tracker</b>. <b>Fierce</b>.<br>During a fierce skirmish involving this minion, it is strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("runner");

		assertEquals("Uruk Runner", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlus2InHisFierceSkirmishAndNotInHisRegularSkirmishOrAfterIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		assertEquals(8, scn.GetStrength(runner));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		// His regular skirmish: no bonus.
		assertFalse(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertEquals(8, scn.GetStrength(runner));
		scn.PassSkirmishActions();
		// 8 against 8: a tie goes to the minion, and Aragorn takes a wound.
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		// His fierce skirmish: 8 + 2.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertEquals(10, scn.GetStrength(runner));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The fierce skirmish over, he is back to 8.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(8, scn.GetStrength(runner));
	}

	@Test
	public void OnlyTheMinionsOwnFierceSkirmishCountsAndTheBonusIsHisAloneAndDoesNotStackWithASecondCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var pursuer = scn.GetShadowCard("pursuer");
		var seeker = scn.GetShadowCard("seeker");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2, pursuer, seeker);

		// No regular skirmishes.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner},
				new PhysicalCardImpl[]{gimli, seeker});
		scn.ShadowAssignToMinions(aragorn, runner2, pursuer);

		// Another minion's fierce skirmish: neither copy is in it, and neither has the bonus.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, seeker));
		assertEquals(8, scn.GetStrength(runner));
		assertEquals(8, scn.GetStrength(runner2));
		assertEquals(6, scn.GetStrength(seeker));
		scn.PassSkirmishActions();

		// Their own fierce skirmish: each copy is 8 + 2 (not + 4), and the other fierce minion in it keeps its 5.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner2));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, pursuer));
		assertEquals(10, scn.GetStrength(runner));
		assertEquals(10, scn.GetStrength(runner2));
		assertEquals(5, scn.GetStrength(pursuer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
