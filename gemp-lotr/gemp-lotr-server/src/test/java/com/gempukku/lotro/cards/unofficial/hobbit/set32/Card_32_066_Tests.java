package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_066_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer; the Shadow phase and
	// everything after it on turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	// Toldëa's site is 5, so she roams there.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("toldea", "32_66");

					// Wise characters, and characters who are not Wise
					put("gandalf", "30_26");    // Gandalf, Friend of Thorin: Wise [gandalf] companion, cost 1; his other texts want [dwarven] followers or Thorin in hand
					put("elrond", "30_21");     // Elrond, Elven Lord: Wise [elven] ally, cost 4; a start-of-turn heal, a regroup ability not used
					put("galadriel", "32_13");  // Galadriel, Elven Lady: Wise [elven] ally, cost 3; a start-of-turn heal of Gandalf, a skirmish ability not used
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion, not Wise; a fellowship ability not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion; a fellowship ability not used
					put("lindir", "33_14");     // Lindir, Elven Minstrel: [elven] Elf ally, not Wise; a heal as the fellowship moves

					// [wraith] minions of two races, and a minion of another culture
					put("nelya", "32_63");      // Úlairë Nelya, Revived: [wraith] Nazgûl, strength 10; damage +1 only while a companion bears a follower
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc, strength 5; +4 and fierce only at 2 wounds on the Ring-bearer
					put("mongrel", "7_194");    // Morgul Mongrel: [wraith] Orc, strength 7; fierce and damage +1 only at 3 wounds on the Ring-bearer
					put("runner", "31_30");     // Goblin Runner: [moria] Orc, strength 5; a text on its own play only
					// A minion in hand whose cost is read beside Toldëa's
					put("sentinel", "33_49");   // Orc Sentinel: [sauron] Orc, cost 1; its shadow ability wants another [sauron] Orc in play
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void UlaireToldeaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Úlairë Toldëa, Revived
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>.
		 * This minion is twilight cost -1 for each Wise character you spot (limit -2).
		 * <b>Skirmish:</b> Remove (3) to make a [wraith] minion strength +2 (limit +4).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("toldea");

		assertEquals("Úlairë Toldëa", card.getBlueprint().getTitle());
		assertEquals("Revived", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ToldeaCostsOneLessForEachWiseCharacterSpottedButNoMoreThanTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toldea = scn.GetShadowCard("toldea");
		var sentinel = scn.GetShadowCard("sentinel");
		var gandalf = scn.GetFreepsCard("gandalf");
		var elrond = scn.GetFreepsCard("elrond");
		var galadriel = scn.GetFreepsCard("galadriel");
		var thorin = scn.GetFreepsCard("thorin");
		var lindir = scn.GetFreepsCard("lindir");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToHand(gandalf, elrond, galadriel, toldea, sentinel);

		scn.StartGame();
		scn.MoveAlliesToTable(lindir);
		assertInZone(Zone.SUPPORT, lindir);
		assertInHand(gandalf, elrond, galadriel, toldea, sentinel);
		scn.FreepsRefreshDecision();

		// Neither Thorin nor Lindir is Wise.
		assertEquals(6, scn.GetTwilightCost(toldea));
		assertEquals(1, scn.GetTwilightCost(sentinel));

		scn.FreepsPlayCard(gandalf);
		assertEquals(5, scn.GetTwilightCost(toldea));
		scn.FreepsPlayCard(elrond);
		assertEquals(4, scn.GetTwilightCost(toldea));
		// A third Wise character: the limit is -2.
		scn.FreepsPlayCard(galadriel);
		assertEquals(4, scn.GetTwilightCost(toldea));
		assertEquals(1, scn.GetTwilightCost(sentinel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(toldea);
		assertInZone(Zone.SHADOW_CHARACTERS, toldea);
		// 4 + 2 roaming
		assertEquals(pool - 6, scn.GetTwilight());
		assertEquals(1, scn.GetTwilightCost(sentinel));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemovesThreeToMakeAWraithMinionStrongerUpToFourInEachSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toldea = scn.GetShadowCard("toldea");
		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		var mongrel = scn.GetShadowCard("mongrel");
		var runner = scn.GetShadowCard("runner");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(thorin, balin);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.MoveMinionsToTable(toldea, nelya, hound, mongrel, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thorin, nelya}, new PhysicalCardImpl[]{balin, mongrel});
		// Toldëa, the Hound and the Runner stay unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thorin);
		scn.FreepsPass();

		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(5, scn.GetStrength(hound));
		assertEquals(12, scn.GetStrength(toldea));
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(toldea));
		scn.ShadowUseCardAction(toldea);
		// A [wraith] minion of either race, in this skirmish or in none, Toldëa herself among them; not the [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(toldea, nelya, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(nelya);
		assertEquals(pool - 3, scn.GetTwilight());
		assertEquals(12, scn.GetStrength(nelya));
		assertEquals(12, scn.GetStrength(toldea));
		assertEquals(5, scn.GetStrength(runner));

		scn.FreepsPass();
		scn.ShadowUseCardAction(toldea);
		scn.ShadowChooseCard(hound);
		assertEquals(7, scn.GetStrength(hound));
		assertEquals(pool - 6, scn.GetTwilight());

		// +4 given in this skirmish, across two minions: the ability is still offered, and its (3) buys nothing,
		// for Toldëa as for those already made stronger.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(toldea));
		scn.ShadowUseCardAction(toldea);
		scn.ShadowChooseCard(toldea);
		assertEquals(pool - 9, scn.GetTwilight());
		assertEquals(12, scn.GetStrength(toldea));
		assertEquals(12, scn.GetStrength(nelya));
		assertEquals(7, scn.GetStrength(hound));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPass();
		scn.ShadowPass();
		// The next skirmish is a phase of its own: the bonuses are gone and the limit is new.
		scn.FreepsResolveSkirmish(balin);
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(5, scn.GetStrength(hound));
		scn.FreepsPass();
		scn.ShadowUseCardAction(toldea);
		scn.ShadowChooseCard(mongrel);
		assertEquals(9, scn.GetStrength(mongrel));
		assertEquals(pool - 12, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithLessThanThreeTwilightToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toldea = scn.GetShadowCard("toldea");
		var nelya = scn.GetShadowCard("nelya");
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.MoveMinionsToTable(toldea, nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, nelya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thorin);
		scn.FreepsPass();

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(toldea));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(toldea));
		assertEquals(10, scn.GetStrength(nelya));
	}
}
