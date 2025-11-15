package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_056_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trap", "103_55");
					put("isengard_tracker", "4_193");
					put("raider_tracker", "103_45");
					put("ambush_southron", "4_252");
					put("ambush_horror", "14_13");
					put("southron", "4_253");
					put("soldier", "1_271");
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SandcraftAmbushStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Sandcraft Ambush
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Assignment
		 * Game Text: Restore all trackers and minions with ambush.  If you restored any, spot a Southron and make the Free Peoples player assign it to an unbound companion with the lowest strength.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trap");

		assertEquals("Sandcraft Ambush", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ASSIGNMENT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SandcraftTrapRestoresTrackersAndNativeAmbushMinions() throws DecisionResultInvalidException, CardNotFoundException {

		//Pre-game setup
		var scn = GetScenario();

		var trap = scn.GetShadowCard("trap");
		var isengard_tracker = scn.GetShadowCard("isengard_tracker");
		var raider_tracker = scn.GetShadowCard("raider_tracker");
		var ambush_southron = scn.GetShadowCard("ambush_southron");
		var ambush_horror = scn.GetShadowCard("ambush_horror");
		var southron = scn.GetShadowCard("southron");
		var soldier = scn.GetShadowCard("soldier");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToHand(trap);
		scn.MoveMinionsToTable(isengard_tracker, raider_tracker, ambush_southron, ambush_horror, southron, soldier, runner);
		scn.HinderCard(isengard_tracker, raider_tracker, ambush_southron, ambush_horror, southron, soldier);

		scn.StartGame();
		
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(isengard_tracker));
		assertTrue(scn.IsHindered(raider_tracker));
		assertTrue(scn.IsHindered(ambush_southron));
		assertTrue(scn.IsHindered(ambush_southron));
		assertTrue(scn.IsHindered(ambush_horror));
		assertTrue(scn.IsHindered(soldier));
		assertFalse(scn.IsHindered(runner));

		assertTrue(scn.ShadowPlayAvailable(trap));
		scn.ShadowPlayCard(trap);

		//Trackers of all cultures restored
		assertFalse(scn.IsHindered(isengard_tracker));
		assertFalse(scn.IsHindered(raider_tracker));
		//Minions of all cultures with ambush restored
		assertFalse(scn.IsHindered(ambush_southron));
		assertFalse(scn.IsHindered(ambush_horror));

		//Non-ambush southron still hindered
		assertTrue(scn.IsHindered(southron));
		//Non-tracker non-ambush minion still hindered
		assertTrue(scn.IsHindered(soldier));
	}
}
