package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_052_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "2_52");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The King-block path: its site 3, The Dimholt, is underground (the Balrog is discarded anywhere else) and is
	// one short of the site number 4 of every [moria] Orc, so an Orc played there pays the roaming penalty.
	protected VirtualTableScenario GetDimholtScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("balrog", "2_52");
					// Goblin Sneak: a [moria] Orc of cost 1, site number 4.  "When you play this minion, you may
					// place a [moria] Orc from your discard pile beneath your draw deck."
					put("sneak", "1_181");
					// Goblin Runner: a [moria] Orc of cost 1, site number 4; the other Orc in the discard pile.
					// (Played, it adds (2); it is not played here.)
					put("runner", "1_178");
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheBalrogStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: The Balrog, Flame of Udûn
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 14
		 * Type: Minion
		 * Subtype: Balrog
		 * Strength: 17
		 * Vitality: 5
		 * Site Number: 4
		 * Game Text: <b>Damage +1</b>. <b>Fierce</b>. To play, spot a [moria] Orc. Discard The Balrog if not underground.<br><b>Shadow:</b> Exert The Balrog and remove (2) to play a [moria] Orc from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("The Balrog", card.getBlueprint().getTitle());
		assertEquals("Flame of Udûn", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.BALROG, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(14, card.getBlueprint().getTwilightCost());
		assertEquals(17, card.getBlueprint().getStrength());
		assertEquals(5, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityIsWithheldUntilThePoolPaysTheTwoItRemovesAndTheOrcAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDimholtScenario();

		var balrog = scn.GetShadowCard("balrog");
		var sneak = scn.GetShadowCard("sneak");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(2);
		// The move to The Dimholt, site 3: underground.
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.MoveMinionsToTable(balrog);
		scn.MoveCardsToDiscard(sneak, runner);
		assertInZone(Zone.SHADOW_CHARACTERS, balrog);
		assertEquals(0, scn.GetWoundsOn(balrog));

		// Either Orc costs 1 + 2 for roaming at site 3 = 3 from the discard pile.  A pool of 4 pays the (2)
		// the ability removes, and it pays the Orc, but not one after the other: 2 + 3 = 5.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(balrog));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(balrog));

		scn.ShadowUseCardAction(balrog);
		assertEquals(1, scn.GetWoundsOn(balrog));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(sneak, runner));

		scn.ShadowChooseCards(sneak);
		assertInZone(Zone.SHADOW_CHARACTERS, sneak);
		assertInZone(Zone.DISCARD, runner);
		// 5 - 2 removed - 1 for the Sneak - 2 for roaming
		assertEquals(0, scn.GetTwilight());
		// The Sneak's own offer, to put the Runner beneath the draw deck.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.DISCARD, runner);
		assertEquals(1, scn.GetWoundsOn(balrog));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
