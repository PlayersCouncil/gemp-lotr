package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A player who reconciles gets his hindered cards back first.
 *
 * Comprehensive Rules 5.0, "reconcile": "When any player reconciles (before they discard any cards from hand),
 * restore all cards owned by that player which would be active upon restoration. ...  If The One Ring is hindered,
 * it is restored when the Free Peoples player reconciles."  "Hinder": a card "remains hindered until 'restored',
 * which can be an effect on cards but also happens automatically when players reconcile."
 *
 * The restoring comes before the discard a reconcile begins with (Ketura on Q-0059), so each test reads it with
 * that discard still being asked for.  "Any player" and "reconciles": the Shadow player's reconcile in the regroup
 * phase as well as the Free Peoples player's, and a reconcile that a card brings about as well as the one the
 * regroup phase ends with.  Each restores its own player's cards and no one else's.
 *
 * The hindering is how the board is laid, and is done by the rig.
 */
public class ReconcilingRestoresHinderedCardsTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Aragorn, Ranger of the North: the Free Peoples player's card to hinder
					put("aragorn", "1_89");
					// Goblin Armory: a [moria] condition that lies in the Shadow player's support area; its
					// texts answer a [moria] weapon played and a wound to a [moria] Orc.  The Shadow player's
					// card to hinder.
					put("armory", "1_173");
					// Aragorn's Bow: a card for each hand to hold, so that each reconcile has a discard to
					// ask about.  Never played.
					put("bow", "1_90");
					// Lembas: "Bearer must be a companion.  Regroup: Discard this possession to reconcile
					// your hand."  (Its "To play, spot 3 Elves" does not come up: the rig puts it on its bearer.)
					put("lembas", "4_75");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EachPlayersRegroupReconcileRestoresHisOwnHinderedCardsBeforeHisDiscardAndLeavesTheOtherPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var ring = scn.GetRing();
		var freepsBow = scn.GetFreepsCard("bow");
		var armory = scn.GetShadowCard("armory");
		var shadowBow = scn.GetShadowCard("bow");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCardsToHand(freepsBow, shadowBow);

		scn.StartGame();
		scn.HinderCard(aragorn, ring, armory);

		// No minion: the Shadow phase is followed by the regroup phase
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.IsHindered(aragorn));
		assertTrue(scn.IsHindered(ring));
		assertTrue(scn.IsHindered(armory));
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetShadowHandCount());

		// The Shadow player reconciles first.  His Armory is back with his discard still to be chosen; the
		// Free Peoples player's cards, The One Ring among them, are as they were.
		scn.PassRegroupActions();
		assertTrue(scn.ShadowDecisionAvailable("Reconcile"));
		assertInZone(Zone.HAND, shadowBow);
		assertEquals(1, scn.GetShadowHandCount());
		assertFalse(scn.IsHindered(armory));
		assertTrue(scn.IsHindered(aragorn));
		assertTrue(scn.IsHindered(ring));

		scn.ShadowDeclineReconciliation();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.IsHindered(aragorn));
		assertTrue(scn.IsHindered(ring));

		// Staying, the Free Peoples player reconciles in his turn
		scn.FreepsChooseToStay();
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		assertInZone(Zone.HAND, freepsBow);
		assertEquals(1, scn.GetFreepsHandCount());
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(ring));
		assertAttachedTo(ring, scn.GetRingBearer());

		scn.FreepsDeclineReconciliation();
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(ring));
		assertFalse(scn.IsHindered(armory));
		assertInZone(Zone.HAND, freepsBow);
		// The turn is over: the other player's fellowship phase
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void AReconcileThatACardBringsAboutRestoresItsPlayersHinderedCardsBeforeTheDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var lembas = scn.GetFreepsCard("lembas");
		var bow = scn.GetFreepsCard("bow");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, lembas);
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCardsToHand(bow);

		scn.StartGame();
		scn.HinderCard(aragorn, armory);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.IsHindered(aragorn));
		assertTrue(scn.IsHindered(armory));
		assertAttachedTo(lembas, frodo);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(lembas));

		// The reconcile is under way, in the middle of the regroup phase: Aragorn is back, and the hand has
		// not yet been touched
		scn.FreepsUseCardAction(lembas);
		assertInZone(Zone.DISCARD, lembas);
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		assertInZone(Zone.HAND, bow);
		assertEquals(1, scn.GetFreepsHandCount());
		assertFalse(scn.IsHindered(aragorn));
		// The Shadow player has not reconciled
		assertTrue(scn.IsHindered(armory));

		scn.FreepsDeclineReconciliation();
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.IsHindered(armory));
		assertInZone(Zone.HAND, bow);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
