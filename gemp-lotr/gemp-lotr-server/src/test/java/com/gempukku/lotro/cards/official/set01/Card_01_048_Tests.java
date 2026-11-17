package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_048_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haldir", "1_48");

					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, strength 6; her text is about Nazgul only
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] Elf ally, cost 2; an archery ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability only
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: [dwarven] Dwarf ally; a maneuver ability only
					put("shoulder", "1_59");    // Shoulder to Shoulder: [elven] condition in the support area; a maneuver ability only
					put("lorienelf", "1_53");   // Lorien Elf: [elven] Elf companion, laid in the dead pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Tower block: site 5 and 7 are battlegrounds, 6 is underground and a sanctuary, 8 and 9 have no terrain.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haldir", "1_48");
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
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: To play, spot an Elf.<br>While Haldir is at site 6, 7, or 8, he is strength +2.
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
		assertEquals(5, card.getBlueprint().getStrength());
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
		var lorienelf = scn.GetFreepsCard("lorienelf");
		// In play: a companion who is no Elf, and a card of the [elven] culture that is no Elf.  An Elf in the dead
		// pile, and one in hand.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(shoulder);
		scn.MoveCardsToDeadPile(lorienelf);
		scn.MoveCardsToHand(haldir, arwen);

		scn.StartGame();

		assertInZone(Zone.DEAD, lorienelf);
		assertFalse(scn.FreepsPlayAvailable(haldir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(arwen);
		assertInZone(Zone.FREE_CHARACTERS, arwen);
		assertTrue(scn.FreepsPlayAvailable(haldir));

		scn.FreepsPlayCard(haldir);
		assertInZone(Zone.FREE_CHARACTERS, haldir);
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

		assertInZone(Zone.SUPPORT, thrarin);
		assertFalse(scn.FreepsPlayAvailable(haldir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// An Elf to spot need not be a companion.
		scn.FreepsPlayCard(orophin);
		assertInZone(Zone.SUPPORT, orophin);
		assertTrue(scn.FreepsPlayAvailable(haldir));

		scn.FreepsPlayCard(haldir);
		assertInZone(Zone.FREE_CHARACTERS, haldir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsStrengthPlus2AtSites6To8AndNotAtSite5Or9() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(haldir, arwen);

		scn.StartGame();

		// Site 5, The Bridge of Khazad-dum: one short of site 6.
		scn.SkipToSite(5);
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetStrength(haldir));

		// Site 6, Galadriel's Glade.
		scn.SkipToSite(6);
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetStrength(haldir));
		// Another Elf companion at the same site is as printed.
		assertEquals(6, scn.GetStrength(arwen));

		// Site 7, Anduin Confluence.
		scn.SkipToSite(7);
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetStrength(haldir));
		assertEquals(6, scn.GetStrength(arwen));

		// Site 8, Anduin Banks.
		scn.SkipToSite(8);
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetStrength(haldir));
		assertEquals(6, scn.GetStrength(arwen));

		// The fellowship moves to site 9, Emyn Muil: one past site 8.
		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetStrength(haldir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSites6To8OfTheTowerBlockHeIsNotStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(haldir);

		scn.StartGame();

		// Site 6 of the Tower block, Caves of Aglarond.
		scn.SkipToSite(6);
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetStrength(haldir));

		// Site 7, Helm's Tower.
		scn.SkipToSite(7);
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetStrength(haldir));

		// Site 8, Ring of Isengard.
		scn.SkipToSite(8);
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetStrength(haldir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
