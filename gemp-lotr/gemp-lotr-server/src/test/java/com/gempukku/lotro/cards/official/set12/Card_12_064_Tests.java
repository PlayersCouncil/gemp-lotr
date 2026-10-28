package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_064_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("southron", "12_64");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a ranger, to spot for What Are They?; a maneuver ability used only on purpose
					put("whatare", "1_119");    // What Are They?: Maneuver event, spot a ranger to discard a roaming minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EnragedSouthronStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Enraged Southron
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 12
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Ambush (3)</b>.<br>The move limit for this turn is +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("southron");

		assertEquals("Enraged Southron", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.AMBUSH));
		assertEquals(3, scn.GetKeywordCount(card, Keyword.AMBUSH));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TheMoveLimitIsPlusOneForTheWholeTurnOnceItIsPlayedThoughItLeavesPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var southron = scn.GetShadowCard("southron");
		var aragorn = scn.GetFreepsCard("aragorn");
		var whatare = scn.GetFreepsCard("whatare");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(whatare);
		scn.SetShadowHand(southron);
		assertInHand(whatare, southron);

		scn.StartGame();
		// Enough for its 4 and the roaming 2 at site 2.
		scn.SetTwilight(10);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());

		assertEquals(2, scn.GetMoveLimit());
		scn.ShadowPlayCard(southron);
		assertInZone(Zone.SHADOW_CHARACTERS, southron);
		assertEquals(3, scn.GetMoveLimit());

		// It roams at site 2, and What Are They? discards it in the maneuver phase.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(whatare);
		assertInDiscard(southron);

		// CR 4.2 "move limit": a modification for a turn is in effect for the whole turn, even if the conditions
		// for it change.
		assertEquals(3, scn.GetMoveLimit());
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetMoveLimit());

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));

		// The turn ends, and so does the +1.
		scn.FreepsChooseToStay();
		scn.PassReconcile(VirtualTableScenario.P1);
		scn.PassReconcile(VirtualTableScenario.P2);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
	}
}
