package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_121_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("faramir", "6_121");
					// Rangers to play from hand, and a companion who is no ranger
					put("ranger", "7_116");     // Ranger of Osgiliath: [gondor] Man ranger, not unique; a Regroup ability only
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf ranger; a bonus against Nazgûl only
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, no ranger, no text
					// Companions to heal
					put("boromir", "0_65");     // Boromir, Steward's Heir: [gondor] Man, Ring-bound in print, keywords only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man; a Maneuver ability only
					put("aragorn2", "1_89");    // a second copy, which cannot be played while the first is in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetStartingFellowshipScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("faramir", "6_121");
					// Three companions whose texts touch no card's cost
					put("gimli", "1_13");       // Gimli, Son of Glóin: cost 2
					put("pippin", "1_306");     // Pippin, Friend to Frodo: cost 1
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man who costs 3, as Faramir is printed
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FaramirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Faramir, Ithilien Ranger
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: <b>Ring-bound</b>. <b>Ranger</b>.<br>If Faramir is in your starting fellowship, his twilight cost is -1.<br><b>Fellowship:</b> Play a ranger to heal a Ring-bound companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("faramir");

		assertEquals("Faramir", card.getBlueprint().getTitle());
		assertEquals("Ithilien Ranger", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void InTheStartingFellowshipFaramirCostsOneLessAndSoFitsBesideACompanionCostingTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStartingFellowshipScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var gimli = scn.GetFreepsCard("gimli");
		var pippin = scn.GetFreepsCard("pippin");
		var boromir = scn.GetFreepsCard("boromir");

		// The table is made with the first player's starting fellowship still to choose, from his draw deck.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertInZone(Zone.DECK, faramir, gimli, pippin, boromir);
		assertTrue(scn.FreepsHasCardChoiceAvailable(faramir, gimli, pippin, boromir));
		// Printed 3; what he would cost to play now is 3 - 1.
		assertEquals(2, scn.GetTwilightCost(faramir));
		// While a player chooses, the game keeps the total of what he has played in the twilight pool.
		assertEquals(0, scn.GetTwilight());

		scn.FreepsChooseCard(gimli);

		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(2, scn.GetTwilight());
		// A starting fellowship may cost 4.  2 + 3 would be 5, and Boromir is no longer offered; 2 + (3 - 1) is
		// the 4, and Faramir still is.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(faramir, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir));

		scn.FreepsChooseCard(faramir);

		assertInZone(Zone.FREE_CHARACTERS, faramir);
		// The total stands at 4 and not at 3: Pippin's 1 no longer fits, nobody is left to offer and the choice
		// ends.  The other player is choosing now.
		assertInZone(Zone.DECK, pippin, boromir);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
	}

	@Test
	public void PlayedFromHandOutsideTheStartingFellowshipFaramirCostsThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCardsToHand(faramir);

		scn.StartGame();

		assertInHand(faramir);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(faramir));

		scn.FreepsPlayCard(faramir);
		// Outside the starting fellowship there is no discount: the printed 3.
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, faramir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionPlaysARangerFromHandToHealARingBoundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var faramir = scn.GetFreepsCard("faramir");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ranger = scn.GetFreepsCard("ranger");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(faramir, boromir, aragorn);
		scn.MoveCardsToHand(ranger, arwen, knight);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(boromir, 2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(faramir, 1);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertInHand(ranger, arwen, knight);
		assertTrue(scn.FreepsPlayAvailable(knight));
		assertTrue(scn.FreepsActionAvailable(faramir));

		scn.FreepsUseCardAction(faramir);
		// Rangers of two cultures and races; not the Knight, a companion who is no ranger and could be played.
		assertTrue(scn.FreepsHasCardChoiceAvailable(ranger, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(ranger);

		assertInZone(Zone.FREE_CHARACTERS, ranger);
		assertInHand(arwen, knight);

		// Ring-bound companions: Frodo (a [shire] Hobbit, the Ring-bearer), Boromir (a [gondor] Man) and Faramir
		// himself; not Aragorn, who is unbound.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, boromir, faramir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(boromir);

		// One wound healed of two, and no other.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoRingBoundCompanionWoundedTheRangerIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var faramir = scn.GetFreepsCard("faramir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(faramir, aragorn);
		scn.MoveCardsToHand(ranger);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 2);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertInHand(ranger);
		assertTrue(scn.FreepsActionAvailable(faramir));

		scn.FreepsUseCardAction(faramir);
		// The ranger is the only one in hand and is played unasked; the only wounded companion is unbound.
		assertInZone(Zone.FREE_CHARACTERS, ranger);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheOnlyRangerInHandCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var faramir = scn.GetFreepsCard("faramir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var aragorn2 = scn.GetFreepsCard("aragorn2");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(faramir, aragorn);
		scn.MoveCardsToHand(aragorn2, knight);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInHand(aragorn2, knight);

		// Aragorn is unique and in play, so the second Aragorn cannot be played; the Knight can, and is no ranger.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(aragorn2));
		assertTrue(scn.FreepsPlayAvailable(knight));
		assertFalse(scn.FreepsActionAvailable(faramir));
		assertEquals(1, scn.GetWoundsOn(frodo));
	}
}
