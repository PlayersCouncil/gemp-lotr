package com.gempukku.lotro.cards.official.set09;

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

public class Card_09_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("glorfindel", "9_16");
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: strength 8, vitality 4, cost 4
					put("knight", "5_35");     // Gondorian Knight: a companion of cost 2 with no text; also a card on top of the deck
					put("guard", "1_7");       // Dwarf Guard: cost 1, here only as a card on top of the deck
					put("fearing", "2_100");   // Fearing the Worst: the twilight cost of each tracker is +2
					put("assassin", "1_262");  // Orc Assassin: a tracker printed at cost 2, here only on top of the deck

					put("lemenya", "1_232");   // Ulaire Lemenya: [wraith] Nazgul, strength 9, vitality 2; weakens only bearers of [wraith] conditions
					put("khamul", "103_61");   // Ulaire Attea, Khamul of the East: a [raider] Nazgul, strength 12, vitality 3; fierce; his text needs 3 burdens
					put("nelya", "2_84");      // Ulaire Nelya: [wraith] Nazgul, strength 10, vitality 3; nothing else below 2 burdens
					put("creeper", "8_75");    // Morgul Creeper: a [wraith] Orc, strength 8, vitality 2; archery only at 3 burdens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("glorfindel", "9_16");
					put("bow", "1_41");         // Elven Bow: twilight cost 1, here only as the card on top of the draw deck
					// Úlairë Cantëa, Lieutenant of Dol Guldur: Nazgûl, strength 10; fierce, and an ability that needs a weapon to discard
					put("cantea", "1_230");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GlorfindelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Glorfindel, Revealed in Wrath
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 9
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: When Glorfindel is in your starting fellowship, his twilight cost is -2.
		 * <b>Skirmish:</b> Reveal the top card of your draw deck. You may exert Glorfindel to make a Nazgûl he is
		 * skirmishing strength -X, where X is the twilight cost of the card revealed.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("glorfindel");

		assertEquals("Glorfindel", card.getBlueprint().getTitle());
		assertEquals("Revealed in Wrath", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void InTheStartingFellowshipHeCostsTwoLessAndNoOtherCompanionDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glorfindel = scn.GetFreepsCard("glorfindel");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");

		// While a player chooses his starting fellowship the game keeps the total of what he has played in the
		// twilight pool.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsHasCardChoiceAvailable(glorfindel, aragorn, knight));
		// 4 - 2; Aragorn and the Knight keep their printed 4 and 2.
		assertEquals(2, scn.GetTwilightCost(glorfindel));
		assertEquals(4, scn.GetTwilightCost(aragorn));
		assertEquals(2, scn.GetTwilightCost(knight));

		scn.FreepsChooseCard(glorfindel);

		assertInZone(Zone.FREE_CHARACTERS, glorfindel);
		assertEquals(2, scn.GetTwilight());
		// The Knight's 2 still fits under the 4, Aragorn's 4 no longer does.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(2, scn.GetTwilightCost(knight));
	}

	@Test
	public void PlayedFromHandOutsideTheStartingFellowshipHeCostsHisPrintedFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glorfindel = scn.GetFreepsCard("glorfindel");
		scn.MoveCardsToHand(glorfindel);

		scn.StartGame();

		assertEquals(4, scn.GetTwilightCost(glorfindel));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(glorfindel));
		scn.FreepsPlayCard(glorfindel);

		assertInZone(Zone.FREE_CHARACTERS, glorfindel);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RevealsTheTopCardAndExertsToMakeANazgulHeIsSkirmishingWeakerByItsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glorfindel = scn.GetFreepsCard("glorfindel");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(glorfindel, aragorn);

		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var nelya = scn.GetShadowCard("nelya");
		var creeper = scn.GetShadowCard("creeper");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, khamul, nelya, creeper);
		// The Knight (cost 2) is the top card, the Guard (cost 1) the one under it.
		scn.MoveCardsToTopOfDeck(guard, knight);

		// Not in the assignment phase, the last in which the Free Peoples player acts before the skirmishes.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(glorfindel));
		scn.PassAssignmentActions();

		// Glorfindel fights two Nazgul, one of them not [wraith], and a [wraith] Orc; a third Nazgul waits for Aragorn.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{glorfindel, lemenya}, new PhysicalCardImpl[]{aragorn, nelya});
		scn.ShadowAssignToMinions(glorfindel, khamul, creeper);
		scn.FreepsResolveSkirmish(glorfindel);

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetWoundsOn(glorfindel));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(8, scn.GetStrength(creeper));
		assertTrue(scn.FreepsActionAvailable(glorfindel));
		scn.FreepsUseCardAction(glorfindel);

		// The top card, and only the top card, is shown to both players.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(knight, false));
		scn.DismissRevealedCards();

		// Nothing has been paid or changed until he chooses to exert.
		assertEquals(0, scn.GetWoundsOn(glorfindel));
		assertEquals(12, scn.GetStrength(khamul));
		scn.FreepsChooseYes();

		// The two Nazgul in his skirmish; not the Orc beside them, nor the Nazgul assigned to Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(creeper, nelya));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(khamul);

		// Gondorian Knight costs 2: 12 - 2 = 10.
		assertEquals(1, scn.GetWoundsOn(glorfindel));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(10, scn.GetStrength(khamul));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(8, scn.GetStrength(creeper));
		// The card revealed stays where it was.
		assertEquals(knight, scn.GetFreepsTopOfDeck());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();

		// A second use, with another card on top. Dwarf Guard costs 1: 9 - 1 = 8.
		scn.MoveCardsToTopOfDeck(guard);
		scn.FreepsUseCardAction(glorfindel);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, false));
		scn.DismissRevealedCards();
		scn.FreepsChooseYes();
		scn.FreepsChooseCard(lemenya);

		assertEquals(2, scn.GetWoundsOn(glorfindel));
		assertEquals(8, scn.GetStrength(lemenya));
		assertEquals(10, scn.GetStrength(khamul));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void DecliningToExertLeavesGlorfindelAndTheNazgulAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glorfindel = scn.GetFreepsCard("glorfindel");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(glorfindel);

		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.MoveCardsToTopOfDeck(knight);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(glorfindel, nelya);

		assertEquals(10, scn.GetStrength(nelya));
		scn.FreepsUseCardAction(glorfindel);
		scn.DismissRevealedCards();
		scn.FreepsChooseNo();

		assertEquals(0, scn.GetWoundsOn(glorfindel));
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(knight, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ThePenaltyLastsForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glorfindel = scn.GetFreepsCard("glorfindel");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(glorfindel, aragorn);

		var nelya = scn.GetShadowCard("nelya");
		var creeper = scn.GetShadowCard("creeper");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, creeper);
		scn.MoveCardsToTopOfDeck(knight);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{glorfindel, nelya}, new PhysicalCardImpl[]{aragorn, creeper});
		scn.FreepsResolveSkirmish(glorfindel);

		assertEquals(10, scn.GetStrength(nelya));
		scn.FreepsUseCardAction(glorfindel);
		scn.DismissRevealedCards();
		scn.FreepsChooseYes();

		// Gondorian Knight costs 2: 10 - 2 = 8.
		assertEquals(8, scn.GetStrength(nelya));
		scn.ShadowPass();
		scn.FreepsPass();

		// Glorfindel 9 beats 8 without overwhelming it; the Nazgul survives the wound.
		assertEquals(1, scn.GetWoundsOn(nelya));
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(10, scn.GetStrength(nelya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoNazgulInHisSkirmishHeMayStillExertAndNoMinionIsMadeWeaker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glorfindel = scn.GetFreepsCard("glorfindel");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(glorfindel);

		var creeper = scn.GetShadowCard("creeper");

		scn.StartGame();
		scn.MoveMinionsToTable(creeper);
		scn.MoveCardsToTopOfDeck(knight);

		scn.SkipToAssignments();
		// Morgul Creeper is [wraith], as most Nazgul are, but an Orc.
		scn.FreepsAssignAndResolve(glorfindel, creeper);

		assertEquals(8, scn.GetStrength(creeper));
		assertTrue(scn.FreepsActionAvailable(glorfindel));
		scn.FreepsUseCardAction(glorfindel);
		assertTrue(scn.ShadowHasCardChoiceAvailable(knight, false));
		scn.DismissRevealedCards();

		// An exertion he can pay is his to pay, though it will buy nothing.
		scn.FreepsChooseYes();

		assertEquals(1, scn.GetWoundsOn(glorfindel));
		assertEquals(8, scn.GetStrength(creeper));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ANazgulInAnotherCompanionsSkirmishIsNotMadeWeaker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glorfindel = scn.GetFreepsCard("glorfindel");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(glorfindel, aragorn);

		var nelya = scn.GetShadowCard("nelya");
		var creeper = scn.GetShadowCard("creeper");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, creeper);
		scn.MoveCardsToTopOfDeck(knight);

		scn.SkipToAssignments();
		// Nelya fights Aragorn and is in a skirmish now; Glorfindel's own is against the Orc, and comes after.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, nelya}, new PhysicalCardImpl[]{glorfindel, creeper});
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(glorfindel));
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(8, scn.GetStrength(creeper));
		assertTrue(scn.FreepsActionAvailable(glorfindel));
		scn.FreepsUseCardAction(glorfindel);
		scn.DismissRevealedCards();
		scn.FreepsChooseYes();

		// Gondorian Knight costs 2, and would have made her 8: she is not the Nazgul he is skirmishing.
		assertEquals(1, scn.GetWoundsOn(glorfindel));
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(8, scn.GetStrength(creeper));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void XIsThePrintedCostOfTheRevealedCardWhateverACardInPlayMakesItCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glorfindel = scn.GetFreepsCard("glorfindel");
		var fearing = scn.GetFreepsCard("fearing");
		var assassin = scn.GetFreepsCard("assassin");
		scn.MoveCompanionsToTable(glorfindel);
		scn.MoveCardsToSupportArea(fearing);

		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.MoveCardsToTopOfDeck(assassin);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(glorfindel, nelya);

		// Fearing the Worst makes the Assassin cost 4 as the game reads it in the deck; it is printed at 2.
		assertEquals(4, scn.GetTwilightCost(assassin));
		assertEquals(10, scn.GetStrength(nelya));
		scn.FreepsUseCardAction(glorfindel);
		scn.DismissRevealedCards();
		scn.FreepsChooseYes();

		// 10 - 2 = 8 (not 10 - 4 = 6).
		assertEquals(1, scn.GetWoundsOn(glorfindel));
		assertEquals(8, scn.GetStrength(nelya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithAnEmptyDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glorfindel = scn.GetFreepsCard("glorfindel");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(glorfindel);

		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(glorfindel, nelya);
		// One card is left in his draw deck.
		scn.MoveCardsToDiscard(new ArrayList<>(scn.GetFreepsDrawDeck()).toArray(new PhysicalCardImpl[0]));
		scn.MoveCardsToTopOfDeck(knight);
		scn.FreepsResolveSkirmish(glorfindel);

		// With a last card to reveal the ability is offered: it is shown, and he does not exert.
		assertEquals(1, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsActionAvailable(glorfindel));
		scn.FreepsUseCardAction(glorfindel);
		assertTrue(scn.ShadowHasCardChoiceAvailable(knight, false));
		scn.DismissRevealedCards();
		scn.FreepsChooseNo();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// That card gone, there is nothing to reveal and so no X for the rest to read: not offered (Q-0148).
		scn.MoveCardsToDiscard(knight);
		scn.ShadowPass();
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(glorfindel));
		assertEquals(10, scn.GetStrength(nelya));
		assertFalse(scn.FreepsActionAvailable(glorfindel));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ExhaustedHeStillRevealsTheTopCardAndIsNotAskedToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var glorfindel = scn.GetFreepsCard("glorfindel");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(glorfindel);

		var cantea = scn.GetShadowCard("cantea");

		scn.StartGame();
		scn.MoveMinionsToTable(cantea);
		// The card on top costs 1 and not 0, or a penalty wrongly given would not show in the Nazgûl's strength.
		scn.MoveCardsToTopOfDeck(bow);
		// Vitality 3: with 1 wound he can exert once more.
		scn.AddWoundsToChar(glorfindel, 1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(glorfindel, cantea);
		scn.FreepsResolveSkirmish(glorfindel);

		// The control: able to exert, he is asked, and exerting makes the Nazgûl 10 - 1.
		assertEquals(10, scn.GetStrength(cantea));
		scn.FreepsUseCardAction(glorfindel);
		scn.DismissRevealedCards();
		scn.FreepsChooseYes();
		assertEquals(2, scn.GetWoundsOn(glorfindel));
		assertEquals(9, scn.GetStrength(cantea));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();

		// With 2 wounds he is exhausted.  The reveal has no price, so the ability is still his to use.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(glorfindel));
		scn.FreepsUseCardAction(glorfindel);

		// The top card is shown to both players.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(bow, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(bow, false));
		scn.DismissRevealedCards();

		// No question follows: he is not exerted (a third wound would have killed him) and the Nazgûl is not
		// weakened a second time (9, where another exertion would have read 9 - 1).
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(2, scn.GetWoundsOn(glorfindel));
		assertInZone(Zone.FREE_CHARACTERS, glorfindel);
		assertEquals(9, scn.GetStrength(cantea));
		assertEquals(bow, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
