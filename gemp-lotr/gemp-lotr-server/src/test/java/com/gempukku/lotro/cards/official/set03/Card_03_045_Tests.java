package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_045_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("resisted", "3_45");
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; a skirmish ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] companion, the Dwarf who exerts for Disquiet of Our People
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] ally; his maneuver ability needs initiative and is used only on purpose

					// Cards whose twilight cost is read in hand.
					put("disquiet", "2_2");     // Disquiet of Our People: [dwarven] Maneuver event, cost 1
					put("nomere", "2_36");      // No Mere Ranger: [gondor] Skirmish event, cost 0
					put("luthien", "1_100");    // The Choice of Luthien: [gondor] condition, cost 1, with a Maneuver ability
					put("hate", "1_250");       // Hate: [sauron] Shadow Maneuver event, cost 0

					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase; its play trigger is never set off
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SomeWhoResistedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Some Who Resisted
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Tale</b>.<br><b>Maneuver:</b> Spot a [gondor] companion to make the twilight cost of each maneuver event +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("resisted");

		assertEquals("Some Who Resisted", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAGondorCompanionMakesEachManeuverEventOfEitherSideCost2MoreUntilTheManeuverPhaseEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var resisted = scn.GetFreepsCard("resisted");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var disquiet = scn.GetFreepsCard("disquiet");
		var nomere = scn.GetFreepsCard("nomere");
		var luthien = scn.GetFreepsCard("luthien");
		var hate = scn.GetShadowCard("hate");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(boromir, gimli);
		scn.MoveCardsToHand(resisted, disquiet, nomere, luthien);
		scn.MoveCardsToHand(hate);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetTwilightCost(disquiet));
		assertEquals(0, scn.GetTwilightCost(nomere));
		assertEquals(1, scn.GetTwilightCost(luthien));
		assertEquals(0, scn.GetTwilightCost(hate));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsPlayAvailable(resisted));

		scn.FreepsPlayCard(resisted);
		assertInDiscard(resisted);

		// Each maneuver event, the Free Peoples player's of another culture and the Shadow player's alike, costs 2 more.
		assertEquals(3, scn.GetTwilightCost(disquiet));
		assertEquals(2, scn.GetTwilightCost(hate));
		// A skirmish event, and a condition with a Maneuver ability, are no maneuver events.
		assertEquals(0, scn.GetTwilightCost(nomere));
		assertEquals(1, scn.GetTwilightCost(luthien));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.ShadowPass();
		int pool = scn.GetTwilight();
		scn.FreepsPlayCard(disquiet);
		// Gimli, the only Dwarf, exerts for it.
		assertEquals(1, scn.GetWoundsOn(gimli));
		// 1 printed + 2
		assertEquals(pool + 3, scn.GetTwilight());

		// Still in force at the last decision of the maneuver phase...
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(2, scn.GetTwilightCost(hate));
		scn.ShadowPass();
		scn.FreepsPass();

		// ...and gone in the archery phase.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(0, scn.GetTwilightCost(hate));
		assertEquals(0, scn.GetTwilightCost(nomere));
		assertEquals(1, scn.GetTwilightCost(luthien));
	}

	@Test
	public void CannotBePlayedWithoutAGondorCompanionAndCanOnceTheHobbitPippinIsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var resisted = scn.GetFreepsCard("resisted");
		var gimli = scn.GetFreepsCard("gimli");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var hate = scn.GetShadowCard("hate");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(resisted);
		scn.MoveCardsToHand(hate);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// A companion of another culture and a [gondor] ally: no [gondor] companion to spot.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(resisted));

		// A [gondor] companion of another title and race.
		scn.MoveCompanionsToTable(pippin);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(resisted));

		assertEquals(0, scn.GetTwilightCost(hate));
		scn.FreepsPlayCard(resisted);
		assertInDiscard(resisted);
		assertEquals(2, scn.GetTwilightCost(hate));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
