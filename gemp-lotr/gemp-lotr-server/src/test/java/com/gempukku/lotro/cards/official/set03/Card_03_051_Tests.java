package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_051_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "3_51");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRevealScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("coming", "3_51");
					put("uruk", "1_151");       // Uruk Savage: an [isengard] minion, vitality 2
					put("blow", "1_5");         // Cleaving Blow: a card for the Free Peoples player's hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ComingfortheRingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Coming for the Ring
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Reveal a card at random from the Free Peoples player's hand. Heal X [isengard] minions, where X is the twilight cost of the card revealed.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Coming for the Ring", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void ComingfortheRingTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}

	@Test
	public void IsNotPlayableWithTheFreePeoplesHandEmpty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRevealScenario();

		var coming = scn.GetShadowCard("coming");
		var uruk = scn.GetShadowCard("uruk");
		var blow = scn.GetFreepsCard("blow");
		scn.MoveCardsToHand(coming);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk);
		scn.SkipToPhase(Phase.REGROUP);
		scn.AddWoundsToChar(uruk, 1);
		assertEquals(1, scn.GetWoundsOn(uruk));
		assertInZone(Zone.HAND, coming);
		scn.FreepsPass();

		// A wounded [isengard] minion, and no card in the Free Peoples hand to reveal: the reveal must be able to
		// happen for the heal to read it, so the event is not playable (Q-0336: B).
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(uruk));
		assertEquals(0, scn.GetFreepsHandCount());
		assertFalse(scn.ShadowPlayAvailable(coming));

		// One card there, and it is playable.
		scn.MoveCardsToHand(blow);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowPlayAvailable(coming));
		assertInZone(Zone.HAND, coming);
	}
}
