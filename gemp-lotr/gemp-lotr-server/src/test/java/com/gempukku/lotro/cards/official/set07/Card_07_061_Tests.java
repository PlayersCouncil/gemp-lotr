package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_061_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hobbitses", "7_61");
					put("gollum", "9_28");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HobbitsesAreDeadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Hobbitses Are Dead
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot Gollum or Sméagol to make a Nazgûl, [sauron] minion, or [gollum] minion strength +2.<br>If you have initiative, you may play this event from your discard pile; place it under your draw deck instead of discarding it.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hobbitses");

		assertEquals("Hobbitses Are Dead", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void HobbitsesAreDeadCanBePlayedFromDiscardWithInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var hobbitses = scn.GetShadowCard("hobbitses");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToDiscard(hobbitses);
		scn.MoveMinionsToTable(gollum);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, gollum);
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.GetTwilight() > 1);
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(Zone.SHADOW_CHARACTERS, gollum.getZone());
		assertEquals(Zone.DISCARD, hobbitses.getZone());
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.ShadowActionAvailable(hobbitses));

		scn.ShadowUseCardAction(hobbitses);
		assertEquals(7, scn.GetStrength(gollum));
		assertEquals(Zone.DECK, hobbitses.getZone());
	}
}
