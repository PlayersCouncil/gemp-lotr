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

public class Card_08_111_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "8_111");
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
					put("sofair", "8_111");
					// Pippin, Friend to Frodo: unbound Hobbit, strength 3.  His text is about [shire] tales.
					put("pippin", "1_306");
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
	public void SoFairSoDesperateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: So Fair, So Desperate
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: If the Shadow has initiative, add a threat to make an unbound Hobbit strength +1 for each [shire] card you spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("So Fair, So Desperate", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// Comprehensive Rules 4.2, "playing a card": "You cannot count The Card for initiative."  For a Free Peoples
	// event that needs the SHADOW to have initiative that is three cards or fewer beside it (F-0464; Ketura,
	// 2026-10-11).
	@Test
	public void WithFourCardsInHandBesideItItCannotBePlayedAndWithFourItselfAmongThemItCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetInitiativeScenario();

		var sofair = scn.GetFreepsCard("sofair");
		var pippin = scn.GetFreepsCard("pippin");
		var card1 = scn.GetFreepsCard("troop");
		var card2 = scn.GetFreepsCard("runner");
		var card3 = scn.GetFreepsCard("marksman");
		var card4 = scn.GetFreepsCard("brigand");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(sofair, card1, card2, card3, card4);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(pippin, savage);

		// Five cards in hand, four beside the event: the Shadow does not have initiative, and the event cannot
		// be played.
		assertEquals(5, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, sofair);
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(sofair));

		// Four cards, the event one of them.  The Free Peoples player has initiative while it lies in his hand;
		// not counting it he has three cards, the Shadow has initiative, and the event can be played.
		scn.MoveCardsToDiscard(card4);
		scn.FreepsRefreshDecision();
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsPlayAvailable(sofair));
		assertEquals(0, scn.GetThreats());
		assertEquals(3, scn.GetStrength(pippin));

		scn.FreepsPlayCard(sofair);
		// Frodo and Pippin are the [shire] cards to spot.
		scn.FreepsChoose("2");

		assertEquals(1, scn.GetThreats());
		// 3 + 1 for each of Frodo and Pippin
		assertEquals(5, scn.GetStrength(pippin));
		assertInZone(Zone.DISCARD, sofair);
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
