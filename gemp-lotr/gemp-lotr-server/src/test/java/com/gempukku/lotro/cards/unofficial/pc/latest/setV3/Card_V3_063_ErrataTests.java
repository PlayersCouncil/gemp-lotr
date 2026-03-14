package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_063_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("watcher", "103_63");
					put("runner", "1_178");
					put("nazgul", "1_232"); // Ulaire Enquea
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CirithUngolWatcherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Cirith Ungol Watcher, Spirit of Vigilance
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Support area
		 * Game Text: To play, spot a [ringwraith] card.
		* 	Each time a companion exerts, draw a card (limit once per site).
		* 	Response: If a companion is exerted by a Free Peoples card, discard another Cirith Ungol Watcher to add (4) and hinder that companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("watcher");

		assertEquals("Cirith Ungol Watcher", card.getBlueprint().getTitle());
		assertEquals("Spirit of Vigilance", card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WatcherCannotPlayWithoutRingwraithSpot() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var watcher = scn.GetShadowCard("watcher");
		scn.MoveCardsToHand(watcher);

		// No ringwraith cards on the table - only a runner (Sauron orc)
		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		// Watcher requires spotting a ringwraith card to play -- runner is Sauron, not ringwraith
		assertFalse(scn.ShadowPlayAvailable(watcher));
	}

	@Test
	public void WatcherCanPlayWithRingwraithSpotAndDoesNotAddBurden() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var watcher = scn.GetShadowCard("watcher");
		var nazgul = scn.GetShadowCard("nazgul");
		scn.MoveCardsToHand(watcher);
		// Place a Nazgul (ringwraith card) on the table for the spot requirement
		scn.MoveMinionsToTable(nazgul);

		scn.StartGame();
		int burdensBefore = scn.GetBurdens();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		// With a Nazgul on the table, the watcher should be playable
		assertTrue(scn.ShadowPlayAvailable(watcher));
		scn.ShadowPlayCard(watcher);

		// Errata removed AddBurdens from ExtraCost, so no burden should be added
		assertEquals(burdensBefore, scn.GetBurdens());
		assertEquals(Zone.SUPPORT, watcher.getZone());
	}
}
