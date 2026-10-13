package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_034_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("narya", "3_34");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion; a strength bonus by races spotted, nothing else
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is not Gandalf; strength +2 only with 3 Ents
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NaryaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Narya
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Vitality: +1
		 * Game Text: Bearer must be Gandalf.
		 * At the start of each of your turns, you may add (3) to remove a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("narya");

		assertEquals("Narya", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RING));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void CannotBePlayedOnAnotherGandalfCompanionAndIsPlayedOnGandalfOnceHeIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narya = scn.GetFreepsCard("narya");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(narya, gandalf);

		scn.StartGame();

		// Ent Avenger is a [gandalf] companion and Frodo a companion: neither is Gandalf.
		assertFalse(scn.FreepsPlayAvailable(narya));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(narya));

		// Gandalf is the one bearer it may go on, so it goes on him unasked.
		scn.FreepsPlayCard(narya);
		assertAttachedTo(narya, gandalf);
		assertNotAttachedTo(narya, avenger);
		assertNotAttachedTo(narya, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfHisOwnersTurnNaryaAddsThreeTwilightToRemoveOneBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narya = scn.GetFreepsCard("narya");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, narya);

		scn.StartGame();
		// The start of the turn, before the fellowship phase.
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(narya));
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetTwilight());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(3, scn.GetTwilight());
		// One burden of the two.
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningLeavesThePoolAndTheBurdensAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narya = scn.GetFreepsCard("narya");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, narya);

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(narya));
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetTwilight());

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoBurdenTheOfferIsStillMadeAndTakingItAddsTheTwilightForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narya = scn.GetFreepsCard("narya");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, narya);

		scn.StartGame();
		// Adding (3) is a cost, so the offer is made though there is no burden to remove (Q-0034).
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(narya));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOfferIsMadeAgainAtHisOwnersNextTurnAndNotAtTheOtherPlayersTurnBetween() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narya = scn.GetFreepsCard("narya");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, narya);

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(narya));
		scn.FreepsDeclineOptionalTrigger();
		scn.AddBurdens(2);
		assertEquals(2, scn.FreepsGetBurdens());

		// The other player's turn: Narya is not active, and player 1 is asked nothing as it starts.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(narya));
		assertEquals(2, scn.FreepsGetBurdens());

		// Back to his owner's turn: the offer is made again, before the fellowship phase.
		scn.SkipToOtherPlayersTurn();
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(narya));
		assertEquals(2, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetTwilight());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
