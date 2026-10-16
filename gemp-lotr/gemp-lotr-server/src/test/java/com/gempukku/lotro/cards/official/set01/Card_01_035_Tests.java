package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_035_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("council", "1_35");
					// Legolas, Greenleaf: an Elf companion; his ability is an Archery one
					put("legolas", "1_50");
					// Orophin, Lorien Bowman: an Elf ally (home 6); his ability is an Archery one
					put("orophin", "1_56");
					// Aragorn, Ranger of the North: a [gondor] Man companion who is no Elf
					put("aragorn", "1_89");
					// Shoulder to Shoulder: an [elven] support-area condition, no Elf; its ability is a Maneuver one
					put("shoulder", "1_59");

					// Tales for the draw deck, of each kind there is.
					// Stairs of Khazad-dum: a [dwarven] tale, a condition that plays to the support area, cost 0
					put("stairs", "1_24");
					// The Saga of Elendil: a [gondor] tale, a condition borne by a [gondor] companion
					put("saga", "1_114");
					// Red Book of Westmarch: a [shire] tale, a possession; its trigger needs Bilbo
					put("redbook", "2_113");
					// Scroll of Isildur: a [gondor] tale, an artifact; to play, spot a [gondor] Man
					put("scroll", "9_36");
					// Alliance Reforged: an [elven] tale, a Fellowship event
					put("alliance", "4_58");
					// Lord of Moria: a [dwarven] support-area condition that is no tale
					put("lordofmoria", "1_21");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheCouncilofElrondStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Council of Elrond
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Spot an Elf to play a tale from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("council");

		assertEquals("The Council of Elrond", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAnElfCompanionPlaysATaleOfAnyKindFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var council = scn.GetFreepsCard("council");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stairs = scn.GetFreepsCard("stairs");
		var saga = scn.GetFreepsCard("saga");
		var redbook = scn.GetFreepsCard("redbook");
		var scroll = scn.GetFreepsCard("scroll");
		var alliance = scn.GetFreepsCard("alliance");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveCardsToHand(council);

		scn.StartGame();
		scn.SetFreepsDrawDeck(stairs, saga, redbook, scroll, alliance, lordofmoria);
		scn.FreepsRefreshDecision();

		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertEquals(6, scn.GetFreepsDeckCount());
		assertInHand(council);
		assertNotAttachedTo(saga, aragorn);
		assertTrue(scn.FreepsPlayAvailable(council));

		scn.FreepsPlayCard(council);
		scn.FreepsDismissRevealedCards();
		// A tale of each kind is offered: support-area condition, borne condition, possession, artifact, event.
		// The condition that is no tale is not.  Nothing obliges the player to play one (Q-0019).
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(stairs, saga, redbook, scroll, alliance));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lordofmoria));
		scn.FreepsChooseCardBPFromSelection(saga);

		// The Saga goes on Aragorn, the one [gondor] companion.  The deck is shuffled as the Saga leaves it, and
		// again once the look is over.
		assertAttachedTo(saga, aragorn);
		assertEquals(5, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertInDiscard(council);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SpottingAnElfAllyPlaysOnlyATaleWhoseOwnRequirementsAreMet() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var council = scn.GetFreepsCard("council");
		var orophin = scn.GetFreepsCard("orophin");
		var stairs = scn.GetFreepsCard("stairs");
		var saga = scn.GetFreepsCard("saga");
		var scroll = scn.GetFreepsCard("scroll");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(council);

		scn.StartGame();
		scn.SetFreepsDrawDeck(stairs, saga, scroll, lordofmoria);
		scn.FreepsRefreshDecision();

		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(4, scn.GetFreepsDeckCount());
		assertInHand(council);
		assertTrue(scn.FreepsPlayAvailable(council));

		scn.FreepsPlayCard(council);
		scn.FreepsDismissRevealedCards();
		// With no [gondor] companion or Man in play, the Saga has no bearer and the Scroll's requirement is unmet.
		assertTrue(scn.FreepsHasCardChoiceAvailable(stairs));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(saga, scroll, lordofmoria));
		scn.FreepsChooseCardBPFromSelection(stairs);

		assertInZone(Zone.SUPPORT, stairs);
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertInDiscard(council);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAnElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var council = scn.GetFreepsCard("council");
		var aragorn = scn.GetFreepsCard("aragorn");
		var shoulder = scn.GetFreepsCard("shoulder");
		var stairs = scn.GetFreepsCard("stairs");
		// A companion who is no Elf, and an [elven] card that is none.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(shoulder);
		scn.MoveCardsToHand(council);

		scn.StartGame();
		scn.SetFreepsDrawDeck(stairs);
		scn.FreepsRefreshDecision();

		assertInPlay(aragorn, shoulder);
		assertEquals(1, scn.GetFreepsDeckCount());
		assertInHand(council);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(council));
	}

	@Test
	public void WithNoTaleInTheDrawDeckItIsPlayedForNothingAndTheDeckIsShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var council = scn.GetFreepsCard("council");
		var legolas = scn.GetFreepsCard("legolas");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		var shoulder = scn.GetFreepsCard("shoulder");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(council);

		scn.StartGame();
		scn.SetFreepsDrawDeck(lordofmoria, shoulder);
		scn.FreepsRefreshDecision();

		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsPlayAvailable(council));

		// There is no penalty for not finding a tale (CR 4.2 "draw deck"): the deck is looked through all the same.
		scn.FreepsPlayCard(council);
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(lordofmoria, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(shoulder, false));
		scn.FreepsDismissRevealedCards();

		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertInDiscard(council);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
