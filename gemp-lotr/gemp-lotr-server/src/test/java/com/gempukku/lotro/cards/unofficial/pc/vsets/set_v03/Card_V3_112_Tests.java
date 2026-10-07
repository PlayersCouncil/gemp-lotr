package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_112_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("barrel1", "103_112");
					put("barrel2", "103_112");
					put("barrel3", "103_112");
					// Frodo's Pipe: [shire] possession, a pipe; bearer must be Frodo.  "Fellowship: Discard a
					// pipeweed possession and spot X pipes to heal a companion with the Frodo signet X times."
					put("pipe", "3_107");
					// Old Toby: [shire] possession, pipeweed.  Its own text is a "when you play", never used here.
					put("toby", "1_305");
					// Halfling Leaf: [shire] condition, pipeweed and no pipe.  "Skirmish: Discard a pipeweed from
					// play to cancel a skirmish involving an unbound Hobbit."
					put("leaf", "17_106");
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] companion
					put("guard", "1_7");        // Dwarf Guard: [dwarven] companion
					put("maggot", "1_288");     // Farmer Maggot: [shire] ally
					// Light Shining Faintly: a [shire] event, here only as a card in a pile
					put("light", "4_309");
					// Aragorn, Ranger of the North: [gondor] companion, strength 8; no unbound Hobbit
					put("aragorn", "1_89");

					put("runner", "1_178");     // Goblin Runner: a Shadow minion, here only as a card in a pile
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: the bearer of Frodo's Pipe
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetShadowCardsOnlyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("barrel1", "103_112");
					put("runner", "1_178");     // Goblin Runner: Shadow minion
					put("troop", "1_177");      // Goblin Patrol Troop: Shadow minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetEmptyDeckScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("barrel1", "103_112");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HornblowerBarrelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Hornblower Barrel
		 * Unique: 2
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: Pipeweed.
		 * 	When you play this, you may discard a Free Peoples card from your draw deck.
		 * 	When you discard this with a pipe, you may spot another Hornblower Barrel to take 2 other Free
		 * 	Peoples cards (except events or Hornblower Barrels) from your discard pile into hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("barrel1");

		assertEquals("Hornblower Barrel", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.PIPEWEED));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedToTheSupportAreaItMayDiscardAFreePeoplesCardFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var barrel1 = scn.GetFreepsCard("barrel1");
		var barrel2 = scn.GetFreepsCard("barrel2");
		var pipe = scn.GetFreepsCard("pipe");
		var toby = scn.GetFreepsCard("toby");
		var leaf = scn.GetFreepsCard("leaf");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var maggot = scn.GetFreepsCard("maggot");
		var light = scn.GetFreepsCard("light");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetFreepsCard("runner");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCardsToHand(barrel1);

		scn.StartGame();

		// The other twelve cards of the scenario are the draw deck: ten Free Peoples cards and two minions.
		assertEquals(12, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetTwilight());
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsPlayAvailable(barrel1));

		scn.FreepsPlayCard(barrel1);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, barrel1);
		assertNotAttachedTo(barrel1, frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(barrel1));

		scn.MarkGameLog();
		scn.FreepsAcceptOptionalTrigger();

		// The whole deck is looked through by its owner, and by nobody else.
		assertEquals(12, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// Any Free Peoples card: an event, companions of three cultures, an ally, possessions, a condition,
		// another Hornblower Barrel.  Not the two minions.
		assertTrue(scn.FreepsHasCardChoiceAvailable(light, merry, guard, aragorn, maggot, toby, pipe, leaf, barrel2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, troop));
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(merry);

		assertInDiscard(merry);
		assertEquals(11, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		// A deck that has been looked through is shuffled, once.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		// The other player is told which card went, by the game log.
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(merry));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(guard));
		assertInZone(Zone.SUPPORT, barrel1);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HavingLookedThroughTheDrawDeckThePlayerNeedNotDiscardACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barrel1 = scn.GetFreepsCard("barrel1");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToHand(barrel1);

		scn.StartGame();

		assertEquals(12, scn.GetFreepsDeckCount());
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(barrel1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(barrel1));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();

		// "There is no penalty if you don't find ... a card you are looking for in your draw deck."
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsDeclineChoosing();

		assertEquals(12, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		// The deck was looked through all the same, and is shuffled.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ASecondCopyPlayedMakesOnlyItsOwnOfferAndDecliningLeavesTheDeckAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barrel1 = scn.GetFreepsCard("barrel1");
		var barrel2 = scn.GetFreepsCard("barrel2");
		scn.MoveCardsToSupportArea(barrel1);
		scn.MoveCardsToHand(barrel2);

		scn.StartGame();

		assertEquals(11, scn.GetFreepsDeckCount());
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.FreepsPlayAvailable(barrel2));

		scn.FreepsPlayCard(barrel2);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, barrel1, barrel2);
		// The copy just played makes its offer; the copy that was already there does not.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(barrel2));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(barrel1));

		scn.FreepsDeclineOptionalTrigger();

		// Declined: the deck is not looked at, so nothing is discarded and nothing shuffled.
		assertEquals(11, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoFreePeoplesCardInTheDrawDeckItIsLookedThroughAndShuffledAndNothingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowCardsOnlyScenario();

		var barrel1 = scn.GetFreepsCard("barrel1");
		var runner = scn.GetFreepsCard("runner");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCardsToHand(barrel1);

		scn.StartGame();

		// Two minions are the whole draw deck.
		assertEquals(2, scn.GetFreepsDeckCount());
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(barrel1);
		// What a draw deck holds is not known until it is looked at: the offer is made.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(barrel1));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, troop));
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DECK, runner, troop);
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAnEmptyDrawDeckTheOfferIsStillMadeAndTakingItDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEmptyDeckScenario();

		var barrel1 = scn.GetFreepsCard("barrel1");
		scn.MoveCardsToHand(barrel1);

		scn.StartGame();

		assertEquals(0, scn.GetFreepsDeckCount());

		scn.FreepsPlayCard(barrel1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(barrel1));
		scn.FreepsAcceptOptionalTrigger();

		// An empty deck is shown, and there is nothing to choose.
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertInZone(Zone.SUPPORT, barrel1);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	@Ignore("Q-0104: an optional trigger with no cost is offered with nothing in the pile for it to act on")
	public void WithAnEmptyDrawDeckNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEmptyDeckScenario();

		var barrel1 = scn.GetFreepsCard("barrel1");
		scn.MoveCardsToHand(barrel1);

		scn.StartGame();

		assertEquals(0, scn.GetFreepsDeckCount());

		scn.FreepsPlayCard(barrel1);
		assertInZone(Zone.SUPPORT, barrel1);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(barrel1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardedWithAPipeItMayTakeTwoOtherFreePeoplesCardsFromTheDiscardPileIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var barrel1 = scn.GetFreepsCard("barrel1");
		var barrel2 = scn.GetFreepsCard("barrel2");
		var barrel3 = scn.GetFreepsCard("barrel3");
		var pipe = scn.GetFreepsCard("pipe");
		var toby = scn.GetFreepsCard("toby");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var maggot = scn.GetFreepsCard("maggot");
		var light = scn.GetFreepsCard("light");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCardsToSupportArea(barrel1, barrel2);
		scn.AttachCardsTo(frodo, pipe);
		// In the discard pile: a [shire] companion, a [dwarven] companion, a [shire] ally and another
		// pipeweed, which may be taken; an event, a Hornblower Barrel and a minion, which may not.
		scn.MoveCardsToDiscard(merry, guard, maggot, toby, light, barrel3, runner);

		scn.StartGame();

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(7, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsActionAvailable(pipe));

		scn.FreepsUseCardAction(pipe);
		assertTrue(scn.FreepsHasCardChoiceAvailable(barrel1, barrel2));
		scn.FreepsChooseCard(barrel1);

		// The Barrel that was discarded makes the offer, from the discard pile; the one in play does not.
		assertInDiscard(barrel1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(barrel1));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(barrel2));
		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, guard, maggot, toby));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(light, barrel3, barrel1, runner));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(merry, toby);

		assertInHand(merry, toby);
		assertEquals(2, scn.GetFreepsHandCount());
		assertInDiscard(guard, maggot, light, barrel3, barrel1, runner);
		assertInZone(Zone.SUPPORT, barrel2);

		// The pipe's own business is then finished: one pipe is spotted, and nobody is wounded to be healed.
		scn.FreepsChoose("1");
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningWhenDiscardedWithAPipeLeavesTheDiscardPileAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var barrel1 = scn.GetFreepsCard("barrel1");
		var barrel2 = scn.GetFreepsCard("barrel2");
		var pipe = scn.GetFreepsCard("pipe");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveCardsToSupportArea(barrel1, barrel2);
		scn.AttachCardsTo(frodo, pipe);
		scn.MoveCardsToDiscard(merry, guard, maggot);

		scn.StartGame();

		scn.FreepsUseCardAction(pipe);
		scn.FreepsChooseCard(barrel1);
		assertInDiscard(barrel1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(barrel1));
		scn.FreepsDeclineOptionalTrigger();

		assertFalse(scn.FreepsHasCardChoiceAvailable(merry));
		assertInDiscard(merry, guard, maggot, barrel1);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDiscardCount());

		scn.FreepsChoose("1");
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardedWithAPipeweedThatIsNoPipeItOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var barrel1 = scn.GetFreepsCard("barrel1");
		var barrel2 = scn.GetFreepsCard("barrel2");
		var pipe = scn.GetFreepsCard("pipe");
		var leaf = scn.GetFreepsCard("leaf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var maggot = scn.GetFreepsCard("maggot");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(barrel1, barrel2, leaf);
		// A pipe is in play, and is not what does the discarding.
		scn.AttachCardsTo(frodo, pipe);
		scn.MoveCardsToDiscard(merry, guard, maggot);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, troop);

		assertTrue(scn.FreepsActionAvailable(leaf));
		scn.FreepsUseCardAction(leaf);
		assertTrue(scn.FreepsHasCardChoiceAvailable(barrel1, barrel2, leaf));
		scn.FreepsChooseCard(barrel1);

		// Discarded by its owner, with another Barrel to spot and cards to take: but not with a pipe.
		assertInDiscard(barrel1);
		assertInZone(Zone.SUPPORT, barrel2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInDiscard(merry, guard, maggot);
		assertEquals(0, scn.GetFreepsHandCount());
		// Aragorn is no unbound Hobbit: the Leaf cancels nothing and the skirmish goes on.
		assertTrue(scn.IsCharSkirmishing(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void DiscardedWithAPipeWhileNoOtherHornblowerBarrelIsInPlayItOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var barrel1 = scn.GetFreepsCard("barrel1");
		var barrel3 = scn.GetFreepsCard("barrel3");
		var pipe = scn.GetFreepsCard("pipe");
		var toby = scn.GetFreepsCard("toby");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var maggot = scn.GetFreepsCard("maggot");
		// Another pipeweed is in play, and another Hornblower Barrel is in the discard pile.
		scn.MoveCardsToSupportArea(barrel1, toby);
		scn.AttachCardsTo(frodo, pipe);
		scn.MoveCardsToDiscard(merry, guard, maggot, barrel3);

		scn.StartGame();

		scn.FreepsUseCardAction(pipe);
		assertTrue(scn.FreepsHasCardChoiceAvailable(barrel1, toby));
		scn.FreepsChooseCard(barrel1);

		assertInDiscard(barrel1);
		assertInZone(Zone.SUPPORT, toby);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		scn.FreepsChoose("1");
		assertInDiscard(merry, guard, maggot, barrel3);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHinderedHornblowerBarrelCannotBeSpottedAsTheOtherOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var barrel1 = scn.GetFreepsCard("barrel1");
		var barrel2 = scn.GetFreepsCard("barrel2");
		var pipe = scn.GetFreepsCard("pipe");
		var toby = scn.GetFreepsCard("toby");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveCardsToSupportArea(barrel1, barrel2, toby);
		scn.AttachCardsTo(frodo, pipe);
		scn.MoveCardsToDiscard(merry, guard, maggot);
		scn.HinderCard(barrel2);

		scn.StartGame();

		assertTrue(scn.IsHindered(barrel2));
		scn.FreepsUseCardAction(pipe);
		// A hindered card is no pipeweed to discard either.
		assertTrue(scn.FreepsHasCardChoiceAvailable(barrel1, toby));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barrel2));
		scn.FreepsChooseCard(barrel1);

		assertInDiscard(barrel1);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		scn.FreepsChoose("1");
		assertTrue(scn.IsHindered(barrel2));
		assertInDiscard(merry, guard, maggot);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithOneCardThatMayBeTakenItIsTakenWithoutAChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var barrel1 = scn.GetFreepsCard("barrel1");
		var barrel2 = scn.GetFreepsCard("barrel2");
		var barrel3 = scn.GetFreepsCard("barrel3");
		var pipe = scn.GetFreepsCard("pipe");
		var merry = scn.GetFreepsCard("merry");
		var light = scn.GetFreepsCard("light");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCardsToSupportArea(barrel1, barrel2);
		scn.AttachCardsTo(frodo, pipe);
		scn.MoveCardsToDiscard(merry, light, barrel3, runner);

		scn.StartGame();

		scn.FreepsUseCardAction(pipe);
		scn.FreepsChooseCard(barrel1);
		assertInDiscard(barrel1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(barrel1));
		scn.FreepsAcceptOptionalTrigger();

		// As much as can be done is done.
		assertInHand(merry);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInDiscard(light, barrel3, barrel1, runner);

		scn.FreepsChoose("1");
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoCardThatMayBeTakenNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var barrel1 = scn.GetFreepsCard("barrel1");
		var barrel2 = scn.GetFreepsCard("barrel2");
		var barrel3 = scn.GetFreepsCard("barrel3");
		var pipe = scn.GetFreepsCard("pipe");
		var light = scn.GetFreepsCard("light");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCardsToSupportArea(barrel1, barrel2);
		scn.AttachCardsTo(frodo, pipe);
		// An event, a Hornblower Barrel and a minion: nothing the text lets be taken.
		scn.MoveCardsToDiscard(light, barrel3, runner);

		scn.StartGame();

		scn.FreepsUseCardAction(pipe);
		scn.FreepsChooseCard(barrel1);
		assertInDiscard(barrel1);
		assertInZone(Zone.SUPPORT, barrel2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		scn.FreepsChoose("1");
		assertInDiscard(light, barrel3, barrel1, runner);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
