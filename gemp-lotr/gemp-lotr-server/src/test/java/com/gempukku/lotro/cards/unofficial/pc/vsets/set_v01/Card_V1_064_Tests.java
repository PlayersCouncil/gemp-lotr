package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_064_Tests
{
	// The Ring-bearer is Frodo, Son of Drogo, who is kept out of the skirmishes.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "0_64");     // Gandalf, Stormcrow: strength 8, vitality 4, no text
					// Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability that is never used
					put("aragorn", "1_89");
					put("legolas", "1_50");     // Legolas, Greenleaf: the fellowship's one archer
					// Pathfinder: "Fellowship or Regroup: Spot a ranger to play the fellowship's next site."  It puts
					// the Ruins on the path while the fellowship is still at site 8.
					put("pathfinder", "1_110");

					// Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text and no damage bonus
					put("troop1", "1_177");
					put("troop2", "1_177");
					put("troop3", "1_177");
					// Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 in print
					put("savage1", "1_151");
					put("savage2", "1_151");
					// Shelob, Last Child of Ungoliant: [gollum] Spider, vitality 8.  Her own damage +1 is only for a
					// fierce skirmish, and she is never assigned here.
					put("shelob", "8_26");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");      // Anduin Banks: adds to the minion archery total for companions over 4
					put("site9", "101_64");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AmonHenRuinsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Amon Hen Ruins
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 9
		 * Shadow Number: 9
		 * Direction: Left
		 * Game Text: Forest. Each minion is damage +1 for each wound on that minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Amon Hen Ruins", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
	}

	@Test
	public void EachMinionIsDamagePlusOneForEachOfItsOwnWoundsOnceTheFellowshipIsAtAmonHenRuins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		var troop = scn.GetShadowCard("troop1");
		var troopOnce = scn.GetShadowCard("troop2");
		var troopTwice = scn.GetShadowCard("troop3");
		var savage = scn.GetShadowCard("savage1");
		var savageOnce = scn.GetShadowCard("savage2");
		var shelob = scn.GetShadowCard("shelob");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(pathfinder);
		scn.AddWoundsToChar(gandalf, 2);

		// To site 8, and with no minion about straight to the regroup phase, where Pathfinder lays site 9.
		scn.FreepsPassCurrentPhaseAction();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPlayCard(pathfinder);
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals("Amon Hen Ruins", scn.GetSite(9).getBlueprint().getTitle());

		scn.MoveMinionsToTable(troop, troopOnce, troopTwice, savage, savageOnce, shelob);
		scn.AddWoundsToChar(troopOnce, 1);
		scn.AddWoundsToChar(troopTwice, 2);
		scn.AddWoundsToChar(savageOnce, 1);
		scn.AddWoundsToChar(shelob, 7);
		// The Ruins are on the path and the fellowship is not there: a wound is worth nothing, and only the
		// Uruk Savages' own damage +1 is to be seen.
		assertFalse(scn.HasKeyword(troopOnce, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troopOnce, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troopTwice, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(savageOnce, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(shelob, Keyword.DAMAGE));

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals("Amon Hen Ruins", scn.GetCurrentSite().getBlueprint().getTitle());
		// Each minion counts its own wounds: none, one, two and seven.  The unwounded one has no damage bonus at
		// all, not a bonus of nothing.
		assertFalse(scn.HasKeyword(troop, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(troopOnce, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(troopOnce, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(troopTwice, Keyword.DAMAGE));
		assertEquals(7, scn.GetKeywordCount(shelob, Keyword.DAMAGE));
		// The bonus is added to one a minion already has: 1 in print, and 1 in print + 1 for the wound.
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(savageOnce, Keyword.DAMAGE));
		// A companion's wounds do nothing for him.
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMinionWoundedByArcheryAtAmonHenRuinsDealsOneMoreWoundAndOneWoundedTwiceDealsTwoMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var troop = scn.GetShadowCard("troop1");
		var troopTwice = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(gandalf, aragorn, legolas);

		scn.FreepsPassCurrentPhaseAction();
		assertEquals("Amon Hen Ruins", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(troop, troopTwice);
		scn.AddWoundsToChar(troopTwice, 2);

		// Legolas is the only archer, so there is one archery wound for the Shadow player to place.
		scn.SkipToArcheryWounds();
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(troopTwice, Keyword.DAMAGE));

		scn.ShadowChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(troopTwice, Keyword.DAMAGE));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { gandalf, troop }, new PhysicalCardImpl[] { aragorn, troopTwice });

		// A Troop's 13 beats 8 without doubling it.  1 wound for losing + 1 for the Troop's one wound.
		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// 1 wound for losing + 2 for the other Troop's two wounds.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// The winners took no wound of their own in winning.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(troopTwice));
		// The fellowship has come through the skirmishes at site 9.
		assertTrue(scn.GameIsFinished());
	}
}
