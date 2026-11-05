package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_088_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rider", "4_88");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: strength 6, +1 with Frodo (a Hobbit) in the fellowship: 7; vitality 4
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf, strength 6; +2 only with 3 Ents
					// Not the First Halfling: another [gandalf] condition that carries [gandalf] tokens by its own
					// fellowship ability; its abilities are used only on purpose.
					put("halfling", "8_18");
					put("pursuer", "4_190");    // Uruk Pursuer: fierce [isengard] Uruk-hai, strength 5, vitality 3; its texts are about unbound Hobbits
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; a "when you play" only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gandalf in a skirmish against two Uruk Savages, with two [gandalf] tokens on the condition.
	protected VirtualTableScenario GetTwoMinionsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rider", "4_88");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: vitality 4; his text is about his own strength
					put("savage1", "1_151");    // Uruk Savage: strength 5, vitality 3
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BeholdtheWhiteRiderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Behold the White Rider
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>Each time Gandalf wins a skirmish, you may place a [gandalf] token here.<br><b>Skirmish:</b> Wound a minion skirmishing Gandalf for each [gandalf] token here. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rider");

		assertEquals("Behold the White Rider", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// "Wound a minion skirmishing Gandalf for each [gandalf] token here": with "for each" the wounds may go to
	// several minions or to one more than once (CR 4.2 "for each"; the shape of F-0224).  The definition chose
	// one minion and wounded it once for each token.
	// F-0458.
	@Test
	public void TheWoundsForEachTokenMayGoToDifferentMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoMinionsScenario();

		var rider = scn.GetFreepsCard("rider");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(rider);
		scn.AddTokensToCard(rider, 2);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();

		assertInZone(Zone.SUPPORT, rider);
		assertEquals(2, scn.GetCultureTokensOn(rider));
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, savage1);
		scn.ShadowAssignToMinions(gandalf, savage2);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.FreepsActionAvailable(rider));
		scn.FreepsUseCardAction(rider);

		// The first token's wound: either minion.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(savage1);
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(savage2));

		// The second token's wound: either minion again, and it goes to the other.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(savage2);
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(1, scn.GetWoundsOn(savage2));

		assertInZone(Zone.DISCARD, rider);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void EachTimeGandalfWinsASkirmishATokenMayBePlacedHereAndAgainInTheFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetFreepsCard("rider");
		var halfling = scn.GetFreepsCard("halfling");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(rider, halfling);
		scn.AddTokensToCard(halfling, 2);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();

		assertInZone(Zone.SUPPORT, rider);
		assertEquals(0, scn.GetCultureTokensOn(rider));
		assertEquals(2, scn.GetCultureTokensOn(halfling));
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, pursuer);
		scn.FreepsResolveSkirmish(gandalf);
		// Gandalf (7) beats the Pursuer (5).
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rider));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(rider));
		// Here, and not on the other [gandalf] condition.
		assertEquals(2, scn.GetCultureTokensOn(halfling));

		// The Pursuer is fierce: Gandalf wins the fierce skirmish too, and is offered it again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gandalf, pursuer);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rider));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(rider));
		assertEquals(2, scn.GetCultureTokensOn(halfling));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheTokenMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetFreepsCard("rider");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(rider);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertInZone(Zone.SUPPORT, rider);
		assertEquals(0, scn.GetCultureTokensOn(rider));
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, savage);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rider));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetCultureTokensOn(rider));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInZone(Zone.SUPPORT, rider);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NoTokenIsOfferedWhenGandalfLosesOrWhenAnotherGandalfCompanionWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetFreepsCard("rider");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToSupportArea(rider);

		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();

		assertInZone(Zone.SUPPORT, rider);
		assertEquals(0, scn.GetCultureTokensOn(rider));
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gandalf, troop},
				new PhysicalCardImpl[]{avenger, runner});

		// The Ent Avenger (6) beats the Runner (5).
		scn.FreepsResolveSkirmish(avenger);
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(rider));
		assertEquals(0, scn.GetCultureTokensOn(rider));

		// The Troop (13) beats Gandalf (7), who is not overwhelmed and lives with a wound.
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(rider));
		assertEquals(0, scn.GetCultureTokensOn(rider));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityWoundsMinionsSkirmishingGandalfOnceForEachTokenHereOneMinionMayTakeEveryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetFreepsCard("rider");
		var halfling = scn.GetFreepsCard("halfling");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(rider, halfling);
		scn.AddTokensToCard(rider, 2);
		scn.AddTokensToCard(halfling, 2);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();

		assertInZone(Zone.SUPPORT, rider);
		assertEquals(2, scn.GetCultureTokensOn(rider));
		assertEquals(2, scn.GetCultureTokensOn(halfling));
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, savage);
		scn.ShadowAssignToMinions(gandalf, runner);
		scn.FreepsResolveSkirmish(gandalf);

		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(rider));
		scn.FreepsUseCardAction(rider);

		// Each wound a choice of either minion skirmishing Gandalf, the Runner at vitality 1 among them (a
		// wound, not an exertion); the Savage takes both (CR 4.2 "for each").
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));

		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, runner));
		scn.FreepsChooseCard(savage);
		assertEquals(2, scn.GetWoundsOn(savage));

		// Two tokens here, two wounds: the tokens on the other condition are not counted.  Then the condition is
		// discarded.
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInDiscard(rider);
		assertEquals(2, scn.GetCultureTokensOn(halfling));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithOneTokenHereTheSkirmishAbilityWoundsOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetFreepsCard("rider");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(rider);
		scn.AddTokensToCard(rider, 1);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();

		assertInZone(Zone.SUPPORT, rider);
		assertEquals(1, scn.GetCultureTokensOn(rider));
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsActionAvailable(rider));
		scn.FreepsUseCardAction(rider);

		// The only minion skirmishing Gandalf takes the one wound unasked.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertInDiscard(rider);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InAnotherCompanionsSkirmishTheSkirmishAbilityWoundsNobodyAndDiscardsItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetFreepsCard("rider");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToSupportArea(rider);
		scn.AddTokensToCard(rider, 1);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertInZone(Zone.SUPPORT, rider);
		assertEquals(1, scn.GetCultureTokensOn(rider));
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gandalf, savage},
				new PhysicalCardImpl[]{avenger, troop});

		// The Ent Avenger's skirmish first: the Troop is skirmishing a [gandalf] companion who is no Gandalf, and
		// the Savage, assigned to Gandalf, is not skirmishing him yet.
		scn.FreepsResolveSkirmish(avenger);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// Discarding itself is something done, so it is offered with nobody to wound.
		assertTrue(scn.FreepsActionAvailable(rider));
		scn.FreepsUseCardAction(rider);

		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInDiscard(rider);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetFreepsCard("rider");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(rider);
		scn.AddTokensToCard(rider, 1);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertInZone(Zone.SUPPORT, rider);
		assertEquals(1, scn.GetCultureTokensOn(rider));
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(rider));

		// In the skirmish that follows it is offered.
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(gandalf, savage);
		scn.FreepsResolveSkirmish(gandalf);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(rider));
		assertEquals(0, scn.GetWoundsOn(savage));
	}
}
