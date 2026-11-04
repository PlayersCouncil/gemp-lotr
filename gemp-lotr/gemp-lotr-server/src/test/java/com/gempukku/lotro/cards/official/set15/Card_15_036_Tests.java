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

public class Card_15_036_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shepherd1", "15_36");
					put("shepherd2", "15_36");

					// Ents
					put("treebeard", "4_104");  // Treebeard, Oldest Living Thing: [gandalf] Ent ally, cost 4; a fellowship ability used only on purpose
					put("onod", "17_26");       // Woodland Onod: [gandalf] Ent companion, cost 4, strength 7; a skirmish ability used only on purpose
					// [gandalf] companions who are no Ents
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion, cost 4
					put("huorn", "9_25");       // Huorn: [gandalf] Tree companion; its own strength is never read here
					// Cards that are not [gandalf] characters
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion
					put("laststand", "18_21");  // Last Stand: a [gandalf] condition

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRevealScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shepherd", "15_36");
					put("axe", "1_3");          // Axe Strike: the top card of the draw deck
					// Cleaving Blow: the bottom card, a [dwarven] event and so no [gandalf] character to play
					put("blow", "1_5");
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShepherdoftheTreesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Shepherd of the Trees
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 5
		 * Type: Companion
		 * Subtype: Ent
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: While you can spot 4 Ents, this companion is strength +3.<br><b>Maneuver:</b> Exert this companion to reveal the bottom card of your draw deck. If it is a [gandalf] character, you may play it.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shepherd1");

		assertEquals("Shepherd of the Trees", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ENT, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void ManeuverAbilityExertsThisCompanionToRevealTheBottomCardOfItsOwnersDrawDeckToBothPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRevealScenario();

		var shepherd = scn.GetFreepsCard("shepherd");
		var axe = scn.GetFreepsCard("axe");
		var blow = scn.GetFreepsCard("blow");
		scn.MoveCompanionsToTable(shepherd);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(axe);
		scn.MoveCardsToBottomOfDeck(blow);
		assertInZone(Zone.DECK, axe, blow);
		scn.MoveMinionsToTable(scn.GetShadowCard("runner"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertSame(axe, scn.GetFreepsTopOfDeck());
		assertSame(blow, scn.GetFreepsBottomOfDeck());
		assertEquals(0, scn.GetWoundsOn(shepherd));
		assertTrue(scn.FreepsActionAvailable(shepherd));

		scn.FreepsUseCardAction(shepherd);
		assertEquals(1, scn.GetWoundsOn(shepherd));
		// Each player is shown the bottom card, and not the top one.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(blow, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(axe, false));
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(blow, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(axe, false));

		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();
		// It is no [gandalf] character: nothing is played, and it lies where it lay.
		assertSame(blow, scn.GetFreepsBottomOfDeck());
		assertInZone(Zone.DECK, blow);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsStrengthPlusThreeWhileFourEntsCanBeSpottedAndTheBonusDoesNotStack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shepherd1 = scn.GetFreepsCard("shepherd1");
		var shepherd2 = scn.GetFreepsCard("shepherd2");
		var onod = scn.GetFreepsCard("onod");
		// Three Ents: the two Shepherds and an Ent ally; two [gandalf] companions beside them who are no Ents.
		scn.MoveCompanionsToTable(shepherd1, shepherd2, scn.GetFreepsCard("gandalf"), scn.GetFreepsCard("huorn"));
		scn.MoveAlliesToTable(scn.GetFreepsCard("treebeard"));
		scn.MoveCardsToHand(onod);

		scn.StartGame();

		assertInZone(Zone.HAND, onod);
		assertEquals(7, scn.GetStrength(shepherd1));
		assertEquals(7, scn.GetStrength(shepherd2));

		scn.FreepsPlayCard(onod);

		assertInZone(Zone.FREE_CHARACTERS, onod);
		// Four Ents: each Shepherd 7 + 3, not 7 + 6 for the two copies; Woodland Onod, another Ent, stays 7.
		assertEquals(10, scn.GetStrength(shepherd1));
		assertEquals(10, scn.GetStrength(shepherd2));
		assertEquals(7, scn.GetStrength(onod));

		// Back to three, and the bonus is gone.
		scn.MoveCardsToDiscard(onod);
		assertEquals(7, scn.GetStrength(shepherd1));
		assertEquals(7, scn.GetStrength(shepherd2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARevealedGandalfCompanionOrAllyMayBePlayedAndOnlyTheCopyUsedExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shepherd1 = scn.GetFreepsCard("shepherd1");
		var shepherd2 = scn.GetFreepsCard("shepherd2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var treebeard = scn.GetFreepsCard("treebeard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var shadowGandalf = scn.GetShadowCard("gandalf");
		scn.MoveCompanionsToTable(shepherd1, shepherd2);

		scn.StartGame();
		// Aragorn on top, Gandalf at the bottom with Treebeard above him; the other player's bottom card is a
		// [gandalf] character too.
		scn.SetDrawDeck(scn.P1(), aragorn, treebeard, gandalf);
		scn.MoveCardsToBottomOfDeck(shadowGandalf);
		assertInZone(Zone.DECK, aragorn, treebeard, gandalf, shadowGandalf);
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertSame(aragorn, scn.GetFreepsTopOfDeck());
		assertSame(gandalf, scn.GetFreepsBottomOfDeck());
		assertSame(treebeard, scn.GetFromBottomOfFreepsDeck(2));
		assertSame(shadowGandalf, scn.GetShadowBottomOfDeck());
		assertEquals(0, scn.GetWoundsOn(shepherd1));
		assertEquals(0, scn.GetWoundsOn(shepherd2));

		scn.FreepsUseCardAction(shepherd1);

		assertEquals(1, scn.GetWoundsOn(shepherd1));
		assertEquals(0, scn.GetWoundsOn(shepherd2));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(gandalf, false));
		scn.DismissRevealedCards();

		// A [gandalf] Wizard companion: the play is offered, and taken.
		assertTrue(scn.FreepsDecisionAvailable("Would you like to play"));
		scn.FreepsChooseYes();

		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertSame(treebeard, scn.GetFreepsBottomOfDeck());
		assertSame(shadowGandalf, scn.GetShadowBottomOfDeck());
		assertInZone(Zone.DECK, shadowGandalf);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.ShadowPass();
		scn.FreepsUseCardAction(shepherd2);

		assertEquals(1, scn.GetWoundsOn(shepherd1));
		assertEquals(1, scn.GetWoundsOn(shepherd2));
		assertTrue(scn.FreepsHasCardChoiceAvailable(treebeard, false));
		scn.DismissRevealedCards();

		// A [gandalf] Ent ally: offered and played too.
		assertTrue(scn.FreepsDecisionAvailable("Would you like to play"));
		scn.FreepsChooseYes();

		assertInZone(Zone.SUPPORT, treebeard);
		assertSame(aragorn, scn.GetFreepsBottomOfDeck());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DecliningThePlayLeavesTheRevealedCompanionAtTheBottomOfTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shepherd1 = scn.GetFreepsCard("shepherd1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(shepherd1);

		scn.StartGame();
		scn.SetDrawDeck(scn.P1(), aragorn, gandalf);
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertSame(gandalf, scn.GetFreepsBottomOfDeck());
		assertEquals(2, scn.GetFreepsDeckCount());
		int twilight = scn.GetTwilight();

		scn.FreepsUseCardAction(shepherd1);
		scn.DismissRevealedCards();

		assertTrue(scn.FreepsDecisionAvailable("Would you like to play"));
		scn.FreepsChooseNo();

		assertInZone(Zone.DECK, gandalf);
		assertSame(gandalf, scn.GetFreepsBottomOfDeck());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ACharacterOfAnotherCultureOrAGandalfConditionRevealedIsNotOfferedToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shepherd1 = scn.GetFreepsCard("shepherd1");
		var shepherd2 = scn.GetFreepsCard("shepherd2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var laststand = scn.GetFreepsCard("laststand");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(shepherd1, shepherd2);

		scn.StartGame();
		scn.SetDrawDeck(scn.P1(), gandalf, laststand, aragorn);
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertSame(aragorn, scn.GetFreepsBottomOfDeck());

		scn.FreepsUseCardAction(shepherd1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, false));
		scn.DismissRevealedCards();

		// A [gondor] companion: nothing asked, and he lies where he lay.
		assertFalse(scn.FreepsDecisionAvailable("Would you like to play"));
		assertSame(aragorn, scn.GetFreepsBottomOfDeck());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.MoveCardsToTopOfDeck(aragorn);
		assertSame(laststand, scn.GetFreepsBottomOfDeck());
		scn.ShadowPass();
		scn.FreepsUseCardAction(shepherd2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(laststand, false));
		scn.DismissRevealedCards();

		// A [gandalf] card that is no character: nothing asked either.
		assertFalse(scn.FreepsDecisionAvailable("Would you like to play"));
		assertSame(laststand, scn.GetFreepsBottomOfDeck());
		assertInZone(Zone.DECK, laststand);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnExhaustedShepherdCannotUseTheAbilityWhileAnotherCopyCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shepherd1 = scn.GetFreepsCard("shepherd1");
		var shepherd2 = scn.GetFreepsCard("shepherd2");
		scn.MoveCompanionsToTable(shepherd1, shepherd2);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(shepherd1, 3);

		scn.StartGame();

		assertEquals(3, scn.GetWoundsOn(shepherd1));
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(shepherd1));
		assertTrue(scn.FreepsActionAvailable(shepherd2));
	}

	@Test
	public void WithAnEmptyDrawDeckTheAbilityIsStillOfferedAndExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shepherd1 = scn.GetFreepsCard("shepherd1");
		scn.MoveCompanionsToTable(shepherd1);

		scn.StartGame();
		scn.SetDrawDeck(scn.P1());
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(shepherd1));
		assertTrue(scn.FreepsActionAvailable(shepherd1));

		scn.FreepsUseCardAction(shepherd1);

		assertEquals(1, scn.GetWoundsOn(shepherd1));
		assertFalse(scn.FreepsDecisionAvailable("Would you like to play"));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
