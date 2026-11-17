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
					put("host", "7_282");
					// Orc Chieftain: another [sauron] Orc, cost 2, site 6; his text answers only a weapon played on him
					put("chieftain", "1_266");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion, so that threats can lie
					put("gimli", "1_13");       // Gimli, Son of Gloin: a third companion, for a third threat
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

		var host = scn.GetFreepsCard("host");

		assertEquals("Host of Udûn", host.getBlueprint().getTitle());
		assertNull(host.getBlueprint().getSubtitle());
		assertTrue(host.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, host.getBlueprint().getSide());
		assertEquals(Culture.SAURON, host.getBlueprint().getCulture());
		assertEquals(CardType.MINION, host.getBlueprint().getCardType());
		assertEquals(Race.ORC, host.getBlueprint().getRace());
		assertEquals(10, host.getBlueprint().getTwilightCost());
		assertEquals(21, host.getBlueprint().getStrength());
		assertEquals(5, host.getBlueprint().getVitality());
		assertEquals(6, host.getBlueprint().getSiteNumber());
	}

	@Test
	public void RemovingAThreatAsItIsPlayedTakesOneOffItsCostAndAPoolOneShortThenPlaysIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreatScenario();

		var host = scn.GetShadowCard("host");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));
		scn.MoveCardsToHand(host);
		assertInHand(host);

		scn.StartGame();
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
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
		assertInHand(host);

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
		assertEquals(1, scn.GetThreats());
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowPlayAvailable(host));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThePoolAbleToPayInFullXRunsFromNoneToEachThreatAndTwoRemovedTakeTwoOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"));
		scn.MoveCardsToHand(host);
		assertInHand(host);

		scn.StartGame();
		// Three companions hold 3 threats.
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		// 20 set + 3 companions + 2 for the site: 25 in the Shadow phase.
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(25, scn.GetTwilight());
		assertEquals(3, scn.GetThreats());
		scn.ShadowPlayCard(host);
		// The pool pays in full: X may be none, and runs to the 3 threats there are.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		assertEquals(1, scn.GetThreats());
		// 25 - (10 - 2 for the threats + 2 roaming)
		assertEquals(15, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, host);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ChoosingNoneRemovesNoThreatAndPaysInFullAndAnotherSauronOrcPlayedAfterIsAskedNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));
		scn.MoveCardsToHand(host, chieftain);
		assertInHand(host, chieftain);

		scn.StartGame();
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		// 20 set + 2 companions + 2 for the site: 24 in the Shadow phase.
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(24, scn.GetTwilight());
		assertEquals(2, scn.GetThreats());
		scn.ShadowPlayCard(host);
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChoose("0");

		assertEquals(2, scn.GetThreats());
		// 24 - (10 + 2 roaming)
		assertEquals(12, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, host);

		// Another [sauron] Orc played after him, threats still there: no X is asked, and he pays his own cost.
		scn.ShadowPlayCard(chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertEquals(2, scn.GetThreats());
		// 12 - (2 + 2 roaming)
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
