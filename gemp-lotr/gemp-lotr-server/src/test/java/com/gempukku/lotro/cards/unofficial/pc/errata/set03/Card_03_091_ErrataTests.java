package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_091_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cruelty", "53_91");

					// The minions.
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, damage +1 and nothing else
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, damage +1 and nothing else

					// The Free Peoples characters.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer
					put("gimli", "1_13");       // Gimli, Son of Gloin: vitality 3
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: Hobbit ally, a fellowship ability

					// Free Peoples cards of three types for the top of the Free Peoples draw deck.
					put("tale", "2_1");         // Beneath the Mountains: [dwarven] condition
					put("disquiet", "2_2");     // Disquiet of Our People: [dwarven] event
					put("bracers", "2_3");      // Dwarven Bracers: [dwarven] possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HisCrueltyandMaliceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: His Cruelty and Malice
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Regroup: Discard a [sauron] minion to reveal the top 2 cards of your opponent's draw deck.  If
		 * at least 1 is a Free Peoples card, exert a companion (except the Ring-bearer).  Your opponent may discard
		 * all revealed Free Peoples cards to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cruelty");

		assertEquals("His Cruelty and Malice", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsASauronMinionToRevealTheTopTwoCardsOfTheOpponentsDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cruelty = scn.GetShadowCard("cruelty");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Two Shadow cards of the Free Peoples player's own.
		var top1 = scn.GetFreepsCard("soldier");
		var top2 = scn.GetFreepsCard("cruelty");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(cruelty);

		scn.StartGame();
		// The last card named ends on top.
		scn.MoveCardsToTopOfDeck(top2, top1);
		scn.MoveMinionsToTable(soldier, sentry, savage);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		int freepsDeck = scn.GetFreepsDeckCount();
		assertSame(top1, scn.GetFreepsTopOfDeck());
		assertSame(top2, scn.GetFromTopOfFreepsDeck(2));
		assertTrue(scn.ShadowActionAvailable(cruelty));

		scn.ShadowUseCardAction(cruelty);
		// Either [sauron] minion, an Orc or an Uruk-hai; not the [isengard] Uruk-hai, and not the [sauron]
		// condition itself.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, cruelty));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sentry);
		assertInDiscard(sentry);

		// Both players are shown the two cards from the top of the Free Peoples deck.
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(top1, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(top2, false));
		assertEquals(2, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(top1, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(top2, false));
		scn.DismissRevealedCards();

		// Neither is a Free Peoples card: nothing more happens, and they stay where they were, in order.
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertSame(top1, scn.GetFreepsTopOfDeck());
		assertSame(top2, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.SUPPORT, cruelty);
		// What the game does after this, with the other [sauron] minion still in play, is read in
		// NothingMoreHappensOnceTheAbilityIsCarriedOut.
	}

	@Test
	public void AFreePeoplesCardRevealedExertsACompanionOfTheShadowPlayersChoosing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cruelty = scn.GetShadowCard("cruelty");
		var soldier = scn.GetShadowCard("soldier");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var bilbo = scn.GetFreepsCard("bilbo");
		// One Free Peoples card and one Shadow card of the Free Peoples player's own.
		var tale = scn.GetFreepsCard("tale");
		var shadowCard = scn.GetFreepsCard("soldier");

		scn.MoveCompanionsToTable(aragorn, sam, gimli);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToSupportArea(cruelty);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(shadowCard, tale);
		scn.MoveMinionsToTable(soldier);
		// Gimli, vitality 3, exhausted.
		scn.AddWoundsToChar(gimli, 2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowUseCardAction(cruelty);
		scn.DismissRevealedCards();

		// Any companion who can exert but the Ring-bearer, Ring-bound Sam included; not an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, gimli, bilbo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sam);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertSame(tale, scn.GetFreepsTopOfDeck());
		assertSame(shadowCard, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(0, scn.GetFreepsDiscardCount());
		// The prevention is not asked again; what the game asks next is read in
		// NothingMoreHappensOnceTheAbilityIsCarriedOut.
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
	}

	@Test
	public void FreePeoplesPlayerMayDiscardTheRevealedFreePeoplesCardToPreventIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cruelty = scn.GetShadowCard("cruelty");
		var soldier = scn.GetShadowCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		var disquiet = scn.GetFreepsCard("disquiet");
		var shadowCard = scn.GetFreepsCard("soldier");
		var bracers = scn.GetFreepsCard("bracers");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(cruelty);

		scn.StartGame();
		// A Free Peoples event, a Shadow card, and a Free Peoples card that is not revealed.
		scn.MoveCardsToTopOfDeck(bracers, shadowCard, disquiet);
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		int freepsDeck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.ShadowUseCardAction(cruelty);
		assertInDiscard(soldier);
		scn.DismissRevealedCards();

		// Aragorn is the one companion who is not the Ring-bearer.
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		assertInDiscard(disquiet);
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertEquals(freepsDeck - 1, scn.GetFreepsDeckCount());
		assertSame(shadowCard, scn.GetFreepsTopOfDeck());
		assertSame(bracers, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// The prevention is not asked again; what the game asks next is read in
		// NothingMoreHappensOnceTheAbilityIsCarriedOut.
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
	}

	@Test
	public void TwoFreePeoplesCardsRevealedAreBothDiscardedToPreventIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cruelty = scn.GetShadowCard("cruelty");
		var soldier = scn.GetShadowCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		var tale = scn.GetFreepsCard("tale");
		var bracers = scn.GetFreepsCard("bracers");
		var disquiet = scn.GetFreepsCard("disquiet");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(cruelty);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(disquiet, bracers, tale);
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		int freepsDeck = scn.GetFreepsDeckCount();

		scn.ShadowUseCardAction(cruelty);
		scn.DismissRevealedCards();
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		assertInDiscard(tale, bracers);
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertEquals(freepsDeck - 2, scn.GetFreepsDeckCount());
		assertSame(disquiet, scn.GetFreepsTopOfDeck());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// The prevention is not asked again; what the game asks next is read in
		// NothingMoreHappensOnceTheAbilityIsCarriedOut.
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
	}

	@Test
	public void WithOnlyTheRingBearerToExertNothingHappensAndThePreventionIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cruelty = scn.GetShadowCard("cruelty");
		var soldier = scn.GetShadowCard("soldier");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var tale = scn.GetFreepsCard("tale");
		var bracers = scn.GetFreepsCard("bracers");

		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(cruelty);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(bracers, tale);
		scn.MoveMinionsToTable(soldier);
		// The one companion besides the Ring-bearer is exhausted.
		scn.AddWoundsToChar(gimli, 2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		scn.ShadowUseCardAction(cruelty);
		assertInDiscard(soldier);
		scn.DismissRevealedCards();

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertSame(tale, scn.GetFreepsTopOfDeck());
		assertSame(bracers, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(0, scn.GetFreepsDiscardCount());
		// What the game asks next is read in NothingMoreHappensOnceTheAbilityIsCarriedOut.
	}

	@Test
	public void NothingMoreHappensOnceTheAbilityIsCarriedOut() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cruelty = scn.GetShadowCard("cruelty");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var tale = scn.GetFreepsCard("tale");
		var bracers = scn.GetFreepsCard("bracers");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(cruelty);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(bracers, tale);
		scn.MoveMinionsToTable(soldier, sentry);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		scn.ShadowUseCardAction(cruelty);
		scn.ShadowChooseCard(soldier);
		scn.DismissRevealedCards();
		scn.FreepsChooseNo();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(soldier);
		// The one minion discarded is the cost; the other [sauron] minion stays, and nothing else is asked.
		assertInZone(Zone.SHADOW_CHARACTERS, sentry);
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBeUsedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cruelty = scn.GetShadowCard("cruelty");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(cruelty);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// The Shadow player's last actions before the regroup phase, when no skirmish is fought.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cruelty));

		scn.ShadowPass();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(cruelty));
	}

	@Test
	public void CannotBeUsedWithoutASauronMinionToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cruelty = scn.GetShadowCard("cruelty");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(cruelty);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cruelty));
	}

	@Test
	public void WithTheOpponentsDrawDeckEmptyItIsOfferedFlaggedNoEffectAndTheMinionIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cruelty = scn.GetShadowCard("cruelty");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var tale = scn.GetFreepsCard("tale");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(cruelty);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, sentry);
		scn.SkipToPhase(Phase.REGROUP);
		// One card is left in the Free Peoples player's draw deck, fewer than the 2 to be revealed: there is
		// something to reveal.
		scn.MoveCardsToDiscard(new ArrayList<>(scn.GetFreepsDrawDeck()).toArray(new PhysicalCardImpl[0]));
		scn.MoveCardsToTopOfDeck(tale);
		scn.FreepsPass();

		assertEquals(1, scn.GetFreepsDeckCount());
		assertTrue(scn.ShadowActionAvailable(cruelty));
		assertEquals("false", scn.ShadowActionNoEffect(cruelty));

		// That card gone, the minion can still be discarded, so the ability is offered (Q-0163); it is marked as
		// doing nothing.  It is the opponent's deck that is empty: the Shadow player's own is not.
		scn.MoveCardsToDiscard(tale);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetFreepsDeckCount());
		assertTrue(scn.GetShadowDeckCount() > 0);
		assertTrue(scn.ShadowActionAvailable(cruelty));
		assertEquals("effect", scn.ShadowActionNoEffect(cruelty));

		// Used all the same, the minion chosen is discarded; nobody is shown anything, no companion is chosen
		// or exerted and the opponent is asked nothing.
		int freepsDiscard = scn.GetFreepsDiscardCount();
		scn.ShadowUseCardAction(cruelty);
		scn.ShadowChooseCard(soldier);
		assertInDiscard(soldier);
		assertInZone(Zone.SHADOW_CHARACTERS, sentry);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(freepsDiscard, scn.GetFreepsDiscardCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
