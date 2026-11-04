package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_169_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merrier", "11_169");
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a Hobbit companion who is [gondor]; a skirmish ability used only on purpose
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheMoreTheMerrierStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: The More, The Merrier
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 6
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Toil 2</b>. <helper>(For each [shire] character you exert when playing this, its twilight cost is -2.)</helper>
		 * Spot 2 [shire] companions to remove 2 burdens.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("merrier");

		assertEquals("The More, The Merrier", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.TOIL));
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(6, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingTwoShireCompanionsOfWhomOneIsNoHobbitRemovesTwoOfThreeBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merrier = scn.GetFreepsCard("merrier");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Frodo and Smeagol, who is no Hobbit, are the only two [shire] companions.
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToHand(merrier);

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		assertInHand(merrier);

		assertTrue(scn.FreepsPlayAvailable(merrier));
		scn.FreepsPlayCard(merrier);
		// Toil: nobody is exerted.
		scn.FreepsDeclineChoosing();

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertInDiscard(merrier);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOneShireCompanionBesideAGondorHobbitAndAShireAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merrier = scn.GetFreepsCard("merrier");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		// Frodo is the one [shire] companion; Pippin is a Hobbit companion of another culture and Bounder a
		// [shire] Hobbit who is no companion.
		scn.MoveCompanionsToTable(pippin);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(merrier);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		assertInHand(merrier);
		scn.FreepsRefreshDecision();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(merrier));
		assertEquals(2, scn.GetBurdens());
	}

	@Test
	public void WithOneBurdenItIsStillPlayedAndRemovesThatOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merrier = scn.GetFreepsCard("merrier");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(merrier);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		assertInHand(merrier);

		assertTrue(scn.FreepsPlayAvailable(merrier));
		scn.FreepsPlayCard(merrier);
		scn.FreepsDeclineChoosing();

		assertEquals(0, scn.GetBurdens());
		assertInDiscard(merrier);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertingTwoShireCharactersForToilTakesFourOffItsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merrier = scn.GetFreepsCard("merrier");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(merrier);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(bounder));

		scn.FreepsPlayCard(merrier);
		// Toil: any [shire] character may be exerted, the ally among them, and none need be; Aragorn is [gondor].
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, smeagol, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(smeagol, bounder);

		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// 6 - 2 - 2 for the two exertions
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertInDiscard(merrier);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
