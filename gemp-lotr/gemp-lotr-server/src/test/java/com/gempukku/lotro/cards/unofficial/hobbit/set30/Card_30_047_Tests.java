package com.gempukku.lotro.cards.unofficial.hobbit.set30;

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

public class Card_30_047_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 (strength 3, vitality 4) as the
	// Ring-bearer; his skirmish ability, played on a [dwarven] skirmish event from hand, is not used. The
	// skirmishes of turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sting", "30_47");
					// Thorin, Oakenshield: [dwarven] Dwarf companion, 7/4; +1 for each [dwarven] follower he bears
					// (none) and a fellowship ability
					put("thorin", "30_19");
					// Master Worrywort, Hobbit Farmer: a [shire] Hobbit ally who is no Bilbo; his texts answer
					// Bilbo winning and a skirmish event, and he is laid only where Sting is played
					put("worrywort", "33_53");
					// Gandalf, Leader of Dwarves: [gandalf] Wizard companion; "Skirmish: Play a [gandalf] skirmish
					// event and add a doubt to play a Dwarf from your draw deck"
					put("gandalf", "30_25");
					// Fili, Brother of Kili: the Dwarf companion Gandalf plays from the draw deck; a response to a
					// [dwarven] companion about to take a wound, never reached
					put("fili", "30_10");

					// Noble intentions: [dwarven] skirmish event, "Exert a companion (except Bilbo) to make Bilbo
					// strength +3"
					put("noble", "30_14");
					put("noble2", "30_14");
					// Battle Fury: [dwarven] skirmish event, "Make a Dwarf character strength +3"
					put("fury", "30_3");
					// Take up arms. Fight!: [gandalf] skirmish event, "Spot Gandalf to make a minion strength -3"
					put("takeup", "30_31");

					// Stairs of Khazad-dûm: a Free Peoples [dwarven] condition in the support area (the Hobbit sets
					// print no Free Peoples condition); +1 to a Dwarf skirmishing a [moria] minion, never reached
					put("stairs", "1_24");
					// Emeralds of Girion: a [dwarven] artifact in the support area, no condition; fellowship and
					// regroup abilities only
					put("emeralds", "31_3");
					// Hatred Rekindled: a [gundabad] condition in the Shadow support area; a Shadow ability only
					put("hatred", "30_34");
					// Goblin Swarms: a [moria] condition in the Shadow support area; a response to the Shadow
					// player's Orc winning, and a Shadow ability
					put("swarms", "31_34");
					// Azog's Mace: a [moria] possession, no condition, borne by a minion who fights nobody
					put("mace", "32_29");

					// Goblin Runner: [moria] Orc, 5/1; a text on being played, cheated in so it does not fire
					put("runner", "31_30");
					put("runner2", "31_30");
					// Dark Bats: [moria] Creature, 4/2; a Shadow ability not used
					put("bats", "32_30");
					// Wicked Spider: a fierce [spider] Spider, 7/2; a skirmish ability used only on purpose
					put("spider", "31_55");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void StingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Sting
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Artifact
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be Bilbo.
		 * 	Each time Bilbo wins a skirmish in which you played a [dwarven] event, you may discard a condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sting");

		assertEquals("Sting", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnlyOnBilbo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var worrywort = scn.GetFreepsCard("worrywort");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(worrywort);

		scn.StartGame();
		scn.MoveCardsToHand(sting);
		scn.FreepsRefreshDecision();
		assertInHand(sting);

		assertTrue(scn.FreepsPlayAvailable(sting));
		scn.FreepsPlayCard(sting);

		// Neither the Dwarf companion nor the Hobbit ally may bear it: Bilbo is the one bearer, and nothing is asked.
		assertAttachedTo(sting, bilbo);
		assertTrue(scn.GetAttachedCards(thorin).isEmpty());
		assertTrue(scn.GetAttachedCards(worrywort).isEmpty());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BilboWinningASkirmishInWhichADwarvenEventWasPlayedMayDiscardAConditionOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var noble = scn.GetFreepsCard("noble");
		var stairs = scn.GetFreepsCard("stairs");
		var emeralds = scn.GetFreepsCard("emeralds");
		var hatred = scn.GetShadowCard("hatred");
		var swarms = scn.GetShadowCard("swarms");
		var mace = scn.GetShadowCard("mace");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(bilbo, sting);
		scn.MoveCardsToSupportArea(stairs, emeralds, hatred, swarms);
		scn.MoveCardsToHand(noble);

		scn.StartGame();
		assertAttachedTo(sting, bilbo);
		assertInZone(Zone.SUPPORT, stairs, emeralds, hatred, swarms);
		assertInHand(noble);
		scn.MoveMinionsToTable(runner, runner2);
		scn.AttachCardsTo(runner2, mace);
		assertAttachedTo(mace, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		// The second Runner, bearing the Mace, fights nobody.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(bilbo);

		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(5, scn.GetStrength(bilbo));
		scn.FreepsPlayCard(noble);
		// Thorin exerts for it, and Bilbo is 3 + 2 for Sting + 3 = 8 against the Runner's 5.
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(8, scn.GetStrength(bilbo));
		scn.ShadowPass();
		scn.FreepsPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sting));
		scn.FreepsAcceptOptionalTrigger();

		// Conditions of either side and of three cultures; not the artifact, nor the possession a minion bears.
		assertTrue(scn.FreepsHasCardChoiceAvailable(stairs, hatred, swarms));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(emeralds, mace));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(hatred);

		assertInDiscard(hatred);
		assertInZone(Zone.SUPPORT, stairs, emeralds, swarms);
		assertAttachedTo(mace, runner2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningDiscardsNothingAndASecondSuchWinOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var noble = scn.GetFreepsCard("noble");
		var noble2 = scn.GetFreepsCard("noble2");
		var stairs = scn.GetFreepsCard("stairs");
		var hatred = scn.GetShadowCard("hatred");
		var runner = scn.GetShadowCard("runner");
		var spider = scn.GetShadowCard("spider");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(bilbo, sting);
		scn.MoveCardsToSupportArea(stairs, hatred);
		scn.MoveCardsToHand(noble, noble2);

		scn.StartGame();
		assertAttachedTo(sting, bilbo);
		assertInZone(Zone.SUPPORT, stairs, hatred);
		assertInHand(noble, noble2);
		scn.MoveMinionsToTable(runner, spider);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		// The fierce Spider is left out of the first round.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(bilbo);
		scn.FreepsPlayCard(noble);
		scn.ShadowPass();
		scn.FreepsPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sting));
		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, stairs, hatred);

		// The fierce round: Bilbo, 5 + 3 for the second Noble intentions, beats the Spider's 7 and is offered again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(bilbo, spider);
		scn.FreepsResolveSkirmish(bilbo);
		scn.FreepsPlayCard(noble2);
		assertEquals(8, scn.GetStrength(bilbo));
		scn.ShadowPass();
		scn.FreepsPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sting));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(stairs);

		assertInDiscard(stairs);
		assertInZone(Zone.SUPPORT, hatred);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BilboLosingASkirmishInWhichADwarvenEventWasPlayedOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var noble = scn.GetFreepsCard("noble");
		var hatred = scn.GetShadowCard("hatred");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(bilbo, sting);
		scn.MoveCardsToSupportArea(hatred);
		scn.MoveCardsToHand(noble);

		scn.StartGame();
		assertAttachedTo(sting, bilbo);
		assertInZone(Zone.SUPPORT, hatred);
		assertInHand(noble);
		scn.MoveMinionsToTable(runner);
		// Strength 9 by hand, so that the Runner beats Bilbo's 8 and Bilbo lives.
		scn.ApplyAdHocModifier(new StrengthModifier(null, runner, null, 4));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.FreepsResolveSkirmish(bilbo);
		scn.FreepsPlayCard(noble);
		assertEquals(8, scn.GetStrength(bilbo));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		scn.ShadowPass();
		scn.FreepsPass();

		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sting));
		assertInZone(Zone.SUPPORT, hatred);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADwarvenEventInAnotherCompanionsSkirmishDoesNotCountForBilbosWin() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var fury = scn.GetFreepsCard("fury");
		var hatred = scn.GetShadowCard("hatred");
		var runner = scn.GetShadowCard("runner");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(bilbo, sting);
		scn.MoveCardsToSupportArea(hatred);
		scn.MoveCardsToHand(fury);

		scn.StartGame();
		assertAttachedTo(sting, bilbo);
		assertInZone(Zone.SUPPORT, hatred);
		assertInHand(fury);
		scn.MoveMinionsToTable(runner, bats);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thorin, runner},
				new PhysicalCardImpl[]{bilbo, bats});

		// Thorin's skirmish first, with a [dwarven] event played in it: Thorin, who does not bear Sting, wins.
		scn.FreepsResolveSkirmish(thorin);
		scn.FreepsPlayCard(fury);
		assertEquals(10, scn.GetStrength(thorin));
		scn.ShadowPass();
		scn.FreepsPass();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sting));

		// Then Bilbo's, with no event played in it: 5 against the Bats' 4, a win, and nothing offered.
		scn.FreepsResolveSkirmish(bilbo);
		assertEquals(0, scn.GetWoundsOn(bats));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(bats));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sting));
		assertInZone(Zone.SUPPORT, hatred);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AGandalfEventAndADwarvenCompanionPlayedInBilbosSkirmishDoNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var bilbo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var fili = scn.GetFreepsCard("fili");
		var takeup = scn.GetFreepsCard("takeup");
		var hatred = scn.GetShadowCard("hatred");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(bilbo, sting);
		scn.MoveCardsToSupportArea(hatred);
		scn.MoveCardsToHand(takeup);

		scn.StartGame();
		assertAttachedTo(sting, bilbo);
		assertInZone(Zone.SUPPORT, hatred);
		assertInHand(takeup);
		assertInZone(Zone.DECK, fili);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.FreepsResolveSkirmish(bilbo);

		// Gandalf's ability plays a [gandalf] skirmish event (the Runner -3) and then a [dwarven] card that is no
		// event, Fili, from the draw deck.
		assertEquals(0, scn.GetBurdens());
		scn.FreepsUseCardAction(gandalf, "Play a [GANDALF] skirmish event");
		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetStrength(runner));
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(fili);
		assertInPlay(fili);
		scn.ShadowPass();
		scn.FreepsPass();

		// Bilbo, 5, beats the Runner, 2: no [dwarven] event was played, and nothing is offered.
		assertInDiscard(runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sting));
		assertInZone(Zone.SUPPORT, hatred);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoConditionInPlaySuchAWinOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var noble = scn.GetFreepsCard("noble");
		var emeralds = scn.GetFreepsCard("emeralds");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(bilbo, sting);
		scn.MoveCardsToSupportArea(emeralds);
		scn.MoveCardsToHand(noble);

		scn.StartGame();
		assertAttachedTo(sting, bilbo);
		assertInZone(Zone.SUPPORT, emeralds);
		assertInHand(noble);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.FreepsResolveSkirmish(bilbo);
		scn.FreepsPlayCard(noble);
		scn.ShadowPass();
		scn.FreepsPass();

		// Bilbo wins, 8 against 5, after a [dwarven] event, but the offer would do nothing: withheld.
		assertInDiscard(runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sting));
		assertInZone(Zone.SUPPORT, emeralds);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
