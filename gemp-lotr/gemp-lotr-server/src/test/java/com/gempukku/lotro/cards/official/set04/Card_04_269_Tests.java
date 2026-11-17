package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_04_269_Tests
{

	// The Fellowship path with Rivendell Waterfall as site 3 (a forest, no plains) and Hollin as site 4 (a plains).
	// With Aragorn, a ranger, at the Waterfall the move limit is 3, so the turn has two regroup moves: one from
	// Ettenmoors (site 2, a plains) to the Waterfall, and one from the Waterfall to Hollin.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eothain", "4_269");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a ranger; a maneuver ability used only on purpose
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3; no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1 (always exhausted); its trigger is on being played
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");      // Ettenmoors: plains; a skirmish ability used only on purpose
					put("site3", "1_342");      // Rivendell Waterfall: forest, sanctuary; the move limit +1 with a ranger there
					put("site4", "2_119");      // Hollin: plains; Uruk-hai are not roaming
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

	// The Towers path with Horse-country as site 1: with a [rohan] mount spotted there the move limit is 3 for the
	// turn, and sites 2, 3 and 4 (Eastfold, Westemnet Plains, Westemnet Hills) are all plains.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eothain", "4_269");
					put("snowmane", "13_136");  // Snowmane, Noble Mearas: a [rohan] mount on a [rohan] Man; its play trigger and skirmish ability reach no test
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3; no text
				}},
				new HashMap<>()
				{{
					put("site1", "4_326");      // Horse-country: plains; the move limit +1 for the turn while a [rohan] mount is spotted there
					put("site2", "4_331");      // Eastfold: plains; a Shadow action that needs 3 [isengard] trackers
					put("site3", "4_342");      // Westemnet Plains: plains, sanctuary; a bar on [dunland] Men's archery wounds
					put("site4", "4_344");      // Westemnet Hills: plains; the move to it heals each mounted companion and ally
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EothainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Éothain, Scout of the Mark
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Each time the fellowship moves to a plains site, each minion must exert.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eothain");

		assertEquals("Éothain", card.getBlueprint().getTitle());
		assertEquals("Scout of the Mark", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void AMoveFromAPlainsToAForestExertsNoMinionAndTheMoveToAPlainsExertsEachMinionButNoCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eothain = scn.GetFreepsCard("eothain");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(eothain, aragorn);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.FreepsPass();          // move 1 -> 2, to Ettenmoors; no minion is in play
		assertEquals(2, scn.GetCurrentSiteNumber());

		scn.MoveMinionsToTable(savage, troop, runner);
		scn.SkipToMovementDecision();
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));

		// From Ettenmoors, a plains, to Rivendell Waterfall, a forest: no minion exerts.
		scn.FreepsChooseToMove();  // move 2 -> 3
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		assertEquals(3, scn.GetMoveLimit());
		scn.FreepsChooseToMove();  // move 3 -> 4, to Hollin, a plains
		assertEquals(4, scn.GetCurrentSiteNumber());

		// Minions of two cultures and races each exert once.  The Runner, exhausted, cannot exert: he takes no
		// wound and stays in play (a wound would have killed him).
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		// No character who is no minion exerts.
		assertEquals(0, scn.GetWoundsOn(eothain));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachOfTwoMovesToAPlainsInOneTurnExertsTheSameMinionsAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var eothain = scn.GetFreepsCard("eothain");
		var snowmane = scn.GetFreepsCard("snowmane");
		scn.MoveCompanionsToTable(eothain);
		scn.AttachCardsTo(eothain, snowmane);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertEquals(eothain, snowmane.getAttachedTo());
		assertEquals(3, scn.GetMoveLimit());
		scn.FreepsPass();          // move 1 -> 2, to Eastfold; no minion is in play

		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToMovementDecision();
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.FreepsChooseToMove();  // move 2 -> 3, to Westemnet Plains
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(troop));

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();  // move 3 -> 4, to Westemnet Hills
		assertEquals(4, scn.GetCurrentSiteNumber());
		// Westemnet Hills' own move trigger (a heal of mounted companions) is required too: the order is asked.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveActionOrder("Éothain");
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(eothain));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
