package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_122_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_122");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetCopiesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pit", "1_122");
					put("savage1", "1_151");    // Uruk Savage: [isengard] Uruk-hai, cost 2, site 5; the one played
					put("savage2", "1_151");    // the two copies in the discard pile
					put("savage3", "1_151");
					put("raiders", "1_158");    // Uruk-hai Raiding Party: an Uruk-hai of another title
					put("runner", "1_178");     // Goblin Runner: the one card of the Shadow player's draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BreedingPitStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Breeding Pit
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: 
		 * Game Text: <b>Response:</b> If you play an Uruk-hai, take all copies of that card in your discard pile and place them beneath your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Breeding Pit", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenYouPlayAnUrukHaiPlacesEveryCopyOfItInYourDiscardPileBeneathYourDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCopiesScenario();

		var pit = scn.GetShadowCard("pit");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var savage3 = scn.GetShadowCard("savage3");
		var raiders = scn.GetShadowCard("raiders");
		var runner = scn.GetShadowCard("runner");
		// A copy in the other player's discard pile is not in "your discard pile".
		var freepsSavage = scn.GetFreepsCard("savage2");
		scn.MoveCardsToHand(pit, savage1);
		scn.MoveCardsToDiscard(savage2, savage3, raiders, freepsSavage);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 3 set + 1 companion + 2 for Ettenmoors
		assertEquals(6, scn.GetTwilight());
		assertEquals(1, scn.GetShadowDeckCount());
		assertSame(runner, scn.GetShadowTopOfDeck());

		scn.ShadowPlayCard(savage1);
		// 2 to play + 2 roaming
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pit));

		scn.ShadowChooseOptionalTrigger(pit);
		// All copies go: the only thing asked is which is placed first, of the two copies and nothing else.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(raiders));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(savage2);

		// (1) for the event.  Both copies are in the draw deck beneath the card that was there.
		assertEquals(1, scn.GetTwilight());
		assertEquals(3, scn.GetShadowDeckCount());
		assertSame(runner, scn.GetShadowTopOfDeck());
		assertInZone(Zone.DECK, savage2, savage3);
		assertInZone(Zone.DISCARD, raiders, pit, freepsSavage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
