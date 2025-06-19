package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.VirtualTableScenario.P1;
import static org.junit.Assert.*;

public class Card_12_098_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "12_98");
					put("card2", "12_98");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcTormentorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Orc Tormentor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: Each time the Free Peoples player assigns this minion to skirmish an unwounded companion, he or she must add a burden or discard a card from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Orc Tormentor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void FreepDiscardsAfterAssigningToOrcTormentor() throws DecisionResultInvalidException, CardNotFoundException {
		// Arrange
		var scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var card2 = scn.GetFreepsCard("card2");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(card);
		scn.MoveCardsToHand(card2);

		scn.StartGame();

		scn.SetTwilight(20);

		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(card);
		scn.ShadowPassCurrentPhaseAction();

		scn.SkipToAssignments();
		assertTrue(scn.FreepsCanAssign(card));

		// Act
		scn.FreepsAssignToMinions(frodo, card);
		var decision = scn.GetAwaitingDecision(P1);
		assertNotNull(decision);

		assertEquals(1, scn.GetFreepsHandCount());
		scn.PlayerDecided(P1, "1");

		// Assert
		assertEquals(0, scn.GetFreepsHandCount());
	}

	@Test
	public void FreepBurdensAfterAssigningToOrcTormentor() throws DecisionResultInvalidException, CardNotFoundException {
		// Arrange
		var scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var card2 = scn.GetFreepsCard("card2");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(card);
		scn.MoveCardsToHand(card2);

		scn.StartGame();

		scn.SetTwilight(20);

		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(card);
		scn.ShadowPassCurrentPhaseAction();

		scn.SkipToAssignments();
		assertTrue(scn.FreepsCanAssign(card));

		// Act
		scn.FreepsAssignToMinions(frodo, card);
		var decision = scn.GetAwaitingDecision(P1);
		assertNotNull(decision);

		assertEquals(1, scn.GetBurdens());
		scn.PlayerDecided(P1, "0");

		// Assert
		assertEquals(2, scn.GetBurdens());
	}
}
