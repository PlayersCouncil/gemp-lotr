package com.gempukku.lotro.mechanics.inplay;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * "Remove N tokens from <a card>" as an effect, where no card holds N.
 *
 * Comprehensive Rules 4.2, "effect": "If the effect of a card or special ability requires you to choose one of two
 * different actions, you must choose an action that you are fully capable of performing (if possible)", and "If
 * the effect of a card or special ability requires you to perform an action and you cannot, you must perform as
 * much as you can and ignore the rest."
 *
 * Ketura on F-0702 (2026-11-03): "effects should do as much as they can in that case where the other half of the
 * choice isn't doable."
 *
 * So, for a removal of N tokens from a card chosen, as an effect:
 * 1. while some card holds N, the choice is of those cards (a choice is of what can be fully performed, as "exert
 *    X twice" is on F-0131), and the removal is done in full;
 * 2. with none holding N, any card holding a token may be chosen and gives up what it has;
 * 3. whether the removal can be done in full still wants a card holding N, so a choice of branches offers it
 *    as one that cannot (F-0310): carried out unasked beside a branch that can, offered beside one that cannot.
 * As a cost a card holding N is wanted outright, as before (Berserk Rager 5_45, TokensSplitBetweenTwoMachinesCannotPay).
 *
 * The card is Armory 5_79: "Maneuver: Spot 2 [rohan] Men to remove 2 tokens from a machine or to take a [rohan]
 * possession into hand from your discard pile. Discard this condition."  Parapet 5_87 says the same of its
 * removal; its other branch can always be done in full, so it is never the board of rule 2.
 */
public class RemovingMoreTokensThanACardHoldsTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armory", "5_79");
					put("recruit", "18_103");   // Rohirrim Recruit: a [rohan] Man companion
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ally; an ability used only on purpose
					put("sword", "4_291");      // Sword of Rohan: a [rohan] possession

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
					put("ram", "5_44");         // Battering Ram: an [isengard] machine; abilities used only on purpose
					// Uruk Shield Wall: an [isengard] machine that is a possession; its trigger answers only an Uruk-hai's wound
					put("wall", "102_35");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	private VirtualTableScenario ToTheArmory(int onRam, int onWall, boolean swordInDiscard) throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetFreepsCard("armory");
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("recruit"));
		scn.MoveAlliesToTable(scn.GetFreepsCard("guma"));
		if (swordInDiscard)
			scn.MoveCardsToDiscard(scn.GetFreepsCard("sword"));

		var ram = scn.GetShadowCard("ram");
		var wall = scn.GetShadowCard("wall");
		scn.MoveCardsToSupportArea(ram, wall);
		scn.AddTokensToCard(ram, onRam);
		scn.AddTokensToCard(wall, onWall);
		assertEquals(onRam, scn.GetCultureTokensOn(ram));
		assertEquals(onWall, scn.GetCultureTokensOn(wall));

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(armory));
		scn.FreepsUseCardAction(armory);
		return scn;
	}

	@Test
	public void WithNoCardHoldingTheFullCountAnyCardHoldingATokenMayBeChosenAndGivesUpWhatItHas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = ToTheArmory(1, 1, false);
		var ram = scn.GetShadowCard("ram");
		var wall = scn.GetShadowCard("wall");

		// Neither branch can be done in full: both are offered (F-0310).
		assertTrue(scn.FreepsChoiceAvailable("Remove 2 tokens"));
		assertTrue(scn.FreepsChoiceAvailable("Take"));
		scn.FreepsChoose("Remove 2 tokens");

		assertTrue(scn.FreepsHasCardChoiceAvailable(ram, wall));
		scn.FreepsChooseCard(wall);

		assertEquals(0, scn.GetCultureTokensOn(wall));
		assertEquals(1, scn.GetCultureTokensOn(ram));
		assertInDiscard(scn.GetFreepsCard("armory"));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WhileACardHoldsTheFullCountOnlySuchACardIsChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = ToTheArmory(2, 1, false);
		var ram = scn.GetShadowCard("ram");
		var wall = scn.GetShadowCard("wall");

		// The removal can be done in full and the take cannot: the removal is carried out, and of the two machines
		// only the Ram can give up 2, so it is the one, with nothing asked.
		assertFalse(scn.FreepsChoiceAvailable("Take"));
		assertEquals(0, scn.GetCultureTokensOn(ram));
		assertEquals(1, scn.GetCultureTokensOn(wall));
		assertInDiscard(scn.GetFreepsCard("armory"));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ARemovalThatCannotBeDoneInFullIsNotChosenBesideABranchThatCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = ToTheArmory(1, 1, true);
		var ram = scn.GetShadowCard("ram");
		var wall = scn.GetShadowCard("wall");
		var sword = scn.GetFreepsCard("sword");

		// The lone [rohan] possession is taken, unasked; no token goes.
		assertFalse(scn.FreepsChoiceAvailable("Remove 2 tokens"));
		assertInHand(sword);
		assertEquals(1, scn.GetCultureTokensOn(ram));
		assertEquals(1, scn.GetCultureTokensOn(wall));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
