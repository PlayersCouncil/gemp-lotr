package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_122_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "103_122");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesolationofAshStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Desolation of Ash
		 * Unique: false
		 * Side:
		 * Culture:
		 * Shadow Number: 8
		 * Type: Site
		 * Subtype: Standard
		 * Site Number: 8K
		 * Game Text: At the start of each phase, the Shadow player may remove (1) to take a Shadow event for that phase into hand from discard.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Desolation of Ash", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
	}

	// TODO: Behavior test for Desolation of Ash requires phase-matching filter
	// to be implemented (currently marked as TODO in the HJSON definition).
	// Once that filter is added, a test should verify that at the start of
	// a phase, Shadow can remove (1) to retrieve a Shadow event for that
	// specific phase from discard.
}
