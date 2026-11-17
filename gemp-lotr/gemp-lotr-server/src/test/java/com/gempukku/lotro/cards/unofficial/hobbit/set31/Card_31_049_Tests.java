package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_049_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  The Shadow phase
	// and the skirmishes of turn 1 are at The Troll Hoard 30_50 (site 2), whose only text is a start-of-turn offer.
	// Every Spider is of site 5 or 8, so each roams there: +2 to play.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cob", "31_49");

					put("runner", "31_30");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; a "when you play" offer, never reached (cheated in)
					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, vitality 2; "Regroup: Discard an Orc to make the Free Peoples player wound a companion"
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc; "Assignment: Assign this minion to skirmish Bilbo. The Free Peoples player may add a doubt to discard this minion."
					put("footman", "30_42");    // Goblin Footman: [moria] Orc, cost 3: an Orc in hand that is no Spider
					put("bats", "32_30");       // Dark Bats: [moria] Creature, no Orc, strength 4, vitality 2, site 4; a Shadow ability not used
					put("bats2", "32_30");
					put("wicked", "31_55");     // Wicked Spider: "Skirmish: Discard an Orc to make a Spider strength +3 ... until the regroup phase"
					put("tomnoddy", "31_53");   // Old Tomnoddy: [spider] Spider, cost 4, strength 8, fierce; "When you play this minion, you may remove (1) to play a Spider or an Orc from your discard pile"
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: a [gollum] Spider, cost 6, site 8; enduring, fierce, damage +1 in a fierce skirmish
					put("nest", "31_54");       // Spider Nest: a [spider] condition, no Spider, cost 2; "To play, spot a Spider"

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, strength 7, vitality 3; a Fellowship ability not used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a ranger, strength 8, vitality 4; a Maneuver ability not used
					put("whatare", "1_119");    // What Are They?: Maneuver event, "Spot a ranger to discard a roaming minion"
					put("pursuit", "1_111");    // Pursuit Just Behind: Regroup event, "Exert a ranger companion to wound every minion"
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void CrazyCobStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Crazy Cob
		 * Unique: True
		 * Side: Shadow
		 * Culture: Spider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Spider
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>.
		 * Each time an Orc is killed or discarded from play (except during the regroup phase), you may play a Spider. Its twilight cost is -2 and strength +2 until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cob");

		assertEquals("Crazy Cob", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SPIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.SPIDER, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AnOrcKilledInASkirmishOffersASpiderFromHandAtTwoLessAndStrongerUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cob = scn.GetShadowCard("cob");
		var runner = scn.GetShadowCard("runner");
		var tomnoddy = scn.GetShadowCard("tomnoddy");
		var shelob = scn.GetShadowCard("shelob");
		var footman = scn.GetShadowCard("footman");
		var nest = scn.GetShadowCard("nest");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToHand(tomnoddy, shelob, footman, nest);

		scn.StartGame();
		assertInHand(tomnoddy, shelob, footman, nest);
		scn.MoveMinionsToTable(cob, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(6);

		// 7 against 5: the Runner (vitality 1) is killed.
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(cob));
		scn.ShadowAcceptOptionalTrigger();

		// Spiders in hand: Shelob's 6 + 2 roaming - 2 is just within the 6 in the pool.  Not the Orc or the [spider]
		// condition in hand, both affordable without the discount, nor the Runner just killed.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(tomnoddy, shelob));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(footman, nest, runner));
		scn.ShadowChooseCard(tomnoddy);

		assertInZone(Zone.SHADOW_CHARACTERS, tomnoddy);
		// 6 - (4 to play + 2 roaming - 2).
		assertEquals(2, scn.GetTwilight());
		assertInHand(shelob, footman, nest);
		// The discount went with that play alone: Shelob in hand costs her printed 6.
		assertEquals(6, scn.GetTwilightCost(shelob));
		// 8 + 2; Crazy Cob, a Spider already in play, is not made stronger.
		assertEquals(10, scn.GetStrength(tomnoddy));
		assertEquals(9, scn.GetStrength(cob));

		// Old Tomnoddy is fierce: Balin takes him in the fierce round, and he is still 10 there.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(balin, tomnoddy);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		assertEquals(10, scn.GetStrength(tomnoddy));
		assertEquals(0, scn.GetWoundsOn(balin));

		// 10 against 7: Balin loses.  In the regroup phase Old Tomnoddy is back to 8.
		scn.PassFierceSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(balin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(8, scn.GetStrength(tomnoddy));
	}

	@Test
	public void AnOrcDiscardedDuringASkirmishOffersAPlayAndTheKillThatFollowsOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cob = scn.GetShadowCard("cob");
		var runner = scn.GetShadowCard("runner");
		var veteran = scn.GetShadowCard("veteran");
		var wicked = scn.GetShadowCard("wicked");
		var tomnoddy = scn.GetShadowCard("tomnoddy");
		var shelob = scn.GetShadowCard("shelob");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToHand(tomnoddy, shelob);

		scn.StartGame();
		assertInHand(tomnoddy, shelob);
		scn.MoveMinionsToTable(cob, runner, veteran, wicked);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(10);

		// Wicked Spider's cost discards the [sauron] Veteran from play: no kill.
		assertEquals(0, scn.GetWoundsOn(veteran));
		scn.FreepsPass();
		scn.ShadowUseCardAction(wicked);
		scn.ShadowChooseCard(veteran);
		assertInDiscard(veteran);
		assertEquals(0, scn.GetWoundsOn(veteran));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(cob));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(tomnoddy);

		assertInZone(Zone.SHADOW_CHARACTERS, tomnoddy);
		// 10 - (4 to play + 2 roaming - 2).
		assertEquals(6, scn.GetTwilight());
		// Old Tomnoddy's own offer as he is played (the Veteran is in the discard pile) comes first, declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tomnoddy));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(10, scn.GetStrength(tomnoddy));
		// Wicked Spider's +3, on itself.
		scn.ShadowChooseCard(wicked);
		assertEquals(10, scn.GetStrength(wicked));
		int pool = scn.GetTwilight();

		// 7 against 5: the Runner is killed, and Crazy Cob offers it again.  Declined, nothing is played.
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(cob));
		scn.ShadowDeclineOptionalTrigger();
		assertInHand(shelob);
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnOrcTheFreePeoplesPlayerDiscardsInTheAssignmentPhaseOffersAPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cob = scn.GetShadowCard("cob");
		var aggressor = scn.GetShadowCard("aggressor");
		var tomnoddy = scn.GetShadowCard("tomnoddy");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToHand(tomnoddy);

		scn.StartGame();
		assertInHand(tomnoddy);
		scn.MoveMinionsToTable(cob, aggressor);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.ShadowUseCardAction(aggressor);

		// The [gundabad] Aggressor is assigned to Bilbo, and the Free Peoples player adds a doubt to discard it.
		assertEquals(0, scn.GetBurdens());
		scn.FreepsChooseYes();
		assertEquals(1, scn.GetBurdens());
		assertInDiscard(aggressor);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(cob));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, tomnoddy);
		// 10 - (4 to play + 2 roaming - 2).
		assertEquals(6, scn.GetTwilight());
		// Old Tomnoddy's own offer as he is played (the Aggressor is in the discard pile) comes first, declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tomnoddy));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(10, scn.GetStrength(tomnoddy));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void MinionsWhoAreNoOrcsDiscardedOrKilledOfferNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cob = scn.GetShadowCard("cob");
		var bats = scn.GetShadowCard("bats");
		var bats2 = scn.GetShadowCard("bats2");
		var tomnoddy = scn.GetShadowCard("tomnoddy");
		var balin = scn.GetFreepsCard("balin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var whatare = scn.GetFreepsCard("whatare");
		scn.MoveCompanionsToTable(balin, aragorn);
		scn.MoveCardsToHand(whatare);
		scn.MoveCardsToHand(tomnoddy);

		scn.StartGame();
		assertInHand(whatare, tomnoddy);
		scn.MoveMinionsToTable(cob, bats, bats2);
		// Vitality 2 with 1 wound: the second Bats dies of the wound it loses.
		scn.AddWoundsToChar(bats2, 1);
		assertEquals(1, scn.GetWoundsOn(bats2));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(10);

		// What Are They? discards Dark Bats (site 4, roaming at site 2) from play.
		scn.FreepsPlayCard(whatare);
		scn.FreepsChooseCard(bats);
		assertInDiscard(bats);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(cob));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, bats2);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);

		// 7 against 4: the other Bats is killed.
		scn.PassSkirmishActions();
		assertInDiscard(bats2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(cob));
		assertInHand(tomnoddy);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OrcsKilledOrDiscardedInTheRegroupPhaseOfferNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cob = scn.GetShadowCard("cob");
		var runner = scn.GetShadowCard("runner");
		var veteran = scn.GetShadowCard("veteran");
		var aggressor = scn.GetShadowCard("aggressor");
		var tomnoddy = scn.GetShadowCard("tomnoddy");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pursuit = scn.GetFreepsCard("pursuit");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(pursuit);
		scn.MoveCardsToHand(tomnoddy);

		scn.StartGame();
		assertInHand(pursuit, tomnoddy);

		// The minions that lived through the skirmishes stand at the start of the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(cob, runner, veteran, aggressor);
		scn.SetTwilight(10);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(runner));

		// Pursuit Just Behind kills the Runner (vitality 1) in the regroup phase.
		scn.FreepsPlayCard(pursuit);
		assertInDiscard(runner);
		assertEquals(1, scn.GetWoundsOn(veteran));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(cob));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// The Veteran's cost discards the Aggressor from play in the regroup phase.
		scn.ShadowUseCardAction(veteran);
		scn.ShadowChooseCard(aggressor);
		assertInDiscard(aggressor);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(cob));
		assertInHand(tomnoddy);
		assertEquals(10, scn.GetTwilight());
	}

	@Test
	public void WithASpiderInHandThePoolCannotPayForAKillOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cob = scn.GetShadowCard("cob");
		var runner = scn.GetShadowCard("runner");
		var tomnoddy = scn.GetShadowCard("tomnoddy");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToHand(tomnoddy);

		scn.StartGame();
		assertInHand(tomnoddy);
		scn.MoveMinionsToTable(cob, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		// Old Tomnoddy's 4 + 2 roaming - 2 is more than the 3 in the pool.
		scn.SetTwilight(3);

		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(cob));
		assertInHand(tomnoddy);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
