package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_91_013_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Aragorn, Ranger of the North: a [gondor] Man companion, vitality 4.
					// "Maneuver: Exert Aragorn to make him defender +1 until the regroup phase."
					put("aragorn", "1_89");
					// Legolas, Greenleaf: an archer. "Archery: Exert Legolas to wound a minion; Legolas does not
					// add to the fellowship archery total." The Free Peoples action of the phase after the maneuver phase.
					put("legolas", "1_50");
					// Defend It and Hope: a Free Peoples Maneuver event, cost 0: exert a [gondor] Man to wound a minion.
					put("defend", "4_115");
					// Saruman, Keeper of Isengard (the errata): an [isengard] minion.
					// "Maneuver: Exert Saruman to make an Uruk-hai fierce until the regroup phase."
					put("saruman", "53_68");
					// Hunt Them Down!: a Shadow Maneuver event, cost 3: make an Uruk-hai fierce until the regroup phase.
					put("hunt", "1_126");
					// Uruk Savage: an [isengard] Uruk-hai, vitality 3; a minion on the table, so that there is a maneuver phase
					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_13", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Aragorn, Ranger of the North: a [gondor] Man companion, vitality 4.
					// "Maneuver: Exert Aragorn to make him defender +1 until the regroup phase."
					put("aragorn", "1_89");
					// Legolas, Greenleaf: an archer. "Archery: Exert Legolas to wound a minion; Legolas does not
					// add to the fellowship archery total." The Free Peoples action of the phase after the maneuver phase.
					put("legolas", "1_50");
					// Defend It and Hope: a Free Peoples Maneuver event, cost 0: exert a [gondor] Man to wound a minion.
					put("defend", "4_115");
					// Saruman, Keeper of Isengard (the errata): an [isengard] minion.
					// "Maneuver: Exert Saruman to make an Uruk-hai fierce until the regroup phase."
					put("saruman", "53_68");
					// Hunt Them Down!: a Shadow Maneuver event, cost 3: make an Uruk-hai fierce until the regroup phase.
					put("hunt", "1_126");
					// Uruk Savage: an [isengard] Uruk-hai, vitality 3; a minion on the table, so that there is a maneuver phase
					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_13"
		);
	}

	@Test
	public void NoManeuverActionsStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_13
		 * Type: MetaSite
		 * Intensity: 2
		 * Game Text: You may not perform maneuver phase actions.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_13", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getIntensity());
	}

	@Test
	public void FreepsCannotUseManeuverActionsWithMod() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_13: "You may not perform maneuver phase actions."
		// FP has the modifier. Aragorn's maneuver ability should be unavailable.

		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);
		scn.MoveMinionsToTable(savage);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		// Freeps should NOT be able to use Aragorn's maneuver action
		assertFalse(scn.FreepsActionAvailable(aragorn));
		// nor to play a Maneuver event: Aragorn is there to exert for it and the Savage to be wounded
		assertFalse(scn.FreepsPlayAvailable(defend));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheOwnerMayStillActInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveMinionsToTable(savage);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.FreepsActionAvailable(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The archery phase is the next one: only maneuver actions are forbidden.
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsActionAvailable(legolas));

		scn.FreepsUseCardAction(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ShadowCanStillUseManeuverActionsWhenFreepsHasMod() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_13: Freeps has the modifier. Shadow should still be able to use their actions.

		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var saruman = scn.GetShadowCard("saruman");
		var hunt = scn.GetShadowCard("hunt");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(saruman, savage);
		scn.MoveCardsToHand(hunt);

		scn.StartGame();
		// Enough for Hunt Them Down! (3)
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.FreepsActionAvailable(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPass();

		// Shadow should still be able to use Saruman's maneuver action
		assertTrue(scn.ShadowActionAvailable(saruman));
		// and to play a Maneuver event
		assertTrue(scn.ShadowPlayAvailable(hunt));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ShadowCannotUseManeuverActionsWithShadowMod() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_13: Shadow has the modifier. Saruman's maneuver action should be unavailable.

		var scn = GetShadowScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		var saruman = scn.GetShadowCard("saruman");
		var hunt = scn.GetShadowCard("hunt");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);
		scn.MoveMinionsToTable(saruman, savage);
		scn.MoveCardsToHand(hunt);

		scn.StartGame();
		// Enough for Hunt Them Down! (3)
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.MANEUVER);
		// The modifier is the other player's: Freeps has his maneuver ability and his Maneuver event.
		assertTrue(scn.FreepsActionAvailable(aragorn));
		assertTrue(scn.FreepsPlayAvailable(defend));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPass();

		// Shadow should NOT be able to use Saruman's maneuver action
		assertFalse(scn.ShadowActionAvailable(saruman));
		// nor to play a Maneuver event he could pay for, with an Uruk-hai on the table for it
		assertFalse(scn.ShadowPlayAvailable(hunt));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
