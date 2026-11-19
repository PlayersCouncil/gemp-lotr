package com.gempukku.lotro.cards.unofficial.rtmd.set_93_lotro;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_93_008_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn: Gondor companion, strength 8, vitality 4 — not a Wraith
					put("oathbreaker", "8_41"); // Oathbreaker: FP Gondor Wraith companion, strength 6, vitality 3, Enduring
					put("stalker", "16_1"); // Barrow-wight Stalker: Shadow Wraith minion, strength 11, vitality 4, Enduring
					put("boromir", "1_96"); // Boromir, Lord of Gondor: [gondor] Man, strength 7, vitality 3
					put("bounder", "1_286"); // Bounder: [shire] Hobbit ally, strength 2, vitality 2, home site 2
					put("corpse", "6_99"); // Corpse Lights: [sauron] Wraith minion, strength 7, vitality 2, not enduring
					put("wisp", "6_108"); // Wisp of Pale Sheen: [sauron] Wraith minion, strength 8, vitality 2, not enduring
					put("lemenya", "1_232"); // Ulaire Lemenya: [wraith] Nazgul, strength 9, vitality 2: the culture and not the race
					put("troop", "1_177"); // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"93_8", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("oathbreaker", "8_41"); // Oathbreaker: FP Gondor Wraith companion, strength 6, vitality 3, Enduring
					put("wisp", "6_108"); // Wisp of Pale Sheen: [sauron] Wraith minion, strength 8, vitality 2, not enduring
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "93_8"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 93
		 * Name: Race Text 93_8
		 * Type: MetaSite
		 * Intensity: -1
		 * Game Text: Each Wraith is strength +1 for each wound on a character in its skirmish.
		 */
		var scn = GetFreepsScenario();
		var card = scn.GetFreepsCard("mod");
		assertEquals("Race Text 93_8", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-1, card.getBlueprint().getIntensity());
	}

	@Test
	public void ShadowWraithGainsBonusFromWoundsInSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var stalker = scn.GetShadowCard("stalker");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(stalker);
		// 2 wounds on Aragorn; stalker is unwounded
		scn.AddWoundsToChar(aragorn, 2);

		scn.StartGame();
		scn.SkipToAssignments();
		assertEquals(11, scn.GetStrength(stalker));
		scn.FreepsAssignAndResolve(aragorn, stalker);

		// In skirmish: 2 wounds on Aragorn, 0 on Stalker = 2 wounds total in skirmish
		// Stalker: base 11 + 2 (93_8 bonus) = 13
		// No Enduring bonus since Stalker has no wounds
		assertEquals(13, scn.GetStrength(stalker));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void FPWraithAlsoGetsBonusInSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var stalker = scn.GetShadowCard("stalker");

		scn.MoveCompanionsToTable(oathbreaker);
		scn.MoveMinionsToTable(stalker);
		// 1 wound on Stalker; Oathbreaker is unwounded
		scn.AddWoundsToChar(stalker, 1);

		scn.StartGame();
		scn.SkipToAssignments();
		assertEquals(6, scn.GetStrength(oathbreaker));
		// 11 + 2 (Enduring, for its own wound)
		assertEquals(13, scn.GetStrength(stalker));
		scn.FreepsAssignAndResolve(oathbreaker, stalker);

		// In skirmish: 1 wound on Stalker, 0 on Oathbreaker = 1 wound total
		// Oathbreaker: base 6 + 1 (93_8 bonus) = 7
		// No Enduring bonus since Oathbreaker has no wounds
		assertEquals(7, scn.GetStrength(oathbreaker));

		// Stalker also gets bonus: base 11 + 1 (93_8 for wound in skirmish)
		// Plus Enduring: +2 for own 1 wound = +2
		// Total: 11 + 1 + 2 = 14
		assertEquals(14, scn.GetStrength(stalker));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void EnduringSelfWoundsStackWithSkirmishBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var stalker = scn.GetShadowCard("stalker");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(stalker);
		// 2 wounds on Stalker, 1 wound on Aragorn
		scn.AddWoundsToChar(stalker, 2);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		scn.SkipToAssignments();
		// 11 + 4 (Enduring: 2 per own wound x 2)
		assertEquals(15, scn.GetStrength(stalker));
		scn.FreepsAssignAndResolve(aragorn, stalker);

		// In skirmish: 2 wounds on Stalker + 1 wound on Aragorn = 3 wounds total
		// Stalker: base 11 + 3 (93_8 bonus) + 4 (Enduring: 2 per own wound x 2) = 18
		assertEquals(18, scn.GetStrength(stalker));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NoBonusWithNoWoundsInSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var stalker = scn.GetShadowCard("stalker");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(stalker);

		scn.StartGame();
		scn.SkipToAssignments();
		assertEquals(11, scn.GetStrength(stalker));
		scn.FreepsAssignAndResolve(aragorn, stalker);

		// No wounds on anyone = no bonus
		assertEquals(11, scn.GetStrength(stalker));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NonWraithDoesNotGetBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var stalker = scn.GetShadowCard("stalker");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(stalker);
		scn.AddWoundsToChar(stalker, 1);

		scn.StartGame();
		scn.SkipToAssignments();
		assertEquals(8, scn.GetStrength(aragorn));
		scn.FreepsAssignAndResolve(aragorn, stalker);

		// Aragorn is a Man, not a Wraith — should not get the bonus
		// Aragorn: base 8, no bonus from 93_8
		assertEquals(8, scn.GetStrength(aragorn));
		// The Wraith he is fighting has it: 11 + 2 (Enduring) + 1
		assertEquals(14, scn.GetStrength(stalker));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ANazgulIsNoWraithAndAWoundOnAnAllyIsCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var lemenya = scn.GetShadowCard("lemenya");
		var wisp = scn.GetShadowCard("wisp");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(bounder, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, wisp);

		// The fellowship moves to site 2, Bounder's home, where he may be assigned
		scn.SkipToAssignments();
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(8, scn.GetStrength(wisp));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{bounder, wisp});

		// A [wraith] Nazgul fighting a wounded companion: of the culture, and not a Wraith
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(9, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn 8 loses to Lemenya 9 and takes a wound
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// A Wraith fighting an ally with one wound: 8 + 1. Aragorn's two wounds are not of this skirmish.
		scn.FreepsResolveSkirmish(bounder);
		assertEquals(9, scn.GetStrength(wisp));
		assertEquals(9, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WoundsOnCharactersOutsideTheSkirmishAreNotCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var corpse = scn.GetShadowCard("corpse");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(boromir, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(corpse, troop);
		scn.AddWoundsToChar(troop, 1);

		scn.SkipToAssignments();
		assertEquals(7, scn.GetStrength(corpse));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, corpse}, new PhysicalCardImpl[]{boromir, troop});
		scn.FreepsResolveSkirmish(aragorn);

		// 7 + 2 for Aragorn's two wounds. Boromir and the Troop, a wound each, are waiting for a
		// skirmish of their own: with theirs counted it would be 11.
		assertEquals(9, scn.GetStrength(corpse));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AWraithGetsNothingFromASkirmishItIsNotIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var corpse = scn.GetShadowCard("corpse");
		var wisp = scn.GetShadowCard("wisp");

		scn.MoveCompanionsToTable(aragorn, oathbreaker);
		scn.AddWoundsToChar(aragorn, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(corpse, wisp);
		scn.AddWoundsToChar(wisp, 1);

		scn.SkipToAssignments();
		assertEquals(6, scn.GetStrength(oathbreaker));
		assertEquals(7, scn.GetStrength(corpse));
		assertEquals(8, scn.GetStrength(wisp));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, corpse}, new PhysicalCardImpl[]{oathbreaker, wisp});

		// Aragorn's skirmish, with his two wounds in it: Corpse Lights is in it, 7 + 2. Oathbreaker and
		// the Wisp are assigned to each other and their skirmish has not begun: nothing for them.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(9, scn.GetStrength(corpse));
		assertEquals(6, scn.GetStrength(oathbreaker));
		assertEquals(8, scn.GetStrength(wisp));

		// Aragorn 8 loses to Corpse Lights 9 and takes a third wound
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(aragorn));

		// Their own skirmish, with the Wisp's one wound in it: 6 + 1 and 8 + 1. Corpse Lights has
		// fought and is in no skirmish now.
		scn.FreepsResolveSkirmish(oathbreaker);
		assertEquals(7, scn.GetStrength(oathbreaker));
		assertEquals(9, scn.GetStrength(wisp));
		assertEquals(7, scn.GetStrength(corpse));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheModifierAcrossTheTableStrengthensBothPlayersWraithsToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		// Player 2 holds the modifier; the text names no player
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var wisp = scn.GetShadowCard("wisp");

		scn.MoveCompanionsToTable(oathbreaker);
		scn.AddWoundsToChar(oathbreaker, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(wisp);

		scn.SkipToAssignments();
		// 6 + 2 (Enduring, for his own wound)
		assertEquals(8, scn.GetStrength(oathbreaker));
		assertEquals(8, scn.GetStrength(wisp));
		scn.FreepsAssignAndResolve(oathbreaker, wisp);

		// One wound in the skirmish: 6 + 2 (Enduring) + 1, and 8 + 1
		assertEquals(9, scn.GetStrength(oathbreaker));
		assertEquals(9, scn.GetStrength(wisp));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
