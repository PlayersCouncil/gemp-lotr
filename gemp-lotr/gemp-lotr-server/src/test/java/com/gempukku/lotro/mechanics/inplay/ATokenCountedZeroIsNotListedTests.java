package com.gempukku.lotro.mechanics.inplay;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A token taken off a card to the last one is no longer listed in the client's card info (Ketura's item, engine pass
 * 59).  The game keeps a count of 0 for a kind of token once had: a companion whose one wound was healed read
 * "Tokens: WOUND: 0" in the card info, and so did a card whose culture tokens were all removed.  The count is kept,
 * since a definition reads it (RemoveCultureTokens with no culture: Grond 58_103's "remove X" with X 0, whose own
 * test reads it after its tokens are gone); the card info lists GameState.getTokensHeld, which leaves it out.
 *
 * The card info itself is built by the server's game mediator, which no table reaches; getTokensHeld, what it
 * lists, is read here directly.  The table is the Fellowship block's, with Frodo, Son of Drogo as the Ring-bearer.
 */
public class ATokenCountedZeroIsNotListedTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a maneuver ability, not used here
					put("boromir", "1_96");     // Boromir, Lord of Gondor: vitality 3; his text is about being overwhelmed
					put("gimli", "1_13");       // Gimli, Son of Glóin: vitality 3; a skirmish ability, not used here
					// A Path Appointed: "Fellowship: Exert a [gondor] Man and either a [dwarven] companion or a
					// [rohan] companion to add a [gondor] token here.  Regroup: Remove a [gondor] token here to
					// discard a minion."
					put("path", "8_42");
					put("troop", "1_177");      // Goblin Patrol Troop: no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AWoundHealedToTheLastLeavesNoWoundTokenAndOneHealedOfTwoLeavesOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.SkipToSite(3);

		// Rivendell Valley, a sanctuary: Aragorn's one wound and one of Boromir's are healed.
		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing"));
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(boromir);
		scn.FreepsDeclineSanctuaryHealing();
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(Map.of(), scn.gameState().getTokensHeld(aragorn)); // lint-ok: the card info's token list, which no table reaches
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(Map.of(Token.WOUND, 1), scn.gameState().getTokensHeld(boromir)); // lint-ok: as above
	}

	@Test
	public void ACultureTokenRemovedToTheLastLeavesNoTokenOfThatCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var path = scn.GetFreepsCard("path");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(path);
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();

		// "Add a [gondor] token here": Aragorn and Gimli exert.
		scn.FreepsUseCardAction(path);
		assertEquals(Map.of(Token.GONDOR, 1), scn.gameState().getTokensHeld(path)); // lint-ok: as above
		assertEquals(Map.of(Token.WOUND, 1), scn.gameState().getTokensHeld(gimli)); // lint-ok: as above

		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.REGROUP);

		// "Remove a [gondor] token here to discard a minion": the one token goes, and is listed no more.
		scn.FreepsUseCardAction(path);
		assertInZone(Zone.DISCARD, troop);
		assertEquals(0, scn.GetCultureTokensOn(path));
		assertEquals(Map.of(), scn.gameState().getTokensHeld(path)); // lint-ok: as above
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
