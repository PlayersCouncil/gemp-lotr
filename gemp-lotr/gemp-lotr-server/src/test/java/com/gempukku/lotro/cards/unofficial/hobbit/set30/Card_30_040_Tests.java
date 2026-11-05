package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_040_Tests
{

	// The Hobbit table: the Hobbit path (Bag End 30_49 at site 1, no terrain; The Troll Hoard 30_50 at site 2, forest
	// and underground; Rivendell 30_51 at site 3, a sanctuary) and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("orc", "30_40");
					put("orc2", "30_40");
					put("orc3", "30_40");
					// Yazneg, Orkish Assassin: [gundabad] Orc, cost 1; a Maneuver ability only
					put("yazneg", "30_41");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void WatchfulOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Watchful Orc
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gundabad
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: When you play this minion, you may play the fellowship's next site (replacing opponent's site if
		 * 	necessary).
		 * 	<b>Shadow:</b> Exert this minion twice to make each site on the adventure path gain battleground, mountain,
		 * 	forest or underground until the end of the turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("orc");

		assertEquals("Watchful Orc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GUNDABAD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedItMayPlayTheFellowshipsNextSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orc = scn.GetShadowCard("orc");
		var orc2 = scn.GetShadowCard("orc2");
		scn.MoveCardsToHand(orc);
		var freepsSite3 = scn.GetFreepsSite(3);
		var shadowSite3 = scn.GetShadowSite(3);

		scn.StartGame();
		scn.MoveMinionsToTable(orc2);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite3, shadowSite3);
		scn.ShadowPlayCard(orc);
		assertInZone(Zone.SHADOW_CHARACTERS, orc);

		// The copy played makes the offer; the copy already in play answers nothing.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(orc));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(orc2));
		scn.ShadowChooseOptionalTrigger(orc);

		// His own site 3, as the site after the fellowship's; the fellowship has not moved.
		assertSame(shadowSite3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_PATH, shadowSite3);
		assertInZone(Zone.ADVENTURE_DECK, freepsSite3);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(orc2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void HisSiteReplacesTheFreePeoplesPlayersSiteLyingNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToHand(orc);
		var freepsSite3 = scn.GetFreepsSite(3);
		var shadowSite3 = scn.GetShadowSite(3);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		// As where the Free Peoples player had laid his own site 3.
		scn.MoveCardToAdventurePath(freepsSite3);
		scn.ShadowRefreshDecision();
		assertSame(freepsSite3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite3);

		scn.ShadowPlayCard(orc);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(orc));
		scn.ShadowAcceptOptionalTrigger();

		assertSame(shadowSite3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite3);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DeclinedNoSiteIsPlayedAndAnotherOrcPlayedAfterSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orc = scn.GetShadowCard("orc");
		var yazneg = scn.GetShadowCard("yazneg");
		scn.MoveCardsToHand(orc, yazneg);
		var freepsSite3 = scn.GetFreepsSite(3);
		var shadowSite3 = scn.GetShadowSite(3);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertNull(scn.GetSite(3));
		scn.ShadowPlayCard(orc);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(orc));
		scn.ShadowDeclineOptionalTrigger();
		assertNull(scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite3, shadowSite3);

		// Another [gundabad] Orc played while the Watchful Orc is in play.
		scn.ShadowPlayCard(yazneg);
		assertInZone(Zone.SHADOW_CHARACTERS, yazneg);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(orc));
		assertNull(scn.GetSite(3));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithHisOwnSiteAlreadyNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToHand(orc);
		var shadowSite3 = scn.GetShadowSite(3);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		// On a numbered path he has no other site 3, so the trigger would do nothing.
		scn.MoveCardToAdventurePath(shadowSite3);
		scn.ShadowRefreshDecision();
		assertSame(shadowSite3, scn.GetSite(3));

		scn.ShadowPlayCard(orc);
		assertInZone(Zone.SHADOW_CHARACTERS, orc);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(orc));
		assertSame(shadowSite3, scn.GetSite(3));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowExertsTwiceToMakeEachSiteOnThePathABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orc = scn.GetShadowCard("orc");
		var orc2 = scn.GetShadowCard("orc2");
		var orc3 = scn.GetShadowCard("orc3");
		scn.MoveCardsToHand(orc3);
		var freepsSite3 = scn.GetFreepsSite(3);
		var shadowSite3 = scn.GetShadowSite(3);

		scn.StartGame();
		scn.MoveMinionsToTable(orc, orc2);
		scn.SetTwilight(20);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		assertEquals(0, scn.GetWoundsOn(orc));
		assertEquals(0, scn.GetWoundsOn(orc2));
		assertFalse(scn.HasKeyword(site1, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(site2, Keyword.BATTLEGROUND));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(orc));
		scn.ShadowUseCardAction(orc);

		// The exertions are this minion's, not the other copy's.
		assertEquals(2, scn.GetWoundsOn(orc));
		assertEquals(0, scn.GetWoundsOn(orc2));
		scn.ShadowChoose("Battleground");

		// Each site on the path, and only those: the sites 3 in the adventure decks do not gain it.
		assertTrue(scn.HasKeyword(site1, Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(site2, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(freepsSite3, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(shadowSite3, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(site1, Keyword.MOUNTAIN));

		// CR 4.2 "event": a site laid on the path afterwards is not one of them.
		scn.ShadowPlayCard(orc3);
		scn.ShadowChooseOptionalTrigger(orc3);
		assertSame(shadowSite3, scn.GetSite(3));
		assertFalse(scn.HasKeyword(shadowSite3, Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(site2, Keyword.BATTLEGROUND));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MountainForestOrUndergroundMayBeChosenInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orc = scn.GetShadowCard("orc");
		var orc2 = scn.GetShadowCard("orc2");
		var orc3 = scn.GetShadowCard("orc3");

		scn.StartGame();
		scn.MoveMinionsToTable(orc, orc2, orc3);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Bag End has no terrain.
		var site1 = scn.GetSite(1);
		assertFalse(scn.HasKeyword(site1, Keyword.MOUNTAIN));
		assertFalse(scn.HasKeyword(site1, Keyword.FOREST));
		assertFalse(scn.HasKeyword(site1, Keyword.UNDERGROUND));

		scn.ShadowUseCardAction(orc);
		scn.ShadowChoose("Mountain");
		assertTrue(scn.HasKeyword(site1, Keyword.MOUNTAIN));
		assertFalse(scn.HasKeyword(site1, Keyword.FOREST));
		assertFalse(scn.HasKeyword(site1, Keyword.BATTLEGROUND));

		scn.ShadowUseCardAction(orc2);
		scn.ShadowChoose("Forest");
		assertTrue(scn.HasKeyword(site1, Keyword.FOREST));
		assertFalse(scn.HasKeyword(site1, Keyword.UNDERGROUND));

		scn.ShadowUseCardAction(orc3);
		scn.ShadowChoose("Underground");
		assertTrue(scn.HasKeyword(site1, Keyword.UNDERGROUND));
		assertEquals(2, scn.GetWoundsOn(orc3));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenItCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orc = scn.GetShadowCard("orc");

		scn.StartGame();
		scn.MoveMinionsToTable(orc);
		// Vitality 3 with 1 wound: it can exert once more, not twice.
		scn.AddWoundsToChar(orc, 1);
		assertEquals(1, scn.GetWoundsOn(orc));
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(orc));
	}

	@Test
	public void TheTerrainLastsUntilTheEndOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orc = scn.GetShadowCard("orc");

		scn.StartGame();
		scn.MoveMinionsToTable(orc);
		scn.SetTwilight(20);
		scn.FreepsPass();

		var site2 = scn.GetSite(2);
		assertFalse(scn.HasKeyword(site2, Keyword.BATTLEGROUND));
		scn.ShadowUseCardAction(orc);
		scn.ShadowChoose("Battleground");
		assertTrue(scn.HasKeyword(site2, Keyword.BATTLEGROUND));

		// No minion, so no skirmish, on the way to a second move.
		scn.MoveCardsToDiscard(orc);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		// The Shadow phase at site 3: still a battleground.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.HasKeyword(site2, Keyword.BATTLEGROUND));

		scn.SkipToOtherPlayersTurn();
		assertFalse(scn.HasKeyword(site2, Keyword.BATTLEGROUND));
	}

	@Test
	public void ASiteTheShadowPlayerControlsIsNotOnThePathAndGainsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orc = scn.GetShadowCard("orc");

		scn.StartGame();
		scn.SkipToSite(2);
		var controlled = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(controlled));
		assertInZone(Zone.SUPPORT, controlled);
		scn.MoveMinionsToTable(orc);
		scn.SkipToPhase(Phase.SHADOW);

		// Site 1 is controlled and sits in the support area; site 2 is still on the path.
		var onPath = scn.GetSite(2);
		assertFalse(scn.HasKeyword(controlled, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(onPath, Keyword.BATTLEGROUND));
		assertTrue(scn.ShadowActionAvailable(orc));
		scn.ShadowUseCardAction(orc);
		scn.ShadowChoose("Battleground");

		assertTrue(scn.HasKeyword(onPath, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(controlled, Keyword.BATTLEGROUND));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
