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

public class Card_07_111_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("walls", "7_111");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability only
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion; Knight is its whole text
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; Valiant is its whole text
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit; a skirmish ability never used

					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, site 4; no text
					put("troop2", "1_177");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, site 5; Damage +1 only
					put("warrior", "4_165");    // Orthanc Warrior: [isengard] Uruk-hai, site 5; Damage +1 only
					put("nelya", "2_84");       // Ulaire Nelya, Ringwraith in Twilight: [wraith] Nazgul, site 2; nothing below 2 burdens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path, on which the Gondorian Merchant (home site 3) skirmishes at site 3 as an ally
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("walls", "7_111");
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man ally, home site 3 of the Towers block
					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, site 4; no text
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ManTheWallsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Man the Walls
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: The site number of each minion skirmishing a [gondor] Man is +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("walls");

		assertEquals("Man the Walls", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachMinionSkirmishingAGondorManIsSiteNumberPlusOneWhileThatSkirmishLasts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var walls = scn.GetFreepsCard("walls");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var guard = scn.GetFreepsCard("guard");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(aragorn, knight, guard, pippin);
		scn.MoveCardsToSupportArea(walls);
		assertInZone(Zone.SUPPORT, walls);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var savage = scn.GetShadowCard("savage");
		var warrior = scn.GetShadowCard("warrior");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2, savage, warrior, nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop1}, new PhysicalCardImpl[]{knight, troop2},
				new PhysicalCardImpl[]{guard, warrior}, new PhysicalCardImpl[]{pippin, nelya});
		// The Savage, left over, is put on Aragorn beside the first Troop.
		scn.ShadowAssignToMinions(aragorn, savage);

		// Assigned, but no skirmish has begun: every minion has its printed number.
		assertEquals(4, scn.GetMinionSiteNumber(troop1));
		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertEquals(4, scn.GetMinionSiteNumber(troop2));
		assertEquals(5, scn.GetMinionSiteNumber(warrior));
		assertEquals(2, scn.GetMinionSiteNumber(nelya));

		// Aragorn's skirmish: both minions skirmishing him, of two cultures and races, are +1; the Troop assigned
		// to the Knight, whose skirmish has not begun, is not.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(5, scn.GetMinionSiteNumber(troop1));    // 4 + 1
		assertEquals(6, scn.GetMinionSiteNumber(savage));    // 5 + 1
		assertEquals(4, scn.GetMinionSiteNumber(troop2));
		assertEquals(5, scn.GetMinionSiteNumber(warrior));
		assertEquals(2, scn.GetMinionSiteNumber(nelya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Once that skirmish is over, they are back to their printed numbers.
		scn.PassSkirmishActions();
		assertEquals(4, scn.GetMinionSiteNumber(troop1));
		assertEquals(5, scn.GetMinionSiteNumber(savage));

		// A minion skirmishing a Man of another culture is not +1.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.IsCharSkirmishing(warrior));
		assertEquals(5, scn.GetMinionSiteNumber(warrior));
		scn.PassSkirmishActions();

		// Nor is one skirmishing a [gondor] companion who is no Man.
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.IsCharSkirmishing(nelya));
		assertEquals(2, scn.GetMinionSiteNumber(nelya));
		scn.PassSkirmishActions();

		// The Knight's skirmish: now his Troop is +1.
		scn.FreepsResolveSkirmish(knight);
		assertEquals(5, scn.GetMinionSiteNumber(troop2));    // 4 + 1
		assertEquals(4, scn.GetMinionSiteNumber(troop1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMinionSkirmishingAGondorManWhoIsAnAllyIsSiteNumberPlusOneToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var walls = scn.GetFreepsCard("walls");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(walls);
		assertInZone(Zone.SUPPORT, walls);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		// Site 3 of the Towers block: the Merchant is at his home and may skirmish.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsCanAssign(merchant));
		scn.FreepsAssignToMinions(merchant, troop1);
		assertEquals(4, scn.GetMinionSiteNumber(troop1));

		scn.FreepsResolveSkirmish(merchant);
		assertEquals(5, scn.GetMinionSiteNumber(troop1));    // 4 + 1
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
