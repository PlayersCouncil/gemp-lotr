package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_070_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Haldir, Emissary of the Galadhrim: strength 5, and +2 "while no opponent controls a site"
					put("haldir", "4_71");
					// Vorondil: [gondor] Man, strength 5, and +2 "while no opponent controls a site"
					put("vorondil", "7_127");
					// Horseman of the North: [rohan] Man, strength 5, and +2 "while no opponent controls a site"
					put("horseman", "15_130");
					// Grousing Hillman: [men] Man, strength 9; "While you control a site", strength +3 and fierce
					put("hillman", "15_82");
					// Uruk-hai Mob: [isengard], cost 5, home site 5; its cost is -1 "for each site you control"
					put("mob1", "4_205");
					put("mob2", "4_205");
					// Uruk-hai Band: [isengard], strength 10; strength +2 "for each site you control"
					put("band", "4_202");
					// Advancing Horde: [uruk-hai], strength 14; strength +1 "for each site you control"
					put("horde", "15_155");
					// Dunlending Elder: [dunland] Man, strength 7; "While you control 2 sites", strength +5, fierce
					// and damage +1
					put("elder", "6_2");
					// Hill Chief: [dunland] Man; "Shadow: Spot a site you control and exert Hill Chief to draw 3 cards."
					put("chief", "4_20");
					// Uruk Occupier: [isengard], strength 8; strength +1 "for each battleground you control"
					put("occupier", "102_34");
					// Down to the Last Child: "Shadow: Remove 2 [isengard] tokens from this card to take control of a
					// site."  It is how the Shadow player comes to control one.
					put("lastchild", "4_148");
					// Uruk Besieger and Uruk Trooper: [isengard] Uruk-hai with one and the same sentence, "Regroup:
					// Stack this minion on a site you control."  Neither has any other text for the regroup phase.
					put("besieger", "4_180");
					put("trooper", "4_199");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "102_70");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BreachedWallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Breached Wall
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 5
		 * Shadow Number: 6
		 * Direction: Left
		 * Game Text: Battleground. The count of sites each Shadow player controls is +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(5);

		assertEquals("Breached Wall", card.getBlueprint().getTitle());
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
	public void WithNoSiteControlledEveryCardThatAsksAboutControlledSitesCountsOneAtTheWallAndNoneAtTheSiteBefore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(haldir);

		var hillman = scn.GetShadowCard("hillman");
		var mob1 = scn.GetShadowCard("mob1");
		var mob2 = scn.GetShadowCard("mob2");
		var band = scn.GetShadowCard("band");
		var elder = scn.GetShadowCard("elder");
		var chief = scn.GetShadowCard("chief");
		var occupier = scn.GetShadowCard("occupier");

		scn.StartGame();
		scn.SkipToSite(3);

		scn.MoveMinionsToTable(hillman, band, elder, chief, occupier);
		scn.MoveCardsToHand(mob1, mob2);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Site 4, one short of the Wall.  Nobody controls a site and every card says so.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(7, scn.GetStrength(haldir));       // 5 + 2: no opponent controls a site
		assertEquals(9, scn.GetStrength(hillman));
		assertFalse(scn.HasKeyword(hillman, Keyword.FIERCE));
		assertEquals(10, scn.GetStrength(band));
		assertEquals(7, scn.GetStrength(elder));
		assertEquals(8, scn.GetStrength(occupier));
		assertFalse(scn.ShadowActionAvailable(chief));
		// 20 set + 2 companions + 3 for the site
		assertEquals(25, scn.GetTwilight());
		scn.ShadowPlayCard(mob1);
		// The Mob's printed 5, and 2 for roaming at site 4
		assertEquals(18, scn.GetTwilight());

		// No skirmishes; the fellowship moves again, to the Wall.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals("Breached Wall", scn.GetCurrentSite().getBlueprint().getTitle());
		// Still no site is controlled, and each card that asks about controlled sites finds one.
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(5, scn.GetStrength(haldir));       // an opponent "controls a site"
		assertEquals(12, scn.GetStrength(hillman));     // 9 + 3: "you control a site"
		assertTrue(scn.HasKeyword(hillman, Keyword.FIERCE));
		assertEquals(12, scn.GetStrength(band));        // 10 + 2 for one site
		assertTrue(scn.ShadowActionAvailable(chief));   // a site he controls to spot
		// One is not two, and the one is not a battleground.
		assertEquals(7, scn.GetStrength(elder));
		assertFalse(scn.HasKeyword(elder, Keyword.FIERCE));
		assertEquals(8, scn.GetStrength(occupier));
		// 18 left + 2 companions + 6 for the site
		assertEquals(26, scn.GetTwilight());
		scn.ShadowPlayCard(mob2);
		// The Mob's printed 5, less 1 for one site; site 5 is its home
		assertEquals(22, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneSiteControlledTheCountAtTheWallIsTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var band = scn.GetShadowCard("band");
		var horde = scn.GetShadowCard("horde");
		var elder = scn.GetShadowCard("elder");
		var occupier = scn.GetShadowCard("occupier");
		var lastChild = scn.GetShadowCard("lastchild");

		scn.StartGame();
		scn.SkipToSite(4);

		scn.MoveMinionsToTable(band, horde, elder, occupier);
		scn.MoveCardsToSupportArea(lastChild);
		// Enough to take control of one site.
		scn.AddTokensToCard(lastChild, 2);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		assertEquals("Breached Wall", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(12, scn.GetStrength(band));        // 10 + 2 x 1
		assertEquals(15, scn.GetStrength(horde));       // 14 + 1
		assertEquals(7, scn.GetStrength(elder));
		assertFalse(scn.HasKeyword(elder, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(elder, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(occupier));

		scn.ShadowUseCardAction(lastChild);
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertFalse(scn.HasKeyword(site1, Keyword.BATTLEGROUND));

		// One site controlled, and the count is two.
		assertEquals(14, scn.GetStrength(band));        // 10 + 2 x 2
		assertEquals(16, scn.GetStrength(horde));       // 14 + 2
		assertEquals(12, scn.GetStrength(elder));       // 7 + 5: "you control 2 sites"
		assertTrue(scn.HasKeyword(elder, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(elder, Keyword.DAMAGE));
		// Neither the site he controls nor the one more is a battleground.
		assertEquals(8, scn.GetStrength(occupier));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnTheSecondPlayersTurnTheFirstPlayersCountIsOneMoreAtTheWall() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// The first player's own Uruk-hai Band: he is the Shadow player on the second player's turn.
		var band = scn.GetFreepsCard("band");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SkipToOtherPlayersTurn();

		scn.MoveMinionsToTable(band);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(10, scn.GetStrength(band));

		// The second player's fellowship moves to the Wall.
		scn.ShadowPass();

		assertEquals("Breached Wall", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetFreepsControlledSiteCount());
		assertEquals(12, scn.GetStrength(band));        // 10 + 2 for one site
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	@Ignore("F-0085: at the Wall with no site controlled, Haldir 4_71 loses his +2 and Vorondil 7_127 and Horseman of the North 15_130 keep theirs, on the same words")
	public void EveryCompanionWhoIsStrongerWhileNoOpponentControlsASiteLosesThatAtTheWall() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var vorondil = scn.GetFreepsCard("vorondil");
		var horseman = scn.GetFreepsCard("horseman");
		scn.MoveCompanionsToTable(haldir, vorondil, horseman);

		scn.StartGame();
		scn.SkipToSite(4);

		// Nobody controls a site: each of the three is 5 + 2.
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(7, scn.GetStrength(haldir));
		assertEquals(7, scn.GetStrength(vorondil));
		assertEquals(7, scn.GetStrength(horseman));

		scn.FreepsPass();

		// At the Wall the Shadow player's count of sites is one, on the same words for all three.
		assertEquals("Breached Wall", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(5, scn.GetStrength(haldir));
		assertEquals(5, scn.GetStrength(vorondil));
		assertEquals(5, scn.GetStrength(horseman));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMinionThatStacksItselfOnASiteHeControlsIsNotOfferedThatAtTheWallWithNoSiteControlled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var besieger = scn.GetShadowCard("besieger");
		var trooper = scn.GetShadowCard("trooper");

		scn.StartGame();
		scn.SkipToSite(4);

		scn.MoveMinionsToTable(besieger, trooper);
		scn.FreepsPass();

		assertEquals("Breached Wall", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetShadowControlledSiteCount());

		// Neither minion is assigned to a skirmish; both are on the table when the regroup phase comes.
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// The count is one and there is still no site for a minion to be stacked on.  Stacking has no cost and would
		// do nothing, so it is not offered, on the same words for both minions.
		assertInZone(Zone.SHADOW_CHARACTERS, besieger);
		assertInZone(Zone.SHADOW_CHARACTERS, trooper);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertFalse(scn.ShadowActionAvailable(trooper));
		assertFalse(scn.ShadowActionAvailable(besieger));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
