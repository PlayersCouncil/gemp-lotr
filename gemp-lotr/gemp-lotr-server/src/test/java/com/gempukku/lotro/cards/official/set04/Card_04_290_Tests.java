package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_290_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "4_290");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A wounded [rohan] ally and a wounded [shire] ally, with a villager in hand to pay with.
	protected VirtualTableScenario GetTwoWoundedAlliesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("supplies", "4_290");
					put("guma", "4_277");
					put("hlafwine", "4_281");
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SuppliesoftheMarkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Supplies of the Mark
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area.<br><b>Fellowship:</b> Add (2) and place a villager from hand beneath your draw deck to heal a [rohan] ally.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Supplies of the Mark", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// "to heal a [rohan] ally": the definition healed any ally.
	@Test
	public void OnlyARohanAllyIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoWoundedAlliesScenario();

		var supplies = scn.GetFreepsCard("supplies");
		var guma = scn.GetFreepsCard("guma");
		var hlafwine = scn.GetFreepsCard("hlafwine");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCardsToSupportArea(supplies);
		scn.MoveAlliesToTable(guma, bounder);
		scn.MoveCardsToHand(hlafwine);
		scn.AddWoundsToChar(guma, 1);
		scn.AddWoundsToChar(bounder, 1);

		scn.StartGame();
		assertTrue(scn.FreepsActionAvailable(supplies));
		scn.FreepsUseCardAction(supplies);

		// Hlafwine is the only villager in hand and Guma the only [rohan] ally: nothing is asked.
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.DECK, hlafwine);
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void SuppliesoftheMarkTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(0, scn.GetTwilight());
	}
}
