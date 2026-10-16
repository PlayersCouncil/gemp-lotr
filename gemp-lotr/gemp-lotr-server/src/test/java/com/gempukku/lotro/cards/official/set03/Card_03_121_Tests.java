package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_121_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "3_121");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A forest that is nothing else, then a river that is a sanctuary as well, then an underground site.
	protected VirtualTableScenario GetForestThenRiverScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "3_121");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");  // Bag End: no terrain
					put("site2", "1_334");  // Trollshaw Forest: forest
					put("site3", "1_338");  // Ford of Bruinen: river, sanctuary
					put("site4", "1_343");  // Balin's Tomb: underground
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A river that is nothing else, then a site that is a sanctuary and nothing else.
	protected VirtualTableScenario GetRiverThenSanctuaryScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "3_121");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");  // Bag End: no terrain
					// Buckleberry Ferry: river.  Its skip of the assignment and skirmish phases needs a fellowship of
					// Hobbits alone, and Legolas is in it.
					put("site2", "1_330");
					put("site3", "1_341");  // Rivendell Valley: sanctuary
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A plains, then a forest that is a sanctuary as well.
	protected VirtualTableScenario GetPlainsThenForestScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "3_121");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");  // Bag End: no terrain
					put("site2", "1_331");  // Ettenmoors: plains
					// Rivendell Waterfall: forest, sanctuary.  Its move limit +1 wants a ranger, and none is in play.
					put("site3", "1_342");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LegolasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Legolas, Son of Thranduil
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Archer</b>.
		 * While Legolas is at a river or forest, add 1 to the fellowship archery total.
		 */

		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");

		assertEquals("Legolas", legolas.getBlueprint().getTitle());
		assertEquals("Son of Thranduil", legolas.getBlueprint().getSubtitle());
		assertTrue(legolas.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, legolas.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, legolas.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, legolas.getBlueprint().getCardType());
		assertEquals(Race.ELF, legolas.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(legolas, Keyword.ARCHER));
		assertEquals(2, legolas.getBlueprint().getTwilightCost());
		assertEquals(6, legolas.getBlueprint().getStrength());
		assertEquals(3, legolas.getBlueprint().getVitality());
		assertEquals(6, legolas.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, legolas.getBlueprint().getSignet());
	}

	@Test
	public void AddsOneToTheFellowshipArcheryTotalAtAForestAndAtARiverAndNotBeforeOrAfterThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetForestThenRiverScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();

		// Site 1, Bag End: no river, no forest.  Legolas is an archer: 1.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// The fellowship moves to site 2, Trollshaw Forest: 1 for the archer + 1.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Site 3, Ford of Bruinen: a river (and a sanctuary), no forest.
		scn.SkipToSite(3);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetFreepsArcheryTotal());

		// Site 4, Balin's Tomb: underground.
		scn.SkipToSite(4);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AddsOneAtARiverThatIsNoSanctuaryAndNothingAtASanctuaryThatIsNeither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRiverThenSanctuaryScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// Site 2, Buckleberry Ferry: a river and nothing else.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Site 3, Rivendell Valley: a sanctuary and no river or forest.
		scn.SkipToSite(3);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AddsNothingAtAPlainsAndOneAtAForestThatIsASanctuary() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlainsThenForestScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// Site 2, Ettenmoors: a plains.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Site 3, Rivendell Waterfall: a forest and a sanctuary.
		scn.SkipToSite(3);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
