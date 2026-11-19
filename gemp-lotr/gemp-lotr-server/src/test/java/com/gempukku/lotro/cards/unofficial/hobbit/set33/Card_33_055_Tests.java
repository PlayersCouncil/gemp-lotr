package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_055_Tests
{

	// The Hobbit table, on the Hobbit path with Prancing Pony as site 1 in place of Bag End 30_49: the game begins
	// there. Bilbo, Reliable Companion 30_46 is the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// [gandalf] cards: a companion, an event and a follower.
					put("gandalf", "30_26");     // Gandalf, Friend of Thorin: [gandalf] Wizard companion
					put("takeup", "30_31");      // Take up arms. Fight!: [gandalf] skirmish event
					put("gwaihir", "31_15");     // Gwaihir, Lord of the Eagles: [gandalf] follower
					// [shire] cards: a possession and an event.
					put("acorn", "31_38");       // An Acorn from Beorn's house: [shire] possession, bearer Bilbo
					put("invisible", "31_39");   // An Invisible Ring: [shire] skirmish event
					// Cards of other cultures: a [dwarven] companion, a [dwarven] event and a [moria] minion.
					put("balin", "30_2");        // Balin, Brother of Dwalin
					put("fury", "30_3");         // Battle Fury
					put("runner", "31_30");      // Goblin Runner
					// Goblin Patrol Troop: a minion with no text. Eight copies lie on top of player 2's draw deck, for
					// what he draws before his own turn, so that the cards beneath are still in his deck then.
					put("troop1", "1_177");
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("troop4", "1_177");
					put("troop5", "1_177");
					put("troop6", "1_177");
					put("troop7", "1_177");
					put("troop8", "1_177");
				}},
				new HashMap<>()
				{{
					put("site1", "33_55");
					put("site2", "30_50");
					put("site3", "30_51");
					put("site4", "30_52");
					put("site5", "30_53");
					put("site6", "30_54");
					put("site7", "30_55");
					put("site8", "30_56");
					put("site9", "30_57");
				}},
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void PrancingPonyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Prancing Pony
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 1
		 * Shadow Number: 0
		 * Direction: none
		 * Game Text: At the start of your fellowship phase, you may add (2) to take a [gandalf] or [shire] card into hand from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Prancing Pony", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
	}

	@Test
	public void AtTheStartOfTheFellowshipPhaseAddingTwoTakesAGandalfOrShireCardIntoHandFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var takeup = scn.GetFreepsCard("takeup");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var acorn = scn.GetFreepsCard("acorn");
		var invisible = scn.GetFreepsCard("invisible");
		var balin = scn.GetFreepsCard("balin");
		var fury = scn.GetFreepsCard("fury");
		var runner = scn.GetFreepsCard("runner");

		scn.StartGame();
		var pony = scn.GetCurrentSite();
		scn.SetFreepsDrawDeck(gandalf, takeup, gwaihir, acorn, invisible, balin, fury, runner);

		assertEquals("33_55", pony.getBlueprintId());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(8, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetTwilight());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pony));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.MarkGameLog();
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetTwilight());
		scn.FreepsDismissRevealedCards();

		// [gandalf] and [shire] cards: a companion, a follower, a possession and two events; not a [dwarven]
		// companion, a [dwarven] event or a Shadow card.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, takeup, gwaihir, acorn, invisible));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(balin, fury, runner));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(acorn);

		assertInHand(acorn);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.DECK, gandalf, takeup, gwaihir, invisible, balin, fury, runner);
		assertEquals(2, scn.GetTwilight());
		// The card taken is named in the game log, and one left in the deck is not.
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(acorn));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(gwaihir));
		// The deck looked through is shuffled, once; the other player's is not.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningAddsNothingAndTheDrawDeckIsNotLookedThrough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var acorn = scn.GetFreepsCard("acorn");

		scn.StartGame();
		var pony = scn.GetCurrentSite();
		scn.SetFreepsDrawDeck(gandalf, acorn);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pony));
		assertInZone(Zone.DECK, gandalf, acorn);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.DECK, gandalf, acorn);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoGandalfOrShireCardInTheDrawDeckTheOfferIsNotMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var fury = scn.GetFreepsCard("fury");
		var runner = scn.GetFreepsCard("runner");

		// The deck is laid before the game begins: the offer is worked out as the first fellowship phase begins.
		scn.SetFreepsDrawDeck(balin, fury, runner);
		scn.StartGame();
		var pony = scn.GetCurrentSite();

		assertInZone(Zone.DECK, balin, fury, runner);
		assertEquals(0, scn.GetFreepsHandCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		// A [dwarven] companion, a [dwarven] event and a Shadow minion: nothing to take.  A Hobbit-set take from the draw deck with nothing to take is withheld (Q-0019; the orchestrator on Q-0406 and Q-0418):
		// no (2) is added for a look that finds nothing.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(pony));
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.DECK, balin, fury, runner);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OfferedToThePlayerWhoseFellowshipPhaseBeginsThereAndNotOnceTheFellowshipHasMovedOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowAcorn = scn.GetShadowCard("acorn");
		var shadowBalin = scn.GetShadowCard("balin");

		scn.StartGame();
		var pony = scn.GetCurrentSite();

		// Player 1's turn at Prancing Pony: he is offered, and declines.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pony));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();

		// Player 2's first turn begins there too: it is his fellowship phase, and his offer.
		scn.SetShadowDrawDeck(scn.GetShadowCard("troop1"), scn.GetShadowCard("troop2"), scn.GetShadowCard("troop3"), scn.GetShadowCard("troop4"), scn.GetShadowCard("troop5"), scn.GetShadowCard("troop6"), scn.GetShadowCard("troop7"), scn.GetShadowCard("troop8"),
				shadowAcorn, shadowBalin);
		scn.SkipToOtherPlayersTurn();

		assertEquals("33_55", scn.GetCurrentSite().getBlueprintId());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertInZone(Zone.DECK, shadowAcorn, shadowBalin);
		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scn.GetCurrentSite()));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(twilight + 2, scn.GetTwilight());
		scn.DismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(shadowAcorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shadowBalin));
		scn.ShadowChooseCardBPFromSelection(shadowAcorn);
		assertInHand(shadowAcorn);
		assertInZone(Zone.DECK, shadowBalin);

		// Player 1's next turn begins at The Troll Hoard (site 2), with Prancing Pony behind: no offer from it.
		scn.SkipToOtherPlayersTurn();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		// The Troll Hoard makes no offer of its own either: no hand weapon in the deck (F-0800).
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
