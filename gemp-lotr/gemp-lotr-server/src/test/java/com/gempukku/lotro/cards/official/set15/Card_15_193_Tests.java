package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
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
					// There's Another Way: [gollum] condition. "To play, spot Sméagol. ... Regroup: Discard this
					// condition to replace the fellowship's current site with a site from your adventure deck."
					put("anotherway", "12_40");
					// One Good Turn Deserves Another: [gollum] Fellowship or Regroup event, cost 0. "Spot Sméagol to
					// play the fellowship's next site. Then you may add a burden to take this card back into hand."
					put("onegoodturn", "11_49");
					put("smeagol", "5_28");     // Sméagol, Old Noser: his regroup ability is used only on purpose

					// Pathfinder: [gondor] Fellowship or Regroup event, cost 0. "Spot a ranger to play the
					// fellowship's next site (replacing opponent's site if necessary)."
					put("pathfinder", "1_110");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the ranger
					// Úlairë Nelya, Third of the Nine Riders: vitality 3. "Shadow: Exert Úlairë Nelya to replace the
					// fellowship's current site with a site from your adventure deck."
					put("nelya", "11_222");
				}},
				new HashMap<>() {{
					put("mountdoom", "15_193");
					put("eastroad", "11_236");      // East Road: each companion is twilight cost +2
					put("ettenmoors", "11_237");    // Ettenmoors: a Skirmish ability for a character
					put("marshland", "11_238");     // Expanding Marshland: a Shadow ability at 3 or fewer minions and tokens
					put("ruins", "18_138");         // Sirannon Ruins: River, no text
					put("nurn", "17_148");          // Nurn: its text is about controlling it
					put("angle", "12_185");         // The Angle: a Shadow ability that plays a Nazgul
					put("homestead", "11_230");     // Buckland Homestead: a Shadow ability that plays a Nazgul
					put("glade", "11_239");         // Fangorn Glade: adds (1) for each Free Peoples weapon when moved to
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
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 3
		 * Direction: Left
		 * Game Text: <b>Battleground</b>. <b>Mountain</b>. <b>Underground</b>. Until the end of the game, sites in this region cannot be replaced.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("mountdoom");

		assertEquals("Mount Doom", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
	}

	/**
	 * Legacy Ruling #3 decided Mount Doom goes into effect immediately upon being played, and not
	 * merely after a player has arrived at that site.
	 *
	 * <a href="https://wiki.lotrtcgpc.net/wiki/Legacy_Ruling_3">Legacy Ruling #3 on the wiki</a>
	 */

	@Test
	public void MountDoomCannotBeReplacedWhenItIsNextSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var onegoodturn = scn.GetFreepsCard("onegoodturn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var anotherway = scn.GetFreepsCard("anotherway");
		scn.MoveCardsToSupportArea(anotherway);
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(onegoodturn);

		var eastRoad = scn.GetFreepsSite("eastroad");
		var ettenmoors = scn.GetShadowSite("ettenmoors");
		var mountDoom = scn.GetFreepsSite("mountdoom");
		var marshland = scn.GetFreepsSite("marshland");

		// The first player starts at East Road and moves to the Shadow player's Ettenmoors, site 2.
		scn.StartGame(eastRoad);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ettenmoors);
		assertSame(ettenmoors, scn.GetCurrentSite());
		assertSame(ettenmoors, scn.GetSite(2));

		scn.SkipToPhase(Phase.REGROUP);

		// One Good Turn Deserves Another plays the fellowship's next site: Mount Doom becomes site 3, with no
		// fellowship at it.  The event is taken back into hand for a burden.
		assertTrue(scn.FreepsPlayAvailable(onegoodturn));
		scn.FreepsPlayCard(onegoodturn);
		scn.FreepsChooseCard(mountDoom);
		assertSame(mountDoom, scn.GetSite(3));
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsChooseYes();
		assertInZone(Zone.HAND, onegoodturn);

		// Played again, it would replace the next site, which is Mount Doom: no site is asked for, and the event
		// has gone on to its second sentence.
		scn.ShadowPass();
		scn.FreepsPlayCard(onegoodturn);
		assertTrue(scn.FreepsDecisionAvailable("Do you want to add a burden"));
		scn.FreepsChooseNo();
		assertSame(mountDoom, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, marshland);

		// There's Another Way would replace the fellowship's current site, site 2, which is in Mount Doom's
		// region: the condition is discarded and nothing is replaced.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(anotherway));
		scn.FreepsUseCardAction(anotherway);
		assertInDiscard(anotherway);
		assertSame(ettenmoors, scn.GetCurrentSite());
		assertSame(ettenmoors, scn.GetSite(2));
		assertSame(mountDoom, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, marshland);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void MountDoomCannotBeRemovedDuringTurnItsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var anotherway = scn.GetFreepsCard("anotherway");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(anotherway);

		var eastRoad = scn.GetFreepsSite("eastroad");
		var mountDoom = scn.GetShadowSite("mountdoom");
		var marshland = scn.GetFreepsSite("marshland");

		scn.StartGame(eastRoad);
		scn.FreepsPass();

		scn.ShadowChooseCardBPFromSelection(mountDoom);
		assertSame(mountDoom, scn.GetCurrentSite());
		scn.SkipToPhase(Phase.REGROUP);

		// There's Another Way would replace the fellowship's current site, Mount Doom itself: the condition is
		// discarded and nothing is replaced.
		assertTrue(scn.FreepsActionAvailable(anotherway));
		scn.FreepsUseCardAction(anotherway);
		assertInDiscard(anotherway);
		assertSame(mountDoom, scn.GetCurrentSite());
		assertSame(mountDoom, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, marshland);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ASiteIsReplacedWhileMountDoomIsStillInTheAdventureDecksAndOnTheNextTurnMountDoomItselfIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(pathfinder);

		var eastRoad = scn.GetFreepsSite("eastroad");
		var ruins = scn.GetShadowSite("ruins");
		var nurn = scn.GetShadowSite("nurn");
		var angle = scn.GetShadowSite("angle");
		var mountDoom = scn.GetFreepsSite("mountdoom");

		scn.StartGame(eastRoad);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);
		scn.MoveMinionsToTable(nelya);
		scn.ShadowRefreshDecision();

		// With Mount Doom still in the adventure decks, the Shadow player replaces the fellowship's site.
		assertInZone(Zone.ADVENTURE_DECK, mountDoom);
		assertSame(ruins, scn.GetCurrentSite());
		assertTrue(scn.ShadowActionAvailable(nelya));

		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCardBPFromSelection(nurn);
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertSame(nurn, scn.GetCurrentSite());
		assertSame(nurn, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, ruins);

		// In the regroup phase the Free Peoples player plays his own Mount Doom as the next site, site 3.
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPlayCard(pathfinder);
		scn.FreepsChooseCard(mountDoom);
		assertSame(mountDoom, scn.GetSite(3));
		assertEquals(2, scn.GetCurrentSiteNumber());

		// The first player's next turn: the fellowship moves on to Mount Doom, laid a turn ago.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertSame(mountDoom, scn.GetCurrentSite());

		scn.MoveMinionsToTable(nelya);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.ShadowActionAvailable(nelya));

		// She exerts, and the site is not replaced: the Shadow player is not asked for a site.
		scn.ShadowUseCardAction(nelya);
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertSame(mountDoom, scn.GetCurrentSite());
		assertSame(mountDoom, scn.GetSite(3));
		assertSame(nurn, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, angle, ruins);
		assertTrue(scn.AwaitingShadowPhaseActions());
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
