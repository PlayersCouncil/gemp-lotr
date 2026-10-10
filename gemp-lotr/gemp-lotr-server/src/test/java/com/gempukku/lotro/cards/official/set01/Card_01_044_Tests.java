package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_044_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_44");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDiscardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foul", "1_44");
					put("legolas", "1_50");     // Legolas, Greenleaf: the Elf, vitality 3

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc minion
					put("ambition", "1_133");   // Saruman's Ambition: [isengard] condition
					put("runner", "1_178");     // Goblin Runner: [moria] Orc minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FoulCreationStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Foul Creation
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Exert an Elf to reveal an opponent's hand. You may discard an [isengard] minion revealed to draw 2 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Foul Creation", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MayDiscardAnIsengardMinionRevealedToDrawTwoCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardScenario();

		var foul = scn.GetFreepsCard("foul");
		var legolas = scn.GetFreepsCard("legolas");
		// The Shadow player's hand: two [isengard] minions, an [isengard] card that is no minion, and a minion
		// of another culture.  The Free Peoples player's own copies of the four are his draw deck.
		var savage = scn.GetShadowCard("savage");
		var worker = scn.GetShadowCard("worker");
		var ambition = scn.GetShadowCard("ambition");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(foul);
		scn.MoveCardsToHand(savage, worker, ambition, runner);

		scn.StartGame();

		assertEquals(4, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsPlayAvailable(foul));

		scn.FreepsPlayCard(foul);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetTwilight());
		assertEquals(4, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// "You may" is asked as a yes or no; then one of the two [isengard] minions and nothing else.
		assertTrue(scn.FreepsDecisionAvailable("to draw 2 cards"));
		scn.FreepsChooseYes();
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, worker));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ambition, runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(worker);
		assertInZone(Zone.DISCARD, worker);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertInZone(Zone.HAND, savage, ambition, runner);
		// The event has left the hand and two cards have come into it.
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertInZone(Zone.DISCARD, foul);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
