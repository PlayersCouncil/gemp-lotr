package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_10_034_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("throw", "10_34");

					// The [gondor] Men, and the companions who nearly are.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability used only on purpose
					// Gondorian Merchant: the [gondor] Man ally; his maneuver ability is used only on purpose
					put("merchant", "7_97");
					// Pippin, Trollslayer: a [gondor] companion who is no Man (a Hobbit); a skirmish ability used only on purpose
					put("pippin", "103_31");
					// Rider of Rohan: a Man companion of another culture; his skirmish ability needs him mounted
					put("rider", "4_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LastThrowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Last Throw
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 5
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: If no opponent controls a site, spot 2 [gondor] Men to make the move limit +1 for this turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("throw");

		assertEquals("Last Throw", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(5, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithTwoGondorMenAndNoSiteControlledTheMoveLimitIsOneMoreForTheRestOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lastThrow = scn.GetFreepsCard("throw");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		// Two [gondor] Men, a companion and an ally, beside a [gondor] Hobbit and a [rohan] Man.
		scn.MoveCompanionsToTable(aragorn, pippin, rider);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(lastThrow);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(1, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.FreepsPlayAvailable(lastThrow));
		scn.FreepsPlayCard(lastThrow);
		assertInDiscard(lastThrow);
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// The second move: in the Shadow phase after it the limit is still 3.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(2, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());

		// In the next regroup phase a third move is offered, and made.
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveCount());

		// The next turn, the other player's, has the limit of 2.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void NotPlayableWithOneGondorManBesideAGondorHobbitAndARohanMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lastThrow = scn.GetFreepsCard("throw");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(aragorn, pippin, rider);
		scn.MoveCardsToHand(lastThrow);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(lastThrow));
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void PlayableWhileTheFreePeoplesPlayerControlsASiteAndNotOnceTheShadowPlayerControlsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lastThrow = scn.GetFreepsCard("throw");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(merchant);

		scn.StartGame();
		// Both markers on site 3: sites 1 and 2 are behind every marker.
		scn.SkipToSite(3);
		scn.SetFreepsHand(lastThrow);
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		assertTrue(scn.FreepsControls(site1));
		assertFalse(scn.ShadowControls(site2));
		scn.SkipToPhase(Phase.REGROUP);

		// A site the Free Peoples player controls is no opponent's.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(lastThrow));

		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site2));
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(lastThrow));
		assertEquals(2, scn.GetMoveLimit());
	}
}
