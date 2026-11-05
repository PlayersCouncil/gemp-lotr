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

public class Card_04_349_Tests
{

	// The Towers path with Helm's Gate as site 5.  Site 4 is White Rocks (its Shadow number counts mounted
	// companions; there are none) and site 6 Caves of Aglarond (a maneuver ability used only on purpose).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					put("rangersword", "1_112");// Ranger's Sword: a POSSESSION hand weapon, strength +2, on Aragorn
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion, strength 7; a skirmish ability used only on purpose
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: an ARTIFACT hand weapon, no strength of its own (he is no Elendil or Isildur)
					put("gimli", "1_13");       // Gimli, Son of Glóin: a [dwarven] Dwarf companion, strength 6; a skirmish ability used only on purpose
					put("axe", "1_9");          // Dwarven Axe: a POSSESSION hand weapon, strength +2, a Dwarf bears; text only when a minion loses to bearer
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf companion, strength 6
					put("bow", "1_41");         // Elven Bow: a RANGED weapon, no strength of its own
					put("merchant", "7_97");    // Gondorian Merchant: a [gondor] Man ALLY, strength 6; a maneuver ability used only on purpose
					put("gondorsword", "4_134");// Sword of Gondor: a hand weapon a [gondor] Man bears, strength +2
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai minion, strength 5; damage +1 is its whole text
					put("uruksword", "1_160");  // Uruk-hai Sword: a hand weapon an Uruk-hai bears, strength +2; a trigger on a skirmish won
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc minion, strength 13, no text
					put("scimitar", "1_180");   // Goblin Scimitar: a hand weapon a [moria] Orc bears, strength +2; a trigger on its own play
					put("otsea", "1_235");      // Ulaire Otsea, Lieutenant of Morgul: a Nazgul minion, strength 9; a Shadow ability used only on purpose
					put("steed", "1_208");      // Black Steed: a mount, strength +2 (and +2 more at a plains site, which Helm's Gate is not)
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site5", "4_349");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HelmsGateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Helm's Gate
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 5
		 * Shadow Number: 6
		 * Direction: Left
		 * Game Text: <b>Battleground</b>. Each companion and minion bearing a hand weapon is strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(5);

		assertEquals("Helm's Gate", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(5, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
	}

	@Test
	public void EachCompanionAndMinionBearingAHandWeaponIsStrengthPlusTwoThereAndNoOtherCharacterIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate = scn.GetShadowSite(5);
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		var boromir = scn.GetFreepsCard("boromir");
		var narsil = scn.GetFreepsCard("narsil");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var legolas = scn.GetFreepsCard("legolas");
		var bow = scn.GetFreepsCard("bow");
		var merchant = scn.GetFreepsCard("merchant");
		var gondorsword = scn.GetFreepsCard("gondorsword");
		var savage = scn.GetShadowCard("savage");
		var uruksword = scn.GetShadowCard("uruksword");
		var troop = scn.GetShadowCard("troop");
		var scimitar = scn.GetShadowCard("scimitar");
		var otsea = scn.GetShadowCard("otsea");
		var steed = scn.GetShadowCard("steed");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, legolas);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(aragorn, rangersword);
		scn.AttachCardsTo(boromir, narsil);
		scn.AttachCardsTo(gimli, axe);
		scn.AttachCardsTo(legolas, bow);
		scn.AttachCardsTo(merchant, gondorsword);

		scn.StartGame();
		scn.SkipToSite(4);

		// At White Rocks.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(10, scn.GetStrength(aragorn));    // 8 + 2 for the sword
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(8, scn.GetStrength(gimli));       // 6 + 2 for the axe
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(8, scn.GetStrength(merchant));    // 6 + 2 for the sword
		assertEquals(4, scn.GetStrength(frodo));       // 3 + 1 for The Ruling Ring

		scn.FreepsPass();

		assertSame(gate, scn.GetCurrentSite());
		scn.MoveMinionsToTable(savage, troop, otsea);
		scn.AttachCardsTo(savage, uruksword);
		scn.AttachCardsTo(troop, scimitar);
		scn.AttachCardsTo(otsea, steed);

		// Companions of three cultures and races with a possession and with an artifact hand weapon, and minions
		// of two cultures and races with theirs.
		assertEquals(12, scn.GetStrength(aragorn));    // 8 + 2 + 2
		assertEquals(9, scn.GetStrength(boromir));     // 7 + 2
		assertEquals(10, scn.GetStrength(gimli));      // 6 + 2 for the axe + 2
		assertEquals(9, scn.GetStrength(savage));      // 5 + 2 for the sword + 2
		assertEquals(17, scn.GetStrength(troop));      // 13 + 2 for the scimitar + 2
		// A companion with a ranged weapon, the Ring-bearer with the Ring (an item that is no weapon), an ally with a
		// hand weapon, and a minion bearing a possession that is no weapon.
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(merchant));
		assertEquals(11, scn.GetStrength(otsea));      // 9 + 2 for the steed
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasMovedOnFromHelmsGateTheBonusIsGone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate = scn.GetShadowSite(5);
		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		var savage = scn.GetShadowCard("savage");
		var uruksword = scn.GetShadowCard("uruksword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, rangersword);

		scn.StartGame();
		scn.SkipToSite(4);
		assertInZone(Zone.ATTACHED, rangersword);
		scn.FreepsPass();
		// The minion stays on the table when the fellowship moves on in the regroup phase. Nobody is assigned to it.
		scn.MoveMinionsToTable(savage);
		scn.AttachCardsTo(savage, uruksword);

		assertSame(gate, scn.GetCurrentSite());
		assertInZone(Zone.ATTACHED, rangersword, uruksword);
		assertEquals(12, scn.GetStrength(aragorn));    // 8 + 2 + 2
		assertEquals(9, scn.GetStrength(savage));      // 5 + 2 + 2

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals("Caves of Aglarond", scn.GetCurrentSite().getBlueprint().getTitle());
		assertInZone(Zone.ATTACHED, rangersword, uruksword);
		assertEquals(10, scn.GetStrength(aragorn));    // 8 + 2 for the sword
		assertEquals(7, scn.GetStrength(savage));      // 5 + 2 for the sword
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
