package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_076_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wall", "11_76");
					put("wall2", "11_76");
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] Hobbit minion, strength 3, vitality 4; his abilities are used only on purpose
					put("men", "11_100");       // Strange-looking Men: a [men] Man minion, strength 10, vitality 2; its ability is used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, strength 13, vitality 3, no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; his ability is used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: for Long I Fell
					put("longifell", "4_97");   // Long I Fell: Skirmish: spot Gandalf to prevent all wounds to him; the Shadow player may make you wound a minion to prevent this
					put("legolas", "1_50");     // Legolas, Greenleaf: an archer; his ability is used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EasterlingShieldWallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Easterling Shield Wall
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper>
		 * 	<b>Response:</b> If a [men] minion is about to take a wound in a skirmish, exert this minion and remove (1) to prevent that.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wall");

		assertEquals("Easterling Shield Wall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ExertsThisCopyAndRemovesOneToPreventTheWoundOfAMenMinionAmongTheLosersOfASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wall = scn.GetShadowCard("wall");
		var wall2 = scn.GetShadowCard("wall2");
		var ted = scn.GetShadowCard("ted");
		var men = scn.GetShadowCard("men");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(wall, wall2, ted, men, troop);
		// Aragorn, 8 + 30, beats the three together, 3 + 10 + 13 = 26, and does not double them: all three lose
		// and are wounded together.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 30));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, ted);
		scn.ShadowAssignToMinions(aragorn, men, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);

		assertEquals(38, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(wall));
		assertEquals(0, scn.GetWoundsOn(wall2));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(men));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(3, scn.GetTwilight());
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wall));
		scn.ShadowChooseOptionalTrigger(wall);
		assertEquals(1, scn.GetWoundsOn(wall));
		assertEquals(0, scn.GetWoundsOn(wall2));
		assertEquals(2, scn.GetTwilight());

		// The wound to be prevented is a [men] minion's, of either race, and not the [moria] Orc's.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ted, men));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(ted);

		// The Men's wound is still to come: offered again, and declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wall2));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(1, scn.GetWoundsOn(men));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(wall));
		assertEquals(0, scn.GetWoundsOn(wall2));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnswersAWoundToAMenMinionWaitingForItsSkirmishDuringAnotherSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wall = scn.GetShadowCard("wall");
		var ted = scn.GetShadowCard("ted");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var longifell = scn.GetFreepsCard("longifell");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToHand(longifell);

		scn.StartGame();
		scn.MoveMinionsToTable(wall, ted, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, troop}, new PhysicalCardImpl[]{aragorn, ted});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gandalf);
		scn.SetTwilight(3);

		// Ted waits for his skirmish with Aragorn.
		assertTrue(scn.IsCharAssigned(ted));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(wall));
		scn.FreepsPlayCard(longifell);
		// The Shadow player has the Free Peoples player wound a minion, to prevent the spell.
		scn.ShadowChooseYes();
		scn.FreepsChooseCard(ted);

		// Long I Fell's 2 in the pool.
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wall));
		scn.ShadowChooseOptionalTrigger(wall);
		assertEquals(1, scn.GetWoundsOn(wall));
		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(ted));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnOrcLosingAloneIsNotAnsweredAndAMenManLosingAloneIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wall = scn.GetShadowCard("wall");
		var men = scn.GetShadowCard("men");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, gandalf);

		scn.StartGame();
		scn.MoveMinionsToTable(wall, men, troop);
		// Aragorn, 8 + 10, beats the Troop's 13, and Gandalf, 6 + 2 (his own text, for the Hobbit and the Man beside
		// him) + 10, the Men's 10; neither doubles his minion.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 10));
		scn.ApplyAdHocModifier(new StrengthModifier(null, gandalf, null, 10));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{gandalf, men});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);

		assertEquals(18, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(wall));
		scn.PassSkirmishActions();

		// The [moria] Orc's wound is not answered, with the cost payable.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(wall));

		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(18, scn.GetStrength(gandalf));
		assertEquals(0, scn.GetWoundsOn(men));
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wall));
		scn.ShadowChooseOptionalTrigger(wall);
		assertEquals(1, scn.GetWoundsOn(wall));
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(men));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnArcheryWoundIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wall = scn.GetShadowCard("wall");
		var ted = scn.GetShadowCard("ted");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(wall, ted);
		scn.SkipToArcheryWounds();

		assertTrue(scn.GetTwilight() >= 1);
		assertEquals(0, scn.GetWoundsOn(wall));
		assertEquals(0, scn.GetWoundsOn(ted));
		// The fellowship's archery wound, placed by the Shadow player.
		scn.ShadowChooseCard(ted);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(wall));
		assertEquals(1, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(wall));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ExhaustedItCannotPreventAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wall = scn.GetShadowCard("wall");
		var men = scn.GetShadowCard("men");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(wall, men);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(wall, 2);
		assertEquals(2, scn.GetWoundsOn(wall));
		// Aragorn, 8 + 5, beats the Men's 10 and does not double it.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, men);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);

		assertEquals(13, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(men));
		scn.PassSkirmishActions();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(wall));
		assertEquals(1, scn.GetWoundsOn(men));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoTwilightToRemoveItCannotPreventAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wall = scn.GetShadowCard("wall");
		var men = scn.GetShadowCard("men");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(wall, men);
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, men);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(0);

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(wall));
		assertEquals(0, scn.GetWoundsOn(men));
		scn.PassSkirmishActions();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(wall));
		assertEquals(1, scn.GetWoundsOn(men));
		assertEquals(0, scn.GetWoundsOn(wall));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
