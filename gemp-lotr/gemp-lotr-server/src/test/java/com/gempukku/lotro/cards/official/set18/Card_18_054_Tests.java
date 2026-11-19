package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_054_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("isildur", "18_54");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");         // Gimli, Son of Glóin: [dwarven] Dwarf, strength 6, vitality 3; a skirmish ability used only on purpose
					put("barliman", "1_70");      // Barliman Butterbur: a Man ally, vitality 2; a fellowship ability used only on purpose
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, strength 13, no text: beats 7 or 8 without doubling them
					put("troop2", "1_177");
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("savage2", "1_151");
					put("pursuer", "4_190");      // Uruk Pursuer: fierce [isengard] Uruk-hai, strength 5, vitality 3; its texts are about unbound Hobbits
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsildurStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Isildur, Sword-bearer
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 1
		 * Game Text: <b>Knight</b>.<br>Each time Isildur loses a skirmish, exert a companion.<br>Each time Isildur wins a skirmish, heal a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("isildur");

		assertEquals("Isildur", card.getBlueprint().getTitle());
		assertEquals("Sword-bearer", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.KNIGHT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(1, card.getBlueprint().getResistance());
	}

	@Test
	public void LosingASkirmishHeExertsACompanionButNotWhenAnotherCompanionLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var isildur = scn.GetFreepsCard("isildur");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(isildur, aragorn, gimli);
		scn.MoveAlliesToTable(barliman);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(gimli, 2);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.MoveMinionsToTable(troop, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{isildur, troop2});

		// Aragorn (8) loses to 13 first: his wound, and nothing more is asked.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(isildur));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish"));

		// Isildur (7) loses to 13: his wound, and then a companion of the player's choice exerts.
		scn.FreepsResolveSkirmish(isildur);
		scn.PassSkirmishActions();
		// His trigger and the loser's wound answer the same moment: the wound first.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(isildur));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, isildur));
		// Gimli is exhausted; Barliman is an ally.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, barliman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(isildur));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(barliman));
		// No heal for a loss, though Aragorn and Gimli are wounded.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachTimeHeLosesACompanionExertsInTheFierceSkirmishToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var isildur = scn.GetFreepsCard("isildur");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(isildur, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		// A strength that only sets the board: 5 + 5 = 10, above Isildur's 7 and under double it.
		scn.ApplyAdHocModifier(new StrengthModifier(null, pursuer, null, 5));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(isildur, pursuer);
		scn.FreepsResolveSkirmish(isildur);
		assertEquals(0, scn.GetWoundsOn(isildur));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		// His trigger and the loser's wound answer the same moment: the wound first.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();

		assertEquals(1, scn.GetWoundsOn(isildur));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, isildur));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The Pursuer is fierce: Isildur is fought and beaten again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(isildur, pursuer);
		scn.FreepsResolveSkirmish(isildur);
		scn.PassSkirmishActions();
		// His trigger and the loser's wound answer the same moment: the wound first.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();

		// Vitality 3 with 2 wounds, he is exhausted and no longer offered.
		assertEquals(2, scn.GetWoundsOn(isildur));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(isildur));
		scn.FreepsChooseCard(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WinningASkirmishHeHealsAWoundedCompanionButNotWhenAnotherCompanionWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var isildur = scn.GetFreepsCard("isildur");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(isildur, aragorn, gimli);
		scn.MoveAlliesToTable(barliman);
		scn.AddWoundsToChar(isildur, 1);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(barliman, 1);

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(isildur));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.MoveMinionsToTable(savage, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{isildur, savage2});

		// Aragorn (8) beats 5 first: nothing is asked, and nobody is healed.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish"));

		// Isildur (7) beats 5: a wounded companion of the player's choice is healed.
		scn.FreepsResolveSkirmish(isildur);
		scn.PassSkirmishActions();
		// His trigger and the loser's wound answer the same moment: the wound first.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertTrue(scn.FreepsHasCardChoiceAvailable(isildur, aragorn, gimli));
		// Frodo is unwounded; Barliman is an ally.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, barliman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		// One wound of his two; no exertion for a win.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(isildur));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachTimeHeWinsACompanionIsHealedInTheFierceSkirmishToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var isildur = scn.GetFreepsCard("isildur");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(isildur, aragorn, gimli);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(gimli, 1);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(isildur, pursuer);
		scn.FreepsResolveSkirmish(isildur);
		scn.PassSkirmishActions();
		// His trigger and the loser's wound answer the same moment: the wound first.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();

		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The Pursuer is fierce and lives through one wound: Isildur beats him again.
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(isildur, pursuer);
		scn.FreepsResolveSkirmish(isildur);
		scn.PassSkirmishActions();
		// His trigger and the loser's wound answer the same moment: the wound first.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();

		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		scn.FreepsChooseCard(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(isildur));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
