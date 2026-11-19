package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_032_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  The Shadow phase of
	// turn 1 is at The Troll Hoard 30_50 (site 2, underground); site 3 is Rivendell 30_51 (a sanctuary, not
	// underground), whose only text bars Trolls.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sneak", "31_32");
					put("sneak2", "31_32");

					put("footman", "30_42");    // Goblin Footman: [moria] Orc; a "when you play" offer of a possession from the discard pile
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, cost 2; an Assignment ability not used
					put("assaulting", "32_27"); // Assaulting Goblin: [moria] Orc; a response at a battleground only
					put("bats", "32_30");       // Dark Bats: [moria] Creature, no Orc; a Shadow ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GoblinSneakStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Goblin Sneak
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: When you play this minion, you may place an Orc (or 2 Orcs at an underground site) from your discard pile beneath your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sneak");

		assertEquals("Goblin Sneak", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtAnUndergroundSiteYouMayPlaceUpToTwoOrcsOfAnyCultureBeneathYourDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sneak = scn.GetShadowCard("sneak");
		var footman = scn.GetShadowCard("footman");
		var aggressor = scn.GetShadowCard("aggressor");
		var assaulting = scn.GetShadowCard("assaulting");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCardsToHand(sneak);
		scn.MoveCardsToDiscard(footman, aggressor, assaulting, bats);

		scn.StartGame();
		assertInZone(Zone.DISCARD, footman, aggressor, assaulting, bats);
		scn.FreepsPass();

		// The Troll Hoard is underground.
		assertTrue(scn.GetCurrentSite().getBlueprint().hasKeyword(Keyword.UNDERGROUND));
		var deck = scn.GetShadowDeckCount();
		scn.ShadowPlayCard(sneak);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak));
		scn.ShadowAcceptOptionalTrigger();

		// Q-0339: a "may" with a parenthesis is 0 to 2 Orcs of any culture where the parenthesis holds.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(footman, aggressor, assaulting));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bats));
		scn.ShadowChooseCards(footman, aggressor);
		// Which goes beneath first: the Footman, so the Aggressor is the bottom card.
		scn.ShadowChooseCards(footman);

		assertEquals(aggressor, scn.GetShadowBottomOfDeck());
		assertEquals(footman, scn.GetFromBottomOfShadowDeck(2));
		assertEquals(deck + 2, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, assaulting, bats);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtAnUndergroundSiteALoneOrcInTheDiscardPileMayStillBePlaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sneak = scn.GetShadowCard("sneak");
		var footman = scn.GetShadowCard("footman");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCardsToHand(sneak);
		scn.MoveCardsToDiscard(footman, bats);

		scn.StartGame();
		assertInZone(Zone.DISCARD, footman, bats);
		scn.FreepsPass();

		var deck = scn.GetShadowDeckCount();
		scn.ShadowPlayCard(sneak);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak));
		scn.ShadowAcceptOptionalTrigger();

		// "Or 2 Orcs" is up to 2 (Q-0339): with one Orc there, that one may go.
		assertTrue(scn.ShadowHasCardChoiceAvailable(footman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bats));
		scn.ShadowChooseCards(footman);

		assertEquals(footman, scn.GetShadowBottomOfDeck());
		assertEquals(deck + 1, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, bats);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AwayFromAnUndergroundSiteOneOrcOfAnyCultureIsPlacedBeneathYourDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sneak = scn.GetShadowCard("sneak");
		var footman = scn.GetShadowCard("footman");
		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCardsToHand(sneak);
		scn.MoveCardsToDiscard(footman, aggressor, bats);

		scn.StartGame();
		assertInZone(Zone.DISCARD, footman, aggressor, bats);
		// The Troll Hoard makes no start-of-fellowship offer: no hand weapon in the deck (F-0800).
		scn.SkipToSite(2);
		// Enough for the Sneak at Rivendell, whose Shadow number is 1; not read.
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals("Rivendell", scn.GetCurrentSite().getBlueprint().getTitle());
		assertFalse(scn.GetCurrentSite().getBlueprint().hasKeyword(Keyword.UNDERGROUND));
		var deck = scn.GetShadowDeckCount();
		scn.ShadowPlayCard(sneak);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak));
		scn.ShadowAcceptOptionalTrigger();

		// One Orc at most, of either culture; the Bats are no Orc.
		assertEquals(1, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(footman, aggressor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bats));
		scn.ShadowChooseCards(aggressor);

		assertEquals(aggressor, scn.GetShadowBottomOfDeck());
		assertEquals(deck + 1, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, footman, bats);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AwayFromAnUndergroundSiteTheChoiceAfterTheYesMayBeLeftEmpty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sneak = scn.GetShadowCard("sneak");
		var footman = scn.GetShadowCard("footman");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToHand(sneak);
		scn.MoveCardsToDiscard(footman, aggressor);

		scn.StartGame();
		assertInZone(Zone.DISCARD, footman, aggressor);
		scn.SkipToSite(2);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals("Rivendell", scn.GetCurrentSite().getBlueprint().getTitle());
		var deck = scn.GetShadowDeckCount();
		scn.ShadowPlayCard(sneak);
		scn.ShadowAcceptOptionalTrigger();

		// Q-0339: a "may" with a parenthesis is a choice of 0 to 1 of the first noun.
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();

		assertInZone(Zone.DISCARD, footman, aggressor);
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesTheDiscardPileAndTheDrawDeckAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sneak = scn.GetShadowCard("sneak");
		var footman = scn.GetShadowCard("footman");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToHand(sneak);
		scn.MoveCardsToDiscard(footman, aggressor);

		scn.StartGame();
		assertInZone(Zone.DISCARD, footman, aggressor);
		scn.FreepsPass();

		var deck = scn.GetShadowDeckCount();
		var bottom = scn.GetShadowBottomOfDeck();
		scn.ShadowPlayCard(sneak);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, footman, aggressor);
		assertEquals(deck, scn.GetShadowDeckCount());
		assertEquals(bottom, scn.GetShadowBottomOfDeck());
		assertInZone(Zone.SHADOW_CHARACTERS, sneak);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnswersItsOwnPlayOnlyAndNotAnotherOrcsOrAnotherCopys() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sneak = scn.GetShadowCard("sneak");
		var sneak2 = scn.GetShadowCard("sneak2");
		var aggressor = scn.GetShadowCard("aggressor");
		var footman = scn.GetShadowCard("footman");
		scn.MoveCardsToHand(aggressor, sneak2);
		scn.MoveCardsToDiscard(footman);

		scn.StartGame();
		assertInZone(Zone.DISCARD, footman);
		scn.MoveMinionsToTable(sneak);
		// Enough for the Aggressor and the second Sneak, each roaming at site 2; not read.
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, sneak);
		// Another Orc played: the Sneak in play makes no offer, though the Footman lies in the discard pile.
		scn.ShadowPlayCard(aggressor);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second copy played: its own offer only.
		scn.ShadowPlayCard(sneak2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(sneak));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, footman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoOrcInYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sneak = scn.GetShadowCard("sneak");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCardsToHand(sneak);
		scn.MoveCardsToDiscard(bats);

		scn.StartGame();
		assertInZone(Zone.DISCARD, bats);
		scn.FreepsPass();

		scn.ShadowPlayCard(sneak);

		// Only the Bats, which is no Orc: nothing to place, and no offer.
		assertInZone(Zone.SHADOW_CHARACTERS, sneak);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, bats);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
