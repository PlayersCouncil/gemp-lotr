package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * "Each time ..., you may ...": the player is asked, each time, and may say no.
 *
 * Ketura on Q-0021: a "you may" that the game carried out without asking "wasn't intentional, that's an
 * oversight"; and on F-0041, of Stewards' Legacy, "Oversight, please fix".  Against it, Q-0017: with no "may" in
 * the text nothing is asked.  And Q-0034: an optional trigger with no cost whose whole effect would do nothing is
 * not offered at all, while a spot before "to" is checked like a cost (it pays nothing, and with nothing to spot
 * the trigger is not offered).  Q-0090: once the offer is taken and one thing is left to do, it is done without
 * a second question.
 *
 * The card is Stewards' Legacy 13_75, which has one of each on one condition:
 *   "Each time Denethor exerts, he gains muster until the end of the regroup phase."         (no "may": required)
 *   "Each time Boromir exerts, you may exert a minion."                                       (asked)
 *   "Each time Faramir exerts, you may spot a minion to prevent it from using special abilities until the
 *    regroup phase."                                                                          (asked)
 * All three were built as required triggers, the two "you may" ones with a choice of 0 to 1 minions: the player
 * was never asked whether, only which.
 *
 * The exertions are made with Ettenmoors 1_331, site 2 of the Fellowship path ("Skirmish: Exert your companion or
 * minion to make that character strength +2"), and, where there must be no minion in play, with Dear Friends 2_98
 * in the fellowship phase ("Exert a companion to place a Free Peoples card from your discard pile beneath your
 * draw deck"; with an empty discard pile it places nothing).
 */
public class YouMayOnAStandingTriggerIsAskedTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legacy", "13_75");     // Stewards' Legacy: [gondor] condition in the support area
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, vitality 3; no ability to use
					put("faramir", "13_66");    // Faramir, Prince of Ithilien: strength 7, vitality 3; no ability to use
					put("denethor", "7_86");    // Denethor, Wizened Steward: vitality 3; stronger for each other [gondor] Man, nothing else
					put("dear1", "2_98");       // Dear Friends
					put("dear2", "2_98");
					put("dear3", "2_98");

					// Uruk Slayer: vitality 2, so it can exert once.  "Skirmish: Remove (1) to make this minion
					// strength +1 (limit +3)": a minion's special ability to stop.
					put("slayer1", "1_153");
					put("slayer2", "1_153");
					put("runner", "1_178");     // Goblin Runner: vitality 1, so it cannot exert
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EachTimeBoromirExertsThePlayerIsAskedAndAMinionAbleToExertIsChosenAndExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var boromir = scn.GetFreepsCard("boromir");
		var slayer1 = scn.GetShadowCard("slayer1");
		var slayer2 = scn.GetShadowCard("slayer2");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(legacy);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer1, slayer2, runner);
		scn.SkipToAssignments();
		assertEquals("Ettenmoors", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.FreepsAssignToMinions(boromir, slayer1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);

		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));

		// Asked whether, and nothing has happened yet.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legacy));
		assertEquals(0, scn.GetWoundsOn(slayer1));
		assertEquals(0, scn.GetWoundsOn(slayer2));
		scn.FreepsAcceptOptionalTrigger();

		// "A minion" is one minion, and having said yes the player chooses exactly one: a minion that cannot
		// exert is not among them.
		assertTrue(scn.FreepsHasCardChoiceAvailable(slayer1, slayer2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(slayer2);

		assertEquals(1, scn.GetWoundsOn(slayer2));
		assertEquals(0, scn.GetWoundsOn(slayer1));
		assertEquals(0, scn.GetWoundsOn(runner));
		// Ettenmoors' own effect followed.
		assertEquals(9, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheOfferForBoromirMayBeDeclinedAndIsMadeAfreshTheNextTimeHeExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var boromir = scn.GetFreepsCard("boromir");
		var slayer1 = scn.GetShadowCard("slayer1");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(legacy);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, slayer1);
		scn.FreepsResolveSkirmish(boromir);

		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(boromir);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legacy));
		scn.FreepsDeclineOptionalTrigger();

		// Declined: no minion is exerted and nobody is asked to choose one.
		assertEquals(0, scn.GetWoundsOn(slayer1));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(9, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// "Each time": his next exertion is another offer, and with one minion able to exert, taking it leaves
		// nothing to choose.
		scn.ShadowPass();
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(boromir);
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legacy));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(slayer1));
		assertEquals(11, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoMinionAbleToExertNothingIsOfferedWhenBoromirExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var boromir = scn.GetFreepsCard("boromir");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(legacy);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, runner);
		scn.FreepsResolveSkirmish(boromir);

		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(boromir);

		// The only minion has 1 vitality: a yes would exert nobody, so the question is not put.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(9, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void EachTimeFaramirExertsThePlayerIsAskedAndTheMinionSpottedCannotUseSpecialAbilities() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var faramir = scn.GetFreepsCard("faramir");
		var slayer1 = scn.GetShadowCard("slayer1");
		var slayer2 = scn.GetShadowCard("slayer2");
		scn.MoveCompanionsToTable(faramir);
		scn.MoveCardsToSupportArea(legacy);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer1, slayer2);
		scn.SkipToAssignments();
		scn.SetTwilight(3);
		scn.FreepsAssignToMinions(faramir, slayer1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(faramir);

		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(faramir);
		assertEquals(1, scn.GetWoundsOn(faramir));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legacy));
		scn.FreepsAcceptOptionalTrigger();

		// Exactly one minion is spotted, of the player's choosing.
		assertTrue(scn.FreepsHasCardChoiceAvailable(slayer1, slayer2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(slayer1);

		// A spot costs the minion nothing; "it" is the one spotted, and the other copy keeps its ability.
		assertEquals(0, scn.GetWoundsOn(slayer1));
		assertEquals(0, scn.GetWoundsOn(slayer2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(slayer1));
		assertTrue(scn.ShadowActionAvailable(slayer2));
	}

	@Test
	public void TheOfferForFaramirMayBeDeclinedAndTheMinionKeepsItsSpecialAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var faramir = scn.GetFreepsCard("faramir");
		var slayer1 = scn.GetShadowCard("slayer1");
		scn.MoveCompanionsToTable(faramir);
		scn.MoveCardsToSupportArea(legacy);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer1);
		scn.SkipToAssignments();
		scn.SetTwilight(3);
		scn.FreepsAssignToMinions(faramir, slayer1);
		scn.FreepsResolveSkirmish(faramir);

		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(faramir);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legacy));
		scn.FreepsDeclineOptionalTrigger();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(slayer1));
		scn.ShadowUseCardAction(slayer1);
		assertEquals(2, scn.GetTwilight());
		assertEquals(10, scn.GetStrength(slayer1));
	}

	@Test
	public void WithNoMinionInPlayNeitherOfferIsMadeAndDenethorsMusterIsNeverAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var boromir = scn.GetFreepsCard("boromir");
		var faramir = scn.GetFreepsCard("faramir");
		var denethor = scn.GetFreepsCard("denethor");
		var dear1 = scn.GetFreepsCard("dear1");
		var dear2 = scn.GetFreepsCard("dear2");
		var dear3 = scn.GetFreepsCard("dear3");
		scn.MoveCompanionsToTable(boromir, faramir, denethor);
		scn.MoveCardsToSupportArea(legacy);
		scn.MoveCardsToHand(dear1, dear2, dear3);

		scn.StartGame();

		// The fellowship phase: no minion to exert, and none to spot.
		scn.FreepsPlayCard(dear1);
		scn.FreepsChooseCard(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(dear2);
		scn.FreepsChooseCard(faramir);
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// "Each time Denethor exerts, he gains muster": no "may", so it happens with nothing asked.
		assertFalse(scn.HasKeyword(denethor, Keyword.MUSTER));
		scn.FreepsPlayCard(dear3);
		scn.FreepsChooseCard(denethor);
		assertEquals(1, scn.GetWoundsOn(denethor));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.HasKeyword(denethor, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(boromir, Keyword.MUSTER));
		assertInZone(Zone.SUPPORT, legacy);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
