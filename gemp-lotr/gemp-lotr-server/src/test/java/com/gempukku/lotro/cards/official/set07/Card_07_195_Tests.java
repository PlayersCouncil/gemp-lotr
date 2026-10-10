package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_195_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_195");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDrawScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("march", "7_195");
					// Elrond, Lord of Rivendell: an ally; "Fellowship: Exert Elrond to draw a card."
					put("elrond", "1_40");
					// Barliman Butterbur, Prancing Pony Proprietor: an ally; "Fellowship: Exert Barliman Butterbur
					// to take a [gandalf] event into hand from your discard pile."
					put("barliman", "1_70");
					put("wizard", "1_78");          // Mysterious Wizard: a [gandalf] event, for the discard pile
					// Aragorn, Ranger of the North: a second companion, so that two threats have room
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulontheMarchStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul on the March
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a Nazgûl.<br>Each time the Free Peoples player draws a card (except during the regroup phase), add a threat.<br><b>Regroup:</b> Discard a Nazgûl and this condition to add a threat.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Morgul on the March", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	@Ignore("F-0376: Morgul on the March adds a threat for a card taken into hand from the discard pile, which is not drawn")
	public void ACardTakenIntoHandFromTheDiscardPileIsNotACardDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDrawScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var barliman = scn.GetFreepsCard("barliman");
		var wizard = scn.GetFreepsCard("wizard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveAlliesToTable(elrond, barliman);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDiscard(wizard);
		scn.MoveCardsToSupportArea(scn.GetShadowCard("march"));

		scn.StartGame();
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetFreepsHandCount());

		// A card drawn: one threat.
		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetThreats());

		// A card taken into hand from the discard pile is in hand and was not drawn: no second threat,
		// though two companions leave room for one.
		scn.FreepsUseCardAction(barliman);
		assertInHand(wizard);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
