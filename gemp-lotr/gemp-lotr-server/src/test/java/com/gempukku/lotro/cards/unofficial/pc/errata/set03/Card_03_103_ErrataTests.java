package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_103_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("dawn", "53_103");
					put("ithil", "9_47");

					put("otherfrodo", "1_290");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void TerribleastheDawnStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Terrible as the Dawn
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If a companion or ally's special ability is used, you may spot a [sauron] card to wound that character.  The Free Peoples player may discard another companion to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dawn");

		assertEquals("Terrible as the Dawn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.hasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TerribleastheDawnDoesNotTriggerOnDiscardToHeal() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var dawn = scn.GetShadowCard("dawn");
		scn.ShadowMoveCardToHand(dawn);
		scn.ShadowMoveCardToSupportArea("ithil"); //For spot requirement

		var frodo = scn.GetRingBearer();
		var otherfrodo = scn.GetFreepsCard("otherfrodo");
		scn.FreepsMoveCardToHand(otherfrodo);

		scn.AddWoundsToChar(frodo, 1);
		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable("heal by discard"));
		scn.FreepsPlayCard(otherfrodo);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}
}
