package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_132_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "4_132");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, cost 4, strength 8; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion who is not Aragorn, strength 7; a skirmish ability never used
					put("band", "4_202");       // Uruk-hai Band: [isengard] Uruk-hai, strength 10 (+2 for each site its owner controls: none here)
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9; damage +1 and nothing else
					put("retainer", "3_57");    // Isengard Retainer: an [isengard] Orc, no Uruk-hai, strength 8; a regroup ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRohanAragornScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "4_132");
					// Aragorn, Defender of Rohan: the only Aragorn who is not [gondor], strength 8; his hunter needs the
					// Ring-bearer assigned, and Frodo is not assigned here
					put("aragorn", "17_93");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion who is not Aragorn
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9; damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RangersSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Ranger's Sword, Blade of Aragorn
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be Aragorn.<br>While skirmishing an Uruk-hai, Aragorn is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sword");

		assertEquals("Ranger's Sword", card.getBlueprint().getTitle());
		assertEquals("Blade of Aragorn", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnlyOnAragornWhoIsStrengthPlus2OnlyOnceHisSkirmishAgainstAnUrukHaiIsUnderWay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var band = scn.GetShadowCard("band");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(aragorn, sword);

		scn.StartGame();

		// Frodo and Boromir, and no Aragorn: nobody may bear it.
		assertFalse(scn.FreepsPlayAvailable(sword));

		scn.FreepsPlayCard(aragorn);
		assertTrue(scn.FreepsPlayAvailable(sword));

		// Aragorn is the only bearer it may have, so nobody is asked.
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();
		scn.MoveMinionsToTable(band, guard);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, band}, new PhysicalCardImpl[]{boromir, guard});

		// Boromir's skirmish first.  Aragorn is assigned to an Uruk-hai but not yet skirmishing him:
		// 8 + 2 for the Sword's printed bonus, and no more.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, guard));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, band));
		assertEquals(10, scn.GetStrength(aragorn));
		// Boromir is skirmishing an Uruk-hai too, and bears no Sword: his printed 7.
		assertEquals(7, scn.GetStrength(boromir));
		scn.FreepsPass();
		scn.ShadowPass();

		// Aragorn's skirmish, against the Uruk-hai: 8 + 2 + 2.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, band));
		assertEquals(12, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsPass();
		scn.ShadowPass();

		// The skirmish is over (12 beats 10): 8 + 2 again.
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AragornSkirmishingAnIsengardOrcIsNotStrengthPlus2WhileAnUrukHaiWaitsToSkirmishBoromir() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var band = scn.GetShadowCard("band");
		var retainer = scn.GetShadowCard("retainer");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, sword);

		scn.StartGame();
		assertAttachedTo(sword, aragorn);
		scn.FreepsPass();
		scn.MoveMinionsToTable(band, retainer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, retainer}, new PhysicalCardImpl[]{boromir, band});

		// Aragorn's skirmish, against an [isengard] Orc who is no Uruk-hai, while the Uruk-hai waits for Boromir.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, retainer));
		assertTrue(scn.IsCharAssignedAgainst(boromir, band));
		// 8 + 2 for the Sword's printed bonus, and no more
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void PlaysOnAragornDefenderOfRohanWhoIsStrengthPlus2SkirmishingASauronUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRohanAragornScenario();

		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(aragorn, sword);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(sword));
		scn.FreepsPlayCard(aragorn);
		assertTrue(scn.FreepsPlayAvailable(sword));
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, aragorn);

		scn.FreepsPass();
		scn.MoveMinionsToTable(guard);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, guard);

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, guard));
		// 8 + 2 for the Sword's printed bonus + 2 against a [sauron] Uruk-hai; Frodo is not assigned, so no hunter
		assertEquals(12, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
