package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_030_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("retreat", "4_30");
					put("retreat2", "4_30");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, no text
					put("savage", "4_17");      // Dunlending Savage: [dunland] Man, no text
					// Saruman, Rabble-rouser: a [dunland] minion who is a Wizard and no Man.  His trigger answers a move;
					// he is laid after the last move of each test.
					put("saruman", "4_33");
					// Easterling Axeman: a [raider] Man; stronger and fierce only at 2 burdens.
					put("axeman", "4_224");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoRetreatStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: No Retreat
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype:
		 * Game Text: Plays on a site you control.<br><b>Regroup:</b> Spot 2 [dunland] Men and discard this condition to make the Free Peoples player choose to move again this turn (if the move limit allows).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("retreat");

		assertEquals("No Retreat", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnASiteItsPlayerControlsAndOnNoOtherSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var retreat = scn.GetShadowCard("retreat");

		scn.StartGame();
		// Both markers at site 4: sites 1 to 3 are behind them.
		scn.SkipToSite(4);
		scn.MoveCardsToHand(retreat);
		assertInHand(retreat);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		var site4 = scn.GetSite(4);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		// The other player controls site 3, from an earlier turn of his as the Shadow player.
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.FreepsControls(site3));

		scn.FreepsPass();
		var site5 = scn.GetSite(5);
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertInHand(retreat);
		assertTrue(scn.ShadowPlayAvailable(retreat));

		scn.ShadowPlayCard(retreat);
		assertTrue(scn.ShadowHasCardChoiceAvailable(site1, site2));
		// The other player's site, a site on the path nobody controls, and the site the fellowship is at.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(site3, site4, site5));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(site2);

		assertAttachedTo(retreat, site2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupActionSpotting2DunlandMenDiscardsThisCopyAndTheFellowshipMovesAgainUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var retreat = scn.GetShadowCard("retreat");
		var retreat2 = scn.GetShadowCard("retreat2");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.AttachCardsTo(site1, retreat);
		scn.AttachCardsTo(site2, retreat2);
		assertAttachedTo(retreat, site1);
		assertAttachedTo(retreat2, site2);

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(brigand, savage);
		scn.FreepsPass();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.ShadowActionAvailable(retreat));
		assertTrue(scn.ShadowActionAvailable(retreat2));

		scn.ShadowUseCardAction(retreat);
		assertInDiscard(retreat);
		assertAttachedTo(retreat2, site2);

		scn.PassCurrentPhaseActions();
		scn.PassReconcile(VirtualTableScenario.P2);

		// No "another move" question: the fellowship moves on to site 5.
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedBesideOneDunlandManADunlandWizardAndARaiderMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var retreat = scn.GetShadowCard("retreat");
		var brigand = scn.GetShadowCard("brigand");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		scn.AttachCardsTo(site1, retreat);
		assertAttachedTo(retreat, site1);

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(brigand, saruman, axeman);
		scn.FreepsPass();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(retreat));

		// The move is the Free Peoples player's to choose, as usual.
		scn.ShadowPass();
		scn.PassReconcile(VirtualTableScenario.P2);
		assertAttachedTo(retreat, site1);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void AtTheMoveLimitTheConditionIsDiscardedForNothingAndTheTurnEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var retreat = scn.GetShadowCard("retreat");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		scn.AttachCardsTo(site1, retreat);
		assertAttachedTo(retreat, site1);

		// To site 4 by the regroup move from site 3: two moves of two.
		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(brigand, savage);
		scn.FreepsPass();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		// Its cost can be paid, so it is offered, though the move limit allows no move.
		assertTrue(scn.ShadowActionAvailable(retreat));
		scn.ShadowUseCardAction(retreat);
		assertInDiscard(retreat);

		scn.PassCurrentPhaseActions();
		scn.PassReconcile(VirtualTableScenario.P2);
		scn.PassReconcile(VirtualTableScenario.P1);

		// The fellowship stayed at site 4: the turn is over, and the other player's has begun.
		assertEquals(4, scn.FreepsGetSiteNumber());
		assertInDiscard(brigand, savage);
		assertEquals(VirtualTableScenario.P2, scn.GetCurrentPlayer());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void TheFreePeoplesPlayerIsMadeToMoveAgainOnceAndIsAskedAsUsualInALaterRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var retreat = scn.GetShadowCard("retreat");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		// A move limit of 3, so that after the move the condition demands there is a third to be asked about: 2 by
		// rule + 1.
		scn.ApplyAdHocModifier(new MoveLimitModifier(null, 1));

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		scn.AttachCardsTo(site1, retreat);
		assertAttachedTo(retreat, site1);

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(brigand, savage);
		scn.FreepsPass();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());

		scn.ShadowUseCardAction(retreat);
		scn.PassCurrentPhaseActions();
		scn.PassReconcile(VirtualTableScenario.P2);

		// The second move is made without the Free Peoples player being asked.
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());

		// The regroup phase at site 4: the limit allows a third move, and it is his to choose.
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.PassReconcile(VirtualTableScenario.P2);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}
}
