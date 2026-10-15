package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_338_Tests
{

	// The Towers path with Golden Hall as site 3.  Site 2 is Eastfold (a Shadow ability that needs 3 [isengard]
	// trackers) and site 4 White Rocks (Shadow number +1 for each mounted companion: there are none).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("sword", "1_112");      // Ranger's Sword: [gondor] hand weapon, a possession; bearer must be Aragorn
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion; an archery ability used only on purpose
					put("bow", "1_41");         // Elven Bow: [elven] ranged weapon, a possession; bearer must be an Elf
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; a skirmish ability only
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: [gondor] hand weapon that is an ARTIFACT; bearer must be a [gondor] Man; a response to a wound on him
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: [shire] Hobbit ally; a fellowship ability used only on purpose
					put("hobbitsword", "1_299");// Hobbit Sword: [shire] hand weapon; bearer must be a Hobbit; no other text
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; a skirmish ability only
					put("armor", "1_8");        // Dwarven Armor: a Free Peoples possession that is no weapon; bearer must be a Dwarf

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc minion; his text answers only a weapon PLAYED on him
					put("banner", "6_98");      // Banner of the Eye: a Shadow hand weapon; bearer must be a [sauron] Orc; a trigger on a skirmish won
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site3", "4_338");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoldenHallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Golden Hall
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 3T
		 * Shadow Number: 1
		 * Direction: Right
		 * Game Text: <b>Sanctuary</b>. When the fellowship moves to Golden Hall, all Free Peoples weapons must be
		 * discarded.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("Golden Hall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(3, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void MovingToGoldenHallDiscardsEveryFreePeoplesWeaponAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var legolas = scn.GetFreepsCard("legolas");
		var bow = scn.GetFreepsCard("bow");
		var boromir = scn.GetFreepsCard("boromir");
		var narsil = scn.GetFreepsCard("narsil");
		var maggot = scn.GetFreepsCard("maggot");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(aragorn, legolas, boromir, gimli);
		scn.MoveAlliesToTable(maggot);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(legolas, bow);
		scn.AttachCardsTo(boromir, narsil);
		scn.AttachCardsTo(maggot, hobbitsword);
		scn.AttachCardsTo(gimli, armor);

		var chieftain = scn.GetShadowCard("chieftain");
		var banner = scn.GetShadowCard("banner");

		scn.StartGame();
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(bow, legolas);
		assertAttachedTo(narsil, boromir);
		assertAttachedTo(hobbitsword, maggot);
		assertAttachedTo(armor, gimli);

		// The move to Eastfold, site 2: nothing is discarded.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(bow, legolas);
		assertAttachedTo(narsil, boromir);
		assertAttachedTo(hobbitsword, maggot);
		assertAttachedTo(armor, gimli);

		// The regroup move to Golden Hall, with a minion bearing a Shadow weapon still in play.
		scn.SkipToMovementDecision();
		scn.MoveMinionsToTable(chieftain);
		scn.AttachCardsTo(chieftain, banner);
		assertAttachedTo(banner, chieftain);
		scn.FreepsChooseToMove();

		assertEquals("Golden Hall", scn.GetCurrentSite().getBlueprint().getTitle());
		// A hand weapon and a ranged weapon, possessions on companions; an artifact weapon; a weapon on an ally.
		assertInZone(Zone.DISCARD, sword, bow, narsil, hobbitsword);
		// Not a Free Peoples possession that is no weapon, nor a Shadow weapon.
		assertAttachedTo(armor, gimli);
		assertAttachedTo(banner, chieftain);
		assertInZone(Zone.FREE_CHARACTERS, aragorn, legolas, boromir, gimli);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WeaponsBorneAtGoldenHallAreKeptAndMovingOnFromItDiscardsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var maggot = scn.GetFreepsCard("maggot");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(maggot);

		scn.StartGame();
		scn.SkipToSite(3);
		assertEquals("Golden Hall", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(maggot, hobbitsword);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(hobbitsword, maggot);

		// The move from Golden Hall to White Rocks, site 4.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(hobbitsword, maggot);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
