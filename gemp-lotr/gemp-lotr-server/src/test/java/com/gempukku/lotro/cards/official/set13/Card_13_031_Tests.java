package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_031_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("flame", "13_31");

					// Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion, strength 6, +1 for each of
					// Hobbit, Dwarf, Elf and Man in the fellowship
					put("gandalf", "1_72");
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] Ent companion; +2 only with 3 Ents
					// Dasron, Merchant From Dorwinion: a [gandalf] Man companion, strength 6; his start-of-fellowship
					// trigger needs culture tokens
					put("dasron", "13_29");
					// Barliman Butterbur, Prancing Pony Proprietor: a [gandalf] ally; a fellowship ability
					put("barliman", "1_70");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability used only on purpose
					// Bounder: a Hobbit ally whose home is site 2; a skirmish ability used only on purpose
					put("bounder", "1_286");

					put("savage1", "1_151");    // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("savage2", "1_151");
					// Mordor Soldier: [sauron] Orc, strength 3, vitality 2; a skirmish ability that removes a threat
					put("soldier", "7_290");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheFlameofAnorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: The Flame of Anor
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Spell</b>.<br>Spot 3 [gandalf] companions to make a minion skirmishing a companion strength -3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("flame");

		assertEquals("The Flame of Anor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingThreeGandalfCompanionsMakesAMinionSkirmishingACompanionStrengthMinus3UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flame = scn.GetFreepsCard("flame");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var dasron = scn.GetFreepsCard("dasron");
		scn.MoveCompanionsToTable(gandalf, avenger, dasron);
		scn.MoveCardsToHand(flame);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2, soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, savage1}, new PhysicalCardImpl[]{dasron, savage2});
		// A second minion in Gandalf's skirmish, of another culture and race.
		scn.ShadowAssignToMinions(gandalf, soldier);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(savage1));
		assertEquals(3, scn.GetStrength(soldier));
		assertEquals(5, scn.GetStrength(savage2));
		assertTrue(scn.FreepsPlayAvailable(flame));

		scn.FreepsPlayCard(flame);
		// Both minions skirmishing Gandalf; not the one assigned to Dasron, whose skirmish has not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage1, soldier));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage1);

		assertInZone(Zone.DISCARD, flame);
		assertEquals(2, scn.GetStrength(savage1));
		assertEquals(3, scn.GetStrength(soldier));
		assertEquals(5, scn.GetStrength(savage2));

		// Still in force at the next decision of the same skirmish.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(2, scn.GetStrength(savage1));

		// Gandalf (6, +1 for Frodo's race and +1 for Dasron's) beats 2 + 3 without overwhelming either; each takes
		// a wound and lives.
		scn.PassSkirmishActions();
		assertInZone(Zone.SHADOW_CHARACTERS, savage1, soldier);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The skirmish is over and the penalty with it.
		scn.FreepsResolveSkirmish(dasron);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(savage1));
	}

	@Test
	public void AMinionSkirmishingACompanionOfAnotherCultureIsMadeWeakerToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flame = scn.GetFreepsCard("flame");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var dasron = scn.GetFreepsCard("dasron");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, avenger, dasron, aragorn);
		scn.MoveCardsToHand(flame);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2, soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage1}, new PhysicalCardImpl[]{dasron, savage2});
		scn.ShadowAssignToMinions(aragorn, soldier);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(savage1));
		assertEquals(3, scn.GetStrength(soldier));
		assertEquals(5, scn.GetStrength(savage2));

		// Aragorn is a [gondor] companion, not a [gandalf] one: the minions skirmishing him are still offered, and
		// the one assigned to Dasron, whose skirmish has not begun, is not.
		scn.FreepsPlayCard(flame);
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage1, soldier));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage2));
		scn.FreepsChooseCard(soldier);

		assertEquals(0, scn.GetStrength(soldier));
		assertEquals(5, scn.GetStrength(savage1));
		assertEquals(5, scn.GetStrength(savage2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOnlyTwoGandalfCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flame = scn.GetFreepsCard("flame");
		var gandalf = scn.GetFreepsCard("gandalf");
		var dasron = scn.GetFreepsCard("dasron");
		var barliman = scn.GetFreepsCard("barliman");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Two [gandalf] companions, beside a [gandalf] ally and a companion of another culture.
		scn.MoveCompanionsToTable(gandalf, dasron, aragorn);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(flame);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, savage1);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.HAND, flame);
		assertFalse(scn.FreepsPlayAvailable(flame));
		assertEquals(5, scn.GetStrength(savage1));
	}

	@Test
	public void InTheSkirmishOfAnAllyNoMinionIsMadeWeaker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flame = scn.GetFreepsCard("flame");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var dasron = scn.GetFreepsCard("dasron");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(gandalf, avenger, dasron);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(flame);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		// The fellowship moves to site 2, the Bounder's home, where he may be assigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bounder, savage1);
		scn.FreepsResolveSkirmish(bounder);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(savage1));
		// Three [gandalf] companions are spotted, so it may be played; the only minion skirmishes an ally, so it
		// does nothing.
		assertTrue(scn.FreepsPlayAvailable(flame));

		scn.FreepsPlayCard(flame);
		assertInZone(Zone.DISCARD, flame);
		assertEquals(5, scn.GetStrength(savage1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
