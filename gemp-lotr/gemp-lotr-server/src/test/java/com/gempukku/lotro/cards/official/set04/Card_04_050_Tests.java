package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_050_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rock", "4_50");
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, strength 4, vitality 2, no text in play
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, damage +1; his skirmish ability is used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability only
					// Cards for the hand.
					put("guard2", "1_7");       // Dwarf Guard: a Free Peoples companion
					put("sword", "1_299");      // Hobbit Sword: a Free Peoples possession
					put("axestrike", "1_3");    // Axe Strike: a Free Peoples event, never played
					put("runner", "1_178");     // Goblin Runner: a Shadow minion, in the Free Peoples player's hand
					// Strength 7, vitality 2: each companion here loses to one and lives (none is overwhelmed).
					put("soldier1", "1_271");   // Orc Soldier; his skirmish ability is used only on purpose
					put("soldier2", "1_271");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HereIsGoodRockStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Here Is Good Rock
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Tale</b>. Plays to your support area.
		 * 		<b>Skirmish:</b> Stack a Free Peoples card from hand here to make a Dwarf <b>damage</b> <b>+1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rock");

		assertEquals("Here Is Good Rock", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void StackingAFreePeoplesCardFromHandMakesADwarfDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rock = scn.GetFreepsCard("rock");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard2 = scn.GetFreepsCard("guard2");
		var sword = scn.GetFreepsCard("sword");
		var axestrike = scn.GetFreepsCard("axestrike");
		var runner = scn.GetFreepsCard("runner");
		var frodo = scn.GetRingBearer();
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		scn.MoveCardsToSupportArea(rock);
		scn.MoveCompanionsToTable(guard, gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(guard2, sword, axestrike, runner);

		scn.StartGame();
		assertInHand(guard2, sword, axestrike, runner);
		scn.MoveMinionsToTable(soldier1, soldier2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, soldier1}, new PhysicalCardImpl[]{aragorn, soldier2});
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetStackedCards(rock).size());
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertTrue(scn.FreepsActionAvailable(rock));

		scn.FreepsUseCardAction(rock);
		// A Free Peoples card of any type; not the Shadow card in the same hand.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard2, sword, axestrike));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sword);

		assertEquals(1, scn.GetStackedCards(rock).size());
		assertTrue(scn.GetStackedCards(rock).contains(sword));
		assertEquals(3, scn.GetFreepsHandCount());

		// Any Dwarf: the one in the skirmish, a companion in no skirmish, an ally; not Aragorn or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(grimir);

		assertEquals(1, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheDamageBonusLastsTheSkirmishPhaseItWasGivenIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rock = scn.GetFreepsCard("rock");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		scn.MoveCardsToSupportArea(rock);
		scn.MoveCompanionsToTable(guard, gimli);
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		assertInHand(sword);
		scn.MoveMinionsToTable(soldier1, soldier2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, soldier1}, new PhysicalCardImpl[]{gimli, soldier2});
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.IsCharAssignedAgainst(gimli, soldier2));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		// Gimli, waiting for his own skirmish, is the Dwarf chosen; the sword is the one card in hand.
		scn.FreepsUseCardAction(rock);
		scn.FreepsChooseCard(gimli);
		assertTrue(scn.GetStackedCards(rock).contains(sword));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// The last decision of this skirmish: the bonus still stands.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// The Guard, 4, loses to the Soldier, 7. Gimli's own skirmish is another skirmish phase.
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, soldier2));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
	}

	@Test
	public void NotOfferedWithOnlyAShadowCardInHandAndOfferedOnceAFreePeoplesCardIsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rock = scn.GetFreepsCard("rock");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetFreepsCard("runner");
		var sword = scn.GetFreepsCard("sword");
		var soldier1 = scn.GetShadowCard("soldier1");
		scn.MoveCardsToSupportArea(rock);
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		assertInHand(runner);
		scn.MoveMinionsToTable(soldier1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, soldier1);

		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(rock));

		scn.MoveCardsToHand(sword);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(rock));
	}

	@Test
	public void WithNoDwarfInPlayTheCardIsStackedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rock = scn.GetFreepsCard("rock");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var sword = scn.GetFreepsCard("sword");
		var soldier1 = scn.GetShadowCard("soldier1");
		scn.MoveCardsToSupportArea(rock);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		assertInHand(sword);
		scn.MoveMinionsToTable(soldier1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, soldier1);

		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DAMAGE));
		// The Dwarf is chosen after the "to": the ability is offered with no Dwarf, and its cost is paid.
		assertTrue(scn.FreepsActionAvailable(rock));
		scn.FreepsUseCardAction(rock);

		assertTrue(scn.GetStackedCards(rock).contains(sword));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
