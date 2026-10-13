package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_053_Tests
{

	// The Fellowship path: the Shadow phase of the first turn is at Ettenmoors, site 2 (Shadow number 2; a skirmish
	// ability only), where every Gollum (site number 3) is roaming and costs 2 + 2.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("captured", "7_53");

					// Four Gollums, each a unique minion of cost 2 and site number 3, with nothing that happens when he is played
					put("villain", "6_40");     // Gollum, Old Villain: [gollum]; a skirmish ability only
					put("wraith", "103_66");    // Gollum, Half a Wraith Himself: [wraith]; answers a Nazgul played, and an assignment ability
					put("nasty", "5_24");       // Gollum, Nasty Treacherous Creature: [gollum]; a regroup ability only
					put("vile", "7_59");        // Gollum, Vile Creature: [gollum]; a skirmish ability only
					// Shelob, Eater of Light: a [gollum] minion with another title, cost 6 and site number 8 (8 at site 2);
					// her own trigger is on being played, and she is never played here
					put("shelob", "8_25");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a second companion, so that two threats could lie
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CapturedbytheRingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Captured by the Ring
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Play Gollum from your draw deck or discard pile to add a threat.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("captured");

		assertEquals("Captured by the Ring", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysAGollumFromTheDrawDeckToAddAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captured = scn.GetShadowCard("captured");
		var villain = scn.GetShadowCard("villain");
		var wraith = scn.GetShadowCard("wraith");
		var nasty = scn.GetShadowCard("nasty");
		var vile = scn.GetShadowCard("vile");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCompanionsToTable("aragorn");
		// Old Villain, Half a Wraith Himself and Shelob stay in the draw deck; Nasty Treacherous Creature gives the
		// discard pile a Gollum too, and Vile Creature is a Gollum in hand.
		scn.MoveCardsToHand(captured, vile);
		scn.MoveCardsToDiscard(nasty);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 10 set + 2 companions + 2 for Ettenmoors: Shelob (6 + 2 roaming) could be paid for.
		assertEquals(14, scn.GetTwilight());
		assertInZone(Zone.DECK, villain, wraith, shelob);
		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetThreatLimit());
		int shuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.ShadowPlayAvailable(captured));

		scn.ShadowPlayCard(captured);
		assertTrue(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowChoose("draw deck");
		scn.ShadowDismissRevealedCards();

		// Gollums of two cultures
		assertTrue(scn.ShadowHasCardChoiceAvailable(villain, wraith));
		// A [gollum] minion with another title, the Gollum in the other pile, and the Gollum in hand
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shelob, nasty, vile));
		scn.ShadowChooseCardBPFromSelection(wraith);

		assertInZone(Zone.SHADOW_CHARACTERS, wraith);
		assertInZone(Zone.DECK, villain, shelob);
		assertInZone(Zone.DISCARD, captured, nasty);
		assertInZone(Zone.HAND, vile);
		// A threat, not two: two companions leave room for both.
		assertEquals(1, scn.GetThreats());
		// The draw deck was looked through, so it is reshuffled: as Gollum left it, and the definition's own
		// shuffle after the play (F-0313). The other player's deck is not.
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlaysAGollumFromTheDiscardPileToAddAThreatAndLeavesTheDeckUnshuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captured = scn.GetShadowCard("captured");
		var villain = scn.GetShadowCard("villain");
		var wraith = scn.GetShadowCard("wraith");
		var vile = scn.GetShadowCard("vile");
		var shelob = scn.GetShadowCard("shelob");
		var freepsNasty = scn.GetFreepsCard("nasty");
		scn.MoveCompanionsToTable("aragorn");
		scn.MoveCardsToHand(captured);
		// Old Villain, Half a Wraith Himself and Shelob are in the discard pile; Vile Creature stays in the draw
		// deck, and the opponent has Nasty Treacherous Creature in his own discard pile.
		scn.MoveCardsToDiscard(villain, wraith, shelob);
		scn.MoveCardsToDiscard(freepsNasty);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(14, scn.GetTwilight());
		assertInZone(Zone.DECK, vile);
		assertEquals(0, scn.GetThreats());
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(captured);
		assertTrue(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowChoose("discard pile");

		// Gollums of two cultures
		assertTrue(scn.ShadowHasCardChoiceAvailable(villain, wraith));
		// A [gollum] minion with another title, the Gollum in the other pile, and the opponent's Gollum in his own pile
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shelob, vile, freepsNasty));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(villain);

		assertInZone(Zone.SHADOW_CHARACTERS, villain);
		assertInZone(Zone.DISCARD, captured, wraith, shelob, freepsNasty);
		assertInZone(Zone.DECK, vile);
		assertEquals(1, scn.GetThreats());
		// Gollum came from the discard pile and the draw deck was never looked through.
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenThePoolIsOneShortOfGollumAndThenPlaysHimFromTheOnlyPileThatHasOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captured = scn.GetShadowCard("captured");
		var villain = scn.GetShadowCard("villain");
		scn.MoveCompanionsToTable("aragorn");
		// Old Villain is the one Gollum in the discard pile; the others are in hand, so the draw deck has none
		// (Shelob is there).
		scn.MoveCardsToHand(captured, scn.GetShadowCard("wraith"), scn.GetShadowCard("nasty"), scn.GetShadowCard("vile"));
		scn.MoveCardsToDiscard(villain);

		scn.StartGame();
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// Gollum costs 2 + 2 roaming at site 2.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(captured));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(captured));
		assertEquals(0, scn.GetThreats());

		// The draw deck has no Gollum, so no pile is asked for and the one Gollum there is is played.
		scn.ShadowPlayCard(captured);
		assertInZone(Zone.SHADOW_CHARACTERS, villain);
		assertInZone(Zone.DISCARD, captured);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoGollumInTheDrawDeckOrDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captured = scn.GetShadowCard("captured");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCompanionsToTable("aragorn");
		// Every Gollum is in hand; Shelob, who could be paid for, stays in the draw deck.
		scn.MoveCardsToHand(captured, scn.GetShadowCard("villain"), scn.GetShadowCard("wraith"),
				scn.GetShadowCard("nasty"), scn.GetShadowCard("vile"));

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(14, scn.GetTwilight());
		assertInZone(Zone.DECK, shelob);
		assertFalse(scn.ShadowPlayAvailable(captured));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileAGollumIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captured = scn.GetShadowCard("captured");
		var villain = scn.GetShadowCard("villain");
		var wraith = scn.GetShadowCard("wraith");
		var nasty = scn.GetShadowCard("nasty");
		scn.MoveCompanionsToTable("aragorn");
		// Half a Wraith Himself stays in the draw deck and Nasty Treacherous Creature is in the discard pile.
		scn.MoveCardsToHand(captured);
		scn.MoveCardsToDiscard(nasty);

		scn.StartGame();
		scn.MoveMinionsToTable(villain);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(14, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, villain);
		assertInZone(Zone.DECK, wraith);
		// Every Gollum is unique, so none can be played while one is in play.
		assertFalse(scn.ShadowPlayAvailable(captured));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AGollumFoundInTheDrawDeckNeedNotBePlayedAndThenNoThreatIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captured = scn.GetShadowCard("captured");
		var villain = scn.GetShadowCard("villain");
		var wraith = scn.GetShadowCard("wraith");
		scn.MoveCompanionsToTable("aragorn");
		// Old Villain and Half a Wraith Himself stay in the draw deck; the other two Gollums are in hand, so the
		// discard pile has none.
		scn.MoveCardsToHand(captured, scn.GetShadowCard("nasty"), scn.GetShadowCard("vile"));

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(14, scn.GetTwilight());
		assertInZone(Zone.DECK, villain, wraith);
		assertEquals(0, scn.GetThreats());
		int deckBefore = scn.GetShadowDeckCount();

		// The discard pile has no Gollum, so no pile is asked for and the draw deck is looked through.
		scn.ShadowPlayCard(captured);
		scn.ShadowDismissRevealedCards();

		// "There is no penalty if you don't find (or choose not to play) a card you are looking for in your draw deck."
		assertTrue(scn.ShadowHasCardChoiceAvailable(villain, wraith));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowDeclineChoosing();

		// No Gollum was played, so the cost was not paid and no threat is added. The event is spent.
		assertInZone(Zone.DECK, villain, wraith);
		assertEquals(deckBefore, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, captured);
		assertEquals(0, scn.GetThreats());
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	@Ignore("F-0494: a draw deck looked through by a play that is a COST, and then playing nothing, is not reshuffled")
	public void ADrawDeckLookedThroughForAGollumNotPlayedIsStillReshuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captured = scn.GetShadowCard("captured");
		var villain = scn.GetShadowCard("villain");
		var wraith = scn.GetShadowCard("wraith");
		scn.MoveCompanionsToTable("aragorn");
		// Old Villain and Half a Wraith Himself stay in the draw deck; the other two Gollums are in hand.
		scn.MoveCardsToHand(captured, scn.GetShadowCard("nasty"), scn.GetShadowCard("vile"));

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertInZone(Zone.DECK, villain, wraith);
		int shuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.ShadowPlayCard(captured);
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(villain, wraith));
		scn.ShadowDeclineChoosing();

		assertInZone(Zone.DECK, villain, wraith);
		// CR 4.2 "draw deck": "When you finish looking through it, reshuffle it" (Q-0013: always), whether or not a
		// card is played from it. The other player's deck is not shuffled.
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
