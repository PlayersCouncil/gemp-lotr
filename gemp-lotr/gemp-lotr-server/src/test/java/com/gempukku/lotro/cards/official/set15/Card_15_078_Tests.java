package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_078_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "15_78");
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
					put("hillman", "15_78");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] companion with no text
					put("guard", "1_7");        // Dwarf Guard: a [dwarven] companion; with Frodo, 3 Free Peoples cultures
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EngrossedHillmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Engrossed Hillman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: At the start of the maneuver phase, if you can spot another [men] Man and 8 characters (or if you can spot 3 Free Peoples cultures), you may take control of a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Engrossed Hillman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithASiteThatCanBeControlledTheStartOfTheManeuverPhaseOffersItAndAYesTakesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var hillman = scn.GetShadowCard("hillman");
		var knight = scn.GetFreepsCard("knight");
		var guard = scn.GetFreepsCard("guard");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight, guard);
		scn.MoveMinionsToTable(hillman);

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hillman));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoSiteThatCanBeControlledTheStartOfTheManeuverPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var hillman = scn.GetShadowCard("hillman");
		var knight = scn.GetFreepsCard("knight");
		var guard = scn.GetFreepsCard("guard");

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 is the only site every marker has passed, and player 1 took it on player 2's turn: there is
		// nothing a yes could take (Q-0034).
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight, guard);
		scn.MoveMinionsToTable(hillman);

		assertTrue(scn.FreepsControls(site1));

		// The Hillman is in play, and [shire], [gondor] and [dwarven] can be spotted.
		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hillman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
	}
}
