package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_107_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("patrol", "5_107");

					// Orc Chaser: a [sauron] Orc and tracker, site 6.  "The site number of each [sauron] Orc is -1."
					// Its other sentence makes it stronger while the Shadow player has initiative.
					put("chaser", "7_298");
					// Orc Soldier: a [sauron] Orc who is not unique and no tracker, cost 2, vitality 2, site 6.
					// A skirmish ability that is never used.
					put("soldier1", "1_271");
					put("soldier2", "1_271");
					// Gorgoroth Axeman: a [sauron] Orc and besieger, site 5.  A skirmish ability never used.
					put("axeman", "7_271");
					// Goblin Patrol Troop: a [moria] Orc, site 4, no text.
					put("troop", "1_177");
					// Corpse Lights: a [sauron] Wraith (twilight), site 4.  A skirmish ability never used.
					put("lights", "6_99");
					// Seeking Its Master: a [sauron] condition in the support area; a Shadow ability never used.
					put("seeking", "1_276");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability never used
					put("saga", "1_114");       // The Saga of Elendil: a [gondor] condition borne by a [gondor] companion; a skirmish ability
					put("lord", "1_21");        // Lord of Moria: a [dwarven] condition in the support area; each Dwarf is damage +1
					put("toby", "1_305");       // Old Toby: a [shire] possession in the support area; its text answers its own play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcPatrolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Orc Patrol
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: Tracker.
		 * 	The site number of each [sauron] Orc is -3.
		 * 	Maneuver: Exert Orc Patrol twice and spot another [sauron] Orc to discard a condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("patrol");

		assertEquals("Orc Patrol", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsPlayedAtHisOwnSiteNumberAndOnceInPlayMakesEachSauronOrcSiteNumberMinusThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol = scn.GetShadowCard("patrol");
		var chaser = scn.GetShadowCard("chaser");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		var axeman = scn.GetShadowCard("axeman");
		var troop = scn.GetShadowCard("troop");
		var lights = scn.GetShadowCard("lights");
		scn.MoveCardsToHand(patrol, soldier2);

		scn.StartGame();
		scn.MoveMinionsToTable(chaser, soldier1, axeman, troop, lights);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 5 set + 1 companion + 2 for the site
		assertEquals(8, scn.GetTwilight());
		// The Orc Chaser's -1 is on every [sauron] Orc, itself included.
		assertEquals(5, scn.GetMinionSiteNumber(chaser));
		assertEquals(5, scn.GetMinionSiteNumber(soldier1));
		assertEquals(4, scn.GetMinionSiteNumber(axeman));
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertEquals(4, scn.GetMinionSiteNumber(lights));
		assertTrue(scn.HasKeyword(chaser, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(soldier1, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(axeman, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(lights, Keyword.ROAMING));
		assertTrue(scn.ShadowPlayAvailable(patrol));

		scn.ShadowPlayCard(patrol);
		// 4 to play + 2 roaming.  His own -3 does not apply to him while he is played: his site number is
		// 6 - 1 for the Chaser, at site 2.  With it he would have been site number 2, not roaming, and cost 4.
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, patrol);

		// Once in play it applies to each [sauron] Orc, himself included.
		assertEquals(2, scn.GetMinionSiteNumber(patrol));       // 6 - 1 - 3
		assertEquals(2, scn.GetMinionSiteNumber(chaser));
		assertEquals(2, scn.GetMinionSiteNumber(soldier1));
		assertEquals(1, scn.GetMinionSiteNumber(axeman));       // 5 - 1 - 3
		assertFalse(scn.HasKeyword(patrol, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(chaser, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(soldier1, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(axeman, Keyword.ROAMING));
		// The [moria] Orc and the [sauron] Wraith are where they were.
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertEquals(4, scn.GetMinionSiteNumber(lights));
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(lights, Keyword.ROAMING));

		// The Orc Soldier in hand is a [sauron] Orc too.  It would have cost 2 + 2 roaming before Orc Patrol;
		// the 2 that are left now pay for it.
		assertTrue(scn.ShadowPlayAvailable(soldier2));
		scn.ShadowPlayCard(soldier2);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, soldier2);
		assertEquals(2, scn.GetMinionSiteNumber(soldier2));
		assertFalse(scn.HasKeyword(soldier2, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ManeuverAbilityExertsHimTwiceToDiscardAConditionOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var lord = scn.GetFreepsCard("lord");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, saga);
		scn.MoveCardsToSupportArea(lord, toby);

		var patrol = scn.GetShadowCard("patrol");
		var soldier = scn.GetShadowCard("soldier1");
		var seeking = scn.GetShadowCard("seeking");
		scn.MoveCardsToSupportArea(seeking);

		scn.StartGame();
		scn.MoveMinionsToTable(patrol, soldier);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertTrue(scn.ShadowActionAvailable(patrol));

		scn.ShadowUseCardAction(patrol);
		assertEquals(2, scn.GetWoundsOn(patrol));
		assertEquals(0, scn.GetWoundsOn(soldier));

		// A Free Peoples condition borne or in the support area, and his own; not a possession.
		assertTrue(scn.ShadowHasCardChoiceAvailable(saga, lord, seeking));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(toby));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(saga);

		assertInZone(Zone.DISCARD, saga);
		assertInZone(Zone.SUPPORT, lord, toby, seeking);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedUntilAnotherSauronOrcIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lord = scn.GetFreepsCard("lord");
		scn.MoveCardsToSupportArea(lord);

		var patrol = scn.GetShadowCard("patrol");
		var troop = scn.GetShadowCard("troop");
		var lights = scn.GetShadowCard("lights");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.MoveMinionsToTable(patrol, troop, lights);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Orc Patrol himself, a [moria] Orc and a [sauron] Wraith: none is another [sauron] Orc.
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(patrol));

		// A [sauron] Orc of another title, a besieger.
		scn.MoveMinionsToTable(axeman);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(patrol));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhileOrcPatrolCanExertOnlyOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lord = scn.GetFreepsCard("lord");
		scn.MoveCardsToSupportArea(lord);

		var patrol = scn.GetShadowCard("patrol");
		var chaser = scn.GetShadowCard("chaser");

		scn.StartGame();
		scn.MoveMinionsToTable(patrol, chaser);
		scn.AddWoundsToChar(patrol, 1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Vitality 3 with 1 wound: one exertion left, and the cost is two.
		assertEquals(1, scn.GetWoundsOn(patrol));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(patrol));
	}
}
