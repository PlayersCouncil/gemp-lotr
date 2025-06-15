
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_036_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
                new HashMap<>() {{
                    put("vile", "101_36");
                    put("spear", "1_182");

                    put("tent1", "2_58");
                    put("tent2", "2_58");
                    put("tent3", "2_66");
                    put("tent4", "2_66");

                    put("watcher", "2_73");

                }},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VileTentacleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Title: Vile Tentacle
		 * Side: Shadow
		 * Culture: moria
		 * Twilight Cost: 3
		 * Type: minion
		 * Subtype: Creature
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: Tentacle. This minion may not bear possessions and is strength -4 while not at a marsh.
		 * Shadow: Discard a [moria] card from hand to make the current site gain <b>marsh</b> until the regroup phase.
		 */

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl vile = scn.GetFreepsCard("vile");

		assertFalse(vile.getBlueprint().isUnique());
		assertTrue(scn.HasKeyword(vile, Keyword.TENTACLE)); // test for keywords as needed
		assertEquals(2, vile.getBlueprint().getTwilightCost());
		assertEquals(7, vile.getBlueprint().getStrength());
		assertEquals(2, vile.getBlueprint().getVitality());
		assertEquals(4, vile.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies
		assertEquals(CardType.MINION, vile.getBlueprint().getCardType());
		assertEquals(Race.CREATURE, vile.getBlueprint().getRace());
		assertEquals(Culture.MORIA, vile.getBlueprint().getCulture());
		assertEquals(Side.SHADOW, vile.getBlueprint().getSide());
	}

	@Test
	public void VileTentacleCannotBearPossessions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl vile = scn.GetShadowCard("vile");
		PhysicalCardImpl spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(vile, spear);

		scn.StartGame();
		scn.SetTwilight(3);
        scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.siteNumber(2), null, Keyword.MARSH));
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(vile);

		assertFalse(scn.ShadowActionAvailable("Goblin Spear"));
	}

	@Test
	public void VileTentacleDoesNotSelfDiscardAtAMarsh() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl vile = scn.GetShadowCard("vile");
		scn.MoveCardsToHand(vile);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(vile);

		assertEquals(0, scn.GetShadowDiscardCount());
	}

	@Test
	public void VileTentacleHandDiscardToAddMarsh() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl vile = scn.GetShadowCard("vile");
		PhysicalCardImpl spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(vile, spear);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(vile);

		assertTrue(scn.ShadowActionAvailable("Vile Tentacle"));

		assertEquals(3, scn.GetStrength(vile));
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(1, scn.GetShadowHandCount());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		scn.ShadowUseCardAction(vile);

		assertEquals(7, scn.GetStrength(vile));
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
	}

}
