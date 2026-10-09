package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * The requirement canAddBurdens: whether a burden could be added by this card now.
 *
 * It asks what the adding of a burden itself asks, so it is false while the Ring-bearer is hindered (Comprehensive
 * Rules 5.0, "hinder": burdens cannot be added) and while a card's "burdens cannot be added" is in force.  It is
 * for a standing text that must know ahead whether a burden it will add later can be added: "The Free Peoples
 * player must add a burden to assign ..." bars the assignment while the burden cannot be added (F-0308, Ulaire
 * Nertea 69_38).
 *
 * Shown with cards of the tests' own: Test Tally 0_8021, "Fellowship: If a burden could be added, add (1)", and
 * Test Seal 0_8004, "Burdens cannot be added."
 */
public class CanAddBurdensRequirementTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Test Tally (the tests' own card): "Fellowship: If a burden could be added, add (1)."
					put("tally", "0_8021");
					// Test Seal (the tests' own card): "Burdens cannot be added."
					put("seal", "0_8004");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MetUnlessTheRingBearerIsHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tally = scn.GetFreepsCard("tally");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(tally);

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(tally));
		scn.FreepsUseCardAction(tally);
		assertEquals(1, scn.GetTwilight());

		scn.HinderCard(frodo);
		scn.FreepsRefreshDecision();

		assertTrue(scn.IsHindered(frodo));
		assertFalse(scn.FreepsActionAvailable(tally));

		scn.RestoreCard(frodo);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsActionAvailable(tally));
	}

	@Test
	public void NotMetWhileACardSaysBurdensCannotBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tally = scn.GetFreepsCard("tally");
		var seal = scn.GetFreepsCard("seal");
		scn.MoveCardsToSupportArea(tally);

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(tally));

		scn.MoveCardsToSupportArea(seal);
		scn.FreepsRefreshDecision();

		assertFalse(scn.FreepsActionAvailable(tally));
		assertEquals(0, scn.GetTwilight());
	}
}
