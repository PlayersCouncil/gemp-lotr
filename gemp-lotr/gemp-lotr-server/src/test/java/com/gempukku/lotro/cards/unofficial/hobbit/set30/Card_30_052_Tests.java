package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_052_Tests
{

	// The Hobbit table: the Hobbit path, with Goblin Town as site 4, Bilbo, Reliable Companion 30_46 as the
	// Ring-bearer and no card in the Ring slot.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ring", VirtualTableScenario.HobbitRing);   // The One Ring 30_48: [shire] artifact, Bearer must be Bilbo
					put("sting", "30_47");     // Sting: [shire] artifact of another title, Bearer must be Bilbo; a skirmish-win trigger never reached
					put("troop", "1_177");     // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GoblinTownStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Goblin Town
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 4
		 * Shadow Number: 4
		 * Direction: none
		 * Game Text: <b>Underground</b>. <b>Maneuver:</b> Play the One Ring from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("Goblin Town", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
	}

	@Test
	public void ManeuverActionPlaysTheOneRingFromTheDrawDeckOnBilboAndNoOtherArtifact() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var sting = scn.GetFreepsCard("sting");
		var bilbo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToSite(3);
		// The turns on the way drew the small deck into the hand: these two go back.
		scn.MoveCardsToTopOfDeck(ring, sting);
		scn.MoveMinionsToTable("troop");
		assertInZone(Zone.DECK, ring, sting);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals("30_52", scn.GetCurrentSite().getBlueprintId());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(scn.GetCurrentSite()));

		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsDismissRevealedCards();

		// The Ring, and not Sting, which Bilbo could bear as well.
		assertTrue(scn.FreepsHasCardChoiceAvailable(ring));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sting));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(ring);

		assertAttachedTo(ring, bilbo);
		assertInZone(Zone.DECK, sting);
		// Shuffled as the Ring leaves the deck that was looked through, and again once the look is over;
		// the other player's deck is not.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());

		// The Shadow player has the site's action no more than the Free Peoples player has it twice.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(scn.GetCurrentSite()));
	}

	@Test
	public void WithNoOneRingInTheDrawDeckTheActionIsNotOfferedAndItIsNoArcheryAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var sting = scn.GetFreepsCard("sting");
		var bilbo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToFreepsDiscard("ring");
		scn.MoveCardsToTopOfDeck(sting);
		scn.MoveMinionsToTable("troop");
		assertInZone(Zone.DISCARD, ring);
		assertInZone(Zone.DECK, sting);
		scn.SkipToPhase(Phase.MANEUVER);

		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		// The One Ring in the discard pile and another artifact in the deck: nothing the site could play.
		// A Hobbit-set play from the draw deck with nothing it could play is withheld (Q-0019; the orchestrator on Q-0406 and Q-0418; F-0803).
		assertFalse(scn.FreepsActionAvailable(scn.GetCurrentSite()));
		assertInZone(Zone.DECK, sting);
		assertInZone(Zone.DISCARD, ring);
		assertEquals(0, scn.GetAttachedCards(bilbo).size());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());

		// The Ring back in the deck: offered in the maneuver phase, and not in the archery phase that follows.
		scn.MoveCardsToTopOfDeck(ring);
		assertInZone(Zone.DECK, ring);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(scn.GetCurrentSite()));
		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(scn.GetCurrentSite()));
	}
}
