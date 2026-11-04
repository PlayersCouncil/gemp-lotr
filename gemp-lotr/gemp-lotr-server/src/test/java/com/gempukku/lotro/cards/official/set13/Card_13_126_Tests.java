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

public class Card_13_126_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("firefoot", "13_126");
					// Éomer, Forthwith Banished: [rohan] Man companion, strength 7, vitality 3; valiant, damage +1 and a
					// cost reduction are his whole text
					put("eomer", "0_66");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, vitality 3; Valiant is its whole text
					// Guma, Plains Farmer: [rohan] Man ally, home 4T; his fellowship ability is used only on purpose
					put("guma", "4_277");
					// Merry, Master Holbytla: a [rohan] Hobbit companion; a Man only while 3 [rohan] companions (or
					// Éowyn) can be spotted, which is never so where he is laid; his texts are used only on purpose
					put("merry", "103_85");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion; his skirmish ability is not used
					// Doom Drove Them: [rohan] support-area condition that adds [rohan] tokens to itself by its
					// fellowship ability; its skirmish ability is used only on purpose
					put("doom1", "8_86");
					put("doom2", "8_86");
					// Garrison of Osgiliath: [gondor] support-area condition that takes [gondor] tokens as it is played
					put("garrison", "6_52");
					// Sword of Rohan: [rohan] hand weapon on a [rohan] Man; damage +1 only against an Uruk-hai
					put("sword", "4_291");

					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1; its only text is on its play
					put("runner2", "1_178");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FirefootStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Firefoot, Mearas of The Mark
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: +1
		 * Game Text: Bearer must be a [rohan] Man.<br>When you play Firefoot, you may reinforce a [rohan] token.<br>At the start of the maneuver phase, if bearer is Éomer, you may remove 2 [rohan] tokens to make him <b>defender +1</b> and strength +1 until the regroup phase.
		*/

		var scn = GetScenario();

		var firefoot = scn.GetFreepsCard("firefoot");

		assertEquals("Firefoot", firefoot.getBlueprint().getTitle());
		assertEquals("Mearas of The Mark", firefoot.getBlueprint().getSubtitle());
		assertTrue(firefoot.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, firefoot.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, firefoot.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, firefoot.getBlueprint().getCardType());
		assertTrue(firefoot.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, firefoot.getBlueprint().getTwilightCost());
		assertEquals(1, firefoot.getBlueprint().getStrength());
	}

	@Test
	public void PlayedOnARohanManItMayReinforceARohanTokenOnACardThatHasOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firefoot = scn.GetFreepsCard("firefoot");
		var eomer = scn.GetFreepsCard("eomer");
		var guma = scn.GetFreepsCard("guma");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var doom1 = scn.GetFreepsCard("doom1");
		var doom2 = scn.GetFreepsCard("doom2");
		var garrison = scn.GetFreepsCard("garrison");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(eomer, merry, boromir);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToSupportArea(doom1, doom2, garrison);
		scn.AddTokensToCard(doom1, 2);
		scn.AddTokensToCard(doom2, 1);
		scn.AddTokensToCard(garrison, 2);
		scn.MoveCardsToHand(firefoot);

		scn.StartGame();

		assertInHand(firefoot);
		assertEquals(2, scn.GetCultureTokensOn(doom1));
		assertEquals(1, scn.GetCultureTokensOn(doom2));
		assertEquals(2, scn.GetCultureTokensOn(garrison));

		scn.FreepsPlayCard(firefoot);
		// [rohan] Men: a companion and an ally.  Not Merry (a [rohan] Hobbit, no Man beside two [rohan] companions),
		// Boromir (a [gondor] Man) or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(eomer, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, boromir, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guma);
		assertAttachedTo(firefoot, guma);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(firefoot));
		scn.FreepsAcceptOptionalTrigger();
		// Either [rohan] card with a [rohan] token; not Guma (a [rohan] card with none), nor the [gondor] tokens.
		assertTrue(scn.FreepsHasCardChoiceAvailable(doom1, doom2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guma, garrison));
		scn.FreepsChooseCard(doom2);

		assertEquals(2, scn.GetCultureTokensOn(doom1));
		assertEquals(2, scn.GetCultureTokensOn(doom2));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheReinforcementMayBeDeclinedAndAnotherRohanPossessionPlayedAfterIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firefoot = scn.GetFreepsCard("firefoot");
		var eomer = scn.GetFreepsCard("eomer");
		var sword = scn.GetFreepsCard("sword");
		var doom1 = scn.GetFreepsCard("doom1");
		scn.MoveCompanionsToTable(eomer);
		scn.MoveCardsToSupportArea(doom1);
		scn.AddTokensToCard(doom1, 1);
		scn.MoveCardsToHand(firefoot, sword);

		scn.StartGame();

		assertEquals(1, scn.GetCultureTokensOn(doom1));
		// Éomer is the only [rohan] Man: Firefoot goes on him unasked.
		scn.FreepsPlayCard(firefoot);
		assertAttachedTo(firefoot, eomer);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(firefoot));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another [rohan] possession played after Firefoot, with the token still there to reinforce.
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, eomer);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(firefoot));
		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoRohanTokenToReinforceTheReinforcementIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firefoot = scn.GetFreepsCard("firefoot");
		var eomer = scn.GetFreepsCard("eomer");
		var doom1 = scn.GetFreepsCard("doom1");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(eomer);
		// A [rohan] card with no token, and [gondor] tokens on a [gondor] card.
		scn.MoveCardsToSupportArea(doom1, garrison);
		scn.AddTokensToCard(garrison, 2);
		scn.MoveCardsToHand(firefoot);

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(doom1));
		assertEquals(2, scn.GetCultureTokensOn(garrison));

		scn.FreepsPlayCard(firefoot);
		assertAttachedTo(firefoot, eomer);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(firefoot));
		assertEquals(0, scn.GetCultureTokensOn(doom1));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfTheManeuverPhaseTwoRohanTokensMakeEomerDefenderPlus1AndStrengthPlus1UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firefoot = scn.GetFreepsCard("firefoot");
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var doom1 = scn.GetFreepsCard("doom1");
		var doom2 = scn.GetFreepsCard("doom2");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(eomer, guard);
		scn.AttachCardsTo(eomer, firefoot);
		scn.MoveCardsToSupportArea(doom1, doom2, garrison);
		scn.AddTokensToCard(doom1, 1);
		scn.AddTokensToCard(doom2, 1);
		scn.AddTokensToCard(garrison, 2);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertAttachedTo(firefoot, eomer);
		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertEquals(1, scn.GetCultureTokensOn(doom2));
		assertEquals(2, scn.GetCultureTokensOn(garrison));

		scn.MoveMinionsToTable(runner1, runner2, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// 7 + 1 for Firefoot.
		assertEquals(8, scn.GetStrength(eomer));
		assertEquals(0, scn.GetKeywordCount(eomer, Keyword.DEFENDER));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(firefoot));
		scn.FreepsAcceptOptionalTrigger();

		// One token from each card: asked a card at a time, the second going unasked.  The [gondor] tokens are not
		// offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(doom1, doom2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(garrison));
		scn.FreepsChooseCard(doom1);

		assertEquals(0, scn.GetCultureTokensOn(doom1));
		assertEquals(0, scn.GetCultureTokensOn(doom2));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(9, scn.GetStrength(eomer));
		assertEquals(1, scn.GetKeywordCount(eomer, Keyword.DEFENDER));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Defender +1: Éomer may be assigned to two minions.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{eomer, runner1, runner2},
				new PhysicalCardImpl[]{guard, savage});
		assertTrue(scn.IsCharAssignedAgainst(eomer, runner1));
		assertTrue(scn.IsCharAssignedAgainst(eomer, runner2));

		// The Guard (6) beats the Savage (5) first.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(eomer);

		// Still in force in the skirmish after one already resolved.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(eomer));
		assertEquals(1, scn.GetKeywordCount(eomer, Keyword.DEFENDER));

		// 9 against 5 + 5: Éomer loses and takes a wound.  At the regroup phase both are gone.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertEquals(8, scn.GetStrength(eomer));
		assertEquals(0, scn.GetKeywordCount(eomer, Keyword.DEFENDER));
	}

	@Test
	public void DeclinedNothingChangesAndTheStartOfTheArcheryPhaseIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firefoot = scn.GetFreepsCard("firefoot");
		var eomer = scn.GetFreepsCard("eomer");
		var doom1 = scn.GetFreepsCard("doom1");
		scn.MoveCompanionsToTable(eomer);
		scn.AttachCardsTo(eomer, firefoot);
		scn.MoveCardsToSupportArea(doom1);
		scn.AddTokensToCard(doom1, 2);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertAttachedTo(firefoot, eomer);
		assertEquals(2, scn.GetCultureTokensOn(doom1));

		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(firefoot));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(doom1));
		assertEquals(8, scn.GetStrength(eomer));
		assertEquals(0, scn.GetKeywordCount(eomer, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The start of the next phase is no start of the maneuver phase.
		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(firefoot));
		assertEquals(2, scn.GetCultureTokensOn(doom1));
		assertEquals(8, scn.GetStrength(eomer));
	}

	@Test
	public void NotOfferedWhileAnotherRohanManBearsItThoughEomerIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firefoot = scn.GetFreepsCard("firefoot");
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var doom1 = scn.GetFreepsCard("doom1");
		scn.MoveCompanionsToTable(eomer, guard);
		scn.AttachCardsTo(guard, firefoot);
		scn.MoveCardsToSupportArea(doom1);
		scn.AddTokensToCard(doom1, 2);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertAttachedTo(firefoot, guard);
		assertEquals(2, scn.GetCultureTokensOn(doom1));

		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(firefoot));
		assertEquals(2, scn.GetCultureTokensOn(doom1));
		assertEquals(7, scn.GetStrength(eomer));
		assertEquals(0, scn.GetKeywordCount(eomer, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DEFENDER));
	}

	@Test
	public void NotOfferedWithOnlyOneRohanToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firefoot = scn.GetFreepsCard("firefoot");
		var eomer = scn.GetFreepsCard("eomer");
		var doom1 = scn.GetFreepsCard("doom1");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(eomer);
		scn.AttachCardsTo(eomer, firefoot);
		scn.MoveCardsToSupportArea(doom1, garrison);
		scn.AddTokensToCard(doom1, 1);
		scn.AddTokensToCard(garrison, 2);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertAttachedTo(firefoot, eomer);
		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertEquals(2, scn.GetCultureTokensOn(garrison));

		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// One [rohan] token; the [gondor] tokens do not make up the second.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(firefoot));
		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(8, scn.GetStrength(eomer));
	}

	@Test
	public void ASecondManeuverPhaseInTheSameTurnOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firefoot = scn.GetFreepsCard("firefoot");
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var doom1 = scn.GetFreepsCard("doom1");
		var doom2 = scn.GetFreepsCard("doom2");
		scn.MoveCompanionsToTable(eomer, guard);
		scn.AttachCardsTo(eomer, firefoot);
		scn.MoveCardsToSupportArea(doom1, doom2);
		scn.AddTokensToCard(doom1, 2);
		scn.AddTokensToCard(doom2, 2);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();

		assertAttachedTo(firefoot, eomer);
		assertEquals(2, scn.GetCultureTokensOn(doom1));
		assertEquals(2, scn.GetCultureTokensOn(doom2));

		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(firefoot));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(doom1);
		scn.FreepsChooseCard(doom1);
		assertEquals(0, scn.GetCultureTokensOn(doom1));
		assertEquals(2, scn.GetCultureTokensOn(doom2));
		assertEquals(9, scn.GetStrength(eomer));

		// The Guard (6) kills the Runner (5, vitality 1); in the regroup phase the bonus is gone.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, runner1);
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(8, scn.GetStrength(eomer));

		// A second move, and a minion for a second maneuver phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(runner2);
		scn.ShadowRefreshDecision();
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(firefoot));
		scn.FreepsAcceptOptionalTrigger();
		// Only the second Doom Drove Them has tokens left: both come from it unasked.
		assertEquals(0, scn.GetCultureTokensOn(doom1));
		assertEquals(0, scn.GetCultureTokensOn(doom2));
		assertEquals(9, scn.GetStrength(eomer));
		assertEquals(1, scn.GetKeywordCount(eomer, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
