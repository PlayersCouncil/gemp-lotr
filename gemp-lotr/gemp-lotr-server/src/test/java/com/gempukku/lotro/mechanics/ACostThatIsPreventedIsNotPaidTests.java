package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A cost that is prevented is not paid, and what it was to buy does not happen.
 *
 * Ketura's ruling on Q-0038 (2026-10-06): preventing a discard that was a cost "is permitted, but it will cause
 * the cost to no longer be considered paid".  Comprehensive Rules 4.2, "cost": "If a player is paying costs for
 * a card and a response action occurs which modifies those costs, that player must continue to pay as many costs
 * as he can, even if it is no longer possible to pay them all.  If all the costs cannot be paid, that card has no
 * effect."
 *
 * (A cost paid in another way by an "instead" is paid: InsteadTests.)
 */
public class ACostThatIsPreventedIsNotPaidTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Corsair Marauder: "When you play this minion, if you can spot another corsair, you may
					// discard a possession to add 2 [raider] tokens to a card that already has a [raider] token
					// on it."  Twilight 4, site 4.
					put("marauder1", "8_57");
					put("marauder2", "8_57");
					put("marauder3", "8_57");
					// Corsair War Galley: a Shadow possession of the support area, and the card with a [raider]
					// token.  Its regroup ability is never used.
					put("galley", "8_59");
					// Scouring of the Shire: "Response: If a [shire] condition or [shire] possession is about to
					// be discarded from play by a Shadow card, add a [shire] token here to prevent that."
					put("scouring", "18_112");
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession, borne by a Hobbit
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void APossessionSavedFromADiscardThatWasACostLeavesTheEffectUndone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var scouring = scn.GetFreepsCard("scouring");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCardsToSupportArea(scouring);
		scn.AttachCardsTo(frodo, sword);

		var marauder1 = scn.GetShadowCard("marauder1");
		var marauder2 = scn.GetShadowCard("marauder2");
		var marauder3 = scn.GetShadowCard("marauder3");
		var galley = scn.GetShadowCard("galley");
		scn.MoveCardsToSupportArea(galley);
		scn.MoveCardsToHand(marauder2, marauder3);

		scn.StartGame();
		scn.MoveMinionsToTable(marauder1);
		scn.AddTokensToCard(galley, 1);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(1, scn.GetCultureTokensOn(galley));
		assertEquals(0, scn.GetCultureTokensOn(scouring));
		assertAttachedTo(sword, frodo);

		// A Marauder is played beside the other, and its text is used: the Hobbit Sword is to be discarded as
		// the cost.  (The War Galley, a possession too, is the other one that could pay.)
		scn.ShadowPlayCard(marauder2);
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, galley));
		scn.ShadowChooseCard(sword);

		// Scouring of the Shire prevents that discard.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scouring));
		scn.FreepsChooseOptionalTrigger(scouring);
		assertEquals(1, scn.GetCultureTokensOn(scouring));
		assertAttachedTo(sword, frodo);

		// The cost was not paid, so no tokens are added: the Galley has the one it had, not 3.
		assertEquals(1, scn.GetCultureTokensOn(galley));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The control, on the same table: with the response declined the Sword is discarded, the cost is paid
		// and the 2 tokens are added.
		scn.ShadowPlayCard(marauder3);
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(sword);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scouring));
		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.DISCARD, sword);
		assertEquals(1, scn.GetCultureTokensOn(scouring));
		assertEquals(3, scn.GetCultureTokensOn(galley));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
