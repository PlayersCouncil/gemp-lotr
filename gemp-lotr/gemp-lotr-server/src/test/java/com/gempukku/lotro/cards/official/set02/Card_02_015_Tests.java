package com.gempukku.lotro.cards.official.set02;

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

public class Card_02_015_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("waiting", "2_15");
					put("waiting2", "2_15");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3; a skirmish ability only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally of another title; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf; a maneuver ability only

					// The Shadow player's cards, held in hand or drawn
					put("runner", "1_178");     // Goblin Runner: a Shadow minion, cost 1; its text is about playing it
					put("troop", "1_177");      // Goblin Patrol Troop: a Shadow minion with no text
					put("spear", "1_182");      // Goblin Spear: a Shadow possession
					put("battlecry", "1_128");  // Lurtz's Battle Cry: a Shadow event
					put("stairs", "1_24");      // Stairs of Khazad-dum: a Free Peoples condition, in the Shadow player's hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhatAreWeWaitingForStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: What Are We Waiting For?
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Exert a Dwarf to make an opponent draw 2 cards. That player then chooses to either discard 2 Shadow cards from hand or skip his or her next Shadow phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("waiting");

		assertEquals("What Are We Waiting For?", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsADwarfToMakeTheOpponentDrawTwoAndHeMayDiscardTwoShadowCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var waiting = scn.GetFreepsCard("waiting");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var spear = scn.GetShadowCard("spear");
		var battlecry = scn.GetShadowCard("battlecry");
		var stairs = scn.GetShadowCard("stairs");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(waiting);

		scn.StartGame();
		scn.MoveCardsToHand(runner, stairs);
		scn.SetShadowDrawDeck(spear, battlecry, troop);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInHand(runner, stairs);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDrawDeck().size());

		scn.FreepsPlayCard(waiting);

		// Gimli, the only Dwarf, exerts; the Shadow player draws the top two cards of his deck.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDrawDeck().size());
		assertInHand(spear, battlecry);

		// He chooses which, and discards from his own hand: a minion, a possession and an event, the two just
		// drawn among them; not the Free Peoples card he holds.
		assertTrue(scn.ShadowChoiceAvailable("Discard 2 Shadow cards"));
		assertTrue(scn.ShadowChoiceAvailable("Skip"));
		scn.ShadowChoose("Discard 2 Shadow cards");
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, spear, battlecry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(stairs));
		assertEquals(2, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(runner, spear);

		assertInDiscard(runner, spear, waiting);
		assertInHand(battlecry, stairs);
		assertEquals(2, scn.GetShadowHandCount());

		// His Shadow phase is not skipped.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ADwarfAllyMayExertAndTheOpponentMaySkipHisNextShadowPhaseButNotTheOneAfter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var waiting = scn.GetFreepsCard("waiting");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var spear = scn.GetShadowCard("spear");
		var battlecry = scn.GetShadowCard("battlecry");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(waiting);

		scn.StartGame();
		scn.MoveCardsToHand(runner, troop);
		scn.SetShadowDrawDeck(spear, battlecry);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInHand(runner, troop);
		assertEquals(2, scn.GetShadowHandCount());

		scn.FreepsPlayCard(waiting);
		// A Dwarf: the companion and the ally; not the companion who is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(grimir);

		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(4, scn.GetShadowHandCount());

		// He could discard two Shadow cards, and skips instead.
		assertTrue(scn.ShadowChoiceAvailable("Discard 2 Shadow cards"));
		scn.ShadowChoose("Skip");
		assertEquals(4, scn.GetShadowHandCount());
		assertInDiscard(waiting);

		// The move to site 2: no Shadow phase, and with no minion in play the regroup phase comes next.
		scn.FreepsPass();
		assertFalse(scn.ShadowDecisionAvailable("Play Shadow action"));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInHand(runner, troop);

		// A second move at the end of the regroup phase brings a second Shadow phase in the same turn: only the
		// next one was skipped, and this one is his as usual.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.AwaitingShadowPhaseActions());

		// His next Shadow phase after that one, on the first player's next turn, is his as usual.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithFewerThanTwoShadowCardsInHandAfterTheDrawTheOpponentSkipsHisShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var waiting = scn.GetFreepsCard("waiting");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		var stairs = scn.GetShadowCard("stairs");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(waiting);

		scn.StartGame();
		// His hand is empty; he draws one Shadow card and one Free Peoples card.
		scn.SetShadowDrawDeck(runner, stairs);
		assertEquals(0, scn.GetShadowHandCount());

		scn.FreepsPlayCard(waiting);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInHand(runner, stairs);
		// Two Shadow cards cannot be discarded, so the skip is what is done, with nothing to choose.
		assertFalse(scn.ShadowChoiceAvailable("Discard 2 Shadow cards"));
		assertFalse(scn.ShadowDecisionAvailable("Choose cards"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInHand(runner, stairs);

		scn.FreepsPass();
		assertFalse(scn.ShadowDecisionAvailable("Play Shadow action"));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInHand(runner, stairs);
	}

	@Test
	public void ASecondCopyMayBeAnsweredBySkippingTheSameShadowPhaseAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var waiting = scn.GetFreepsCard("waiting");
		var waiting2 = scn.GetFreepsCard("waiting2");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var spear = scn.GetShadowCard("spear");
		var battlecry = scn.GetShadowCard("battlecry");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(waiting, waiting2);

		scn.StartGame();
		scn.SetShadowDrawDeck(runner, troop, spear, battlecry);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.FreepsPlayCard(waiting);
		scn.ShadowChoose("Skip");
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(1, scn.GetWoundsOn(gimli));

		// The second copy: he holds four Shadow cards and may still choose to skip the same Shadow phase.
		scn.FreepsPlayCard(waiting2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(4, scn.GetShadowHandCount());
		assertTrue(scn.ShadowChoiceAvailable("Discard 2 Shadow cards"));
		assertTrue(scn.ShadowChoiceAvailable("Skip"));
		scn.ShadowChoose("Skip");

		assertEquals(4, scn.GetShadowHandCount());
		assertInDiscard(waiting, waiting2);
		scn.FreepsPass();
		assertFalse(scn.ShadowDecisionAvailable("Play Shadow action"));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyDwarfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var waiting = scn.GetFreepsCard("waiting");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(waiting);
		// Vitality 3 with 2 wounds: exhausted.  Aragorn, who is no Dwarf, could exert.
		scn.AddWoundsToChar(gimli, 2);

		scn.StartGame();

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsPlayAvailable(waiting));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
