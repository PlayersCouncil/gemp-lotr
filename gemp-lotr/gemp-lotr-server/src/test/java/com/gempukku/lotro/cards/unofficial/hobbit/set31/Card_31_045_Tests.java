package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_045_Tests
{

	// The Hobbit table: the Hobbit path with Underground Lake as site 4 in place of Goblin Town 30_52, Bilbo,
	// Reliable Companion 30_46 as the Ring-bearer and no card in the Ring slot.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ring", VirtualTableScenario.HobbitRing);   // The One Ring 30_48: [shire] artifact, Bearer must be Bilbo
					// There and Back Again: a Free Peoples condition (a [shire] tale) that Bilbo may bear. The Hobbit
					// sets print no Free Peoples condition.
					put("tale", "1_317");
					put("borders", "33_30");    // Borders of the Forest: [mirkwood] support-area condition, no requirement to play
					put("jail", "32_39");       // Jail: [sauron] support-area condition, no requirement to play
					// Goblin Scimitar: [moria] possession (hand weapon) for an Orc, to play with a [moria] card spotted
					put("scimitar", "31_31");
					// Goblin Runner: [moria] Orc, the Scimitar's bearer; its play trigger is not reached (cheated in)
					put("runner", "31_30");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.HobbitSites);
					put("site4", "31_45");
				}},
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void UndergroundLakeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Underground Lake
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 4
		 * Shadow Number: 2
		 * Direction: none
		 * Game Text: <b>Underground</b>. <b>River</b>. <b>Maneuver:</b> Play a Shadow condition or the One Ring from your draw deck (limit once per player).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("Underground Lake", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void EachPlayerOnceAPhasePlaysTheOneRingOrAShadowConditionFromHisDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var tale = scn.GetFreepsCard("tale");
		var borders = scn.GetShadowCard("borders");
		var jail = scn.GetShadowCard("jail");
		var scimitar = scn.GetShadowCard("scimitar");
		var runner = scn.GetShadowCard("runner");
		var bilbo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToSite(3);
		// The turns on the way drew the small decks into the hands: these go back.
		scn.MoveCardsToTopOfDeck(ring, tale, borders, jail, scimitar);
		scn.MoveMinionsToTable(runner);
		assertInZone(Zone.DECK, ring, tale, borders, jail, scimitar);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(10);

		assertEquals("31_45", scn.GetCurrentSite().getBlueprintId());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(scn.GetCurrentSite()));

		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsDismissRevealedCards();
		// The Ring, and not a Free Peoples condition that Bilbo could bear as well.
		assertTrue(scn.FreepsHasCardChoiceAvailable(ring));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(tale));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(ring);

		assertAttachedTo(ring, bilbo);
		assertInZone(Zone.DECK, tale);
		// Shuffled as the Ring leaves the deck that was looked through, and again once the look is over;
		// the other player's deck is not.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());

		// The Free Peoples player's use does not spend the Shadow player's.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(scn.GetCurrentSite()));
		scn.ShadowUseCardAction(scn.GetCurrentSite());
		scn.ShadowDismissRevealedCards();
		// A Shadow condition of each of two cultures, and not a Shadow possession he could play on his Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(borders, jail));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(scimitar));
		scn.ShadowChooseCardBPFromSelection(jail);

		assertInZone(Zone.SUPPORT, jail);
		assertInZone(Zone.DECK, borders, scimitar);
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles + 2, scn.GetShadowDeckShuffleCount());

		// Once for each player: neither is offered it again this phase, with a Shadow condition still in the deck.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(scn.GetCurrentSite()));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(scn.GetCurrentSite()));
	}

	@Test
	public void WithNoOneRingOrShadowConditionInTheDrawDeckTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var tale = scn.GetFreepsCard("tale");
		var bilbo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToFreepsDiscard("ring");
		scn.MoveCardsToTopOfDeck(tale);
		scn.MoveMinionsToTable("troop");
		assertInZone(Zone.DISCARD, ring);
		assertInZone(Zone.DECK, tale);
		scn.SkipToPhase(Phase.MANEUVER);

		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		// The One Ring in the discard pile and a Free Peoples condition in the deck: nothing the site could play.
		// A Hobbit-set play from the draw deck with nothing it could play is withheld (Q-0019; the orchestrator on Q-0406 and Q-0418).
		assertFalse(scn.FreepsActionAvailable(scn.GetCurrentSite()));
		assertInZone(Zone.DECK, tale);
		assertInZone(Zone.DISCARD, ring);
		assertEquals(0, scn.GetAttachedCards(bilbo).size());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());

		// The Ring back in the deck: offered, the phase's use not spent.
		scn.MoveCardsToTopOfDeck(ring);
		assertInZone(Zone.DECK, ring);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(scn.GetCurrentSite()));
	}

	@Test
	public void ItIsNoArcheryAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var borders = scn.GetShadowCard("borders");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToTopOfDeck(ring, borders);
		scn.MoveMinionsToTable("troop");
		assertInZone(Zone.DECK, ring, borders);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(10);

		assertTrue(scn.FreepsActionAvailable(scn.GetCurrentSite()));
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(scn.GetCurrentSite()));
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(scn.GetCurrentSite()));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(scn.GetCurrentSite()));
		assertInZone(Zone.DECK, ring, borders);
	}

	@Test
	public void TheLimitIsForThePhaseSoThePlayerHasItAgainAsTheShadowPlayerOnTheOtherPlayersTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var borders = scn.GetFreepsCard("borders");
		var troop = scn.GetFreepsCard("troop");
		var bilbo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToTopOfDeck(ring);
		scn.MoveMinionsToTable("troop");
		assertInZone(Zone.DECK, ring);
		scn.SkipToPhase(Phase.MANEUVER);

		var lake = scn.GetCurrentSite();
		assertEquals("31_45", lake.getBlueprintId());
		scn.FreepsUseCardAction(lake);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(ring);
		assertAttachedTo(ring, bilbo);

		// The other player's fellowship comes to the same Underground Lake on his turn, with a minion of the first
		// player's to make a maneuver phase there.
		scn.SkipToOtherPlayersTurn();
		scn.MoveCardsToTopOfDeck(borders);
		assertInZone(Zone.DECK, borders);
		scn.SkipToPhaseInverted(Phase.SHADOW);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhaseInverted(Phase.MANEUVER);
		scn.SetTwilight(10);

		assertSame(lake, scn.GetCurrentSite());
		assertTrue(scn.ShadowDecisionAvailable("Play Maneuver action or Pass"));
		scn.ShadowPass();
		assertTrue(scn.FreepsDecisionAvailable("Play Maneuver action or Pass"));
		assertTrue(scn.FreepsActionAvailable(lake));
		scn.FreepsUseCardAction(lake);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(borders);
		assertInZone(Zone.SUPPORT, borders);
		assertTrue(scn.ShadowDecisionAvailable("Play Maneuver action or Pass"));
	}
}
