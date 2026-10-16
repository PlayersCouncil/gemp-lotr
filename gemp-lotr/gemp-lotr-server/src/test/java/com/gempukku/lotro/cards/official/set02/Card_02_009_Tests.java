package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_009_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("works", "2_9");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; a skirmish ability only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf; a maneuver ability only

					put("axestrike", "1_3");    // Axe Strike: [dwarven] Skirmish event
					put("delving", "1_6");      // Delving: [dwarven] Fellowship event
					put("cleaving", "1_5");     // Cleaving Blow: [dwarven] Skirmish event
					put("axe", "1_9");          // Dwarven Axe: [dwarven] possession, no event
					put("sleep", "1_84");       // Sleep, Caradhras: [gandalf] event

					// Draw deck fillers: no text at all.
					put("filler1", "1_177");    // Goblin Patrol Troop
					put("filler2", "1_177");
					put("filler3", "1_177");
					put("filler4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreatWorksBegunThereStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Great Works Begun There
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Tale</b>. Plays to your support area.<br>Each time you play a Dwarf, you may discard the top 2 cards from your draw deck to take a [dwarven] event into hand from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("works");

		assertEquals("Great Works Begun There", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayingADwarfCompanionDiscardsTheTopTwoCardsToTakeADwarvenEventFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var works = scn.GetFreepsCard("works");
		var gimli = scn.GetFreepsCard("gimli");
		var axestrike = scn.GetFreepsCard("axestrike");
		var delving = scn.GetFreepsCard("delving");
		var axe = scn.GetFreepsCard("axe");
		var sleep = scn.GetFreepsCard("sleep");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		// The other player's copy of a [dwarven] event, in his own discard pile: no copy of it is in this player's.
		var shadowCleaving = scn.GetShadowCard("cleaving");
		scn.MoveCardsToSupportArea(works);
		scn.MoveCardsToHand(gimli);

		scn.StartGame();
		scn.SetFreepsDrawDeck(filler1, filler2, filler3);
		scn.MoveCardsToDiscard(axestrike, delving, axe, sleep, shadowCleaving);
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(4, scn.GetFreepsDiscardCount());
		assertInDiscard(axestrike, delving, axe, sleep, shadowCleaving);

		scn.FreepsPlayCard(gimli);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(works));
		scn.FreepsAcceptOptionalTrigger();

		// The top 2 cards of the draw deck are discarded.
		assertEquals(1, scn.GetFreepsDeckCount());
		assertInDiscard(filler1, filler2);
		assertInZone(Zone.DECK, filler3);

		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(axestrike, delving));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axe, sleep, shadowCleaving));
		scn.FreepsChooseCardBPFromSelection(axestrike);

		assertInHand(axestrike);
		assertInDiscard(delving, axe, sleep);
		assertEquals(5, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OfferedEachTimeADwarfIsPlayedAllyOrCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var works = scn.GetFreepsCard("works");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var axestrike = scn.GetFreepsCard("axestrike");
		var delving = scn.GetFreepsCard("delving");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		var filler4 = scn.GetFreepsCard("filler4");
		scn.MoveCardsToSupportArea(works);
		scn.MoveCardsToHand(gimli, grimir);

		scn.StartGame();
		scn.SetFreepsDrawDeck(filler1, filler2, filler3, filler4);
		scn.MoveCardsToDiscard(axestrike, delving);
		assertEquals(4, scn.GetFreepsDeckCount());
		assertInDiscard(axestrike, delving);

		scn.FreepsPlayCard(grimir);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(works));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCardBPFromSelection(delving);
		assertEquals(2, scn.GetFreepsDeckCount());
		assertInHand(delving);

		scn.FreepsPlayCard(gimli);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(works));
		scn.FreepsAcceptOptionalTrigger();
		// Axe Strike is the only [dwarven] event left there: taken with no choice.
		assertEquals(0, scn.GetFreepsDeckCount());
		assertInHand(axestrike);
		assertInDiscard(filler1, filler2, filler3, filler4);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedForItsOwnPlayOrACompanionWhoIsNoDwarfAndDecliningForADwarfChangesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var works = scn.GetFreepsCard("works");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var axestrike = scn.GetFreepsCard("axestrike");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		scn.MoveCardsToHand(works, gimli, aragorn);

		scn.StartGame();
		scn.SetFreepsDrawDeck(filler1, filler2, filler3);
		scn.MoveCardsToDiscard(axestrike);
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertInDiscard(axestrike);

		// Its own play: a [dwarven] card, and no Dwarf.
		scn.FreepsPlayCard(works);
		assertInZone(Zone.SUPPORT, works);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(works));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(aragorn);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(works));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(gimli);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(works));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertInDiscard(axestrike);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWithOneCardInTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var works = scn.GetFreepsCard("works");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var axestrike = scn.GetFreepsCard("axestrike");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		scn.MoveCardsToSupportArea(works);
		scn.MoveCardsToHand(gimli, grimir);

		scn.StartGame();
		scn.SetFreepsDrawDeck(filler1);
		scn.MoveCardsToDiscard(axestrike);
		assertEquals(1, scn.GetFreepsDeckCount());
		assertInDiscard(axestrike);

		scn.FreepsPlayCard(grimir);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(works));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertInDiscard(axestrike);

		// With a second card the cost can be paid.
		scn.MoveCardsToTopOfDeck(filler2);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetFreepsDeckCount());
		scn.FreepsPlayCard(gimli);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(works));
	}

	@Test
	public void WithNoDwarvenEventInTheDiscardPileTheCardsAreDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var works = scn.GetFreepsCard("works");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var sleep = scn.GetFreepsCard("sleep");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		scn.MoveCardsToSupportArea(works);
		scn.MoveCardsToHand(gimli);

		scn.StartGame();
		scn.SetFreepsDrawDeck(filler1, filler2, filler3);
		scn.MoveCardsToDiscard(axe, sleep);
		assertEquals(3, scn.GetFreepsDeckCount());
		assertInDiscard(axe, sleep);

		scn.FreepsPlayCard(gimli);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(works));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetFreepsDeckCount());
		assertInDiscard(filler1, filler2, axe, sleep);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// Q-0331: may the event be one the cost has just discarded from the top of the draw deck?  The likelier
	// reading, after They Are Coming (CR 4.2 "effect") and Ketura's Q-0192 and Q-0320, is no.
	@Test
	@Ignore("Q-0331: a [dwarven] event the cost just discarded from the draw deck is offered to be taken")
	public void ADwarvenEventTheCostDiscardsIsNotTheOneTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var works = scn.GetFreepsCard("works");
		var gimli = scn.GetFreepsCard("gimli");
		var axestrike = scn.GetFreepsCard("axestrike");
		var delving = scn.GetFreepsCard("delving");
		var cleaving = scn.GetFreepsCard("cleaving");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		scn.MoveCardsToSupportArea(works);
		scn.MoveCardsToHand(gimli);

		scn.StartGame();
		scn.SetFreepsDrawDeck(cleaving, filler1, filler2);
		scn.MoveCardsToDiscard(axestrike, delving);
		assertEquals(cleaving, scn.GetFreepsTopOfDeck());
		assertInDiscard(axestrike, delving);

		scn.FreepsPlayCard(gimli);
		scn.FreepsAcceptOptionalTrigger();

		assertInDiscard(cleaving, filler1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(axestrike, delving));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(cleaving));
		scn.FreepsChooseCardBPFromSelection(axestrike);
		assertInHand(axestrike);
		assertInDiscard(cleaving, delving);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
