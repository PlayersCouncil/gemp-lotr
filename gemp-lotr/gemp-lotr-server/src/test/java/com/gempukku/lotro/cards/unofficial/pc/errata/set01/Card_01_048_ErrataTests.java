package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_048_ErrataTests
{
	// Site 1 has no terrain, 2 is plains, 5 underground, 6 a sanctuary and nothing else, 7 a river.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haldir", "51_48");

					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, cost 2, strength 6
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] Elf ally, cost 2; an archery ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: [dwarven] Dwarf ally; a maneuver ability only
					put("shoulder", "1_59");    // Shoulder to Shoulder: [elven] condition in the support area; a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetForestAndRiverScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haldir", "51_48");

					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, strength 6
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

	// Tower block: site 5 and 7 are battlegrounds, 6 is underground and a sanctuary, 8 and 9 have no terrain.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haldir", "51_48");

					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, strength 6
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HaldirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Haldir, Elf of the Golden Wood
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: To play, spot an Elf.
		 * While Haldir is at a forest or river (or any site 6, 7, or 8), he is strength +2 and an archer.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("haldir");

		assertEquals("Haldir", card.getBlueprint().getTitle());
		assertEquals("Elf of the Golden Wood", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void CannotBePlayedUntilAnElfCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var shoulder = scn.GetFreepsCard("shoulder");
		// In play: a companion who is no Elf, and a card of the [elven] culture that is no Elf.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(shoulder);
		scn.MoveCardsToHand(haldir, arwen);

		scn.StartGame();

		// An Elf in hand is not spotted.
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.FreepsPlayAvailable(haldir));

		scn.FreepsPlayCard(arwen);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(haldir));

		scn.FreepsPlayCard(haldir);
		assertInZone(Zone.FREE_CHARACTERS, haldir);
		// 2 for Arwen + 2 for Haldir
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElfAllyLetsHaldirBePlayedOnceHeIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var orophin = scn.GetFreepsCard("orophin");
		var thrarin = scn.GetFreepsCard("thrarin");
		// An ally who is no Elf is in play; the Elf ally is still in hand.
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(haldir, orophin);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.FreepsPlayAvailable(haldir));

		// An Elf to spot need not be a companion.
		scn.FreepsPlayCard(orophin);
		assertInZone(Zone.SUPPORT, orophin);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(haldir));

		scn.FreepsPlayCard(haldir);
		assertInZone(Zone.FREE_CHARACTERS, haldir);
		// 2 for Orophin + 2 for Haldir
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsStrengthPlus2AndAnArcherAtAForestAndAtARiverAndNotBeforeOrAfterThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetForestAndRiverScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(haldir, arwen);

		scn.StartGame();

		// Site 1, Bag End: no forest, no river.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(6, scn.GetStrength(haldir));
		assertFalse(scn.HasKeyword(haldir, Keyword.ARCHER));

		// The fellowship moves to site 2, Trollshaw Forest.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(8, scn.GetStrength(haldir));
		assertTrue(scn.HasKeyword(haldir, Keyword.ARCHER));
		// Another Elf at the same forest is as printed.
		assertEquals(6, scn.GetStrength(arwen));
		assertFalse(scn.HasKeyword(arwen, Keyword.ARCHER));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Site 3, Ford of Bruinen: a river and no forest.
		scn.SkipToSite(3);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(8, scn.GetStrength(haldir));
		assertTrue(scn.HasKeyword(haldir, Keyword.ARCHER));
		assertEquals(6, scn.GetStrength(arwen));
		assertFalse(scn.HasKeyword(arwen, Keyword.ARCHER));

		// Site 4, Balin's Tomb: underground.
		scn.SkipToSite(4);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(6, scn.GetStrength(haldir));
		assertFalse(scn.HasKeyword(haldir, Keyword.ARCHER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsStrengthPlus2OnceAndAnArcherAtSite6AndAtARiverThatIsSite7OfTheFellowshipBlock() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(haldir);

		scn.StartGame();

		// Site 5, The Bridge of Khazad-dum: underground, and one short of site 6.
		scn.SkipToSite(5);
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(6, scn.GetStrength(haldir));
		assertFalse(scn.HasKeyword(haldir, Keyword.ARCHER));

		// Site 6, Galadriel's Glade: a sanctuary, neither forest nor river.
		scn.SkipToSite(6);
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(8, scn.GetStrength(haldir));
		assertTrue(scn.HasKeyword(haldir, Keyword.ARCHER));

		// Site 7, Anduin Confluence: a river and a site 7 at once is still +2, not +4.
		scn.SkipToSite(7);
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(8, scn.GetStrength(haldir));
		assertTrue(scn.HasKeyword(haldir, Keyword.ARCHER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsStrengthPlus2AndAnArcherAtSites6To8OfTheTowerBlockAndNotAtSite5Or9() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(haldir, arwen);

		scn.StartGame();

		// Site 5 of the Tower block, Deeping Wall: a battleground.
		scn.SkipToSite(5);
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(6, scn.GetStrength(haldir));
		assertFalse(scn.HasKeyword(haldir, Keyword.ARCHER));

		// Site 6, Caves of Aglarond: underground and a sanctuary.
		scn.SkipToSite(6);
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(8, scn.GetStrength(haldir));
		assertTrue(scn.HasKeyword(haldir, Keyword.ARCHER));
		// Another Elf at the same site is as printed.
		assertEquals(6, scn.GetStrength(arwen));
		assertFalse(scn.HasKeyword(arwen, Keyword.ARCHER));

		// Site 7, Helm's Tower: a battleground.
		scn.SkipToSite(7);
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(8, scn.GetStrength(haldir));
		assertTrue(scn.HasKeyword(haldir, Keyword.ARCHER));

		// Site 8, Ring of Isengard: no terrain.
		scn.SkipToSite(8);
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(8, scn.GetStrength(haldir));
		assertTrue(scn.HasKeyword(haldir, Keyword.ARCHER));

		// The fellowship moves to site 9, Palantir Chamber: no terrain, and one past site 8.
		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals(6, scn.GetStrength(haldir));
		assertFalse(scn.HasKeyword(haldir, Keyword.ARCHER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
