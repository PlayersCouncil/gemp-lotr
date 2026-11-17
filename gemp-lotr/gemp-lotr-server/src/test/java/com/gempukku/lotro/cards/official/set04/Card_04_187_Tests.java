package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_187_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("soldier", "4_187");
					put("soldier2", "4_187");
					put("merry", "1_303");      // Merry, From O'er the Brandywine: strength 3, vitality 4; +2 only while bearing a weapon (none here)
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("seeker", "4_195");     // Uruk Seeker: printed fierce, strength 6; +2 and damage +1 only against a bearer of a search card
					put("destroyer", "17_124"); // White Hand Destroyer: printed fierce, strength 10; hunter 3 only for each site controlled (none)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukFootSoldierStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk Foot Soldier
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 5
		 * Vitality: 1
		 * Site Number: 5
		 * Game Text: <b>Tracker</b>. <b>Fierce</b>.
		 * 	During a fierce skirmish involving this minion, it is strength +3 and <b>damage +1</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("soldier");

		assertEquals("Uruk Foot Soldier", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlus3AndDamagePlus1InHisFierceSkirmishAndNotInHisRegularSkirmishOrAfterIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);

		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		assertEquals(5, scn.GetStrength(soldier));
		assertEquals(0, scn.GetKeywordCount(soldier, Keyword.DAMAGE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, soldier);
		scn.FreepsResolveSkirmish(merry);

		// His regular skirmish: no bonus.
		assertFalse(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(merry, soldier));
		assertEquals(5, scn.GetStrength(soldier));
		assertEquals(0, scn.GetKeywordCount(soldier, Keyword.DAMAGE));
		scn.PassSkirmishActions();
		// 5 against 3, not doubled: Merry loses and takes one wound.
		assertEquals(1, scn.GetWoundsOn(merry));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(merry, soldier);
		scn.FreepsResolveSkirmish(merry);

		// His fierce skirmish: 5 + 3, and damage +1.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(merry, soldier));
		assertEquals(8, scn.GetStrength(soldier));
		assertEquals(1, scn.GetKeywordCount(soldier, Keyword.DAMAGE));
		scn.PassSkirmishActions();
		// 8 against 3: Merry is overwhelmed.
		assertInZone(Zone.DEAD, merry);

		// The fierce skirmish over, he is back to 5 and no damage bonus.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(5, scn.GetStrength(soldier));
		assertEquals(0, scn.GetKeywordCount(soldier, Keyword.DAMAGE));
	}

	@Test
	public void OnlyTheMinionsOwnFierceSkirmishCountsAndTheBonusIsHisAloneAndDoesNotStackWithASecondCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var soldier = scn.GetShadowCard("soldier");
		var soldier2 = scn.GetShadowCard("soldier2");
		var seeker = scn.GetShadowCard("seeker");
		var destroyer = scn.GetShadowCard("destroyer");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, soldier2, seeker, destroyer);

		// No regular skirmishes.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, soldier},
				new PhysicalCardImpl[]{gimli, seeker});
		scn.ShadowAssignToMinions(aragorn, soldier2, destroyer);

		// Another minion's fierce skirmish: neither copy is in it, and neither has the bonus.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, seeker));
		assertEquals(5, scn.GetStrength(soldier));
		assertEquals(5, scn.GetStrength(soldier2));
		assertEquals(0, scn.GetKeywordCount(soldier, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(soldier2, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(seeker));
		assertEquals(0, scn.GetKeywordCount(seeker, Keyword.DAMAGE));
		scn.PassSkirmishActions();

		// Their own fierce skirmish: each copy is 5 + 3 (not + 6) and damage +1 (not +2), and the other fierce
		// minion in it keeps its 10 and no damage bonus.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, soldier));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, soldier2));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, destroyer));
		assertEquals(8, scn.GetStrength(soldier));
		assertEquals(8, scn.GetStrength(soldier2));
		assertEquals(1, scn.GetKeywordCount(soldier, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(soldier2, Keyword.DAMAGE));
		assertEquals(10, scn.GetStrength(destroyer));
		assertEquals(0, scn.GetKeywordCount(destroyer, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
