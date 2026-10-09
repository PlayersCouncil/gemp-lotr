package com.gempukku.lotro.cards.unofficial.pc.errata.set09;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_016_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("glorfindel", "59_16");
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: strength 8, vitality 4
					put("knight", "5_35");     // Gondorian Knight: cost 2, here only as a card on top of the deck
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
		 * Game Text: <b>Skirmish:</b> Reveal the top card of your draw deck. You may exert Glorfindel to make a Nazgûl he is skirmishing strength –X, where X is the twilight cost of the card revealed.
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
	public void IsPlayedFromHandForFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glorfindel = scn.GetFreepsCard("glorfindel");
		scn.MoveCardsToHand(glorfindel);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(glorfindel));
		scn.FreepsPlayCard(glorfindel);

		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, glorfindel);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RevealsTheTopCardAndExertsToMakeANazgulHeIsSkirmishingWeakerByItsCostUntilHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
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
		scn.ShadowPass();

		// Vitality 3 with 2 wounds: exhausted. The card is still revealed, and he is not asked to exert.
		assertTrue(scn.FreepsActionAvailable(glorfindel));
		scn.FreepsUseCardAction(glorfindel);
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, false));
		scn.DismissRevealedCards();

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
}
