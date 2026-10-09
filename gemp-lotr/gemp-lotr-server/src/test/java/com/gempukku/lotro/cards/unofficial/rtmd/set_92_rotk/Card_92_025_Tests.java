package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_92_025_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("runner", "1_178");     // Goblin Runner: a card for each deck, never drawn or played
					put("guard", "1_7");        // Dwarf Guard: a plain companion, so that the fellowship still has an unhindered one
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_25", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("runner", "1_178");     // Goblin Runner: a card for each deck, never drawn or played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_25"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_25
		 * Type: MetaSite
		 * Intensity: 3
		 * Game Text: Each time your fellowship moves, add (1) for each burden on the Ring-bearer.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_25", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(3, card.getBlueprint().getIntensity());
	}

	@Test
	public void AddsTwilightEqualToBurdensOnMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		scn.StartGame();

		scn.AddBurdens(4);
		assertEquals(4, scn.GetBurdens());
		assertEquals(0, scn.GetTwilight());

		// Pass fellowship phase to trigger the move to site 2
		scn.FreepsPass();

		// After the move, twilight should include site 2's shadow number + 4 burdens worth
		int twilightAfterMove = scn.GetTwilight();
		int site2Shadow = scn.GetCurrentSite().getBlueprint().getTwilightCost();

		// The move adds: site shadow number + 1 per companion moving + burden count
		// Frodo is the only companion, so +1 for him (Ettenmoors has a Shadow number of 2: 7 in all)
		assertEquals(2, site2Shadow);
		assertEquals( site2Shadow + 1 + 4, twilightAfterMove);
		assertEquals(4, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NoBurdensAddsNoExtraTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		scn.StartGame();

		// The game starts with no burden
		assertEquals(0, scn.GetBurdens());

		int twilightBeforeMove = scn.GetTwilight();

		scn.FreepsPass();

		int twilightAfterMove = scn.GetTwilight();
		int site2Shadow = scn.GetCurrentSite().getBlueprint().getTwilightCost();

		// No burdens, so only site shadow + companion twilight
		assertEquals(0, twilightBeforeMove);
		assertEquals(2, site2Shadow);
		assertEquals(twilightBeforeMove + site2Shadow + 1, twilightAfterMove);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OpponentNotAffected() throws DecisionResultInvalidException, CardNotFoundException {
		// Shadow owns the modifier; Freeps moving should not trigger extra twilight
		var scn = GetShadowScenario();

		scn.StartGame();

		scn.AddBurdens(4);
		assertEquals(4, scn.GetBurdens());
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();

		int twilightAfterMove = scn.GetTwilight();
		int site2Shadow = scn.GetCurrentSite().getBlueprint().getTwilightCost();

		// No extra twilight from burdens since Shadow owns the modifier
		assertEquals(2, site2Shadow);
		assertEquals( site2Shadow + 1, twilightAfterMove);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondMoveAddsAgainForTheBurdensThenOnTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		scn.StartGame();

		scn.AddBurdens(2);
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		// 2 for Ettenmoors + 1 for Frodo + 2 burdens
		assertEquals(5, scn.GetTwilight());

		scn.SkipToMovementDecision();
		// A third burden before the second move
		scn.AddBurdens(1);
		assertEquals(3, scn.GetBurdens());
		int twilightBeforeSecondMove = scn.GetTwilight();

		scn.FreepsChooseToMove();
		// Rivendell Valley has a Shadow number of 0: 1 for Frodo + 3 burdens
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(twilightBeforeSecondMove + 1 + 3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheBurdensOnAHinderedRingBearerAreStillCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");

		scn.MoveCompanionsToTable(guard);

		scn.StartGame();

		scn.AddBurdens(2);
		scn.FreepsPass();
		// 2 for Ettenmoors + 2 companions + 2 burdens
		assertEquals(6, scn.GetTwilight());

		// A Ring-bearer hindered during the turn is still hindered when the fellowship moves again:
		// the Free Peoples player has not reconciled. The Dwarf Guard is unhindered, so the fellowship may move
		scn.HinderCard(frodo);
		scn.SkipToMovementDecision();
		assertTrue(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(guard));
		assertEquals(2, scn.GetBurdens());
		int twilightBeforeSecondMove = scn.GetTwilight();

		scn.FreepsChooseToMove();
		// Rivendell Valley has a Shadow number of 0, and a hindered Frodo is not counted as a companion for the
		// move: 1 for the Dwarf Guard + the 2 burdens
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(twilightBeforeSecondMove + 1 + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
