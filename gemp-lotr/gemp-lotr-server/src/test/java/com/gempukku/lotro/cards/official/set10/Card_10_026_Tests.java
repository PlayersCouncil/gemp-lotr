package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_026_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "10_26");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CursedofErechStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Cursed of Erech
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Wraith
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Enduring</b>. To play, add a threat.<br>When you play Cursed of Erech, exert him twice.<br>At the start of the maneuver phase, you may exert another [gondor] Wraith to make Cursed of Erech <b>defender +1</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Cursed of Erech", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void IsPlayedByAddingAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cursed = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(cursed);

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(cursed));
		scn.FreepsPlayCard(cursed);

		assertEquals(1, scn.GetThreats());
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, cursed);
		// "When you play Cursed of Erech, exert him twice."
		assertEquals(2, scn.GetWoundsOn(cursed));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAThreatCannotBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cursed = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(cursed);

		scn.StartGame();

		// The Ring-bearer is the only companion in play, so one threat is as many as there may be: the board
		// nearest to one on which the threat can be added.  He is not counted as a companion before he is played.
		scn.AddThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.HAND, cursed);
		assertFalse(scn.FreepsPlayAvailable(cursed));

		// With the threat gone he is offered again.
		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(cursed));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
