package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_05_041_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "5_41");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: Gandalf signet
					put("boromir", "1_96");       // Boromir, Lord of Gondor: a Man with the Aragorn signet
					put("armor", "1_92");         // Armor: possession, bearer must be a Man
					put("hobbitsword", "1_299");  // Hobbit Sword: possession, bearer must be a Hobbit
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheseAreMyPeopleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: These Are My People
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Exert Aragorn twice to play any number of possessions from your discard pile onto companions with the Aragorn signet.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("These Are My People", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void TheseAreMyPeopleTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(3, scn.GetTwilight());
	}

	@Test
	public void IsNotPlayableUnlessAPossessionInTheDiscardPileCanBePlayedOnACompanionWithTheAragornSignet() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var armor = scn.GetFreepsCard("armor");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(card);
		//A possession that could be played, but only on Frodo, who has the Frodo signet
		scn.MoveCardsToDiscard(hobbitsword);

		scn.StartGame();

		//Playing from the discard pile is treated as a cost: with nothing to play, the event is withheld
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(card));

		//Armor can go on Boromir, who has the Aragorn signet
		scn.MoveCardsToDiscard(armor);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsPlayAvailable(card));
	}
}
