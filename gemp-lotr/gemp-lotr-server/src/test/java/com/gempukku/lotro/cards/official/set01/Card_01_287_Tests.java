package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_287_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_287");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetHealScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("resilience", "1_287");
					// The Gaffer, Sam's Father: "Fellowship: Exert The Gaffer to heal Frodo or Sam."
					put("gaffer", "1_291");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ExtraordinaryResilienceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Extraordinary Resilience
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: 
		 * Game Text: <b>Response:</b> If the Ring-bearer is about to heal, remove a burden instead.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Extraordinary Resilience", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ResponseRemovesABurdenInsteadOfTheRingBearerHealing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHealScenario();

		var resilience = scn.GetFreepsCard("resilience");
		var gaffer = scn.GetFreepsCard("gaffer");
		var frodo = scn.GetRingBearer();
		scn.MoveAlliesToTable(gaffer);
		scn.MoveCardsToHand(resilience);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		// Two burdens, so that "a burden" is told from every burden.
		scn.AddBurdens(2);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(gaffer));
		assertTrue(scn.FreepsActionAvailable(gaffer));

		// Frodo is the only Frodo or Sam, so the Gaffer's heal is his without a choice.
		scn.FreepsUseCardAction(gaffer);
		assertEquals(1, scn.GetWoundsOn(gaffer));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsPlayAvailable(resilience));

		scn.FreepsPlayCard(resilience);
		// The wound stays, a burden goes, and what the Gaffer paid stays paid.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(gaffer));
		assertInZone(Zone.DISCARD, resilience);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
