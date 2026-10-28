package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_138_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("villain", "7_138");
					put("villain2", "7_138");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesertVillainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Desert Villain
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron</b>.
		 * At the start of each skirmish involving this minion, you may remove (1) to add a threat.
		*/

		var scn = GetScenario();

		var villain = scn.GetFreepsCard("villain");

		assertEquals("Desert Villain", villain.getBlueprint().getTitle());
		assertNull(villain.getBlueprint().getSubtitle());
		assertFalse(villain.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, villain.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, villain.getBlueprint().getCulture());
		assertEquals(CardType.MINION, villain.getBlueprint().getCardType());
		assertEquals(Race.MAN, villain.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(villain, Keyword.SOUTHRON));
		assertEquals(4, villain.getBlueprint().getTwilightCost());
		assertEquals(9, villain.getBlueprint().getStrength());
		assertEquals(2, villain.getBlueprint().getVitality());
		assertEquals(4, villain.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachCopyIsOfferedAtTheStartOfItsOwnSkirmishOnlyAndRemovesOneToAddAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var villain = scn.GetShadowCard("villain");
		var villain2 = scn.GetShadowCard("villain2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(villain, villain2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, villain}, new PhysicalCardImpl[]{gimli, villain2});
		scn.SetTwilight(5);

		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());

		// At the start of Aragorn's skirmish only the copy in it offers.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(villain));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(villain2));
		scn.ShadowChooseOptionalTrigger(villain);
		assertEquals(4, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();

		// At the start of Gimli's skirmish only the other copy offers; declined, nothing changes.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(villain2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(villain));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(4, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoTwilightToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var villain = scn.GetShadowCard("villain");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(villain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, villain);
		scn.SetTwilight(0);

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(villain));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsOfferedAgainAtTheStartOfItsSecondSkirmishOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var villain = scn.GetShadowCard("villain");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(villain);
		// Fierce, so that it skirmishes a second time in the turn: no printed card makes a [raider] Man fierce
		// away from site 9.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, villain, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, villain);
		scn.SetTwilight(5);

		assertEquals(0, scn.GetThreats());
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(villain));
		scn.ShadowChooseOptionalTrigger(villain);
		assertEquals(4, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());

		// 8 against 9: Aragorn loses and takes a wound; the minion lives on into the fierce round.
		scn.PassSkirmishActions();
		assertInPlay(villain);
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, villain);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(villain));
		scn.ShadowChooseOptionalTrigger(villain);
		assertEquals(3, scn.GetTwilight());
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
