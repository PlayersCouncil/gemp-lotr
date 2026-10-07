package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

/**
 * Keywords copied from another card "until ..." are a snapshot.
 *
 * Ketura, 2026-10-07, on Q-0096 (Northern Signal-fire, Flame of Calenhad 103_86: "Until the regroup phase, each
 * of your other Men gain each of the unloaded keywords (except race) on that Man"): "The intent was that a
 * snapshot be made at the time of invocation, and whatever keywords were on the target would get memorized and
 * applied to everyone else."  Comprehensive Rules 4.2, "game text": a boldfaced keyword in the text box, number
 * included, is game text, so a copied "Damage +1" is gained with its number (Ketura on F-0126).
 *
 * The card's own tests show the others keeping the keywords when the Man is killed or hindered.  What they cannot
 * show, an unloaded keyword having no number, is shown here with a card of the tests' own.
 */
public class KeywordSnapshotTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Test War-horn (the tests' own card): "Maneuver: Exert your minion. Until the regroup phase,
					// each of your other minions gains each keyword on that minion."
					put("horn", "0_8002");
					put("party", "1_158");      // Uruk-hai Raiding Party: vitality 3; Damage +1 is its whole text
					put("savage", "1_151");     // Uruk Savage: vitality 3; Damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: no text at all

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KeywordsTakenAsASnapshotKeepTheirNumbersAndStayWhateverBecomesOfTheCardTheyWereOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var horn = scn.GetShadowCard("horn");
		var party = scn.GetShadowCard("party");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(horn);

		scn.StartGame();
		scn.MoveMinionsToTable(party, savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(1, scn.GetKeywordCount(party, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));

		scn.ShadowUseCardAction(horn);
		scn.ShadowChooseCard(party);

		// Damage +1 is gained with its number: the Savage has 1 of its own and 1 copied.
		assertEquals(1, scn.GetWoundsOn(party));
		assertEquals(1, scn.GetKeywordCount(party, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(troop, Keyword.DAMAGE));

		// What the Raiding Party gains afterwards was not on it when the ability was used.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, party, null, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(party, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));

		// And when it is gone the others keep what was taken from it.
		scn.MoveCardsToDiscard(party);
		assertInZone(Zone.DISCARD, party);
		assertEquals(2, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
