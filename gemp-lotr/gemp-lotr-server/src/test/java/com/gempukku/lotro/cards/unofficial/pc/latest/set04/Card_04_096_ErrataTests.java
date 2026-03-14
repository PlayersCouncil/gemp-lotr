package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_04_096_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tongue", "54_96");
					put("gandalf", "1_364");  // Gandalf, The Grey Wizard (STR 7, VIT 4, signet: Gandalf)
					put("runner", "1_178");   // Goblin Runner
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KeepYourForkedTongueStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Keep Your Forked Tongue
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Spot Gandalf and 3 twilight tokens to make a companion
		 *  who has the Gandalf signet unable to take wounds.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tongue");

		assertEquals("Keep Your Forked Tongue", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void KeepYourForkedTonguePreventsWoundsToGandalfSignetCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tongue = scn.GetFreepsCard("tongue");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(tongue);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gandalf, runner);

		// There should be at least 3 twilight from Gandalf's cost (4) + site
		// Play Keep Your Forked Tongue
		scn.FreepsPlayCard(tongue);
		// Gandalf (only companion with Gandalf signet) auto-selected

		// Gandalf should now be unable to take wounds
		// Resolve skirmish -- Gandalf should take no wounds
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(gandalf));
	}
}
