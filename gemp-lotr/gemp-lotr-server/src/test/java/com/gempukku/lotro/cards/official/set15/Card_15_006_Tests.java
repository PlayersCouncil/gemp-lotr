package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_006_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gloin", "15_6");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, 8/4; a maneuver ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, 4/2; "To play, spot a Dwarf" only
					// Uruk Pursuer: [isengard] Uruk-hai, fierce, strength 5, vitality 3; texts about unbound Hobbits only.
					// Gloin (6) beats him in both rounds and he lives through the two wounds.
					put("pursuer", "4_190");
					// Hunting Herdsman: [men] Man, strength 8, hunter 1 (9 against Gloin), vitality 2; +3 for each
					// follower, and there are none.  Beats Gloin without doubling him.
					put("herdsman", "15_83");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, 5/1; its play trigger does nothing when cheated in
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					// Wish For Our Kinfolk: [dwarven] support-area condition whose Fellowship ability adds [dwarven] tokens to it
					put("wish", "8_8");
					// Run Until Found: [dwarven] support-area condition that takes a [dwarven] token as it is played
					put("run", "18_2");
					// A Path Appointed: [gondor] support-area condition whose Fellowship ability adds [gondor] tokens to it
					put("path", "8_42");
					put("bracers", "2_3");      // Dwarven Bracers: [dwarven] possession; a response used only on purpose
					put("axestrike", "1_3");    // Axe Strike: [dwarven] Skirmish event
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GloinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Glóin, Son of Gróin
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Each time Glóin wins a skirmish, you may draw 3 cards, then place 3 cards from your hand beneath your draw deck in any order.<br><b>Skirmish:</b> Remove a [dwarven] token to make Glóin strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gloin");

		assertEquals("Glóin", card.getBlueprint().getTitle());
		assertEquals("Son of Gróin", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void WinningASkirmishOffersToDrawThreeCardsAndThenPlaceThreeFromHandBeneathTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloin = scn.GetFreepsCard("gloin");
		var guard = scn.GetFreepsCard("guard");
		var bracers = scn.GetFreepsCard("bracers");
		var axestrike = scn.GetFreepsCard("axestrike");
		// Cards of the Free Peoples player's own deck: a companion, a possession, an event and two Shadow cards.
		var myRunner = scn.GetFreepsCard("runner");
		var myTroop = scn.GetFreepsCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gloin);

		scn.StartGame();
		scn.MoveCardsToHand(guard, bracers);
		scn.MoveCardsToTopOfDeck(axestrike, myRunner, myTroop);
		assertInHand(guard, bracers);
		assertInZone(Zone.DECK, axestrike, myRunner, myTroop);
		assertEquals(myTroop, scn.GetFreepsTopOfDeck());
		assertEquals(myRunner, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(axestrike, scn.GetFromTopOfFreepsDeck(3));
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gloin, runner);
		scn.FreepsResolveSkirmish(gloin);
		int deck = scn.GetFreepsDeckCount();
		assertEquals(2, scn.GetFreepsHandCount());
		scn.PassSkirmishActions();

		// Gloin 6 against the Runner 5.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gloin));
		scn.FreepsAcceptOptionalTrigger();

		// The top three cards are drawn.
		assertInHand(guard, bracers, axestrike, myRunner, myTroop);
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(deck - 3, scn.GetFreepsDeckCount());

		// Then three cards from hand, of any kind: exactly three.
		assertTrue(scn.FreepsDecisionAvailable("Choose cards from hand"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, bracers, axestrike, myRunner, myTroop));
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(bracers, myTroop, guard);

		// In the order the player gives, one card at a time; each placed goes beneath the one before.
		assertTrue(scn.FreepsDecisionAvailable("Choose card to put beneath draw deck"));
		scn.FreepsChooseCard(guard);
		assertEquals(guard, scn.GetFreepsBottomOfDeck());
		scn.FreepsChooseCard(bracers);

		assertEquals(2, scn.GetFreepsHandCount());
		assertInHand(axestrike, myRunner);
		assertEquals(deck, scn.GetFreepsDeckCount());
		// The first placed lies highest of the three; the last, placed unasked, is the bottom card.
		assertEquals(myTroop, scn.GetFreepsBottomOfDeck());
		assertEquals(bracers, scn.GetFromBottomOfFreepsDeck(2));
		assertEquals(guard, scn.GetFromBottomOfFreepsDeck(3));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningLeavesHandAndDeckAndAFierceSecondWinOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloin = scn.GetFreepsCard("gloin");
		var guard = scn.GetFreepsCard("guard");
		var bracers = scn.GetFreepsCard("bracers");
		var axestrike = scn.GetFreepsCard("axestrike");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(gloin);

		scn.StartGame();
		scn.MoveCardsToHand(guard, bracers);
		scn.MoveCardsToTopOfDeck(axestrike);
		assertInHand(guard, bracers);
		assertInZone(Zone.DECK, axestrike);
		assertEquals(axestrike, scn.GetFreepsTopOfDeck());
		scn.MoveMinionsToTable(pursuer);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gloin, pursuer);
		scn.FreepsResolveSkirmish(gloin);
		int deck = scn.GetFreepsDeckCount();
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gloin));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetFreepsHandCount());
		assertInHand(guard, bracers);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(axestrike, scn.GetFreepsTopOfDeck());
		assertFalse(scn.FreepsDecisionAvailable("Choose cards from hand"));
		assertEquals(1, scn.GetWoundsOn(pursuer));

		// The fierce round: Gloin beats the Pursuer a second time this turn.
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(gloin, pursuer);
		scn.FreepsResolveSkirmish(gloin);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(pursuer));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gloin));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(5, scn.GetFreepsHandCount());
		assertInHand(axestrike);
		assertTrue(scn.FreepsDecisionAvailable("Choose cards from hand"));
	}

	@Test
	public void LosingASkirmishAndLivingOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloin = scn.GetFreepsCard("gloin");
		var herdsman = scn.GetShadowCard("herdsman");
		scn.MoveCompanionsToTable(gloin);

		scn.StartGame();
		scn.MoveMinionsToTable(herdsman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gloin, herdsman);
		scn.FreepsResolveSkirmish(gloin);
		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetWoundsOn(gloin));
		scn.PassSkirmishActions();

		// 9 against 6: Gloin loses, takes a wound and lives.
		assertEquals(1, scn.GetWoundsOn(gloin));
		assertInZone(Zone.FREE_CHARACTERS, gloin);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gloin));
		assertEquals(hand, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemovesADwarvenTokenToMakeHimStrengthPlus2UntilTheEndOfThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloin = scn.GetFreepsCard("gloin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var wish = scn.GetFreepsCard("wish");
		var run = scn.GetFreepsCard("run");
		var path = scn.GetFreepsCard("path");
		var runner = scn.GetShadowCard("runner");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(gloin, aragorn, guard);
		scn.MoveCardsToSupportArea(wish, run, path);

		scn.StartGame();
		scn.AddTokensToCard(wish, 1);
		scn.AddTokensToCard(run, 1);
		scn.AddTokensToCard(path, 1);
		assertEquals(1, scn.GetCultureTokensOn(wish));
		assertEquals(1, scn.GetCultureTokensOn(run));
		assertEquals(1, scn.GetCultureTokensOn(path));
		scn.MoveMinionsToTable(runner, pursuer);

		// The assignment phase is no skirmish phase.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gloin));
		scn.PassCurrentPhaseActions();

		// Gloin is assigned to nobody.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{guard, pursuer});
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(6, scn.GetStrength(gloin));
		assertTrue(scn.FreepsActionAvailable(gloin));
		scn.FreepsUseCardAction(gloin);
		// A [dwarven] token from either card; not the [gondor] token.
		assertTrue(scn.FreepsHasCardChoiceAvailable(wish, run));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(path));
		scn.FreepsChooseCard(wish);

		assertEquals(0, scn.GetCultureTokensOn(wish));
		assertEquals(1, scn.GetCultureTokensOn(run));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(8, scn.GetStrength(gloin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The next skirmish is a phase of its own.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(guard);
		assertEquals(6, scn.GetStrength(gloin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithNoDwarvenTokenToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloin = scn.GetFreepsCard("gloin");
		var wish = scn.GetFreepsCard("wish");
		var path = scn.GetFreepsCard("path");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gloin);
		scn.MoveCardsToSupportArea(wish, path);

		scn.StartGame();
		scn.AddTokensToCard(path, 2);
		assertEquals(0, scn.GetCultureTokensOn(wish));
		assertEquals(2, scn.GetCultureTokensOn(path));
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gloin, runner);
		scn.FreepsResolveSkirmish(gloin);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gloin));
		assertEquals(6, scn.GetStrength(gloin));
	}
}
