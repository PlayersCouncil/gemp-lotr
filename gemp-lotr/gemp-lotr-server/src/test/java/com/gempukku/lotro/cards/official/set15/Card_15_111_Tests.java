package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_111_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("menace", "15_111");
					put("menace2", "15_111");
					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc minion, strength 7; his text answers only a weapon played on him

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used
					// Cards of the Free Peoples draw deck, two of a kind so that the top card after the shuffle is known.
					put("guardian1", "17_7");     // Elven Guardian: twilight cost 2
					put("guardian2", "17_7");
					put("gandalf1", "1_72");      // Gandalf, Friend of the Shirefolk: twilight cost 4
					put("gandalf2", "1_72");
					put("assassin1", "1_262");    // Orc Assassin: a tracker printed at twilight cost 2
					put("assassin2", "1_262");
					put("fearing", "2_100");      // Fearing the Worst: the twilight cost of each search card and each tracker is +2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MoriaMenaceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Moria Menace
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Skirmish:</b> Exert this minion to make the Free Peoples player shuffle his or her draw deck and reveal the top card of that draw deck. Make this minion strength +X, where X is the twilight cost of the revealed card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("menace");

		assertEquals("Moria Menace", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityExertsThisMinionShufflesTheFreePeoplesDeckAndAddsTheTwilightCostOfTheTopCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf1 = scn.GetFreepsCard("gandalf1");
		var gandalf2 = scn.GetFreepsCard("gandalf2");
		scn.MoveCompanionsToTable(aragorn);

		var menace = scn.GetShadowCard("menace");
		var menace2 = scn.GetShadowCard("menace2");

		scn.StartGame();
		scn.SetFreepsDrawDeck(gandalf1, gandalf2);
		scn.MoveMinionsToTable(menace, menace2);

		// Both copies in Aragorn's skirmish.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, menace);
		scn.ShadowAssignToMinions(aragorn, menace2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(menace));
		assertEquals(0, scn.GetWoundsOn(menace2));
		assertEquals(7, scn.GetStrength(menace));
		assertEquals(7, scn.GetStrength(menace2));
		assertTrue(scn.ShadowActionAvailable(menace));
		scn.ShadowUseCardAction(menace);

		// The Free Peoples deck is shuffled, and its top card shown to both players.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf1, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(gandalf1, false));
		scn.DismissRevealedCards();

		// Gandalf costs 4: 7 + 4 = 11.  The exertion and the bonus are this copy's alone.
		assertEquals(1, scn.GetWoundsOn(menace));
		assertEquals(11, scn.GetStrength(menace));
		assertEquals(0, scn.GetWoundsOn(menace2));
		assertEquals(7, scn.GetStrength(menace2));
		// A reveal moves nothing.
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void XIsThePrintedCostOfTheRevealedCardAndTheBonusEndsWithTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var fearing = scn.GetFreepsCard("fearing");
		var assassin1 = scn.GetFreepsCard("assassin1");
		var assassin2 = scn.GetFreepsCard("assassin2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(fearing);

		var menace = scn.GetShadowCard("menace");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, fearing);
		scn.SetFreepsDrawDeck(assassin1, assassin2);
		scn.MoveMinionsToTable(menace, chieftain);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, menace}, new PhysicalCardImpl[]{frodo, chieftain});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Fearing the Worst makes the Assassin cost 4 as the game reads it in the deck; it is printed at 2.
		assertEquals(4, scn.GetTwilightCost(assassin1));
		assertEquals(7, scn.GetStrength(menace));
		scn.ShadowUseCardAction(menace);
		scn.DismissRevealedCards();

		// 7 + 2 = 9, not 7 + 4 = 11; in force at the next decision of the skirmish.
		assertEquals(9, scn.GetStrength(menace));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// The Menace, 9 against Aragorn's 8, wins; in the next skirmish the bonus is gone.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(7, scn.GetStrength(menace));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnUnassignedMenaceMayUseItDuringAnotherSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guardian1 = scn.GetFreepsCard("guardian1");
		var guardian2 = scn.GetFreepsCard("guardian2");
		scn.MoveCompanionsToTable(aragorn);

		var menace = scn.GetShadowCard("menace");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SetFreepsDrawDeck(guardian1, guardian2);
		scn.MoveMinionsToTable(menace, chieftain);

		// The Menace is left out of the assignments; the text names no skirmish of its own.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, chieftain);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertFalse(scn.IsCharAssigned(menace));
		assertEquals(0, scn.GetWoundsOn(menace));
		assertEquals(7, scn.GetStrength(menace));
		assertTrue(scn.ShadowActionAvailable(menace));
		scn.ShadowUseCardAction(menace);
		scn.DismissRevealedCards();

		// Elven Guardian costs 2: 7 + 2 = 9.
		assertEquals(1, scn.GetWoundsOn(menace));
		assertEquals(9, scn.GetStrength(menace));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileThisMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var menace = scn.GetShadowCard("menace");
		var menace2 = scn.GetShadowCard("menace2");

		scn.StartGame();
		scn.MoveMinionsToTable(menace, menace2);
		// Vitality 2 with 1 wound: it cannot exert.  The other copy, in the same skirmish, can.
		scn.AddWoundsToChar(menace, 1);
		assertEquals(1, scn.GetWoundsOn(menace));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, menace);
		scn.ShadowAssignToMinions(aragorn, menace2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(menace));
		assertTrue(scn.ShadowActionAvailable(menace2));
	}

	@Test
	public void WithTheFreePeoplesDeckEmptyItIsOfferedAndTheExertionBuysNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var menace = scn.GetShadowCard("menace");

		scn.StartGame();
		scn.MoveMinionsToTable(menace);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, menace);
		scn.MoveCardsToDiscard(new ArrayList<>(scn.GetFreepsDrawDeck()).toArray(new PhysicalCardImpl[0]));
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(menace));
		assertEquals(7, scn.GetStrength(menace));
		// A reveal a cost buys is offered with the deck empty (Q-0163).
		assertTrue(scn.ShadowActionAvailable(menace));
		scn.ShadowUseCardAction(menace);

		assertEquals(1, scn.GetWoundsOn(menace));
		assertEquals(7, scn.GetStrength(menace));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
