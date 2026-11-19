package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_178_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("runner", "1_178");
					put("runner2", "1_178");
					// Goblin Backstabber: a [moria] Orc of cost 1 and site number 4, like the Runner; damage +2
					// only in a skirmish beside another [moria] Orc
					put("backstabber", "1_174");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Site 2 is Midgewater Marshes, a marsh, where Watcher in the Water stays in play.  Moving there, Frodo exerts.
	protected VirtualTableScenario GetMarshScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("runner", "1_178");
					// Watcher in the Water, Keeper of Westgate: "While you can spot Watcher in the Water, discard
					// all other minions (except tentacles)"; discarded if not at a marsh
					put("watcher", "2_73");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_332");
					put("site3", "1_341");
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
	public void GoblinRunnerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Goblin Runner
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: When you play this minion, you may add (2).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("runner");

		assertEquals("Goblin Runner", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	// "When you play this minion, you may add (2)": the game adds the 2 without asking, and is meant to (F-0009).
	// Read here as the first of the old `at` tests read it in passing (dwarvenAxeDoesNotFreeze, 2011), with the
	// Runner roaming.
	@Test
	public void PlayedWhileRoamingItPaysItsCostAndThePenaltyAndAddsItsTwoWithoutBeingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.FreepsPass();

		// Site 2, and the Runner's site number is 4.  The move left 2 for the site and 1 for the Ring-bearer.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(runner));

		scn.ShadowPlayCard(runner);

		// 3 - 1 to play - 2 roaming + the 2 it adds
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherOrcPlayedAddsNothingAndASecondRunnerAddsItsOwnTwoOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var backstabber = scn.GetShadowCard("backstabber");
		scn.MoveCardsToHand(runner, runner2, backstabber);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();

		// Site 4: no minion here roams.
		assertEquals(4, scn.GetCurrentSiteNumber());
		int pool = scn.GetTwilight();

		scn.ShadowPlayCard(runner);
		// - 1 to play + 2
		assertEquals(pool + 1, scn.GetTwilight());

		// Another [moria] Orc played beside the Runner: - 1 to play, and nothing added.
		scn.ShadowPlayCard(backstabber);
		assertEquals(pool, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// A second Runner: - 1 to play + 2 for its own play, and the first Runner adds nothing.
		scn.ShadowPlayCard(runner2);
		assertEquals(pool + 1, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, runner, runner2, backstabber);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// CR 4.2 "discard": "If Watcher in the Water, Keeper of Westgate is in play ... and you play Goblin Runner
	// ..., (2) is not added."
	@Test
	public void PlayedBesideWatcherInTheWaterItIsDiscardedAndAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var runner = scn.GetShadowCard("runner");
		var watcher = scn.GetShadowCard("watcher");
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		// The move to Midgewater Marshes adds only 2: enough to pay for the Runner's roaming.
		scn.SetTwilight(3);
		scn.FreepsPass();
		scn.MoveMinionsToTable(watcher);
		scn.ShadowRefreshDecision();

		assertEquals(2, scn.GetCurrentSiteNumber());
		int pool = scn.GetTwilight();

		scn.ShadowPlayCard(runner);

		// - 1 to play - 2 roaming (site number 4 at site 2), and the (2) not added
		assertEquals(pool - 3, scn.GetTwilight());
		assertInZone(Zone.DISCARD, runner);
		assertInZone(Zone.SHADOW_CHARACTERS, watcher);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
