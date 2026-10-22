package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_078_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scout1", "10_78");
					put("scout2", "10_78");
					put("chieftain1", "1_266");  // Orc Chieftain: a [sauron] Orc, cost 2, site 6; text only on a weapon played on him
					put("chieftain2", "1_266");
					put("sentry", "10_84");      // Cirith Ungol Sentry: a [sauron] Uruk-hai, site 6; damage +1 and nothing else
					// Goblin Runner: a [moria] Orc, site 4; his "may add (2)" answers only his own play, and he is cheated in.
					put("runner", "1_178");
					// Strength Born of Fear: a [sauron] condition, no minion; its skirmish ability costs (3) and is not used.
					put("fear", "1_278");

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("sam", "1_311");         // Sam, Son of Hamfast: strength 3; abilities used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AdvanceScoutStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Advance Scout
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Shadow:</b> Exert or discard this minion to make each [sauron] minion you spot roaming until the regroup phase.
		*/

		var scn = GetScenario();

		var scout = scn.GetFreepsCard("scout1");

		assertEquals("Advance Scout", scout.getBlueprint().getTitle());
		assertNull(scout.getBlueprint().getSubtitle());
		assertFalse(scout.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, scout.getBlueprint().getSide());
		assertEquals(Culture.SAURON, scout.getBlueprint().getCulture());
		assertEquals(CardType.MINION, scout.getBlueprint().getCardType());
		assertEquals(Race.ORC, scout.getBlueprint().getRace());
		assertEquals(2, scout.getBlueprint().getTwilightCost());
		assertEquals(7, scout.getBlueprint().getStrength());
		assertEquals(2, scout.getBlueprint().getVitality());
		assertEquals(6, scout.getBlueprint().getSiteNumber());
	}

	@Test
	public void ExertedItMakesTheSauronMinionsSpottedRoamingUntilTheRegroupPhaseAndNotOneOfThemPlayedLater() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var scout1 = scn.GetShadowCard("scout1");
		var scout2 = scn.GetShadowCard("scout2");
		var chieftain1 = scn.GetShadowCard("chieftain1");
		var chieftain2 = scn.GetShadowCard("chieftain2");
		var sentry = scn.GetShadowCard("sentry");
		var runner = scn.GetShadowCard("runner");
		var fear = scn.GetShadowCard("fear");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(scout1, scout2, chieftain1, sentry, runner);
		scn.MoveCardsToSupportArea(fear);
		scn.MoveCardsToHand(chieftain2);
		scn.SetTwilight(10);
		// The move to site 6: every one of these minions is at or under his site number, and none is roaming.
		scn.FreepsPass();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		for (var minion : new PhysicalCardImpl[]{scout1, scout2, chieftain1, sentry, runner}) {
			assertFalse(scn.HasKeyword(minion, Keyword.ROAMING));
		}
		assertEquals(0, scn.GetWoundsOn(scout1));
		assertTrue(scn.ShadowActionAvailable(scout1));

		scn.ShadowUseCardAction(scout1);
		// Both halves of the cost can be paid: the Shadow player is asked which.
		assertEquals(List.of("Exert this minion", "Discard this minion"), scn.ShadowGetMultipleChoices());
		scn.ShadowChooseOption("Exert this minion");
		assertEquals(1, scn.GetWoundsOn(scout1));
		assertInZone(Zone.SHADOW_CHARACTERS, scout1);

		// "Each [sauron] minion you spot": the player chooses which he spots (CR 4.2 "spot"), any of them or none.
		// The [moria] Orc, the [sauron] condition and the companion are no candidates.
		assertTrue(scn.ShadowHasCardChoiceAvailable(scout1, scout2, chieftain1, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, fear, aragorn));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(scout1, chieftain1, sentry);

		assertTrue(scn.HasKeyword(scout1, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(chieftain1, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(sentry, Keyword.ROAMING));
		// Not spotted, and not of the culture.
		assertFalse(scn.HasKeyword(scout2, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(runner, Keyword.ROAMING));

		// A [sauron] minion played after the ability was used is not one of those it made roaming: at site 6 a site 6
		// minion is not roaming, and he is played for his cost alone, with no roaming penalty.
		// 10 set + 3 companions + 3 for Galadriel's Glade
		assertEquals(16, scn.GetTwilight());
		scn.ShadowPlayCard(chieftain2);
		assertEquals(14, scn.GetTwilight());
		assertFalse(scn.HasKeyword(chieftain2, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Until the regroup phase: still roaming in the second skirmish, after the first has resolved.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, scout2}, new PhysicalCardImpl[]{sam, runner});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.HasKeyword(scout1, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(chieftain1, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(sentry, Keyword.ROAMING));

		// The Runner (strength 5) beats Sam (3) and the last skirmish is over: the regroup phase, and roaming is gone.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(scout1, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(chieftain1, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(sentry, Keyword.ROAMING));
	}

	@Test
	public void ExhaustedItIsDiscardedWithoutBeingAskedAndTheOthersAreMadeRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout1 = scn.GetShadowCard("scout1");
		var scout2 = scn.GetShadowCard("scout2");
		var chieftain1 = scn.GetShadowCard("chieftain1");
		var sentry = scn.GetShadowCard("sentry");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(scout1, scout2, chieftain1, sentry);
		// Vitality 2 with 1 wound: he cannot exert.
		scn.AddWoundsToChar(scout1, 1);
		assertEquals(1, scn.GetWoundsOn(scout1));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.HasKeyword(chieftain1, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(sentry, Keyword.ROAMING));
		assertTrue(scn.ShadowActionAvailable(scout1));

		scn.ShadowUseCardAction(scout1);
		// Only the discard can be paid, and no choice of the two is asked.
		assertInDiscard(scout1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(scout2, chieftain1, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(scout1));
		scn.ShadowChooseCards(chieftain1, sentry);

		assertTrue(scn.HasKeyword(chieftain1, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(sentry, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(scout2, Keyword.ROAMING));
		assertEquals(0, scn.GetWoundsOn(scout2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedInTheShadowPhaseItIsOfferedThereAndNotInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout1 = scn.GetShadowCard("scout1");
		var chieftain1 = scn.GetShadowCard("chieftain1");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(chieftain1);
		scn.MoveCardsToHand(scout1);
		scn.SetTwilight(10);
		assertInHand(scout1);
		scn.FreepsPass();

		scn.ShadowPlayCard(scout1);
		assertInZone(Zone.SHADOW_CHARACTERS, scout1);
		assertEquals(0, scn.GetWoundsOn(scout1));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(scout1));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		// Unwounded, with both halves of its cost payable: the phase alone keeps it off.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(scout1));
	}
}
