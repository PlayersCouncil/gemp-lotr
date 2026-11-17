package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_047_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("duality", "13_47");
					put("duality2", "13_47");
					put("smeagol", "6_45");     // Sméagol, Poor Creature: [gollum] companion; a skirmish ability only
					put("shiresmeagol", "103_116"); // Smeagol, Once Like Us: [shire] companion; an assignment ability only
					put("dontlook", "6_39");    // Don't Look at Them: a Free Peoples [gollum] condition that is no Sméagol; a skirmish ability only
					put("runner", "1_178");     // Goblin Runner: Shadow minion, for the Shadow hand
					put("fens", "5_22");        // Evil-smelling Fens: Shadow condition, for the Shadow hand
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Free Peoples card, for the Shadow hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DualityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Duality
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Spot Sméagol to choose a Shadow player who must discard a Shadow card from hand or remove (2).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("duality");

		assertEquals("Duality", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithBothPossibleTheShadowPlayerChoosesAndDiscardsAShadowCardFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duality = scn.GetFreepsCard("duality");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(duality);

		var runner = scn.GetShadowCard("runner");
		var fens = scn.GetShadowCard("fens");
		var aragorn = scn.GetShadowCard("aragorn");
		scn.MoveCardsToHand(runner, fens, aragorn);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetTwilight());
		assertEquals(3, scn.GetShadowHandCount());
		assertInHand(runner, fens, aragorn);
		assertTrue(scn.FreepsPlayAvailable(duality));
		scn.FreepsPlayCard(duality);

		// The Shadow player has the choice of the two.
		assertTrue(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertTrue(scn.ShadowChoiceAvailable("Discard a Shadow card from hand"));
		assertTrue(scn.ShadowChoiceAvailable("Remove (2)"));
		scn.ShadowChoose("Discard a Shadow card from hand");

		// A Shadow card of any type; not the Free Peoples card in his hand.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, fens));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(fens);

		assertInDiscard(fens, duality);
		assertInHand(runner, aragorn);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayChooseToRemoveTwoTwilightAndAShireSmeagolIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duality = scn.GetFreepsCard("duality");
		var smeagol = scn.GetFreepsCard("shiresmeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(duality);

		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetTwilight());
		assertInHand(runner);
		assertTrue(scn.FreepsPlayAvailable(duality));
		scn.FreepsPlayCard(duality);

		assertTrue(scn.ShadowDecisionAvailable("Choose action to perform"));
		scn.ShadowChoose("Remove (2)");

		assertEquals(1, scn.GetTwilight());
		assertInHand(runner);
		assertInDiscard(duality);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhatTheShadowPlayerCanDoInFullIsDoneUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duality = scn.GetFreepsCard("duality");
		var duality2 = scn.GetFreepsCard("duality2");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(duality, duality2);

		var runner = scn.GetShadowCard("runner");
		var fens = scn.GetShadowCard("fens");
		var aragorn = scn.GetShadowCard("aragorn");
		// Only a Free Peoples card in the Shadow hand: no Shadow card to discard.
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetTwilight());
		assertInHand(aragorn);
		scn.FreepsPlayCard(duality);

		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetTwilight());
		assertInHand(aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Now (1) in the pool, short of (2), and two Shadow cards in hand: the discard is all that can be done in full.
		scn.MoveCardsToHand(runner, fens);
		assertInHand(runner, fens);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(duality2);

		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, fens));
		scn.ShadowChooseCard(runner);

		assertInDiscard(runner);
		assertInHand(fens, aragorn);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilSmeagolIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duality = scn.GetFreepsCard("duality");
		var smeagol = scn.GetFreepsCard("smeagol");
		var dontlook = scn.GetFreepsCard("dontlook");
		scn.MoveCardsToSupportArea(dontlook);
		scn.MoveCardsToHand(duality);

		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();

		// Frodo and a [gollum] condition, but no Sméagol.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(duality));

		scn.MoveCompanionsToTable(smeagol);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(duality));
	}
}
