package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_051_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("coming", "3_51");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1 only
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, vitality 3; a regroup ability used only on purpose
					put("grima", "4_153");      // Grima, Son of Galmod: [isengard] Man, vitality 3; strength -1 to companions bearing Shadow conditions
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, vitality 3, no text
					put("runner", "1_178");     // Goblin Runner: a card of the Shadow player's own hand

					put("gimli", "1_13");       // Gimli, Son of Gloin: a card of cost 2 for the Free Peoples hand
					put("bow", "1_41");         // Elven Bow: a card of cost 1 for the Free Peoples hand
					put("wreathed", "1_238");   // Wreathed in Shadow: a card of cost 0 for the Free Peoples hand
					put("underhill", "1_129");  // The Misadventure of Mr. Underhill: each companion played away from a sanctuary costs +2
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
					put("coming", "3_51");
					put("uruk", "1_151");       // Uruk Savage: an [isengard] minion, vitality 3
					put("blow", "1_5");         // Cleaving Blow: a card for the Free Peoples player's hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ComingfortheRingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Coming for the Ring
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Reveal a card at random from the Free Peoples player's hand. Heal X [isengard] minions, where X is the twilight cost of the card revealed.
		*/

		var scn = GetScenario();

		var coming = scn.GetFreepsCard("coming");

		assertEquals("Coming for the Ring", coming.getBlueprint().getTitle());
		assertNull(coming.getBlueprint().getSubtitle());
		assertFalse(coming.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, coming.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, coming.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, coming.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(coming, Timeword.REGROUP));
		assertEquals(2, coming.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsNotPlayableInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coming = scn.GetShadowCard("coming");
		var savage = scn.GetShadowCard("savage");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(coming);
		scn.MoveCardsToHand(gimli);
		assertInHand(coming, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(savage, 1);
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.SetTwilight(5);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// A card in the Free Peoples hand, a wounded [isengard] minion and the twilight to pay: only the phase is wrong.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInHand(coming, gimli);
		assertTrue(scn.GetTwilight() >= 2);
		assertFalse(scn.ShadowPlayAvailable(coming));
	}

	@Test
	public void RevealsACardAtRandomToItsPlayerAndHealsAsManyIsengardMinionsAsItsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coming = scn.GetShadowCard("coming");
		var savage = scn.GetShadowCard("savage");
		var servant = scn.GetShadowCard("servant");
		var grima = scn.GetShadowCard("grima");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(coming, runner);
		scn.MoveCardsToHand(gimli);
		assertInHand(coming, runner, gimli);

		scn.StartGame();
		scn.FreepsPass();
		// No minion: the Shadow phase is followed by the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(savage, servant, grima, troop);
		// Two wounds on each [isengard] minion, so that one healed reads 1; the others are wounded too.
		scn.AddWoundsToChar(savage, 2);
		scn.AddWoundsToChar(servant, 2);
		scn.AddWoundsToChar(grima, 2);
		scn.AddWoundsToChar(troop, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.SetTwilight(2);

		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(servant));
		assertEquals(2, scn.GetWoundsOn(grima));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInHand(coming, runner, gimli);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(coming));

		scn.ShadowPlayCard(coming);
		// The Free Peoples player's one card is shown to the Shadow player, the Shadow player's own hand is not,
		// and the hand's owner is shown nothing.
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(runner, false));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		scn.ShadowDismissRevealedCards();

		// Gimli costs 2: two [isengard] minions, and only [isengard] minions.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, servant, grima));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, frodo));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(savage, grima);

		// One wound off each minion chosen.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(grima));
		assertEquals(2, scn.GetWoundsOn(servant));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(frodo));
		// The card revealed stays in the hand it was shown from.
		assertInHand(gimli, runner);
		assertInDiscard(coming);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACardOfCostOneHealsOneIsengardMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coming = scn.GetShadowCard("coming");
		var savage = scn.GetShadowCard("savage");
		var servant = scn.GetShadowCard("servant");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCardsToHand(coming);
		scn.MoveCardsToHand(bow);
		assertInHand(coming, bow);

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(savage, servant);
		scn.AddWoundsToChar(savage, 2);
		scn.AddWoundsToChar(servant, 2);
		scn.SetTwilight(2);

		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(servant));
		assertInHand(bow);
		scn.FreepsPass();

		scn.ShadowPlayCard(coming);
		assertTrue(scn.ShadowHasCardChoiceAvailable(bow, false));
		scn.ShadowDismissRevealedCards();

		// The Bow costs 1.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, servant));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(servant);

		assertEquals(1, scn.GetWoundsOn(servant));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACardOfCostZeroHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coming = scn.GetShadowCard("coming");
		var savage = scn.GetShadowCard("savage");
		var servant = scn.GetShadowCard("servant");
		var wreathed = scn.GetFreepsCard("wreathed");
		scn.MoveCardsToHand(coming);
		scn.MoveCardsToHand(wreathed);
		assertInHand(coming, wreathed);

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(savage, servant);
		scn.AddWoundsToChar(savage, 1);
		scn.AddWoundsToChar(servant, 1);
		scn.SetTwilight(2);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(servant));
		assertInHand(wreathed);
		scn.FreepsPass();

		scn.ShadowPlayCard(coming);
		assertTrue(scn.ShadowHasCardChoiceAvailable(wreathed, false));
		scn.ShadowDismissRevealedCards();

		// X is 0: nobody is chosen and nobody is healed.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(servant));
		assertInDiscard(coming);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoIsengardMinionWoundedItIsPlayedAndHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coming = scn.GetShadowCard("coming");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(coming);
		scn.MoveCardsToHand(gimli);
		assertInHand(coming, gimli);

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(savage, troop);
		// The [isengard] minion is unwounded; the [moria] minion and the Ring-bearer are wounded.
		scn.AddWoundsToChar(troop, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.SetTwilight(2);

		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(coming));

		scn.ShadowPlayCard(coming);
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, false));
		scn.ShadowDismissRevealedCards();

		// X is 2, and nothing it may heal: the event is played for nothing.
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInDiscard(coming);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheNumberHealedIsTheCostOfTheCardShownAndNotOfAnotherCardOfTheHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coming = scn.GetShadowCard("coming");
		var savage = scn.GetShadowCard("savage");
		var servant = scn.GetShadowCard("servant");
		var grima = scn.GetShadowCard("grima");
		var bow = scn.GetFreepsCard("bow");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(coming);
		// Two cards of different costs, 1 and 2, and three wounded [isengard] minions, so that either is asked as a choice.
		scn.MoveCardsToHand(bow, gimli);

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(savage, servant, grima);
		scn.AddWoundsToChar(savage, 1);
		scn.AddWoundsToChar(servant, 1);
		scn.AddWoundsToChar(grima, 1);
		scn.SetTwilight(2);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(servant));
		assertEquals(1, scn.GetWoundsOn(grima));
		assertEquals(2, scn.GetFreepsHandCount());
		scn.FreepsPass();

		scn.ShadowPlayCard(coming);
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		// Which card is shown is chance; what is read is which it was.
		var bowShown = scn.ShadowHasCardChoiceAvailable(bow, false);
		var gimliShown = scn.ShadowHasCardChoiceAvailable(gimli, false);
		assertNotEquals(bowShown, gimliShown);
		scn.ShadowDismissRevealedCards();

		var expected = bowShown ? 1 : 2;
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, servant, grima));
		assertEquals(expected, scn.ShadowGetChoiceMin());
		assertEquals(expected, scn.ShadowGetChoiceMax());
		// The game rests on the choice of whom to heal.
		assertTrue(scn.ShadowDecisionAvailable("Choose cards to heal"));
	}

	@Test
	public void XIsTheCostPrintedOnTheCardThoughMrUnderhillMakesItCostMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coming = scn.GetShadowCard("coming");
		var savage = scn.GetShadowCard("savage");
		var servant = scn.GetShadowCard("servant");
		var grima = scn.GetShadowCard("grima");
		var underhill = scn.GetShadowCard("underhill");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(coming);
		scn.MoveCardsToHand(gimli);
		scn.MoveCardsToSupportArea(underhill);
		assertInZone(Zone.SUPPORT, underhill);
		assertInHand(coming, gimli);

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(savage, servant, grima);
		// Three wounded [isengard] minions, so that 2 and 4 are told apart.
		scn.AddWoundsToChar(savage, 1);
		scn.AddWoundsToChar(servant, 1);
		scn.AddWoundsToChar(grima, 1);
		scn.SetTwilight(2);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(servant));
		assertEquals(1, scn.GetWoundsOn(grima));
		assertInZone(Zone.SUPPORT, underhill);
		scn.FreepsPass();
		// Site 2 is no sanctuary: Gimli, printed 2, would cost 4 to play there.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetTwilightCost(gimli));

		scn.ShadowPlayCard(coming);
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, false));
		scn.ShadowDismissRevealedCards();

		// X is 2: a card in play changes another card's cost only as that card is played.
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(savage, servant);

		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertEquals(1, scn.GetWoundsOn(grima));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotPlayableWithTheFreePeoplesHandEmpty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRevealScenario();

		var coming = scn.GetShadowCard("coming");
		var uruk = scn.GetShadowCard("uruk");
		var blow = scn.GetFreepsCard("blow");
		scn.MoveCardsToHand(coming);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk);
		scn.SkipToPhase(Phase.REGROUP);
		scn.AddWoundsToChar(uruk, 1);
		assertEquals(1, scn.GetWoundsOn(uruk));
		assertInZone(Zone.HAND, coming);
		scn.FreepsPass();

		// A wounded [isengard] minion, and no card in the Free Peoples hand to reveal: the reveal must be able to
		// happen for the heal to read it, so the event is not playable (Q-0336: B).
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(uruk));
		assertEquals(0, scn.GetFreepsHandCount());
		assertFalse(scn.ShadowPlayAvailable(coming));

		// One card there, and it is playable.
		scn.MoveCardsToHand(blow);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowPlayAvailable(coming));
		assertInZone(Zone.HAND, coming);
	}
}
