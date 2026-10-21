package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_024_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hugin", "2_24");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is not Gandalf

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Free Peoples companion
					put("shield", "1_107");     // Great Shield: a Free Peoples possession
					put("sleep", "1_84");       // Sleep, Caradhras: a Free Peoples event
					put("troop", "1_177");      // Goblin Patrol Troop: a Shadow minion, no text
					put("cloak", "1_42");       // Elven Cloak: a Free Peoples possession, laid in the other player's discard pile only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HuginStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Hugin, Emissary from Laketown
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: To play, spot Gandalf.
		 * <b>Fellowship:</b> Exert Hugin to reveal a Free Peoples card from your discard pile and place it beneath your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hugin");

		assertEquals("Hugin", card.getBlueprint().getTitle());
		assertEquals("Emissary from Laketown", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void CannotBePlayedUntilGandalfIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hugin = scn.GetFreepsCard("hugin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(hugin, gandalf);

		scn.StartGame();

		// The Ent Avenger is a [gandalf] companion, and no Gandalf.
		assertFalse(scn.FreepsPlayAvailable(hugin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(hugin));

		scn.FreepsPlayCard(hugin);
		assertInZone(Zone.SUPPORT, hugin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionExertsHuginToRevealAFreePeoplesCardFromTheDiscardPileAndPlaceItBeneathTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hugin = scn.GetFreepsCard("hugin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var shield = scn.GetFreepsCard("shield");
		var sleep = scn.GetFreepsCard("sleep");
		var troop = scn.GetFreepsCard("troop");
		var cloak = scn.GetShadowCard("cloak");
		scn.MoveAlliesToTable(hugin);
		scn.MoveCardsToDiscard(aragorn, shield, sleep, troop, cloak);

		scn.StartGame();

		assertInDiscard(aragorn, shield, sleep, troop, cloak);
		assertEquals(0, scn.GetWoundsOn(hugin));
		assertTrue(scn.FreepsActionAvailable(hugin));
		scn.FreepsUseCardAction(hugin);
		assertEquals(1, scn.GetWoundsOn(hugin));

		// A companion, a possession and an event of his own; not his own Shadow card, nor the other player's
		// Free Peoples card in the other player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, shield, sleep));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop, cloak));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(shield);

		// Revealed: each player is shown the card chosen and no other from the pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(shield, false));
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(shield, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(aragorn, false));
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		scn.DismissRevealedCards();

		assertEquals(shield, scn.GetFreepsBottomOfDeck());
		assertInDiscard(aragorn, sleep, troop, cloak);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoFreePeoplesCardInHisDiscardPileTheActionIsPaidForAndMovesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hugin = scn.GetFreepsCard("hugin");
		var troop = scn.GetFreepsCard("troop");
		var cloak = scn.GetShadowCard("cloak");
		scn.MoveAlliesToTable(hugin);
		scn.MoveCardsToDiscard(troop, cloak);

		scn.StartGame();

		var bottom = scn.GetFreepsBottomOfDeck();
		assertInDiscard(troop, cloak);
		assertEquals(0, scn.GetWoundsOn(hugin));
		// The exertion can be paid, so the action is offered (Q-0034: an action with a cost).
		assertTrue(scn.FreepsActionAvailable(hugin));
		scn.FreepsUseCardAction(hugin);

		assertEquals(1, scn.GetWoundsOn(hugin));
		assertInDiscard(troop, cloak);
		assertEquals(bottom, scn.GetFreepsBottomOfDeck());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedOnceHuginIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hugin = scn.GetFreepsCard("hugin");
		var shield = scn.GetFreepsCard("shield");
		scn.MoveAlliesToTable(hugin);
		scn.MoveCardsToDiscard(shield);

		scn.StartGame();
		// Vitality 2: one wound leaves him exhausted.
		scn.AddWoundsToChar(hugin, 1);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetWoundsOn(hugin));
		assertInDiscard(shield);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(hugin));
	}
}
