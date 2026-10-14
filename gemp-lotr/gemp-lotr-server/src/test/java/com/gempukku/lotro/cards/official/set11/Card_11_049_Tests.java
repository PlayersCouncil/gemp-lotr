package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_049_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("turn", "11_49");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSmeagolScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("turn", "11_49");
					// Sméagol, Slinker: "To play, add a burden", so he is put on the table; his skirmish ability
					// is not used
					put("smeagol", "5_29");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship-block path: each player holds one site of each number, so the site played is his own site of
	// the next number, with nothing to choose.
	protected VirtualTableScenario GetSpotScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("turn", "11_49");
					// Sméagol, Slinker: [gollum] companion; "To play, add a burden"; his skirmish ability is not used
					put("smeagol", "5_29");
					// Déagol, Fateful Finder: a [gollum] follower of another title; his skirmish ability is not used
					put("deagol", "13_46");
					// Gollum, Vile Creature: a [gollum] minion of another title; a skirmish ability only
					put("gollum", "7_59");
					// Smeagol, Once Like Us: the [shire] Sméagol; "To play, add a burden"; an assignment ability only
					put("shiresmeagol", "103_116");
					// Sam, Son of Hamfast: a [shire] companion of another title; a fellowship ability only used on purpose
					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OneGoodTurnDeservesAnotherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: One Good Turn Deserves Another
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship or Regroup
		 * Game Text: Spot Sméagol to play the fellowship's next site. Then you may add a burden to take this card back into hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("turn");

		assertEquals("One Good Turn Deserves Another", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilTheGollumSmeagolIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSpotScenario();

		var turn = scn.GetFreepsCard("turn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var deagol = scn.GetFreepsCard("deagol");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(turn, smeagol);
		scn.MoveCardsToSupportArea(deagol);

		scn.StartGame();
		// The other player's Gollum, laid in the fellowship phase to be spotted there (Q-0228).
		scn.MoveMinionsToTable(gollum);
		scn.FreepsRefreshDecision();

		// A [gollum] card and a [gollum] character of other titles, and no Sméagol.
		assertInZone(Zone.SUPPORT, deagol);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(turn));

		scn.FreepsPlayCard(smeagol);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.FreepsPlayAvailable(turn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilTheShireSmeagolIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSpotScenario();

		var turn = scn.GetFreepsCard("turn");
		var shireSmeagol = scn.GetFreepsCard("shiresmeagol");
		var sam = scn.GetFreepsCard("sam");
		var site2 = scn.GetFreepsSite(2);
		scn.MoveCardsToHand(turn, shireSmeagol);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		// A [shire] companion of another title, and no Sméagol.
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(turn));

		scn.FreepsPlayCard(shireSmeagol);
		assertInZone(Zone.FREE_CHARACTERS, shireSmeagol);
		assertTrue(scn.FreepsPlayAvailable(turn));

		assertNull(scn.GetSite(2));
		scn.FreepsPlayCard(turn);
		assertSame(site2, scn.GetSite(2));
		assertTrue(scn.FreepsDecisionAvailable("add a burden"));
		scn.FreepsChooseNo();
		assertInZone(Zone.DISCARD, turn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AddingABurdenTakesTheEventBackIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSmeagolScenario();

		var turn = scn.GetFreepsCard("turn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(turn);

		scn.StartGame();

		// Only site 1 is on the adventure path.
		var freepsSite2 = scn.GetFreepsSite(2);
		assertNull(scn.GetSite(2));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsPlayAvailable(turn));

		scn.FreepsPlayCard(turn);
		// The fellowship's next site is played, from its player's own adventure deck, and then the question
		// is put.
		assertSame(freepsSite2, scn.GetSite(2));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("add a burden"));

		scn.FreepsChooseYes();
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.HAND, turn);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningTheBurdenLeavesTheEventInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSmeagolScenario();

		var turn = scn.GetFreepsCard("turn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(turn);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.FreepsPlayCard(turn);
		assertTrue(scn.FreepsDecisionAvailable("add a burden"));

		scn.FreepsChooseNo();
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.DISCARD, turn);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void InTheRegroupPhaseItPlaysTheNextSiteAndCanBeTakenBackIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSpotScenario();

		var turn = scn.GetFreepsCard("turn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(turn);

		var site3 = scn.GetFreepsSite(3);
		var shadowSite3 = scn.GetShadowSite(3);

		scn.StartGame();
		// No minion is played, so the turn goes from the move to site 2 straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, site3, shadowSite3);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(turn));

		scn.FreepsPlayCard(turn);
		assertSame(site3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite3);
		assertNull(scn.GetSite(4));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("add a burden"));

		scn.FreepsChooseYes();
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.HAND, turn);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ReplacesTheOpponentsSiteLyingNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSpotScenario();

		var turn = scn.GetFreepsCard("turn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);

		var site4 = scn.GetFreepsSite(4);
		var shadowSite4 = scn.GetShadowSite(4);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsHand(turn);

		// The other player's Balin's Tomb lies at site 4, as it does where he has played his next site himself.
		// Both players hold the same nine sites here, so the site that replaces it is its twin.
		scn.MoveCardToAdventurePath(shadowSite4);

		assertSame(shadowSite4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, site4);
		assertEquals(3, scn.GetCurrentSiteNumber());

		scn.FreepsPlayCard(turn);

		// His own site 4 lies on the path, and the other player's is back in its owner's adventure deck.
		assertSame(site4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_PATH, site4);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite4);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("add a burden"));
		scn.FreepsChooseNo();
		assertInZone(Zone.DISCARD, turn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
