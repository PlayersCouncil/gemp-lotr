package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.cards.build.LotroCardBlueprintBuilder;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A limit on what a card takes off a twilight cost: "its twilight cost is -1 for each ... (limit -2)".
 *
 * Comprehensive Rules 4.2, "limit": "When a card has a limit, such as '(limit +3),' the limit applies to that card
 * for an entire phase."  "A limit does not span multiple phases", and "a limit does not apply to a different copy
 * of the same card."  Ketura, 2026-10-08 (Q-0134, F-0245): a printed "(limit -N)" reads as "(limit +N)" does, the
 * card's limit for the whole phase over all its uses, and not a cap on each use.
 *
 * A discount is read before the card is played as well (to say what can be played, and whether the ability can be
 * used at all), so the tests also show that reading it spends nothing, and that what is offered counts only what is
 * left of the limit.
 *
 * Every play here is at site 2 of the Fellowship path, so each minion (site 4 or 5) pays 2 for roaming.
 */
public class LimitOnADiscountTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Test Muster.  "Shadow or Maneuver: Play a minion; its twilight cost is -1 for each minion you
					// can spot (limit -2)."
					put("muster1", "0_8014");
					put("muster2", "0_8014");
					// Test Barrow.  "Shadow: Play a minion from your discard pile; its twilight cost is -1 for each
					// minion you can spot (limit -2)."
					put("barrow", "0_8015");

					put("troop1", "1_177");         // Goblin Patrol Troop: cost 6, site 4, no text
					put("troop2", "1_177");
					put("savage1", "1_151");        // Uruk Savage: cost 2, site 5; Damage +1 and nothing else
					put("savage2", "1_151");
					put("savage3", "1_151");
					put("raiders", "1_158");        // Uruk-hai Raiding Party: cost 4, site 5; Damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OverSeveralUsesInOnePhaseACardTakesOffNoMoreThanItsLimitInAll() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var muster = scn.GetShadowCard("muster1");
		var troop = scn.GetShadowCard("troop1");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToSupportArea(muster);
		scn.MoveCardsToHand(savage1, savage2, raiders);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// 20 set + 1 companion + 2 for Ettenmoors
		assertEquals(23, scn.GetTwilight());

		// One minion to spot: -1, and 1 of the card's -2 is left.
		assertTrue(scn.ShadowActionAvailable(muster));
		scn.ShadowUseCardAction(muster);
		scn.ShadowChooseCard(savage1);
		// 2 to play + 2 roaming - 1
		assertEquals(20, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, savage1);

		// Two minions to spot now, which would be -2: only the 1 that is left is taken off.
		assertTrue(scn.ShadowActionAvailable(muster));
		scn.ShadowUseCardAction(muster);
		scn.ShadowChooseCard(savage2);
		// 2 to play + 2 roaming - 1
		assertEquals(17, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, savage2);

		// The limit is reached.  The ability still plays a minion, at its full cost.
		assertTrue(scn.ShadowActionAvailable(muster));
		scn.ShadowUseCardAction(muster);
		// 4 to play + 2 roaming
		assertEquals(11, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, raiders);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhatIsOfferedCountsOnlyWhatIsLeftOfTheLimitAndAnotherCopyHasALimitOfItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var muster1 = scn.GetShadowCard("muster1");
		var muster2 = scn.GetShadowCard("muster2");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var savage3 = scn.GetShadowCard("savage3");
		scn.MoveCardsToSupportArea(muster1, muster2);
		scn.MoveCardsToHand(savage1, savage2, savage3);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SetTwilight(2);
		scn.FreepsPass();

		// 2 set + 1 companion + 2 for Ettenmoors: a Savage (2 to play + 2 roaming) can be played from hand, once.
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(savage1));

		// Asking what is on offer, as often as one likes, spends nothing of either copy's limit.
		assertTrue(scn.ShadowActionAvailable(muster1));
		assertTrue(scn.ShadowActionAvailable(muster2));
		assertTrue(scn.ShadowActionAvailable(muster1));

		scn.ShadowUseCardAction(muster1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage1, savage2, savage3));
		scn.ShadowChooseCard(savage1);
		// 2 to play + 2 roaming - 2
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, savage1);

		// 3 in the pool: a Savage costs 4, and 2 with the discount.  The first copy has nothing left to take off,
		// so it has no minion it could play; the second copy has its own -2.
		assertFalse(scn.ShadowPlayAvailable(savage2));
		assertFalse(scn.ShadowActionAvailable(muster1));
		assertTrue(scn.ShadowActionAvailable(muster2));

		scn.ShadowUseCardAction(muster2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage2, savage3));
		scn.ShadowChooseCard(savage2);
		// 2 to play + 2 roaming - 2
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, savage2);
		assertInHand(savage3);

		assertFalse(scn.ShadowActionAvailable(muster1));
		assertFalse(scn.ShadowActionAvailable(muster2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheLimitIsNewInAnotherPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var muster = scn.GetShadowCard("muster1");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var savage3 = scn.GetShadowCard("savage3");
		scn.MoveCardsToSupportArea(muster);
		scn.MoveCardsToHand(savage1, savage2, savage3);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(23, scn.GetTwilight());

		// The Shadow phase: -2, and then nothing more.
		scn.ShadowUseCardAction(muster);
		scn.ShadowChooseCard(savage1);
		// 2 to play + 2 roaming - 2
		assertEquals(21, scn.GetTwilight());
		scn.ShadowUseCardAction(muster);
		scn.ShadowChooseCard(savage2);
		// 2 to play + 2 roaming
		assertEquals(17, scn.GetTwilight());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The maneuver phase is another phase, with a limit of its own.
		assertEquals(17, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(muster));
		scn.ShadowUseCardAction(muster);
		// 2 to play + 2 roaming - 2
		assertEquals(15, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, savage3);
	}

	@Test
	public void ADiscountOnACardPlayedFromTheDiscardPileIsLimitedInTheSameWay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barrow = scn.GetShadowCard("barrow");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCardsToSupportArea(barrow);
		scn.MoveCardsToDiscard(savage1, savage2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(23, scn.GetTwilight());

		assertTrue(scn.ShadowActionAvailable(barrow));
		scn.ShadowUseCardAction(barrow);
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage1, savage2));
		scn.ShadowChooseCard(savage1);
		// 2 to play + 2 roaming - 2
		assertEquals(21, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, savage1);

		// The card's -2 is spent for the phase.
		assertTrue(scn.ShadowActionAvailable(barrow));
		scn.ShadowUseCardAction(barrow);
		// 2 to play + 2 roaming
		assertEquals(17, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, savage2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// The definition of a Shadow condition with one Shadow-phase ability, whose fields are given.
	private static String condition(String id, String abilityFields) {
		return "\"" + id + "\": { \"title\": \"Test Card\", \"unique\": false, \"side\": \"Shadow\", \"culture\": \"Sauron\","
				+ " \"twilight\": 0, \"type\": \"Condition\", \"keywords\": \"Support Area\","
				+ " \"effects\": { \"type\": \"activated\", \"phase\": \"shadow\", " + abilityFields + " } }";
	}

	/**
	 * A value that counts on a limit is spent by being worked out, and a discount is worked out before the card is
	 * played as well as when it is.  The play reads a cardPhaseLimit that is the whole of its discount without
	 * spending it; a limit written anywhere else in a discount could only be spent by the asking, so the card that
	 * holds one is not loaded (the library logs why, as for any definition it cannot read).
	 */
	@Test
	public void ADiscountWithALimitWrittenWhereItCannotWorkIsRefusedWhenTheCardIsLoaded() throws Exception {
		// Each is the ability of a Shadow condition; the texts are JSON, which the library reads as it reads HJSON.
		final String limit = "{ \"type\": \"cardPhaseLimit\", \"limit\": 2, \"amount\": -1 }";
		final String sum = "{ \"type\": \"Sum\", \"source\": [ " + limit + ", -1 ] }";
		final String play = "\"effect\": { \"type\": \"PlayCardFromHand\", \"select\": \"choose(minion)\", \"discount\": ";
		final String ask = "\"requires\": { \"type\": \"Playable\", \"filter\": \"minion\", \"fromZone\": \"HAND\", \"discount\": ";
		final String thenAddTwilight = " }, \"effect\": { \"type\": \"AddTwilight\", \"amount\": 1 }";
		final String definitions = "{"
				// The whole of the discount: loaded.
				+ condition("0_9001", play + limit + " }") + ","
				// One term of a sum.
				+ condition("0_9002", play + sum + " }") + ","
				// A limit counted for each card affected: a play's discount has no such card.
				+ condition("0_9003", play + "{ \"type\": \"cardAffectedLimitPerPhase\", \"limit\": 2, \"source\": -1 } }") + ","
				// A limit on the amount of a limit.
				+ condition("0_9004", play + "{ \"type\": \"cardPhaseLimit\", \"limit\": 2, \"amount\": " + limit + " } }") + ","
				// The discount of a requirement, which is only ever asked, with the limit as one term of a sum.
				+ condition("0_9005", ask + sum + thenAddTwilight) + ","
				// The same requirement with the limit as the whole of its discount: loaded.
				+ condition("0_9006", ask + limit + thenAddTwilight)
				+ "}";

		var loaded = LotroCardBlueprintLibrary.loadCardsFromFile(new LotroCardBlueprintBuilder(),
				new ByteArrayInputStream(definitions.getBytes(StandardCharsets.UTF_8)));

		assertTrue(loaded.containsKey("0_9001"));
		assertFalse(loaded.containsKey("0_9002"));
		assertFalse(loaded.containsKey("0_9003"));
		assertFalse(loaded.containsKey("0_9004"));
		assertFalse(loaded.containsKey("0_9005"));
		assertTrue(loaded.containsKey("0_9006"));
	}
}
