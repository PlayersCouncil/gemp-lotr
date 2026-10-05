package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_040_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					// The card is not unique, and its text speaks of "this minion": two copies.
					put("rit1", "101_40");
					put("rit2", "101_40");

					// Ulaire Attea, The Easterling: a twilight [ringwraith] Nazgul, strength 12, vitality 3.
					// "When you play Ulaire Attea, you may wound an ally."  He is never played here.
					put("attea", "2_82");

					// Aragorn, Ranger of the North: strength 8, vitality 4.  A maneuver ability, never used here.
					put("aragorn", "1_89");

					// Sam, Son of Hamfast: Ring-bound as Frodo is, and not the Ring-bearer.  Vitality 4.  A fellowship
					// ability and a response to Frodo dying, neither used here.
					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				// Frodo, Son of Drogo: vitality 4, so exhausted with 3 wounds.  A fellowship ability, never used here.
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RingwraithInTwilightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Ringwraith in Twilight
		 * Unique: False
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: Twilight.
		 * 	This minion is strength +1 for each wound on the Ring-bearer.
		 * 	At the start of the maneuver phase, you may exert this minion to make it fierce until the regroup phase.
		 * 	The Free Peoples player may exert the Ring-bearer to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rit1");

		assertEquals("Ringwraith in Twilight", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CostsFourAndTwoMoreForRoamingToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rit = scn.GetShadowCard("rit1");
		scn.MoveCardsToHand(rit);

		scn.StartGame();
		scn.SetTwilight(7);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 7 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.HAND, rit);

		scn.ShadowPlayCard(rit);

		assertInZone(Zone.SHADOW_CHARACTERS, rit);
		// 10 - 4 to play - 2 roaming (a site 3 minion played at site 2)
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsStrengthPlusOneForEachWoundOnTheRingBearerAndForNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var rit1 = scn.GetShadowCard("rit1");
		var rit2 = scn.GetShadowCard("rit2");
		var attea = scn.GetShadowCard("attea");

		scn.StartGame();
		scn.MoveMinionsToTable(rit1, rit2, attea);

		// Wounds on another companion and burdens on the Ring-bearer are not wounds on the Ring-bearer.  Nor
		// are wounds on Sam, who is Ring-bound and does not bear the Ring.
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(sam, 2);
		scn.AddBurdens(2);

		assertTrue(scn.HasKeyword(sam, Keyword.RING_BOUND));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetBurdens());
		assertEquals(9, scn.GetStrength(rit1));
		assertEquals(9, scn.GetStrength(rit2));
		assertEquals(12, scn.GetStrength(attea));

		scn.AddWoundsToChar(frodo, 1);
		assertEquals(10, scn.GetStrength(rit1));
		assertEquals(10, scn.GetStrength(rit2));

		scn.AddWoundsToChar(frodo, 1);
		assertEquals(11, scn.GetStrength(rit1));
		assertEquals(11, scn.GetStrength(rit2));

		scn.AddWoundsToChar(frodo, 1);
		// 9 + 3: Frodo has vitality 4 and can hold no more.
		assertEquals(12, scn.GetStrength(rit1));
		assertEquals(12, scn.GetStrength(rit2));
		// Another twilight [ringwraith] Nazgul gains nothing: it is "this minion" that is stronger.
		assertEquals(12, scn.GetStrength(attea));
	}

	@Test
	public void ExertsAtTheStartOfTheManeuverPhaseToBeFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var rit = scn.GetShadowCard("rit1");

		scn.StartGame();
		scn.MoveMinionsToTable(rit);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(rit));
		assertFalse(scn.HasKeyword(rit, Keyword.FIERCE));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rit));

		scn.ShadowAcceptOptionalTrigger();
		// The exertion is paid before the Free Peoples player is asked.
		assertEquals(1, scn.GetWoundsOn(rit));
		assertFalse(scn.HasKeyword(rit, Keyword.FIERCE));
		assertTrue(scn.FreepsDecisionAvailable("prevent"));

		scn.FreepsChooseNo();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(rit));
		assertTrue(scn.HasKeyword(rit, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The normal skirmish: strength 9 against Aragorn's 8.
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, rit);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// A fierce minion is defended against a second time.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.HasKeyword(rit, Keyword.FIERCE));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, rit);

		// Just before the regroup phase: in its fierce skirmish, it is still fierce.
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.IsCharSkirmishingAgainst(rit, aragorn));
		assertTrue(scn.HasKeyword(rit, Keyword.FIERCE));

		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.HasKeyword(rit, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningExertsNothingAndTheOfferIsNotMadeAgainInALaterPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var rit = scn.GetShadowCard("rit1");

		scn.StartGame();
		scn.MoveMinionsToTable(rit);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rit));

		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(rit));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.HasKeyword(rit, Keyword.FIERCE));
		// The Free Peoples player is not asked whether to prevent anything.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The start of the archery phase and of the assignment phase are not the start of the maneuver phase.
		scn.PassManeuverActions();
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		scn.PassArcheryActions();
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// Not fierce: once its one skirmish is over the regroup phase follows.
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, rit);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(rit));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnExhaustedCopyIsNotOfferedTheExertionWhileOneThatCanExertIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var exhausted = scn.GetShadowCard("rit1");
		var wounded = scn.GetShadowCard("rit2");

		scn.StartGame();
		scn.MoveMinionsToTable(exhausted, wounded);
		// Vitality 3: two wounds leave one copy exhausted, one wound leaves the other able to exert once.
		scn.AddWoundsToChar(exhausted, 2);
		scn.AddWoundsToChar(wounded, 1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wounded));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(exhausted));

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(wounded));
		assertEquals(2, scn.GetWoundsOn(exhausted));

		scn.FreepsChooseNo();
		assertTrue(scn.HasKeyword(wounded, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(exhausted, Keyword.FIERCE));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void EachCopyMakesItsOwnOfferAndOnlyTheCopyUsedExertsAndBecomesFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rit1 = scn.GetShadowCard("rit1");
		var rit2 = scn.GetShadowCard("rit2");

		scn.StartGame();
		scn.MoveMinionsToTable(rit1, rit2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rit1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rit2));

		scn.ShadowChooseOptionalTrigger(rit2);
		assertEquals(1, scn.GetWoundsOn(rit2));
		assertEquals(0, scn.GetWoundsOn(rit1));

		scn.FreepsChooseNo();
		assertTrue(scn.HasKeyword(rit2, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(rit1, Keyword.FIERCE));

		// The other copy's offer is still to be answered.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rit1));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rit2));

		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(rit1));
		assertEquals(1, scn.GetWoundsOn(rit2));
		assertFalse(scn.HasKeyword(rit1, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(rit2, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayExertTheRingBearerToPreventTheFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var rit = scn.GetShadowCard("rit1");

		scn.StartGame();
		scn.MoveMinionsToTable(rit);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rit));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(rit));
		assertEquals(9, scn.GetStrength(rit));

		scn.FreepsChooseYes();
		// The Ring-bearer pays, with nothing to choose: Sam is Ring-bound too, and it is not his to do.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertFalse(scn.HasKeyword(rit, Keyword.FIERCE));
		// What was prevented is the fierce; the minion's own exertion was a cost and stays paid.
		assertEquals(1, scn.GetWoundsOn(rit));
		// The Ring-bearer's exertion is a wound on the Ring-bearer: 9 + 1.
		assertEquals(10, scn.GetStrength(rit));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Not fierce: once its one skirmish is over the regroup phase follows.
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, rit);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnExhaustedRingBearerCannotPreventTheFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var rit = scn.GetShadowCard("rit1");

		scn.StartGame();
		scn.MoveMinionsToTable(rit);
		// Vitality 4: exhausted.  Aragorn and Sam beside him could exert, and it is not theirs to do, though
		// Sam is Ring-bound as Frodo is.
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rit));
		assertFalse(scn.HasKeyword(rit, Keyword.FIERCE));

		scn.ShadowAcceptOptionalTrigger();

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(1, scn.GetWoundsOn(rit));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.HasKeyword(rit, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
