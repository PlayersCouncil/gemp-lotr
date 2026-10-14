package com.gempukku.lotro.mechanics.ring;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * "Remove up to N burdens" under a bar on removing burdens asks nothing (F-0512).
 *
 * A definition's `removeBurdens` written with a range (`amount: 0-2`) asks "Choose how many burdens to remove",
 * from its minimum up to its maximum, held to the burdens in play.  Where burdens may not be removed (the
 * definition word `cantRemoveBurdens`: Black Breath 1_207, Spirit of Dread 16_5, Steward's Tomb 10_119 and 18_139)
 * no number chosen would remove one, so as an EFFECT the burdens that can be removed are taken as none: the
 * question is not asked and nothing is done (F-0379's shape: a "you may" whose yes does nothing).  A cost is not
 * reached: it cannot be paid under the bar, and is not offered.
 *
 * The card: Cliffs of Emyn Muil 4_299, "When the fellowship moves from site 1[T], remove up to 2 burdens", a
 * required trigger with a choice of 0 to 2.  The bar: Black Breath 1_207, "Burdens and wounds may not be removed
 * from bearer", which bars every removal of a burden while it is borne by the Ring-bearer, and none while it is
 * borne by another companion (the control).
 */
public class ABarOnRemovingBurdensTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Cliffs of Emyn Muil: a [shire] condition of the support area; its skirmish ability is never used.
					put("cliffs", "4_299");
					// Black Breath: a [wraith] condition; its skirmish transfer is never used.
					put("breath", "1_207");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion; his maneuver ability is never used
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WithNoBarUpToTwoBurdensAreAskedFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cliffs = scn.GetFreepsCard("cliffs");
		scn.MoveCardsToSupportArea(cliffs);

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());

		scn.FreepsPass();

		assertTrue(scn.FreepsDecisionAvailable("Choose how many burdens to remove"));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");
		assertEquals(1, scn.GetBurdens());
	}

	@Test
	public void UnderABarOnRemovingBurdensNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cliffs = scn.GetFreepsCard("cliffs");
		var breath = scn.GetShadowCard("breath");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(cliffs);
		scn.AttachCardsTo(frodo, breath);

		scn.StartGame();
		scn.AddBurdens(3);
		assertAttachedTo(breath, frodo);
		assertEquals(3, scn.GetBurdens());

		scn.FreepsPass();

		assertFalse(scn.FreepsDecisionAvailable("Choose how many burdens to remove"));
		assertEquals(3, scn.GetBurdens());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheBarNotInForceLeavesTheQuestionAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cliffs = scn.GetFreepsCard("cliffs");
		var breath = scn.GetShadowCard("breath");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(cliffs);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, breath);

		scn.StartGame();
		scn.AddBurdens(1);
		assertAttachedTo(breath, aragorn);

		scn.FreepsPass();

		// Borne by a companion who is not the Ring-bearer, Black Breath bars no burden; the choice is held to the one
		// burden in play.
		assertTrue(scn.FreepsDecisionAvailable("Choose how many burdens to remove"));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");
		assertEquals(0, scn.GetBurdens());
	}
}
