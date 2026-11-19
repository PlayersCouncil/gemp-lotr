package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_100_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grishnakh", "5_100");

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

					put("merry", "1_302");      // Merry, Friend to Sam: an unbound Hobbit; a skirmish ability never used
					// Unheeded, a [shire] response event: "If a minion exerts, exert an unbound Hobbit to wound that
					// minion."
					put("unheeded", "8_115");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrishnakhStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Grishnákh, Orc Captain
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
		 * 	Shadow: Exert Grishnákh twice and spot another [sauron] Orc to draw 3 cards. The Free Peoples player
		 * 	may add 2 burdens to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grishnakh");

		assertEquals("Grishnákh", card.getBlueprint().getTitle());
		assertEquals("Orc Captain", card.getBlueprint().getSubtitle());
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

		var grishnakh = scn.GetShadowCard("grishnakh");
		var chaser = scn.GetShadowCard("chaser");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		var axeman = scn.GetShadowCard("axeman");
		var troop = scn.GetShadowCard("troop");
		var lights = scn.GetShadowCard("lights");
		scn.MoveCardsToHand(grishnakh, soldier2);

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
		assertTrue(scn.ShadowPlayAvailable(grishnakh));

		scn.ShadowPlayCard(grishnakh);
		// 4 to play + 2 roaming.  His own -3 does not apply to him while he is played: his site number is
		// 6 - 1 for the Chaser, at site 2.  With it he would have been site number 2, not roaming, and cost 4.
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, grishnakh);

		// Once in play it applies to each [sauron] Orc, himself included.
		assertEquals(2, scn.GetMinionSiteNumber(grishnakh));     // 6 - 1 - 3
		assertEquals(2, scn.GetMinionSiteNumber(chaser));
		assertEquals(2, scn.GetMinionSiteNumber(soldier1));
		assertEquals(1, scn.GetMinionSiteNumber(axeman));        // 5 - 1 - 3
		assertFalse(scn.HasKeyword(grishnakh, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(chaser, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(soldier1, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(axeman, Keyword.ROAMING));
		// The [moria] Orc and the [sauron] Wraith are where they were.
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertEquals(4, scn.GetMinionSiteNumber(lights));
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(lights, Keyword.ROAMING));

		// The Orc Soldier in hand is a [sauron] Orc too.  It would have cost 2 + 2 roaming before Grishnákh;
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
	public void ShadowAbilityExertsHimTwiceToDrawThreeCardsWhenTheFreePeoplesPlayerDeclinesToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grishnakh = scn.GetShadowCard("grishnakh");
		var soldier = scn.GetShadowCard("soldier1");

		scn.StartGame();
		scn.MoveMinionsToTable(grishnakh, soldier);
		scn.FreepsPass();

		int deck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetWoundsOn(grishnakh));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(grishnakh));

		scn.ShadowUseCardAction(grishnakh);
		assertEquals(2, scn.GetWoundsOn(grishnakh));
		assertEquals(0, scn.GetWoundsOn(soldier));

		assertTrue(scn.FreepsDecisionAvailable("add 2 burdens"));
		scn.FreepsChooseNo();

		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(deck - 3, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayAddTwoBurdensToPreventTheDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grishnakh = scn.GetShadowCard("grishnakh");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.MoveMinionsToTable(grishnakh, axeman);
		scn.FreepsPass();

		int deck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(grishnakh));

		scn.ShadowUseCardAction(grishnakh);
		assertTrue(scn.FreepsDecisionAvailable("add 2 burdens"));
		scn.FreepsChooseYes();

		assertEquals(2, scn.GetBurdens());
		// The exertions were the cost and stay paid.
		assertEquals(2, scn.GetWoundsOn(grishnakh));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedUntilAnotherSauronOrcIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grishnakh = scn.GetShadowCard("grishnakh");
		var troop = scn.GetShadowCard("troop");
		var lights = scn.GetShadowCard("lights");
		var soldier = scn.GetShadowCard("soldier1");
		scn.MoveCardsToHand(soldier);

		scn.StartGame();
		scn.MoveMinionsToTable(grishnakh, troop, lights);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// Grishnákh himself, a [moria] Orc and a [sauron] Wraith: none is another [sauron] Orc.
		assertEquals(0, scn.GetWoundsOn(grishnakh));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(grishnakh));

		scn.ShadowPlayCard(soldier);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);

		assertTrue(scn.ShadowActionAvailable(grishnakh));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileGrishnakhCanExertOnlyOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grishnakh = scn.GetShadowCard("grishnakh");
		var soldier = scn.GetShadowCard("soldier1");

		scn.StartGame();
		scn.MoveMinionsToTable(grishnakh, soldier);
		scn.AddWoundsToChar(grishnakh, 1);
		scn.FreepsPass();

		// Vitality 3 with 1 wound: one exertion left, and the cost is two.
		assertEquals(1, scn.GetWoundsOn(grishnakh));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(grishnakh));
	}

	@Test
	public void UnheededWoundingHimAfterTheFirstExertionLeavesTheSecondUnpaidAndNothingIsDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var unheeded = scn.GetFreepsCard("unheeded");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(unheeded);

		var grishnakh = scn.GetShadowCard("grishnakh");
		var soldier = scn.GetShadowCard("soldier1");

		scn.StartGame();
		scn.MoveMinionsToTable(grishnakh, soldier);
		scn.FreepsPass();

		int deck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetShadowHandCount());
		scn.ShadowUseCardAction(grishnakh);

		// The first exertion is answered: Unheeded exerts Merry to wound Grishnákh.
		assertEquals(1, scn.GetWoundsOn(grishnakh));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(unheeded));
		scn.FreepsChooseOptionalTrigger(unheeded);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(grishnakh));

		// With one vitality left he cannot exert the second time, so nothing is drawn and nothing is asked of
		// the Free Peoples player.
		assertFalse(scn.FreepsDecisionAvailable("add 2 burdens"));
		assertEquals(2, scn.GetWoundsOn(grishnakh));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTheShadowDrawDeckEmptyThePreventionIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grishnakh = scn.GetShadowCard("grishnakh");
		var soldier = scn.GetShadowCard("soldier1");

		scn.StartGame();
		scn.MoveMinionsToTable(grishnakh, soldier);
		scn.SetShadowDrawDeck();
		scn.FreepsPass();

		assertEquals(0, scn.GetShadowDeckCount());
		assertTrue(scn.ShadowActionAvailable(grishnakh));
		scn.ShadowUseCardAction(grishnakh);

		// The cost is paid; with nothing to draw the Free Peoples player is not asked to prevent it.
		assertEquals(2, scn.GetWoundsOn(grishnakh));
		assertFalse(scn.FreepsDecisionAvailable("add 2 burdens"));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
