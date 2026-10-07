package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_091_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lance", "103_91");
					put("lance2", "103_91");
					put("guard", "5_83");        // Household Guard: [rohan] Man companion, strength 6, vitality 3, valiant and nothing else
					put("guard2", "5_83");
					put("eowyn", "5_122");       // Éowyn, Daughter of Éomund: [rohan], strength 6, vitality 3; a maneuver ability never used here
					put("merry", "103_85");      // Merry, Master Holbytla: a [rohan] companion who is a Hobbit, strength 3
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a [gondor] Man
					put("guma", "4_277");        // Guma, Plains Farmer: a [rohan] Man who is an ally
					// Rider's Mount: bearer must be a [rohan] Man; no strength of its own.  At the start of a
					// skirmish involving its bearer each minion skirmishing him must exert.
					put("mount", "4_287");
					put("mount2", "4_287");

					put("savage", "1_151");      // Uruk Savage: [isengard], strength 5, vitality 3, damage +1
					put("troop", "1_177");       // Goblin Patrol Troop: [moria], strength 13, vitality 3, no text
					put("runner", "1_178");      // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VanguardsLanceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Vanguard's Lance
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be a [Rohan] companion.
		 * 	While bearer is mounted, bearer is strength +1 and each time bearer wins a skirmish, you must exert them to wound a minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lance");

		assertEquals("Vanguard's Lance", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(0, card.getBlueprint().getVitality());
	}

	@Test
	public void IsPlayedForTwoOnARohanCompanionWhoNeedNotBeAMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var lance = scn.GetFreepsCard("lance");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(guard, merry, aragorn);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(lance);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.FreepsPlayAvailable(lance));

		scn.FreepsPlayCard(lance);

		// A [rohan] Man and a [rohan] Hobbit.  Not a [gondor] Man, a [rohan] ally or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, guma, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(merry);

		assertAttachedTo(lance, merry);
		assertEquals(2, scn.GetTwilight());
		// 3 + 2
		assertEquals(5, scn.GetStrength(merry));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilARohanCompanionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lance = scn.GetFreepsCard("lance");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(lance, guard);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(lance));

		scn.FreepsPlayCard(guard);

		assertTrue(scn.FreepsPlayAvailable(lance));

		scn.FreepsPlayCard(lance);

		assertAttachedTo(lance, guard);
		// 2 for the Guard + 2 for the lance
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlusOneOnlyWhileMounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lance = scn.GetFreepsCard("lance");
		var lance2 = scn.GetFreepsCard("lance2");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(guard, guard2, eowyn);
		// Two bearers of a lance with no mount, and a mounted companion with no lance.
		scn.AttachCardsTo(guard, lance);
		scn.AttachCardsTo(guard2, lance2);
		scn.AttachCardsTo(eowyn, mount2);
		scn.MoveCardsToHand(mount);

		scn.StartGame();

		// 6 + 2
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(guard2));
		assertEquals(6, scn.GetStrength(eowyn));

		scn.FreepsPlayCard(mount);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guard2));
		scn.FreepsChooseCard(guard);

		assertAttachedTo(mount, guard);
		// 6 + 2 + 1
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(guard2));
		assertEquals(6, scn.GetStrength(eowyn));

		// Without his mount again he is back to the lance's own +2.
		scn.MoveCardsToDiscard(mount);

		assertEquals(8, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(guard2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AMountedBearerWhoWinsASkirmishExertsToWoundAMinionOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lance = scn.GetFreepsCard("lance");
		var lance2 = scn.GetFreepsCard("lance2");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(guard, guard2, aragorn);
		scn.AttachCardsTo(guard, lance, mount);
		// A second mounted bearer of a lance, who stays out of the fight.
		scn.AttachCardsTo(guard2, lance2, mount2);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop, runner);
		scn.SkipToAssignments();
		// The Savage fights the bearer, the Runner waits for Aragorn, and nobody faces the Troop.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { guard, savage },
				new PhysicalCardImpl[] { aragorn, runner }
		);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);

		// The Savage has exerted for the Rider's Mount.  6 + 2 + 1 against 5.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(0, scn.GetWoundsOn(guard));

		scn.PassCurrentPhaseActions();
		// The skirmish's own wound first.
		scn.FreepsResolveRuleFirst();

		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(guard));
		// The minion beaten, a minion waiting for another skirmish and a minion nobody fights.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, runner, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, guard2, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(guard2));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Aragorn's 8 beats the Runner's 5; he bears no lance.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertInZone(Zone.DISCARD, runner);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(guard2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ABearerWhoIsNotMountedWinsWithoutExertingOrWoundingAnyone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lance = scn.GetFreepsCard("lance");
		var mount = scn.GetFreepsCard("mount");
		var guard = scn.GetFreepsCard("guard");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(guard, eowyn);
		scn.AttachCardsTo(guard, lance);
		// Someone is mounted, and it is not the bearer of the lance.
		scn.AttachCardsTo(eowyn, mount);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);

		// 6 + 2 against 5
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.PassCurrentPhaseActions();

		// The skirmish's own wound and nothing else.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMountedBearerWhoLosesAndAnotherMountedCompanionWhoWinsSetNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lance = scn.GetFreepsCard("lance");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		var guard = scn.GetFreepsCard("guard");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(guard, eowyn);
		scn.AttachCardsTo(guard, lance, mount);
		// Mounted, and no lance.
		scn.AttachCardsTo(eowyn, mount2);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { guard, troop },
				new PhysicalCardImpl[] { eowyn, savage }
		);

		// Éowyn's 6 beats the Savage's 5.
		scn.FreepsResolveSkirmish(eowyn);
		scn.PassCurrentPhaseActions();

		// The Savage exerted for her mount and took the skirmish's wound; nobody else is touched.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(troop));

		// The Guard's 6 + 2 + 1 loses to the Troop's 13.
		scn.FreepsResolveSkirmish(guard);
		assertEquals(9, scn.GetStrength(guard));
		scn.PassCurrentPhaseActions();

		// The Troop exerted for his mount; he took the skirmish's wound and did not exert.
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnExhaustedBearerWinsWithoutExertingAndNoMinionIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lance = scn.GetFreepsCard("lance");
		var mount = scn.GetFreepsCard("mount");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, lance, mount);
		// Vitality 3
		scn.AddWoundsToChar(guard, 2);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);

		assertEquals(9, scn.GetStrength(guard));
		assertEquals(1, scn.GetWoundsOn(savage));

		scn.PassCurrentPhaseActions();

		// The mount's exertion and the skirmish's wound, and none from the lance.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AgainstAFierceMinionTheBearerExertsAndWoundsAMinionEachTimeHeWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lance = scn.GetFreepsCard("lance");
		var mount = scn.GetFreepsCard("mount");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, lance, mount);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		// So that the Guard fights the Savage twice in one turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, savage, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();

		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		scn.FreepsChooseCard(troop);

		// The mount's exertion and the skirmish's wound on the Savage; the lance's wound on the Troop.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(troop));

		// The fierce skirmish.
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignToMinions(guard, savage);
		scn.FreepsResolveSkirmish(guard);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();

		// The Savage is killed by the skirmish's wound, which leaves the Troop as the only minion to wound.
		assertInZone(Zone.DISCARD, savage);
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoMinionLeftToWoundTheBearerExertsAllTheSame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lance = scn.GetFreepsCard("lance");
		var mount = scn.GetFreepsCard("mount");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, lance, mount);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, runner);
		scn.FreepsResolveSkirmish(guard);

		// Vitality 1: the Runner cannot exert for the mount.  9 against 5 is no overwhelming.
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(guard));

		scn.PassCurrentPhaseActions();
		// The skirmish's own wound first, which kills the only minion.
		scn.FreepsResolveRuleFirst();

		assertInZone(Zone.DISCARD, runner);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
