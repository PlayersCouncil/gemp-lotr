package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Toil.
 *
 * Comprehensive Rules 4.2, "toil": "When you play a card that has the toil keyword, you may reduce its twilight
 * cost by the number specified.  You do this by exerting one of your characters of the same culture as that card.
 * You may exert multiple characters of the same culture.  Each different character you exert reduces the twilight
 * cost by the number specified.  You may exert a given character only once when playing a toil card."
 *
 * So the exertions are the player's to make or not when the pool can pay; they are counted when the game judges
 * whether the card can be played at all (CR 4.2 "twilight cost": "A Shadow card cannot be played if its twilight
 * cost cannot be met by the tokens available in the twilight pool"), and then as many as the pool is short by
 * must be made.  What toil takes off is the card's twilight cost and no more: the roaming penalty is "an
 * additional two twilight tokens" (CR 4.2 "roaming") beside the cost.
 *
 * The card is Legion of Harad 11_88, a [men] minion of cost 6 and site number 4 whose whole text is Toil 2.  It is
 * played at site 4, where it is not roaming, in every test but the two that are about roaming.  The characters
 * exerted are copies of Corps of Harad 11_73, a [men] minion of vitality 3 (fierce only at a battleground, and
 * this path has none).
 *
 * The last three tests are the keyword on a card that is played on a bearer, Berserker Torch 12_136: the old test
 * they replace was written in 2021 for a fault of just such a card (played from a pool one exertion short, it was
 * discarded).  That board passes; the pool one further short is F-0350.
 */
public class ToilTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legion", "11_88");     // Legion of Harad: [men] minion, cost 6, site 4; Toil 2 is its whole text
					put("corps1", "11_73");     // Corps of Harad: [men] minion, vitality 3, site 4
					put("corps2", "11_73");
					put("corps3", "11_73");
					put("corps4", "11_73");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text, vitality 3
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who could exert, and is not the Shadow player's

					// Berserker Torch: [uruk-hai] possession, cost 3.  "Toil 1.  Bearer must be an [uruk-hai]
					// Uruk-hai."  Its strength bonus counts wounds in a skirmish, and no test here has one.
					put("torch", "12_136");
					// Uruk Dominator: [uruk-hai] Uruk-hai, vitality 3; damage +1 and a skirmish ability
					put("dominator", "12_152");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WithNobodyToExertAToilCardIsPlayedForItsWholeCostAndIsNotOfferedOneShort() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legion = scn.GetShadowCard("legion");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(legion);
		scn.MoveMinionsToTable(troop);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Nobody can be exerted for a [men] card: the Troop is the Shadow player's but [moria], and Aragorn is
		// no character of his.  So the Legion costs its 6.
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(legion));

		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(legion));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Nothing is asked on the way.
		scn.ShadowPlayCard(legion);
		assertInZone(Zone.SHADOW_CHARACTERS, legion);
		assertFalse(scn.HasKeyword(legion, Keyword.ROAMING));
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void APoolShortByWhatOneExertionCoversMakesThatExertionAndAPoolShortByMoreWithholdsTheCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legion = scn.GetShadowCard("legion");
		var corps = scn.GetShadowCard("corps1");
		var worn = scn.GetShadowCard("corps2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(legion);
		scn.MoveMinionsToTable(corps, worn, troop);
		// The second Corps is exhausted (vitality 3), and so cannot be exerted for anything.
		scn.AddWoundsToChar(worn, 2);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.IsExhausted(worn));

		// One [men] character can exert: 6 - 2 is the least the Legion can cost.  With the exhausted Corps or
		// the [moria] Troop counted it would be 2, and a pool of 3 would do.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(legion));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(legion));
		assertEquals(0, scn.GetWoundsOn(corps));

		// The one exertion is the only way to pay, and the one Corps the only one who can make it: it is made
		// without a question.
		scn.ShadowPlayCard(legion);
		assertInZone(Zone.SHADOW_CHARACTERS, legion);
		assertEquals(1, scn.GetWoundsOn(corps));
		assertEquals(2, scn.GetWoundsOn(worn));
		assertEquals(0, scn.GetWoundsOn(troop));
		// 4 - (6 - 2)
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoWhoCouldExertAndThePoolShortByOneExertionAtLeastOneMustBeChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legion = scn.GetShadowCard("legion");
		var corps1 = scn.GetShadowCard("corps1");
		var corps2 = scn.GetShadowCard("corps2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(legion);
		scn.MoveMinionsToTable(corps1, corps2, troop);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(legion));
		scn.ShadowPlayCard(legion);

		// The pool is 2 short of the cost: one exertion is owed, and the second is the player's to make or not.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(corps1, corps2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));

		scn.ShadowChooseCard(corps2);
		assertInZone(Zone.SHADOW_CHARACTERS, legion);
		assertEquals(0, scn.GetWoundsOn(corps1));
		assertEquals(1, scn.GetWoundsOn(corps2));
		// 4 - (6 - 2)
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoWhoCouldExertAndThePoolShortByOneExertionBothMayBeChosenAndTheCostIsLessByBoth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legion = scn.GetShadowCard("legion");
		var corps1 = scn.GetShadowCard("corps1");
		var corps2 = scn.GetShadowCard("corps2");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(legion);
		scn.MoveMinionsToTable(corps1, corps2);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(legion);
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());

		// The exertion that was not owed counts as much as the one that was.
		scn.ShadowChooseCards(corps1, corps2);
		assertInZone(Zone.SHADOW_CHARACTERS, legion);
		assertEquals(1, scn.GetWoundsOn(corps1));
		assertEquals(1, scn.GetWoundsOn(corps2));
		// 4 - (6 - 2 - 2)
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThePoolPayingTheWholeCostThePlayerIsAskedWhomToExertAndExertingNoneLeavesItWhole() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legion = scn.GetShadowCard("legion");
		var corps1 = scn.GetShadowCard("corps1");
		var corps2 = scn.GetShadowCard("corps2");
		var worn = scn.GetShadowCard("corps3");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(legion);
		scn.MoveMinionsToTable(corps1, corps2, worn, troop);
		scn.AddWoundsToChar(worn, 2);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.IsExhausted(worn));

		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(legion));
		scn.ShadowPlayCard(legion);

		// "You may": from nobody up to every [men] character of the player's who can exert, each of them once.
		// The maximum is the number of such characters (2), not of the exertions they could make between them
		// (4).  The exhausted Corps, the [moria] Troop and the other player's companion are not among them.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(corps1, corps2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(worn, troop, aragorn));

		scn.ShadowDeclineChoosing();
		assertInZone(Zone.SHADOW_CHARACTERS, legion);
		assertEquals(0, scn.GetWoundsOn(corps1));
		assertEquals(0, scn.GetWoundsOn(corps2));
		assertEquals(2, scn.GetWoundsOn(worn));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// 6 - 6
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ExertingOneOfTwoCharactersTakesTheToilNumberOffTheCostOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legion = scn.GetShadowCard("legion");
		var corps1 = scn.GetShadowCard("corps1");
		var corps2 = scn.GetShadowCard("corps2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(legion);
		scn.MoveMinionsToTable(corps1, corps2, troop);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());

		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(legion);
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(corps1);
		assertInZone(Zone.SHADOW_CHARACTERS, legion);
		assertEquals(1, scn.GetWoundsOn(corps1));
		assertEquals(0, scn.GetWoundsOn(corps2));
		assertEquals(0, scn.GetWoundsOn(troop));
		// 6 - (6 - 2)
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachCharacterExertedTakesTheToilNumberOffTheCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legion = scn.GetShadowCard("legion");
		var corps1 = scn.GetShadowCard("corps1");
		var corps2 = scn.GetShadowCard("corps2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(legion);
		scn.MoveMinionsToTable(corps1, corps2, troop);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());

		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(legion);
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(corps1, corps2);
		assertInZone(Zone.SHADOW_CHARACTERS, legion);
		assertEquals(1, scn.GetWoundsOn(corps1));
		assertEquals(1, scn.GetWoundsOn(corps2));
		assertEquals(0, scn.GetWoundsOn(troop));
		// 6 - (6 - 2 - 2)
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ExertingMoreThanTheCostNeedsLeavesTheCostAtNothingAndTheRoamingPenaltyIsStillPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legion = scn.GetShadowCard("legion");
		var corps1 = scn.GetShadowCard("corps1");
		var corps2 = scn.GetShadowCard("corps2");
		var corps3 = scn.GetShadowCard("corps3");
		var corps4 = scn.GetShadowCard("corps4");
		scn.MoveCardsToHand(legion);

		scn.StartGame();
		scn.MoveMinionsToTable(corps1, corps2, corps3, corps4);
		scn.FreepsPass();
		// Site 2 is below the Legion's site number of 4: it is roaming.
		assertEquals(2, scn.GetCurrentSiteNumber());

		scn.SetTwilight(8);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(legion));
		scn.ShadowPlayCard(legion);

		// Three exertions bring the cost to nothing; the fourth may still be made.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(corps1, corps2, corps3, corps4));

		scn.ShadowChooseCards(corps1, corps2, corps3, corps4);
		assertInZone(Zone.SHADOW_CHARACTERS, legion);
		assertTrue(scn.HasKeyword(legion, Keyword.ROAMING));
		assertEquals(1, scn.GetWoundsOn(corps1));
		assertEquals(1, scn.GetWoundsOn(corps2));
		assertEquals(1, scn.GetWoundsOn(corps3));
		assertEquals(1, scn.GetWoundsOn(corps4));
		// The cost is 6 - 8, which is nothing and not 2 back; the 2 for roaming is no part of the cost and is
		// removed all the same: 8 - 0 - 2.
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithToilAbleToPayTheWholeCostARoamingCardStillNeedsTheRoamingPenaltyInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legion = scn.GetShadowCard("legion");
		var corps1 = scn.GetShadowCard("corps1");
		var corps2 = scn.GetShadowCard("corps2");
		var corps3 = scn.GetShadowCard("corps3");
		var corps4 = scn.GetShadowCard("corps4");
		scn.MoveCardsToHand(legion);

		scn.StartGame();
		scn.MoveMinionsToTable(corps1, corps2, corps3, corps4);
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());

		// Four exertions could take 8 off, but only the cost of 6 is theirs to take: the 2 for roaming must be
		// in the pool.
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(legion));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(legion));
		scn.ShadowPlayCard(legion);

		// The whole cost is owed in exertions: three of them (3 x 2 = 6), and the fourth is still the player's
		// to make.
		assertEquals(3, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(corps1, corps2, corps3);
		assertInZone(Zone.SHADOW_CHARACTERS, legion);
		assertTrue(scn.HasKeyword(legion, Keyword.ROAMING));
		assertEquals(1, scn.GetWoundsOn(corps1));
		assertEquals(1, scn.GetWoundsOn(corps2));
		assertEquals(1, scn.GetWoundsOn(corps3));
		assertEquals(0, scn.GetWoundsOn(corps4));
		// 2 - (6 - 6) - 2 for roaming
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void APossessionWithToilIsPaidForByExertingTheMinionWhoWillBearIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var torch = scn.GetShadowCard("torch");
		var dominator = scn.GetShadowCard("dominator");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(torch);

		scn.StartGame();
		scn.MoveMinionsToTable(dominator, troop);
		scn.FreepsPass();

		// Cost 3, toil 1, and the Dominator is the one [uruk-hai] character: 3 - 1 is the least it can cost.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(torch));
		assertEquals(0, scn.GetWoundsOn(dominator));

		// The exertion is owed and the Dominator the only one who can make it, as he is the only one who can
		// bear the Torch: neither is asked.
		scn.ShadowPlayCard(torch);
		assertAttachedTo(torch, dominator);
		assertEquals(1, scn.GetWoundsOn(dominator));
		assertEquals(0, scn.GetWoundsOn(troop));
		// 2 - (3 - 1)
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	@Ignore("F-0350: a toil card that is played on a bearer is offered with the pool short by twice what toil can cover, and playing it then discards it")
	public void APossessionWithToilIsNotOfferedFromAPoolShortByMoreThanItsBearersExertionCovers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var torch = scn.GetShadowCard("torch");
		var dominator = scn.GetShadowCard("dominator");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(torch);

		scn.StartGame();
		scn.MoveMinionsToTable(dominator, troop);
		scn.FreepsPass();

		// One [uruk-hai] character can exert: 3 - 1 is the least the Torch can cost, and a pool of 1 is short
		// of it.
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(torch));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(torch));
		assertInHand(torch);
		assertEquals(0, scn.GetWoundsOn(dominator));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void APossessionWithToilPlayedFromAPoolThatPaysInFullAsksAndDecliningExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var torch = scn.GetShadowCard("torch");
		var dominator = scn.GetShadowCard("dominator");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(torch);

		scn.StartGame();
		scn.MoveMinionsToTable(dominator, troop);
		scn.FreepsPass();

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(torch));
		scn.ShadowPlayCard(torch);

		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(dominator));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));

		scn.ShadowDeclineChoosing();
		assertAttachedTo(torch, dominator);
		assertEquals(0, scn.GetWoundsOn(dominator));
		assertEquals(0, scn.GetWoundsOn(troop));
		// 3 - 3
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
