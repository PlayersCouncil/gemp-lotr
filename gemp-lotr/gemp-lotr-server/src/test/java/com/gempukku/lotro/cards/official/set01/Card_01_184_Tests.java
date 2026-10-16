package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.ArcheryTotalModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_184_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wallcrawler", "1_184");
					put("wallcrawler2", "1_184");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6, strength 13; no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; its (2) only when played
					put("cavetroll", "1_165");  // Cave Troll of Moria, Scourge of the Black Pit: [moria] Troll, no Orc; its other text is about playing it
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc; its text answers a weapon played on it
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinWallcrawlerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Goblin Wallcrawler
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: While at an underground site, this minion is strength +2.
		 * 	While you can spot another [moria] Orc, the fellowship archery total is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wallcrawler");

		assertEquals("Goblin Wallcrawler", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtAnUndergroundSiteEachWallcrawlerIsStrengthPlus2AndNoOtherOrcIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wallcrawler = scn.GetShadowCard("wallcrawler");
		var wallcrawler2 = scn.GetShadowCard("wallcrawler2");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(3);
		// Rivendell Valley, site 3, is no underground site.
		scn.MoveMinionsToTable(wallcrawler, wallcrawler2, runner);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(6, scn.GetStrength(wallcrawler));
		assertEquals(6, scn.GetStrength(wallcrawler2));
		assertEquals(5, scn.GetStrength(runner));

		// The fellowship moves to Balin's Tomb, site 4, an underground site.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		// Each copy +2 for itself, and no more; the other [moria] Orc as printed.
		assertEquals(8, scn.GetStrength(wallcrawler));
		assertEquals(8, scn.GetStrength(wallcrawler2));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FellowshipArcheryTotalIsOneLessOnceAnotherMoriaOrcIsPlayedBesideIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wallcrawler = scn.GetShadowCard("wallcrawler");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveCardsToHand(wallcrawler, troop);
		// A fellowship archery total high enough that 1 less is told from 2 less and from none.
		scn.ApplyAdHocModifier(new ArcheryTotalModifier(null, Side.FREE_PEOPLE, 3));
		scn.SetTwilight(12);
		scn.FreepsPass();

		assertEquals(3, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());

		scn.ShadowPlayCard(wallcrawler);
		assertInZone(Zone.SHADOW_CHARACTERS, wallcrawler);
		// It is a [moria] Orc itself, and not another one.
		assertEquals(3, scn.GetFreepsArcheryTotal());

		scn.ShadowPlayCard(troop);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertEquals(2, scn.GetFreepsArcheryTotal());
		// The minion archery total is untouched.
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnlyAnotherMoriaOrcThatCanBeSpottedLowersTheTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wallcrawler = scn.GetShadowCard("wallcrawler");
		var runner = scn.GetShadowCard("runner");
		var cavetroll = scn.GetShadowCard("cavetroll");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.ApplyAdHocModifier(new ArcheryTotalModifier(null, Side.FREE_PEOPLE, 3));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.MoveMinionsToTable(wallcrawler);
		assertEquals(3, scn.GetFreepsArcheryTotal());

		// A [moria] minion who is no Orc, and an Orc who is not [moria].
		scn.MoveMinionsToTable(cavetroll, chieftain);
		assertEquals(3, scn.GetFreepsArcheryTotal());

		scn.MoveMinionsToTable(runner);
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TwoGoblinWallcrawlersEachSpotTheOtherAndEachTakesOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wallcrawler = scn.GetShadowCard("wallcrawler");
		var wallcrawler2 = scn.GetShadowCard("wallcrawler2");

		scn.StartGame();
		scn.ApplyAdHocModifier(new ArcheryTotalModifier(null, Side.FREE_PEOPLE, 3));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.MoveMinionsToTable(wallcrawler);
		assertEquals(3, scn.GetFreepsArcheryTotal());

		scn.MoveMinionsToTable(wallcrawler2);
		// 3 - 1 - 1
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
