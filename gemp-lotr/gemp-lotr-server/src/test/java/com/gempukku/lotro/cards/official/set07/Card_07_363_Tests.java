package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_363_Tests
{

	// The King path with Slag Mounds as its site 9, reached by SkipToSite(8) and the fellowship-phase move.
	// Site 8 is Watchers of Cirith Ungol, whose only text is a skirmish ability for a [wraith] Orc.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "1_7");        // Dwarf Guard: [dwarven] Dwarf companion, strength 4, vitality 2
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, strength 7, vitality 3; a skirmish ability for Hobbits
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					// Thrarin, Dwarven Smith: Dwarf ally, strength 4, vitality 2.  "Maneuver: Exert Thrarin to allow him
					// to participate in archery fire and skirmishes until the regroup phase."
					put("thrarin", "1_27");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text; overwhelms anyone of strength 6 or less
					put("troop2", "1_177");
					put("brigand", "4_10");     // Dunlending Brigand: strength 8, vitality 1, no text
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its play trigger is not reached by a cheat
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
					put("site9", "7_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same path in a PC format, where the regroup phase at site 9 is played (the game is won when both players
	// have passed in it): the one board on which a companion is discarded from play beside this site.
	protected VirtualTableScenario GetRegroupAtSite9Scenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "1_7");        // Dwarf Guard: [dwarven] Dwarf companion, no text but its requirement to play
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion; a maneuver ability used only on purpose
					put("despair", "7_148");    // Fierce in Despair: a Shadow Regroup event, twilight 7: "Spot 2 [raider] Men to discard a companion (except the Ring-bearer)."
					put("axeman1", "4_224");    // Easterling Axeman: a [raider] Man minion; his text needs 2 burdens
					put("axeman2", "4_224");
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
					put("site9", "7_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.PCMovie
		);
	}

	@Test
	public void SlagMoundsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Slag Mounds
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 9K
		 * Shadow Number: 9
		 * Direction: Left
		 * Game Text: Each time a companion is killed, add a burden.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Slag Mounds", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void EachCompanionKilledAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var boromir = scn.GetFreepsCard("boromir");
		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(guard, boromir);

		scn.StartGame();
		scn.SkipToSite(8);
		scn.FreepsPass();

		assertEquals("Slag Mounds", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(troop, brigand);
		// Boromir has one vitality left, so the wound for losing kills him.
		scn.AddWoundsToChar(boromir, 2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, troop}, new PhysicalCardImpl[]{boromir, brigand});

		assertEquals(0, scn.GetBurdens());

		// A Dwarf overwhelmed: 13 is more than twice 4.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		// The site's trigger is ordered against the rule's own business of the death.
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.FreepsDecisionAvailable("Required responses"));
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetBurdens());

		// A Man killed by his last wound: 8 beats 7.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, boromir);
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.GameIsFinished());
	}

	@Test
	public void AnAllyKilledAMinionKilledAndACompanionWoundedAddNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		var troop = scn.GetShadowCard("troop2");
		var brigand = scn.GetShadowCard("brigand");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.SkipToSite(8);
		scn.FreepsPass();

		assertEquals("Slag Mounds", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(troop, brigand, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thrarin, troop}, new PhysicalCardImpl[]{aragorn, runner},
				new PhysicalCardImpl[]{boromir, brigand});

		assertEquals(0, scn.GetBurdens());

		// An ally overwhelmed: 13 is more than twice 4.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, thrarin);
		assertEquals(0, scn.GetBurdens());

		// A minion killed by the wound for losing: 8 beats 5, vitality 1.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertEquals(0, scn.GetBurdens());

		// A companion who loses and lives: 8 beats 7, and Boromir has three vitality.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.GameIsFinished());
	}

	@Test
	public void ACompanionDiscardedFromPlayIsNotKilledAndAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupAtSite9Scenario();

		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var despair = scn.GetShadowCard("despair");
		var axeman1 = scn.GetShadowCard("axeman1");
		var axeman2 = scn.GetShadowCard("axeman2");
		scn.MoveCompanionsToTable(guard, aragorn);

		scn.StartGame();
		scn.SkipToSite(8);
		scn.FreepsPass();

		assertEquals("Slag Mounds", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(axeman1, axeman2);
		scn.MoveCardsToHand(despair);
		scn.SkipToPhase(Phase.REGROUP);
		// Fierce in Despair costs 7.
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetFreepsDeadCount());
		scn.ShadowPlayCard(despair);
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, aragorn));
		scn.ShadowChooseCard(guard);

		// Discarded from play is not killed (CR 4.2 "killed"): the dead pile stays empty and no burden is added.
		assertInZone(Zone.DISCARD, guard);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
