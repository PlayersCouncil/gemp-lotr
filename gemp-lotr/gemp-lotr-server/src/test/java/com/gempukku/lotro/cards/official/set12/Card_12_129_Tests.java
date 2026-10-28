package com.gempukku.lotro.cards.official.set12;

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

public class Card_12_129_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rosie", "12_129");
					// Sam, Son of Hamfast: [shire] Hobbit companion, strength 3, vitality 4; a fellowship ability and a
					// response to Frodo's death, neither used here
					put("sam", "1_311");
					// Merry, From O'er the Brandywine: [shire] Hobbit companion who is no Sam, strength 3 (+2 only
					// while he bears a weapon, and he bears none here)
					put("merry", "1_303");
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5; "When you play" only
					put("runner2", "1_178");
					// Uruk Pursuer: fierce [isengard] Uruk-hai, strength 5, vitality 3; its other texts reach no test here
					put("pursuer", "4_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RosieCottonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Rosie Cotton, Barmaid
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 2
		 * Vitality: 3
		 * Resistance: 8
		 * Game Text: Sam cannot be overwhelmed unless his strength is tripled.<br>Each time the Free Peoples player assigns Rosie Cotton to a skirmish, add 2 burdens.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rosie");

		assertEquals("Rosie Cotton", card.getBlueprint().getTitle());
		assertEquals("Barmaid", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getResistance());
	}

	@Test
	public void SamIsNotOverwhelmedAtDoubleStrengthAndAnotherHobbitIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rosie = scn.GetFreepsCard("rosie");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(rosie, sam, merry);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		// 5 + 3: at least double a strength of 3, and one short of triple.
		scn.ApplyAdHocModifier(new StrengthModifier(null, runner1, null, 3));
		scn.ApplyAdHocModifier(new StrengthModifier(null, runner2, null, 3));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, runner1}, new PhysicalCardImpl[]{merry, runner2});

		scn.FreepsResolveSkirmish(sam);
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(8, scn.GetStrength(runner1));
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.PassSkirmishActions();
		// Spared the overwhelm: he takes the one wound for losing.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertInPlay(sam);

		// Merry, a Hobbit who is no Sam, loses to the same 8 and is overwhelmed.
		scn.FreepsResolveSkirmish(merry);
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(8, scn.GetStrength(runner2));
		assertEquals(0, scn.GetFreepsDeadCount());
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, merry);
		assertEquals(1, scn.GetFreepsDeadCount());

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SamIsOverwhelmedAtTripleStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rosie = scn.GetFreepsCard("rosie");
		var sam = scn.GetFreepsCard("sam");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(rosie, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		// 5 + 4: triple a strength of 3.
		scn.ApplyAdHocModifier(new StrengthModifier(null, runner1, null, 4));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner1);

		scn.FreepsResolveSkirmish(sam);
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(9, scn.GetStrength(runner1));
		assertEquals(0, scn.GetFreepsDeadCount());
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, sam);
		assertEquals(1, scn.GetFreepsDeadCount());

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachTimeTheFreePeoplesPlayerAssignsRosieTwoBurdensAreAddedTheFierceRoundIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rosie = scn.GetFreepsCard("rosie");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(rosie);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		// Strength 3 by hand, so that the Pursuer's 5 does not overwhelm her and she lives to be assigned again.
		scn.ApplyAdHocModifier(new StrengthModifier(null, rosie, null, 1));
		scn.SkipToAssignments();
		assertEquals(0, scn.GetBurdens());

		scn.FreepsAssignToMinions(rosie, pursuer);
		assertEquals(2, scn.GetBurdens());

		scn.FreepsResolveSkirmish(rosie);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(rosie));
		assertEquals(2, scn.GetBurdens());

		// The fierce round is a second assignment: assigned again, two more.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(rosie, pursuer);
		assertEquals(4, scn.GetBurdens());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AnotherCompanionAssignedOrRosieAssignedByTheShadowPlayerAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rosie = scn.GetFreepsCard("rosie");
		var sam = scn.GetFreepsCard("sam");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(rosie, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		assertEquals(0, scn.GetBurdens());

		// The Free Peoples player assigns Sam and leaves the second Runner over.
		scn.FreepsAssignToMinions(sam, runner1);
		assertEquals(0, scn.GetBurdens());

		// The leftover minion is the Shadow player's to assign, and he assigns it to Rosie.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		scn.ShadowAssignToMinions(rosie, runner2);
		assertTrue(scn.IsCharAssignedAgainst(rosie, runner2));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
