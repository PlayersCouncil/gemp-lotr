package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_122_Tests
{

	protected VirtualTableScenario GetCopiesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pit", "1_122");
					put("savage1", "1_151");    // Uruk Savage: [isengard] Uruk-hai, cost 2, site 5; the one played
					put("savage2", "1_151");    // the two copies in the discard pile
					put("savage3", "1_151");
					put("raiders", "1_158");    // Uruk-hai Raiding Party: an Uruk-hai of another title
					put("runner", "1_178");     // Goblin Runner: the one card of the Shadow player's draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetOtherCultureScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pit", "1_122");
					put("sentry1", "10_84");    // Cirith Ungol Sentry: a [sauron] Uruk-hai, damage +1 only; the one played
					put("sentry2", "10_84");    // the copy in the discard pile
					put("runner", "1_178");     // Goblin Runner: the one card of the Shadow player's draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetNoUrukHaiScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pit", "1_122");
					put("worker1", "3_62");     // Isengard Worker: an [isengard] Orc, no Uruk-hai; the one played
					put("worker2", "3_62");     // the copy in the discard pile
					put("runner", "1_178");     // Goblin Runner: the one card of the Shadow player's draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSameTitleScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pit", "1_122");
					put("leader", "4_160");     // Mauhur, Patrol Leader: [isengard] Uruk-hai, unique; the one played
					put("hunter", "15_164");    // Mauhur, Relentless Hunter: the same title, another card, in the discard pile
					put("runner", "1_178");     // Goblin Runner: the one card of the Shadow player's draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BreedingPitStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Breeding Pit
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: 
		 * Game Text: <b>Response:</b> If you play an Uruk-hai, take all copies of that card in your discard pile and place them beneath your draw deck.
		*/

		var scn = GetCopiesScenario();

		var card = scn.GetFreepsCard("pit");

		assertEquals("Breeding Pit", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenYouPlayAnUrukHaiPlacesEveryCopyOfItInYourDiscardPileBeneathYourDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCopiesScenario();

		var pit = scn.GetShadowCard("pit");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var savage3 = scn.GetShadowCard("savage3");
		var raiders = scn.GetShadowCard("raiders");
		var runner = scn.GetShadowCard("runner");
		// A copy in the other player's discard pile is not in "your discard pile".
		var freepsSavage = scn.GetFreepsCard("savage2");
		scn.MoveCardsToHand(pit, savage1);
		scn.MoveCardsToDiscard(savage2, savage3, raiders, freepsSavage);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(pit);
		assertInDiscard(savage2, savage3, raiders, freepsSavage);
		assertEquals(1, scn.GetShadowDeckCount());
		assertSame(runner, scn.GetShadowTopOfDeck());

		scn.ShadowPlayCard(savage1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pit));

		scn.ShadowChooseOptionalTrigger(pit);
		// All copies go: the only thing asked is which is placed first, of the two copies and nothing else.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(raiders));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(savage2);

		// Both copies are in the draw deck beneath the card that was there.
		assertEquals(3, scn.GetShadowDeckCount());
		assertSame(runner, scn.GetShadowTopOfDeck());
		assertInZone(Zone.DECK, savage2, savage3);
		assertInZone(Zone.DISCARD, raiders, pit, freepsSavage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnUrukHaiOfAnotherCulturePlayedHasItsCopyPlacedBeneathTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOtherCultureScenario();

		var pit = scn.GetShadowCard("pit");
		var sentry1 = scn.GetShadowCard("sentry1");
		var sentry2 = scn.GetShadowCard("sentry2");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(pit, sentry1);
		scn.MoveCardsToDiscard(sentry2);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(pit);
		assertEquals(1, scn.GetShadowDeckCount());
		assertInDiscard(sentry2);

		scn.ShadowPlayCard(sentry1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pit));

		scn.ShadowChooseOptionalTrigger(pit);
		assertEquals(2, scn.GetShadowDeckCount());
		assertSame(runner, scn.GetShadowTopOfDeck());
		assertSame(sentry2, scn.GetShadowBottomOfDeck());
		assertInDiscard(pit);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAMinionThatIsNoUrukHaiIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNoUrukHaiScenario();

		var pit = scn.GetShadowCard("pit");
		var worker1 = scn.GetShadowCard("worker1");
		var worker2 = scn.GetShadowCard("worker2");
		scn.MoveCardsToHand(pit, worker1);
		scn.MoveCardsToDiscard(worker2);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(pit);
		assertEquals(1, scn.GetShadowDeckCount());
		assertInDiscard(worker2);

		scn.ShadowPlayCard(worker1);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pit));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(pit);
		assertInDiscard(worker2);
		assertEquals(1, scn.GetShadowDeckCount());
	}

	@Test
	public void ACardOfTheSameTitleIsACopyOfThatCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSameTitleScenario();

		var pit = scn.GetShadowCard("pit");
		var leader = scn.GetShadowCard("leader");
		var hunter = scn.GetShadowCard("hunter");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(pit, leader);
		scn.MoveCardsToDiscard(hunter);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(pit);
		assertEquals(1, scn.GetShadowDeckCount());
		assertInDiscard(hunter);

		scn.ShadowPlayCard(leader);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pit));

		scn.ShadowChooseOptionalTrigger(pit);
		// "Copies of that card" goes by title (Q-0092).
		assertEquals(2, scn.GetShadowDeckCount());
		assertSame(runner, scn.GetShadowTopOfDeck());
		assertSame(hunter, scn.GetShadowBottomOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoCopyInTheDiscardPileItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCopiesScenario();

		var pit = scn.GetShadowCard("pit");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var savage3 = scn.GetShadowCard("savage3");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToHand(pit, savage1);
		// Only an Uruk-hai of another title in the discard pile; the other two Savages stay out of the way.
		scn.MoveCardsToDiscard(raiders);

		scn.StartGame();
		scn.MoveCardsToBottomOfDeck(savage2, savage3);
		scn.SetTwilight(3);
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(pit);
		assertEquals(3, scn.GetShadowDeckCount());
		assertSame(savage3, scn.GetShadowBottomOfDeck());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertInDiscard(raiders);

		scn.ShadowPlayCard(savage1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pit));

		scn.ShadowChooseOptionalTrigger(pit);
		assertInDiscard(pit, raiders);
		assertEquals(3, scn.GetShadowDeckCount());
		assertSame(savage3, scn.GetShadowBottomOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
