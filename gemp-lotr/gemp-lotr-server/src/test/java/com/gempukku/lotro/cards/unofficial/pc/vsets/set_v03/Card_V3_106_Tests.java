package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_106_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("flotsam1", "103_106");
					put("flotsam2", "103_106");
					put("flotsam3", "103_106");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability never used here
					put("sam", "1_311");          // Sam, Son of Hamfast: a Hobbit companion; a fellowship ability never used here
					put("gimli", "1_13");         // Gimli, Son of Gloin: a companion who can bear none of these pipes
					put("bounder", "1_286");      // Bounder: a Hobbit ally; a skirmish ability

					// The pipes all read "Fellowship: Discard a pipeweed possession and spot X pipes to ...", never used here.
					put("apipe", "1_91");         // Aragorn's Pipe: [gondor], cost 1, bearer a [gondor] companion
					put("bpipe", "1_285");        // Bilbo's Pipe: [shire], cost 1, bearer a Hobbit
					put("gafferpipe", "1_292");   // The Gaffer's Pipe: [shire], cost 1, bearer a Hobbit
					put("gpipe", "1_74");         // Gandalf's Pipe: [gandalf], bearer Gandalf: a pipe for the discard pile

					put("toby", "1_305");         // Old Toby: a [shire] pipeweed possession
					put("longstem", "17_21");     // Long-stemmed Pipe: a [gandalf] pipeweed condition and, for all its name, no pipe
					put("sword", "1_299");        // Hobbit Sword: a [shire] possession (a hand weapon) that is neither pipe nor pipeweed
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FlotsamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Flotsam
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype:
		 * Game Text: Pipeweed. Bearer must bear a pipe.
		 * 	When you play this possession, you may shuffle up to 2 pipes or pipeweed
		 * 	from your discard pile into your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("flotsam1");

		assertEquals("Flotsam", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.PIPEWEED));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedForOneOnACharacterBearingAPipeAndItsOfferMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var flotsam = scn.GetFreepsCard("flotsam1");
		var otherFlotsam = scn.GetFreepsCard("flotsam3");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var apipe = scn.GetFreepsCard("apipe");
		var bpipe = scn.GetFreepsCard("bpipe");
		var sword = scn.GetFreepsCard("sword");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bounder);
		// Frodo bears a possession and a pipeweed, and no pipe.
		scn.AttachCardsTo(frodo, sword, otherFlotsam);
		scn.MoveCardsToHand(flotsam, apipe, bpipe);
		scn.MoveCardsToDiscard(toby);

		scn.StartGame();

		// Nobody bears a pipe.
		assertFalse(scn.FreepsPlayAvailable(flotsam));

		// Aragorn's Pipe goes on the one [gondor] companion.
		scn.FreepsPlayCard(apipe);
		assertAttachedTo(apipe, aragorn);
		assertTrue(scn.FreepsPlayAvailable(flotsam));

		scn.FreepsPlayCard(bpipe);
		scn.FreepsChooseCard(bounder);
		assertAttachedTo(bpipe, bounder);
		assertEquals(2, scn.GetTwilight());

		scn.FreepsPlayCard(flotsam);

		// A [gondor] companion and a [shire] ally, each with a pipe.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(bounder);

		// On its bearer, not in the support area where other pipeweed lies.
		assertAttachedTo(flotsam, bounder);
		// 2 for the pipes + 1 for Flotsam
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(flotsam));
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsDeclineOptionalTrigger();

		assertInDiscard(toby);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertAttachedTo(flotsam, bounder);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenPlayedItMayShuffleTwoPipesOrPipeweedFromTheDiscardPileIntoTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flotsam = scn.GetFreepsCard("flotsam1");
		var earlierFlotsam = scn.GetFreepsCard("flotsam2");
		var discardedFlotsam = scn.GetFreepsCard("flotsam3");
		var aragorn = scn.GetFreepsCard("aragorn");
		var apipe = scn.GetFreepsCard("apipe");
		var bpipe = scn.GetFreepsCard("bpipe");
		var gpipe = scn.GetFreepsCard("gpipe");
		var toby = scn.GetFreepsCard("toby");
		var longstem = scn.GetFreepsCard("longstem");
		var sword = scn.GetFreepsCard("sword");
		var opponentsPipe = scn.GetShadowCard("gafferpipe");
		scn.MoveCompanionsToTable(aragorn);
		// A pipe in play with a copy of Flotsam already on its bearer, a pipe in hand, and a pipe in the other
		// player's discard pile.
		scn.AttachCardsTo(aragorn, apipe, earlierFlotsam);
		scn.MoveCardsToHand(flotsam, bpipe);
		scn.MoveCardsToDiscard(opponentsPipe);
		// A pipe, pipeweed of two cultures and two card types, another copy of this card; and a [shire]
		// possession that is neither.
		scn.MoveCardsToDiscard(gpipe, toby, longstem, discardedFlotsam, sword);

		scn.StartGame();

		scn.FreepsPlayCard(flotsam);

		// A second copy goes on the same bearer: neither has an item class.
		assertAttachedTo(flotsam, aragorn);
		assertAttachedTo(earlierFlotsam, aragorn);
		// The copy just played makes the offer; the copy that was there already makes none.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(flotsam));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(earlierFlotsam));

		scn.FreepsChooseOptionalTrigger(flotsam);

		assertTrue(scn.FreepsHasCardChoiceAvailable(gpipe, toby, longstem, discardedFlotsam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword, apipe, bpipe, opponentsPipe));
		assertEquals(0, scn.FreepsGetChoiceMin());
		// Four could be taken; two is the most.
		assertEquals(2, scn.FreepsGetChoiceMax());
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		// A pipe and a pipeweed
		scn.FreepsChooseCards(gpipe, longstem);

		assertInZone(Zone.DECK, gpipe);
		assertInZone(Zone.DECK, longstem);
		assertInDiscard(toby);
		assertInDiscard(discardedFlotsam);
		assertInDiscard(sword);
		assertEquals(deck + 2, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.HAND, bpipe);
		assertAttachedTo(apipe, aragorn);
		assertInDiscard(opponentsPipe);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OneCardMayBeTakenOrNoneAndTakingNoneStillShufflesTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flotsam1 = scn.GetFreepsCard("flotsam1");
		var flotsam2 = scn.GetFreepsCard("flotsam2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var apipe = scn.GetFreepsCard("apipe");
		var gpipe = scn.GetFreepsCard("gpipe");
		var toby = scn.GetFreepsCard("toby");
		var longstem = scn.GetFreepsCard("longstem");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, apipe);
		scn.MoveCardsToHand(flotsam1, flotsam2);
		scn.MoveCardsToDiscard(gpipe, toby, longstem);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(flotsam1);
		scn.FreepsChooseOptionalTrigger(flotsam1);
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());

		// One of the three
		scn.FreepsChooseCards(toby);

		assertInZone(Zone.DECK, toby);
		assertInDiscard(gpipe);
		assertInDiscard(longstem);
		assertEquals(deck + 1, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The offer taken and then no card chosen: nothing goes into the draw deck, and the deck is shuffled
		// all the same.
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		scn.FreepsPlayCard(flotsam2);
		scn.FreepsChooseOptionalTrigger(flotsam2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gpipe, longstem));
		scn.FreepsChooseCards();

		assertInDiscard(gpipe);
		assertInDiscard(longstem);
		assertEquals(deck + 1, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertAttachedTo(flotsam2, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoPipeOrPipeweedInTheDiscardPileTheOfferIsStillMadeAndTakingItShufflesTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flotsam = scn.GetFreepsCard("flotsam1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var apipe = scn.GetFreepsCard("apipe");
		var bpipe = scn.GetFreepsCard("bpipe");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		// A pipe in play and a pipe in hand; in the discard pile, a [shire] possession that is neither pipe nor
		// pipeweed.
		scn.AttachCardsTo(aragorn, apipe);
		scn.MoveCardsToHand(flotsam, bpipe);
		scn.MoveCardsToDiscard(sword);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsPlayCard(flotsam);

		assertAttachedTo(flotsam, aragorn);
		assertEquals(1, scn.GetTwilight());
		// There is nothing to shuffle in, and the offer does something all the same: it shuffles the draw deck.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(flotsam));

		scn.FreepsChooseOptionalTrigger(flotsam);

		assertInDiscard(sword);
		assertInZone(Zone.HAND, bpipe);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnotherPossessionPlayedOrATransferOfItMakesNoOfferAndItsOwnPlayDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var flotsam = scn.GetFreepsCard("flotsam1");
		var earlierFlotsam = scn.GetFreepsCard("flotsam2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var apipe = scn.GetFreepsCard("apipe");
		var bpipe = scn.GetFreepsCard("bpipe");
		var gafferpipe = scn.GetFreepsCard("gafferpipe");
		var gpipe = scn.GetFreepsCard("gpipe");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(aragorn, sam, gimli);
		scn.AttachCardsTo(aragorn, apipe, earlierFlotsam);
		scn.AttachCardsTo(frodo, bpipe);
		scn.MoveCardsToHand(flotsam, gafferpipe);
		scn.MoveCardsToDiscard(gpipe, toby);

		scn.StartGame();

		// Another possession is played while a Flotsam is in play: a pipe, on Sam, the one Hobbit without one.
		scn.FreepsPlayCard(gafferpipe);

		assertAttachedTo(gafferpipe, sam);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The Flotsam in play is transferred: to a companion who bears a pipe, for its twilight cost again.
		// A transfer is not a play (CR 4.2 "transfer").
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsTransferAvailable(earlierFlotsam));
		scn.FreepsTransferCard(earlierFlotsam);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		scn.FreepsChooseCard(sam);

		assertAttachedTo(earlierFlotsam, sam);
		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInDiscard(gpipe);
		assertInDiscard(toby);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Its own play: the copy played makes the offer and the copy in play makes none.
		scn.FreepsPlayCard(flotsam);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		scn.FreepsChooseCard(frodo);

		assertAttachedTo(flotsam, frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(flotsam));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(earlierFlotsam));

		scn.FreepsChooseOptionalTrigger(flotsam);
		scn.FreepsChooseCards(gpipe, toby);

		assertInZone(Zone.DECK, gpipe);
		assertInZone(Zone.DECK, toby);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
