package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_010_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("supplies", "18_10");
					put("supplies2", "18_10");
					// A Path Appointed: [gondor] support-area condition that its fellowship ability gives [gondor]
					// tokens; its abilities need a [gondor] Man to exert or a regroup phase.
					put("path", "8_42");
					// Strands of Elven Hair: [elven] possession in the support area, given 2 [elven] tokens as it is
					// played; cheated in, its fellowship ability is used only on purpose.
					put("strands", "9_22");
					// Ships of Great Draught: [raider] support-area possession that its Shadow ability gives [raider]
					// tokens: a Shadow card holding culture tokens.
					put("ships", "8_65");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("supplies", "18_10");
					put("supplies2", "18_10");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, strength 6; +3 against a Nazgûl only
					put("guardian", "4_76");    // Lórien Guardian: [elven] Elf companion, strength 5; a regroup ability only

					put("scout", "1_191");      // Moria Scout: [moria] Orc, strength 6, vitality 2; a play trigger only
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] Uruk-hai, strength 5, vitality 3, fierce; texts about unbound Hobbits
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; a play trigger only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSite3Scenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("supplies", "18_10");
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf companion, strength 6, damage +1
					// Elrond, Lord of Rivendell: [elven] Elf ally, strength 8, home site 3, where he may be assigned.
					// His start-of-turn heal finds nobody wounded.
					put("elrond", "1_40");

					put("scout", "1_191");      // Moria Scout: [moria] Orc, strength 6, vitality 2; a play trigger only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; a play trigger only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElvenSuppliesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Elven Supplies
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Fellowship:</b> Remove a culture token from a Free Peoples card to add an [elven] token here.<br><b>Skirmish:</b> Remove 2 [elven] tokens from here to make a minion skirmishing an [elven] companion strength -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("supplies");

		assertEquals("Elven Supplies", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipAbilityMovesACultureTokenFromAFreePeoplesCardOfAnyCultureToThisCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var supplies = scn.GetFreepsCard("supplies");
		var supplies2 = scn.GetFreepsCard("supplies2");
		var path = scn.GetFreepsCard("path");
		var strands = scn.GetFreepsCard("strands");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToSupportArea(supplies, supplies2, path, strands, ships);
		scn.AddTokensToCard(supplies, 1);
		scn.AddTokensToCard(supplies2, 1);
		scn.AddTokensToCard(path, 2);
		scn.AddTokensToCard(strands, 2);
		scn.AddTokensToCard(ships, 2);
		assertEquals(1, scn.GetCultureTokensOn(supplies));
		assertEquals(1, scn.GetCultureTokensOn(supplies2));
		assertEquals(2, scn.GetCultureTokensOn(path));
		assertEquals(2, scn.GetCultureTokensOn(strands));
		assertEquals(2, scn.GetCultureTokensOn(ships));

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(supplies));
		scn.FreepsUseCardAction(supplies);
		// Free Peoples cards of both cultures and two types, this one and the other copy among them; not the
		// Shadow card.
		assertTrue(scn.FreepsHasCardChoiceAvailable(supplies, supplies2, path, strands));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ships));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(path);

		// A [gondor] token paid, an [elven] token on this copy, and on no other card
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(2, scn.GetCultureTokensOn(supplies));
		assertEquals(1, scn.GetCultureTokensOn(supplies2));
		assertEquals(2, scn.GetCultureTokensOn(strands));
		assertEquals(2, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedWithNoCultureTokenOnAFreePeoplesCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var supplies = scn.GetFreepsCard("supplies");
		var strands = scn.GetFreepsCard("strands");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToSupportArea(supplies, strands, ships);
		// Tokens on a Shadow card only
		scn.AddTokensToCard(ships, 2);
		assertEquals(0, scn.GetCultureTokensOn(supplies));
		assertEquals(0, scn.GetCultureTokensOn(strands));
		assertEquals(2, scn.GetCultureTokensOn(ships));

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(supplies));
	}

	@Test
	public void SkirmishAbilityRemoves2TokensToMakeAMinionSkirmishingAnElvenCompanionStrengthMinus1ForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var supplies = scn.GetFreepsCard("supplies");
		var arwen = scn.GetFreepsCard("arwen");
		var guardian = scn.GetFreepsCard("guardian");
		scn.MoveCompanionsToTable(arwen, guardian);
		scn.MoveCardsToSupportArea(supplies);
		scn.AddTokensToCard(supplies, 3);
		assertEquals(3, scn.GetCultureTokensOn(supplies));

		var scout = scn.GetShadowCard("scout");
		var pursuer = scn.GetShadowCard("pursuer");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(scout, pursuer, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{arwen, scout},
				new PhysicalCardImpl[]{guardian, runner});
		scn.ShadowAssignToMinions(arwen, pursuer);

		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, scout));
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, pursuer));
		assertEquals(6, scn.GetStrength(scout));
		assertEquals(5, scn.GetStrength(pursuer));
		assertEquals(5, scn.GetStrength(runner));

		assertTrue(scn.FreepsActionAvailable(supplies));
		scn.FreepsUseCardAction(supplies);
		assertEquals(1, scn.GetCultureTokensOn(supplies));
		// Both minions skirmishing Arwen, an Orc and an Uruk-hai of two cultures; not the Runner, assigned to
		// the Guardian, whose skirmish has not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(scout, pursuer));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner));
		scn.FreepsChooseCard(scout);
		assertEquals(5, scn.GetStrength(scout));
		assertEquals(5, scn.GetStrength(pursuer));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Arwen 6 against 5 + 5 loses, and takes one wound; the Scout lives on.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertInZone(Zone.SHADOW_CHARACTERS, scout);

		// In the next skirmish the Scout is back to 6.
		scn.FreepsResolveSkirmish(guardian);
		assertTrue(scn.IsCharSkirmishingAgainst(guardian, runner));
		assertEquals(6, scn.GetStrength(scout));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsPaidForNothingWhereNoElvenCompanionIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSite3Scenario();

		var supplies = scn.GetFreepsCard("supplies");
		var gimli = scn.GetFreepsCard("gimli");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(supplies);
		scn.AddTokensToCard(supplies, 4);
		assertEquals(4, scn.GetCultureTokensOn(supplies));

		var scout = scn.GetShadowCard("scout");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(scout, runner);

		// The fellowship moves to site 3, Elrond's home.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, runner},
				new PhysicalCardImpl[]{elrond, scout});

		// A companion who is not [elven]
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, runner));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.FreepsActionAvailable(supplies));
		scn.FreepsUseCardAction(supplies);
		assertEquals(2, scn.GetCultureTokensOn(supplies));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(6, scn.GetStrength(scout));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Gimli 6 against 5 wins, and the Runner dies.
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);

		// An [elven] Elf who is an ally, not a companion
		scn.FreepsResolveSkirmish(elrond);
		assertTrue(scn.IsCharSkirmishingAgainst(elrond, scout));
		assertEquals(6, scn.GetStrength(scout));
		assertTrue(scn.FreepsActionAvailable(supplies));
		scn.FreepsUseCardAction(supplies);
		assertEquals(0, scn.GetCultureTokensOn(supplies));
		assertEquals(6, scn.GetStrength(scout));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithFewerThan2ElvenTokensHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var supplies = scn.GetFreepsCard("supplies");
		var supplies2 = scn.GetFreepsCard("supplies2");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(supplies, supplies2);
		// One token here, and two on the other copy
		scn.AddTokensToCard(supplies, 1);
		scn.AddTokensToCard(supplies2, 2);
		assertEquals(1, scn.GetCultureTokensOn(supplies));
		assertEquals(2, scn.GetCultureTokensOn(supplies2));

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, runner);
		scn.FreepsResolveSkirmish(arwen);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(supplies));
		assertTrue(scn.FreepsActionAvailable(supplies2));
	}
}
