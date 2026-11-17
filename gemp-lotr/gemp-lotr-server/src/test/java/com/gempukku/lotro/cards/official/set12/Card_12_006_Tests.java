package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_006_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("skill", "12_6");
					put("skill2", "12_6");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3, resistance 6; a skirmish ability used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, home 3F, strength 3, resistance 0 as every ally; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose

					put("runner", "1_178");     // Goblin Runner: Orc, strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DwarvenSkillStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Dwarven Skill
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Toil 2</b>. <helper>(For each [dwarven] character you exert when playing this, its twilight cost is -2.)</helper><br><b>Skirmish:</b> Discard this condition to make a Dwarf strength +3 (or +4 if he has resistance 4 or more).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("skill");

		assertEquals("Dwarven Skill", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.TOIL));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToilTakesTwoOffItsCostForTheDwarvenCharacterExertedAsItIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skill = scn.GetFreepsCard("skill");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(skill);

		scn.StartGame();
		assertInHand(skill);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.FreepsPlayCard(skill);
		// The [dwarven] characters who can exert, the ally among them, and no other.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(gimli);

		// 3 less 2 for Gimli's exertion
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertInZone(Zone.SUPPORT, skill);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsThisCopyToMakeADwarfOfResistance3Plus3ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skill = scn.GetFreepsCard("skill");
		var skill2 = scn.GetFreepsCard("skill2");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(skill, skill2);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, skill, skill2);
		// Three burdens: each companion's resistance is 3 less, Gimli's 6 - 3.
		scn.AddBurdens(3);
		assertEquals(3, scn.GetResistance(gimli));
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Not a skirmish phase: not offered among the assignment actions.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(skill));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runner}, new PhysicalCardImpl[]{aragorn, runner2});
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.FreepsActionAvailable(skill));

		scn.FreepsUseCardAction(skill);
		// A Dwarf in the skirmish and a Dwarf in none are offered; companions who are no Dwarf are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		// 6 + 3: resistance 3 is short of 4.
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(3, scn.GetStrength(grimir));
		assertInDiscard(skill);
		assertInZone(Zone.SUPPORT, skill2);

		// Still +3 at the next decision of the skirmish.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(gimli));

		// Gimli beats the Runner; in the next skirmish, a phase of its own, he is 6 again.
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(6, scn.GetStrength(gimli));
		assertInZone(Zone.SUPPORT, skill2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ADwarfOfResistance4IsPlus4AndAnAllyOfResistance0IsPlus3BesideHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skill = scn.GetFreepsCard("skill");
		var skill2 = scn.GetFreepsCard("skill2");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(skill, skill2);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, skill, skill2);
		// Two burdens: Gimli's resistance is 6 - 2.
		scn.AddBurdens(2);
		assertEquals(4, scn.GetResistance(gimli));
		assertEquals(0, scn.GetResistance(grimir));
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(3, scn.GetStrength(grimir));

		scn.FreepsUseCardAction(skill);
		scn.FreepsChooseCard(gimli);
		// 6 + 4
		assertEquals(10, scn.GetStrength(gimli));
		assertInDiscard(skill);

		// The ally has resistance 0 (CR 4.2 "resistance"), while Gimli beside him has 4: 3 + 3.
		scn.ShadowPass();
		scn.FreepsUseCardAction(skill2);
		scn.FreepsChooseCard(grimir);
		assertEquals(6, scn.GetStrength(grimir));
		assertEquals(10, scn.GetStrength(gimli));
		assertInDiscard(skill2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoDwarfInPlayItIsOfferedAndDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skill = scn.GetFreepsCard("skill");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(skill);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, skill);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsActionAvailable(skill));

		scn.FreepsUseCardAction(skill);
		assertInDiscard(skill);
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
