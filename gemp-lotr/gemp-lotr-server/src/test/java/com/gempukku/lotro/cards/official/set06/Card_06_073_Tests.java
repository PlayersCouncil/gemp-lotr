package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_073_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scaffold", "6_73");
					put("scaffold2", "6_73");
					// [isengard] Orcs, each of site number 4. Artisan (cost 4, strength 8, vitality 3), Journeyman (cost 2,
					// vitality 3) and Tinker (cost 1, strength 5, vitality 2) share one text: "Regroup: Discard an
					// [isengard] Orc to make the Free Peoples player wound a companion."
					put("artisan", "6_65");
					put("journeyman", "6_67");
					put("tinker", "6_71");
					// Isengard Builder: "Regroup: If stacked on an [isengard] card, discard this card to add (2)."
					put("builder", "6_66");
					// Isengard Tender and Warg-rider: cards for the stack. The Tender's regroup ability, from a stack,
					// discards it and removes (2) to exhaust a companion, and is never used; the Warg-rider has none
					put("tender", "6_70");
					put("warg", "5_67");
					// Isengard Worker: strength 5, vitality 3; its regroup ability exerts it, and is used only on purpose
					put("worker", "3_62");
					put("savage", "1_151");     // Uruk Savage: an [isengard] minion who is an Uruk-hai, strength 5, vitality 3
					put("runner", "1_178");     // Goblin Runner: an Orc of [moria], strength 5, vitality 1

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man and a ranger, strength 8, vitality 4
					put("sword", "1_112");      // Ranger's Sword: on Aragorn, strength +2 and damage +1
					// Gondor's Vengeance: Regroup event, cost 0. "Exert a ranger companion to discard a minion."
					put("vengeance", "1_106");
					put("vengeance2", "1_106");
					put("vengeance3", "1_106");
					// What Are They?: Maneuver event, cost 1. "Spot a ranger to discard a roaming minion."
					put("whatare", "1_119");
					// Calaglin, Elf of Lorien: ally. "Regroup: Exert Calaglin to wound a minion."
					put("calaglin", "3_10");
					// Defend It and Hope: Maneuver event, cost 0: exert a [gondor] Man to wound a minion once (twice an
					// Uruk-hai)
					put("defend1", "4_115");
					put("defend2", "4_115");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ScaffoldingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Scaffolding
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>Each time a regroup action discards an [isengard] Orc, you may stack that Orc on this card.<br><b>Response:</b> If an [isengard] Orc is about to take a wound, discard 2 cards stacked here to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("scaffold");

		assertEquals("Scaffolding", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AnIsengardOrcDiscardedByAFreePeoplesRegroupEventMayBeStackedHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var vengeance = scn.GetFreepsCard("vengeance");
		var vengeance2 = scn.GetFreepsCard("vengeance2");
		var vengeance3 = scn.GetFreepsCard("vengeance3");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(vengeance, vengeance2, vengeance3);

		var scaffold = scn.GetShadowCard("scaffold");
		var tinker = scn.GetShadowCard("tinker");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var journeyman = scn.GetShadowCard("journeyman");
		scn.MoveCardsToSupportArea(scaffold);
		// An [isengard] Orc that was in the discard pile before: the Orc stacked is the one just discarded, not
		// this one.
		scn.MoveCardsToDiscard(journeyman);

		scn.StartGame();
		assertInZone(Zone.DISCARD, journeyman);
		scn.MoveMinionsToTable(tinker, savage, runner);
		// Nobody is assigned to them, so all three are in play when the regroup phase starts.
		scn.SkipToPhase(Phase.REGROUP);

		// An [isengard] minion who is no Orc.
		scn.FreepsPlayCard(vengeance);
		scn.FreepsChooseCard(savage);
		assertInZone(Zone.DISCARD, savage);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scaffold));
		scn.ShadowPass();

		// An Orc of another culture.
		scn.FreepsPlayCard(vengeance2);
		scn.FreepsChooseCard(runner);
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scaffold));
		scn.ShadowPass();

		// The [isengard] Orc is the one minion left.
		scn.FreepsPlayCard(vengeance3);
		assertInZone(Zone.DISCARD, tinker);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scaffold));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.STACKED, tinker);
		assertEquals(1, scn.GetStackedCards(scaffold).size());
		assertTrue(scn.GetStackedCards(scaffold).contains(tinker));
		assertInZone(Zone.DISCARD, savage, runner, journeyman);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AnIsengardOrcDiscardedToPayForARegroupAbilityMayBeStackedOnOneCopyEachTimeItHappens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var scaffold = scn.GetShadowCard("scaffold");
		var scaffold2 = scn.GetShadowCard("scaffold2");
		var artisan = scn.GetShadowCard("artisan");
		var journeyman = scn.GetShadowCard("journeyman");
		scn.MoveCardsToSupportArea(scaffold, scaffold2);

		scn.StartGame();
		scn.MoveMinionsToTable(artisan, journeyman);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(artisan));
		scn.ShadowUseCardAction(artisan);
		// The Artisan pays with itself.
		scn.ShadowChooseCard(artisan);
		assertInZone(Zone.DISCARD, artisan);

		// Each copy offers to take it, and it goes to the copy chosen.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scaffold));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scaffold2));
		scn.ShadowChooseOptionalTrigger(scaffold2);

		assertInZone(Zone.STACKED, artisan);
		assertEquals(0, scn.GetStackedCards(scaffold).size());
		assertEquals(1, scn.GetStackedCards(scaffold2).size());
		assertTrue(scn.GetStackedCards(scaffold2).contains(artisan));
		// The Orc is no longer in the discard pile for the other copy to take: the Artisan's ability goes on to
		// its wound.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scaffold));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// A second regroup ability discards a second Orc: offered again, and declined.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(journeyman));
		scn.ShadowUseCardAction(journeyman);
		assertInZone(Zone.DISCARD, journeyman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scaffold));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scaffold2));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, journeyman);
		assertEquals(0, scn.GetStackedCards(scaffold).size());
		assertEquals(1, scn.GetStackedCards(scaffold2).size());
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnIsengardOrcDiscardedByAManeuverEventIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var whatare = scn.GetFreepsCard("whatare");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(whatare);

		var scaffold = scn.GetShadowCard("scaffold");
		var tinker = scn.GetShadowCard("tinker");
		scn.MoveCardsToSupportArea(scaffold);

		scn.StartGame();
		scn.MoveMinionsToTable(tinker);
		scn.SkipToPhase(Phase.MANEUVER);

		// Site number 4 at site 2: the Tinker is roaming, and the one minion.
		scn.FreepsPlayCard(whatare);

		assertInZone(Zone.DISCARD, tinker);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scaffold));
		assertEquals(0, scn.GetStackedCards(scaffold).size());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnIsengardOrcDiscardedWhenTheTurnEndsIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scaffold = scn.GetShadowCard("scaffold");
		var artisan = scn.GetShadowCard("artisan");
		scn.MoveCardsToSupportArea(scaffold);

		scn.StartGame();
		scn.MoveMinionsToTable(artisan);
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.SHADOW_CHARACTERS, artisan);
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseToStay();

		// The rule discards the minions as the turn ends; no regroup action did.
		assertInZone(Zone.DISCARD, artisan);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetStackedCards(scaffold).size());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void AnOrcStackedHereThatDiscardsItselfByItsOwnRegroupAbilityIsNotOfferedBack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scaffold = scn.GetShadowCard("scaffold");
		var builder = scn.GetShadowCard("builder");
		scn.MoveCardsToSupportArea(scaffold);
		scn.StackCardsOn(scaffold, builder);

		scn.StartGame();
		assertInZone(Zone.STACKED, builder);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPass();
		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(builder));
		scn.ShadowUseCardAction(builder);

		// It was discarded from a stack, not from play.
		assertInZone(Zone.DISCARD, builder);
		assertEquals(twilight + 2, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scaffold));
		assertEquals(0, scn.GetStackedCards(scaffold).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// CR 4.2 "killed": "Cards that trigger when a character is killed will not trigger when a character is
	// discarded, and vice-versa."
	@Test
	public void AnIsengardOrcKilledByARegroupAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calaglin = scn.GetFreepsCard("calaglin");

		var scaffold = scn.GetShadowCard("scaffold");
		var tinker = scn.GetShadowCard("tinker");
		scn.MoveCardsToSupportArea(scaffold);

		scn.StartGame();
		scn.MoveMinionsToTable(tinker);
		scn.SkipToPhase(Phase.REGROUP);
		// Calaglin is an archer, so he comes down once the archery phase is past.
		scn.MoveAlliesToTable(calaglin);
		scn.AddWoundsToChar(tinker, 1);
		scn.FreepsRefreshDecision();

		// The Tinker has 1 vitality left: Calaglin's wound kills it.
		assertTrue(scn.FreepsActionAvailable(calaglin));
		scn.FreepsUseCardAction(calaglin);

		assertEquals(1, scn.GetWoundsOn(calaglin));
		assertInZone(Zone.DISCARD, tinker);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scaffold));
		assertEquals(0, scn.GetStackedCards(scaffold).size());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void DiscardsTwoCardsStackedHereToPreventAWoundToAnIsengardOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend1 = scn.GetFreepsCard("defend1");
		var defend2 = scn.GetFreepsCard("defend2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend1, defend2);

		var scaffold = scn.GetShadowCard("scaffold");
		var scaffold2 = scn.GetShadowCard("scaffold2");
		var tender = scn.GetShadowCard("tender");
		var warg = scn.GetShadowCard("warg");
		var builder = scn.GetShadowCard("builder");
		var journeyman = scn.GetShadowCard("journeyman");
		var artisan = scn.GetShadowCard("artisan");
		var worker = scn.GetShadowCard("worker");
		var tinker = scn.GetShadowCard("tinker");
		scn.MoveCardsToSupportArea(scaffold, scaffold2);
		scn.StackCardsOn(scaffold, tender, warg, builder);
		scn.StackCardsOn(scaffold2, journeyman, artisan);

		scn.StartGame();
		assertInZone(Zone.STACKED, tender, warg, builder, journeyman, artisan);
		assertEquals(3, scn.GetStackedCards(scaffold).size());
		assertEquals(2, scn.GetStackedCards(scaffold2).size());
		scn.MoveMinionsToTable(worker, tinker);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(worker));
		scn.FreepsPlayCard(defend1);
		scn.FreepsChooseCard(worker);

		// The Worker is about to take the wound: each copy, with 2 or more cards stacked on it, may answer.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scaffold));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scaffold2));
		scn.ShadowChooseOptionalTrigger(scaffold);

		// Exactly 2 of the 3 cards stacked on this copy, and none of those on the other.
		assertTrue(scn.ShadowHasCardChoiceAvailable(tender, warg, builder));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(journeyman, artisan));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(tender, warg);

		assertInZone(Zone.DISCARD, tender, warg);
		assertEquals(1, scn.GetStackedCards(scaffold).size());
		assertTrue(scn.GetStackedCards(scaffold).contains(builder));
		assertEquals(2, scn.GetStackedCards(scaffold2).size());
		assertEquals(0, scn.GetWoundsOn(worker));
		assertInZone(Zone.SHADOW_CHARACTERS, worker);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		// An [isengard] Orc of another title. One card is left on the first copy: only the second can answer, and
		// its two cards go with no choice.
		assertEquals(0, scn.GetWoundsOn(tinker));
		scn.FreepsPlayCard(defend2);
		scn.FreepsChooseCard(tinker);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scaffold));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scaffold2));
		scn.ShadowChooseOptionalTrigger(scaffold2);

		assertInZone(Zone.DISCARD, journeyman, artisan);
		assertEquals(0, scn.GetStackedCards(scaffold2).size());
		assertEquals(1, scn.GetStackedCards(scaffold).size());
		assertEquals(0, scn.GetWoundsOn(tinker));
		assertInZone(Zone.SHADOW_CHARACTERS, tinker);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhenAnIsengardUrukHaiOrAnOrcOfAnotherCultureIsAboutToTakeAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend1 = scn.GetFreepsCard("defend1");
		var defend2 = scn.GetFreepsCard("defend2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend1, defend2);

		var scaffold = scn.GetShadowCard("scaffold");
		var tender = scn.GetShadowCard("tender");
		var warg = scn.GetShadowCard("warg");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(scaffold);
		scn.StackCardsOn(scaffold, tender, warg);

		scn.StartGame();
		assertInZone(Zone.STACKED, tender, warg);
		assertEquals(2, scn.GetStackedCards(scaffold).size());
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// The Savage: [isengard], and an Uruk-hai. Defend It and Hope wounds it twice, and neither wound is answered.
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.FreepsPlayCard(defend1);
		scn.FreepsChooseCard(savage);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scaffold));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		// The Goblin Runner: an Orc, and [moria]. Of vitality 1, it dies of the wound.
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		scn.FreepsPlayCard(defend2);
		scn.FreepsChooseCard(runner);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scaffold));
		assertInZone(Zone.DISCARD, runner);

		assertEquals(2, scn.GetStackedCards(scaffold).size());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void OnlyTheWoundItAnswersIsPreventedAndTheSecondWoundOfALostSkirmishIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		var scaffold = scn.GetShadowCard("scaffold");
		var tender = scn.GetShadowCard("tender");
		var warg = scn.GetShadowCard("warg");
		var artisan = scn.GetShadowCard("artisan");
		scn.MoveCardsToSupportArea(scaffold);
		scn.StackCardsOn(scaffold, tender, warg);

		scn.StartGame();
		assertInZone(Zone.STACKED, tender, warg);
		assertEquals(2, scn.GetStackedCards(scaffold).size());
		assertAttachedTo(sword, aragorn);
		scn.MoveMinionsToTable(artisan);
		scn.SkipToAssignments();
		// Aragorn, 8 + 2, beats the Artisan, 8, without doubling it: two wounds for the sword's damage +1.
		scn.FreepsAssignAndResolve(aragorn, artisan);
		scn.PassSkirmishActions();

		assertEquals(0, scn.GetWoundsOn(artisan));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scaffold));
		scn.ShadowChooseOptionalTrigger(scaffold);
		assertInZone(Zone.DISCARD, tender, warg);
		assertEquals(0, scn.GetStackedCards(scaffold).size());

		// The first wound is prevented; the second finds nothing stacked here, and is taken.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scaffold));
		assertEquals(1, scn.GetWoundsOn(artisan));
		assertInZone(Zone.SHADOW_CHARACTERS, artisan);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenSeveralMinionsAreWoundedTogetherInALostSkirmishItSavesOnlyTheIsengardOrcChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var scaffold = scn.GetShadowCard("scaffold");
		var tender = scn.GetShadowCard("tender");
		var warg = scn.GetShadowCard("warg");
		var worker = scn.GetShadowCard("worker");
		var tinker = scn.GetShadowCard("tinker");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(scaffold);
		scn.StackCardsOn(scaffold, tender, warg);

		scn.StartGame();
		assertInZone(Zone.STACKED, tender, warg);
		assertEquals(2, scn.GetStackedCards(scaffold).size());
		scn.MoveMinionsToTable(worker, tinker, savage, runner);
		// Aragorn, 8 + 20, beats the four together, 5 + 5 + 5 + 5 = 20, and is far from doubling them: nobody is
		// overwhelmed, and the four lose together and are wounded together.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 20));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, worker);
		scn.ShadowAssignToMinions(aragorn, tinker, savage, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(28, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertEquals(0, scn.GetWoundsOn(tinker));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		scn.PassSkirmishActions();

		// Two [isengard] Orcs are about to take a wound, beside an [isengard] Uruk-hai and a [moria] Orc.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scaffold));
		scn.ShadowChooseOptionalTrigger(scaffold);
		assertInZone(Zone.DISCARD, tender, warg);

		// The wound to be prevented is an [isengard] Orc's: either of the two, and not the others.
		assertTrue(scn.ShadowHasCardChoiceAvailable(worker, tinker));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(worker);

		// That wound alone was prevented. The others take theirs; the Runner, of vitality 1, dies of it.
		assertEquals(0, scn.GetWoundsOn(worker));
		assertEquals(1, scn.GetWoundsOn(tinker));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
