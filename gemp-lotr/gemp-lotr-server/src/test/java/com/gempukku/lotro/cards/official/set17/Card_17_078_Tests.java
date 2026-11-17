package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_078_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("footman", "17_78");
					put("footman2", "17_78");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, strength 13, no text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability only ever used on purpose
					put("boromir", "1_97");     // Boromir, Lord of Gondor: companion of cost 3, played to add to the pool
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishFootmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Orkish Footman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: This minion is strength +1 for each twilight token you can spot.
		*/

		var scn = GetScenario();

		var footman = scn.GetFreepsCard("footman");

		assertEquals("Orkish Footman", footman.getBlueprint().getTitle());
		assertNull(footman.getBlueprint().getSubtitle());
		assertFalse(footman.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, footman.getBlueprint().getSide());
		assertEquals(Culture.ORC, footman.getBlueprint().getCulture());
		assertEquals(CardType.MINION, footman.getBlueprint().getCardType());
		assertEquals(Race.ORC, footman.getBlueprint().getRace());
		assertEquals(3, footman.getBlueprint().getTwilightCost());
		assertEquals(7, footman.getBlueprint().getStrength());
		assertEquals(2, footman.getBlueprint().getVitality());
		assertEquals(4, footman.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachFootmanIsStrengthPlus1ForEachTwilightTokenAndNoOtherMinionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var footman = scn.GetShadowCard("footman");
		var footman2 = scn.GetShadowCard("footman2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// The minions are laid in the fellowship phase to be read as the pool changes (Q-0228).
		scn.MoveMinionsToTable(footman, footman2, troop);
		// Tokens that are not twilight: a burden and a wound.
		scn.AddBurdens(1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(footman));
		assertEquals(7, scn.GetStrength(footman2));
		assertEquals(13, scn.GetStrength(troop));

		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());
		// 7 + 3 each: a copy is not raised by the other copy's text.
		assertEquals(10, scn.GetStrength(footman));
		assertEquals(10, scn.GetStrength(footman2));
		assertEquals(13, scn.GetStrength(troop));

		scn.SetTwilight(8);
		assertEquals(8, scn.GetTwilight());
		assertEquals(15, scn.GetStrength(footman));
		assertEquals(15, scn.GetStrength(footman2));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionsCostAddedToThePoolRaisesTheFootman() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToHand(boromir);

		var footman = scn.GetShadowCard("footman");

		scn.StartGame();
		scn.MoveMinionsToTable(footman);
		scn.SetTwilight(2);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetTwilight());
		assertEquals(9, scn.GetStrength(footman));

		scn.FreepsPlayCard(boromir);

		// 2 + Boromir's 3.
		assertEquals(5, scn.GetTwilight());
		assertEquals(12, scn.GetStrength(footman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
