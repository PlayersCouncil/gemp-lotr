package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_034_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot.  The Wolves Army is played in the Shadow phase at Goblin Town 30_52 (site 4), its own site number, so
	// that no minion roams there.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wolves", "32_34");
					// [moria] cards for the piles: a character, an item and a card that is neither.
					put("bats", "32_30");        // Dark Bats: [moria] Creature, cost 1; a Shadow ability, not used
					put("mace", "32_29");        // Azog's Mace: unique [moria] hand weapon, cost 1, bearer must be an Orc (the Wolves Army); a Shadow ability, not used
					put("swarms", "31_34");      // Goblin Swarms: [moria] condition, cost 1, to play spot a [moria] card; abilities on stacked Orcs, none here
					put("swarms2", "31_34");
					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc, cost 6, no text
					// Cards of other cultures.
					put("aggressor", "30_39");   // Orkish Aggressor: [gundabad] Orc, cost 2; an Assignment ability, not used
					put("aggressor2", "30_39");
					put("jail", "32_39");        // Jail: [sauron] condition, cost 2; a Regroup ability, not used
					// A [moria] card for the other player's own discard pile, of a blueprint this player's piles do not hold.
					put("footman", "30_42");     // Goblin Footman: [moria] Orc
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TheWolvesArmyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: The Wolves Army
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: When you play this minion, you may play a [moria] card from your draw deck or discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wolves");

		assertEquals("The Wolves Army", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedMayPlayAMoriaCardFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wolves = scn.GetShadowCard("wolves");
		var bats = scn.GetShadowCard("bats");
		var mace = scn.GetShadowCard("mace");
		var swarms = scn.GetShadowCard("swarms");
		var swarms2 = scn.GetShadowCard("swarms2");
		var aggressor2 = scn.GetShadowCard("aggressor2");
		var footman = scn.GetFreepsCard("footman");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();
		scn.SetShadowHand(wolves);
		scn.SetShadowDrawDeck(swarms);
		scn.MoveCardsToDiscard(bats, mace, swarms2, aggressor2, footman);
		scn.SetTwilight(15);
		scn.ShadowRefreshDecision();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertInHand(wolves);
		assertInZone(Zone.DECK, swarms);
		assertInZone(Zone.DISCARD, bats, mace, swarms2, aggressor2, footman);
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(wolves);
		assertInZone(Zone.SHADOW_CHARACTERS, wolves);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wolves));
		scn.ShadowAcceptOptionalTrigger();

		// A [moria] card in each pile: the player is asked which.
		assertTrue(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowChoose("discard pile");
		// A character, an item and a condition, all [moria]; not a [gundabad] card, nor the other player's [moria]
		// card in his own discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(bats, mace, swarms2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aggressor2, footman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(bats);

		assertInZone(Zone.SHADOW_CHARACTERS, bats);
		assertInZone(Zone.DISCARD, mace, swarms2, aggressor2, footman);
		assertInZone(Zone.DECK, swarms);
		// No deck was looked through.
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAMoriaCardOnlyInTheDrawDeckItIsPlayedFromThereAndTheDeckShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wolves = scn.GetShadowCard("wolves");
		var swarms = scn.GetShadowCard("swarms");
		var bats = scn.GetShadowCard("bats");
		var mace = scn.GetShadowCard("mace");
		var aggressor = scn.GetShadowCard("aggressor");
		var aggressor2 = scn.GetShadowCard("aggressor2");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();
		scn.SetShadowHand(wolves);
		scn.SetShadowDrawDeck(swarms, bats, mace, aggressor);
		scn.MoveCardsToDiscard(aggressor2);
		scn.SetTwilight(15);
		scn.ShadowRefreshDecision();
		assertInHand(wolves);
		assertInZone(Zone.DECK, swarms, bats, mace, aggressor);
		assertInZone(Zone.DISCARD, aggressor2);
		int shuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.ShadowPlayCard(wolves);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wolves));
		scn.ShadowAcceptOptionalTrigger();

		// Nothing in the discard pile can be played: the deck is looked through unasked.
		scn.DismissRevealedCards();
		// A condition, a character and an item, all [moria]; not a [gundabad] card.
		assertTrue(scn.ShadowHasCardChoiceAvailable(swarms, bats, mace));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aggressor));
		scn.ShadowChooseCardBPFromSelection(swarms);

		assertInZone(Zone.SUPPORT, swarms);
		assertInZone(Zone.DECK, bats, mace, aggressor);
		assertInZone(Zone.DISCARD, aggressor2);
		// One shuffle for the card leaving the deck that was looked through, one for the look itself.
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesBothPilesAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wolves = scn.GetShadowCard("wolves");
		var bats = scn.GetShadowCard("bats");
		var swarms = scn.GetShadowCard("swarms");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();
		scn.SetShadowHand(wolves);
		scn.SetShadowDrawDeck(swarms);
		scn.MoveCardsToDiscard(bats);
		scn.SetTwilight(15);
		scn.ShadowRefreshDecision();
		assertInHand(wolves);
		assertInZone(Zone.DECK, swarms);
		assertInZone(Zone.DISCARD, bats);
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(wolves);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wolves));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, wolves);
		assertInZone(Zone.DECK, swarms);
		assertInZone(Zone.DISCARD, bats);
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherMoriaOrcPlayedAfterItIsOfferedNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wolves = scn.GetShadowCard("wolves");
		var troop = scn.GetShadowCard("troop");
		var bats = scn.GetShadowCard("bats");
		var swarms = scn.GetShadowCard("swarms");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();
		scn.MoveMinionsToTable(wolves);
		scn.SetShadowHand(troop);
		scn.SetShadowDrawDeck(swarms);
		scn.MoveCardsToDiscard(bats);
		scn.SetTwilight(15);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, wolves);
		assertInHand(troop);
		assertInZone(Zone.DECK, swarms);
		assertInZone(Zone.DISCARD, bats);

		scn.ShadowPlayCard(troop);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DECK, swarms);
		assertInZone(Zone.DISCARD, bats);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NothingIsOfferedWithNoMoriaCardInEitherPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wolves = scn.GetShadowCard("wolves");
		var aggressor = scn.GetShadowCard("aggressor");
		var aggressor2 = scn.GetShadowCard("aggressor2");
		var jail = scn.GetShadowCard("jail");
		var footman = scn.GetFreepsCard("footman");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();
		// [gundabad] and [sauron] cards in this player's piles, and a [moria] card in the other player's own discard pile.
		scn.SetShadowHand(wolves);
		scn.SetShadowDrawDeck(aggressor, jail);
		scn.MoveCardsToDiscard(aggressor2, footman);
		scn.SetTwilight(15);
		scn.ShadowRefreshDecision();
		assertInHand(wolves);
		assertInZone(Zone.DECK, aggressor, jail);
		assertInZone(Zone.DISCARD, aggressor2, footman);

		scn.ShadowPlayCard(wolves);
		assertInZone(Zone.SHADOW_CHARACTERS, wolves);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NothingIsOfferedWithOneTwilightTooFewForTheOnlyMoriaCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wolves = scn.GetShadowCard("wolves");
		var troop = scn.GetShadowCard("troop");
		var jail = scn.GetShadowCard("jail");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();
		scn.SetShadowHand(wolves);
		scn.SetShadowDrawDeck(jail);
		scn.MoveCardsToDiscard(troop);
		// 5 for the Wolves Army leaves 5, one short of the Troop's 6.
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInHand(wolves);
		assertInZone(Zone.DECK, jail);
		assertInZone(Zone.DISCARD, troop);

		scn.ShadowPlayCard(wolves);
		assertInZone(Zone.SHADOW_CHARACTERS, wolves);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAMoriaCardOnlyInTheDiscardPileItIsPlayedFromThereUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wolves = scn.GetShadowCard("wolves");
		var troop = scn.GetShadowCard("troop");
		var jail = scn.GetShadowCard("jail");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();
		scn.SetShadowHand(wolves);
		scn.SetShadowDrawDeck(jail);
		scn.MoveCardsToDiscard(troop);
		// 5 for the Wolves Army leaves exactly the Troop's 6.
		scn.SetTwilight(11);
		scn.ShadowRefreshDecision();
		assertInHand(wolves);
		assertInZone(Zone.DECK, jail);
		assertInZone(Zone.DISCARD, troop);
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(wolves);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wolves));
		scn.ShadowAcceptOptionalTrigger();

		// The one [moria] card in the one pile that holds any is played unasked.
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.DECK, jail);
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
