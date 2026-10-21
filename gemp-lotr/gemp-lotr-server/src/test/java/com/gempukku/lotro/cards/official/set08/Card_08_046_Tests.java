package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_046_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "8_46");
					// Dead Man of Dunharrow: [gondor] Wraith companion, strength 6, vitality 3, enduring; his "when you
					// play" exertions are not set off, since he is cheated in
					put("deadman", "10_27");
					// Oathbreaker: [gondor] Wraith companion, strength 6, vitality 3, enduring; a skirmish ability used
					// only on purpose
					put("oath", "8_41");
					// Boromir, Son of Denethor: [gondor] Man companion, no Wraith; a skirmish ability used only on purpose
					put("boromir", "1_97");

					// Cards to hold in hand, one of each type and of two cultures, with no text that reaches a test
					// from there: a [gondor] companion (Gondorian Knight), a [shire] possession (Hobbit Sword), a
					// [shire] event whose phase is the fellowship (Sorry About Everything), a [shire] condition (A
					// Talent for Not Being Seen)
					put("h1", "5_35");
					put("h2", "1_299");
					put("h3", "1_312");
					put("h4", "1_316");

					// Goblin Runner: [moria] Orc, strength 5, vitality 1; its "when you play" is not set off, since it
					// is cheated in
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SpectralSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Spectral Sword
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +1
		 * Game Text: Bearer must be a [gondor] Wraith.<br><b>Skirmish:</b> Discard 3 cards from hand to heal bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sword");

		assertEquals("Spectral Sword", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAGondorWraithAndNotOnAGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var deadman = scn.GetFreepsCard("deadman");
		var oath = scn.GetFreepsCard("oath");
		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(deadman, oath, boromir);
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		assertInHand(sword);

		assertTrue(scn.FreepsPlayAvailable(sword));
		scn.FreepsPlayCard(sword);
		// Two [gondor] Wraiths of different titles; not Boromir, a [gondor] Man, nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(deadman, oath));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(oath);

		assertAttachedTo(sword, oath);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsThreeCardsToHealBearerInAnotherCompanionsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var deadman = scn.GetFreepsCard("deadman");
		var oath = scn.GetFreepsCard("oath");
		var h1 = scn.GetFreepsCard("h1");
		var h2 = scn.GetFreepsCard("h2");
		var h3 = scn.GetFreepsCard("h3");
		var h4 = scn.GetFreepsCard("h4");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(deadman, oath);
		scn.AttachCardsTo(deadman, sword);
		scn.MoveCardsToHand(h1, h2, h3, h4);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.AddWoundsToChar(deadman, 2);
		scn.AddWoundsToChar(oath, 1);
		assertAttachedTo(sword, deadman);
		assertInHand(h1, h2, h3, h4);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetWoundsOn(deadman));
		assertEquals(1, scn.GetWoundsOn(oath));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(oath, runner);
		scn.FreepsResolveSkirmish(oath);

		// The bearer is in no skirmish; the text names none.
		assertTrue(scn.FreepsActionAvailable(sword));
		scn.FreepsUseCardAction(sword);
		// A companion, a possession and an event: the cost takes any three cards, of any type or culture.
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(h1, h2, h3);

		assertInDiscard(h1, h2, h3);
		assertInHand(h4);
		assertEquals(1, scn.GetFreepsHandCount());
		// One wound healed from the bearer; Oathbreaker, wounded and skirmishing, is not healed.
		assertEquals(1, scn.GetWoundsOn(deadman));
		assertEquals(1, scn.GetWoundsOn(oath));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOnlyTwoCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var deadman = scn.GetFreepsCard("deadman");
		var h1 = scn.GetFreepsCard("h1");
		var h2 = scn.GetFreepsCard("h2");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(deadman);
		scn.AttachCardsTo(deadman, sword);
		scn.MoveCardsToHand(h1, h2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.AddWoundsToChar(deadman, 1);
		assertAttachedTo(sword, deadman);
		assertEquals(1, scn.GetWoundsOn(deadman));
		assertEquals(2, scn.GetFreepsHandCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(deadman, runner);
		scn.FreepsResolveSkirmish(deadman);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sword));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetWoundsOn(deadman));
	}

	@Test
	public void IsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var deadman = scn.GetFreepsCard("deadman");
		var h1 = scn.GetFreepsCard("h1");
		var h2 = scn.GetFreepsCard("h2");
		var h3 = scn.GetFreepsCard("h3");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(deadman);
		scn.AttachCardsTo(deadman, sword);
		scn.MoveCardsToHand(h1, h2, h3);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.AddWoundsToChar(deadman, 1);
		assertAttachedTo(sword, deadman);
		assertEquals(1, scn.GetWoundsOn(deadman));
		assertEquals(3, scn.GetFreepsHandCount());

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sword));
	}

	@Test
	public void WithTheBearerUnwoundedTheCardsAreDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var deadman = scn.GetFreepsCard("deadman");
		var h1 = scn.GetFreepsCard("h1");
		var h2 = scn.GetFreepsCard("h2");
		var h3 = scn.GetFreepsCard("h3");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(deadman);
		scn.AttachCardsTo(deadman, sword);
		scn.MoveCardsToHand(h1, h2, h3);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		assertAttachedTo(sword, deadman);
		assertEquals(0, scn.GetWoundsOn(deadman));
		assertInHand(h1, h2, h3);
		assertEquals(3, scn.GetFreepsHandCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(deadman, runner);
		scn.FreepsResolveSkirmish(deadman);

		// The ability has a cost, so it is offered with no wound to heal.
		assertTrue(scn.FreepsActionAvailable(sword));
		scn.FreepsUseCardAction(sword);

		assertInDiscard(h1, h2, h3);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(deadman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
