package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_060_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "103_60");
					put("southron1", "4_222");   // Desert Warrior - Raider minion for spotting
					put("event1", "103_59");     // Strength of Men - Shadow Raider event, cost 2
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheMouthofSauronStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: The Mouth of Sauron, Herald of the Dark Lord
		 * Unique: true
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: Lurker.
		* 	Your Shadow events are twilight cost -2. Response: If a minion is about to take a wound, exert this minion to prevent that wound and hinder a Free Peoples weapon.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("The Mouth of Sauron", card.getBlueprint().getTitle());
		assertEquals("Herald of the Dark Lord", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TheMouthofSauronReducesShadowEventCostBy2() throws DecisionResultInvalidException, CardNotFoundException {
		// Errata: ModifyCost is -2 (was -1 in original)
		// Mouth of Sauron is a lurker, so he sits in the support area
		var scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var southron1 = scn.GetShadowCard("southron1");
		var event1 = scn.GetShadowCard("event1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(card);
		scn.MoveMinionsToTable(southron1);
		scn.MoveCardsToHand(event1);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		int twilightBefore = scn.GetTwilight();

		// Strength of Men normally costs 2, with -2 from Mouth of Sauron = 0
		// Mouth (support area) + southron1 (in play) = 2 [raider] cards spotted
		scn.ShadowPlayCard(event1);

		// Event cost 2 - 2 (Mouth's modifier) = 0 twilight spent
		assertEquals(twilightBefore, scn.GetTwilight());
	}
}
