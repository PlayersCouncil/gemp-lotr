package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_092_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tall", "54_92");
					put("gandalf", "1_72");
					put("lordOfMoria", "1_21");  // Free Peoples support condition
					put("bowmen1", "1_264");     // Orc Bowmen: Shadow support condition, inert without [sauron] Orcs
					put("bowmen2", "1_264");
					put("bowmen3", "1_264");
					put("bowmen4", "1_264");
					put("bowmen5", "1_264");
					put("bowmen6", "1_264");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrownSuddenlyTallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Grown Suddenly Tall
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Spell</b>.
		* 	Spot Gandalf to hinder all conditions.  Add (1) for each Shadow condition hindered (limit (5)).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tall");

		assertEquals("Grown Suddenly Tall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	private static void ShadowPlaysSiteIfAsked(VirtualTableScenario scn) throws DecisionResultInvalidException {
		if (scn.ShadowDecisionAvailable("Choose site to play"))
			scn.ShadowChooseAnyCard();
	}

	/**
	 * Gandalf on the table, the given number of Shadow conditions (plus one Free Peoples condition) in play,
	 * Grown Suddenly Tall in hand if requested; then a fellowship-phase move followed by a regroup-phase move.
	 * Returns the scenario at the point where the regroup move has just happened.
	 */
	private VirtualTableScenario MoveTwiceAtRegroup(boolean tallInHand, boolean withGandalf, int shadowConditions) throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		if (withGandalf)
			scn.MoveCompanionsToTable(scn.GetFreepsCard("gandalf"));
		scn.MoveCardsToSupportArea(scn.GetFreepsCard("lordOfMoria"));
		if (tallInHand)
			scn.MoveCardsToHand(scn.GetFreepsCard("tall"));
		for (int i = 1; i <= shadowConditions; i++)
			scn.MoveCardsToSupportArea(scn.GetShadowCard("bowmen" + i));

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();          // fellowship-phase move 1 -> 2
		ShadowPlaysSiteIfAsked(scn);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();                    // regroup-phase move 2 -> 3
		ShadowPlaysSiteIfAsked(scn);
		return scn;
	}

	/** Twilight in the pool once the regroup move has fully resolved with no response played. */
	private int TwilightAfterRegroupMove(int shadowConditions) throws DecisionResultInvalidException, CardNotFoundException {
		var control = MoveTwiceAtRegroup(false, true, shadowConditions);
		assertFalse(control.FreepsHasOptionalTriggerAvailable());
		return control.GetTwilight();
	}

	@Test
	public void RegroupMoveHindersAllConditionsAndAddsTwilightPerShadowCondition() throws DecisionResultInvalidException, CardNotFoundException {
		// Regression for #1059: the errata was keyed to the fellowship phase instead of the regroup phase
		int expectedWithoutEvent = TwilightAfterRegroupMove(2);

		var scn = MoveTwiceAtRegroup(true, true, 2);
		var tall = scn.GetFreepsCard("tall");
		var lordOfMoria = scn.GetFreepsCard("lordOfMoria");
		var bowmen1 = scn.GetShadowCard("bowmen1");
		var bowmen2 = scn.GetShadowCard("bowmen2");

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.IsHindered(lordOfMoria));
		assertFalse(scn.IsHindered(bowmen1));
		assertFalse(scn.IsHindered(bowmen2));

		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.IsHindered(lordOfMoria));
		assertTrue(scn.IsHindered(bowmen1));
		assertTrue(scn.IsHindered(bowmen2));
		assertEquals(expectedWithoutEvent + 2, scn.GetTwilight());   // only the 2 Shadow conditions count
		assertEquals(Zone.DISCARD, tall.getZone());
	}

	@Test
	public void TwilightAddedIsLimitedToFive() throws DecisionResultInvalidException, CardNotFoundException {
		int expectedWithoutEvent = TwilightAfterRegroupMove(6);

		var scn = MoveTwiceAtRegroup(true, true, 6);
		var bowmen6 = scn.GetShadowCard("bowmen6");

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.IsHindered(bowmen6));
		assertEquals(expectedWithoutEvent + 5, scn.GetTwilight());
	}

	@Test
	public void NotOfferedWithoutGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = MoveTwiceAtRegroup(true, false, 1);
		var tall = scn.GetFreepsCard("tall");
		var bowmen1 = scn.GetShadowCard("bowmen1");

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.IsHindered(bowmen1));
		assertEquals(Zone.HAND, tall.getZone());
	}
}
