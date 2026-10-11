package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_174_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("staff", "4_174");

					// Saruman, Servant of the Eye: an [isengard] Wizard, strength 8, vitality 4.  "Saruman may not
					// take wounds during the archery phase and may not be assigned to a skirmish.  Assignment:
					// Exert Saruman to assign an [isengard] minion to a companion (except the Ring-bearer).  That
					// companion may exert to prevent this."
					put("saruman", "3_69");
					// Saruman, Rabble-rouser: the [dunland] Saruman.  "Saruman may not be assigned to a skirmish";
					// his other texts answer a move and a skirmish.
					put("rabble", "4_33");
					// Saruman, Of Many Colours: his text opens "Damage +1. Fierce. Lurker." and then names a
					// culture when he is played (he is never played here).
					put("colours", "12_54");
					// Saruman, Agent of the Dark Lord: an [uruk-hai] Saruman whose text opens "Lurker."; his other
					// texts are a skirmish ability and an offer when a companion is killed.
					put("agent", "12_144");
					// Saruman, Servant of Sauron: a follower, strength +2, Aid (1).
					put("follower", "17_38");

					put("orc", "3_58");         // Isengard Servant: [isengard] Orc, strength 7, vitality 3; a regroup ability
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text

					put("sam", "1_311");        // Sam, Son of Hamfast: a companion who is not the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("legolas", "1_50");     // Legolas, Greenleaf: an archer, the fellowship's one archery wound
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a Wizard who is not Saruman
					// Gandalf, The Grey of the Hobbit sets: a Gandalf who is not Saruman, and whose first ability
					// ("Each time you play a [gandalf] event, you may wound a minion") the game switches off with the
					// same flag.  The Hobbit sets have no sheet of card text.
					put("hobbitGandalf", "30_27");
					// Depart Silently: a [gandalf] Maneuver event, cost 1, "spot Gandalf"; at sites 1 to 5 Hobbits
					// cannot be assigned until the regroup phase, which nothing here assigns.
					put("depart", "3_31");
					put("depart2", "3_31");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumansStaffStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Saruman's Staff, Wizard's Device
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Staff
		 * Strength: 2
		 * Game Text: Plays on Saruman.
		 * He is <b>fierce</b> and <b>damage +1</b>.
		 * <b>Maneuver:</b> Make the first sentence of Saruman's game text not apply until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("staff");

		assertEquals("Saruman's Staff", card.getBlueprint().getTitle());
		assertEquals("Wizard's Device", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.STAFF));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnSarumanForTwoAndMakesHimStrengthPlus2FierceAndDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var orc = scn.GetShadowCard("orc");
		var troop = scn.GetShadowCard("troop");
		var staff = scn.GetShadowCard("staff");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("gandalf"));
		scn.MoveCardsToHand(staff);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, orc, troop);
		scn.ShadowRefreshDecision();

		// 2 companions + 2 for Ettenmoors
		assertEquals(4, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(saruman));
		assertFalse(scn.HasKeyword(saruman, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(saruman, Keyword.DAMAGE));
		assertTrue(scn.ShadowPlayAvailable(staff));

		// Saruman is the only bearer there is: nobody is asked where it goes.
		scn.ShadowPlayCard(staff);
		assertEquals(2, scn.GetTwilight());
		assertAttachedTo(staff, saruman);
		assertEquals(10, scn.GetStrength(saruman));
		assertTrue(scn.HasKeyword(saruman, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(saruman, Keyword.DAMAGE));

		// "He": the other minions are as they were.
		assertEquals(7, scn.GetStrength(orc));
		assertFalse(scn.HasKeyword(orc, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(orc, Keyword.DAMAGE));
		assertEquals(13, scn.GetStrength(troop));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlaysOnASarumanOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rabble = scn.GetShadowCard("rabble");
		var orc = scn.GetShadowCard("orc");
		var staff = scn.GetShadowCard("staff");
		scn.MoveCardsToHand(staff);

		scn.StartGame();
		scn.FreepsPass();
		// Down after the move: the Rabble-rouser's own text answers one.
		scn.MoveMinionsToTable(rabble, orc);
		scn.ShadowRefreshDecision();

		// 1 companion + 2 for Ettenmoors
		assertEquals(3, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(rabble));
		assertFalse(scn.HasKeyword(rabble, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(rabble, Keyword.DAMAGE));
		assertTrue(scn.ShadowPlayAvailable(staff));

		scn.ShadowPlayCard(staff);
		assertEquals(1, scn.GetTwilight());
		assertAttachedTo(staff, rabble);
		assertEquals(10, scn.GetStrength(rabble));
		assertTrue(scn.HasKeyword(rabble, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(rabble, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(orc, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedOnTheFollowerSarumanNorOnAnotherMinionOrWizard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var follower = scn.GetShadowCard("follower");
		var orc = scn.GetShadowCard("orc");
		var staff = scn.GetShadowCard("staff");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("gandalf"));
		scn.MoveCardsToSupportArea(follower);
		scn.MoveCardsToHand(staff);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(orc);
		scn.ShadowRefreshDecision();

		// 2 companions + 2 for Ettenmoors: the Staff could be paid for.  A follower cannot bear other cards, an
		// [isengard] Orc is not Saruman, and neither is the other player's Wizard.
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, follower);
		assertInZone(Zone.HAND, staff);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(staff));
	}

	@Test
	public void ManeuverAbilityLetsSarumanTakeAnArcheryWoundAndBeAssignedToASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, legolas);

		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		var staff = scn.GetShadowCard("staff");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, troop);
		scn.AttachCardsTo(saruman, staff);

		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.FreepsCanAssign(saruman));
		assertFalse(scn.ShadowCanAssign(saruman));
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(staff));

		scn.ShadowUseCardAction(staff);
		assertTrue(scn.FreepsCanAssign(saruman));
		assertTrue(scn.ShadowCanAssign(saruman));
		assertAttachedTo(staff, saruman);

		// The sentence is off already: using the ability again would do nothing, and it is not offered.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(staff));

		// "May not take wounds during the archery phase" is part of that sentence: the Shadow player may put
		// Legolas's one archery wound on Saruman.
		scn.SkipToArcheryWounds();
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(saruman, troop));
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.ShadowChooseCard(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(troop));

		// "May not be assigned to a skirmish" is the rest of it: he is offered beside the Troop, and assigned.
		scn.PassAssignmentActions();
		assertEquals(2, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(saruman));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop));
		scn.FreepsAssignToMinions(aragorn, saruman);
		scn.ShadowDeclineAssignments();
		assertTrue(scn.IsCharAssignedAgainst(aragorn, saruman));

		// 8 + 2 for the Staff against 8: Aragorn loses and takes 2 wounds, 1 + 1 for the Staff's damage +1.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, saruman));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		// The Staff makes him fierce: the fierce assignments follow.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithoutTheAbilitySarumanTakesNoArcheryWoundAndCannotBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, legolas);

		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		var staff = scn.GetShadowCard("staff");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, troop);
		scn.AttachCardsTo(saruman, staff);

		// The ability is offered in the maneuver phase, and not used.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(staff));
		scn.ShadowPass();

		// A Maneuver action: not offered in the archery phase.
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(staff));

		// Saruman may not take the archery wound: it goes to the Troop, and nobody is asked.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		scn.ShadowPass();
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(troop));

		// He may not be assigned: the Troop is the only minion on offer to either player.
		scn.PassAssignmentActions();
		assertEquals(1, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(saruman));
		scn.FreepsDeclineAssignments();
		assertTrue(scn.ShadowDecisionAvailable("Assign minions to companions or allies at home"));
		assertEquals(1, scn.ShadowGetShadowAssignmentTargetCount());
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(troop));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(saruman));
	}

	@Test
	public void TheFirstSentenceAppliesAgainFromTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, legolas);

		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		var staff = scn.GetShadowCard("staff");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, troop);
		scn.AttachCardsTo(saruman, staff);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowUseCardAction(staff);

		// The archery wound goes to the Troop this time; Aragorn fights Saruman and loses (8 against 8 + 2).
		scn.SkipToArcheryWounds();
		scn.ShadowChooseCard(troop);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, saruman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// The fierce assignment phase, the last before the regroup phase: the sentence is still off.  Saruman,
		// fierce by the Staff, is the one minion to be assigned again, and either player may assign him.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		assertEquals(1, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(saruman));
		scn.FreepsDeclineAssignments();
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(saruman));
		scn.ShadowDeclineAssignments();

		// The regroup phase: it applies again.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsCanAssign(saruman));
		assertFalse(scn.ShadowCanAssign(saruman));

		// The fellowship moves again, and this time the ability is offered and not used.
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(staff));
		scn.ShadowPass();

		// The archery wound cannot go to Saruman: the Troop takes it, and nobody is asked.
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(troop));
		scn.PassArcheryActions();
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(2, scn.GetWoundsOn(troop));

		// Nor can he be assigned.
		scn.PassAssignmentActions();
		assertTrue(scn.FreepsDecisionAvailable("Assign minions to companions or allies at home"));
		assertEquals(1, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(saruman));
	}

	@Test
	public void SarumansOwnAssignmentAbilityStillAppliesAndMayAssignHimOnceTheFirstSentenceDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");
		var orc = scn.GetShadowCard("orc");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, orc);
		scn.AttachCardsTo(saruman, staff);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowUseCardAction(staff);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Only the first sentence is off: his second, the assignment ability, is still his to use.
		assertTrue(scn.ShadowActionAvailable(saruman));
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.ShadowUseCardAction(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		// Sam is the only companion but the Ring-bearer.  "An [isengard] minion": Saruman himself, who may now
		// be assigned, beside the Orc.
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(saruman, orc));
		scn.ShadowChooseCard(saruman);
		// Sam may exert to prevent it, and does not.
		scn.FreepsChooseNo();

		assertTrue(scn.IsCharAssignedAgainst(sam, saruman));
		assertFalse(scn.IsCharAssigned(orc));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedOnSarumanOfManyColoursWhereItWouldHaveNoEffect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		var colours = scn.GetShadowCard("colours");
		var staff = scn.GetShadowCard("staff");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(colours);
		scn.AttachCardsTo(colours, staff);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// "Damage +1. Fierce. Lurker." is not a sentence the Staff switches off: its ability does nothing here.
		// His printed damage +1 and the Staff's.
		assertEquals(2, scn.GetKeywordCount(colours, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(colours, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(colours, Keyword.LURKER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(staff));
	}

	@Test
	public void IsNotOfferedOnSarumanAgentOfTheDarkLordWhereItWouldHaveNoEffect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		var agent = scn.GetShadowCard("agent");
		var staff = scn.GetShadowCard("staff");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(agent);
		scn.AttachCardsTo(agent, staff);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// "Lurker." is not a sentence the Staff switches off: its ability does nothing here.
		assertTrue(scn.HasKeyword(agent, Keyword.LURKER));
		assertTrue(scn.HasKeyword(agent, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(agent, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(staff));
	}

	@Test
	@Ignore("F-0037: after the Staff's ability, the first ability of Gandalf, The Grey (30_27) of the Hobbit sets is switched off as well; the text says Saruman's")
	public void AGandalfOfTheHobbitSetsKeepsHisFirstAbilityWhenTheStaffIsUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hobbitGandalf = scn.GetFreepsCard("hobbitGandalf");
		var depart = scn.GetFreepsCard("depart");
		var depart2 = scn.GetFreepsCard("depart2");
		scn.MoveCompanionsToTable(hobbitGandalf);
		scn.MoveCardsToHand(depart, depart2);

		var saruman = scn.GetShadowCard("saruman");
		var orc = scn.GetShadowCard("orc");
		var staff = scn.GetShadowCard("staff");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, orc);
		scn.AttachCardsTo(saruman, staff);

		// Before the Staff is used, playing a [gandalf] event offers this Gandalf's wound: he is not Saruman
		// and it is the first thing he says.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(depart);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hobbitGandalf));
		scn.FreepsDeclineOptionalTrigger();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(staff));
		scn.ShadowUseCardAction(staff);

		// The sentence switched off is Saruman's, and this Gandalf's first ability is still his to use.
		assertTrue(scn.FreepsPlayAvailable(depart2));
		scn.FreepsPlayCard(depart2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hobbitGandalf));
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
