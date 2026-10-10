package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_050_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hour", "102_50");

					// Valiant companions of two cultures and two races.
					put("guard", "5_83");        // Household Guard: [rohan] Man, strength 6, vitality 3; "Valiant" is his whole text
					put("haldir", "102_8");      // Haldir, Naith Commander: [elven] Elf, strength 6; his text needs a ranged weapon or an [elven] possession
					// Aragorn, Defender of Rohan: valiant, strength 8, vitality 4, so he can exert and lose two
					// skirmishes.  His text does nothing unless the Ring-bearer is assigned to a skirmish.
					put("aragorn", "17_93");
					// Rider of Rohan: a [rohan] companion who is not valiant, strength 5.  His ability needs a mount.
					put("rider", "4_286");
					// Guma, Plains Farmer: a [rohan] Man who is an ally, vitality 2; a fellowship ability.
					put("guma", "4_277");
					// Northern Signal-fire, Flame of Calenhad: a [rohan] possession of the support area.
					// "Maneuver: Hinder this beacon and exert an unbound Man.  Until the regroup phase, each of
					// your other Men gain each of the unloaded keywords (except race) on that Man."  No ally is
					// valiant in print; this is how one comes to be: a valiant character who is not a companion.
					put("flame", "103_86");

					put("runner1", "1_178");     // Goblin Runner: [moria], strength 5, vitality 1
					put("runner2", "1_178");
					put("savage", "1_151");      // Uruk Savage: [isengard], strength 5, vitality 3, damage +1
					// Ulaire Cantea: fierce, strength 10, vitality 3.  His ability discards a weapon, and nobody bears one.
					put("cantea", "1_230");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LetThisBetheHourStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Let This Be the Hour
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Assignment
		 * Game Text: Exert a valiant companion to make that companion defender +1. That companion is strength +1
		 * for each minion skirmishing them until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hour");

		assertEquals("Let This Be the Hour", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ASSIGNMENT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAValiantCompanionWhoBecomesDefenderPlusOneAndIsStrongerForEachMinionSkirmishingThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var hour = scn.GetFreepsCard("hour");
		var guard = scn.GetFreepsCard("guard");
		var haldir = scn.GetFreepsCard("haldir");
		var rider = scn.GetFreepsCard("rider");
		var guma = scn.GetFreepsCard("guma");
		var flame = scn.GetFreepsCard("flame");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(hour);
		scn.MoveCompanionsToTable(guard, haldir, rider);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToSupportArea(flame);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, savage);

		// In the maneuver phase the Flame of Calenhad has the Household Guard exert, and until the regroup
		// phase the player's other Men have his keyword: the Rider of Rohan, a companion, and Guma, an ally.
		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.HasKeyword(rider, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(guma, Keyword.VALIANT));
		scn.FreepsUseCardAction(flame);
		scn.FreepsChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.HasKeyword(rider, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(guma, Keyword.VALIANT));

		// An assignment event: not to be played in the phase before.
		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(hour));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(hour));
		var pool = scn.GetTwilight();
		assertTrue(scn.HasKeyword(guma, Keyword.VALIANT));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertFalse(scn.HasKeyword(guard, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(haldir, Keyword.DEFENDER));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(haldir));

		// The companions who are valiant, in print or by the Flame; not the ally who is, nor Frodo, who is
		// not.
		scn.FreepsPlayCard(hour);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, haldir, rider));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guma, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		assertInZone(Zone.DISCARD, hour);
		assertEquals(pool + 2, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(guard));
		assertTrue(scn.HasKeyword(guard, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DEFENDER));
		// Nobody is skirmishing him yet.
		assertEquals(6, scn.GetStrength(guard));
		// The valiant companion who was not chosen.
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertFalse(scn.HasKeyword(haldir, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());

		scn.ShadowPass();
		scn.FreepsPass();
		// Defender +1: one companion takes two minions, a [moria] Orc and an [isengard] Uruk-hai.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { guard, runner1, savage },
				new PhysicalCardImpl[] { haldir, runner2 });
		assertTrue(scn.IsCharAssignedAgainst(guard, runner1));
		assertTrue(scn.IsCharAssignedAgainst(guard, savage));

		// Haldir's skirmish first.  The minions assigned to the Guard are not skirmishing him yet, and
		// the minion skirmishing Haldir does nothing for Haldir.
		scn.FreepsResolveSkirmish(haldir);
		assertTrue(scn.IsCharSkirmishingAgainst(haldir, runner2));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(haldir));
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner2);

		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.IsCharSkirmishingAgainst(guard, runner1));
		assertTrue(scn.IsCharSkirmishingAgainst(guard, savage));
		// 6 + 1 for each of the two minions skirmishing him.
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(haldir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DefenderLastsForTheAssignmentPhaseAndTheStrengthBonusUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hour = scn.GetFreepsCard("hour");
		var aragorn = scn.GetFreepsCard("aragorn");
		var cantea = scn.GetShadowCard("cantea");
		scn.MoveCardsToHand(hour);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(cantea);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		assertEquals(8, scn.GetStrength(aragorn));
		// The only valiant companion: he is the one exerted.
		scn.FreepsPlayCard(hour);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.HasKeyword(aragorn, Keyword.DEFENDER));

		// Still defender +1 when the assignments are made, at the end of the assignment phase.
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		scn.FreepsAssignToMinions(aragorn, cantea);

		// The first sentence names no duration, so the defender bonus ends with the assignment phase.
		// The strength bonus goes on: 8 + 1 for the minion skirmishing him.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		assertEquals(9, scn.GetStrength(aragorn));
		// 9 against 10: he loses and takes a wound.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// Cantea is fierce.  In the second skirmish, after one already resolved, the bonus is still there.
		scn.PassFierceAssignmentActions();
		assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		scn.FreepsAssignToMinions(aragorn, cantea);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, cantea));
		assertEquals(9, scn.GetStrength(aragorn));
		scn.PassFierceSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(aragorn));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));

		// The regroup phase has begun and the bonus is over: the fellowship moves again, Cantea is
		// still there, and with him skirmishing Aragorn once more Aragorn is strength 8.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, cantea);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, cantea));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyValiantCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hour = scn.GetFreepsCard("hour");
		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var flame = scn.GetFreepsCard("flame");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(hour);
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToSupportArea(flame);
		scn.AddWoundsToChar(guard, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		// In the maneuver phase the Flame of Calenhad has the Household Guard, the only unbound Man, exert:
		// vitality 3 with 2 wounds, he cannot exert again.  Until the regroup phase Guma, the player's other
		// Man, has his keyword: a valiant ally, who can exert, as Frodo can, who is not valiant.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(flame);
		assertTrue(scn.IsExhausted(guard));
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.HasKeyword(guard, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(guma, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(frodo, Keyword.VALIANT));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertFalse(scn.FreepsPlayAvailable(hour));
	}
}
