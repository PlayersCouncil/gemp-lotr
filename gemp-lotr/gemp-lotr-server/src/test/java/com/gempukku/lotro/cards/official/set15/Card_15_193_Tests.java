package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_193_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("anotherway", "12_40");
					put("onegoodturn", "11_49");
					put("smeagol", "5_28");

					put("leader", "12_34");
					put("gandalf", "1_364");

				}},
				new HashMap<>() {{
					put("Mount Doom", "15_193");
					put("East Road", "11_236");
					put("Ettenmoors", "11_237");
					put("Fangorn Glade", "11_238");
					put("site5", "11_239");
					put("site6", "11_239");
					put("site7", "11_239");
					put("site8", "11_239");
					put("site9", "11_239");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	// Mount Doom lies behind the fellowship here, as site 2, and the path runs on into region 2.
	protected VirtualTableScenario GetMountDoomBehindTheFellowshipScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Traveled Leader: [gandalf] Maneuver or Regroup event, cost 1. "Spot a [gandalf] Wizard to
					// replace a site in the fellowship's current region with a site from your adventure deck."
					put("leader1", "12_34");
					put("leader2", "12_34");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: the [gandalf] Wizard; his text is about his strength
				}},
				new HashMap<>() {{
					put("glade", "11_239");     // Fangorn Glade: adds (1) for each Free Peoples weapon when moved to
					put("mountdoom", "15_193");
					put("ruins", "18_138");     // Sirannon Ruins: River, no text
					put("nurn", "17_148");      // Nurn: its text is about controlling it
					put("angle", "12_185");     // The Angle: a Shadow ability that plays a Nazgul
					put("homestead", "11_230"); // Buckland Homestead: a Shadow ability that plays a Nazgul
					put("crags", "11_234");     // Crags of Emyn Muil: wounds on a regroup move from it in region 2
					put("emynmuil", "12_187");  // Emyn Muil: a Maneuver ability for a minion
					put("rauros", "17_146");    // Falls of Rauros: acts when moved to in region 2
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void MountDoomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Mount Doom
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 3
		 * Type: Site
		 * Subtype: 
		 * Site Number: *
		 * Game Text: <b>Battleground</b>. <b>Mountain</b>. <b>Underground</b>. Until the end of the game, sites in this region cannot be replaced.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("Mount Doom");

		assertEquals("Mount Doom", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	/**
	 * Legacy Ruling #3 decided Mount Doom goes into effect immediately upon being played, and not
	 * merely after a player has arrived at that site.
	 *
	 * <a href="https://wiki.lotrtcgpc.net/wiki/Legacy_Ruling_3">Legacy Ruling #3 on the wiki</a>
	 */

	@Test
	public void MountDoomCannotBeReplacedWhenItIsNextSite() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var onegoodturn = scn.GetFreepsCard("onegoodturn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var anotherway = scn.GetFreepsCard("anotherway");
		scn.MoveCardsToSupportArea(anotherway);
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(onegoodturn);

		var site1 = scn.GetFreepsSite("East Road");
		var freepsSite3 = scn.GetFreepsSite("Fangorn Glade");
		var shadowSite2 = scn.GetShadowSite("Ettenmoors");
		var mountdoom = scn.GetFreepsSite("mountdoom");

		scn.StartGame(site1);

		//Start at East Road with player one
		scn.FreepsPass();

		//P1 then moves to P2 Ettenmoors
		scn.ShadowChooseCardBPFromSelection(shadowSite2);
		assertEquals(scn.GetCurrentSite(), shadowSite2);
		assertEquals(scn.GetSite(2), shadowSite2);

		//P1 then stops and we reconcile
		scn.SkipToPhase(Phase.REGROUP);

		//We now play One Good Turn Deserves Another: "Spot Sméagol to play the fellowship's next site.
		// Then you may add a burden to take this card back into hand."
		// Mount Doom should not be replaceable once it is played, even though no player is there yet.
		assertTrue(scn.FreepsPlayAvailable(onegoodturn));
		scn.FreepsPlayCard(onegoodturn);
		scn.FreepsChooseCard(mountdoom);
		assertEquals(scn.GetSite(3), mountdoom);
		scn.FreepsChooseYes(); //take One Good Turn back into hand

		scn.ShadowPass();
		scn.FreepsPlayCard(onegoodturn);
		//This ensures that we have skipped over choosing a site and instead OGT has moved on to its other clause.
		assertTrue(scn.FreepsDecisionAvailable("Do you want to add a burden"));
		scn.FreepsChooseNo();
		assertNotEquals(scn.GetSite(3), freepsSite3);
		assertEquals(scn.GetSite(3), mountdoom);

		//There's Another Way: "Discard this condition to replace the fellowship's current site with one
		// from your adventure deck."  This can be activated but the replacement should completely fail,
		// since the current site (site 2) is in the same region as Mount Doom now.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(anotherway));
		scn.FreepsUseCardAction(anotherway);
		assertEquals(scn.GetCurrentSite(), shadowSite2);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
	}

	@Test
	public void MountDoomCannotBeRemovedDuringTurnItsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var anotherway = scn.GetFreepsCard("anotherway");
		scn.MoveCardsToSupportArea(anotherway);

		var site1 = scn.GetFreepsSite("East Road");
		var mountdoom = scn.GetShadowSite("Mount Doom");

		scn.StartGame(site1);
		scn.FreepsPass();

		scn.ShadowChooseCardBPFromSelection(mountdoom);
		assertEquals(scn.GetCurrentSite(), mountdoom);
		scn.SkipToPhase(Phase.REGROUP);

		//There's Another Way: "Discard this condition to replace the fellowship's current site with one
		// from your adventure deck."  This can be activated but the replacement should completely fail.
		assertTrue(scn.FreepsActionAvailable(anotherway));
		scn.FreepsUseCardAction(anotherway);
		assertEquals(scn.GetCurrentSite(), mountdoom);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
	}

	@Test
	public void OnceTheFellowshipHasLeftMountDoomASiteChosenInItsRegionIsNotReplacedAndASiteInTheNextRegionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMountDoomBehindTheFellowshipScenario();

		var leader1 = scn.GetFreepsCard("leader1");
		var leader2 = scn.GetFreepsCard("leader2");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(leader1, leader2);

		var glade = scn.GetFreepsSite("glade");
		var mountDoom = scn.GetShadowSite("mountdoom");
		var ruins = scn.GetShadowSite("ruins");
		var nurn = scn.GetShadowSite("nurn");
		var angle = scn.GetFreepsSite("angle");

		// Two moves in the first turn: the Shadow player lays Mount Doom as site 2 and Sirannon Ruins as site 3,
		// both in region 1.
		scn.StartGame(glade);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(mountDoom);
		assertSame(mountDoom, scn.GetCurrentSite());
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(ruins);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertSame(glade, scn.GetSite(1));
		assertSame(mountDoom, scn.GetSite(2));
		assertSame(ruins, scn.GetSite(3));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(leader1));

		// The three sites of region 1 may be chosen, Mount Doom among them; the one chosen is not replaced, and
		// the Free Peoples player is not asked for a site from his adventure deck.
		scn.FreepsPlayCard(leader1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(glade, mountDoom, ruins));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(glade);
		assertSame(glade, scn.GetSite(1));
		assertSame(mountDoom, scn.GetSite(2));
		assertSame(ruins, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, angle);
		assertInDiscard(leader1);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// The first player's next turn: on to site 4, the first site of region 2, where Mount Doom's text does
		// not reach.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(nurn);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertSame(nurn, scn.GetCurrentSite());
		assertSame(mountDoom, scn.GetSite(2));
		assertTrue(scn.FreepsPlayAvailable(leader2));

		// Site 4 is the only site of region 2 on the path, so the game takes it without asking which; the Free
		// Peoples player picks its replacement from his adventure deck.
		scn.FreepsPlayCard(leader2);
		scn.FreepsChooseCardBPFromSelection(angle);
		assertSame(angle, scn.GetSite(4));
		assertSame(angle, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, nurn);
		assertSame(glade, scn.GetSite(1));
		assertSame(mountDoom, scn.GetSite(2));
		assertSame(ruins, scn.GetSite(3));
		assertInDiscard(leader2);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
