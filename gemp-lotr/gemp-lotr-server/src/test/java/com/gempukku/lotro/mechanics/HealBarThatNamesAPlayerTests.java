package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInDiscard;
import static org.junit.Assert.*;

/**
 * A bar on healing may name the player who may not heal.
 *
 * Comprehensive Rules 4.2, "healing": "When you 'heal a character,' you remove one wound."  A heal is made by a
 * player, and either player can heal a card of either side: the Free Peoples player heals a minion with Hrethel
 * 6_95 ("Skirmish: Heal a minion skirmishing Hrethel to make Hrethel strength +3"), the Shadow player his own
 * with Scaling Ladder 5_57 ("Skirmish: Spot an [isengard] token here to heal an Uruk-hai.  Discard this
 * condition.").
 *
 * The game used to ask only whether a card could be healed, never by whom, so the definition word cantHeal
 * stopped every heal of the cards it reached and a text that limits one player's heals ("you may heal no more
 * than one wound from each character", Race Text 92_35) could not be built without stopping the other player's
 * as well (F-0319).  Now every heal is asked about with the player who makes it, and cantHeal may name a player:
 * his heals are stopped and the other player's are not.  A cantHeal that names nobody stops both, as before.
 *
 * The one printed text that names a player is Race Text 92_35's, and the named bar is shown in that card's own
 * tests (Card_92_035_Tests: a minion its owner has healed at a sanctuary is still healed by the Shadow player).
 * The bar that names nobody is shown here: Steward's Tomb 10_119, "Wounds cannot be prevented or healed."
 */
public class HealBarThatNamesAPlayerTests
{
	// The King-block sites of the rig, with Steward's Tomb as site 5.
	protected VirtualTableScenario GetStewardsTombScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hrethel", "6_95");      // Hrethel, Rider of Rohan: strength 6; Skirmish: heal a minion skirmishing
					                             //   Hrethel to make Hrethel strength +3 (limit +3)
					put("savage", "1_151");      // Uruk Savage: Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					put("ladder1", "5_57");      // Scaling Ladder: Shadow support-area condition; Skirmish: spot an [isengard]
					                             //   token here to heal an Uruk-hai, and discard this condition
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
	public void ABarThatNamesNobodyStopsBothPlayersHeals() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStewardsTombScenario();

		var hrethel = scn.GetFreepsCard("hrethel");
		scn.MoveCompanionsToTable(hrethel);

		var tomb = scn.GetShadowSite("tomb");   // on player 1's turn the site ahead is laid by the Shadow player
		var savage = scn.GetShadowCard("savage");
		var ladder1 = scn.GetShadowCard("ladder1");
		scn.MoveCardsToSupportArea(ladder1);
		scn.AddTokensToCard(ladder1, 1);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(savage, 2);

		// The fellowship moves to site 5, Steward's Tomb, and fights there.
		scn.FreepsPass();
		assertEquals(tomb, scn.GetCurrentSite());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(hrethel, savage);
		scn.FreepsResolveSkirmish(hrethel);

		// The Free Peoples player cannot heal the Savage, so Hrethel's cost cannot be paid.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(hrethel));
		scn.FreepsPass();

		// Nor can the Shadow player: his Ladder is spent and heals nothing.
		assertTrue(scn.ShadowActionAvailable(ladder1));
		scn.ShadowUseCardAction(ladder1);
		assertEquals(2, scn.GetWoundsOn(savage));
		assertInDiscard(ladder1);

		assertFalse(scn.FreepsActionAvailable(hrethel));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
