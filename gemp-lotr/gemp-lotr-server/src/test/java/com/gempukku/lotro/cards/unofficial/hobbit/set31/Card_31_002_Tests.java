package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_002_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. Followers in play and
	// the draw deck are laid after any SkipToSite, since followers return to the support area at each regroup.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("song", "31_2");

					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion, vitality 4; a Fellowship ability not used
					put("thorin3", "17_6");     // Thorin III, Stonehelm: a [dwarven] companion whose title only begins with "Thorin", vitality 3
					put("nori", "30_15");       // Nori, Of the Blue Mountains: [dwarven] companion, to bear a follower; a Maneuver ability not used
					put("bifur", "30_5");       // Bifur, Inarticulate: [dwarven] follower, twilight cost 1
					put("bofur", "30_6");       // Bofur, Encouraging Fellow: [dwarven] follower, twilight cost 2
					put("ori", "30_18");        // Ori, Dwarven Chronicler: [dwarven] follower, twilight cost 1, kept in hand
					put("gwaihir", "31_15");    // Gwaihir, Lord of the Eagles: a [gandalf] follower, twilight cost 1
					put("pot", "33_5");         // Dwarven Cooking Pot: a [dwarven] support-area possession of cost 1, no follower

					// For the draw deck
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion
					put("dain", "32_2");        // Dain Ironfoot, Lord of the Iron Hills: [dwarven] ally
					put("crown", "32_1");       // Crown of Erebor: [dwarven] artifact, support area
					put("dori", "30_8");        // Dori, Really a decent fellow: [dwarven] follower
					put("fury", "30_3");        // Battle Fury: a [dwarven] event
					put("gandalf", "30_26");    // Gandalf, Friend of Thorin: a [gandalf] companion, no [dwarven] card
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void DwarvenSongStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Dwarven Song
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: At sites 1 to 5, discard a [dwarven] follower with a twilight cost of X to play X [dwarven] cards
		 * 	(except events) from your draw deck. At any other site, heal Thorin twice.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("song");

		assertEquals("Dwarven Song", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtSite1DiscardingAFollowerOfCost1PlaysOneDwarvenCardThatIsNoEventFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var thorin = scn.GetFreepsCard("thorin");
		var nori = scn.GetFreepsCard("nori");
		var bifur = scn.GetFreepsCard("bifur");
		var bofur = scn.GetFreepsCard("bofur");
		var ori = scn.GetFreepsCard("ori");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var pot = scn.GetFreepsCard("pot");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		var crown = scn.GetFreepsCard("crown");
		var dori = scn.GetFreepsCard("dori");
		var fury = scn.GetFreepsCard("fury");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(thorin, nori);

		scn.StartGame();
		scn.MoveCardsToSupportArea(bifur, gwaihir, pot);
		scn.AttachCardsTo(nori, bofur);
		scn.MoveCardsToHand(song, ori);
		scn.SetFreepsDrawDeck(balin, dain, crown, dori, fury, gandalf);
		scn.AddWoundsToChar(thorin, 2);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, bifur, gwaihir, pot);
		assertAttachedTo(bofur, nori);
		assertInHand(song, ori);
		assertInZone(Zone.DECK, balin, dain, crown, dori, fury, gandalf);
		assertEquals(2, scn.GetWoundsOn(thorin));
		int shuffles = scn.GetFreepsDeckShuffleCount();

		assertTrue(scn.FreepsPlayAvailable(song));
		scn.FreepsPlayCard(song);
		// [dwarven] followers in play, in the support area and borne; not a [gandalf] follower, a [dwarven] card that
		// is no follower, nor a [dwarven] follower in hand.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bifur, bofur));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gwaihir, pot, ori));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(bifur);
		assertInDiscard(bifur);

		// The draw deck is shown, and then [dwarven] cards of four kinds may be played; not the [dwarven] event, nor
		// Gandalf. The choice from a draw deck runs from 0 (Q-0019).
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, dain, crown, dori));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(fury, gandalf));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(crown);

		// X is 1: the Crown alone is played, and the deck shuffled as the Crown leaves it and again after the look.
		assertInZone(Zone.SUPPORT, crown);
		assertInZone(Zone.DECK, balin, dain, dori, fury, gandalf);
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		// Not at "any other site": Thorin is not healed.
		assertEquals(2, scn.GetWoundsOn(thorin));
		assertAttachedTo(bofur, nori);
		assertInDiscard(song);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSite5DiscardingAFollowerOfCost2PlaysTwoDwarvenCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var thorin = scn.GetFreepsCard("thorin");
		var nori = scn.GetFreepsCard("nori");
		var bifur = scn.GetFreepsCard("bifur");
		var bofur = scn.GetFreepsCard("bofur");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		var crown = scn.GetFreepsCard("crown");
		scn.MoveCompanionsToTable(thorin, nori);

		scn.StartGame();
		scn.SkipToSite(5);
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.MoveCardsToSupportArea(bifur);
		scn.AttachCardsTo(nori, bofur);
		scn.MoveCardsToHand(song);
		scn.SetFreepsDrawDeck(balin, dain, crown);
		scn.AddWoundsToChar(thorin, 2);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, bifur);
		assertAttachedTo(bofur, nori);
		assertInHand(song);
		assertInZone(Zone.DECK, balin, dain, crown);
		assertEquals(2, scn.GetWoundsOn(thorin));

		scn.FreepsPlayCard(song);
		scn.FreepsChooseCard(bofur);
		assertInDiscard(bofur);

		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(crown);
		assertInZone(Zone.SUPPORT, crown);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(dain);
		assertInZone(Zone.SUPPORT, dain);

		// X is 2: two cards played and no third; Bifur, not chosen, stays.
		assertInZone(Zone.DECK, balin);
		assertInZone(Zone.SUPPORT, bifur);
		// Site 5 is not "any other site": Thorin is not healed.
		assertEquals(2, scn.GetWoundsOn(thorin));
		assertInDiscard(song);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoDwarvenCardToPlayInTheDrawDeckTheSongIsNotPlayableAtSites1To5() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var bifur = scn.GetFreepsCard("bifur");
		var fury = scn.GetFreepsCard("fury");
		var gandalf = scn.GetFreepsCard("gandalf");

		scn.StartGame();
		scn.MoveCardsToSupportArea(bifur);
		scn.MoveCardsToHand(song);
		scn.SetFreepsDrawDeck(fury, gandalf);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, bifur);
		assertInZone(Zone.DECK, fury, gandalf);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		// A [dwarven] event and a card of another culture: nothing the Song could play.  A Hobbit-set play from the draw deck with nothing it could play is withheld (Q-0019; the orchestrator on Q-0406 and Q-0418):
		// no follower is discarded for a look that finds nothing.
		assertFalse(scn.FreepsPlayAvailable(song));
		assertInZone(Zone.SUPPORT, bifur);
		assertInZone(Zone.HAND, song);
		assertInZone(Zone.DECK, fury, gandalf);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSites1To5CannotBePlayedWithoutADwarvenFollowerInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var ori = scn.GetFreepsCard("ori");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var pot = scn.GetFreepsCard("pot");
		var balin = scn.GetFreepsCard("balin");

		scn.StartGame();
		scn.MoveCardsToSupportArea(gwaihir, pot);
		scn.MoveCardsToHand(song, ori);
		scn.SetFreepsDrawDeck(balin);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, gwaihir, pot);
		assertInHand(song, ori);

		assertFalse(scn.FreepsPlayAvailable(song));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSite6HealsThorinTwiceAndDiscardsNoFollower() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var thorin = scn.GetFreepsCard("thorin");
		var thorin3 = scn.GetFreepsCard("thorin3");
		var bifur = scn.GetFreepsCard("bifur");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(thorin, thorin3);

		scn.StartGame();
		scn.SkipToSite(6);
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.MoveCardsToSupportArea(bifur);
		scn.MoveCardsToHand(song);
		scn.SetFreepsDrawDeck(balin);
		scn.AddWoundsToChar(thorin, 3);
		scn.AddWoundsToChar(thorin3, 1);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, bifur);
		assertInZone(Zone.DECK, balin);
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetWoundsOn(thorin3));
		int shuffles = scn.GetFreepsDeckShuffleCount();

		assertTrue(scn.FreepsPlayAvailable(song));
		scn.FreepsPlayCard(song);

		// Two wounds healed; Thorin III, whose title only begins with Thorin, none.
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetWoundsOn(thorin3));
		// No follower discarded, no draw deck looked through.
		assertInZone(Zone.SUPPORT, bifur);
		assertInZone(Zone.DECK, balin);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertInDiscard(song);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSite6WithThorinUnwoundedTheSongIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var thorin = scn.GetFreepsCard("thorin");
		var nori = scn.GetFreepsCard("nori");
		scn.MoveCompanionsToTable(thorin, nori);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToHand(song);
		scn.AddWoundsToChar(nori, 1);
		scn.FreepsRefreshDecision();
		assertInHand(song);
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetWoundsOn(nori));

		assertTrue(scn.FreepsPlayAvailable(song));
		scn.FreepsPlayCard(song);

		assertEquals(1, scn.GetWoundsOn(nori));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertInDiscard(song);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
