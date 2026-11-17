package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_079_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("walkers", "1_79");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion, the Gandalf spotted
					put("avenger", "6_27");     // Ent Avenger: [gandalf] Ent companion who is not Gandalf
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, cost 4, a maneuver ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, cost 3, a skirmish ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, cost 2, a skirmish ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, cost 2, an archery ability only
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] Elf ally, cost 2, an archery ability only
					put("smeagol", "6_45");     // Smeagol, Poor Creature: [gollum] companion with no race, cost 0; to play, add a burden
					put("underhill", "1_129");  // The Misadventure of Mr. Underhill: each companion played to a site that is no sanctuary is twilight cost +2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheNineWalkersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Nine Walkers
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot Gandalf. Plays to your support area.<br>Each time you play a companion whose race you cannot spot, that companion's twilight cost is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("walkers");

		assertEquals("The Nine Walkers", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilGandalfIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var walkers = scn.GetFreepsCard("walkers");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		// Ent Avenger is a [gandalf] companion, but he is not Gandalf.
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(walkers);

		scn.StartGame();

		assertInHand(walkers);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(walkers));

		scn.MoveCompanionsToTable(gandalf);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(walkers));
		scn.FreepsPlayCard(walkers);

		assertInZone(Zone.SUPPORT, walkers);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachCompanionWhoseRaceCannotBeSpottedIsTwilightCostMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var walkers = scn.GetFreepsCard("walkers");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		// Frodo is the Hobbit and Gandalf the Wizard: no Man and no Dwarf can be spotted.
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(walkers);
		scn.MoveCardsToHand(aragorn, gimli);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, walkers);
		scn.FreepsPlayCard(aragorn);
		// 4 printed - 2
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, aragorn);

		// Each time: the second companion played is cheaper too.
		scn.FreepsPlayCard(gimli);
		// 2 + (2 printed - 2)
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionWhoseRaceACompanionInPlayHasPaysHisFullCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var walkers = scn.GetFreepsCard("walkers");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		// Aragorn is a Man in play.
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(walkers);
		scn.MoveCardsToHand(boromir);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, walkers);
		scn.FreepsPlayCard(boromir);
		// 3 printed, with no discount
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnAllyIsNoCompanionAndHisRaceCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var walkers = scn.GetFreepsCard("walkers");
		var gandalf = scn.GetFreepsCard("gandalf");
		var orophin = scn.GetFreepsCard("orophin");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(walkers);
		scn.MoveCardsToHand(orophin, legolas);

		scn.StartGame();

		// No Elf can be spotted, but Orophin is an ally and not a companion.
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, walkers);
		scn.FreepsPlayCard(orophin);
		// 2 printed, with no discount
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, orophin);

		// An Elf can now be spotted, though only an ally is one.
		scn.FreepsPlayCard(legolas);
		// 2 + 2 printed, with no discount
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SmeagolIsNotACompanionWhoseRaceYouCannotSpot() throws DecisionResultInvalidException, CardNotFoundException {
		// CR 4.2 "Gollum/Smeagol": "Smeagol is not a companion whose race you cannot spot. When The Nine Walkers
		// is in play, Smeagol does not have his cost reduced."
		var scn = GetScenario();

		var walkers = scn.GetFreepsCard("walkers");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var underhill = scn.GetShadowCard("underhill");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(walkers, underhill);
		scn.MoveCardsToHand(aragorn, smeagol);

		scn.StartGame();

		// Smeagol costs 0: Mr. Underhill's +2 at Bag End, which is no sanctuary, lets a discount be seen.
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, walkers);
		assertInZone(Zone.SUPPORT, underhill);
		scn.FreepsPlayCard(smeagol);
		// 0 printed + 2, with no discount
		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.FREE_CHARACTERS, smeagol);

		// The Nine Walkers is at work on this board: Aragorn, a Man, is cheaper.
		scn.FreepsPlayCard(aragorn);
		// 2 + (4 printed + 2 - 2)
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
