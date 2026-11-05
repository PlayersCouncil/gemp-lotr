package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_052_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 (3/4) as the Ring-bearer and no card in the
	// Ring slot.  The event is played in the Shadow phase at Erebor 30_55 (site 7, a mountain, where Smaug stays in
	// play), reached by SkipToSite(6) and the move; its Shadow ability is not used.  The piles are laid after the
	// skipped turns, which draw.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("awakening", "32_52");
					// A second copy, left in the discard pile, for "this card".
					put("awakening2", "32_52");
					// Smaug, The Golden: the one Smaug in print, a [smaug] Dragon minion, 17/5, damage +2, fierce, cost
					// 16 (-1 for each Dwarf, Man and Elf spotted), discarded if not at a mountain.  Two copies, for the
					// two piles.
					put("smaug", "30_66");
					put("smaug2", "30_66");
					// Goblin Patrol Troop: [moria] Orc, cost 6, 13/3, no text: a minion of another title the pool can pay
					// for, and an Orc.
					put("troop", "1_177");
					// Orc Sentinel: [sauron] Orc, 5/2; [sauron] Orcs' site number -1 and a Shadow ability, not used.
					put("sentinel", "33_49");
					// Orkish Aggressor: [gundabad] Orc, 6/2; an Assignment ability, not used.
					put("aggressor", "30_39");
					// Dark Bats: [moria] Creature, 4/2, no Orc; a Shadow ability, not used.
					put("bats", "32_30");
					// Balin, Brother of Dwalin: [dwarven] Dwarf companion, 7/3; a Fellowship ability, not used.
					put("balin", "30_2");
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void SmaugsAwakeningStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Smaug's Awakening
		 * Unique: False
		 * Side: Shadow
		 * Culture: Smaug
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Play Smaug from your draw deck or discard pile at twilight cost -8. He is strength -4 and damage
		 * -1 until the regroup phase. Then you may discard 2 Orcs to take this card back into hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("awakening");

		assertEquals("Smaug's Awakening", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SMAUG, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysSmaugFromTheDrawDeckAtTwilightCostMinus8AndShufflesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var awakening = scn.GetShadowCard("awakening");
		var smaug = scn.GetShadowCard("smaug");
		var smaug2 = scn.GetShadowCard("smaug2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		// A Smaug in each pile, and the Troop in the deck.
		scn.SetShadowHand(awakening);
		scn.SetShadowDrawDeck(smaug, troop);
		scn.MoveCardsToDiscard(smaug2);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInHand(awakening);
		assertInZone(Zone.DECK, smaug, troop);
		assertInZone(Zone.DISCARD, smaug2);
		assertEquals(7, scn.GetCurrentSiteNumber());
		int shuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.ShadowPlayAvailable(awakening));

		scn.ShadowPlayCard(awakening);
		// Smaug lies in both piles: the player is asked which.
		assertTrue(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowChoose("draw deck");
		scn.DismissRevealedCards();

		// Smaug is offered; the Troop, a minion of another title the pool could pay for, is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(smaug));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		scn.ShadowChooseCardBPFromSelection(smaug);

		assertInZone(Zone.SHADOW_CHARACTERS, smaug);
		assertInZone(Zone.DISCARD, smaug2, awakening);
		assertInZone(Zone.DECK, troop);
		// 10 - (16 - 8): only Bilbo, a Hobbit, to spot
		assertEquals(2, scn.GetTwilight());
		// One shuffle for the card leaving the deck that was looked through, one for the look itself.
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		// No Orc in play: nothing is asked about taking the event back.
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSmaugInBothPilesTheDiscardPileMayBeTakenAndTheMinus8AddsToSmaugsOwnDiscount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var awakening = scn.GetShadowCard("awakening");
		var smaug = scn.GetShadowCard("smaug");
		var smaug2 = scn.GetShadowCard("smaug2");
		var troop = scn.GetShadowCard("troop");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, balin);
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.SetShadowHand(awakening);
		scn.SetShadowDrawDeck(smaug2);
		// The Troop beside Smaug: a minion of another title the pool could pay for.
		scn.MoveCardsToDiscard(smaug, troop);
		// 16 - 1 for Balin, a Dwarf, - 8: exactly the pool.
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.DECK, smaug2);
		assertInZone(Zone.DISCARD, smaug, troop);
		int shuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.ShadowPlayAvailable(awakening));

		scn.ShadowPlayCard(awakening);
		scn.ShadowChoose("discard pile");

		// The one Smaug in the discard pile is played unasked.
		assertInZone(Zone.SHADOW_CHARACTERS, smaug);
		assertInZone(Zone.DECK, smaug2);
		assertInZone(Zone.DISCARD, troop, awakening);
		assertEquals(0, scn.GetTwilight());
		// No deck was looked through.
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSmaugOnlyInTheDiscardPileNoPileIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var awakening = scn.GetShadowCard("awakening");
		var smaug = scn.GetShadowCard("smaug");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.SetShadowHand(awakening);
		scn.SetShadowDrawDeck(troop);
		scn.MoveCardsToDiscard(smaug);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.DECK, troop);
		assertInZone(Zone.DISCARD, smaug);

		scn.ShadowPlayCard(awakening);

		// Nothing to play in the deck: the discard pile is the one pile left, and its one Smaug is played unasked.
		assertFalse(scn.ShadowChoiceAvailable("draw deck"));
		assertInZone(Zone.SHADOW_CHARACTERS, smaug);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotPlayableWithNoSmaugInEitherPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var awakening = scn.GetShadowCard("awakening");
		var smaug = scn.GetShadowCard("smaug");
		var smaug2 = scn.GetShadowCard("smaug2");
		var troop = scn.GetShadowCard("troop");
		var freepsSmaug = scn.GetFreepsCard("smaug");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		// This player's Smaugs in his hand, the other player's in that player's own discard pile, and the Troop alone
		// in this player's draw deck.
		scn.SetShadowHand(awakening, smaug, smaug2);
		scn.SetShadowDrawDeck(troop);
		scn.MoveCardsToDiscard(freepsSmaug);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertInHand(awakening, smaug, smaug2);
		assertInZone(Zone.DECK, troop);
		assertInZone(Zone.DISCARD, freepsSmaug);

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(awakening));
	}

	@Test
	public void IsNotPlayableWithOneTwilightTooFewForSmaugInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var awakening = scn.GetShadowCard("awakening");
		var smaug = scn.GetShadowCard("smaug");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.SetShadowHand(awakening);
		scn.SetShadowDrawDeck(troop);
		scn.MoveCardsToDiscard(smaug);
		// 16 - 8 is 8, with only Bilbo to spot.
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.DISCARD, smaug);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(awakening));

		scn.SetTwilight(8);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(awakening));
	}

	@Test
	public void SmaugIsStrengthMinus4AndDamageMinus1UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var awakening = scn.GetShadowCard("awakening");
		var smaug = scn.GetShadowCard("smaug");
		var smaug2 = scn.GetShadowCard("smaug2");
		var bats = scn.GetShadowCard("bats");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, balin);
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(bats);
		scn.SetShadowHand(awakening);
		scn.SetShadowDrawDeck(smaug2);
		scn.MoveCardsToDiscard(smaug);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.DISCARD, smaug);
		assertEquals(17, scn.GetStrength(smaug));
		assertEquals(2, scn.GetKeywordCount(smaug, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(bats));

		scn.ShadowPlayCard(awakening);
		scn.ShadowChoose("discard pile");

		assertInZone(Zone.SHADOW_CHARACTERS, smaug);
		assertEquals(13, scn.GetStrength(smaug));
		assertEquals(1, scn.GetKeywordCount(smaug, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(smaug, Keyword.FIERCE));
		// The other minion is untouched.
		assertEquals(4, scn.GetStrength(bats));
		assertEquals(0, scn.GetKeywordCount(bats, Keyword.DAMAGE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, smaug}, new PhysicalCardImpl[]{bilbo, bats});
		// Bilbo's skirmish first: he loses to the Bats' 4 and takes a wound.
		scn.FreepsResolveSkirmish(bilbo);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(bilbo));

		scn.FreepsResolveSkirmish(balin);
		// In the skirmish that follows one already resolved, still 13 and damage +1.
		assertEquals(13, scn.GetStrength(smaug));
		assertEquals(1, scn.GetKeywordCount(smaug, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(balin));
		scn.PassSkirmishActions();
		// Balin's 7 is not overwhelmed by 13: 1 wound for losing and 1 for damage +1.
		assertEquals(2, scn.GetWoundsOn(balin));
		assertInZone(Zone.FREE_CHARACTERS, balin);

		// The fierce round, in which nobody is assigned to Smaug.
		assertTrue(scn.InFierceRound());
		assertEquals(13, scn.GetStrength(smaug));
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// At the first regroup decision he is himself again.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(17, scn.GetStrength(smaug));
		assertEquals(2, scn.GetKeywordCount(smaug, Keyword.DAMAGE));
	}

	@Test
	public void ThenDiscardingTwoOrcsTakesThisCardBackIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var awakening = scn.GetShadowCard("awakening");
		var awakening2 = scn.GetShadowCard("awakening2");
		var smaug = scn.GetShadowCard("smaug");
		var smaug2 = scn.GetShadowCard("smaug2");
		var sentinel = scn.GetShadowCard("sentinel");
		var aggressor = scn.GetShadowCard("aggressor");
		var troop = scn.GetShadowCard("troop");
		var bats = scn.GetShadowCard("bats");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(sentinel, aggressor, troop, bats);
		scn.SetShadowHand(awakening);
		scn.SetShadowDrawDeck(smaug2);
		scn.MoveCardsToDiscard(smaug, awakening2);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, sentinel, aggressor, troop, bats);
		assertInZone(Zone.DISCARD, smaug, awakening2);

		scn.ShadowPlayCard(awakening);
		scn.ShadowChoose("discard pile");
		assertInZone(Zone.SHADOW_CHARACTERS, smaug);

		assertTrue(scn.ShadowDecisionAvailable("discard 2 Orcs"));
		scn.ShadowChooseYes();
		// Orcs of three cultures; not the Bats, a Creature, nor Smaug, a Dragon.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sentinel, aggressor, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bats, smaug));
		assertEquals(2, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(sentinel, troop);

		assertInZone(Zone.DISCARD, sentinel, troop);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor, bats, smaug);
		assertInHand(awakening);
		// The copy that was not played stays where it was.
		assertInZone(Zone.DISCARD, awakening2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheTakeBackMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var awakening = scn.GetShadowCard("awakening");
		var smaug = scn.GetShadowCard("smaug");
		var smaug2 = scn.GetShadowCard("smaug2");
		var sentinel = scn.GetShadowCard("sentinel");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(sentinel, troop);
		scn.SetShadowHand(awakening);
		scn.SetShadowDrawDeck(smaug2);
		scn.MoveCardsToDiscard(smaug);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, sentinel, troop);
		assertInZone(Zone.DISCARD, smaug);

		scn.ShadowPlayCard(awakening);
		scn.ShadowChoose("discard pile");

		assertTrue(scn.ShadowDecisionAvailable("discard 2 Orcs"));
		scn.ShadowChooseNo();

		assertInZone(Zone.SHADOW_CHARACTERS, sentinel, troop, smaug);
		assertInZone(Zone.DISCARD, awakening);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneOrcTheTakeBackIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var awakening = scn.GetShadowCard("awakening");
		var smaug = scn.GetShadowCard("smaug");
		var smaug2 = scn.GetShadowCard("smaug2");
		var sentinel = scn.GetShadowCard("sentinel");
		var bats = scn.GetShadowCard("bats");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		// One Orc, and a minion that is no Orc.
		scn.MoveMinionsToTable(sentinel, bats);
		scn.SetShadowHand(awakening);
		scn.SetShadowDrawDeck(smaug2);
		scn.MoveCardsToDiscard(smaug);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, sentinel, bats);
		assertInZone(Zone.DISCARD, smaug);

		scn.ShadowPlayCard(awakening);
		scn.ShadowChoose("discard pile");

		assertInZone(Zone.SHADOW_CHARACTERS, smaug, sentinel, bats);
		assertInZone(Zone.DISCARD, awakening);
		assertFalse(scn.ShadowDecisionAvailable("discard 2 Orcs"));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
