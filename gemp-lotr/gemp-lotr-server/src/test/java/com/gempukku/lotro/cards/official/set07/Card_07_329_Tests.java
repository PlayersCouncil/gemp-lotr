package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_329_Tests
{

	// The King path with Dunharrow Plateau as its site 1, where the first fellowship phase is played.  Every card
	// named below is in each player's draw deck until the test moves it.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion; a skirmish ability used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally; a fellowship ability used only on purpose
					// Lord of Moria: [dwarven] condition, support area, cost 2; "Each Dwarf is damage +1"
					put("lord", "1_21");
					// Endurance of Dwarves: [dwarven] condition borne by a Dwarf, cost 1; a skirmish ability
					put("endurance", "2_4");
					// Dwarven Axe: a [dwarven] possession, cost 0, borne by a Dwarf: no condition
					put("axe", "1_9");
					// Gandalf's Wisdom: a [gandalf] condition, support area, cost 0, with no requirement to play
					put("wisdom", "2_23");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.KingSites);
					put("site1", "7_329");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DunharrowPlateauStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Dunharrow Plateau
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 1
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Fellowship:</b> Spot a Dwarf to play a [dwarven] condition from your draw deck (limit once per turn).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Dunharrow Plateau", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void FellowshipActionPlaysADwarvenConditionFromTheDrawDeckAndShufflesItOnceATurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var plateau = scn.GetFreepsSite(1);
		var gimli = scn.GetFreepsCard("gimli");
		var lord = scn.GetFreepsCard("lord");
		var endurance = scn.GetFreepsCard("endurance");
		var axe = scn.GetFreepsCard("axe");
		var wisdom = scn.GetFreepsCard("wisdom");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();

		assertSame(plateau, scn.GetCurrentSite());
		assertInZone(Zone.DECK, lord, endurance, axe, wisdom);
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsActionAvailable(plateau));

		scn.FreepsUseCardAction(plateau);
		// The whole deck is shown first.
		assertTrue(scn.FreepsHasCardChoiceAvailable(lord, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, false));
		scn.FreepsDismissRevealedCards();

		// Two [dwarven] conditions, one for the support area and one for a Dwarf to bear; a [dwarven] possession
		// and a condition of another culture, each of which he could play, are not offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(lord));
		assertTrue(scn.FreepsHasCardChoiceAvailable(endurance));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axe));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(wisdom));
		scn.FreepsChooseCardBPFromSelection(lord);

		assertInZone(Zone.SUPPORT, lord);
		assertInZone(Zone.DECK, endurance, axe, wisdom);
		assertTrue(scn.GetFreepsDeckShuffleCount() > freepsShuffles);
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());

		// Limit once per turn: Gimli is still there and so is a [dwarven] condition in the deck.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(plateau));
	}

	@Test
	public void ADwarfAllyIsADwarfToSpotAndCanBearTheConditionPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var plateau = scn.GetFreepsSite(1);
		var grimir = scn.GetFreepsCard("grimir");
		var endurance = scn.GetFreepsCard("endurance");
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();

		assertSame(plateau, scn.GetCurrentSite());
		assertTrue(scn.FreepsActionAvailable(plateau));

		scn.FreepsUseCardAction(plateau);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(endurance);

		// Grimir is the one Dwarf to bear it.
		assertAttachedTo(endurance, grimir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilADwarfIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var plateau = scn.GetFreepsSite(1);
		var gimli = scn.GetFreepsCard("gimli");
		var lord = scn.GetFreepsCard("lord");
		// A [dwarven] card in play that is no Dwarf
		scn.MoveCardsToSupportArea(lord);

		scn.StartGame();

		assertSame(plateau, scn.GetCurrentSite());
		assertInZone(Zone.SUPPORT, lord);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(plateau));

		scn.MoveCompanionsToTable(gimli);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsActionAvailable(plateau));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoDwarvenConditionInTheDeckItIsStillOfferedAndUsedOnlyLooks() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var plateau = scn.GetFreepsSite(1);
		var gimli = scn.GetFreepsCard("gimli");
		var lord = scn.GetFreepsCard("lord");
		var endurance = scn.GetFreepsCard("endurance");
		var axe = scn.GetFreepsCard("axe");
		var wisdom = scn.GetFreepsCard("wisdom");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToDiscard(lord, endurance);

		scn.StartGame();

		assertInZone(Zone.DECK, axe, wisdom);
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int pool = scn.GetTwilight();
		// No penalty for failing to find a card in a draw deck (Q-0019): the look may always be made.
		assertTrue(scn.FreepsActionAvailable(plateau));

		scn.FreepsUseCardAction(plateau);
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, false));
		// Nothing there can be chosen, so the showing is the only question.
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DECK, axe, wisdom);
		assertInZone(Zone.DISCARD, lord, endurance);
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.GetFreepsDeckShuffleCount() > freepsShuffles);
		// The look was the use of the turn.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(plateau));
	}
}
