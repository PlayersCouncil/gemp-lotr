package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_243_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("despair", "1_243");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability only
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc; his text answers a weapon played on him
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] minion who is no Orc (Uruk-hai), damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: an Orc who is not [sauron] ([moria]), no text

					// Dwarf Guard: a companion who is not unique (only "To play, spot a Dwarf"), for the dead pile
					put("dwarf1", "1_7");
					put("dwarf2", "1_7");
					put("dwarf3", "1_7");
					put("dwarf4", "1_7");
					put("dwarf5", "1_7");
					put("dwarf6", "1_7");
					put("grimir", "1_17");      // Grimir, Dwarven Elder: an ally, a character in the dead pile who is no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DespairStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Despair
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> If the total number of burdens and companions in the dead pile is at least 12, spot a [sauron] Orc to corrupt the Ring-bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("despair");

		assertEquals("Despair", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithNineBurdensAndThreeCompanionsInTheDeadPileItCorruptsTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var despair = scn.GetShadowCard("despair");
		var soldier = scn.GetShadowCard("soldier");
		var dwarf1 = scn.GetFreepsCard("dwarf1");
		var dwarf2 = scn.GetFreepsCard("dwarf2");
		var dwarf3 = scn.GetFreepsCard("dwarf3");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToDeadPile(dwarf1, dwarf2, dwarf3);
		scn.MoveCardsToHand(despair);
		assertInZone(Zone.DEAD, dwarf1, dwarf2, dwarf3);

		scn.StartGame();
		// Resistance 10: nine burdens leave Frodo at 1.
		scn.AddBurdens(9);
		assertEquals(9, scn.FreepsGetBurdens());
		scn.MoveMinionsToTable(soldier);
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		// 9 burdens + 3 companions in the dead pile = 12.
		assertEquals(9, scn.FreepsGetBurdens());
		assertEquals(3, scn.GetFreepsDeadCount());
		assertEquals(1, scn.GetResistance(frodo));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(despair));

		scn.MarkGameLog();
		scn.ShadowPlayCard(despair);

		assertTrue(scn.GameLogSinceMarkSays(scn.P1() + " lost due to: The Ring-Bearer is corrupted"));
		assertTrue(scn.FreepsHasLost());
		assertTrue(scn.GameIsFinished());
	}

	@Test
	public void ElevenIsNotEnoughWhateverElseIsInADeadPileUntilATwelfthCompanionDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var despair = scn.GetShadowCard("despair");
		var soldier = scn.GetShadowCard("soldier");
		var dwarf1 = scn.GetFreepsCard("dwarf1");
		var dwarf2 = scn.GetFreepsCard("dwarf2");
		var dwarf3 = scn.GetFreepsCard("dwarf3");
		var grimir = scn.GetFreepsCard("grimir");
		var shadowDwarf = scn.GetShadowCard("dwarf4");
		var frodo = scn.GetRingBearer();
		// Beside 2 companions in his dead pile: an ally there, and a companion in the Shadow player's own dead pile.
		scn.MoveCardsToDeadPile(dwarf1, dwarf2, grimir, shadowDwarf);
		scn.MoveCardsToHand(despair);
		assertInZone(Zone.DEAD, dwarf1, dwarf2, grimir, shadowDwarf);

		scn.StartGame();
		scn.AddBurdens(9);
		// A wound on the Ring-bearer is no burden.
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(9, scn.FreepsGetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.MoveMinionsToTable(soldier);
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		// 9 burdens + 2 companions in his dead pile = 11.
		assertEquals(9, scn.FreepsGetBurdens());
		assertEquals(3, scn.GetFreepsDeadCount());
		assertInZone(Zone.DEAD, shadowDwarf);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(despair));

		// A third companion in his dead pile makes 12.
		scn.MoveCardsToDeadPile(dwarf3);
		scn.ShadowRefreshDecision();
		assertEquals(4, scn.GetFreepsDeadCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(despair));
	}

	@Test
	public void NotPlayableWithoutASauronOrcToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var despair = scn.GetShadowCard("despair");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var dwarf1 = scn.GetFreepsCard("dwarf1");
		var dwarf2 = scn.GetFreepsCard("dwarf2");
		var dwarf3 = scn.GetFreepsCard("dwarf3");
		var dwarf4 = scn.GetFreepsCard("dwarf4");
		var dwarf5 = scn.GetFreepsCard("dwarf5");
		var dwarf6 = scn.GetFreepsCard("dwarf6");
		scn.MoveCardsToDeadPile(dwarf1, dwarf2, dwarf3, dwarf4, dwarf5, dwarf6);
		scn.MoveCardsToHand(despair);
		assertInZone(Zone.DEAD, dwarf1, dwarf2, dwarf3, dwarf4, dwarf5, dwarf6);

		scn.StartGame();
		scn.AddBurdens(6);
		assertEquals(6, scn.FreepsGetBurdens());
		scn.MoveMinionsToTable(guard, troop);
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		// 6 burdens + 6 companions in the dead pile = 12, but a [sauron] Uruk-hai and a [moria] Orc are no [sauron] Orc.
		assertEquals(6, scn.FreepsGetBurdens());
		assertEquals(6, scn.GetFreepsDeadCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(despair));

		scn.MoveMinionsToTable(chieftain);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(despair));

		scn.MarkGameLog();
		scn.ShadowPlayCard(despair);
		assertTrue(scn.GameLogSinceMarkSays(scn.P1() + " lost due to: The Ring-Bearer is corrupted"));
		assertTrue(scn.FreepsHasLost());
	}
}
