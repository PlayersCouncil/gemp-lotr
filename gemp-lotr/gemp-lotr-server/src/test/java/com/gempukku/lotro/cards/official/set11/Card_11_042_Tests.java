package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_042_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gollum", "11_42");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: damage +1; a skirmish ability used only on purpose
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9; damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GollumStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Gollum, Skulker
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: 
		 * Strength: 5
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br><b>Skirmish:</b> Exert Gollum twice to make another minion strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gollum");

		assertEquals("Gollum", card.getBlueprint().getTitle());
		assertEquals("Skulker", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityExertsGollumTwiceToMakeAnotherMinionStrengthPlus2ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var gollum = scn.GetShadowCard("gollum");
		var raiders = scn.GetShadowCard("raiders");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, raiders, troop);
		// Vitality 4 with 1 wound: 3 left, enough to exert twice.
		scn.AddWoundsToChar(gollum, 1);
		assertEquals(1, scn.GetWoundsOn(gollum));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, raiders}, new PhysicalCardImpl[]{gimli, troop});
		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(gollum));

		// Aragorn's skirmish: the Raiding Party is in it, the Goblin Patrol Troop waits for Gimli's, and Gollum is in none.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(9, scn.GetStrength(raiders));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.ShadowActionAvailable(gollum));
		scn.ShadowUseCardAction(gollum);
		assertEquals(3, scn.GetWoundsOn(gollum));
		assertTrue(scn.ShadowHasCardChoiceAvailable(raiders, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gollum, aragorn, gimli));
		scn.ShadowChooseCard(troop);
		assertEquals(15, scn.GetStrength(troop));
		assertEquals(9, scn.GetStrength(raiders));
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Each skirmish is a phase of its own: in Gimli's, the next, the Troop is 13 again.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(13, scn.GetStrength(troop));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhenGollumCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var gollum = scn.GetShadowCard("gollum");
		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, raiders);
		// Vitality 4 with 2 wounds: 2 left, enough for one exertion and not for two.
		scn.AddWoundsToChar(gollum, 2);
		assertEquals(2, scn.GetWoundsOn(gollum));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, raiders);
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gollum));
		assertEquals(2, scn.GetWoundsOn(gollum));
	}

	@Test
	public void WithNoOtherMinionTheAbilityIsOfferedAndGollumExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowActionAvailable(gollum));
		scn.ShadowUseCardAction(gollum);
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
