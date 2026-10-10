package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_133_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "13_133");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The card's own block: the Shadow player lays each next site, and holds others to replace it with.
	protected VirtualTableScenario GetReplacedSiteScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tactician", "13_133");
					put("guard", "5_83");           // Household Guard: a [rohan] Man; Valiant is its whole text
					// Úlairë Nelya, Third of the Nine Riders: "Shadow: Exert Úlairë Nelya to replace the
					// fellowship's current site with a site from your adventure deck."  Fierce, vitality 3.
					put("nelya", "11_222");
					put("marksman1", "1_176");      // Goblin Marksman: Archer is its whole text
					put("marksman2", "1_176");
					put("marksman3", "1_176");
				}},
				// site1 is Fangorn Glade, site2 Abandoned Mine Shaft, site5 Sirannon Ruins: none does anything
				// on the first move of a game or in an archery phase
				VirtualTableScenario.ShadowsSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void RiddermarkTacticianStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Riddermark Tactician
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 5
		 * Game Text: When you play this, you may reinforce a [rohan] token.<br>Each time a Shadow player replaces the fellowship's current site, you may spot another [rohan] Man to make, until the regroup phase, the fellowship archery total +2 and the minion archery total -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Riddermark Tactician", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
	}

	@Test
	public void AShadowPlayerReplacingTheFellowshipsCurrentSiteOffersTheArcheryChangeWithAnotherRohanManSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetReplacedSiteScenario();

		var tactician = scn.GetFreepsCard("tactician");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(tactician, guard);

		var nelya = scn.GetShadowCard("nelya");
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		var marksman3 = scn.GetShadowCard("marksman3");
		var shaft = scn.GetShadowSite("site2");
		var ruins = scn.GetShadowSite("site5");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shaft);
		scn.MoveMinionsToTable(nelya, marksman1, marksman2, marksman3);
		scn.ShadowRefreshDecision();

		assertEquals(shaft, scn.GetCurrentSite());
		// No companion is an archer, and three minions are.
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals(3, scn.GetShadowArcheryTotal());

		// The Shadow player's Nazgûl replaces Abandoned Mine Shaft, under the fellowship, with Sirannon Ruins.
		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCardBPFromSelection(ruins);
		assertEquals(ruins, scn.GetCurrentSite());
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals(3, scn.GetShadowArcheryTotal());

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tactician));
		scn.FreepsAcceptOptionalTrigger();
		// 0 + 2, and 3 - 2
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
