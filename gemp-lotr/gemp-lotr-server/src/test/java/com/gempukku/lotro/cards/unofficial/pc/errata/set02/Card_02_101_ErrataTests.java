package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_101_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("notfatty", "52_101");
					put("sam", "1_311");
					put("bilbo", "1_284");
					put("aragorn", "6_50");

					put("tracker1", "4_190");
					put("tracker2", "4_190");
					put("tracker3", "4_190");
					put("tracker4", "4_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FilibertBolgerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 2
		* Title: *Filibert Bolger, Wily Rascal
		* Side: Free Peoples
		* Culture: Shire
		* Twilight Cost: 1
		* Type: ally
		* Subtype: Hobbit
		* Strength: 1
		* Vitality: 2
		* Site Number: 1
		* Game Text: <b>Skirmish:</b> Exert a Hobbit companion twice (except the Ring-bearer)
		 * to cancel a fierce skirmish involving that Hobbit.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var notfatty = scn.GetFreepsCard("notfatty");

		assertTrue(notfatty.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, notfatty.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, notfatty.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, notfatty.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, notfatty.getBlueprint().getRace());
		assertEquals(1, notfatty.getBlueprint().getTwilightCost());
		assertEquals(1, notfatty.getBlueprint().getStrength());
		assertEquals(2, notfatty.getBlueprint().getVitality());
		assertTrue(notfatty.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 1)));
	}

	@Test
	public void FilibertAbilityCancelsFierceSkirmishWithNonRBHobbitCompanionsAndNoOneElse() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var notfatty = scn.GetFreepsCard("notfatty");
		var bilbo = scn.GetFreepsCard("bilbo");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(notfatty, bilbo);
		scn.MoveCompanionsToTable(sam, aragorn);

		var tracker1 = scn.GetShadowCard("tracker1");
		var tracker2 = scn.GetShadowCard("tracker2");
		var tracker3 = scn.GetShadowCard("tracker3");
		var tracker4 = scn.GetShadowCard("tracker4");

		scn.StartGame();

		//We want to get to site 3 for Bilbo to skirmish
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseToMove();

		scn.MoveMinionsToTable(tracker1, tracker2, tracker3, tracker4);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, tracker1}, new PhysicalCardImpl[]{sam, tracker2});
		scn.ShadowDeclineAssignments();

		//RB cannot use Frederick's ability at all
		scn.FreepsResolveSkirmish(frodo);
		assertFalse(scn.FreepsActionAvailable(notfatty));
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineOptionalTrigger(); // one ring

		//Sam cannot use Frederick's ability during a non-fierce skirmish
		scn.FreepsResolveSkirmish(sam);
		assertFalse(scn.FreepsActionAvailable(notfatty));
		scn.PassCurrentPhaseActions();

		//Fierce assignement actions
		scn.PassCurrentPhaseActions();

		//fierce skirmishes
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{frodo, tracker1},
				new PhysicalCardImpl[]{sam, tracker2},
				new PhysicalCardImpl[]{bilbo, tracker3},
				new PhysicalCardImpl[]{aragorn, tracker4});

		//RB cannot use Frederick's ability at all
		scn.FreepsResolveSkirmish(frodo);
		assertFalse(scn.FreepsActionAvailable(notfatty));
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineOptionalTrigger(); // one ring

		//Sam can successfully use Frederick's ability
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.FreepsActionAvailable(notfatty));
		assertEquals(1, scn.GetWoundsOn(sam));
		var skirmish = scn.GetActiveSkirmish();
		assertSame(skirmish.getFellowshipCharacter(), sam);
		assertFalse(skirmish.isCancelled());
		scn.FreepsUseCardAction(notfatty);
		assertEquals(3, scn.GetWoundsOn(sam));
		assertTrue(skirmish.isCancelled());

		//Bilbo as an ally cannot use Frederick's ability at all
		scn.FreepsResolveSkirmish(bilbo);
		assertFalse(scn.FreepsActionAvailable(notfatty));
		scn.PassCurrentPhaseActions();

		//Aragorn as a non-shire companion cannot use Frederick's ability at all
		scn.FreepsResolveSkirmish(aragorn);
		assertFalse(scn.FreepsActionAvailable(notfatty));
		scn.PassCurrentPhaseActions();

	}
}
