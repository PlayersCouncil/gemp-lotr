package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_92_026_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession, borne by a Hobbit
					put("map", "1_318");        // Thror's Map: a [shire] possession that lies in the support area
					put("armor", "1_8");        // Dwarven Armor: a [dwarven] possession, borne by a Dwarf
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion
					put("coat", "13_153");      // Mithril-coat, Dwarf-mail: a [shire] artifact, borne by a Ring-bound Hobbit
					put("harp", "18_3");        // Thorin's Harp: a [dwarven] condition in the support area
					put("poison", "8_28");      // Spider Poison: a Shadow possession, in the Shadow player's support area
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_26", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession, borne by a Hobbit
					put("map", "1_318");        // Thror's Map: a [shire] possession that lies in the support area
					put("armor", "1_8");        // Dwarven Armor: a [dwarven] possession, borne by a Dwarf
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion
					put("coat", "13_153");      // Mithril-coat, Dwarf-mail: a [shire] artifact, borne by a Ring-bound Hobbit
					put("harp", "18_3");        // Thorin's Harp: a [dwarven] condition in the support area
					put("poison", "8_28");      // Spider Poison: a Shadow possession, in the Shadow player's support area
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_26"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_26
		 * Type: MetaSite
		 * Intensity: 4
		 * Game Text: Each time your fellowship moves, add (1) for each Free Peoples possession.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_26", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(4, card.getBlueprint().getIntensity());
	}

	@Test
	public void AddsTwilightForEachPossessionOnMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var sword = scn.GetFreepsCard("sword");
		var map = scn.GetFreepsCard("map");

		scn.AttachCardsTo(scn.GetRingBearer(), sword);
		scn.MoveCardsToSupportArea(map);

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());

		// Move to site 2
		scn.FreepsPass();

		int twilightAfterMove = scn.GetTwilight();
		int site2Shadow = scn.GetCurrentSite().getBlueprint().getTwilightCost();

		// Site shadow + 1 companion (Frodo) + 2 possessions (Ettenmoors has a Shadow number of 2: 5 in all)
		assertEquals(2, site2Shadow);
		assertEquals(site2Shadow + 1 + 2, twilightAfterMove);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NoPossessionsAddsNoExtraTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();

		int twilightAfterMove = scn.GetTwilight();
		int site2Shadow = scn.GetCurrentSite().getBlueprint().getTwilightCost();

		// Site shadow + 1 companion (Frodo), no possessions
		assertEquals(2, site2Shadow);
		assertEquals(site2Shadow + 1, twilightAfterMove);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OpponentNotAffected() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var sword = scn.GetFreepsCard("sword");
		var map = scn.GetFreepsCard("map");

		scn.AttachCardsTo(scn.GetRingBearer(), sword);
		scn.MoveCardsToSupportArea(map);

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();

		int twilightAfterMove = scn.GetTwilight();
		int site2Shadow = scn.GetCurrentSite().getBlueprint().getTwilightCost();

		// Shadow owns the modifier, so no extra twilight from possessions
		assertEquals(2, site2Shadow);
		assertEquals(site2Shadow + 1, twilightAfterMove);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CountsFreePeoplesPossessionsOfAnyCultureAndPlaceAndNothingElseAtEachMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		var map = scn.GetFreepsCard("map");
		var armor = scn.GetFreepsCard("armor");
		var coat = scn.GetFreepsCard("coat");
		var harp = scn.GetFreepsCard("harp");
		var poison = scn.GetShadowCard("poison");

		scn.MoveCompanionsToTable(gimli);
		// Three Free Peoples possessions: two [shire], one [dwarven]; two borne, one in the support area
		scn.AttachCardsTo(frodo, sword);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveCardsToSupportArea(map);
		// Not counted: a Free Peoples artifact, a Free Peoples condition, and a Shadow possession
		scn.AttachCardsTo(frodo, coat);
		scn.MoveCardsToSupportArea(harp, poison);

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		// 2 for Ettenmoors + 2 companions + 3 possessions
		assertEquals(7, scn.GetTwilight());

		scn.SkipToMovementDecision();
		// Thror's Map is gone before the second move
		scn.MoveCardsToDiscard(map);
		int twilightBeforeSecondMove = scn.GetTwilight();

		scn.FreepsChooseToMove();
		// Rivendell Valley has a Shadow number of 0: 2 companions + 2 possessions
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(twilightBeforeSecondMove + 2 + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
