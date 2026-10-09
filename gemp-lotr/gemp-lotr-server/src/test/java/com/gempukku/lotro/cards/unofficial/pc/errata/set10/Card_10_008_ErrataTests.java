package com.gempukku.lotro.cards.unofficial.pc.errata.set10;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_008_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("cirdan", "60_8");
					put("lorien", "1_53");      // Lorien Elf: Elf companion; to play, spot an Elf, so cheated in
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: Elf ally, cost 2; a fellowship ability never used here
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man, strength 8; a maneuver ability never used here

					// Seven [elven] events, one of each, only ever in a discard pile or a hand.  Defiance alone
					// is played: "Skirmish: Make an Elf strength +2 (or +4 if skirmishing a Nazgul)", cost 1.
					put("enmity", "1_29");      // Ancient Enmity
					put("border", "1_32");      // Border Defenses
					put("defiance", "1_37");    // Defiance
					put("doubleshot", "1_38");  // Double Shot: an Archery event
					put("elfsong", "1_39");     // Elf-song
					put("lightfoot", "1_52");   // Lightfootedness
					put("supporting", "4_83");  // Supporting Fire

					put("gilgalad", "1_66");    // The Tale of Gil-galad: [elven] condition; an [elven] card that is no event
					put("axestrike", "1_3");    // Axe Strike: [dwarven] Skirmish event; an event that is not [elven]

					put("men", "5_37");         // Men of Numenor: [gondor] Skirmish event, cost 0: wound a minion bearing a [gondor] fortification
					put("wall", "5_33");        // City Wall: [gondor] fortification; its bearer is strength -3

					put("nelya", "1_233");      // Ulaire Nelya, Lieutenant of Morgul: [wraith] Nazgul, strength 10, vitality 3, fierce; a Shadow-phase ability never used here
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 and nothing else
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its text is about being played, and it is cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CirdanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Cirdan, The Shipwright
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: To play, spot 2 Elves.
		 * 	<b>Skirmish:</b> Exert Cirdan to make a minion he is skirmishing strength -1 for each [elven] event in your discard pile. If that minion is overwhelmed, remove half of the [elven] events in your discard pile from the game (rounded down).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cirdan");

		assertEquals("Cirdan", card.getBlueprint().getTitle());
		assertEquals("The Shipwright", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedWithOneElfAndIsPlayedForFourOnceASecondIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirdan = scn.GetFreepsCard("cirdan");
		var lorien = scn.GetFreepsCard("lorien");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(lorien, aragorn);
		// Aragorn is of the [elven] culture but no Elf.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.ELVEN));
		scn.MoveCardsToHand(cirdan, celeborn);

		scn.StartGame();
		// One Elf, a companion.
		assertFalse(scn.FreepsPlayAvailable(cirdan));

		// A second Elf, an ally.
		scn.FreepsPlayCard(celeborn);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(cirdan));

		scn.FreepsPlayCard(cirdan);
		// 2 for Celeborn + 4
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, cirdan);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsCirdanToMakeAMinionHeIsSkirmishingStrengthMinusOneForEachElvenEventInHisOwnersDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirdan = scn.GetFreepsCard("cirdan");
		var aragorn = scn.GetFreepsCard("aragorn");
		var nelya = scn.GetShadowCard("nelya");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(cirdan, aragorn);
		// Three [elven] events in the discard pile, beside an [elven] condition and a [dwarven] event.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("enmity"), scn.GetFreepsCard("border"), scn.GetFreepsCard("defiance"),
				scn.GetFreepsCard("gilgalad"), scn.GetFreepsCard("axestrike"));
		// An [elven] event in hand, and two in the other player's discard pile.
		scn.MoveCardsToHand(scn.GetFreepsCard("doubleshot"));
		scn.MoveCardsToDiscard(scn.GetShadowCard("elfsong"), scn.GetShadowCard("lightfoot"));

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, savage, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{cirdan, savage}, new PhysicalCardImpl[]{aragorn, nelya});
		// The Goblin Runner left over is the Shadow player's to assign.
		scn.ShadowAssignToMinions(cirdan, runner);
		scn.FreepsResolveSkirmish(cirdan);

		assertEquals(0, scn.GetWoundsOn(cirdan));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(10, scn.GetStrength(nelya));
		assertTrue(scn.FreepsActionAvailable(cirdan));

		scn.FreepsUseCardAction(cirdan);
		assertEquals(1, scn.GetWoundsOn(cirdan));
		// The two minions Cirdan is skirmishing, and not the one assigned to Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(nelya));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(savage);
		// 5 - 3
		assertEquals(2, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(10, scn.GetStrength(nelya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void UsedInAnotherCompanionsSkirmishCirdanExertsAndNoMinionIsWeakened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirdan = scn.GetFreepsCard("cirdan");
		var aragorn = scn.GetFreepsCard("aragorn");
		var nelya = scn.GetShadowCard("nelya");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(cirdan, aragorn);
		scn.MoveCardsToDiscard(scn.GetFreepsCard("enmity"), scn.GetFreepsCard("border"), scn.GetFreepsCard("defiance"));

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{cirdan, savage}, new PhysicalCardImpl[]{aragorn, nelya});
		scn.FreepsResolveSkirmish(aragorn);

		// The action has a cost, so it is offered though Cirdan is skirmishing nobody yet.
		assertTrue(scn.FreepsActionAvailable(cirdan));

		scn.FreepsUseCardAction(cirdan);
		assertEquals(1, scn.GetWoundsOn(cirdan));
		// Neither the minion in the skirmish under way nor the minion Cirdan is assigned to.
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheAssignmentPhaseNorOnceCirdanIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirdan = scn.GetFreepsCard("cirdan");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(cirdan);
		scn.AddWoundsToChar(cirdan, 2);
		scn.MoveCardsToDiscard(scn.GetFreepsCard("enmity"));

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(cirdan));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(cirdan, savage);
		scn.FreepsResolveSkirmish(cirdan);
		// Vitality 4 with 2 wounds: he can exert once more.
		assertTrue(scn.FreepsActionAvailable(cirdan));
		assertEquals(5, scn.GetStrength(savage));

		scn.FreepsUseCardAction(cirdan);
		assertEquals(3, scn.GetWoundsOn(cirdan));
		// 5 - 1
		assertEquals(4, scn.GetStrength(savage));

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(cirdan));
	}

	@Test
	public void WhenThatMinionIsOverwhelmedHalfOfTheElvenEventsRoundedDownAreRemovedFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirdan = scn.GetFreepsCard("cirdan");
		var enmity = scn.GetFreepsCard("enmity");
		var border = scn.GetFreepsCard("border");
		var defiance = scn.GetFreepsCard("defiance");
		var doubleshot = scn.GetFreepsCard("doubleshot");
		var elfsong = scn.GetFreepsCard("elfsong");
		var lightfoot = scn.GetFreepsCard("lightfoot");
		var supporting = scn.GetFreepsCard("supporting");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var axestrike = scn.GetFreepsCard("axestrike");
		var theirEnmity = scn.GetShadowCard("enmity");
		var theirBorder = scn.GetShadowCard("border");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCompanionsToTable(cirdan);
		// Seven [elven] events, an [elven] condition and a [dwarven] event.
		scn.MoveCardsToDiscard(enmity, border, defiance, doubleshot, elfsong, lightfoot, supporting, gilgalad, axestrike);
		// Two [elven] events in the other player's discard pile.
		scn.MoveCardsToDiscard(theirEnmity, theirBorder);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(cirdan, nelya);
		scn.FreepsResolveSkirmish(cirdan);
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(0, scn.GetWoundsOn(cirdan));

		scn.FreepsUseCardAction(cirdan);
		assertEquals(1, scn.GetWoundsOn(cirdan));
		// 10 - 7
		assertEquals(3, scn.GetStrength(nelya));

		// 7 against 3: more than double.
		scn.PassSkirmishActions();
		// The skirmish's result and Cirdan's text happen together: the result first.
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DISCARD, nelya);

		// Half of seven, rounded down: three, of the seven [elven] events and nothing else.
		assertTrue(scn.FreepsHasCardChoiceAvailable(enmity, border, defiance, doubleshot, elfsong, lightfoot, supporting));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gilgalad, axestrike));
		assertEquals(7, scn.FreepsGetCardChoiceCount());
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(enmity, border, defiance);
		assertInZone(Zone.REMOVED, enmity, border, defiance);
		assertInZone(Zone.DISCARD, doubleshot, elfsong, lightfoot, supporting, gilgalad, axestrike);
		assertInZone(Zone.DISCARD, theirEnmity, theirBorder);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheStrengthIsNotCountedAgainAndTheHalfIsOfTheEventsThereWhenTheMinionIsOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirdan = scn.GetFreepsCard("cirdan");
		var enmity = scn.GetFreepsCard("enmity");
		var border = scn.GetFreepsCard("border");
		var elfsong = scn.GetFreepsCard("elfsong");
		var defiance = scn.GetFreepsCard("defiance");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(cirdan);
		scn.MoveCardsToDiscard(enmity, border, elfsong);
		scn.MoveCardsToHand(defiance);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(cirdan, savage);
		scn.FreepsResolveSkirmish(cirdan);

		scn.FreepsUseCardAction(cirdan);
		// 5 - 3
		assertEquals(2, scn.GetStrength(savage));
		scn.ShadowPass();

		// A fourth [elven] event goes to the discard pile.  Cirdan is the only Elf: 7 + 2.
		scn.FreepsPlayCard(defiance);
		assertInZone(Zone.DISCARD, defiance);
		assertEquals(9, scn.GetStrength(cirdan));
		// Still 5 - 3: what Cirdan took off is not worked out again.
		assertEquals(2, scn.GetStrength(savage));

		// 9 against 2: more than double.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DISCARD, savage);

		// Half of the four there now, not of the three there when Cirdan exerted.
		assertEquals(4, scn.FreepsGetCardChoiceCount());
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(enmity, defiance);
		assertInZone(Zone.REMOVED, enmity, defiance);
		assertInZone(Zone.DISCARD, border, elfsong);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void UsedTwiceOnAMinionThatIsThenOverwhelmedHalfIsRemovedAndThenHalfOfWhatIsLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirdan = scn.GetFreepsCard("cirdan");
		var enmity = scn.GetFreepsCard("enmity");
		var border = scn.GetFreepsCard("border");
		var elfsong = scn.GetFreepsCard("elfsong");
		var lightfoot = scn.GetFreepsCard("lightfoot");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCompanionsToTable(cirdan);
		scn.MoveCardsToDiscard(enmity, border, elfsong, lightfoot);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(cirdan, nelya);
		scn.FreepsResolveSkirmish(cirdan);
		assertEquals(10, scn.GetStrength(nelya));

		scn.FreepsUseCardAction(cirdan);
		// 10 - 4
		assertEquals(6, scn.GetStrength(nelya));
		scn.ShadowPass();
		scn.FreepsUseCardAction(cirdan);
		// 10 - 4 - 4
		assertEquals(2, scn.GetStrength(nelya));
		assertEquals(2, scn.GetWoundsOn(cirdan));

		// 7 against 2: more than double.  Each use brought its own "if that minion is overwhelmed".
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DISCARD, nelya);

		// Half of four.
		assertEquals(4, scn.FreepsGetCardChoiceCount());
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(enmity, border);
		assertInZone(Zone.REMOVED, enmity, border);

		// Half of the two that are left.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(elfsong);
		assertInZone(Zone.REMOVED, enmity, border, elfsong);
		assertInZone(Zone.DISCARD, lightfoot);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOneElvenEventInTheDiscardPileNothingIsRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirdan = scn.GetFreepsCard("cirdan");
		var enmity = scn.GetFreepsCard("enmity");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(cirdan);
		// 7 + 1 = 8, double the Uruk Savage's 5 - 1.
		scn.ApplyAdHocModifier(new StrengthModifier(null, cirdan, null, 1));
		// One [elven] event, and an [elven] condition that would make it two [elven] cards.
		scn.MoveCardsToDiscard(enmity, gilgalad);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(cirdan, savage);
		scn.FreepsResolveSkirmish(cirdan);
		assertEquals(5, scn.GetStrength(savage));

		scn.FreepsUseCardAction(cirdan);
		// 5 - 1
		assertEquals(4, scn.GetStrength(savage));

		// 8 against 4: overwhelmed.  Half of one, rounded down, is none.
		scn.PassSkirmishActions();
		// The skirmish's result and Cirdan's text happen together: the result first.
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DISCARD, savage);
		assertInZone(Zone.DISCARD, enmity, gilgalad);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMinionBeatenWithoutBeingOverwhelmedRemovesNothingAndBothEffectsEndWithTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirdan = scn.GetFreepsCard("cirdan");
		var enmity = scn.GetFreepsCard("enmity");
		var border = scn.GetFreepsCard("border");
		var elfsong = scn.GetFreepsCard("elfsong");
		var lightfoot = scn.GetFreepsCard("lightfoot");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCompanionsToTable(cirdan);
		scn.MoveCardsToDiscard(enmity, border, elfsong, lightfoot);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(cirdan, nelya);
		scn.FreepsResolveSkirmish(cirdan);

		scn.FreepsUseCardAction(cirdan);
		// 10 - 4
		assertEquals(6, scn.GetStrength(nelya));
		scn.ShadowPass();
		assertEquals(6, scn.GetStrength(nelya));

		// 7 against 6: Ulaire Nelya loses, is not overwhelmed, and takes the skirmish's wound.
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertInZone(Zone.DISCARD, enmity, border, elfsong, lightfoot);

		// Ulaire Nelya is fierce.  In the second skirmish the strength is what is printed again.
		assertTrue(scn.InFierceRound());
		// 7 + 13 = 20, double Ulaire Nelya's 10.
		scn.ApplyAdHocModifier(new StrengthModifier(null, cirdan, null, 13));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(cirdan, nelya);
		scn.FreepsResolveSkirmish(cirdan);
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(20, scn.GetStrength(cirdan));

		// Overwhelmed in a skirmish in which Cirdan did not exert: nothing is removed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, nelya);
		assertInZone(Zone.DISCARD, enmity, border, elfsong, lightfoot);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnotherMinionOverwhelmedAfterTheChosenOneWasKilledRemovesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirdan = scn.GetFreepsCard("cirdan");
		var enmity = scn.GetFreepsCard("enmity");
		var border = scn.GetFreepsCard("border");
		var men = scn.GetFreepsCard("men");
		var wall = scn.GetFreepsCard("wall");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(cirdan);
		// 7 + 3 = 10, double the Uruk Savage's 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, cirdan, null, 3));
		scn.MoveCardsToDiscard(enmity, border);
		scn.MoveCardsToHand(men);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		// The Goblin Runner bears a [gondor] fortification, so that Men of Numenor can wound it: 5 - 3.
		scn.AttachCardsTo(runner, wall);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(cirdan, runner);
		scn.ShadowAssignToMinions(cirdan, savage);
		scn.FreepsResolveSkirmish(cirdan);
		assertEquals(2, scn.GetStrength(runner));

		scn.FreepsUseCardAction(cirdan);
		scn.FreepsChooseCard(runner);
		// 5 - 3 - 2
		assertEquals(0, scn.GetStrength(runner));
		assertEquals(5, scn.GetStrength(savage));
		scn.ShadowPass();

		// The Goblin Runner, of vitality 1, is killed by a wound and not by being overwhelmed.
		scn.FreepsPlayCard(men);
		assertInZone(Zone.DISCARD, runner);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);

		// 10 against 5: the Uruk Savage is overwhelmed, and it is not the minion Cirdan weakened.
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, savage);
		assertInZone(Zone.DISCARD, enmity, border);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
