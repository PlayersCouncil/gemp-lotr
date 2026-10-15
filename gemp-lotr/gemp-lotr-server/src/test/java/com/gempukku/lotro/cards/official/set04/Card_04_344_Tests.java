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

public class Card_04_344_Tests
{

	// The Towers path with Westemnet Hills as site 4.  Site 2 is Eastfold (plains; a Shadow ability that needs 3
	// [isengard] trackers), site 3 Barrows of Edoras (a sanctuary; a fellowship ability used only on purpose) and
	// site 5 Deeping Wall (a Shadow ability that plays Saruman).  No site up to 5 is underground, where Asfaloth
	// and the Black Steed would be discarded.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, valiant and nothing else
					put("guard2", "5_83");
					put("mount", "4_287");      // Rider's Mount: a [rohan] mount a [rohan] Man bears; text only at the start of his skirmish
					put("sword", "4_291");      // Sword of Rohan: a [rohan] possession that is no mount; text only against an Uruk-hai
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability used only on purpose
					put("horse", "4_283");      // Horse of Rohan: a mount a Man, Elf or Wizard bears; text only at the start of his skirmish
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: an Elf ally, vitality 3; a skirmish ability used only on purpose
					put("asfaloth", "1_31");    // Asfaloth: an [elven] mount an Elf bears; strength +2 at a plains site
					put("saelbeth", "3_25");    // Saelbeth, Elven Councilor: an Elf ally, vitality 3; a skirmish ability used only on purpose
					put("otsea", "1_235");      // Ulaire Otsea, Lieutenant of Morgul: a Nazgul minion, vitality 3; a Shadow ability used only on purpose
					put("steed", "1_208");      // Black Steed: a mount a Nazgul bears; strength +2 at a plains site
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site4", "4_344");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WestemnetHillsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Westemnet Hills
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 4
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: <b>Plains</b>. When the fellowship moves to Westemnet Hills, heal each mounted companion and
		 * 	mounted ally.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("Westemnet Hills", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void MovingToWestemnetHillsHealsEachMountedCompanionAndAllyAndNobodyElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hills = scn.GetShadowSite(4);
		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var mount = scn.GetFreepsCard("mount");
		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var horse = scn.GetFreepsCard("horse");
		var golradir = scn.GetFreepsCard("golradir");
		var asfaloth = scn.GetFreepsCard("asfaloth");
		var saelbeth = scn.GetFreepsCard("saelbeth");
		var otsea = scn.GetShadowCard("otsea");
		var steed = scn.GetShadowCard("steed");
		scn.MoveCompanionsToTable(guard, guard2, aragorn);
		scn.MoveAlliesToTable(golradir, saelbeth);
		scn.AttachCardsTo(guard, mount);
		// A companion bearing a possession that is no mount: not mounted.
		scn.AttachCardsTo(guard2, sword);
		scn.AttachCardsTo(aragorn, horse);
		scn.AttachCardsTo(golradir, asfaloth);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(guard, 2);
		scn.AddWoundsToChar(guard2, 2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(golradir, 2);
		scn.AddWoundsToChar(saelbeth, 2);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ATTACHED, mount, sword, horse, asfaloth);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(guard2));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(golradir));
		assertEquals(2, scn.GetWoundsOn(saelbeth));

		// The move to site 3, another site, heals nobody.
		scn.FreepsPass();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(guard2));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(golradir));
		assertEquals(2, scn.GetWoundsOn(saelbeth));

		// A mounted minion, on the table when the fellowship moves again in the regroup phase. Nobody is assigned
		// to it.
		scn.MoveMinionsToTable(otsea);
		scn.AttachCardsTo(otsea, steed);
		scn.AddWoundsToChar(otsea, 1);
		scn.SkipToMovementDecision();

		assertEquals(1, scn.GetWoundsOn(otsea));
		assertInZone(Zone.ATTACHED, steed);
		scn.FreepsChooseToMove();

		assertSame(hills, scn.GetCurrentSite());
		// The mounted companions, of two cultures and two mounts, and the mounted ally: one wound healed from each.
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(golradir));
		// A companion bearing a possession that is no mount, the Ring-bearer, an ally who is not mounted and a
		// mounted minion: none healed.
		assertEquals(2, scn.GetWoundsOn(guard2));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(saelbeth));
		assertEquals(1, scn.GetWoundsOn(otsea));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingOnFromWestemnetHillsHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hills = scn.GetShadowSite(4);
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var golradir = scn.GetFreepsCard("golradir");
		var asfaloth = scn.GetFreepsCard("asfaloth");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(golradir);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(golradir, asfaloth);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddWoundsToChar(guard, 2);
		scn.AddWoundsToChar(golradir, 2);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ATTACHED, mount, asfaloth);
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(golradir));

		scn.FreepsPass();

		assertSame(hills, scn.GetCurrentSite());
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(golradir));

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		// At Deeping Wall: moving from Westemnet Hills heals nobody.
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ATTACHED, mount, asfaloth);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(golradir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
