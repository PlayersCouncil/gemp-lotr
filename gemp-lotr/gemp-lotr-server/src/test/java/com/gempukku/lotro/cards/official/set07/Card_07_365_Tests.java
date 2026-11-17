package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_365_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eomer", "7_365");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, cost 2, valiant and nothing else
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit companion, a Man only while 3 [rohan] companions or Eowyn can be spotted
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: a [rohan] Man ALLY, cost 1; an archery ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability used only on purpose

					// Lorien Guardian (the PC erratum): an [elven] Elf companion printed valiant; a regroup ability used only on purpose
					put("guardian", "54_76");
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ally; a fellowship ability used only on purpose
					// Northern Signal-fire: "Maneuver: Hinder this beacon and exert an unbound Man.  Until the regroup phase, each of
					// your other Men gain each of the unloaded keywords (except race) on that Man": with the Guard exerted, Guma is a
					// valiant ally.
					put("signal", "103_86");

					// For the hand: a companion (Aragorn above), a minion, an event and a possession.
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; its play trigger is not reached by a cheat
					put("runner2", "1_178");
					put("deftness", "1_293");   // Halfling Deftness: a [shire] Skirmish event
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession

					// Mordor Fighter: while a threat and another [sauron] Orc can be spotted, the Shadow has initiative
					put("fighter1", "7_286");
					put("fighter2", "7_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EomerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Éomer, Valiant Warchief
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Theoden
		 * Game Text: <b>Valiant</b>. While you can spot a [rohan] Man, Éomer's twilight cost is -1.
		 * 	<b>Skirmish:</b> If you have initiative, discard 3 cards from hand to make Éomer strength +1 for each
		 * 	valiant companion you spot.
		 */

		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");

		assertEquals("Éomer", eomer.getBlueprint().getTitle());
		assertEquals("Valiant Warchief", eomer.getBlueprint().getSubtitle());
		assertTrue(eomer.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, eomer.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, eomer.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, eomer.getBlueprint().getCardType());
		assertEquals(Race.MAN, eomer.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(eomer, Keyword.VALIANT));
		assertEquals(3, eomer.getBlueprint().getTwilightCost());
		assertEquals(7, eomer.getBlueprint().getStrength());
		assertEquals(3, eomer.getBlueprint().getVitality());
		assertEquals(6, eomer.getBlueprint().getResistance());
		assertEquals(Signet.THEODEN, eomer.getBlueprint().getSignet());
	}

	@Test
	public void WithARohanManCompanionToSpotEomerIsPlayedForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(eomer, aragorn);
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();

		assertEquals(2, scn.GetTwilightCost(eomer));    // 3 - 1
		// Another companion in the hand, beside the [rohan] Man he spots, costs what it is printed at.
		assertEquals(4, scn.GetTwilightCost(aragorn));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(eomer);

		// The text names Éomer himself, so it applies to him as he is played (CR 4.2 "modifier").
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanHobbitAndAGondorManDoNotLowerHisCostAndARohanManAllyPlayedThenDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var aldor = scn.GetFreepsCard("aldor");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(eomer, aldor);
		scn.MoveCompanionsToTable(merry, aragorn);

		scn.StartGame();

		// A [rohan] companion who is no Man, and a Man who is not [rohan].
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertEquals(3, scn.GetTwilightCost(eomer));

		scn.FreepsPlayCard(aldor);

		assertInZone(Zone.SUPPORT, aldor);
		assertEquals(2, scn.GetTwilightCost(eomer));    // 3 - 1
		int pool = scn.GetTwilight();

		scn.FreepsPlayCard(eomer);

		assertEquals(pool + 2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		// Two [rohan] companions: Merry is still no Man.
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsThreeCardsToMakeHimStrongerForEachValiantCompanionSpottedUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var guardian = scn.GetFreepsCard("guardian");
		var guma = scn.GetFreepsCard("guma");
		var signal = scn.GetFreepsCard("signal");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ownRunner = scn.GetFreepsCard("runner1");
		var deftness = scn.GetFreepsCard("deftness");
		var sword = scn.GetFreepsCard("sword");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(eomer, guard, guardian);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToSupportArea(signal);
		scn.MoveCardsToHand(aragorn, ownRunner, deftness, sword);

		scn.StartGame();
		assertInHand(aragorn, ownRunner, deftness, sword);
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.MANEUVER);

		// The Guard's valiant is given to Guma, a Man ally.
		assertFalse(scn.HasKeyword(guma, Keyword.VALIANT));
		scn.FreepsUseCardAction(signal);
		scn.FreepsChooseCard(guard);
		assertTrue(scn.IsHindered(signal));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.HasKeyword(guma, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(frodo, Keyword.VALIANT));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, runner1}, new PhysicalCardImpl[]{guardian, runner2});
		// Éomer is in no skirmish at all.
		scn.FreepsResolveSkirmish(guard);

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(7, scn.GetStrength(eomer));
		assertTrue(scn.FreepsActionAvailable(eomer));

		scn.FreepsUseCardAction(eomer);

		// The cost: any 3 cards from hand.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, ownRunner, deftness, sword));
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, ownRunner, deftness);

		assertInDiscard(aragorn, ownRunner, deftness);
		assertInHand(sword);
		assertEquals(1, scn.GetFreepsHandCount());

		// The valiant companions are Éomer, the Guard and the Guardian; not Guma (an ally) nor Frodo (not valiant).
		assertTrue(scn.HasKeyword(guma, Keyword.VALIANT));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(3), scn.FreepsGetChoiceDefault());
		assertEquals(7, scn.GetStrength(eomer));
		scn.FreepsChoose("2");

		// 7 + 2 spotted
		assertEquals(9, scn.GetStrength(eomer));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();

		// The next skirmish is a phase of its own: the bonus is gone.
		scn.FreepsResolveSkirmish(guardian);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(eomer));
	}

	@Test
	public void InHisOwnSkirmishWithNoOtherValiantCompanionHeSpotsHimselfAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ownRunner = scn.GetFreepsCard("runner1");
		var deftness = scn.GetFreepsCard("deftness");
		var sword = scn.GetFreepsCard("sword");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(eomer);
		scn.MoveCardsToHand(aragorn, ownRunner, deftness, sword);

		scn.StartGame();
		assertInHand(aragorn, ownRunner, deftness, sword);
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eomer, runner1);
		scn.FreepsResolveSkirmish(eomer);

		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(7, scn.GetStrength(eomer));
		assertTrue(scn.FreepsActionAvailable(eomer));

		scn.FreepsUseCardAction(eomer);
		scn.FreepsChooseCards(aragorn, ownRunner, sword);
		assertInDiscard(aragorn, ownRunner, sword);
		assertEquals(1, scn.GetFreepsHandCount());

		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(1), scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("1");

		// 7 + 1 spotted
		assertEquals(8, scn.GetStrength(eomer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsOfferedOnlyWhileTheFreePeoplesPlayerHasInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ownRunner = scn.GetFreepsCard("runner1");
		var deftness = scn.GetFreepsCard("deftness");
		var sword = scn.GetFreepsCard("sword");
		var fighter1 = scn.GetShadowCard("fighter1");
		var fighter2 = scn.GetShadowCard("fighter2");
		scn.MoveCompanionsToTable(eomer, guard);
		scn.MoveCardsToHand(aragorn, ownRunner, deftness);

		scn.StartGame();
		assertInHand(aragorn, ownRunner, deftness);
		scn.MoveMinionsToTable(fighter1, fighter2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{eomer, fighter1}, new PhysicalCardImpl[]{guard, fighter2});
		scn.FreepsResolveSkirmish(eomer);

		// Three cards in hand: the cost could be paid, but the Shadow has initiative.
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(eomer));

		// A fourth card: initiative, and the ability.
		scn.MoveCardsToHand(sword);
		scn.FreepsRefreshDecision();
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsActionAvailable(eomer));

		// A threat and two Mordor Fighters give the Shadow initiative whatever the hand holds.
		scn.AddThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertEquals(4, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(eomer));

		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsActionAvailable(eomer));

		// From exactly four cards: discarding three leaves one, and the effect still follows.
		scn.FreepsUseCardAction(eomer);
		scn.FreepsChooseCards(aragorn, ownRunner, deftness);
		assertEquals(1, scn.GetFreepsHandCount());
		scn.FreepsChoose("2");

		// 7 + 2 spotted (Éomer and the Guard)
		assertEquals(9, scn.GetStrength(eomer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
