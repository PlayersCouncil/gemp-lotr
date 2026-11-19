package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_119_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("orc", "11_119");
					put("orc2", "11_119");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; a skirmish ability only
					put("bounder", "1_286");    // Bounder: a Hobbit ALLY, home site 2; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EmboldenedOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Emboldened Orc
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 11
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br>This minion is strength +1 for each companion who is not assigned to a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("orc");

		assertEquals("Emboldened Orc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CountsEachCompanionNotAssignedToASkirmishAndNoOtherCardAsTheAssignmentsAreMadeAndTheSkirmishesEnd() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var bounder = scn.GetFreepsCard("bounder");
		var orc = scn.GetShadowCard("orc");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(orc, troop);

		// 11 + 3: Frodo, Aragorn and Boromir; not Bounder, an ally, nor the Troop, a minion, though neither is assigned.
		assertEquals(14, scn.GetStrength(orc));
		assertEquals(13, scn.GetStrength(troop));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, orc }, new PhysicalCardImpl[] { boromir, troop });

		assertTrue(scn.IsCharAssignedAgainst(aragorn, orc));
		assertTrue(scn.IsCharAssignedAgainst(boromir, troop));
		assertFalse(scn.IsCharAssigned(frodo));
		// 11 + 1: Frodo alone is not assigned.
		assertEquals(12, scn.GetStrength(orc));

		// The lurker's skirmish comes last: Boromir's first.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, troop));
		assertEquals(12, scn.GetStrength(orc));
		assertEquals(0, scn.GetWoundsOn(boromir));

		scn.PassSkirmishActions();
		// Boromir, 7 against 13, loses and lives; his skirmish is over and he is no longer assigned
		// (CR 4.2 "skirmish phase(s)").
		assertEquals(1, scn.GetWoundsOn(boromir));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, orc));
		// 11 + 2: Frodo and Boromir.
		assertEquals(13, scn.GetStrength(orc));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void EachCopyIsStrengthenedForItselfAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var orc = scn.GetShadowCard("orc");
		var orc2 = scn.GetShadowCard("orc2");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(orc, orc2);

		// 11 + 2 (Frodo and Aragorn) each: a second copy adds nothing to the first.
		assertEquals(13, scn.GetStrength(orc));
		assertEquals(13, scn.GetStrength(orc2));
	}
}
