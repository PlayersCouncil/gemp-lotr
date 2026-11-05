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

public class Card_33_048_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  Turn 1's Shadow phase
	// is at The Troll Hoard 30_50 (site 2); Goblin Town 30_52 is site 4.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ambusher", "33_48");
					put("ambusher2", "33_48");

					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, site 4; archer only at site 9, a Regroup ability not used
					put("pursuer", "32_42");    // Orkish Pursuer: [sauron] Orc, site 4; texts on a win and in regroup only
					// Azog, Servant of Sauron: unique [sauron] Orc, twilight 4, site 5; texts on a win and in the maneuver phase only
					put("azog", "32_35");
					put("sauron", "32_45");     // Sauron, The Necromancer: [sauron] Maia, site 5; an Assignment ability not used
					put("runner", "31_30");     // Goblin Runner: [moria] Orc, site 4; its play trigger never fires (cheated in)

					put("bard", "30_22");       // Bard, The Bowman: [esgaroth] Man ally; a Regroup ability not used
					put("radagast", "31_17");   // Radagast, The Brown: [gandalf] Wizard ally; a Fellowship ability not used
					// Rivendell Scout: [elven] Elf ally; answers a companion healing, and none heals
					put("scout", "33_16");
					put("ironhills", "32_5");   // Iron Hills Army: [dwarven] Dwarf ally; a Maneuver ability not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion; a Fellowship ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void OrcAmbusherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Orc Ambusher
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
		 * 	Shadow: Exert this minion and spot another [sauron] Orc to draw a card for each ally you spot (limit 3).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ambusher");

		assertEquals("Orc Ambusher", card.getBlueprint().getTitle());
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

		var ambusher = scn.GetShadowCard("ambusher");
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

		scn.MoveMinionsToTable(ambusher);
		// The Ambusher's own and two other [sauron] Orcs', one of them in hand.
		assertEquals(4, scn.GetMinionSiteNumber(ambusher));
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

		var ambusher = scn.GetShadowCard("ambusher");
		var azog = scn.GetShadowCard("azog");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(ambusher, azog);
		assertInHand(ambusher, azog);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		// Its own text does not name it, and is not on it as it is played (CR 4.2 "modifier"): site 5 at site 4.
		scn.ShadowPlayCard(ambusher);
		// 10 - (1 + 2 roaming)
		assertEquals(7, scn.GetTwilight());
		// In play, its own site number is 4.
		assertFalse(scn.HasKeyword(ambusher, Keyword.ROAMING));

		assertEquals(4, scn.GetMinionSiteNumber(azog));
		scn.ShadowPlayCard(azog);
		// 7 - 4: Azog (site 5) is at site 4 and not roaming.
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.HasKeyword(azog, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityExertsThisMinionToDrawACardForEachAllySpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ambusher = scn.GetShadowCard("ambusher");
		var ambusher2 = scn.GetShadowCard("ambusher2");
		var veteran = scn.GetShadowCard("veteran");
		var bard = scn.GetFreepsCard("bard");
		var radagast = scn.GetFreepsCard("radagast");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(bard, radagast);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(ambusher, ambusher2, veteran);
		scn.ShadowRefreshDecision();
		int hand = scn.GetShadowHandCount();
		assertEquals(0, scn.GetWoundsOn(ambusher));
		assertEquals(0, scn.GetWoundsOn(ambusher2));

		assertTrue(scn.ShadowActionAvailable(ambusher));
		scn.ShadowUseCardAction(ambusher);
		assertEquals(1, scn.GetWoundsOn(ambusher));
		assertEquals(0, scn.GetWoundsOn(ambusher2));
		// Two allies of two cultures; the companions Bilbo and Balin are not counted.
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(2), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("2");

		assertEquals(hand + 2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DrawsNoMoreThan3CardsInEachShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ambusher = scn.GetShadowCard("ambusher");
		var pursuer = scn.GetShadowCard("pursuer");
		var bard = scn.GetFreepsCard("bard");
		var radagast = scn.GetFreepsCard("radagast");
		var scout = scn.GetFreepsCard("scout");
		var ironhills = scn.GetFreepsCard("ironhills");
		scn.MoveAlliesToTable(bard, radagast, scout, ironhills);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(ambusher, pursuer);
		// Vitality 5, so that the one Ambusher can exert four times: a number that only arranges the board.
		scn.ApplyAdHocModifier(new VitalityModifier(null, ambusher, null, 3));
		scn.ShadowRefreshDecision();
		int hand = scn.GetShadowHandCount();

		// Four allies; one named.
		scn.ShadowUseCardAction(ambusher);
		assertEquals(4, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(4), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("1");
		assertEquals(1, scn.GetWoundsOn(ambusher));
		assertEquals(hand + 1, scn.GetShadowHandCount());

		// Four named; two more reach the limit of 3 for the phase.
		scn.ShadowUseCardAction(ambusher);
		scn.ShadowChoose("4");
		assertEquals(2, scn.GetWoundsOn(ambusher));
		assertEquals(hand + 3, scn.GetShadowHandCount());

		// At the limit it is still offered, and paid for nothing.
		assertTrue(scn.ShadowActionAvailable(ambusher));
		scn.ShadowUseCardAction(ambusher);
		scn.ShadowChoose("4");
		assertEquals(3, scn.GetWoundsOn(ambusher));
		assertEquals(hand + 3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The Ambusher is left unassigned; the regroup move brings a second Shadow phase, and the limit is new.
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		int handAfterMove = scn.GetShadowHandCount();
		assertEquals(3, scn.GetWoundsOn(ambusher));

		scn.ShadowUseCardAction(ambusher);
		scn.ShadowChoose("2");
		assertEquals(4, scn.GetWoundsOn(ambusher));
		assertEquals(handAfterMove + 2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoAllyToSpotTheAbilityIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ambusher = scn.GetShadowCard("ambusher");
		var veteran = scn.GetShadowCard("veteran");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(ambusher, veteran);
		scn.ShadowRefreshDecision();
		int hand = scn.GetShadowHandCount();
		assertEquals(0, scn.GetWoundsOn(ambusher));

		// Companions only: Bilbo and Balin.
		assertTrue(scn.ShadowActionAvailable(ambusher));
		scn.ShadowUseCardAction(ambusher);
		assertEquals(1, scn.GetWoundsOn(ambusher));
		assertEquals(hand, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithoutAnotherSauronOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ambusher = scn.GetShadowCard("ambusher");
		var sauron = scn.GetShadowCard("sauron");
		var runner = scn.GetShadowCard("runner");
		var bard = scn.GetFreepsCard("bard");
		scn.MoveAlliesToTable(bard);

		scn.StartGame();
		scn.FreepsPass();
		// A [sauron] minion who is no Orc and an Orc of another culture; the Ambusher is the one [sauron] Orc.
		scn.MoveMinionsToTable(ambusher, sauron, runner);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ambusher));
		assertEquals(0, scn.GetWoundsOn(ambusher));
	}

	@Test
	public void NotOfferedWhileThisMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ambusher = scn.GetShadowCard("ambusher");
		var ambusher2 = scn.GetShadowCard("ambusher2");
		var veteran = scn.GetShadowCard("veteran");
		var bard = scn.GetFreepsCard("bard");
		scn.MoveAlliesToTable(bard);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(ambusher, ambusher2, veteran);
		scn.AddWoundsToChar(ambusher, 1);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetWoundsOn(ambusher));
		assertEquals(0, scn.GetWoundsOn(ambusher2));

		// The other copy, able to exert, is still offered its own.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ambusher));
		assertTrue(scn.ShadowActionAvailable(ambusher2));
	}
}
