package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_124_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hillorc", "11_124");
					put("hillorc2", "11_124");

					// Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4, resistance 6.  A maneuver
					// ability, never used.
					put("aragorn", "1_89");
					// Farin, Emissary of Erebor: a [dwarven] Dwarf, vitality 3, resistance 6.  Muster at a battleground or
					// underground site only, and none is reached.
					put("farin", "11_7");
					// Gimli, Lively Combatant: a [dwarven] Dwarf, strength 6, vitality 3, resistance 7.  Draws when he wins a
					// skirmish, which he never does here.
					put("gimli", "11_8");
					// Sam, Son of Hamfast: a [shire] Hobbit, vitality 4, resistance 5.  His abilities are never used.
					put("sam", "1_311");
					// Bounder: a Hobbit ally (resistance 0, as every ally has).  A skirmish ability, never used.
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HillOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Hill Orc
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 12
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: Each time this minion wins a skirmish, you may exert each companion who has resistance 4 or less.
		*/

		var scn = GetScenario();

		var hillorc = scn.GetFreepsCard("hillorc");

		assertEquals("Hill Orc", hillorc.getBlueprint().getTitle());
		assertNull(hillorc.getBlueprint().getSubtitle());
		assertFalse(hillorc.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, hillorc.getBlueprint().getSide());
		assertEquals(Culture.ORC, hillorc.getBlueprint().getCulture());
		assertEquals(CardType.MINION, hillorc.getBlueprint().getCardType());
		assertEquals(Race.ORC, hillorc.getBlueprint().getRace());
		assertEquals(4, hillorc.getBlueprint().getTwilightCost());
		assertEquals(12, hillorc.getBlueprint().getStrength());
		assertEquals(2, hillorc.getBlueprint().getVitality());
		assertEquals(4, hillorc.getBlueprint().getSiteNumber());
	}

	@Test
	public void WinningItMayExertEachCompanionWhoHasResistance4OrLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var farin = scn.GetFreepsCard("farin");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, farin, gimli, sam);
		scn.MoveAlliesToTable(bounder);

		var hillorc = scn.GetShadowCard("hillorc");
		var hillorc2 = scn.GetShadowCard("hillorc2");

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(hillorc, hillorc2);
		// Sam is exhausted: vitality 4, 3 wounds.
		scn.AddWoundsToChar(sam, 3);
		// 6, 6, 7, 5 and 10, each less 2 burdens; an ally has resistance 0.
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(4, scn.GetResistance(farin));
		assertEquals(5, scn.GetResistance(gimli));
		assertEquals(3, scn.GetResistance(sam));
		assertEquals(8, scn.GetResistance(frodo));
		assertEquals(0, scn.GetResistance(bounder));
		assertEquals(3, scn.GetWoundsOn(sam));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, hillorc);
		// The second copy is left out of every skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// 12 against 8: the Hill Orc wins, and Aragorn has taken the wound of the loss.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hillorc));
		// The copy that won is the one that answers.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hillorc2));
		scn.ShadowAcceptOptionalTrigger();

		// Each companion of resistance 4 or less who can exert does: Aragorn and Farin.  Not Gimli (5), Frodo (8) or
		// Bounder, an ally; exhausted Sam cannot exert, and is still there with his 3 wounds.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DeclinedNoCompanionExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(aragorn, farin);

		var hillorc = scn.GetShadowCard("hillorc");

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(hillorc);
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(4, scn.GetResistance(farin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, hillorc);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hillorc));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingASkirmishAndLivingSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(aragorn, farin);

		var hillorc = scn.GetShadowCard("hillorc");

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(hillorc);
		// 18 against 12, short of double: the Hill Orc loses and takes one wound of its 2 vitality.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 10));
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(4, scn.GetResistance(farin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, hillorc);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(18, scn.GetStrength(aragorn));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(hillorc));
		assertInZone(Zone.SHADOW_CHARACTERS, hillorc);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenNoCompanionHasResistance4OrLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(aragorn, farin);

		var hillorc = scn.GetShadowCard("hillorc");

		scn.StartGame();
		scn.MoveMinionsToTable(hillorc);
		// No burdens: 6, 6 and Frodo's 10.
		assertEquals(0, scn.GetBurdens());
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(farin));
		assertEquals(10, scn.GetResistance(scn.GetRingBearer()));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, hillorc);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// It wins, but its exertions would do nothing: not offered.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(farin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachWinOffersTheExertionsAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(gimli, farin);

		var hillorc = scn.GetShadowCard("hillorc");

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(hillorc);
		// Fierce, so that it skirmishes, and wins, a second time in the turn.  No printed card makes an [orc] Orc
		// fierce short of site 9.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, hillorc, null, Keyword.FIERCE));
		// Gimli at 7, so that 12 is no overwhelm; his resistance, 5, keeps him out of the exertions.
		scn.ApplyAdHocModifier(new StrengthModifier(null, gimli, null, 1));
		assertEquals(5, scn.GetResistance(gimli));
		assertEquals(4, scn.GetResistance(farin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, hillorc);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hillorc));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(farin));
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, hillorc);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hillorc));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(farin));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
