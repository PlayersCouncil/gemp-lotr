package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_349_Tests
{

	// The Bridge is site 5 of FellowshipSites.  Each test begins its turn at site 4, so that the move to the Bridge
	// is the turn's first and the Shadow phase follows there, on the Shadow player's own copy.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// The Balrog, The Terror of Khazad-dûm: [moria] minion, cost 12, site 4; his text acts only on himself
					put("terror", "12_79");
					// The Balrog, Demon of Might: [moria] minion, cost 14, site 4; underground, the archery and maneuver
					// phases are skipped while he is in play
					put("demon", "19_18");
					// Goblin Patrol Troop: a [moria] Orc minion who is no Balrog, cost 6, site 4, no text
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheBridgeofKhazaddumStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Bridge of Khazad-dûm
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 5
		 * Shadow Number: 6
		 * Direction: Left
		 * Game Text: Underground. Shadow: Play The Balrog from your draw deck or hand; The Balrog's twilight cost
		 * is -6.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(5);

		assertEquals("The Bridge of Khazad-dûm", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(5, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
	}

	@Test
	public void ShadowActionPlaysABalrogFromHandForSixLessWhereAPlainPlayCannotPayForHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bridge = scn.GetShadowSite(5);
		var terror = scn.GetShadowCard("terror");
		var demon = scn.GetShadowCard("demon");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetShadowHand(terror, demon, troop);
		scn.FreepsPass();

		assertSame(bridge, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.SetTwilight(8);
		scn.ShadowRefreshDecision();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		// Neither Balrog can be paid for by a plain play (12 and 14); the Troop can (6).
		assertFalse(scn.ShadowPlayAvailable(terror));
		assertFalse(scn.ShadowPlayAvailable(demon));
		assertTrue(scn.ShadowPlayAvailable(troop));
		assertTrue(scn.ShadowActionAvailable(bridge));

		scn.ShadowUseCardAction(bridge);
		assertTrue(scn.ShadowChoiceAvailable("from hand"));
		assertTrue(scn.ShadowChoiceAvailable("from your draw deck"));
		scn.ShadowChoose("from hand");

		// Either Balrog, at 12 - 6 and 14 - 6; the [moria] Orc who is no Balrog is not offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(terror, demon));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		scn.ShadowChooseCard(terror);

		assertInZone(Zone.SHADOW_CHARACTERS, terror);
		// 8 - (12 - 6)
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.HAND, demon, troop);
		// Nobody looked through a draw deck.
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionPlaysABalrogFromTheDrawDeckForSixLessAndShufflesThatDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bridge = scn.GetShadowSite(5);
		var terror = scn.GetShadowCard("terror");
		var demon = scn.GetShadowCard("demon");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetShadowHand();
		scn.MoveCardsToTopOfDeck(terror, demon, troop);
		scn.FreepsPass();

		assertSame(bridge, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.ShadowActionAvailable(bridge));

		// With no card in hand, the draw deck is the one place left, and nothing is asked about it.
		scn.ShadowUseCardAction(bridge);
		assertTrue(scn.ShadowHasCardChoiceAvailable(terror, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(demon, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, false));
		scn.ShadowDismissRevealedCards();

		// Either Balrog, at 12 - 6 and 14 - 6; the [moria] Orc who is no Balrog, whom the pool could pay for, is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(terror));
		assertTrue(scn.ShadowHasCardChoiceAvailable(demon));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		scn.ShadowChooseCardBPFromSelection(demon);

		assertInZone(Zone.SHADOW_CHARACTERS, demon);
		// 10 - (14 - 6)
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.DECK, terror, troop);
		assertTrue(scn.GetShadowDeckShuffleCount() > shadowShuffles);
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoBalrogToPlayTheActionIsStillOfferedAndOnlyLooksThroughTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bridge = scn.GetShadowSite(5);
		var terror = scn.GetShadowCard("terror");
		var demon = scn.GetShadowCard("demon");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetShadowHand(troop);
		scn.MoveCardsToDiscard(terror, demon);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		// No penalty for failing to play from a draw deck (Q-0019): the action may be used for a look.
		assertTrue(scn.ShadowActionAvailable(bridge));
		scn.ShadowUseCardAction(bridge);
		scn.ShadowDismissRevealedCards();

		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.HAND, troop);
		assertInZone(Zone.DISCARD, terror, demon);
		assertTrue(scn.GetShadowDeckShuffleCount() > shadowShuffles);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bridge = scn.GetShadowSite(5);
		var terror = scn.GetShadowCard("terror");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetShadowHand(terror);
		scn.FreepsPass();

		assertSame(bridge, scn.GetCurrentSite());
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(bridge));

		// A minion on the table, so that there is a maneuver phase.
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.HAND, terror);
		assertFalse(scn.ShadowActionAvailable(bridge));
	}
}
