package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_239_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("leowyn", "7_239");
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man companion, vitality 3; a skirmish ability used only on purpose
					// Merry, Master Holbytla: a [rohan] Hobbit companion, a Man only while 3 [rohan] companions (or Eowyn)
					// can be spotted, which these boards never have; his exertion trigger meets no exertion here
					put("merry", "103_85");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion; a skirmish ability
					// Cards for the hand, played by nobody: a companion, a possession, an event and a Shadow card
					put("rider2", "4_286");
					put("chain", "4_278");      // Heavy Chain: [rohan] possession
					put("defiance", "1_37");    // Defiance: [elven] skirmish event
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] minion, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LeowynStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Léowyn
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 2
		 * Site Number: 3T
		 * Game Text: <b>Villager</b>.
		 * <b>Fellowship:</b> If you have initiative, discard 2 cards from hand to heal a [rohan] Man.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("leowyn");

		assertEquals("Léowyn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VILLAGER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.TWO_TOWERS, 3)));
	}

	@Test
	public void DiscardsAnyTwoCardsToHealOneWoundFromARohanManHerselfIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leowyn = scn.GetFreepsCard("leowyn");
		var rider = scn.GetFreepsCard("rider");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var rider2 = scn.GetFreepsCard("rider2");
		var chain = scn.GetFreepsCard("chain");
		var defiance = scn.GetFreepsCard("defiance");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(rider, merry, boromir);
		scn.MoveAlliesToTable(leowyn);
		scn.MoveCardsToHand(rider2, chain, defiance, troop);

		scn.StartGame();
		assertInHand(rider2, chain, defiance, troop);
		scn.AddWoundsToChar(rider, 2);
		scn.AddWoundsToChar(leowyn, 1);
		scn.AddWoundsToChar(merry, 1);
		scn.AddWoundsToChar(boromir, 1);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetWoundsOn(leowyn));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(boromir));

		// Four cards in hand: he has initiative.
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsActionAvailable(leowyn));
		scn.FreepsUseCardAction(leowyn);

		// The cost: exactly two cards of the hand, whatever their type or side.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider2, chain, defiance, troop));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(chain, troop);
		assertInDiscard(chain, troop);
		assertInHand(rider2, defiance);
		assertEquals(2, scn.GetFreepsHandCount());

		// A [rohan] Man companion and Léowyn herself, a [rohan] Man ally; not the [rohan] Hobbit who is no Man
		// (two [rohan] companions only), nor the [gondor] Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, leowyn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, boromir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(rider);

		// One wound of two.
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetWoundsOn(leowyn));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithThreeCardsInHandItIsNotOfferedAndWithFourItIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leowyn = scn.GetFreepsCard("leowyn");
		var rider = scn.GetFreepsCard("rider");
		var rider2 = scn.GetFreepsCard("rider2");
		var chain = scn.GetFreepsCard("chain");
		var defiance = scn.GetFreepsCard("defiance");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(rider);
		scn.MoveAlliesToTable(leowyn);
		scn.MoveCardsToHand(rider2, chain, defiance);

		scn.StartGame();
		assertInHand(rider2, chain, defiance);
		scn.AddWoundsToChar(rider, 1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetWoundsOn(rider));

		// Three cards, enough to pay, but the Shadow has initiative.
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(leowyn));

		scn.MoveCardsToHand(troop);
		scn.FreepsRefreshDecision();
		assertInHand(troop);
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsActionAvailable(leowyn));

		scn.FreepsUseCardAction(leowyn);
		scn.FreepsChooseCards(rider2, troop);
		assertInDiscard(rider2, troop);
		// The one wounded [rohan] Man is healed with no choice asked.
		assertEquals(0, scn.GetWoundsOn(rider));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoWoundedRohanManItIsUsedAndItsCostPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leowyn = scn.GetFreepsCard("leowyn");
		var rider = scn.GetFreepsCard("rider");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var rider2 = scn.GetFreepsCard("rider2");
		var chain = scn.GetFreepsCard("chain");
		var defiance = scn.GetFreepsCard("defiance");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(rider, merry, boromir);
		scn.MoveAlliesToTable(leowyn);
		scn.MoveCardsToHand(rider2, chain, defiance, troop);

		scn.StartGame();
		assertInHand(rider2, chain, defiance, troop);
		scn.AddWoundsToChar(merry, 1);
		scn.AddWoundsToChar(boromir, 1);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(leowyn));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(boromir));

		// The wounded ones are a [rohan] Hobbit who is no Man and a [gondor] Man.
		assertTrue(scn.FreepsActionAvailable(leowyn));
		scn.FreepsUseCardAction(leowyn);
		scn.FreepsChooseCards(rider2, defiance);

		assertInDiscard(rider2, defiance);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
