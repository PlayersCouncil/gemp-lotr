package com.gempukku.lotro.cards.official.set08;

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

public class Card_08_029_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("far", "8_29");
					put("smeagol", "5_29");     // Sméagol, Slinker: [gollum] companion; to play, add a burden
					put("smeagol2", "103_116"); // Smeagol, Once Like Us: the [shire] Sméagol; his one ability is an Assignment one
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is no Sméagol
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver and an assignment phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StillFarAheadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Still Far Ahead
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship or Regroup
		 * Game Text: Spot Sméagol to play the fellowship's next site (replacing opponent's site if necessary).
		 * The Shadow number of the fellowship's next site is -1 until the end of the turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("far");

		assertEquals("Still Far Ahead", card.getBlueprint().getTitle());
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
	public void SpotsSmeagolToPlayHisOwnNextSiteWhoseShadowNumberIsOneLessForTheMoveThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var far = scn.GetFreepsCard("far");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(far);

		scn.StartGame();

		var freepsSite2 = scn.GetFreepsSite(2);
		var shadowSite2 = scn.GetShadowSite(2);
		assertNull(scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite2, shadowSite2);
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsPlayAvailable(far));

		scn.FreepsPlayCard(far);

		// His own site 2, Ettenmoors (Shadow number 2), is laid, and it reads one less.  The fellowship has not moved.
		assertSame(freepsSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		assertEquals(1, scn.GetTwilightCost(freepsSite2));
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DISCARD, far);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();
		// 1 for the site + 2 companions (Frodo and Sméagol).
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilSmeagolIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var far = scn.GetFreepsCard("far");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(far, smeagol);

		scn.StartGame();

		// A Ring-bound companion who is no Sméagol is in play.
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertInHand(smeagol);
		assertNull(scn.GetSite(2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(far));

		scn.FreepsPlayCard(smeagol);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.FreepsPlayAvailable(far));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotPlayableInTheManeuverOrAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var far = scn.GetFreepsCard("far");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(far);
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInHand(far);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(far));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(far));
	}

	@Test
	public void TheShireSmeagolIsSpottedInTheRegroupPhaseAndTheSiteStaysOneLessUntilTheEndOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var far = scn.GetFreepsCard("far");
		var smeagol2 = scn.GetFreepsCard("smeagol2");
		scn.MoveCompanionsToTable(smeagol2);
		scn.MoveCardsToHand(far);

		var freepsSite4 = scn.GetFreepsSite(4);
		var shadowSite4 = scn.GetShadowSite(4);

		scn.StartGame();
		scn.SkipToSite(2);
		// No minion is played, so the turn goes from the move to site 3 straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite4, shadowSite4);
		assertInHand(far);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(far));

		scn.FreepsPlayCard(far);

		// Balin's Tomb, Shadow number 3, reads 2; the fellowship stays at site 3.
		assertSame(freepsSite4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite4);
		assertEquals(2, scn.GetTwilightCost(freepsSite4));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DISCARD, far);

		scn.SkipToMovementDecision();
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());
		scn.FreepsChooseToMove();

		// The second move: 2 for the site + 2 companions.  Still 2 in the Shadow phase that follows it.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetTwilight());
		assertEquals(2, scn.GetTwilightCost(freepsSite4));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Back to 3 on the next turn.
		scn.SkipToOtherPlayersTurn();
		assertEquals(3, scn.GetTwilightCost(freepsSite4));
	}

	@Test
	public void ASiteThatBecomesTheNextSiteAfterTheMoveKeepsItsPrintedShadowNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var far = scn.GetFreepsCard("far");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(far);

		var freepsSite4 = scn.GetFreepsSite(4);
		var shadowSite5 = scn.GetShadowSite(5);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SkipToPhase(Phase.REGROUP);
		assertInHand(far);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		scn.FreepsPlayCard(far);
		assertEquals(2, scn.GetTwilightCost(freepsSite4));

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// CR 4.2 "event": the effect is on the site that was next when the event was played.  Site 5, The Bridge of
		// Khazad-dûm, is the fellowship's next site now, and keeps its Shadow number of 6.
		assertInZone(Zone.ADVENTURE_DECK, shadowSite5);
		scn.MoveCardToAdventurePath(shadowSite5);
		assertSame(shadowSite5, scn.GetSite(5));
		assertEquals(6, scn.GetTwilightCost(shadowSite5));
		assertEquals(2, scn.GetTwilightCost(freepsSite4));
	}

	@Test
	public void ReplacesTheOpponentsSiteLyingNextAndTheSiteLaidIsOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var far = scn.GetFreepsCard("far");
		scn.MoveCompanionsToTable("smeagol");

		var freepsSite4 = scn.GetFreepsSite(4);
		var shadowSite4 = scn.GetShadowSite(4);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsHand(far);

		// The other player's site 4 lies next, as where he has played his next site himself.
		scn.MoveCardToAdventurePath(shadowSite4);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertSame(shadowSite4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite4);
		assertTrue(scn.FreepsPlayAvailable(far));

		scn.FreepsPlayCard(far);

		// His own site 4 lies on the path, one less, and the other player's is back in its owner's adventure deck.
		assertSame(freepsSite4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite4);
		assertEquals(2, scn.GetTwilightCost(freepsSite4));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DISCARD, far);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithHisOwnSiteAlreadyNextNoSiteIsPlayedAndThatSiteIsStillOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var far = scn.GetFreepsCard("far");
		scn.MoveCompanionsToTable("smeagol");
		scn.MoveCardsToHand(far);

		var freepsSite2 = scn.GetFreepsSite(2);
		var shadowSite2 = scn.GetShadowSite(2);

		scn.StartGame();
		// His own site 2 lies next already, as where an earlier card of his played it.
		scn.MoveCardToAdventurePath(freepsSite2);
		scn.FreepsRefreshDecision();

		assertSame(freepsSite2, scn.GetSite(2));
		assertEquals(2, scn.GetTwilightCost(freepsSite2));
		assertTrue(scn.FreepsPlayAvailable(far));

		scn.FreepsPlayCard(far);

		// Nothing is played, but the fellowship's next site is still made one less.
		assertSame(freepsSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		assertEquals(1, scn.GetTwilightCost(freepsSite2));
		assertInZone(Zone.DISCARD, far);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
