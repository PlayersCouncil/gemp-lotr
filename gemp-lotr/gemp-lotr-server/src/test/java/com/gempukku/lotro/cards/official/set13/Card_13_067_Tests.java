package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_067_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "13_67");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability that is never used here
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally; a maneuver ability that needs initiative
					put("foes", "1_105");       // Foes of Mordor: [gondor] condition, support area; a maneuver ability
					// I Will Go: [gondor] condition, bearer must be a [gondor] Man; defender +1 with initiative
					put("go1", "7_105");
					put("go2", "7_105");
					put("promise", "2_112");    // A Promise: [shire] condition, support area; needs Sam to do anything
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GuardoftheWhiteTreeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Guard of the White Tree
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 5
		 * Game Text: <b>Knight</b>.<br><b>Skirmish:</b> Discard a [gondor] condition from your support area to make this companion strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Guard of the White Tree", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.KNIGHT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void GuardoftheWhiteTreeTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(3, scn.GetTwilight());
	}

	@Test
	public void SkirmishAbilityMayDiscardAGondorConditionAnAllyBearsAndNotOneACompanionBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("card");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var foes = scn.GetFreepsCard("foes");
		var go1 = scn.GetFreepsCard("go1");
		var go2 = scn.GetFreepsCard("go2");
		var promise = scn.GetFreepsCard("promise");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(guard, aragorn);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(foes, promise);
		// In your support area: a [gondor] condition lying there, and one an ally there bears.
		scn.AttachCardsTo(merchant, go1);
		// Not in it: the same condition on a companion.  In it and not [gondor]: A Promise.
		scn.AttachCardsTo(aragorn, go2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, runner);

		assertEquals(7, scn.GetStrength(guard));
		assertTrue(scn.FreepsActionAvailable(guard));
		scn.FreepsUseCardAction(guard);
		assertTrue(scn.FreepsHasCardChoiceAvailable(foes, go1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(go2, promise));
		scn.FreepsChooseCard(go1);

		assertInDiscard(go1);
		assertEquals(9, scn.GetStrength(guard));
		assertInZone(Zone.SUPPORT, foes, promise);
		assertAttachedTo(go2, aragorn);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
