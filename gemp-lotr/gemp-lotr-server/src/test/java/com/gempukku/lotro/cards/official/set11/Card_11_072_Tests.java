package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_072_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("column", "11_72");
					put("column2", "11_72");
					// Corps of Harad: [men] Man minion, strength 10; fierce only at a battleground, and this path has none
					put("corps", "11_73");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, strength 3; his abilities are
					// used only on purpose
					put("ted", "18_75");
					put("soldier", "4_254");    // Southron Soldier: a [raider] Man minion, strength 4; Southron and ambush (2) only
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, strength 13, with no text
					// Goaded to War: a [men] condition in the support area; its trigger answers a move to a
					// battleground or plains, and no move is made after it is laid
					put("goaded", "12_67");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability
					put("guard", "1_7");        // Dwarf Guard: a companion with no text once in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ColumnofEasterlingsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Column of Easterlings
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br><b>Skirmish:</b> Discard this minion to make a [men] minion strength +4.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("column");

		assertEquals("Column of Easterlings", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityDiscardsItselfToMakeAMenMinionStrengthPlus4UntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var column = scn.GetShadowCard("column");
		var column2 = scn.GetShadowCard("column2");
		var corps = scn.GetShadowCard("corps");
		var ted = scn.GetShadowCard("ted");
		var soldier = scn.GetShadowCard("soldier");
		var goaded = scn.GetShadowCard("goaded");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");

		scn.MoveCompanionsToTable(aragorn, guard);
		scn.StartGame();
		scn.MoveMinionsToTable(column, column2, corps, ted, soldier);
		scn.MoveCardsToSupportArea(goaded);
		assertInZone(Zone.SUPPORT, goaded);

		scn.SkipToAssignments();
		// The second Column is a lurker, so the Guard's skirmish comes after Aragorn's.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, corps}, new PhysicalCardImpl[]{guard, column2});
		// The first Column, Ted and the Soldier stay out of any skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(10, scn.GetStrength(corps));
		assertEquals(3, scn.GetStrength(ted));
		assertEquals(8, scn.GetStrength(column2));
		assertEquals(4, scn.GetStrength(soldier));
		assertTrue(scn.ShadowActionAvailable(column));

		scn.ShadowUseCardAction(column);

		// The copy used is the one discarded; the other stays, and is a [men] minion.
		assertInDiscard(column);
		assertInZone(Zone.SHADOW_CHARACTERS, column2);
		// [men] minions in this skirmish, in the other one and in none, a Man and a Hobbit; not a Man of
		// another culture nor a [men] card that is no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(corps, column2, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(soldier, goaded));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(corps);

		assertEquals(14, scn.GetStrength(corps));
		assertEquals(3, scn.GetStrength(ted));
		assertEquals(8, scn.GetStrength(column2));
		assertEquals(4, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Still +4 at the next decision of this skirmish.
		scn.FreepsPass();
		assertEquals(14, scn.GetStrength(corps));
		scn.ShadowPass();

		scn.FreepsResolveSkirmish(guard);
		// The next skirmish is a phase of its own: the Corps is back to its printed 10.
		assertEquals(10, scn.GetStrength(corps));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoOtherMenMinionItIsStillOfferedAndDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var column = scn.GetShadowCard("column");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.MoveMinionsToTable(column, troop, soldier);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(13, scn.GetStrength(troop));
		assertEquals(4, scn.GetStrength(soldier));
		assertTrue(scn.ShadowActionAvailable(column));

		scn.ShadowUseCardAction(column);

		assertInDiscard(column);
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(4, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var column = scn.GetShadowCard("column");
		var corps = scn.GetShadowCard("corps");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.MoveMinionsToTable(column, corps);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.ShadowDecisionAvailable("Play Assignment action or Pass"));
		assertFalse(scn.ShadowActionAvailable(column));
	}
}
