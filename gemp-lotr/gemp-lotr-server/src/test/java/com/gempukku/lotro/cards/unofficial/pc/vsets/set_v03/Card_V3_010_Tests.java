package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_010_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("broken", "103_10");
					put("gandalf", "1_72");
					put("staff", "2_22");

					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void YourStaffisBrokenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Your Staff is Broken
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: At the start of each Assignment phase, you may exert Gandalf bearing an artifact to discard a hindered Shadow card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("broken");

		assertEquals("Your Staff is Broken", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void YourStaffDoesNotTriggerIfOnlyArtifactOnGandalfIsHindered() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var broken = scn.GetFreepsCard("broken");

		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");

		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToSupportArea(broken);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.HinderCard(staff);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
	}
}
