package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_19_010_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gollum", "19_10");
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc minion, strength 5; its play trigger is not reached by a cheat
					put("farin", "1_11");       // Farin, Dwarven Emissary: strength 5, vitality 3; his +2 is only against an Orc
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GollumStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Gollum, Threatening Guide
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype:
		 * Strength: 5
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: Gollum is strength +1 for each threat you can spot.<br>Each time Gollum wins a skirmish, you may add a threat.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gollum");

		assertEquals("Gollum", card.getBlueprint().getTitle());
		assertEquals("Threatening Guide", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void GollumIsStrengthPlusOneForEachThreatAndTheMinionBesideHimIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var farin = scn.GetFreepsCard("farin");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Three companions, so that up to three threats can be laid.
		scn.MoveCompanionsToTable(farin, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, runner);

		assertEquals(0, scn.GetThreats());
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(5, scn.GetStrength(runner));

		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertEquals(6, scn.GetStrength(gollum));
		assertEquals(5, scn.GetStrength(runner));

		scn.AddThreats(1);
		assertEquals(2, scn.GetThreats());
		assertEquals(7, scn.GetStrength(gollum));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeGollumWinsASkirmishHeMayAddAThreatAndGrowsWithIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var farin = scn.GetFreepsCard("farin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(farin, aragorn);

		scn.StartGame();
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.MoveMinionsToTable(gollum);
		// Fierce, so that he skirmishes, and wins, a second time in the turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, gollum, null, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(farin, gollum);
		scn.FreepsResolveSkirmish(farin);
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(farin));
		// 5 + 1 threat against Farin's 5.
		assertEquals(6, scn.GetStrength(gollum));
		assertEquals(5, scn.GetStrength(farin));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(farin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gollum));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetThreats());
		assertEquals(7, scn.GetStrength(gollum));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(farin, gollum);
		scn.FreepsResolveSkirmish(farin);
		scn.PassFierceSkirmishActions();

		// 7 against 5 again: the second win is offered the same way.
		assertEquals(2, scn.GetWoundsOn(farin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gollum));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(3, scn.GetThreats());
		assertEquals(8, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DeclinedTheWinAddsNoThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var farin = scn.GetFreepsCard("farin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(farin, aragorn);

		scn.StartGame();
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.MoveMinionsToTable(gollum);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(farin, gollum);
		scn.FreepsResolveSkirmish(farin);
		assertEquals(1, scn.GetThreats());
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(farin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gollum));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetThreats());
		assertEquals(6, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NoThreatIsOfferedWhenGollumLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var farin = scn.GetFreepsCard("farin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(farin, aragorn);

		scn.StartGame();
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.MoveMinionsToTable(gollum);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(gollum));
		// 6 against Aragorn's 8: Gollum loses, and with vitality 4 lives through the wound.
		assertEquals(6, scn.GetStrength(gollum));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(gollum));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(gollum));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NoThreatIsOfferedForAWinWhenNoThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var farin = scn.GetFreepsCard("farin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(farin, aragorn);

		scn.StartGame();
		// As many threats as companions: no more can be added.
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		scn.MoveMinionsToTable(gollum);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(farin, gollum);
		scn.FreepsResolveSkirmish(farin);
		assertEquals(3, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(farin));
		// 5 + 3 against 5: a win, short of double Farin's strength.
		assertEquals(8, scn.GetStrength(gollum));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(farin));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(gollum));
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
