package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_262_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("battlements", "7_262");
					put("battlements2", "7_262");
					put("pillager", "7_275");   // Gorgoroth Pillager: besieger Orc, cost 4, site 5; its text works only while it is stacked
					put("soldier", "7_278");    // Gorgoroth Soldier: besieger Orc, cost 3, site 5; a response that needs 2 cards in hand
					put("garrison", "7_273");   // Gorgoroth Garrison: besieger Orc, cost 3, site 5; a regroup ability
					put("troll", "8_102");      // Great Hill Troll: a besieger who is no Orc, cost 9, site 5; fierce; its own trigger is for sites 5K to 9K
					put("assassin", "1_262");   // Orc Assassin: a [sauron] Orc who is no besieger; an assignment ability that needs 2 Hobbit companions
					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc who is no besieger, cost 2, site 6; his text answers a weapon played on him
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] minion who is no Orc (Uruk-hai)
					put("hound", "7_192");      // Morgul Hound: an Orc who is not [sauron] ([wraith])
					put("teeth", "5_110");      // Teeth of Mordor: [sauron] support-area condition that play stacks a [sauron] Orc on
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AbovetheBattlementStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Above the Battlement
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Play a besieger stacked on a site you control or
		 * remove a burden to play a [sauron] Orc from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("battlements");

		assertEquals("Above the Battlement", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithNoBurdenToRemoveItPlaysABesiegerStackedOnASiteYouControl() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battlements = scn.GetShadowCard("battlements");
		var pillager = scn.GetShadowCard("pillager");
		var troll = scn.GetShadowCard("troll");
		var assassin = scn.GetShadowCard("assassin");
		var soldier = scn.GetShadowCard("soldier");
		var garrison = scn.GetShadowCard("garrison");
		var chieftain = scn.GetShadowCard("chieftain");
		var teeth = scn.GetShadowCard("teeth");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		// On the site: two besiegers, and a [sauron] Orc who is none.  A besieger stacked on a condition, and a
		// besieger and a [sauron] Orc in the discard pile, which no burden is there to pay for.
		scn.StackCardsOn(site1, pillager, troll, assassin);
		scn.MoveCardsToSupportArea(teeth);
		scn.StackCardsOn(teeth, soldier);
		scn.MoveCardsToDiscard(garrison, chieftain);
		scn.SetShadowHand(battlements);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// 20 set + 1 companion + 0 for site 3
		assertEquals(21, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowPlayAvailable(battlements));

		scn.ShadowPlayCard(battlements);

		// Only the first half can be done, so no choice between the two is asked.
		assertTrue(scn.ShadowHasCardChoiceAvailable(pillager, troll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(assassin, soldier, garrison, chieftain));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troll);

		// 9 + 2 for roaming: site number 5 at site 3
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertInZone(Zone.STACKED, pillager, assassin, soldier);
		assertInDiscard(garrison, chieftain, battlements);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoBesiegerOnTheSiteItRemovesABurdenToPlayASauronOrcFromYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battlements = scn.GetShadowCard("battlements");
		var assassin = scn.GetShadowCard("assassin");
		var garrison = scn.GetShadowCard("garrison");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		var theirSoldier = scn.GetFreepsCard("soldier");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		// A [sauron] Orc who is no besieger on the site.  In the discard pile: two [sauron] Orcs, one a besieger
		// and one not, a [sauron] Uruk-hai and a [wraith] Orc; and a [sauron] Orc in the other player's.
		scn.StackCardsOn(site1, assassin);
		scn.MoveCardsToDiscard(garrison, chieftain, guard, hound, theirSoldier);
		scn.SetShadowHand(battlements);
		scn.AddBurdens(2);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// 20 set + 1 companion + 0 for site 3
		assertEquals(21, scn.GetTwilight());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.ShadowPlayAvailable(battlements));

		scn.ShadowPlayCard(battlements);

		// Only the second half can be done, so no choice between the two is asked: one burden goes.
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowHasCardChoiceAvailable(garrison, chieftain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, hound, theirSoldier, assassin));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(garrison);

		// 3 + 2 for roaming: site number 5 at site 3
		assertEquals(16, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, garrison);
		assertInDiscard(chieftain, guard, hound, theirSoldier, battlements);
		assertInZone(Zone.STACKED, assassin);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithBothOpenThePlayerChoosesOneAndOnlyTheSecondTakesABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battlements = scn.GetShadowCard("battlements");
		var second = scn.GetShadowCard("battlements2");
		var pillager = scn.GetShadowCard("pillager");
		var soldier = scn.GetShadowCard("soldier");
		var garrison = scn.GetShadowCard("garrison");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.StackCardsOn(site1, pillager, soldier);
		scn.MoveCardsToDiscard(garrison, chieftain);
		scn.SetShadowHand(battlements, second);
		scn.AddBurdens(2);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// 20 set + 1 companion + 0 for site 3
		assertEquals(21, scn.GetTwilight());

		scn.ShadowPlayCard(battlements);

		assertTrue(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("stacked");

		assertTrue(scn.ShadowHasCardChoiceAvailable(pillager, soldier));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(garrison, chieftain));
		scn.ShadowChooseCard(pillager);

		// 4 + 2 for roaming; the burdens and the discard pile are as they were.
		assertEquals(15, scn.GetTwilight());
		assertEquals(2, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, pillager);
		assertInZone(Zone.STACKED, soldier);
		assertInDiscard(garrison, chieftain, battlements);

		scn.ShadowPlayCard(second);

		assertTrue(scn.ShadowDecisionAvailable("Choose action to perform"));
		scn.ShadowChooseOption("burden");

		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowHasCardChoiceAvailable(garrison, chieftain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(soldier));
		scn.ShadowChooseCardBPFromSelection(chieftain);

		// 2 + 2 for roaming; the besieger on the site stays there.
		assertEquals(11, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertInZone(Zone.STACKED, soldier);
		assertInDiscard(garrison, second);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithABurdenAndNoSauronOrcInTheDiscardPileItPlaysTheBesiegerWithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battlements = scn.GetShadowCard("battlements");
		var pillager = scn.GetShadowCard("pillager");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		// One besieger on the site.  A burden to remove, but nothing in the discard pile it could pay for: a
		// [sauron] Uruk-hai and a [wraith] Orc.
		scn.StackCardsOn(site1, pillager);
		scn.MoveCardsToDiscard(guard, hound);
		scn.SetShadowHand(battlements);
		scn.AddBurdens(1);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// 20 set + 1 companion + 0 for site 3
		assertEquals(21, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(battlements));

		scn.ShadowPlayCard(battlements);

		// The second half cannot be done in full, so it is not one to choose: the besieger is played.
		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertInZone(Zone.SHADOW_CHARACTERS, pillager);
		assertEquals(1, scn.GetBurdens());
		// 4 + 2 for roaming
		assertEquals(15, scn.GetTwilight());
		assertInDiscard(guard, hound, battlements);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsPlayedInTheShadowPhaseAndNotInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battlements = scn.GetShadowCard("battlements");
		var pillager = scn.GetShadowCard("pillager");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.StackCardsOn(site1, pillager);
		scn.SetShadowHand(battlements);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(battlements));

		// A minion on the table, so that there is a maneuver phase.
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(21, scn.GetTwilight());
		assertInZone(Zone.STACKED, pillager);
		assertFalse(scn.ShadowPlayAvailable(battlements));
	}

	@Test
	public void CannotBePlayedWithNoBesiegerOnTheSiteAndNoBurdenToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battlements = scn.GetShadowCard("battlements");
		var assassin = scn.GetShadowCard("assassin");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		// A [sauron] Orc who is no besieger on the site, and a [sauron] Orc in the discard pile.
		scn.StackCardsOn(site1, assassin);
		scn.MoveCardsToDiscard(chieftain);
		scn.SetShadowHand(battlements);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(battlements));

		scn.AddBurdens(1);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(battlements));
	}

	@Test
	public void CannotBePlayedWithABurdenAndNoSauronOrcInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battlements = scn.GetShadowCard("battlements");
		var assassin = scn.GetShadowCard("assassin");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		// A [sauron] Orc on the site who is no besieger; a [sauron] Uruk-hai and a [wraith] Orc in the discard pile.
		scn.StackCardsOn(site1, assassin);
		scn.MoveCardsToDiscard(guard, hound);
		scn.SetShadowHand(battlements);
		scn.AddBurdens(1);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(battlements));

		scn.MoveCardsToDiscard(chieftain);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(battlements));
	}

	@Test
	public void CannotBePlayedWhileThePoolIsShortOfTheMinionItWouldPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battlements = scn.GetShadowCard("battlements");
		var pillager = scn.GetShadowCard("pillager");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.StackCardsOn(site1, pillager);
		scn.MoveCardsToDiscard(chieftain);
		scn.SetShadowHand(battlements);
		scn.FreepsPass();

		// The Pillager on the site costs 4 + 2 for roaming.
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(battlements));

		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(battlements));

		// With a burden to remove, the Orc Chieftain in the discard pile costs 2 + 2 for roaming.
		scn.AddBurdens(1);
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(battlements));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(battlements));

		scn.ShadowPlayCard(battlements);

		// Only the Chieftain can be paid for, so nothing is asked.
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertInZone(Zone.STACKED, pillager);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ABesiegerStackedOnASiteTheOtherPlayerControlsIsNotPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battlements = scn.GetShadowCard("battlements");
		var theirPillager = scn.GetFreepsCard("pillager");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(2);
		// The player whose fellowship is moving controls site 1, with a besieger of his own stacked on it.
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.StackCardsOn(site1, theirPillager);
		scn.MoveCardsToDiscard(chieftain);
		scn.SetShadowHand(battlements);
		scn.AddBurdens(1);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.FreepsControls(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.GetStackedCards(site1).contains(theirPillager));
		assertEquals(21, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(battlements));

		scn.ShadowPlayCard(battlements);

		// That besieger is nothing to choose, so the burden goes and the one Orc in the discard pile is played.
		assertEquals(0, scn.GetBurdens());
		// 2 + 2 for roaming
		assertEquals(17, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertInZone(Zone.STACKED, theirPillager);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
