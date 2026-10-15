package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_353_Tests
{

	// The Towers path with Great Hall as its site 6.  Neither [rohan] ally here has a home on it at site 5, 6 or 7
	// (Hlafwine 4T, the Herdsman 3T), and nor has the [gondor] Gondorian Merchant (3T).  Aldor 4_262, whose homes are
	// 3T and 6T, is at home at Great Hall whatever its text says, and is left out.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hlafwine", "4_281");   // Hlafwine, Village Farmhand: unique [rohan] Man ally, home 4T, strength 4, vitality 2; discarded only if an opponent controls 4T; a skirmish ability used only on purpose
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally, not unique, home 3T, strength 4, vitality 2; a regroup ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally, home 3T, strength 6, vitality 1; a maneuver ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose

					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; its text answers only its own play
					put("runner2", "1_178");
					put("marksman1", "1_176");  // Goblin Marksman: archer, strength 7, vitality 1, no other text
					put("marksman2", "1_176");
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");      // Eastemnet Gullies: a fellowship ability used only on purpose
					put("site2", "4_331");      // Eastfold: a Shadow ability that needs 3 [isengard] trackers
					put("site3", "4_337");      // Barrows of Edoras: a sanctuary; a fellowship ability used only on purpose
					put("site4", "4_346");      // White Rocks: its Shadow number counts mounted companions (none here)
					put("site5", "4_348");      // Deeping Wall: a Shadow ability that plays Saruman (none here)
					put("site6", "4_353");      // Great Hall
					put("site7", "102_71");     // Helm's Tower: damage +1 only while 6 companions can be spotted
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreatHallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Great Hall
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 6
		 * Shadow Number: 3
		 * Direction: Left
		 * Game Text: <b>Battleground</b>. <b>Sanctuary</b>. Each [rohan] ally may participate in archery fire and skirmishes at Great Hall.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(6);

		assertEquals("Great Hall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(6, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void AtGreatHallEachRohanAllyMayBeAssignedToASkirmishAndFightsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hlafwine = scn.GetFreepsCard("hlafwine");
		var herdsman = scn.GetFreepsCard("herdsman");
		var merchant = scn.GetFreepsCard("merchant");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(hlafwine, herdsman, merchant);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals("Great Hall", scn.GetCurrentSite().getBlueprint().getTitle());

		// The game's rule: both [rohan] allies, of two titles and homes, and not the [gondor] ally.
		assertTrue(scn.FreepsCanAssign(hlafwine));
		assertTrue(scn.FreepsCanAssign(herdsman));
		assertFalse(scn.FreepsCanAssign(merchant));

		// And what the Free Peoples player is asked.
		scn.PassAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(hlafwine));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(herdsman));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(merchant));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{hlafwine, runner1}, new PhysicalCardImpl[]{herdsman, runner2});
		assertTrue(scn.IsCharAssignedAgainst(hlafwine, runner1));
		assertTrue(scn.IsCharAssignedAgainst(herdsman, runner2));

		scn.FreepsResolveSkirmish(hlafwine);
		assertTrue(scn.IsCharSkirmishingAgainst(hlafwine, runner1));
		assertEquals(0, scn.GetWoundsOn(hlafwine));
		scn.PassSkirmishActions();
		// 5 against 4: Hlafwine loses and takes a wound.
		assertEquals(1, scn.GetWoundsOn(hlafwine));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AtGreatHallTheShadowPlayerMayAssignAMinionToARohanAllyAsAtHisHomeSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hlafwine = scn.GetFreepsCard("hlafwine");
		var herdsman = scn.GetFreepsCard("herdsman");
		var merchant = scn.GetFreepsCard("merchant");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(hlafwine, herdsman, merchant);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals("Great Hall", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.ShadowCanAssign(hlafwine));
		assertTrue(scn.ShadowCanAssign(herdsman));
		assertFalse(scn.ShadowCanAssign(merchant));

		scn.PassAssignmentActions();
		// The Free Peoples player assigns Aragorn and leaves a minion over.
		scn.FreepsAssignToMinions(aragorn, runner1);

		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(hlafwine));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(herdsman));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(merchant));
		scn.ShadowAssignToMinions(herdsman, runner2);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, runner1));
		assertTrue(scn.IsCharAssignedAgainst(herdsman, runner2));
		assertFalse(scn.IsCharAssigned(hlafwine));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AtGreatHallArcheryWoundsMayBePlacedOnEachRohanAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hlafwine = scn.GetFreepsCard("hlafwine");
		var herdsman = scn.GetFreepsCard("herdsman");
		var merchant = scn.GetFreepsCard("merchant");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(hlafwine, herdsman, merchant);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(marksman1, marksman2);
		scn.SkipToArcheryWounds();
		assertEquals("Great Hall", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetWoundsOn(hlafwine));
		assertEquals(0, scn.GetWoundsOn(herdsman));

		// Two archery wounds, placed one at a time.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, hlafwine, herdsman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant));
		scn.FreepsChooseCard(hlafwine);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, hlafwine, herdsman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant));
		scn.FreepsChooseCard(herdsman);

		assertEquals(1, scn.GetWoundsOn(hlafwine));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftGreatHallRohanAlliesTakeNoArcheryWoundAndCannotBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hlafwine = scn.GetFreepsCard("hlafwine");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var marksman1 = scn.GetShadowCard("marksman1");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(hlafwine, herdsman);

		scn.StartGame();
		scn.SkipToSite(6);
		assertEquals("Great Hall", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(marksman1, runner1);
		scn.SkipToArcheryWounds();
		// Great Hall is behind the fellowship, at Helm's Tower.
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals("Great Hall", scn.GetSite(6).getBlueprint().getTitle());

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(hlafwine, herdsman));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(hlafwine));
		assertEquals(0, scn.GetWoundsOn(herdsman));

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsCanAssign(hlafwine));
		assertFalse(scn.FreepsCanAssign(herdsman));
		assertFalse(scn.ShadowCanAssign(hlafwine));
		assertFalse(scn.ShadowCanAssign(herdsman));
		assertTrue(scn.FreepsCanAssign(aragorn));
		scn.PassAssignmentActions();
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(hlafwine));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(herdsman));
	}
}
