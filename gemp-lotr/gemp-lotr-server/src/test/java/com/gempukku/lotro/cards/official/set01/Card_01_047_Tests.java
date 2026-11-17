package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_047_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gwemegil", "1_47");
					put("arwen", "1_30");         // Arwen, Daughter of Elrond: Elf companion, strength 6, vitality 3; +3 only against a Nazgûl
					put("legolas", "1_50");       // Legolas, Greenleaf: an [elven] Elf companion who is not Arwen
					put("boromir", "1_97");       // Boromir, Son of Denethor: strength 7; a skirmish ability never used
					put("runner", "1_178");       // Goblin Runner: strength 5, vitality 1; its play trigger is never set off
					// Axe Strike: a Dwarven skirmish event, never playable here (no Dwarf); cards to hold and discard
					put("chaff1", "1_3");
					put("chaff2", "1_3");
					put("chaff3", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gwemegil", "1_47");
					// Arwen, Lady of Rivendell: the ally Arwen, strength 6, vitality 3, home 3F; her text is about Aragorn,
					// who is not here
					put("arwen", "102_6");
					put("boromir", "1_97");       // Boromir, Son of Denethor: strength 7; a skirmish ability never used
					put("runner", "1_178");       // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GwemegilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Gwemegil
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be Arwen.
		 * She is damage +1.
		 * Skirmish: Exert Arwen or discard 2 cards from hand to make her strength +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gwemegil");

		assertEquals("Gwemegil", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnlyOnArwenAndMakesHerDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwemegil = scn.GetFreepsCard("gwemegil");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(arwen, gwemegil);

		scn.StartGame();

		// Frodo and an [elven] Elf of another title: nobody may bear it.
		assertFalse(scn.FreepsPlayAvailable(gwemegil));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(arwen);
		assertTrue(scn.FreepsPlayAvailable(gwemegil));
		assertEquals(0, scn.GetKeywordCount(arwen, Keyword.DAMAGE));

		// Arwen is the only bearer it may have, so nobody is asked.
		scn.FreepsPlayCard(gwemegil);
		assertAttachedTo(gwemegil, arwen);
		assertEquals(1, scn.GetKeywordCount(arwen, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(legolas, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysOnArwenTheAllyAndMakesHerDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var gwemegil = scn.GetFreepsCard("gwemegil");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveAlliesToTable(arwen);
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(gwemegil);

		scn.StartGame();

		assertEquals(CardType.ALLY, arwen.getBlueprint().getCardType());
		assertEquals(0, scn.GetKeywordCount(arwen, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(gwemegil));

		scn.FreepsPlayCard(gwemegil);
		assertAttachedTo(gwemegil, arwen);
		assertEquals(1, scn.GetKeywordCount(arwen, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishActionExertsArwenToMakeHerStrengthPlus1UntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwemegil = scn.GetFreepsCard("gwemegil");
		var arwen = scn.GetFreepsCard("arwen");
		var frodo = scn.GetRingBearer();
		var chaff1 = scn.GetFreepsCard("chaff1");
		var chaff2 = scn.GetFreepsCard("chaff2");
		var chaff3 = scn.GetFreepsCard("chaff3");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, gwemegil);
		scn.MoveCardsToHand(chaff1, chaff2, chaff3);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, runner);
		scn.FreepsResolveSkirmish(arwen);

		// 6 + 2 for Gwemegil
		assertEquals(8, scn.GetStrength(arwen));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsActionAvailable(gwemegil));

		scn.FreepsUseCardAction(gwemegil);
		// Either cost can be paid: the player says which.
		assertTrue(scn.FreepsChoiceAvailable("Exert Arwen"));
		assertTrue(scn.FreepsChoiceAvailable("Discard 2 cards from hand"));
		scn.FreepsChoose("Exert Arwen");

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(9, scn.GetStrength(arwen));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(5, scn.GetStrength(runner));

		// Arwen wins and the Runner dies of its wound; the skirmish phase is over, and so is the bonus.
		scn.PassCurrentPhaseActions();
		assertInDiscard(runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(8, scn.GetStrength(arwen));
	}

	@Test
	public void SkirmishActionDiscardsTwoCardsFromHandInPlaceOfTheExertion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwemegil = scn.GetFreepsCard("gwemegil");
		var arwen = scn.GetFreepsCard("arwen");
		var frodo = scn.GetRingBearer();
		var chaff1 = scn.GetFreepsCard("chaff1");
		var chaff2 = scn.GetFreepsCard("chaff2");
		var chaff3 = scn.GetFreepsCard("chaff3");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, gwemegil);
		scn.MoveCardsToHand(chaff1, chaff2, chaff3);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, runner);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(8, scn.GetStrength(arwen));
		assertEquals(3, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(gwemegil);
		scn.FreepsChoose("Discard 2 cards from hand");
		assertTrue(scn.FreepsHasCardChoiceAvailable(chaff1, chaff2, chaff3));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(chaff1, chaff3);

		assertInDiscard(chaff1, chaff3);
		assertInZone(Zone.HAND, chaff2);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(9, scn.GetStrength(arwen));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithArwenExhaustedTheTwoCardsAreDiscardedWithNoChoiceOfCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwemegil = scn.GetFreepsCard("gwemegil");
		var arwen = scn.GetFreepsCard("arwen");
		var chaff1 = scn.GetFreepsCard("chaff1");
		var chaff2 = scn.GetFreepsCard("chaff2");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, gwemegil);
		scn.MoveCardsToHand(chaff1, chaff2);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, runner);
		scn.FreepsResolveSkirmish(arwen);
		// Vitality 3 with 2 wounds: she cannot exert.
		scn.AddWoundsToChar(arwen, 2);
		scn.FreepsRefreshDecision();

		assertEquals(8, scn.GetStrength(arwen));
		assertTrue(scn.FreepsActionAvailable(gwemegil));

		// Only the discard can be paid, and the hand is just two cards: nothing is asked.
		scn.FreepsUseCardAction(gwemegil);
		assertInDiscard(chaff1, chaff2);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(9, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithOneCardInHandArwenExertsWithNoChoiceOfCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwemegil = scn.GetFreepsCard("gwemegil");
		var arwen = scn.GetFreepsCard("arwen");
		var chaff1 = scn.GetFreepsCard("chaff1");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, gwemegil);
		scn.MoveCardsToHand(chaff1);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, runner);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(8, scn.GetStrength(arwen));

		scn.FreepsUseCardAction(gwemegil);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertInZone(Zone.HAND, chaff1);
		assertEquals(9, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileArwenIsExhaustedAndOnlyOneCardIsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwemegil = scn.GetFreepsCard("gwemegil");
		var arwen = scn.GetFreepsCard("arwen");
		var chaff1 = scn.GetFreepsCard("chaff1");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, gwemegil);
		scn.MoveCardsToHand(chaff1);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, runner);
		scn.FreepsResolveSkirmish(arwen);
		// Vitality 3 with 2 wounds, and one card in hand: neither cost can be paid.
		scn.AddWoundsToChar(arwen, 2);
		scn.FreepsRefreshDecision();

		assertFalse(scn.FreepsActionAvailable(gwemegil));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ArwenTheAllyPaysTheExertionAndIsStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var gwemegil = scn.GetFreepsCard("gwemegil");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveAlliesToTable(arwen);
		scn.AttachCardsTo(arwen, gwemegil);
		scn.MoveCompanionsToTable(boromir);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, runner);
		scn.FreepsResolveSkirmish(boromir);

		// 6 + 2 for Gwemegil; she is in no skirmish, and her ability asks nothing of that.
		assertEquals(8, scn.GetStrength(arwen));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(gwemegil));

		// The hand is empty: the exertion is the only cost, taken with no question.
		scn.FreepsUseCardAction(gwemegil);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(9, scn.GetStrength(arwen));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void BorneByAHinderedArwenItDiscardsTwoCardsToMakeNobodyStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwemegil = scn.GetFreepsCard("gwemegil");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		var chaff1 = scn.GetFreepsCard("chaff1");
		var chaff2 = scn.GetFreepsCard("chaff2");
		var chaff3 = scn.GetFreepsCard("chaff3");
		scn.MoveCompanionsToTable(arwen, boromir);
		scn.AttachCardsTo(arwen, gwemegil);
		scn.MoveCardsToHand(chaff1, chaff2, chaff3);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.HinderCard(arwen);
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, runner);
		scn.FreepsResolveSkirmish(boromir);

		assertTrue(scn.IsHindered(arwen));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsActionAvailable(gwemegil));

		// Arwen cannot exert: the discard is the cost, with no choice between the two.
		scn.FreepsUseCardAction(gwemegil);
		assertFalse(scn.FreepsChoiceAvailable("Exert Arwen"));
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(chaff1, chaff2);

		assertInDiscard(chaff1, chaff2);
		assertInZone(Zone.HAND, chaff3);
		assertEquals(0, scn.GetWoundsOn(arwen));
		// Nobody is strength +1.
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
