package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_091_Tests
{

	// Fellowship path: site 2 is Ettenmoors 1_331 (plains only), site 4 Balin's Tomb 1_343 (underground only).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("artisan", "12_91");
					put("denizen", "11_115");   // Denizen of Khazad-dum: an [orc] Orc; strength +2 at an underground site
					put("halftroll", "14_12");  // Half-troll of Far Harad: an [orc] minion who is no Orc
					put("gear", "17_90");       // Rider's Gear: [orc] armor, bearer must be an [orc] Orc
					put("ax", "18_84");         // Orkish Ax: [orc] hand weapon, bearer must be an [orc] Orc
					put("gauntlets", "11_109"); // Bladed Gauntlets: [orc] armor, bearer must be an [orc] minion
					// Grond, Forged With Black Steel: an [orc] possession played to the support area; to play, spot an [orc] minion
					put("grond", "18_82");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Towers path: site 4 is White Rocks 4_346 (battleground only), site 6 Caves of Aglarond 4_352 (underground, sanctuary).
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("artisan", "12_91");
					put("gear", "17_90");       // Rider's Gear: [orc] armor, bearer must be an [orc] Orc
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Towers path with Uruk Camp 4_335 (battleground, plains) as site 2.
	protected VirtualTableScenario GetUrukCampScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("artisan", "12_91");
					put("gear", "17_90");       // Rider's Gear: [orc] armor, bearer must be an [orc] Orc
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcArtisanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Orc Artisan
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: Each time you play an [orc] possession on an [orc] Orc, you may add (1) (or (2) if that Orc is at a battleground or underground site).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("artisan");

		assertEquals("Orc Artisan", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachOrcPossessionPlayedOnAnOrcOrcAtAPlainsSiteMayAdd1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var artisan = scn.GetShadowCard("artisan");
		var denizen = scn.GetShadowCard("denizen");
		var gear = scn.GetShadowCard("gear");
		var ax = scn.GetShadowCard("ax");

		scn.StartGame();
		scn.MoveMinionsToTable(artisan, denizen);
		scn.MoveCardsToHand(gear, ax);
		scn.SetTwilight(10);
		assertInZone(Zone.HAND, gear);
		assertInZone(Zone.HAND, ax);
		// The fellowship moves to Ettenmoors, site 2: plains, neither battleground nor underground.
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals("Ettenmoors", scn.GetCurrentSite().getBlueprint().getTitle());

		// An [orc] armor on the Orc Artisan himself.
		scn.ShadowPlayCard(gear);
		assertTrue(scn.ShadowHasCardChoiceAvailable(artisan, denizen));
		scn.ShadowChooseCard(artisan);
		assertAttachedTo(gear, artisan);

		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(artisan));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(twilight + 1, scn.GetTwilight());

		// Each time: an [orc] hand weapon on another [orc] Orc.
		scn.ShadowPlayCard(ax);
		scn.ShadowChooseCard(denizen);
		assertAttachedTo(ax, denizen);

		twilight = scn.GetTwilight();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(artisan));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(twilight + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var artisan = scn.GetShadowCard("artisan");
		var gear = scn.GetShadowCard("gear");

		scn.StartGame();
		scn.MoveMinionsToTable(artisan);
		scn.MoveCardsToHand(gear);
		scn.SetTwilight(10);
		assertInZone(Zone.HAND, gear);
		scn.FreepsPass();

		// The Orc Artisan is the only bearer, and the Gear goes on him unasked.
		scn.ShadowPlayCard(gear);
		assertAttachedTo(gear, artisan);

		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(artisan));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(twilight, scn.GetTwilight());
		assertAttachedTo(gear, artisan);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcPossessionOnAnOrcMinionWhoIsNoOrcOrOnNoOrcAtAllIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var artisan = scn.GetShadowCard("artisan");
		var halftroll = scn.GetShadowCard("halftroll");
		var gauntlets = scn.GetShadowCard("gauntlets");
		var grond = scn.GetShadowCard("grond");

		scn.StartGame();
		scn.MoveMinionsToTable(artisan, halftroll);
		scn.MoveCardsToHand(gauntlets, grond);
		scn.SetTwilight(10);
		assertInZone(Zone.HAND, gauntlets);
		assertInZone(Zone.HAND, grond);
		scn.FreepsPass();

		// Bladed Gauntlets on the Half-troll, an [orc] minion who is no Orc.
		scn.ShadowPlayCard(gauntlets);
		assertTrue(scn.ShadowHasCardChoiceAvailable(artisan, halftroll));
		scn.ShadowChooseCard(halftroll);
		assertAttachedTo(gauntlets, halftroll);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(artisan));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Grond, an [orc] possession played to the support area, on nobody.
		scn.ShadowPlayCard(grond);
		assertInZone(Zone.SUPPORT, grond);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(artisan));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtBalinsTombAnUndergroundSiteItMayAdd2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var artisan = scn.GetShadowCard("artisan");
		var gear = scn.GetShadowCard("gear");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(artisan);
		scn.MoveCardsToHand(gear);
		scn.SetTwilight(10);
		assertInZone(Zone.HAND, gear);
		scn.FreepsPass();

		assertEquals("Balin's Tomb", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.ShadowPlayCard(gear);
		assertAttachedTo(gear, artisan);

		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(artisan));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtCavesOfAglarondAnUndergroundSanctuaryItMayAdd2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var artisan = scn.GetShadowCard("artisan");
		var gear = scn.GetShadowCard("gear");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(artisan);
		scn.MoveCardsToHand(gear);
		scn.SetTwilight(10);
		assertInZone(Zone.HAND, gear);
		scn.FreepsPass();

		assertEquals("Caves of Aglarond", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.ShadowPlayCard(gear);
		assertAttachedTo(gear, artisan);

		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(artisan));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtWhiteRocksABattlegroundItMayAdd2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var artisan = scn.GetShadowCard("artisan");
		var gear = scn.GetShadowCard("gear");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(artisan);
		scn.MoveCardsToHand(gear);
		scn.SetTwilight(10);
		assertInZone(Zone.HAND, gear);
		scn.FreepsPass();

		assertEquals("White Rocks", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.ShadowPlayCard(gear);
		assertAttachedTo(gear, artisan);

		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(artisan));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtUrukCampABattlegroundPlainsItMayAdd2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukCampScenario();

		var artisan = scn.GetShadowCard("artisan");
		var gear = scn.GetShadowCard("gear");

		scn.StartGame();
		scn.MoveMinionsToTable(artisan);
		scn.MoveCardsToHand(gear);
		scn.SetTwilight(10);
		assertInZone(Zone.HAND, gear);
		scn.FreepsPass();

		assertEquals("Uruk Camp", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.ShadowPlayCard(gear);
		assertAttachedTo(gear, artisan);

		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(artisan));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
