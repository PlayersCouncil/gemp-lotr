package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_92_033_Tests
{
	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "7_6");       // Gimli, Faithful Companion: Free Peoples companion, "While you have initiative, ..."
					put("blacknum", "8_49");   // Black Numenorean: [raider] minion, "Skirmish: If you have initiative, ..."
					put("sneak", "7_135");     // Desert Sneak: [raider] minion, "... and you have initiative, ..."
					put("enquea", "1_231");    // Ulaire Enquea, Lieutenant of Morgul: minion with no "initiative" in its game text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_33"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "7_6");       // Gimli, Faithful Companion: Free Peoples companion, "While you have initiative, ..."
					put("blacknum", "8_49");   // Black Numenorean: [raider] minion, "Skirmish: If you have initiative, ..."
					put("sneak", "7_135");     // Desert Sneak: [raider] minion, "... and you have initiative, ..."
					put("enquea", "1_231");    // Ulaire Enquea, Lieutenant of Morgul: minion with no "initiative" in its game text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_33", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_33
		 * Type: MetaSite
		 * Intensity: -2
		 * Game Text: At the start of each of your Shadow phases, you may take one card with "initiative" in its game text into hand from your draw deck.
		 */

		var scn = GetShadowScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_33", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-2, card.getBlueprint().getIntensity());
	}

	@Test
	public void TakesInitiativeCardFromDeckAtStartOfShadow() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var blacknum = scn.GetShadowCard("blacknum");
		var gimli = scn.GetShadowCard("gimli");
		var enquea = scn.GetShadowCard("enquea");

		// Leave blacknum, gimli, and enquea in the draw deck (default location)

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.SHADOW);

		int handBefore = scn.GetShadowHandCount();
		int deckBefore = scn.GetShadowDeckCount();
		assertInZone(Zone.DECK, blacknum);

		// Optional trigger fires at start of shadow phase
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowDismissRevealedCards();

		// Should be able to choose between blacknum and gimli (both have "initiative")
		// but not enquea (no "initiative" in game text)
		assertTrue(scn.ShadowHasCardChoiceAvailable(blacknum, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(enquea));
		scn.ShadowChooseCard(blacknum);

		assertInZone(Zone.HAND, blacknum);
		assertInZone(Zone.DECK, gimli);
		assertEquals(handBefore + 1, scn.GetShadowHandCount());
		assertEquals(deckBefore - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OneCardOfTheOwnersOwnDrawDeckIsTakenTheLogNamesItAndThatDeckAloneIsShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var blacknum = scn.GetShadowCard("blacknum");
		var gimli = scn.GetShadowCard("gimli");
		var sneak = scn.GetShadowCard("sneak");
		var enquea = scn.GetShadowCard("enquea");
		var freepsGimli = scn.GetFreepsCard("gimli");

		// A card that says "initiative" and lies in his discard pile is not in his draw deck
		scn.MoveCardsToDiscard(sneak);

		scn.StartGame();
		scn.FreepsPass();

		int handBefore = scn.GetShadowHandCount();
		int deckBefore = scn.GetShadowDeckCount();
		int freepsDeckBefore = scn.GetFreepsDeckCount();
		int shufflesBefore = scn.GetShadowDeckShuffleCount();
		int freepsShufflesBefore = scn.GetFreepsDeckShuffleCount();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.MarkGameLog();
		scn.ShadowAcceptOptionalTrigger();
		// He looks through his deck; the other player is shown nothing
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		scn.ShadowDismissRevealedCards();

		// A Shadow minion and a Free Peoples companion both say "initiative"; one card, or none, may be taken
		assertTrue(scn.ShadowHasCardChoiceAvailable(blacknum, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(enquea, sneak));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(gimli);

		assertInZone(Zone.HAND, gimli);
		assertInZone(Zone.DECK, blacknum);
		assertInZone(Zone.DECK, enquea);
		assertInZone(Zone.DISCARD, sneak);
		assertEquals(handBefore + 1, scn.GetShadowHandCount());
		assertEquals(deckBefore - 1, scn.GetShadowDeckCount());
		// The other player's deck, and his own Gimli in it, are as they were
		assertInZone(Zone.DECK, freepsGimli);
		assertEquals(freepsDeckBefore, scn.GetFreepsDeckCount());
		// A deck that has been looked through is shuffled
		assertEquals(shufflesBefore + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShufflesBefore, scn.GetFreepsDeckShuffleCount());
		// The card was taken for what it says, and nothing says "reveal": the log is what tells the other
		// player which card it was
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(gimli));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(blacknum));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoSuchCardInTheDrawDeckTheDeckIsLookedThroughAndNothingIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var blacknum = scn.GetShadowCard("blacknum");
		var gimli = scn.GetShadowCard("gimli");
		var sneak = scn.GetShadowCard("sneak");
		var enquea = scn.GetShadowCard("enquea");

		// Enquea is all that is left in his draw deck
		scn.MoveCardsToDiscard(blacknum, gimli, sneak);

		scn.StartGame();
		scn.FreepsPass();

		int handBefore = scn.GetShadowHandCount();
		int deckBefore = scn.GetShadowDeckCount();
		int shufflesBefore = scn.GetShadowDeckShuffleCount();

		// He cannot know what his deck holds until he looks: the offer is made all the same
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(enquea, false));
		scn.ShadowDismissRevealedCards();

		assertInZone(Zone.DECK, enquea);
		assertEquals(handBefore, scn.GetShadowHandCount());
		assertEquals(deckBefore, scn.GetShadowDeckCount());
		assertEquals(shufflesBefore + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOfferIsMadeAgainAtASecondShadowPhaseOfTheSameTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var blacknum = scn.GetShadowCard("blacknum");
		var gimli = scn.GetShadowCard("gimli");
		var sneak = scn.GetShadowCard("sneak");
		var enquea = scn.GetShadowCard("enquea");

		scn.StartGame();
		scn.FreepsPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCard(blacknum);
		assertInZone(Zone.HAND, blacknum);

		scn.SkipToMovementDecision();
		// The Shadow player's reconcile has drawn the rest of his little deck: it goes back
		scn.MoveCardsToTopOfDeck(gimli, sneak, enquea);
		scn.FreepsChooseToMove();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, sneak));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(enquea));
		scn.ShadowChooseCard(sneak);

		assertInZone(Zone.HAND, blacknum);
		assertInZone(Zone.HAND, sneak);
		assertInZone(Zone.DECK, gimli);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanDeclineOptionalTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var blacknum = scn.GetShadowCard("blacknum");

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.SHADOW);

		int handBefore = scn.GetShadowHandCount();
		int deckBefore = scn.GetShadowDeckCount();
		int shufflesBefore = scn.GetShadowDeckShuffleCount();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(handBefore, scn.GetShadowHandCount());
		assertEquals(deckBefore, scn.GetShadowDeckCount());
		assertInZone(Zone.DECK, blacknum);
		// A deck that was not looked through is not shuffled
		assertEquals(shufflesBefore, scn.GetShadowDeckShuffleCount());
		// "You" may: the other player is not asked in his place
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FreepsOwnerCannotUseTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		// The Shadow phase of the owner's own turn is the other player's, not one of "your Shadow phases"
		var scn = GetFreepsScenario();

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.SHADOW);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOwnerAsPlayerOneIsOfferedTheTakeInTheShadowPhaseOfPlayerTwosTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var blacknum = scn.GetFreepsCard("blacknum");
		var gimli = scn.GetFreepsCard("gimli");
		var sneak = scn.GetFreepsCard("sneak");
		var enquea = scn.GetFreepsCard("enquea");

		scn.StartGame();

		// Player 2's turn: player 1, who owns the modifier, is now the Shadow player.
		scn.SkipToOtherPlayersTurn();
		// The reconcile that ended his own turn drew his little deck into his hand: it goes back
		scn.MoveCardsToTopOfDeck(blacknum, gimli, sneak, enquea);

		int handBefore = scn.GetFreepsHandCount();
		int deckBefore = scn.GetFreepsDeckCount();
		int shufflesBefore = scn.GetFreepsDeckShuffleCount();
		int otherShufflesBefore = scn.GetShadowDeckShuffleCount();

		scn.ShadowPass(); // player 2's fellowship moves, and player 1's Shadow phase starts

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(blacknum, gimli, sneak));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(enquea));
		scn.FreepsChooseCard(sneak);

		assertInZone(Zone.HAND, sneak);
		assertEquals(handBefore + 1, scn.GetFreepsHandCount());
		assertEquals(deckBefore - 1, scn.GetFreepsDeckCount());
		assertEquals(shufflesBefore + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(otherShufflesBefore, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
