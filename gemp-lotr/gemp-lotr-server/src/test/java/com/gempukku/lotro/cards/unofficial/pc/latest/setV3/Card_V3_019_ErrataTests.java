package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_019_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "103_19");
					put("boromir", "1_97"); // Gondor Man
					put("eomer", "4_267"); // Rohan Man
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
		 * Game Text: Aragorn is strength +1 for each other Man of a different culture you can spot.
		* 	Each time the fellowship moves, you may exert Aragorn and hinder another unbound Man to remove a threat or a burden.
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
	public void AragornStrengthBonusIs1PerCultureMan() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var eomer = scn.GetFreepsCard("eomer");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		int aragornBaseStrength = scn.GetStrength(aragorn);

		// Add Eomer (Rohan Man)
		scn.MoveCompanionsToTable(eomer);
		// Errata: Rohan Man bonus reduced from +2 to +1
		assertEquals(aragornBaseStrength + 1, scn.GetStrength(aragorn));

		// Add Boromir (Gondor Man)
		scn.MoveCompanionsToTable(boromir);
		// +1 for Rohan Man, +1 for Gondor Man = +2 total
		assertEquals(aragornBaseStrength + 2, scn.GetStrength(aragorn));
	}

	@Test
	public void AragornMoveTriggerExertsSelfAndHindersManToRemoveThreat() throws DecisionResultInvalidException, CardNotFoundException {
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
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsHindered(boromir));

		// Pass Fellowship phase to trigger movement
		scn.FreepsPassCurrentPhaseAction();

		// Trigger should be offered
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// Errata: Aragorn must exert as part of the cost
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// Boromir auto-selected as only valid Man to hinder
		assertTrue(scn.IsHindered(boromir));

		// Choose to remove threat
		scn.FreepsChoose("threat");

		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetBurdens()); // Unchanged
	}
}
