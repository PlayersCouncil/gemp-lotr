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

public class Card_13_135_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "13_135");
					// Éomer, Third Marshal of Riddermark: [rohan] Man companion, strength 7, vitality 3; his strength
					// bonus counts wounds on minions he is skirmishing, and he skirmishes nobody here
					put("eomer", "4_267");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, strength 6, vitality 3; valiant is its whole text
					// Rohirrim Herdsman: a [rohan] Man ally, strength 4; a regroup ability used only on purpose
					put("herdsman", "7_247");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability used only on purpose
					// Merry, Master Holbytla: a [rohan] Hobbit, a Man only while 3 [rohan] companions or Éowyn can be
					// spotted (here, two)
					put("merry", "103_85");
					put("spear", "4_288");      // Rider's Spear: a [rohan] hand weapon, strength +2; damage +1 if mounted
					put("mount", "4_287");      // Rider's Mount: a [rohan] mount, no bonus; a start-of-skirmish trigger only
					put("mount2", "4_287");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1
					put("assassin", "2_93");    // Tower Assassin: [sauron] Orc, vitality 3; a maneuver ability used only on purpose
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, vitality 1, so he cannot exert
					put("runner2", "1_178");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					// Uruk Pursuer: [isengard] Uruk-hai, strength 5, vitality 3, fierce; its other texts answer unbound Hobbits
					put("pursuer", "4_190");
					// Siege Troop: [sauron] Orc, strength 20, vitality 4; a regroup ability used only on purpose, and
					// never assigned here
					put("siegetroop", "8_106");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RidersBowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Rider's Bow
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Ranged weapon
		 * Game Text: Bearer must be a [rohan] Man.<br>If bearer is mounted, he or she is strength +1.<br>Each time bearer wins a skirmish, you may choose a Shadow player who must wound one of his or her minions.
		*/

		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");

		assertEquals("Rider's Bow", bow.getBlueprint().getTitle());
		assertNull(bow.getBlueprint().getSubtitle());
		assertFalse(bow.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, bow.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, bow.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, bow.getBlueprint().getCardType());
		assertTrue(bow.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(1, bow.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnARohanManCompanionOrAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		// Two [rohan] companions, the Guard and Merry, and no Éowyn: Merry is no Man.
		scn.MoveCompanionsToTable(guard, aragorn, merry);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(bow);

		scn.StartGame();
		scn.FreepsPlayCard(bow);
		// [rohan] Men, companions and an ally; not a [gondor] Man, not a [rohan] Hobbit, not Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, herdsman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, merry, scn.GetRingBearer()));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(herdsman);

		assertAttachedTo(bow, herdsman);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionBearerIsStrengthPlus1OnceAMountIsPlayedOnHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var spear = scn.GetFreepsCard("spear");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(guard, bow, spear);
		scn.AttachCardsTo(herdsman, mount2);
		scn.MoveCardsToHand(mount);
		assertAttachedTo(bow, guard);
		assertAttachedTo(spear, guard);
		assertAttachedTo(mount2, herdsman);

		scn.StartGame();
		// Bearing a possession that is no mount: 6 + 2 for the Spear, nothing for the Bow.  The Herdsman is mounted
		// and bears no Bow: his 4.
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(herdsman));

		scn.FreepsPlayCard(mount);
		assertAttachedTo(mount, guard);
		// The Mount has no bonus of its own: 6 + 2 + 1.
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(herdsman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnAllyBearerIsStrengthPlus1WhileMounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(herdsman, bow, mount);
		scn.AttachCardsTo(guard, mount2);
		assertAttachedTo(bow, herdsman);
		assertAttachedTo(mount, herdsman);
		assertAttachedTo(mount2, guard);

		scn.StartGame();
		// 4 + 1; the Guard is mounted and bears no Bow: his 6.
		assertEquals(5, scn.GetStrength(herdsman));
		assertEquals(6, scn.GetStrength(guard));
	}

	@Test
	public void EachTimeBearerWinsTheShadowPlayerMayBeMadeToWoundOneOfHisMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(guard, aragorn);
		scn.AttachCardsTo(guard, bow);
		assertAttachedTo(bow, guard);

		var savage = scn.GetShadowCard("savage");
		var assassin = scn.GetShadowCard("assassin");
		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, assassin, runner1);
		scn.SkipToAssignments();
		// The Runner waits for Aragorn's skirmish; the Assassin is left in no skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, savage}, new PhysicalCardImpl[]{aragorn, runner1});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(assassin));
		assertEquals(0, scn.GetWoundsOn(runner1));

		// The Guard (6) beats the Savage (5); the offer comes with the Savage's wound already on.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bow));
		scn.FreepsAcceptOptionalTrigger();

		// The Shadow player chooses among his minions, in the skirmish, waiting for another, or in none; no companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, assassin, runner1));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(runner1);

		// A wound, not an exertion: the Runner, vitality 1, is killed.
		assertInDiscard(runner1);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(assassin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningLeavesEveryMinionUnwounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, bow);
		assertAttachedTo(bow, guard);

		var savage = scn.GetShadowCard("savage");
		var assassin = scn.GetShadowCard("assassin");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, assassin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(assassin));

		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bow));
		scn.FreepsDeclineOptionalTrigger();

		// The Savage has the wound for losing and nothing more; the Assassin none.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(assassin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenBearerLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, bow);
		assertAttachedTo(bow, guard);

		var brigand = scn.GetShadowCard("brigand");
		var assassin = scn.GetShadowCard("assassin");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, assassin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, brigand);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(assassin));

		// The Brigand (8) beats the Guard (6), who takes a wound and lives; the Assassin is there to be wounded.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bow));
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertEquals(0, scn.GetWoundsOn(assassin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenARohanManWhoDoesNotBearItWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(eomer, guard);
		scn.AttachCardsTo(eomer, bow);
		assertAttachedTo(bow, eomer);

		var savage = scn.GetShadowCard("savage");
		var assassin = scn.GetShadowCard("assassin");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, assassin);
		scn.SkipToAssignments();
		// Éomer bears the Bow and fights nobody; the Guard, a [rohan] Man it could be played on, beats the Savage.
		scn.FreepsAssignToMinions(guard, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(assassin));

		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bow));
		assertEquals(0, scn.GetWoundsOn(assassin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheWinLeavesNoMinionToWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, bow);
		assertAttachedTo(bow, guard);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, runner1);
		scn.FreepsResolveSkirmish(guard);
		assertInZone(Zone.SHADOW_CHARACTERS, runner1);

		// The Guard (6) beats the only minion, the Runner (5, vitality 1), whose wound for losing kills him: nothing
		// is left to wound.
		scn.PassSkirmishActions();
		assertInDiscard(runner1);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bow));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BothWinsAgainstAFierceMinionOfferItEachTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, bow);
		assertAttachedTo(bow, guard);

		var pursuer = scn.GetShadowCard("pursuer");
		var siegetroop = scn.GetShadowCard("siegetroop");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer, siegetroop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, pursuer);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(siegetroop));

		// The Guard (6) beats the Pursuer (5).
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bow));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(pursuer, siegetroop));
		scn.ShadowChooseCard(siegetroop);
		assertEquals(1, scn.GetWoundsOn(siegetroop));
		assertEquals(1, scn.GetWoundsOn(pursuer));

		// The fierce round: the Guard beats the Pursuer again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(guard, pursuer);
		scn.FreepsResolveSkirmish(guard);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bow));
		scn.FreepsAcceptOptionalTrigger();
		scn.ShadowChooseCard(siegetroop);
		assertEquals(2, scn.GetWoundsOn(siegetroop));
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
