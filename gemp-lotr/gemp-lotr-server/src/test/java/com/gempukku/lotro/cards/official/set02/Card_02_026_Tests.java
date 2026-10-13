package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_026_Tests
{

	// The Fellowship-block path: site 2 is Ettenmoors (plains), site 3 Rivendell Valley (a sanctuary), and
	// site 4 Balin's Tomb, the first underground site.
	protected VirtualTableScenario GetFellowshipScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("speak", "2_26");
					put("speak2", "2_26");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk
					// Barliman Butterbur: a [gandalf] ally who is not Gandalf; a fellowship ability used only on purpose
					put("barliman", "1_70");
					// Gandalf's Cart and Gandalf's Pipe: cards for the draw deck
					put("fodder1", "1_73");
					put("fodder2", "1_74");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A Shadows-block path: the sites have no printed number, and whoever plays the next site chooses it.
	// Abandoned Mine Shaft 13_185 (under "site2") is underground; Crags of Emyn Muil 11_234 (under "site3") is
	// a mountain.
	protected VirtualTableScenario GetShadowsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("speak", "2_26");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk
					// Gandalf's Cart and Gandalf's Pipe: cards for the draw deck
					put("fodder1", "1_73");
					put("fodder2", "1_74");
				}},
				VirtualTableScenario.ShadowsSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void SpeakFriendandEnterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Speak "Friend" and Enter
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship or Regroup
		 * Game Text: <b>Fellowship</b> <i>or</i> <b>Regroup:</b> Spot Gandalf to play the fellowship's next site (replacing opponent's site if necessary). Draw a card if you play an underground site.
		*/

		var scn = GetFellowshipScenario();

		var card = scn.GetFreepsCard("speak");

		assertEquals("Speak \"Friend\" and Enter", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpeakFriendandEnterRequiresGandalfToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipScenario();

		var speak = scn.GetFreepsCard("speak");
		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(speak, gandalf);

		scn.StartGame();

		// A [gandalf] ally is in play, and no Gandalf.
		assertInZone(Zone.SUPPORT, barliman);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(speak));

		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(speak));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SpeakFriendandEnterCanBeUsedInFellowshipPhaseToPlayNextSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipScenario();

		var speak = scn.GetFreepsCard("speak");
		scn.MoveCardsToHand(speak);
		scn.MoveCompanionsToTable("gandalf");

		var site2 = scn.GetFreepsSite(2);
		var shadowSite2 = scn.GetShadowSite(2);

		scn.StartGame();

		// Only site 1 is on the adventure path; each player's site 2 is in his adventure deck.
		assertNull(scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, site2, shadowSite2);
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck >= 2);

		scn.FreepsPlayCard(speak);

		// On a path of numbered sites there is one site 2 in his adventure deck, and nothing to choose.
		assertFalse(scn.FreepsDecisionAvailable("Choose site to play"));
		// Site 2 is his own, where a move would have had the Shadow player lay his.  Site 3 is still unlaid,
		// and the fellowship has not moved.
		assertSame(site2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		assertNull(scn.GetSite(3));
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.DISCARD, speak);
		// Ettenmoors is not underground: no card is drawn.
		assertFalse(scn.HasKeyword(site2, Keyword.UNDERGROUND));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SpeakFriendandEnterCanBeUsedInRegroupPhaseToPlayNextSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipScenario();

		var speak = scn.GetFreepsCard("speak");
		scn.MoveCardsToHand(speak);
		scn.MoveCompanionsToTable("gandalf");

		var site3 = scn.GetFreepsSite(3);
		var shadowSite3 = scn.GetShadowSite(3);

		scn.StartGame();

		// No minion is played, so the turn goes from the move to site 2 straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, site3, shadowSite3);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(speak));
		int pool = scn.GetTwilight();
		assertEquals(1, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck >= 2);

		scn.FreepsPlayCard(speak);

		assertSame(site3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_PATH, site3);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite3);
		assertNull(scn.GetSite(4));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(pool + 1, scn.GetTwilight());
		assertInZone(Zone.DISCARD, speak);
		// Rivendell Valley is not underground: no card is drawn.
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void SpeakFriendandEnterDrawsACardIfSiteIsUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipScenario();

		var speak = scn.GetFreepsCard("speak");
		scn.MoveCompanionsToTable("gandalf");

		var site4 = scn.GetFreepsSite(4);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsHand(speak);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(4));
		assertTrue(scn.HasKeyword(site4, Keyword.UNDERGROUND));
		assertEquals(1, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck >= 2);

		scn.FreepsPlayCard(speak);

		// Balin's Tomb is underground: the event has left the hand and one card has come into it.
		assertSame(site4, scn.GetSite(4));
		assertInZone(Zone.DISCARD, speak);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SpeakFriendandEnterReplacesOpponentSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipScenario();

		var speak = scn.GetFreepsCard("speak");
		scn.MoveCompanionsToTable("gandalf");

		var site4 = scn.GetFreepsSite(4);
		var shadowSite4 = scn.GetShadowSite(4);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsHand(speak);

		// The other player's Balin's Tomb lies at site 4, as it does where he has played his next site himself.
		// Both players hold the same nine sites here, so the site that replaces it is its twin.
		scn.MoveCardToAdventurePath(shadowSite4);

		assertSame(shadowSite4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, site4);
		assertInZone(Zone.ADVENTURE_PATH, shadowSite4);
		assertEquals(1, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck >= 2);

		scn.FreepsPlayCard(speak);

		// His own site 4 lies on the path, and the other player's is back in its owner's adventure deck.
		assertSame(site4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_PATH, site4);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite4);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DISCARD, speak);
		// He played an underground site: one card drawn.
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithHisOwnSiteAlreadyNextNoSiteIsPlayedAndNoCardIsDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipScenario();

		var speak = scn.GetFreepsCard("speak");
		var speak2 = scn.GetFreepsCard("speak2");
		scn.MoveCompanionsToTable("gandalf");

		var site4 = scn.GetFreepsSite(4);
		var shadowSite4 = scn.GetShadowSite(4);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsHand(speak, speak2);

		scn.FreepsPlayCard(speak);
		// The first copy has laid his own Balin's Tomb at site 4 and drawn a card for it.
		assertSame(site4, scn.GetSite(4));
		assertEquals(1, scn.GetTwilight());
		assertEquals(2, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck >= 1);

		// An event may be played though it will do nothing.
		assertTrue(scn.FreepsPlayAvailable(speak2));
		scn.FreepsPlayCard(speak2);

		// The site that lies next is his own and stays; he has no other site 4 to play.  No site was played,
		// so no card is drawn, underground though the next site is.
		assertSame(site4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_PATH, site4);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite4);
		assertInZone(Zone.DISCARD, speak2);
		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SpeakFriendandEnterCanBeUsedInFellowshipPhaseToPlayNextSiteOnShadowsPath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsScenario();

		var speak = scn.GetFreepsCard("speak");
		scn.MoveCardsToHand(speak);
		scn.MoveCompanionsToTable("gandalf");

		var site1 = scn.GetFreepsSite("site1");
		var mineShaft = scn.GetFreepsSite("site2");

		scn.StartGame(site1);

		assertInZone(Zone.ADVENTURE_DECK, mineShaft);
		assertNull(scn.GetSite(2));
		assertEquals(1, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck >= 2);

		scn.FreepsPlayCard(speak);
		// He chooses among the eight sites left in his adventure deck, and must choose one.
		assertTrue(scn.FreepsDecisionAvailable("Choose site to play"));
		assertEquals(8, scn.FreepsGetBPChoiceCount());
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCardBPFromSelection(mineShaft);
		assertInZone(Zone.ADVENTURE_PATH, mineShaft);
		assertSame(mineShaft, scn.GetSite(2));
		assertNull(scn.GetSite(3));
		assertEquals(1, scn.GetTwilight());
		// Abandoned Mine Shaft is underground: the event has left the hand and one card has come into it.
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SpeakFriendandEnterCanBeUsedInRegroupPhaseToPlayNextSiteOnShadowsPath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsScenario();

		var speak = scn.GetFreepsCard("speak");
		scn.MoveCardsToHand(speak);
		scn.MoveCompanionsToTable("gandalf");

		var site1 = scn.GetFreepsSite("site1");
		var crags = scn.GetFreepsSite("site3");

		scn.StartGame(site1);
		scn.FreepsPass();
		// The fellowship moves, and the Shadow player lays his Abandoned Mine Shaft as site 2.
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));

		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_DECK, crags);
		assertNull(scn.GetSite(3));
		assertTrue(scn.FreepsPlayAvailable(speak));
		assertEquals(1, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck >= 2);

		scn.FreepsPlayCard(speak);
		assertTrue(scn.FreepsDecisionAvailable("Choose site to play"));
		scn.FreepsChooseCardBPFromSelection(crags);
		assertInZone(Zone.ADVENTURE_PATH, crags);
		assertSame(crags, scn.GetSite(3));
		// Crags of Emyn Muil is a mountain and not underground: no card is drawn.
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
