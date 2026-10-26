package com.gempukku.lotro.cards.official.set08;

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

public class Card_08_073_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("madness", "8_73");
					put("cantea", "1_230");     // Úlairë Cantëa, Lieutenant of Dol Guldur: [wraith] Nazgul, strength 10, vitality 3; fierce; a skirmish ability used only on purpose
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgul, strength 12; fierce; his text needs 3 or 5 burdens
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgul, strength 9, vitality 2; not fierce; his text reads [wraith] conditions on companions
					put("nertea", "1_234");     // Úlairë Nertëa, Messenger of Dol Guldur: [wraith] Nazgul, strength 9, vitality 2; not fierce; a play trigger only
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, no Nazgul, strength 5; its text needs 2 wounds on the Ring-bearer
					put("steed", "1_208");      // Black Steed: a Shadow mount on a Nazgul; its text reads plains and underground sites
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("rangersword", "1_112"); // Ranger's Sword: a Free Peoples possession that is no mount, on Aragorn
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7; a skirmish ability used only on purpose
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man; a skirmish ability used only on purpose
					put("ridermount", "4_287"); // Rider's Mount: [rohan] mount; exerts minions only at the start of its bearer's skirmish
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: a fellowship ability used only on purpose
					put("shadowfax", "4_100");  // Shadowfax: [gandalf] mount; acts on a hand weapon and at the start of Gandalf's skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MasteredByMadnessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Mastered By Madness
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a Nazgûl strength +2. If that Nazgûl wins this skirmish, you may discard a Free Peoples mount.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("madness");

		assertEquals("Mastered By Madness", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesANazgulStrengthPlus2AndWhenHeWinsTheSkirmishAFreePeoplesMountIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madness = scn.GetShadowCard("madness");
		var cantea = scn.GetShadowCard("cantea");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var steed = scn.GetShadowCard("steed");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		var rider = scn.GetFreepsCard("rider");
		var ridermount = scn.GetFreepsCard("ridermount");
		var gandalf = scn.GetFreepsCard("gandalf");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		scn.MoveCompanionsToTable(aragorn, rider, gandalf);
		scn.AttachCardsTo(aragorn, rangersword);
		scn.AttachCardsTo(rider, ridermount);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.MoveCardsToHand(madness);

		scn.StartGame();
		scn.SetTwilight(5);

		scn.MoveMinionsToTable(cantea, khamul, hound);
		scn.AttachCardsTo(khamul, steed);
		assertAttachedTo(rangersword, aragorn);
		assertAttachedTo(ridermount, rider);
		assertAttachedTo(shadowfax, gandalf);
		assertAttachedTo(steed, khamul);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, cantea);
		// Khamul and the Hound are left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(10, scn.GetStrength(cantea));
		// 12, + 2 for Black Steed, + 2 more at a plains site (Ettenmoors).
		assertEquals(16, scn.GetStrength(khamul));
		assertTrue(scn.ShadowPlayAvailable(madness));
		scn.ShadowPlayCard(madness);

		// A Nazgul of each culture, the one fighting and one in no skirmish; not the [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(cantea, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(cantea);
		assertEquals(12, scn.GetStrength(cantea));
		assertEquals(16, scn.GetStrength(khamul));
		assertInDiscard(madness);

		// 12 against Aragorn's 8 + 2 for Ranger's Sword.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// The Free Peoples mounts of two cultures; not the Shadow mount, nor a Free Peoples possession that is no mount.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ridermount, shadowfax));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(steed, rangersword));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(shadowfax);

		assertInDiscard(shadowfax);
		assertAttachedTo(ridermount, rider);
		assertAttachedTo(steed, khamul);
		assertAttachedTo(rangersword, aragorn);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
	}

	@Test
	public void TheDiscardMayBeDeclinedAndAFierceSkirmishWonIsAnsweredByNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madness = scn.GetShadowCard("madness");
		var cantea = scn.GetShadowCard("cantea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.MoveCardsToHand(madness);

		scn.StartGame();
		scn.SetTwilight(5);

		scn.MoveMinionsToTable(cantea);
		assertAttachedTo(shadowfax, gandalf);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, cantea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Cantëa is the only Nazgul and is made stronger with nothing asked.
		scn.ShadowPlayCard(madness);
		assertEquals(12, scn.GetStrength(cantea));

		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();
		assertAttachedTo(shadowfax, gandalf);

		// Cantëa is fierce: her fierce skirmish is another skirmish, and the bonus is gone.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, cantea);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(10, scn.GetStrength(cantea));
		scn.FreepsPass();
		scn.ShadowPass();
		// 10 against 8: she wins again, and nothing is offered.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertAttachedTo(shadowfax, gandalf);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsAskedWhenThatNazgulLosesTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madness = scn.GetShadowCard("madness");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.MoveCardsToHand(madness);

		scn.StartGame();
		scn.SetTwilight(5);

		scn.MoveMinionsToTable(lemenya);
		// Aragorn at 13, to beat Lemenya's 9 + 2 without being twice her strength.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		assertAttachedTo(shadowfax, gandalf);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		scn.ShadowPlayCard(madness);
		assertEquals(11, scn.GetStrength(lemenya));
		assertEquals(13, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(lemenya));

		scn.FreepsPass();
		scn.ShadowPass();
		// Lemenya lost, took the wound for it and lives (vitality 2); nothing is asked.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertAttachedTo(shadowfax, gandalf);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PlayedOnANazgulWhoIsNotFightingItLastsThatSkirmishAndNeitherWinIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madness = scn.GetShadowCard("madness");
		var lemenya = scn.GetShadowCard("lemenya");
		var nertea = scn.GetShadowCard("nertea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		scn.MoveCompanionsToTable(aragorn, boromir, gandalf);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.MoveCardsToHand(madness);

		scn.StartGame();
		scn.SetTwilight(5);

		scn.MoveMinionsToTable(lemenya, nertea);
		assertAttachedTo(shadowfax, gandalf);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{boromir, nertea});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Played on Nertëa, whose skirmish with Boromir is still to come, while Lemenya fights Aragorn.
		scn.ShadowPlayCard(madness);
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, nertea));
		scn.ShadowChooseCard(nertea);
		assertEquals(11, scn.GetStrength(nertea));
		assertEquals(9, scn.GetStrength(lemenya));

		// Lemenya, 9 against 8, wins: she is not that Nazgul, and nothing is asked.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// Nertëa's own skirmish is not the one the event was played in: the bonus is gone, and her win, 9 against
		// Boromir's 7, is answered by nothing.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(9, scn.GetStrength(nertea));
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertAttachedTo(shadowfax, gandalf);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoFreePeoplesMountInPlayAWinIsAnsweredByNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madness = scn.GetShadowCard("madness");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var steed = scn.GetShadowCard("steed");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, rangersword);
		scn.MoveCardsToHand(madness);

		scn.StartGame();
		scn.SetTwilight(5);

		scn.MoveMinionsToTable(lemenya, khamul);
		scn.AttachCardsTo(khamul, steed);
		assertAttachedTo(rangersword, aragorn);
		assertAttachedTo(steed, khamul);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		scn.ShadowPlayCard(madness);
		scn.ShadowChooseCard(lemenya);
		assertEquals(11, scn.GetStrength(lemenya));

		// Ranger's Sword gives Aragorn +2: 11 against 10.  Only a Shadow mount and a possession that is no mount
		// are in play, so there is nothing to discard and nothing is asked.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertAttachedTo(steed, khamul);
		assertAttachedTo(rangersword, aragorn);
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhaseAndCanInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madness = scn.GetShadowCard("madness");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(madness);

		scn.StartGame();
		scn.SetTwilight(5);

		scn.MoveMinionsToTable(lemenya);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(madness));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(madness));
		scn.ShadowPlayCard(madness);
		assertEquals(11, scn.GetStrength(lemenya));
		assertInDiscard(madness);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoNazgulInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madness = scn.GetShadowCard("madness");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(madness);

		scn.StartGame();
		scn.SetTwilight(5);

		scn.MoveMinionsToTable(hound);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, hound);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.ShadowPlayAvailable(madness));
		scn.ShadowPlayCard(madness);
		// The [wraith] Orc is no Nazgul: nothing is made stronger.
		assertEquals(5, scn.GetStrength(hound));
		assertInDiscard(madness);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
