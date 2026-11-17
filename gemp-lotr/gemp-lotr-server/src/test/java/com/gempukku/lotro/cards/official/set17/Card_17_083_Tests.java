package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_083_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("runner", "17_83");
					put("runner2", "17_83");

					put("halftroll", "14_12");  // Half-troll of Far Harad: [orc] minion who is no Orc; a start-of-assignment trigger
					put("denizen", "11_115");   // Denizen of Khazad-dum: [orc] Orc; +2 only at an underground site
					put("brute", "10_90");      // Mordor Brute: a [sauron] Orc; a wound bar on [sauron] Orcs bearing possessions
					put("beckoning", "12_83");  // The Beckoning Shadow: an [orc] card that is no minion (condition, support area)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishRunnerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Orkish Runner
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Shadow:</b> Exert this minion and spot another [orc] minion to add (X), where X is the number of the current region.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("runner");

		assertEquals("Orkish Runner", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtSite4ShadowAbilityExertsTheRunnerToAddTwoForRegion2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var halftroll = scn.GetShadowCard("halftroll");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(runner, halftroll);
		assertEquals(0, scn.GetWoundsOn(runner));
		// The move to site 4.
		scn.FreepsPass();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();
		// The only other [orc] minion is no Orc.
		assertTrue(scn.ShadowActionAvailable(runner));
		scn.ShadowUseCardAction(runner);

		assertEquals(1, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		// Sites 4 to 6 are region 2.
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite3ItAddsOneForRegion1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(runner, runner2);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(runner2));
		// The move to site 3.
		scn.FreepsPass();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();
		// The other copy is the other [orc] minion spotted.
		assertTrue(scn.ShadowActionAvailable(runner));
		scn.ShadowUseCardAction(runner);

		assertEquals(1, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(runner2));
		assertEquals(pool + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite7ItAddsThreeForRegion3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveMinionsToTable(runner, denizen);
		assertEquals(0, scn.GetWoundsOn(runner));
		// The move to site 7.
		scn.FreepsPass();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(runner));
		scn.ShadowUseCardAction(runner);

		assertEquals(1, scn.GetWoundsOn(runner));
		assertEquals(pool + 3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedUntilAnotherOrcMinionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var brute = scn.GetShadowCard("brute");
		var beckoning = scn.GetShadowCard("beckoning");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		// An Orc of another culture, and an [orc] card that is no minion.
		scn.MoveMinionsToTable(runner, brute);
		scn.MoveCardsToSupportArea(beckoning);
		assertInZone(Zone.SUPPORT, beckoning);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(runner));

		scn.MoveMinionsToTable(denizen);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(runner));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileTheRunnerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		// Vitality 2: one wound exhausts it.
		scn.AddWoundsToChar(runner, 1);
		assertEquals(1, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(runner2));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(runner));
		// The other copy can still exert, and spots the exhausted one.
		assertTrue(scn.ShadowActionAvailable(runner2));
	}
}
