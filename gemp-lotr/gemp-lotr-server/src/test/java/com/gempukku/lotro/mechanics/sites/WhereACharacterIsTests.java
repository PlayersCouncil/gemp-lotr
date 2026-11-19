package com.gempukku.lotro.mechanics.sites;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Where a character is, for a text that names his place ("while bearer is at a plains site", "heal that Man if at
 * a plains"): the definition word atSite(...), Filters.atSite (F-0518, engine pass 48).
 *
 * Comprehensive Rules 4.2, "ally": "These home site numbers all refer to adventure paths from the Fellowship block,
 * Tower block, and King block.  Allies have no home sites on adventure paths from the Shadows expansion set onward.
 * ...  Block symbol is a factor in determining an ally's home site.  ...  Each ally in your support area is
 * considered to be at his home site.  When your fellowship is at your ally's home site, that ally is considered to
 * be at the same site as the fellowship."  A companion is with the fellowship.
 *
 * The reading is shown through Rohirrim Mount 11_156, "While bearer is at a plains site, he or she is defender +1",
 * whose definition writes it `bearer,atSite(plains)`: the keyword is the whole reading, with no skirmish needed.
 * Its other text ("At the start of each skirmish involving bearer, each minion skirmishing bearer must exert") is
 * never reached: no minion is played.
 *
 * Built so, beside the rules' words: an ally whose home site has not been laid yet is at no site (his home is
 * ahead of the fellowship, with no site card there to read); and an ally of two homes is at each of them that has
 * been laid, wherever the fellowship is.  Ketura's word on the second is asked in Q-0318.
 *
 * The Towers path here has Westemnet Plains 4_342 (plains, sanctuary; "Each [dunland] Man may not take wounds
 * during the archery phase") as site 3, so that a home of site 3 is a plains.  Site 1 is Eastemnet Gullies 4_325 (no
 * terrain), site 2 Eastfold 4_331 (plains; a Shadow ability needing [isengard] trackers), site 4 White Rocks 4_346
 * (battleground; its Shadow number counts mounted companions), site 6 Caves of Aglarond 4_352 (underground,
 * sanctuary).  No site's ability is used.
 */
public class WhereACharacterIsTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				Cards(),
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site3", "4_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A Shadows path, the fellowship starting at Nurn 17_148 (plains, no text).
	protected VirtualTableScenario GetShadowsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				Cards(),
				VirtualTableScenario.ShadowsSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	private static HashMap<String, String> Cards() {
		return new HashMap<>()
		{{
			put("guard", "5_83");       // Household Guard: [rohan] Man companion; valiant and nothing else
			put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally, home 3T; a regroup ability used only on purpose
			put("guma", "4_277");       // Guma: [rohan] Man ally, home 4T; a fellowship ability used only on purpose
			put("aldor", "4_262");      // Aldor: [rohan] Man ally, homes 3T and 6T; an archery ability that needs a villager
			put("mount", "11_156");     // Rohirrim Mount: on a [rohan] Man; defender +1 while bearer is at a plains site
			put("mount2", "11_156");
			put("mount3", "11_156");
			put("mount4", "11_156");
		}};
	}

	@Test
	public void ACompanionIsAtTheSiteTheFellowshipIsAt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, mount);

		scn.StartGame();
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertFalse(scn.HasKeyword(guard, Keyword.DEFENDER));

		scn.SkipToSite(2);
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertTrue(scn.HasKeyword(guard, Keyword.DEFENDER));

		scn.SkipToSite(4);
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertFalse(scn.HasKeyword(guard, Keyword.DEFENDER));
	}

	@Test
	public void AnAllyIsAtHisHomeSiteWhereverTheFellowshipIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herdsman = scn.GetFreepsCard("herdsman");
		var guma = scn.GetFreepsCard("guma");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		scn.MoveAlliesToTable(herdsman, guma);
		scn.AttachCardsTo(herdsman, mount);
		scn.AttachCardsTo(guma, mount2);

		// At Eastfold, a plains: neither home has been laid, so neither ally is at a site.
		scn.StartGame();
		scn.SkipToSite(2);
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertNull(scn.GetSite(3));
		assertFalse(scn.HasKeyword(herdsman, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(guma, Keyword.DEFENDER));

		// At Westemnet Plains, the Herdsman's home and a plains: he is there with the fellowship.
		scn.SkipToSite(3);
		assertEquals("Westemnet Plains", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(herdsman, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(guma, Keyword.DEFENDER));

		// At White Rocks, Guma's home and a battleground: the Herdsman is still at Westemnet Plains behind the
		// fellowship, and Guma is at White Rocks with it.
		scn.SkipToSite(4);
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertTrue(scn.HasKeyword(herdsman, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(guma, Keyword.DEFENDER));
	}

	@Test
	public void AnAllyOfTwoHomesIsAtEachThatHasBeenLaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aldor = scn.GetFreepsCard("aldor");
		var mount = scn.GetFreepsCard("mount");
		scn.MoveAlliesToTable(aldor);
		scn.AttachCardsTo(aldor, mount);

		scn.StartGame();
		scn.SkipToSite(2);
		assertFalse(scn.HasKeyword(aldor, Keyword.DEFENDER));

		// Site 3, a home of his, is a plains; site 6, the other, is not.
		scn.SkipToSite(3);
		assertTrue(scn.HasKeyword(aldor, Keyword.DEFENDER));

		scn.SkipToSite(6);
		assertEquals("Caves of Aglarond", scn.GetCurrentSite().getBlueprint().getTitle());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertTrue(scn.HasKeyword(aldor, Keyword.DEFENDER));
	}

	@Test
	public void OnAShadowsPathAnAllyIsAtNoSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsScenario();

		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var guma = scn.GetFreepsCard("guma");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		var mount3 = scn.GetFreepsCard("mount3");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(herdsman, guma);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(herdsman, mount2);
		scn.AttachCardsTo(guma, mount3);

		var nurn = scn.GetFreepsSite("site4");
		scn.StartGame(nurn);
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals("Nurn", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));

		// The companion is at the plains; the allies, whose homes are site 3 and site 4 of the Towers path, at no site.
		assertTrue(scn.HasKeyword(guard, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(herdsman, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(guma, Keyword.DEFENDER));
	}
}
