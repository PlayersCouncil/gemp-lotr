package com.gempukku.lotro.cards.official.set10;

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

public class Card_10_014_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "10_14");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: your Wizard
					put("gimli", "5_7");        // Gimli, Skilled Defender: a companion in hand
					put("legolas", "1_50");     // Legolas, Greenleaf: a second companion in hand
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the companion in the dead pile
					put("boromir", "1_96");     // Boromir, Lord of Gondor: the companion in the discard pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BorneFarAwayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Borne Far Away
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Spot your Wizard to exchange a companion in hand with a companion in your dead pile or discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Borne Far Away", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExchangesACompanionInHandWithACompanionInTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var borne = scn.GetFreepsCard("card");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(borne, gimli, legolas);
		scn.MoveCardsToDeadPile(aragorn);
		scn.MoveCardsToDiscard(boromir);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(borne));
		scn.FreepsPlayCard(borne);

		// A companion lies in each pile, so the player is asked which pile to exchange with.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("dead pile");
		// Aragorn is the only companion in the dead pile; which companion in hand goes there is chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, legolas));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		assertInZone(Zone.HAND, aragorn);
		assertInZone(Zone.DEAD, gimli);
		assertInZone(Zone.HAND, legolas);
		assertInZone(Zone.DISCARD, boromir);
		assertInDiscard(borne);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExchangesACompanionInHandWithACompanionInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var borne = scn.GetFreepsCard("card");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(borne, gimli, legolas);
		scn.MoveCardsToDeadPile(aragorn);
		scn.MoveCardsToDiscard(boromir);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(borne));
		scn.FreepsPlayCard(borne);

		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("discard");
		// Boromir is the only companion in the discard pile; which companion in hand goes there is chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, legolas));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		assertInZone(Zone.HAND, boromir);
		assertInZone(Zone.DISCARD, gimli);
		assertInZone(Zone.HAND, legolas);
		assertInZone(Zone.DEAD, aragorn);
		assertInDiscard(borne);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
