package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_13_165_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("infiltrator", "13_165");
					put("infiltrator2", "13_165");
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: [elven] Elf companion, archer, strength 6; +3 only against a Nazgul
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, strength 6; +3 only against a Nazgul
					put("bow", "1_41");         // Elven Bow: bearer (an Elf) is an archer, nothing else
					put("erethon", "4_65");     // Erethon, Naith Lieutenant: [elven] Elf companion, strength 6, no archer; his text asks a ranged weapon he never bears
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, strength 8; his maneuver ability is never used
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: [elven] Elf ALLY, strength 8, home site 3 of the Fellowship path
					put("marksman", "1_176");   // Goblin Marksman: Shadow archer, nothing else
					put("tyrannical", "11_204"); // Tyrannical Uruk: [uruk-hai] Uruk-hai, strength 14; damage +1 and toil only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path with Derndingle, a forest, at site 2; site 1 (Eastemnet Gullies) has no terrain.
	protected VirtualTableScenario GetDerndingleScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("infiltrator", "13_165");
					put("legolas", "1_51");
					put("arwen", "1_30");
					put("bow", "1_41");
					put("marksman", "1_176");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_330");      // Derndingle: Forest; move limit +1 while an unbound Hobbit is there, and none is
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Rivendell Waterfall, a forest that is also a sanctuary, at site 3.
	protected VirtualTableScenario GetWaterfallScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("infiltrator", "13_165");
					put("legolas", "1_51");
					put("arwen", "1_30");
					put("bow", "1_41");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "1_342");      // Rivendell Waterfall: Forest, Sanctuary; move limit +1 while a ranger is spotted there
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsengardInfiltratorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Isengard Infiltrator
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 7
		 * Vitality: 1
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>While the fellowship is at a forest site, the fellowship archery total is -1.<br>Each [elven] companion skirmishing this minion is strength -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("infiltrator");

		assertEquals("Isengard Infiltrator", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void FellowshipArcheryTotalIsOneLessOnceTheFellowshipMovesToAForest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDerndingleScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(legolas, arwen);
		scn.AttachCardsTo(arwen, bow);

		var infiltrator = scn.GetShadowCard("infiltrator");
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(infiltrator, marksman);

		// Site 1 is no forest: two archers, a total of 2; the Marksman makes the minion total 1.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(arwen, Keyword.ARCHER));
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.FreepsPass();
		// At Derndingle, a forest: 2 - 1.  The minion archery total is not touched.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FellowshipArcheryTotalIsOneLessAtAForestThatIsASanctuary() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWaterfallScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(legolas, arwen);
		scn.AttachCardsTo(arwen, bow);

		var infiltrator = scn.GetShadowCard("infiltrator");

		scn.StartGame();
		scn.SkipToSite(3);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetFreepsArcheryTotal());

		scn.MoveMinionsToTable(infiltrator);
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachElvenCompanionSkirmishingThisMinionIsStrengthMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erethon = scn.GetFreepsCard("erethon");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(erethon, arwen);

		var infiltrator = scn.GetShadowCard("infiltrator");
		var infiltrator2 = scn.GetShadowCard("infiltrator2");

		scn.StartGame();
		scn.MoveMinionsToTable(infiltrator, infiltrator2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{erethon, infiltrator}, new PhysicalCardImpl[]{arwen, infiltrator2});
		// Assigned, but neither skirmish has begun.
		assertEquals(6, scn.GetStrength(erethon));
		assertEquals(6, scn.GetStrength(arwen));

		scn.FreepsResolveSkirmish(erethon);
		// Each copy reaches only the companion skirmishing it: Erethon is -2, not -4; Arwen waits, unchanged.
		assertEquals(4, scn.GetStrength(erethon));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(7, scn.GetStrength(infiltrator));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(4, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(erethon));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CompanionsOfAnotherCultureAnElvenAllyAndAnElfSkirmishingAnotherMinionKeepTheirStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var erethon = scn.GetFreepsCard("erethon");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn, erethon);
		scn.MoveAlliesToTable(elrond);

		var infiltrator = scn.GetShadowCard("infiltrator");
		var infiltrator2 = scn.GetShadowCard("infiltrator2");
		var tyrannical = scn.GetShadowCard("tyrannical");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(infiltrator, infiltrator2, tyrannical);

		// The move to site 3, Elrond's home, lets him skirmish.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, infiltrator}, new PhysicalCardImpl[]{elrond, infiltrator2},
				new PhysicalCardImpl[]{erethon, tyrannical});

		scn.FreepsResolveSkirmish(erethon);
		// An [elven] companion, while the Infiltrators are in play, skirmishing another minion.
		assertEquals(6, scn.GetStrength(erethon));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(elrond);
		// An [elven] character skirmishing this minion who is no companion.
		assertEquals(8, scn.GetStrength(elrond));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(aragorn);
		// A companion of another culture skirmishing this minion.
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
