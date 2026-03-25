package com.gempukku.lotro.cards.unofficial.rtmd.set_93_lotro;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * 93_10: At the start of the regroup phase, you may discard your [Dunland] possession to take control of a site.
 *
 * Owner-gated to Shadow (via OwnerIsShadow).
 *
 * Test cards:
 * - Dark Axe (4_21): Dunland possession (twilight 1), bears on Dunland Man
 * - Dunlending Madman (4_15): Dunland Man minion
 */
public class Card_93_010_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Iron Axe (4_26): Dunland possession, bears on Dunland Man
		put("axe", "4_26");
		// Dunlending Madman (4_12): Dunland Man minion, twilight 3
		put("madman", "4_12");
	}};

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "93_10"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"93_10", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var card = scn.GetShadowCard("mod");
		assertEquals("Race Text 93_10", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void DiscardDunlandPossessionToControlSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var madman = scn.GetShadowCard("madman");
		var axe = scn.GetShadowCard("axe");

		scn.StartGame();

		scn.SkipToSite(3);

		scn.MoveMinionsToTable(madman);
		scn.AttachCardsTo(madman, axe);

		scn.SkipToPhase(Phase.REGROUP);

		// Start of regroup — trigger should fire
		// Choose to discard the Dunland possession
		scn.ShadowAcceptOptionalTrigger();

		// Axe should be discarded
		assertEquals(Zone.DISCARD, axe.getZone());

		// Should have taken control of a site
		assertTrue(scn.GetShadowControlledSiteCount() >= 1);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CanDeclineToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var madman = scn.GetShadowCard("madman");
		var axe = scn.GetShadowCard("axe");

		scn.StartGame();

		scn.SkipToSite(3);

		scn.MoveMinionsToTable(madman);
		scn.AttachCardsTo(madman, axe);

		scn.FreepsPassCurrentPhaseAction();
		scn.SkipToPhase(Phase.REGROUP);

		// Decline
		scn.ShadowDeclineOptionalTrigger();

		// Axe should still be attached
		assertEquals(Zone.ATTACHED, axe.getZone());

		// No site control gained
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OwnerGatingFreepsCantTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var madman = scn.GetShadowCard("madman");
		var axe = scn.GetShadowCard("axe");

		scn.MoveMinionsToTable(madman);
		scn.AttachCardsTo(madman, axe);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();
		scn.SkipToPhase(Phase.REGROUP);

		// Freeps owns the mod — trigger doesn't fire for Shadow
		// Axe should still be attached
		assertEquals(Zone.ATTACHED, axe.getZone());
		assertEquals(0, scn.GetShadowControlledSiteCount());
	}
}
