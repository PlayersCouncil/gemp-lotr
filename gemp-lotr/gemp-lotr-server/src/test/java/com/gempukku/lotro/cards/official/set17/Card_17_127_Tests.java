package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_17_127_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "17_127");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSiteControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("intruder", "17_127");
					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandIntruderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: White Hand Intruder
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>Each time this minion wins a skirmish, if you do not control a site, you may take control of a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("White Hand Intruder", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithASiteThatCanBeControlledWinningASkirmishOffersItAndAYesTakesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var intruder = scn.GetShadowCard("intruder");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(intruder);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, intruder);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.PassCurrentPhaseActions();
		// 9 against 6: the Knight loses, and takes 2 wounds for damage +1.
		assertEquals(2, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(intruder));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteThatCanBeControlledWinningASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var intruder = scn.GetShadowCard("intruder");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 is the only site every marker has passed, and player 1 took it on player 2's turn: there is
		// nothing a yes could take (Q-0034).
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(intruder);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, intruder);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.FreepsControls(site1));

		scn.PassCurrentPhaseActions();
		// The Intruder has won, and the Shadow player controls no site.
		assertEquals(2, scn.GetWoundsOn(knight));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(intruder));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
	}
}
