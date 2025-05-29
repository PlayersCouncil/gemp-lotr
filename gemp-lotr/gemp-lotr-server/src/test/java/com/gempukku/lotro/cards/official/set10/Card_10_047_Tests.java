package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_047_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("call", "10_47");
					put("southron", "4_220");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RallyingCallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Rallying Call
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a [raider] Man.
		 * Threats cannot be removed by Free Peoples cards.
		 * <b>Shadow:</b> Remove (1) and play a Southron to add a threat.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("call");

		assertEquals("Rallying Call", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RallyingCallRemoves1AndPlaysASouthronToAddAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var call = scn.GetShadowCard("call");
		var southron = scn.GetShadowCard("southron");
		scn.MoveCardsToSupportArea(call);
		scn.MoveCardsToHand(southron);

		scn.StartGame();
		scn.SetTwilight(7);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(10, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(call));
		assertTrue(scn.ShadowPlayAvailable(southron));
		assertEquals(Zone.HAND, southron.getZone());

		scn.ShadowUseCardAction(call);

		//10 twiliight -4 base -2 roaming -1 extra tax from Call
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		assertEquals(Zone.SHADOW_CHARACTERS, southron.getZone());
	}

	@Test
	public void RallyingCallHasEffectEvenWhenFinalTwilightPoolIsZero() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var call = scn.GetShadowCard("call");
		var southron = scn.GetShadowCard("southron");
		scn.MoveCardsToSupportArea(call);
		scn.MoveCardsToHand(southron);

		scn.StartGame();
		scn.SetTwilight(4);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(7, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(call));
		assertTrue(scn.ShadowPlayAvailable(southron));
		assertEquals(Zone.HAND, southron.getZone());

		scn.ShadowUseCardAction(call);

		//We must ensure that the final tally results in an empty twilight pool
		// as it is here that PlayCardFromHand falters if set up incorrectly
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		assertEquals(Zone.SHADOW_CHARACTERS, southron.getZone());
	}
}
