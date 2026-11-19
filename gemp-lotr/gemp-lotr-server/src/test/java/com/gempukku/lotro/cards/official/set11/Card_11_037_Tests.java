package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_037_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("authority", "11_37");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion; a strength bonus by the races in the fellowship
					// Saruman, The White: a [gandalf] Wizard who is an ally (a Hobbit-set card, laid under Q-0312 as the
					// one card that tells "a [gandalf] Wizard" from a [gandalf] Wizard companion).  His abilities are never used.
					put("sarumanally", "32_24");
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Wizard; strength +2 only beside 3 Ents
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; "Maneuver: Exert Aragorn to make him defender +1 until the regroup phase."
					put("sam", "1_311");        // Sam, Son of Hamfast: resistance 5; his fellowship ability is never used
					put("smeagol", "7_71");     // Smeagol, Always Helps: a Free Peoples companion, "To play, add a burden."
					// Gil-galad, High King of the Noldor: strength 9, so that 17 beats him without overwhelming him;
					// his maneuver and regroup abilities are never used.
					put("gilgalad", "18_12");

					put("miscreant", "11_131"); // Orc Miscreant: [orc] Orc, vitality 3, "Regroup: Exert this minion to add a burden."
					put("slayer", "3_93");      // Morgul Slayer: [sauron] Orc; a regroup ability never used
					put("gleaming", "3_89");    // Gleaming in the Snow: [sauron] event, "Regroup: Exert a [sauron] Orc and spot a [gondor] companion to add a burden."
					put("hatred", "7_310");     // Sauron's Hatred: [sauron] condition, "Regroup: Spot a [sauron] Orc and remove (4) to add a threat."
					// The Witch-king, Black Captain: strength 14, vitality 4, enduring; Men skirmishing him lose weapon bonuses.
					put("witchking", "8_84");
					// The Pale Blade, Sword of Flame: [wraith] hand weapon, strength +3: "If bearer is The Witch-king,
					// each time he wins a skirmish, you may exert him to add a burden."
					put("paleblade", "11_214");

					put("taskmaster", "2_90");  // Orc Taskmaster: [sauron] Orc, vitality 3, "Shadow: Exert this minion to add (1)."
					put("detachment", "11_74"); // Detachment of Haradrim: [men] Man, vitality 3, "Maneuver: Exert this minion to add (1) for each companion who has resistance 5 or less."
					put("hollowing", "3_54");   // Hollowing of Isengard: [isengard] support-area condition, "Shadow: Discard this condition to add (3)."
					put("enraged", "1_254");    // Mordor Enraged: [sauron] condition on a [sauron] minion, "Shadow: Exert bearer to add (1) for each archer companion."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NewAuthorityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: New Authority
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a [gandalf] Wizard.
		 * Each time a Shadow card adds a burden, you may remove (3).
		 * Response: If a minion uses its special ability, discard this condition to cancel the effect of that special ability.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("authority");

		assertEquals("New Authority", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAGandalfWizardIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var authority = scn.GetFreepsCard("authority");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(authority, gandalf);
		assertInZone(Zone.HAND, authority);

		scn.StartGame();

		// A [gandalf] companion who is no Wizard is not enough.
		assertFalse(scn.FreepsPlayAvailable(authority));
		scn.FreepsPlayCard(gandalf);

		assertTrue(scn.FreepsPlayAvailable(authority));
		scn.FreepsPlayCard(authority);
		assertInZone(Zone.SUPPORT, authority);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGandalfWizardWhoIsAnAllyMayBeSpottedToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var authority = scn.GetFreepsCard("authority");
		var sarumanally = scn.GetFreepsCard("sarumanally");
		scn.MoveAlliesToTable(sarumanally);
		scn.MoveCardsToHand(authority);
		assertInZone(Zone.HAND, authority);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(authority));
		scn.FreepsPlayCard(authority);
		assertInZone(Zone.SUPPORT, authority);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeAMinionsAbilityAddsABurdenThreeMayBeRemovedOrNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var authority = scn.GetFreepsCard("authority");
		scn.MoveCardsToSupportArea(authority);
		assertInZone(Zone.SUPPORT, authority);
		var miscreant = scn.GetShadowCard("miscreant");

		scn.StartGame();
		scn.MoveMinionsToTable(miscreant);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetWoundsOn(miscreant));
		scn.ShadowUseCardAction(miscreant);
		assertEquals(1, scn.GetWoundsOn(miscreant));
		// The Response to the minion's ability is offered first, before its burden, and passed.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(authority));
		assertEquals(0, scn.FreepsGetBurdens());
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.FreepsGetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(authority));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, authority);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// Each time: the second burden is answered again, and declining leaves the pool alone.
		scn.FreepsPass();
		scn.ShadowUseCardAction(miscreant);
		assertEquals(2, scn.GetWoundsOn(miscreant));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.FreepsGetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(authority));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ABurdenAShadowPossessionAddsIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var authority = scn.GetFreepsCard("authority");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		scn.MoveCardsToSupportArea(authority);
		scn.MoveCompanionsToTable(gilgalad);
		var witchking = scn.GetShadowCard("witchking");
		var paleblade = scn.GetShadowCard("paleblade");

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		scn.AttachCardsTo(witchking, paleblade);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gilgalad, witchking);
		scn.FreepsResolveSkirmish(gilgalad);
		scn.SetTwilight(5);
		// 14 + 3 beats 9, and does not overwhelm him.
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(gilgalad));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(paleblade));
		assertEquals(0, scn.FreepsGetBurdens());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(witchking));
		assertEquals(1, scn.FreepsGetBurdens());

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(authority));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ABurdenAShadowEventAddsIsAnsweredAndRemovesWhatThePoolHolds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var authority = scn.GetFreepsCard("authority");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(authority);
		scn.MoveCompanionsToTable(aragorn);
		var slayer = scn.GetShadowCard("slayer");
		var gleaming = scn.GetShadowCard("gleaming");
		scn.MoveCardsToHand(gleaming);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetWoundsOn(slayer));
		scn.ShadowPlayCard(gleaming);
		assertEquals(1, scn.GetWoundsOn(slayer));
		assertEquals(1, scn.FreepsGetBurdens());

		// Two in the pool: as much as can be removed is removed.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(authority));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ABurdenAFreePeoplesCardAddsIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var authority = scn.GetFreepsCard("authority");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToSupportArea(authority);
		scn.MoveCardsToHand(smeagol);
		assertInZone(Zone.HAND, smeagol);

		scn.StartGame();
		scn.SetTwilight(5);

		assertEquals(0, scn.FreepsGetBurdens());
		scn.FreepsPlayCard(smeagol);
		assertEquals(1, scn.FreepsGetBurdens());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(authority));
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AThreatAShadowCardAddsIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var authority = scn.GetFreepsCard("authority");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(authority);
		scn.MoveCompanionsToTable(aragorn);
		var slayer = scn.GetShadowCard("slayer");
		var hatred = scn.GetShadowCard("hatred");
		scn.MoveCardsToSupportArea(hatred);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(8);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		scn.ShadowUseCardAction(hatred);
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.FreepsGetBurdens());
		// 8 - 4 for the ability; nothing more is offered to take.
		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(authority));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithAnEmptyPoolABurdenAddedByAShadowCardIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var authority = scn.GetFreepsCard("authority");
		scn.MoveCardsToSupportArea(authority);
		var miscreant = scn.GetShadowCard("miscreant");

		scn.StartGame();
		scn.MoveMinionsToTable(miscreant);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(0);
		scn.FreepsPass();

		scn.ShadowUseCardAction(miscreant);
		// The Response to the minion's ability is passed.
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(authority));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheResponseAnswersAMinionsOwnShadowAbilityAndNotAConditionsOrOneOnACardAMinionBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var authority = scn.GetFreepsCard("authority");
		scn.MoveCardsToSupportArea(authority);
		var taskmaster = scn.GetShadowCard("taskmaster");
		var hollowing = scn.GetShadowCard("hollowing");
		var enraged = scn.GetShadowCard("enraged");
		scn.MoveCardsToSupportArea(hollowing);
		assertInZone(Zone.SUPPORT, authority);
		assertInZone(Zone.SUPPORT, hollowing);

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster);
		scn.AttachCardsTo(taskmaster, enraged);
		scn.FreepsPass();
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();

		// A Shadow condition's special ability.
		scn.ShadowUseCardAction(hollowing);
		assertInZone(Zone.DISCARD, hollowing);
		assertEquals(5, scn.GetTwilight());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(authority));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The ability of a condition the minion bears, paid by exerting him, is not his own.
		assertEquals(0, scn.GetWoundsOn(taskmaster));
		scn.ShadowUseCardAction(enraged);
		assertEquals(1, scn.GetWoundsOn(taskmaster));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(authority));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The minion's own: offered, and passed, so the effect is carried out.
		scn.ShadowUseCardAction(taskmaster);
		assertEquals(2, scn.GetWoundsOn(taskmaster));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(authority));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, authority);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DiscardsItselfToCancelAMinionsManeuverAbilityAndNotACompanionsAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var authority = scn.GetFreepsCard("authority");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToSupportArea(authority);
		scn.MoveCompanionsToTable(aragorn, sam);
		assertInZone(Zone.SUPPORT, authority);
		var detachment = scn.GetShadowCard("detachment");

		scn.StartGame();
		scn.MoveMinionsToTable(detachment);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(3);

		// A companion's special ability.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(authority));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Sam has resistance 5, so the minion's ability would add (1).  The cost stays paid.
		assertEquals(0, scn.GetWoundsOn(detachment));
		scn.ShadowUseCardAction(detachment);
		assertEquals(1, scn.GetWoundsOn(detachment));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(authority));
		scn.FreepsAcceptOptionalTrigger();
		assertInZone(Zone.DISCARD, authority);
		assertEquals(1, scn.GetWoundsOn(detachment));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
