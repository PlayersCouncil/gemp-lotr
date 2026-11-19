package com.gempukku.lotro.cards.official.set17;

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

public class Card_17_142_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ring", "17_142");
					// Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgûl, strength 9; his text is about characters bearing [wraith] conditions
					put("lemenya", "1_232");
					// Úlairë Otsëa, Lieutenant of Morgul: [wraith] Nazgûl; a Shadow ability only
					put("otsea", "1_235");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl, strength 12, vitality 3; fierce, his text needs 3 burdens
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] minion who is no Nazgûl (an Orc); his text needs 2 wounds on the Ring-bearer
					put("hound", "7_192");

					// Orc Chieftain: a [sauron] minion, a card for the Shadow player's hand
					put("chieftain", "1_266");
					// Bent on Discovery: [wraith] support-area condition; the Shadow player draws a card at the start of his Shadow phase
					put("bent", "1_206");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf, vitality 3
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf, vitality 3
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit, vitality 4
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] ally
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RingofSavageryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Ring of Savagery
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Strength: +1
		 * Game Text: Bearer must be a Nazgûl.<br>Each time bearer wins a skirmish, the Free Peoples player must either add a burden or exert a companion.<br><b>Response:</b> If a player reconciles, return bearer to his owner's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ring");

		assertEquals("Ring of Savagery", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RING));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void BearerMustBeANazgul() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, khamul, hound);
		scn.MoveCardsToHand(ring);
		scn.SkipToPhase(Phase.SHADOW);

		assertInZone(Zone.HAND, ring);
		assertTrue(scn.ShadowPlayAvailable(ring));

		scn.ShadowPlayCard(ring);

		// A [wraith] Nazgûl and a [raider] Nazgûl may bear it; a [wraith] Orc may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(khamul);

		assertAttachedTo(ring, khamul);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachWinOfTheBearerHasTheFreePeoplesPlayerAddABurdenOrExertACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, legolas, gimli, sam);
		scn.MoveAlliesToTable(barliman);

		var ring = scn.GetShadowCard("ring");
		var khamul = scn.GetShadowCard("khamul");

		scn.StartGame();
		// Sam is exhausted: 3 wounds of 4 vitality.
		scn.AddWoundsToChar(sam, 3);
		scn.MoveMinionsToTable(khamul);
		scn.AttachCardsTo(khamul, ring);

		assertAttachedTo(ring, khamul);
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, khamul);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// Khamûl, 12 + 1, beats Aragorn, 8; the Ring's trigger is ordered with the rule's skirmish wound.
		scn.FreepsResolveRuleFirst();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(List.of("Add a burden", "Exert a companion"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Add a burden");

		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(barliman));

		// Khamûl is fierce: he beats Aragorn again in the fierce round, and the Free Peoples player is asked again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, khamul);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(List.of("Add a burden", "Exert a companion"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Exert a companion");

		// Any companion who can exert, the Ring-bearer and the companion who lost included; not the exhausted Sam,
		// nor an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, legolas, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, barliman, khamul));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithEveryCompanionExhaustedTheBurdenIsAddedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var ring = scn.GetShadowCard("ring");
		var khamul = scn.GetShadowCard("khamul");

		scn.StartGame();
		// Frodo (vitality 4) and Aragorn (vitality 4) are both exhausted; Aragorn dies of the wound for losing.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(khamul);
		scn.AttachCardsTo(khamul, ring);

		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertAttachedTo(ring, khamul);
		assertEquals(0, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, khamul);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		// No companion can exert: the burden is added and nothing is asked.
		assertInZone(Zone.DEAD, aragorn);
		assertEquals(1, scn.GetBurdens());
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.InFierceRound());
	}

	@Test
	public void ALossOfTheBearerOrAWinOfAnotherNazgulAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var ring = scn.GetShadowCard("ring");
		var khamul = scn.GetShadowCard("khamul");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(khamul, lemenya);
		scn.AttachCardsTo(khamul, ring);
		// Aragorn is made strong enough to beat Khamûl (18 against 13) without overwhelming him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 10));

		assertAttachedTo(ring, khamul);
		assertEquals(0, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, khamul},
				new PhysicalCardImpl[]{gimli, lemenya});

		// Lemenya, a Nazgûl who does not bear the Ring, beats Gimli (9 against 6).
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The bearer loses his own skirmish and lives through it.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(khamul));
		assertInZone(Zone.SHADOW_CHARACTERS, khamul);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.InFierceRound());
	}

	@Test
	public void AtHisOwnReconcileTheShadowPlayerMayReturnTheBearerToHisHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var lemenya = scn.GetShadowCard("lemenya");
		var otsea = scn.GetShadowCard("otsea");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// Two Nazgûl that came through the skirmishes, one bearing the Ring.
		scn.MoveMinionsToTable(lemenya, otsea);
		scn.AttachCardsTo(lemenya, ring);
		scn.SetShadowHand(chieftain);

		assertAttachedTo(ring, lemenya);
		assertEquals(1, scn.GetShadowHandCount());

		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.ShadowDecisionAvailable("Reconcile"));
		scn.ShadowDeclineReconciliation();

		// The hand is made up to 8 before the response is offered.
		assertEquals(8, scn.GetShadowHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya, otsea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ring));

		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.HAND, lemenya);
		assertEquals(9, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, ring);
		assertInZone(Zone.SHADOW_CHARACTERS, otsea);
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void DeclinedAtHisOwnReconcileItIsOfferedAgainWhenTheFreePeoplesPlayerReconciles() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var lemenya = scn.GetShadowCard("lemenya");
		var otsea = scn.GetShadowCard("otsea");
		var chieftain = scn.GetShadowCard("chieftain");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(lemenya, otsea);
		scn.AttachCardsTo(lemenya, ring);
		scn.SetShadowHand(chieftain);
		scn.SetFreepsHand(aragorn);

		assertAttachedTo(ring, lemenya);

		scn.FreepsPass();
		scn.ShadowPass();
		scn.ShadowDeclineReconciliation();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ring));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, lemenya, otsea);
		assertTrue(scn.FreepsDecisionAvailable("another move"));

		scn.FreepsChooseToStay();

		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		scn.FreepsDeclineReconciliation();

		assertEquals(8, scn.GetFreepsHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya, otsea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ring));

		scn.ShadowAcceptOptionalTrigger();

		// Lemenya is back in hand; Otsëa is discarded with the other minions as the turn ends.
		assertInZone(Zone.HAND, lemenya);
		assertInZone(Zone.DISCARD, ring, otsea);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void ACardDrawnOutsideAReconcileIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var lemenya = scn.GetShadowCard("lemenya");
		var bent = scn.GetShadowCard("bent");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.AttachCardsTo(lemenya, ring);
		scn.MoveCardsToSupportArea(bent);
		int hand = scn.GetShadowHandCount();

		assertAttachedTo(ring, lemenya);

		scn.FreepsPass();

		// Bent on Discovery has drawn the Shadow player a card at the start of his Shadow phase.
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertAttachedTo(ring, lemenya);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
