package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_022_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  Turn 1's Shadow phase
	// and skirmishes are at The Troll Hoard 30_50 (site 2, Shadow number 3); Rivendell 30_51 (site 3, Shadow number 1)
	// is a sanctuary whose only text bars Trolls.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("riddles", "31_22");

					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, vitality 2; an Assignment ability not used
					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, strength 7, vitality 2; a Regroup ability not used
					put("runner", "31_30");     // Goblin Runner: [moria] Orc, vitality 1, so it cannot exert; its play trigger never fires
					put("bats", "32_30");       // Dark Bats: [moria] Creature, strength 4, vitality 2; a Shadow ability not used
					put("footman", "30_42");    // Goblin Footman: a card for a hand; never played

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, strength 7; Fellowship: exert Balin to shuffle an event
					// Skillful Negotiator: [shire] Maneuver event, "Spot an ally and exert Bilbo to ..."; two copies for two plays
					put("negotiator", "31_43");
					put("negotiator2", "31_43");
					// Master Worrywort, Hobbit Farmer: [shire] Hobbit ally; texts on Bilbo winning and in a skirmish, neither reached
					put("worrywort", "33_53");
					put("bard", "30_22");       // Bard, The Bowman: [esgaroth] Man ally; a Regroup ability not used
					put("acorn", "31_38");      // An Acorn from Beorn's house: [shire] possession on Bilbo; a Regroup ability not used
					put("thrush", "31_42");     // Old Thrush, Messenger Bird: [shire] follower; its Aid is declined
					put("dori", "30_8");        // Dori, Really a decent fellow: [dwarven] follower; its Aid is declined
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void RiddlesintheDarkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Riddles in the Dark
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time the fellowship moves, add (2).
		 * 	<b>Response:</b> If Bilbo exerts or takes a wound, exert a minion and discard a card from hand to discard a
		 * 	[shire] card (except Bilbo). The Free Peoples player may discard 2 cards from hand to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("riddles");

		assertEquals("Riddles in the Dark", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachMoveOfTheFellowshipAddsTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riddles = scn.GetShadowCard("riddles");
		scn.MoveCardsToSupportArea(riddles);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, riddles);
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 3 for The Troll Hoard + 1 for Bilbo + 2
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The regroup move, in the same turn.
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.SetTwilight(0);
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		// 1 for Rivendell + 1 for Bilbo + 2
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingAtTheRegroupDecisionAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riddles = scn.GetShadowCard("riddles");
		// A card in the Free Peoples hand, so that the turn waits at his reconcile, before the next turn empties the pool.
		var card1 = scn.GetFreepsCard("aggressor");
		scn.MoveCardsToSupportArea(riddles);

		scn.StartGame();
		scn.MoveCardsToHand(card1);
		assertInHand(card1);
		assertInZone(Zone.SUPPORT, riddles);
		scn.FreepsPass();
		scn.SkipToMovementDecision();
		int before = scn.GetTwilight();

		scn.FreepsChooseToStay();
		assertEquals(before, scn.GetTwilight());
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertInZone(Zone.SUPPORT, riddles);
		scn.PassReconcile(VirtualTableScenario.P1);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void BilboExertingLetsItExertAMinionAndDiscardACardToDiscardAShireCardOfAnyKind() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riddles = scn.GetShadowCard("riddles");
		var aggressor = scn.GetShadowCard("aggressor");
		var veteran = scn.GetShadowCard("veteran");
		var runner = scn.GetShadowCard("runner");
		var footman = scn.GetShadowCard("footman");
		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var negotiator = scn.GetFreepsCard("negotiator");
		var worrywort = scn.GetFreepsCard("worrywort");
		var acorn = scn.GetFreepsCard("acorn");
		var thrush = scn.GetFreepsCard("thrush");
		var dori = scn.GetFreepsCard("dori");
		var card1 = scn.GetFreepsCard("aggressor");
		var card2 = scn.GetFreepsCard("veteran");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(worrywort);
		scn.MoveCardsToSupportArea(riddles, thrush, dori);
		scn.AttachCardsTo(bilbo, acorn);

		scn.StartGame();
		scn.MoveCardsToHand(negotiator, card1, card2, footman);
		assertInHand(negotiator, card1, card2, footman);
		assertAttachedTo(acorn, bilbo);
		assertInZone(Zone.SUPPORT, riddles, worrywort, thrush, dori);
		scn.FreepsPass();
		scn.MoveMinionsToTable(aggressor, veteran, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		// Old Thrush's and Dori's Aids, declined.
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(veteran));

		scn.FreepsPlayCard(negotiator);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(riddles));
		scn.ShadowChooseOptionalTrigger(riddles);
		// A minion of either culture that can exert; not the Runner, who cannot.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, veteran));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		scn.ShadowChooseCard(veteran);
		assertEquals(1, scn.GetWoundsOn(veteran));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		// The one card in hand is discarded.
		assertInDiscard(footman);
		assertEquals(0, scn.GetShadowHandCount());

		// The Free Peoples player, with 2 cards in hand, declines to prevent it.
		assertTrue(scn.FreepsDecisionAvailable("discard 2 cards"));
		scn.FreepsChooseNo();
		// A [shire] ally, possession and follower; not a [dwarven] companion or follower, nor Bilbo.
		assertTrue(scn.ShadowHasCardChoiceAvailable(worrywort, acorn, thrush));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(balin, dori, bilbo));
		scn.ShadowChooseCard(thrush);
		assertInDiscard(thrush);
		assertInZone(Zone.SUPPORT, worrywort, dori);
		assertAttachedTo(acorn, bilbo);
		assertInHand(card1, card2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void BilboTakingAWoundLetsItBeUsedAndTheFreePeoplesPlayerDiscards2CardsToPreventIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riddles = scn.GetShadowCard("riddles");
		var veteran = scn.GetShadowCard("veteran");
		var bats = scn.GetShadowCard("bats");
		var footman = scn.GetShadowCard("footman");
		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var worrywort = scn.GetFreepsCard("worrywort");
		var card1 = scn.GetFreepsCard("aggressor");
		var card2 = scn.GetFreepsCard("veteran");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(worrywort);
		scn.MoveCardsToSupportArea(riddles);

		scn.StartGame();
		scn.MoveCardsToHand(card1, card2, footman);
		assertInHand(card1, card2, footman);
		scn.MoveMinionsToTable(veteran, bats);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, veteran}, new PhysicalCardImpl[]{bilbo, bats});
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(bilbo));

		// 7 against 7: Balin loses and takes a wound, which is not answered.
		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(balin));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// 3 against 4: Bilbo loses and takes a wound.
		scn.FreepsResolveSkirmish(bilbo);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(riddles));
		scn.ShadowChooseOptionalTrigger(riddles);
		scn.ShadowChooseCard(bats);
		assertEquals(1, scn.GetWoundsOn(bats));
		assertInDiscard(footman);

		assertTrue(scn.FreepsDecisionAvailable("discard 2 cards"));
		scn.FreepsChooseYes();
		assertInDiscard(card1, card2);
		assertEquals(0, scn.GetFreepsHandCount());
		// Prevented: the [shire] ally stays, and the Shadow player's costs stay paid.
		assertInZone(Zone.SUPPORT, worrywort);
		assertEquals(1, scn.GetWoundsOn(bats));
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOneCardInTheFreePeoplesHandThePreventionIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riddles = scn.GetShadowCard("riddles");
		var aggressor = scn.GetShadowCard("aggressor");
		var footman = scn.GetShadowCard("footman");
		var bilbo = scn.GetRingBearer();
		var negotiator = scn.GetFreepsCard("negotiator");
		var worrywort = scn.GetFreepsCard("worrywort");
		var acorn = scn.GetFreepsCard("acorn");
		var card1 = scn.GetFreepsCard("aggressor");
		scn.MoveAlliesToTable(worrywort);
		scn.MoveCardsToSupportArea(riddles);
		scn.AttachCardsTo(bilbo, acorn);

		scn.StartGame();
		scn.MoveCardsToHand(negotiator, card1, footman);
		assertInHand(negotiator, card1, footman);
		assertAttachedTo(acorn, bilbo);
		scn.FreepsPass();
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(negotiator);
		scn.ShadowChooseOptionalTrigger(riddles);
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertInDiscard(footman);

		// One card in hand cannot pay the 2: nothing is asked of the Free Peoples player.
		assertFalse(scn.FreepsDecisionAvailable("discard 2 cards"));
		scn.ShadowChooseCard(acorn);
		assertInDiscard(acorn);
		assertInHand(card1);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoShireCardButBilboItIsPaidForNothingAndNoPreventionIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riddles = scn.GetShadowCard("riddles");
		var aggressor = scn.GetShadowCard("aggressor");
		var footman = scn.GetShadowCard("footman");
		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var negotiator = scn.GetFreepsCard("negotiator");
		var bard = scn.GetFreepsCard("bard");
		var card1 = scn.GetFreepsCard("aggressor");
		var card2 = scn.GetFreepsCard("veteran");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(bard);
		scn.MoveCardsToSupportArea(riddles);

		scn.StartGame();
		scn.MoveCardsToHand(negotiator, card1, card2, footman);
		assertInHand(negotiator, card1, card2, footman);
		scn.FreepsPass();
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(aggressor));

		// The only ally to spot is Bard, of another culture; Bilbo is the one [shire] card in play.
		scn.FreepsPlayCard(negotiator);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(riddles));
		scn.ShadowChooseOptionalTrigger(riddles);
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertInDiscard(footman);
		assertFalse(scn.FreepsDecisionAvailable("discard 2 cards"));
		assertInHand(card1, card2);
		assertInZone(Zone.FREE_CHARACTERS, bilbo, balin);
		assertInZone(Zone.SUPPORT, bard);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotAnsweredWhenAnotherCompanionExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riddles = scn.GetShadowCard("riddles");
		var aggressor = scn.GetShadowCard("aggressor");
		var footman = scn.GetShadowCard("footman");
		var balin = scn.GetFreepsCard("balin");
		var worrywort = scn.GetFreepsCard("worrywort");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(worrywort);
		scn.MoveCardsToSupportArea(riddles);

		scn.StartGame();
		// The minion stands in the fellowship phase only to make the cost payable (cheated in early).
		scn.MoveMinionsToTable(aggressor);
		scn.MoveCardsToHand(footman);
		scn.FreepsRefreshDecision();
		assertInHand(footman);
		assertEquals(0, scn.GetWoundsOn(balin));

		scn.FreepsUseCardAction(balin);
		assertEquals(1, scn.GetWoundsOn(balin));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(riddles));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInHand(footman);
		assertInZone(Zone.SUPPORT, worrywort);
	}

	@Test
	public void NotOfferedWithAnEmptyHandOrWithNoMinionAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riddles = scn.GetShadowCard("riddles");
		var aggressor = scn.GetShadowCard("aggressor");
		var footman = scn.GetShadowCard("footman");
		var bilbo = scn.GetRingBearer();
		var negotiator = scn.GetFreepsCard("negotiator");
		var negotiator2 = scn.GetFreepsCard("negotiator2");
		var worrywort = scn.GetFreepsCard("worrywort");
		scn.MoveAlliesToTable(worrywort);
		scn.MoveCardsToSupportArea(riddles);

		scn.StartGame();
		scn.MoveCardsToHand(negotiator, negotiator2);
		assertInHand(negotiator, negotiator2);
		scn.FreepsPass();
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetShadowHandCount());

		// A minion able to exert, and no card in the Shadow hand.
		scn.FreepsPlayCard(negotiator);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(riddles));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// A card in hand, and the one minion exhausted.
		scn.MoveCardsToHand(footman);
		scn.AddWoundsToChar(aggressor, 1);
		assertInHand(footman);
		assertEquals(1, scn.GetWoundsOn(aggressor));
		scn.ShadowPass();
		scn.FreepsPlayCard(negotiator2);
		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(riddles));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertInHand(footman);
	}
}
