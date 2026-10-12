package com.gempukku.lotro.mechanics.sites;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Playing sites on the adventure path, each block's way, and replacing them.
 *
 * Comprehensive Rules 4.2, "setting up the game": "The first player chooses any site from his adventure deck and
 * places it on the table to begin the adventure path.  This becomes site 1.  Each player places his player marker
 * onto that site."
 *
 * "Site": "If your fellowship moves to a site that has not been played yet, one of the Shadow players must place a
 * new site on the adventure path. ...  That player looks through his adventure deck and chooses any site to play
 * as the next site.  It takes on the next consecutive number on the adventure path as its site number.  It also
 * takes on a region number.  Exception: See format."  And: "You may play a copy of a site on the adventure path
 * even if an opponent's copy was already played as an earlier site and is still active.  The copies are treated
 * as different sites, with each given a different site number."
 *
 * "Format": "When you play a game using the Fellowship block, Tower block, or King block format, you cannot
 * choose any site from your adventure deck when it is time to play a new site on the adventure path.  Instead,
 * the site which has the next consecutive site number must be played. ...  You do not add pool for the
 * fellowship's region number when you are moving your fellowship."  Elsewhere ("moving your fellowship"): "Add 3
 * twilight tokens if the new site is in region 2; or 6 if it is in region 3", and "region": "Sites 1-3 are in
 * region 1, sites 4-6 are in region 2, and sites 7-9 are in region 3."
 *
 * "Site", again: "Some cards allow a player to play the next site on the adventure path at times when the
 * fellowship is not moving.  These may be used even when the next site is already there.  In such cases, the new
 * site replaces the old one; put the old site back in its owner's adventure deck.  The new site takes the same
 * site number the old site had ...  When a site is replaced, all cards played on or stacked on the old site are
 * moved to the new site. ...  An effect that allows the playing of the 'next site' can't be used to play 'site
 * 10.'  The adventure path is limited to only nine sites."
 *
 * The same entry ends "You can't replace a site card with the same site card."  The rig gives both players the
 * same nine sites, so on a numbered path one player's site is replaced by the other's copy of it, which the game
 * allows; whether it should is asked of Ketura as Q-0226 and parked by her, and the test that replaces a site so
 * reads whose card lies there and nothing else.  Where a player's own site lies next, a card that plays his next
 * site replaces that as well (Q-0238: A).
 *
 * Which Shadow player lays a site (the arrow at the foot of the site left) is a matter for three players or more:
 * with two there is one Shadow player.  A controlled site that is replaced is ControlOfSitesTests; the order of a
 * move's steps is StepsOfAMoveTests.
 */
public class PlayingSitesTests
{
	protected VirtualTableScenario GetScenario(HashMap<String, String> sites, String format) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Aragorn, Ranger of the North: a ranger
					put("aragorn", "1_89");
					// Pathfinder: [gondor] event, cost 0.  "Fellowship or Regroup: Spot a ranger to play the
					// fellowship's next site (replacing opponent's site if necessary)."
					put("pathfinder", "1_110");

					// Úlairë Nelya, Lieutenant of Morgul: vitality 3.  "Shadow: Exert Úlairë Nelya and spot an
					// opponent's site to replace it with your site of the same number."
					put("nelya", "1_233");
					// Saruman's Frost: [isengard] condition.  "Plays on a site.  Limit 1 per site.  Each Hobbit
					// at this site is strength -2.  Discard this condition at the end of the turn."
					put("frost", "1_135");
					// Úlairë Nelya, Third of the Nine Riders: vitality 3.  "Shadow: Exert Úlairë Nelya to replace
					// the fellowship's current site with a site from your adventure deck."
					put("thirdOfTheNine", "11_222");
					// Duilin, Ranger From Blackroot Vale: strength 5.  "While the fellowship is at a forest or
					// river site, Duilin is strength +3."
					put("duilin", "14_7");
				}},
				sites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				format
		);
	}

	// Fellowship-block sites in the Fellowship block format.  Ettenmoors, site 2, has a Shadow number of 2;
	// Balin's Tomb, site 4, of 3; Anduin Confluence, site 7, of 6.  None does anything to a lone Frodo.
	protected VirtualTableScenario GetFellowshipBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(VirtualTableScenario.FellowshipSites, VirtualTableScenario.Fellowship);
	}

	// Towers-block sites in the Towers block format.  Eastfold, site 2, has a Shadow number of 2; White Rocks,
	// site 4, of 3 (and one more for each mounted companion: nobody is mounted); Hornburg Causeway, site 7, of 6.
	protected VirtualTableScenario GetTowersBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site7", "4_356");
				}},
				"ttt_block"
		);
	}

	// King-block sites in the King block format.  King's Tent, site 2, has a Shadow number of 1; Osgiliath
	// Fallen, site 4, of 2; Osgiliath Channel, site 7, of 6.
	protected VirtualTableScenario GetKingBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(VirtualTableScenario.KingSites, "king_block");
	}

	// Shadows-block sites, which have no number, in a format that plays them.  Under their keys: Fangorn Glade
	// (site1; Shadow number 0; it adds twilight for Free Peoples weapons when the fellowship moves TO it), Abandoned
	// Mine Shaft (site2; 0; its text is about a regroup move FROM it), Buckland Homestead (site6; 0) and The Angle
	// (site8; 0), whose Shadow actions want a Nazgûl played, Nurn (site4; 1; a text about controlling it), Sirannon
	// Ruins (site5; 4; no text), Emyn Muil (site7; 0; a maneuver action for a minion).
	protected VirtualTableScenario GetShadowsBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(VirtualTableScenario.ShadowsSites, VirtualTableScenario.Shadows);
	}

	@Test
	public void OnANumberedPathTheFirstPlayerLaysHisSiteOneAndEachNewSiteIsLaidByThePlayerWhoIsThenTheShadowPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var firstPlayersSite1 = scn.GetFreepsSite(1);
		var firstPlayersSite2 = scn.GetFreepsSite(2);
		var firstPlayersSite3 = scn.GetFreepsSite(3);
		var secondPlayersSite1 = scn.GetShadowSite(1);
		var secondPlayersSite2 = scn.GetShadowSite(2);
		var secondPlayersSite3 = scn.GetShadowSite(3);

		scn.StartGame();

		// Site 1 is the first player's, and both markers are on it.  Nothing else is on the path.
		assertSame(firstPlayersSite1, scn.GetSite(1));
		assertInZone(Zone.ADVENTURE_PATH, firstPlayersSite1);
		assertInZone(Zone.ADVENTURE_DECK, secondPlayersSite1, firstPlayersSite2, secondPlayersSite2);
		assertEquals(1, scn.FreepsGetSiteNumber());
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertNull(scn.GetSite(2));

		// The first player moves to a site not yet played: the Shadow player's site 2 is laid.
		scn.FreepsPass();
		assertSame(secondPlayersSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_PATH, secondPlayersSite2);
		assertInZone(Zone.ADVENTURE_DECK, firstPlayersSite2);
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The second player's turn.  His fellowship moves to site 2, which is there: nothing is laid.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		scn.ShadowPass();
		assertEquals(2, scn.ShadowGetSiteNumber());
		assertSame(secondPlayersSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, firstPlayersSite2);
		assertNull(scn.GetSite(3));

		// He moves again, to a site not yet played.  The first player is the Shadow player now, and his
		// site 3 is the one laid.
		scn.SkipToPhaseInverted(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.PassReconcile(scn.P1());
		scn.ShadowChooseToMove();
		assertEquals(3, scn.ShadowGetSiteNumber());
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertSame(firstPlayersSite3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_PATH, firstPlayersSite3);
		assertInZone(Zone.ADVENTURE_DECK, secondPlayersSite3);
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void InTheFellowshipBlockTheSiteOfTheNextNumberIsLaidWithNothingAskedAndNoTwilightIsAddedForTheRegion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var site2 = scn.GetShadowSite(2);

		scn.StartGame();

		// Region 1.  Ettenmoors has a Shadow number of 2, and there is one companion.
		scn.FreepsPass();
		assertFalse(scn.ShadowDecisionAvailable("Choose site to play"));
		assertSame(site2, scn.GetSite(2));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Region 2.  Balin's Tomb, site 4, has a Shadow number of 3: 4 tokens, and not 3 more for the region.
		scn.SkipToSite(3);
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Region 3.  Anduin Confluence, site 7, has a Shadow number of 6: 7 tokens, and not 6 more.
		scn.SkipToSite(6);
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void InTheTowersBlockTheSiteOfTheNextNumberIsLaidWithNothingAskedAndNoTwilightIsAddedForTheRegion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersBlockScenario();

		var site2 = scn.GetShadowSite(2);

		scn.StartGame();

		// Region 1.  Eastfold has a Shadow number of 2.
		scn.FreepsPass();
		assertFalse(scn.ShadowDecisionAvailable("Choose site to play"));
		assertSame(site2, scn.GetSite(2));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Region 2.  White Rocks, site 4, has a Shadow number of 3.
		scn.SkipToSite(3);
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Region 3.  Hornburg Causeway, site 7, has a Shadow number of 6.
		scn.SkipToSite(6);
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void InTheKingBlockTheSiteOfTheNextNumberIsLaidWithNothingAskedAndNoTwilightIsAddedForTheRegion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingBlockScenario();

		var site2 = scn.GetShadowSite(2);

		scn.StartGame();

		// Region 1.  King's Tent has a Shadow number of 1.
		scn.FreepsPass();
		assertFalse(scn.ShadowDecisionAvailable("Choose site to play"));
		assertSame(site2, scn.GetSite(2));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Region 2.  Osgiliath Fallen, site 4, has a Shadow number of 2.
		scn.SkipToSite(3);
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Region 3.  Osgiliath Channel, site 7, has a Shadow number of 6.
		scn.SkipToSite(6);
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnAShadowsPathEachSiteIsChosenFromTheWholeAdventureDeckAndTakesTheNextNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsBlockScenario();

		var firstPlayersNurn = scn.GetFreepsSite("site4");
		var secondPlayersNurn = scn.GetShadowSite("site4");
		var secondPlayersRuins = scn.GetShadowSite("site5");

		// "The first player chooses any site from his adventure deck": all nine are offered, and he takes
		// one from the middle of them.
		assertTrue(scn.FreepsDecisionAvailable("Choose site to play"));
		assertEquals(9, scn.FreepsGetBPChoiceCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.StartGame(firstPlayersNurn);

		assertSame(firstPlayersNurn, scn.GetSite(1));
		assertInZone(Zone.ADVENTURE_PATH, firstPlayersNurn);
		assertEquals(1, scn.FreepsGetSiteNumber());
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertNull(scn.GetSite(2));

		// The Shadow player chooses the next among his own nine, his copy of the site that is site 1 among them.
		scn.FreepsPass();
		assertTrue(scn.ShadowDecisionAvailable("Choose site to play"));
		assertEquals(9, scn.ShadowGetBPChoiceCount());
		assertTrue(scn.ShadowHasBPChoiceAvailable("17_148"));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		scn.ShadowChooseCardBPFromSelection(secondPlayersNurn);

		// "The copies are treated as different sites, with each given a different site number."
		assertSame(firstPlayersNurn, scn.GetSite(1));
		assertSame(secondPlayersNurn, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_PATH, firstPlayersNurn, secondPlayersNurn);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The fellowship moves again: eight are left to choose from, and the one chosen is site 3.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowDecisionAvailable("Choose site to play"));
		assertEquals(8, scn.ShadowGetBPChoiceCount());
		assertFalse(scn.ShadowHasBPChoiceAvailable("17_148"));
		scn.ShadowChooseCardBPFromSelection(secondPlayersRuins);
		assertSame(secondPlayersRuins, scn.GetSite(3));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnAShadowsPathTwilightIsAddedForTheRegionOfTheSiteMovedTo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsBlockScenario();

		var glade = scn.GetFreepsSite("site1");
		var angle = scn.GetShadowSite("site8");
		var homestead = scn.GetShadowSite("site6");
		var emynMuil = scn.GetShadowSite("site7");
		var nurn = scn.GetShadowSite("site4");
		var ruins = scn.GetShadowSite("site5");
		var mineShaft = scn.GetShadowSite("site2");

		scn.StartGame(glade);

		// Region 1.  Site 2, The Angle, Shadow number 0: one token, for Frodo.
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(angle);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetTwilight());

		// Site 3, Buckland Homestead, Shadow number 0: one token again.
		scn.SkipToMovementDecision();
		scn.SetTwilight(0);
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(homestead);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetTwilight());

		// Region 2.  Site 4, Emyn Muil, Shadow number 0: "add 3 twilight tokens if the new site is in region
		// 2", and one for Frodo.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(emynMuil);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetTwilight());

		// Site 5, Nurn, Shadow number 1: 1, 3 and 1.
		scn.SkipToMovementDecision();
		scn.SetTwilight(0);
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(nurn);
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetTwilight());

		// Site 6, Sirannon Ruins, Shadow number 4: 4, 3 and 1.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(8, scn.GetTwilight());

		// Region 3.  Site 7, Abandoned Mine Shaft, Shadow number 0: "or 6 if it is in region 3", and one.
		scn.SkipToMovementDecision();
		scn.SetTwilight(0);
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(mineShaft);
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACardThatPlaysTheNextSiteLaysItFromItsPlayersOwnAdventureDeckAndTheMoveThenLaysNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(pathfinder);

		var firstPlayersSite2 = scn.GetFreepsSite(2);
		var secondPlayersSite2 = scn.GetShadowSite(2);

		scn.StartGame();

		assertNull(scn.GetSite(2));

		// "At times when the fellowship is not moving": in the fellowship phase, the mover's own site 2
		scn.FreepsPlayCard(pathfinder);
		assertSame(firstPlayersSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_PATH, firstPlayersSite2);
		assertInZone(Zone.ADVENTURE_DECK, secondPlayersSite2);
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The move goes to the site that is there, and the Shadow player lays none.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertSame(firstPlayersSite2, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, secondPlayersSite2);
		// Ettenmoors' Shadow number of 2, and two companions
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACardThatPlaysTheNextSiteWhereOneAlreadyLiesReplacesItAndTheOldSiteGoesBackToItsOwnersAdventureDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsBlockScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		scn.MoveCompanionsToTable(aragorn);

		var firstPlayersHomestead = scn.GetFreepsSite("site6");
		var firstPlayersEmynMuil = scn.GetFreepsSite("site7");
		var secondPlayersAngle = scn.GetShadowSite("site8");

		scn.StartGame(scn.GetFreepsSite("site1"));

		// The first player moves once, to The Angle.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(secondPlayersAngle);

		// The second player moves twice, and the first player lays Buckland Homestead as site 3 for him.
		scn.SkipToOtherPlayersTurn();
		scn.ShadowPass();
		scn.SkipToPhaseInverted(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.PassReconcile(scn.P1());
		scn.ShadowChooseToMove();
		assertTrue(scn.FreepsDecisionAvailable("Choose site to play"));
		scn.FreepsChooseCardBPFromSelection(firstPlayersHomestead);
		assertSame(firstPlayersHomestead, scn.GetSite(3));

		// The first player's second turn, at site 2 with a site 3 already lying ahead of him.
		scn.SkipToOtherPlayersTurn();
		scn.SetHand(scn.P1(), pathfinder);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(3, scn.ShadowGetSiteNumber());

		// "These may be used even when the next site is already there."  He chooses among the seven sites
		// left in his adventure deck.
		assertTrue(scn.FreepsPlayAvailable(pathfinder));
		scn.FreepsPlayCard(pathfinder);
		assertTrue(scn.FreepsDecisionAvailable("Choose site to play"));
		assertEquals(7, scn.FreepsGetBPChoiceCount());
		scn.FreepsChooseCardBPFromSelection(firstPlayersEmynMuil);

		// "The new site replaces the old one; put the old site back in its owner's adventure deck.  The new
		// site takes the same site number the old site had."  The marker that stood on site 3 stands there
		// still.
		assertSame(firstPlayersEmynMuil, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_PATH, firstPlayersEmynMuil);
		assertInZone(Zone.ADVENTURE_DECK, firstPlayersHomestead);
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(3, scn.ShadowGetSiteNumber());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The fellowship moves to the new site 3, and nobody lays another.
		scn.FreepsPass();
		assertFalse(scn.ShadowDecisionAvailable("Choose site to play"));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertSame(firstPlayersEmynMuil, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFellowshipsCurrentSiteIsReplacedUnderItAndTheFellowshipIsThenAtTheNewSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsBlockScenario();

		var duilin = scn.GetFreepsCard("duilin");
		scn.MoveCompanionsToTable(duilin);

		var nelya = scn.GetShadowCard("thirdOfTheNine");
		var angle = scn.GetShadowSite("site8");
		var mineShaft = scn.GetShadowSite("site2");

		scn.StartGame(scn.GetFreepsSite("site1"));

		// The fellowship moves to The Angle, a forest: one token for each of two companions.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(angle);
		scn.MoveMinionsToTable(nelya);
		scn.ShadowRefreshDecision();
		assertSame(angle, scn.GetCurrentSite());
		assertEquals(8, scn.GetStrength(duilin));
		assertEquals(2, scn.GetTwilight());

		// The Shadow player replaces it with any of the eight sites left in his adventure deck.
		scn.ShadowUseCardAction(nelya);
		assertTrue(scn.ShadowDecisionAvailable("Choose site to play"));
		assertEquals(8, scn.ShadowGetBPChoiceCount());
		scn.ShadowChooseCardBPFromSelection(mineShaft);
		assertEquals(1, scn.GetWoundsOn(nelya));

		// Abandoned Mine Shaft is site 2 now, and the old site is back in its owner's adventure deck.
		assertSame(mineShaft, scn.GetSite(2));
		assertSame(mineShaft, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_PATH, mineShaft);
		assertInZone(Zone.ADVENTURE_DECK, angle);
		assertEquals(2, scn.GetCurrentSiteNumber());
		// The fellowship is at the new site, underground and no forest ...
		assertEquals(5, scn.GetStrength(duilin));
		// ... and has not moved there: nothing is added to the pool.
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnANumberedPathASiteIsReplacedByTheOtherPlayersSiteOfTheSameNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var nelya = scn.GetShadowCard("nelya");

		var oldSite = scn.GetFreepsSite(1);
		var newSite = scn.GetShadowSite(1);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(nelya);
		scn.ShadowRefreshDecision();

		assertSame(oldSite, scn.GetSite(1));
		assertEquals(3, scn.GetTwilight());

		// Site 1 is the first player's, the only site of his on the path.  The Shadow player's site 1 is
		// another copy of the same card (Q-0226): what is read is whose card lies there.
		scn.ShadowUseCardAction(nelya);
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertSame(newSite, scn.GetSite(1));
		assertInZone(Zone.ADVENTURE_PATH, newSite);
		// The old site is in its owner's adventure deck, not in that of the player who replaced it.
		assertInZone(Zone.ADVENTURE_DECK, oldSite);
		assertSame(scn.GetFreepsSite(1), oldSite);
		// Nobody has moved, and nothing is added to the pool.
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACardPlayedOnASiteThatIsReplacedMovesToTheNewSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var nelya = scn.GetShadowCard("nelya");
		var frost = scn.GetShadowCard("frost");

		var oldSite = scn.GetFreepsSite(1);
		var newSite = scn.GetShadowSite(1);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(nelya);
		// "Plays on a site"
		scn.AttachCardsTo(oldSite, frost);
		scn.ShadowRefreshDecision();

		assertAttachedTo(frost, oldSite);
		scn.ShadowUseCardAction(nelya);
		assertSame(newSite, scn.GetSite(1));
		assertInZone(Zone.ADVENTURE_DECK, oldSite);

		// "When a site is replaced, all cards played on or stacked on the old site are moved to the new site."
		assertAttachedTo(frost, newSite);
		assertInZone(Zone.ATTACHED, frost);
		assertEquals(1, scn.GetAttachedCards(newSite).size());
		assertEquals(0, scn.GetAttachedCards(oldSite).size());
		// Each seat is told of its new bearer, as of any card that changes bearer.
		assertTrue(scn.PlayerSeesCardBorneBy(scn.P1(), frost, newSite));
		assertTrue(scn.PlayerSeesCardBorneBy(scn.P2(), frost, newSite));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACardPlayedOnTheFellowshipsCurrentSiteGoesOnActingAtTheSiteThatReplacesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsBlockScenario();

		var frodo = scn.GetRingBearer();

		var nelya = scn.GetShadowCard("thirdOfTheNine");
		var frost = scn.GetShadowCard("frost");
		var angle = scn.GetShadowSite("site8");
		var mineShaft = scn.GetShadowSite("site2");

		scn.StartGame(scn.GetFreepsSite("site1"));
		// 3, and 1 for The Ruling Ring
		assertEquals(4, scn.GetStrength(frodo));

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(angle);
		scn.MoveMinionsToTable(nelya);
		// "Plays on a site. ...  Each Hobbit at this site is strength -2."
		scn.AttachCardsTo(angle, frost);
		scn.ShadowRefreshDecision();
		assertSame(angle, scn.GetCurrentSite());
		assertEquals(2, scn.GetStrength(frodo));

		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCardBPFromSelection(mineShaft);
		assertSame(mineShaft, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, angle);

		// The Frost has not left play: it lies on the new site, and "this site" is the site it now lies on.
		assertAttachedTo(frost, mineShaft);
		assertInZone(Zone.ATTACHED, frost);
		assertEquals(0, scn.GetAttachedCards(angle).size());
		assertEquals(2, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheNextSiteIsNotPlayedBeyondSiteNine() throws DecisionResultInvalidException, CardNotFoundException {
		// In this format the regroup phase at site 9 is played before the game is won.
		var scn = GetScenario(VirtualTableScenario.FellowshipSites, "pc_fotr_block");

		var aragorn = scn.GetFreepsCard("aragorn");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(8);
		scn.SetHand(scn.P1(), pathfinder);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(9, scn.GetCurrentSiteNumber());
		assertFalse(scn.GameIsFinished());
		var site9 = scn.GetCurrentSite();

		// "An effect that allows the playing of the 'next site' can't be used to play 'site 10.'"
		scn.FreepsPlayCard(pathfinder);
		assertFalse(scn.FreepsDecisionAvailable("Choose site to play"));
		assertInZone(Zone.DISCARD, pathfinder);
		assertNull(scn.GetSite(10));
		assertSame(site9, scn.GetSite(9));
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
