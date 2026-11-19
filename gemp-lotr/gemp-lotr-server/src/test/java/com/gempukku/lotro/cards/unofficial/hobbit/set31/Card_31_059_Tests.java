package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_059_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  The Shadow phase of
	// turn 1 is at The Troll Hoard (site 2).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("campfire", "31_59");
					// Trolls to spot, of two cultures, and what is no Troll
					put("bert", "31_56");       // Bert, Troll of Ettenmoors: [troll] Troll, cost 5, "To play, discard an Orc"; his trigger answers discards of conditions
					put("demolition", "32_31"); // Demolition Troll: [moria] Troll; a skirmish ability not used
					put("sack", "31_57");       // Caught In A Sack: [troll] support-area condition, no Troll; a Regroup ability not used
					put("yazneg", "30_41");     // Yazneg, Orkish Assassin: [gundabad] Orc, cost 1; a maneuver ability not used
					// For the hand, a card of each kind, and a minion among them.  A choice from a pile is read by
					// blueprint, so none of these shares one with a card of the discard pile.
					put("footman", "30_42");    // Goblin Footman: [moria] Orc minion; a trigger on his own play
					put("scimitar", "31_31");   // Goblin Scimitar: [moria] hand weapon
					put("anger", "30_32");      // Anger: [gundabad] Skirmish event
					// For the discard pile
					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, cost 2, site 4; a Regroup ability
					put("ferny", "33_22");      // Bill Ferny Sr., Servant of Azog: [gundabad] Man, cost 2, site 2; texts on assigning him, a Regroup ability
					put("warg", "32_32");       // Vicious Warg: [moria] mount, a Shadow card that is no minion
					// Goblin Sneak: [moria] Orc; the Free Peoples player's copy lies in his own discard pile
					put("sneak", "31_32");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TrollCampfireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Troll Campfire
		 * Unique: True
		 * Side: Shadow
		 * Culture: Troll
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a Troll.
		 * 	<b>Shadow:</b> Discard 2 cards from hand to play a minion from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("campfire");

		assertEquals("Troll Campfire", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.TROLL, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilATrollIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var campfire = scn.GetShadowCard("campfire");
		var bert = scn.GetShadowCard("bert");
		var sack = scn.GetShadowCard("sack");
		var yazneg = scn.GetShadowCard("yazneg");
		scn.MoveCardsToHand(campfire, bert);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(yazneg);
		scn.MoveCardsToSupportArea(sack);
		assertInZone(Zone.SHADOW_CHARACTERS, yazneg);
		assertInZone(Zone.SUPPORT, sack);
		assertInHand(campfire, bert);
		scn.FreepsPass();

		// An Orc, and a [troll] card that is no Troll.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(campfire));

		// Bert discards the one Orc as he is played, with nothing asked.
		scn.ShadowPlayCard(bert);
		assertInZone(Zone.SHADOW_CHARACTERS, bert);
		assertInZone(Zone.DISCARD, yazneg);
		assertTrue(scn.ShadowPlayAvailable(campfire));
		scn.ShadowPlayCard(campfire);
		assertInZone(Zone.SUPPORT, campfire);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMoriaTrollIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var campfire = scn.GetShadowCard("campfire");
		var demolition = scn.GetShadowCard("demolition");
		scn.MoveCardsToHand(campfire);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(demolition);
		assertInZone(Zone.SHADOW_CHARACTERS, demolition);
		assertInHand(campfire);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(campfire));
		scn.ShadowPlayCard(campfire);
		assertInZone(Zone.SUPPORT, campfire);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionDiscards2CardsFromHandToPlayAMinionThatWasAlreadyInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var campfire = scn.GetShadowCard("campfire");
		var footman = scn.GetShadowCard("footman");
		var scimitar = scn.GetShadowCard("scimitar");
		var anger = scn.GetShadowCard("anger");
		var veteran = scn.GetShadowCard("veteran");
		var ferny = scn.GetShadowCard("ferny");
		var warg = scn.GetShadowCard("warg");
		var fpSneak = scn.GetFreepsCard("sneak");

		scn.StartGame();
		scn.MoveCardsToSupportArea(campfire);
		scn.MoveCardsToHand(footman, scimitar, anger);
		scn.MoveCardsToDiscard(veteran, ferny, warg);
		// A minion in the other player's discard pile, of a title the Shadow player's own discard pile does not hold.
		scn.MoveCardsToDiscard(fpSneak);
		scn.SetTwilight(20);
		assertInZone(Zone.SUPPORT, campfire);
		assertInHand(footman, scimitar, anger);
		assertInDiscard(veteran, ferny, warg, fpSneak);
		scn.FreepsPass();

		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(campfire));

		scn.ShadowUseCardAction(campfire);
		// Any 2 cards of the 3: a minion, a possession and an event.
		assertTrue(scn.ShadowHasCardChoiceAvailable(footman, scimitar, anger));
		assertEquals(2, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(footman, anger);
		assertInDiscard(footman, anger);
		assertInHand(scimitar);
		assertEquals(1, scn.GetShadowHandCount());

		// Minions of two cultures and two races that lay in the pile before the cost was paid; not the mount, not the
		// other player's minion, and not the Goblin Footman that the cost has just put there (Q-0320).
		assertTrue(scn.ShadowHasCardChoiceAvailable(veteran, ferny));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(warg, fpSneak, footman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(ferny);

		assertInZone(Zone.SHADOW_CHARACTERS, ferny);
		assertInDiscard(veteran, warg, footman, anger, fpSneak);
		assertInZone(Zone.SUPPORT, campfire);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionIsNotOfferedWithOneCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var campfire = scn.GetShadowCard("campfire");
		var anger = scn.GetShadowCard("anger");
		var veteran = scn.GetShadowCard("veteran");

		scn.StartGame();
		scn.MoveCardsToSupportArea(campfire);
		scn.MoveCardsToHand(anger);
		scn.MoveCardsToDiscard(veteran);
		scn.SetTwilight(20);
		assertInZone(Zone.SUPPORT, campfire);
		assertInHand(anger);
		assertInDiscard(veteran);
		scn.FreepsPass();

		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(campfire));
	}

	@Test
	public void ShadowActionIsNotOfferedWithNoMinionInTheDiscardPileThoughOneIsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var campfire = scn.GetShadowCard("campfire");
		var footman = scn.GetShadowCard("footman");
		var anger = scn.GetShadowCard("anger");
		var warg = scn.GetShadowCard("warg");
		var fpSneak = scn.GetFreepsCard("sneak");

		scn.StartGame();
		scn.MoveCardsToSupportArea(campfire);
		// The minion in hand would be discarded by the cost, and so cannot be the minion played (Q-0320).
		scn.MoveCardsToHand(footman, anger);
		scn.MoveCardsToDiscard(warg, fpSneak);
		scn.SetTwilight(20);
		assertInZone(Zone.SUPPORT, campfire);
		assertInHand(footman, anger);
		assertInDiscard(warg, fpSneak);
		scn.FreepsPass();

		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(campfire));
	}

	@Test
	public void ShadowActionIsNotOfferedUntilThePoolCanPayForTheMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var campfire = scn.GetShadowCard("campfire");
		var scimitar = scn.GetShadowCard("scimitar");
		var anger = scn.GetShadowCard("anger");
		var veteran = scn.GetShadowCard("veteran");

		scn.StartGame();
		scn.MoveCardsToSupportArea(campfire);
		scn.MoveCardsToHand(scimitar, anger);
		scn.MoveCardsToDiscard(veteran);
		assertInZone(Zone.SUPPORT, campfire);
		assertInHand(scimitar, anger);
		assertInDiscard(veteran);
		scn.FreepsPass();

		// The Orkish Veteran costs 2, and 2 for roaming (a site 4 minion at site 2): one short.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(campfire));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(campfire));
	}
}
