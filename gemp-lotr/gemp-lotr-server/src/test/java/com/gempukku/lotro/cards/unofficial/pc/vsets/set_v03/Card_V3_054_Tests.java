package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_054_Tests
{

// ----------------------------------------
// HORN OF FAR HARAD TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horn", "103_54");        // Horn of Far Harad
					put("explorer", "4_250");     // Southron Explorer
					put("mumak", "5_73");         // Mumak - [Raider] mount possession
					put("charger", "103_41");     // Bladetusk Charger - transforms into mounted Southron
					put("southron1", "4_222");    // Desert Warrior - for stacking
					put("orc", "1_271");          // Orc Soldier - bait

					put("aragorn", "1_89");
					put("eowyn", "5_122");
					put("horse", "4_283");           // Horse of Rohan
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HornofFarHaradStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Horn of Far Harad
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Horn
		 * Game Text: Bearer must be a mounted Southron.
		* 	When you play this, hinder all Free Peoples mounts.
		* 	Skirmish: Remove a threat to make a Southron strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horn");

		assertEquals("Horn of Far Harad", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HORN));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}



	@Test
	public void HornAttachesToMountedSouthronAndHindersFPMounts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetShadowCard("horn");
		var explorer = scn.GetShadowCard("explorer");
		var mumak = scn.GetShadowCard("mumak");
		var eowyn = scn.GetFreepsCard("eowyn");
		var horse = scn.GetFreepsCard("horse");
		scn.MoveCardsToHand(horn);
		scn.MoveMinionsToTable(explorer);
		scn.AttachCardsTo(explorer, mumak); // Explorer is now mounted
		scn.MoveCompanionsToTable(eowyn);
		scn.AttachCardsTo(eowyn, horse); // FP horse

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertFalse(scn.IsHindered(horse));
		assertAttachedTo(mumak, explorer);

		scn.ShadowPlayCard(horn);
		// Explorer auto-selected as only mounted Southron

		// Horn attached
		assertInZone(Zone.ATTACHED, horn);

		// FP mount hindered on play
		assertTrue(scn.IsHindered(horse));
	}

	@Test
	public void HornCannotAttachToUnmountedSouthron() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetShadowCard("horn");
		var explorer = scn.GetShadowCard("explorer"); // No mount
		scn.MoveCardsToHand(horn);
		scn.MoveMinionsToTable(explorer);

		scn.StartGame();
		scn.SetTwilight(10);

		assertFalse(scn.HasKeyword(explorer, Keyword.MOUNTED));

		scn.FreepsPassCurrentPhaseAction();

		// Horn should not be playable - no mounted Southron
		assertFalse(scn.ShadowPlayAvailable(horn));
	}

	@Test
	public void HornSkirmishAbilityPumpsSouthron() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetShadowCard("horn");
		var explorer = scn.GetShadowCard("explorer");
		var mumak = scn.GetShadowCard("mumak");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveMinionsToTable(explorer);
		scn.AttachCardsTo(explorer, mumak, horn);
		scn.MoveCompanionsToTable(aragorn);
		scn.AddThreats(2);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, explorer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		int explorerStrengthBefore = scn.GetStrength(explorer);
		int threatsBefore = scn.GetThreats();

		assertTrue(scn.ShadowActionAvailable(horn));
		scn.ShadowUseCardAction(horn);
		// Explorer auto-selected as only Southron

		assertEquals(threatsBefore - 1, scn.GetThreats());
		assertEquals(explorerStrengthBefore + 2, scn.GetStrength(explorer));
	}

	@Test
	public void HornCanAttachToTransformedBladetusk() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetShadowCard("horn");
		var charger = scn.GetShadowCard("charger");
		var southron = scn.GetShadowCard("southron1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(horn);
		scn.MoveCardsToSupportArea(charger);
		scn.MoveMinionsToTable(southron); // Bait
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Transform Charger
		scn.ShadowUseCardAction(charger);

		assertTrue(scn.HasKeyword(charger, Keyword.MOUNTED));
		assertTrue(scn.HasKeyword(charger, Keyword.SOUTHRON));
		assertInZone(Zone.SHADOW_CHARACTERS, charger);

		// Skip to Regroup without assigning Charger
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		//Fierce
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// Regroup
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();

		// Now back to Shadow phase at site 2
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());

		// Charger should still be transformed (until end of turn)
		assertTrue(scn.HasKeyword(charger, Keyword.MOUNTED));
		assertTrue(scn.HasKeyword(charger, Keyword.SOUTHRON));

		// Horn should be playable on transformed Charger
		assertTrue(scn.ShadowPlayAvailable(horn));
		scn.ShadowPlayCard(horn);

		assertAttachedTo(horn, charger);
	}
}
