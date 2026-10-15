package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_054_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mallorn", "1_54");

					put("song", "1_39");        // Elf-song: an [elven] event, held in hand
					put("lorienelf", "1_53");   // Lorien Elf: an [elven] companion, held in hand
					put("cloak", "1_42");       // Elven Cloak: an [elven] possession, held in hand
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion, held in hand
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an [elven] ally in play; a fellowship ability used only on purpose
					put("bow", "1_41");         // Elven Bow: an [elven] possession, in the Shadow player's hand

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRevealScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mallorn", "1_54");
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: an [elven] companion, held in hand
					put("elf", "1_53");         // Lórien Elf: another [elven] companion, held in hand
					put("guard", "1_7");        // Dwarf Guard: a [dwarven] companion, the card in hand that is not [elven]
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MallorntreesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Mallorn-trees
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br><b>Fellowship:</b> Reveal an [elven] card from hand and place it beneath your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mallorn");

		assertEquals("Mallorn-trees", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// F-0420 (Ketura on Q-0034: an optional action with no cost that would do nothing is not offered; on this card,
	// 2026-10-11: "Yeah that looks like a reasonable fix").  "Fellowship: Reveal an [elven] card from hand and
	// place it beneath your draw deck": with no [elven] card in hand there is nothing to reveal or to place.
	@Test
	public void TheAbilityIsNotOfferedWithNoElvenCardInHandAndWithOnePlacesTheCardRevealedBeneathTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRevealScenario();

		var mallorn = scn.GetFreepsCard("mallorn");
		var haldir = scn.GetFreepsCard("haldir");
		var elf = scn.GetFreepsCard("elf");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToSupportArea(mallorn);
		scn.MoveCardsToHand(guard);

		scn.StartGame();

		// A card in hand, and it is [dwarven].
		assertInHand(guard);
		assertEquals(1, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(mallorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Two [elven] cards beside it.
		scn.MoveCardsToHand(haldir, elf);
		assertInHand(haldir, elf);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(mallorn));
		scn.FreepsUseCardAction(mallorn);

		assertTrue(scn.FreepsHasCardChoiceAvailable(haldir, elf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(haldir);

		// Revealed: the Shadow player is shown it.  Then it lies beneath the draw deck.
		assertTrue(scn.ShadowHasCardChoiceAvailable(haldir, false));
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		assertEquals(haldir, scn.GetFreepsBottomOfDeck());
		assertInHand(guard, elf);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElvenCardOfEachTypeInHandIsOfferedAndTheOneChosenIsShownToTheShadowPlayerAndPlacedBeneathTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mallorn = scn.GetFreepsCard("mallorn");
		var song = scn.GetFreepsCard("song");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var cloak = scn.GetFreepsCard("cloak");
		var aragorn = scn.GetFreepsCard("aragorn");
		var celeborn = scn.GetFreepsCard("celeborn");
		var bow = scn.GetShadowCard("bow");
		scn.MoveCardsToSupportArea(mallorn);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(song, lorienelf, cloak, aragorn, bow);

		scn.StartGame();

		assertInHand(song, lorienelf, cloak, aragorn, bow);
		assertEquals(4, scn.GetFreepsHandCount());
		assertNotEquals(cloak, scn.GetFreepsBottomOfDeck());
		assertTrue(scn.FreepsActionAvailable(mallorn));
		scn.FreepsUseCardAction(mallorn);

		// An [elven] event, companion and possession in hand are offered.  Not offered: a [gondor] card in hand,
		// [elven] cards in play (Celeborn, and Mallorn-trees itself), and an [elven] card in the other player's hand.
		assertTrue(scn.FreepsHasCardChoiceAvailable(song, lorienelf, cloak));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, celeborn, mallorn, bow));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(cloak);

		// Revealed from hand: the Shadow player is shown it, and its owner is shown nothing.
		assertTrue(scn.ShadowHasCardChoiceAvailable(cloak, false));
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		assertEquals(cloak, scn.GetFreepsBottomOfDeck());
		assertInHand(song, lorienelf, aragorn, bow);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElvenEventAloneInHandIsEnoughToOfferTheAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mallorn = scn.GetFreepsCard("mallorn");
		var song = scn.GetFreepsCard("song");
		scn.MoveCardsToSupportArea(mallorn);
		scn.MoveCardsToHand(song);

		scn.StartGame();

		// The one [elven] card in hand is an event: neither a companion nor a possession, which the other tests rest on.
		assertInHand(song);
		assertEquals(1, scn.GetFreepsHandCount());
		assertNotEquals(song, scn.GetFreepsBottomOfDeck());
		assertTrue(scn.FreepsActionAvailable(mallorn));

		// The only card to choose is chosen unasked.
		scn.FreepsUseCardAction(mallorn);
		assertTrue(scn.ShadowHasCardChoiceAvailable(song, false));
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		assertEquals(song, scn.GetFreepsBottomOfDeck());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mallorn = scn.GetFreepsCard("mallorn");
		var cloak = scn.GetFreepsCard("cloak");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(mallorn);
		scn.MoveCardsToHand(cloak);

		scn.StartGame();
		assertTrue(scn.FreepsActionAvailable(mallorn));

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInHand(cloak);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(mallorn));
	}
}
