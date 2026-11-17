package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_017_Tests
{
	// The Towers path, whose site 1 (Eastemnet Gullies) and site 3 (Barrows of Edoras, a sanctuary) are neither
	// battleground nor forest. Site 2 is Uruk Camp (Battleground, Plains) and site 4 White Rocks 4_346 (Battleground
	// alone; its Shadow number counts mounted companions, and none is): two battlegrounds that differ in a keyword.
	protected VirtualTableScenario GetBattlegroundScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haldir", "15_17");

					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf companion, cost 2, with nothing needed to play her
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an Elf ally, cost 2; a fellowship ability never used here
					put("lorienelf", "1_53");   // Lorien Elf: an Elf companion of strength 4 with no text but a requirement to play
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man
					put("finalcount", "4_69");  // Final Count: [elven] condition in the support area; its texts act only for a Legolas, and none is here
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");      // Uruk Camp: Battleground. Plains. Exerts each Uruk-hai played; none is
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with two forests that differ in a keyword: Trollshaw Forest 1_334 at site 2 (Forest alone;
	// draws when a possession or artifact is played on a companion, and none is) and Rivendell Waterfall 1_342 at
	// site 3 (Forest, Sanctuary; a move limit bonus while a ranger is there, and none is). Bag End at site 1, and
	// Balin's Tomb at site 4, are neither.
	protected VirtualTableScenario GetForestScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haldir", "15_17");

					put("lorienelf", "1_53");   // Lorien Elf: an Elf companion of strength 4 with no text but a requirement to play
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_334");
					put("site3", "1_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HaldirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Haldir, Sentry of the Golden Wood
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: To play, spot an Elf.
		 * 	While the fellowship is at a battleground site, Haldir is strength +2.
		 * 	While the fellowship is at a forest site, Haldir is an <b>archer</b>.
		 */

		var scn = GetBattlegroundScenario();

		var card = scn.GetFreepsCard("haldir");

		assertEquals("Haldir", card.getBlueprint().getTitle());
		assertEquals("Sentry of the Golden Wood", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void CannotBePlayedUntilAnElfCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBattlegroundScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var finalcount = scn.GetFreepsCard("finalcount");
		// A companion who is no Elf, and an [elven] card that is no Elf.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(finalcount);
		scn.MoveCardsToHand(haldir, arwen);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, finalcount);
		assertFalse(scn.FreepsPlayAvailable(haldir));

		scn.FreepsPlayCard(arwen);
		assertInZone(Zone.FREE_CHARACTERS, arwen);
		assertTrue(scn.FreepsPlayAvailable(haldir));

		scn.FreepsPlayCard(haldir);
		assertInZone(Zone.FREE_CHARACTERS, haldir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAnElfAllyIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBattlegroundScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var finalcount = scn.GetFreepsCard("finalcount");
		// A companion who is no Elf, and an [elven] card that is no Elf.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(finalcount);
		scn.MoveCardsToHand(haldir, celeborn);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, finalcount);
		assertFalse(scn.FreepsPlayAvailable(haldir));

		scn.FreepsPlayCard(celeborn);
		assertInZone(Zone.SUPPORT, celeborn);
		assertTrue(scn.FreepsPlayAvailable(haldir));

		scn.FreepsPlayCard(haldir);
		assertInZone(Zone.FREE_CHARACTERS, haldir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsStrengthPlusTwoOnlyWhileTheFellowshipIsAtABattlegroundSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBattlegroundScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		// Another Elf companion, who goes where Haldir goes.
		scn.MoveCompanionsToTable(haldir, lorienelf);

		scn.StartGame();

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(5, scn.GetStrength(haldir));
		assertEquals(4, scn.GetStrength(lorienelf));

		// The fellowship moves to Uruk Camp.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertEquals(7, scn.GetStrength(haldir));
		assertEquals(4, scn.GetStrength(lorienelf));
		// A battleground that is no forest does not make him an archer.
		assertFalse(scn.HasKeyword(haldir, Keyword.ARCHER));

		// And on to the Barrows of Edoras.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(5, scn.GetStrength(haldir));
		assertEquals(4, scn.GetStrength(lorienelf));

		// White Rocks, a battleground that is no plains.
		scn.SkipToSite(4);
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(7, scn.GetStrength(haldir));
		assertEquals(4, scn.GetStrength(lorienelf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsAnArcherOnlyWhileTheFellowshipIsAtAForestSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetForestScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		// Another Elf companion, who goes where Haldir goes.
		scn.MoveCompanionsToTable(haldir, lorienelf);

		scn.StartGame();

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertFalse(scn.HasKeyword(haldir, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(lorienelf, Keyword.ARCHER));

		// The fellowship moves to Trollshaw Forest.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(haldir, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(lorienelf, Keyword.ARCHER));
		// A forest that is no battleground does not make him stronger.
		assertEquals(5, scn.GetStrength(haldir));

		// And on to Rivendell Waterfall, a forest that is a sanctuary as well.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertTrue(scn.HasKeyword(haldir, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(lorienelf, Keyword.ARCHER));
		assertEquals(5, scn.GetStrength(haldir));

		// Balin's Tomb, an underground site that is no forest.
		scn.SkipToSite(4);
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertFalse(scn.HasKeyword(haldir, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(lorienelf, Keyword.ARCHER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
