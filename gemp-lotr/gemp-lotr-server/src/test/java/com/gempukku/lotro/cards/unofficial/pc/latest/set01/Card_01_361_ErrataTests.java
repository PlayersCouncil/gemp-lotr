package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_361_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("slopes", "51_361");
					put("uruk", "1_151");   // Uruk Lieutenant (twilight 3)
					put("runner", "1_178"); // Goblin Runner (twilight 2, not Isengard)
					put("guard", "1_7");    // Dwarf Guard
				}},
				new String[] {"51_361", "1_337", "1_338", "1_339", "1_340", "1_341", "1_342", "1_343", "1_344"},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SlopesofAmonHenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Slopes of Amon Hen
		 * Unique: false
		 * Shadow Number: 9
		 * Type: Site
		 * Site Number: 9
		 * Game Text: The twilight cost of each [isengard] minion is -2. Each time you play
		 * an [isengard] card, the Free Peoples player must exert a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Slopes of Amon Hen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getSiteNumber());
	}
}
