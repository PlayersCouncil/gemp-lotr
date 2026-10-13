package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_12_184_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("beast", "12_184");
					// The Witch-king, Lord of Angmar: fierce, strength 14, vitality 4; strength +2 for each other Nazgûl
					put("twk", "1_237");
					// The Witch-king, Black Captain: enduring and NOT fierce, strength 14, vitality 4; his text is
					// about weapons borne by a Man he skirmishes
					put("captain", "8_84");
					put("lemenya", "1_232");    // Úlairë Lemenya: a [wraith] Nazgûl, strength 9; her text is about [wraith] conditions
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a Nazgûl who is [raider]
					put("hound", "7_192");      // Morgul Hound: a [wraith] minion who is an Orc
					put("rider", "12_161");     // Black Rider: a fierce Nazgûl of vitality 3, who can exert twice
					put("rancor", "9_44");      // Ring of Rancor: artifact on a Nazgûl, vitality +1

					put("fodder1", "1_302");    // Merry, Friend to Sam: strength 3
					put("fodder2", "1_307");    // Pippin, Hobbit of Some Intelligence: strength 3
					put("fodder3", "1_310");    // Sam, Faithful Companion: strength 3
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, not overwhelmed unless tripled
					// Éowyn, Northwoman: "Assignment: Exert Éowyn and assign a minion to the Ring-bearer to make that
					// minion lose all game text keywords and unable to gain game text keywords until the regroup phase."
					put("eowyn", "17_96");
					// Úlairë Enquëa, Magnified through Suffering: fierce and relentless; strength 12, vitality 4.  At the
					// start of his skirmish with a wounded companion he may exert to heal and exert: no companion is
					// wounded where he is here.
					put("enquea", "103_74");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetNobodyAssignedScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("beast", "12_184");
					// The Witch-king, Black Lord: fierce, vitality 4; his other texts are about his cost and about
					// being revealed from hand
					put("witchking", "12_183");
					// Black Rider: another fierce [wraith] Nazgûl, who bears no Beast (strength +1 for each companion)
					put("rider", "12_161");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion beside the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheWitchkingsBeastStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: The Witch-king's Beast, Fell Creature
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: 2
		 * Game Text: Bearer must be a Nazgûl.
		 * If bearer is The Witch-king, after all skirmishes and fierce skirmishes have been resolved,
		 * you may exert him twice to make him participate in one additional assignment and skirmish phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("beast");

		assertEquals("The Witch-king's Beast", card.getBlueprint().getTitle());
		assertEquals("Fell Creature", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlayedFromHandOnANazgul() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beast = scn.GetShadowCard("beast");
		var twk = scn.GetShadowCard("twk");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(beast);

		scn.StartGame();
		scn.MoveMinionsToTable(twk, lemenya, khamul, hound);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(beast));

		scn.ShadowPlayCard(beast);

		// Any Nazgûl, The Witch-king or not, [wraith] or not; not a [wraith] minion who is no Nazgûl.
		assertTrue(scn.ShadowHasCardChoiceAvailable(twk, lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lemenya);

		assertAttachedTo(beast, lemenya);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheWitchkingsBeastTriggersAThirdRoundOfSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fodder1 = scn.GetFreepsCard("fodder1");
		var fodder2 = scn.GetFreepsCard("fodder2");
		var fodder3 = scn.GetFreepsCard("fodder3");
		scn.MoveCompanionsToTable(fodder1, fodder2, fodder3);

		var twk = scn.GetShadowCard("twk");
		var beast = scn.GetShadowCard("beast");

		scn.StartGame();
		scn.MoveMinionsToTable(twk);
		scn.AttachCardsTo(twk, beast);
		scn.SkipToAssignments();

		// The first round.  The Witch-king is 16 (14 + 2 for the Beast) and overwhelms a Hobbit of 3.
		scn.FreepsAssignToMinions(fodder1, twk);
		scn.FreepsResolveSkirmish(fodder1);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, fodder1);

		// He is fierce: his fierce skirmish is still to come, and the Beast offers nothing yet.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(beast));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(fodder2, twk);
		scn.FreepsResolveSkirmish(fodder2);
		scn.PassFierceSkirmishActions();
		assertInZone(Zone.DEAD, fodder2);

		// Every skirmish and fierce skirmish is resolved: now the offer.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(beast));
		assertEquals(0, scn.GetWoundsOn(twk));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(twk));

		// One more assignment phase and one more skirmish for him.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertFalse(scn.InFierceRound());
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(fodder3, twk);
		scn.FreepsResolveSkirmish(fodder3);
		assertTrue(scn.IsCharSkirmishingAgainst(fodder3, twk));
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, fodder3);

		assertEquals(2, scn.GetWoundsOn(twk));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(beast));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningLeavesHimUnexertedAndTheRegroupPhaseFollows() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		var twk = scn.GetShadowCard("twk");
		var beast = scn.GetShadowCard("beast");

		scn.StartGame();
		scn.MoveMinionsToTable(twk);
		scn.AttachCardsTo(twk, beast);

		// Nobody is assigned in the first round or in the fierce one.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(beast));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(twk));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOneExertionLeftInHimItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		var twk = scn.GetShadowCard("twk");
		var beast = scn.GetShadowCard("beast");

		scn.StartGame();
		scn.MoveMinionsToTable(twk);
		scn.AttachCardsTo(twk, beast);
		// Vitality 4 with 2 wounds: he can exert once more, not twice.
		scn.AddWoundsToChar(twk, 2);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(beast));
		assertEquals(2, scn.GetWoundsOn(twk));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnAnotherNazgulWithTheWitchKingBesideHimItOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		var twk = scn.GetShadowCard("twk");
		var rider = scn.GetShadowCard("rider");
		var beast = scn.GetShadowCard("beast");

		scn.StartGame();
		scn.MoveMinionsToTable(twk, rider);
		// The Black Rider has vitality 3 and could exert twice.
		scn.AttachCardsTo(rider, beast);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(beast));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(twk));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AWitchKingWhoIsNotFierceIsOfferedItAfterTheFirstRoundAndTheShadowPlayerMayAssignHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		var captain = scn.GetShadowCard("captain");
		var beast = scn.GetShadowCard("beast");

		scn.StartGame();
		scn.MoveMinionsToTable(captain);
		scn.AttachCardsTo(captain, beast);

		// No minion is fierce, so once the first round is over there is no fierce skirmish to wait for.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertFalse(scn.InFierceRound());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(beast));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(captain));

		// In the additional assignment phase the Free Peoples player leaves him and the Shadow player assigns him.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.PassAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(boromir, captain);
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, captain));

		// Enduring: 14 + 2 for the Beast + 4 for his two wounds is 20, short of three times Boromir's 7.
		assertEquals(20, scn.GetStrength(captain));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AfterTheAdditionalSkirmishPhaseItIsNotOfferedAgainThoughHeCouldExertTwiceMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		var twk = scn.GetShadowCard("twk");
		var beast = scn.GetShadowCard("beast");
		var rancor = scn.GetShadowCard("rancor");

		scn.StartGame();
		scn.MoveMinionsToTable(twk);
		// Ring of Rancor makes his vitality 5: after two exertions he has three left, enough for two more.
		scn.AttachCardsTo(twk, beast, rancor);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(beast));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(twk));
		assertEquals(3, scn.GetVitality(twk));

		// The one additional assignment and skirmish phase.  He is 16 (14 + 2 for the Beast), short of three times
		// Boromir's 7, so Boromir loses and lives.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(boromir, twk);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));

		// That skirmish is resolved as well, and he has three vitality left to exert with: there is no second offer.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(beast));
		assertEquals(2, scn.GetWoundsOn(twk));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AfterASecondMoveOfTheTurnNoAdditionalPhaseComesWithoutPayingAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		var twk = scn.GetShadowCard("twk");
		var beast = scn.GetShadowCard("beast");

		scn.StartGame();
		scn.MoveMinionsToTable(twk);
		scn.AttachCardsTo(twk, beast);

		// At site 2 nobody is assigned in any round; the offer is taken, and his additional phase comes and goes.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(twk));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.PassAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());

		// The fellowship moves again, and he is still in play at site 3 with one exertion left in him.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// He cannot pay a second time, and the phase he paid for at site 2 does not come again here.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(beast));
		assertEquals(2, scn.GetWoundsOn(twk));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheAdditionalPhaseIsHisThoughEowynHasBarredHimFromGainingKeywords() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(eowyn);

		var twk = scn.GetShadowCard("twk");
		var beast = scn.GetShadowCard("beast");

		scn.StartGame();
		scn.MoveMinionsToTable(twk);
		scn.AttachCardsTo(twk, beast);
		// A strength that lets the Ring-bearer live through the skirmish Éowyn gives him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, frodo, null, 20));
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Éowyn exerts and assigns The Witch-king to the Ring-bearer: he loses all game text keywords, fierce among
		// them, and is unable to gain any until the regroup phase.
		assertTrue(scn.HasKeyword(twk, Keyword.FIERCE));
		scn.FreepsUseCardAction(eowyn);
		assertTrue(scn.IsCharAssignedAgainst(frodo, twk));
		assertFalse(scn.HasKeyword(twk, Keyword.FIERCE));
		scn.ShadowPass();
		scn.FreepsPass();

		// Frodo (3 + 1 for the Ring + 20) beats him (16), and with no fierce minion every skirmish is resolved.
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(twk));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(beast));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(3, scn.GetWoundsOn(twk));

		// "Participate in one additional assignment and skirmish phase" is no keyword of his to lose.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.PassAssignmentActions();
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(twk));
	}

	@Test
	public void WithARelentlessMinionInPlayHisAdditionalPhaseIsTheRelentlessRoundAndBothTakePart() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		var twk = scn.GetShadowCard("twk");
		var beast = scn.GetShadowCard("beast");
		var enquea = scn.GetShadowCard("enquea");
		var rider = scn.GetShadowCard("rider");

		scn.StartGame();
		scn.MoveMinionsToTable(twk, enquea, rider);
		scn.AttachCardsTo(twk, beast);

		// Nobody is assigned in the first round or in the fierce one.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(beast));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(twk));

		// His additional phase and Enquëa's relentless round are one round (Q-0267): both are there to be
		// defended against, and the Black Rider, fierce and neither relentless nor The Witch-king, is not.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.InRelentlessRound());
		scn.PassAssignmentActions();
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(2, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(twk));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(enquea));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(rider));
	}

	@Test
	public void TheExtraAssignmentPhaseIsOfferedThoughNobodyWasAssignedInEitherRound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNobodyAssignedScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var witchking = scn.GetShadowCard("witchking");
		var rider = scn.GetShadowCard("rider");
		var beast = scn.GetShadowCard("beast");

		scn.StartGame();
		scn.MoveMinionsToTable(witchking, rider);
		scn.AttachCardsTo(witchking, beast);

		// The first round: nobody is assigned by either player, so no skirmish is fought.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// The fierce round: nobody again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// With no skirmish left of either kind the Beast makes its offer, the phase still the assignment phase.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(beast));
		assertEquals(0, scn.GetWoundsOn(witchking));
		assertEquals(0, scn.GetWoundsOn(rider));

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(witchking));
		assertEquals(0, scn.GetWoundsOn(rider));

		// One more assignment phase, for him: the other fierce Nazgûl has no part in it.  It is neither the fierce
		// round nor a relentless one (Q-0267): no keyword of his makes it.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertFalse(scn.InFierceRound());
		assertFalse(scn.InRelentlessRound());
		scn.PassAssignmentActions();
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsCanAssign(witchking));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(witchking));
		assertFalse(scn.FreepsCanAssign(rider));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(rider));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
	}
}
