package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_020_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("master", "32_20");
					put("bard", "30_22");         // Bard, The Bowman: [esgaroth] Man ally, vitality 3; a regroup ability not used
					// Esgaroth Volunteers: [esgaroth] Man ally of another title, vitality 4; a maneuver ability not used
					put("volunteers", "32_17");

					// Cards held in hand: a companion, a possession, an event and two Shadow minions.
					put("balin", "30_2");         // Balin, Brother of Dwalin
					put("spear", "32_4");         // Dwarven Spear
					put("fury", "30_3");          // Battle Fury
					put("aggressor", "30_39");    // Orkish Aggressor
					put("bats", "32_30");         // Dark Bats

					// Cards put on top of the draw deck, to be read by name once drawn.
					put("thorin", "30_19");       // Thorin, Oakenshield
					put("kili", "30_12");         // Kili, Brother of Fili
					put("sentinel", "33_49");     // Orc Sentinel
					put("underling", "33_50");    // Orc Underling
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TheMasterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: The Master, Master of Lake-Town
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Esgaroth
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Man
		 * Home: 6H
		 * Strength: 5
		 * Vitality: 3
		 * Game Text: At the start of each of your turns, you may spot Bard to heal Bard and The Master.
		 * 	<b>Fellowship:</b> Exert The Master twice to discard up to 3 cards from hand and draw an equal number of cards.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("master");

		assertEquals("The Master", card.getBlueprint().getTitle());
		assertEquals("Master of Lake-Town", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ESGAROTH, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.HOBBIT, 6)));
	}

	@Test
	public void AtTheStartOfTheTurnSpottingBardHealsBardAndTheMasterOnceEachAndNoOtherAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var master = scn.GetFreepsCard("master");
		var bard = scn.GetFreepsCard("bard");
		var volunteers = scn.GetFreepsCard("volunteers");
		scn.MoveAlliesToTable(master, bard, volunteers);
		scn.AddWoundsToChar(master, 2);
		scn.AddWoundsToChar(bard, 2);
		scn.AddWoundsToChar(volunteers, 2);
		assertEquals(2, scn.GetWoundsOn(master));
		assertEquals(2, scn.GetWoundsOn(bard));
		assertEquals(2, scn.GetWoundsOn(volunteers));

		scn.StartGame();
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(master));
		scn.FreepsAcceptOptionalTrigger();

		// One wound off each of the two named, and none off the other [esgaroth] ally.
		assertEquals(1, scn.GetWoundsOn(master));
		assertEquals(1, scn.GetWoundsOn(bard));
		assertEquals(2, scn.GetWoundsOn(volunteers));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithBardUnwoundedTheOfferIsStillMadeAndHealsTheMaster() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var master = scn.GetFreepsCard("master");
		var bard = scn.GetFreepsCard("bard");
		scn.MoveAlliesToTable(master, bard);
		scn.AddWoundsToChar(master, 1);
		assertEquals(1, scn.GetWoundsOn(master));
		assertEquals(0, scn.GetWoundsOn(bard));

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(master));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(master));
		assertEquals(0, scn.GetWoundsOn(bard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWithoutBardThoughAnotherEsgarothAllyIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var master = scn.GetFreepsCard("master");
		var volunteers = scn.GetFreepsCard("volunteers");
		scn.MoveAlliesToTable(master, volunteers);
		scn.AddWoundsToChar(master, 2);
		scn.AddWoundsToChar(volunteers, 2);
		assertEquals(2, scn.GetWoundsOn(master));
		assertEquals(2, scn.GetWoundsOn(volunteers));

		scn.StartGame();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(master));
		assertEquals(2, scn.GetWoundsOn(master));
		assertEquals(2, scn.GetWoundsOn(volunteers));
	}

	@Test
	public void NotOfferedWithBardInPlayAndNobodyToHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var master = scn.GetFreepsCard("master");
		var bard = scn.GetFreepsCard("bard");
		scn.MoveAlliesToTable(master, bard);
		assertEquals(0, scn.GetWoundsOn(master));
		assertEquals(0, scn.GetWoundsOn(bard));

		scn.StartGame();
		// The heal would do nothing, and the offer costs nothing, so it is not made (Q-0034).
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(master));
	}

	@Test
	public void DeclinedItHealsNothingAndIsOfferedAgainAtTheStartOfHisNextTurnAndNotOnTheOtherPlayersTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var master = scn.GetFreepsCard("master");
		var bard = scn.GetFreepsCard("bard");
		scn.MoveAlliesToTable(master, bard);
		scn.AddWoundsToChar(master, 2);
		scn.AddWoundsToChar(bard, 2);
		assertEquals(2, scn.GetWoundsOn(master));
		assertEquals(2, scn.GetWoundsOn(bard));

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(master));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(master));
		assertEquals(2, scn.GetWoundsOn(bard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Turn 2 is the other player's: his fellowship phase opens with nothing asked of this player.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(master));
		assertEquals(2, scn.GetWoundsOn(master));
		assertEquals(2, scn.GetWoundsOn(bard));

		// Turn 3, this player's again.
		scn.SkipToOtherPlayersTurn();
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(master));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(master));
		assertEquals(1, scn.GetWoundsOn(bard));
		// The fellowship is at The Troll Hoard (site 2), which makes no offer of its own: the Dwarven Spear is the one
		// hand weapon, and no [dwarven] character is in play to bear it (F-0800).
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(scn.GetCurrentSite()));
	}

	@Test
	public void ExertsTheMasterTwiceToDiscardThreeChosenCardsFromHandAndDrawThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var master = scn.GetFreepsCard("master");
		var bard = scn.GetFreepsCard("bard");
		var balin = scn.GetFreepsCard("balin");
		var spear = scn.GetFreepsCard("spear");
		var fury = scn.GetFreepsCard("fury");
		var aggressor = scn.GetFreepsCard("aggressor");
		var bats = scn.GetFreepsCard("bats");
		var thorin = scn.GetFreepsCard("thorin");
		var kili = scn.GetFreepsCard("kili");
		var sentinel = scn.GetFreepsCard("sentinel");
		var underling = scn.GetFreepsCard("underling");
		scn.MoveAlliesToTable(master, bard);
		scn.MoveCardsToHand(balin, spear, fury, aggressor, bats);
		scn.MoveCardsToShadowHand("balin", "aggressor");

		scn.StartGame();
		// Orc Underling is the top card of the deck, then the Orc Sentinel, Kili and Thorin.
		scn.MoveCardsToTopOfDeck(thorin, kili, sentinel, underling);
		scn.FreepsRefreshDecision();

		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetWoundsOn(master));
		assertEquals(0, scn.GetWoundsOn(bard));
		assertInHand(balin, spear, fury, aggressor, bats);
		assertInZone(Zone.DECK, thorin, kili, sentinel, underling);
		assertSame(underling, scn.GetFreepsTopOfDeck());
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.FreepsActionAvailable(master));

		scn.FreepsUseCardAction(master);
		// The Master exerts twice, and Bard, the other ally, does not.
		assertEquals(2, scn.GetWoundsOn(master));
		assertEquals(0, scn.GetWoundsOn(bard));

		// Any card in hand may go, of either side and of any type: none of them need, and no more than 3 may.
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, spear, fury, aggressor, bats));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(balin, fury, aggressor);

		// 3 discarded and 3 drawn: the three cards from the top of the deck, and not the fourth.
		assertInDiscard(balin, fury, aggressor);
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertInHand(spear, bats, underling, sentinel, kili);
		assertEquals(5, scn.GetFreepsHandCount()); // 5 - 3 discarded + 3 drawn
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertSame(thorin, scn.GetFreepsTopOfDeck());

		// The hand and the deck are the player's own.
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardsOneChosenCardAndDrawsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var master = scn.GetFreepsCard("master");
		var balin = scn.GetFreepsCard("balin");
		var spear = scn.GetFreepsCard("spear");
		var fury = scn.GetFreepsCard("fury");
		var aggressor = scn.GetFreepsCard("aggressor");
		var sentinel = scn.GetFreepsCard("sentinel");
		var underling = scn.GetFreepsCard("underling");
		scn.MoveAlliesToTable(master);
		scn.MoveCardsToHand(balin, spear, fury, aggressor);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(sentinel, underling);
		scn.FreepsRefreshDecision();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetWoundsOn(master));
		assertInHand(balin, spear, fury, aggressor);
		assertInZone(Zone.DECK, sentinel, underling);
		assertSame(underling, scn.GetFreepsTopOfDeck());
		assertEquals(4, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(master);
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(spear);

		assertEquals(2, scn.GetWoundsOn(master));
		assertInDiscard(spear);
		assertEquals(1, scn.GetFreepsDiscardCount());
		// One card drawn for the one discarded: the top card, and the next stays on the deck.
		assertInHand(balin, fury, aggressor, underling);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertSame(sentinel, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ChoosingNoCardStillExertsTheMasterTwiceAndNothingIsDiscardedOrDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var master = scn.GetFreepsCard("master");
		var balin = scn.GetFreepsCard("balin");
		var spear = scn.GetFreepsCard("spear");
		var underling = scn.GetFreepsCard("underling");
		scn.MoveAlliesToTable(master);
		scn.MoveCardsToHand(balin, spear);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(underling);
		scn.FreepsRefreshDecision();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetWoundsOn(master));
		assertInHand(balin, spear);
		assertInZone(Zone.DECK, underling);
		assertSame(underling, scn.GetFreepsTopOfDeck());

		scn.FreepsUseCardAction(master);
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, spear));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		// "Up to 3" may be none. The two exertions were the price, and are paid all the same.
		assertEquals(2, scn.GetWoundsOn(master));
		assertInHand(balin, spear);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertSame(underling, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAnEmptyHandTheMasterStillExertsTwiceAndNothingIsDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var master = scn.GetFreepsCard("master");
		scn.MoveAlliesToTable(master);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck > 0);
		assertEquals(0, scn.GetWoundsOn(master));
		assertEquals(0, scn.GetFreepsHandCount());
		// The exertions can be paid, so the ability is there to be used, for nothing.
		assertTrue(scn.FreepsActionAvailable(master));

		scn.FreepsUseCardAction(master);
		assertEquals(2, scn.GetWoundsOn(master));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheMasterCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var master = scn.GetFreepsCard("master");
		var balin = scn.GetFreepsCard("balin");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveAlliesToTable(master);
		scn.MoveCardsToHand(balin, spear);

		scn.StartGame();
		// Vitality 3 with 1 wound: he can exert once, not twice.
		scn.AddWoundsToChar(master, 1);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetWoundsOn(master));
		assertInHand(balin, spear);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.GetFreepsDeckCount() > 0);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(master));

		scn.RemoveWoundsFromChar(master, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(master));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
