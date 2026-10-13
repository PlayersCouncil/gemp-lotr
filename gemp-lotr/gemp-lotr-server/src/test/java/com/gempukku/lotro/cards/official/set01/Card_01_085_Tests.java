package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_085_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_85");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetExertionScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spirit", "1_85");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: the Gandalf spotted
					put("sam", "1_311");        // Sam, Son of Hamfast: "Fellowship: Exert Sam to remove a burden."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrengthofSpiritStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Strength of Spirit
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: 
		 * Game Text: <b>Spell</b>.<br><b>Response:</b> If a companion is about to exert, spot Gandalf to place no token for that exertion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Strength of Spirit", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ResponseSpottingGandalfPlacesNoWoundForACompanionsExertion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetExertionScenario();

		var spirit = scn.GetFreepsCard("spirit");
		var gandalf = scn.GetFreepsCard("gandalf");
		// The companion who exerts is not Gandalf: "a companion" and the Gandalf spotted are two cards.
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(gandalf, sam);
		scn.MoveCardsToHand(spirit);

		scn.StartGame();
		scn.AddBurdens(2);

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetBurdens());

		// Sam exerts as the cost of his ability: the response comes before the wound is placed.
		scn.FreepsUseCardAction(sam);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(spirit));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetBurdens());

		scn.FreepsChooseOptionalTrigger(spirit);
		// No wound for the exertion, which still pays for the burden removed.
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.DISCARD, spirit);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
