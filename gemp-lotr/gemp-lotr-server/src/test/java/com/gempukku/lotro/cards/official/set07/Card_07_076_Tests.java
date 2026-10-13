package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_076_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("friends", "7_76");

					// Sméagol, Slinker: [gollum] companion, twilight cost 0.  "To play, add a burden."  His skirmish
					// ability is never used here.
					put("smeagol", "5_29");
					// Sméagol, Poor Creature: another [gollum] Sméagol, cost 0, "To play, add a burden."
					put("smeagol2", "6_45");
					// Smeagol, Once Like Us: the [shire] Sméagol, twilight cost 1.  "To play, add a burden."
					put("shiresmeagol", "103_116");
					// Gollum, Nasty Treacherous Creature: the player's own [gollum] minion, a card of another title
					put("gollum", "5_24");

					// A hand of three kinds of card.
					put("dwarf", "1_7");        // Dwarf Guard, a companion
					put("armor", "1_8");        // Dwarven Armor, a possession
					put("strike", "1_3");       // Axe Strike, an event

					// Dwarf Guards for the draw deck, so that a reconcile has eight cards to draw.
					put("deck1", "1_7");
					put("deck2", "1_7");
					put("deck3", "1_7");
					put("deck4", "1_7");
					put("deck5", "1_7");
					put("deck6", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VeryNiceFriendsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Very Nice Friends
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Play Sméagol from your discard pile to play the fellowship's next site
		 *  (replacing an opponent's site if necessary). If you do not move again this turn, discard your hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("friends");

		assertEquals("Very Nice Friends", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void VeryNiceFriendsRequiresSmeagolInDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var smeagol = scn.GetFreepsCard("smeagol");
		var gollum = scn.GetFreepsCard("gollum");
		scn.MoveCardsToHand(friends, smeagol);
		// His own Gollum lies in his discard pile, and the other player's Sméagol in the other player's.
		scn.MoveCardsToDiscard(gollum, scn.GetShadowCard("smeagol"));

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.HAND, friends, smeagol);
		assertInZone(Zone.DISCARD, gollum);
		assertFalse(scn.FreepsPlayAvailable(friends));

		scn.MoveCardsToDiscard(smeagol);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(friends));
	}

	@Test
	public void NotPlayableWhileTheSmeagolInTheDiscardPileCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var smeagol = scn.GetFreepsCard("smeagol");
		var smeagol2 = scn.GetFreepsCard("smeagol2");
		scn.MoveCardsToHand(friends);
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToDiscard(smeagol2);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// A Sméagol is in play: the one in the discard pile cannot be played, so the event's cost cannot be paid.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.DISCARD, smeagol2);
		assertFalse(scn.FreepsPlayAvailable(friends));

		scn.MoveCardsToDiscard(smeagol);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(friends));
	}

	@Test
	public void VeryNiceFriendsPlaysSmeagolFromDiscardToPlayNextSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var site3 = scn.GetFreepsSite(3);
		var shadowSite3 = scn.GetShadowSite(3);
		var friends = scn.GetFreepsCard("friends");
		var smeagol = scn.GetFreepsCard("smeagol");
		var shiresmeagol = scn.GetFreepsCard("shiresmeagol");
		var gollum = scn.GetFreepsCard("gollum");
		scn.MoveCardsToHand(friends);
		scn.MoveCardsToDiscard(smeagol, shiresmeagol, gollum);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsPlayAvailable(friends));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_DECK, site3, shadowSite3);
		assertEquals(0, scn.GetBurdens());
		int pool = scn.GetTwilight();

		scn.FreepsPlayCard(friends);

		// Either Sméagol in his discard pile, and not his Gollum.
		assertTrue(scn.FreepsHasCardChoiceAvailable(smeagol, shiresmeagol));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gollum));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(smeagol);

		// Sméagol is played, for his own cost: (0) and a burden.
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertInZone(Zone.DISCARD, shiresmeagol, gollum);
		assertEquals(1, scn.GetBurdens());
		assertEquals(pool, scn.GetTwilight());

		// His own site 3 is laid ahead of the fellowship, which stays where it is.
		assertSame(site3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_PATH, site3);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite3);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DISCARD, friends);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheShireSmeagolIsPlayedByItAndTheOtherPlayersSiteIsReplaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var site5 = scn.GetFreepsSite(5);
		var shadowSite5 = scn.GetShadowSite(5);
		var friends = scn.GetFreepsCard("friends");
		var shiresmeagol = scn.GetFreepsCard("shiresmeagol");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsHand(friends);
		scn.MoveCardsToDiscard(shiresmeagol);

		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(4, scn.GetCurrentSiteNumber());

		// The other player's site 5 lies next, as it does where he has played his next site himself.  Both
		// players hold the same nine sites here, so the site that replaces it is its twin.
		scn.MoveCardToAdventurePath(shadowSite5);
		assertSame(shadowSite5, scn.GetSite(5));
		assertInZone(Zone.ADVENTURE_DECK, site5);
		assertEquals(0, scn.GetBurdens());
		int pool = scn.GetTwilight();

		assertTrue(scn.FreepsPlayAvailable(friends));
		scn.FreepsPlayCard(friends);

		// This Sméagol is a [shire] companion of twilight cost 1.
		assertInZone(Zone.FREE_CHARACTERS, shiresmeagol);
		assertEquals(1, scn.GetBurdens());
		assertEquals(pool + 1, scn.GetTwilight());

		// His own site 5 lies on the path, and the other player's is back in its owner's adventure deck.
		assertSame(site5, scn.GetSite(5));
		assertInZone(Zone.ADVENTURE_PATH, site5);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite5);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void MovingDuringRegroupKeepsHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var smeagol = scn.GetFreepsCard("smeagol");
		var dwarf = scn.GetFreepsCard("dwarf");
		var armor = scn.GetFreepsCard("armor");
		var strike = scn.GetFreepsCard("strike");
		scn.MoveCardsToHand(friends, dwarf, armor, strike);
		scn.MoveCardsToDiscard(smeagol);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPlayCard(friends);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(3, scn.GetFreepsHandCount());
		scn.ShadowPass();
		scn.FreepsPass();

		assertTrue(scn.FreepsDecisionAvailable("Do you want to make another move?"));
		scn.FreepsChooseToMove();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.HAND, dwarf, armor, strike);

		// He has moved again: at the end of this turn's last regroup phase the hand is still his to reconcile.
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.PassReconcile(VirtualTableScenario.P2);
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		assertInZone(Zone.HAND, dwarf, armor, strike);
		assertEquals(3, scn.GetFreepsHandCount());
	}

	@Test
	public void StayingDuringRegroupDiscardsHandBeforeReconcile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var smeagol = scn.GetFreepsCard("smeagol");
		var dwarf = scn.GetFreepsCard("dwarf");
		var armor = scn.GetFreepsCard("armor");
		var strike = scn.GetFreepsCard("strike");
		scn.MoveCardsToHand(friends, dwarf, armor, strike);
		scn.MoveCardsToDiscard(smeagol);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPlayCard(friends);
		assertEquals(3, scn.GetFreepsHandCount());
		scn.ShadowPass();
		scn.FreepsPass();

		assertTrue(scn.FreepsDecisionAvailable("Do you want to make another move?"));
		assertInZone(Zone.HAND, dwarf, armor, strike);
		// Nine cards are left in his draw deck.
		assertEquals(9, scn.GetFreepsDeckCount());
		int shadowHand = scn.GetShadowHandCount();

		scn.FreepsChooseToStay();

		// The companion, the possession and the event are all discarded, and then he reconciles: with no card
		// in hand he draws eight.
		assertInZone(Zone.DISCARD, dwarf, armor, strike);
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void AtTheMoveLimitTheHandIsDiscardedThoughNoMoveIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var smeagol = scn.GetFreepsCard("smeagol");
		var dwarf = scn.GetFreepsCard("dwarf");
		var armor = scn.GetFreepsCard("armor");
		var strike = scn.GetFreepsCard("strike");
		scn.MoveCardsToHand(friends, dwarf, armor, strike);
		scn.MoveCardsToDiscard(smeagol);

		scn.StartGame();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.REGROUP);

		// The second move of two: no other move can follow.
		assertEquals(2, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.FreepsPlayAvailable(friends));
		scn.FreepsPlayCard(friends);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertInZone(Zone.HAND, dwarf, armor, strike);

		scn.ShadowPass();
		scn.FreepsPass();
		scn.PassReconcile(VirtualTableScenario.P2);

		// He does not move again this turn: the hand is discarded, and his reconcile draws eight.
		assertInZone(Zone.DISCARD, dwarf, armor, strike);
		assertEquals(8, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsDecisionAvailable("Do you want to make another move?"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
