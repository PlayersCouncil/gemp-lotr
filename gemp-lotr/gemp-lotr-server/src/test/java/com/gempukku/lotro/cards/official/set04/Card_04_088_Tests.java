package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_088_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "4_88");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gandalf in a skirmish against two Uruk Savages, with two [gandalf] tokens on the condition.
	protected VirtualTableScenario GetTwoMinionsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rider", "4_88");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: vitality 4; his text is about his own strength
					put("savage1", "1_151");    // Uruk Savage: strength 5, vitality 3
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BeholdtheWhiteRiderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Behold the White Rider
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area.<br>Each time Gandalf wins a skirmish, you may place a [gandalf] token here.<br><b>Skirmish:</b> Wound a minion skirmishing Gandalf for each [gandalf] token here. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Behold the White Rider", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// "Wound a minion skirmishing Gandalf for each [gandalf] token here": with "for each" the wounds may go to
	// several minions or to one more than once (CR 4.2 "for each"; the shape of F-0224).  The definition chose
	// one minion and wounded it once for each token.
	// F-0458.
	@Test
	public void TheWoundsForEachTokenMayGoToDifferentMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoMinionsScenario();

		var rider = scn.GetFreepsCard("rider");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(rider);
		scn.AddTokensToCard(rider, 2);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, savage1);
		scn.ShadowAssignToMinions(gandalf, savage2);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.FreepsActionAvailable(rider));
		scn.FreepsUseCardAction(rider);

		// The first token's wound: either minion.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(savage1);
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(savage2));

		// The second token's wound: either minion again, and it goes to the other.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(savage2);
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(1, scn.GetWoundsOn(savage2));

		assertInZone(Zone.DISCARD, rider);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void BeholdtheWhiteRiderTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}
}
