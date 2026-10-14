package com.gempukku.lotro.mechanics.wounds;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * A wound is no exertion: the other half of AWoundThatIsNotTakenTests.
 *
 * Comprehensive Rules 4.2, "exert": "Exerting a character is different from wounding a character, even though both
 * require placement of a wound token."  A wound placed on a character, by a card's effect or by a lost skirmish,
 * is no exertion of his, and nothing that answers "each time X exerts" answers it.  That is shown once, here
 * (Ketura on Q-0315, 2026-10-14: "So long as the relevant triggers are tested in the mechanics tests, I'd say this
 * also doesn't need to repeat it on all relevant cards. A."), and not card by card.
 *
 * What answers an exertion in these tests is the Boromir sentence of Stewards' Legacy 13_75, "Each time Boromir
 * exerts, you may exert a minion": an optional trigger with no cost, offered only while a minion can exert
 * (Q-0034), so each board keeps a minion able to exert when Boromir is wounded.  Its control in each test is
 * Boromir exerting as the cost of Defend It and Hope 4_115.
 */
public class AWoundIsNoExertionTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Stewards' Legacy, a [gondor] condition of the support area: "Each time Denethor exerts, he gains
					// muster until the end of the regroup phase.  Each time Boromir exerts, you may exert a minion.
					// Each time Faramir exerts, you may spot a minion to prevent it from using special abilities
					// until the regroup phase."  No Denethor or Faramir is laid.
					put("legacy", "13_75");
					// Boromir, Lord of Gondor: a [gondor] Man, strength 7, vitality 3; "Ranger.  Boromir is not
					// overwhelmed unless his strength is tripled."
					put("boromir", "1_96");
					// Defend It and Hope: "Maneuver: Exert a [gondor] Man to wound a minion once (or twice if that
					// minion is an Uruk-hai)."  Boromir is the one [gondor] Man.
					put("defendit", "4_115");

					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, strength 13, vitality 3, no text
					// Morgul Slayer: a [sauron] Orc, vitality 2; "Regroup: Exert this minion to wound a companion
					// (except the Ring-bearer)."
					put("mslayer", "3_93");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AWoundFromALostSkirmishIsNoExertionAndNothingThatAnswersAnExertionAnswersIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var boromir = scn.GetFreepsCard("boromir");
		var defendit = scn.GetFreepsCard("defendit");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(legacy);
		scn.MoveCardsToHand(defendit);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// The control: Boromir exerts for Defend It and Hope, and the exertion is answered.
		scn.FreepsPlayCard(defendit);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legacy));
		scn.FreepsDeclineOptionalTrigger();
		// The event's own wound goes to the Troop, the one minion.
		assertEquals(1, scn.GetWoundsOn(troop));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, troop);
		scn.FreepsResolveSkirmish(boromir);
		// 7 against 13: a loss, and not an overwhelming (that wants 21).
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(13, scn.GetStrength(troop));
		scn.PassSkirmishActions();

		// Boromir takes the wound of the lost skirmish.  The Troop, with 2 vitality left, could still exert, and
		// nothing is offered: a wound is no exertion.
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetVitality(troop));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(legacy));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AWoundFromACardsEffectIsNoExertionAndNothingThatAnswersAnExertionAnswersIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legacy = scn.GetFreepsCard("legacy");
		var boromir = scn.GetFreepsCard("boromir");
		var defendit = scn.GetFreepsCard("defendit");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(legacy);
		scn.MoveCardsToHand(defendit);

		var troop = scn.GetShadowCard("troop");
		var mslayer = scn.GetShadowCard("mslayer");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, mslayer);
		scn.SkipToPhase(Phase.MANEUVER);

		// The control: Boromir exerts for Defend It and Hope, and the exertion is answered.
		scn.FreepsPlayCard(defendit);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legacy));
		scn.FreepsDeclineOptionalTrigger();
		// The event's own wound goes to the Troop, so that the Slayer can still use his ability.
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(mslayer));

		// No skirmish: the fellowship goes on to the regroup phase with Boromir's one exertion on him.
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(1, scn.GetWoundsOn(boromir));
		scn.FreepsPass();

		// The Slayer exerts to wound Boromir, the one companion he may choose.  The Troop, with 2 vitality left,
		// could still exert, and nothing is offered: the Slayer's exertion is not Boromir's, and Boromir's wound is
		// no exertion.
		scn.ShadowUseCardAction(mslayer);
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(mslayer));
		assertEquals(2, scn.GetVitality(troop));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(legacy));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}
}
