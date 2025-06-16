package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("kingdom", "1_16");
					put("gimli", "1_13");
					put("guard", "1_7");

					put("runner1", "1_178");
					put("runner2", "1_178");
					put("runner3", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreatestKingdomofMyPeopleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Greatest Kingdom of My People
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Tale.</b>  To play, exert a Dwarf.
		 * 	Each time your opponent plays an Orc, that player discards the top card of his or her draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("kingdom");

		assertEquals("Greatest Kingdom of My People", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void GreatestKingdomExertsADwarfToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionToTable(gimli);
		scn.MoveCardsToHand(kingdom, guard);

		scn.AddWoundsToChar(gimli, 2);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(kingdom));
		scn.FreepsPlayCard(guard);
		assertEquals(0, scn.GetWoundsOn(guard));
		scn.FreepsPlayCard(kingdom);
		assertEquals(1, scn.GetWoundsOn(guard));
	}

	@Test
	public void GreatestKingdomTriggerDiscardsFromShadowTopDeckEachTime() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToSupportArea(kingdom);
		scn.MoveCardsToDiscard(gimli, guard);
		scn.MoveCardsToFreepsDiscard("runner1", "runner2");

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCardsToHand(runner1, runner2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		var top1 = scn.GetFromTopOfShadowDeck(1);
		var top2 = scn.GetFromTopOfShadowDeck(2);
		var top3 = scn.GetFromTopOfShadowDeck(3);

		assertEquals(Zone.DECK, top1.getZone());
		assertEquals(Zone.DECK, top2.getZone());
		assertEquals(Zone.DECK, top3.getZone());
		scn.ShadowPlayCard(runner1);
		scn.FreepsResolveActionOrder("0");

		assertEquals(Zone.DISCARD, top1.getZone());
		assertEquals(Zone.DECK, top2.getZone());
		assertEquals(Zone.DECK, top3.getZone());

		scn.ShadowPlayCard(runner2);
		scn.FreepsResolveActionOrder("0");

		assertEquals(Zone.DISCARD, top1.getZone());
		assertEquals(Zone.DISCARD, top2.getZone());
		assertEquals(Zone.DECK, top3.getZone());

	}
}
