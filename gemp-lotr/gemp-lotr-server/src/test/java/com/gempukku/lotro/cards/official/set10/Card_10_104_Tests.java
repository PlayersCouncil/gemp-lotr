package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_104_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("present", "10_104");
					put("appetite", "1_294");   // Hobbit Appetite: a [shire] Fellowship event
					put("appetite2", "1_294");
					put("deftness", "1_293");   // Halfling Deftness: a [shire] Skirmish event
					put("stealth", "1_298");    // Hobbit Stealth: a [shire] Skirmish event of a third title
					put("sword", "1_299");      // Hobbit Sword: a [shire] card that is no event
					put("swordarm", "1_116");   // Swordarm of the White Tower: a [gondor] event

					put("savage", "1_151");     // Uruk Savage: a minion with nothing but damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BirthdayPresentStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Birthday Present
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Fellowship:</b> Add a burden to choose 2 [shire] events with different card titles from your discard pile. Choose an opponent and make him or her choose 1 of those cards for you to take into hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("present");

		assertEquals("Birthday Present", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipAbilityAddsABurdenAndTheShadowPlayerChoosesWhichOfTwoShireEventsIsTakenIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var present = scn.GetFreepsCard("present");
		var appetite = scn.GetFreepsCard("appetite");
		var appetite2 = scn.GetFreepsCard("appetite2");
		var deftness = scn.GetFreepsCard("deftness");
		var stealth = scn.GetFreepsCard("stealth");
		var sword = scn.GetFreepsCard("sword");
		var swordarm = scn.GetFreepsCard("swordarm");
		scn.MoveCardsToSupportArea(present);
		scn.MoveCardsToDiscard(appetite, appetite2, deftness, stealth, sword, swordarm);

		scn.StartGame();

		assertInDiscard(appetite, appetite2, deftness, stealth, sword, swordarm);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsActionAvailable(present));
		scn.FreepsUseCardAction(present);
		assertEquals(1, scn.GetBurdens());

		// Three [shire] events of three titles, of two timewords: the second Hobbit Appetite is no fourth choice,
		// and neither the [shire] possession nor the [gondor] event is offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(appetite, deftness, stealth));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword, swordarm));
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(appetite, deftness);

		// The opponent chooses 1 of the two chosen, and is not offered the third.
		assertTrue(scn.ShadowHasCardChoiceAvailable(appetite, deftness));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(stealth));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(deftness);

		assertInHand(deftness);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInDiscard(appetite, appetite2, stealth, sword, swordarm);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AbilityIsOfferedInTheFellowshipPhaseAndNotInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var present = scn.GetFreepsCard("present");
		var appetite = scn.GetFreepsCard("appetite");
		var deftness = scn.GetFreepsCard("deftness");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(present);
		scn.MoveCardsToDiscard(appetite, deftness);

		scn.StartGame();

		assertInDiscard(appetite, deftness);
		assertTrue(scn.FreepsActionAvailable(present));

		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(present));
		assertEquals(0, scn.GetBurdens());
	}

	@Test
	public void WithOnlyShireCardsThatAreNoEventsOrEventsOfAnotherCultureTheBurdenIsAddedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var present = scn.GetFreepsCard("present");
		var sword = scn.GetFreepsCard("sword");
		var swordarm = scn.GetFreepsCard("swordarm");
		var shadowAppetite = scn.GetShadowCard("appetite");
		scn.MoveCardsToSupportArea(present);
		scn.MoveCardsToDiscard(sword, swordarm);
		// The opponent's own [shire] event, in his own discard pile.
		scn.MoveCardsToDiscard(shadowAppetite);

		scn.StartGame();

		assertInDiscard(sword, swordarm, shadowAppetite);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsActionAvailable(present));
		scn.FreepsUseCardAction(present);
		assertEquals(1, scn.GetBurdens());

		// Nothing to choose: neither player is asked, and nothing is taken into hand.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertInDiscard(sword, swordarm, shadowAppetite);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithCopiesOfOneShireEventAndNoOtherOneCopyOfThatEventIsTakenIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var present = scn.GetFreepsCard("present");
		var appetite = scn.GetFreepsCard("appetite");
		var appetite2 = scn.GetFreepsCard("appetite2");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCardsToSupportArea(present);
		scn.MoveCardsToDiscard(appetite, appetite2, sword);

		scn.StartGame();

		assertInDiscard(appetite, appetite2, sword);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetBurdens());
		scn.FreepsUseCardAction(present);
		assertEquals(1, scn.GetBurdens());

		// CR 4.2 Section Three: with only copies of one [shire] event, that event is taken into hand. One copy is
		// set aside, and a choice among cards set aside is put even with one card in it: the player chooses that
		// one, and the opponent then chooses it as the 1 of those cards.
		assertTrue(scn.FreepsHasCardChoiceAvailable(appetite));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword));
		assertEquals(1, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCards(appetite);
		assertTrue(scn.ShadowHasCardChoiceAvailable(appetite));
		assertEquals(1, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCards(appetite);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertEquals("Hobbit Appetite", scn.GetFreepsHand().get(0).getBlueprint().getTitle());
		assertInDiscard(sword);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
