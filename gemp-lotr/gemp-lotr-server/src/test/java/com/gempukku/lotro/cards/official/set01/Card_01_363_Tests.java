
package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_363_Tests
{

	// Tol Brandir is site 9 (Shadow number 9).  A [sauron] Orc has site number 6 and an [isengard] Uruk-hai 5,
	// so nobody is roaming there.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
                new HashMap<>() {{
					// Three [sauron] Orc trackers, costing 1, 2 and 3.  Each lowers the roaming penalty of [sauron]
					// minions; the Ambusher's response answers a regroup event, the Assassin's assignment ability
					// needs 2 Hobbit companions, the Band's skirmish ability is used only on purpose.
					put("tracker1", "1_261");   // Orc Ambusher
					put("tracker2", "1_262");   // Orc Assassin
					put("tracker3", "1_270");   // Orc Scouting Band
					// Uruk Scout: an [isengard] Uruk-hai tracker, cost 3, damage +1; its response answers a ranger event
					put("urukscout", "2_47");
					// Orc Scout: a [sauron] Orc tracker, cost 2: the tracker in hand
					put("orcscout", "2_89");
					// Uruk Seeker: an [isengard] tracker, cost 2: the tracker in the other player's discard pile
					put("seeker", "4_195");
					// Orc Chieftain: a [sauron] Orc, cost 2, that is no tracker; its text answers a weapon played on it
					put("chieftain", "1_266");
					// Hollowing of Isengard: a support-area condition, "Shadow: Discard this condition to add (3)"
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
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 9
		 * Shadow Number: 9
		 * Direction: Left
		 * Game Text: <b>River</b>. <b>Shadow:</b> Play up to 3 trackers from your discard pile; end your Shadow phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Tol Brandir", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void TolBrandirActionPlays3TrackersAndEndsShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tolbrandir = scn.GetShadowSite(9);
		var ambusher = scn.GetShadowCard("tracker1");
		var assassin = scn.GetShadowCard("tracker2");
		var band = scn.GetShadowCard("tracker3");
		var urukScout = scn.GetShadowCard("urukscout");
		var orcScout = scn.GetShadowCard("orcscout");
		var chieftain = scn.GetShadowCard("chieftain");
		var hollowing = scn.GetShadowCard("hollowing");
		var firstPlayersSeeker = scn.GetFreepsCard("seeker");

		scn.StartGame();

		scn.SkipToSite(8);
		scn.MoveCardsToSupportArea(hollowing);
		scn.SetShadowHand(orcScout);
		scn.MoveCardsToDiscard(ambusher, assassin, band, urukScout, chieftain, firstPlayersSeeker);
		scn.FreepsPass();

		assertSame(tolbrandir, scn.GetCurrentSite());
		// 1 companion + 9 for Tol Brandir
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(tolbrandir));
		assertTrue(scn.ShadowActionAvailable(hollowing));
		assertTrue(scn.ShadowPlayAvailable(orcScout));
		assertInZone(Zone.DISCARD, ambusher, assassin, band, urukScout);

		scn.ShadowUseCardAction(tolbrandir);
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));
		// The trackers in his own discard pile, of either culture
		assertTrue(scn.ShadowHasCardChoiceAvailable(ambusher, assassin, band, urukScout));
		// Not the Orc there that is no tracker, the tracker in his hand, or the tracker in the other player's
		// discard pile
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(chieftain, orcScout, firstPlayersSeeker));
		assertEquals(4, scn.ShadowGetBPChoiceCount());

		scn.ShadowChooseCardBPFromSelection(ambusher);
		assertInZone(Zone.SHADOW_CHARACTERS, ambusher);
		// 1 to play
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));

		scn.ShadowChooseCardBPFromSelection(urukScout);
		assertInZone(Zone.SHADOW_CHARACTERS, urukScout);
		// 3 to play
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));

		scn.ShadowChooseCardBPFromSelection(band);
		assertInZone(Zone.SHADOW_CHARACTERS, band);
		// 3 to play
		assertEquals(3, scn.GetTwilight());

		// Three were played.  The fourth, which the 3 left would pay for, is not asked for.
		assertFalse(scn.ShadowDecisionAvailable("Choose card from discard"));
		assertInZone(Zone.DISCARD, assassin);

		// His Shadow phase is over: he can still pass, and nothing else.  The tracker in his hand costs 2.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(hollowing));
		assertFalse(scn.ShadowPlayAvailable(orcScout));
		assertFalse(scn.ShadowActionAvailable(tolbrandir));

		scn.ShadowPass();
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TolBrandirActionCanPlay0TrackersAndDecline3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tolbrandir = scn.GetShadowSite(9);
		var ambusher = scn.GetShadowCard("tracker1");
		var assassin = scn.GetShadowCard("tracker2");
		var band = scn.GetShadowCard("tracker3");
		var orcScout = scn.GetShadowCard("orcscout");

		scn.StartGame();

		scn.SkipToSite(8);
		scn.SetShadowHand(orcScout);
		scn.MoveCardsToDiscard(ambusher, assassin, band);
		scn.FreepsPass();

		assertSame(tolbrandir, scn.GetCurrentSite());
		assertTrue(scn.ShadowActionAvailable(tolbrandir));
		assertTrue(scn.ShadowPlayAvailable(orcScout));
		assertInZone(Zone.DISCARD, ambusher, assassin, band);
		assertEquals(10, scn.GetTwilight());

		scn.ShadowUseCardAction(tolbrandir);
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));
		assertEquals(3, scn.ShadowGetBPChoiceCount());
		// "Up to 3": each of the three plays may be passed over.
		assertEquals(0, scn.ShadowGetChoiceMin());

		scn.ShadowDeclineChoosing();
		scn.ShadowDeclineChoosing();
		scn.ShadowDeclineChoosing();
		assertInZone(Zone.DISCARD, ambusher, assassin, band);
		assertEquals(10, scn.GetTwilight());

		// Having played none, his Shadow phase is over all the same: he can pass, and nothing else.  The
		// tracker in his hand costs 2.
		assertTrue(scn.ShadowDecisionAvailable("Play Shadow action or pass"));
		assertFalse(scn.ShadowPlayAvailable(orcScout));
		assertFalse(scn.ShadowActionAvailable(tolbrandir));
	}

	@Test
	public void TolBrandirActionCanPlay1TrackerAndDecline2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tolbrandir = scn.GetShadowSite(9);
		var ambusher = scn.GetShadowCard("tracker1");
		var assassin = scn.GetShadowCard("tracker2");
		var band = scn.GetShadowCard("tracker3");

		scn.StartGame();

		scn.SkipToSite(8);
		scn.SetShadowHand();
		scn.MoveCardsToDiscard(ambusher, assassin, band);
		scn.FreepsPass();

		assertSame(tolbrandir, scn.GetCurrentSite());
		assertTrue(scn.ShadowActionAvailable(tolbrandir));
		assertInZone(Zone.DISCARD, ambusher, assassin, band);
		assertEquals(10, scn.GetTwilight());

		scn.ShadowUseCardAction(tolbrandir);
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));
		assertEquals(3, scn.ShadowGetBPChoiceCount());

		scn.ShadowChooseCardBPFromSelection(ambusher);
		assertInZone(Zone.SHADOW_CHARACTERS, ambusher);
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));

		scn.ShadowDeclineChoosing();
		scn.ShadowDeclineChoosing();
		assertInZone(Zone.DISCARD, assassin, band);
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Play Shadow action or pass"));
		assertFalse(scn.ShadowActionAvailable(tolbrandir));
	}

	@Test
	public void TolBrandirActionCanPlay2TrackersAndDecline1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tolbrandir = scn.GetShadowSite(9);
		var ambusher = scn.GetShadowCard("tracker1");
		var assassin = scn.GetShadowCard("tracker2");
		var band = scn.GetShadowCard("tracker3");

		scn.StartGame();

		scn.SkipToSite(8);
		scn.SetShadowHand();
		scn.MoveCardsToDiscard(ambusher, assassin, band);
		scn.FreepsPass();

		assertSame(tolbrandir, scn.GetCurrentSite());
		assertTrue(scn.ShadowActionAvailable(tolbrandir));
		assertInZone(Zone.DISCARD, ambusher, assassin, band);
		assertEquals(10, scn.GetTwilight());

		scn.ShadowUseCardAction(tolbrandir);
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));
		assertEquals(3, scn.ShadowGetBPChoiceCount());

		scn.ShadowChooseCardBPFromSelection(ambusher);
		assertInZone(Zone.SHADOW_CHARACTERS, ambusher);
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));

		scn.ShadowChooseCardBPFromSelection(assassin);
		assertInZone(Zone.SHADOW_CHARACTERS, assassin);
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));

		scn.ShadowDeclineChoosing();
		assertInZone(Zone.DISCARD, band);
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Play Shadow action or pass"));
		assertFalse(scn.ShadowActionAvailable(tolbrandir));
	}

	@Test
	public void TolBrandirActionIsNotOfferedUnlessATrackerInTheDiscardPileCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tolbrandir = scn.GetShadowSite(9);
		var band = scn.GetShadowCard("tracker3");
		var orcScout = scn.GetShadowCard("orcscout");
		var chieftain = scn.GetShadowCard("chieftain");
		var firstPlayersSeeker = scn.GetFreepsCard("seeker");

		scn.StartGame();

		scn.SkipToSite(8);
		// A tracker in his hand, an Orc that is no tracker in his discard pile, and a tracker in the other
		// player's discard pile
		scn.SetShadowHand(orcScout);
		scn.MoveCardsToDiscard(chieftain, firstPlayersSeeker);
		scn.FreepsPass();

		assertSame(tolbrandir, scn.GetCurrentSite());
		assertEquals(10, scn.GetTwilight());
		// Playing from the discard pile is treated as a cost: with nothing to play, the ability is withheld
		assertTrue(scn.ShadowDecisionAvailable("Play Shadow action or pass"));
		assertTrue(scn.ShadowPlayAvailable(orcScout));
		assertFalse(scn.ShadowActionAvailable(tolbrandir));

		// A tracker in his discard pile that costs 3, and 2 in the pool
		scn.MoveCardsToDiscard(band);
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(orcScout));
		assertFalse(scn.ShadowActionAvailable(tolbrandir));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowActionAvailable(tolbrandir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
