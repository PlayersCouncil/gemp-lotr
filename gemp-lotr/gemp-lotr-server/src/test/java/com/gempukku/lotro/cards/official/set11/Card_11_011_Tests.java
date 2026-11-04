package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_011_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hall", "11_11");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetExchangeScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hall", "11_11");
					put("gimli", "5_7");        // Gimli, Skilled Defender: the Dwarf to spot; his text answers a skirmish he wins
					put("armor", "1_8");        // Dwarven Armor and Dwarf Guard: the two cards stacked on the Hall
					put("guard", "1_7");
					put("strike", "1_3");       // Axe Strike, Battle Fury and Cleaving Blow: the three cards in hand
					put("fury", "1_4");
					put("blow", "1_5");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
					put("troop2", "1_177");

					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a Dwarf ally; his ability is not used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion; his ability is not used
					// Mines of Khazad-dum: a [dwarven] condition played after the Hall; it answers [dwarven] tales only
					put("mines", "3_3");
					// Courtesy of My Hall: another [dwarven] condition with a card stacked on it; its ability is not used
					put("courtesy", "4_44");
					// Four more cards for the draw deck, below the five above: Axe Strike
					put("d6", "1_3");
					put("d7", "1_3");
					put("d8", "1_3");
					put("d9", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HallofOurFathersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Hall of Our Fathers
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When you play this condition, stack the top 8 cards of your draw deck here.<br>At the start of your maneuver phase, you may spot a Dwarf to exchange the cards stacked here with your hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hall");

		assertEquals("Hall of Our Fathers", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtTheStartOfYourManeuverPhaseMaySpotADwarfToExchangeTheCardsStackedHereWithYourHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetExchangeScenario();

		var hall = scn.GetFreepsCard("hall");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var guard = scn.GetFreepsCard("guard");
		var strike = scn.GetFreepsCard("strike");
		var fury = scn.GetFreepsCard("fury");
		var blow = scn.GetFreepsCard("blow");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(hall);
		// The Hall stacks the top cards of its player's draw deck, whatever they are.
		scn.StackCardsOn(hall, armor, guard);
		scn.MoveCardsToHand(strike, fury, blow);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInZone(Zone.STACKED, armor, guard);
		assertInHand(strike, fury, blow);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetStackedCards(hall).size());
		assertInZone(Zone.STACKED, armor, guard);
		assertEquals(3, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, strike, fury, blow);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hall));
		scn.FreepsAcceptOptionalTrigger();

		// Everything changes places, not one card for one: the two that were stacked are the whole hand, and
		// the three that were the hand are what is stacked.
		assertEquals(2, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, armor, guard);
		assertEquals(3, scn.GetStackedCards(hall).size());
		assertInZone(Zone.STACKED, strike, fury, blow);
		assertTrue(scn.GetStackedCards(hall).contains(strike));
		assertTrue(scn.GetStackedCards(hall).contains(fury));
		assertTrue(scn.GetStackedCards(hall).contains(blow));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	// The same exchange with one side empty.  CR 4.2 "effect": "If the effect of a card or special ability
	// requires you to perform an action and you cannot, you must perform as much as you can and ignore the rest."
	@Test
	public void WithNothingStackedHereTheWholeHandIsStackedAndNothingComesBack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetExchangeScenario();

		var hall = scn.GetFreepsCard("hall");
		var gimli = scn.GetFreepsCard("gimli");
		var strike = scn.GetFreepsCard("strike");
		var fury = scn.GetFreepsCard("fury");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(hall);
		scn.MoveCardsToHand(strike, fury);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInHand(strike, fury);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetStackedCards(hall).size());
		assertEquals(2, scn.GetFreepsHandCount());
		assertInHand(strike, fury);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hall));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetStackedCards(hall).size());
		assertInZone(Zone.STACKED, strike, fury);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	// The other side empty: the cards stacked here come into hand, and nothing goes back.  The game used to
	// make the offer and, taken, move nothing: its exchange wanted at least one card in the hand (F-0371).
	@Test
	public void WithNoCardInHandTheCardsStackedHereAreTakenIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetExchangeScenario();

		var hall = scn.GetFreepsCard("hall");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(hall);
		scn.StackCardsOn(hall, armor, guard);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInZone(Zone.STACKED, armor, guard);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetStackedCards(hall).size());
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.STACKED, armor, guard);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hall));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, armor, guard);
		assertEquals(0, scn.GetStackedCards(hall).size());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	// Ketura on Q-0034: an optional trigger with no cost whose whole effect would do nothing is not offered, and
	// a spot is no cost.  With nothing stacked here and no card in hand there is nothing to exchange; the game
	// used to offer it all the same (F-0372).
	@Test
	public void WithNothingStackedHereAndNoCardInHandTheExchangeIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetExchangeScenario();

		var hall = scn.GetFreepsCard("hall");
		var gimli = scn.GetFreepsCard("gimli");
		var courtesy = scn.GetFreepsCard("courtesy");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(hall, courtesy);
		// A card stacked on another condition is not stacked here.
		scn.StackCardsOn(courtesy, guard);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInZone(Zone.STACKED, guard);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetStackedCards(hall).size());
		assertEquals(java.util.List.of(guard), scn.GetStackedCards(courtesy));
		assertEquals(0, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(hall));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WhenPlayedTheTopEightCardsOfTheDrawDeckAreStackedHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetExchangeScenario();

		var hall = scn.GetFreepsCard("hall");
		var gimli = scn.GetFreepsCard("gimli");
		var d1 = scn.GetFreepsCard("armor");
		var d2 = scn.GetFreepsCard("guard");
		var d3 = scn.GetFreepsCard("strike");
		var d4 = scn.GetFreepsCard("fury");
		var d5 = scn.GetFreepsCard("blow");
		var d6 = scn.GetFreepsCard("d6");
		var d7 = scn.GetFreepsCard("d7");
		var d8 = scn.GetFreepsCard("d8");
		var d9 = scn.GetFreepsCard("d9");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(hall);

		scn.StartGame();
		scn.SetDrawDeck(VirtualTableScenario.P1, d1, d2, d3, d4, d5, d6, d7, d8, d9);
		assertEquals(9, scn.GetFreepsDeckCount());
		assertEquals(d1, scn.GetFreepsTopOfDeck());
		assertInHand(hall);

		scn.FreepsPlayCard(hall);

		assertInZone(Zone.SUPPORT, hall);
		assertEquals(8, scn.GetStackedCards(hall).size());
		assertTrue(scn.GetStackedCards(hall).containsAll(java.util.List.of(d1, d2, d3, d4, d5, d6, d7, d8)));
		// The ninth card stays where it was, now on top.
		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(d9, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnotherDwarvenConditionPlayedAfterItStacksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetExchangeScenario();

		var hall = scn.GetFreepsCard("hall");
		var gimli = scn.GetFreepsCard("gimli");
		var mines = scn.GetFreepsCard("mines");
		var d1 = scn.GetFreepsCard("armor");
		var d2 = scn.GetFreepsCard("guard");
		var d3 = scn.GetFreepsCard("strike");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(hall);
		scn.MoveCardsToHand(mines);

		scn.StartGame();
		scn.SetDrawDeck(VirtualTableScenario.P1, d1, d2, d3);
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetStackedCards(hall).size());
		assertInHand(mines);

		scn.FreepsPlayCard(mines);

		assertInZone(Zone.SUPPORT, mines);
		assertEquals(0, scn.GetStackedCards(hall).size());
		assertEquals(0, scn.GetStackedCards(mines).size());
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(d1, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningLeavesHandAndStackAsTheyWereAndTheStartOfArcheryOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetExchangeScenario();

		var hall = scn.GetFreepsCard("hall");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var guard = scn.GetFreepsCard("guard");
		var strike = scn.GetFreepsCard("strike");
		var fury = scn.GetFreepsCard("fury");
		var blow = scn.GetFreepsCard("blow");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(hall);
		scn.StackCardsOn(hall, armor, guard);
		scn.MoveCardsToHand(strike, fury, blow);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		assertInZone(Zone.STACKED, armor, guard);
		assertInHand(strike, fury, blow);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hall));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(2, scn.GetStackedCards(hall).size());
		assertInZone(Zone.STACKED, armor, guard);
		assertEquals(3, scn.GetFreepsHandCount());
		assertInHand(strike, fury, blow);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The start of the phase after: the same board, and nothing asked.
		scn.SkipToPhase(Phase.ARCHERY);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(hall));
		assertInZone(Zone.STACKED, armor, guard);
		assertInHand(strike, fury, blow);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void ADwarfAllyAloneIsSpottedAndACardStackedOnAnotherConditionStaysThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetExchangeScenario();

		var hall = scn.GetFreepsCard("hall");
		var thrarin = scn.GetFreepsCard("thrarin");
		var courtesy = scn.GetFreepsCard("courtesy");
		var armor = scn.GetFreepsCard("armor");
		var guard = scn.GetFreepsCard("guard");
		var strike = scn.GetFreepsCard("strike");
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(hall, courtesy);
		scn.StackCardsOn(hall, armor);
		scn.StackCardsOn(courtesy, guard);
		scn.MoveCardsToHand(strike);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		assertInZone(Zone.STACKED, armor, guard);
		assertEquals(java.util.List.of(armor), scn.GetStackedCards(hall));
		assertEquals(java.util.List.of(guard), scn.GetStackedCards(courtesy));
		assertEquals(1, scn.GetFreepsHandCount());
		assertInHand(strike);
		scn.SkipToPhase(Phase.MANEUVER);

		// Thrarin is the only Dwarf: Frodo is a Hobbit.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hall));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetFreepsHandCount());
		assertInHand(armor);
		assertEquals(java.util.List.of(strike), scn.GetStackedCards(hall));
		assertEquals(java.util.List.of(guard), scn.GetStackedCards(courtesy));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoDwarfToSpotTheExchangeIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetExchangeScenario();

		var hall = scn.GetFreepsCard("hall");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var guard = scn.GetFreepsCard("guard");
		var strike = scn.GetFreepsCard("strike");
		var fury = scn.GetFreepsCard("fury");
		var blow = scn.GetFreepsCard("blow");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(hall);
		scn.StackCardsOn(hall, armor, guard);
		scn.MoveCardsToHand(strike, fury, blow);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		assertInZone(Zone.STACKED, armor, guard);
		assertInHand(strike, fury, blow);
		scn.SkipToPhase(Phase.MANEUVER);

		// Aragorn and Frodo are companions, and neither is a Dwarf.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(hall));
		assertInZone(Zone.STACKED, armor, guard);
		assertInHand(strike, fury, blow);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void OfferedAgainAtTheStartOfHisNextManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetExchangeScenario();

		var hall = scn.GetFreepsCard("hall");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var guard = scn.GetFreepsCard("guard");
		var strike = scn.GetFreepsCard("strike");
		var fury = scn.GetFreepsCard("fury");
		var blow = scn.GetFreepsCard("blow");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(hall);
		scn.StackCardsOn(hall, armor, guard);
		scn.MoveCardsToHand(strike, fury, blow);

		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		assertInZone(Zone.STACKED, armor, guard);
		assertInHand(strike, fury, blow);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hall));
		scn.FreepsAcceptOptionalTrigger();
		assertInZone(Zone.STACKED, strike, fury, blow);

		// Player 2's turn, then his own turn 3, with a hand of one known card and another minion.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		scn.SetFreepsHand(armor);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetStackedCards(hall).size());
		scn.MoveMinionsToTable(troop2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hall));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(3, scn.GetFreepsHandCount());
		assertInHand(strike, fury, blow);
		assertEquals(java.util.List.of(armor), scn.GetStackedCards(hall));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
