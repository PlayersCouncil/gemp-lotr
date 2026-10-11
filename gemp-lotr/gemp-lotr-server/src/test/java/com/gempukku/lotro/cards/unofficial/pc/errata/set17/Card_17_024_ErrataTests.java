package com.gempukku.lotro.cards.unofficial.pc.errata.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_024_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadowfax", "67_24");

					put("gandalf", "1_364");     // Gandalf, The Grey Wizard: strength 7, vitality 4, resistance 6; a fellowship ability used only on purpose
					put("radagast", "9_26");     // Radagast, The Brown: the [gandalf] Wizard who is not Gandalf; move limit +1, and a trigger on a regroup move
					put("saruman", "32_24");     // Saruman, The White: the [gandalf] Wizard who is an ally; gives Gandalf defender +1 and has abilities used only on purpose
					put("avenger", "6_27");      // Ent Avenger: a [gandalf] companion who is no Wizard

					put("glamdring", "1_75");    // Glamdring: possession, hand weapon, cost 2; bearer must be Gandalf
					put("hobbitglamdring", "30_29"); // Glamdring (The Hobbit): [gandalf] artifact, hand weapon, bearer must be Gandalf; its trigger needs a skirmish won
					put("pipe", "1_74");         // Gandalf's Pipe: possession, pipe, cost 0; bearer must be Gandalf
					put("wizardstaff", "2_29");  // Wizard Staff: possession, staff, cost 2; bearer must be a Wizard
					put("bag", "18_26");         // Radagast's Herb Bag: possession with no class, cost 1; bearer must be a [gandalf] Wizard
					put("staff", "4_91");        // Gandalf's Staff, Walking Stick: an artifact that is no weapon; vitality +1
					put("poison", "8_28");       // Spider Poison: a Shadow possession that a companion bears
					put("evil", "1_283");        // You Bring Great Evil: a Shadow condition that a companion bears; only matters at a sanctuary
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShadowfaxStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Shadowfax, Greatest of the Mearas
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: +1
		 * Vitality: +1
		 * Resistance: +1
		 * Game Text: Bearer must be a [gandalf] Wizard.
		 * Discard all other possessions and weapons on bearer.
		 * Each time the fellowship moves, you may exert bearer and add a threat to remove a burden.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shadowfax");

		assertEquals("Shadowfax", card.getBlueprint().getTitle());
		assertEquals("Greatest of the Mearas", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(1, card.getBlueprint().getResistance());
	}

	@Test
	public void PlaysForTwoOnAGandalfWizardWhoIsOneBetterInStrengthVitalityAndResistance() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var saruman = scn.GetFreepsCard("saruman");
		var avenger = scn.GetFreepsCard("avenger");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, radagast, avenger);
		scn.MoveAlliesToTable(saruman);
		scn.MoveCardsToHand(shadowfax);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(4, scn.GetVitality(gandalf));
		assertEquals(6, scn.GetResistance(gandalf));
		assertTrue(scn.FreepsPlayAvailable(shadowfax));
		scn.FreepsPlayCard(shadowfax);

		// Ent Avenger is a [gandalf] companion and no Wizard; Frodo is neither.  Every printed Free Peoples
		// Wizard is a [gandalf] card, so no Wizard of another culture stands here (Q-0213).
		// Saruman is a [gandalf] Wizard who is an ally, not a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, radagast, saruman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(avenger, frodo));
		scn.FreepsChooseCard(gandalf);

		assertEquals(2, scn.GetTwilight());
		assertAttachedTo(shadowfax, gandalf);
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(5, scn.GetVitality(gandalf));
		assertEquals(7, scn.GetResistance(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenPlayedItDiscardsEveryOtherPossessionOnItsBearerAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var glamdring = scn.GetFreepsCard("glamdring");
		var pipe = scn.GetFreepsCard("pipe");
		var staff = scn.GetFreepsCard("staff");
		var wizardstaff = scn.GetFreepsCard("wizardstaff");
		var poison = scn.GetShadowCard("poison");
		var evil = scn.GetShadowCard("evil");
		scn.MoveCompanionsToTable(gandalf, radagast);
		// On Gandalf: a hand weapon, a possession of another class, a Shadow possession, an artifact and a condition.
		scn.AttachCardsTo(gandalf, glamdring, pipe, poison, staff, evil);
		scn.AttachCardsTo(radagast, wizardstaff);
		scn.MoveCardsToHand(shadowfax);

		scn.StartGame();

		assertAttachedTo(glamdring, gandalf);
		assertAttachedTo(pipe, gandalf);
		assertAttachedTo(poison, gandalf);
		scn.FreepsPlayCard(shadowfax);
		scn.FreepsChooseCard(gandalf);

		assertAttachedTo(shadowfax, gandalf);
		assertInDiscard(glamdring, pipe, poison);
		// An artifact that is no weapon and a condition are neither possessions nor weapons.
		assertAttachedTo(staff, gandalf);
		assertAttachedTo(evil, gandalf);
		// Another companion's possession is not on the bearer.
		assertAttachedTo(wizardstaff, radagast);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenPlayedItDiscardsAnArtifactThatIsAWeaponAndKeepsOneThatIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("hobbitglamdring");
		var staff = scn.GetFreepsCard("staff");
		scn.MoveCompanionsToTable(gandalf);
		// Two artifacts, neither a possession: a hand weapon, and a staff that is no weapon.
		scn.AttachCardsTo(gandalf, glamdring, staff);
		scn.MoveCardsToHand(shadowfax);

		scn.StartGame();

		assertAttachedTo(glamdring, gandalf);
		assertAttachedTo(staff, gandalf);
		scn.FreepsPlayCard(shadowfax);

		assertAttachedTo(shadowfax, gandalf);
		assertInDiscard(glamdring);
		assertAttachedTo(staff, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void APossessionPlayedOnOrTransferredToTheBearerLaterIsDiscardedAsItArrives() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var wizardstaff = scn.GetFreepsCard("wizardstaff");
		var bag = scn.GetFreepsCard("bag");
		scn.MoveCompanionsToTable(gandalf, radagast);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.AttachCardsTo(radagast, bag);
		scn.MoveCardsToHand(wizardstaff);

		scn.StartGame();

		// A possession played on the bearer: its cost is paid and it goes to the discard pile.
		assertTrue(scn.FreepsPlayAvailable(wizardstaff));
		scn.FreepsPlayCard(wizardstaff);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, radagast));
		scn.FreepsChooseCard(gandalf);
		assertEquals(2, scn.GetTwilight());
		assertInDiscard(wizardstaff);

		// A possession transferred to the bearer: Gandalf is the only other Wizard who could bear the Herb Bag.
		assertTrue(scn.FreepsTransferAvailable(bag));
		scn.FreepsTransferCard(bag);
		// 2 for the Wizard Staff + 1 for the transfer
		assertEquals(3, scn.GetTwilight());
		assertInDiscard(bag);

		assertAttachedTo(shadowfax, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHandWeaponPlayedOnTheBearerIsDiscardedAsItArrives() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.MoveCardsToHand(glamdring);

		scn.StartGame();

		assertEquals(7 + 1, scn.GetStrength(gandalf));
		assertTrue(scn.FreepsPlayAvailable(glamdring));
		scn.FreepsPlayCard(glamdring);

		// Its cost is paid and it is gone: Gandalf has Shadowfax's +1 and not Glamdring's +2.
		assertEquals(2, scn.GetTwilight());
		assertInDiscard(glamdring);
		assertEquals(7 + 1, scn.GetStrength(gandalf));
		assertAttachedTo(shadowfax, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// Comprehensive Rules 5.0, "hinder": while Shadowfax is hindered its text does not apply, and a possession
	// played on its bearer stays.  When it is restored the standing discard is in force again and the possession
	// is discarded (Ketura on Q-0223).  The restore here is the Free Peoples player's own reconcile.
	@Test
	public void APossessionTheBearerTookUpWhileItWasHinderedIsDiscardedWhenItIsRestored() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var pipe = scn.GetFreepsCard("pipe");
		var wizardstaff = scn.GetFreepsCard("wizardstaff");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, shadowfax);
		// The Wizard Staff stays in hand, so that the reconcile has a discard to ask about
		scn.MoveCardsToHand(pipe, wizardstaff);

		scn.StartGame();
		scn.HinderCard(shadowfax);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(pipe);
		assertAttachedTo(pipe, gandalf);
		assertTrue(scn.IsHindered(shadowfax));

		// The fellowship moves: a hindered Shadowfax offers nothing.  No minion, so the Shadow phase is followed
		// by the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassRegroupActions();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.IsHindered(shadowfax));
		assertAttachedTo(pipe, gandalf);

		scn.FreepsChooseToStay();
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		assertFalse(scn.IsHindered(shadowfax));
		assertAttachedTo(shadowfax, gandalf);
		assertInDiscard(pipe);
		assertInZone(Zone.HAND, wizardstaff);
	}

	@Test
	public void TransferredToAnotherWizardItDiscardsThePossessionHeBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var wizardstaff = scn.GetFreepsCard("wizardstaff");
		scn.MoveCompanionsToTable(gandalf, radagast);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.AttachCardsTo(radagast, wizardstaff);

		scn.StartGame();

		assertAttachedTo(wizardstaff, radagast);
		assertTrue(scn.FreepsTransferAvailable(shadowfax));
		scn.FreepsTransferCard(shadowfax);

		assertEquals(2, scn.GetTwilight());
		assertAttachedTo(shadowfax, radagast);
		assertInDiscard(wizardstaff);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenTheFellowshipMovesTheBearerMayExertAndAddAThreatToRemoveABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCompanionsToTable(gandalf, radagast);
		scn.AttachCardsTo(gandalf, shadowfax);

		scn.StartGame();
		scn.AddBurdens(2);

		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shadowfax));
		scn.FreepsAcceptOptionalTrigger();

		// The bearer exerts, not the other [gandalf] Wizard.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningChangesNothingAndTheOfferComesAgainAtTheSecondMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, shadowfax);

		scn.StartGame();
		scn.AddBurdens(1);

		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shadowfax));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shadowfax));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedOnceTheBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, shadowfax);
		// Vitality 4 + 1 for Shadowfax, with 3 wounds: one exertion left.
		scn.AddWoundsToChar(gandalf, 3);

		scn.StartGame();
		scn.AddBurdens(2);

		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shadowfax));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(4, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());

		// A burden is left and a second threat could be added (2 companions), but Gandalf cannot exert.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(4, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedOnceThereAreAsManyThreatsAsCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, shadowfax);

		scn.StartGame();
		scn.AddBurdens(2);
		// Frodo and Gandalf: room for one more threat.
		scn.AddThreats(1);

		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shadowfax));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());

		// A burden is left and Gandalf could exert again, but no threat can be added.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BorneByRadagastItExertsHimAndIsOfferedWithNoBurdenToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCompanionsToTable(gandalf, radagast);
		scn.MoveCardsToHand(shadowfax);

		scn.StartGame();

		scn.FreepsPlayCard(shadowfax);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, radagast));
		scn.FreepsChooseCard(radagast);
		assertAttachedTo(shadowfax, radagast);

		assertEquals(0, scn.GetBurdens());
		scn.FreepsPass();
		// The offer has a cost, so it is made though there is no burden to remove.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shadowfax));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(radagast));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
