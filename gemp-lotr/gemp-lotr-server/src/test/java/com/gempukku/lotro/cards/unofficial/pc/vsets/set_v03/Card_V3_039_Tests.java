package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_039_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fury1", "103_39");
					put("fury2", "103_39");
					put("fury3", "103_39");
					put("party", "1_158");      // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3; damage +1 is its whole text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					put("rider", "5_53");       // Isengard Rider: [isengard] Orc, strength 8, vitality 3; fierce while not exhausted
					put("retainer", "3_57");    // Isengard Retainer: [isengard] Orc, strength 8; a regroup ability never used here
					put("feral", "11_183");     // Feral Uruk: [uruk-hai] Uruk-hai, strength 9, vitality 2; its text needs a battleground
					put("runner", "1_178");     // Goblin Runner: [moria] Orc
					put("hollowing", "3_54");   // Hollowing of Isengard: a Shadow card that is no minion
					put("poison", "8_28");      // Spider Poison: Shadow possession a companion bears; bearer exerts when assigned to a skirmish

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3
					put("athelas", "1_94");     // Athelas: possession, bearer must be a [gondor] Man; a fellowship ability
					put("athelas2", "1_94");
					put("stranger", "1_108");   // No Stranger to the Shadows: condition, bearer must be a ranger
					put("sword", "1_299");      // Hobbit Sword: possession, bearer must be a Hobbit
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: ally, home site 3, strength 8, vitality 4
					put("tale", "1_66");        // The Tale of Gil-galad: condition, bearer must be an Elf; vitality +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SavageFuryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Savage Fury
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time your [isengard] minion wins a skirmish, you may discard another Savage Fury to
		 * 	hinder each Free Peoples card on a companion it was skirmishing.
		 * 	Maneuver: Discard a minion to make your [isengard] minion <b>fierce</b> or <b>relentless</b> until the
		 * 	regroup phase <i>(it participates in 1 extra round of skirmishes after fierce)</i>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fury1");

		assertEquals("Savage Fury", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysForOneToTheSupportAreaAndATwoDotCardMayHaveTwoCopiesInPlayButNotThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury1 = scn.GetShadowCard("fury1");
		var fury2 = scn.GetShadowCard("fury2");
		var fury3 = scn.GetShadowCard("fury3");
		scn.MoveCardsToHand(fury1, fury2, fury3);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 5 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(fury1));

		scn.ShadowPlayCard(fury1);
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, fury1);
		assertTrue(scn.ShadowPlayAvailable(fury2));

		scn.ShadowPlayCard(fury2);
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, fury1, fury2);
		assertFalse(scn.ShadowPlayAvailable(fury3));
		assertInZone(Zone.HAND, fury3);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WinningIsengardMinionMayDiscardTheOtherCopyToHinderEachFreePeoplesCardOnTheLoser() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");
		var stranger = scn.GetFreepsCard("stranger");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		// On the companion who will lose: a Free Peoples possession, a Free Peoples condition and a Shadow
		// possession. On a companion who is not in the skirmish: a Free Peoples possession.
		scn.AttachCardsTo(aragorn, athelas, stranger);
		scn.AttachCardsTo(frodo, sword);

		var fury1 = scn.GetShadowCard("fury1");
		var fury2 = scn.GetShadowCard("fury2");
		var party = scn.GetShadowCard("party");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToSupportArea(fury1, fury2);
		scn.AttachCardsTo(aragorn, poison);

		scn.StartGame();
		scn.MoveMinionsToTable(party);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, party);
		scn.FreepsResolveSkirmish(aragorn);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		scn.PassSkirmishActions();

		// Raiding Party 9 beats Aragorn 8: 1 exertion for Spider Poison + 2 wounds from a damage +1 minion
		assertEquals(3, scn.GetWoundsOn(aragorn));
		// Each copy offers to discard the other.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fury1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fury2));
		assertFalse(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(stranger));

		scn.ShadowChooseOptionalTrigger(fury1);
		assertInZone(Zone.SUPPORT, fury1);
		assertInZone(Zone.DISCARD, fury2);
		assertTrue(scn.IsHindered(athelas));
		assertTrue(scn.IsHindered(stranger));
		assertFalse(scn.IsHindered(poison));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(sword));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(fury1));

		// The copy left has no other to discard.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOneRingOnARingBearerItBeatIsNotHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sword = scn.GetFreepsCard("sword");
		scn.AttachCardsTo(frodo, sword);

		var fury1 = scn.GetShadowCard("fury1");
		var fury2 = scn.GetShadowCard("fury2");
		var party = scn.GetShadowCard("party");
		scn.MoveCardsToSupportArea(fury1, fury2);

		scn.StartGame();
		scn.MoveMinionsToTable(party);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(frodo, party);
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();
		// Raiding Party 9 beats Frodo 3 + 2 for the sword, and is short of double. 2 wounds from a damage +1
		// minion; the Ring's offer to take each as a burden is declined.
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fury1));
		assertFalse(scn.IsHindered(sword));
		assertFalse(scn.IsHindered(ring));

		scn.ShadowChooseOptionalTrigger(fury1);
		assertInZone(Zone.DISCARD, fury2);
		// The Ring-bearer bears The One Ring, which is a card of neither side.
		assertTrue(scn.IsHindered(sword));
		assertFalse(scn.IsHindered(ring));
		assertFalse(scn.IsHindered(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningKeepsBothCopiesAndASecondWinIsOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var athelas = scn.GetFreepsCard("athelas");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, athelas);

		var fury1 = scn.GetShadowCard("fury1");
		var fury2 = scn.GetShadowCard("fury2");
		// An [isengard] minion that is no Uruk-hai, and fierce by its own text.
		var rider = scn.GetShadowCard("rider");
		scn.MoveCardsToSupportArea(fury1, fury2);

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(boromir, rider);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		// Isengard Rider 8 beats Boromir 7
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fury1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fury2));

		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, fury1, fury2);
		assertFalse(scn.IsHindered(athelas));

		// The fierce round.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, rider);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fury1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fury2));

		// This time the second copy's offer is taken, and it is the first that pays for it.
		scn.ShadowChooseOptionalTrigger(fury2);
		assertInZone(Zone.SUPPORT, fury2);
		assertInZone(Zone.DISCARD, fury1);
		assertTrue(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(boromir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenAnUrukOfAnotherCultureWinsNorWhenAnIsengardMinionLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var athelas = scn.GetFreepsCard("athelas");
		var athelas2 = scn.GetFreepsCard("athelas2");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(boromir, athelas);
		scn.AttachCardsTo(aragorn, athelas2);

		var fury1 = scn.GetShadowCard("fury1");
		var fury2 = scn.GetShadowCard("fury2");
		var feral = scn.GetShadowCard("feral");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(fury1, fury2);

		scn.StartGame();
		scn.MoveMinionsToTable(feral, savage);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { boromir, feral },
				new PhysicalCardImpl[] { aragorn, savage }
		);

		// An Uruk-hai of the [uruk-hai] culture wins: Feral Uruk 9 beats Boromir 7.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		// An [isengard] minion loses: Aragorn 8 beats Uruk Savage 5.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		assertInZone(Zone.SUPPORT, fury1, fury2);
		assertFalse(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(athelas2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoOtherCopyInPlayTheWinIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, athelas);

		var fury1 = scn.GetShadowCard("fury1");
		var fury2 = scn.GetShadowCard("fury2");
		var fury3 = scn.GetShadowCard("fury3");
		var party = scn.GetShadowCard("party");
		scn.MoveCardsToSupportArea(fury1);
		// Other copies there are, but not in play.
		scn.MoveCardsToHand(fury2);
		scn.MoveCardsToDiscard(fury3);

		scn.StartGame();
		scn.MoveMinionsToTable(party);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, party);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Raiding Party 9 beats Aragorn 8, with damage +1
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, fury1);
		assertFalse(scn.IsHindered(athelas));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CardsOnAnAllyItBeatAreNotHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var tale = scn.GetFreepsCard("tale");
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, tale);

		var fury1 = scn.GetShadowCard("fury1");
		var fury2 = scn.GetShadowCard("fury2");
		var party = scn.GetShadowCard("party");
		scn.MoveCardsToSupportArea(fury1, fury2);

		scn.StartGame();
		// Elrond's home is site 3, where he may be assigned to a skirmish.
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(party);
		scn.FreepsPass();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(elrond, party);
		scn.FreepsResolveSkirmish(elrond);
		scn.PassSkirmishActions();

		// Raiding Party 9 beats Elrond 8, with damage +1
		assertEquals(2, scn.GetWoundsOn(elrond));
		// The other copy can be discarded, so the offer is made.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fury1));

		scn.ShadowChooseOptionalTrigger(fury1);
		assertInZone(Zone.DISCARD, fury2);
		// It was skirmishing an ally and no companion.
		assertFalse(scn.IsHindered(tale));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ManeuverActionDiscardsAnyMinionAndMakesAnIsengardMinionOfTheShadowPlayersChoiceFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury1 = scn.GetShadowCard("fury1");
		var hollowing = scn.GetShadowCard("hollowing");
		var party = scn.GetShadowCard("party");
		var retainer = scn.GetShadowCard("retainer");
		var feral = scn.GetShadowCard("feral");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(fury1, hollowing);

		scn.StartGame();
		scn.MoveMinionsToTable(party, retainer, feral, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(fury1));
		assertFalse(scn.HasKeyword(retainer, Keyword.FIERCE));

		scn.ShadowUseCardAction(fury1);
		// Any minion pays: [isengard] Uruk-hai and Orc, an [uruk-hai] Uruk-hai, a [moria] Orc. No other Shadow card does.
		assertTrue(scn.ShadowHasCardChoiceAvailable(party, retainer, feral, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hollowing, fury1));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(runner);
		assertInZone(Zone.DISCARD, runner);
		assertInZone(Zone.SHADOW_CHARACTERS, party, retainer, feral);

		scn.ShadowChoose("Fierce");
		// An [isengard] minion of either race; not the Uruk-hai of another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(party, retainer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(feral));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(retainer);
		assertTrue(scn.HasKeyword(retainer, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(retainer, Keyword.RELENTLESS));
		assertFalse(scn.HasKeyword(party, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(feral, Keyword.FIERCE));
		assertInZone(Zone.SUPPORT, fury1);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void FierceLastsThroughTheFierceSkirmishAndIsGoneAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var fury1 = scn.GetShadowCard("fury1");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(fury1);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowUseCardAction(fury1);
		scn.ShadowChooseCard(runner);
		scn.ShadowChoose("Fierce");
		// Uruk Savage is the only [isengard] minion left.
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));

		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		scn.FreepsPass();
		// A Maneuver ability, with a minion still there to discard, is not offered in the archery phase.
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(fury1));
		scn.ShadowPass();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// Aragorn 8 beats Uruk Savage 5
		assertEquals(1, scn.GetWoundsOn(savage));

		// A second round of assignments, for the fierce minion.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
	}

	@Test
	public void RelentlessBringsARelentlessRoundAndIsGoneAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var fury1 = scn.GetShadowCard("fury1");
		var savage = scn.GetShadowCard("savage");
		var feral = scn.GetShadowCard("feral");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(fury1);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, feral, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(savage, Keyword.RELENTLESS));

		scn.ShadowUseCardAction(fury1);
		scn.ShadowChooseCard(runner);
		scn.ShadowChoose("Relentless");
		// Uruk Savage is the only [isengard] minion left; the Uruk-hai of another culture is not asked about.
		assertTrue(scn.HasKeyword(savage, Keyword.RELENTLESS));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(feral, Keyword.RELENTLESS));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		// Feral Uruk is left out of the skirmishes.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// Aragorn 8 beats Uruk Savage 5
		assertEquals(1, scn.GetWoundsOn(savage));

		// No minion is fierce, so the next round is the relentless one.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.HasKeyword(savage, Keyword.RELENTLESS));
		scn.PassAssignmentActions();
		assertTrue(scn.FreepsCanAssign(savage));
		assertFalse(scn.FreepsCanAssign(feral));
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.HasKeyword(savage, Keyword.RELENTLESS));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertFalse(scn.HasKeyword(savage, Keyword.RELENTLESS));
	}

	@Test
	public void WithOneMinionInPlayItIsStillOfferedAndDiscardsThatMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury1 = scn.GetShadowCard("fury1");
		var party = scn.GetShadowCard("party");
		scn.MoveCardsToSupportArea(fury1);

		scn.StartGame();
		scn.MoveMinionsToTable(party);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The cost can be paid, so the action is there, though nobody will be left to gain anything.
		assertTrue(scn.ShadowActionAvailable(fury1));

		scn.ShadowUseCardAction(fury1);
		assertInZone(Zone.DISCARD, party);
		// No minion is left to be given either keyword, so the Shadow player is not asked which (F-0116).
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertInZone(Zone.SUPPORT, fury1);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
