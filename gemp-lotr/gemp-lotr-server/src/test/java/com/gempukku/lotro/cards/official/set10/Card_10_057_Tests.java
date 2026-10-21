package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_057_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("watchman", "10_57");
					// A second Watchman, so that two of them can skirmish one companion.
					put("watchman2", "10_57");

					// Cirith Ungol Soldier: another [wraith] Orc, strength 8, vitality 2.  Its Shadow ability is never used.
					put("soldier", "10_55");

					// Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4.  A maneuver ability, never used.
					put("aragorn", "1_89");
					// Ranger's Sword: a possession, strength +2, "He is damage +1".  On Aragorn.
					put("sword", "1_112");

					// Gimli, Son of Gloin: a [dwarven] Dwarf, strength 6, printed damage +1.  "Skirmish: Exert Gimli to
					// make him strength +2": a strength bonus that is not from a possession.
					put("gimli", "1_13");
					// Dwarven Axe: a possession, strength +2.  Its trigger on a minion losing to its bearer is not reached.
					put("axe", "1_9");
					// Ring of Accretion: an ARTIFACT, strength +1 and "Bearer is damage +1".  Its maneuver ability is never used.
					put("ring", "9_6");

					// Thrarin, Dwarven Smith: a Dwarf ALLY, strength 4, vitality 2.  "Maneuver: Exert Thrarin to allow
					// him to participate in archery fire and skirmishes until the regroup phase."
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CirithUngolWatchmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Cirith Ungol Watchman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: Each companion skirmishing this minion loses all <b>damage</b> and strength bonuses from possessions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("watchman");

		assertEquals("Cirith Ungol Watchman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ACompanionSkirmishingTheWatchmanLosesTheDamageAndStrengthBonusesOfHisPossessionsUntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		var watchman = scn.GetShadowCard("watchman");

		scn.StartGame();
		scn.MoveMinionsToTable(watchman);

		assertAttachedTo(sword, aragorn);
		// 8 + 2 from the Sword, and the Sword's damage +1.
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, watchman);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The Watchman's 9 beats Aragorn's 8: one wound, and he lives.
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		// No longer skirmishing the Watchman, he has the Sword's bonuses again.
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
	}

	@Test
	public void OnlyTheWatchmansOwnSkirmishTakesAwayOnlyTheBonusesOfPossessions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var ring = scn.GetFreepsCard("ring");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(gimli, axe, ring);

		var watchman = scn.GetShadowCard("watchman");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(watchman, soldier);

		assertAttachedTo(sword, aragorn);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(ring, gimli);
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		// 6 + 2 from the Axe + 1 from the Ring; damage +1 printed and +1 from the Ring.
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, soldier},
				new PhysicalCardImpl[]{gimli, watchman});

		// Assigned to the Watchman, Gimli is not yet skirmishing him.
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn is skirmishing another [wraith] Orc, not this minion, and keeps the Sword's bonuses.  Gimli is
		// still waiting for his own skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// Aragorn's 10 beats the Soldier's 8, and his damage +1 kills it (vitality 2).
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// The Axe's +2 is gone; the Ring is an artifact, and its +1 and damage +1 stay, as does his printed damage +1.
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// A strength bonus that is not from a possession is not lost.
		scn.FreepsUseCardAction(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(9, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnAllySkirmishingTheWatchmanKeepsTheStrengthBonusOfHisPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(thrarin, axe);

		var watchman = scn.GetShadowCard("watchman");

		scn.StartGame();
		scn.MoveMinionsToTable(watchman);

		assertAttachedTo(axe, thrarin);
		// 4 + 2 from the Axe.
		assertEquals(6, scn.GetStrength(thrarin));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, watchman);
		scn.FreepsResolveSkirmish(thrarin);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// An ally is no companion: the Axe's +2 stays.
		assertEquals(6, scn.GetStrength(thrarin));
	}

	@Test
	public void TwoWatchmenSkirmishingOneCompanionTakeHisBonusesOnlyOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		var watchman = scn.GetShadowCard("watchman");
		var watchman2 = scn.GetShadowCard("watchman2");

		scn.StartGame();
		scn.MoveMinionsToTable(watchman, watchman2);

		assertAttachedTo(sword, aragorn);
		// 8 + 2 from the Sword, and the Sword's damage +1.
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, watchman);
		scn.ShadowAssignToMinions(aragorn, watchman2);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// The Sword's +2 is lost once, not twice: 8, not 6.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
	}
}
