package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_13_072_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legacy", "13_72");
					put("elendil", "9_32");      // Elendil, The Tall: [gondor] Man, strength 8, vitality 5; his abilities are used only on purpose
					put("isildur", "9_33");      // Isildur, Bearer of Heirlooms: [gondor] Man, strength 7, vitality 3; his texts need him the Ring-bearer or bearing an artifact
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose
					put("rohanaragorn", "17_93"); // Aragorn, Defender of Rohan: the [rohan] Aragorn; hunter for [rohan] companions only while the Ring-bearer is assigned
					put("boromir", "1_97");      // Boromir, Son of Denethor: [gondor] Man who is no king, strength 7; a skirmish ability used only on purpose
					put("pippin", "103_31");     // Pippin, Trollslayer: a [gondor] Hobbit companion; his skirmish ability is used only on purpose
					put("arwen", "1_30");        // Arwen, Daughter of Elrond: [elven] companion, strength 6; +3 only against a Nazgûl
					put("rider", "4_286");       // Rider of Rohan: a [rohan] Man companion; his ability wants a mount
					put("merchant", "7_97");     // Gondorian Merchant: the [gondor] Man ally; his ability needs initiative
					put("celeborn", "1_34");     // Celeborn, Lord of Lórien: an [elven] ally; a fellowship ability used only on purpose
					put("barliman", "1_70");     // Barliman Butterbur: a [gandalf] Man ally, vitality 2; a fellowship ability used only on purpose
					put("savage1", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1; beaten by every king here
					put("savage2", "1_151");
					put("pursuer1", "4_190");    // Uruk Pursuer: fierce, strength 5, vitality 3; its texts are about unbound Hobbits
					put("pursuer2", "4_190");
					put("troop1", "1_177");      // Goblin Patrol Troop: strength 13, no text; beats a king of 7 or 8 without overwhelming him
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("axeman", "4_224");      // Easterling Axeman: the opponent's Man minion, strength 6, vitality 2; strength +3 and fierce only at 2 burdens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KingsLegacyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Kings' Legacy
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time Elendil wins a skirmish, make a [gondor] or [elven] companion strength +2 until the start of the regroup phase.<br>Each time Isildur wins a skirmish, heal a Man.<br>Each time Aragorn wins a skirmish, make a [gondor] Man <b>damage +1</b> until the start of the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legacy");

		assertEquals("Kings' Legacy", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ElendilWinningMakesAGondorOrElvenCompanionStrengthPlus2UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var legacy = scn.GetFreepsCard("legacy");
		var elendil = scn.GetFreepsCard("elendil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var arwen = scn.GetFreepsCard("arwen");
		var merchant = scn.GetFreepsCard("merchant");
		var celeborn = scn.GetFreepsCard("celeborn");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCardsToSupportArea(legacy);
		scn.MoveCompanionsToTable(elendil, aragorn, pippin, arwen);
		scn.MoveAlliesToTable(merchant, celeborn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{elendil, savage1}, new PhysicalCardImpl[]{arwen, savage2});

		scn.FreepsResolveSkirmish(elendil);
		scn.PassSkirmishActions();
		// The trigger and the loser's wound answer the same moment: the wound first.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(savage1));
		// [gondor] companions of two races, and an [elven] one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(elendil, aragorn, pippin, arwen));
		// A [shire] companion, a [gondor] ally and an [elven] ally.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, merchant, celeborn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(6, scn.GetStrength(arwen));
		scn.FreepsChooseCard(arwen);
		assertEquals(8, scn.GetStrength(arwen));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Still in force in the next skirmish.
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(8, scn.GetStrength(arwen));
		scn.PassSkirmishActions();

		// Gone as the regroup phase starts.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(6, scn.GetStrength(arwen));
	}

	@Test
	public void EachTimeElendilWinsAnotherPlus2IsMadeInTheFierceRound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var elendil = scn.GetFreepsCard("elendil");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pursuer1 = scn.GetShadowCard("pursuer1");
		var pursuer2 = scn.GetShadowCard("pursuer2");
		scn.MoveCardsToSupportArea(legacy);
		scn.MoveCompanionsToTable(elendil, arwen, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer1, pursuer2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{elendil, pursuer1}, new PhysicalCardImpl[]{arwen, pursuer2});

		scn.FreepsResolveSkirmish(elendil);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		scn.FreepsChooseCard(arwen);
		assertEquals(8, scn.GetStrength(arwen));
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();

		// The Pursuers are fierce: Elendil wins again, and Arwen gets a second +2.
		assertTrue(scn.InFierceRound());
		assertEquals(8, scn.GetStrength(arwen));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{elendil, pursuer1}, new PhysicalCardImpl[]{arwen, pursuer2});
		scn.FreepsResolveSkirmish(elendil);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertTrue(scn.FreepsHasCardChoiceAvailable(elendil, arwen, aragorn));
		scn.FreepsChooseCard(arwen);
		// 6 + 2 + 2
		assertEquals(10, scn.GetStrength(arwen));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(10, scn.GetStrength(arwen));
		scn.PassSkirmishActions();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(6, scn.GetStrength(arwen));
	}

	@Test
	public void KingsWhoLoseAndAGondorCompanionWhoIsNoKingWinningSetNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var elendil = scn.GetFreepsCard("elendil");
		var isildur = scn.GetFreepsCard("isildur");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var arwen = scn.GetFreepsCard("arwen");
		var barliman = scn.GetFreepsCard("barliman");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCardsToSupportArea(legacy);
		scn.MoveCompanionsToTable(elendil, isildur, aragorn, boromir, arwen);
		scn.MoveAlliesToTable(barliman);
		// A wounded Man for a heal to reach.
		scn.AddWoundsToChar(barliman, 1);

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(barliman));
		scn.MoveMinionsToTable(troop1, troop2, troop3, savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{elendil, troop1}, new PhysicalCardImpl[]{isildur, troop2},
				new PhysicalCardImpl[]{aragorn, troop3}, new PhysicalCardImpl[]{boromir, savage1});

		// Each king loses to 13 and takes his wound; nothing is asked.
		scn.FreepsResolveSkirmish(elendil);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(elendil));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(isildur);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(isildur));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Boromir beats 5: a [gondor] companion, but no king.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage1));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertEquals(1, scn.GetWoundsOn(elendil));
		assertEquals(1, scn.GetWoundsOn(isildur));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
	}

	@Test
	public void IsildurWinningHealsAManOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var legacy = scn.GetFreepsCard("legacy");
		var isildur = scn.GetFreepsCard("isildur");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var barliman = scn.GetFreepsCard("barliman");
		var savage1 = scn.GetShadowCard("savage1");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(legacy);
		scn.MoveCompanionsToTable(isildur, aragorn, boromir, pippin);
		scn.MoveAlliesToTable(barliman);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(barliman, 1);
		scn.AddWoundsToChar(pippin, 1);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertEquals(1, scn.GetWoundsOn(pippin));
		scn.MoveMinionsToTable(savage1, axeman);
		scn.AddWoundsToChar(axeman, 1);
		scn.SkipToAssignments();
		assertEquals(1, scn.GetWoundsOn(axeman));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{isildur, savage1}, new PhysicalCardImpl[]{boromir, axeman});

		scn.FreepsResolveSkirmish(isildur);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		// A Man companion, a Man ally and the opponent's Man minion, each wounded.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, barliman, axeman));
		// A wounded [gondor] Hobbit; an unwounded Hobbit.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		// One wound of his two.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void EachTimeIsildurWinsAManIsHealedInTheFierceRoundToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var isildur = scn.GetFreepsCard("isildur");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pursuer1 = scn.GetShadowCard("pursuer1");
		scn.MoveCardsToSupportArea(legacy);
		scn.MoveCompanionsToTable(isildur, aragorn);
		scn.AddWoundsToChar(aragorn, 2);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(pursuer1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(isildur, pursuer1);

		// Aragorn is the only wounded Man: healed with nothing asked.
		scn.FreepsResolveSkirmish(isildur);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(isildur, pursuer1);
		scn.FreepsResolveSkirmish(isildur);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(pursuer1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AragornWinningMakesAGondorManDamagePlus1UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		var merchant = scn.GetFreepsCard("merchant");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCardsToSupportArea(legacy);
		scn.MoveCompanionsToTable(aragorn, boromir, pippin, rider);
		scn.MoveAlliesToTable(merchant);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage1}, new PhysicalCardImpl[]{boromir, savage2});

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		// [gondor] Man companions and the [gondor] Man ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, merchant));
		// A [gondor] Hobbit and a [rohan] Man.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, rider));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		scn.FreepsChooseCard(boromir);
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Still in force in the next skirmish: Boromir's win is two wounds.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage2));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
	}

	@Test
	public void TheRohanAragornWinningSetsItOffToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var rohanaragorn = scn.GetFreepsCard("rohanaragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var merchant = scn.GetFreepsCard("merchant");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCardsToSupportArea(legacy);
		scn.MoveCompanionsToTable(rohanaragorn, boromir);
		scn.MoveAlliesToTable(merchant);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{rohanaragorn, savage1}, new PhysicalCardImpl[]{boromir, savage2});

		scn.FreepsResolveSkirmish(rohanaragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, merchant));
		// He is a [rohan] Man himself.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rohanaragorn));
		scn.FreepsChooseCard(boromir);
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void EachTimeAragornWinsAnotherDamagePlus1IsMadeInTheFierceRound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var pursuer1 = scn.GetShadowCard("pursuer1");
		var pursuer2 = scn.GetShadowCard("pursuer2");
		scn.MoveCardsToSupportArea(legacy);
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer1, pursuer2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, pursuer1}, new PhysicalCardImpl[]{boromir, pursuer2});

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		scn.FreepsChooseCard(boromir);
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, pursuer1}, new PhysicalCardImpl[]{boromir, pursuer2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		scn.FreepsChooseCard(boromir);
		assertEquals(2, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
	}
}
