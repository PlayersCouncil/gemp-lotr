package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_053_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hate", "53_53");
					put("uruk", "1_151");      // Uruk Lieutenant (Uruk-hai)
					put("goblinman", "2_42");  // Goblin Man (Isengard Orc)
					put("runner", "1_178");    // Goblin Runner

					put("chaff1", "1_180");
					put("chaff2", "1_181");
					put("chaff3", "1_182");
					put("chaff4", "1_183");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HateAndAngerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Hate and Anger
		 * Unique: false
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Spot an Uruk-hai and an Orc to draw 3 cards.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hate");

		assertEquals("Hate and Anger", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void HateAndAngerDrawsThreeCardsWithUrukHaiAndOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hate = scn.GetShadowCard("hate");
		var uruk = scn.GetShadowCard("uruk");
		var runner = scn.GetShadowCard("runner");

		scn.MoveMinionsToTable(uruk, runner);
		scn.MoveCardsToHand(hate);
		scn.MoveCardsToTopOfShadowDeck("chaff1", "chaff2", "chaff3", "chaff4");

		scn.StartGame();
		scn.SkipToPhase(Phase.SHADOW);

		int handBefore = scn.GetShadowHand().size();

		// Shadow plays Hate and Anger
		scn.ShadowPlayCard(hate);

		// Card played from hand (-1), 3 cards drawn (+3) = net +2
		assertEquals(handBefore + 2, scn.GetShadowHand().size());
	}

	@Test
	public void HateAndAngerDrawsThreeCardsWithUrukHaiAndIsenOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hate = scn.GetShadowCard("hate");
		var uruk = scn.GetShadowCard("uruk");
		var goblinman = scn.GetShadowCard("goblinman");

		scn.MoveMinionsToTable(uruk, goblinman);
		scn.MoveCardsToHand(hate);
		scn.MoveCardsToTopOfShadowDeck("chaff1", "chaff2", "chaff3", "chaff4");

		scn.StartGame();
		scn.SkipToPhase(Phase.SHADOW);

		int handBefore = scn.GetShadowHand().size();

		// Shadow plays Hate and Anger
		scn.ShadowPlayCard(hate);

		// Card played from hand (-1), 3 cards drawn (+3) = net +2
		assertEquals(handBefore + 2, scn.GetShadowHand().size());
	}
}
