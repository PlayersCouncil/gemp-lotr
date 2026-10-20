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
					// Uruk Besieger and Uruk Trooper: [isengard] Uruk-hai with one and the same sentence, "Regroup:
					// Stack this minion on a site you control."  Neither has any other text for the regroup phase.
					put("besieger", "4_180");
					put("trooper", "4_199");
					// Sturdy Shield: borne by a [rohan] Man; "While no opponent controls a site, bearer is strength +1."
					put("shield", "15_141");
					// Household Guard: a [rohan] Man, strength 6, vitality 3; valiant and nothing else
					put("guard", "5_83");
					// Gondor Still Stands: "While no opponent controls a site, wound a minion at the start of each
					// regroup phase."
					put("stands", "7_95");
					// White Hand Intruder: [uruk-hai], strength 9, damage +1; "Each time this minion wins a skirmish,
					// if you do not control a site, you may take control of a site."
					put("intruder", "17_127");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3
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

		scn.StartGame();
		scn.SkipToSite(4);

		scn.MoveMinionsToTable(band, horde, elder, occupier);
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

		scn.ShadowTakeControlOfSite();
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
	public void AShieldAndAConditionThatWorkWhileNoOpponentControlsASiteStopAtTheWall() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var shield = scn.GetFreepsCard("shield");
		var stands = scn.GetFreepsCard("stands");
		var band = scn.GetShadowCard("band");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.SkipToSite(3);

		scn.AttachCardsTo(guard, shield);
		scn.MoveCardsToSupportArea(stands);
		scn.MoveMinionsToTable(band);
		scn.FreepsPass();

		// Site 4, one short of the Wall: nobody controls a site.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(7, scn.GetStrength(guard));        // 6 + 1 for the Shield
		assertEquals(0, scn.GetWoundsOn(band));

		// The Band is assigned to nobody.  At the start of the regroup phase the condition wounds it.
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(1, scn.GetWoundsOn(band));

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		// At the Wall the Shadow player's count of sites is one, and still no site is controlled.
		assertEquals("Breached Wall", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(6, scn.GetStrength(guard));

		scn.SkipToPhase(Phase.REGROUP);
		// No second wound: the Band has 3 vitality and is still there to take one.
		assertInZone(Zone.SHADOW_CHARACTERS, band);
		assertEquals(1, scn.GetWoundsOn(band));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMinionThatTakesASiteIfItsOwnerControlsNoneIsNotOfferedThatAtTheWall() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var intruder = scn.GetShadowCard("intruder");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.SkipToSite(3);

		scn.MoveMinionsToTable(intruder);
		scn.FreepsPass();

		// Site 4, one short of the Wall.  The Intruder (9) beats Aragorn (8), and with no site controlled its
		// owner may take one.
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, intruder);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));      // 1 for losing + 1 for damage +1
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(intruder));
		// Declined, so that he still controls no site at the Wall.
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals("Breached Wall", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(gimli, intruder);
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassCurrentPhaseActions();

		// The Intruder (9) beats Gimli (6) and nothing is offered: at the Wall its owner's count of sites is one.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(intruder));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
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

	@Test
	public void OnceTheFellowshipHasLeftTheWallNoCardCountsASiteThatNobodyControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(haldir);

		var band = scn.GetShadowCard("band");
		var chief = scn.GetShadowCard("chief");

		scn.StartGame();
		scn.SkipToSite(4);

		scn.MoveMinionsToTable(band, chief);
		scn.FreepsPass();

		// At the Wall nobody controls a site and each card that asks finds one.
		var wall = scn.GetCurrentSite();
		assertEquals("Breached Wall", wall.getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(5, scn.GetStrength(haldir));       // an opponent "controls a site"
		assertEquals(12, scn.GetStrength(band));        // 10 + 2 for one site
		assertTrue(scn.ShadowActionAvailable(chief));   // a site he controls to spot

		// Nobody is assigned.  The fellowship moves again, to site 6, and the Wall lies behind it.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertSame(wall, scn.GetSite(5));
		assertTrue(scn.AwaitingShadowPhaseActions());
		// The same cards on the same table.  The text of a site binds only while the fellowship is at it: the
		// count is none again.
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertInZone(Zone.SHADOW_CHARACTERS, band, chief);
		assertEquals(7, scn.GetStrength(haldir));       // 5 + 2: no opponent controls a site
		assertEquals(10, scn.GetStrength(band));
		assertFalse(scn.ShadowActionAvailable(chief));
	}
}
