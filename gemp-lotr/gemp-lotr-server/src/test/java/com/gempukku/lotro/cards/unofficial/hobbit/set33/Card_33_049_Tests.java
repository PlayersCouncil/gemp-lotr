package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_049_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  Turn 1's Shadow phase
	// is at The Troll Hoard 30_50 (site 2); Goblin Town 30_52 is site 4.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sentinel", "33_49");
					put("sentinel2", "33_49");

					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, site 4; archer only at site 9, a Regroup ability not used
					put("pursuer", "32_42");    // Orkish Pursuer: [sauron] Orc, site 4; texts on a win and in regroup only
					// Azog, Servant of Sauron: unique [sauron] Orc, twilight 4, site 5; texts on a win and in the maneuver phase only
					put("azog", "32_35");
					put("sauron", "32_45");     // Sauron, The Necromancer: [sauron] Maia, site 5; an Assignment ability not used
					put("runner", "31_30");     // Goblin Runner: [moria] Orc, site 4; its play trigger never fires (cheated in)
					// More Goblin Runners, left in the draw deck so that it does not run dry across two Shadow phases' draws
					put("runner2", "31_30");
					put("runner3", "31_30");
					put("runner4", "31_30");
					put("runner5", "31_30");
					put("runner6", "31_30");
					put("runner7", "31_30");

					// Úlairë Lemenya, Revived: unique [wraith] Nazgûl; a trigger on a condition the Free Peoples player discards, and none is
					put("lemenya", "32_62");
					// Úlairë Otsëa, Revived: unique [wraith] Nazgûl; +1 to [wraith] minions while a companion bears a follower, and none does
					put("otsea", "32_65");

					put("bard", "30_22");       // Bard, The Bowman: unique [esgaroth] Man ally; a Regroup ability not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: unique [dwarven] companion; a Fellowship ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void OrcSentinelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Orc Sentinel
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: The site number of each [sauron] Orc is -1.
		 * 	Shadow: Exert this minion and spot another [sauron] Orc to draw a card for each unique minion you spot (limit 3).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sentinel");

		assertEquals("Orc Sentinel", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TheSiteNumberOfEachSauronOrcIsMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentinel = scn.GetShadowCard("sentinel");
		var veteran = scn.GetShadowCard("veteran");
		var azog = scn.GetShadowCard("azog");
		var sauron = scn.GetShadowCard("sauron");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(veteran, sauron, runner);
		scn.MoveCardsToHand(azog);
		assertInHand(azog);
		assertEquals(4, scn.GetMinionSiteNumber(veteran));
		assertEquals(5, scn.GetMinionSiteNumber(azog));
		assertEquals(5, scn.GetMinionSiteNumber(sauron));
		assertEquals(4, scn.GetMinionSiteNumber(runner));

		scn.MoveMinionsToTable(sentinel);
		// The Sentinel's own and two other [sauron] Orcs', one of them in hand.
		assertEquals(4, scn.GetMinionSiteNumber(sentinel));
		assertEquals(3, scn.GetMinionSiteNumber(veteran));
		assertEquals(4, scn.GetMinionSiteNumber(azog));
		// A [sauron] minion who is no Orc, and an Orc of another culture.
		assertEquals(5, scn.GetMinionSiteNumber(sauron));
		assertEquals(4, scn.GetMinionSiteNumber(runner));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtSite4ItIsRoamingButASauronOrcPlayedAfterItIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentinel = scn.GetShadowCard("sentinel");
		var azog = scn.GetShadowCard("azog");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(sentinel, azog);
		assertInHand(sentinel, azog);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		// Its own text does not name it, and is not on it as it is played (CR 4.2 "modifier"): site 5 at site 4.
		scn.ShadowPlayCard(sentinel);
		// 10 - (1 + 2 roaming)
		assertEquals(7, scn.GetTwilight());
		// In play, its own site number is 4.
		assertFalse(scn.HasKeyword(sentinel, Keyword.ROAMING));

		assertEquals(4, scn.GetMinionSiteNumber(azog));
		scn.ShadowPlayCard(azog);
		// 7 - 4: Azog (site 5) is at site 4 and not roaming.
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.HasKeyword(azog, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityExertsThisMinionToDrawACardForEachUniqueMinionSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentinel = scn.GetShadowCard("sentinel");
		var sentinel2 = scn.GetShadowCard("sentinel2");
		var veteran = scn.GetShadowCard("veteran");
		var azog = scn.GetShadowCard("azog");
		var lemenya = scn.GetShadowCard("lemenya");
		var bard = scn.GetFreepsCard("bard");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(bard);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(sentinel, sentinel2, veteran, azog, lemenya);
		scn.ShadowRefreshDecision();
		int hand = scn.GetShadowHandCount();
		assertEquals(0, scn.GetWoundsOn(sentinel));
		assertEquals(0, scn.GetWoundsOn(sentinel2));

		assertTrue(scn.ShadowActionAvailable(sentinel));
		scn.ShadowUseCardAction(sentinel);
		assertEquals(1, scn.GetWoundsOn(sentinel));
		assertEquals(0, scn.GetWoundsOn(sentinel2));
		// Two unique minions of two cultures and races, Azog and Lemenya.  Neither Sentinel nor the Veteran (minions
		// that are not unique) is counted, nor Bilbo, Balin and Bard (unique Free Peoples characters).
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(2), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("2");

		assertEquals(hand + 2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DrawsNoMoreThan3CardsInEachShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentinel = scn.GetShadowCard("sentinel");
		var pursuer = scn.GetShadowCard("pursuer");
		var azog = scn.GetShadowCard("azog");
		var sauron = scn.GetShadowCard("sauron");
		var lemenya = scn.GetShadowCard("lemenya");
		var otsea = scn.GetShadowCard("otsea");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(sentinel, pursuer, azog, sauron, lemenya, otsea);
		// Vitality 5, so that the one Sentinel can exert four times: a number that only arranges the board.
		scn.ApplyAdHocModifier(new VitalityModifier(null, sentinel, null, 3));
		scn.ShadowRefreshDecision();
		int hand = scn.GetShadowHandCount();

		// Four unique minions; one named.
		scn.ShadowUseCardAction(sentinel);
		assertEquals(4, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(4), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("1");
		assertEquals(1, scn.GetWoundsOn(sentinel));
		assertEquals(hand + 1, scn.GetShadowHandCount());

		// Four named; two more reach the limit of 3 for the phase.
		scn.ShadowUseCardAction(sentinel);
		scn.ShadowChoose("4");
		assertEquals(2, scn.GetWoundsOn(sentinel));
		assertEquals(hand + 3, scn.GetShadowHandCount());

		// At the limit it is still offered, and paid for nothing.
		assertTrue(scn.ShadowActionAvailable(sentinel));
		scn.ShadowUseCardAction(sentinel);
		scn.ShadowChoose("4");
		assertEquals(3, scn.GetWoundsOn(sentinel));
		assertEquals(hand + 3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The minions are left unassigned; the regroup move brings a second Shadow phase, and the limit is new.
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		int handAfterMove = scn.GetShadowHandCount();
		assertEquals(3, scn.GetWoundsOn(sentinel));

		scn.ShadowUseCardAction(sentinel);
		scn.ShadowChoose("2");
		assertEquals(4, scn.GetWoundsOn(sentinel));
		assertEquals(handAfterMove + 2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoUniqueMinionToSpotTheAbilityIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentinel = scn.GetShadowCard("sentinel");
		var veteran = scn.GetShadowCard("veteran");
		var bard = scn.GetFreepsCard("bard");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(bard);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(sentinel, veteran);
		scn.ShadowRefreshDecision();
		int hand = scn.GetShadowHandCount();
		assertEquals(0, scn.GetWoundsOn(sentinel));

		// Unique characters, but none of them a minion: Bilbo, Balin and Bard.
		assertTrue(scn.ShadowActionAvailable(sentinel));
		scn.ShadowUseCardAction(sentinel);
		assertEquals(1, scn.GetWoundsOn(sentinel));
		assertEquals(hand, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithoutAnotherSauronOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentinel = scn.GetShadowCard("sentinel");
		var sauron = scn.GetShadowCard("sauron");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.FreepsPass();
		// A [sauron] minion who is no Orc and an Orc of another culture; the Sentinel is the one [sauron] Orc.
		// Sauron is unique, so the ability would draw a card.
		scn.MoveMinionsToTable(sentinel, sauron, runner);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(sentinel));
		assertEquals(0, scn.GetWoundsOn(sentinel));
	}

	@Test
	public void NotOfferedWhileThisMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentinel = scn.GetShadowCard("sentinel");
		var sentinel2 = scn.GetShadowCard("sentinel2");
		var veteran = scn.GetShadowCard("veteran");
		var azog = scn.GetShadowCard("azog");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(sentinel, sentinel2, veteran, azog);
		scn.AddWoundsToChar(sentinel, 1);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetWoundsOn(sentinel));
		assertEquals(0, scn.GetWoundsOn(sentinel2));

		// The other copy, able to exert, is still offered its own.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(sentinel));
		assertTrue(scn.ShadowActionAvailable(sentinel2));
	}
}
