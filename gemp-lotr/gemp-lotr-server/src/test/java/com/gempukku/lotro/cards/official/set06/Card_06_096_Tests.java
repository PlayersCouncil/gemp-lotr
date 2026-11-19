package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_096_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("news", "6_96");
					put("guard", "5_83");        // Household Guard: [rohan] Man companion, Valiant and no other text
					put("guard2", "5_83");       // a second Household Guard, left unmounted
					put("sword", "4_291");       // Sword of Rohan: a [rohan] hand weapon, no mount; its text acts only against an Uruk-hai
					put("rider", "4_286");       // Rider of Rohan: [rohan] Man companion; a skirmish ability used only on purpose
					put("herdsman", "7_247");    // Rohirrim Herdsman: [rohan] Man ally (home 3T); a regroup ability used only on purpose
					put("mount1", "4_287");      // Rider's Mount: [rohan] mount, bearer must be a [rohan] Man; its text acts only in skirmishes
					put("mount2", "4_287");
					put("mount3", "4_287");
					put("knight", "5_35");       // Gondorian Knight: [gondor] Man companion, Knight and no other text
					put("knightsmount", "8_40"); // Knight's Mount: [gondor] mount on a knight; a skirmish-win trigger only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NewsFromtheMarkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: News From the Mark
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Spot 3 mounted [rohan] Men to make the move limit for this turn +2. Each Shadow player may draw 6 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("news");

		assertEquals("News From the Mark", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ThreeMountedRohanMenMakeTheMoveLimitPlusTwoForTheTurnAndTheShadowPlayerMayDrawSix() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var news = scn.GetFreepsCard("news");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var rider = scn.GetFreepsCard("rider");
		var herdsman = scn.GetFreepsCard("herdsman");
		var mount1 = scn.GetFreepsCard("mount1");
		var mount2 = scn.GetFreepsCard("mount2");
		var mount3 = scn.GetFreepsCard("mount3");
		var knight = scn.GetFreepsCard("knight");
		var knightsmount = scn.GetFreepsCard("knightsmount");
		scn.MoveCompanionsToTable(guard, guard2, rider, knight);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(guard, mount1);
		scn.AttachCardsTo(rider, mount2);
		scn.AttachCardsTo(herdsman, mount3);
		scn.AttachCardsTo(knight, knightsmount);
		scn.MoveCardsToHand(news);

		scn.StartGame();

		// Exactly three mounted [rohan] Men: two companions of different titles and an ally. The unmounted
		// [rohan] Man and the mounted [gondor] Man beside them are no part of the count.
		assertAttachedTo(mount1, guard);
		assertAttachedTo(mount2, rider);
		assertAttachedTo(mount3, herdsman);
		assertAttachedTo(knightsmount, knight);
		assertInHand(news);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowHandCount());
		int freepsDeck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.FreepsPlayAvailable(news));

		scn.FreepsPlayCard(news);

		assertEquals(4, scn.GetMoveLimit());
		// The Shadow player is the one asked, and takes all 6: the Rule of 4 binds a player in his own
		// fellowship phase only, and this is the Free Peoples player's.
		assertTrue(scn.ShadowDecisionAvailable("draw 6 cards"));
		scn.ShadowChooseYes();
		assertEquals(6, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 6, scn.GetShadowDeckCount());
		// The Free Peoples player draws nothing: his hand is only the event less.
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertInDiscard(news);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();          // move 1 -> 2
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();  // move 2 -> 3
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// Still in force in the Shadow phase after the second move.
		assertEquals(4, scn.GetMoveLimit());
		assertEquals(2, scn.GetMoveCount());

		// Gone with the turn: 2 on the other player's turn.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void TheShadowPlayerMayDeclineTheDrawAndTheMoveLimitIsStillRaised() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var news = scn.GetFreepsCard("news");
		var guard = scn.GetFreepsCard("guard");
		var rider = scn.GetFreepsCard("rider");
		var herdsman = scn.GetFreepsCard("herdsman");
		var mount1 = scn.GetFreepsCard("mount1");
		var mount2 = scn.GetFreepsCard("mount2");
		var mount3 = scn.GetFreepsCard("mount3");
		scn.MoveCompanionsToTable(guard, rider);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(guard, mount1);
		scn.AttachCardsTo(rider, mount2);
		scn.AttachCardsTo(herdsman, mount3);
		scn.MoveCardsToHand(news);

		scn.StartGame();

		assertAttachedTo(mount1, guard);
		assertAttachedTo(mount2, rider);
		assertAttachedTo(mount3, herdsman);
		assertEquals(0, scn.GetShadowHandCount());
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsPlayCard(news);

		assertTrue(scn.ShadowDecisionAvailable("draw 6 cards"));
		scn.ShadowChooseNo();
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(4, scn.GetMoveLimit());
		assertInDiscard(news);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TwoMountedRohanMenBesideAnUnmountedRohanManAndAMountedGondorManCannotPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var news = scn.GetFreepsCard("news");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var rider = scn.GetFreepsCard("rider");
		var herdsman = scn.GetFreepsCard("herdsman");
		var mount1 = scn.GetFreepsCard("mount1");
		var mount2 = scn.GetFreepsCard("mount2");
		var knight = scn.GetFreepsCard("knight");
		var knightsmount = scn.GetFreepsCard("knightsmount");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(guard, guard2, rider, knight);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(guard, mount1);
		scn.AttachCardsTo(rider, mount2);
		scn.AttachCardsTo(knight, knightsmount);
		scn.AttachCardsTo(guard2, sword);
		scn.MoveCardsToHand(news);

		scn.StartGame();

		// Two mounted [rohan] Men; the second Guard (bearing a possession that is no mount) and the Herdsman
		// are [rohan] Men with no mount, the Knight a mounted Man of [gondor].
		assertAttachedTo(sword, guard2);
		assertAttachedTo(mount1, guard);
		assertAttachedTo(mount2, rider);
		assertAttachedTo(knightsmount, knight);
		assertInHand(news);
		assertFalse(scn.FreepsPlayAvailable(news));
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
