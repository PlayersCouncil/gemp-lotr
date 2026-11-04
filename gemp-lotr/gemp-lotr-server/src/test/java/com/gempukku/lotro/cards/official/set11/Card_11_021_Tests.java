package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_021_Tests
{

	// The Towers path: site 1 (Eastemnet Gullies) and site 3 (Barrows of Edoras, a sanctuary) are neither battleground
	// nor forest. Site 2 is Uruk Camp 4_335 (Battleground, Plains; exerts each Uruk-hai played, and none is), and site 4
	// White Rocks 4_346 (Battleground alone; its Shadow number counts mounted companions, and none is).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "11_21");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, resistance 6; a skirmish ability never used here
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with two forests: Trollshaw Forest 1_334 at site 2 (Forest alone; draws when a possession
	// or artifact is played on a companion, and none is) and Rivendell Waterfall 1_342 at site 3 (Forest, Sanctuary;
	// a move limit bonus while a ranger is there, and none is). Bag End at site 1 is neither.
	protected VirtualTableScenario GetForestScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "11_21");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, resistance 6; a skirmish ability never used here
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
	public void LegolasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Legolas, Companion of the Ring
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: While Legolas is at a battleground or forest site, he is strength +2 and resistance +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legolas");

		assertEquals("Legolas", card.getBlueprint().getTitle());
		assertEquals("Companion of the Ring", card.getBlueprint().getSubtitle());
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
	public void IsStrengthPlus2AndResistancePlus2OnlyWhileAtABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(legolas, gimli);

		scn.StartGame();

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(7, scn.GetResistance(legolas));

		// The fellowship moves to Uruk Camp.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertEquals(8, scn.GetStrength(legolas));
		assertEquals(9, scn.GetResistance(legolas));
		// Gimli, at the same site, is not touched.
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetResistance(gimli));

		// And on to the Barrows of Edoras, a sanctuary that is neither.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(7, scn.GetResistance(legolas));

		// White Rocks, a battleground that is no plains.
		scn.SkipToSite(4);
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(8, scn.GetStrength(legolas));
		assertEquals(9, scn.GetResistance(legolas));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetResistance(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsStrengthPlus2AndResistancePlus2AtEachForest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetForestScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(legolas, gimli);

		scn.StartGame();

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(7, scn.GetResistance(legolas));

		// The fellowship moves to Trollshaw Forest.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(8, scn.GetStrength(legolas));
		assertEquals(9, scn.GetResistance(legolas));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetResistance(gimli));

		// And on to Rivendell Waterfall, a forest that is a sanctuary as well.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertEquals(8, scn.GetStrength(legolas));
		assertEquals(9, scn.GetResistance(legolas));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetResistance(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
