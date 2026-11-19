package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_099_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lights", "6_99");
					put("lights2", "6_99");      // a second copy: not unique, and "this minion" is its own
					put("nelya", "2_84");        // Ulaire Nelya: a twilight [wraith] Nazgul, strength 10; her keywords need 2 burdens or 2 wounds on the Ring-bearer
					put("soldier", "1_271");     // Orc Soldier: a [sauron] Orc who is not twilight, strength 7; a skirmish ability used only on purpose
					put("soldier2", "1_271");
					put("runner", "1_178");      // Goblin Runner: a [moria] Orc, neither [sauron] nor twilight; its text answers only its own play
					put("cover", "103_64");      // Cover of Darkness: a twilight [wraith] condition, no minion; its ability is used only on purpose
					put("guard", "5_83");        // Household Guard: [rohan] Man companion, strength 6, vitality 3, Valiant and no other text
					put("guard2", "5_83");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CorpseLightsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Corpse Lights
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Wraith
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Twilight</b>.<br><b>Skirmish:</b> Exert this minion to make a [sauron] or twilight minion strength +1 for each twilight minion you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lights");

		assertEquals("Corpse Lights", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityExertsItToMakeASauronOrTwilightMinionStrongerForEachTwilightMinionForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lights = scn.GetShadowCard("lights");
		var nelya = scn.GetShadowCard("nelya");
		var soldier = scn.GetShadowCard("soldier");
		var soldier2 = scn.GetShadowCard("soldier2");
		var runner = scn.GetShadowCard("runner");
		var cover = scn.GetShadowCard("cover");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(guard, guard2);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(lights, nelya, soldier, soldier2, runner);
		scn.MoveCardsToSupportArea(cover);
		assertInZone(Zone.SUPPORT, cover);

		scn.SkipToAssignments();
		// The Guard fights Corpse Lights first; the Orc Soldier waits for the second Guard's skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, lights}, new PhysicalCardImpl[]{guard2, soldier});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(lights));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.ShadowActionAvailable(lights));
		scn.ShadowUseCardAction(lights);

		// Itself in the skirmish, the waiting Orc Soldier, the other one and Nelya unassigned; never Goblin Runner.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lights, soldier, soldier2, nelya));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		scn.ShadowChooseCard(soldier);

		// Two twilight minions, Corpse Lights and Nelya: +2. Not the Orc Soldiers ([sauron], no twilight), Goblin
		// Runner or Cover of Darkness (twilight, no minion).
		assertEquals(1, scn.GetWoundsOn(lights));
		assertEquals(9, scn.GetStrength(soldier));
		assertEquals(7, scn.GetStrength(soldier2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Corpse Lights beats the Guard, 7 to 6; in the next skirmish the bonus is gone.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(guard2);
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithItselfTheOnlyTwilightMinionItMakesItselfStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lights = scn.GetShadowCard("lights");
		var soldier = scn.GetShadowCard("soldier");
		var cover = scn.GetShadowCard("cover");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(lights, soldier);
		scn.MoveCardsToSupportArea(cover);
		assertInZone(Zone.SUPPORT, cover);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, lights);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);
		scn.FreepsPass();

		assertEquals(7, scn.GetStrength(lights));
		scn.ShadowUseCardAction(lights);
		assertTrue(scn.ShadowHasCardChoiceAvailable(lights, soldier));
		scn.ShadowChooseCard(lights);

		// One twilight minion, itself: +1.
		assertEquals(1, scn.GetWoundsOn(lights));
		assertEquals(8, scn.GetStrength(lights));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ExhaustedItIsNotOfferedAndASecondCopyExertsOnlyItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lights = scn.GetShadowCard("lights");
		var lights2 = scn.GetShadowCard("lights2");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(lights, lights2);
		// Vitality 2: one wound exhausts it.
		scn.AddWoundsToChar(lights, 1);
		assertEquals(1, scn.GetWoundsOn(lights));
		assertEquals(0, scn.GetWoundsOn(lights2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, lights);
		scn.ShadowAssignToMinions(guard, lights2);
		scn.FreepsResolveSkirmish(guard);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(lights));
		assertTrue(scn.ShadowActionAvailable(lights2));
		scn.ShadowUseCardAction(lights2);
		scn.ShadowChooseCard(lights);

		// The second copy pays with itself; two twilight minions, so the first copy is 7 + 2.
		assertEquals(1, scn.GetWoundsOn(lights2));
		assertEquals(1, scn.GetWoundsOn(lights));
		assertEquals(9, scn.GetStrength(lights));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
