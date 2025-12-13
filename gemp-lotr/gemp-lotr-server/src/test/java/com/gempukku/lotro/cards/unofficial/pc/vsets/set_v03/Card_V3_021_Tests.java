package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_021_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("banner", "103_21");
					put("kingdead", "8_38"); // Gondor Wraith, strength 7, enduring
					put("deadman", "10_27"); // Gondor Wraith, strength 6, enduring
					put("aragorn", "1_89"); // Non-Wraith for negative test

					put("savage1", "1_151"); // Strength 5
					put("savage2", "1_151");
					put("savage3", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EtherealBannerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ethereal Banner
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Vitality: 1
		 * Game Text: Bearer must be a [gondor] Wraith.
		* 	Each time bearer wins a skirmish, you may exert a Wraith and a minion it is assigned to skirmish.
		* 	Response: If bearer overwhelms a minion, discard this possession and remove 2 threats to wound 2 minions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("banner");

		assertEquals("Ethereal Banner", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}



	@Test
	public void EtherealBannerAttachesToGondorWraithAndGivesVitality() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var kingdead = scn.GetFreepsCard("kingdead");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(kingdead, aragorn);
		scn.MoveCardsToHand(banner);

		scn.StartGame();

		int kingVitalityBefore = scn.GetVitality(kingdead);

		// Should auto-attach to King (only valid Gondor Wraith target)
		assertTrue(scn.FreepsPlayAvailable(banner));
		scn.FreepsPlayCard(banner);

		assertAttachedTo(banner, kingdead);
		assertEquals(kingVitalityBefore + 1, scn.GetVitality(kingdead));
	}

	@Test
	public void EtherealBannerWinTriggerExertsWraithAndAssignedMinion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var kingdead = scn.GetFreepsCard("kingdead");
		var deadman = scn.GetFreepsCard("deadman");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.MoveCompanionsToTable(kingdead, deadman);
		scn.AttachCardsTo(kingdead, banner);
		scn.MoveMinionsToTable(savage1, savage2);

		scn.StartGame();
		scn.SkipToAssignments();

		assertEquals(0, scn.GetWoundsOn(deadman));
		assertEquals(0, scn.GetWoundsOn(savage2));

		// King (strength 7) vs savage1 (strength 5) - King wins
		// Dead Man vs savage2
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { kingdead, savage1 },
				new PhysicalCardImpl[] { deadman, savage2 }
		);
		scn.ShadowDeclineAssignments();

		// Resolve King's skirmish first
		scn.FreepsResolveSkirmish(kingdead);
		scn.PassCurrentPhaseActions();

		// King wins, trigger should fire
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// Choose Wraith to exert (Dead Man, since he's assigned to savage2)
		scn.FreepsChooseCard(deadman);

		// Dead Man exerted
		assertEquals(1, scn.GetWoundsOn(deadman));

		// savage2 (minion assigned to Dead Man) should be exerted
		assertEquals(1, scn.GetWoundsOn(savage2));
	}

	@Test
	public void EtherealBannerWinTriggerCanBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var kingdead = scn.GetFreepsCard("kingdead");
		var deadman = scn.GetFreepsCard("deadman");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.MoveCompanionsToTable(kingdead, deadman);
		scn.AttachCardsTo(kingdead, banner);
		scn.MoveMinionsToTable(savage1, savage2);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { kingdead, savage1 },
				new PhysicalCardImpl[] { deadman, savage2 }
		);
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(kingdead);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();

		// Nothing exerted
		assertEquals(0, scn.GetWoundsOn(deadman));
		assertEquals(0, scn.GetWoundsOn(savage2));
	}

	@Test
	public void EtherealBannerOverwhelmResponseWounds2Minions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var kingdead = scn.GetFreepsCard("kingdead");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var savage3 = scn.GetShadowCard("savage3");

		scn.MoveCompanionsToTable(kingdead);
		scn.AttachCardsTo(kingdead, banner);
		scn.MoveMinionsToTable(savage1, savage2, savage3);

		scn.StartGame();

		// Add 2 wounds to King - enduring makes him 7 + 4 = 11 strength
		scn.AddWoundsToChar(kingdead, 2);
		assertEquals(11, scn.GetStrength(kingdead));

		// Add 2 threats for the cost
		scn.AddThreats(2);

		scn.SkipToAssignments();

		// King (11 strength) vs savage1 (5 strength) - King overwhelms (11 > 10 = 2x5)
		scn.FreepsAssignToMinions(kingdead, savage1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(kingdead);
		scn.PassCurrentPhaseActions();

		// savage1 should be overwhelmed (killed)
		assertInZone(Zone.DISCARD, savage1);

		// Win trigger offered first (decline it)
		if (scn.FreepsHasOptionalTriggerAvailable("Discard this and remove 2 threats to wound 2 minions")) {
			scn.FreepsChooseAction("Discard this and remove 2 threats to wound 2 minions");
		}

		// Banner discarded, 2 threats removed
		assertInZone(Zone.DISCARD, banner);
		assertEquals(0, scn.GetThreats());

		assertEquals(1, scn.GetWoundsOn(savage2));
		assertEquals(1, scn.GetWoundsOn(savage3));
	}

	@Test
	public void EtherealBannerOverwhelmResponseNotOfferedWithoutEnoughThreats() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var kingdead = scn.GetFreepsCard("kingdead");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.MoveCompanionsToTable(kingdead);
		scn.AttachCardsTo(kingdead, banner);
		scn.MoveMinionsToTable(savage1, savage2);

		scn.StartGame();

		// Add 2 wounds for overwhelm strength
		scn.AddWoundsToChar(kingdead, 2);

		// Only 1 threat (need 2 for cost)
		scn.AddThreats(1);

		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(kingdead, savage1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(kingdead);
		scn.PassCurrentPhaseActions();

		// savage1 overwhelmed
		assertInZone(Zone.DISCARD, savage1);

		// Win trigger first (decline)
		if (scn.FreepsHasOptionalTriggerAvailable()) {
			scn.FreepsDeclineOptionalTrigger();
		}

		// Response should NOT be offered (not enough threats)
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// Banner should still be attached
		assertAttachedTo(banner, kingdead);
		assertEquals(1, scn.GetThreats());
	}

	@Test
	public void EtherealBannerOverwhelmResponseNotOfferedForNormalWin() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var kingdead = scn.GetFreepsCard("kingdead");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.MoveCompanionsToTable(kingdead);
		scn.AttachCardsTo(kingdead, banner);
		scn.MoveMinionsToTable(savage1, savage2);

		scn.StartGame();

		// No extra wounds - King is strength 7 vs savage strength 5
		// 7 is NOT double 5 (would need 10+), so no overwhelm
		scn.AddThreats(2);

		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(kingdead, savage1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(kingdead);
		scn.PassCurrentPhaseActions();

		// savage1 loses but is NOT overwhelmed
		assertEquals(1, scn.GetWoundsOn(savage1));

		// Win trigger offered (decline it)
		assertTrue(scn.FreepsHasOptionalTriggerAvailable("Exert a Wraith and a minion it is assigned to"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable("Discard this and remove 2 threats to wound 2 minions"));
	}
}
