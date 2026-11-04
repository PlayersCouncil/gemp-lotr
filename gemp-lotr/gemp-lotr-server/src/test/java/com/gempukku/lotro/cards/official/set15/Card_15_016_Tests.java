package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gift", "15_16");

					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, strength 6, no archer; stronger only against a Nazgul
					put("guardian", "17_7");    // Elven Guardian: another Elf companion, hunter 1; a maneuver ability never used here
					put("legolas", "15_18");    // Legolas, Fleet-footed Hunter: Elf companion, strength 6, hunter 1; a regroup ability never used here
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: Elf ally whose home is 6F, strength 6, no archer; a fellowship ability never used here
					put("swanship", "1_65");    // Swan-ship of the Galadhrim: exert an Elf ally whose home is site 6: +3 and he skirmishes until regroup
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the [gondor] Aragorn; a maneuver ability never used here
					put("rohanaragorn", "17_93"); // Aragorn, Defender of Rohan: the [rohan] Aragorn; hunter for [rohan] companions only while the Ring-bearer is assigned
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man who is no Aragorn; a skirmish ability never used here
					put("future", "11_26");     // Uncertain Future: another [elven] support-area card that holds [elven] tokens; a maneuver ability never used here

					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					put("uruk", "15_168");      // Searching Uruk: fierce, hunter 1, strength 5, vitality 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GiftoftheEvenstarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Gift of the Evenstar, Blessed Light
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Support area
		 * Game Text: Each time an Elf wins a skirmish, you may exert that Elf or Aragorn to place an [elven] token here.<br>At the start of each maneuver phase, you may remove an [elven] token here to draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gift");

		assertEquals("Gift of the Evenstar", card.getBlueprint().getTitle());
		assertEquals("Blessed Light", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachTimeAnElfWinsHisOwnerMayExertThatElfOrAragornToPlaceATokenHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var arwen = scn.GetFreepsCard("arwen");
		var guardian = scn.GetFreepsCard("guardian");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		// Another Elf who did not win, and a [gondor] Man who is no Aragorn, each able to exert.
		scn.MoveCompanionsToTable(arwen, guardian, aragorn, boromir);
		scn.MoveCardsToSupportArea(gift);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, gift);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, runner);
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();

		// Arwen won, 6 against 5.
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetCultureTokensOn(gift));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gift));
		scn.FreepsAcceptOptionalTrigger();

		// Both can pay, so he is asked which (Q-0308).
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guardian, boromir, frodo));
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(guardian));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetCultureTokensOn(gift));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithAragornExhaustedAnElfAllyWhoWinsExertsHimself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var celeborn = scn.GetFreepsCard("celeborn");
		var swanship = scn.GetFreepsCard("swanship");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(gift);
		scn.MoveCardsToHand(swanship);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(aragorn, 3);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, gift);
		assertInZone(Zone.HAND, swanship);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// The ally is made to skirmish by a printed card: exerted once for it.
		scn.FreepsPlayCard(swanship);
		assertEquals(1, scn.GetWoundsOn(celeborn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(celeborn, runner);
		scn.FreepsResolveSkirmish(celeborn);
		scn.PassSkirmishActions();

		// Celeborn won, 6 + 3 against 5.
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gift));
		scn.FreepsAcceptOptionalTrigger();

		// Aragorn cannot exert, so the Elf does, and nobody is asked.
		assertEquals(2, scn.GetWoundsOn(celeborn));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetCultureTokensOn(gift));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTheWinningElfExhaustedAragornOfRohanExertsInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("rohanaragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveCardsToSupportArea(gift);
		// Vitality 3 with 2 wounds: exhausted, and she still wins.
		scn.AddWoundsToChar(arwen, 2);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertInZone(Zone.SUPPORT, gift);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, runner);
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gift));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetCultureTokensOn(gift));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTheWinningElfAndAragornBothExhaustedNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var runner = scn.GetShadowCard("runner");
		// Boromir could exert, but is neither the Elf nor Aragorn.
		scn.MoveCompanionsToTable(arwen, aragorn, boromir);
		scn.MoveCardsToSupportArea(gift);
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(aragorn, 3);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, gift);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, runner);
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gift));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetCultureTokensOn(gift));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenAnElfLosesASkirmishOrAManWinsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var legolas = scn.GetFreepsCard("legolas");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(legolas, boromir, aragorn);
		scn.MoveCardsToSupportArea(gift);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, gift);
		scn.MoveMinionsToTable(runner, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, runner}, new PhysicalCardImpl[]{legolas, troop});

		// Boromir, a Man, wins: 7 against 5.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gift));

		// Legolas, an Elf, loses and lives: 6 + 1 for a hunter against a minion that is none, against 13.
		scn.FreepsResolveSkirmish(legolas);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gift));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetCultureTokensOn(gift));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheOfferExertsNobodyAndPlacesNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveCardsToSupportArea(gift);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, gift);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, runner);
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gift));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetCultureTokensOn(gift));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheSameElfWinningAgainInTheFierceRoundIsOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var uruk = scn.GetShadowCard("uruk");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveCardsToSupportArea(gift);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, gift);
		scn.MoveMinionsToTable(uruk);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(legolas, uruk);
		scn.FreepsResolveSkirmish(legolas);
		scn.PassSkirmishActions();

		// Hunter against hunter, neither has a bonus: 6 against 5.
		assertEquals(1, scn.GetWoundsOn(uruk));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gift));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(legolas);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetCultureTokensOn(gift));

		// The Uruk is fierce and alive: Legolas wins a second skirmish and is offered again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(legolas, uruk);
		scn.FreepsResolveSkirmish(legolas);
		scn.PassFierceSkirmishActions();

		assertInZone(Zone.DISCARD, uruk);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gift));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, aragorn));
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetCultureTokensOn(gift));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtTheStartOfEachManeuverPhaseATokenHereMayBeRemovedToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(gift);

		scn.StartGame();
		scn.AddTokensToCard(gift, 2);
		assertEquals(2, scn.GetCultureTokensOn(gift));
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gift));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetCultureTokensOn(gift));
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// A second move, and the next maneuver phase of the same turn offers it again.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(runner2);
		scn.ShadowRefreshDecision();
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gift));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(0, scn.GetCultureTokensOn(gift));
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DecliningKeepsTheTokenAndTheStartOfTheArcheryPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(gift);

		scn.StartGame();
		scn.AddTokensToCard(gift, 1);
		assertEquals(1, scn.GetCultureTokensOn(gift));
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gift));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetCultureTokensOn(gift));
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The phase next door: at the start of the archery phase nothing is offered.
		scn.PassCurrentPhaseActions();

		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gift));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(1, scn.GetCultureTokensOn(gift));
		assertEquals(0, scn.GetFreepsHandCount());
	}

	@Test
	public void WithNoTokenHereNothingIsOfferedThoughAnotherElvenCardHoldsTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var future = scn.GetFreepsCard("future");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(gift, future);

		scn.StartGame();
		// Uncertain Future gets its [elven] tokens as it is played: cheated in, they are put on by hand.
		scn.AddTokensToCard(future, 3);
		assertEquals(3, scn.GetCultureTokensOn(future));
		assertEquals(0, scn.GetCultureTokensOn(gift));
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gift));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(3, scn.GetCultureTokensOn(future));
		assertEquals(0, scn.GetFreepsHandCount());
	}
}
