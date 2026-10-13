package com.gempukku.lotro.cards.unofficial.pc.errata.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_18_082_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<String, String>()
				{{
					put("grond", "68_82");
					put("goblin", "11_137");     // Scurrying Goblin: [orc] Orc, cost 1, site 4; its offer needs an underground site
					put("halftroll", "14_12");   // Half-troll of Far Harad: [orc] minion who is no Orc, cost 3, site 4
					put("beckoning", "12_83");   // The Beckoning Shadow: [orc] condition, support area; nothing below 5 burdens
					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc with no text

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] companion
					put("gimli", "1_13");        // Gimli, Son of Gloin: [dwarven] companion
					put("legolas", "1_50");      // Legolas, Greenleaf: [elven] companion
					put("armor", "1_8");         // Dwarven Armor: [dwarven] possession
					put("sleep", "1_84");        // Sleep, Caradhras: [gandalf] Fellowship event
					// Kept in the draw deck, so that the Free Peoples cards there are of three types.
					put("sword", "1_299");       // Hobbit Sword: [shire] possession, hand weapon
					put("nobody", "1_23");       // Nobody Tosses a Dwarf: [dwarven] response event
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrondStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Grond, Forged With Black Steel
		 * Unique: true
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: To play, spot an [orc] minion.
		 * 	Each time the fellowship moves during the regroup phase, you may discard this possession to make the
		 * 	Free Peoples player shuffle 2 cards from their hand into their draw deck.  Search that deck and discard
		 * 	2 Free Peoples cards from it.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grond");

		assertEquals("Grond", card.getBlueprint().getTitle());
		assertEquals("Forged With Black Steel", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAnOrcCultureMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grond = scn.GetShadowCard("grond");
		var goblin = scn.GetShadowCard("goblin");
		var beckoning = scn.GetShadowCard("beckoning");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(grond, goblin);
		scn.MoveCardsToSupportArea(beckoning);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// An [orc] condition and an Orc of the [moria] culture: no [orc] minion.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(grond));

		scn.ShadowPlayCard(goblin);
		assertTrue(scn.ShadowPlayAvailable(grond));

		scn.ShadowPlayCard(grond);
		assertInZone(Zone.SUPPORT, grond);
		assertNull(grond.getAttachedTo());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcCultureMinionWhoIsNoOrcIsEnoughToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grond = scn.GetShadowCard("grond");
		var halftroll = scn.GetShadowCard("halftroll");
		var beckoning = scn.GetShadowCard("beckoning");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(grond, halftroll);
		scn.MoveCardsToSupportArea(beckoning);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(grond));

		// A Half-troll of the [orc] culture.
		scn.ShadowPlayCard(halftroll);
		assertTrue(scn.ShadowPlayAvailable(grond));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARegroupMoveLetsGrondBeDiscardedToShuffleTwoCardsFromHandInAndDiscardTwoFreePeoplesCardsFromTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grond = scn.GetShadowCard("grond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var armor = scn.GetFreepsCard("armor");
		var sleep = scn.GetFreepsCard("sleep");
		// The Free Peoples player's own Shadow cards: one in his hand, the rest in his draw deck.
		var hisTroop = scn.GetFreepsCard("troop");
		var hisGoblin = scn.GetFreepsCard("goblin");
		var hisGrond = scn.GetFreepsCard("grond");
		// Left in his draw deck: a Free Peoples possession and event, to be offered beside the companions.
		var sword = scn.GetFreepsCard("sword");
		var nobody = scn.GetFreepsCard("nobody");
		scn.MoveCardsToHand(aragorn, armor, sleep, hisTroop);
		scn.MoveCardsToSupportArea(grond);

		scn.StartGame();

		// The move of the fellowship phase does not offer it.
		scn.FreepsPass();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grond));

		assertInZone(Zone.SUPPORT, grond);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		int deck = scn.GetFreepsDeckCount();
		int shadowHand = scn.GetShadowHandCount();
		int shadowDeck = scn.GetShadowDeckCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.ShadowAcceptOptionalTrigger();
		assertInDiscard(grond);

		// The Free Peoples player chooses which two cards of his hand, of either side.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, armor, sleep, hisTroop));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCards(aragorn, hisTroop);
		assertInZone(Zone.DECK, aragorn, hisTroop);
		assertInHand(armor, sleep);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck + 2, scn.GetFreepsDeckCount());
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		// The Shadow player, and nobody else, is shown that deck, the two cards just shuffled in among the rest.
		assertEquals(deck + 2, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(hisTroop, false));
		assertFalse(scn.FreepsAnyDecisionsAvailable());

		// He then chooses two Free Peoples cards from it, of any type: Aragorn, just shuffled in, is one he may
		// take.  The Shadow cards in that deck are not offered, nor is anything in the Free Peoples player's hand.
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, legolas, sword, nobody));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hisTroop, hisGoblin, hisGrond, armor, sleep));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCardBPFromSelection(aragorn, gimli);
		assertInDiscard(aragorn, gimli);
		assertInZone(Zone.DECK, legolas, sword, nobody, hisTroop, hisGoblin, hisGrond);
		assertInHand(armor, sleep);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheDeckThatWasSearchedIsShuffledAgainAndTheShadowPlayersOwnDeckIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grond = scn.GetShadowCard("grond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var armor = scn.GetFreepsCard("armor");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveCardsToHand(aragorn, armor, sleep);
		scn.MoveCardsToSupportArea(grond);

		scn.StartGame();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grond));

		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsChooseCards(armor, sleep);
		// Shuffled as the two cards went in.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());

		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(gimli, legolas);
		assertInDiscard(gimli, legolas);
		// And once more when the Shadow player has finished looking through it; his own deck was never looked at.
		assertEquals(freepsShuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DeclinedItStaysInPlayAndIsOfferedAgainAtTheNextRegroupMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grond = scn.GetShadowCard("grond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveCardsToHand(aragorn, armor, sleep);
		scn.MoveCardsToSupportArea(grond);
		// A third move this turn, so that the fellowship moves twice in regroup phases.
		scn.ApplyAdHocModifier(new MoveLimitModifier(null, 1));

		scn.StartGame();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grond));

		int deck = scn.GetFreepsDeckCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, grond);
		assertInHand(aragorn, armor, sleep);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		// Nobody looked through a deck.
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grond));

		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, grond);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AHandOfOneCardIsShuffledInAndADeckWithOneFreePeoplesCardLosesThatOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grond = scn.GetShadowCard("grond");
		var gimli = scn.GetFreepsCard("gimli");
		var hisTroop = scn.GetFreepsCard("troop");
		var hisGoblin = scn.GetFreepsCard("goblin");
		// His one card in hand is a Shadow card; Gimli is the one Free Peoples card in his draw deck.
		scn.MoveCardsToHand(hisTroop);
		scn.MoveCardsToFreepsDiscard("aragorn", "legolas", "armor", "sleep", "sword", "nobody");
		scn.MoveCardsToSupportArea(grond);

		scn.StartGame();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grond));

		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(6, scn.GetFreepsDiscardCount());
		int deck = scn.GetFreepsDeckCount();

		scn.ShadowAcceptOptionalTrigger();
		assertInDiscard(grond);
		// As much as he can: the one card.
		assertInZone(Zone.DECK, hisTroop);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck + 1, scn.GetFreepsDeckCount());

		scn.ShadowDismissRevealedCards();
		assertInDiscard(gimli);
		assertInZone(Zone.DECK, hisTroop, hisGoblin);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(7, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
