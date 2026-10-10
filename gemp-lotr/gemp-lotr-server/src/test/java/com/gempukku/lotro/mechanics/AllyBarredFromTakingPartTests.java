package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * An ally a card says "may not participate in archery fire or skirmishes", against a card that has him participate.
 *
 * Comprehensive Rules 4.2, "ally": an ally participates in archery fire and skirmishes only while the fellowship is
 * at his home site, or where a card allows it.  Participating in archery fire is being counted for the fellowship
 * archery total (an archer) and being assigned archery wounds; participating in skirmishes is being assigned to a
 * minion, by either player or by a card, and fighting the skirmish.
 *
 * Streets of Edoras 4_340 ("Each [rohan] ally may not participate in archery fire or skirmishes at Streets of
 * Edoras") is the one card that bars an ally.  Many cards have an ally participate; the two here are One Whom Men
 * Would Follow 1_109 ("Exert Aragorn to spot an ally. Until the regroup phase, that ally is strength +2 and
 * participates in archery fire and skirmishes") and Dunlending Warrior 4_18 ("Assignment: Spot an ally to make that
 * ally participate in skirmishes and assign this minion to skirmish that ally").
 *
 * Ketura's ruling (Q-0150, 2026-10-08): the bar wins.  "Can't" beats "can" (Q-0011), whatever card grants the
 * "can", and a bar on participating bars every part of it (Q-0147).  So at the Streets a [rohan] ally whom another
 * card has participate adds nothing to the fellowship archery total, is assigned no archery wound, is assigned to
 * no minion and fights no skirmish.  What else the other card does to him (the strength bonus) still happens.  The
 * game used to let the allowing card win in all four.  A minion whose own text would have such an ally fight
 * and assign itself to him is not offered that text when the barred ally is the only one it could take (Ketura on
 * F-0283: A; MinionThatAssignsItselfToAnAllyTests has the other boards).
 *
 * Each test at the Streets has its control beside it: an ally who is not [rohan], away from home, whom the same
 * card does have participate there.  The tests at site 2 are the other control: the same [rohan] ally, away from
 * home at a site that bars nobody.
 */
public class AllyBarredFromTakingPartTests
{
	/**
	 * The Towers path with Streets of Edoras as its site 3.  Aldor's homes are sites 3 and 6 of this path; Celeborn
	 * and Calaglin, whose home is on the Fellowship path, are at home nowhere on it.
	 */
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("onewhom1", "1_109");   // One Whom Men Would Follow
					put("onewhom2", "1_109");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("aldor", "4_262");      // Aldor: [rohan] Man ally, home sites 3T and 6T, strength 3, vitality 2; an archery ability that needs a villager (none here)
					put("celeborn", "1_34");    // Celeborn: Elf ally, home site 6 of the Fellowship path, strength 6, vitality 3; a fellowship ability used only on purpose
					put("calaglin", "3_10");    // Calaglin: Elf ally and archer, home site 6 of the Fellowship path, vitality 3; a regroup ability used only on purpose
					put("marksman1", "1_176");  // Goblin Marksman: archer, strength 7, vitality 1, no other text
					put("marksman2", "1_176");
					put("savage1", "1_151");    // Uruk Savage: strength 5, vitality 3, damage +1 and no other text
					put("savage2", "1_151");
					put("warrior1", "4_18");    // Dunlending Warrior: strength 9, vitality 1
					put("warrior2", "4_18");
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");      // Eastemnet Gullies: a fellowship ability used only on purpose
					put("site2", "4_331");      // Eastfold: a Shadow ability that needs 3 [isengard] trackers
					put("site3", "4_340");      // Streets of Edoras: a sanctuary; bars each [rohan] ally from archery fire and skirmishes
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AnArcherAllyBarredFromArcheryFireAddsNothingToTheArcheryTotalThoughACardHasHimParticipate() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var onewhom1 = scn.GetFreepsCard("onewhom1");
		var onewhom2 = scn.GetFreepsCard("onewhom2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var aldor = scn.GetFreepsCard("aldor");
		var calaglin = scn.GetFreepsCard("calaglin");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(aldor, calaglin);
		scn.MoveCardsToHand(onewhom1, onewhom2);
		// There is no [rohan] ally who is an archer: Aldor is made one.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, aldor, null, Keyword.ARCHER));

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(savage1);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals("Streets of Edoras", scn.GetCurrentSite().getBlueprint().getTitle());

		// Aldor is at home and barred; Calaglin is away from home.  No companion is an archer.
		assertEquals(0, scn.GetFreepsArcheryTotal());

		// The control: at the Streets the card does have an ally who is not [rohan] participate.
		scn.FreepsPlayCard(onewhom1);
		scn.FreepsChooseCard(calaglin);
		assertEquals(1, scn.GetFreepsArcheryTotal());
		scn.ShadowPass();

		scn.FreepsPlayCard(onewhom2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aldor));
		scn.FreepsChooseCard(aldor);
		// The rest of the card reaches him; the participating does not.
		assertEquals(5, scn.GetStrength(aldor));
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheSameArcherAllyAwayFromHomeWhereNoCardBarsHimIsCountedOnceACardHasHimParticipate() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var onewhom1 = scn.GetFreepsCard("onewhom1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var aldor = scn.GetFreepsCard("aldor");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(aldor);
		scn.MoveCardsToHand(onewhom1);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, aldor, null, Keyword.ARCHER));

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToPhase(Phase.MANEUVER);
		// Site 2 is not one of Aldor's homes.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetFreepsArcheryTotal());

		// Aldor is the one ally, and is taken without asking.
		scn.FreepsPlayCard(onewhom1);
		assertEquals(5, scn.GetStrength(aldor));
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnAllyBarredFromArcheryFireIsAssignedNoArcheryWoundThoughACardHasHimParticipate() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var onewhom1 = scn.GetFreepsCard("onewhom1");
		var onewhom2 = scn.GetFreepsCard("onewhom2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var aldor = scn.GetFreepsCard("aldor");
		var celeborn = scn.GetFreepsCard("celeborn");
		var frodo = scn.GetRingBearer();
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(aldor, celeborn);
		scn.MoveCardsToHand(onewhom1, onewhom2);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(marksman1, marksman2);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals("Streets of Edoras", scn.GetCurrentSite().getBlueprint().getTitle());

		scn.FreepsPlayCard(onewhom1);
		scn.FreepsChooseCard(celeborn);
		scn.ShadowPass();
		scn.FreepsPlayCard(onewhom2);
		scn.FreepsChooseCard(aldor);
		assertEquals(5, scn.GetStrength(aldor));

		scn.SkipToArcheryWounds();
		// Celeborn, away from home, takes part because the card says so.  Aldor, whom the same card names, does not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aldor));
		scn.FreepsChooseCard(celeborn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aldor));
		scn.FreepsChooseCard(frodo);

		assertEquals(0, scn.GetWoundsOn(aldor));
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheSameAllyAwayFromHomeWhereNoCardBarsHimIsOfferedAnArcheryWoundOnceACardHasHimParticipate() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var onewhom1 = scn.GetFreepsCard("onewhom1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var aldor = scn.GetFreepsCard("aldor");
		var celeborn = scn.GetFreepsCard("celeborn");
		var frodo = scn.GetRingBearer();
		var marksman1 = scn.GetShadowCard("marksman1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(aldor, celeborn);
		scn.MoveCardsToHand(onewhom1);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman1);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetCurrentSiteNumber());

		scn.FreepsPlayCard(onewhom1);
		scn.FreepsChooseCard(aldor);

		scn.SkipToArcheryWounds();
		// Neither ally is at home at site 2: the one the card named is offered, and the other is not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, aldor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(celeborn));
		scn.FreepsChooseCard(aldor);
		assertEquals(1, scn.GetWoundsOn(aldor));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnAllyBarredFromSkirmishesIsOfferedToNeitherPlayerForAssignmentThoughACardHasHimParticipate() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var onewhom1 = scn.GetFreepsCard("onewhom1");
		var onewhom2 = scn.GetFreepsCard("onewhom2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var aldor = scn.GetFreepsCard("aldor");
		var celeborn = scn.GetFreepsCard("celeborn");
		var frodo = scn.GetRingBearer();
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(aldor, celeborn);
		scn.MoveCardsToHand(onewhom1, onewhom2);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(savage1);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals("Streets of Edoras", scn.GetCurrentSite().getBlueprint().getTitle());

		scn.FreepsPlayCard(onewhom1);
		scn.FreepsChooseCard(celeborn);
		scn.ShadowPass();
		scn.FreepsPlayCard(onewhom2);
		scn.FreepsChooseCard(aldor);
		assertEquals(5, scn.GetStrength(aldor));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		// The game's rule, for either player.
		assertFalse(scn.FreepsCanAssign(aldor));
		assertFalse(scn.ShadowCanAssign(aldor));
		assertTrue(scn.FreepsCanAssign(celeborn));
		assertTrue(scn.ShadowCanAssign(celeborn));

		// And what each player is asked.
		scn.PassAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(celeborn));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(aldor));
		scn.FreepsDeclineAssignments();

		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(celeborn));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(aldor));
	}

	@Test
	public void TheSameAllyAwayFromHomeWhereNoCardBarsHimIsOfferedForAssignmentOnceACardHasHimParticipate() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var onewhom1 = scn.GetFreepsCard("onewhom1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var aldor = scn.GetFreepsCard("aldor");
		var celeborn = scn.GetFreepsCard("celeborn");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(aldor, celeborn);
		scn.MoveCardsToHand(onewhom1);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetCurrentSiteNumber());

		scn.FreepsPlayCard(onewhom1);
		scn.FreepsChooseCard(aldor);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.FreepsCanAssign(aldor));
		assertTrue(scn.ShadowCanAssign(aldor));
		assertFalse(scn.FreepsCanAssign(celeborn));

		scn.PassAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aldor));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(celeborn));
		scn.FreepsDeclineAssignments();
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(aldor));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(celeborn));
	}

	@Test
	public void AMinionWhoseOwnTextAssignsItToAnAllyIsNotAssignedToOneBarredFromSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var aldor = scn.GetFreepsCard("aldor");
		var celeborn = scn.GetFreepsCard("celeborn");
		var calaglin = scn.GetFreepsCard("calaglin");
		var warrior1 = scn.GetShadowCard("warrior1");
		var warrior2 = scn.GetShadowCard("warrior2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(aldor);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(warrior1, warrior2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals("Streets of Edoras", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.FreepsPass();

		// "Spot an ally to make that ally participate in skirmishes and assign this minion to skirmish that ally."
		// The only ally is barred from skirmishes: the ability could assign the Warrior to nobody, and an
		// ability with no cost that would do nothing is not offered (Ketura on F-0283: A).
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(warrior1));
		assertFalse(scn.ShadowActionAvailable(warrior2));
		assertFalse(scn.ShadowCanAssign(aldor));

		// The control: two allies away from home whom nothing bars.  The ability is offered, and the barred ally
		// is not among those it may take.
		scn.MoveAlliesToTable(celeborn, calaglin);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(warrior1));
		scn.ShadowUseCardAction(warrior1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(celeborn, calaglin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aldor));
		scn.ShadowChooseCard(celeborn);
		assertTrue(scn.IsCharAssignedAgainst(celeborn, warrior1));
		assertFalse(scn.IsCharAssigned(aldor));
		assertFalse(scn.IsCharAssigned(warrior2));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnAllyBarredFromSkirmishesFightsNoSkirmishThoughACardHasHimParticipate() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var onewhom1 = scn.GetFreepsCard("onewhom1");
		var onewhom2 = scn.GetFreepsCard("onewhom2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var aldor = scn.GetFreepsCard("aldor");
		var celeborn = scn.GetFreepsCard("celeborn");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(aldor, celeborn);
		scn.MoveCardsToHand(onewhom1, onewhom2);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals("Streets of Edoras", scn.GetCurrentSite().getBlueprint().getTitle());

		scn.FreepsPlayCard(onewhom1);
		scn.FreepsChooseCard(celeborn);
		scn.ShadowPass();
		scn.FreepsPlayCard(onewhom2);
		scn.FreepsChooseCard(aldor);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		// The Free Peoples player assigns the ally who may fight and leaves a minion over.
		scn.FreepsAssignToMinions(celeborn, savage1);
		// The Shadow player may not send it to Aldor, and sends it to Aragorn.
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(aldor));
		scn.ShadowAssignToMinions(aragorn, savage2);

		// Two skirmishes, and Aldor is in neither.
		scn.FreepsResolveSkirmish(celeborn);
		assertTrue(scn.IsCharSkirmishingAgainst(celeborn, savage1));
		assertFalse(scn.IsCharSkirmishing(aldor));
		scn.PassSkirmishActions();
		// Celeborn, strength 6 + 2, beats the Savage's 5.
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(savage1));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage2));
		assertFalse(scn.IsCharSkirmishing(aldor));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage2));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(aldor));
	}
}
