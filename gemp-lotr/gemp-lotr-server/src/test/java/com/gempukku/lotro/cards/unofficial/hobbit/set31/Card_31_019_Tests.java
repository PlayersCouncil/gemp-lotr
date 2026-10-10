package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_019_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nothing", "31_19");
					// A Wizard Is Never Late: a [gandalf] Fellowship event of the Hobbit sets, cost 1, which plays
					// Gandalf (or an ally who is no Dwarf) from the draw deck or the discard pile.
					put("late", "30_23");
					// Gandalf, Friend of the Shirefolk: cost 4, a strength bonus and nothing else.  The card the
					// event plays, so that it is seen to have been played and carried out.
					put("gandalf", "1_72");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BetterThanNothingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Better Than Nothing
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: (No sheet has the Hobbit sets and the definition carries no game text: these are the
		 * 	definition's own fields.)
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nothing");

		assertEquals("Better Than Nothing", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// From the definition, there being no text to go by: while Gollum can be spotted, a [gandalf] event costs a
	// burden more to play.  This is the other side of it: no Gollum, no burden.
	@Test
	public void WithNoGollumInPlayAGandalfEventCostsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nothing = scn.GetShadowCard("nothing");
		var late = scn.GetFreepsCard("late");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToSupportArea(nothing);
		scn.MoveCardsToHand(late);
		scn.MoveCardsToDiscard(gandalf);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, nothing);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(late));

		scn.FreepsPlayCard(late);
		// The draw deck is offered whatever it holds; Gandalf is in the discard pile, the one card the event
		// could play.
		scn.FreepsChoose("discard pile");

		assertInZone(Zone.DISCARD, late);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		// 1 for the event + 4 for Gandalf
		assertEquals(5, scn.GetTwilight());
		// The same reading as before the play, and it is the check: were the burden charged whether or not
		// Gollum is in play, this would read 1.
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
