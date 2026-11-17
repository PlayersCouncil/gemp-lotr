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

public class Card_32_067_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer; the Shadow phase and
	// everything after it on turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	// The Witch-King's site is 5, so he roams there.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("witchking", "32_67");

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
					// A minion in hand whose cost is read beside the Witch-King's, and cards in hand to discard
					put("sentinel", "33_49");   // Orc Sentinel: [sauron] Orc, cost 1; its shadow ability wants another [sauron] Orc in play
					put("worms", "32_33");      // Were-worms: [moria] condition
					// A [wraith] card that is no minion
					put("summoning", "32_58");  // He Is Summoning His Servants: unique [wraith] condition, cost 2; [wraith] minions +2 (no Wise characters here), a Shadow ability, not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TheWitchKingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: The Witch-King, Revived
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 8
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 14
		 * Vitality: 4
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>.
		 * The Witch-King is twilight cost -1 for each Wise character you spot.
		 * <b>Skirmish:</b> Discard 3 cards from hand to make a [wraith] minion <b>damage +1</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("witchking");

		assertEquals("The Witch-King", card.getBlueprint().getTitle());
		assertEquals("Revived", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TheWitchKingCostsOneLessForEachWiseCharacterSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var sentinel = scn.GetShadowCard("sentinel");
		var gandalf = scn.GetFreepsCard("gandalf");
		var elrond = scn.GetFreepsCard("elrond");
		var galadriel = scn.GetFreepsCard("galadriel");
		var thorin = scn.GetFreepsCard("thorin");
		var lindir = scn.GetFreepsCard("lindir");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToHand(gandalf, elrond, galadriel, witchking, sentinel);

		scn.StartGame();
		scn.MoveAlliesToTable(lindir);
		assertInZone(Zone.SUPPORT, lindir);
		assertInHand(gandalf, elrond, galadriel, witchking, sentinel);
		scn.FreepsRefreshDecision();

		// Neither Thorin nor Lindir is Wise.
		assertEquals(8, scn.GetTwilightCost(witchking));
		assertEquals(1, scn.GetTwilightCost(sentinel));

		scn.FreepsPlayCard(gandalf);
		assertEquals(7, scn.GetTwilightCost(witchking));
		scn.FreepsPlayCard(elrond);
		assertEquals(6, scn.GetTwilightCost(witchking));
		// A third Wise character: the text has no limit.
		scn.FreepsPlayCard(galadriel);
		assertEquals(5, scn.GetTwilightCost(witchking));
		assertEquals(1, scn.GetTwilightCost(sentinel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(witchking);
		assertInZone(Zone.SHADOW_CHARACTERS, witchking);
		// 5 + 2 roaming
		assertEquals(pool - 7, scn.GetTwilight());
		assertEquals(1, scn.GetTwilightCost(sentinel));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsThreeCardsFromHandToMakeAWraithMinionDamagePlusOneForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		var mongrel = scn.GetShadowCard("mongrel");
		var runner = scn.GetShadowCard("runner");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(thorin, balin);
		// The Shadow player's hand: minions, a condition and Free Peoples cards, of five cultures.
		var sentinel = scn.GetShadowCard("sentinel");
		var worms = scn.GetShadowCard("worms");
		var shadowBalin = scn.GetShadowCard("balin");
		var shadowLindir = scn.GetShadowCard("lindir");
		var shadowGandalf = scn.GetShadowCard("gandalf");
		var shadowElrond = scn.GetShadowCard("elrond");
		var shadowGaladriel = scn.GetShadowCard("galadriel");
		var summoning = scn.GetShadowCard("summoning");

		scn.StartGame();
		scn.MoveCardsToHand(sentinel, worms, shadowBalin, shadowLindir, shadowGandalf, shadowElrond, shadowGaladriel);
		scn.MoveCardsToSupportArea(summoning);
		assertInZone(Zone.SUPPORT, summoning);
		assertInHand(sentinel, worms, shadowBalin, shadowLindir, shadowGandalf, shadowElrond, shadowGaladriel);
		assertEquals(7, scn.GetShadowHandCount());
		scn.MoveMinionsToTable(witchking, nelya, hound, mongrel, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thorin, nelya}, new PhysicalCardImpl[]{balin, mongrel});
		// The Witch-King, the Hound and the Runner stay unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thorin);
		scn.FreepsPass();

		assertEquals(7, scn.GetShadowHandCount());
		assertEquals(0, scn.GetKeywordCount(hound, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(nelya, Keyword.DAMAGE));
		assertTrue(scn.ShadowActionAvailable(witchking));
		scn.ShadowUseCardAction(witchking);
		// Any 3 cards of the hand.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sentinel, worms, shadowBalin, shadowLindir, shadowGandalf, shadowElrond, shadowGaladriel));
		assertEquals(3, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(sentinel, worms, shadowBalin);
		assertInDiscard(sentinel, worms, shadowBalin);
		assertInHand(shadowLindir, shadowGandalf, shadowElrond, shadowGaladriel);
		assertEquals(4, scn.GetShadowHandCount());
		// A [wraith] minion of either race, in this skirmish or in none, the Witch-King himself among them; not the [moria] Orc,
		// nor a [wraith] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(witchking, nelya, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, summoning));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hound);
		assertEquals(1, scn.GetKeywordCount(hound, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(nelya, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(witchking, Keyword.DAMAGE));

		// A second use on the same minion adds to it.
		scn.FreepsPass();
		scn.ShadowUseCardAction(witchking);
		scn.ShadowChooseCards(shadowGandalf, shadowElrond, shadowGaladriel);
		assertInDiscard(shadowGandalf, shadowElrond, shadowGaladriel);
		scn.ShadowChooseCard(hound);
		assertEquals(2, scn.GetKeywordCount(hound, Keyword.DAMAGE));
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPass();
		scn.ShadowPass();
		// The next skirmish is a phase of its own: the damage bonuses are gone.
		scn.FreepsResolveSkirmish(balin);
		assertEquals(0, scn.GetKeywordCount(hound, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithFewerThanThreeCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var nelya = scn.GetShadowCard("nelya");
		var sentinel = scn.GetShadowCard("sentinel");
		var worms = scn.GetShadowCard("worms");
		var runner = scn.GetShadowCard("runner");
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.MoveCardsToHand(sentinel, worms);
		assertEquals(2, scn.GetShadowHandCount());
		scn.MoveMinionsToTable(witchking, nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, nelya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thorin);
		scn.FreepsPass();

		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(witchking));

		scn.MoveCardsToHand(runner);
		assertEquals(3, scn.GetShadowHandCount());
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(witchking));
		assertEquals(0, scn.GetKeywordCount(nelya, Keyword.DAMAGE));
	}
}
