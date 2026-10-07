package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_310_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "51_310");
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession
					put("pipe", "1_285");       // Bilbo's Pipe: a [shire] possession
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Sam, Faithful Companion
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 5
		 * Signet: Frodo
		 * Game Text: <b>Fellowship:</b> Exert Sam to take a [shire] item into hand from your draw deck (limit once per turn).<br><b>Response:</b> If Frodo dies, make Sam the <b>Ring-bearer (resistance 5)</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Sam", card.getBlueprint().getTitle());
		assertEquals("Faithful Companion", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet()); 
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void SamTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);
		scn.MoveCompanionsToTable(card);
		scn.MoveCardsToSupportArea(card);
		scn.MoveCardsToDiscard(card);
		scn.MoveCardsToTopOfDeck(card);

		//var card = scn.GetShadowCard("card");
		scn.MoveCardsToHand(card);
		scn.MoveMinionsToTable(card);
		scn.MoveCardsToSupportArea(card);
		scn.MoveCardsToDiscard(card);
		scn.MoveCardsToTopOfDeck(card);

		scn.StartGame();
		
		assertFalse(true);
	}

	@Test
	public void FellowshipAbilityAtItsLimitStillExertsSamAndTakesNothingIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(card);

		scn.StartGame();

		assertEquals("false", scn.FreepsActionNoEffect(card));
		scn.FreepsUseCardAction(card);
		scn.DismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(sword);
		assertEquals(1, scn.GetWoundsOn(card));
		assertEquals(Zone.HAND, sword.getZone());
		assertEquals(1, scn.GetFreepsHandCount());

		// Limit once per turn: Sam can still exert, so the ability is still offered, flagged as doing
		// nothing (Ketura on Q-0041).
		assertTrue(scn.FreepsActionAvailable(card));
		assertEquals("limit", scn.FreepsActionNoEffect(card));

		// Used anyway: Sam exerts, and the draw deck (which still holds a [shire] item) is not looked through.
		scn.FreepsUseCardAction(card);
		assertEquals(2, scn.GetWoundsOn(card));
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
