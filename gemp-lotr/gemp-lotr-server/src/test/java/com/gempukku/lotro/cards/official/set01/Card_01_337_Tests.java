package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_337_Tests
{

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
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 3
		 * Shadow Number: 0
		 * Direction: Right
		 * Game Text: <b>Sanctuary</b>. When the fellowship moves from Council Courtyard, remove (2).
		*/

		var scn = GetCourtyardScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("Council Courtyard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(3, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
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

	@Test
	public void MovingToCouncilCourtyardRemovesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCourtyardScenario();

		scn.StartGame();
		scn.SkipToSite(2);

		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());

		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals("Council Courtyard", scn.GetCurrentSite().getBlueprint().getTitle());
		// 3 in the pool + 1 companion + 0 for the Courtyard: nothing is removed on the way in.  Had the move to
		// the Courtyard set it off, the pool would read 3 - 2 + 1 = 2.
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
