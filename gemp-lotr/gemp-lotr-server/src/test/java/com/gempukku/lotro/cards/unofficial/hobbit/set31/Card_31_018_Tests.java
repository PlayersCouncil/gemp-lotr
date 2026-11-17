package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_018_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("staff", "31_18");
					put("gandalf", "30_26");    // Gandalf, Friend of Thorin: a Wizard companion; a fellowship ability only
					// Saruman, The White: a Wizard ally; a standing defender +1 for Gandalf and abilities used only on purpose
					put("saruman", "32_24");
					put("balin", "30_2");       // Balin, Brother of Dwalin: a companion who is no Wizard

					// For the draw deck. Take up arms. Fight!: a [gandalf] spell event. Salve: a [gandalf] spell
					// condition. He gives me courage: a [gandalf] event that is no spell. Caradhras Has Not
					// Forgiven Us: an [isengard] spell.
					put("takeup", "30_31");
					put("salve", "12_32");
					put("courage", "30_30");
					put("caradhras", "1_123");
					// Goblin Patrol Troop: a card with no text, laid on top of a draw deck so that the reconciles
					// before a later turn do not take the spell its offer is to find.
					for (int i = 1; i <= 10; i++)
						put("troop" + i, "1_177");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void WizardStaffStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Wizard Staff
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Staff
		 * Vitality: +1
		 * Game Text: Bearer must be a Wizard.
		 * 	At the start of each of your turns, you may exert bearer to take a [gandalf] spell from your draw deck into your hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("staff");

		assertEquals("Wizard Staff", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.STAFF));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnAWizardCompanionOrAllyAndNoOtherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var saruman = scn.GetFreepsCard("saruman");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, balin);
		scn.MoveAlliesToTable(saruman);

		scn.StartGame();
		scn.MoveCardsToHand(staff);
		scn.FreepsRefreshDecision();
		assertInHand(staff);

		assertTrue(scn.FreepsPlayAvailable(staff));
		scn.FreepsPlayCard(staff);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, saruman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(balin, bilbo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(saruman);
		assertAttachedTo(staff, saruman);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfTheTurnBearerMayExertToTakeAGandalfSpellFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var takeup = scn.GetFreepsCard("takeup");
		var salve = scn.GetFreepsCard("salve");
		var courage = scn.GetFreepsCard("courage");
		var caradhras = scn.GetFreepsCard("caradhras");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);
		assertAttachedTo(staff, gandalf);
		assertEquals(0, scn.GetWoundsOn(gandalf));

		scn.StartGame();
		assertInZone(Zone.DECK, takeup, salve, courage, caradhras);
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(staff));
		int hand = scn.GetFreepsHandCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		scn.MarkGameLog();

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// The deck is shown first, then the choice: the two [gandalf] spells may be taken, the [gandalf] event that
		// is no spell and the [isengard] spell may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(courage, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(caradhras, false));
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(takeup, salve));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(courage, caradhras));
		scn.FreepsChooseCardBPFromSelection(takeup);

		assertInHand(takeup);
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertInZone(Zone.DECK, salve, courage, caradhras);
		// The deck looked through is shuffled once; the other player's is not.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		// The card taken is named in the log, as the showing of its kind; one left in the deck is not.
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(takeup));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(salve));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnAllyBearerExertsAndTheOfferComesAgainOnlyAtItsOwnersNextTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var saruman = scn.GetFreepsCard("saruman");
		var salve = scn.GetFreepsCard("salve");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(saruman);
		scn.AttachCardsTo(saruman, staff);
		assertAttachedTo(staff, saruman);
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(gandalf));

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(staff));
		scn.FreepsAcceptOptionalTrigger();
		// Saruman, the bearer, exerts; Gandalf, the other Wizard, does not.
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(salve);
		assertInHand(salve);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		// A [gandalf] spell kept in the deck for the turns to come: the offer is made only with one there.
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("troop1"), scn.GetFreepsCard("troop2"), scn.GetFreepsCard("troop3"), scn.GetFreepsCard("troop4"), scn.GetFreepsCard("troop5"), scn.GetFreepsCard("troop6"), scn.GetFreepsCard("troop7"), scn.GetFreepsCard("troop8"), scn.GetFreepsCard("troop9"), scn.GetFreepsCard("troop10"),
				scn.GetFreepsCard("takeup"));

		// The other player's turn: nothing is offered.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(staff));

		// Its owner's next turn: offered again.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(staff));
	}

	@Test
	public void DeclinedNothingIsExertedTakenOrShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var takeup = scn.GetFreepsCard("takeup");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);
		assertEquals(0, scn.GetWoundsOn(gandalf));

		scn.StartGame();
		assertInZone(Zone.DECK, takeup);
		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(staff));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(hand, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertInZone(Zone.DECK, takeup);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoGandalfSpellInTheDrawDeckTheOfferIsNotMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var takeup = scn.GetFreepsCard("takeup");
		var salve = scn.GetFreepsCard("salve");
		var courage = scn.GetFreepsCard("courage");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToDiscard(takeup, salve);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInDiscard(takeup, salve);

		scn.StartGame();
		assertInZone(Zone.DECK, courage);
		int hand = scn.GetFreepsHandCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		// A [gandalf] event that is no spell and a spell of another culture: nothing to take.  A Hobbit-set take from the draw deck with nothing to take is withheld (Q-0019; the orchestrator on Q-0406 and Q-0418):
		// the bearer does not exert for a look that finds nothing.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(staff));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(hand, scn.GetFreepsHandCount());
		assertInZone(Zone.DECK, courage);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhileBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);
		// Vitality 4 + 1 (the Staff) = 5: four wounds leave him exhausted.
		scn.AddWoundsToChar(gandalf, 4);
		assertEquals(4, scn.GetWoundsOn(gandalf));
		assertTrue(scn.IsExhausted(gandalf));

		scn.StartGame();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(staff));
		assertEquals(4, scn.GetWoundsOn(gandalf));
	}
}
