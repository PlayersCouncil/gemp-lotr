package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_094_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "2_94");
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
					put("verily", "2_94");
					// Elrond, Lord of Rivendell: an ally.  "Fellowship: Exert Elrond to draw a card."
					put("elrond", "1_40");
					put("sword", "1_299");      // Hobbit Sword: the card on top of the draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VerilyIComeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Verily I Come
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Game Text: To play, exert a [sauron] Orc. Plays on the Ring-bearer.<br>Each time the Free Peoples player draws a card (or takes a card into hand) during the fellowship phase, add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Verily I Come", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AddsABurdenWhenTheFreePeoplesPlayerDrawsACardInTheFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDrawScenario();

		var verily = scn.GetShadowCard("verily");
		var frodo = scn.GetRingBearer();
		var elrond = scn.GetFreepsCard("elrond");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.AttachCardsTo(frodo, verily);
		scn.MoveCardsToTopOfDeck(sword);
		scn.FreepsRefreshDecision();

		assertAttachedTo(verily, frodo);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertTrue(scn.FreepsActionAvailable(elrond));

		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertInZone(Zone.HAND, sword);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
