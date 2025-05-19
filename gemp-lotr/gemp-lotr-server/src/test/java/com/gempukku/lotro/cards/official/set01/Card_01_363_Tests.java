
package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_363_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
                new HashMap<>() {{
					put("tracker1", "1_261");
					put("tracker2", "1_262");
					put("tracker3", "1_270");
					put("shelob", "8_26");
					put("hollowing", "3_54");
                }},
                new HashMap<>() {{
                    put("site1", "1_319");
                    put("site2", "1_327");
                    put("site3", "1_337");
                    put("site4", "1_343");
                    put("site5", "1_349");
                    put("site6", "1_350");
                    put("site7", "1_353");
                    put("site8", "1_356");
                    put("site9", "1_363");
                }},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TolBrandirRuinsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Tol Brandir
		 * Shadow Number: 9
		 * Type: Site
		 * Site Number: 9
		 * Game Text: River.  Shadow: Play up to 3 trackers from your discard pile; end your Shadow phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Tol Brandir", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TolBrandirActionPlays3TrackersAndEndsShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var tolbrandir = scn.GetShadowSite(9);
		var tracker1 = scn.GetShadowCard("tracker1");
		var tracker2 = scn.GetShadowCard("tracker2");
		var tracker3 = scn.GetShadowCard("tracker3");
		var hollowing = scn.GetShadowCard("hollowing");
		scn.MoveCardsToSupportArea(hollowing);
		scn.MoveCardsToDiscard(tracker1, tracker2, tracker3);
		scn.MoveCardsToShadowDiscard("shelob");

		scn.StartGame();

		scn.SkipToSite(8);
		scn.FreepsPassCurrentPhaseAction();

		assertSame(tolbrandir, scn.GetCurrentSite());
		assertTrue(scn.ShadowActionAvailable(tolbrandir));
		assertTrue(scn.ShadowActionAvailable(hollowing));
		assertEquals(Zone.DISCARD, tracker1.getZone());
		assertEquals(Zone.DISCARD, tracker2.getZone());
		assertEquals(Zone.DISCARD, tracker3.getZone());
		assertEquals(10, scn.GetTwilight());

		scn.ShadowUseCardAction(tolbrandir);
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));
		//Should not have the non-tracker in the choice pool
		assertEquals(3, scn.ShadowGetBPChoices().size());

		scn.ShadowChooseCardBPFromSelection(tracker1);
		assertEquals(Zone.SHADOW_CHARACTERS, tracker1.getZone());
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));

		scn.ShadowChooseCardBPFromSelection(tracker2);
		assertEquals(Zone.SHADOW_CHARACTERS, tracker2.getZone());
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));

		scn.ShadowChooseCardBPFromSelection(tracker3);
		assertEquals(Zone.SHADOW_CHARACTERS, tracker3.getZone());
		assertEquals(4, scn.GetTwilight());

		//No more shadow actions should be permitted (but Shadow is still required to pass manually)
		assertFalse(scn.ShadowActionAvailable(hollowing));
	}

	@Test
	public void TolBrandirActionCanPlay0TrackersAndDecline3() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var tolbrandir = scn.GetShadowSite(9);
		var tracker1 = scn.GetShadowCard("tracker1");
		var tracker2 = scn.GetShadowCard("tracker2");
		var tracker3 = scn.GetShadowCard("tracker3");
		scn.MoveCardsToDiscard(tracker1, tracker2, tracker3);

		scn.StartGame();

		scn.SkipToSite(8);
		scn.FreepsPassCurrentPhaseAction();

		assertSame(tolbrandir, scn.GetCurrentSite());
		assertTrue(scn.ShadowActionAvailable(tolbrandir));
		assertEquals(Zone.DISCARD, tracker1.getZone());
		assertEquals(Zone.DISCARD, tracker2.getZone());
		assertEquals(Zone.DISCARD, tracker3.getZone());
		assertEquals(10, scn.GetTwilight());

		scn.ShadowUseCardAction(tolbrandir);
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));
		//Should not have the non-tracker in the choice pool
		assertEquals(3, scn.ShadowGetBPChoices().size());

		scn.ShadowDeclineChoosing();
		scn.ShadowDeclineChoosing();
		scn.ShadowDeclineChoosing();
		assertEquals(Zone.DISCARD, tracker1.getZone());
		assertEquals(Zone.DISCARD, tracker2.getZone());
		assertEquals(Zone.DISCARD, tracker3.getZone());
		assertTrue(scn.ShadowDecisionAvailable("Play Shadow action or pass"));
	}

	@Test
	public void TolBrandirActionCanPlay1TrackerAndDecline2() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var tolbrandir = scn.GetShadowSite(9);
		var tracker1 = scn.GetShadowCard("tracker1");
		var tracker2 = scn.GetShadowCard("tracker2");
		var tracker3 = scn.GetShadowCard("tracker3");
		scn.MoveCardsToDiscard(tracker1, tracker2, tracker3);

		scn.StartGame();

		scn.SkipToSite(8);
		scn.FreepsPassCurrentPhaseAction();

		assertSame(tolbrandir, scn.GetCurrentSite());
		assertTrue(scn.ShadowActionAvailable(tolbrandir));
		assertEquals(Zone.DISCARD, tracker1.getZone());
		assertEquals(Zone.DISCARD, tracker2.getZone());
		assertEquals(Zone.DISCARD, tracker3.getZone());
		assertEquals(10, scn.GetTwilight());

		scn.ShadowUseCardAction(tolbrandir);
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));
		//Should not have the non-tracker in the choice pool
		assertEquals(3, scn.ShadowGetBPChoices().size());

		scn.ShadowChooseCardBPFromSelection(tracker1);
		assertEquals(Zone.SHADOW_CHARACTERS, tracker1.getZone());
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));

		scn.ShadowDeclineChoosing();
		scn.ShadowDeclineChoosing();
		assertEquals(Zone.DISCARD, tracker2.getZone());
		assertEquals(Zone.DISCARD, tracker3.getZone());
		assertTrue(scn.ShadowDecisionAvailable("Play Shadow action or pass"));
	}

	@Test
	public void TolBrandirActionCanPlay2TrackersAndDecline1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var tolbrandir = scn.GetShadowSite(9);
		var tracker1 = scn.GetShadowCard("tracker1");
		var tracker2 = scn.GetShadowCard("tracker2");
		var tracker3 = scn.GetShadowCard("tracker3");
		scn.MoveCardsToDiscard(tracker1, tracker2, tracker3);

		scn.StartGame();

		scn.SkipToSite(8);
		scn.FreepsPassCurrentPhaseAction();

		assertSame(tolbrandir, scn.GetCurrentSite());
		assertTrue(scn.ShadowActionAvailable(tolbrandir));
		assertEquals(Zone.DISCARD, tracker1.getZone());
		assertEquals(Zone.DISCARD, tracker2.getZone());
		assertEquals(Zone.DISCARD, tracker3.getZone());
		assertEquals(10, scn.GetTwilight());

		scn.ShadowUseCardAction(tolbrandir);
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));
		//Should not have the non-tracker in the choice pool
		assertEquals(3, scn.ShadowGetBPChoices().size());

		scn.ShadowChooseCardBPFromSelection(tracker1);
		assertEquals(Zone.SHADOW_CHARACTERS, tracker1.getZone());
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));

		scn.ShadowChooseCardBPFromSelection(tracker2);
		assertEquals(Zone.SHADOW_CHARACTERS, tracker2.getZone());
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));

		scn.ShadowDeclineChoosing();
		assertEquals(Zone.DISCARD, tracker3.getZone());
		assertTrue(scn.ShadowDecisionAvailable("Play Shadow action or pass"));
	}
}
