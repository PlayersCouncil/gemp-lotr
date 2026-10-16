package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_011_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cast", "3_11");
					// Lorien Elf: [elven] Elf companion, strength 4; "To play, spot an Elf", so it is cheated in
					put("elf", "1_53");
					// Silinde, Elf of Mirkwood: [elven] Elf ally, home site 3; has site 3's text only while that site is on the path
					put("silinde", "1_60");
					// Boromir, Son of Denethor: [gondor] Man companion; a skirmish ability used only on purpose
					put("boromir", "1_97");
					// Final Count: an [elven] support-area condition, no Elf; its texts need Legolas
					put("count", "4_69");
					// Goblin Patrol Troop: [moria] Orc minion with no text, on the table so that there is a maneuver phase
					put("troop1", "1_177");

					// Cards for the hands and the top of the draw deck, of several types; their texts are never used.
					put("troop2", "1_177");     // Goblin Patrol Troop: a minion
					put("armor", "1_8");        // Dwarven Armor: a possession
					put("defend", "4_115");     // Defend It and Hope: an event
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CastItIntotheFireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Cast It Into the Fire!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Spot an Elf to make each opponent discard a card from his or her hand. Draw a
		 * card for each card discarded in this way.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cast");

		assertEquals("Cast It Into the Fire!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheShadowPlayerDiscardsACardOfHisChoiceAndTheFreePeoplesPlayerDrawsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cast = scn.GetFreepsCard("cast");
		var elf = scn.GetFreepsCard("elf");
		var keep = scn.GetFreepsCard("armor");
		var top = scn.GetFreepsCard("defend");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var armor = scn.GetShadowCard("armor");
		var defend = scn.GetShadowCard("defend");
		scn.MoveCompanionsToTable(elf);
		scn.MoveCardsToHand(cast, keep);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetShadowHand(troop2, armor, defend);
		scn.MoveCardsToTopOfDeck(top);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetShadowHandCount());
		int shadowDiscard = scn.GetShadowDiscardCount();
		assertEquals(2, scn.GetFreepsHandCount());
		assertSame(top, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, top);
		assertInHand(cast, keep);
		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsPlayAvailable(cast));

		scn.FreepsPlayCard(cast);
		// The Shadow player chooses, from the whole of his hand, exactly one card.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop2, armor, defend));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(armor);

		assertInDiscard(armor);
		assertEquals(shadowDiscard + 1, scn.GetShadowDiscardCount());
		assertInHand(troop2, defend);
		assertEquals(2, scn.GetShadowHandCount());
		// One card discarded: one card drawn, the top of the deck.  The Free Peoples player's own card stays.
		assertInHand(keep, top);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertInDiscard(cast);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithOnlyAnElfAllyItIsPlayedAndWithTheShadowHandEmptyNothingIsDiscardedOrDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cast = scn.GetFreepsCard("cast");
		var silinde = scn.GetFreepsCard("silinde");
		var boromir = scn.GetFreepsCard("boromir");
		var keep = scn.GetFreepsCard("armor");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(cast, keep);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetShadowHand();
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetShadowHandCount());
		int shadowDiscard = scn.GetShadowDiscardCount();
		assertEquals(2, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		assertInHand(cast, keep);
		// The ally is the only Elf there is.
		assertTrue(scn.FreepsPlayAvailable(cast));

		scn.FreepsPlayCard(cast);
		assertInDiscard(cast);
		assertEquals(shadowDiscard, scn.GetShadowDiscardCount());
		assertInHand(keep);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAnElfThoughAnElvenConditionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cast = scn.GetFreepsCard("cast");
		var boromir = scn.GetFreepsCard("boromir");
		var count = scn.GetFreepsCard("count");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(count);
		scn.MoveCardsToHand(cast);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetShadowHand(troop2);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, count);
		assertInHand(cast);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(cast));
	}
}
