package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_032_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gathering", "15_32");
					put("guardian", "5_17");    // Forest Guardian: [gandalf] Ent companion; his ability is an assignment one
					put("treebeard", "4_104");  // Treebeard, Oldest Living Thing: [gandalf] Ent ally; a fellowship ability with a cost
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] companion who is no Ent
					// In the draw deck:
					put("dasron", "13_29");     // Dasron, Merchant From Dorwinion: [gandalf] Man companion
					put("avenger", "6_27");     // Ent Avenger: [gandalf] Ent companion
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] ally, so no companion, and no condition
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] companion
					put("doom", "12_36");       // With Doom We Come: [gandalf] support condition
					put("watch", "12_35");      // Watch and Wait: [gandalf] condition borne by a companion
					put("lordofmoria", "1_21"); // Lord of Moria: [dwarven] support condition
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MomentousGatheringStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Momentous Gathering
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Choose one: spot an Ent to take a [gandalf] companion into hand from your draw deck; or spot 2 Ents to take a [gandalf] companion and a [gandalf] condition into hand from your draw deck. Shuffle your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gathering");

		assertEquals("Momentous Gathering", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithoutAnEntToSpotAndCanOnceAnEntAllyIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gathering = scn.GetFreepsCard("gathering");
		var gandalf = scn.GetFreepsCard("gandalf");
		var treebeard = scn.GetFreepsCard("treebeard");
		scn.MoveCardsToHand(gathering);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();

		// Gandalf is a [gandalf] companion and no Ent.
		assertFalse(scn.FreepsPlayAvailable(gathering));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveAlliesToTable(treebeard);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(gathering));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithOneEntTakesAGandalfCompanionUnaskedAndShufflesTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gathering = scn.GetFreepsCard("gathering");
		var guardian = scn.GetFreepsCard("guardian");
		var gandalf = scn.GetFreepsCard("gandalf");
		var dasron = scn.GetFreepsCard("dasron");
		var avenger = scn.GetFreepsCard("avenger");
		var barliman = scn.GetFreepsCard("barliman");
		var gimli = scn.GetFreepsCard("gimli");
		var doom = scn.GetFreepsCard("doom");
		var watch = scn.GetFreepsCard("watch");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		scn.MoveCardsToHand(gathering);
		// One Ent; Gandalf beside him nearly makes the second.
		scn.MoveCompanionsToTable(guardian, gandalf);

		scn.StartGame();
		assertInZone(Zone.DECK, dasron, avenger, barliman, gimli, doom, watch, lordofmoria);
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsPlayCard(gathering);
		scn.MarkGameLog();

		// No choice of branch: the second cannot be taken with one Ent. The deck is shown, and then
		// [gandalf] companions of two races are offered; a [gandalf] ally, a [dwarven] companion and
		// [gandalf] conditions are not.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(dasron, avenger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman, gimli, doom, watch, lordofmoria));
		scn.FreepsChooseCardBPFromSelection(dasron);

		assertInHand(dasron);
		assertInZone(Zone.DECK, avenger, doom, watch);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInDiscard(gathering);
		// Named in the log as a card of the kind taken; the one left in the deck is not.
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(dasron));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(avenger));
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTwoEntsEitherBranchIsOfferedAndTheSecondTakesACompanionAndACondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gathering = scn.GetFreepsCard("gathering");
		var guardian = scn.GetFreepsCard("guardian");
		var treebeard = scn.GetFreepsCard("treebeard");
		var dasron = scn.GetFreepsCard("dasron");
		var avenger = scn.GetFreepsCard("avenger");
		var barliman = scn.GetFreepsCard("barliman");
		var gimli = scn.GetFreepsCard("gimli");
		var doom = scn.GetFreepsCard("doom");
		var watch = scn.GetFreepsCard("watch");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		scn.MoveCardsToHand(gathering);
		// Two Ents: a companion and an ally.
		scn.MoveCompanionsToTable(guardian);
		scn.MoveAlliesToTable(treebeard);

		scn.StartGame();
		assertInZone(Zone.DECK, dasron, avenger, barliman, gimli, doom, watch, lordofmoria);
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(gathering);

		assertEquals(2, scn.FreepsGetMultipleChoices().size());
		assertTrue(scn.FreepsChoiceAvailable("companion into hand"));
		assertTrue(scn.FreepsChoiceAvailable("condition into hand"));
		scn.FreepsChoose("condition into hand");

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(dasron, avenger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman, gimli, doom, watch, lordofmoria));
		scn.FreepsChooseCardBPFromSelection(avenger);

		// The deck is shown again for the second take. [gandalf] conditions of the support area and borne kinds; not a [dwarven] condition, nor a
		// [gandalf] card that is no condition.
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(doom, watch));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lordofmoria, barliman, dasron));
		scn.FreepsChooseCardBPFromSelection(watch);

		assertInHand(avenger, watch);
		assertInZone(Zone.DECK, dasron, doom);
		assertEquals(2, scn.GetFreepsHandCount());
		assertInDiscard(gathering);
		// Each of the two takes shuffles the deck as its look ends; a deck shuffled twice is shuffled, and no
		// player can tell the two apart.
		assertEquals(freepsShuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
