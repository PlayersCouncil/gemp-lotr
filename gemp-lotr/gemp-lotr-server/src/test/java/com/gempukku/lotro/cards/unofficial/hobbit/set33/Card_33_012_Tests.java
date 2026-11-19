package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_012_Tests
{

	// The Hobbit table: the Hobbit path (site 2 is The Troll Hoard 30_50, Shadow number 3), Bilbo, Reliable
	// Companion 30_46 as the Ring-bearer and no card in the Ring slot.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thror", "33_12");
					// [dwarven] Dwarf companions to pay the cost.  Balin: a Fellowship ability, not used.  Dwalin: a
					// Maneuver ability, not used.
					put("balin", "30_2");
					put("dwalin", "30_9");
					// Gandalf, Leader of Dwarves: a [gandalf] companion, no Dwarf.  His texts are a reconcile trigger
					// and two Skirmish abilities, none reached.
					put("gandalf", "30_25");
					// Dain Ironfoot, Lord of the Iron Hills: a [dwarven] Dwarf ALLY.  A start-of-regroup offer that
					// needs a [dwarven] follower in play, and Thorin +1.
					put("dain", "32_2");
					// [dwarven] cards for the draw deck: a companion, a possession, an event and an artifact.
					put("fili", "30_10");        // Fili, Brother of Kili: a Response, not used
					put("axe", "31_1");          // Dwarven Axe
					put("fury", "30_3");         // Battle Fury: a Skirmish event
					put("arkenstone", "31_7");   // The Arkenstone
					// Cards of other cultures for the draw deck.
					put("sting", "30_47");       // Sting: [shire] artifact
					put("takeup", "30_31");      // Take up arms. Fight!: [gandalf] Skirmish event
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void ThrorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Thrór, Overthrown King
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 7
		 * Vitality: 5
		 * Resistance: none printed
		 * Game Text: Each time the fellowship moves, place Thrór in your dead pile.
		 * 	<b>Fellowship:</b> Place 2 [dwarven] companions in your dead pile to take 3 [dwarven] cards into hand from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("thror");

		assertEquals("Thrór", card.getBlueprint().getTitle());
		assertEquals("Overthrown King", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(5, card.getBlueprint().getVitality());
		// The picture prints no resistance.
		assertEquals(0, card.getBlueprint().getResistance());
	}

	@Test
	public void WhenTheFellowshipMovesThrorIsPlacedInTheDeadPileBeforeTheCompanionsAddTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thror = scn.GetFreepsCard("thror");
		scn.MoveCompanionsToTable(thror);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, thror);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();

		assertInZone(Zone.DEAD, thror);
		assertEquals(1, scn.GetFreepsDeadCount());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 3 for The Troll Hoard + 1 for Bilbo: Thrór has left before the companions are counted (CR 4.2 "moving
		// your fellowship"); 5 had he been counted.
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ThrorStaysInPlayWhenTheFellowshipDoesNotMoveAgainInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thror = scn.GetFreepsCard("thror");

		scn.StartGame();
		// Thrór comes into play after the move, as Gandalf, Leader of Dwarves 30_25's Skirmish ability plays a
		// Dwarf from the draw deck; he is laid at the start of the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCompanionsToTable(thror);
		assertInZone(Zone.FREE_CHARACTERS, thror);
		assertEquals(0, scn.GetFreepsDeadCount());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();

		assertInZone(Zone.FREE_CHARACTERS, thror);
		assertEquals(0, scn.GetFreepsDeadCount());
		// The turn is over without a second move: player 2's fellowship phase begins, at his own site 1.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(1, scn.GetCurrentSiteNumber());
	}

	@Test
	public void AMoveInTheRegroupPhasePlacesThrorInTheDeadPileToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thror = scn.GetFreepsCard("thror");

		scn.StartGame();
		// Laid as in the test above.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCompanionsToTable(thror);
		assertInZone(Zone.FREE_CHARACTERS, thror);
		assertEquals(0, scn.GetFreepsDeadCount());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertInZone(Zone.DEAD, thror);
		assertEquals(1, scn.GetFreepsDeadCount());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FellowshipPlacesTwoDwarvenCompanionsInTheDeadPileToTakeThreeDwarvenCardsFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thror = scn.GetFreepsCard("thror");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var dain = scn.GetFreepsCard("dain");
		var fili = scn.GetFreepsCard("fili");
		var axe = scn.GetFreepsCard("axe");
		var fury = scn.GetFreepsCard("fury");
		var arkenstone = scn.GetFreepsCard("arkenstone");
		var sting = scn.GetFreepsCard("sting");
		var takeup = scn.GetFreepsCard("takeup");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thror, balin, dwalin, gandalf);
		scn.MoveAlliesToTable(dain);

		scn.StartGame();
		scn.SetFreepsDrawDeck(fili, axe, fury, arkenstone, sting, takeup);
		assertInZone(Zone.FREE_CHARACTERS, thror, balin, dwalin, gandalf);
		assertInZone(Zone.SUPPORT, dain);
		assertInZone(Zone.DECK, fili, axe, fury, arkenstone, sting, takeup);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertEquals(0, scn.GetFreepsHandCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		assertTrue(scn.FreepsActionAvailable(thror));
		scn.FreepsUseCardAction(thror);

		// [dwarven] companions, Thrór himself among them; not the [dwarven] ally, nor companions of other cultures.
		assertTrue(scn.FreepsHasCardChoiceAvailable(thror, balin, dwalin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dain, gandalf, bilbo));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(balin, dwalin);

		assertInZone(Zone.DEAD, balin, dwalin);
		assertEquals(2, scn.GetFreepsDeadCount());
		assertInZone(Zone.FREE_CHARACTERS, thror, gandalf);
		assertInZone(Zone.SUPPORT, dain);

		scn.MarkGameLog();
		scn.FreepsDismissRevealedCards();
		// [dwarven] cards of four types; not cards of other cultures.  The look never forces a take (F-0049).
		assertTrue(scn.FreepsHasCardChoiceAvailable(fili, axe, fury, arkenstone));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sting, takeup));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(fili, axe, fury);

		assertInHand(fili, axe, fury);
		assertEquals(3, scn.GetFreepsHandCount());
		assertInZone(Zone.DECK, arkenstone, sting, takeup);
		// Taken by culture: the game log names the cards taken, and not one left behind.
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(fili, axe, fury));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(arkenstone));
		// The deck looked through is reshuffled; the other player's is not.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoDwarvenCardInTheDrawDeckTheFellowshipActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thror = scn.GetFreepsCard("thror");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var sting = scn.GetFreepsCard("sting");
		var takeup = scn.GetFreepsCard("takeup");
		scn.MoveCompanionsToTable(thror, balin, dwalin);

		scn.StartGame();
		scn.SetFreepsDrawDeck(sting, takeup);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.FREE_CHARACTERS, thror, balin, dwalin);
		assertInZone(Zone.DECK, sting, takeup);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertEquals(0, scn.GetFreepsHandCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();

		// A [shire] artifact and a [gandalf] event: nothing to take.  A Hobbit-set take from the draw deck with nothing to take is withheld (Q-0019; the orchestrator on Q-0406 and Q-0418):
		// no two companions are placed in the dead pile for a look that finds nothing.
		assertFalse(scn.FreepsActionAvailable(thror));
		assertInZone(Zone.FREE_CHARACTERS, thror, balin, dwalin);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertInZone(Zone.DECK, sting, takeup);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedWithOnlyOneDwarvenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thror = scn.GetFreepsCard("thror");
		var balin = scn.GetFreepsCard("balin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var dain = scn.GetFreepsCard("dain");
		var fili = scn.GetFreepsCard("fili");
		scn.MoveCompanionsToTable(thror, gandalf);
		scn.MoveAlliesToTable(dain);

		scn.StartGame();
		scn.SetFreepsDrawDeck(fili);
		assertInZone(Zone.FREE_CHARACTERS, thror, gandalf);
		assertInZone(Zone.SUPPORT, dain);
		assertInZone(Zone.DECK, fili);

		// Thrór is the only [dwarven] companion: the ally Dain and Gandalf do not make up the second.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(thror));

		// A second [dwarven] companion, and it is offered.
		scn.MoveCompanionsToTable(balin);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.FREE_CHARACTERS, balin);
		assertTrue(scn.FreepsActionAvailable(thror));
	}
}
