package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_112_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "3_112");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("seek", "3_112");
					// Merry, Friend to Sam: a [shire] companion, as the Ring-bearer is; strength 3, vitality 4.  His
					// ability is for when he is not assigned to a skirmish.
					put("merry", "1_302");
					// Uruk Savage: strength 5; Damage +1 is its whole text.  It beats Merry without overwhelming him.
					put("savage", "1_151");
					put("raiders", "1_158");    // Uruk-hai Raiding Party: Damage +1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SeekandHideStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Seek and Hide
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Stealth</b>.<br><b>Skirmish:</b> Spot 2 [shire] companions to make a minion lose all <b>damage</b> bonuses.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Seek and Hide", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.STEALTH));
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesTheChosenMinionLoseItsDamageBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var seek = scn.GetFreepsCard("seek");
		var merry = scn.GetFreepsCard("merry");
		var savage = scn.GetShadowCard("savage");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(seek);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(merry);

		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(raiders, Keyword.DAMAGE));
		// 2 companions + 2 for the site
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(seek));

		scn.FreepsPlayCard(seek);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, raiders));
		scn.FreepsChooseCard(savage);
		assertEquals(5, scn.GetTwilight());
		assertInDiscard(seek);
		assertFalse(scn.HasKeyword(savage, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(raiders, Keyword.DAMAGE));

		// Merry, 3 against 5, loses: the one wound of the loss, and no second for damage +1.
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
