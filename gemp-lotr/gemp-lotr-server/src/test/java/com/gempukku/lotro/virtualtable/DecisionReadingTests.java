package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Tests of the rig itself: the checks that read what a decision is offering by the card it concerns (an optional
 * trigger, an assignment, one of several actions of a card, one of two optional actions that a card offers at the
 * same moment), the count of how often a deck has been shuffled, and
 * the answers that are refused when what they answer is not what is being asked (the order of required responses,
 * a showing of cards, an optional trigger of a card that is not offering one), and the number a question for a
 * number comes filled in with.
 */
public class DecisionReadingTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Three for the Elven-Kings: "Each time the fellowship moves, you may spot an [elven] or [gandalf]
					// ring to place a card from hand beneath your draw deck and draw a card..."  Its definition words
					// the prompt itself, so the game does not offer it under the card's name.
					put("three", "101_12");
					put("elrond", "1_40");          // Elrond, Lord of Rivendell: ally, home site 3. Fellowship: Exert Elrond to draw a card.
					put("vilya", "3_27");           // Vilya: an [elven] ring, borne by Elrond
					put("aragorn", "1_89");         // Aragorn, Ranger of the North

					// Library of Rivendell.  "Fellowship: Stack your tale or artifact from play or from hand here to
					// draw a card."  The game offers stacking from play and stacking from hand as two actions.
					put("library", "101_11");
					put("redbook", "2_113");        // Red Book of Westmarch: a tale, in the support area
					put("staff", "2_22");           // Gandalf's Staff: an artifact

					put("gaffer", "1_291");         // The Gaffer: the ally Bag End plays from the draw deck
					put("deck1", "1_3");            // an event, neither a tale nor an artifact
					put("deck2", "1_3");
					put("deck3", "1_3");

					put("runner", "1_178");         // Goblin Runner
					// Thrashing Tentacle: fierce, vitality 2.  "Each time this minion loses a skirmish, add a burden",
					// a required trigger that falls at the same moment as the wound for losing.  It is discarded when
					// not at a marsh, so the test that uses it makes every site one.
					put("thrashing", "101_33");
				}},
				// Site 1 is Bag End: "Fellowship: Exert a Hobbit to play The Gaffer from your draw deck."
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OptionalTriggerCheckGoesByTheCardWhateverThePromptSays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var three = scn.GetFreepsCard("three");
		var elrond = scn.GetFreepsCard("elrond");
		var vilya = scn.GetFreepsCard("vilya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(three);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, vilya);
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();

		// Nothing optional is being asked in the fellowship phase.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(three));

		scn.FreepsPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		// The prompt does not name the card.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable("Three for the Elven-Kings"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(three));
		// The trigger is the condition's, not the ring's or its bearer's, and not the Shadow player's to answer.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(vilya));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(elrond));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(three));
	}

	@Test
	public void AssignmentTargetCheckReadsTheDecisionEachPlayerIsFacing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var elrond = scn.GetFreepsCard("elrond");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		// Not an assignment decision.
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(aragorn));

		scn.SkipToAssignments();

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(runner));
		// An ally away from home (Elrond's is site 3, and this is site 2) cannot be assigned.
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(elrond));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(aragorn));

		// The Free Peoples player assigns nobody, and the Shadow player is asked.
		scn.FreepsDeclineAssignments();
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(runner));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(elrond));
	}

	@Test
	public void OneOfSeveralActionsOfACardIsFoundAndTakenByItsWording() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var library = scn.GetFreepsCard("library");
		var redbook = scn.GetFreepsCard("redbook");
		var staff = scn.GetFreepsCard("staff");
		scn.MoveCardsToSupportArea(library, redbook);
		scn.MoveCardsToHand(staff);

		scn.StartGame();
		// What stacking draws is a card that cannot itself be stacked.
		scn.MoveCardsToTopOfDeck(scn.GetFreepsCard("deck1"), scn.GetFreepsCard("deck2"));
		scn.FreepsRefreshDecision();

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsActionAvailable(library));
		assertTrue(scn.FreepsActionAvailable(library, "from play"));
		assertTrue(scn.FreepsActionAvailable(library, "FROM HAND"));
		assertFalse(scn.FreepsActionAvailable(library, "from the discard pile"));
		// The wording alone is not enough: the action has to be that card's.
		assertFalse(scn.FreepsActionAvailable(staff, "from hand"));
		assertFalse(scn.ShadowActionAvailable(library, "from hand"));

		assertInZone(Zone.HAND, staff);
		assertInZone(Zone.SUPPORT, redbook);

		// The staff is the only tale or artifact in hand, so it is stacked without a choice.
		scn.FreepsUseCardAction(library, "from hand");
		assertInZone(Zone.STACKED, staff);
		assertInZone(Zone.SUPPORT, redbook);

		// With the hand emptied of artifacts only the other action is left.
		assertTrue(scn.FreepsActionAvailable(library, "from play"));
		assertFalse(scn.FreepsActionAvailable(library, "from hand"));

		scn.FreepsUseCardAction(library, "from play");
		assertInZone(Zone.STACKED, redbook);
		assertFalse(scn.FreepsActionAvailable(library));
	}

	@Test
	public void ShuffleCountRisesWhenADeckIsShuffledAndNotWhenACardIsDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var elrond = scn.GetFreepsCard("elrond");
		var gaffer = scn.GetFreepsCard("gaffer");
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		// Elrond draws the top card; The Gaffer must not be it (the deck's order is random, and he was 1 time in 12).
		// He is put at the bottom once the game has started: the decks are shuffled as it starts.
		scn.MoveCardsToBottomOfDeck(gaffer);
		scn.FreepsRefreshDecision();

		int freeps = scn.GetFreepsDeckShuffleCount();
		int shadow = scn.GetShadowDeckShuffleCount();
		assertInZone(Zone.DECK, gaffer);

		// Drawing a card leaves the deck in its order.
		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(freeps, scn.GetFreepsDeckShuffleCount());

		// Bag End plays The Gaffer from the draw deck, which is then reshuffled (CR 4.2 "draw deck").
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(gaffer);
		assertInZone(Zone.SUPPORT, gaffer);
		assertEquals(freeps + 2, scn.GetFreepsDeckShuffleCount()); // as the card left the deck, and the definition's own shuffle after the play (F-0313)
		// The other player's deck is untouched.
		assertEquals(shadow, scn.GetShadowDeckShuffleCount());
	}

	// Runs the call and says whether the rig refused it.
	private static boolean Refused(Runnable call) {
		try {
			call.run();
		}
		catch (AssertionError refusal) {
			return true;
		}
		return false;
	}

	@Test
	public void OrderingRequiredResponsesIsRefusedWhenAnOptionalTriggerIsWhatIsOnOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var three = scn.GetFreepsCard("three");
		var elrond = scn.GetFreepsCard("elrond");
		var vilya = scn.GetFreepsCard("vilya");
		scn.MoveCardsToSupportArea(three);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, vilya);
		scn.MoveCardsToHand(scn.GetFreepsCard("aragorn"));

		scn.StartGame();
		// Nothing of the kind is being asked in the fellowship phase.
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertTrue(Refused(scn::FreepsResolveRuleFirst));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();

		// An optional trigger is on offer.  It is not an ordering of required responses, and is not taken as one.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(three));
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertTrue(Refused(scn::FreepsResolveRuleFirst));
		assertTrue(Refused(() -> scn.FreepsResolveActionOrder("Three")));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(three));

		// Nor is the trigger taken by naming a card that is not offering one.
		assertTrue(Refused(() -> scn.FreepsChooseOptionalTrigger(vilya)));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(three));

		scn.FreepsChooseOptionalTrigger(three);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(three));
	}

	@Test
	public void RequiredResponsesAtTheSameMomentAreOrderedByTheFreePeoplesPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var thrashing = scn.GetShadowCard("thrashing");
		scn.MoveCompanionsToTable(aragorn);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.SITE, null, Keyword.MARSH));

		scn.StartGame();
		// With a burden to spot, the tentacle adds one and not two.
		scn.AddBurdens(1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(thrashing);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, thrashing);
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		scn.PassSkirmishActions();

		// The wound for losing and the tentacle's burden fall together, and the Free Peoples player says which is first.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(0, scn.GetWoundsOn(thrashing));
		assertEquals(1, scn.GetBurdens());

		scn.FreepsResolveRuleFirst();

		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(1, scn.GetWoundsOn(thrashing));
		assertEquals(2, scn.GetBurdens());
	}

	protected VirtualTableScenario GetIsAboutToScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Uruk Shield Wall: "Each time an Uruk-hai costing X is about to take a wound (except during a
					// skirmish), prevent that and exert it.  X is the number of [isengard] tokens you can spot here."
					// Two copies with the same tokens each have a required trigger on the one wound.
					put("wall1", "102_35");
					put("wall2", "102_35");
					put("savage", "1_151");         // Uruk Savage: an Uruk-hai costing 2, vitality 3
					put("legolas", "1_50");         // Legolas, Greenleaf: "Archery: Exert Legolas to wound a minion..."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RequiredIsAboutToResponsesAtTheSameMomentAreOrderedByTheFreePeoplesPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetIsAboutToScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var wall1 = scn.GetShadowCard("wall1");
		var wall2 = scn.GetShadowCard("wall2");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToSupportArea(wall1, wall2);
		scn.AddTokensToCard(wall1, 2);
		scn.AddTokensToCard(wall2, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ARCHERY);
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());

		scn.FreepsUseCardAction(legolas);

		// Both copies answer the wound that is about to fall.  The game words this ordering differently from the
		// one for things that have happened ('... - Required "is about to" responses'), and it is the same question.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertTrue(scn.FreepsDecisionAvailable("is about to"));
		assertEquals(0, scn.GetWoundsOn(savage));

		// Neither is the game's own rule, so there is no rule to resolve first: one of the two is named.
		assertTrue(Refused(scn::FreepsResolveRuleFirst));
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.FreepsResolveActionOrder("Uruk Shield Wall");

		// The first copy prevented the wound and exerted him; nothing is left for the second to answer.
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	protected VirtualTableScenario GetDeathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// King of the Dead, Dead Keeper: "Each time King of the Dead is killed, heal every Wraith."  A
					// required trigger of the Free Peoples player's own, which the game lists before the rule.
					put("king", "103_27");
					put("deadman", "10_27");        // Dead Man of Dunharrow: a Wraith, vitality 3
					// Ferocious Haradrim: strength 12.  "Each time a Free Peoples character is killed, each of your
					// [men] minions is strength +3 until the regroup phase."  A required trigger of the Shadow
					// player's, which the game lists after the rule.  An archer, so he is put down in the regroup phase.
					put("haradrim", "11_80");
					put("guard", "1_7");            // Dwarf Guard: vitality 2
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: vitality 4
					// Morgul Slayer: "Regroup: Exert this minion to wound a companion (except the Ring-bearer)."
					put("slayer", "3_93");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheRuleIsResolvedFirstThoughACardsTriggerIsListedBeforeIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDeathScenario();

		var king = scn.GetFreepsCard("king");
		var deadman = scn.GetFreepsCard("deadman");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCompanionsToTable(king, deadman);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer);
		// Vitality 4 with 3 wounds
		scn.AddWoundsToChar(king, 3);
		scn.AddWoundsToChar(deadman, 1);
		scn.AddThreats(1);
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(king);

		// A companion's death turns the threat into a wound, by rule, and sets off the King's own text.  The
		// King's trigger is listed first; the rule is second, and the game gives it no wording.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(Arrays.asList("103_27", "rules"), scn.FreepsGetADParamAsList("blueprintId"));
		assertNull(scn.FreepsGetADParamAsList("actionText").get(1));
		assertEquals(1, scn.GetWoundsOn(deadman));

		scn.FreepsResolveRuleFirst();

		// The rule and not the first thing listed: the threat's wound is being placed, and nobody is healed yet.
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertInZone(Zone.DEAD, king);
		assertTrue(scn.FreepsHasCardChoiceAvailable(deadman));
		assertEquals(1, scn.GetWoundsOn(deadman));
		scn.FreepsChooseCard(deadman);

		// 1 + 1 for the threat, and then the King's healing: 1.
		assertEquals(0, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(deadman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACardsTriggerIsNamedPastARuleThatHasNoWording() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDeathScenario();

		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var haradrim = scn.GetShadowCard("haradrim");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCompanionsToTable(guard, aragorn);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(haradrim, slayer);
		// Vitality 2 with 1 wound
		scn.AddWoundsToChar(guard, 1);
		scn.AddThreats(1);
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(guard);

		// Here the rule is listed first, with no wording, and the card's trigger second.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(Arrays.asList("rules", "11_80"), scn.FreepsGetADParamAsList("blueprintId"));
		assertNull(scn.FreepsGetADParamAsList("actionText").get(0));
		assertEquals(12, scn.GetStrength(haradrim));

		scn.FreepsResolveActionOrder("Ferocious Haradrim");

		// The card's trigger went first: the threat's wound is still to be placed and the Haradrim is already +3.
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(15, scn.GetStrength(haradrim));
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheRuleWithNoWordingIsResolvedFirstWhereItIsListedFirst() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDeathScenario();

		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var haradrim = scn.GetShadowCard("haradrim");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCompanionsToTable(guard, aragorn);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(haradrim, slayer);
		// Vitality 2 with 1 wound
		scn.AddWoundsToChar(guard, 1);
		scn.AddThreats(1);
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(guard);

		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(Arrays.asList("rules", "11_80"), scn.FreepsGetADParamAsList("blueprintId"));
		assertEquals(12, scn.GetStrength(haradrim));

		scn.FreepsResolveRuleFirst();

		// The rule went first: the threat's wound is being placed and the Haradrim is not yet stronger.
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		assertEquals(12, scn.GetStrength(haradrim));
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(15, scn.GetStrength(haradrim));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	protected VirtualTableScenario GetTwoOffersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Ethereal Banner: "Each time bearer wins a skirmish, you may exert a Wraith and a minion it is
					// assigned to skirmish.  Response: If bearer overwhelms a minion, discard this possession and
					// remove 2 threats to wound 2 minions."  A win that is an overwhelm sets off both, and both are
					// this one card's: the game words them "Exert a Wraith and a minion it is assigned to" and
					// "Discard this and remove 2 threats to wound 2 minions".
					put("banner", "103_21");
					// King of the Dead, Oathbreaker: a [gondor] Wraith, strength 7, vitality 4, enduring.  With two
					// wounds he is 11, more than double a Goblin Runner's 5.
					put("king", "8_38");
					put("runner", "1_178");         // Goblin Runner: strength 5, vitality 1
					put("savage1", "1_151");        // Uruk Savage: vitality 3, and nothing to say about any of this
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OneOfTwoOptionalActionsOfACardIsFoundAndTakenByItsWording() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoOffersScenario();

		var banner = scn.GetFreepsCard("banner");
		var king = scn.GetFreepsCard("king");
		var runner = scn.GetShadowCard("runner");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(king);
		scn.AttachCardsTo(king, banner);

		scn.StartGame();
		scn.AddWoundsToChar(king, 2);
		scn.AddThreats(3);
		scn.MoveMinionsToTable(runner, savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(king, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(king);
		scn.PassCurrentPhaseActions();

		// The King has overwhelmed the Runner.  The Banner offers its trigger and its response in one decision,
		// and the card alone does not say which is meant.
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "Exert a Wraith"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "wound 2 minions"));
		// Not words that neither has, not another card with the right words, and not the player who is not asked.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(banner, "heal"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(king, "wound 2 minions"));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(banner, "wound 2 minions"));
		assertTrue(Refused(() -> scn.FreepsChooseOptionalTrigger(banner, "heal")));
		assertTrue(Refused(() -> scn.FreepsChooseOptionalTrigger(king, "wound 2 minions")));
		assertTrue(Refused(() -> scn.ShadowChooseOptionalTrigger(banner, "wound 2 minions")));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "wound 2 minions"));
		assertEquals(3, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(king));

		scn.FreepsChooseOptionalTrigger(banner, "wound 2 minions");

		// The response, and not the trigger: the Banner and two threats are spent, the two minions left are
		// wounded, and the King has not exerted.
		assertInZone(Zone.DISCARD, banner);
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertEquals(2, scn.GetWoundsOn(king));
	}

	@Test
	public void OnTheSecondPlayersTurnTheShadowMethodsFindAndTakeOneOfTwoOptionalActionsOfACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoOffersScenario();

		// Player 2's own Banner and King on his own turn, against player 1's minions.
		var banner = scn.GetShadowCard("banner");
		var king = scn.GetShadowCard("king");
		var runner = scn.GetFreepsCard("runner");
		var savage1 = scn.GetFreepsCard("savage1");
		var savage2 = scn.GetFreepsCard("savage2");

		scn.StartGame();
		scn.SkipToOtherPlayersTurn();

		scn.MoveCompanionsToTable(king);
		scn.AttachCardsTo(king, banner);
		scn.AddWoundsToChar(king, 2);
		scn.AddThreats(3);
		scn.MoveMinionsToTable(runner, savage1, savage2);
		scn.SkipToPhaseInverted(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.ShadowAssignToMinions(king, runner);
		scn.FreepsDeclineAssignments();
		scn.ShadowResolveSkirmish(king);
		scn.PassCurrentPhaseActions();

		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(banner));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(banner, "Exert a Wraith"));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(banner, "wound 2 minions"));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(banner, "heal"));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(king, "Exert a Wraith"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(banner, "Exert a Wraith"));
		assertTrue(Refused(() -> scn.ShadowChooseOptionalTrigger(banner, "heal")));
		assertTrue(Refused(() -> scn.FreepsChooseOptionalTrigger(banner, "Exert a Wraith")));
		assertEquals(2, scn.GetWoundsOn(king));

		scn.ShadowChooseOptionalTrigger(banner, "Exert a Wraith");

		// The trigger, and not the response: the King, the one Wraith there is, has exerted, and nothing was spent.
		assertEquals(3, scn.GetWoundsOn(king));
		assertAttachedTo(banner, king);
		assertEquals(3, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(savage1));
		// The response is still his to take for the same win, and the trigger is not offered a second time.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(banner, "wound 2 minions"));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(banner, "Exert a Wraith"));
	}

	protected VirtualTableScenario GetNumberScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");         // Aragorn, Ranger of the North
					// Aragorn's Pipe: "Fellowship: Discard a pipeweed possession and spot X pipes to heal X
					// companions."  The game asks how many pipes to spot, filled in with all of them.
					put("pipe", "1_91");
					put("bilbopipe", "1_285");      // Bilbo's Pipe: a second pipe, borne by Frodo
					put("toby", "1_305");           // Old Toby: a pipeweed possession, in the support area
					put("gollum", "5_24");          // Gollum, Nasty Treacherous Creature: his regroup ability is not used
					// Plotting: "To play, spot Gollum and add up to 9 threats."  The game asks how many threats
					// to add, and fills no number in.
					put("plotting", "7_67");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheNumberAQuestionIsFilledInWithIsReadAndIsNullWhereItFillsNoneIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNumberScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var pipe = scn.GetFreepsCard("pipe");
		var bilbopipe = scn.GetFreepsCard("bilbopipe");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, pipe);
		scn.AttachCardsTo(frodo, bilbopipe);
		scn.MoveCardsToSupportArea(toby);

		var gollum = scn.GetShadowCard("gollum");
		var plotting = scn.GetShadowCard("plotting");
		scn.MoveCardsToHand(plotting);

		scn.StartGame();

		// A choice of actions is no question for a number, and a player who is asked nothing has none to read.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(Refused(scn::FreepsGetChoiceDefault));
		assertTrue(Refused(scn::ShadowGetChoiceDefault));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Two pipes to spot: the question takes 0 to 2 and comes filled in with 2.
		scn.FreepsUseCardAction(pipe);
		assertTrue(scn.FreepsDecisionAvailable("Choose how many to spot"));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(2), scn.FreepsGetChoiceDefault());
		// It is the Free Peoples player's question and nobody else's.
		assertTrue(Refused(scn::ShadowGetChoiceDefault));
		// Reading it answers nothing, and what is filled in is a suggestion: another number may be given.
		assertTrue(scn.FreepsDecisionAvailable("Choose how many to spot"));
		scn.FreepsChoose("0");
		assertInZone(Zone.DISCARD, toby);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// "Add up to 9 threats", with two companions to hold them: 0 to 2, and no number filled in.
		scn.FreepsPass();
		scn.MoveMinionsToTable(gollum);
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(plotting);
		assertTrue(scn.ShadowDecisionAvailable("Choose how many threats to add"));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertNull(scn.ShadowGetChoiceDefault());
		assertTrue(Refused(scn::FreepsGetChoiceDefault));
		scn.ShadowChoose("1");

		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, plotting);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShownCardsAreCountedAndDismissedOnlyByThePlayerBeingShownThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaffer = scn.GetFreepsCard("gaffer");

		scn.StartGame();

		// Nobody is being shown anything in the fellowship phase.
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(Refused(scn::FreepsDismissRevealedCards));
		assertTrue(Refused(scn::ShadowDismissRevealedCards));
		assertTrue(Refused(scn::DismissRevealedCards));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Bag End looks through the Free Peoples player's draw deck, which that player alone is shown.
		int deck = scn.GetFreepsDeckCount();
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		assertEquals(deck, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(Refused(scn::ShadowDismissRevealedCards));
		assertEquals(deck, scn.FreepsGetTotalRevealedCardCount());

		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(gaffer);
		assertInZone(Zone.SUPPORT, gaffer);
	}
}
