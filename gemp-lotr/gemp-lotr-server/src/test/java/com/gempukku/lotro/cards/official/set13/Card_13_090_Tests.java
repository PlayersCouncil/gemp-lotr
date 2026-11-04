package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_090_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("runner", "13_90");
					put("runner2", "13_90");
					put("column", "11_72");     // Column of Easterlings: [men] Man minion, lurker; a skirmish ability used only on purpose
					put("fearsome", "11_79");   // Fearsome Dunlending: [men] Man minion, ambush; a play trigger that is not set off when cheated in
					put("soldier", "4_220");    // Desert Soldier: [raider] Man minion; a start-of-skirmish trigger only
					// Cards that take culture tokens by their own text, cheated in with the tokens laid by hand:
					put("stragglers", "13_99"); // Stragglers: [men] support-area condition, a [men] token as it is played; a skirmish ability
					put("fleet", "18_66");      // Fleet of Corsair Ships: [men] support-area possession, a [men] token as it is played; a regroup ability
					put("threatening", "13_103"); // Always Threatening: [orc] support-area condition, [orc] tokens as it is played; a skirmish ability
					put("garrison", "6_52");    // Garrison of Osgiliath: [gondor] Free Peoples support-area condition, 2 [gondor] tokens as it is played; a skirmish ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EasterlingRunnerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Easterling Runner
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Shadow:</b> Exert this minion, spot another [men] minion, and remove a culture token to reinforce a [men] token.
		 */

		var scn = GetScenario();

		var runner = scn.GetFreepsCard("runner");

		assertEquals("Easterling Runner", runner.getBlueprint().getTitle());
		assertNull(runner.getBlueprint().getSubtitle());
		assertFalse(runner.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, runner.getBlueprint().getSide());
		assertEquals(Culture.MEN, runner.getBlueprint().getCulture());
		assertEquals(CardType.MINION, runner.getBlueprint().getCardType());
		assertEquals(Race.MAN, runner.getBlueprint().getRace());
		assertEquals(3, runner.getBlueprint().getTwilightCost());
		assertEquals(8, runner.getBlueprint().getStrength());
		assertEquals(2, runner.getBlueprint().getVitality());
		assertEquals(4, runner.getBlueprint().getSiteNumber());
	}

	@Test
	public void ExertingItAndRemovingAnyCultureTokenReinforcesAMenToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCardsToSupportArea(garrison);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var column = scn.GetShadowCard("column");
		var stragglers = scn.GetShadowCard("stragglers");
		var fleet = scn.GetShadowCard("fleet");
		var threatening = scn.GetShadowCard("threatening");
		scn.MoveCardsToSupportArea(stragglers, fleet, threatening);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2, column);
		scn.AddTokensToCard(garrison, 2);
		scn.AddTokensToCard(stragglers, 1);
		scn.AddTokensToCard(fleet, 1);
		scn.AddTokensToCard(threatening, 2);
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(stragglers));
		assertEquals(1, scn.GetCultureTokensOn(fleet));
		assertEquals(2, scn.GetCultureTokensOn(threatening));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(runner2));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(runner));
		scn.ShadowUseCardAction(runner);

		// Any culture token, on either player's card.
		assertTrue(scn.ShadowHasCardChoiceAvailable(stragglers, fleet, threatening, garrison));
		scn.ShadowChooseCard(threatening);
		assertEquals(1, scn.GetCultureTokensOn(threatening));

		// Only a card that already has a [men] token.
		assertTrue(scn.ShadowHasCardChoiceAvailable(stragglers, fleet));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(threatening, garrison));
		scn.ShadowChooseCard(fleet);

		assertEquals(2, scn.GetCultureTokensOn(fleet));
		assertEquals(1, scn.GetCultureTokensOn(stragglers));
		assertEquals(1, scn.GetCultureTokensOn(threatening));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		// This minion exerts, and not the other copy.
		assertEquals(1, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(runner2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedUntilAnotherMenMinionCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var fearsome = scn.GetShadowCard("fearsome");
		var soldier = scn.GetShadowCard("soldier");
		var stragglers = scn.GetShadowCard("stragglers");
		scn.MoveCardsToSupportArea(stragglers);

		scn.StartGame();
		// A Man minion of another culture, and a [men] card that is no minion.
		scn.MoveMinionsToTable(runner, soldier);
		scn.AddTokensToCard(stragglers, 1);
		assertEquals(1, scn.GetCultureTokensOn(stragglers));
		assertEquals(0, scn.GetWoundsOn(runner));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(runner));

		// A [men] minion of another title.
		scn.MoveMinionsToTable(fearsome);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(runner));
	}

	@Test
	public void NotOfferedWhileItIsExhaustedThoughTheOtherCopyIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var column = scn.GetShadowCard("column");
		var stragglers = scn.GetShadowCard("stragglers");
		scn.MoveCardsToSupportArea(stragglers);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2, column);
		scn.AddTokensToCard(stragglers, 1);
		// Vitality 2: one wound exhausts it.
		scn.AddWoundsToChar(runner, 1);
		assertEquals(1, scn.GetWoundsOn(runner));
		assertEquals(1, scn.GetCultureTokensOn(stragglers));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(runner));
		assertTrue(scn.ShadowActionAvailable(runner2));
	}

	@Test
	public void NotOfferedWithNoCultureTokenToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var column = scn.GetShadowCard("column");
		var stragglers = scn.GetShadowCard("stragglers");
		scn.MoveCardsToSupportArea(stragglers);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, column);
		assertEquals(0, scn.GetCultureTokensOn(stragglers));
		assertEquals(0, scn.GetWoundsOn(runner));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(runner));

		scn.AddTokensToCard(stragglers, 1);
		assertEquals(1, scn.GetCultureTokensOn(stragglers));
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(runner));
	}

	@Test
	public void WithNoMenTokenToReinforceItIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var column = scn.GetShadowCard("column");
		var stragglers = scn.GetShadowCard("stragglers");
		var threatening = scn.GetShadowCard("threatening");
		scn.MoveCardsToSupportArea(stragglers, threatening);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, column);
		// The only token in play is an [orc] one; the [men] condition has none.
		scn.AddTokensToCard(threatening, 1);
		assertEquals(1, scn.GetCultureTokensOn(threatening));
		assertEquals(0, scn.GetCultureTokensOn(stragglers));
		assertEquals(0, scn.GetWoundsOn(runner));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(runner));
		scn.ShadowUseCardAction(runner);

		// The lone token goes unasked; nothing is reinforced.
		assertEquals(1, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetCultureTokensOn(threatening));
		assertEquals(0, scn.GetCultureTokensOn(stragglers));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
