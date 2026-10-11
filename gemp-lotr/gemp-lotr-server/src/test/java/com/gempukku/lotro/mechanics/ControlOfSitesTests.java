package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Control of sites, and their liberation.
 *
 * Comprehensive Rules 4.2, "control": "Shadow players may use effects to take control of sites on the adventure
 * path.  When you take control of a site, place that site in your support area, lengthwise.  A controlled site,
 * once placed in a player's support area, is no longer a site on the adventure path.
 *
 * A Shadow player may only take control of a site on the adventure path if all player markers are on sites with
 * higher site numbers."  Its example: "When all player markers are at site 2 or higher, you may take control of
 * site 1."
 *
 * "When a Shadow player takes control of a site, the site on the adventure path with the lowest site number must
 * be selected."  Its example: "An opponent controls site 1 and all player markers are at site 3 or higher, so you
 * must take control of site 2."
 *
 * Legacy Ruling 3, in the same entry: "The non-keyword game text of a controlled site does not apply unless that
 * game text mentions that site being controlled.  The keywords of a controlled site continue to apply."  And: "A
 * player may 'control a battleground.'"
 *
 * "When your opponent controls a site from your adventure deck, it's still your site.  Some cards can replace a
 * controlled site.  Move any cards stacked on the site being replaced to the new site, then return the old site to
 * its owner's adventure deck."
 *
 * "Liberate": "Only a site that another player controls may be liberated.  You cannot liberate a site on the
 * adventure path, a site in any adventure deck, or a site you control.  When you liberate a site, place it back on
 * the adventure path.  Any cards on that site are discarded.  You must liberate the site with the highest site
 * number controlled by one of your opponents first ...  (If you control the site with the highest site number, you
 * must liberate the next highest site controlled by an opponent.)"
 *
 * Each player has sat in the Shadow seat by the time a site can be controlled, so either may control one: where
 * a test wants a site in a player's hands before it begins, the rig's cheat puts it there.
 */
public class ControlOfSitesTests
{
	protected VirtualTableScenario GetScenario(HashMap<String, String> sites) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Théoden, Son of Thengel: strength 6, vitality 2.  "Regroup: Spot a [rohan] ally and
					// exert Théoden to liberate a site."
					put("theoden", "4_292");
					// Guma, Plains Farmer: [rohan] ally.  He is discarded if an opponent controls his home,
					// site 4 of the Towers block, and has a fellowship ability, not used here.
					put("guma", "4_277");

					// Southron Explorer: strength 5, vitality 3.  "Regroup: Exert this minion and remove (5)
					// to take control of a site."
					put("explorer1", "4_250");
					put("explorer2", "4_250");
					// Uruk-hai Band: strength 10.  "This minion is strength +2 for each site you control."
					put("band", "4_202");
					// Advance Uruk Patrol: strength 10.  "While at a battleground, this minion is fierce.
					// While you control a battleground, this minion is strength +4."
					put("patrol", "4_136");
					// Uruk Savage: an Uruk-hai, cost 2, vitality 3; Damage +1 is its whole text
					put("savage", "1_151");
					// Uruk Besieger: "Regroup: Stack this minion on a site you control."
					put("besieger", "4_180");
					// No Retreat: [dunland] condition.  "Plays on a site you control."  Its regroup ability
					// needs 2 [dunland] Men.
					put("retreat", "4_30");
					// Úlairë Nelya, Lieutenant of Morgul: vitality 3.  "Shadow: Exert Úlairë Nelya and spot an
					// opponent's site to replace it with your site of the same number."
					put("nelya", "1_233");
				}},
				sites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// No site of this path is a battleground, and none does anything to a fellowship that only passes by.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(VirtualTableScenario.FellowshipSites);
	}

	// Site 2 is Uruk Camp: "Battleground.  Plains.  Each time an Uruk-hai is played, that minion must exert."
	// Site 1, Eastemnet Gullies, has no keyword, and site 4 is Westemnet Hills, a plains and no battleground
	// (it heals mounted companions on arrival, and nobody is mounted).
	protected VirtualTableScenario GetBattlegroundScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(new HashMap<>()
		{{
			putAll(VirtualTableScenario.TowersSites);
			put("site2", "4_335");
			put("site4", "4_344");
		}});
	}

	@Test
	public void AShadowPlayerTakesControlOfASiteEveryMarkerHasPassedAndItLiesInHisSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var explorer = scn.GetShadowCard("explorer1");
		var band = scn.GetShadowCard("band");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.MoveMinionsToTable(explorer, band);
		// The fellowship moves on to site 3; the other player's marker is at site 2.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(10);

		assertEquals(3, scn.FreepsGetSiteNumber());
		assertEquals(2, scn.ShadowGetSiteNumber());
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(10, scn.GetStrength(band));

		// "When all player markers are at site 2 or higher, you may take control of site 1."
		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, site1);
		assertNotInZone(Zone.ADVENTURE_PATH, site1);
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetFreepsControlledSiteCount());
		// "Each site you control" counts it.
		assertEquals(12, scn.GetStrength(band));
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// Site 2 has one marker beyond it and the other on it: the cost is paid again and no site is taken.
		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer);
		assertEquals(2, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.IsSiteControlled(site2));
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(12, scn.GetStrength(band));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhileAMarkerIsStillOnSiteOneNoSiteCanBeControlled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var explorer = scn.GetShadowCard("explorer1");

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		// The first turn of the game: the fellowship moves to site 2 and the other player has not moved.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(5);
		var site1 = scn.GetSite(1);

		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(1, scn.ShadowGetSiteNumber());

		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer);
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.IsSiteControlled(site1));
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheLowestNumberedSiteThatNobodyControlsIsTheOneTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var explorer1 = scn.GetShadowCard("explorer1");
		var explorer2 = scn.GetShadowCard("explorer2");

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		// The rules' own example: "an opponent controls site 1 and all player markers are at site 3 or
		// higher".  The opponent is the player now moving, who took it on an earlier turn.
		scn.FreepsTakeControlOfSite(site1);
		scn.MoveMinionsToTable(explorer1, explorer2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(10);

		assertEquals(5, scn.FreepsGetSiteNumber());
		assertEquals(4, scn.ShadowGetSiteNumber());
		assertTrue(scn.FreepsControls(site1));

		// Sites 2 and 3 could both be controlled.  Nothing is asked: "so you must take control of site 2."
		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer1);
		assertTrue(scn.ShadowControls(site2));
		assertFalse(scn.IsSiteControlled(site3));
		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// And then site 3
		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer2);
		assertTrue(scn.ShadowControls(site3));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheKeywordsOfAControlledSiteStillApplyAndItsOtherTextDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBattlegroundScenario();

		var explorer1 = scn.GetShadowCard("explorer1");
		var explorer2 = scn.GetShadowCard("explorer2");
		var patrol = scn.GetShadowCard("patrol");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(3);
		var gullies = scn.GetSite(1);
		var camp = scn.GetSite(2);
		scn.MoveMinionsToTable(explorer1, explorer2, patrol);
		scn.MoveCardsToHand(savage);
		// The fellowship moves to Westemnet Hills, site 4.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(10);

		assertTrue(scn.HasKeyword(camp, Keyword.BATTLEGROUND));
		assertEquals(10, scn.GetStrength(patrol));

		// Eastemnet Gullies is controlled, and is no battleground.
		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer1);
		assertTrue(scn.ShadowControls(gullies));
		assertEquals(10, scn.GetStrength(patrol));

		// Uruk Camp is controlled: it is a battleground still, and the Shadow player controls a battleground.
		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer2);
		assertTrue(scn.ShadowControls(camp));
		assertInZone(Zone.SUPPORT, camp);
		assertTrue(scn.HasKeyword(camp, Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(camp, Keyword.PLAINS));
		assertEquals(14, scn.GetStrength(patrol));

		// The fellowship moves again, and an Uruk-hai is played with Uruk Camp in the Shadow player's
		// support area: "each time an Uruk-hai is played, that minion must exert" does not apply.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.ShadowPlayCard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ALiberatedSiteGoesBackOnTheAdventurePathAndTheCardsOnItAreDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(theoden);
		scn.MoveAlliesToTable(guma);

		var band = scn.GetShadowCard("band");
		var besieger = scn.GetShadowCard("besieger");
		var retreat = scn.GetShadowCard("retreat");

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 is the first player's own: "when your opponent controls a site from your adventure deck,
		// it's still your site."
		var site1 = scn.GetSite(1);
		assertSame(scn.GetFreepsSite(1), site1);
		scn.ShadowTakeControlOfSite(site1);
		scn.StackCardsOn(site1, besieger);
		scn.AttachCardsTo(site1, retreat);
		scn.MoveMinionsToTable(band);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetStackedCards(site1).size());
		assertAttachedTo(retreat, site1);
		assertEquals(12, scn.GetStrength(band));
		assertEquals(0, scn.GetShadowDiscardCount());

		assertTrue(scn.FreepsActionAvailable(theoden));
		scn.FreepsUseCardAction(theoden);
		assertEquals(1, scn.GetWoundsOn(theoden));

		// Back on the adventure path as site 1, and nobody's
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertFalse(scn.IsSiteControlled(site1));
		assertSame(site1, scn.GetSite(1));
		assertSame(site1, scn.GetFreepsSite(1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(10, scn.GetStrength(band));

		// "Any cards on that site are discarded": the minion stacked there and the condition played on it
		assertInZone(Zone.DISCARD, besieger, retreat);
		assertEquals(0, scn.GetStackedCards(site1).size());
		assertEquals(2, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheHighestNumberedSiteAnOpponentControlsIsTheOneLiberatedPassingOverASiteOfTheLiberatorsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(theoden);
		scn.MoveAlliesToTable(guma);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		// The other player controls sites 1 and 2, and the liberator the highest, site 3.
		scn.ShadowTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite(site2);
		scn.FreepsTakeControlOfSite(site3);
		scn.SkipToPhase(Phase.REGROUP);

		// Nothing is asked: "you must liberate the next highest site controlled by an opponent."
		scn.FreepsUseCardAction(theoden);
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertFalse(scn.IsSiteControlled(site2));
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsControls(site3));
		assertInZone(Zone.SUPPORT, site1, site3);
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ASiteThePlayerControlsHimselfIsNotLiberated() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(theoden);
		scn.MoveAlliesToTable(guma);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		// The only controlled site is the liberator's own.
		scn.FreepsTakeControlOfSite(site1);
		scn.SkipToPhase(Phase.REGROUP);

		// The exertion is a cost and can be paid, so the ability is there to use; it liberates nothing.
		assertTrue(scn.FreepsActionAvailable(theoden));
		scn.FreepsUseCardAction(theoden);
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertTrue(scn.FreepsControls(site1));
		assertInZone(Zone.SUPPORT, site1);
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AControlledSiteThatIsReplacedGoesBackToItsOwnersAdventureDeckAndTheNewSiteIsControlledWithTheCardsStackedOnIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		var besieger = scn.GetShadowCard("besieger");

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 on the path is the first player's, and the other player holds his own site 1 in his
		// adventure deck.
		var oldSite = scn.GetSite(1);
		var newSite = scn.GetShadowSite(1);
		assertSame(scn.GetFreepsSite(1), oldSite);
		assertNotSame(oldSite, newSite);
		assertInZone(Zone.ADVENTURE_DECK, newSite);
		scn.ShadowTakeControlOfSite(oldSite);
		scn.StackCardsOn(oldSite, besieger);
		scn.MoveMinionsToTable(nelya);

		// The fellowship moves to site 3.  The site its opponent controls is still the first player's
		// site, and the only one of his on the table: Nelya spots it with nothing asked.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(nelya));
		scn.ShadowUseCardAction(nelya);
		assertEquals(1, scn.GetWoundsOn(nelya));

		// The old site is back in its owner's adventure deck ...
		assertInZone(Zone.ADVENTURE_DECK, oldSite);
		assertFalse(scn.IsSiteControlled(oldSite));
		// ... and the new one is site 1, controlled as the old one was, with the stacked card on it.
		assertSame(newSite, scn.GetSite(1));
		assertTrue(scn.ShadowControls(newSite));
		assertInZone(Zone.SUPPORT, newSite);
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetStackedCards(newSite).size());
		assertSame(besieger, scn.GetStackedCards(newSite).get(0));
		assertEquals(0, scn.GetStackedCards(oldSite).size());
		assertInZone(Zone.STACKED, besieger);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Ignore("F-0467: a card played on a site stays on the old site when that site is replaced; CR 4.2 \"site\" moves it to the new site")
	@Test
	public void ACardPlayedOnAControlledSiteThatIsReplacedMovesToTheNewSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		var retreat = scn.GetShadowCard("retreat");

		scn.StartGame();
		scn.SkipToSite(2);
		var oldSite = scn.GetSite(1);
		var newSite = scn.GetShadowSite(1);
		scn.ShadowTakeControlOfSite(oldSite);
		// "Plays on a site you control"
		scn.AttachCardsTo(oldSite, retreat);
		scn.MoveMinionsToTable(nelya);

		scn.FreepsPass();
		assertAttachedTo(retreat, oldSite);
		scn.ShadowUseCardAction(nelya);
		assertSame(newSite, scn.GetSite(1));
		assertTrue(scn.ShadowControls(newSite));
		assertInZone(Zone.ADVENTURE_DECK, oldSite);

		// "When a site is replaced, all cards played on or stacked on the old site are moved to the new site."
		assertAttachedTo(retreat, newSite);
		assertEquals(1, scn.GetAttachedCards(newSite).size());
		assertEquals(0, scn.GetAttachedCards(oldSite).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
