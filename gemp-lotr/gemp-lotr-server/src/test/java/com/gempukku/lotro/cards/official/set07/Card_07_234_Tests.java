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
					put("card", "7_234");
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

		var card = scn.GetFreepsCard("card");

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

		int twilight = scn.GetTwilight();
		scn.FreepsPlayCard(fastness);
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(card1, card2, card3, card4));
		scn.FreepsChooseCards(card1, card2);

		assertInZone(Zone.DISCARD, card1, card2, fastness);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(twilight + 1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(guma));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
