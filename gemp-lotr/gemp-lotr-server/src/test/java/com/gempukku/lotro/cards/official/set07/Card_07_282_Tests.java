package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_282_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_282");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetThreatScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("host", "7_282");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a second companion, so that 2 threats can lie
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HostofUdunStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Host of Udûn
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 10
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 21
		 * Vitality: 5
		 * Site Number: 6
		 * Game Text: When you play Host of Udûn, you may remove X threats to make its twilight cost -X.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Host of Udûn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(10, card.getBlueprint().getTwilightCost());
		assertEquals(21, card.getBlueprint().getStrength());
		assertEquals(5, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void RemovingAThreatAsItIsPlayedTakesOneOffItsCostAndAPoolOneShortThenPlaysIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreatScenario();

		var host = scn.GetShadowCard("host");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));
		scn.MoveCardsToHand(host);

		scn.StartGame();
		scn.AddThreats(2);
		// 7 set + 2 companions + 2 for the site: 11 in the Shadow phase.
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(11, scn.GetTwilight());
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.HAND, host);
		// 10 to play + 2 roaming (site number 6, at site 2) is 12: one more than the pool holds, which one threat
		// removed makes up.
		assertTrue(scn.ShadowPlayAvailable(host));

		scn.ShadowPlayCard(host);
		// X is his to choose: from the 1 the pool is short by, up to the 2 threats there are.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");

		assertEquals(1, scn.GetThreats());
		// 10 + 2 roaming - 1 for the threat.
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, host);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThePoolOneShortAndNoThreatToRemoveItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreatScenario();

		var host = scn.GetShadowCard("host");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));
		scn.MoveCardsToHand(host);

		scn.StartGame();
		// 7 set + 2 companions + 2 for the site: 11 in the Shadow phase.
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(11, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.HAND, host);
		// 10 to play + 2 roaming is 12, and there is no threat to make up the 1 the pool is short by.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(host));

		// One threat on the same table, and it is offered.
		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowPlayAvailable(host));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
