package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_13_043_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("vapour", "13_43");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion, vitality 4
					put("avenger", "6_27");     // Ent Avenger: [gandalf] Ent companion, vitality 4; a bonus only beside 3 Ents
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] Man ally, a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, a maneuver ability only
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VapourandSteamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Vapour and Steam
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Skirmish:</b> Exert your [gandalf] companion and add a burden to make that companion <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("vapour");

		assertEquals("Vapour and Steam", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SkirmishAbilityExertsAGandalfCompanionAndAddsABurdenToMakeThatCompanionDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vapour = scn.GetFreepsCard("vapour");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var barliman = scn.GetFreepsCard("barliman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner1");
		scn.MoveCardsToSupportArea(vapour);
		scn.MoveCompanionsToTable(gandalf, avenger, aragorn);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		// Gandalf skirmishes; Ent Avenger is assigned to nobody, and the text names no skirmish.
		scn.FreepsAssignToMinions(gandalf, runner);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(avenger, Keyword.DAMAGE));
		assertTrue(scn.FreepsActionAvailable(vapour));

		scn.FreepsUseCardAction(vapour);
		// Each [gandalf] companion, in the skirmish or not; neither the [gandalf] ally nor the [gondor] companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, avenger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(avenger);

		assertEquals(1, scn.GetWoundsOn(avenger));
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetKeywordCount(avenger, Keyword.DAMAGE));
		// The companion exerted is the one made damage +1: the other [gandalf] companion is untouched.
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void DamageBonusLastsForTheSkirmishItWasMadeInAndIsGoneInTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vapour = scn.GetFreepsCard("vapour");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCardsToSupportArea(vapour);
		scn.MoveCompanionsToTable(gandalf, avenger);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, runner1}, new PhysicalCardImpl[]{avenger, runner2});
		scn.FreepsResolveSkirmish(gandalf);

		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		scn.FreepsUseCardAction(vapour);
		scn.FreepsChooseCard(gandalf);

		// Still in force at the next decision of the same skirmish.
		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Gandalf (6, +1 for Frodo's race) beats the Runner (5); the next skirmish is a phase of its own, and the
		// bonus is gone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(avenger);

		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheOnlyGandalfCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vapour = scn.GetFreepsCard("vapour");
		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner1");
		scn.MoveCardsToSupportArea(vapour);
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		// Vitality 4 with 3 wounds: Gandalf cannot exert. Aragorn and the [gandalf] ally could, and do not pay.
		scn.AddWoundsToChar(gandalf, 3);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, runner);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(vapour));
	}
}
