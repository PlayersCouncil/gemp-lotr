package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_124_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "103_124");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "103_124");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothWastesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Gorgoroth Wastes
		 * Unique: false
		 * Side:
		 * Culture:
		 * Shadow Number: 5
		 * Type: Site
		 * Subtype: Standard
		 * Site Number: 8K
		 * Game Text: When moving here, for each Free Peoples card borne by companions, the Free Peoples player must discard it or add (1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Gorgoroth Wastes", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
	}

	@Test
	public void GorgorothWastesCanBeMovedToWithNoBorneCards() throws DecisionResultInvalidException, CardNotFoundException {
		// Errata changed the AddTwilight multiplier from 2 to 1 per remaining borne card.
		// When there are no borne FP cards, the trigger has nothing to discard,
		// so the fellowship should move to site 8 without any interaction.
		var scn = GetScenario();

		// Ring bearer has The One Ring attached, but no other borne FP cards.
		// Start game with bare-minimum fellowship (just Frodo with Ring).
		scn.StartGame();

		// SkipToSite(8) should work without issues since the trigger
		// will find The One Ring (which gets memorized) and apply the discard-or-add choice.
		// The SkipToSite auto-handler should process this.
		scn.SkipToSite(8);

		// Verify we arrived at site 8
		assertEquals(8, scn.GetCurrentSiteNumber());
	}
}
