package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_011_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("toss", "6_11");
					put("toss2", "6_11");
					put("gimli", "1_13");       // Gimli, Son of Glóin: a [dwarven] character, for the hand
					put("axe", "1_9");          // Dwarven Axe: a [dwarven] item
					put("strike", "1_3");       // Axe Strike: a [dwarven] card that is neither
					put("elf", "1_53");         // Lórien Elf: a card of another culture
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetStackScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("toss", "6_11");
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6
					put("guard", "1_7");        // Dwarf Guard: another Dwarf, strength 4, whom the text does not name
					// Here Is Good Rock: [dwarven] condition in the support area.  "Skirmish: Stack a Free Peoples
					// card from hand here to make a Dwarf damage +1" is how a card comes to be stacked on it.
					put("rock", "4_50");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetStackByPlayScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("toss", "6_11");
					put("toss2", "6_11");
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6
					put("guard", "1_7");        // Dwarf Guard: another Dwarf, strength 4, whom the text does not name
					// Here Is Good Rock: [dwarven] tale in the support area, "Skirmish: Stack a Free Peoples card
					// from hand here to make a Dwarf damage +1"
					put("rock", "4_50");
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, Damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TossMeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Toss Me
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: While this card is stacked on a [dwarven] condition, Gimli is strength +1.<br><b>Fellowship:</b> Place this card or another [dwarven] card from hand on top of or beneath your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("toss");

		assertEquals("Toss Me", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhileStackedOnADwarvenConditionGimliIsStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStackScenario();

		var toss = scn.GetFreepsCard("toss");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var rock = scn.GetFreepsCard("rock");
		scn.MoveCompanionsToTable(gimli, guard);
		scn.MoveCardsToSupportArea(rock);
		scn.MoveCardsToHand(toss);

		scn.StartGame();

		// In hand the card does nothing.
		assertInHand(toss);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));

		// The stacking is arrangement: what is shown is the text of a card that lies on a stack and is not in play.
		scn.StackCardsOn(rock, toss);

		assertInZone(Zone.STACKED, toss);
		assertEquals(List.of(toss), scn.GetStackedCards(rock));
		// 6 + 1, and for Gimli alone.
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachCopyStackedByPlayAddsItsOwnBonusAndACopyInHandNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStackByPlayScenario();

		var toss = scn.GetFreepsCard("toss");
		var toss2 = scn.GetFreepsCard("toss2");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var rock = scn.GetFreepsCard("rock");
		scn.MoveCompanionsToTable(gimli, guard);
		scn.MoveCardsToSupportArea(rock);
		scn.StackCardsOn(rock, toss);
		scn.MoveCardsToHand(toss2);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertEquals(List.of(toss), scn.GetStackedCards(rock));
		assertInHand(toss2);
		// One copy stacked, the other in hand: 6 + 1.
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);

		// Here Is Good Rock stacks the second copy from hand, by play.
		assertTrue(scn.FreepsActionAvailable(rock));
		scn.FreepsUseCardAction(rock);
		// The one card in hand is stacked unasked; the Dwarf is chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard));
		scn.FreepsChooseCard(gimli);

		assertEquals(List.of(toss, toss2), scn.GetStackedCards(rock));
		// 6 + 1 + 1.
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoOtherDwarvenCardInHandTheEventPlacesItselfOnTopOfTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toss = scn.GetFreepsCard("toss");
		var elf = scn.GetFreepsCard("elf");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCardsToHand(toss, elf);

		scn.StartGame();
		scn.SetDrawDeck(scn.P1(), axe);

		assertInHand(toss, elf);
		assertEquals(axe, scn.GetFreepsTopOfDeck());
		assertEquals(1, scn.GetFreepsDeckCount());

		assertTrue(scn.FreepsPlayAvailable(toss));
		scn.FreepsPlayCard(toss);

		// Lórien Elf is no [dwarven] card, so "this card" is the only branch: only top or beneath is asked.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("on top"));
		assertTrue(scn.FreepsChoiceAvailable("beneath"));
		scn.FreepsChooseOption("on top");

		assertEquals(toss, scn.GetFreepsTopOfDeck());
		assertEquals(axe, scn.GetFreepsBottomOfDeck());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertInHand(elf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheEventMayPlaceItselfBeneathTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toss = scn.GetFreepsCard("toss");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCardsToHand(toss);

		scn.StartGame();
		scn.SetDrawDeck(scn.P1(), axe);

		assertInHand(toss);
		assertEquals(axe, scn.GetFreepsTopOfDeck());

		scn.FreepsPlayCard(toss);
		scn.FreepsChooseOption("beneath");

		assertEquals(axe, scn.GetFreepsTopOfDeck());
		assertEquals(toss, scn.GetFreepsBottomOfDeck());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheEventMayPlaceAnotherDwarvenCardFromHandOnTopOfOrBeneathTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toss = scn.GetFreepsCard("toss");
		var toss2 = scn.GetFreepsCard("toss2");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var strike = scn.GetFreepsCard("strike");
		var elf = scn.GetFreepsCard("elf");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCardsToHand(toss, toss2, gimli, axe, strike, elf);

		scn.StartGame();
		scn.SetDrawDeck(scn.P1(), troop);

		assertInHand(toss, toss2, gimli, axe, strike, elf);
		assertEquals(troop, scn.GetFreepsTopOfDeck());

		scn.FreepsPlayCard(toss);

		// Both branches can be done: this card, or another [dwarven] card.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("this card"));
		assertTrue(scn.FreepsChoiceAvailable("another"));
		scn.FreepsChooseOption("another");
		scn.FreepsChooseOption("beneath");

		// A [dwarven] character, item and event, and another copy of the event; not a card of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(toss2, gimli, axe, strike));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elf, toss));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(axe);

		assertEquals(troop, scn.GetFreepsTopOfDeck());
		assertEquals(axe, scn.GetFreepsBottomOfDeck());
		assertInDiscard(toss);
		assertInHand(toss2, gimli, strike, elf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOtherDwarvenCardMayGoOnTopOfTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toss = scn.GetFreepsCard("toss");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCardsToHand(toss, gimli);

		scn.StartGame();
		scn.SetDrawDeck(scn.P1(), troop);

		assertInHand(toss, gimli);
		assertEquals(troop, scn.GetFreepsTopOfDeck());

		scn.FreepsPlayCard(toss);
		scn.FreepsChooseOption("another");
		scn.FreepsChooseOption("on top");

		// The one [dwarven] card in hand goes unasked.
		assertEquals(gimli, scn.GetFreepsTopOfDeck());
		assertEquals(troop, scn.GetFreepsBottomOfDeck());
		assertInDiscard(toss);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheEventIsNotPlayableInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toss = scn.GetFreepsCard("toss");
		scn.MoveCardsToHand(toss);
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(toss));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(toss));
		assertInHand(toss);
	}
}
