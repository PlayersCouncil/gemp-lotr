package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_121_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
				}},
				VirtualTableScenario.FellowshipSites,
				// Frodo, Resolute Hobbit is the card under test, laid as the table's Ring-bearer.
				VirtualTableScenario.MDFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrodoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Frodo, Resolute Hobbit
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 12
		 * Signet: Frodo
		 * Game Text: <b>Ring-bearer (resistance 12).</b>
		*/

		var scn = GetScenario();

		var card = scn.GetRingBearer();

		assertEquals("Frodo", card.getBlueprint().getTitle());
		assertEquals("Resolute Hobbit", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(12, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
		assertTrue(card.getBlueprint().canStartWithRing());
		// Every Frodo is Ring-bound by rule (CR 4.2 "Ring-bound"), though the card does not print it.
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
	}
}
