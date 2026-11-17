package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_082_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grond", "18_82");
					put("goblin", "11_137");     // Scurrying Goblin: [orc] Orc, cost 1; its play trigger wants an underground site
					put("halftroll", "14_12");   // Half-troll of Far Harad: an [orc] minion who is no Orc, cost 3; a start-of-assignment trigger
					put("beckoning", "12_83");   // The Beckoning Shadow: [orc] condition, support area; nothing below 5 burdens
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Free Peoples player's draw deck is his copy of every card named here: three Free Peoples cards of
	// three types, and his own Grond, a Shadow card.
	protected VirtualTableScenario GetRegroupMoveScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grond", "18_82");
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] companion
					put("armor", "1_8");         // Dwarven Armor: [dwarven] possession
					put("sleep", "1_84");        // Sleep, Caradhras: [gandalf] Fellowship event
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Rivendell Waterfall as site 3, with Aragorn, a ranger: the move limit is 3 there, so a turn has two regroup moves.
	protected VirtualTableScenario GetWaterfallScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grond", "18_82");
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a ranger; a maneuver ability only
					put("armor", "1_8");         // Dwarven Armor: [dwarven] possession, for the Free Peoples draw deck
					put("sleep", "1_84");        // Sleep, Caradhras: [gandalf] Fellowship event, for the Free Peoples draw deck
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_342");   // Rivendell Waterfall
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrondStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Grond, Forged With Black Steel
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: To play, spot an [orc] minion.<br>Each time the fellowship moves in the regroup phase, you may discard this possession to search the Free Peoples player's draw deck and choose 2 Free Peoples cards found there. Remove those cards from the game.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grond");

		assertEquals("Grond", card.getBlueprint().getTitle());
		assertEquals("Forged With Black Steel", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ARegroupMoveLetsGrondBeDiscardedToRemoveTwoFreePeoplesCardsInTheFreePeoplesDrawDeckFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupMoveScenario();

		var grond = scn.GetShadowCard("grond");
		scn.MoveCardsToSupportArea(grond);
		assertInZone(Zone.SUPPORT, grond);

		// All four are left in the Free Peoples player's draw deck.
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var sleep = scn.GetFreepsCard("sleep");
		var hisGrond = scn.GetFreepsCard("grond");

		scn.StartGame();

		// The move of the fellowship phase does not offer it.
		scn.FreepsPass();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grond));

		assertInZone(Zone.SUPPORT, grond);
		assertEquals(4, scn.GetFreepsDeckCount());
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int shadowDeck = scn.GetShadowDeckCount();

		scn.ShadowAcceptOptionalTrigger();
		assertInDiscard(grond);

		// The Shadow player, and nobody else, is shown that deck, the Shadow card in it with the rest.
		assertEquals(4, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(hisGrond, false));
		assertFalse(scn.FreepsAnyDecisionsAvailable());

		// He then chooses exactly two Free Peoples cards from it, of any type; the Shadow card is not offered.
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, armor, sleep));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hisGrond));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCardBPFromSelection(aragorn, sleep);
		assertInZone(Zone.REMOVED, aragorn, sleep);
		assertInZone(Zone.DECK, armor, hisGrond);
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		// The deck that was looked through is shuffled once more; the Shadow player's own was never looked at.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAnOrcMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grond = scn.GetShadowCard("grond");
		var goblin = scn.GetShadowCard("goblin");
		var beckoning = scn.GetShadowCard("beckoning");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(grond, goblin);
		scn.MoveCardsToSupportArea(beckoning);
		assertInHand(grond, goblin);
		assertInZone(Zone.SUPPORT, beckoning);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// An [orc] condition and an Orc of the [moria] culture: no [orc] minion.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(grond));

		scn.ShadowPlayCard(goblin);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);
		assertTrue(scn.ShadowPlayAvailable(grond));

		scn.ShadowPlayCard(grond);
		assertInZone(Zone.SUPPORT, grond);
		assertNull(grond.getAttachedTo());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcMinionWhoIsNoOrcIsEnoughToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grond = scn.GetShadowCard("grond");
		var halftroll = scn.GetShadowCard("halftroll");
		var beckoning = scn.GetShadowCard("beckoning");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(grond, halftroll);
		scn.MoveCardsToSupportArea(beckoning);
		assertInHand(grond, halftroll);
		assertInZone(Zone.SUPPORT, beckoning);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(grond));

		// A Half-troll of the [orc] culture.
		scn.ShadowPlayCard(halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, halftroll);
		assertTrue(scn.ShadowPlayAvailable(grond));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DeclinedAtOneRegroupMoveItStaysAndIsOfferedAgainAtTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWaterfallScenario();

		var grond = scn.GetShadowCard("grond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(grond);
		assertInZone(Zone.SUPPORT, grond);

		scn.StartGame();
		scn.SetFreepsDrawDeck(armor, sleep);
		assertEquals(2, scn.GetFreepsDeckCount());

		// The move of the fellowship phase does not offer it.
		scn.FreepsPass();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(grond));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		// The first regroup move, to Rivendell Waterfall.
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grond));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, grond);
		assertInZone(Zone.DECK, armor, sleep);
		assertEquals(2, scn.GetFreepsDeckCount());
		// Nobody looked through a deck.
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		assertEquals(3, scn.GetMoveLimit());

		// The second regroup move of the same turn.
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grond));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, grond);
		assertEquals(3, scn.GetMoveCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
