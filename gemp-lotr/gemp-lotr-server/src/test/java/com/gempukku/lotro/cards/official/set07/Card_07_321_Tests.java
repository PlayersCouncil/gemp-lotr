package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_321_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_321");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North
					put("rider", "4_286");        // Rider of Rohan: [rohan] Man
					put("spear", "4_288");        // Rider's Spear: [rohan] possession, bearer must be a [rohan] Man
					put("armor", "1_92");         // Armor: [gondor] possession, bearer must be a Man
					put("troop", "1_177");        // Goblin Patrol Troop
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Merry, Swordthain
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Theoden
		 * Game Text: <b>Skirmish:</b> If Merry is not assigned to a skirmish, return him to your hand to play up to 2 [rohan] possessions from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Merry", card.getBlueprint().getTitle());
		assertEquals("Swordthain", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.THEODEN, card.getBlueprint().getSignet()); 
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void MerryTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}

	@Test
	public void SkirmishActionIsNotOfferedUnlessARohanPossessionInTheDiscardPileCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rider = scn.GetFreepsCard("rider");
		var spear = scn.GetFreepsCard("spear");
		var armor = scn.GetFreepsCard("armor");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(card, aragorn, rider);
		//A possession that could be played, but is not a [rohan] one
		scn.MoveCardsToDiscard(armor);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		//Playing from the discard pile is treated as a cost: with nothing to play, the ability is withheld
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(card));

		//The Spear can go on the Rider
		scn.MoveCardsToDiscard(spear);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsActionAvailable(card));
	}
}
