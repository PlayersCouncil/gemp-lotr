package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_018_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("menu", "102_18");

					// The [sauron] trackers.  Orc Ambusher: strength 5, vitality 2; a response to regroup
					// events.  Orc Assassin: strength 6, vitality 2; an assignment ability that needs 2 Hobbit
					// companions.  Neither is fierce.
					put("ambusher", "1_261");
					put("assassin", "1_262");
					// Orc Swordsman: a [sauron] Orc who is no tracker; strength 9, vitality 3; stronger only
					// with a weapon.
					put("swordsman", "3_98");

					// The [isengard] trackers.  Uruk Scout: strength 7, vitality 2, Damage +1, not fierce; a
					// response to events that spot or exert a ranger.  Uruk Runner: strength 8, vitality 2,
					// Fierce; strength +2 during a fierce skirmish it is in.
					put("scout", "2_47");
					put("runner", "4_193");
					// Uruk Savage: an [isengard] Uruk-hai who is no tracker; strength 5, vitality 3, Damage +1.
					put("savage", "1_151");

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability
					put("guard", "5_83");        // Household Guard: strength 6, vitality 3; "Valiant" is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BackontheMenuStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Back on the Menu
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Skirmish: Discard a [Sauron] tracker to make an [Isengard] tracker strength +2.
		 * Skirmish: Exert an [isengard] tracker to make a [sauron] tracker <b>fierce</b> until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("menu");

		assertEquals("Back on the Menu", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedFromHandForNothingToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var menu = scn.GetShadowCard("menu");
		scn.MoveCardsToHand(menu);

		scn.StartGame();
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 4 set + 1 companion + 2 for the site.
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.HAND, menu);
		assertTrue(scn.ShadowPlayAvailable(menu));

		scn.ShadowPlayCard(menu);
		// A cost of 0, and a condition has no roaming penalty.
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, menu);
		assertNull(menu.getAttachedTo());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishActionDiscardsASauronTrackerToMakeAnIsengardTrackerStrengthPlus2ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);

		var menu = scn.GetShadowCard("menu");
		var ambusher = scn.GetShadowCard("ambusher");
		var assassin = scn.GetShadowCard("assassin");
		var swordsman = scn.GetShadowCard("swordsman");
		var scout = scn.GetShadowCard("scout");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(menu);

		scn.StartGame();
		scn.MoveMinionsToTable(ambusher, assassin, swordsman, scout, runner, savage);
		// Every [sauron] tracker in print is an Orc and every [isengard] tracker an Uruk-hai.  The Uruk Scout is
		// made an Orc as well and the Orc Assassin an Uruk-hai as well: each then has the other culture's
		// race and still not its culture.
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, scout, null, Race.ORC));
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, assassin, null, Race.URUK_HAI));

		// Not in the assignment phase, the last in which the Shadow player is asked before a skirmish.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(menu));
		scn.ShadowPass();

		// Aragorn fights the Uruk Scout and the Orc Ambusher; the Household Guard will fight the Uruk Runner
		// afterwards.  The Orc Assassin, the Orc Swordsman and the Uruk Savage fight nobody.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, scout },
				new PhysicalCardImpl[] { guard, runner }
		);
		scn.ShadowAssignToMinions(aragorn, ambusher);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishing(ambusher));
		assertTrue(scn.IsCharSkirmishing(scout));
		assertFalse(scn.IsCharSkirmishing(runner));
		assertEquals(7, scn.GetStrength(scout));
		assertEquals(8, scn.GetStrength(runner));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(assassin));
		assertEquals(9, scn.GetStrength(swordsman));
		assertTrue(scn.ShadowActionAvailable(menu, "Discard a"));

		scn.ShadowUseCardAction(menu, "Discard a");
		// A [sauron] tracker, in this skirmish or in none: not a [sauron] Orc who is no tracker, and not an
		// [isengard] tracker.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ambusher));
		assertTrue(scn.ShadowHasCardChoiceAvailable(assassin));
		assertFalse(scn.ShadowHasCardChoiceAvailable(swordsman));
		assertFalse(scn.ShadowHasCardChoiceAvailable(scout));
		assertFalse(scn.ShadowHasCardChoiceAvailable(runner));
		assertFalse(scn.ShadowHasCardChoiceAvailable(savage));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(ambusher);
		assertInZone(Zone.DISCARD, ambusher);
		assertInZone(Zone.SHADOW_CHARACTERS, assassin);

		// An [isengard] tracker, in this skirmish or waiting for another: not an [isengard] Uruk-hai who is
		// no tracker, and not a [sauron] tracker.
		assertTrue(scn.ShadowHasCardChoiceAvailable(scout));
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner));
		assertFalse(scn.ShadowHasCardChoiceAvailable(savage));
		assertFalse(scn.ShadowHasCardChoiceAvailable(assassin));
		assertFalse(scn.ShadowHasCardChoiceAvailable(swordsman));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(runner);
		assertEquals(10, scn.GetStrength(runner));
		assertEquals(7, scn.GetStrength(scout));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(assassin));
		assertEquals(9, scn.GetStrength(swordsman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn's 8 beats the Scout's 7, and that skirmish is over.  The Runner's own is still to come,
		// and its +2 has gone with the skirmish it was given in.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(scout));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertEquals(8, scn.GetStrength(runner));
	}

	@Test
	public void SkirmishActionExertsAnIsengardTrackerToMakeASauronTrackerFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var menu = scn.GetShadowCard("menu");
		var ambusher = scn.GetShadowCard("ambusher");
		var assassin = scn.GetShadowCard("assassin");
		var swordsman = scn.GetShadowCard("swordsman");
		var scout = scn.GetShadowCard("scout");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(menu);

		scn.StartGame();
		scn.MoveMinionsToTable(ambusher, assassin, swordsman, scout, runner, savage);
		// Every [sauron] tracker in print is an Orc and every [isengard] tracker an Uruk-hai.  The Uruk Scout is
		// made an Orc as well and the Orc Assassin an Uruk-hai as well: each then has the other culture's
		// race and still not its culture.
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, scout, null, Race.ORC));
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, assassin, null, Race.URUK_HAI));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(menu));
		scn.ShadowPass();

		// Aragorn fights the Orc Ambusher, and nobody else fights.
		scn.FreepsAssignToMinions(aragorn, ambusher);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertFalse(scn.HasKeyword(ambusher, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(assassin, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(swordsman, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(scout, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable(menu, "Exert an"));

		scn.ShadowUseCardAction(menu, "Exert an");
		// An [isengard] tracker: not an [isengard] Uruk-hai who is no tracker, and not a [sauron] tracker.
		assertTrue(scn.ShadowHasCardChoiceAvailable(scout));
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner));
		assertFalse(scn.ShadowHasCardChoiceAvailable(savage));
		assertFalse(scn.ShadowHasCardChoiceAvailable(ambusher));
		assertFalse(scn.ShadowHasCardChoiceAvailable(assassin));
		assertFalse(scn.ShadowHasCardChoiceAvailable(swordsman));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(scout);
		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(runner));

		// A [sauron] tracker, in this skirmish or in none: not a [sauron] Orc who is no tracker, and not an
		// [isengard] tracker.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ambusher));
		assertTrue(scn.ShadowHasCardChoiceAvailable(assassin));
		assertFalse(scn.ShadowHasCardChoiceAvailable(swordsman));
		assertFalse(scn.ShadowHasCardChoiceAvailable(scout));
		assertFalse(scn.ShadowHasCardChoiceAvailable(runner));
		assertFalse(scn.ShadowHasCardChoiceAvailable(savage));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(assassin);
		assertTrue(scn.HasKeyword(assassin, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(ambusher, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(swordsman, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(scout, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn's 8 beats the Ambusher's 5; it has vitality 2 and lives.  The normal skirmishes are over.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(ambusher));
		assertInZone(Zone.SHADOW_CHARACTERS, ambusher);

		// The fierce assignment: the Assassin is offered beside the Uruk Runner, who is fierce in print, and
		// the minions that are not fierce are not.
		scn.PassFierceAssignmentActions();
		assertTrue(scn.HasKeyword(assassin, Keyword.FIERCE));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(assassin));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(runner));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(ambusher));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(swordsman));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(scout));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(savage));

		scn.FreepsAssignToMinions(aragorn, assassin);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishing(assassin));
		assertTrue(scn.HasKeyword(assassin, Keyword.FIERCE));

		// Aragorn's 8 beats the Assassin's 6; it has vitality 2 and lives to be read in the regroup phase.
		scn.PassFierceSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, assassin);
		assertFalse(scn.HasKeyword(assassin, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(runner, Keyword.FIERCE));
	}

	@Test
	public void WithNoSauronTrackerTheFirstActionIsNotOfferedAndTheSecondExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var menu = scn.GetShadowCard("menu");
		var swordsman = scn.GetShadowCard("swordsman");
		var scout = scn.GetShadowCard("scout");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(menu);

		scn.StartGame();
		// A [sauron] Orc who is no tracker, and trackers that are not [sauron].
		scn.MoveMinionsToTable(swordsman, scout, runner, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(menu, "Discard a"));
		// What follows "to" is no cost: the second action is there with nobody to make fierce.
		assertTrue(scn.ShadowActionAvailable(menu, "Exert an"));

		scn.ShadowUseCardAction(menu, "Exert an");
		assertTrue(scn.ShadowHasCardChoiceAvailable(scout));
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner));
		scn.ShadowChooseCard(scout);

		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertFalse(scn.HasKeyword(swordsman, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(scout, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertInZone(Zone.SUPPORT, menu);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoIsengardTrackerTheSecondActionIsNotOfferedAndTheFirstDiscardsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var menu = scn.GetShadowCard("menu");
		var ambusher = scn.GetShadowCard("ambusher");
		var assassin = scn.GetShadowCard("assassin");
		var swordsman = scn.GetShadowCard("swordsman");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(menu);

		scn.StartGame();
		// An [isengard] Uruk-hai who is no tracker, and trackers that are not [isengard]: all could exert.
		scn.MoveMinionsToTable(ambusher, assassin, swordsman, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(menu, "Exert an"));
		// What follows "to" is no cost: the first action is there with nobody to strengthen.
		assertTrue(scn.ShadowActionAvailable(menu, "Discard a"));

		scn.ShadowUseCardAction(menu, "Discard a");
		assertTrue(scn.ShadowHasCardChoiceAvailable(ambusher));
		assertTrue(scn.ShadowHasCardChoiceAvailable(assassin));
		scn.ShadowChooseCard(ambusher);

		assertInZone(Zone.DISCARD, ambusher);
		assertEquals(6, scn.GetStrength(assassin));
		assertEquals(9, scn.GetStrength(swordsman));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInZone(Zone.SUPPORT, menu);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SecondActionIsNotOfferedWhileTheOnlyIsengardTrackerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var menu = scn.GetShadowCard("menu");
		var ambusher = scn.GetShadowCard("ambusher");
		var assassin = scn.GetShadowCard("assassin");
		var scout = scn.GetShadowCard("scout");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(menu);

		scn.StartGame();
		scn.MoveMinionsToTable(ambusher, assassin, scout, savage);
		// Vitality 2 with 1 wound.  The Uruk Savage and the two [sauron] trackers could all exert.
		scn.AddWoundsToChar(scout, 1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.IsExhausted(scout));
		assertEquals(7, scn.GetStrength(scout));
		assertFalse(scn.ShadowActionAvailable(menu, "Exert an"));
		assertTrue(scn.ShadowActionAvailable(menu, "Discard a"));

		// An exhausted tracker can still be made stronger.  The Scout is the only [isengard] tracker, so
		// nothing is asked about who gets the +2.
		scn.ShadowUseCardAction(menu, "Discard a");
		scn.ShadowChooseCard(assassin);
		assertInZone(Zone.DISCARD, assassin);
		assertEquals(9, scn.GetStrength(scout));
		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(ambusher));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
