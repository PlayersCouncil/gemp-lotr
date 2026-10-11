package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_071_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "12_71");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A [men] minion of 1 vitality to be killed in a skirmish, and a [men] minion in hand to play.
	protected VirtualTableScenario GetKilledMinionScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("days", "12_71");
					put("bree", "11_90");
					put("corps", "11_73");
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LastDaysStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Last Days
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a [men] minion is killed or discarded from play (except during the regroup phase), you may play a [men] minion. Its twilight cost is -2, and it is <b>fierce</b> and strength +2 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Last Days", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// "Each time a [men] minion is killed or discarded from play".  A killed minion is not discarded (CR 4.2
	// "killed"; F-0444), so the definition has a `killed` trigger beside its `discarded` one: this holds the kill.
	// The discard, and one offer for each, are in mechanics/KilledIsNotDiscardedTests.
	@Test
	public void AMenMinionKilledInASkirmishLetsAMenMinionBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKilledMinionScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var days = scn.GetShadowCard("days");
		var bree = scn.GetShadowCard("bree");
		var corps = scn.GetShadowCard("corps");
		scn.MoveCardsToSupportArea(days);
		scn.MoveCardsToHand(corps);

		scn.StartGame();
		scn.MoveMinionsToTable(bree);
		scn.SetTwilight(6);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, bree);
		scn.FreepsResolveSkirmish(aragorn);

		// 6 set + 2 companions + 2 for the site
		assertEquals(10, scn.GetTwilight());
		// 8 against 4: Man of Bree is killed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, bree);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(days));
		scn.ShadowAcceptOptionalTrigger();

		// Corps of Harad is the only [men] minion in hand: 4 - 2, + 2 for roaming.
		assertInZone(Zone.SHADOW_CHARACTERS, corps);
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.HasKeyword(corps, Keyword.FIERCE));
		assertEquals(12, scn.GetStrength(corps));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void LastDaysTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(0, scn.GetTwilight());
	}
}
