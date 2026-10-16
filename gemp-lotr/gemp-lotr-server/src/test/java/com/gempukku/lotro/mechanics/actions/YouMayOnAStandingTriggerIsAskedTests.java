package com.gempukku.lotro.mechanics.actions;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * "Each time ..., you may ...": the player is asked, each time, and may say no.
 *
 * Ketura on Q-0021: a "you may" that the game carried out without asking "wasn't intentional, that's an
 * oversight"; and on F-0041, of Stewards' Legacy, "Oversight, please fix".  Q-0090: once the offer is taken and one
 * thing is left to do, it is done without a second question.
 *
 * Shown on one standing optional trigger, Stewards' Legacy 13_75's "Each time Boromir exerts, you may exert a
 * minion" (once built as a required trigger with a choice of 0 to 1 minions: the player was never asked whether,
 * only which): asked, declined, and asked again and taken at his next exertion.  The card's own boards (its other
 * two sentences, the minions that can and cannot exert, a trigger with nothing to do) are Card_13_075_Tests'.
 *
 * The exertions are made with Ettenmoors 1_331, site 2 of the Fellowship path ("Skirmish: Exert your companion or
 * minion to make that character strength +2").
 */
public class YouMayOnAStandingTriggerIsAskedTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legacy", "13_75");     // Stewards' Legacy: [gondor] condition in the support area
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, vitality 3; no ability to use

					// Uruk Slayer: vitality 2, so it can exert once.
					put("slayer1", "1_153");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
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
}
