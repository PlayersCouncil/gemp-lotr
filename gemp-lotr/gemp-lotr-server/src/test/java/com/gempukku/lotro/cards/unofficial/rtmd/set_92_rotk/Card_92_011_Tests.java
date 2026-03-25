package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_92_011_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Goblin Runner (1_178): Moria Orc minion, twilight 0, strength 5, vitality 1
		put("runner", "1_178");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_11", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_11"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_11
		 * Type: MetaSite
		 * Game Text: If the fellowship has moved more than once this turn, your minions are each strength +1.
		 */

		var scn = GetShadowScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_11", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void MinionNotBuffedAfterSingleMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var runner = scn.GetShadowCard("runner");

		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		// After a single move, no buff
		assertEquals(5, scn.GetStrength(runner));
	}

	@Test
	public void MinionBuffedAfterDoubleMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		//Put cards in the discard so reconciliation isn't offered
		scn.MoveCardsToFreepsDiscard("runner");
		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();

		// Double move
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// After double move, runner should be strength 6
		assertEquals(6, scn.GetStrength(runner));
	}

	@Test
	public void DoesNotApplyWhenOwnedByFreeps() throws DecisionResultInvalidException, CardNotFoundException {
		// When freeps owns the meta-site, "your minions" refers to freeps minions,
		// which don't exist. Shadow's minions should NOT be buffed.
		var scn = GetFreepsScenario();

		//Put cards in the discard so reconciliation isn't offered
		scn.MoveCardsToFreepsDiscard("runner");
		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();

		// Double move
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// Should NOT be buffed — freeps owns the modifier
		assertEquals(5, scn.GetStrength(runner));
	}
}
