package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_085_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "8_85");
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
					put("charged", "8_85");
					put("guard", "5_83");       // Household Guard: [rohan] Man, strength 6, vitality 3; valiant and nothing else
					// Rider's Spear: [rohan] hand weapon, cost 1, strength +2; damage +1 only on a mounted bearer
					put("spear", "4_288");
					// Heavy Chain: [rohan] armor, cost 1; its bearer is not overwhelmed unless his strength is tripled
					put("chain", "4_278");
					// Rider's Mount: [rohan] mount, cost 1; its text answers the start of a skirmish
					put("mount", "4_287");
					put("armor", "1_8");        // Dwarven Armor: a possession that is not [rohan]
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; damage +1
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
	public void ChargedHeadlongStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Charged Headlong
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: If you have initiative, discard a [rohan] possession to play a [rohan] possession from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Charged Headlong", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// Comprehensive Rules 4.2, "playing a card": "You cannot count The Card for initiative. ...  If you are the
	// Free Peoples player and initiative is a requirement for playing The Card, you must have four cards in your
	// hand not counting The Card" (F-0464; Ketura, 2026-10-11).
	@Test
	public void WithFourCardsInHandItselfAmongThemItCannotBePlayedAndWithFourBesideItItCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetInitiativeScenario();

		var charged = scn.GetFreepsCard("charged");
		var guard = scn.GetFreepsCard("guard");
		var spear = scn.GetFreepsCard("spear");
		var chain = scn.GetFreepsCard("chain");
		var mount = scn.GetFreepsCard("mount");
		var armor = scn.GetFreepsCard("armor");
		var card1 = scn.GetFreepsCard("troop");
		var card2 = scn.GetFreepsCard("runner");
		var card3 = scn.GetFreepsCard("marksman");
		var card4 = scn.GetFreepsCard("brigand");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, spear, chain);
		scn.MoveCardsToDiscard(mount, armor);
		scn.MoveCardsToHand(charged, card1, card2, card3);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, savage);

		// Four cards in hand, the event one of them.  He has initiative, and three cards beside the event: it
		// cannot be played, though there is a possession to discard and one to play.
		assertEquals(4, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, charged);
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(charged));

		// With four cards beside it, it can.
		scn.MoveCardsToHand(card4);
		scn.FreepsRefreshDecision();
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsPlayAvailable(charged));

		int twilight = scn.GetTwilight();
		scn.FreepsPlayCard(charged);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(spear, chain));
		scn.FreepsChooseCard(spear);
		assertInZone(Zone.DISCARD, spear);

		assertTrue(scn.FreepsHasCardChoiceAvailable(mount));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armor));
		scn.FreepsChooseCards(mount);

		assertAttachedTo(mount, guard);
		assertAttachedTo(chain, guard);
		assertInZone(Zone.DISCARD, charged, armor);
		assertEquals(4, scn.GetFreepsHandCount());
		// 1 for the event + 1 for the Mount
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
