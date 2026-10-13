package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_027_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "9_27");
					// Radagast, The Brown: a unique Wizard companion of cost 4; the move limit is +1, and his other
					// text answers a move in the regroup phase.  Two copies: one dead, one in hand.
					put("radagast1", "9_26");
					put("radagast2", "9_26");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SentBackStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Sent Back
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Skirmish:</b> Discard this condition to discard each minion skirmishing a Wizard. Place that Wizard in your dead pile.<br><b>Fellowship</b> <i>or</i> <b>Regroup:</b> Play a Wizard (even if another copy of that Wizard is in your dead pile).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Sent Back", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipAbilityPlaysAWizardWhoseOtherCopyIsInTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentback = scn.GetFreepsCard("card");
		var dead = scn.GetFreepsCard("radagast1");
		var held = scn.GetFreepsCard("radagast2");
		scn.MoveCardsToSupportArea(sentback);
		scn.MoveCardsToDeadPile(dead);
		scn.MoveCardsToHand(held);

		scn.StartGame();

		// A Radagast in the dead pile bars the copy in hand from being played in the ordinary way.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.HAND, held);
		assertFalse(scn.FreepsPlayAvailable(held));
		assertTrue(scn.FreepsActionAvailable(sentback));
		assertEquals(0, scn.GetTwilight());
		scn.FreepsUseCardAction(sentback);

		// He is played, for his own cost of 4, and the condition stays where it was.
		assertInZone(Zone.FREE_CHARACTERS, held);
		assertInZone(Zone.DEAD, dead);
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, sentback);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
