package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_134_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_134");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetEndOfTurnScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chill", "1_134");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumansChillStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Saruman's Chill
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: <b>Spell</b>. <b>Weather</b>. To play, exert an [isengard] minion. Plays on a site. Limit 1 per site.<br>Each Hobbit who moves from this site must exert. Discard this condition at the end of the turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Saruman's Chill", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasKeyword(card, Keyword.WEATHER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsDiscardedAtTheEndOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEndOfTurnScenario();

		var chill = scn.GetShadowCard("chill");

		scn.StartGame();
		scn.FreepsPass();
		// Laid on the site the fellowship has just come to, so that no Hobbit moves from it this turn.
		var site = scn.GetCurrentSite();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.AttachCardsTo(site, chill);

		// The last thing asked in the turn: the Chill has stayed through every phase of it.
		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertAttachedTo(chill, site);
		assertTrue(scn.FreepsDecisionAvailable("another move"));

		scn.FreepsChooseToStay();
		// The first thing asked in the next turn, which is the other player's.
		assertInDiscard(chill);
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
