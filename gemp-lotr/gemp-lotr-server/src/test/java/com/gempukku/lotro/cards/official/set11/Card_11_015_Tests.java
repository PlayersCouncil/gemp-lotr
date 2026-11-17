package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_015_Tests
{

	// The Fellowship path with Derndingle 4_330 as site 2 (a forest; its move limit text needs an unbound Hobbit, and
	// none is laid) and Rivendell Waterfall 1_342 as site 3 (a forest and a sanctuary; its move limit text needs a
	// ranger, and none is laid).  Bag End 1_319, site 1, is no forest.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "11_15");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, Knight and no other text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("troop2", "1_177");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, damage +1 and nothing else
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "4_330");
					put("site3", "1_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArwenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Arwen, Staunch Defender
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: Each minion skirmishing Arwen is strength -1 for each forest site on the adventure path.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arwen");

		assertEquals("Arwen", card.getBlueprint().getTitle());
		assertEquals("Staunch Defender", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void WithTwoForestSitesOnThePathEachMinionSkirmishingArwenIsStrengthMinus2AndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(arwen, knight);

		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(troop, troop2, savage);
		scn.SkipToAssignments();
		// Sites 1 to 3 are on the path: Derndingle and Rivendell Waterfall are forests, Bag End is not.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetSite(1), Keyword.FOREST));
		assertTrue(scn.HasKeyword(scn.GetSite(2), Keyword.FOREST));
		assertTrue(scn.HasKeyword(scn.GetSite(3), Keyword.FOREST));

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{arwen, troop},
				new PhysicalCardImpl[]{knight, troop2});
		// The Uruk-hai left over goes to Arwen as well: two minions in her skirmish.
		scn.ShadowAssignToMinions(arwen, savage);

		// Assigned to her, but no skirmish has begun.
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop2));

		// The Knight's skirmish first: his minion, and the minions waiting for Arwen, are as printed.
		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.IsCharSkirmishingAgainst(knight, troop2));
		assertEquals(13, scn.GetStrength(troop2));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		scn.PassSkirmishActions();

		// Arwen's skirmish: an Orc and an Uruk-hai, each 1 less for each of the two forests.
		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, troop));
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, savage));
		assertEquals(11, scn.GetStrength(troop));
		assertEquals(3, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithOneForestSiteOnThePathTheMinionSkirmishingArwenIsStrengthMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		// Sites 1 and 2 are on the path, and only Derndingle is a forest; Rivendell Waterfall is not laid yet.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetSite(1), Keyword.FOREST));
		assertTrue(scn.HasKeyword(scn.GetSite(2), Keyword.FOREST));
		assertNull(scn.GetSite(3));

		scn.FreepsAssignToMinions(arwen, troop);
		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, troop));
		assertEquals(12, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
