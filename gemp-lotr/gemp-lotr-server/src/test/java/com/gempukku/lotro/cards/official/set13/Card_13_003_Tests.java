package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "13_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetAheadScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hatred", "13_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DeepHatredStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Deep Hatred
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time the fellowship moves, add (2).<br><b>Skirmish:</b> Exert a Dwarf who is not assigned to a skirmish to wound 2 Orcs (or to wound 1 Orc twice).<br>Discard this from play if the fellowship is ahead on the adventure path.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Deep Hatred", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsDiscardedWhenTheFellowshipMovesAheadOfTheOtherPlayerAndStaysWhileLevelOrBehind() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAheadScenario();

		// Deep Hatred is the second player's.
		var hatred = scn.GetShadowCard("hatred");
		scn.MoveCardsToSupportArea(hatred);

		scn.StartGame();

		// The first player moves to site 2, and his turn ends there.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.SkipToOtherPlayersTurn();

		// The second player's fellowship is at site 1, behind.
		// waits: G-0053 no reader for the marker of the player whose turn it is not (the first player's, read at 2 above)
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, hatred);

		// It moves to site 2: level with the other player, which is not ahead.
		scn.ShadowPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, hatred);

		// It moves again in the regroup phase, to site 3: ahead.
		scn.SkipToMovementDecision();
		assertInZone(Zone.SUPPORT, hatred);
		scn.ShadowChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInDiscard(hatred);
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void IsDiscardedAsTheFirstPlayerMovesOnFromSite1WhereBothPlayersBegin() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAheadScenario();

		var hatred = scn.GetFreepsCard("hatred");
		scn.MoveCardsToSupportArea(hatred);

		scn.StartGame();

		// Both players' markers are at site 1: level.
		// waits: G-0053 no reader for the marker of the player whose turn it is not (the second player's, at site 1)
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, hatred);

		// The first player moves to site 2 while the other is still at site 1.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInDiscard(hatred);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
