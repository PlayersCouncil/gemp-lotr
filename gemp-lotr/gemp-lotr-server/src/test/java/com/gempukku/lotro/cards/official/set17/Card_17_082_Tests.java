package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_082_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "17_82");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDiscardPileScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rider", "17_82");
					// Three [orc] mounts of three blueprints, each for an [orc] Orc of strength 9 or less.
					put("vicious", "17_92");   // Vicious Warg: for the hand, the only [orc] card there
					put("gothmog", "18_81");   // Gothmog's Warg, Leader's Mount, cost 1: for the discard pile
					put("threatening", "17_91"); // Threatening Warg: for the discard pile
					// Goblin Runner: a [moria] card in hand, which cannot pay
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishRiderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Orkish Rider
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: Each mounted [orc] Orc is strength +1.<br><b>Shadow:</b> Discard an [orc] card from your hand to play an [orc] mount from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Orkish Rider", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TheMountPlayedIsNotTheCardDiscardedToPayForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var rider = scn.GetShadowCard("rider");
		var vicious = scn.GetShadowCard("vicious");
		var gothmog = scn.GetShadowCard("gothmog");
		var threatening = scn.GetShadowCard("threatening");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToHand(vicious, runner);
		scn.MoveCardsToDiscard(gothmog, threatening);

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		assertInHand(vicious, runner);
		assertInDiscard(gothmog, threatening);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertInHand(vicious, runner);
		assertInDiscard(gothmog, threatening);
		// 20 set + 1 companion + 2 for Ettenmoors
		assertEquals(23, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(rider));
		scn.ShadowUseCardAction(rider);
		// The Vicious Warg is the one [orc] card in hand and pays without a choice
		assertInDiscard(vicious);
		assertInHand(runner);

		// The two mounts that lay in the discard pile before the cost was paid, and not the one that lies there
		// now because it paid it (CR 4.2 "effect", the They Are Coming example; Q-0320).
		assertTrue(scn.ShadowHasCardChoiceAvailable(gothmog, threatening));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(vicious));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(gothmog);

		// The Rider is the only Orc to bear it
		assertAttachedTo(gothmog, rider);
		assertEquals(22, scn.GetTwilight());
		assertInDiscard(vicious, threatening);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void OrkishRiderTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}
}
