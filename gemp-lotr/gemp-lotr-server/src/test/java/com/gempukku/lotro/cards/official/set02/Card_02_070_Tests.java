package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_070_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("power", "2_70");
					// The Balrog, The Terror of Khazad-dum: cost 12, site 4.  Nothing is needed to play it and
					// nothing discards it away from an underground site; its text only acts at one.
					put("balrog", "12_79");

					put("runner", "1_178");     // Goblin Runner: a [moria] Orc (its text acts only when it is played)
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text
					put("marksman", "1_176");   // Goblin Marksman: a [moria] Orc; Archer is its whole text
					put("troop2", "1_177");     // a second Troop, for a [moria] Orc that is in play and not in hand
					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc
					// Cave Troll of Moria: a [moria] minion that is neither an Orc nor The Balrog.  "To play, spot
					// a [moria] Orc."
					put("troll", "1_165");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PowerandTerrorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Power and Terror
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Shadow:</b> Reveal any number of [moria] Orcs from your hand to play The Balrog. Its twilight cost is -2 for each Orc revealed.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("power");

		assertEquals("Power and Terror", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RevealingTwoOfThreeMoriaOrcsInHandPlaysTheBalrogForFourLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var balrog = scn.GetShadowCard("balrog");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var marksman = scn.GetShadowCard("marksman");
		var troop2 = scn.GetShadowCard("troop2");
		var chieftain = scn.GetShadowCard("chieftain");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCardsToHand(power, balrog, runner, troop, marksman, chieftain, troll);

		scn.StartGame();
		scn.MoveMinionsToTable(troop2);
		scn.FreepsPass();
		// 2 short of what The Balrog costs here: 12 + 2 roaming at site 2.
		scn.SetTwilight(12);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(balrog));
		assertTrue(scn.ShadowPlayAvailable(power));

		scn.ShadowPlayCard(power);
		// The [moria] Orcs in hand, and nothing else: not the [sauron] Orc, not the [moria] Troll, not The
		// Balrog, and not the [moria] Orc that is in play.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, troop, marksman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(chieftain, troll, balrog, troop2));
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(runner, troop);

		// The two are shown to the other player; the player whose hand it is is shown nothing.
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(marksman, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// The Balrog is the card played, with no choice put: the Troll, which could be played here, is not one.
		assertInZone(Zone.SHADOW_CHARACTERS, balrog);
		// 12 - (12 + 2 roaming - 2 for each of the 2 Orcs revealed); the event itself costs nothing
		assertEquals(2, scn.GetTwilight());
		assertInHand(runner, troop, marksman, chieftain, troll);
		assertInDiscard(power);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoOrcRevealedTheBalrogIsPlayedAtItsFullCostAndNobodyIsShownAnything() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var balrog = scn.GetShadowCard("balrog");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(power, balrog, runner, troop);

		scn.StartGame();
		scn.FreepsPass();
		scn.SetTwilight(14);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(power);
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowDeclineChoosing();

		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertInZone(Zone.SHADOW_CHARACTERS, balrog);
		// 14 - (12 + 2 roaming)
		assertEquals(0, scn.GetTwilight());
		assertInHand(runner, troop);
		assertInDiscard(power);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OrcsOfAnotherCultureAndMoriaCardsThatAreNotOrcsBuyNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var balrog = scn.GetShadowCard("balrog");
		var troop2 = scn.GetShadowCard("troop2");
		var chieftain = scn.GetShadowCard("chieftain");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCardsToHand(power, balrog, chieftain, troll);

		scn.StartGame();
		scn.MoveMinionsToTable(troop2);
		scn.FreepsPass();

		// One short of 12 + 2 roaming.  A [sauron] Orc and a [moria] Troll in hand, and a [moria] Orc in play,
		// take nothing off The Balrog: it cannot be paid for, so the event may not be played.
		scn.SetTwilight(13);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(power, balrog);
		assertFalse(scn.ShadowPlayAvailable(power));

		scn.SetTwilight(14);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(power));
	}

	@Test
	public void MayBePlayedOnlyWhenRevealingEveryOrcInHandBringsTheBalrogWithinThePool() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var balrog = scn.GetShadowCard("balrog");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(power, balrog, runner, troop, marksman);

		scn.StartGame();
		scn.FreepsPass();

		assertEquals(12, balrog.getBlueprint().getTwilightCost());

		// With 3 [moria] Orcs in hand, the maximum discount that Power and Terror can give is -6.
		// 12 - 6 + 2 (roaming) = 8.  One short of that, P&T may not be played.
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(power, balrog);
		assertFalse(scn.ShadowPlayAvailable(power));

		// With exactly 8 on the table, P&T should permit the play, though The Balrog cannot be played without it.
		scn.SetTwilight(8);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(power));
		assertFalse(scn.ShadowPlayAvailable(balrog));
	}

	@Test
	public void WhenEveryOrcInHandIsNeededToPayForTheBalrogAllAreRevealedAndNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var balrog = scn.GetShadowCard("balrog");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(power, balrog, runner, troop, marksman);

		scn.StartGame();
		scn.FreepsPass();
		// 12 - 6 + 2 (roaming) = 8: exactly what all three Orcs bring The Balrog down to.
		scn.SetTwilight(8);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(balrog);
		scn.ShadowPlayCard(power);

		// Only one number of Orcs pays for The Balrog, all of them, so there is nothing to choose: the three
		// are shown at once.
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(marksman, false));
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.SHADOW_CHARACTERS, balrog);
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(power);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFewestOrcsThatMayBeRevealedAreThoseThatPayForTheBalrog() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var balrog = scn.GetShadowCard("balrog");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(power, balrog, runner, troop, marksman);

		scn.StartGame();
		scn.FreepsPass();
		// 3 short of 12 + 2 roaming: one Orc is not enough, two are.
		scn.SetTwilight(11);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(power));
		scn.ShadowPlayCard(power);

		// Playing The Balrog is what the reveal is for, and a play is a cost: a reveal that leaves The Balrog
		// out of reach is not one the player may make.
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(runner, marksman);
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.SHADOW_CHARACTERS, balrog);
		// 11 - (12 + 2 roaming - 4)
		assertEquals(1, scn.GetTwilight());
		assertInDiscard(power);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilTheBalrogIsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var balrog = scn.GetShadowCard("balrog");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCardsToHand(power, runner, troop, troll);
		scn.MoveCardsToDiscard(balrog);

		scn.StartGame();
		scn.MoveMinionsToTable(troop2);
		scn.FreepsPass();
		scn.SetTwilight(30);
		scn.ShadowRefreshDecision();

		// The Balrog is in the discard pile.  The Troll in hand, a [moria] minion that could be played here
		// (a [moria] Orc is in play to spot), is not The Balrog.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(troll));
		assertInHand(power);
		assertFalse(scn.ShadowPlayAvailable(power));

		scn.MoveCardsToHand(balrog);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(power));
	}
}
