package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_076_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guardian", "4_76");
					put("guardian2", "4_76");

					put("swordsman", "3_98");   // Orc Swordsman: [sauron] Orc, strength 9, vitality 3; +2 strength with a weapon
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1 (exhausted as it stands); its "when you play" is not reached when cheated in
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, vitality 3, damage +1 and nothing else
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LorienGuardianStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Lórien Guardian
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Regroup: Exert this companion to make a Shadow player wound a minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("guardian");

		assertEquals("Lórien Guardian", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void RegroupAbilityExertsThisCompanionToMakeTheShadowPlayerWoundAMinionOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guardian = scn.GetFreepsCard("guardian");
		var guardian2 = scn.GetFreepsCard("guardian2");
		scn.MoveCompanionsToTable(guardian, guardian2);

		var swordsman = scn.GetShadowCard("swordsman");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(swordsman, troop, runner);
		// The minion chosen carries a wound already, so that one wound can be told from two.
		scn.AddWoundsToChar(swordsman, 1);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(guardian));
		assertEquals(0, scn.GetWoundsOn(guardian2));
		assertEquals(1, scn.GetWoundsOn(swordsman));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.FreepsActionAvailable(guardian));

		scn.FreepsUseCardAction(guardian);
		assertEquals(1, scn.GetWoundsOn(guardian));
		assertEquals(0, scn.GetWoundsOn(guardian2));

		// The Shadow player picks the minion, from his minions of either culture, and must pick one.  The Goblin
		// Runner, with 1 vitality, cannot exert and is offered all the same: it is a wound.
		assertTrue(scn.ShadowHasCardChoiceAvailable(swordsman, troop, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guardian, guardian2, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(swordsman);
		assertEquals(2, scn.GetWoundsOn(swordsman));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(1, scn.GetWoundsOn(guardian));
		assertEquals(0, scn.GetWoundsOn(guardian2));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedOutsideRegroupOrWhenThisCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guardian = scn.GetFreepsCard("guardian");
		var guardian2 = scn.GetFreepsCard("guardian2");
		scn.MoveCompanionsToTable(guardian, guardian2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(guardian));
		assertFalse(scn.FreepsActionAvailable(guardian2));

		// Vitality 3 with 2 wounds: exhausted, so this Guardian cannot exert. The other copy can.
		scn.AddWoundsToChar(guardian, 2);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(guardian));
		assertTrue(scn.FreepsActionAvailable(guardian2));
	}

	@Test
	public void AnExhaustedMinionChosenIsKilledByTheWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guardian = scn.GetFreepsCard("guardian");
		scn.MoveCompanionsToTable(guardian);

		var runner = scn.GetShadowCard("runner");
		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(runner, raiders);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertTrue(scn.FreepsActionAvailable(guardian));
		scn.FreepsUseCardAction(guardian);
		assertEquals(1, scn.GetWoundsOn(guardian));

		// An [isengard] Uruk-hai and a [moria] Orc.  The Runner has 1 vitality: one wound kills it.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, raiders));
		scn.ShadowChooseCard(runner);
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoMinionInPlayTheAbilityIsOfferedAndTheExertionIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guardian = scn.GetFreepsCard("guardian");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(guardian, aragorn);

		scn.StartGame();
		// No minion is played: the game goes from the Shadow phase straight to regroup.
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(guardian));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsActionAvailable(guardian));
		scn.FreepsUseCardAction(guardian);

		// The exertion is paid; there is no minion, and no companion is wounded in its place.
		assertEquals(1, scn.GetWoundsOn(guardian));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
