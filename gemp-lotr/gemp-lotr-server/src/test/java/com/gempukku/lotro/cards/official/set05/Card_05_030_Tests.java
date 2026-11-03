package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_030_Tests
{

	// The Fellowship path: the Shadow phase of the first turn is at Ettenmoors, site 2 (Shadow number 2; a skirmish
	// ability only), where every Gollum (site number 3) is roaming and costs 2 + 2.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("musthave", "5_30");

					// Gollums, each a unique minion of cost 2 and site number 3 (2 + 2 roaming at Ettenmoors, site 2)
					put("villain", "6_40");     // Gollum, Old Villain: [gollum]; a skirmish ability only
					put("wraith", "103_66");    // Gollum, Half a Wraith Himself: [wraith]; answers a Nazgul played, and an assignment ability
					put("nasty", "5_24");       // Gollum, Nasty Treacherous Creature: [gollum]; a regroup ability only
					put("vile", "7_59");        // Gollum, Vile Creature: [gollum]; a skirmish ability only
					// Shelob, Eater of Light: a [gollum] minion with another title, cost 6 and site number 8 (8 at site 2);
					// her own trigger is on being played, and she is never played here
					put("shelob", "8_25");

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its text is about being played
					put("filler", "1_178");     // two more copies, cards in hand for the random discard
					put("filler2", "1_178");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WeMustHaveItStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: We Must Have It
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow, Skirmish
		 * Game Text: <b>Shadow:</b> Play Gollum from your draw deck or discard pile.<br><b>Skirmish:</b> Discard a card at random from hand to make Gollum strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("musthave");

		assertEquals("We Must Have It", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void InTheShadowPhaseItPlaysAGollumFromTheDrawDeckAndDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetShadowCard("musthave");
		var villain = scn.GetShadowCard("villain");
		var wraith = scn.GetShadowCard("wraith");
		var nasty = scn.GetShadowCard("nasty");
		var vile = scn.GetShadowCard("vile");
		var shelob = scn.GetShadowCard("shelob");
		var filler = scn.GetShadowCard("filler");
		scn.MoveCompanionsToTable("aragorn");
		// Old Villain, Half a Wraith Himself and Shelob stay in the draw deck; Nasty Treacherous Creature gives the
		// discard pile a Gollum too, and Vile Creature is a Gollum in hand beside a Goblin Runner.
		scn.MoveCardsToHand(card, vile, filler);
		scn.MoveCardsToDiscard(nasty);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 10 set + 2 companions + 2 for Ettenmoors: Shelob (6 + 2 roaming) could be paid for.
		assertEquals(14, scn.GetTwilight());
		assertInZone(Zone.DECK, villain, wraith, shelob);
		assertEquals(3, scn.GetShadowHandCount());
		int shuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.ShadowPlayAvailable(card));

		scn.ShadowPlayCard(card);
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
		assertInZone(Zone.DISCARD, card, nasty);
		// The Skirmish half is not carried out in the Shadow phase: no card of the hand is discarded, and the
		// Gollum played is at his printed 5.
		assertInZone(Zone.HAND, vile, filler);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(5, scn.GetStrength(wraith));
		// The draw deck was looked through, so it is reshuffled: as Gollum left it, and the definition's own
		// shuffle after the play (F-0313). The other player's deck is not.
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void InTheShadowPhaseItPlaysAGollumFromTheDiscardPileAndLeavesTheDeckUnshuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetShadowCard("musthave");
		var villain = scn.GetShadowCard("villain");
		var wraith = scn.GetShadowCard("wraith");
		var vile = scn.GetShadowCard("vile");
		var shelob = scn.GetShadowCard("shelob");
		var freepsNasty = scn.GetFreepsCard("nasty");
		scn.MoveCompanionsToTable("aragorn");
		scn.MoveCardsToHand(card);
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
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(card);
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
		assertInZone(Zone.DISCARD, card, wraith, shelob, freepsNasty);
		assertInZone(Zone.DECK, vile);
		// Gollum came from the discard pile and the draw deck was never looked through.
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenThePoolIsOneShortOfGollumAndThenPlaysHimFromTheOnlyPileThatHasOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetShadowCard("musthave");
		var villain = scn.GetShadowCard("villain");
		scn.MoveCompanionsToTable("aragorn");
		// Old Villain is the one Gollum in the discard pile; the others are in hand, so the draw deck has none
		// (Shelob is there).
		scn.MoveCardsToHand(card, scn.GetShadowCard("wraith"), scn.GetShadowCard("nasty"), scn.GetShadowCard("vile"));
		scn.MoveCardsToDiscard(villain);

		scn.StartGame();
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// Gollum costs 2 + 2 roaming at site 2.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(card));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(card));

		// The draw deck has no Gollum, so no pile is asked for and the one Gollum there is is played.
		scn.ShadowPlayCard(card);
		assertInZone(Zone.SHADOW_CHARACTERS, villain);
		assertInZone(Zone.DISCARD, card);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoGollumInTheDrawDeckOrDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetShadowCard("musthave");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCompanionsToTable("aragorn");
		// Every Gollum is in hand; Shelob, who could be paid for, stays in the draw deck.
		scn.MoveCardsToHand(card, scn.GetShadowCard("villain"), scn.GetShadowCard("wraith"),
				scn.GetShadowCard("nasty"), scn.GetShadowCard("vile"));

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(14, scn.GetTwilight());
		assertInZone(Zone.DECK, shelob);
		assertFalse(scn.ShadowPlayAvailable(card));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheShadowPhaseWhileAGollumIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetShadowCard("musthave");
		var villain = scn.GetShadowCard("villain");
		var wraith = scn.GetShadowCard("wraith");
		var nasty = scn.GetShadowCard("nasty");
		var filler = scn.GetShadowCard("filler");
		scn.MoveCompanionsToTable("aragorn");
		// Half a Wraith Himself stays in the draw deck and Nasty Treacherous Creature is in the discard pile; a
		// Goblin Runner in hand could pay the Skirmish half's discard.
		scn.MoveCardsToHand(card, filler);
		scn.MoveCardsToDiscard(nasty);

		scn.StartGame();
		scn.MoveMinionsToTable(villain);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(14, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, villain);
		assertInZone(Zone.DECK, wraith);
		// Every Gollum is unique, so none can be played while one is in play; and the Skirmish half, which could
		// be paid for, is not offered in the Shadow phase.
		assertFalse(scn.ShadowPlayAvailable(card));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetShadowCard("musthave");
		var villain = scn.GetShadowCard("villain");
		var runner = scn.GetShadowCard("runner");
		var filler = scn.GetShadowCard("filler");
		scn.MoveCompanionsToTable("aragorn");
		// A Gollum in the discard pile and another in the draw deck, none in play; a card in hand beside the event.
		scn.MoveCardsToHand(card, filler);
		scn.MoveCardsToDiscard(villain);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		// A minion, so that there is a maneuver phase.
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		// 10 + 2 + 2: Gollum could be paid for.
		assertEquals(14, scn.GetTwilight());
		assertInZone(Zone.DISCARD, villain);
		assertFalse(scn.ShadowPlayAvailable(card));
	}

	@Test
	public void ADrawDeckLookedThroughForAGollumNotPlayedIsStillReshuffled() throws DecisionResultInvalidException, CardNotFoundException {
		// The play is the effect of a costToEffect inside the event's cost, so it has always been carried out as an
		// effect and the shuffle after it reached (F-0494 was Captured by the Ring 7_53's, whose play is the cost).
		var scn = GetScenario();

		var card = scn.GetShadowCard("musthave");
		var villain = scn.GetShadowCard("villain");
		var wraith = scn.GetShadowCard("wraith");
		// Old Villain and Half a Wraith Himself stay in the draw deck; the other two Gollums are in hand, so the
		// discard pile has none and no pile is asked for.
		scn.MoveCardsToHand(card, scn.GetShadowCard("nasty"), scn.GetShadowCard("vile"));

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertInZone(Zone.DECK, villain, wraith);
		int shuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int twilight = scn.GetTwilight();

		scn.ShadowPlayCard(card);
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(villain, wraith));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();

		// CR 4.2 "draw deck": "When you finish looking through it, reshuffle it" (Q-0013: always), once, whether
		// or not a card is played from it. The other player's deck is not shuffled.
		assertInZone(Zone.DECK, villain, wraith);
		assertInZone(Zone.DISCARD, card);
		assertEquals(twilight, scn.GetTwilight());
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void InASkirmishItDiscardsTheOtherCardInHandToMakeGollumStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetShadowCard("musthave");
		var nasty = scn.GetShadowCard("nasty");
		var runner = scn.GetShadowCard("runner");
		var filler = scn.GetShadowCard("filler");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// The event and one other card, which the random discard can only take.  Kept out of the hand until
		// the skirmish, so that nothing earlier acts on it.
		scn.MoveCardsToDiscard(card, filler);

		scn.StartGame();
		scn.MoveMinionsToTable(nasty, runner);
		scn.SkipToAssignments();
		// Gollum and a Goblin Runner in one skirmish against Aragorn: the second minion is the Shadow player's to
		// assign.
		scn.FreepsAssignToMinions(aragorn, nasty);
		scn.ShadowAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		scn.MoveCardsToHand(card, filler);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(nasty));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(card));

		scn.ShadowPlayCard(card);

		assertInZone(Zone.DISCARD, card, filler);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(8, scn.GetStrength(nasty));
		// The other minion in the same skirmish, and the companion, are as they were.
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void InASkirmishItCannotBePlayedWithNoOtherCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetShadowCard("musthave");
		var nasty = scn.GetShadowCard("nasty");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDiscard(card);

		scn.StartGame();
		scn.MoveMinionsToTable(nasty);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, nasty);
		scn.FreepsResolveSkirmish(aragorn);
		scn.MoveCardsToHand(card);
		scn.FreepsPass();

		// The event is the only card in hand, and cannot be discarded to pay for itself.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(1, scn.GetShadowHandCount());
		assertFalse(scn.ShadowPlayAvailable(card));
		assertEquals(5, scn.GetStrength(nasty));
	}

	@Test
	public void AGollumInAnotherSkirmishIsStrengthPlus3UntilThatSkirmishEndsAndTheCardDiscardedIsTakenAtRandom() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetShadowCard("musthave");
		var wraith = scn.GetShadowCard("wraith");
		var runner = scn.GetShadowCard("runner");
		var filler = scn.GetShadowCard("filler");
		var filler2 = scn.GetShadowCard("filler2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		// Kept out of the hand until the skirmish (a random discard takes whatever is there).
		scn.MoveCardsToDiscard(card, filler, filler2);

		scn.StartGame();
		scn.MoveMinionsToTable(wraith, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner},
				new PhysicalCardImpl[]{gimli, wraith});

		// The Goblin Runner's skirmish first: Gollum (Half a Wraith Himself) waits for his own.
		scn.FreepsResolveSkirmish(aragorn);
		scn.MoveCardsToHand(card, filler, filler2);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(wraith));
		assertEquals(3, scn.GetShadowHandCount());
		int discards = scn.GetShadowDiscardCount();

		scn.ShadowPlayCard(card);

		// One of the two other cards went, chosen by nobody: the game is back at the Free Peoples player's
		// skirmish actions with no choice of cards asked.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetShadowHandCount());
		// The event and one Goblin Runner
		assertEquals(discards + 2, scn.GetShadowDiscardCount());
		assertInZone(Zone.DISCARD, card);
		// "Gollum" names no skirmish: the one in another skirmish is made stronger.
		assertEquals(8, scn.GetStrength(wraith));
		assertEquals(5, scn.GetStrength(runner));

		// Still so at the next decision of this skirmish.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(wraith));

		// Aragorn (8) kills the Goblin Runner (vitality 1).  Gollum's own skirmish is another phase.
		scn.ShadowPass();
		assertInZone(Zone.DISCARD, runner);
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(5, scn.GetStrength(wraith));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
