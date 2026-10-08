package com.gempukku.lotro.cards.unofficial.pc.errata.set07;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_082_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cirion", "57_82");
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: another [gondor] Man, strength 8; a maneuver ability used only on purpose

					// War-beacon: [gondor] beacon possession, cost 1, three may be in play; to play, spot an unbound Man.
					// Its own abilities are used only on purpose.
					put("beacon1", "103_35");
					put("beacon2", "103_35");
					put("beacon3", "103_35");
					put("beacon4", "103_35");
					put("beacon5", "103_35");
					// Northern Signal-fire, Flame of Calenhad: [rohan] beacon possession, cost 2; to play, hinder 2 beacons.
					put("signalfire", "103_86");
					// Northern Signal-fire, Flame of Amon Din and Flame of Eilenach: [gondor] beacon possessions, cost 2; to play,
					// hinder 2 beacons. A response to a wound and a skirmish ability, both used only on purpose. They stand
					// in play beside the War-beacons, so that the 4 beacons Gondor Calls For Aid! hinders can be found; two
					// of that title may be in play, so the Signal-fire in the deck is then not playable.
					put("fire1", "103_28");
					put("fire2", "103_29");
					// Gondor Calls For Aid!: a [rohan] beacon condition, cost 3, so a beacon that is no possession; to play,
					// hinder 4 beacons. Only in the draw deck here, where its text does nothing.
					put("calls", "103_83");
					put("armor", "1_92");           // Armor: [gondor] possession that is no beacon, playable on a Man

					// Man the Walls: [gondor] support condition, cost 1, nothing to play it; cards to play from hand
					put("walls1", "7_111");
					put("walls2", "7_111");
					put("walls3", "7_111");

					put("runner", "1_178");         // Goblin Runner: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CirionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Cirion
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: While you have only 4 or 5 cards in hand, Cirion is strength +2.
		 * Fellowship: Exert Cirion to play a beacon from your draw deck (limit once per turn).
		 */

		var scn = GetScenario();

		var cirion = scn.GetFreepsCard("cirion");

		assertEquals("Cirion", cirion.getBlueprint().getTitle());
		assertNull(cirion.getBlueprint().getSubtitle());
		assertTrue(cirion.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, cirion.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, cirion.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, cirion.getBlueprint().getCardType());
		assertEquals(Race.MAN, cirion.getBlueprint().getRace());
		assertEquals(2, cirion.getBlueprint().getTwilightCost());
		assertEquals(5, cirion.getBlueprint().getStrength());
		assertEquals(3, cirion.getBlueprint().getVitality());
		assertEquals(6, cirion.getBlueprint().getResistance());
		assertNull(cirion.getBlueprint().getSignet());
	}

	@Test
	public void CirionIsPlayedFromHandForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirion = scn.GetFreepsCard("cirion");
		scn.MoveCardsToHand(cirion);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(cirion));

		scn.FreepsPlayCard(cirion);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, cirion);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void StrengthIsPlusTwoOnlyWhileYourHandHoldsFourOrFiveCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirion = scn.GetFreepsCard("cirion");
		var aragorn = scn.GetFreepsCard("aragorn");
		var walls1 = scn.GetFreepsCard("walls1");
		var walls2 = scn.GetFreepsCard("walls2");
		var walls3 = scn.GetFreepsCard("walls3");
		scn.MoveCompanionsToTable(cirion, aragorn);

		scn.StartGame();

		scn.MoveCardsToHand(walls1, walls2, walls3, scn.GetFreepsCard("beacon1"), scn.GetFreepsCard("beacon2"),
				scn.GetFreepsCard("armor"));
		// The other player's hand holds 4: it is not "your" hand.
		scn.MoveCardsToHand(scn.GetShadowCard("walls1"), scn.GetShadowCard("walls2"), scn.GetShadowCard("walls3"),
				scn.GetShadowCard("armor"));
		scn.FreepsRefreshDecision();

		assertEquals(6, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(5, scn.GetStrength(cirion));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsPlayCard(walls1);
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(7, scn.GetStrength(cirion));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsPlayCard(walls2);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(7, scn.GetStrength(cirion));

		scn.FreepsPlayCard(walls3);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(5, scn.GetStrength(cirion));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipExertsCirionToPlayABeaconFromYourDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirion = scn.GetFreepsCard("cirion");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		var beacon3 = scn.GetFreepsCard("beacon3");
		var beacon4 = scn.GetFreepsCard("beacon4");
		var beacon5 = scn.GetFreepsCard("beacon5");
		var signalfire = scn.GetFreepsCard("signalfire");
		var calls = scn.GetFreepsCard("calls");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Aragorn beside him: the exertion is Cirion's and no other companion's.
		scn.MoveCompanionsToTable(cirion, aragorn);
		// Four beacons in play, so that the condition's "hinder 4 beacons" can be paid. Only two of them are
		// War-beacons: three may be in play, and the one in the deck is a third.
		scn.MoveCardsToSupportArea(beacon1, beacon2, scn.GetFreepsCard("fire1"), scn.GetFreepsCard("fire2"));

		scn.StartGame();

		// Left in the deck: beacon3, the Signal-fire, Gondor Calls For Aid!, Armor and cards that are no beacons.
		scn.MoveCardsToDiscard(beacon4);
		scn.MoveCardsToHand(beacon5);

		assertEquals(0, scn.GetWoundsOn(cirion));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetTwilight());
		int deckCount = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsActionAvailable(cirion));

		scn.FreepsUseCardAction(cirion);
		assertEquals(1, scn.GetWoundsOn(cirion));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.DismissRevealedCards();

		// The War-beacon and the condition in the deck, and nothing else: not Armor, not the War-beacons in the
		// discard pile and in hand, and not the Signal-fire in the deck, a third of its title when two may be in play.
		assertTrue(scn.FreepsHasBPChoiceAvailable("103_35", true));
		assertTrue(scn.FreepsHasBPChoiceAvailable("103_83", true));
		assertFalse(scn.FreepsHasBPChoiceAvailable("1_92", true));
		assertFalse(scn.FreepsHasBPChoiceAvailable("103_86", true));
		assertEquals(2, scn.FreepsGetSelectableCount());

		scn.FreepsChooseCardBPFromSelection(beacon3);
		assertInZone(Zone.SUPPORT, beacon3);
		assertEquals(1, scn.GetTwilight());     // War-beacon's cost
		assertEquals(deckCount - 1, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, signalfire, calls);
		assertInDiscard(beacon4);
		assertInHand(beacon5);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhileCirionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirion = scn.GetFreepsCard("cirion");
		scn.MoveCompanionsToTable(cirion);
		scn.AddWoundsToChar(cirion, 2);

		scn.StartGame();

		// War-beacons in the deck, and an unbound Man to spot for them.
		assertFalse(scn.FreepsActionAvailable(cirion));

		scn.RemoveWoundsFromChar(cirion, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(cirion));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirion = scn.GetFreepsCard("cirion");
		scn.MoveCompanionsToTable(cirion);

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(cirion));

		scn.MoveMinionsToTable(scn.GetShadowCard("runner"));
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(cirion));
		assertEquals(0, scn.GetWoundsOn(cirion));
	}

	@Test
	public void FellowshipAbilityIsNotOfferedAgainInTheTurnItWasUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirion = scn.GetFreepsCard("cirion");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		scn.MoveCompanionsToTable(cirion);

		scn.StartGame();

		scn.FreepsUseCardAction(cirion);
		scn.DismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(beacon1);
		assertEquals(1, scn.GetWoundsOn(cirion));
		assertEquals(1, scn.GetTwilight());

		// Limit once per turn: a limit on uses, so the ability is not offered again, though Cirion could
		// still exert and the draw deck still holds a beacon (Ketura on Q-0093).
		assertFalse(scn.FreepsActionAvailable(cirion));
		assertEquals(1, scn.GetWoundsOn(cirion));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The other player's turn, and then his own next turn: offered again.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		scn.MoveCardsToTopOfDeck(beacon2);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetWoundsOn(cirion));
		assertTrue(scn.FreepsActionAvailable(cirion));
	}

	@Test
	public void ThePlayerMayChooseNotToPlayABeaconAndTheDeckIsShuffledAllTheSame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirion = scn.GetFreepsCard("cirion");
		scn.MoveCompanionsToTable(cirion);

		scn.StartGame();

		int deckCount = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsUseCardAction(cirion);
		scn.DismissRevealedCards();
		assertTrue(scn.FreepsHasBPChoiceAvailable("103_35", true));
		assertEquals(0, scn.FreepsGetChoiceMin());

		scn.FreepsDeclineChoosing();
		assertEquals(1, scn.GetWoundsOn(cirion));
		assertEquals(0, scn.GetTwilight());
		assertEquals(deckCount, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoBeaconThatCanBePlayedInTheDeckCirionStillExertsAndTheDeckIsShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirion = scn.GetFreepsCard("cirion");
		var signalfire = scn.GetFreepsCard("signalfire");
		scn.MoveCompanionsToTable(cirion);

		scn.StartGame();

		// Every War-beacon is in the discard pile. The Signal-fire is in the deck, but with no beacon in play to
		// hinder it cannot be played.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("beacon1"), scn.GetFreepsCard("beacon2"), scn.GetFreepsCard("beacon3"),
				scn.GetFreepsCard("beacon4"), scn.GetFreepsCard("beacon5"));
		scn.FreepsRefreshDecision();

		int deckCount = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.FreepsActionAvailable(cirion));

		scn.FreepsUseCardAction(cirion);
		scn.DismissRevealedCards();
		// The deck is shown, and with nothing in it that can be chosen, no choice is asked.
		assertFalse(scn.FreepsDecisionAvailable("Choose"));

		assertEquals(1, scn.GetWoundsOn(cirion));
		assertEquals(0, scn.GetTwilight());
		assertEquals(deckCount, scn.GetFreepsDeckCount());
		assertNotInZone(Zone.SUPPORT, signalfire);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
