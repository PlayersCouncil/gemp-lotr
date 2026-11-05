package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_031_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  Turn 1's skirmishes
	// are at The Troll Hoard 30_50 (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guards", "33_31");

					put("watchful", "30_40");   // Watchful Orc: [gundabad] Orc, strength 8, vitality 3; a Shadow ability not used
					put("yazneg", "30_41");     // Yazneg, Orkish Assassin: [gundabad] Orc, strength 5, vitality 2; a Maneuver ability not used
					put("azog", "31_23");       // Azog, The Defiler: [gundabad] Orc, strength 9, damage +1; a trigger on playing a mount
					put("ferny", "33_22");      // Bill Ferny Sr., Servant of Azog: a [gundabad] Man; only the Shadow player may assign him
					put("runner", "31_30");     // Goblin Runner: a [moria] Orc; a trigger on its own play only
					// Assaulting Goblin: a [moria] Orc, strength 7, vitality 2; a Response only at a battleground
					put("goblin", "32_27");
					put("warg", "31_27");       // Warg: [gundabad] mount on a [gundabad] Orc, bearer fierce; a Regroup ability not used

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, strength 7, vitality 3; a Fellowship ability not used
					// Thranduil, Elven King: an [elven] Elf ally, strength 9, vitality 4; his Maneuver ability (discard a
					// [dwarven] artifact) lets him skirmish
					put("thranduil", "31_12");
					put("emeralds", "31_3");    // Emeralds of Girion: a [dwarven] artifact in the support area, paid for Thranduil
					put("gandalf", "30_26");    // Gandalf, Friend of Thorin: [gandalf] companion; standing texts that reach no test here
					put("courage", "30_30");    // He gives me courage: [gandalf] Maneuver event, spot Gandalf to remove a doubt

					put("jails", "33_32");      // Elven Jails: a [mirkwood] condition that stacks [dwarven] companions
					put("fili", "30_10");       // Fili, Brother of Kili: a [dwarven] companion, stacked on the Jails
					// Here Is Good Rock: a [dwarven] condition that stacks Free Peoples cards from hand
					put("rock", "4_50");
					put("knowledge", "30_1");   // Ancestral Knowledge: a [dwarven] event, stacked on the Rock
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void ElfGuardsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Elf-Guards
		 * Unique: True
		 * Side: Shadow
		 * Culture: Mirkwood
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time a Free Peoples character loses a skirmish, you may discard a [gundabad] Orc from play to add a doubt.
		 * 	While a [dwarven] card is stacked on a [mirkwood] condition, doubts cannot be removed.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("guards");

		assertEquals("Elf-Guards", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MIRKWOOD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ACompanionLosingASkirmishLetsTheShadowPlayerDiscardAGundabadOrcToAddADoubt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guards = scn.GetShadowCard("guards");
		var watchful = scn.GetShadowCard("watchful");
		var yazneg = scn.GetShadowCard("yazneg");
		var ferny = scn.GetShadowCard("ferny");
		var runner = scn.GetShadowCard("runner");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(guards);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, guards);
		scn.MoveMinionsToTable(watchful, yazneg, ferny, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, watchful);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(balin));
		scn.PassSkirmishActions();
		// 8 against 7: Balin loses.
		assertEquals(1, scn.GetWoundsOn(balin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(guards));
		scn.ShadowAcceptOptionalTrigger();
		// [gundabad] Orcs of two titles, the winner among them; not a [gundabad] Man, not a [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(watchful, yazneg));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ferny, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(yazneg);

		assertInDiscard(yazneg);
		assertInZone(Zone.SHADOW_CHARACTERS, watchful, ferny, runner);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyLosingASkirmishSetsItOffToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guards = scn.GetShadowCard("guards");
		var azog = scn.GetShadowCard("azog");
		var yazneg = scn.GetShadowCard("yazneg");
		var thranduil = scn.GetFreepsCard("thranduil");
		var emeralds = scn.GetFreepsCard("emeralds");
		scn.MoveAlliesToTable(thranduil);
		scn.MoveCardsToSupportArea(guards, emeralds);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, guards, emeralds);
		scn.MoveMinionsToTable(azog, yazneg);
		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.FreepsCanAssign(thranduil));
		scn.FreepsUseCardAction(thranduil);
		assertInDiscard(emeralds);
		assertTrue(scn.FreepsCanAssign(thranduil));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thranduil, azog);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thranduil);

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(thranduil));
		scn.PassSkirmishActions();
		// 9 against 9: the tie goes to the Shadow side, and Thranduil takes 2 wounds (damage +1).
		assertEquals(2, scn.GetWoundsOn(thranduil));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(guards));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(yazneg);

		assertInDiscard(yazneg);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DeclinedItIsOfferedAgainWhenTheSameCompanionLosesTheFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guards = scn.GetShadowCard("guards");
		var watchful = scn.GetShadowCard("watchful");
		var warg = scn.GetShadowCard("warg");
		var yazneg = scn.GetShadowCard("yazneg");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(guards);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, guards);
		scn.MoveMinionsToTable(watchful, yazneg);
		// The Warg makes Watchful Orc fierce.
		scn.AttachCardsTo(watchful, warg);
		assertAttachedTo(warg, watchful);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, watchful);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);

		assertEquals(0, scn.GetBurdens());
		scn.PassSkirmishActions();
		// 8 + 2 against 7: Balin loses.
		assertEquals(1, scn.GetWoundsOn(balin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(guards));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, watchful, yazneg);

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(balin, watchful);
		scn.FreepsResolveSkirmish(balin);
		scn.PassFierceSkirmishActions();
		// He loses the fierce skirmish too, and lives.
		assertEquals(2, scn.GetWoundsOn(balin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(guards));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(yazneg);
		assertInDiscard(yazneg);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenTheFreePeoplesCharacterWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guards = scn.GetShadowCard("guards");
		var watchful = scn.GetShadowCard("watchful");
		var yazneg = scn.GetShadowCard("yazneg");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(guards);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, guards);
		scn.MoveMinionsToTable(watchful, yazneg);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, yazneg);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);

		assertEquals(0, scn.GetWoundsOn(yazneg));
		scn.PassSkirmishActions();
		// 7 against 5: the minion loses and lives.
		assertEquals(1, scn.GetWoundsOn(yazneg));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, watchful, yazneg);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithNoGundabadOrcToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guards = scn.GetShadowCard("guards");
		var goblin = scn.GetShadowCard("goblin");
		var ferny = scn.GetShadowCard("ferny");
		var runner = scn.GetShadowCard("runner");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(guards);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, guards);
		scn.MoveMinionsToTable(goblin, ferny, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, goblin);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);

		scn.PassSkirmishActions();
		// 7 against 7: Balin loses; only a [moria] Orc and a [gundabad] Man could be discarded.
		assertEquals(1, scn.GetWoundsOn(balin));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, goblin, ferny, runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DoubtsCannotBeRemovedWhileADwarvenCardIsStackedOnAMirkwoodCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guards = scn.GetShadowCard("guards");
		var jails = scn.GetShadowCard("jails");
		var yazneg = scn.GetShadowCard("yazneg");
		var gandalf = scn.GetFreepsCard("gandalf");
		var fili = scn.GetFreepsCard("fili");
		var courage = scn.GetFreepsCard("courage");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(guards, jails);
		scn.MoveCardsToHand(courage);

		scn.StartGame();
		// Fili in the Jails, as its Regroup ability puts him there.
		scn.StackCardsOn(jails, fili);
		assertTrue(scn.GetStackedCards(jails).contains(fili));
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		assertInHand(courage);
		scn.MoveMinionsToTable(yazneg);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(courage);
		assertInDiscard(courage);
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DoubtsCanBeRemovedWithNoDwarvenCardOnAMirkwoodCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guards = scn.GetShadowCard("guards");
		var jails = scn.GetShadowCard("jails");
		var yazneg = scn.GetShadowCard("yazneg");
		var gandalf = scn.GetFreepsCard("gandalf");
		var rock = scn.GetFreepsCard("rock");
		var knowledge = scn.GetFreepsCard("knowledge");
		var courage = scn.GetFreepsCard("courage");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(guards, jails);
		scn.MoveCardsToSupportArea(rock);
		scn.MoveCardsToHand(courage);

		scn.StartGame();
		// The Jails hold nothing; a [dwarven] card is stacked on a [dwarven] condition, as the Rock's ability puts it there.
		scn.StackCardsOn(rock, knowledge);
		assertTrue(scn.GetStackedCards(rock).contains(knowledge));
		assertTrue(scn.GetStackedCards(jails).isEmpty());
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		assertInHand(courage);
		scn.MoveMinionsToTable(yazneg);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(courage);
		assertInDiscard(courage);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
