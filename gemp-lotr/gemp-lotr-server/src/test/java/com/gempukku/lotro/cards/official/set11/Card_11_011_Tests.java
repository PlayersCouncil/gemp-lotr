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
					put("card", "11_11");
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

		var card = scn.GetFreepsCard("card");

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
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetStackedCards(hall).size());
		assertEquals(2, scn.GetFreepsHandCount());

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
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetStackedCards(hall).size());
		assertEquals(0, scn.GetFreepsHandCount());

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
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(hall);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetStackedCards(hall).size());
		assertEquals(0, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(hall));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
