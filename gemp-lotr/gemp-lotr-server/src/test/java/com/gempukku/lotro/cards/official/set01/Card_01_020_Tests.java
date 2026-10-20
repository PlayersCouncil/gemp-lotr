package com.gempukku.lotro.cards.official.set01;

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

public class Card_01_020_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("letthem", "1_20");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3, damage +1; a skirmish ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, home site 3, strength 4, vitality 2; a maneuver ability used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, home site 3, strength 3, vitality 3; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Man companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("mallorn", "1_54");     // Mallorn-trees: an [elven] condition in the support area; a fellowship ability used only on purpose

					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, strength 5, vitality 3; a regroup ability used only on purpose
					put("soldier", "7_290");    // Mordor Soldier: [sauron] Orc, strength 3, vitality 2; a skirmish ability used only on purpose
					put("slayer", "3_97");      // Orc Slayer: [sauron] Orc, strength 5, vitality 2; a regroup ability used only on purpose
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; his text answers only a weapon played on him
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, no Orc, strength 5, vitality 3, damage +1 only
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LetThemComeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Let Them Come!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>Each time a Dwarf wins a skirmish against an Orc, discard that Orc. Discard this condition if a Dwarf loses a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("letthem");

		assertEquals("Let Them Come!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachOrcADwarfWinsAgainstIsDiscardedAndOneAManWinsAgainstIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var letthem = scn.GetFreepsCard("letthem");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var worker = scn.GetShadowCard("worker");
		var soldier = scn.GetShadowCard("soldier");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCardsToSupportArea(letthem);
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, letthem);
		scn.SkipToSite(2);
		scn.FreepsPass();
		// Site 3: Thrarin's home, where he may be assigned.
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(worker, soldier, slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, worker}, new PhysicalCardImpl[]{thrarin, soldier},
				new PhysicalCardImpl[]{aragorn, slayer});

		// A Dwarf companion beats an [isengard] Orc, 6 to 5: two wounds (damage +1) leave it alive, and it is discarded.
		scn.FreepsResolveSkirmish(gimli);
		assertInPlay(worker);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertInDiscard(worker);
		assertInPlay(letthem);

		// A Dwarf ally beats a [sauron] Orc, 4 to 3: one wound leaves it alive, and it is discarded too.
		scn.FreepsResolveSkirmish(thrarin);
		assertInPlay(soldier);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertInDiscard(soldier);
		assertInPlay(letthem);

		// A Man beats another [sauron] Orc, 8 to 5: it takes its wound and stays.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(slayer));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(slayer));
		assertInPlay(slayer);
		assertInPlay(letthem);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OfAnOrcAndAnUrukHaiADwarfBeatsTogetherOnlyTheOrcIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var letthem = scn.GetFreepsCard("letthem");
		var gimli = scn.GetFreepsCard("gimli");
		var worker = scn.GetShadowCard("worker");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(letthem);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, letthem);
		scn.MoveMinionsToTable(worker, savage);
		// A strength that only sets the board: 6 + 6 = 12 beats the two minions' 10, and is short of overwhelming them.
		scn.ApplyAdHocModifier(new StrengthModifier(null, gimli, null, 6));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, worker);
		scn.ShadowAssignToMinions(gimli, savage);

		scn.FreepsResolveSkirmish(gimli);
		assertEquals(12, scn.GetStrength(gimli));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		// Each loser takes two wounds (damage +1) and lives; only the Orc is discarded.
		assertInDiscard(worker);
		assertInPlay(savage);
		assertEquals(2, scn.GetWoundsOn(savage));
		assertInPlay(letthem);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADwarfWhoLosesToAnOrcLeavesTheOrcInPlayAndThisConditionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var letthem = scn.GetFreepsCard("letthem");
		var gimli = scn.GetFreepsCard("gimli");
		var chieftain = scn.GetShadowCard("chieftain");
		var mallorn = scn.GetFreepsCard("mallorn");
		scn.MoveCardsToSupportArea(letthem, mallorn);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, letthem);
		assertInZone(Zone.SUPPORT, mallorn);
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, chieftain);

		// 7 against 6: the Orc wins, short of overwhelming him.
		scn.FreepsResolveSkirmish(gimli);
		assertInPlay(letthem);
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInPlay(chieftain);
		assertInDiscard(letthem);
		// Another condition of the same player beside it is not discarded with it.
		assertInZone(Zone.SUPPORT, mallorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ThisConditionIsDiscardedWhenADwarfAllyLosesToAnUrukHaiAndNotWhenAManLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var letthem = scn.GetFreepsCard("letthem");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(letthem);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, letthem);
		scn.SkipToSite(2);
		scn.FreepsPass();
		// Site 3: Grimir's home, where he may be assigned.
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{grimir, savage}, new PhysicalCardImpl[]{aragorn, troop});

		// A Man loses, 13 to 8, short of being overwhelmed: the condition stays.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInPlay(letthem);

		// A Dwarf ally loses to an Uruk-hai, 5 to 3, short of being overwhelmed: the condition goes.
		scn.FreepsResolveSkirmish(grimir);
		assertEquals(0, scn.GetWoundsOn(grimir));
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(grimir));
		assertInPlay(grimir);
		assertInDiscard(letthem);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
