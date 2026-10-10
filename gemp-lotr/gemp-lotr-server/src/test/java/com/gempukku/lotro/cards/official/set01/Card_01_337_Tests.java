package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_337_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_337");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Council Courtyard is site 3; the rest is the usual Fellowship path.  Site 4 is Balin's Tomb, Shadow number 3.
	protected VirtualTableScenario GetCourtyardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>(),
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_337");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CouncilCourtyardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Council Courtyard
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 0
		 * Type: Sanctuary
		 * Subtype: 
		 * Site Number: 3
		 * Game Text: <b>Sanctuary</b>. When the fellowship moves from Council Courtyard, remove (2).
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		//var card = scn.GetFreepsSite(3);
		var card = scn.GetFreepsCard("card");

		assertEquals("Council Courtyard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void MovingFromCouncilCourtyardRemovesTwoTwilightBeforeTheMoveAddsItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCourtyardScenario();

		scn.StartGame();
		scn.SkipToSite(3);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals("Council Courtyard", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());

		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		// 3 in the pool - 2 on leaving the Courtyard, then 1 companion + 3 for Balin's Tomb
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingFromCouncilCourtyardWithOneTwilightRemovesItAndTheMovesOwnTwilightIsUntouched() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCourtyardScenario();

		scn.StartGame();
		scn.SkipToSite(3);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals("Council Courtyard", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.SetTwilight(1);
		assertEquals(1, scn.GetTwilight());

		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		// "When you move from" comes before the move's twilight is added: the 1 in the pool is all there is to
		// remove, and then 1 companion + 3 for Balin's Tomb.  Removed after the move it would be 1 + 4 - 2 = 3.
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
