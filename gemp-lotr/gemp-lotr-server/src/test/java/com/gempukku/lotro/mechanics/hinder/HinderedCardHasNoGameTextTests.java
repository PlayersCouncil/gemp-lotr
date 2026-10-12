package com.gempukku.lotro.mechanics.hinder;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

/**
 * A hindered card has no game text.
 *
 * Comprehensive Rules 5.0, "hinder": "The entire game text of hindered cards does not apply.  This includes
 * triggers, special abilities, modifiers, keywords, and other effects."
 *
 * One test here has to count a second sentence of the same entry, because the trigger it reads answers a move:
 * "If a hindered card is a Companion, it does not add 1 twilight when moving."
 *
 * In every test the hindering is only how the board is laid, and is done by the rig.  What is read is what the
 * hindered card's text would have done to the game: an offer, another card's number, a twilight pool, a draw deck.
 * (That a hindered card's keyword cannot be SPOTTED by another card is HinderedCardCannotBeSpottedTests'.)
 */
public class HinderedCardHasNoGameTextTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Aragorn, Ranger of the North: "Ranger.  Maneuver: Exert Aragorn to make him defender +1
					// until the regroup phase."  Resistance 6.
					put("aragorn", "1_89");
					put("troop1", "1_177");     // Goblin Patrol Troop: an Orc, cost 6, site 4, no text
					put("troop2", "1_177");
					// Legolas, Greenleaf: "Archer.  Archery: Exert Legolas to wound a minion; Legolas does not
					// add to the fellowship archery total."  The ability is never used.
					put("legolas", "1_50");

					// Orc Commander: "Each other [isengard] Orc is strength +1."  A regroup ability never used.
					put("commander", "3_64");
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, strength 6; a Shadow ability never used
					// Elendil, High-king of Gondor: "Each of your [gondor] companions is resistance +1."  His
					// maneuver ability needs a [gondor] token.
					put("elendil", "13_65");

					// Greatest Kingdom of My People: [dwarven] condition in the support area.  "Each time your
					// opponent plays an Orc, that player discards the top card of his or her draw deck."
					put("kingdom", "1_16");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Aragorn, Heir to the White City: "Each time the fellowship moves during the fellowship phase, remove (2)."
	// He shares a title with the Aragorn of the other table, so he has one of his own.
	protected VirtualTableScenario GetMoveScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("heir", "3_38");
				}},
				// Site 2 is Ettenmoors, Shadow number 2.
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AHinderedCompanionsSpecialAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(aragorn));

		scn.HinderCard(aragorn);
		scn.FreepsRefreshDecision();
		assertFalse(scn.FreepsActionAvailable(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.RestoreCard(aragorn);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(aragorn));

		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AHinderedCardsStandingTextIsOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var elendil = scn.GetFreepsCard("elendil");
		var commander = scn.GetShadowCard("commander");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCompanionsToTable(aragorn, elendil);

		scn.StartGame();
		scn.MoveMinionsToTable(commander, forger);

		// A minion's text on another minion, and a companion's on another companion.
		// 6 + 1
		assertEquals(7, scn.GetStrength(forger));
		// 6 + 1
		assertEquals(7, scn.GetResistance(aragorn));

		scn.HinderCard(commander);
		assertTrue(scn.IsHindered(commander));
		assertEquals(6, scn.GetStrength(forger));
		assertEquals(7, scn.GetResistance(aragorn));

		scn.RestoreCard(commander);
		assertEquals(7, scn.GetStrength(forger));

		scn.HinderCard(elendil);
		assertTrue(scn.IsHindered(elendil));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(7, scn.GetStrength(forger));

		scn.RestoreCard(elendil);
		assertEquals(7, scn.GetResistance(aragorn));
	}

	@Test
	public void ACompanionsTriggerOnAMoveHappensWhileHeIsNotHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMoveScenario();

		var heir = scn.GetFreepsCard("heir");
		scn.MoveCompanionsToTable(heir);

		scn.StartGame();
		// His text is answered before the twilight of the move is added, so the pool has something to remove.
		scn.SetTwilight(3);
		scn.FreepsPass();

		// 3 set - 2 for his text + 2 for the site + 2 companions
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AHinderedCompanionsTriggerDoesNotHappen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMoveScenario();

		var heir = scn.GetFreepsCard("heir");
		scn.MoveCompanionsToTable(heir);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.HinderCard(heir);
		scn.FreepsPass();

		// 3 set + 2 for the site + 1 companion.  Nothing is removed for his text; and, hindered, he is not a
		// companion who adds 1 to the pool for the move.  (With his text in force this would read 4, and 7 if
		// it were off and he added his 1.)
		assertTrue(scn.IsHindered(heir));
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AHinderedConditionsTriggerDoesNotHappen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCardsToSupportArea(kingdom);
		scn.MoveCardsToHand(troop1, troop2);

		scn.StartGame();
		// Enough for two minions of cost 6, each roaming at site 2.
		scn.SetTwilight(20);
		scn.FreepsPass();

		// The opponent plays an Orc: the top card of his draw deck is discarded.
		var first = scn.GetShadowTopOfDeck();
		assertEquals(0, scn.GetShadowDiscardCount());
		scn.ShadowPlayCard(troop1);
		assertInZone(Zone.DISCARD, first);
		assertEquals(1, scn.GetShadowDiscardCount());

		scn.HinderCard(kingdom);

		// He plays another with the condition hindered: nothing is discarded.
		var second = scn.GetShadowTopOfDeck();
		scn.ShadowPlayCard(troop2);
		assertInZone(Zone.SHADOW_CHARACTERS, troop2);
		assertInZone(Zone.DECK, second);
		assertSame(second, scn.GetShadowTopOfDeck());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AHinderedCompanionHasNoKeywordPrintedOrGiven() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var troop = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(aragorn, legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// A keyword an effect gave: Aragorn makes himself defender +1 until the regroup phase.
		assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.HasKeyword(aragorn, Keyword.DEFENDER));

		scn.HinderCard(aragorn);
		assertTrue(scn.IsHindered(aragorn));
		assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));

		// Restored before the regroup phase he has it again: what he gave himself was out of force while he
		// was hindered, not ended.  The rules do not say which; this is what the game does, and Q-0222 asks.
		scn.RestoreCard(aragorn);
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.HasKeyword(aragorn, Keyword.DEFENDER));

		// A printed keyword.  HasKeyword reads a printed keyword off a hindered card as the rig reads his
		// printed numbers, so this one is read by what it does: Legolas, an archer, is the fellowship archery
		// total.
		assertTrue(scn.HasKeyword(legolas, Keyword.ARCHER));
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.HinderCard(legolas);
		assertTrue(scn.IsHindered(legolas));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.RestoreCard(legolas);
		assertEquals(1, scn.GetFreepsArcheryTotal());
	}
}
