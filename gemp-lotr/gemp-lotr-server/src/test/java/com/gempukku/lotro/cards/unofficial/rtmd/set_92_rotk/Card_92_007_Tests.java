package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_92_007_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin
					put("legolas", "1_50");     // Legolas, Greenleaf
					put("runner", "1_178");     // Goblin Runner
					put("enquea", "1_231");     // Ulaire Enquea, Lieutenant of Morgul
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_7", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin
					put("legolas", "1_50");     // Legolas, Greenleaf
					put("runner", "1_178");     // Goblin Runner
					put("enquea", "1_231");     // Ulaire Enquea, Lieutenant of Morgul
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_7"
		);
	}

	// The same table with no modifier: the control.
	protected VirtualTableScenario GetNoModScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin
					put("legolas", "1_50");     // Legolas, Greenleaf
					put("runner", "1_178");     // Goblin Runner
					put("enquea", "1_231");     // Ulaire Enquea, Lieutenant of Morgul
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_7
		 * Type: MetaSite
		 * Intensity: 4
		 * Game Text: You must play with your hand revealed.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_7", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(4, card.getBlueprint().getIntensity());
	}

	@Test
	public void GameStatsReportTheFreepsOwnersHandAsRevealed() throws DecisionResultInvalidException, CardNotFoundException {
		// The owner is whoever holds 92_7, which is not tied to a side: both orientations are tested.
		var scn = GetFreepsScenario();

		scn.StartGame();

		// Every client is told whose hand is revealed: the owner's, and not the other player's.
		assertTrue(scn.FreepsHandIsRevealed());
		assertFalse(scn.ShadowHandIsRevealed());
	}

	@Test
	public void GameStatsReportTheShadowOwnersHandAsRevealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		scn.StartGame();

		assertTrue(scn.ShadowHandIsRevealed());
		assertFalse(scn.FreepsHandIsRevealed());
	}

	@Test
	public void RevealedHandStillReportedAfterRolesSwap() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_7 belongs to player 1, who is the Free Peoples player on turn 1 and the Shadow player on turn 2.
		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(gimli, runner);

		scn.StartGame();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertTrue(scn.FreepsHandIsRevealed());
		assertTrue(scn.ShadowSeesCardInHand(gimli));

		scn.SkipToOtherPlayersTurn();

		// Player 2 is the one moving a fellowship now, and it is still player 1's hand that lies open.
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertTrue(scn.FreepsHandIsRevealed());
		assertFalse(scn.ShadowHandIsRevealed());
		assertInZone(Zone.HAND, gimli, runner);
		assertTrue(scn.ShadowSeesCardInHand(gimli));
		assertFalse(scn.FreepsSeesCardInHand(runner));

		// And on the turn after, when he is the Free Peoples player again.
		scn.SkipToOtherPlayersTurn();

		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertTrue(scn.FreepsHandIsRevealed());
		assertFalse(scn.ShadowHandIsRevealed());
	}

	@Test
	public void NoHandIsReportedRevealedWithout92_7() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNoModScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(gimli, runner);

		scn.StartGame();

		assertFalse(scn.FreepsHandIsRevealed());
		assertFalse(scn.ShadowHandIsRevealed());

		// Each player is sent his own hand and neither is sent the other's.
		assertTrue(scn.FreepsSeesCardInHand(gimli));
		assertFalse(scn.ShadowSeesCardInHand(gimli));
		assertTrue(scn.ShadowSeesCardInHand(runner));
		assertFalse(scn.FreepsSeesCardInHand(runner));
	}

	@Test
	public void TheOtherPlayerIsSentTheFreepsOwnersHandAndTheOwnerIsNotSentHis() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var runner = scn.GetShadowCard("runner");
		var enquea = scn.GetShadowCard("enquea");
		scn.MoveCardsToHand(gimli, legolas, runner, enquea);

		scn.StartGame();

		// Player 2 has both of player 1's cards in hand before him, beside his own two.
		assertTrue(scn.ShadowSeesCardInHand(gimli));
		assertTrue(scn.ShadowSeesCardInHand(legolas));
		assertTrue(scn.ShadowSeesCardInHand(runner));
		assertTrue(scn.ShadowSeesCardInHand(enquea));

		// Player 1 has his own two and neither of player 2's: the modifier opens its owner's hand only.
		assertTrue(scn.FreepsSeesCardInHand(gimli));
		assertTrue(scn.FreepsSeesCardInHand(legolas));
		assertFalse(scn.FreepsSeesCardInHand(runner));
		assertFalse(scn.FreepsSeesCardInHand(enquea));
	}

	@Test
	public void TheOtherPlayerIsSentTheShadowOwnersHandAndTheOwnerIsNotSentHis() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var runner = scn.GetShadowCard("runner");
		var enquea = scn.GetShadowCard("enquea");
		scn.MoveCardsToHand(gimli, legolas, runner, enquea);

		scn.StartGame();

		assertTrue(scn.FreepsSeesCardInHand(runner));
		assertTrue(scn.FreepsSeesCardInHand(enquea));
		assertTrue(scn.FreepsSeesCardInHand(gimli));
		assertTrue(scn.FreepsSeesCardInHand(legolas));

		assertTrue(scn.ShadowSeesCardInHand(runner));
		assertTrue(scn.ShadowSeesCardInHand(enquea));
		assertFalse(scn.ShadowSeesCardInHand(gimli));
		assertFalse(scn.ShadowSeesCardInHand(legolas));
	}

	@Test
	public void TheOtherPlayerIsSentCardsDrawnIntoTheRevealedHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(gimli);

		// On the draw deck the card is in nobody's sight.
		assertFalse(scn.ShadowSeesCardInHand(gimli));
		assertFalse(scn.FreepsSeesCardInHand(gimli));

		scn.FreepsDrawCards(1);

		assertInZone(Zone.HAND, gimli);
		assertTrue(scn.ShadowSeesCardInHand(gimli));
		assertTrue(scn.FreepsSeesCardInHand(gimli));
	}

	@Test
	public void ACardThatLeavesTheFreepsOwnersHandGoesFromTheOtherPlayersSight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToHand(gimli, legolas);

		scn.StartGame();

		assertTrue(scn.ShadowSeesCardInHand(gimli));
		assertTrue(scn.ShadowSeesCardInHand(legolas));

		scn.FreepsPlayCard(gimli);

		// Played, Gimli is on the table and no longer a card of the open hand; Legolas is there still.
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertFalse(scn.ShadowSeesCardInHand(gimli));
		assertTrue(scn.ShadowSeesCardInHand(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveCardsToDiscard(legolas);

		assertInZone(Zone.DISCARD, legolas);
		assertFalse(scn.ShadowSeesCardInHand(legolas));
		assertTrue(scn.FreepsHandIsRevealed());
	}

	@Test
	public void ACardThatLeavesTheShadowOwnersHandGoesFromTheOtherPlayersSight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var runner = scn.GetShadowCard("runner");
		var enquea = scn.GetShadowCard("enquea");
		scn.MoveCardsToHand(runner, enquea);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.FreepsSeesCardInHand(runner));
		assertTrue(scn.FreepsSeesCardInHand(enquea));

		scn.ShadowPlayCard(runner);

		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertFalse(scn.FreepsSeesCardInHand(runner));
		assertTrue(scn.FreepsSeesCardInHand(enquea));
		assertTrue(scn.ShadowHandIsRevealed());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
