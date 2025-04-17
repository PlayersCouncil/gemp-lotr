package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_031_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("trail", "102_31");
					put("ladder", "5_57");
					put("ram", "5_44");
					put("savage", "1_151");

					put("sam", "1_311");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void TrailofSavageryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Trail of Savagery
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each site you control gains <b>battleground</b>.
		 * Skirmish: Remove 2 [isengard] tokens to cancel a skirmish involving an Uruk-hai.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trail");

		assertEquals("Trail of Savagery", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TrailofSavageryMakesControlledSitesGainBattleground() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var trail = scn.GetShadowCard("trail");
		scn.ShadowMoveCardToDiscard(trail);
		//For inscrutible reasons, the site control function only works if there are 0 valid
		// actions for the shadow player to take.  Thus we can't have any cards in hand.
		scn.ShadowMoveCardToDiscard("ram", "ladder");

		var site1 = scn.GetFreepsSite(1);

		scn.StartGame();

		scn.SkipToSite(2);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowTakeControlOfSite();

		assertTrue(scn.ShadowControls(site1));
		assertFalse(scn.hasKeyword(site1, Keyword.BATTLEGROUND));
		scn.ShadowMoveCardToSupportArea(trail);
		assertTrue(scn.hasKeyword(site1, Keyword.BATTLEGROUND));
	}

	@Test
	public void TrailofSavageryRemoves2IsengardTokensToCancelAnUrukSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var trail = scn.GetShadowCard("trail");
		var ram = scn.GetShadowCard("ram");
		var ladder = scn.GetShadowCard("ladder");
		var savage = scn.GetShadowCard("savage");
		scn.ShadowMoveCardToSupportArea(trail, ladder, ram);
		scn.ShadowMoveCharToTable(savage);
		scn.AddTokensToCard(ram, 1);
		scn.AddTokensToCard(ladder, 1);

		var sam = scn.GetFreepsCard("sam");
		scn.FreepsMoveCharToTable(sam);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, savage);
		scn.FreepsResolveSkirmish(sam);

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(1, scn.GetCultureTokensOn(ram));
		assertEquals(1, scn.GetCultureTokensOn(ladder));
		assertTrue(scn.IsCharSkirmishing(savage));
		assertTrue(scn.ShadowActionAvailable(trail));

		scn.ShadowUseCardAction(trail);
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCard(ram);

		assertEquals(0, scn.GetCultureTokensOn(ram));
		assertEquals(0, scn.GetCultureTokensOn(ladder));
		assertFalse(scn.IsCharSkirmishing(savage));

	}
}
