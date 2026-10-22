package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_117_Tests
{
	// The King path with The Dimholt as its site 3.  Nothing on the way there asks anything of the fellowships
	// laid here (Edoras Hall, King's Tent).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("knight1", "5_35");     // Gondorian Knight: [gondor] Man companion, no text
					put("knight2", "5_35");
					put("knight3", "5_35");
					put("knight4", "5_35");     // a companion in the draw deck who is not enduring

					// Oathbreaker: [gondor] Wraith companion, enduring; "To play, add a threat"; a skirmish ability only
					put("oathbreaker", "8_41");
					// Merry, Master Holbytla: [rohan] Hobbit companion, valiant and enduring; no text that answers a play
					// from the deck.  Every Decipher enduring companion is a [gondor] Wraith: he is the one of another
					// culture and race.
					put("merry", "103_85");
				}},
				new HashMap<>()
				{{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");      // The Dimholt
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheDimholtStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: The Dimholt
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 3K
		 * Shadow Number: 1
		 * Direction: Right
		 * Game Text: <b>Underground</b>. <b>Sanctuary</b>. <b>Fellowship:</b> Add 3 threats to play an enduring
		 * companion from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("The Dimholt", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(3, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void AddingThreeThreatsPlaysAnEnduringCompanionOfEitherCultureFromTheDrawDeckAndShufflesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var knight3 = scn.GetFreepsCard("knight3");
		var knight4 = scn.GetFreepsCard("knight4");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var merry = scn.GetFreepsCard("merry");
		// Four companions: after the 3 threats a fourth can still be added, so Oathbreaker's own cost can be paid.
		scn.MoveCompanionsToTable(knight1, knight2, knight3);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsDrawDeck(oathbreaker, merry, knight4);
		// The fellowship decision the skip ended on was worked out before the deck was set.
		scn.FreepsRefreshDecision();
		var site = scn.GetCurrentSite();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.DECK, oathbreaker, merry, knight4);
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(site));

		scn.FreepsUseCardAction(site);
		// The 3 threats are the cost, paid before the deck is looked through.
		assertEquals(3, scn.GetThreats());
		scn.FreepsDismissRevealedCards();

		// An enduring companion of each culture and race may be played; a companion who is not enduring may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(oathbreaker, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight4));
		// A play from a draw deck that has been looked through may find nothing (F-0042).
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(merry);

		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertInZone(Zone.DECK, oathbreaker, knight4);
		assertEquals(3, scn.GetThreats());
		// The deck looked through is shuffled as the played card leaves it and again by the action's own shuffle;
		// the Shadow player's is not.
		assertEquals(freepsShuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoEnduringCompanionInTheDeckItIsStillUsedAndTheThreatsAreAddedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var knight4 = scn.GetFreepsCard("knight4");
		// Three companions and no threat: the 3 threats can be added, and no more.
		scn.MoveCompanionsToTable(knight1, knight2);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsDrawDeck(knight4);
		// The fellowship decision the skip ended on was worked out before the deck was set.
		scn.FreepsRefreshDecision();
		var site = scn.GetCurrentSite();

		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.DECK, knight4);
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		// Failing to find a card in a draw deck costs nothing (Q-0019), so the action is offered.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(site));

		scn.FreepsUseCardAction(site);
		assertEquals(3, scn.GetThreats());
		// The deck is shown, and nothing in it can be chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight4, false));
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DECK, knight4);
		// One shuffle, the action's own, with nothing played out of the deck.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhenOnlyTwoOfTheThreeThreatsCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var merry = scn.GetFreepsCard("merry");
		// Three companions, as on the board where it is offered, but one threat already: only two more can be
		// added (CR 4.2 "threats"), and a cost is judged payable in full before it begins (F-0425).
		scn.MoveCompanionsToTable(knight1, knight2);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsDrawDeck(merry);
		scn.AddThreats(1);
		// The fellowship decision the skip ended on was worked out before these cheats.
		scn.FreepsRefreshDecision();
		var site = scn.GetCurrentSite();

		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.DECK, merry);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(site));
	}
}
