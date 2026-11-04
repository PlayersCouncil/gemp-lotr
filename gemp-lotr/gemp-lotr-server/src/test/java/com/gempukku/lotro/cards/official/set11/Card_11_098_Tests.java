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

public class Card_11_098_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("easterling", "11_98");
					put("easterling2", "11_98");
					put("legion", "11_88");     // Legion of Harad: a [men] Man minion, strength 14, vitality 3; toil only
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, strength 3; his abilities are
					// used only on purpose
					put("ted", "18_75");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc minion, strength 13, no text
					// Crazed Hillman: a [men] Man printed fierce, strength 7 (+7 in a fierce skirmish against a
					// companion of resistance 5 or more)
					put("hillman", "12_60");
					// Black Rider: a [wraith] Nazgul printed fierce, strength 8 +1 for each companion
					put("rider", "12_161");

					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, resistance 6, no other text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; his ability is never used
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3; his abilities are used only on purpose
					// Ranger of Osgiliath: strength 6, vitality 3, resistance 6; its regroup ability is never used
					put("osgiliath", "7_116");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RampagingEasterlingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Rampaging Easterling
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br>Each time another [men] minion wins a skirmish, make this minion strength +3 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("easterling");

		assertEquals("Rampaging Easterling", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachWinOfAMenManOrHobbitAddsThreeUntilTheRegroupPhaseAndAWinOfAnotherCultureNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var easterling = scn.GetShadowCard("easterling");
		var easterling2 = scn.GetShadowCard("easterling2");
		var troop = scn.GetShadowCard("troop");
		var legion = scn.GetShadowCard("legion");
		var ted = scn.GetShadowCard("ted");
		var knight = scn.GetFreepsCard("knight");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var osgiliath = scn.GetFreepsCard("osgiliath");
		scn.MoveCompanionsToTable(knight, aragorn, sam, osgiliath);

		scn.StartGame();
		scn.MoveMinionsToTable(easterling, easterling2, troop, legion, ted);
		scn.SkipToAssignments();
		// The second copy, a lurker, waits for the Ranger, so its skirmish comes last; the first is assigned to none.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, troop}, new PhysicalCardImpl[]{aragorn, legion},
				new PhysicalCardImpl[]{sam, ted}, new PhysicalCardImpl[]{osgiliath, easterling2});
		scn.ShadowDeclineAssignments();

		// The [moria] Troop, 13, beats the Knight, 6: no bonus.
		assertEquals(6, scn.GetStrength(easterling));
		assertEquals(6, scn.GetStrength(easterling2));
		scn.FreepsResolveSkirmish(knight);
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish"));
		assertEquals(6, scn.GetStrength(easterling));
		assertEquals(6, scn.GetStrength(easterling2));

		// The Legion, a [men] Man, 14, beats Aragorn, 8: +3 on each copy.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// The loser's wound and the trigger answer the win together.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish"));
		assertEquals(9, scn.GetStrength(easterling));
		assertEquals(9, scn.GetStrength(easterling2));

		// Ted, a [men] Hobbit, 3, beats Sam, 3 (a tie goes to the Shadow): +3 more, with the lurker's skirmish
		// still to come.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		// The loser's wound and the trigger answer the win together.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish"));
		assertEquals(12, scn.GetStrength(easterling));
		assertEquals(12, scn.GetStrength(easterling2));

		// The second copy, 12, beats the Ranger, 6, in the last skirmish, and the regroup phase begins: both
		// bonuses are gone.
		scn.FreepsResolveSkirmish(osgiliath);
		assertEquals(12, scn.GetStrength(easterling2));
		scn.PassSkirmishActions();
		// Its own win sets off the first copy's trigger beside the loser's wound.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(6, scn.GetStrength(easterling));
		assertEquals(6, scn.GetStrength(easterling2));
	}

	@Test
	public void ItsOwnWinAndALossOfAMenMinionAddNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var easterling = scn.GetShadowCard("easterling");
		var easterling2 = scn.GetShadowCard("easterling2");
		var legion = scn.GetShadowCard("legion");
		var knight = scn.GetFreepsCard("knight");
		var aragorn = scn.GetFreepsCard("aragorn");
		var osgiliath = scn.GetFreepsCard("osgiliath");
		scn.MoveCompanionsToTable(knight, aragorn, osgiliath);

		scn.StartGame();
		scn.MoveMinionsToTable(easterling, easterling2, legion);
		// Only arranges the board: Aragorn, 8 + 14, beats the Legion, 14, without killing it.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 14));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, legion}, new PhysicalCardImpl[]{knight, easterling},
				new PhysicalCardImpl[]{osgiliath, easterling2});
		scn.ShadowDeclineAssignments();

		// The Legion loses and lives: no bonus.
		assertEquals(0, scn.GetWoundsOn(legion));
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(legion));
		assertInZone(Zone.SHADOW_CHARACTERS, legion);
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish"));
		assertEquals(6, scn.GetStrength(easterling));
		assertEquals(6, scn.GetStrength(easterling2));

		// The first copy, 6, beats the Knight, 6: the other copy is +3, and the winner is not.
		scn.FreepsResolveSkirmish(knight);
		scn.PassSkirmishActions();
		// The loser's wound and the trigger answer the win together.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish"));
		assertEquals(6, scn.GetStrength(easterling));
		assertEquals(9, scn.GetStrength(easterling2));
	}

	@Test
	public void TheSameMenMinionWinningInEachRoundAddsThreeEachTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var easterling = scn.GetShadowCard("easterling");
		var hillman = scn.GetShadowCard("hillman");
		var rider = scn.GetShadowCard("rider");
		var knight = scn.GetFreepsCard("knight");
		var aragorn = scn.GetFreepsCard("aragorn");
		var osgiliath = scn.GetFreepsCard("osgiliath");
		scn.MoveCompanionsToTable(knight, aragorn, osgiliath);

		scn.StartGame();
		scn.MoveMinionsToTable(easterling, hillman, rider);
		scn.SkipToAssignments();
		// Only the Hillman fights in the first round; the Easterling and the Rider are assigned to none.
		scn.FreepsAssignToMinions(knight, hillman);
		scn.ShadowDeclineAssignments();

		// The Hillman, 7, beats the Knight, 6.
		assertEquals(6, scn.GetStrength(easterling));
		scn.FreepsResolveSkirmish(knight);
		scn.PassSkirmishActions();
		// The loser's wound and the trigger answer the win together.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(knight));

		// Still in force in the fierce round.
		assertTrue(scn.InFierceRound());
		assertEquals(9, scn.GetStrength(easterling));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{osgiliath, hillman}, new PhysicalCardImpl[]{aragorn, rider});
		scn.ShadowDeclineAssignments();

		// The Hillman, 7 + 7 in a fierce skirmish, beats the Ranger, 6, again: +3 more, with the Rider's skirmish
		// still to come.
		scn.FreepsResolveSkirmish(osgiliath);
		scn.PassFierceSkirmishActions();
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish"));
		assertEquals(12, scn.GetStrength(easterling));

		// The Rider's skirmish is the last; the regroup phase begins and the bonus is gone.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(6, scn.GetStrength(easterling));
	}
}
