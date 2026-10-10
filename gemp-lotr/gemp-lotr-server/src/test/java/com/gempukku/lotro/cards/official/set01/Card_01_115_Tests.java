package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_115_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_115");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetCancelScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("kings", "1_115");
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: the [gondor] Man, strength 8, no archer

					put("runner", "1_178");         // Goblin Runner: [moria] Orc, strength 5
					put("territory1", "1_201");     // Unfamiliar Territory: [moria] Skirmish event, cost 0, "Make a [moria] Orc strength +2 (or +4 if skirmishing an archer)"
					put("territory2", "1_201");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrengthofKingsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Strength of Kings
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: 
		 * Game Text: <b>Response:</b> If a skirmish event is played during a skirmish involving a [gondor] Man, cancel that event.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Strength of Kings", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CancelsASkirmishEventPlayedInAGondorMansSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCancelScenario();

		var kings = scn.GetFreepsCard("kings");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var territory1 = scn.GetShadowCard("territory1");
		var territory2 = scn.GetShadowCard("territory2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(kings);
		scn.MoveCardsToHand(territory1, territory2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		scn.FreepsPass();
		assertEquals(5, scn.GetStrength(runner));
		scn.ShadowPlayCard(territory1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(kings));

		scn.FreepsChooseOptionalTrigger(kings);
		// Cancelled: the Runner is still 5.  Aragorn is no archer, so the event would have made it 7.
		assertEquals(5, scn.GetStrength(runner));
		// Both events are spent (CR 4.2 "cancel": a cancelled event is discarded), and the skirmish goes on.
		assertInZone(Zone.DISCARD, territory1, kings);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The same event again, with no Strength of Kings left to answer it, does what it says.
		scn.FreepsPass();
		scn.ShadowPlayCard(territory2);
		assertEquals(7, scn.GetStrength(runner));
		assertInZone(Zone.DISCARD, territory2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
