package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_023_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("backstabber", "1_174"); // Goblin Backstabber: clean, non-unique Moria minion printed at site 4
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_23", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_23"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_23
		 * Type: MetaSite
		 * Intensity: 3
		 * Game Text: The site number of each of your minions is +1. The site number of each of your
		 * opponent's minions is -1.
		 */
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		assertEquals("Race Text 94_23", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(3, mod.getBlueprint().getIntensity());
	}

	@Test
	public void YourOwnMinionsSiteNumberIsIncreased() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveMinionsToTable(backstabber);
		scn.StartGame();

		// Printed site 4, +1 because the runner (Shadow) owns both the mod and the minion
		assertEquals(5, scn.GetMinionSiteNumber(backstabber));
	}

	@Test
	public void OpponentsMinionsSiteNumberIsDecreased() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		// The minion is still Shadow's; the mod is owned by Freeps, so "not(your)" matches it.
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveMinionsToTable(backstabber);
		scn.StartGame();

		// Printed site 4, -1 because the minion belongs to the mod owner's opponent
		assertEquals(3, scn.GetMinionSiteNumber(backstabber));
	}

	@Test
	public void PlayingFromHandIsChargedRoamingWhenTheIncreasedSiteNumberExceedsTheCurrentSite()
			throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveCardsToHand(backstabber);
		scn.StartGame();
		scn.SkipToSite(3);
		scn.SkipToPhase(Phase.SHADOW); // this turn's mandatory move lands the fellowship on site 4
		assertEquals(4, scn.GetCurrentSiteNumber());

		scn.SetTwilight(10);
		int twilightBefore = scn.GetTwilight();

		// Modified site number is 5 (printed 4, +1 as the runner's own minion) which is > the
		// current site (4), so the not-yet-played minion is charged the roaming penalty.
		scn.ShadowPlayCard(backstabber);
		assertEquals(twilightBefore - 1 - 2, scn.GetTwilight());
	}

	@Test
	public void PlayingFromHandIsNotChargedRoamingWhenTheDecreasedSiteNumberMatchesTheCurrentSite()
			throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveCardsToHand(backstabber);
		scn.StartGame();
		scn.SkipToSite(2);
		scn.SkipToPhase(Phase.SHADOW); // this turn's mandatory move lands the fellowship on site 3
		assertEquals(3, scn.GetCurrentSiteNumber());

		scn.SetTwilight(10);
		int twilightBefore = scn.GetTwilight();

		// Printed site 4 would normally roam here (4 > 3), but the mod's -1 (minion is not(your))
		// brings it down to 3, matching the current site, so no roaming penalty applies.
		scn.ShadowPlayCard(backstabber);
		assertEquals(twilightBefore - 1, scn.GetTwilight());
	}
}
