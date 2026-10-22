package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_080_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("beaten1", "10_80");
					put("chieftain1", "1_266");  // Orc Chieftain: a [sauron] Orc, strength 7, vitality 2; text only on a weapon played on him
					put("chieftain2", "1_266");
					put("sentry", "10_84");      // Cirith Ungol Sentry: a [sauron] Uruk-hai, strength 8; damage +1 and nothing else
					// Goblin Runner: a [moria] Orc, strength 5; his "may add (2)" answers only his own play, and he is cheated in.
					put("runner", "1_178");
					// Strength Born of Fear: a [sauron] condition, no minion; its skirmish ability costs (3) and is not used.
					put("fear", "1_278");
					// Cards that only make up a hand's count: Goblin Runners, which neither player plays here.
					put("filler1", "1_178");
					put("filler2", "1_178");
					put("filler3", "1_178");
					put("filler4", "1_178");

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("sam", "1_311");         // Sam, Son of Hamfast: strength 3; abilities used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BeatenBackStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Beaten Back
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a [sauron] minion strength +2 for each site you control. If you have initiative, that minion is <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var beaten = scn.GetFreepsCard("beaten1");

		assertEquals("Beaten Back", beaten.getBlueprint().getTitle());
		assertNull(beaten.getBlueprint().getSubtitle());
		assertFalse(beaten.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, beaten.getBlueprint().getSide());
		assertEquals(Culture.SAURON, beaten.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, beaten.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(beaten, Timeword.SKIRMISH));
		assertEquals(1, beaten.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsNotPlayableInTheAssignmentPhaseAndInASkirmishMakesASauronMinionPlus2ForEachOfTwoSitesForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var beaten = scn.GetShadowCard("beaten1");
		var chieftain1 = scn.GetShadowCard("chieftain1");
		var chieftain2 = scn.GetShadowCard("chieftain2");
		var sentry = scn.GetShadowCard("sentry");
		var runner = scn.GetShadowCard("runner");
		var fear = scn.GetShadowCard("fear");

		scn.StartGame();
		scn.SkipToSite(3);
		// Sites 1 and 2, behind both markers, to the Shadow player.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.MoveMinionsToTable(chieftain1, chieftain2, sentry, runner);
		scn.MoveCardsToSupportArea(fear);
		// Four cards in the Free Peoples hand: the Shadow has no initiative.
		scn.SetFreepsHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"), scn.GetFreepsCard("filler4"));
		scn.SetShadowHand(beaten);
		scn.SetTwilight(5);
		assertEquals(4, scn.GetFreepsHandCount());
		assertInHand(beaten);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertFalse(scn.ShadowHasInitiative());

		// The phase next door: the Shadow player's assignment actions.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(beaten));
		scn.ShadowPass();

		// The second Chieftain and the Runner are left out of every skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, chieftain1}, new PhysicalCardImpl[]{sam, sentry});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(chieftain1));
		assertTrue(scn.ShadowPlayAvailable(beaten));

		scn.ShadowPlayCard(beaten);
		// The text names no skirmish: the Orc in this skirmish, the Uruk-hai in the next and the Orc in none are all
		// offered; not the [moria] Orc, the [sauron] condition or the companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain1, sentry, chieftain2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, fear, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(chieftain1);

		// 7 + 2 for each of the two sites the Shadow player controls.
		assertInDiscard(beaten);
		assertEquals(11, scn.GetStrength(chieftain1));
		assertEquals(7, scn.GetStrength(chieftain2));
		assertEquals(8, scn.GetStrength(sentry));
		// No initiative: not fierce.
		assertFalse(scn.HasKeyword(chieftain1, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The bonus lasts the skirmish it was given in: in the next it is gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(chieftain1));
	}

	@Test
	public void WithInitiativeThatMinionIsFierceUntilTheRegroupPhaseAndASiteTheFreePeoplesPlayerControlsIsNotCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var beaten = scn.GetShadowCard("beaten1");
		var chieftain1 = scn.GetShadowCard("chieftain1");
		var sentry = scn.GetShadowCard("sentry");

		scn.StartGame();
		scn.SkipToSite(3);
		// Site 1 to the Free Peoples player, site 2 to the Shadow player.
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.MoveMinionsToTable(chieftain1, sentry);
		// Three cards in the Free Peoples hand: the Shadow has initiative.  The Shadow player's own hand is the
		// larger, and is no part of it.
		scn.SetFreepsHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));
		scn.SetShadowHand(beaten, scn.GetShadowCard("filler1"), scn.GetShadowCard("filler2"), scn.GetShadowCard("filler3"), scn.GetShadowCard("filler4"));
		scn.SetTwilight(5);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetShadowHandCount());
		scn.SkipToAssignments();
		assertTrue(scn.ShadowHasInitiative());

		// The Sentry is left out of the skirmish.
		scn.FreepsAssignToMinions(aragorn, chieftain1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertFalse(scn.HasKeyword(chieftain1, Keyword.FIERCE));
		scn.ShadowPlayCard(beaten);
		scn.ShadowChooseCard(chieftain1);

		// 7 + 2 for the one site the Shadow player controls; the Free Peoples player's site counts for nothing.
		assertEquals(9, scn.GetStrength(chieftain1));
		assertTrue(scn.HasKeyword(chieftain1, Keyword.FIERCE));
		// "That minion": the other [sauron] minion is not made fierce.
		assertFalse(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The Chieftain (9) beats Aragorn (8), and is still fierce for the fierce round, where his bonus is gone.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(chieftain1, Keyword.FIERCE));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, chieftain1);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(chieftain1));
		assertTrue(scn.HasKeyword(chieftain1, Keyword.FIERCE));

		// Aragorn (8) beats him, and he lives with 1 wound: in the regroup phase he is fierce no longer.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(chieftain1));
		assertFalse(scn.HasKeyword(chieftain1, Keyword.FIERCE));
	}

	@Test
	public void WithNoSauronMinionToChooseItIsStillPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var beaten = scn.GetShadowCard("beaten1");
		var runner = scn.GetShadowCard("runner");
		var fear = scn.GetShadowCard("fear");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToSupportArea(fear);
		scn.SetFreepsHand(scn.GetFreepsCard("filler1"));
		scn.SetShadowHand(beaten);
		scn.SetTwilight(5);
		assertInHand(beaten);
		scn.SkipToAssignments();
		assertTrue(scn.ShadowHasInitiative());

		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.ShadowPlayAvailable(beaten));

		// Only a [moria] Orc and a [sauron] condition: nobody is asked, and nothing changes.
		scn.ShadowPlayCard(beaten);
		assertInDiscard(beaten);
		assertEquals(5, scn.GetStrength(runner));
		assertFalse(scn.HasKeyword(runner, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
