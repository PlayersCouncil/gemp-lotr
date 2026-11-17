package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_18_093_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wary", "18_93");
					put("wary2", "18_93");      // a second copy: not unique

					put("faramir", "18_48");    // Faramir, Captain of Ithilien: [gondor] Man companion, ranger, hunter 2; a maneuver heal used only on purpose
					put("legolas", "15_18");    // Legolas, Fleet-footed Hunter: [elven] Elf companion, hunter 1; a regroup draw used only on purpose
					put("tracker", "15_171");   // Tracking Uruk: [uruk-hai] Uruk-hai minion, strength 14; hunter 1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WaryOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Wary Orc
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: This minion is strength +1 for each hunter companion you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wary");

		assertEquals("Wary Orc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void StrengthPlus1ForEachHunterCompanionAndForNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var faramir = scn.GetFreepsCard("faramir");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToHand(faramir, legolas);

		var wary = scn.GetShadowCard("wary");
		var wary2 = scn.GetShadowCard("wary2");
		var tracker = scn.GetShadowCard("tracker");

		scn.StartGame();
		// Laid now to be read across the Free Peoples player's plays (Q-0228).
		scn.MoveMinionsToTable(wary, wary2, tracker);
		scn.FreepsRefreshDecision();

		// A companion who is no hunter (Frodo) and a hunter who is no companion: neither counts.
		assertFalse(scn.HasKeyword(frodo, Keyword.HUNTER));
		assertTrue(scn.HasKeyword(tracker, Keyword.HUNTER));
		assertEquals(7, scn.GetStrength(wary));
		assertEquals(7, scn.GetStrength(wary2));
		assertEquals(14, scn.GetStrength(tracker));

		scn.FreepsPlayCard(faramir);
		// One hunter companion: +1, though his hunter number is 2. Each copy has its own +1, not the other's too.
		assertEquals(8, scn.GetStrength(wary));
		assertEquals(8, scn.GetStrength(wary2));
		assertEquals(14, scn.GetStrength(tracker));

		scn.FreepsPlayCard(legolas);
		// Two hunter companions, of two cultures and races: +2.
		assertEquals(9, scn.GetStrength(wary));
		assertEquals(9, scn.GetStrength(wary2));
		assertEquals(14, scn.GetStrength(tracker));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
