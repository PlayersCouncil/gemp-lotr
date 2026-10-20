package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_202_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("devilry", "1_202");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3; no text
					put("scout", "1_191");      // Moria Scout: [moria] Orc, vitality 2; a "when you play" trigger only, harmless cheated in
					put("troll", "1_165");      // Cave Troll of Moria: a [moria] minion who is a Troll, vitality 4; text on playing it only
					put("soldier", "1_271");    // Orc Soldier: a [sauron] Orc, vitality 2; a skirmish ability only
					put("runner", "1_178");     // Goblin Runner: a [moria] minion of twilight cost 1, for a card that is no companion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, cost 4; no requirement to play
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, cost 2; no requirement to play
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] Man ally, cost 0; a character who is no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("devilry", "1_202");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, cost 4; no requirement to play
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, cost 2; no requirement to play
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhatIsThisNewDevilryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: What Is This New Devilry?
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Search</b>. To play, exert a [moria] Orc. Plays to your support area.<br>While the fellowship is at site 5 or higher, each companion's twilight cost is +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("devilry");

		assertEquals("What Is This New Devilry?", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SEARCH));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToPlayAMoriaOrcIsExertedAndItGoesToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var devilry = scn.GetShadowCard("devilry");
		var troop = scn.GetShadowCard("troop");
		var scout = scn.GetShadowCard("scout");
		var troll = scn.GetShadowCard("troll");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(devilry);

		scn.StartGame();
		assertInHand(devilry);
		scn.MoveMinionsToTable(troop, scout, troll, soldier);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertInHand(devilry);
		assertTrue(scn.ShadowPlayAvailable(devilry));
		scn.ShadowPlayCard(devilry);

		// [moria] Orcs of two titles; not the [moria] Troll or the [sauron] Orc, each able to exert.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, scout));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troll, soldier));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(scout);

		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertInZone(Zone.SUPPORT, devilry);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyMoriaOrcIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var devilry = scn.GetShadowCard("devilry");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(devilry);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, troll, soldier);
		scn.AddWoundsToChar(troop, 1);
		scn.SetTwilight(10);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertInHand(devilry);
		scn.FreepsPass();

		// Vitality 3 with 1 wound: the Troop can still exert.
		assertTrue(scn.ShadowPlayAvailable(devilry));
		scn.AddWoundsToChar(troop, 1);
		scn.ShadowRefreshDecision();

		// Vitality 3 with 2 wounds: exhausted.  The [moria] Troll and the [sauron] Orc beside it could exert.
		assertFalse(scn.ShadowPlayAvailable(devilry));
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertInHand(devilry);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FromSite5OnEachCompanionCostsTwoMoreAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var devilry = scn.GetShadowCard("devilry");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCardsToSupportArea(devilry);

		scn.StartGame();
		// Site 4, Balin's Tomb: one short of the threshold.
		scn.SkipToSite(4);
		scn.SetFreepsHand(aragorn, legolas, barliman);
		scn.SetShadowHand(runner);
		assertInZone(Zone.SUPPORT, devilry);
		assertEquals(4, scn.GetTwilightCost(aragorn));
		assertEquals(2, scn.GetTwilightCost(legolas));

		// Site 5, The Bridge of Khazad-dum: at the threshold.
		scn.SkipToSite(5);
		scn.SetFreepsHand(aragorn, legolas, barliman);
		scn.SetShadowHand(runner);
		assertInZone(Zone.SUPPORT, devilry);
		// Companions of two cultures and races, each +2; an ally and a minion as printed.
		assertEquals(6, scn.GetTwilightCost(aragorn));
		assertEquals(4, scn.GetTwilightCost(legolas));
		assertEquals(0, scn.GetTwilightCost(barliman));
		assertEquals(1, scn.GetTwilightCost(runner));

		int pool = scn.GetTwilight();
		scn.FreepsPlayCard(legolas);
		// 2 printed + 2
		assertEquals(pool + 4, scn.GetTwilight());
		assertInPlay(legolas);
		scn.FreepsPlayCard(barliman);
		// The ally adds his printed 0.
		assertEquals(pool + 4, scn.GetTwilight());
		assertInPlay(barliman);

		// Site 6, Galadriel's Glade: higher than 5.
		scn.SkipToSite(6);
		scn.SetFreepsHand(aragorn);
		assertInZone(Zone.SUPPORT, devilry);
		assertEquals(6, scn.GetTwilightCost(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSites5And6OfTheTowerBlockACompanionCostsWhatIsPrinted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var devilry = scn.GetShadowCard("devilry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToSupportArea(devilry);

		scn.StartGame();
		// Site 5 of the Tower block, Deeping Wall: "site 5 or higher" names the Fellowship block's sites alone.
		scn.SkipToSite(5);
		scn.SetFreepsHand(aragorn, legolas);
		assertInZone(Zone.SUPPORT, devilry);
		assertEquals(4, scn.GetTwilightCost(aragorn));
		assertEquals(2, scn.GetTwilightCost(legolas));

		int pool = scn.GetTwilight();
		scn.FreepsPlayCard(legolas);
		// 2 printed and nothing more
		assertEquals(pool + 2, scn.GetTwilight());
		assertInPlay(legolas);

		// Site 6 of the Tower block, Caves of Aglarond.
		scn.SkipToSite(6);
		scn.SetFreepsHand(aragorn);
		assertInZone(Zone.SUPPORT, devilry);
		assertEquals(4, scn.GetTwilightCost(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
