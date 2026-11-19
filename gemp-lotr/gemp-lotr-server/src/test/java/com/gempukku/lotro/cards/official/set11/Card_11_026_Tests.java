package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_026_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("future", "11_26");

					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf companion; an archery ability used only on purpose
					put("orophin", "1_56");     // Orophin, Lorien Bowman: an Elf ally; an archery ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Elf; a maneuver ability only
					// Final Count: an [elven] support-area condition that carries [elven] tokens of its own
					put("count", "4_69");
					// Strength of Arms: another [elven] support-area condition, with nothing that answers its own play
					put("arms", "4_82");

					// Cards for the draw decks: Lorien Elf and Boromir, Son of Denethor, by title
					put("lorienelf", "1_53");
					put("boromir", "1_97");

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UncertainFutureStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Uncertain Future
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: When you play this condition, spot an Elf to add 3 [elven] tokens here.
		 * 	<b>Maneuver:</b> Discard this condition or remove an [elven] token from here to reveal the top card of
		 * 	your draw deck. You may place that card beneath your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("future");

		assertEquals("Uncertain Future", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedBesideAnElfCompanionItTakesThreeTokensAndAnElvenConditionPlayedAfterAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var future = scn.GetFreepsCard("future");
		var count = scn.GetFreepsCard("count");
		var arms = scn.GetFreepsCard("arms");
		scn.MoveCompanionsToTable("legolas");
		scn.MoveCardsToSupportArea(count);
		scn.MoveCardsToHand(future, arms);

		scn.StartGame();
		scn.AddTokensToCard(count, 1);
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertInHand(future, arms);

		assertTrue(scn.FreepsPlayAvailable(future));
		scn.FreepsPlayCard(future);
		assertInZone(Zone.SUPPORT, future);
		assertEquals(3, scn.GetCultureTokensOn(future));
		// "Here": Final Count's token is as it was.
		assertEquals(1, scn.GetCultureTokensOn(count));

		// Another [elven] condition played while this one is in play sets nothing off.
		scn.FreepsPlayCard(arms);
		assertInZone(Zone.SUPPORT, arms);
		assertEquals(0, scn.GetCultureTokensOn(arms));
		assertEquals(3, scn.GetCultureTokensOn(future));
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedWithAnElfAllyAsTheOnlyElfItTakesThreeTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var future = scn.GetFreepsCard("future");
		scn.MoveAlliesToTable("orophin");
		scn.MoveCardsToHand(future);

		scn.StartGame();
		assertInHand(future);

		scn.FreepsPlayCard(future);
		assertInZone(Zone.SUPPORT, future);
		assertEquals(3, scn.GetCultureTokensOn(future));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedWithOnlyACompanionWhoIsNoElfItTakesNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var future = scn.GetFreepsCard("future");
		scn.MoveCompanionsToTable("aragorn");
		scn.MoveCardsToHand(future);

		scn.StartGame();
		assertInHand(future);

		// Nothing in the text is needed to play it: it is played, and nothing is added.
		assertTrue(scn.FreepsPlayAvailable(future));
		scn.FreepsPlayCard(future);
		assertInZone(Zone.SUPPORT, future);
		assertEquals(0, scn.GetCultureTokensOn(future));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverAbilityRemovesATokenFromHereToRevealTheTopCardAndMayPlaceItBeneathTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var future = scn.GetFreepsCard("future");
		var count = scn.GetFreepsCard("count");
		var top = scn.GetFreepsCard("lorienelf");
		var second = scn.GetFreepsCard("boromir");
		var shadowTop = scn.GetShadowCard("legolas");
		scn.MoveCompanionsToTable("legolas");
		scn.MoveCardsToSupportArea(future, count);
		assertInZone(Zone.SUPPORT, future);

		scn.StartGame();
		scn.AddTokensToCard(future, 3);
		scn.AddTokensToCard(count, 1);
		scn.SetFreepsDrawDeck(top, second);
		scn.SetShadowDrawDeck(shadowTop);
		assertEquals(3, scn.GetCultureTokensOn(future));
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertSame(top, scn.GetFreepsTopOfDeck());
		assertSame(second, scn.GetFreepsBottomOfDeck());
		assertSame(shadowTop, scn.GetShadowTopOfDeck());

		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(future));
		scn.FreepsUseCardAction(future);

		// Both costs can be paid, and the player is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Discard this condition"));
		assertTrue(scn.FreepsChoiceAvailable("token"));
		scn.FreepsChooseOption("token");
		assertEquals(2, scn.GetCultureTokensOn(future));
		assertInZone(Zone.SUPPORT, future);
		// "From here": Final Count keeps its token.
		assertEquals(1, scn.GetCultureTokensOn(count));

		// The top card of his own deck, and only that, is shown to each player.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(top, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(top, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(second, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(shadowTop, false));
		scn.DismissRevealedCards();

		assertTrue(scn.FreepsDecisionAvailable("beneath your draw deck"));
		scn.FreepsChooseYes();

		assertSame(top, scn.GetFreepsBottomOfDeck());
		assertSame(second, scn.GetFreepsTopOfDeck());
		assertEquals(2, scn.GetFreepsDeckCount());
		// The other player's deck is untouched.
		assertSame(shadowTop, scn.GetShadowTopOfDeck());
		assertEquals(1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityDiscardsThisConditionToRevealTheTopCardAndDecliningLeavesItOnTop() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var future = scn.GetFreepsCard("future");
		var top = scn.GetFreepsCard("lorienelf");
		var second = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable("legolas");
		scn.MoveCardsToSupportArea(future);
		assertInZone(Zone.SUPPORT, future);

		scn.StartGame();
		scn.AddTokensToCard(future, 2);
		scn.SetFreepsDrawDeck(top, second);
		assertEquals(2, scn.GetCultureTokensOn(future));
		assertSame(top, scn.GetFreepsTopOfDeck());

		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(future);
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("Discard this condition");
		assertInDiscard(future);

		assertTrue(scn.FreepsHasCardChoiceAvailable(top, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(top, false));
		scn.DismissRevealedCards();

		assertTrue(scn.FreepsDecisionAvailable("beneath your draw deck"));
		scn.FreepsChooseNo();

		assertSame(top, scn.GetFreepsTopOfDeck());
		assertSame(second, scn.GetFreepsBottomOfDeck());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoTokenHereTheManeuverAbilityDiscardsThisConditionUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var future = scn.GetFreepsCard("future");
		var count = scn.GetFreepsCard("count");
		var top = scn.GetFreepsCard("lorienelf");
		var second = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable("legolas");
		scn.MoveCardsToSupportArea(future, count);
		assertInZone(Zone.SUPPORT, future);

		scn.StartGame();
		// An [elven] token on another card, none here.
		scn.AddTokensToCard(count, 1);
		scn.SetFreepsDrawDeck(top, second);
		assertEquals(0, scn.GetCultureTokensOn(future));
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertSame(top, scn.GetFreepsTopOfDeck());

		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(future));
		scn.FreepsUseCardAction(future);

		// Only the discard can be paid: no choice of costs.
		assertInDiscard(future);
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertTrue(scn.FreepsHasCardChoiceAvailable(top, false));
		scn.DismissRevealedCards();
		scn.FreepsChooseNo();

		assertSame(top, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithAnEmptyDrawDeckTheManeuverAbilityIsPaidForAndRevealsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var future = scn.GetFreepsCard("future");
		scn.MoveCompanionsToTable("legolas");
		scn.MoveCardsToSupportArea(future);
		assertInZone(Zone.SUPPORT, future);

		scn.StartGame();
		scn.AddTokensToCard(future, 2);
		scn.SetFreepsDrawDeck();
		assertEquals(2, scn.GetCultureTokensOn(future));
		assertEquals(0, scn.GetFreepsDeckCount());

		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		// An ability with a cost is offered whatever its effect can do.
		assertTrue(scn.FreepsActionAvailable(future));
		scn.FreepsUseCardAction(future);
		scn.FreepsChooseOption("token");
		assertEquals(1, scn.GetCultureTokensOn(future));

		// Nothing is shown, and with no card revealed nothing is asked.
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertFalse(scn.FreepsDecisionAvailable("beneath your draw deck"));
		assertEquals(0, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var future = scn.GetFreepsCard("future");
		scn.MoveCompanionsToTable("aragorn");
		scn.MoveCardsToSupportArea(future);
		assertInZone(Zone.SUPPORT, future);

		scn.StartGame();
		scn.AddTokensToCard(future, 3);
		assertEquals(3, scn.GetCultureTokensOn(future));

		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(future));
		assertEquals(3, scn.GetCultureTokensOn(future));
	}
}
