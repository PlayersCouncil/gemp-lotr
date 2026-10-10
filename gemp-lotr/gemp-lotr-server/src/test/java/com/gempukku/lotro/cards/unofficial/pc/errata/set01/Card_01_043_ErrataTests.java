package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_043_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eyes", "51_43");

					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, cost 2
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] Elf ally, cost 2
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: [dwarven] Dwarf ally, cost 1
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, cost 4
					put("axe", "1_3");          // Axe Strike: a Free Peoples event
					put("runner", "1_178");     // Goblin Runner: a Shadow minion
					put("troop", "1_177");      // Goblin Patrol Troop: a Shadow minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Five Elves to play in one fellowship phase, and a draw deck that outlasts the four draws the Rule of 4 allows.
	protected VirtualTableScenario GetFiveElvesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eyes", "51_43");

					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, cost 2
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] Elf ally, cost 2
					put("elf1", "1_53");        // Lorien Elf: [elven] Elf companion, cost 1; to play, spot an Elf
					put("elf2", "1_53");
					put("elf3", "1_53");
					put("card1", "1_3");        // Axe Strike: six cards for the Free Peoples player to draw,
					put("card2", "1_3");        // and two of the Shadow player's copies for him to hold
					put("card3", "1_3");
					put("card4", "1_3");
					put("card5", "1_3");
					put("card6", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FarseeingEyesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Far-seeing Eyes
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time you play an Elf, draw a card.  Any Shadow player may discard a card from hand
		 * to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eyes");

		assertEquals("Far-seeing Eyes", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachElfPlayedDrawsACardWhileTheShadowPlayerHoldsNoCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eyes = scn.GetFreepsCard("eyes");
		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCardsToHand(eyes, arwen, orophin);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetTwilight());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowHandCount());

		// The condition itself, an [elven] card in play by the time its play is announced, is no Elf: nothing
		// is drawn for it.
		scn.FreepsPlayCard(eyes);
		assertInZone(Zone.SUPPORT, eyes);
		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());

		// An Elf companion: one card out of the hand, one drawn.
		scn.FreepsPlayCard(arwen);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// An Elf ally
		scn.FreepsPlayCard(orophin);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayDiscardACardOfHisChoiceFromHandToPreventTheDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eyes = scn.GetFreepsCard("eyes");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");
		var axe = scn.GetFreepsCard("axe");
		// The Shadow player's hand: two Shadow cards and a Free Peoples card.
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var shadowThrarin = scn.GetShadowCard("thrarin");
		scn.MoveCardsToSupportArea(eyes);
		scn.MoveCardsToHand(aragorn, arwen, orophin, axe);
		scn.MoveCardsToHand(runner, troop, shadowThrarin);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		// A companion who is no Elf: nothing to draw and nothing to prevent.
		scn.FreepsPlayCard(aragorn);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(arwen);
		assertTrue(scn.ShadowDecisionAvailable("Discard a card from hand to prevent"));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());

		scn.ShadowChooseYes();
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, troop, shadowThrarin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axe));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(shadowThrarin);
		assertInZone(Zone.DISCARD, shadowThrarin);
		assertInZone(Zone.HAND, runner);
		assertInZone(Zone.HAND, troop);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		// Prevented: Arwen left the hand and nothing came into it.
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInZone(Zone.HAND, axe);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The next Elf: he is asked again, and lets it be.
		scn.FreepsPlayCard(orophin);
		assertTrue(scn.ShadowDecisionAvailable("Discard a card from hand to prevent"));

		scn.ShadowChooseNo();
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		// Orophin out, one card drawn
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HisLastCardMayBeDiscardedToPreventAndWithAnEmptyHandHeIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eyes = scn.GetFreepsCard("eyes");
		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(eyes);
		scn.MoveCardsToHand(arwen, orophin);
		scn.MoveCardsToHand(runner);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetShadowHandCount());

		scn.FreepsPlayCard(arwen);
		assertTrue(scn.ShadowDecisionAvailable("Discard a card from hand to prevent"));

		// His only card: it is discarded without his being asked which.
		scn.ShadowChooseYes();
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// With no card left he cannot pay: he is not asked, and the card is drawn.
		scn.FreepsPlayCard(orophin);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoCardToDrawTheShadowPlayerIsNotAskedToPreventIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eyes = scn.GetFreepsCard("eyes");
		var arwen = scn.GetFreepsCard("arwen");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(eyes);
		scn.MoveCardsToHand(arwen);
		// Everything else of the Free Peoples player's deck is put in his discard pile.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("orophin"), scn.GetFreepsCard("thrarin"), scn.GetFreepsCard("aragorn"),
				scn.GetFreepsCard("axe"), scn.GetFreepsCard("runner"), scn.GetFreepsCard("troop"));
		scn.MoveCardsToHand(runner, troop);

		scn.StartGame();

		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetShadowHandCount());

		scn.FreepsPlayCard(arwen);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheFifthElfOfAFellowshipPhaseTheRuleOfFourStopsTheDrawAndHeIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFiveElvesScenario();

		var eyes = scn.GetFreepsCard("eyes");
		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");
		var elf1 = scn.GetFreepsCard("elf1");
		var elf2 = scn.GetFreepsCard("elf2");
		var elf3 = scn.GetFreepsCard("elf3");
		scn.MoveCardsToSupportArea(eyes);
		scn.MoveCardsToHand(arwen, orophin, elf1, elf2, elf3);
		scn.MoveCardsToHand(scn.GetShadowCard("card1"), scn.GetShadowCard("card2"));

		scn.StartGame();

		// The six Axe Strikes
		assertEquals(6, scn.GetFreepsDeckCount());
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetShadowHandCount());

		// The first four Elves of the phase: he is asked each time, lets it be, and a card is drawn.
		scn.FreepsPlayCard(arwen);
		assertTrue(scn.ShadowDecisionAvailable("Discard a card from hand to prevent"));
		scn.ShadowChooseNo();
		// One Elf out of the hand and one card into it
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetFreepsDeckCount());

		scn.FreepsPlayCard(orophin);
		assertTrue(scn.ShadowDecisionAvailable("Discard a card from hand to prevent"));
		scn.ShadowChooseNo();
		assertEquals(4, scn.GetFreepsDeckCount());

		scn.FreepsPlayCard(elf1);
		assertTrue(scn.ShadowDecisionAvailable("Discard a card from hand to prevent"));
		scn.ShadowChooseNo();
		assertEquals(3, scn.GetFreepsDeckCount());

		scn.FreepsPlayCard(elf2);
		assertTrue(scn.ShadowDecisionAvailable("Discard a card from hand to prevent"));
		scn.ShadowChooseNo();
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The fifth: four cards have been drawn this fellowship phase, so no card will be drawn for it, though
		// two are left in the deck. He holds two cards and is asked nothing.
		scn.FreepsPlayCard(elf3);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertInZone(Zone.FREE_CHARACTERS, elf3);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
