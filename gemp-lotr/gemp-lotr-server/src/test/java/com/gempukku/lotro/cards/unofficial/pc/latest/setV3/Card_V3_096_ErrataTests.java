package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_096_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "103_96");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EndlessNightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Endless Night
		 * Unique: true
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 5
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Twilight. To play, hinder 4 twilight conditions. This cannot be discarded or hindered. Orcs gain <b>fierce</b>. Trolls gain <b>enduring</b>. Nazgul gain <b>damage +1</b>. Your Men and Uruk-hai gain <b>archer</b>.
		* 	Shadow: Hinder X of your other Shadow support cards to play Sauron from your hand or discard pile; he is twilight cost -X.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Endless Night", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		// Errata: twilight cost increased from 4 to 5
		assertEquals(5, card.getBlueprint().getTwilightCost());
	}
}
