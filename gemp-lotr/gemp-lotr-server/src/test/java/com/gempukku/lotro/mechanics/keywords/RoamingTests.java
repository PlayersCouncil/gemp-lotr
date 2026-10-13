package com.gempukku.lotro.mechanics.keywords;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Roaming.
 *
 * Comprehensive Rules 4.2, "roaming": "Each minion is normally played to a certain range of sites beginning with
 * the minion's site number.  If the minion is played to (or is currently at) a site that has a lower site number,
 * that minion is roaming.  The player must pay a roaming penalty by removing an additional two twilight tokens
 * when playing that minion."  Its example: "A minion with a site number of 4 must remove 2 more twilight tokens to
 * play at site 3 ...  If that same minion plays to site 4 ..., there is no roaming penalty.  If he survives the
 * fellowship's first move to 3, he would no longer be roaming when the fellowship moves to site 4."
 *
 * With CR 4.2 "twilight cost" ("A Shadow card cannot be played if its twilight cost cannot be met by the tokens
 * available in the twilight pool") the penalty is part of what the pool must hold before the minion is offered.
 * The penalty follows whether the minion IS roaming: a card that says a minion is not roaming takes the penalty
 * away with it, and a card that changes a site number changes which sites the minion roams at.
 *
 * A card that makes the penalty smaller, or lets a minion be played without it, is that card's own to show
 * (Orc Assassin 1_262, Sauron's Hatred 7_310).  Toil beside the penalty is in ToilTests.
 */
public class RoamingTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Orc Chieftain: [sauron] Orc, cost 2, site 6.  Its text answers a weapon played on it.
					put("chieftain1", "1_266");
					put("chieftain2", "1_266");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6, site 4, no text
					// Ulaire Lemenya, Lieutenant of Morgul: Nazgul, cost 4, site 3.  Its text weakens a
					// character who bears a [wraith] condition, and there is none.
					put("lemenya", "1_232");
					// Bill Ferny, Swarthy Sneering Fellow: [wraith] Man, site 2.  "Nazgul are not roaming."
					// He is discarded underground: site 2 of this path is plains.
					put("bill", "2_75");
					// Grishnakh, Orc Captain: [sauron] Orc, site 6.  "The site number of each [sauron] Orc is
					// -3."  His Shadow ability is used only on purpose.
					put("grishnakh", "5_100");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ARoamingMinionIsNotOfferedUntilThePoolPaysItsCostAndTwoMoreAndIsThenPlayedForBoth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chieftain = scn.GetShadowCard("chieftain1");
		scn.MoveCardsToHand(chieftain);

		scn.StartGame();
		scn.FreepsPass();
		// Site 2 is below the Chieftain's site number of 6.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(6, scn.GetMinionSiteNumber(chieftain));

		// 2 to play + 2 roaming: a pool of 3 pays the cost and not the penalty with it.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(chieftain));
		assertInHand(chieftain);
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(chieftain));

		scn.ShadowPlayCard(chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		// 4 - 2 to play - 2 roaming
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMinionPlayedAtOrPastItsOwnSiteNumberPaysNoPenaltyAndIsNotRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var lemenya = scn.GetShadowCard("lemenya");
		var chieftain = scn.GetShadowCard("chieftain1");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(troop, lemenya, chieftain);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());

		// The Troop's site number is 4, the site's own: it costs its 6 and no more.
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(troop));

		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(troop));
		scn.ShadowPlayCard(troop);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.HasKeyword(troop, Keyword.ROAMING));

		// Lemenya's site number is 3, a site already passed: she costs her 4 and no more.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(lemenya));
		scn.ShadowPlayCard(lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.HasKeyword(lemenya, Keyword.ROAMING));

		// The Chieftain's is 6, which is still ahead: 2 to play + 2 roaming.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(chieftain));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(chieftain));
		scn.ShadowPlayCard(chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARoamingMinionThatSurvivesIsNoLongerRoamingOnceTheFellowshipReachesItsSiteNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetShadowHand(troop);
		scn.FreepsPass();
		// The rules' own example: a minion of site number 4 played at site 3.
		assertEquals(3, scn.GetCurrentSiteNumber());

		scn.SetTwilight(8);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(troop);
		// 8 - 6 to play - 2 roaming
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));

		// Nobody is assigned to the Troop, and the fellowship moves again.
		scn.SkipToMovementDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		scn.FreepsChooseToMove();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertFalse(scn.HasKeyword(troop, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMinionThatACardSaysIsNotRoamingPaysNoPenaltyAndHasNoRoamingKeyword() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bill = scn.GetShadowCard("bill");
		var lemenya = scn.GetShadowCard("lemenya");
		var chieftain = scn.GetShadowCard("chieftain1");
		scn.MoveCardsToHand(lemenya, chieftain);

		scn.StartGame();
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());

		// Lemenya's site number is 3: without Bill Ferny she is roaming at site 2, 4 to play + 2.
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(lemenya));

		// "Nazgul are not roaming": with him in play she costs her 4, from a smaller pool than the one that
		// could not pay for her.
		scn.MoveMinionsToTable(bill);
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(lemenya));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(lemenya));
		scn.ShadowPlayCard(lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertEquals(3, scn.GetMinionSiteNumber(lemenya));
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.HasKeyword(lemenya, Keyword.ROAMING));

		// The Chieftain is no Nazgul: 2 to play + 2 roaming, and he is roaming in play.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(chieftain));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(chieftain));
		scn.ShadowPlayCard(chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RoamingIsJudgedByTheSiteNumberAsCardsHaveChangedIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grishnakh = scn.GetShadowCard("grishnakh");
		var first = scn.GetShadowCard("chieftain1");
		var second = scn.GetShadowCard("chieftain2");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(first, second, troop);

		scn.StartGame();
		scn.MoveMinionsToTable(grishnakh);
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());

		// Grishnakh makes each [sauron] Orc's site number 3 less: the Chieftains' 6 is 3, and so is his own.
		// The Troop is [moria], and its 4 stands.
		assertEquals(3, scn.GetMinionSiteNumber(first));
		assertEquals(3, scn.GetMinionSiteNumber(grishnakh));
		assertEquals(4, scn.GetMinionSiteNumber(troop));

		// Site 2 is still below 3: 2 to play + 2 roaming.
		assertTrue(scn.HasKeyword(grishnakh, Keyword.ROAMING));
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(first));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(first));
		scn.ShadowPlayCard(first);
		assertInZone(Zone.SHADOW_CHARACTERS, first);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.HasKeyword(first, Keyword.ROAMING));

		// Nobody is assigned, and the fellowship moves on to site 3: the changed number, and three below the
		// printed one.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SHADOW_CHARACTERS, grishnakh, first);
		assertFalse(scn.HasKeyword(grishnakh, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(first, Keyword.ROAMING));

		// The Troop is still one site short of its own number: 6 to play + 2 roaming.
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(troop));

		scn.SetTwilight(8);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(troop));
		scn.ShadowPlayCard(troop);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));

		// The second Chieftain is played for its 2 and no more, and is not roaming.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(second));
		scn.ShadowPlayCard(second);
		assertInZone(Zone.SHADOW_CHARACTERS, second);
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.HasKeyword(second, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
