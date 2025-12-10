package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_032_Tests
{

// ----------------------------------------
// TORMENTED REVENANT TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("revenant", "103_32");    // Tormented Revenant
					put("aragorn", "1_89");       // For comparison / additional companion
					put("legolas", "1_50");

					put("runner", "1_178");       // Goblin Runner - cost 1
					put("orc", "1_271");          // Orc Soldier - cost 2
					put("sauron", "9_48");        // Sauron - cost 18
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TormentedRevenantStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Tormented Revenant
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Wraith
		 * Strength: 4
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Enduring.
		* 	To play, spot or add 2 threats.
		* 	This companion is strength +X, where X is the twilight cost of each minion skirmishing this companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("revenant");

		assertEquals("Tormented Revenant", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}



// ========================================
// EXTRA COST TESTS - Spot or Add 2 Threats
// ========================================

	@Test
	public void TormentedRevenantAdds2ThreatsIfNotEnoughToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var revenant = scn.GetFreepsCard("revenant");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(revenant);

		scn.StartGame();

		int threatsBefore = scn.GetThreats();
		assertEquals(0, threatsBefore);

		assertTrue(scn.FreepsPlayAvailable(revenant));
		scn.FreepsPlayCard(revenant);

		// Should have added 2 threats as cost
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, revenant);
	}

	@Test
	public void TormentedRevenantAdds2ThreatsIfThereIsJust1Threat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var revenant = scn.GetFreepsCard("revenant");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToHand(revenant);
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.AddThreats(1); // Already have 1 threat

		scn.StartGame();

		assertEquals(1, scn.GetThreats());

		assertTrue(scn.FreepsPlayAvailable(revenant));
		scn.FreepsPlayCard(revenant);

		// Should have added 2 since they could not be spotted.
		assertEquals(3, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, revenant);
	}

	@Test
	public void TormentedRevenantSpotsThreatsIfAlready2OrMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var revenant = scn.GetFreepsCard("revenant");
		scn.MoveCardsToHand(revenant);
		scn.AddThreats(3); // Already have 3 threats

		scn.StartGame();

		int threatsBefore = scn.GetThreats();
		assertEquals(3, threatsBefore);

		assertTrue(scn.FreepsPlayAvailable(revenant));
		scn.FreepsPlayCard(revenant);

		// Should NOT have added more threats, just spotted existing ones
		assertEquals(3, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, revenant);
	}

// ========================================
// STRENGTH MODIFIER TESTS
// ========================================

	@Test
	public void TormentedRevenantGainsStrengthEqualToSkirmishingMinionTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var revenant = scn.GetFreepsCard("revenant");
		var sauron = scn.GetShadowCard("sauron"); // Cost 18
		scn.MoveCompanionsToTable(revenant);
		scn.MoveMinionsToTable(sauron);

		scn.StartGame();

		int baseStrength = scn.GetStrength(revenant);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(revenant, sauron);
		scn.FreepsResolveSkirmish(revenant);

		// Sauron costs 18, so Revenant should be strength +18
		assertEquals(baseStrength + 18, scn.GetStrength(revenant));
	}

	@Test
	public void TormentedRevenantGainsStrengthFromMultipleSkirmishingMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var revenant = scn.GetFreepsCard("revenant");
		var runner = scn.GetShadowCard("runner"); // Cost 1
		var orc = scn.GetShadowCard("orc");       // Cost 2
		scn.MoveCompanionsToTable(revenant);
		scn.MoveMinionsToTable(runner, orc);

		scn.StartGame();

		int baseStrength = scn.GetStrength(revenant);

		scn.SkipToAssignments();
		// Assign both minions to Revenant
		scn.FreepsAssignToMinions(revenant, runner);
		scn.ShadowAssignToMinions(revenant, orc);
		scn.FreepsResolveSkirmish(revenant);

		// Runner (1) + Orc (2) = 3 total twilight cost
		assertEquals(baseStrength + 3, scn.GetStrength(revenant));
	}

}
