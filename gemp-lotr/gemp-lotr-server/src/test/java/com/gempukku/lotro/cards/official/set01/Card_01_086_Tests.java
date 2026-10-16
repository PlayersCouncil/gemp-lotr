package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_086_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("treachery", "1_86");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a standing strength bonus and nothing else
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf

					put("runner", "1_178");     // Goblin Runner: [moria] Orc
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai
					put("axestrike", "1_3");    // Axe Strike: a Free Peoples event
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TreacheryDeeperThanYouKnowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Treachery Deeper Than You Know
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Spell</b>.<br><b>Fellowship:</b> Spot Gandalf to reveal an opponent's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("treachery");

		assertEquals("Treachery Deeper Than You Know", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingGandalfShowsTheWholeShadowHandToThePlayerOfTheEventAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treachery = scn.GetFreepsCard("treachery");
		var gandalf = scn.GetFreepsCard("gandalf");
		// The Free Peoples player's own card, in his own hand, is no part of "an opponent's hand".  A showing is
		// read by blueprint, so it is of a title the Shadow hand does not hold.
		var ownAxeStrike = scn.GetFreepsCard("axestrike");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var axestrike = scn.GetShadowCard("axestrike");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(treachery, ownAxeStrike);
		// Two of the Shadow player's cards in hand; his Axe Strike stays in his draw deck, and neither it nor the
		// Free Peoples player's own Axe Strike is shown.
		scn.MoveCardsToHand(runner, savage);

		scn.StartGame();

		assertEquals(2, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, runner, savage, ownAxeStrike);
		assertInZone(Zone.DECK, axestrike);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(treachery));

		scn.MarkGameLog();
		scn.FreepsPlayCard(treachery);

		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(axestrike, false));
		// The owner of the hand is shown nothing and asked nothing; the game log names what was shown.
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(runner, savage));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(axestrike));

		scn.FreepsDismissRevealedCards();
		// Gandalf is spotted, not exerted, and the hand stays as it was.
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, runner, savage, ownAxeStrike);
		assertInZone(Zone.DISCARD, treachery);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutGandalfThoughAnotherGandalfCompanionIsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treachery = scn.GetFreepsCard("treachery");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(treachery);
		scn.MoveCardsToHand(scn.GetShadowCard("runner"), scn.GetShadowCard("savage"));

		scn.StartGame();

		assertInZone(Zone.FREE_CHARACTERS, avenger);
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(treachery));
	}

	@Test
	public void AgainstAnEmptyHandItMayStillBePlayedAndNothingIsShown() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treachery = scn.GetFreepsCard("treachery");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(treachery);

		scn.StartGame();

		assertEquals(0, scn.GetShadowHandCount());
		// CR 4.2 "effect": an event is played though its effect will do nothing.
		assertTrue(scn.FreepsPlayAvailable(treachery));

		scn.MarkGameLog();
		scn.FreepsPlayCard(treachery);

		// Nobody is shown or asked anything, and the game log says there was nothing to reveal (Q-0144).
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkSays("no cards in " + scn.P2() + "'s hand"));
		assertInZone(Zone.DISCARD, treachery);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
