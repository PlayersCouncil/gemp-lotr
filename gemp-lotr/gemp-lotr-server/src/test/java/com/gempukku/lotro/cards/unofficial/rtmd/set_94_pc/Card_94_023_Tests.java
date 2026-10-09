package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_023_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Goblin Backstabber: [moria] Orc, cost 1, site number 4, not unique; damage +2 only in a
					// skirmish beside another [moria] Orc
					put("backstabber", "1_174");
					// Uruk Savage: [isengard] Uruk-hai, site number 5; Damage +1 is its whole text
					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_23", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("backstabber", "1_174"); // Goblin Backstabber: [moria] Orc, cost 1, site number 4
					put("savage", "1_151");      // Uruk Savage: [isengard] Uruk-hai, site number 5
				}},
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
		var savage = scn.GetShadowCard("savage");

		scn.MoveMinionsToTable(backstabber, savage);
		scn.StartGame();

		// Printed site 4, +1 because the runner (Shadow) owns both the mod and the minion
		assertEquals(5, scn.GetMinionSiteNumber(backstabber));
		// "Each of your minions": an Uruk-hai of another culture, printed site 5
		assertEquals(6, scn.GetMinionSiteNumber(savage));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void YourOwnMinionsSiteNumberIsIncreasedOnPlayerTwosTurnWhenPlayerOneOwnsTheMod() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		// Player 1 owns the mod, and on player 2's turn it is player 1 who has minions
		var backstabber = scn.GetFreepsCard("backstabber");
		var savage = scn.GetFreepsCard("savage");

		scn.StartGame();
		scn.SkipToOtherPlayersTurn();
		scn.MoveMinionsToTable(backstabber, savage);

		// Printed site 4 and 5, +1 as the mod owner's own minions
		assertEquals(5, scn.GetMinionSiteNumber(backstabber));
		assertEquals(6, scn.GetMinionSiteNumber(savage));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void OpponentsMinionsSiteNumberIsDecreased() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		// The minion is still Shadow's; the mod is owned by Freeps, so "not(your)" matches it.
		var backstabber = scn.GetShadowCard("backstabber");
		var savage = scn.GetShadowCard("savage");

		scn.MoveMinionsToTable(backstabber, savage);
		scn.StartGame();

		// Printed site 4, -1 because the minion belongs to the mod owner's opponent
		assertEquals(3, scn.GetMinionSiteNumber(backstabber));
		// "Each of your opponent's minions": an Uruk-hai of another culture, printed site 5
		assertEquals(4, scn.GetMinionSiteNumber(savage));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OpponentsMinionsSiteNumberIsDecreasedOnPlayerTwosTurnWhenPlayerTwoOwnsTheMod() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		// Player 2 owns the mod, and on his own turn the minions are player 1's: his opponent's
		var backstabber = scn.GetFreepsCard("backstabber");
		var savage = scn.GetFreepsCard("savage");

		scn.StartGame();
		scn.SkipToOtherPlayersTurn();
		scn.MoveMinionsToTable(backstabber, savage);

		// Printed site 4 and 5, -1 as minions of the mod owner's opponent
		assertEquals(3, scn.GetMinionSiteNumber(backstabber));
		assertEquals(4, scn.GetMinionSiteNumber(savage));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
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
		// 1 to play + 2 roaming
		scn.ShadowPlayCard(backstabber);
		assertEquals(twilightBefore - 1 - 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
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
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
