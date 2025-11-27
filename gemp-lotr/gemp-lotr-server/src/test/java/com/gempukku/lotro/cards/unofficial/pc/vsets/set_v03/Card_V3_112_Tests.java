package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_112_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("barrel1", "103_112");
					put("barrel2", "103_112");
					put("frodospipe", "3_107");
					put("fodder1", "3_108");
					put("fodder2", "3_110");
					put("fodder3", "3_106");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HornblowerBarrelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Hornblower Barrel
		 * Unique: 2
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: Pipeweed.
		 *		When you play this possession, you may discard a Free Peoples card from your draw deck.
		 *		When you discard this with a pipe, you may spot another Hornblower Barrel to take 2 Free Peoples cards
		 *		(except events) from your discard pile into your hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Hornblower Barrel", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.PIPEWEED));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void HornblowerBarrelRetrieves2CardsWhenDiscardedByPipe() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var barrel1 = scn.GetFreepsCard("barrel1");
		var barrel2 = scn.GetFreepsCard("barrel2");
		var frodospipe = scn.GetFreepsCard("frodospipe");
		var fodder1 = scn.GetFreepsCard("fodder1");
		var fodder2 = scn.GetFreepsCard("fodder2");
		var fodder3 = scn.GetFreepsCard("fodder3");
		scn.MoveCardsToSupportArea(barrel1, barrel2);
		scn.AttachCardsTo(frodo, frodospipe);
		scn.MoveCardsToDiscard(fodder1, fodder2, fodder3);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, barrel1);
		assertInZone(Zone.SUPPORT, barrel2);
		assertInZone(Zone.DISCARD, fodder1);
		assertInZone(Zone.DISCARD, fodder2);
		assertInZone(Zone.DISCARD, fodder3);

		assertTrue(scn.FreepsActionAvailable(frodospipe));
		scn.FreepsUseCardAction(frodospipe);
		scn.FreepsChooseCard(barrel1);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCards(fodder1, fodder2);

		assertInZone(Zone.DISCARD, barrel1);
		assertInZone(Zone.SUPPORT, barrel2);
		assertInZone(Zone.HAND, fodder1);
		assertInZone(Zone.HAND, fodder2);
		assertInZone(Zone.DISCARD, fodder3);
	}
}
