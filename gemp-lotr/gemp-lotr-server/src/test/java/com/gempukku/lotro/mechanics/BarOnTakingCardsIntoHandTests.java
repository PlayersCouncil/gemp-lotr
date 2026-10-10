package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.modifiers.SpecialFlagModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A bar on taking cards into hand from a player's own draw deck or discard pile.
 *
 * The one text that lays it is Race Text 94_30: "You may not play or take into hand cards from your discard pile
 * or draw deck (except when reconciling or starting fellowship)."  The half about taking is the definition word
 * CantTakeCardsIntoHandFromDeckOrDiscardPile with a player, and the game reads it in one place
 * (RuleUtils.isBarredFromTakingIntoHand, isBarredFromDrawing).  Ketura, 2026-10-10:
 *
 * A card drawn is a card taken into hand from the draw deck (Q-0206): "I intended for it to include standard card
 * draw; the reconcile exception was so that the player didn't just sit down to play with their starting
 * fellowship and nothing else."  The barred player draws only when reconciling.
 *
 * The bar is on the player doing the taking (Q-0207): another player's card that returns the barred player's card
 * to his hand does so, and so does the barred player's own card that takes from another player's pile, which is
 * not "your" pile.  The taker is the player the text has take the card: "each Shadow player may take up to 4
 * cards into hand from his or her discard pile" on a Free Peoples card is the Shadow player's taking, and "make
 * an opponent ... draw 8 cards" is the opponent's drawing.
 *
 * The bar is laid here by itself, on one player, with no card: 94_30 also bars plays from the two piles, which
 * these tests are not about.
 */
public class BarOnTakingCardsIntoHandTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");        // Gimli, Son of Gloin: Dwarf companion, vitality 3
					put("delving", "1_6");       // Delving.  Fellowship: Exert a Dwarf companion to draw 3 cards.
					// Tidings of Erebor.  Regroup: Spot a Dwarf to draw 3 cards.  Any Shadow player may remove (3)
					// to prevent this.
					put("tidings", "2_13");
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a ranger, vitality 4
					// Change of Plans.  Regroup: Exert a ranger to make an opponent shuffle his hand into his draw
					// deck and draw 8 cards.
					put("change", "1_99");
					put("gandalf", "1_72");      // Gandalf, Friend of the Shirefolk: the [gandalf] Wizard
					// A New Light.  Fellowship: Spot a [gandalf] Wizard to search a Shadow player's discard pile
					// and choose a minion, then return that minion to its owner's hand.  If you do, you may reveal
					// that Shadow player's hand and discard a minion found there.
					put("light", "17_15");
					// Barliman Butterbur.  Fellowship: Exert Barliman Butterbur to take a [gandalf] event into
					// hand from your discard pile.
					put("barliman", "1_70");
					put("sleep", "1_84");        // Sleep, Caradhras: a [gandalf] event, never played here
					// Risk a Little Light.  Fellowship: If the twilight pool has fewer than 2 twilight tokens,
					// spot Gandalf to look at the top 2 cards of your draw deck.  Take one into hand and discard
					// the other.
					put("risk", "1_82");
					// Stern People: Free Peoples condition.  Regroup: Discard cards from hand equal to the number
					// of cards in an opponent's hand to make the move limit +1 for this turn.  Each Shadow player
					// may take up to 4 cards into hand from his or her discard pile.  Discard this condition.
					put("stern", "7_251");
					put("backstabber", "1_174"); // Goblin Backstabber
					put("troop", "1_177");       // Goblin Patrol Troop
					put("runner", "1_178");      // Goblin Runner

					put("deck1", "1_3");         // cards to draw
					put("deck2", "1_3");
					put("deck3", "1_3");
					put("deck4", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ABarredPlayersDrawOutsideAReconcileDrawsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();
		scn.ApplyAdHocModifier(new SpecialFlagModifier(null, null,
				ModifierFlag.CANT_TAKE_INTO_HAND_FROM_DISCARD_OR_DECK, VirtualTableScenario.P1));

		var gimli = scn.GetFreepsCard("gimli");
		var delving = scn.GetFreepsCard("delving");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(delving);

		scn.StartGame();

		// An event is offered whatever its effect will do; the game says of this one that it will do nothing.
		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsPlayAvailable(delving));
		assertEquals("effect", scn.FreepsActionNoEffect(delving));
		scn.FreepsPlayCard(delving);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInDiscard(delving);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ABarOnOnePlayerLeavesTheOtherPlayersDrawsAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();
		scn.ApplyAdHocModifier(new SpecialFlagModifier(null, null,
				ModifierFlag.CANT_TAKE_INTO_HAND_FROM_DISCARD_OR_DECK, VirtualTableScenario.P2));

		var gimli = scn.GetFreepsCard("gimli");
		var delving = scn.GetFreepsCard("delving");
		var tidings = scn.GetFreepsCard("tidings");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(delving, tidings);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals("false", scn.FreepsActionNoEffect(delving));
		scn.FreepsPlayCard(delving);
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertEquals(4, scn.GetFreepsHandCount());

		// A draw that will happen is one the Shadow player is asked whether to prevent.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		scn.FreepsPlayCard(tidings);
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseNo();

		assertEquals(deck - 6, scn.GetFreepsDeckCount());
		assertEquals(6, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void NobodyIsAskedToPreventADrawThatIsBarred() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();
		scn.ApplyAdHocModifier(new SpecialFlagModifier(null, null,
				ModifierFlag.CANT_TAKE_INTO_HAND_FROM_DISCARD_OR_DECK, VirtualTableScenario.P1));

		var gimli = scn.GetFreepsCard("gimli");
		var tidings = scn.GetFreepsCard("tidings");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(tidings);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);

		// A draw made in the regroup phase by a card is no reconcile.
		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsPlayAvailable(tidings));
		scn.FreepsPlayCard(tidings);

		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertInDiscard(tidings);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheDrawsOfAReconcileAreLetThrough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();
		scn.ApplyAdHocModifier(new SpecialFlagModifier(null, null,
				ModifierFlag.CANT_TAKE_INTO_HAND_FROM_DISCARD_OR_DECK, VirtualTableScenario.P1));

		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveCardsToHand(sleep);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetFreepsHandCount());
		scn.FreepsChooseToStay();
		scn.FreepsDeclineReconciliation();

		assertInHand(sleep);
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(deck - 7, scn.GetFreepsDeckCount());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void ADrawThatAnotherPlayersCardHasTheBarredPlayerMakeDrawsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();
		scn.ApplyAdHocModifier(new SpecialFlagModifier(null, null,
				ModifierFlag.CANT_TAKE_INTO_HAND_FROM_DISCARD_OR_DECK, VirtualTableScenario.P2));

		var aragorn = scn.GetFreepsCard("aragorn");
		var change = scn.GetFreepsCard("change");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(change);

		var backstabber = scn.GetShadowCard("backstabber");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(backstabber, troop);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// "Make an opponent shuffle his hand into his draw deck and draw 8 cards": the opponent is the one who
		// draws.  His hand goes into his deck and no card comes out of it.
		int deck = scn.GetShadowDeckCount();
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.FreepsPlayAvailable(change));
		scn.FreepsPlayCard(change);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(deck + 2, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// His reconcile, at the end of the same phase, draws his eight.
		scn.ShadowPass();
		scn.FreepsPass();

		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(deck + 2 - 8, scn.GetShadowDeckCount());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void ACardThatAnotherPlayersEffectReturnsToTheBarredPlayersHandIsReturned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();
		scn.ApplyAdHocModifier(new SpecialFlagModifier(null, null,
				ModifierFlag.CANT_TAKE_INTO_HAND_FROM_DISCARD_OR_DECK, VirtualTableScenario.P2));

		var gandalf = scn.GetFreepsCard("gandalf");
		var light = scn.GetFreepsCard("light");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(light);

		// Two minions in his discard pile, so that one is chosen; a third in his hand, so that the card's own
		// "discard a minion found there" need not take the one just returned.
		var backstabber = scn.GetShadowCard("backstabber");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToDiscard(backstabber, troop);
		scn.MoveCardsToHand(runner);

		scn.StartGame();

		scn.FreepsPlayCard(light);
		scn.FreepsChooseCardBPFromSelection(backstabber);

		// The Free Peoples player's card puts it there: its owner has taken nothing.
		assertInHand(backstabber);
		assertInDiscard(troop);
		assertEquals(2, scn.GetShadowHandCount());

		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(runner);

		assertInHand(backstabber);
		assertInDiscard(runner);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ABarredPlayersCardReturnsACardFromAnotherPlayersPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();
		scn.ApplyAdHocModifier(new SpecialFlagModifier(null, null,
				ModifierFlag.CANT_TAKE_INTO_HAND_FROM_DISCARD_OR_DECK, VirtualTableScenario.P1));

		var gandalf = scn.GetFreepsCard("gandalf");
		var light = scn.GetFreepsCard("light");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(light);

		var backstabber = scn.GetShadowCard("backstabber");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToDiscard(backstabber, troop);
		scn.MoveCardsToHand(runner);

		scn.StartGame();

		scn.FreepsPlayCard(light);
		scn.FreepsChooseCardBPFromSelection(backstabber);

		// The pile is the Shadow player's, not the barred player's own.
		assertInHand(backstabber);
		assertInDiscard(troop);
		assertEquals(2, scn.GetShadowHandCount());

		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(runner);

		assertInHand(backstabber);
		assertInDiscard(runner);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ABarredPlayerWhomAnotherPlayersCardHasTakeFromHisOwnPileTakesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();
		scn.ApplyAdHocModifier(new SpecialFlagModifier(null, null,
				ModifierFlag.CANT_TAKE_INTO_HAND_FROM_DISCARD_OR_DECK, VirtualTableScenario.P2));

		var stern = scn.GetFreepsCard("stern");
		scn.MoveCardsToSupportArea(stern);

		var backstabber = scn.GetShadowCard("backstabber");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToDiscard(backstabber, troop);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// "Each Shadow player may take up to 4 cards into hand from his or her discard pile": the card is the
		// Free Peoples player's and the taking is the Shadow player's own.
		assertTrue(scn.FreepsActionAvailable(stern));
		scn.FreepsUseCardAction(stern);
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));
		scn.ShadowChooseCardBPFromSelection(backstabber, troop);

		assertInDiscard(backstabber);
		assertInDiscard(troop);
		assertEquals(0, scn.GetShadowHandCount());
		assertInDiscard(stern);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ThePlayerWhoIsNotBarredTakesFromHisOwnPileByTheBarredPlayersCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();
		scn.ApplyAdHocModifier(new SpecialFlagModifier(null, null,
				ModifierFlag.CANT_TAKE_INTO_HAND_FROM_DISCARD_OR_DECK, VirtualTableScenario.P1));

		var stern = scn.GetFreepsCard("stern");
		scn.MoveCardsToSupportArea(stern);

		var backstabber = scn.GetShadowCard("backstabber");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToDiscard(backstabber, troop);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsActionAvailable(stern));
		scn.FreepsUseCardAction(stern);
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));
		scn.ShadowChooseCardBPFromSelection(backstabber, troop);

		assertInHand(backstabber);
		assertInHand(troop);
		assertEquals(2, scn.GetShadowHandCount());
		assertInDiscard(stern);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ABarredPlayerTakesNothingFromHisOwnPilesByHisOwnCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();
		scn.ApplyAdHocModifier(new SpecialFlagModifier(null, null,
				ModifierFlag.CANT_TAKE_INTO_HAND_FROM_DISCARD_OR_DECK, VirtualTableScenario.P1));

		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		var sleep = scn.GetFreepsCard("sleep");
		var risk = scn.GetFreepsCard("risk");
		var deck1 = scn.GetFreepsCard("deck1");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToDiscard(sleep);
		scn.MoveCardsToHand(risk);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(deck1);

		// From his discard pile: Barliman's cost is paid and the event stays where it lay.
		assertTrue(scn.FreepsActionAvailable(barliman));
		scn.FreepsUseCardAction(barliman);
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertInDiscard(sleep);
		assertEquals(1, scn.GetFreepsHandCount());

		// From his draw deck: the card chosen of the two looked at does not come to hand, and is discarded with
		// the other by the event's own "discard the other".
		scn.FreepsPlayCard(risk);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCard(deck1);
		assertInDiscard(deck1);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
