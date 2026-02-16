package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_082_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("death", "103_82");
					put("rider", "4_286");      // Rider of Rohan - Rohan companion

					put("hollowing", "3_54");   // Isengard condition, cost 4
					put("swarms1", "1_183");    // Moria condition, cost 1, stackable
					put("swarms2", "1_183");    // Second copy
					put("ships", "8_65");       // Raider possession, cost 2, culture tokens
					put("stone", "9_47");       // Sauron artifact, cost 0, unique
					put("runner", "1_178");     // Goblin Runner, for stacking
					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DeathTakeUsAllStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Death Take Us All
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: The Shadow player may hinder any of their own support cards.
		* 	Spot a Shadow support card.  Make a [rohan] companion strength +1 for each of the following: that card's twilight cost, cards stacked on that card, culture tokens on that card, copies of that card in play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("death");

		assertEquals("Death Take Us All", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}



// ======== BASIC STRENGTH PUMP ========

	@Test
	public void DeathTakeUsAllPumpsByTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var death = scn.GetFreepsCard("death");
		var rider = scn.GetFreepsCard("rider");
		var hollowing = scn.GetShadowCard("hollowing");  // Cost 4
		var savage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(rider);
		scn.MoveCardsToHand(death);
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(hollowing);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(rider, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(rider);

		int riderStrength = scn.GetStrength(rider);

		scn.FreepsPlayCard(death);

		// Shadow may hinder - decline
		scn.ShadowDeclineChoosing();

		// Choose Shadow support card - Hollowing auto-selected
		// Choose Rohan companion - Rider auto-selected

		// Pump = twilight cost 4 + 0 stacked + 0 tokens + 1 copy = 5
		assertEquals(riderStrength + 5, scn.GetStrength(rider));
	}

	@Test
	public void DeathTakeUsAllCountsStackedCards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var death = scn.GetFreepsCard("death");
		var rider = scn.GetFreepsCard("rider");
		var swarms1 = scn.GetShadowCard("swarms1");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(rider);
		scn.MoveCardsToHand(death);
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(swarms1);
		scn.StackCardsOn(swarms1, runner);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(rider, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(rider);

		int riderStrength = scn.GetStrength(rider);

		scn.FreepsPlayCard(death);
		scn.ShadowDeclineChoosing();

		// Pump = twilight cost 1 + 1 stacked + 0 tokens + 1 copy = 3
		assertEquals(riderStrength + 3, scn.GetStrength(rider));
	}

	@Test
	public void DeathTakeUsAllCountsCultureTokens() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var death = scn.GetFreepsCard("death");
		var rider = scn.GetFreepsCard("rider");
		var ships = scn.GetShadowCard("ships");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(rider);
		scn.MoveCardsToHand(death);
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(ships);
		scn.AddTokensToCard(ships, 3);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(rider, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(rider);

		int riderStrength = scn.GetStrength(rider);

		scn.FreepsPlayCard(death);
		scn.ShadowDeclineChoosing();

		// Pump = twilight cost 2 + 0 stacked + 3 tokens + 1 copy = 6
		assertEquals(riderStrength + 6, scn.GetStrength(rider));
	}

	@Test
	public void DeathTakeUsAllCountsCopiesInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var death = scn.GetFreepsCard("death");
		var rider = scn.GetFreepsCard("rider");
		var swarms1 = scn.GetShadowCard("swarms1");
		var swarms2 = scn.GetShadowCard("swarms2");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(rider);
		scn.MoveCardsToHand(death);
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(swarms1, swarms2);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(rider, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(rider);

		int riderStrength = scn.GetStrength(rider);

		scn.FreepsPlayCard(death);
		scn.ShadowDeclineChoosing();

		// Choose which Goblin Swarms to reference
		scn.FreepsChooseCard(swarms1);

		// Pump = twilight cost 1 + 0 stacked + 0 tokens + 2 copies = 3
		assertEquals(riderStrength + 3, scn.GetStrength(rider));
	}

	@Test
	public void DeathTakeUsAllCombinesAllFactors() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var death = scn.GetFreepsCard("death");
		var rider = scn.GetFreepsCard("rider");
		var swarms1 = scn.GetShadowCard("swarms1");
		var swarms2 = scn.GetShadowCard("swarms2");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(rider);
		scn.MoveCardsToHand(death);
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(swarms1, swarms2);
		scn.StackCardsOn(swarms1, runner);
		scn.AddTokensToCard(swarms1, 2);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(rider, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(rider);

		int riderStrength = scn.GetStrength(rider);

		scn.FreepsPlayCard(death);
		scn.ShadowDeclineChoosing();
		scn.FreepsChooseCard(swarms1);

		// Pump = twilight cost 1 + 1 stacked + 2 tokens + 2 copies = 6
		assertEquals(riderStrength + 6, scn.GetStrength(rider));
	}

// ======== SHADOW HINDER PROTECTION ========

	@Test
	public void ShadowCanHinderToProtectSupportCards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var death = scn.GetFreepsCard("death");
		var rider = scn.GetFreepsCard("rider");
		var hollowing = scn.GetShadowCard("hollowing");  // Cost 4
		var stone = scn.GetShadowCard("stone");          // Cost 0
		var savage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(rider);
		scn.MoveCardsToHand(death);
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(hollowing, stone);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(rider, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(rider);

		int riderStrength = scn.GetStrength(rider);

		scn.FreepsPlayCard(death);

		// Shadow hinders the high-cost Hollowing to protect it
		scn.ShadowChooseCard(hollowing);

		assertTrue(scn.IsHindered(hollowing));

		// Only Stone remains as valid target - auto-selected
		// Pump = twilight cost 0 + 0 stacked + 0 tokens + 1 copy = 1
		assertEquals(riderStrength + 1, scn.GetStrength(rider));
	}

	@Test
	public void DeathTakeUsAllWorksWithArtifacts() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var death = scn.GetFreepsCard("death");
		var rider = scn.GetFreepsCard("rider");
		var stone = scn.GetShadowCard("stone");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(rider);
		scn.MoveCardsToHand(death);
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(stone);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(rider, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(rider);

		int riderStrength = scn.GetStrength(rider);

		scn.FreepsPlayCard(death);
		scn.ShadowDeclineChoosing();

		// Pump = twilight cost 0 + 0 stacked + 0 tokens + 1 copy = 1
		assertEquals(riderStrength + 1, scn.GetStrength(rider));
	}
}
