package com.gempukku.lotro.cards.official.set11;

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

public class Card_11_049_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "11_49");
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

	@Test
	public void OneGoodTurnbrDeservesAnotherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: One Good Turn Deserves Another
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship or regroup
		 * Game Text: Spot Sméagol to play the fellowship's next site. Then you may add a burden to take this card back into hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

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
}
