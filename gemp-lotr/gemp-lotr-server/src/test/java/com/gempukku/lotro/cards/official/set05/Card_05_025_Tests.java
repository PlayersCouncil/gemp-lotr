package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_025_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gollum", "5_25");
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, strength 4, vitality 2, no text but a requirement to play
					put("guard2", "1_7");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; its text is about being played, and it is cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GollumStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Gollum, Stinker
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Minion
		 * Strength: 5
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: Gollum is strength +1 for each burden.
		 * 	Each time Gollum wins a skirmish, you may add a burden.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gollum");

		assertEquals("Gollum", card.getBlueprint().getTitle());
		assertEquals("Stinker", card.getBlueprint().getSubtitle());
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
	public void GollumIsStrengthPlusOneForEachBurdenOfTheFellowshipAndNoOtherMinionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		// On his own turn player 2's Ring-bearer takes 3 burdens.
		scn.SkipToOtherPlayersTurn();
		scn.AddBurdens(3);
		assertEquals(3, scn.ShadowGetBurdens());

		// Player 1's turn again, with no burden of his own.
		scn.SkipToOtherPlayersTurn();
		scn.MoveMinionsToTable(gollum, runner);
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(3, scn.ShadowGetBurdens());
		// The 3 burdens on player 2's own Ring-bearer, inactive on this turn, are not counted.
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(5, scn.GetStrength(runner));

		scn.AddBurdens(2);
		assertEquals(2, scn.FreepsGetBurdens());
		// 5 + 2
		assertEquals(7, scn.GetStrength(gollum));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeGollumWinsASkirmishTheShadowPlayerMayAddABurdenAndGollumGrowsWithIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(guard, guard2);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, gollum);
		scn.FreepsResolveSkirmish(guard);
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(5, scn.GetStrength(gollum));

		// 5 against 4: Gollum wins.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gollum));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.FreepsGetBurdens());
		// 5 + 1
		assertEquals(6, scn.GetStrength(gollum));

		// A second move, and Gollum still in play to skirmish again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard2, gollum);
		scn.FreepsResolveSkirmish(guard2);
		assertEquals(6, scn.GetStrength(gollum));

		// 6 against 4: Gollum wins again, and not by double.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard2));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gollum));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.FreepsGetBurdens());
		// 5 + 2
		assertEquals(7, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayDeclineToAddTheBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, gollum);
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gollum));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenAnotherMinionWinsOrWhenGollumLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var troop = scn.GetShadowCard("troop");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(guard, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, troop}, new PhysicalCardImpl[]{aragorn, gollum});

		// 13 against 4: the Troop wins (by more than double: the Dwarf Guard is killed).  Nothing is offered.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(0, scn.FreepsGetBurdens());

		// 8 against 5: Gollum loses and takes the skirmish's wound.  Nothing is offered.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
