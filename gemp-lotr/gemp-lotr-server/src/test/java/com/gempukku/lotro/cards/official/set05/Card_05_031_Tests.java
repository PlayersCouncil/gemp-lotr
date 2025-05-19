package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_05_031_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("alcarin", "5_31");
					put("secondlevel", "7_118");

					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AlcarinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Alcarin, Warrior of Lamedon
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Knight</b>. The twilight cost of each other knight in your starting fellowship is -1.<br><b>Assignment:</b> Assign Alcarin to a minion bearing a [gondor] fortification to heal Alcarin.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("alcarin");

		assertEquals("Alcarin", card.getBlueprint().getTitle());
		assertEquals("Warrior of Lamedon", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.KNIGHT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void AlcarinCanSelfAssignToAFortifiedMinion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var alcarin = scn.GetFreepsCard("alcarin");
		var secondlevel = scn.GetFreepsCard("secondlevel");
		scn.MoveCompanionToTable(alcarin);
		scn.MoveCardsToSupportArea(secondlevel);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(secondlevel);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(1, scn.GetWoundsOn(alcarin));
		assertEquals(Zone.ATTACHED, secondlevel.getZone());
		assertEquals(runner, secondlevel.getAttachedTo());
		assertTrue(scn.FreepsActionAvailable(alcarin));
		assertFalse(scn.IsCharAssigned(alcarin));
		assertFalse(scn.IsCharAssigned(runner));

		scn.FreepsUseCardAction(alcarin);
		assertTrue(scn.IsCharAssigned(alcarin));
		assertTrue(scn.IsCharAssigned(runner));
		assertEquals(0, scn.GetWoundsOn(alcarin));
	}
}
