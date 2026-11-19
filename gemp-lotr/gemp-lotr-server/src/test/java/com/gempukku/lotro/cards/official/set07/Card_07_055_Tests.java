package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_055_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("days", "7_55");
					// For the discard pile: the two Sméagols of two cultures, each "To play, add a burden" and a
					// phase ability used only on purpose.
					put("smeagol", "6_45");       // Sméagol, Poor Creature: [gollum] companion
					put("shiresmeagol", "103_116"); // Smeagol, Once Like Us: [shire] companion
					put("slinker", "5_29");       // Sméagol, Slinker: laid in the Shadow player's own discard pile
					put("dontlook", "6_39");      // Don't Look at Them: [gollum] Free Peoples condition, no requirement to play
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a companion of another title
					// For the hand, one card of each kind: a companion, a possession, an event and a Shadow card.
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk
					put("armor", "1_8");          // Dwarven Armor
					put("deftness", "1_293");     // Halfling Deftness: [shire] Skirmish event
					put("runner", "1_178");       // Goblin Runner: [moria] minion; also laid in play for the maneuver phase
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
					put("days", "7_55");
					// Three Sméagols of three blueprints, so that a choice from the pile tells them apart.  Each
					// plays for a burden; nothing else of their text is reached.
					put("helps", "7_71");      // Sméagol, Always Helps: for the hand
					put("noser", "5_28");      // Sméagol, Old Noser: for the discard pile
					put("slinker", "5_29");    // Sméagol, Slinker: for the discard pile
					// Two more cards for the hand, which is discarded whole
					put("guard", "1_7");       // Dwarf Guard: [dwarven] companion
					put("armor", "1_8");       // Dwarven Armor: [dwarven] possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DaysGrowingDarkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Days Growing Dark
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Fellowship:</b> Discard 3 cards from hand to play Sméagol from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("days");

		assertEquals("Days Growing Dark", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipAbilityDiscards3CardsFromHandToPlaySmeagolFromYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var days = scn.GetFreepsCard("days");
		var smeagol = scn.GetFreepsCard("smeagol");
		var shiresmeagol = scn.GetFreepsCard("shiresmeagol");
		var dontlook = scn.GetFreepsCard("dontlook");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var armor = scn.GetFreepsCard("armor");
		var deftness = scn.GetFreepsCard("deftness");
		var runner = scn.GetFreepsCard("runner");
		var slinker = scn.GetShadowCard("slinker");

		scn.MoveCardsToSupportArea(days);
		scn.MoveCardsToHand(gandalf, armor, deftness, runner);
		scn.MoveCardsToDiscard(smeagol, shiresmeagol, dontlook, aragorn, slinker);

		scn.StartGame();

		assertInHand(gandalf, armor, deftness, runner);
		assertInDiscard(smeagol, shiresmeagol, dontlook, aragorn, slinker);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsActionAvailable(days));
		scn.FreepsUseCardAction(days);

		// Any 3 cards of the 4: a companion, a possession, an event and a Shadow card.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, armor, deftness, runner));
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(gandalf, armor, runner);

		assertInDiscard(gandalf, armor, runner);
		assertInHand(deftness);
		assertEquals(1, scn.GetFreepsHandCount());

		// Either Sméagol in your discard pile, of either culture; not a [gollum] card or a companion of another title,
		// nor the Sméagol in the Shadow player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(smeagol, shiresmeagol));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dontlook, aragorn, slinker));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCardBPFromSelection(smeagol);

		// Played, and so his own "To play, add a burden" is paid.
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(1, scn.GetBurdens());
		assertInDiscard(shiresmeagol, dontlook, aragorn, slinker);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedWithOnly2CardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var days = scn.GetFreepsCard("days");
		var smeagol = scn.GetFreepsCard("smeagol");
		var gandalf = scn.GetFreepsCard("gandalf");
		var armor = scn.GetFreepsCard("armor");

		scn.MoveCardsToSupportArea(days);
		scn.MoveCardsToHand(gandalf, armor);
		scn.MoveCardsToDiscard(smeagol);

		scn.StartGame();

		assertEquals(2, scn.GetFreepsHandCount());
		assertInDiscard(smeagol);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(days));
	}

	@Test
	public void FellowshipAbilityIsNotOfferedWithNoSmeagolInYourDiscardPileButTheOneInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var days = scn.GetFreepsCard("days");
		var smeagol = scn.GetFreepsCard("smeagol");
		var dontlook = scn.GetFreepsCard("dontlook");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var armor = scn.GetFreepsCard("armor");
		var slinker = scn.GetShadowCard("slinker");

		scn.MoveCardsToSupportArea(days);
		// Three cards to pay with, Sméagol among them: the one discarded for the cost is not the one played (Q-0320).
		scn.MoveCardsToHand(smeagol, gandalf, armor);
		scn.MoveCardsToDiscard(dontlook, aragorn, slinker);

		scn.StartGame();

		assertInHand(smeagol, gandalf, armor);
		assertEquals(3, scn.GetFreepsHandCount());
		assertInDiscard(dontlook, aragorn, slinker);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(days));
	}

	@Test
	public void FellowshipAbilityIsNotOfferedWhileASmeagolIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var days = scn.GetFreepsCard("days");
		var smeagol = scn.GetFreepsCard("smeagol");
		var slinker = scn.GetFreepsCard("slinker");
		var gandalf = scn.GetFreepsCard("gandalf");
		var armor = scn.GetFreepsCard("armor");
		var deftness = scn.GetFreepsCard("deftness");

		scn.MoveCardsToSupportArea(days);
		// Sméagol is unique: with one in play the one in the discard pile cannot be played.
		scn.MoveCompanionsToTable(slinker);
		scn.MoveCardsToHand(gandalf, armor, deftness);
		scn.MoveCardsToDiscard(smeagol);

		scn.StartGame();

		assertInZone(Zone.FREE_CHARACTERS, slinker);
		assertEquals(3, scn.GetFreepsHandCount());
		assertInDiscard(smeagol);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(days));
	}

	@Test
	public void FellowshipAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var days = scn.GetFreepsCard("days");
		var smeagol = scn.GetFreepsCard("smeagol");
		var gandalf = scn.GetFreepsCard("gandalf");
		var armor = scn.GetFreepsCard("armor");
		var deftness = scn.GetFreepsCard("deftness");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToSupportArea(days);
		scn.MoveCardsToHand(gandalf, armor, deftness);
		scn.MoveCardsToDiscard(smeagol);

		scn.StartGame();

		assertEquals(3, scn.GetFreepsHandCount());
		assertInDiscard(smeagol);
		assertTrue(scn.FreepsActionAvailable(days));

		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(3, scn.GetFreepsHandCount());
		assertInDiscard(smeagol);
		assertFalse(scn.FreepsActionAvailable(days));
	}

	@Test
	public void TheSmeagolPlayedIsNotOneOfTheCardsDiscardedToPayForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var days = scn.GetFreepsCard("days");
		var helps = scn.GetFreepsCard("helps");
		var noser = scn.GetFreepsCard("noser");
		var slinker = scn.GetFreepsCard("slinker");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");

		scn.MoveCardsToSupportArea(days);
		scn.MoveCardsToHand(helps, guard, armor);
		scn.MoveCardsToDiscard(noser, slinker);

		scn.StartGame();

		assertInHand(helps, guard, armor);
		assertInDiscard(noser, slinker);
		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(days));
		scn.FreepsUseCardAction(days);
		// Three cards in hand pay the discard of 3 without a choice
		assertEquals(0, scn.GetFreepsHandCount());
		assertInDiscard(helps, guard, armor);

		// The two Sméagols that lay in the discard pile before the cost was paid, and not the one that lies there
		// now because it paid it (CR 4.2 "effect", the They Are Coming example; Q-0320).
		assertTrue(scn.FreepsHasCardChoiceAvailable(noser, slinker));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(helps));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCardBPFromSelection(noser);

		assertInZone(Zone.FREE_CHARACTERS, noser);
		assertEquals(1, scn.GetBurdens());
		assertInDiscard(helps, slinker, guard, armor);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
