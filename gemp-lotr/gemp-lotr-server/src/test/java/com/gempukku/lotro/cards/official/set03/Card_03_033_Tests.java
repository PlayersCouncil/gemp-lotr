package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_033_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("check", "3_33");
					put("gandalf", "1_364");

					put("runner", "1_178");
					put("sauron", "9_48");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HisFirstSeriousCheckStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: His First Serious Check
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Spot Gandalf to reveal a card at random from an opponent's hand. You may add (X) to discard that card, where X is the twilight cost of the card revealed.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("check");

		assertEquals("His First Serious Check", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void HisFirstSeriousCheckSpotsGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var check = scn.GetFreepsCard("check");
		scn.MoveCardsToHand(check);

		scn.MoveMinionsToTable("runner");
		scn.MoveCardsToShadowHand("sauron", "check", "gandalf");

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.FreepsPlayAvailable(check));
	}

	@Test
	public void HisFirstSeriousCheckRevealsACardAtRandom() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var check = scn.GetFreepsCard("check");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(check);
		scn.MoveCompanionToTable(gandalf);

		scn.MoveMinionsToTable("runner");
		scn.MoveCardsToShadowHand("sauron", "check", "gandalf");

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsPlayAvailable(check));
		assertEquals(3, scn.GetShadowHandCount());

		scn.FreepsPlayCard(check);
		assertEquals(1, scn.FreepsGetCardChoiceCount());
		scn.DismissRevealedCards();

		assertTrue(scn.FreepsDecisionAvailable("Would you like to add twilight to discard"));
		scn.FreepsChooseNo();
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.ShadowAnyDecisionsAvailable());
	}

	@Test
	public void HisFirstSeriousCheckCanAddRevealedCardsTwilightToDiscardIt() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var check = scn.GetFreepsCard("check");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(check);
		scn.MoveCompanionToTable(gandalf);

		var sauron = scn.GetShadowCard("sauron");
		scn.MoveMinionsToTable("runner");
		scn.MoveCardsToHand(sauron);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();
		scn.SetTwilight(0);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(check));
		assertEquals(1, scn.GetShadowHandCount());

		scn.FreepsPlayCard(check);
		assertEquals(1, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasBPChoice(sauron));
		scn.DismissRevealedCards();

		assertEquals(Zone.HAND, sauron.getZone());
		assertEquals(1, scn.GetTwilight()); // 0 + HFSC's cost
		assertTrue(scn.FreepsDecisionAvailable("Would you like to add twilight to discard"));
		scn.FreepsChooseYes();

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(Zone.DISCARD, sauron.getZone());
		assertEquals(19, scn.GetTwilight());
		assertTrue(scn.ShadowAnyDecisionsAvailable());
	}
}
