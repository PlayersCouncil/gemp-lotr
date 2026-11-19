package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_026_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enraged", "6_26");
					put("enraged2", "6_26");
					put("enraged3", "6_26");
					put("enraged4", "6_26");

					put("quickbeam", "6_33");   // Quickbeam, Bregalad: Ent companion; only a cost discount of his own
					put("onod", "17_26");       // Woodland Onod: Ent companion; a skirmish ability used only on purpose
					put("treebeard", "4_104");  // Treebeard, Oldest Living Thing: Ent ally; a Fellowship ability only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] companion who is no Ent
					put("entmoot", "6_29");     // Ent Moot: another [gandalf] condition borne by an Ent (limit 1 per bearer of its own)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EnragedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Enraged
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: 
		 * Strength: 4
		 * Game Text: Bearer must be an Ent. Limit 1 per bearer.<br>Discard this condition at the end of the turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("enraged");

		assertEquals("Enraged", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAnEntCompanionOrAllyAndNotOnAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enraged = scn.GetFreepsCard("enraged");
		var quickbeam = scn.GetFreepsCard("quickbeam");
		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(quickbeam, gandalf);
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCardsToHand(enraged);

		scn.StartGame();

		assertInHand(enraged);
		assertTrue(scn.FreepsPlayAvailable(enraged));
		scn.FreepsPlayCard(enraged);

		assertTrue(scn.FreepsHasCardChoiceAvailable(quickbeam, treebeard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(treebeard);

		assertAttachedTo(enraged, treebeard);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnEntAlreadyBearingEnragedIsNotOfferedAndWithEveryEntBearingOneItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enraged = scn.GetFreepsCard("enraged");
		var enraged2 = scn.GetFreepsCard("enraged2");
		var enraged3 = scn.GetFreepsCard("enraged3");
		var enraged4 = scn.GetFreepsCard("enraged4");
		var quickbeam = scn.GetFreepsCard("quickbeam");
		var onod = scn.GetFreepsCard("onod");
		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var entmoot = scn.GetFreepsCard("entmoot");
		scn.MoveCompanionsToTable(quickbeam, onod, gandalf);
		scn.MoveAlliesToTable(treebeard);
		scn.AttachCardsTo(quickbeam, enraged);
		scn.AttachCardsTo(onod, entmoot);
		scn.MoveCardsToHand(enraged2, enraged3, enraged4);

		scn.StartGame();

		assertAttachedTo(enraged, quickbeam);
		assertAttachedTo(entmoot, onod);
		scn.FreepsPlayCard(enraged2);

		// Onod bears another [gandalf] condition, which does not count; Treebeard bears nothing.
		assertTrue(scn.FreepsHasCardChoiceAvailable(onod, treebeard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(quickbeam, gandalf));
		scn.FreepsChooseCard(treebeard);
		assertAttachedTo(enraged2, treebeard);

		// Onod is the one Ent left without one, and takes it unasked.
		scn.FreepsPlayCard(enraged3);
		assertAttachedTo(enraged3, onod);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(enraged4));
	}

	@Test
	public void StaysThroughASecondMoveAndEachCopyIsDiscardedAtTheEndOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enraged = scn.GetFreepsCard("enraged");
		var enraged2 = scn.GetFreepsCard("enraged2");
		var quickbeam = scn.GetFreepsCard("quickbeam");
		var onod = scn.GetFreepsCard("onod");
		var treebeard = scn.GetFreepsCard("treebeard");
		var entmoot = scn.GetFreepsCard("entmoot");
		scn.MoveCompanionsToTable(quickbeam, onod);
		scn.MoveAlliesToTable(treebeard);
		scn.AttachCardsTo(quickbeam, enraged);
		scn.AttachCardsTo(treebeard, enraged2);
		scn.AttachCardsTo(onod, entmoot);

		scn.StartGame();

		assertAttachedTo(enraged, quickbeam);
		assertAttachedTo(enraged2, treebeard);
		assertAttachedTo(entmoot, onod);

		// The end of the regroup phase, with a second move, is no end of the turn.
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseToMove();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertAttachedTo(enraged, quickbeam);
		assertAttachedTo(enraged2, treebeard);

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertAttachedTo(enraged, quickbeam);
		assertAttachedTo(enraged2, treebeard);

		// At the move limit, passing the regroup phase ends the turn: the Shadow player reconciles, still in the
		// regroup phase, and the turn then ends.
		scn.PassCurrentPhaseActions();
		assertTrue(scn.ShadowDecisionAvailable("Reconcile"));
		assertAttachedTo(enraged, quickbeam);
		scn.ShadowDeclineReconciliation();

		assertInDiscard(enraged, enraged2);
		assertAttachedTo(entmoot, onod);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
