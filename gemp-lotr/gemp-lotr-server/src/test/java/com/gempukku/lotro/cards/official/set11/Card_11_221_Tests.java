package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_221_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lemenya", "11_221");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, resistance 6; a maneuver ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3, vitality 4, resistance 5; abilities not used
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its (2) is never added, being cheated in
					// Thrarin, Dwarven Smith: an ally (resistance 0) of strength 4, vitality 2, who may exert in the
					// maneuver phase to take part in skirmishes
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireLemenyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Úlairë Lemenya, Fifth of the Nine Riders
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: Each character skirmishing Úlairë Lemenya who has resistance 5 or less is strength -3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lemenya");

		assertEquals("Úlairë Lemenya", card.getBlueprint().getTitle());
		assertEquals("Fifth of the Nine Riders", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ACompanionOfResistance5SkirmishingLemenyaIsStrengthMinus3AndNoOtherIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		// One burden: Aragorn 6 - 1 = 5, at the threshold; Sam 5 - 1 = 4.
		scn.AddBurdens(1);
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(4, scn.GetResistance(sam));
		scn.MoveMinionsToTable(lemenya, runner);
		scn.FreepsPass();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, lemenya},
				new PhysicalCardImpl[]{sam, runner});
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(sam));

		// Sam skirmishes another minion: his strength is his own.  Aragorn is assigned to Lemenya but not yet
		// skirmishing her.
		scn.FreepsResolveSkirmish(sam);
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(8, scn.GetStrength(aragorn));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));

		// In his own skirmish with her: 8 - 3
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(5, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(9, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ACompanionOfResistance6SkirmishingLemenyaKeepsHisStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		// No burden: Aragorn's resistance is 6, one over.
		assertEquals(0, scn.GetBurdens());
		assertEquals(6, scn.GetResistance(aragorn));
		scn.MoveMinionsToTable(lemenya);
		scn.FreepsPass();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAllySkirmishingLemenyaIsStrengthMinus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.MANEUVER);

		// An ally's resistance is 0 (CR 4.2 "resistance").
		assertEquals(0, scn.GetResistance(thrarin));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, lemenya);
		assertEquals(4, scn.GetStrength(thrarin));
		scn.FreepsResolveSkirmish(thrarin);
		// 4 - 3
		assertEquals(1, scn.GetStrength(thrarin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
