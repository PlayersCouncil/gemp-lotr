package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_102_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("repairs", "7_102");
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; a skirmish ability only
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion of another title; Knight is its whole text
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man ally; a maneuver ability only
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit; a skirmish ability used only on purpose
					put("household", "5_83");   // Household Guard: a [rohan] Man companion; valiant is its whole text

					// Cards for the hand, of several cultures, types and both sides; none is played
					put("handknight", "5_35");  // Gondorian Knight: a [gondor] companion
					put("armor", "1_8");        // Dwarven Armor: a [dwarven] possession
					put("cover", "5_40");       // Take Cover: a [gondor] Fellowship event
					put("runner", "1_178");     // Goblin Runner: a [moria] minion
					put("extra", "1_9");        // Dwarven Axe: a [dwarven] possession, for a fifth card
					put("third", "7_125");      // Third Level: a [gondor] fortification, in the hand

					// For the discard pile: [gondor] fortifications, conditions of cost 1 with abilities used only on purpose
					put("citywall", "5_33");    // City Wall
					put("first", "7_92");       // First Level
					put("stonetower", "5_39");  // Stone Tower, laid in the other player's discard pile
					put("arrowslits", "5_80");  // Arrow-slits: a [rohan] fortification condition
					put("rallypoint", "5_38");  // Rally Point: a [gondor] condition that is no fortification
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDiscardPileScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("repairs", "7_102");
					// Three [gondor] fortifications of three blueprints, each played to the support area; their
					// abilities are never used.
					put("citadel", "5_32");    // Citadel of the Stars: for the hand
					put("wall", "5_33");       // City Wall, cost 1: for the discard pile
					put("first", "7_92");      // First Level: for the discard pile
					// Three more cards, to make the hand 4
					put("guard", "1_7");       // Dwarf Guard: [dwarven] companion
					put("armor", "1_8");       // Dwarven Armor: [dwarven] possession
					put("axe", "1_9");         // Dwarven Axe: [dwarven] possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HastyRepairsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Hasty Repairs
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot 3 [gondor] Men.<br><b>Regroup:</b> While you have only 4 cards in hand, you may discard 2 cards from hand to play a [gondor] fortification from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("repairs");

		assertEquals("Hasty Repairs", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilThreeGondorMenAreSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var repairs = scn.GetFreepsCard("repairs");
		var boromir = scn.GetFreepsCard("boromir");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var household = scn.GetFreepsCard("household");
		scn.MoveCompanionsToTable(boromir, pippin, household);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(repairs, knight);
		assertInHand(repairs, knight);

		scn.StartGame();

		// Two [gondor] Men, a companion and an ally, beside a [gondor] Hobbit and a [rohan] Man.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(repairs));

		scn.FreepsPlayCard(knight);
		assertTrue(scn.FreepsPlayAvailable(repairs));

		scn.FreepsPlayCard(repairs);
		assertInZone(Zone.SUPPORT, repairs);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupAbilityDiscardsAnyTwoCardsFromHandToPlayAGondorFortificationFromItsOwnersDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var repairs = scn.GetFreepsCard("repairs");
		var handknight = scn.GetFreepsCard("handknight");
		var armor = scn.GetFreepsCard("armor");
		var cover = scn.GetFreepsCard("cover");
		var runner = scn.GetFreepsCard("runner");
		var citywall = scn.GetFreepsCard("citywall");
		var first = scn.GetFreepsCard("first");
		var arrowslits = scn.GetFreepsCard("arrowslits");
		var rallypoint = scn.GetFreepsCard("rallypoint");
		scn.MoveCardsToSupportArea(repairs);
		scn.MoveCardsToHand(handknight, armor, cover, runner);
		scn.MoveCardsToDiscard(citywall, first, arrowslits, rallypoint);
		// The other player's [gondor] fortification, of a title the Free Peoples player's piles do not hold.
		var stonetower = scn.GetShadowCard("stonetower");
		scn.MoveCardsToDiscard(stonetower);
		assertInZone(Zone.SUPPORT, repairs);
		assertInHand(handknight, armor, cover, runner);
		assertInZone(Zone.DISCARD, citywall, first, arrowslits, rallypoint, stonetower);

		scn.StartGame();
		// No minion: the game goes straight from the Shadow phase to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(repairs));
		scn.FreepsUseCardAction(repairs);

		// Any two cards: a companion, a possession, an event, a Shadow card.
		assertTrue(scn.FreepsHasCardChoiceAvailable(handknight, armor, cover, runner));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(cover, runner);
		assertInDiscard(cover, runner);
		assertInHand(handknight, armor);

		// Two [gondor] fortifications of his own; not a [rohan] fortification, a [gondor] condition that is no
		// fortification, nor the other player's [gondor] fortification in his own pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(citywall, first));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arrowslits, rallypoint, stonetower));
		scn.FreepsChooseCard(first);

		assertInZone(Zone.SUPPORT, first);
		assertInZone(Zone.DISCARD, citywall, arrowslits, rallypoint, stonetower);
		assertInZone(Zone.SUPPORT, repairs);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheFortificationPlayedIsNotOneOfTheCardsDiscardedToPayForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var repairs = scn.GetFreepsCard("repairs");
		var citadel = scn.GetFreepsCard("citadel");
		var wall = scn.GetFreepsCard("wall");
		var first = scn.GetFreepsCard("first");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var axe = scn.GetFreepsCard("axe");

		scn.MoveCardsToSupportArea(repairs);
		scn.MoveCardsToHand(citadel, guard, armor, axe);
		scn.MoveCardsToDiscard(wall, first);

		scn.StartGame();
		assertInHand(citadel, guard, armor, axe);
		assertInDiscard(wall, first);
		scn.SkipToPhase(Phase.REGROUP);

		assertInHand(citadel, guard, armor, axe);
		assertInDiscard(wall, first);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(repairs));
		scn.FreepsUseCardAction(repairs);
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(citadel, guard);
		assertInDiscard(citadel, guard);
		assertInHand(armor, axe);

		// The two fortifications that lay in the discard pile before the cost was paid, and not the one that lies
		// there now because it paid for it (CR 4.2 "effect", the They Are Coming example; Q-0320).
		assertTrue(scn.FreepsHasCardChoiceAvailable(wall, first));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(citadel));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCardBPFromSelection(wall);

		assertInZone(Zone.SUPPORT, wall);
		assertInDiscard(citadel, first, guard);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithThreeCardsInHandTheRegroupAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var repairs = scn.GetFreepsCard("repairs");
		var handknight = scn.GetFreepsCard("handknight");
		var armor = scn.GetFreepsCard("armor");
		var cover = scn.GetFreepsCard("cover");
		var citywall = scn.GetFreepsCard("citywall");
		scn.MoveCardsToSupportArea(repairs);
		scn.MoveCardsToHand(handknight, armor, cover);
		scn.MoveCardsToDiscard(citywall);
		assertInZone(Zone.SUPPORT, repairs);
		assertInHand(handknight, armor, cover);
		assertInDiscard(citywall);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(repairs));
	}

	@Test
	public void WithFiveCardsInHandTheRegroupAbilityIsNotOfferedThoughTheShadowPlayerHoldsFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var repairs = scn.GetFreepsCard("repairs");
		var citywall = scn.GetFreepsCard("citywall");
		scn.MoveCardsToSupportArea(repairs);
		scn.MoveCardsToDiscard(citywall);
		assertInZone(Zone.SUPPORT, repairs);
		assertInDiscard(citywall);

		scn.StartGame();
		scn.SetFreepsHand(scn.GetFreepsCard("handknight"), scn.GetFreepsCard("armor"), scn.GetFreepsCard("cover"),
				scn.GetFreepsCard("runner"), scn.GetFreepsCard("extra"));
		scn.SetShadowHand(scn.GetShadowCard("handknight"), scn.GetShadowCard("armor"), scn.GetShadowCard("cover"),
				scn.GetShadowCard("runner"));
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetShadowHandCount());
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(repairs));
	}

	@Test
	public void WithNoGondorFortificationInTheDiscardPileTheRegroupAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var repairs = scn.GetFreepsCard("repairs");
		var third = scn.GetFreepsCard("third");
		var handknight = scn.GetFreepsCard("handknight");
		var armor = scn.GetFreepsCard("armor");
		var cover = scn.GetFreepsCard("cover");
		var arrowslits = scn.GetFreepsCard("arrowslits");
		var rallypoint = scn.GetFreepsCard("rallypoint");
		scn.MoveCardsToSupportArea(repairs);
		// A [gondor] fortification in the hand, which the cost could put in the discard pile.
		scn.MoveCardsToHand(third, handknight, armor, cover);
		scn.MoveCardsToDiscard(arrowslits, rallypoint);
		var stonetower = scn.GetShadowCard("stonetower");
		scn.MoveCardsToDiscard(stonetower);
		assertInZone(Zone.SUPPORT, repairs);
		assertInHand(third, handknight, armor, cover);
		assertInZone(Zone.DISCARD, arrowslits, rallypoint, stonetower);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// The play is part of the price (Q-0062): with only a [rohan] fortification and a [gondor] condition that is
		// no fortification in his pile, and the other player's fortification in his, nothing is asked of the hand.
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(repairs));
		assertInHand(third, handknight, armor, cover);
	}
}
