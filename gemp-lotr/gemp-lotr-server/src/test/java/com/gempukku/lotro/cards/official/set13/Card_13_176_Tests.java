package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_176_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "13_176");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// An [uruk-hai] lurker and an [orc] lurker, with an [uruk-hai] hand weapon in the Shadow player's discard pile.
	protected VirtualTableScenario GetTwoLurkersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("machine", "13_176");
					put("crusher", "12_140");
					put("emboldened", "11_119");
					put("spear", "17_117");
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WarMachineStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: War Machine
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert your [uruk-hai] lurker minion to play an [uruk-hai] hand weapon from your discard pile on your [uruk-hai] minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("War Machine", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// "Exert your [uruk-hai] lurker minion": the definition took any lurker minion of the player's.
	@Test
	public void TheLurkerExertedIsAnUrukHaiCultureOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoLurkersScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var machine = scn.GetShadowCard("machine");
		var crusher = scn.GetShadowCard("crusher");
		var emboldened = scn.GetShadowCard("emboldened");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(machine);
		scn.MoveCardsToDiscard(spear);

		scn.StartGame();
		scn.MoveMinionsToTable(crusher, emboldened);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, crusher);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// 2 companions + 2 for the site
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(machine));
		scn.ShadowPlayCard(machine);

		// Crushing Uruk is the only [uruk-hai] lurker: he exerts with nothing asked, and Emboldened Orc, an [orc]
		// lurker, is left alone.  One weapon in the discard pile and one [uruk-hai] minion to bear it.
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(crusher));
		assertEquals(0, scn.GetWoundsOn(emboldened));
		assertAttachedTo(spear, crusher);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void WarMachineTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}
}
