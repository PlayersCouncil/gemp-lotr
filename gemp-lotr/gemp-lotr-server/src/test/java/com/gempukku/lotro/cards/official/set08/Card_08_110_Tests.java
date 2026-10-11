package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_110_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "8_110");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRegroupScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foothills", "8_110");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is not Ring-bound
					put("guard", "1_7");        // Dwarf Guard, Dwarven Armor, Gimli: three cards for the hand
					put("armor", "1_8");
					put("gimli", "1_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgaiFoothillsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Morgai Foothills
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When the fellowship moves from site 4[K], remove (1) for each Ring-bound companion you spot.<br><b>Regroup:</b> Discard a card from hand for each Ring-bound companion you spot. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Morgai Foothills", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// F-0418 (Ketura, 2026-10-11: "yeah clearly a bug"): "Regroup: Discard a card from hand for each Ring-bound
	// companion you spot.  Discard this condition."  The ability discarded the condition and no card from hand.
	@Test
	public void TheRegroupAbilityDiscardsACardFromHandForTheOneRingBoundCompanionAndThenTheCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var foothills = scn.GetFreepsCard("foothills");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(foothills);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(guard, armor, gimli);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// Frodo is the one Ring-bound companion; Aragorn is not counted.  Three cards in hand.
		assertEquals(3, scn.GetFreepsHandCount());
		assertInZone(Zone.SUPPORT, foothills);
		assertTrue(scn.FreepsActionAvailable(foothills));
		scn.FreepsUseCardAction(foothills);

		// One card for the one Ring-bound companion, of the player's choosing among the three.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, armor, gimli));
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(armor);

		assertInDiscard(armor);
		assertInHand(guard, gimli);
		assertEquals(2, scn.GetFreepsHandCount());
		assertInDiscard(foothills);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void MorgaiFoothillsTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(0, scn.GetTwilight());
	}
}
