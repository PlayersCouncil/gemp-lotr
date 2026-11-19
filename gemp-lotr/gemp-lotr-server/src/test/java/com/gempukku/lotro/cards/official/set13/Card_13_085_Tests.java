package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_085_Tests
{

	// The skirmishes are fought at Ettenmoors 1_331, site 2; its skirmish ability is used only on purpose.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dunlending", "13_85");
					put("dunlending2", "13_85");
					put("dunlending3", "13_85");
					put("runner", "13_90");     // Easterling Runner: [men] Man minion, strength 8, vitality 2; a Shadow ability only
					put("saruman", "17_48");    // Saruman, Coldly Still: [men] Wizard minion; +2 only to [men] minions bearing a possession
					put("hillman", "12_60");    // Crazed Hillman: [men] Man minion, printed fierce; stronger only in a fierce skirmish
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, no text
					put("stragglers", "13_99"); // Stragglers: [men] support-area condition; its skirmish ability is used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: his abilities are used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CruelDunlendingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Cruel Dunlending
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Skirmish:</b> If this minion is not assigned to a skirmish, you may exert him to make another [men] minion <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dunlending");

		assertEquals("Cruel Dunlending", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void UnassignedHeExertsToMakeAnotherMenMinionFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var dunlending = scn.GetShadowCard("dunlending");
		var dunlending2 = scn.GetShadowCard("dunlending2");
		var runner = scn.GetShadowCard("runner");
		var saruman = scn.GetShadowCard("saruman");
		var hillman = scn.GetShadowCard("hillman");
		var brigand = scn.GetShadowCard("brigand");
		var stragglers = scn.GetShadowCard("stragglers");
		scn.MoveCardsToSupportArea(stragglers);

		scn.StartGame();
		scn.MoveMinionsToTable(dunlending, dunlending2, runner, saruman, hillman, brigand);
		scn.SkipToAssignments();
		assertInZone(Zone.SUPPORT, stragglers);
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(dunlending));
		assertEquals(0, scn.GetWoundsOn(dunlending2));
		assertFalse(scn.HasKeyword(runner, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable(dunlending));
		scn.ShadowUseCardAction(dunlending);

		// Another [men] minion: of either race, in this skirmish or in none, fierce already or not, the other copy
		// too; not a [dunland] Man, a [men] condition, himself or a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, saruman, hillman, dunlending2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand, stragglers, dunlending, aragorn, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(runner);

		assertEquals(1, scn.GetWoundsOn(dunlending));
		assertEquals(0, scn.GetWoundsOn(dunlending2));
		assertTrue(scn.HasKeyword(runner, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(saruman, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 8 against 8: the tie goes to the Runner, who lives to fight again in the fierce round.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.HasKeyword(runner, Keyword.FIERCE));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		// Still fierce in his fierce skirmish, just before the regroup phase.
		assertTrue(scn.HasKeyword(runner, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertFalse(scn.HasKeyword(runner, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileAssignedToASkirmishOrExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var dunlending = scn.GetShadowCard("dunlending");
		var dunlending2 = scn.GetShadowCard("dunlending2");
		var dunlending3 = scn.GetShadowCard("dunlending3");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(dunlending, dunlending2, dunlending3, runner);
		// Vitality 2: one wound exhausts the second copy.
		scn.AddWoundsToChar(dunlending2, 1);
		scn.SkipToAssignments();
		assertEquals(0, scn.GetWoundsOn(dunlending));
		assertEquals(1, scn.GetWoundsOn(dunlending2));
		assertEquals(0, scn.GetWoundsOn(dunlending3));
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner},
				new PhysicalCardImpl[]{sam, dunlending});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The first copy is assigned to Sam, whose skirmish is still to come; the second cannot exert.  The third,
		// unassigned and unwounded, can.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(dunlending));
		assertFalse(scn.ShadowActionAvailable(dunlending2));
		assertTrue(scn.ShadowActionAvailable(dunlending3));
		assertEquals(0, scn.GetWoundsOn(dunlending));
		assertEquals(1, scn.GetWoundsOn(dunlending2));
	}

	@Test
	public void WithNoOtherMenMinionHeIsStillOfferedAndExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var dunlending = scn.GetShadowCard("dunlending");
		var brigand = scn.GetShadowCard("brigand");
		var stragglers = scn.GetShadowCard("stragglers");
		scn.MoveCardsToSupportArea(stragglers);

		scn.StartGame();
		scn.MoveMinionsToTable(dunlending, brigand);
		scn.SkipToAssignments();
		assertInZone(Zone.SUPPORT, stragglers);
		scn.FreepsAssignToMinions(aragorn, brigand);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The only other minion is a [dunland] Man and the only other [men] card a condition: nothing to choose.
		assertEquals(0, scn.GetWoundsOn(dunlending));
		assertFalse(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(dunlending, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable(dunlending));
		scn.ShadowUseCardAction(dunlending);

		assertEquals(1, scn.GetWoundsOn(dunlending));
		assertFalse(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(dunlending, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
