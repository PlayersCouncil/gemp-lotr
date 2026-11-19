package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_035_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("host", "30_35");
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, cost 2, site 3; an Assignment ability
					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, cost 2, site 4; a Regroup ability
					// Bill Ferny Sr.: a [gundabad] minion who is a Man, not an Orc
					put("ferny", "33_22");
					// Goblin Sneak: [moria] Orc; the Free Peoples player's copy lies in his own discard pile
					put("sneak", "31_32");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void HostofThousandsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Host of Thousands
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gundabad
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Play an Orc from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("host");

		assertEquals("Host of Thousands", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GUNDABAD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysAnOrcFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var aggressor = scn.GetShadowCard("aggressor");
		var veteran = scn.GetShadowCard("veteran");
		var ferny = scn.GetShadowCard("ferny");
		var fpSneak = scn.GetFreepsCard("sneak");

		scn.StartGame();
		scn.MoveCardsToHand(host);
		scn.MoveCardsToDiscard(aggressor, veteran, ferny);
		// An Orc in the other player's discard pile, of a title the Shadow player's own discard pile does not hold.
		scn.MoveCardsToDiscard(fpSneak);
		scn.SetTwilight(20);
		assertInHand(host);
		assertInDiscard(aggressor, veteran, ferny, fpSneak);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(host));
		scn.ShadowPlayCard(host);

		// Orcs of two cultures; not the [gundabad] Man, nor the other player's Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, veteran));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ferny, fpSneak));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(aggressor);

		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertInDiscard(veteran, ferny, fpSneak, host);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoOrcInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var ferny = scn.GetShadowCard("ferny");
		var fpSneak = scn.GetFreepsCard("sneak");

		scn.StartGame();
		scn.MoveCardsToHand(host);
		scn.MoveCardsToDiscard(ferny, fpSneak);
		scn.SetTwilight(20);
		assertInHand(host);
		assertInDiscard(ferny, fpSneak);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(host));
		assertInHand(host);
	}

	@Test
	public void CannotBePlayedUntilThePoolCanPayForTheOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.MoveCardsToHand(host);
		scn.MoveCardsToDiscard(aggressor);
		assertInHand(host);
		assertInDiscard(aggressor);
		scn.FreepsPass();

		// 0 for the event, and 2 for the Orkish Aggressor and 2 for roaming (a site 3 minion at site 2): one short.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(host));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(host));
	}
}
