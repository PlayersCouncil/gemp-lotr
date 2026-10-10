package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_207_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_207");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetBurdenScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("breath", "1_207");
					put("sam", "1_311");        // Sam, Son of Hamfast: "Fellowship: Exert Sam to remove a burden."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BlackBreathStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Black Breath
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area.<br><b>Skirmish:</b> Transfer this condition from your support area to a character skirmishing a Nazgûl. Burdens and wounds may not be removed from bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Black Breath", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NoBurdenIsRemovedWhileItIsOnTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBurdenScenario();

		var breath = scn.GetShadowCard("breath");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, breath);

		scn.StartGame();
		scn.AddBurdens(2);

		assertAttachedTo(breath, frodo);
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));
		// An ability with a cost is offered though its effect will do nothing.
		assertTrue(scn.FreepsActionAvailable(sam));

		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ABurdenIsRemovedWhileItIsOnACompanionWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBurdenScenario();

		var breath = scn.GetShadowCard("breath");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		// The burdens are the Ring-bearer's, and Sam is not he: on Sam it keeps no burden where it is.  Nor
		// does it stop him exerting, which adds a wound and removes none.
		scn.AttachCardsTo(sam, breath);

		scn.StartGame();
		scn.AddBurdens(2);

		assertAttachedTo(breath, sam);
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsActionAvailable(sam));

		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
