package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_057_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("event", "102_57");
					put("event2", "102_57");

					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, cost 2, site 3, no text
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, cost 2; a skirmish ability
					put("pinned", "1_192");     // Pinned Down: a Shadow condition that does nothing without a [moria] archer

					// The minions costing 5 or more.
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6, site 4, no text
					put("hunter", "1_256");     // Morgul Hunter: [sauron] Orc, cost 5, site 6; stronger for each companion
					// Ulaire Nelya, Ringwraith in Twilight: a unique [ringwraith] Nazgul, cost 5, site 2; his
					// text needs 2 burdens or 2 wounds on the Ring-bearer.
					put("nelya", "2_84");
					put("uruks", "1_143");      // Troop of Uruk-hai: [isengard], cost 5, site 5; Damage +1
					put("band", "4_202");       // Uruk-hai Band: [isengard], cost 5, site 5; Damage +1
					// Sauron, The Lord of the Rings: cost 18, site 6; cheaper for burdens, threats and
					// controlled sites, of which there are none.
					put("sauron", "9_48");

					// Marsh Wight: [sauron] Wraith, cost 5, site 4.  To play, spot a twilight minion; its cost is
					// -1 for each burden; damage +1 at 4 burdens.
					put("wight", "102_61");

					// Uruk-hai Raiding Party: cost 4, site 5, so 6 to play while it is roaming.
					put("party", "1_158");
					// Sharku's Warg: a Shadow possession costing 5, which a warg-rider can bear.
					put("warg", "5_59");
					put("wargrider", "5_67");   // Warg-rider: [isengard] Orc, cost 1; fierce while not exhausted
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AttentionOfTheEyeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Attention of the Eye
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Shadow: Spot your minion and remove (3) to draw 2 cards.
		 * 	Shadow: Spot 18 twilight tokens (or 25 if in region 3) to play up to 2 minions from your draw deck
		 * 	costing (5) or more. If in region 1, this action can be performed from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("event");

		assertEquals("Attention of the Eye", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NeedsAMinionOfYoursAndThenRemoves3ToDraw2Cards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var brigand = scn.GetShadowCard("brigand");
		var pinned = scn.GetShadowCard("pinned");
		scn.MoveCardsToHand(event, brigand);
		scn.MoveCardsToSupportArea(pinned);

		scn.StartGame();
		// 4 set + 1 companion + 2 for site 2
		scn.SetTwilight(4);
		scn.FreepsPass();

		// The Shadow player has a condition in play and no minion.
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, pinned);
		assertFalse(scn.ShadowPlayAvailable(event));

		// 2 to play + 2 roaming
		scn.ShadowPlayCard(brigand);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(event));
		assertEquals(1, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();

		// With fewer than 18 in the pool there is nothing to choose between.
		scn.ShadowPlayCard(event);
		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, event);
		// The cards come off the top: the deck was not looked through.
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithAMinionAndOnly2InThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(event, brigand);

		scn.StartGame();
		// 3 set + 1 companion + 2 for site 2
		scn.SetTwilight(3);
		scn.FreepsPass();
		assertEquals(6, scn.GetTwilight());

		// 2 to play + 2 roaming
		scn.ShadowPlayCard(brigand);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertFalse(scn.ShadowPlayAvailable(event));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void Spots18TwilightToPlayTwoMinionsCosting5OrMoreFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var event2 = scn.GetShadowCard("event2");
		var troop = scn.GetShadowCard("troop");
		var hunter = scn.GetShadowCard("hunter");
		var nelya = scn.GetShadowCard("nelya");
		var uruks = scn.GetShadowCard("uruks");
		var band = scn.GetShadowCard("band");
		var sauron = scn.GetShadowCard("sauron");
		var party = scn.GetShadowCard("party");
		var soldier = scn.GetShadowCard("soldier");
		var brigand = scn.GetShadowCard("brigand");
		var pinned = scn.GetShadowCard("pinned");
		// Minions costing 5 that are not in the draw deck.
		scn.MoveCardsToHand(event, uruks);
		scn.MoveCardsToDiscard(band, event2);
		scn.MoveCardsToSupportArea(pinned);

		scn.StartGame();
		// 15 set + 1 companion + 2 for site 2
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertEquals(18, scn.GetTwilight());
		assertInZone(Zone.DECK, troop, hunter, nelya, sauron, party);
		assertInZone(Zone.SUPPORT, pinned);
		assertTrue(scn.ShadowPlayAvailable(event));
		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();
		int opponentShuffles = scn.GetFreepsDeckShuffleCount();

		// With a condition of his on the table and no minion there is nothing to choose between: the
		// draw deck is shown.
		scn.ShadowPlayCard(event);
		assertEquals(18, scn.GetTwilight());
		scn.ShadowDismissRevealedCards();

		// An Orc of [moria] costing 6, an Orc of [sauron] costing 5, a unique Nazgul costing 5.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, hunter, nelya));
		// The Raiding Party costs 4, though roaming makes it 6 to play.  The others cost 1 or 2, are in
		// hand or in the discard pile, or, as Sauron (18 + 2 roaming), cannot be paid for.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(party, soldier, brigand, uruks, band, sauron));
		assertEquals(0, scn.ShadowGetChoiceMin());

		// 6 to play + 2 roaming
		scn.ShadowChooseCardBPFromSelection(troop);
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, troop);

		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(hunter, nelya));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(party, uruks, band, sauron));
		assertEquals(0, scn.ShadowGetChoiceMin());

		// 5 to play, and site 2 is Nelya's own
		scn.ShadowChooseCardBPFromSelection(nelya);
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);

		assertInZone(Zone.DISCARD, event, band);
		assertInZone(Zone.HAND, uruks);
		assertInZone(Zone.DECK, hunter, party);
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		// The deck was looked through, so it is shuffled; the other player's is not.
		assertTrue(scn.GetShadowDeckShuffleCount() > shuffles);
		assertEquals(opponentShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWith17InThePoolAndNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var event2 = scn.GetShadowCard("event2");
		scn.MoveCardsToHand(event);

		scn.StartGame();
		// 14 set + 1 companion + 2 for site 2
		scn.SetTwilight(14);
		scn.FreepsPass();

		assertEquals(17, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(event));
		// Nor is the copy in the draw deck offered.
		assertInZone(Zone.DECK, event2);
		assertFalse(scn.ShadowActionAvailable(event2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void With17InThePoolAndAMinionOnlyTheTwoCardsAreDrawnAndOnlyFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var event2 = scn.GetShadowCard("event2");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(event);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		// 14 set + 1 companion + 2 for site 2
		scn.SetTwilight(14);
		scn.FreepsPass();

		assertEquals(17, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(event));
		// Region 1, and the copy in the draw deck is not offered: the action that can be performed from
		// there is the one that spots 18.
		assertInZone(Zone.DECK, event2);
		assertFalse(scn.ShadowActionAvailable(event2));
		assertEquals(1, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();

		// No choice is put and no deck is shown: 2 cards are drawn.
		scn.ShadowPlayCard(event);
		assertEquals(14, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAMinionAnd18InThePoolTheTwoCardsMayBeDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(event);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		// 15 set + 1 companion + 2 for site 2
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(event));
		assertEquals(1, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(event);
		assertEquals(2, scn.ShadowGetMultipleChoices().size());
		assertTrue(scn.ShadowChoiceAvailable("draw 2 cards"));
		assertTrue(scn.ShadowChoiceAvailable("Play up to 2 minions"));

		scn.ShadowChoose("draw 2 cards");
		assertEquals(15, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.DISCARD, event);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAMinionAnd18InThePoolAMinionMayBePlayedFromTheDrawDeckAndNoPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var wargrider = scn.GetShadowCard("wargrider");
		var warg = scn.GetShadowCard("warg");
		var troop = scn.GetShadowCard("troop");
		var hunter = scn.GetShadowCard("hunter");
		var nelya = scn.GetShadowCard("nelya");
		var party = scn.GetShadowCard("party");
		scn.MoveCardsToHand(event);

		scn.StartGame();
		scn.MoveMinionsToTable(wargrider);
		// 15 set + 1 companion + 2 for site 2
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertEquals(18, scn.GetTwilight());
		assertInZone(Zone.DECK, warg, troop, hunter, nelya, party);
		assertEquals(1, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(event);
		assertEquals(2, scn.ShadowGetMultipleChoices().size());

		scn.ShadowChoose("Play up to 2 minions");
		// Nothing is removed for this action.
		assertEquals(18, scn.GetTwilight());
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, hunter, nelya));
		// Sharku's Warg costs 5 and the Warg-rider on the table could bear it, but it is no minion.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(warg, party));

		// 5 to play + 2 roaming
		scn.ShadowChooseCardBPFromSelection(hunter);
		assertEquals(11, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, hunter);

		// "Up to 2": the second may be left in the deck.
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, nelya));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(warg, party));
		scn.ShadowDeclineChoosing();

		assertEquals(11, scn.GetTwilight());
		assertInZone(Zone.DECK, troop, nelya, warg);
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		// No card was drawn.
		assertEquals(0, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, event);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMinionPrintedAt5ThatABurdenMakesCheaperToPlayStillCosts5() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var nelya = scn.GetShadowCard("nelya");
		var wight = scn.GetShadowCard("wight");
		var party = scn.GetShadowCard("party");
		scn.MoveCardsToHand(event);

		scn.StartGame();
		// A twilight minion on the table, which the Marsh Wight needs to be played at all.
		scn.MoveMinionsToTable(nelya);
		scn.AddBurdens(1);
		// 15 set + 1 companion + 2 for site 2
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertEquals(18, scn.GetTwilight());
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.DECK, wight, party);

		scn.ShadowPlayCard(event);
		scn.ShadowChoose("Play up to 2 minions");
		scn.ShadowDismissRevealedCards();
		// The Marsh Wight is printed at 5 and with one burden takes 4 to play; the Raiding Party is
		// printed at 4 and, roaming, takes 6.  It is the printed cost that the event asks about.
		assertTrue(scn.ShadowHasCardChoiceAvailable(wight));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(party));

		// 5 - 1 for the burden + 2 roaming
		scn.ShadowChooseCardBPFromSelection(wight);
		assertEquals(12, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, wight);

		scn.ShadowDismissRevealedCards();
		scn.ShadowDeclineChoosing();
		assertEquals(12, scn.GetTwilight());
		assertInZone(Zone.DISCARD, event);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NoMoreThanTwoMinionsArePlayedThoughAThirdCouldBePaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var troop = scn.GetShadowCard("troop");
		var hunter = scn.GetShadowCard("hunter");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(event);

		scn.StartGame();
		// 27 set + 1 companion + 2 for site 2
		scn.SetTwilight(27);
		scn.FreepsPass();

		assertEquals(30, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(event));

		scn.ShadowPlayCard(event);
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, hunter, nelya));

		// 5 to play
		scn.ShadowChooseCardBPFromSelection(nelya);
		assertEquals(25, scn.GetTwilight());

		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, hunter));
		// 6 to play + 2 roaming
		scn.ShadowChooseCardBPFromSelection(troop);
		assertEquals(17, scn.GetTwilight());

		// The Hunter would take 7 of the 17 left, and nothing more is asked.
		assertInZone(Zone.SHADOW_CHARACTERS, nelya, troop);
		assertInZone(Zone.DECK, hunter);
		assertInZone(Zone.DISCARD, event);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoMinionCosting5OrMoreInTheDrawDeckItIsStillPlayedAndTheDeckIsShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var party = scn.GetShadowCard("party");
		scn.MoveCardsToHand(event);
		scn.MoveCardsToShadowDiscard("troop", "hunter", "nelya", "uruks", "band", "sauron", "event2");

		scn.StartGame();
		// 15 set + 1 companion + 2 for site 2
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertEquals(18, scn.GetTwilight());
		// A minion costing 4 is all the deck has.
		assertInZone(Zone.DECK, party);
		assertTrue(scn.ShadowPlayAvailable(event));
		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(event);
		scn.ShadowDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		assertEquals(18, scn.GetTwilight());
		assertInZone(Zone.DECK, party);
		assertEquals(deck, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, event);
		// Shown twice with nothing to choose, and shuffled after each showing.
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite6StillInRegion2APoolOf18IsEnoughAndBothMinionsMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var troop = scn.GetShadowCard("troop");
		var hunter = scn.GetShadowCard("hunter");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.SkipToSite(5);

		// The turns skipped have drawn the deck into hand: the minions go back to be found.
		scn.MoveCardsToTopOfDeck(troop, hunter, nelya);
		scn.MoveCardsToHand(event);
		// 14 set + 1 companion + 3 for site 6
		scn.SetTwilight(14);
		scn.FreepsPass();

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(event));
		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(event);
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, hunter, nelya));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();

		// Having left the first, the player is still shown the deck for the second.
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, hunter, nelya));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();

		assertEquals(18, scn.GetTwilight());
		assertInZone(Zone.DECK, troop, hunter, nelya);
		assertEquals(deck, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, event);
		// The deck was looked through all the same: shown twice, and shuffled after each showing.
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite7InRegion3APoolOf24IsNotEnough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(6);

		scn.MoveCardsToTopOfDeck(troop);
		scn.MoveCardsToHand(event);
		// 17 set + 1 companion + 6 for site 7
		scn.SetTwilight(17);
		scn.FreepsPass();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(24, scn.GetTwilight());
		assertInZone(Zone.HAND, event);
		assertFalse(scn.ShadowPlayAvailable(event));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite7With24InThePoolAndAMinionOnlyTheTwoCardsAreDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		var hunter = scn.GetShadowCard("hunter");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.SkipToSite(6);

		scn.MoveCardsToTopOfDeck(troop, hunter, nelya);
		scn.MoveCardsToHand(event);
		scn.MoveMinionsToTable(soldier);
		// 17 set + 1 companion + 6 for site 7
		scn.SetTwilight(17);
		scn.FreepsPass();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(24, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(event));
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();

		// No choice is put and no deck is shown: 2 cards are drawn.
		scn.ShadowPlayCard(event);
		assertEquals(21, scn.GetTwilight());
		// the event gone, 2 drawn
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite7InRegion3APoolOf25IsEnoughAndOnlyFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var event2 = scn.GetShadowCard("event2");
		var troop = scn.GetShadowCard("troop");
		var hunter = scn.GetShadowCard("hunter");
		var sauron = scn.GetShadowCard("sauron");
		var party = scn.GetShadowCard("party");

		scn.StartGame();
		scn.SkipToSite(6);

		scn.MoveCardsToTopOfDeck(troop, hunter, sauron, party);
		scn.MoveCardsToHand(event);
		scn.MoveCardsToBottomOfDeck(event2);
		// 18 set + 1 companion + 6 for site 7
		scn.SetTwilight(18);
		scn.FreepsPass();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(25, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(event));
		// Not region 1: the copy in the draw deck is not offered.
		assertInZone(Zone.DECK, event2);
		assertFalse(scn.ShadowActionAvailable(event2));

		scn.ShadowPlayCard(event);
		scn.ShadowDismissRevealedCards();
		// At site 7 nothing here is roaming, and Sauron's 18 can be paid.  The Raiding Party still costs 4.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, hunter, sauron));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(party));

		// 6 to play
		scn.ShadowChooseCardBPFromSelection(troop);
		assertEquals(19, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, troop);

		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(hunter, sauron));
		// 18 to play
		scn.ShadowChooseCardBPFromSelection(sauron);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, sauron);
		assertInZone(Zone.DISCARD, event);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite3InRegion1ItCanBePerformedFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var event2 = scn.GetShadowCard("event2");
		var troop = scn.GetShadowCard("troop");
		var hunter = scn.GetShadowCard("hunter");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.SkipToSite(2);

		scn.MoveCardsToTopOfDeck(troop, hunter, nelya);
		scn.MoveCardsToBottomOfDeck(event);
		scn.MoveCardsToDiscard(event2);
		// 17 set + 1 companion + 0 for site 3
		scn.SetTwilight(17);
		scn.FreepsPass();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertInZone(Zone.DECK, event);
		assertTrue(scn.ShadowActionAvailable(event));
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();
		int opponentShuffles = scn.GetFreepsDeckShuffleCount();

		scn.ShadowUseCardAction(event);
		assertEquals(18, scn.GetTwilight());
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, hunter, nelya));

		// 5 to play; site 2 is Nelya's
		scn.ShadowChooseCardBPFromSelection(nelya);
		assertEquals(13, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);

		scn.ShadowDismissRevealedCards();
		// 6 to play + 2 roaming
		scn.ShadowChooseCardBPFromSelection(troop);
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, troop);

		// The event has left the deck for the discard pile, as an event played from hand would.
		assertInZone(Zone.DISCARD, event);
		assertEquals(hand, scn.GetShadowHandCount());
		// the event and the two minions
		assertEquals(deck - 3, scn.GetShadowDeckCount());
		assertTrue(scn.GetShadowDeckShuffleCount() > shuffles);
		assertEquals(opponentShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FromTheDrawDeckOnlyTheMinionsArePlayedEvenWithAMinionOnTheTable() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var event2 = scn.GetShadowCard("event2");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToDiscard(event2);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		// 15 set + 1 companion + 2 for site 2
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertEquals(18, scn.GetTwilight());
		assertInZone(Zone.DECK, event);
		assertTrue(scn.ShadowActionAvailable(event));
		assertEquals(0, scn.GetShadowHandCount());

		// From hand this board puts a choice; from the draw deck it does not, and the deck is shown at once.
		scn.ShadowUseCardAction(event);
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop));
		scn.ShadowDeclineChoosing();
		scn.ShadowDismissRevealedCards();
		scn.ShadowDeclineChoosing();

		// Nothing was removed and nothing drawn.
		assertEquals(18, scn.GetTwilight());
		assertEquals(0, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, event);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite4InRegion2ItCannotBePerformedFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var event = scn.GetShadowCard("event");
		var event2 = scn.GetShadowCard("event2");

		scn.StartGame();
		scn.SkipToSite(3);

		scn.MoveCardsToHand(event);
		scn.MoveCardsToBottomOfDeck(event2);
		// 14 set + 1 companion + 3 for site 4
		scn.SetTwilight(14);
		scn.FreepsPass();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		// The copy in hand can be played; the copy in the draw deck is not offered.
		assertTrue(scn.ShadowPlayAvailable(event));
		assertInZone(Zone.DECK, event2);
		assertFalse(scn.ShadowActionAvailable(event2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
