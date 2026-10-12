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

public class Card_01_114_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saga", "1_114");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability never used
					// Pippin, Trollslayer: a [gondor] companion who is a Hobbit, strength 3, vitality 4, enduring;
					// cheated in past his "to play"; a skirmish ability never used
					put("pippin", "103_31");
					put("merchant", "7_97");    // Gondorian Merchant: the only [gondor] ally; his maneuver ability is never used
					put("legolas", "1_50");     // Legolas, Greenleaf: an [elven] companion; his archery ability is never used
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9, vitality 2, Damage +1 and no other text
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, strength 5; its play trigger is not set off by a cheat
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheSagaofElendilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Saga of Elendil
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Vitality: +1
		 * Game Text: Tale. Bearer must be a [gondor] companion.
		 * Skirmish: Discard this condition to make bearer strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("saga");

		assertEquals("The Saga of Elendil", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnlyOnAGondorCompanionOfAnyRace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saga = scn.GetFreepsCard("saga");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, pippin, legolas);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(saga);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(saga));
		scn.FreepsPlayCard(saga);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant, legolas, frodo));

		scn.FreepsChooseCard(pippin);
		assertAttachedTo(saga, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsItToMakeBearerStrengthPlus2ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saga = scn.GetFreepsCard("saga");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetShadowCard("guard");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, pippin);
		scn.AttachCardsTo(aragorn, saga);

		scn.StartGame();
		scn.MoveMinionsToTable(guard, runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Not a skirmish phase: not offered.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(saga));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, guard},
				new PhysicalCardImpl[]{pippin, runner});
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.FreepsActionAvailable(saga));

		scn.FreepsUseCardAction(saga);
		assertInZone(Zone.DISCARD, saga);
		assertEquals(10, scn.GetStrength(aragorn));
		// Pippin, another [gondor] companion, is not the bearer.
		assertEquals(3, scn.GetStrength(pippin));

		// Still in force as the skirmish is resolved: 10 beats the Guard's 9, which 8 would not.
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(guard));

		// Gone in the next skirmish, a phase of its own.
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
