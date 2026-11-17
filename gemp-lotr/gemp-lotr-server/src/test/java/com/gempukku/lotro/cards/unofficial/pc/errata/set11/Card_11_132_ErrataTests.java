package com.gempukku.lotro.cards.unofficial.pc.errata.set11;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_11_132_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smith1", "61_132");
					put("smith2", "61_132");
					put("denizen", "11_116");      // Denizen of Moria: an [orc] Orc, cost 2; stronger at an underground site and nothing else

					// [orc] conditions for the discard pile. None is ever in play here, so their texts do nothing.
					put("spurred", "11_139");      // Spurred to Battle: support area, cost 1, not unique
					put("hordes", "11_123");       // Goblin Hordes: support area, cost 2, not unique
					put("blow", "15_115");         // Pummeling Blow: unique, cost 0, plays on the fellowship's current site
					put("retribution", "12_101");  // Retribution: unique, support area, cost 0

					// Cards that are nearly [orc] conditions in a discard pile.
					put("scimitar", "11_128");     // Mordor Scimitar: an [orc] possession
					put("halls", "11_112");        // Conquered Halls: an [orc] event
					put("wings", "11_208");        // Dark Wings: a [wraith] support-area condition

					// [orc] conditions that lie somewhere else.
					put("effort", "15_106");       // Coordinated Effort: adds (2) each time the Free Peoples player plays a possession; none is played here
					put("demoralized", "11_114");  // Demoralized: stays in hand
					put("cost", "12_88");          // Great Cost: lies in the other player's discard pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishSmithStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Orkish Smith
		 * Unique: false
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: When you play this minion, you may shuffle up to 2 [orc] conditions from your discard pile
		 * 	into your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetShadowCard("smith1");

		assertEquals("Orkish Smith", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedForTwoPlusRoamingAndItsOfferMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smith = scn.GetShadowCard("smith1");
		var spurred = scn.GetShadowCard("spurred");
		scn.MoveCardsToHand(smith);
		scn.MoveCardsToDiscard(spurred);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		int pool = scn.GetTwilight();
		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.ShadowPlayAvailable(smith));

		scn.ShadowPlayCard(smith);

		assertInZone(Zone.SHADOW_CHARACTERS, smith);
		// 2 to play + 2 roaming: his site number is 4 and the fellowship is at site 2
		assertEquals(pool - 4, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(smith));

		scn.ShadowDeclineOptionalTrigger();

		assertInDiscard(spurred);
		assertEquals(deck, scn.GetShadowDeckCount());
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.SHADOW_CHARACTERS, smith);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedItMayShuffleTwoOrcConditionsFromTheDiscardPileIntoTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smith = scn.GetShadowCard("smith1");
		var discardedSmith = scn.GetShadowCard("smith2");
		var spurred = scn.GetShadowCard("spurred");
		var hordes = scn.GetShadowCard("hordes");
		var blow = scn.GetShadowCard("blow");
		var retribution = scn.GetShadowCard("retribution");
		var scimitar = scn.GetShadowCard("scimitar");
		var halls = scn.GetShadowCard("halls");
		var wings = scn.GetShadowCard("wings");
		var effort = scn.GetShadowCard("effort");
		var demoralized = scn.GetShadowCard("demoralized");
		var opponentsCost = scn.GetFreepsCard("cost");
		// An [orc] condition in play, one in hand, and one in the other player's discard pile.
		scn.MoveCardsToSupportArea(effort);
		scn.MoveCardsToHand(smith, demoralized);
		scn.MoveCardsToDiscard(opponentsCost);
		// Four [orc] conditions, unique and not, of the support area and of a site; and an [orc] minion, an [orc]
		// possession, an [orc] event and a [wraith] condition.
		scn.MoveCardsToDiscard(spurred, hordes, blow, retribution, discardedSmith, scimitar, halls, wings);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		scn.ShadowPlayCard(smith);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(smith));

		scn.ShadowChooseOptionalTrigger(smith);

		assertTrue(scn.ShadowHasCardChoiceAvailable(spurred, hordes, blow, retribution));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(discardedSmith, scimitar, halls, wings, effort, demoralized, opponentsCost));
		assertEquals(0, scn.ShadowGetChoiceMin());
		// Four could be taken; two is the most.
		assertEquals(2, scn.ShadowGetChoiceMax());
		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();
		int opponentsDeck = scn.GetFreepsDeckCount();
		int opponentsShuffles = scn.GetFreepsDeckShuffleCount();

		// One from the support area and the one that plays on a site
		scn.ShadowChooseCards(spurred, blow);

		assertInZone(Zone.DECK, spurred);
		assertInZone(Zone.DECK, blow);
		assertInDiscard(hordes);
		assertInDiscard(retribution);
		assertInDiscard(discardedSmith);
		assertInDiscard(scimitar);
		assertInDiscard(halls);
		assertInDiscard(wings);
		assertEquals(deck + 2, scn.GetShadowDeckCount());
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(opponentsDeck, scn.GetFreepsDeckCount());
		assertEquals(opponentsShuffles, scn.GetFreepsDeckShuffleCount());
		assertInZone(Zone.SUPPORT, effort);
		assertInZone(Zone.HAND, demoralized);
		assertInDiscard(opponentsCost);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OneConditionMayBeTakenOrNoneAndTakingNoneStillShufflesTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smith1 = scn.GetShadowCard("smith1");
		var smith2 = scn.GetShadowCard("smith2");
		var spurred = scn.GetShadowCard("spurred");
		var hordes = scn.GetShadowCard("hordes");
		var blow = scn.GetShadowCard("blow");
		scn.MoveCardsToHand(smith1, smith2);
		scn.MoveCardsToDiscard(spurred, hordes, blow);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(smith1);
		scn.ShadowChooseOptionalTrigger(smith1);
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());

		// One of the three
		scn.ShadowChooseCards(hordes);

		assertInZone(Zone.DECK, hordes);
		assertInDiscard(spurred);
		assertInDiscard(blow);
		assertEquals(deck + 1, scn.GetShadowDeckCount());
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The offer taken and then no card chosen: nothing goes into the draw deck, and the deck is shuffled
		// all the same.
		int opponentsShuffles = scn.GetFreepsDeckShuffleCount();
		scn.ShadowPlayCard(smith2);
		scn.ShadowChooseOptionalTrigger(smith2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(spurred, blow));
		scn.ShadowDeclineChoosing();

		assertInDiscard(spurred);
		assertInDiscard(blow);
		assertEquals(deck + 1, scn.GetShadowDeckCount());
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(opponentsShuffles, scn.GetFreepsDeckShuffleCount());
		assertInZone(Zone.SHADOW_CHARACTERS, smith2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoOrcConditionInTheDiscardPileTheOfferIsStillMadeAndTakingItShufflesTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smith = scn.GetShadowCard("smith1");
		var discardedSmith = scn.GetShadowCard("smith2");
		var scimitar = scn.GetShadowCard("scimitar");
		var wings = scn.GetShadowCard("wings");
		var effort = scn.GetShadowCard("effort");
		var demoralized = scn.GetShadowCard("demoralized");
		// An [orc] condition in play and one in hand; in the discard pile an [orc] minion, an [orc] possession
		// and a [wraith] condition.
		scn.MoveCardsToSupportArea(effort);
		scn.MoveCardsToHand(smith, demoralized);
		scn.MoveCardsToDiscard(discardedSmith, scimitar, wings);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();
		int opponentsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.ShadowPlayCard(smith);

		assertInZone(Zone.SHADOW_CHARACTERS, smith);
		// There is nothing to shuffle in, and the offer does something all the same: it shuffles the draw deck.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(smith));

		scn.ShadowChooseOptionalTrigger(smith);

		assertInDiscard(discardedSmith);
		assertInDiscard(scimitar);
		assertInDiscard(wings);
		assertInZone(Zone.SUPPORT, effort);
		assertInZone(Zone.HAND, demoralized);
		assertEquals(deck, scn.GetShadowDeckCount());
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(opponentsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherOrcMinionPlayedMakesNoOfferAndASecondSmithsOfferIsItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smith = scn.GetShadowCard("smith1");
		var earlierSmith = scn.GetShadowCard("smith2");
		var denizen = scn.GetShadowCard("denizen");
		var spurred = scn.GetShadowCard("spurred");
		var hordes = scn.GetShadowCard("hordes");
		scn.MoveCardsToHand(smith, denizen);
		scn.MoveCardsToDiscard(spurred, hordes);

		scn.StartGame();
		scn.MoveMinionsToTable(earlierSmith);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		int shuffles = scn.GetShadowDeckShuffleCount();

		// Another [orc] Orc is played while a Smith is in play.
		scn.ShadowPlayCard(denizen);

		assertInZone(Zone.SHADOW_CHARACTERS, denizen);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(spurred);
		assertInDiscard(hordes);
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second Smith is played: the copy played makes the offer and the copy in play makes none.
		scn.ShadowPlayCard(smith);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(smith));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(earlierSmith));

		scn.ShadowChooseOptionalTrigger(smith);
		assertTrue(scn.ShadowHasCardChoiceAvailable(spurred, hordes));
		scn.ShadowChooseCards(spurred);

		assertInZone(Zone.DECK, spurred);
		assertInDiscard(hordes);
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
