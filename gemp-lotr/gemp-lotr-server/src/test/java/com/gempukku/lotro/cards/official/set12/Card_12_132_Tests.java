package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_12_132_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fury", "12_132");
					put("merry", "4_310");

					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SuddenFuryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Sudden Fury
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a [shire] companion loses a skirmish, add a [shire] token here.
		 * <b>Skirmish:</b> Remove a [shire] token from here to make a [shire] companion strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fury");

		assertEquals("Sudden Fury", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SuddenFuryAddsATokenWhenAHobbitLosesAStandardSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToSupportArea(fury);
		scn.MoveCompanionsToTable(merry);

		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(merry, savage);

		assertEquals(0, scn.GetCultureTokensOn(fury));
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetCultureTokensOn(fury));
	}

	/**
	 * Legacy Ruling #2 decided that Sudden Fury (and other such cards that care about skirmish losers)
	 * does indeed trigger when the final character is removed from a skirmish.  Characters removed before
	 * the final removed one remain counting as "neither winning nor losing".
	 *
	 * <a href="https://wiki.lotrtcgpc.net/wiki/Legacy_Ruling_2">Legacy Ruling #2 on the wiki</a>
	 */
	@Test
	public void SuddenFuryAddsATokenWhenAHobbitIsTheLastFreepsSkirmisherAndIsRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToSupportArea(fury);
		scn.MoveCompanionsToTable(merry);

		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(merry, savage);

		assertEquals(0, scn.GetCultureTokensOn(fury));
		scn.FreepsUseCardAction(merry);
		scn.ShadowChooseNo();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetCultureTokensOn(fury));
	}

	@Test
	public void SuddenFuryDoesNotAddTokenWhenHobbitRemovedBeforeSkirmishBegins() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		scn.MoveCardsToHand(fury);

		scn.StartGame();
		scn.FreepsPlayCard(fury);

		assertEquals(2, scn.GetTwilight());
	}
}
