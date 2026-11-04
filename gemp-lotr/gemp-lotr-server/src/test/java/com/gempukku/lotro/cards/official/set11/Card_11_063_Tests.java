package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_063_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rest", "11_63");

					// [gondor] companions of two races at resistance 5 once a burden is on, and the companions who fail one
					// word of "[gondor] companion who has resistance 5 or more".
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man, resistance 6, vitality 3; "Knight." is his whole text
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit, resistance 6; a skirmish ability used only on purpose
					put("whitetree", "13_67");  // Guard of the White Tree: [gondor] Man, resistance 5; a skirmish ability used only on purpose
					put("guard", "5_83");       // Household Guard: [rohan] Man, resistance 6; "Valiant." is his whole text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MuchneededRestStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Much-needed Rest
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Heal each [gondor] companion who has resistance 5 or more.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rest");

		assertEquals("Much-needed Rest", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void HealsOneWoundFromEachGondorCompanionOfResistance5OrMoreAndNoOtherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var rest = scn.GetFreepsCard("rest");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var whitetree = scn.GetFreepsCard("whitetree");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(knight, pippin, whitetree, guard);
		scn.MoveCardsToHand(rest);

		scn.StartGame();
		// One burden takes 1 from every companion's resistance (CR 4.2 "resistance").
		scn.AddBurdens(1);
		scn.AddWoundsToChar(knight, 2);
		scn.AddWoundsToChar(pippin, 1);
		scn.AddWoundsToChar(whitetree, 1);
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.FreepsRefreshDecision();
		assertEquals(5, scn.GetResistance(knight));
		assertEquals(5, scn.GetResistance(pippin));
		assertEquals(4, scn.GetResistance(whitetree));
		assertEquals(5, scn.GetResistance(guard));
		assertEquals(9, scn.GetResistance(frodo));
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(whitetree));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInHand(rest);

		assertTrue(scn.FreepsPlayAvailable(rest));
		scn.FreepsPlayCard(rest);

		// The [gondor] Man and the [gondor] Hobbit at exactly 5 are healed, one wound each.  Not the [gondor] Man at 4,
		// the [rohan] Man at 5 or Frodo at 9.
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(whitetree));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInDiscard(rest);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoWoundedGondorCompanionOfResistance5OrMoreItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rest = scn.GetFreepsCard("rest");
		var knight = scn.GetFreepsCard("knight");
		var whitetree = scn.GetFreepsCard("whitetree");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(knight, whitetree, guard);
		scn.MoveCardsToHand(rest);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.AddWoundsToChar(whitetree, 1);
		scn.AddWoundsToChar(guard, 1);
		scn.FreepsRefreshDecision();
		assertEquals(4, scn.GetResistance(whitetree));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(whitetree));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInHand(rest);

		assertTrue(scn.FreepsPlayAvailable(rest));
		scn.FreepsPlayCard(rest);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(whitetree));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInDiscard(rest);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rest = scn.GetFreepsCard("rest");
		var knight = scn.GetFreepsCard("knight");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(knight);
		scn.AddWoundsToChar(knight, 1);
		scn.MoveCardsToHand(rest);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertInHand(rest);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// A [gondor] companion of resistance 6 with a wound to heal, but this is no fellowship phase.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(rest));
		assertEquals(1, scn.GetWoundsOn(knight));
	}
}
