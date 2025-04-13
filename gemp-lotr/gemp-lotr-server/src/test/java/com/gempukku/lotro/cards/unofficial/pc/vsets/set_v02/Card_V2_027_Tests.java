package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_027_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("legion", "102_27");
					put("runner", "1_178");

					put("gimli", "1_13");
					put("naith", "4_68");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void LegionofIsengardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Legion of Isengard
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 8
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 17
		 * Vitality: 4
		 * Site Number: 5
		 * Game Text: Damage +1.
		* 	While you control a battleground, this minion is damage +1.
		* 	While at a battleground, skirmish special abilities cannot be used during skirmishes involving this minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legion");

		assertEquals("Legion of Isengard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.hasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(17, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void LegionofIsengardIsDamagePlus1WhenControllingABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var legion = scn.GetShadowCard("legion");
		scn.ShadowMoveCardToHand(legion);

		var site1 = scn.GetFreepsSite(1);

		scn.StartGame();
		scn.SkipToSite(2);

		//cheating to ensure site 1 qualifies
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.siteNumber(1), null, Keyword.BATTLEGROUND));

		scn.ShadowMoveCharToTable(legion);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.hasKeyword(site1, Keyword.BATTLEGROUND));
		assertTrue(scn.hasKeyword(legion, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(legion, Keyword.DAMAGE));

		scn.ShadowTakeControlOfSite();
		assertTrue(scn.hasKeyword(site1, Keyword.BATTLEGROUND));
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.hasKeyword(legion, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(legion, Keyword.DAMAGE));
	}

	@Test
	public void LegionofIsengardPreventsSkirmishActionsDuringOwnSkrimishAtBattleground() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var legion = scn.GetShadowCard("legion");
		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCharToTable(legion, runner);

		var gimli = scn.GetFreepsCard("gimli");
		var naith = scn.GetFreepsCard("naith");
		scn.FreepsMoveCharToTable(gimli, naith);

		var site2 = scn.GetShadowSite(2);

		scn.StartGame();

		//cheating to ensure site 2 qualifies
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.siteNumber(2), null, Keyword.BATTLEGROUND));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, legion);
		scn.ShadowAssignToMinions(naith, runner);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.hasKeyword(site2, Keyword.BATTLEGROUND));
		assertTrue(scn.IsCharSkirmishing(legion));
		assertFalse(scn.FreepsActionAvailable(gimli));

		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(naith);

		assertTrue(scn.hasKeyword(site2, Keyword.BATTLEGROUND));
		assertFalse(scn.IsCharSkirmishing(legion));
		assertTrue(scn.FreepsActionAvailable(naith));
	}
}
