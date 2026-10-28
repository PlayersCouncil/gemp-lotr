package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_362_Tests
{

	// The King path with Narchost as its site 9, reached by SkipToSite(8) and the fellowship-phase move.
	// Site 8 is Watchers of Cirith Ungol, whose only text is a skirmish ability for a [wraith] Orc.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7; a skirmish ability for Hobbits used only on purpose
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; its play trigger is not reached by a cheat
					put("runner2", "1_178");
				}},
				new HashMap<>() {{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_362");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same path in a PC format, where the regroup phase at site 9 is played: the one board on which the Ring
	// can be read coming off at the regroup phase here.
	protected VirtualTableScenario GetRegroupAtSite9Scenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7; a skirmish ability for Hobbits used only on purpose
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; its play trigger is not reached by a cheat
					put("runner2", "1_178");
				}},
				new HashMap<>() {{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_362");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.PCMovie
		);
	}

	@Test
	public void NarchostStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Narchost
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 9
		 * Shadow Number: 9
		 * Direction: Left
		 * Game Text: <b>Shadow:</b> Spot 8 burdens to make the Ring-bearer wear The One Ring until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Narchost", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void ShadowAbilityNeedsEightBurdensToSpotAndNotSeven() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.AddBurdens(7);
		assertEquals(7, scn.GetBurdens());
		scn.MoveMinionsToTable(runner1);

		// The fellowship moves to site 9.
		scn.FreepsPass();

		var narchost = scn.GetCurrentSite();
		assertEquals("Narchost", narchost.getBlueprint().getTitle());
		assertEquals(7, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(narchost));

		// One more burden: exactly 8 to spot.
		scn.AddBurdens(1);
		scn.ShadowRefreshDecision();
		assertEquals(8, scn.GetBurdens());
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(narchost));
	}

	@Test
	public void ShadowAbilityPutsTheRingOnUntilTheRegroupPhaseAndIsNotOfferedAgainWhileItIsWorn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupAtSite9Scenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.SkipToSite(8);
		scn.AddBurdens(8);
		assertEquals(8, scn.GetBurdens());
		scn.MoveMinionsToTable(runner1, runner2);

		// The fellowship moves to site 9.
		scn.FreepsPass();

		var narchost = scn.GetCurrentSite();
		assertEquals("Narchost", narchost.getBlueprint().getTitle());
		assertEquals(8, scn.GetBurdens());
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(narchost));

		scn.ShadowUseCardAction(narchost);

		assertTrue(scn.RBWearingOneRing());
		// Spotting the burdens pays nothing; with the Ring already worn the ability would do nothing.
		assertEquals(8, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(narchost));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner1}, new PhysicalCardImpl[]{boromir, runner2});
		assertTrue(scn.RBWearingOneRing());

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// The second skirmish, after the first has been resolved: still worn.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.RBWearingOneRing());
		scn.PassSkirmishActions();

		// The regroup phase: the Ring is off.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.RBWearingOneRing());
		assertEquals(8, scn.GetBurdens());
	}
}
