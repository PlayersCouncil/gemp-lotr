package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_041_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("host", "9_41");

					// Goblin Patrol Troop: a [moria] Orc minion with no text; cost 6, site 4
					put("troop", "1_177");
					// Goblin Scimitar: a [moria] hand weapon, cost 0, borne by a [moria] Orc; may draw a card as it is played
					put("scimitar", "1_180");
					// Pinned Down: a [moria] support-area condition, cost 1; its text needs a [moria] archer
					put("pinned", "1_192");
					// Orc Chieftain: a [sauron] Orc minion, cost 2, site 6; its text answers a weapon played on it
					put("chieftain", "1_266");
					// Goblin Marksman: a [moria] archer minion, cost 3, site 4; laid only in the Free Peoples player's discard pile
					put("marksman", "1_176");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HostofMoriaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Host of Moria, Legion of the Underdeeps
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 15
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: When you play Host of Moria, you may play a [moria] card from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("host");

		assertEquals("Host of Moria", card.getBlueprint().getTitle());
		assertEquals("Legion of the Underdeeps", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(15, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedHostOfMoriaMayPlayAMoriaCardFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var troop = scn.GetShadowCard("troop");
		var scimitar = scn.GetShadowCard("scimitar");
		var pinned = scn.GetShadowCard("pinned");
		var chieftain = scn.GetShadowCard("chieftain");
		var marksman = scn.GetFreepsCard("marksman");
		scn.StartGame();
		scn.MoveCardsToDiscard(troop, scimitar, pinned, chieftain, marksman);
		scn.MoveCardsToHand(host);
		assertInZone(Zone.DISCARD, troop, scimitar, pinned, chieftain, marksman);
		assertInZone(Zone.HAND, host);
		// 15 set + 1 companion + 2 for Ettenmoors = 18: after Host (7 + 2 roaming) 9 are left, enough for every card
		// in the piles (the Chieftain 2 + 2 roaming, the Marksman 3 + 2) as well as the ones offered.
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertEquals(18, scn.GetTwilight());
		assertInZone(Zone.DISCARD, troop, scimitar, pinned, chieftain, marksman);

		scn.ShadowPlayCard(host);

		assertInZone(Zone.SHADOW_CHARACTERS, host);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(host));

		scn.ShadowAcceptOptionalTrigger();

		// A [moria] minion, possession and condition are offered; a [sauron] minion is not, nor a [moria] card in
		// the Free Peoples player's own discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, scimitar, pinned));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(chieftain, marksman));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(pinned);

		assertInZone(Zone.SUPPORT, pinned);
		assertInZone(Zone.DISCARD, troop, scimitar, chieftain, marksman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesTheDiscardPileAndAMoriaOrcPlayedAfterHostAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var troop = scn.GetShadowCard("troop");
		var pinned = scn.GetShadowCard("pinned");
		scn.StartGame();
		scn.MoveCardsToDiscard(pinned);
		scn.MoveCardsToHand(host, troop);
		assertInZone(Zone.DISCARD, pinned);
		assertInZone(Zone.HAND, host, troop);
		// 15 set + 1 companion + 2 for Ettenmoors = 18: Host (7 + 2 roaming) and the Troop (6 + 2) after him.
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertInZone(Zone.DISCARD, pinned);

		scn.ShadowPlayCard(host);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(host));

		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, pinned);
		assertInZone(Zone.HAND, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [moria] Orc played with Host in play is no play of Host.
		scn.ShadowPlayCard(troop);

		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, pinned);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheOnlyMoriaCardInTheDiscardPileCostsMoreThanIsLeftInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var troop = scn.GetShadowCard("troop");
		var chieftain = scn.GetShadowCard("chieftain");
		var marksman = scn.GetFreepsCard("marksman");
		scn.StartGame();
		scn.MoveCardsToDiscard(troop, chieftain, marksman);
		scn.MoveCardsToHand(host);
		assertInZone(Zone.DISCARD, troop, chieftain, marksman);
		assertInZone(Zone.HAND, host);
		// 13 set + 1 companion + 2 for Ettenmoors = 16.
		scn.SetTwilight(13);
		scn.FreepsPass();

		assertEquals(16, scn.GetTwilight());
		assertInZone(Zone.DISCARD, troop, chieftain, marksman);

		scn.ShadowPlayCard(host);

		// 16 - (7 + 2 roaming) = 7, one short of the Troop's 6 + 2 roaming; the Chieftain (2 + 2) could be paid for
		// but is no [moria] card.
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, host);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, troop, chieftain, marksman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
