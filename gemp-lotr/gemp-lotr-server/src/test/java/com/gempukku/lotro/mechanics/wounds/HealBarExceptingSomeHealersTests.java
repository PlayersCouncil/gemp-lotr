package com.gempukku.lotro.mechanics.wounds;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * A bar on healing may except the heals of some cards.
 *
 * Spirit of Dread 16_5: "While you can spot another [wraith] Wraith, wounds cannot be healed and burdens can not be
 * removed (except by Wraiths)."  Ketura's ruling (Q-0357: A) is that the parenthesis reaches both halves.  The bar
 * on removing burdens always went by the card that removes (CantRemoveBurdensModifier), but the bar on healing
 * went only by the card healed and the player who heals, so "except by Wraiths" could not be said of a heal.  Now
 * every heal is asked about with the card whose text makes it as well (ModifiersQuerying.canBeHealed's
 * healSource), and the definition word cantHeal takes a `by` filter of the healers it bars, every card unless one
 * is given.  A heal made by no card is barred whatever `by` says.
 *
 * The exception is shown on the one card that has it, in its own tests: Card_16_005_Tests, where King of the Dead
 * 103_27, a Wraith, heals the Dead Man of Dunharrow 10_27 under the bar and Faramir 18_48, no Wraith, does not.
 * Shown here is the other side: a bar that names no healers (Steward's Tomb 10_119, "Wounds cannot be prevented or
 * healed") still stops a Wraith's own heal, so that the exception is the definition's word and no pass for Wraiths.
 */
public class HealBarExceptingSomeHealersTests
{
	protected VirtualTableScenario GetStewardsTombScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// King of the Dead, Dead Keeper: [gondor] Wraith companion; "Assignment: Exert King of the Dead and
					// restore a Wraith to heal that Wraith"
					put("king", "103_27");
					// Dead Man of Dunharrow: [gondor] Wraith companion; cheated in, so its play trigger never fires
					put("deadman", "10_27");
					put("savage", "1_151");     // Uruk Savage: Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
				}},
				new HashMap<>()
				{{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("tomb", "10_119");      // Steward's Tomb: Wounds cannot be prevented or healed. Burdens cannot be removed.
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ABarThatExceptsNoHealersStopsAWraithsOwnHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStewardsTombScenario();

		var king = scn.GetFreepsCard("king");
		var deadman = scn.GetFreepsCard("deadman");
		scn.MoveCompanionsToTable(king, deadman);

		var tomb = scn.GetShadowSite("tomb");   // on player 1's turn the site ahead is laid by the Shadow player
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(deadman, 1);
		scn.HinderCard(deadman);
		assertEquals(1, scn.GetWoundsOn(deadman));
		assertEquals(0, scn.GetWoundsOn(king));

		// The fellowship moves to site 5, Steward's Tomb.
		scn.FreepsPass();
		assertEquals(tomb, scn.GetCurrentSite());
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The King's costs are paid, the Dead Man restored, and the heal, a Wraith's, is refused all the same.
		assertTrue(scn.FreepsActionAvailable(king));
		scn.FreepsUseCardAction(king);
		assertEquals(1, scn.GetWoundsOn(king));
		assertFalse(scn.IsHindered(deadman));
		assertEquals(1, scn.GetWoundsOn(deadman));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}
}
