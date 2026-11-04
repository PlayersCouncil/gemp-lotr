package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_049_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("roads", "6_49");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man ranger, strength 8, vitality 4; a maneuver ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf ranger, strength 6, vitality 3; +3 only against a Nazgul
					put("osgiliath", "7_116");  // Ranger of Osgiliath: [gondor] Man ranger, cost 2; a regroup ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man who is no ranger, strength 6, vitality 3; Knight only
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, site 5; Damage +1 only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1, site 4; cheated in, so its play trigger never fires
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum], no race, strength 5, vitality 4, site 3; a regroup ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, site 4; no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AncientRoadsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Ancient Roads
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>While you can spot 3 rangers, the site number of each minion skirmishing a ranger is +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("roads");

		assertEquals("Ancient Roads", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithAThirdRangerPlayedEachMinionSkirmishingARangerIsSiteNumberPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var roads = scn.GetFreepsCard("roads");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var osgiliath = scn.GetFreepsCard("osgiliath");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCardsToSupportArea(roads);
		scn.MoveCompanionsToTable(aragorn, arwen, knight);
		scn.MoveCardsToHand(osgiliath);
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var gollum = scn.GetShadowCard("gollum");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, roads);
		assertInHand(osgiliath);
		// The third ranger, by legal play.
		scn.FreepsPlayCard(osgiliath);
		assertInZone(Zone.FREE_CHARACTERS, osgiliath);

		scn.MoveMinionsToTable(savage, runner, gollum, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{arwen, gollum},
				new PhysicalCardImpl[]{knight, troop});
		// The Runner, left over, goes to Aragorn as well: two minions in one skirmish against a ranger.
		scn.ShadowAssignToMinions(aragorn, runner);

		scn.FreepsResolveSkirmish(aragorn);
		// Both minions skirmishing Aragorn, of two cultures and races, +2.
		assertEquals(7, scn.GetMinionSiteNumber(savage));
		assertEquals(6, scn.GetMinionSiteNumber(runner));
		// Assigned to a ranger and to a companion who is no ranger, their skirmishes not begun: printed.
		assertEquals(3, scn.GetMinionSiteNumber(gollum));
		assertEquals(4, scn.GetMinionSiteNumber(troop));

		// 10 against 8, not doubled: Aragorn loses and lives (2 wounds, with the Savage's damage +1).
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// Skirmishing Arwen, a ranger of another culture and race.
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(5, scn.GetMinionSiteNumber(gollum));
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gollum));

		// Skirmishing the Knight, who is no ranger, with three rangers still in play: printed. Gollum, whose
		// skirmish is over, is printed again too.
		scn.FreepsResolveSkirmish(knight);
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertEquals(3, scn.GetMinionSiteNumber(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTwoRangersNoMinionSkirmishingARangerIsChanged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var roads = scn.GetFreepsCard("roads");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCardsToSupportArea(roads);
		// Two rangers, and a companion beside them who is no ranger.
		scn.MoveCompanionsToTable(aragorn, arwen, knight);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, roads);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
