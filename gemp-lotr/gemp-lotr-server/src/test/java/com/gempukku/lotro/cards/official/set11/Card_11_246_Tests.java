package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_246_Tests
{

	// The turn-skippers walk a Fellowship-block path, which they cannot do on a Shadows-block one.  Each adventure
	// deck has Mere of Dead Faces in place of site 9, and the tests put the Shadow player's copy on the path as site
	// 2, where the fellowship reaches its regroup phase on turn 1 with no site 3 laid yet.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smeagol", "6_45");     // Sméagol, Poor Creature: [gollum] companion, vitality 4; a skirmish ability only
					put("shiresmeagol", "103_116"); // Smeagol, Once Like Us: [shire] companion, vitality 4; an assignment ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: a companion who is no Sméagol; a fellowship ability only
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion, vitality 4; a regroup ability never used here
					put("wraithgollum", "103_66");  // Gollum, Half a Wraith Himself: [wraith] minion, vitality 4; nothing here sets off his text
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: a [gollum] minion who is no Gollum
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("mere", "11_246");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MereofDeadFacesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Mere of Dead Faces
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 2
		 * Direction: Left
		 * Game Text: <b>Marsh</b>. <b>Regroup:</b> Exert your Gollum or your Sméagol to play the fellowship's next site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("mere");

		assertEquals("Mere of Dead Faces", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MARSH));
	}

	@Test
	public void SmeagolExertsToPlayTheFreePeoplesPlayersOwnSiteAsTheNextSiteAndAgainPlaysNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(smeagol, sam);

		var gollum = scn.GetShadowCard("gollum");
		var mere = scn.GetShadowSite("mere");
		var freepsSite3 = scn.GetFreepsSite("site3");

		scn.StartGame();
		scn.MoveCardToAdventurePath(mere);
		scn.FreepsPass();
		assertSame(mere, scn.GetCurrentSite());
		scn.SkipToPhase(Phase.REGROUP);
		// The Shadow player's Gollum, in play in the regroup phase as one that has lived through a skirmish is.
		scn.MoveMinionsToTable(gollum);
		scn.FreepsRefreshDecision();

		assertNull(scn.GetSite(3));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.FreepsActionAvailable(mere));

		// His Sméagol is the one card he can exert: the other player's Gollum, Frodo and Sam are not his Gollum or
		// his Sméagol, and Sméagol exerts with no question.
		scn.FreepsUseCardAction(mere);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		// His own site 3 is the fellowship's next site.
		assertSame(freepsSite3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_PATH, freepsSite3);
		assertEquals(2, scn.GetCurrentSiteNumber());

		// Used again with his own site already next: the exertion is paid and nothing is played.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(mere));
		scn.FreepsUseCardAction(mere);
		assertEquals(2, scn.GetWoundsOn(smeagol));
		assertSame(freepsSite3, scn.GetSite(3));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheShireSmeagolExertsAndTheFreePeoplesPlayersSiteReplacesTheShadowPlayersSiteLyingNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("shiresmeagol");
		scn.MoveCompanionsToTable(smeagol);

		var mere = scn.GetShadowSite("mere");
		var freepsSite3 = scn.GetFreepsSite("site3");
		var shadowSite3 = scn.GetShadowSite("site3");

		scn.StartGame();
		scn.MoveCardToAdventurePath(mere);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		// The Shadow player's site 3 already lies next, as where he played it himself.
		scn.MoveCardToAdventurePath(shadowSite3);
		scn.FreepsRefreshDecision();

		assertSame(mere, scn.GetCurrentSite());
		assertSame(shadowSite3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite3);
		assertEquals(0, scn.GetWoundsOn(smeagol));

		scn.FreepsUseCardAction(mere);

		assertEquals(1, scn.GetWoundsOn(smeagol));
		// His own site 3 replaces the Shadow player's, which goes back to its owner's adventure deck.
		assertSame(freepsSite3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite3);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayerExertsHisGollumToPlayHisOwnSiteAsTheNextSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);

		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var mere = scn.GetShadowSite("mere");
		var shadowSite3 = scn.GetShadowSite("site3");

		scn.StartGame();
		scn.MoveCardToAdventurePath(mere);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(gollum, shelob);
		scn.FreepsRefreshDecision();

		assertNull(scn.GetSite(3));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertEquals(0, scn.GetWoundsOn(smeagol));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(mere));
		// His Gollum is the one card he can exert: Shelob is no Gollum, and the Sméagol is the other player's.
		scn.ShadowUseCardAction(mere);

		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// His own site 3 is the fellowship's next site, before any move is made.
		assertSame(shadowSite3, scn.GetSite(3));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheWraithGollumExertsAndTheShadowPlayersSiteReplacesTheFreePeoplesPlayersSiteLyingNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("wraithgollum");
		var mere = scn.GetShadowSite("mere");
		var freepsSite3 = scn.GetFreepsSite("site3");
		var shadowSite3 = scn.GetShadowSite("site3");

		scn.StartGame();
		scn.MoveCardToAdventurePath(mere);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(gollum);
		// The Free Peoples player's site 3 already lies next, as where he played it himself.
		scn.MoveCardToAdventurePath(freepsSite3);
		scn.FreepsRefreshDecision();

		assertSame(freepsSite3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite3);
		assertEquals(0, scn.GetWoundsOn(gollum));

		scn.FreepsPass();
		scn.ShadowUseCardAction(mere);

		assertEquals(1, scn.GetWoundsOn(gollum));
		assertSame(shadowSite3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite3);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedToTheFreePeoplesPlayerWhoseSmeagolIsExhaustedThoughTheShadowPlayersGollumCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(smeagol, sam);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(smeagol, 3);
		assertEquals(3, scn.GetWoundsOn(smeagol));

		var gollum = scn.GetShadowCard("gollum");
		var mere = scn.GetShadowSite("mere");

		scn.StartGame();
		scn.MoveCardToAdventurePath(mere);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(gollum);
		scn.FreepsRefreshDecision();

		assertSame(mere, scn.GetCurrentSite());
		assertEquals(3, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		// Frodo and Sam can exert, and the Gollum is the other player's.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(mere));

		// The Gollum's own player is offered it.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(mere));
	}

	@Test
	public void NotOfferedToTheShadowPlayerWhoseGollumIsExhaustedThoughTheFreePeoplesSmeagolCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);

		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var mere = scn.GetShadowSite("mere");

		scn.StartGame();
		scn.MoveCardToAdventurePath(mere);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(gollum, shelob);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(gollum, 3);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		// The Sméagol's own player is offered it.
		assertTrue(scn.FreepsActionAvailable(mere));

		scn.FreepsPass();
		// Shelob can exert, and the Sméagol is the other player's.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mere));
	}
}
