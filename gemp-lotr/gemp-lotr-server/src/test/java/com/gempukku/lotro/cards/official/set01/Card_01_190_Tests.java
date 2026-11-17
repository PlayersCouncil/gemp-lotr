package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_190_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "1_190");

					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc, strength 7, vitality 1, Archer and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger never fires, as it is cheated in
					put("troll", "1_165");      // Cave Troll of Moria: a [moria] minion who is no Orc
					put("soldier", "1_271");    // Orc Soldier: a [sauron] Orc; a skirmish ability used only on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MoriaAxeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Moria Axe
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [moria] Orc.<br>Bearer is <b>damage +1</b>.<br><b>Skirmish:</b> Exert bearer to make him strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axe");

		assertEquals("Moria Axe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAMoriaOrcAndMakesHimDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetShadowCard("axe");
		var marksman = scn.GetShadowCard("marksman");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(axe);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman, troop, troll, soldier);
		scn.SetTwilight(10);
		assertInHand(axe);
		scn.FreepsPass();

		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(marksman, Keyword.DAMAGE));

		assertTrue(scn.ShadowPlayAvailable(axe));
		scn.ShadowPlayCard(axe);

		// Two [moria] Orcs that differ in everything else are offered; a [moria] Troll and a [sauron] Orc are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(marksman, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troll, soldier));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);

		assertAttachedTo(axe, troop);
		// The bearer is damage +1; the other Orc is not.
		assertEquals(1, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(marksman, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsBearerToMakeHimStrengthPlusTwoUntilThatSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetShadowCard("axe");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.AttachCardsTo(troop, axe);
		assertAttachedTo(axe, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, runner}, new PhysicalCardImpl[]{aragorn, troop});

		// The Runner's skirmish first: the Troop is waiting for his own, and the text names no skirmish.
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(15, scn.GetStrength(troop)); // 13 + 2 for the Axe
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.ShadowActionAvailable(axe));

		scn.ShadowUseCardAction(axe);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(17, scn.GetStrength(troop));
		// The Orc who is fighting is not touched.
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(5, scn.GetStrength(runner));

		// Still +2 at the next decision of this skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(17, scn.GetStrength(troop));

		scn.PassSkirmishActions();
		// Boromir 7 beats the Runner 5, who dies of the wound.
		assertInDiscard(runner);

		// Gone in the next skirmish.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(15, scn.GetStrength(troop));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileItsBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetShadowCard("axe");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, axe);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(troop, 2);
		assertAttachedTo(axe, troop);
		assertEquals(2, scn.GetWoundsOn(troop));
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, troop);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(axe));
		assertEquals(2, scn.GetWoundsOn(troop));
	}
}
