package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_092_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("massing", "53_92");

					// The minions.
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability
					put("soldier2", "1_271");
					put("soldier3", "1_271");
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, damage +1 and nothing else
					put("sentry2", "10_84");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, damage +1 and nothing else
					put("bowmen", "1_264");     // Orc Bowmen: a [sauron] condition, not a minion; its archery bonus matters in no regroup phase

					// The fellowship, by race; Frodo the Ring-bearer is a Hobbit.
					put("sam", "1_311");        // Sam, Son of Hamfast: a second Hobbit
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Man
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: an Elf ally, not in the fellowship; nothing away from site 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MassingintheEastStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Massing in the East
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: To play, discard a [sauron] minion. Return up to X [sauron] minions to hand, where X is the
		 * number of races you can spot in the fellowship.  The Free Peoples player may discard X cards from hand to
		 * prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("massing");

		assertEquals("Massing in the East", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsASauronMinionToReturnSauronMinionsToHandOneForEachRaceInTheFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var massing = scn.GetShadowCard("massing");
		var soldier = scn.GetShadowCard("soldier");
		var soldier2 = scn.GetShadowCard("soldier2");
		var soldier3 = scn.GetShadowCard("soldier3");
		var sentry = scn.GetShadowCard("sentry");
		var savage = scn.GetShadowCard("savage");
		var bowmen = scn.GetShadowCard("bowmen");

		// Two Hobbits and a Man: two races.  The Elf is an ally, not in the fellowship.
		scn.MoveCompanionsToTable("sam", "aragorn");
		scn.MoveAlliesToTable("silinde");
		scn.MoveCardsToHand(massing);
		scn.MoveCardsToHand(scn.GetFreepsCard("soldier"), scn.GetFreepsCard("sentry"));

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, soldier2, soldier3, sentry, savage);
		scn.SkipToPhase(Phase.REGROUP);
		// Put down only now: with a [sauron] Orc in play it adds to the minion archery total, and the archery
		// phase would stop to wound a companion.
		scn.MoveCardsToSupportArea(bowmen);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(massing));

		scn.ShadowPlayCard(massing);
		assertEquals(2, scn.GetTwilight());

		// Any [sauron] minion, an Orc or an Uruk-hai; not the [isengard] Uruk-hai, not the [sauron] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, soldier2, soldier3, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, bowmen));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(soldier);
		assertInDiscard(soldier);

		// [sauron] minions still in play, no more than 2; not the one just discarded, not the [isengard] one,
		// not the [sauron] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier2, soldier3, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, soldier, bowmen));
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(soldier2, sentry);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertInHand(soldier2, sentry);
		assertEquals(2, scn.GetShadowHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, soldier3, savage);
		assertInZone(Zone.SUPPORT, bowmen);
		assertInDiscard(massing);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayReturnNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var massing = scn.GetShadowCard("massing");
		var soldier = scn.GetShadowCard("soldier");
		var soldier2 = scn.GetShadowCard("soldier2");
		var sentry = scn.GetShadowCard("sentry");

		scn.MoveCompanionsToTable("aragorn");
		scn.MoveCardsToHand(massing);
		scn.MoveCardsToHand(scn.GetFreepsCard("soldier"), scn.GetFreepsCard("sentry"));

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, soldier2, sentry);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		scn.FreepsPass();

		scn.ShadowPlayCard(massing);
		scn.ShadowChooseCard(soldier);

		// X is 2 (a Hobbit and a Man), and the choice may be left empty.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards();

		// Nothing is to be returned, so there is nothing to prevent.
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertInZone(Zone.SHADOW_CHARACTERS, soldier2, sentry);
		assertEquals(2, scn.GetFreepsHandCount());
		assertInDiscard(soldier, massing);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithThreeRacesFewerThanThreeMayBeReturnedAndPreventingStillCostsThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var massing = scn.GetShadowCard("massing");
		var soldier = scn.GetShadowCard("soldier");
		var soldier2 = scn.GetShadowCard("soldier2");
		var soldier3 = scn.GetShadowCard("soldier3");
		var sentry = scn.GetShadowCard("sentry");
		var sentry2 = scn.GetShadowCard("sentry2");

		var held1 = scn.GetFreepsCard("soldier");
		var held2 = scn.GetFreepsCard("sentry");
		var held3 = scn.GetFreepsCard("savage");

		// A Hobbit, a Man and a Dwarf.
		scn.MoveCompanionsToTable("aragorn", "gimli");
		scn.MoveCardsToHand(massing);
		scn.MoveCardsToHand(held1, held2, held3);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, soldier2, soldier3, sentry, sentry2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		scn.FreepsPass();

		scn.ShadowPlayCard(massing);
		scn.ShadowChooseCard(soldier);

		// Four [sauron] minions left, no more than 3 of them, and fewer if the Shadow player likes.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(soldier2, sentry);

		// Preventing the return of 2 minions still costs X, 3 cards.
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();
		assertInDiscard(held1, held2, held3);
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, soldier2, soldier3, sentry, sentry2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithThreeRacesThreeMayBeChosenAndPreventingCostsThreeCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var massing = scn.GetShadowCard("massing");
		var soldier = scn.GetShadowCard("soldier");
		var soldier2 = scn.GetShadowCard("soldier2");
		var soldier3 = scn.GetShadowCard("soldier3");
		var sentry = scn.GetShadowCard("sentry");
		var sentry2 = scn.GetShadowCard("sentry2");

		var held1 = scn.GetFreepsCard("soldier");
		var held2 = scn.GetFreepsCard("sentry");
		var held3 = scn.GetFreepsCard("savage");
		var held4 = scn.GetFreepsCard("soldier2");

		// A Hobbit, a Man and a Dwarf.
		scn.MoveCompanionsToTable("aragorn", "gimli");
		scn.MoveCardsToHand(massing);
		scn.MoveCardsToHand(held1, held2, held3, held4);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, soldier2, soldier3, sentry, sentry2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		scn.FreepsPass();

		scn.ShadowPlayCard(massing);
		scn.ShadowChooseCard(soldier);

		// Four [sauron] minions left, and no more than 3 of them.
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(soldier2, sentry, sentry2);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		// Preventing it costs 3 of his 4 cards, of his choosing.
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(held1, held2, held4);

		assertInDiscard(held1, held2, held4);
		assertInHand(held3);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier2, soldier3, sentry, sentry2);
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void FreePeoplesPlayerMayDiscardXCardsOfHisChoosingToPreventIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var massing = scn.GetShadowCard("massing");
		var soldier = scn.GetShadowCard("soldier");
		var soldier2 = scn.GetShadowCard("soldier2");
		var soldier3 = scn.GetShadowCard("soldier3");
		var sentry = scn.GetShadowCard("sentry");
		var held1 = scn.GetFreepsCard("soldier");
		var held2 = scn.GetFreepsCard("sentry");
		var held3 = scn.GetFreepsCard("savage");

		// A Hobbit and a Man: X is 2.
		scn.MoveCompanionsToTable("aragorn");
		scn.MoveCardsToHand(massing);
		scn.MoveCardsToHand(held1, held2, held3);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, soldier2, soldier3, sentry);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		scn.FreepsPass();

		scn.ShadowPlayCard(massing);
		scn.ShadowChooseCard(soldier);
		scn.ShadowChooseCards(soldier2, sentry);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		// He chooses which 2 of his 3 cards go.
		assertTrue(scn.FreepsHasCardChoiceAvailable(held1, held2, held3));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(held1, held3);

		assertInDiscard(held1, held3);
		assertInHand(held2);
		assertEquals(1, scn.GetFreepsHandCount());
		// The minions stay; the cost stays paid.
		assertInZone(Zone.SHADOW_CHARACTERS, soldier2, sentry);
		assertInDiscard(soldier, massing);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithFewerThanXCardsInHandTheFreePeoplesPlayerIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var massing = scn.GetShadowCard("massing");
		var soldier = scn.GetShadowCard("soldier");
		var soldier2 = scn.GetShadowCard("soldier2");
		var soldier3 = scn.GetShadowCard("soldier3");
		var sentry = scn.GetShadowCard("sentry");

		// A Hobbit and a Man: X is 2, against one card in hand.
		scn.MoveCompanionsToTable("aragorn");
		scn.MoveCardsToHand(massing);
		scn.MoveCardsToHand(scn.GetFreepsCard("soldier"));

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, soldier2, soldier3, sentry);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		scn.FreepsPass();

		scn.ShadowPlayCard(massing);
		scn.ShadowChooseCard(soldier);
		scn.ShadowChooseCards(soldier2, sentry);

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertInHand(soldier2, sentry);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoOtherSauronMinionInPlayNothingIsReturnedAndThePreventionIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var massing = scn.GetShadowCard("massing");
		var soldier = scn.GetShadowCard("soldier");
		var savage = scn.GetShadowCard("savage");

		// A Hobbit and a Man: X is 2, against two cards in hand.
		scn.MoveCompanionsToTable("aragorn");
		scn.MoveCardsToHand(massing);
		scn.MoveCardsToHand(scn.GetFreepsCard("soldier"), scn.GetFreepsCard("sentry"));

		scn.StartGame();
		// The one [sauron] minion is the cost; the [isengard] one cannot be returned.
		scn.MoveMinionsToTable(soldier, savage);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(massing));
		scn.ShadowPlayCard(massing);

		// The event is played and its cost is paid, though there is nothing for it to return.
		assertInDiscard(soldier, massing);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var massing = scn.GetShadowCard("massing");
		var soldier = scn.GetShadowCard("soldier");
		var soldier2 = scn.GetShadowCard("soldier2");
		scn.MoveCardsToHand(massing);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, soldier2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(3);
		scn.FreepsPass();

		// The Shadow player's last actions before the regroup phase, when no skirmish is fought.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(massing));

		scn.ShadowPass();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(massing));
	}

	@Test
	public void CannotBePlayedWithoutASauronMinionToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var massing = scn.GetShadowCard("massing");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(massing);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(massing));
	}
}
