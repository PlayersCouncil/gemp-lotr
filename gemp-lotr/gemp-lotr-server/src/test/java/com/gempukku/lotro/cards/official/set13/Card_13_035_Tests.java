package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_035_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("colour", "13_35");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] companion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion
					put("wisdom", "2_23");      // Gandalf's Wisdom: [gandalf] condition in the support area
					put("draught", "9_24");     // Ent Draught: [gandalf] condition borne by a companion
					put("lord", "1_21");        // Lord of Moria: [dwarven] condition in the support area
					put("cart", "1_73");        // Gandalf's Cart: [gandalf] possession in the support area
					put("ent", "6_27");         // Ent Avenger: the top card of the draw deck, to be drawn

					put("seeking", "1_276");    // Seeking Its Master: [sauron] condition in the support area
					put("weary", "4_212");      // Weary: [isengard] condition borne by a companion
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoColourNowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: No Colour Now
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Discard your [gandalf] condition from play to discard a condition from play and draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("colour");

		assertEquals("No Colour Now", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsAGandalfConditionToDiscardAConditionOfEitherSideAndDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var colour = scn.GetFreepsCard("colour");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var wisdom = scn.GetFreepsCard("wisdom");
		var draught = scn.GetFreepsCard("draught");
		var lord = scn.GetFreepsCard("lord");
		var cart = scn.GetFreepsCard("cart");
		var ent = scn.GetFreepsCard("ent");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(wisdom, lord, cart);
		scn.AttachCardsTo(gandalf, draught);
		scn.MoveCardsToHand(colour);

		var seeking = scn.GetShadowCard("seeking");
		var weary = scn.GetShadowCard("weary");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(seeking);
		scn.AttachCardsTo(aragorn, weary);
		assertInZone(Zone.SUPPORT, wisdom, lord, cart, seeking);
		assertAttachedTo(draught, gandalf);
		assertAttachedTo(weary, aragorn);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(ent);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		assertInZone(Zone.DECK, ent);
		int deck = scn.GetFreepsDeckCount();

		assertTrue(scn.FreepsPlayAvailable(colour));
		scn.FreepsPlayCard(colour);

		// The cost: a [gandalf] condition, in the support area or borne; not a condition of another culture, nor a
		// [gandalf] possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(wisdom, draught));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lord, cart));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(wisdom);
		assertInDiscard(wisdom);

		// The effect: a condition of either side, in a support area or borne; not a possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(draught, lord, seeking, weary));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(cart));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(weary);
		assertInDiscard(weary);
		assertAttachedTo(draught, gandalf);
		assertInZone(Zone.SUPPORT, lord, cart, seeking);

		// And a card drawn.
		assertInHand(ent);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertInDiscard(colour);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotPlayableWithoutAGandalfConditionToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var colour = scn.GetFreepsCard("colour");
		var gandalf = scn.GetFreepsCard("gandalf");
		var wisdom = scn.GetFreepsCard("wisdom");
		var lord = scn.GetFreepsCard("lord");
		var cart = scn.GetFreepsCard("cart");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(lord, cart);
		scn.MoveCardsToHand(colour);

		var seeking = scn.GetShadowCard("seeking");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(seeking);
		assertInZone(Zone.SUPPORT, lord, cart, seeking);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// A condition of another culture and a [gandalf] possession do not pay the cost.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(colour));

		scn.MoveCardsToSupportArea(wisdom);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(colour));
	}

	@Test
	public void WithNoOtherConditionInPlayItIsPlayedTheCostPaidAndACardDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var colour = scn.GetFreepsCard("colour");
		var gandalf = scn.GetFreepsCard("gandalf");
		var wisdom = scn.GetFreepsCard("wisdom");
		var cart = scn.GetFreepsCard("cart");
		var ent = scn.GetFreepsCard("ent");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(wisdom, cart);
		scn.MoveCardsToHand(colour);
		assertInZone(Zone.SUPPORT, wisdom, cart);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(ent);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		assertInZone(Zone.DECK, ent);

		scn.FreepsPlayCard(colour);
		assertInDiscard(wisdom, colour);
		assertInZone(Zone.SUPPORT, cart);
		assertInHand(ent);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
