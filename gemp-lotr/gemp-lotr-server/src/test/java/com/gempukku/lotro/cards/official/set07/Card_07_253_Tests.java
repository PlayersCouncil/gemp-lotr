package com.gempukku.lotro.cards.official.set07;

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

public class Card_07_253_Tests
{

	// The Fellowship path: site 2, Ettenmoors 1_331, is a plains; site 3, Rivendell Valley 1_341, is not.  Edoras
	// Hall 7_330 (each [rohan] possession costs 1 less) is not under the play.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("steed", "7_253");
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man companion, strength 5, vitality 3; a skirmish ability used only on purpose
					put("rider2", "4_286");     // a second Rider of Rohan (not unique), with no steed
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, vitality 3; valiant and nothing else
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: [rohan] Man ally; an archery ability that needs a villager
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; a skirmish ability used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: [rohan] Hobbit companion; a Man only beside 3 [rohan] companions or Eowyn

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
					put("savage2", "1_151");
					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1 (exhausted); its play trigger is never met
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] Uruk-hai, fierce, strength 5, vitality 3; its texts are about unbound Hobbits
					put("marksman1", "1_176");  // Goblin Marksman: Archer is its whole text
					put("marksman2", "1_176");
					// Úlairë Nelya, Ringwraith in Twilight: strength 10, vitality 3; fierce and damage +1 while there are 2 burdens
					put("nelya", "2_84");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path with Westemnet Hills 4_344 as site 4: a plains, and the home of Guma (4T).  Site 3, the home
	// of the Rohirrim Herdsman (3T), is Barrows of Edoras 4_337, a sanctuary and no plains.  Moving to Westemnet
	// Hills heals each mounted ally; nobody is wounded then.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("steed", "7_253");
					put("steed2", "7_253");
					put("guma", "4_277");       // Guma: [rohan] Man ally, home 4T, strength 4, vitality 2; a fellowship ability used only on purpose
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally, home 3T, strength 4, vitality 2; a regroup ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion; a maneuver ability used only on purpose
					// One Whom Men Would Follow: "Exert Aragorn to spot an ally.  Until the regroup phase, that ally is
					// strength +2 and participates in archery fire and skirmishes."
					put("follow", "1_109");
					put("warrior", "4_165");    // Orthanc Warrior: strength 7, vitality 2; damage +1 is its whole text
					put("warrior2", "4_165");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site4", "4_344");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SwiftSteedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Swift Steed
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Game Text: Bearer must be a [rohan] Man.<br>While at a plains site, bearer takes no more than 1 wound during each skirmish.<br>At the start of each skirmish involving bearer, each minion skirmishing bearer must exert.
		*/

		var scn = GetScenario();

		var steed = scn.GetFreepsCard("steed");

		assertEquals("Swift Steed", steed.getBlueprint().getTitle());
		assertNull(steed.getBlueprint().getSubtitle());
		assertFalse(steed.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, steed.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, steed.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, steed.getBlueprint().getCardType());
		assertTrue(steed.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, steed.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedOnARohanManCompanionOrAllyAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var steed = scn.GetFreepsCard("steed");
		var rider = scn.GetFreepsCard("rider");
		var aldor = scn.GetFreepsCard("aldor");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(rider, boromir, merry);
		scn.MoveAlliesToTable(aldor);
		scn.MoveCardsToHand(steed);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(steed));
		scn.FreepsPlayCard(steed);
		// A [rohan] Man companion and a [rohan] Man ally; not a [gondor] Man, nor Merry, a [rohan] Hobbit who is no
		// Man beside two [rohan] companions and no Eowyn, nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, aldor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, merry, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(rider);
		assertAttachedTo(steed, rider);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtAPlainsBearerTakesOneWoundOfTwoWhenHeLosesAndAnotherRohanManTakesBoth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steed = scn.GetFreepsCard("steed");
		var rider = scn.GetFreepsCard("rider");
		var rider2 = scn.GetFreepsCard("rider2");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(rider, rider2);
		scn.AttachCardsTo(rider, steed);
		assertAttachedTo(steed, rider);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2);
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{rider, savage}, new PhysicalCardImpl[]{rider2, savage2});

		// The second Rider, with no steed: 5 against 5, the tie goes to the Shadow side, and the damage +1 makes 2 wounds.
		scn.FreepsResolveSkirmish(rider2);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(rider2));

		// The bearer: the same loss, 1 wound of the 2.
		assertEquals(0, scn.GetWoundsOn(rider));
		scn.FreepsResolveSkirmish(rider);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(rider));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AwayFromAPlainsBearerTakesBothWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steed = scn.GetFreepsCard("steed");
		var rider = scn.GetFreepsCard("rider");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, steed);
		assertAttachedTo(steed, rider);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		// Rivendell Valley, site 3: no plains.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		scn.FreepsAssignToMinions(rider, savage);

		assertEquals(0, scn.GetWoundsOn(rider));
		scn.FreepsResolveSkirmish(rider);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(rider));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtAPlainsBearerTakesBothArcheryWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steed = scn.GetFreepsCard("steed");
		var rider = scn.GetFreepsCard("rider");
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, steed);
		assertAttachedTo(steed, rider);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman1, marksman2);
		scn.SkipToArcheryWounds();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));

		// The archery phase, next door to the skirmishes: both archery wounds go on the bearer, and he takes both.
		assertEquals(0, scn.GetWoundsOn(rider));
		scn.FreepsChooseCard(rider);
		scn.FreepsChooseCard(rider);
		assertEquals(2, scn.GetWoundsOn(rider));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AtAPlainsTheLimitIsOneWoundInEachSkirmishTheFierceOneIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steed = scn.GetFreepsCard("steed");
		var guard = scn.GetFreepsCard("guard");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, steed);
		assertAttachedTo(steed, guard);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(nelya);
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(nelya, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(nelya, Keyword.DAMAGE));
		scn.FreepsAssignToMinions(guard, nelya);

		// Nelya 10 against the Guard's 6, under double: 2 wounds each time, 1 taken.
		assertEquals(0, scn.GetWoundsOn(guard));
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(guard, nelya);
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		// One more in the fierce skirmish: a limit for the turn would have kept him at 1, no limit would have killed him.
		assertEquals(2, scn.GetWoundsOn(guard));
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BorneByAnAllyItHoldsHisWoundsOnlyWhileHisHomeSiteIsAPlains() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var steed = scn.GetFreepsCard("steed");
		var steed2 = scn.GetFreepsCard("steed2");
		var guma = scn.GetFreepsCard("guma");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var follow = scn.GetFreepsCard("follow");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(guma, herdsman);
		scn.AttachCardsTo(guma, steed);
		scn.AttachCardsTo(herdsman, steed2);
		scn.MoveCardsToHand(follow);

		var warrior = scn.GetShadowCard("warrior");
		var warrior2 = scn.GetShadowCard("warrior2");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(warrior, warrior2);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals("Westemnet Hills", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));

		// The Herdsman, away from his home site, is made to take part in skirmishes: 4 + 2.
		scn.FreepsPlayCard(follow);
		scn.FreepsChooseCard(herdsman);
		assertEquals(6, scn.GetStrength(herdsman));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guma, warrior},
				new PhysicalCardImpl[]{herdsman, warrior2});

		// Guma is at his home site, the plains the fellowship is at: 7 beats his 4 and its damage +1 would place 2
		// wounds; he takes 1.
		scn.FreepsResolveSkirmish(guma);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guma));
		assertInZone(Zone.SUPPORT, guma);

		// The Herdsman is at his home site, Barrows of Edoras, which is no plains (CR 4.2 "ally"): 7 beats his 6,
		// and the 2 wounds kill him (vitality 2).
		scn.FreepsResolveSkirmish(herdsman);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, herdsman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtTheStartOfBearersSkirmishEachMinionSkirmishingHimExertsAndNoOtherSkirmishDoesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steed = scn.GetFreepsCard("steed");
		var rider = scn.GetFreepsCard("rider");
		var rider2 = scn.GetFreepsCard("rider2");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(rider, rider2);
		scn.AttachCardsTo(rider, steed);
		assertAttachedTo(steed, rider);

		scn.StartGame();
		// The second Rider is given the strength to live through the Troop: 5 + 4 against 13, which is under double.
		scn.ApplyAdHocModifier(new StrengthModifier(null, rider2, null, 4));
		scn.MoveMinionsToTable(troop1, savage, runner, troop2, troop3);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{rider, troop1}, new PhysicalCardImpl[]{rider2, troop2}, new PhysicalCardImpl[]{frodo, troop3});
		// The Uruk Savage and the Goblin Runner were left over: the Shadow player puts them on the Rider as well.
		scn.ShadowAssignToMinions(rider, savage, runner);

		// The second Rider's skirmish first, a [rohan] Man with no steed: nobody exerts, those assigned to the bearer included.
		scn.FreepsResolveSkirmish(rider2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(troop3));

		scn.PassSkirmishActions();
		// The second Rider, 9 against 13, loses and takes a wound.
		assertEquals(1, scn.GetWoundsOn(rider2));
		assertEquals(0, scn.GetWoundsOn(troop2));

		// The bearer's skirmish: an Orc and an Uruk-hai, each exerts once; the Runner, with 1 vitality, cannot exert
		// and is neither wounded nor killed.  The Troop that fought the second Rider does not exert, nor the one
		// assigned to Frodo, whose skirmish has not begun.
		assertEquals(0, scn.GetWoundsOn(runner));
		scn.FreepsResolveSkirmish(rider);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(troop3));
	}

	@Test
	public void AMinionSkirmishingBearerAgainInTheFierceRoundExertsAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steed = scn.GetFreepsCard("steed");
		var rider = scn.GetFreepsCard("rider");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, steed);
		assertAttachedTo(steed, rider);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(rider, pursuer);

		assertEquals(0, scn.GetWoundsOn(pursuer));
		scn.FreepsResolveSkirmish(rider);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(pursuer));

		// 5 against 5: a tie goes to the Shadow side, so the Rider takes a wound and the Pursuer none.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetWoundsOn(pursuer));

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(rider, pursuer);
		scn.FreepsResolveSkirmish(rider);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetWoundsOn(pursuer));
	}
}
