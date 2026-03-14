package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_02_046_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("captain", "52_46");
					put("uruk", "1_151");    // Uruk Lieutenant (another Uruk-hai to exert)
					put("brood", "1_154");   // Uruk Brood (Uruk-hai in discard to play)
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukCaptainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Uruk Captain
		 * Unique: true
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Shadow:</b> Remove (1) and exert an Uruk-hai
		 * to play an Uruk-hai from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("captain");

		assertEquals("Uruk Captain", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void UrukCaptainExertsAnyUrukHaiToPlayFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var uruk = scn.GetShadowCard("uruk");
		var brood = scn.GetShadowCard("brood");

		scn.MoveMinionsToTable(captain, uruk);
		scn.MoveCardsToDiscard(brood);

		scn.StartGame();

		scn.AddTwilight(4);

		scn.SkipToPhase(Phase.SHADOW);

		// Captain's ability should be available
		assertTrue(scn.ShadowActionAvailable(captain));
		scn.ShadowUseCardAction(captain);

		// Choose which Uruk-hai to exert -- can pick the Lieutenant instead of self
		// (Errata changed from "exert Uruk Captain" to "exert an Uruk-hai")
		assertTrue(scn.ShadowCanChooseCharacter(uruk));
		assertTrue(scn.ShadowCanChooseCharacter(captain));
		scn.ShadowChooseCard(uruk);

		// Uruk Brood should be auto-selected from discard and played
		assertEquals(1, scn.GetWoundsOn(uruk));
		assertEquals(0, scn.GetWoundsOn(captain));
		assertInZone(Zone.SHADOW_CHARACTERS, brood);
	}
}
