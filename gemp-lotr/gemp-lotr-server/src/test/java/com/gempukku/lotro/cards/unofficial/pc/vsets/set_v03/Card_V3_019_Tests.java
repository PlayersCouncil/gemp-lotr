package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_019_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "103_19");
					put("boromir", "1_97"); // Gondor Man
					put("eomer", "4_267"); // Rohan Man
					put("legolas", "1_50"); // Not a Man
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AragornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Aragorn, King of Gondor and Arnor
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 5
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: While you can spot another [Gondor] Man, Aragorn is strength +1.
		* 	While you can spot a [Rohan] Man, Aragorn is strength +2.
		* 	Each time the fellowship moves, you may hinder another unbound Man to remove a threat or a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aragorn");

		assertEquals("Aragorn", card.getBlueprint().getTitle());
		assertEquals("King of Gondor and Arnor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet()); 
	}

	@Test
	public void AragornGainsStrengthPlus2WithRohanMan() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var eomer = scn.GetFreepsCard("eomer");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		int aragornBaseStrength = scn.GetStrength(aragorn);

		// Add Eomer (Rohan Man)
		scn.MoveCompanionsToTable(eomer);

		// Aragorn should be +2 strength
		assertEquals(aragornBaseStrength + 2, scn.GetStrength(aragorn));
	}

	@Test
	public void AragornGainsStrengthPlus1WithAnotherGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		int aragornBaseStrength = scn.GetStrength(aragorn);

		// Add Boromir (Gondor Man)
		scn.MoveCompanionsToTable(boromir);

		// Aragorn should be +1 strength
		assertEquals(aragornBaseStrength + 1, scn.GetStrength(aragorn));
	}

	@Test
	public void AragornStrengthBonusesStack() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var eomer = scn.GetFreepsCard("eomer");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		int aragornBaseStrength = scn.GetStrength(aragorn);

		// Add both Boromir and Eomer
		scn.MoveCompanionsToTable(boromir, eomer);

		// Aragorn should be +3 strength (+1 Gondor Man, +2 Rohan Man)
		assertEquals(aragornBaseStrength + 3, scn.GetStrength(aragorn));
	}


	@Test
	public void AragornTriggerHindersManToRemoveThreat() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");

		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();

		scn.AddThreats(2);
		scn.AddBurdens(2);

		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetBurdens());
		assertFalse(scn.IsHindered(boromir));

		// Pass Fellowship phase to trigger movement
		scn.FreepsPassCurrentPhaseAction();

		// Trigger should be offered
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// Boromir auto-selected as only valid Man to hinder
		assertTrue(scn.IsHindered(boromir));

		// Choose to remove threat
		scn.FreepsChoose("threat");

		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetBurdens()); // Unchanged
	}

	@Test
	public void AragornTriggerHindersManToRemoveBurden() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");

		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();

		scn.AddThreats(2);
		scn.AddBurdens(2);

		// Pass Fellowship phase to trigger movement
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.IsHindered(boromir));

		// Choose to remove burden
		scn.FreepsChoose("burden");

		assertEquals(2, scn.GetThreats()); // Unchanged
		assertEquals(1, scn.GetBurdens());
	}

	@Test
	public void AragornTriggerCanBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");

		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();

		scn.AddThreats(2);
		scn.AddBurdens(2);

		// Pass Fellowship phase to trigger movement
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();

		// Nothing should change
		assertFalse(scn.IsHindered(boromir));
		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetBurdens());
	}

	@Test
	public void AragornTriggerNotOfferedWithoutAnotherUnboundMan() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas"); // Elf, not Man

		scn.MoveCompanionsToTable(aragorn, legolas);

		scn.StartGame();

		scn.AddThreats(2);
		scn.AddBurdens(2);

		// Pass Fellowship phase to trigger movement
		scn.FreepsPassCurrentPhaseAction();

		// Trigger should NOT be offered - no other unbound Man to hinder
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
	}

	@Test
	public void AragornTriggerAllowsChoiceBetweenMultipleMen() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var eomer = scn.GetFreepsCard("eomer");

		scn.MoveCompanionsToTable(aragorn, boromir, eomer);

		scn.StartGame();

		scn.AddThreats(1);

		// Pass Fellowship phase to trigger movement
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// Should have choice between Boromir and Eomer
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoicesAvailable(boromir, eomer));

		// Choose Eomer
		scn.FreepsChooseCard(eomer);

		assertTrue(scn.IsHindered(eomer));
		assertFalse(scn.IsHindered(boromir));
	}
}
