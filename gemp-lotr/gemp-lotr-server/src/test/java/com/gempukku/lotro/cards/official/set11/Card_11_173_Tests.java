package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_173_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sting", "11_173");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetResistanceScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sting", "11_173");
					// Aragorn, Ranger of the North: strength 8, resistance 6; his own maneuver ability is not used
					put("aragorn", "1_89");
					// Gimli, Skilled Defender: resistance 6; his text answers a skirmish he wins, and he is in none
					put("gimli", "5_7");
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally, the character who is no companion
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its text is about being played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetPlayScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sting", "11_173");
					// Sam, Son of Hamfast: strength 3, resistance 5; his abilities are used only on purpose
					put("sam", "1_311");
					// Bilbo, Well-spoken Gentlehobbit: the Bilbo who is an ally; a fellowship ability that needs a
					// tale, and none is here
					put("bilbo", "2_96");
					// Merry, From O'er the Brandywine: a Hobbit companion who is neither Bilbo, Frodo nor Sam
					put("merry", "1_303");
					// Aragorn, Ranger of the North: strength 8, resistance 6; his own maneuver ability is not used
					put("aragorn", "1_89");
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli is the Ring-bearer, so that the Frodo here is not.
	protected VirtualTableScenario GetGimliScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sting", "11_173");
					// Bilbo, Melancholy Hobbit: the Bilbo who is a companion; his start-of-skirmish trigger is not
					// reached here
					put("bilbo", "12_119");
					// Frodo, Frenzied Fighter: the one Frodo who can stand beside another Ring-bearer; his skirmish
					// ability is not reached here
					put("frodo", "13_149");
					put("merry", "1_303");
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sting", "11_173");
					put("sam", "1_311");
					put("aragorn", "1_89");
					// Goblin Scrabbler: strength 4, vitality 1; its texts are about being played and roaming.  Sam with
					// Sting (3 + 2) beats it.
					put("scrabbler1", "2_64");
					put("scrabbler2", "2_64");
					// Goblin Runner: strength 5, vitality 1.  Against Sam with Sting (5) it wins the tie.
					put("runner", "1_178");
					put("sword", "1_299");      // Hobbit Sword: a hand weapon a Hobbit bears, strength +2, no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Sting, Weapon of Heritage
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be Bilbo, Frodo, or Sam.<br><b>Maneuver:</b> If bearer is Bilbo or Frodo, exert him to make another companion resistance +2 until the regroup phase. If bearer is Sam, each time he wins a skirmish, you may remove a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sting");

		assertEquals("Sting", card.getBlueprint().getTitle());
		assertEquals("Weapon of Heritage", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void BorneByFrodoItExertsHimToMakeAnotherCompanionResistancePlus2UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResistanceScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bounder);
		scn.AttachCardsTo(frodo, sting);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(gimli));
		assertTrue(scn.FreepsActionAvailable(sting));

		scn.FreepsUseCardAction(sting);
		assertEquals(1, scn.GetWoundsOn(frodo));
		// "Another companion": not Frodo himself, and not the ally.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, bounder));

		scn.FreepsChooseCard(aragorn);
		assertEquals(8, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(gimli));
		assertEquals(10, scn.GetResistance(frodo));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Still there in the skirmish that follows ...
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(8, scn.GetResistance(aragorn));

		// ... and gone at the first decision of the regroup phase.  (Aragorn, 8, beats the Runner, 5.)
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(6, scn.GetResistance(aragorn));
	}

	@Test
	public void CanBePlayedOnFrodoSamOrTheAllyBilboButNotOnAnotherHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var sting = scn.GetFreepsCard("sting");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToHand(sting);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(sting));

		scn.FreepsPlayCard(sting);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, bilbo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry));
		scn.FreepsChooseCard(sam);

		assertAttachedTo(sting, sam);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedOnAFrodoWhoIsNotTheRingBearerHeExertsToMakeAnotherCompanionResistancePlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var sting = scn.GetFreepsCard("sting");
		var gimli = scn.GetRingBearer();
		var bilbo = scn.GetFreepsCard("bilbo");
		var frodo = scn.GetFreepsCard("frodo");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(bilbo, frodo, merry);
		scn.MoveCardsToHand(sting);

		scn.StartGame();
		scn.FreepsPlayCard(sting);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, frodo));
		// Neither the Ring-bearer nor a Hobbit of another title.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, merry));
		scn.FreepsChooseCard(frodo);
		assertAttachedTo(sting, frodo);

		scn.MoveMinionsToTable(scn.GetShadowCard("runner"));
		scn.SkipToPhase(Phase.MANEUVER);
		int gimliResistance = scn.GetResistance(gimli);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsActionAvailable(sting));

		scn.FreepsUseCardAction(sting);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, bilbo, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		scn.FreepsChooseCard(gimli);

		assertEquals(gimliResistance + 2, scn.GetResistance(gimli));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void PlayedOnTheCompanionBilboHeExertsToMakeAnotherCompanionResistancePlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var sting = scn.GetFreepsCard("sting");
		var gimli = scn.GetRingBearer();
		var bilbo = scn.GetFreepsCard("bilbo");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(bilbo, merry);
		scn.MoveCardsToHand(sting);

		scn.StartGame();
		// Bilbo is the one character here who may bear it, so nothing is asked.
		scn.FreepsPlayCard(sting);
		assertAttachedTo(sting, bilbo);

		scn.MoveMinionsToTable(scn.GetShadowCard("runner"));
		scn.SkipToPhase(Phase.MANEUVER);
		int gimliResistance = scn.GetResistance(gimli);
		int merryResistance = scn.GetResistance(merry);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsActionAvailable(sting));

		scn.FreepsUseCardAction(sting);
		// Bilbo exerts, and nobody else.
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(1, scn.FreepsGetChoiceMin());
		// Another companion than the bearer, who is a companion himself here.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo));
		scn.FreepsChooseCard(merry);

		assertEquals(merryResistance + 2, scn.GetResistance(merry));
		assertEquals(gimliResistance, scn.GetResistance(gimli));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void PlayedOnTheAllyBilboHeExertsAndTheBonusLastsThroughASecondSkirmish()throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var sting = scn.GetFreepsCard("sting");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToHand(sting);

		scn.StartGame();
		scn.FreepsPlayCard(sting);
		scn.FreepsChooseCard(bilbo);
		assertAttachedTo(sting, bilbo);

		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(6, scn.GetResistance(aragorn));
		assertTrue(scn.FreepsActionAvailable(sting));

		scn.FreepsUseCardAction(sting);
		// Bilbo exerts, and nobody else.
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// Each companion is another than Bilbo; Bilbo himself is no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, runner1));
		scn.FreepsChooseCard(sam);
		assertEquals(7, scn.GetResistance(sam));
		assertEquals(6, scn.GetResistance(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner1}, new PhysicalCardImpl[]{sam, runner2});
		// Aragorn, 8, beats the first Runner.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner1);

		// Still +2 in the skirmish that follows ...
		scn.FreepsResolveSkirmish(sam);
		assertEquals(7, scn.GetResistance(sam));

		// ... and gone at the first decision of the regroup phase.  (Sam, 3, loses to the Runner, 5.)
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(5, scn.GetResistance(sam));
	}

	@Test
	public void BorneBySamTheManeuverActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(sam, sting);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("runner1"));
		scn.SkipToPhase(Phase.MANEUVER);

		// Frodo is in play, but he is not the bearer.
		assertAttachedTo(sting, sam);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sting));
	}

	@Test
	public void WithFrodoExhaustedTheManeuverActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var sting = scn.GetFreepsCard("sting");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, sting);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 3);
		scn.MoveMinionsToTable(scn.GetShadowCard("runner1"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetVitality(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sting));
	}

	@Test
	public void EachTimeSamWinsASkirmishYouMayRemoveABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var scrabbler1 = scn.GetShadowCard("scrabbler1");
		var scrabbler2 = scn.GetShadowCard("scrabbler2");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, sting);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(scrabbler1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, scrabbler1);
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sting));
		assertEquals(3, scn.GetBurdens());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// A second move, and a second skirmish won.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(scrabbler2);
		scn.ShadowRefreshDecision();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, scrabbler2);
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sting));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnotherCompanionWinningOffersNothingAndSamsOfferMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var scrabbler1 = scn.GetShadowCard("scrabbler1");
		var scrabbler2 = scn.GetShadowCard("scrabbler2");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(sam, sting);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(scrabbler1, scrabbler2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, scrabbler1}, new PhysicalCardImpl[]{sam, scrabbler2});

		// Aragorn wins: he is not the bearer.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, scrabbler1);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetBurdens());

		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sting));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SamLosingASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, sting);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner);
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();

		// 5 against 5: the Runner wins.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SamWinningWithNoBurdenToRemoveOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var scrabbler1 = scn.GetShadowCard("scrabbler1");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, sting);

		scn.StartGame();
		scn.MoveMinionsToTable(scrabbler1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, scrabbler1);
		scn.FreepsResolveSkirmish(sam);
		assertEquals(0, scn.GetBurdens());
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, scrabbler1);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BorneByFrodoAWinOffersNothingAndNorDoesSamsWin() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var sting = scn.GetFreepsCard("sting");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		var scrabbler1 = scn.GetShadowCard("scrabbler1");
		var scrabbler2 = scn.GetShadowCard("scrabbler2");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, sting);
		scn.AttachCardsTo(sam, sword);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(scrabbler1, scrabbler2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, scrabbler1}, new PhysicalCardImpl[]{sam, scrabbler2});

		// Frodo (3, +1 for The Ruling Ring, +2 for Sting) beats the Scrabbler: he is not Sam.
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, scrabbler1);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetBurdens());

		// Sam (3, +2 for the Hobbit Sword) beats the other: he is Sam, but not the bearer.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, scrabbler2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
