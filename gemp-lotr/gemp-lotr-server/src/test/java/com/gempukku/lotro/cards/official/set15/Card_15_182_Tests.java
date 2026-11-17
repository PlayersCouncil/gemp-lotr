package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_182_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fell", "15_182");

					// Úlairë Enquëa, Lieutenant of Morgul: [wraith] Nazgûl, strength 11, vitality 4, fierce; his
					// maneuver ability needs 6 companions or 5 burdens, and is never used
					put("enquea", "1_231");
					// Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgûl, strength 9, vitality 2 (she can exert once,
					// never twice); her text lowers a character who bears a [wraith] condition, and none does
					put("lemenya", "1_232");
					// Úlairë Nertëa, Messenger of Dol Guldur: [wraith] Nazgûl, strength 9, vitality 2; a play trigger
					// only, and she is cheated in
					put("nertea", "1_234");
					// Ulaire Attea, Khamul of the East: the [raider] Nazgûl, strength 12, vitality 3, fierce, site 3;
					// his texts need 3 and 5 burdens
					put("khamul", "103_61");
					// Úlairë Nelya, Ringwraith in Twilight: [wraith] Nazgûl, cost 5, site 2, so not roaming at site 2;
					// fierce and damage +1 only at 2 burdens or 2 wounds on the Ring-bearer
					put("nelya", "2_84");
					// Úlairë Toldëa, Messenger of Morgul: a Nazgûl of a title the Shadow player's piles do not hold,
					// for the Free Peoples player's discard pile; his ability needs 4 burdens
					put("toldea", "1_236");
					// Morgul Hound: a [wraith] Orc, no Nazgûl, strength 5, vitality 2; +4 and fierce only at 2 wounds on
					// the Ring-bearer
					put("hound", "7_192");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a Maneuver ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3; a Skirmish ability only
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, vitality 3; a Skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AShadowFellOverThemStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: A Shadow Fell Over Them
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a Nazgûl wins a skirmish, you may exert 2 Nazgûl (or a Nazgûl twice) to add a threat.<br><b>Assignment:</b> Spot a Nazgûl and remove (4) to play a Nazgûl from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fell");

		assertEquals("A Shadow Fell Over Them", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachTimeANazgulWinsTheShadowPlayerMayExertTwoNazgulOrOneTwiceToAddAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var fell = scn.GetShadowCard("fell");
		var enquea = scn.GetShadowCard("enquea");
		var khamul = scn.GetShadowCard("khamul");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(fell);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea, khamul, lemenya, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(hound));
		scn.PassSkirmishActions();

		// Enquëa (11) beats Aragorn (8).  Every Nazgûl can exert, and Enquëa and Khamul can each exert twice: the
		// Shadow player is asked which price he pays.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fell));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(List.of("Exert 2 Nazgul", "Exert a Nazgul twice"), scn.ShadowGetMultipleChoices());
		scn.ShadowChoose("Exert 2 Nazgul");

		// Any two Nazgûl, the winner or not, of either culture; the Hound, a [wraith] minion who is no Nazgûl, not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(enquea, khamul, lemenya));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(khamul, lemenya);

		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertEquals(1, scn.GetThreats());

		// The fierce round: Enquëa wins again, and the trigger is offered again.  Lemenya is exhausted and Khamul
		// has one exertion left, so only Enquëa can exert twice; Enquëa and Khamul can still exert 2 Nazgûl.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fell));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(List.of("Exert 2 Nazgul", "Exert a Nazgul twice"), scn.ShadowGetMultipleChoices());
		scn.ShadowChoose("Exert a Nazgul twice");

		assertEquals(2, scn.GetWoundsOn(enquea));
		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertEquals(2, scn.GetThreats());
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void ARaiderNazgulAloneWinningExertsHimselfTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var fell = scn.GetShadowCard("fell");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(fell);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, khamul);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(hound));
		scn.PassSkirmishActions();

		// Khamul (12) beats Aragorn (8).  He is the only Nazgûl: two Nazgûl cannot exert, so he exerts twice, with
		// nothing asked; the Hound could exert, and is no Nazgûl.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fell));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.ShadowDecisionAvailable("Exert"));
	}

	@Test
	public void TwoNazgulWhoCanEachExertOnlyOnceEachExertOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		var fell = scn.GetShadowCard("fell");
		var lemenya = scn.GetShadowCard("lemenya");
		var nertea = scn.GetShadowCard("nertea");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(fell);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, nertea, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(nertea));
		assertEquals(0, scn.GetWoundsOn(hound));
		scn.PassSkirmishActions();

		// Lemenya (9) beats Boromir (7).  Neither Nazgûl has the vitality to exert twice, so two Nazgûl exert, with
		// nothing asked: the two Nazgûl are the only ones who can, and the Hound, who could, is not taken.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fell));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(1, scn.GetWoundsOn(nertea));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.ShadowDecisionAvailable("Exert"));
	}

	@Test
	public void NotOfferedWhenANazgulLosesNorWhenAMinionThatIsNoNazgulWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var fell = scn.GetShadowCard("fell");
		var enquea = scn.GetShadowCard("enquea");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(fell);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea, lemenya, hound);
		// Aragorn at 10 beats Lemenya (9) without doubling her; the Hound at 8 beats Gimli (6) without doubling him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));
		scn.ApplyAdHocModifier(new StrengthModifier(null, hound, null, 3));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{gimli, hound});
		scn.ShadowDeclineAssignments();

		// Enquëa, unassigned and unwounded, could pay either price all along.
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetThreats());
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Lemenya lost and lives, with one wound: nothing is offered.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// The Hound won: nothing is offered.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWhenNoNazgulCanPayEitherPrice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		var fell = scn.GetShadowCard("fell");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(fell);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertEquals(0, scn.GetThreats());
		scn.PassSkirmishActions();

		// Lemenya wins, but she is the only Nazgûl and can exert only once; the Hound, who could exert, is no Nazgûl.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertEquals(0, scn.GetThreats());
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void DecliningLeavesEverythingAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var fell = scn.GetShadowCard("fell");
		var enquea = scn.GetShadowCard("enquea");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToSupportArea(fell);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea, lemenya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetThreats());
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fell));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignmentAbilityRemovesFourToPlayANazgulFromYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var freepsToldea = scn.GetFreepsCard("toldea");
		scn.MoveCardsToFreepsDiscard("toldea");

		var fell = scn.GetShadowCard("fell");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(fell);
		scn.MoveCardsToShadowDiscard("nelya", "khamul", "hound");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, fell);
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(14);
		scn.FreepsPass();

		assertInDiscard(nelya, khamul, hound, freepsToldea);
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(fell));
		scn.ShadowUseCardAction(fell);

		// Either Nazgûl in his own discard pile, of either culture; not the Hound, and not the Nazgûl in the other
		// player's discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, freepsToldea));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(nelya);

		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertInDiscard(khamul, hound, freepsToldea);
		assertInZone(Zone.SUPPORT, fell);
		// 14 - 4 removed - 5 for Nelya (site 2, her own number: not roaming)
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignmentAbilityIsNotOfferedUntilANazgulIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell = scn.GetShadowCard("fell");
		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(fell);
		scn.MoveCardsToShadowDiscard("nelya");

		scn.StartGame();
		// The Hound is a [wraith] minion and no Nazgûl.
		scn.MoveMinionsToTable(hound);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(14);
		scn.FreepsPass();

		assertInDiscard(nelya);
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(fell));
	}

	@Test
	public void TheRaiderNazgulIsSpottedAndThePoolMustPayTheFourAndTheNazgulPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell = scn.GetShadowCard("fell");
		var khamul = scn.GetShadowCard("khamul");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToSupportArea(fell);
		scn.MoveCardsToShadowDiscard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// 4 removed + 5 for Nelya = 9: one short.
		scn.SetTwilight(8);
		scn.FreepsPass();

		assertInDiscard(nelya);
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(fell));

		scn.SetTwilight(9);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(fell));

		scn.ShadowUseCardAction(fell);
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWithNoNazgulInTheDiscardPileThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell = scn.GetShadowCard("fell");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(fell);
		scn.MoveCardsToShadowDiscard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(14);
		scn.FreepsPass();

		// Only the Hound, no Nazgûl, lies in the discard pile.
		assertInDiscard(hound);
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(fell));
	}
}
