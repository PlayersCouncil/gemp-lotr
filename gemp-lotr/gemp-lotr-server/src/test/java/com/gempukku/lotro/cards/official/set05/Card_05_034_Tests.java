package com.gempukku.lotro.cards.official.set05;

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

import static org.junit.Assert.*;

public class Card_05_034_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "5_34");
					put("wall", "5_33");        // City Wall: a [gondor] fortification, a Free Peoples condition that a minion bears
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					put("scimitar1", "1_180");  // Goblin Scimitar: a [moria] possession, a Shadow card a minion bears
					put("scimitar2", "1_180");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FallBackStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Fall Back
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Discard all Shadow cards borne by a minion bearing a [gondor] fortification.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Fall Back", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void FallBackTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(0, scn.GetTwilight());
	}
	@Test
	public void AHinderedShadowCardBorneByThatMinionIsDiscardedWithTheOthers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		var wall = scn.GetFreepsCard("wall");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(card);

		var troop = scn.GetShadowCard("troop");
		var hindered = scn.GetShadowCard("scimitar1");
		var unhindered = scn.GetShadowCard("scimitar2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, wall, hindered, unhindered);
		// A hindered card has no type or culture, and is a Shadow card still (CR 5.0 "hinder"; Ketura on Q-0098).
		scn.HinderCard(hindered);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, troop);

		assertTrue(scn.IsHindered(hindered));
		assertTrue(scn.FreepsPlayAvailable(card));
		scn.FreepsPlayCard(card);

		// Both Shadow cards the minion bore are discarded, the hindered one with the other; the Free Peoples
		// fortification stays on it.
		assertEquals(Zone.DISCARD, hindered.getZone());
		assertEquals(Zone.DISCARD, unhindered.getZone());
		assertEquals(Zone.ATTACHED, wall.getZone());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
