package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_234_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fastness", "7_234");
					// Royal Guard: [rohan] Man ally, vitality 3; "to play, spot Theoden", which the cheat passes over, and a
					// skirmish ability used only on purpose
					put("guard", "102_54");
					// Guma, Plains Farmer: [rohan] Man ally, vitality 2, home 4T; a fellowship ability used only on purpose
					put("guma", "4_277");
					put("rider", "4_286");        // Rider of Rohan: [rohan] Man companion, vitality 3; a skirmish ability used only on purpose
					// Barliman Butterbur: [gandalf] Man ally, vitality 2; a fellowship ability used only on purpose
					put("barliman", "1_70");
					put("savage", "1_151");       // Uruk Savage: damage +1 and nothing else; a minion for the maneuver phase
					// Cards for the hand, played by nobody: a companion, a possession, an event, and a Shadow card
					put("rider2", "4_286");
					put("chain", "4_278");        // Heavy Chain: [rohan] possession
					put("defiance", "1_37");      // Defiance: [elven] skirmish event
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] minion, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetInitiativeScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fastness", "7_234");
					// Guma, Plains Farmer: [rohan] ally, vitality 2.  His home is a Towers site and his
					// ability a fellowship one.
					put("guma", "4_277");
					put("savage", "1_151");     // Uruk Savage: damage +1 and nothing else; a minion for the maneuver phase
					// The Free Peoples player's own copies of these are cards to hold in hand: he can play none
					// of them.
					put("troop", "1_177");      // Goblin Patrol Troop: no text
					put("runner", "1_178");     // Goblin Runner: its text is about being played
					put("marksman", "1_176");   // Goblin Marksman: archer and nothing else
					put("brigand", "4_10");     // Dunlending Brigand: no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GuardedFastnessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Guarded Fastness
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: If you have initiative, discard 2 cards from hand to heal all [rohan] allies.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fastness");

		assertEquals("Guarded Fastness", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// Comprehensive Rules 4.2, "playing a card": "You cannot count The Card for initiative. ...  If you are the
	// Free Peoples player and initiative is a requirement for playing The Card, you must have four cards in your
	// hand not counting The Card" (F-0464; Ketura, 2026-10-11).
	@Test
	public void WithFourCardsInHandItselfAmongThemItCannotBePlayedAndWithFourBesideItItCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetInitiativeScenario();

		var fastness = scn.GetFreepsCard("fastness");
		var guma = scn.GetFreepsCard("guma");
		var card1 = scn.GetFreepsCard("troop");
		var card2 = scn.GetFreepsCard("runner");
		var card3 = scn.GetFreepsCard("marksman");
		var card4 = scn.GetFreepsCard("brigand");
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(fastness, card1, card2, card3);
		assertInZone(Zone.HAND, fastness, card1, card2, card3);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(guma, 1);
		scn.SkipToPhase(Phase.MANEUVER);

		// Four cards in hand, the event one of them.  He has initiative, and three cards beside the event: it
		// cannot be played.
		assertEquals(4, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, fastness);
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(fastness));

		// With four cards beside it, it can.
		scn.MoveCardsToHand(card4);
		scn.FreepsRefreshDecision();
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsPlayAvailable(fastness));
		assertEquals(1, scn.GetWoundsOn(guma));

		scn.FreepsPlayCard(fastness);
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(card1, card2, card3, card4));
		scn.FreepsChooseCards(card1, card2);

		assertInZone(Zone.DISCARD, card1, card2, fastness);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(guma));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DiscardsTwoCardsToHealEachRohanAllyOnceAndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fastness = scn.GetFreepsCard("fastness");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var rider = scn.GetFreepsCard("rider");
		var barliman = scn.GetFreepsCard("barliman");
		var rider2 = scn.GetFreepsCard("rider2");
		var chain = scn.GetFreepsCard("chain");
		var defiance = scn.GetFreepsCard("defiance");
		var handTroop = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(rider);
		scn.MoveAlliesToTable(guard, guma, barliman);
		scn.MoveCardsToHand(fastness, rider2, chain, defiance, handTroop);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertInHand(fastness, rider2, chain, defiance, handTroop);
		scn.AddWoundsToChar(guard, 2);
		scn.AddWoundsToChar(guma, 1);
		scn.AddWoundsToChar(rider, 1);
		scn.AddWoundsToChar(barliman, 1);
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(guma));
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetWoundsOn(barliman));
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(fastness));
		scn.FreepsPlayCard(fastness);

		// The cost: any two cards of the hand, whatever their type or side.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider2, chain, defiance, handTroop));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(chain, handTroop);
		assertInDiscard(chain, handTroop);
		assertInHand(rider2, defiance);

		// Each [rohan] ally is healed once, the Royal Guard of one of his two wounds; not the [rohan] companion,
		// nor the ally of another culture.
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertInDiscard(fastness);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoWoundedRohanAllyItIsPlayedAndItsCostPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fastness = scn.GetFreepsCard("fastness");
		var guma = scn.GetFreepsCard("guma");
		var rider = scn.GetFreepsCard("rider");
		var barliman = scn.GetFreepsCard("barliman");
		var rider2 = scn.GetFreepsCard("rider2");
		var chain = scn.GetFreepsCard("chain");
		var defiance = scn.GetFreepsCard("defiance");
		var handTroop = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(rider);
		scn.MoveAlliesToTable(guma, barliman);
		scn.MoveCardsToHand(fastness, rider2, chain, defiance, handTroop);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertInHand(fastness, rider2, chain, defiance, handTroop);
		scn.AddWoundsToChar(rider, 1);
		scn.AddWoundsToChar(barliman, 1);
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetWoundsOn(barliman));
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// Guma, the one [rohan] ally, has no wound to heal.
		assertTrue(scn.FreepsPlayAvailable(fastness));
		scn.FreepsPlayCard(fastness);
		scn.FreepsChooseCards(rider2, defiance);

		assertInDiscard(fastness, rider2, defiance);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
