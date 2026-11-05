package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_049_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Bifur, Inarticulate: [dwarven] follower of race Dwarf, cost 1.  Aid and a fierce-win trigger.
					put("bifur", "30_5");
					put("bifur2", "30_5");
					// Roäc, Son of Carc: [dwarven] follower of race Bird, cost 1.  Aid and a maneuver ability.
					put("roac", "32_8");
					// Old Thrush, Messenger Bird: a [shire] follower, cost 1.  Aid and a maneuver ability.
					put("thrush", "31_42");
					// Thorin, Oakenshield: a [dwarven] companion, cost 2, nothing needed to play him.
					put("thorin", "30_19");
					// Kili, Brother of Fili: a [dwarven] companion, here only the card laid on top of the draw deck.
					put("kili", "30_12");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BagEndStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Bag End
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 1
		 * Shadow Number: 0
		 * Direction: none
		 * Game Text: <b>Fellowship:</b> Play a [dwarven] follower to draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Bag End", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
	}

	@Test
	public void FellowshipActionPlaysADwarvenFollowerFromHandToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bifur = scn.GetFreepsCard("bifur");
		var roac = scn.GetFreepsCard("roac");
		var thrush = scn.GetFreepsCard("thrush");
		var thorin = scn.GetFreepsCard("thorin");
		var kili = scn.GetFreepsCard("kili");

		scn.StartGame();
		var bagEnd = scn.GetCurrentSite();
		assertEquals("30_49", bagEnd.getBlueprintId());
		scn.SetFreepsHand(bifur, roac, thrush, thorin);
		scn.MoveCardsToTopOfDeck(kili);
		scn.FreepsRefreshDecision();

		assertEquals(4, scn.GetFreepsHandCount());
		assertInZone(Zone.DECK, kili);
		assertEquals(kili, scn.GetFreepsTopOfDeck());
		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsActionAvailable(bagEnd));

		scn.FreepsUseCardAction(bagEnd);

		// A Dwarf follower and a Bird follower, both [dwarven]; not a [shire] follower, nor a [dwarven] companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bifur, roac));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thrush, thorin));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(bifur);

		assertInZone(Zone.SUPPORT, bifur);
		assertInZone(Zone.HAND, kili, roac, thrush, thorin);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedWithNoDwarvenFollowerInHandThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bifur = scn.GetFreepsCard("bifur");
		var bifur2 = scn.GetFreepsCard("bifur2");
		var thrush = scn.GetFreepsCard("thrush");
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCardsToSupportArea(bifur);

		scn.StartGame();
		var bagEnd = scn.GetCurrentSite();
		// A second Bifur, who cannot be played beside the first (unique), a [shire] follower and a [dwarven] companion.
		scn.SetFreepsHand(bifur2, thrush, thorin);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, bifur);
		assertInZone(Zone.HAND, bifur2, thrush, thorin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(thrush));
		assertTrue(scn.FreepsPlayAvailable(thorin));
		assertFalse(scn.FreepsPlayAvailable(bifur2));
		assertFalse(scn.FreepsActionAvailable(bagEnd));
	}
}
