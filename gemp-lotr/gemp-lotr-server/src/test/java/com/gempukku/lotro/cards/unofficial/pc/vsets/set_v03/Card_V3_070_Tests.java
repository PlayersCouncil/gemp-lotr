package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_070_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mouth", "103_70");
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text at all
					put("rider", "12_161");       // Black Rider: [wraith] Nazgul, vitality 3; fierce, strength +1 for each companion
					put("predator", "7_196");     // Morgul Predator: [wraith] Orc, strength 11, vitality 3. Skirmish: exert this minion to make a Nazgul or [wraith] Orc strength +1
					put("party", "1_158");        // Uruk-hai Raiding Party: strength 9, vitality 3; damage +1 is its whole text
					put("hunt", "1_126");         // Hunt Them Down!: [isengard] Shadow event, (3). Maneuver: make an Uruk-hai fierce until the regroup phase

					put("gimli", "1_13");         // Gimli, Son of Gloin: strength 6, vitality 3, damage +1
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4, ranger. Maneuver: exert him to make him defender +1
					put("rangersword", "1_112");  // Ranger's Sword: Aragorn is strength +2 and damage +1
					put("arwen", "1_30");         // Arwen, Daughter of Elrond: Elf, strength 6, vitality 3; stronger only against a Nazgul
					put("legolas", "1_50");       // Legolas, Greenleaf: archer. Archery: exert Legolas to wound a minion; he then adds nothing to the archery total
					put("bow1", "1_41");          // Elven Bow: bearer is an archer
					put("bow2", "1_41");
					put("bow3", "1_41");
					put("trails", "1_104");       // Eregion's Trails: [gondor] event, (1). Maneuver: exert a ranger to make each roaming minion strength -3 until the regroup phase
					put("defiance1", "1_37");     // Defiance: [elven] event, (1). Skirmish: make an Elf strength +2 (+4 against a Nazgul)
					put("defiance2", "1_37");
					put("sorry", "1_312");        // Sorry About Everything: [shire] event, (0). Fellowship: exert a Hobbit companion to remove a burden
					put("hobbitsword", "1_299");  // Hobbit Sword: possession, (1), bearer must be a Hobbit; no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheMouthofSauronStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: The Mouth of Sauron, Bearer of Dread News
		 * Unique: true
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: Lurker.
		 * 	Other minions cannot take more than 1 wound per phase.
		 * 	Response: If a Free Peoples event is played, exert this minion to cancel that event.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mouth");

		assertEquals("The Mouth of Sauron", card.getBlueprint().getTitle());
		assertEquals("Bearer of Dread News", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedFromHandForThreeAndTwoMoreWhileRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		scn.MoveCardsToHand(mouth);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for site 2
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(mouth));

		scn.ShadowPlayCard(mouth);
		// 3 to play + 2 roaming: his site number is 3 and the fellowship is at site 2
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, mouth);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherMinionLosingToDamagePlusOneTakesOneWoundWhileTheMouthAndACompanionTakeTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var savage = scn.GetShadowCard("savage");
		var party = scn.GetShadowCard("party");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(gimli, aragorn, arwen);
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("rangersword"));

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, savage, party);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, savage},
				new PhysicalCardImpl[]{arwen, party},
				new PhysicalCardImpl[]{aragorn, mouth});

		// Gimli (6, damage +1) beats the Savage (5): 2 wounds, of which the Savage can take 1.
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));

		// A companion is no minion: Arwen (6) loses to the Raiding Party (9, damage +1) and takes both.
		assertEquals(0, scn.GetWoundsOn(arwen));
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(arwen));

		// The Mouth is not one of the "other" minions: Aragorn (8 + 2, damage +1) beats him (9) and he takes both.
		assertEquals(0, scn.GetWoundsOn(mouth));
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(mouth));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithoutTheMouthTheSameLostSkirmishDealsBothWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetWoundsOn(savage));
		scn.FreepsAssignAndResolve(gimli, savage);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachOtherMinionCanBeAssignedOneArcheryWoundAndTheMouthMoreThanOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var troop = scn.GetShadowCard("troop");
		var rider = scn.GetShadowCard("rider");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(legolas, arwen, aragorn, gimli);
		// Four archers: Legolas, and three bows. The bows on Aragorn and Gimli are put there by the rig.
		scn.AttachCardsTo(arwen, scn.GetFreepsCard("bow1"));
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("bow2"));
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("bow3"));

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, troop, rider);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(4, scn.GetFreepsArcheryTotal());
		scn.PassCurrentPhaseActions();

		// First wound: any of the three.
		assertTrue(scn.ShadowHasCardChoiceAvailable(mouth, troop, rider));
		scn.ShadowChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));

		// The Troop, a [moria] minion, has had its wound for this phase.
		assertTrue(scn.ShadowHasCardChoiceAvailable(mouth, rider));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		scn.ShadowChooseCard(mouth);
		assertEquals(1, scn.GetWoundsOn(mouth));

		// The Mouth has had one too, and can be given another; the Rider's own wound is still to come.
		assertTrue(scn.ShadowHasCardChoiceAvailable(mouth, rider));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		scn.ShadowChooseCard(mouth);
		assertEquals(2, scn.GetWoundsOn(mouth));

		assertTrue(scn.ShadowHasCardChoiceAvailable(mouth, rider));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		scn.ShadowChooseCard(rider);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(2, scn.GetWoundsOn(mouth));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AMinionWoundedOnceInTheArcheryPhaseCanBeWoundedOnceMoreInItsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(legolas, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, savage, troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(savage));
		scn.FreepsUseCardAction(legolas);
		assertTrue(scn.FreepsHasCardChoiceAvailable(mouth, savage, troop));
		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));

		// A second "wound a minion" in the same phase cannot be aimed at the Savage.
		scn.ShadowPass();
		scn.FreepsUseCardAction(legolas);
		assertTrue(scn.FreepsHasCardChoiceAvailable(mouth, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage));
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(troop));

		// Legolas used his ability, so he adds nothing to the archery total and no archery wound follows.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// The skirmish is a new phase: of the 2 wounds for losing to damage +1 the Savage takes 1 more.
		// 1 in the archery phase + 1 in the skirmish
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnExertionIsNotTheOneWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var predator = scn.GetShadowCard("predator");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("rangersword"));

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, predator);
		// Aragorn 8 + 2 for the sword + 3 here = 13, to beat the Predator once it has made itself 12.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 3));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, predator);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The Predator is the only Nazgul or [wraith] Orc on the table, so its exertion makes itself strength +1.
		assertEquals(0, scn.GetWoundsOn(predator));
		scn.ShadowUseCardAction(predator);
		assertEquals(1, scn.GetWoundsOn(predator));
		assertEquals(12, scn.GetStrength(predator));
		assertEquals(13, scn.GetStrength(aragorn));

		scn.PassSkirmishActions();

		// It loses to damage +1 and takes 1 wound of the 2: 1 exertion + 1 wound.
		// Were the exertion its one wound it would have 1 token; with no limit it would have 3 and be dead.
		assertEquals(2, scn.GetWoundsOn(predator));
		assertInZone(Zone.SHADOW_CHARACTERS, predator);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ExertsToCancelAFreePeoplesEventWhoseCostsStayPaidAndIsNotOfferedAgainForThatEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var trails = scn.GetFreepsCard("trails");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(trails);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		var pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(mouth));
		assertEquals(9, scn.GetStrength(mouth));
		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsPlayCard(trails);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mouth));
		scn.ShadowChooseOptionalTrigger(mouth);

		assertEquals(1, scn.GetWoundsOn(mouth));
		assertEquals(0, scn.GetWoundsOn(troop));
		// The event's costs stay paid and the card is discarded.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(pool + 1, scn.GetTwilight());
		assertInDiscard(trails);
		// Its effect is not carried out: both minions are roaming at site 2 and neither is strength -3.
		assertEquals(9, scn.GetStrength(mouth));
		assertEquals(13, scn.GetStrength(troop));

		// The Mouth could exert once more, and is not asked again about an event already cancelled.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mouth));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CancelsASkirmishEventWithHisLastExertionAndCannotAnswerTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var arwen = scn.GetFreepsCard("arwen");
		var defiance1 = scn.GetFreepsCard("defiance1");
		var defiance2 = scn.GetFreepsCard("defiance2");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(defiance1, defiance2);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, savage, troop);
		// Vitality 3 with 1 wound: one exertion left.
		scn.AddWoundsToChar(mouth, 1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(arwen);

		var pool = scn.GetTwilight();
		assertEquals(6, scn.GetStrength(arwen));

		scn.FreepsPlayCard(defiance1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mouth));
		scn.ShadowChooseOptionalTrigger(mouth);

		assertEquals(2, scn.GetWoundsOn(mouth));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(pool + 1, scn.GetTwilight());
		assertInDiscard(defiance1);

		// Exhausted, he cannot exert: the second copy is not answered and does what it says.
		scn.ShadowPass();
		scn.FreepsPlayCard(defiance2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mouth));
		assertEquals(8, scn.GetStrength(arwen));
		assertEquals(2, scn.GetWoundsOn(mouth));
		assertInDiscard(defiance2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void APossessionPlayedAnAbilityUsedAndAShadowEventBringNoResponseAndAnEventLeftAloneResolves() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var savage = scn.GetShadowCard("savage");
		var hunt = scn.GetShadowCard("hunt");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var sorry = scn.GetFreepsCard("sorry");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(hobbitsword, sorry, hunt);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, savage);
		scn.AddBurdens(1);
		scn.SetTwilight(10);

		// A Free Peoples possession played from hand is no event.
		scn.FreepsPlayCard(hobbitsword);
		assertAttachedTo(hobbitsword, frodo);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mouth));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A Free Peoples event on the same table is answered; left alone, it does what it says.
		assertEquals(1, scn.GetBurdens());
		scn.FreepsPlayCard(sorry);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mouth));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(mouth));

		// A companion's special ability is no event.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mouth));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// A Shadow event is no Free Peoples event.
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		scn.ShadowPlayCard(hunt);
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertInDiscard(hunt);
		assertEquals(0, scn.GetWoundsOn(mouth));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mouth));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
