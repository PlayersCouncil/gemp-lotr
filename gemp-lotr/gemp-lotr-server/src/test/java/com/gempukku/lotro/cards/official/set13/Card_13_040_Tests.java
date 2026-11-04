package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_040_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadowfax", "13_40");
					// Gandalf, The Grey Wizard: a Wizard companion, strength 7; his Fellowship ability is used only
					// on purpose
					put("gandalf", "1_364");
					// Radagast, The Brown: a Wizard ally (Hobbit set, Q-0312: the one Free Peoples Wizard who is no
					// companion); his Fellowship ability is used only on purpose
					put("radagast", "31_17");
					// Ent Avenger: a [gandalf] companion who is no Wizard; his text wants 3 Ents
					put("avenger", "6_27");
					// Aragorn, Ranger of the North: a companion of another culture and race; his Maneuver ability is
					// used only on purpose
					put("aragorn", "1_89");
					// Glamdring: another [gandalf] possession, played on Gandalf after Shadowfax
					put("glamdring", "1_75");
					// Fear and Great Wonder: a [gandalf] condition that takes a [gandalf] token as it is played; its
					// Maneuver ability is used only on purpose
					put("fear", "13_30");
					// Not the First Halfling: a [gandalf] condition that gets [gandalf] tokens by its Fellowship
					// ability; its Skirmish ability is used only on purpose
					put("halfling1", "8_18");
					put("halfling2", "8_18");
					// Garrison of Osgiliath: a [gondor] condition that holds [gondor] tokens
					put("garrison", "6_52");
					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc with no text
					put("troop2", "1_177");
					// Uruk-hai Raiding Party: an [isengard] Uruk-hai, vitality 3, damage +1
					put("party", "1_158");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetHorseScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadowfax", "13_40");
					// Gandalf, The Grey Wizard: a Wizard companion; his Fellowship ability is used only on purpose
					put("gandalf", "1_364");
					// Horse of Rohan: a mount; to play, spot a [rohan] Man; bearer must be a Man, Elf, or Wizard
					put("horse1", "4_283");
					put("horse2", "4_283");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; Valiant and no other text
					put("rider", "4_265");      // Elite Rider: a [rohan] Man companion; his text is about his own cost and strength
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShadowfaxStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Shadowfax, Roaring Wind
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: +1
		 * Game Text: Bearer must be a Wizard.
		 * When you play Shadowfax, you may reinforce a [gandalf] token.
		 * <b>Skirmish:</b> Remove 2 [gandalf] tokens to wound a minion skirmishing bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shadowfax");

		assertEquals("Shadowfax", card.getBlueprint().getTitle());
		assertEquals("Roaring Wind", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlayedOnAWizardItMayReinforceAGandalfTokenOnACardThatHasOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var fear = scn.GetFreepsCard("fear");
		var halfling1 = scn.GetFreepsCard("halfling1");
		var halfling2 = scn.GetFreepsCard("halfling2");
		var garrison = scn.GetFreepsCard("garrison");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, avenger, aragorn);
		scn.MoveAlliesToTable(radagast);
		scn.MoveCardsToSupportArea(fear, halfling1, halfling2, garrison);
		scn.AddTokensToCard(fear, 1);
		scn.AddTokensToCard(halfling1, 1);
		scn.AddTokensToCard(garrison, 2);
		scn.MoveCardsToHand(shadowfax);

		scn.StartGame();

		assertEquals(1, scn.GetCultureTokensOn(fear));
		assertEquals(1, scn.GetCultureTokensOn(halfling1));
		assertEquals(0, scn.GetCultureTokensOn(halfling2));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		scn.FreepsPlayCard(shadowfax);
		// A Wizard companion and a Wizard ally may bear it; a [gandalf] companion who is no Wizard may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, radagast));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(avenger, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gandalf);
		assertAttachedTo(shadowfax, gandalf);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shadowfax));
		scn.FreepsAcceptOptionalTrigger();
		// Either [gandalf] card that has a [gandalf] token; not the [gandalf] card with none, nor the card with
		// [gondor] tokens.
		assertTrue(scn.FreepsHasCardChoiceAvailable(fear, halfling1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(halfling2, garrison));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(halfling1);
		assertEquals(1, scn.GetCultureTokensOn(fear));
		assertEquals(2, scn.GetCultureTokensOn(halfling1));
		assertEquals(0, scn.GetCultureTokensOn(halfling2));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheReinforcementMayBeDeclinedAndAGandalfPossessionPlayedAfterShadowfaxIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var fear = scn.GetFreepsCard("fear");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(fear);
		scn.AddTokensToCard(fear, 1);
		scn.MoveCardsToHand(shadowfax, glamdring);

		scn.StartGame();

		assertEquals(1, scn.GetCultureTokensOn(fear));
		// Gandalf is the only Wizard: Shadowfax goes on him unasked.
		scn.FreepsPlayCard(shadowfax);
		assertAttachedTo(shadowfax, gandalf);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shadowfax));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(fear));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(glamdring);
		assertAttachedTo(glamdring, gandalf);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(shadowfax));
		assertEquals(1, scn.GetCultureTokensOn(fear));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoGandalfTokenToReinforceTheReinforcementIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var halfling1 = scn.GetFreepsCard("halfling1");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(halfling1, garrison);
		scn.AddTokensToCard(garrison, 2);
		scn.MoveCardsToHand(shadowfax);

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(halfling1));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		scn.FreepsPlayCard(shadowfax);
		assertAttachedTo(shadowfax, gandalf);
		// A [gandalf] card with no token and [gondor] tokens: nothing to reinforce, so nothing is offered.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(shadowfax));
		assertEquals(0, scn.GetCultureTokensOn(halfling1));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemovesTwoGandalfTokensToWoundAMinionSkirmishingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var fear = scn.GetFreepsCard("fear");
		var halfling1 = scn.GetFreepsCard("halfling1");
		var garrison = scn.GetFreepsCard("garrison");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.MoveCardsToSupportArea(fear, halfling1, garrison);
		scn.AddTokensToCard(fear, 1);
		scn.AddTokensToCard(halfling1, 1);
		scn.AddTokensToCard(garrison, 2);

		scn.StartGame();
		assertAttachedTo(shadowfax, gandalf);
		assertEquals(1, scn.GetCultureTokensOn(fear));
		assertEquals(1, scn.GetCultureTokensOn(halfling1));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		scn.MoveMinionsToTable(troop1, troop2, party);
		// The Raiding Party has 1 vitality left: a wound kills it, where an exertion could not be made.
		scn.AddWoundsToChar(party, 2);
		assertEquals(2, scn.GetWoundsOn(party));
		scn.SkipToAssignments();

		// Gandalf faces a Troop and the Raiding Party; Aragorn the other Troop, whose skirmish comes later.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gandalf, troop1},
				new PhysicalCardImpl[]{aragorn, troop2});
		scn.ShadowAssignToMinions(gandalf, party);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.FreepsActionAvailable(shadowfax));
		scn.FreepsUseCardAction(shadowfax);
		// The two tokens come one card at a time: the [gandalf] cards, never the [gondor] one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(fear, halfling1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(garrison));
		scn.FreepsChooseCard(fear);
		assertEquals(0, scn.GetCultureTokensOn(fear));
		assertEquals(0, scn.GetCultureTokensOn(halfling1));
		assertEquals(2, scn.GetCultureTokensOn(garrison));

		// Either minion skirmishing Gandalf; not the Troop assigned to Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, party));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(party);

		assertInDiscard(party);
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void UsedInAnotherCompanionsSkirmishItWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var fear = scn.GetFreepsCard("fear");
		var halfling1 = scn.GetFreepsCard("halfling1");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.MoveCardsToSupportArea(fear, halfling1);
		scn.AddTokensToCard(fear, 1);
		scn.AddTokensToCard(halfling1, 1);

		scn.StartGame();
		assertAttachedTo(shadowfax, gandalf);
		assertEquals(1, scn.GetCultureTokensOn(fear));
		assertEquals(1, scn.GetCultureTokensOn(halfling1));
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gandalf, troop1},
				new PhysicalCardImpl[]{aragorn, troop2});
		// Aragorn's skirmish first, Gandalf's Troop still waiting for his.
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertTrue(scn.FreepsActionAvailable(shadowfax));
		scn.FreepsUseCardAction(shadowfax);
		scn.FreepsChooseCard(fear);

		// The tokens are paid; the Troop fighting Aragorn is not skirmishing bearer, and Gandalf's Troop is not
		// skirmishing yet.
		assertEquals(0, scn.GetCultureTokensOn(fear));
		assertEquals(0, scn.GetCultureTokensOn(halfling1));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOnlyOneGandalfToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var fear = scn.GetFreepsCard("fear");
		var garrison = scn.GetFreepsCard("garrison");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.MoveCardsToSupportArea(fear, garrison);
		scn.AddTokensToCard(fear, 1);
		scn.AddTokensToCard(garrison, 2);

		scn.StartGame();
		assertAttachedTo(shadowfax, gandalf);
		assertEquals(1, scn.GetCultureTokensOn(fear));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(gandalf, troop1);
		scn.FreepsResolveSkirmish(gandalf);

		// One [gandalf] token, beside two [gondor] ones: the cost cannot be paid in full.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(shadowfax));
		assertEquals(1, scn.GetCultureTokensOn(fear));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(0, scn.GetWoundsOn(troop1));
	}

	@Test
	public void HinderedOnGandalfItStillKeepsAHorseOfRohanOffHim() throws DecisionResultInvalidException, CardNotFoundException {
		// CR 5.0 "hinder": "If Shadowfax is hindered on Gandalf, you cannot play a Horse of Rohan on Gandalf."
		var scn = GetHorseScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var horse1 = scn.GetFreepsCard("horse1");
		var horse2 = scn.GetFreepsCard("horse2");
		var guard = scn.GetFreepsCard("guard");
		var rider = scn.GetFreepsCard("rider");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, guard, rider, aragorn);
		scn.MoveCardsToHand(horse1, horse2);

		scn.StartGame();

		// Without Shadowfax, Gandalf is a Wizard the Horse may go on.
		assertInHand(horse1, horse2);
		scn.FreepsPlayCard(horse1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, guard, rider, aragorn));
		scn.FreepsChooseCard(guard);
		assertAttachedTo(horse1, guard);

		scn.AttachCardsTo(gandalf, shadowfax);
		scn.HinderCard(shadowfax);
		scn.FreepsRefreshDecision();
		assertAttachedTo(shadowfax, gandalf);
		assertTrue(scn.IsHindered(shadowfax));

		scn.FreepsPlayCard(horse2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, guard));
		scn.FreepsChooseCard(rider);
		assertAttachedTo(horse2, rider);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
