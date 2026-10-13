package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_062_Tests
{
	// The Ring-bearer is Frodo, Son of Drogo.  He is kept out of every skirmish but the one that is about him: The
	// Ruling Ring answers a wound he is about to take in one.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "0_64");     // Gandalf, Stormcrow: strength 8, vitality 4, no text
					put("aragorn", "4_109");    // Aragorn, Heir of Elendil: strength 8, vitality 4, defender +1 and no text
					put("grimir", "1_17");      // Grimir, Dwarven Elder: ally, strength 3, vitality 3; a fellowship ability
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: ally, vitality 2; a maneuver ability, never used
					// One Whom Men Would Follow: "Maneuver: Exert Aragorn to spot an ally. Until the regroup phase,
					// that ally is strength +2 and participates in archery fire and skirmishes."  It puts an ally
					// into a skirmish away from his home site without exerting him.
					put("follow", "1_109");

					put("savage1", "1_151");    // Uruk Savage: [isengard], strength 5, vitality 3, damage +1
					put("savage2", "1_151");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria], strength 13, vitality 3, no text
					put("runner1", "1_178");    // Goblin Runner: [moria], strength 5, vitality 1
					put("runner2", "1_178");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "101_62");
					put("site8", "1_356");      // Anduin Banks: adds to the minion archery total for companions over 4
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ValleyoftheAnduinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Valley of the Anduin
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 7
		 * Shadow Number: 6
		 * Direction: Right
		 * Game Text: River. At the start of each skirmish phase, wound each unwounded character in that skirmish.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(7);

		assertEquals("Valley of the Anduin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(7, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void EachSkirmishWoundsItsUnwoundedCompanionAndMinionAsItBeginsAndNobodyOutsideIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage1");
		var troop = scn.GetShadowCard("troop");
		var idle = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(gandalf, aragorn);

		scn.FreepsPass();
		assertEquals("Valley of the Anduin", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(savage, troop, idle);

		scn.SkipToAssignments();
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(savage));

		// Gandalf meets the Uruk Savage and Aragorn the Goblin Patrol Troop.  Frodo and the second Savage are left
		// out of it.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { gandalf, savage }, new PhysicalCardImpl[] { aragorn, troop });
		scn.ShadowDeclineAssignments();
		// Nothing yet: the assignment phase is not a skirmish phase.
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.FreepsResolveSkirmish(gandalf);
		// The skirmish has begun and nobody has yet acted in it.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(savage));
		// Not the pair whose skirmish is still to come, nor the companion and the minion who are in none.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(idle));

		// Gandalf's 8 beats the Savage's 5: the Savage takes the wound for losing, and that is all.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(troop));

		// The second skirmish phase of the turn wounds its own two and nobody else.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(idle));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DoesNotWoundACompanionOrMinionWhoIsAlreadyWoundedAndKillsAnUnwoundedMinionOfVitalityOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var fresh = scn.GetShadowCard("savage1");
		var hurt = scn.GetShadowCard("savage2");
		var runner = scn.GetShadowCard("runner1");
		var idleRunner = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(gandalf);
		scn.AddWoundsToChar(gandalf, 1);

		scn.FreepsPass();
		assertEquals("Valley of the Anduin", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(fresh, hurt, runner, idleRunner);
		// Vitality 3 with 2 wounds: one more would kill it.
		scn.AddWoundsToChar(hurt, 2);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(gandalf, fresh, hurt, runner);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(fresh));
		assertEquals(2, scn.GetWoundsOn(hurt));
		assertInPlay(runner, idleRunner);

		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(fresh));
		assertEquals(2, scn.GetWoundsOn(hurt));
		assertInPlay(hurt);
		// A wound, not an exertion: the Goblin Runner in the skirmish has 1 vitality and dies of it.  The one that
		// was not assigned is untouched.
		assertInDiscard(runner);
		assertInPlay(idleRunner);
		assertEquals(0, scn.GetWoundsOn(idleRunner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WoundsAnAllyWhoIsInTheSkirmishAndNotOneWhoIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var grimir = scn.GetFreepsCard("grimir");
		var thrarin = scn.GetFreepsCard("thrarin");
		var follow = scn.GetFreepsCard("follow");
		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(grimir, thrarin);
		scn.MoveCardsToHand(follow);

		scn.FreepsPass();
		assertEquals("Valley of the Anduin", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(savage);

		// One Whom Men Would Follow exerts Aragorn and lets Grimir skirmish this turn.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(follow);
		scn.FreepsChooseCard(grimir);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(grimir, savage);
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.FreepsResolveSkirmish(grimir);
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WoundsTheRingBearerWhenTheSkirmishIsHis() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.SkipToSite(6);

		scn.FreepsPass();
		assertEquals("Valley of the Anduin", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.FreepsResolveSkirmish(frodo);
		// The text makes no exception of the Ring-bearer.  The Ruling Ring answers a wound its bearer is about to
		// take in a skirmish; it is left unused, and the wound lands.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scn.GetRing()));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
