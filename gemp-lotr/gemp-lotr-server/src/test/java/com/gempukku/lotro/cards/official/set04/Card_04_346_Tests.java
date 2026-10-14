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

public class Card_04_346_Tests
{

	// The Towers path, White Rocks its site 4.  Site 3 is Barrows of Edoras (Shadow number 0, a sanctuary; a
	// fellowship ability used only on purpose) and site 5 Deeping Wall (Shadow number 7).  No site up to 5 is
	// underground, where Asfaloth and the Black Steed would be discarded.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, valiant and nothing else
					put("mount", "4_287");      // Rider's Mount: a mount a [rohan] Man bears; text only at the start of his skirmish
					put("mount2", "4_287");
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: an [elven] Elf companion; text only against a Nazgul
					put("asfaloth", "1_31");    // Asfaloth: a mount an Elf bears; strength +2 at a plains site
					put("sam", "4_316");        // Sam, Samwise the Brave: a Ring-bound [shire] Hobbit companion; two responses used only on purpose
					put("bill", "13_143");      // Bill the Pony, Dearly-loved: a mount a Ring-bound Hobbit bears; a regroup ability used only on purpose
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ally, home 4T; an ability used only on purpose
					put("lemenya", "1_232");    // Ulaire Lemenya: a Nazgul minion; text only about [wraith] conditions
					put("steed", "1_208");      // Black Steed: a mount a Nazgul bears; strength +2 at a plains site
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion; a skirmish ability used only on purpose
					put("armor", "1_8");        // Dwarven Armor: a possession that is no mount, borne by a Dwarf, and no number changed
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteRocksStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: White Rocks
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 4
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: <b>Battleground</b>. The Shadow number of this site is +1 for each mounted companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("White Rocks", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
	}

	@Test
	public void TheMoveToWhiteRocksAddsOneMoreForEachMountedCompanionAndNoneForAMountedAllyOrMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rocks = scn.GetShadowSite(4);
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		var legolas = scn.GetFreepsCard("legolas");
		var asfaloth = scn.GetFreepsCard("asfaloth");
		var sam = scn.GetFreepsCard("sam");
		var bill = scn.GetFreepsCard("bill");
		var guma = scn.GetFreepsCard("guma");
		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(guard, legolas, sam, gimli);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guard, mount);
		// A companion bearing a possession that is no mount: not mounted.
		scn.AttachCardsTo(gimli, armor);
		scn.AttachCardsTo(legolas, asfaloth);
		scn.AttachCardsTo(sam, bill);
		scn.AttachCardsTo(guma, mount2);

		scn.StartGame();
		scn.SkipToSite(3);
		// A mounted minion, laid early (it would be on the table in the Shadow phase all the same).
		scn.MoveMinionsToTable(lemenya);
		scn.AttachCardsTo(lemenya, steed);
		scn.SetTwilight(10);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ATTACHED, mount, asfaloth, bill, mount2, steed, armor);
		scn.FreepsPass();

		assertSame(rocks, scn.GetCurrentSite());
		// Three mounted companions: the Rohan Man, the Elf and the Ring-bound Hobbit; not the ally, not the minion,
		// not the Dwarf who bears armor.
		assertEquals(6, scn.GetTwilightCost(rocks));
		// 10 set + 5 companions + 3 and 3 for White Rocks
		assertEquals(21, scn.GetTwilight());
		// Barrows of Edoras, site 3, keeps its own number.
		assertEquals(0, scn.GetTwilightCost(scn.GetSite(3)));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMountPlayedOnACompanionAtWhiteRocksRaisesItsShadowNumberByOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rocks = scn.GetShadowSite(4);
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var legolas = scn.GetFreepsCard("legolas");
		var asfaloth = scn.GetFreepsCard("asfaloth");
		scn.MoveCompanionsToTable(guard, legolas);
		scn.AttachCardsTo(legolas, asfaloth);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(mount);

		assertSame(rocks, scn.GetCurrentSite());
		assertInZone(Zone.ATTACHED, asfaloth);
		// One mounted companion.
		assertEquals(4, scn.GetTwilightCost(rocks));

		scn.FreepsPlayCard(mount);

		assertInZone(Zone.ATTACHED, mount);
		assertEquals(5, scn.GetTwilightCost(rocks));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasMovedOnFromWhiteRocksItsNumberIsThreeAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rocks = scn.GetShadowSite(4);
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var legolas = scn.GetFreepsCard("legolas");
		var asfaloth = scn.GetFreepsCard("asfaloth");
		scn.MoveCompanionsToTable(guard, legolas);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(legolas, asfaloth);

		scn.StartGame();
		scn.SkipToSite(4);

		assertSame(rocks, scn.GetCurrentSite());
		assertEquals(5, scn.GetTwilightCost(rocks));

		scn.SkipToSite(5);

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ATTACHED, mount, asfaloth);
		assertEquals(3, scn.GetTwilightCost(rocks));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
