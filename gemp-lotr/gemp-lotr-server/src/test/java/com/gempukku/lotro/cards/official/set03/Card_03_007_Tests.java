package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_007_Tests
{
	// The One Ring, Answer To All Riddles has no response of its own, so the Ring-bearer's wounds are answered by
	// Arwen alone (The Ruling Ring would offer its own response to every wound in a skirmish).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "3_7");
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound Hobbit companion, not the Ring-bearer
					put("marksman1", "1_176");  // Goblin Marksman: archer minion, nothing else
					put("marksman2", "1_176");
					put("marksman3", "1_176");
					put("savage", "1_151");     // Uruk Savage: strength 5, damage +1, nothing else

					// Cards for the hand, of several types; their texts are never used.
					put("troop1", "1_177");     // Goblin Patrol Troop: a minion with no text
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a companion
					put("armor", "1_8");        // Dwarven Armor: a possession
					put("defend", "4_115");     // Defend It and Hope: an event
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.ATARRing
		);
	}

	@Test
	public void ArwenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Arwen, Elven Rider
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: <b>Ranger</b>.
		 * <b>Response:</b> If the Ring-bearer is about to take a wound, discard 3 cards from hand to prevent that wound.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arwen");

		assertEquals("Arwen", card.getBlueprint().getTitle());
		assertEquals("Elven Rider", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void ThreeCardsFromHandPreventAWoundToTheRingBearerAndNotToOtherCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var arwen = scn.GetFreepsCard("arwen");
		var sam = scn.GetFreepsCard("sam");
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		var marksman3 = scn.GetShadowCard("marksman3");
		var troop1 = scn.GetFreepsCard("troop1");
		var boromir = scn.GetFreepsCard("boromir");
		var armor = scn.GetFreepsCard("armor");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(arwen, sam);
		scn.MoveCardsToHand(troop1, boromir, armor, defend);
		assertInHand(troop1, boromir, armor, defend);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman1, marksman2, marksman3);
		scn.SkipToArcheryWounds();
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		// Sam, Ring-bound and not the Ring-bearer, and Arwen herself take theirs unanswered, with the cost payable.
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.FreepsChooseCard(sam);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(arwen));
		assertEquals(1, scn.GetWoundsOn(sam));

		assertEquals(0, scn.GetWoundsOn(arwen));
		scn.FreepsChooseCard(arwen);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(arwen));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(4, scn.GetFreepsHandCount());

		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsChooseCard(frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(arwen));
		scn.FreepsChooseOptionalTrigger(arwen);

		// Exactly three of the four, whatever their type.
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, boromir, armor, defend));
		scn.FreepsChooseCards(troop1, armor, defend);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(troop1, armor, defend);
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertInHand(boromir);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OneWoundIsPreventedForThreeCardsAndWithTwoLeftTheNextIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var arwen = scn.GetFreepsCard("arwen");
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		var troop1 = scn.GetFreepsCard("troop1");
		var troop2 = scn.GetFreepsCard("troop2");
		var troop3 = scn.GetFreepsCard("troop3");
		var boromir = scn.GetFreepsCard("boromir");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(troop1, troop2, troop3, boromir, armor);
		assertInHand(troop1, troop2, troop3, boromir, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman1, marksman2);
		scn.SkipToArcheryWounds();
		assertEquals(2, scn.GetShadowArcheryTotal());

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.FreepsChooseCard(frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(arwen));
		scn.FreepsChooseOptionalTrigger(arwen);
		scn.FreepsChooseCards(troop1, troop2, troop3);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertInDiscard(troop1, troop2, troop3);

		// The second wound has a window of its own, and two cards are one short of three: it is taken.
		scn.FreepsChooseCard(frodo);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(arwen));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInHand(boromir, armor);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void EachResponsePreventsOneWoundOfADamagePlusOneLoss() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var arwen = scn.GetFreepsCard("arwen");
		var savage = scn.GetShadowCard("savage");
		var troop1 = scn.GetFreepsCard("troop1");
		var troop2 = scn.GetFreepsCard("troop2");
		var troop3 = scn.GetFreepsCard("troop3");
		var boromir = scn.GetFreepsCard("boromir");
		var armor = scn.GetFreepsCard("armor");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(troop1, troop2, troop3, boromir, armor, defend);
		assertInHand(troop1, troop2, troop3, boromir, armor, defend);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.FreepsResolveSkirmish(frodo);
		// Uruk Savage, strength 5, beats Frodo, strength 3, without overwhelming him: two wounds with damage +1.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(6, scn.GetFreepsHandCount());
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(arwen));
		scn.FreepsChooseOptionalTrigger(arwen);
		scn.FreepsChooseCards(troop1, troop2, troop3);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetFreepsHandCount());

		// The other wound is answered again, with three cards still in hand, and declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(arwen));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInHand(boromir, armor, defend);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
