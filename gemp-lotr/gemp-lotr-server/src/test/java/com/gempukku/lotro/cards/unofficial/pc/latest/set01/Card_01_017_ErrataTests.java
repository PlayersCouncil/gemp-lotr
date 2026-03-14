package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_017_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grimir", "51_17");
					put("guard", "1_7");
					put("skirmishEvent", "1_3"); // Dwarf Axe, Dwarven Skirmish event
					put("fellowshipEvent", "1_6"); // Delving, Dwarven Fellowship event
					put("runner", "1_178"); // Goblin Runner
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrimirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Grimir, Dwarven Elder
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Dwarf
		 * Strength: 3
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: Skirmish: Spot a [dwarven] companion and exert Grimir twice to play a [dwarven] skirmish event from your discard pile, then remove it from the game.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grimir");

		assertEquals("Grimir", card.getBlueprint().getTitle());
		assertEquals("Dwarven Elder", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void GrimirAbilityPlaysOnlyDwarvenSkirmishEventsFromDiscardAndRemovesThem() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");
		var guard = scn.GetFreepsCard("guard");
		var skirmishEvent = scn.GetFreepsCard("skirmishEvent");
		var fellowshipEvent = scn.GetFreepsCard("fellowshipEvent");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(grimir);
		// Both a skirmish event and a fellowship event in discard
		scn.MoveCardsToDiscard(skirmishEvent, fellowshipEvent);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.SKIRMISH);
		scn.FreepsAssignAndResolve(guard, runner);

		// Grimir's ability should be available during skirmish
		assertTrue(scn.FreepsActionAvailable(grimir));
		assertEquals(0, scn.GetWoundsOn(grimir));

		scn.FreepsUseCardAction(grimir);

		// Only the skirmish event is a valid target, so it is auto-selected
		// (fellowship event is ignored). Grimir should have 2 wounds (exerted twice).
		assertEquals(2, scn.GetWoundsOn(grimir));
		// The skirmish event should have been removed from the game
		assertEquals(Zone.REMOVED, skirmishEvent.getZone());
		// The fellowship event should still be in discard (untouched)
		assertEquals(Zone.DISCARD, fellowshipEvent.getZone());
	}

	@Test
	public void GrimirAbilityRequiresDwarvenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");
		var skirmishEvent = scn.GetFreepsCard("skirmishEvent");
		var runner = scn.GetShadowCard("runner");

		// No Dwarven companion on the table -- only Frodo (Hobbit)
		scn.MoveCardsToSupportArea(grimir);
		scn.MoveCardsToDiscard(skirmishEvent);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.SKIRMISH);
		// Frodo gets assigned to runner
		scn.FreepsAssignAndResolve(scn.GetRingBearer(), runner);

		// Grimir's ability should NOT be available without a Dwarven companion
		assertFalse(scn.FreepsActionAvailable(grimir));
	}

	@Test
	public void GrimirAbilityRequiresExertingTwiceSoExhaustedGrimirCannotUse() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");
		var guard = scn.GetFreepsCard("guard");
		var skirmishEvent = scn.GetFreepsCard("skirmishEvent");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(grimir);
		scn.MoveCardsToDiscard(skirmishEvent);
		scn.MoveMinionsToTable(runner);

		// Wound Grimir twice so he's exhausted (1 vitality remaining)
		scn.AddWoundsToChar(grimir, 2);

		scn.StartGame();
		scn.SkipToPhase(Phase.SKIRMISH);
		scn.FreepsAssignAndResolve(guard, runner);

		// Grimir is exhausted -- cannot exert twice
		assertFalse(scn.FreepsActionAvailable(grimir));
	}

}
