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

public class Card_07_131_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fighter", "7_131");
					put("fighter2", "7_131");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesertFighterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Desert Fighter
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron</b>.
		 * At the start of each skirmish involving this minion, you may remove (1) to add a threat.
		*/

		var scn = GetScenario();

		var fighter = scn.GetFreepsCard("fighter");

		assertEquals("Desert Fighter", fighter.getBlueprint().getTitle());
		assertNull(fighter.getBlueprint().getSubtitle());
		assertFalse(fighter.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, fighter.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, fighter.getBlueprint().getCulture());
		assertEquals(CardType.MINION, fighter.getBlueprint().getCardType());
		assertEquals(Race.MAN, fighter.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(fighter, Keyword.SOUTHRON));
		assertEquals(3, fighter.getBlueprint().getTwilightCost());
		assertEquals(8, fighter.getBlueprint().getStrength());
		assertEquals(2, fighter.getBlueprint().getVitality());
		assertEquals(4, fighter.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachCopyIsOfferedAtTheStartOfItsOwnSkirmishOnlyAndRemovesOneToAddAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fighter = scn.GetShadowCard("fighter");
		var fighter2 = scn.GetShadowCard("fighter2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(fighter, fighter2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, fighter}, new PhysicalCardImpl[]{gimli, fighter2});
		scn.SetTwilight(5);

		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());

		// At the start of Aragorn's skirmish only the copy in it offers.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fighter));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(fighter2));
		scn.ShadowChooseOptionalTrigger(fighter);
		assertEquals(4, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();

		// At the start of Gimli's skirmish only the other copy offers; declined, nothing changes.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fighter2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(fighter));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(4, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoTwilightToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fighter = scn.GetShadowCard("fighter");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(fighter);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, fighter);
		scn.SetTwilight(0);

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(fighter));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsOfferedAgainAtTheStartOfItsSecondSkirmishOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fighter = scn.GetShadowCard("fighter");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(fighter);
		// Fierce, so that it skirmishes a second time in the turn: no printed card makes a [raider] Man fierce
		// away from site 9.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, fighter, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, fighter);
		scn.SetTwilight(5);

		assertEquals(0, scn.GetThreats());
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fighter));
		scn.ShadowChooseOptionalTrigger(fighter);
		assertEquals(4, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());

		// 8 against 8: Aragorn loses and takes a wound; the minion lives on into the fierce round.
		scn.PassSkirmishActions();
		assertInPlay(fighter);
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, fighter);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fighter));
		scn.ShadowChooseOptionalTrigger(fighter);
		assertEquals(3, scn.GetTwilight());
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
