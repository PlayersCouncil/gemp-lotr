package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_222_Tests
{

	// The card's own block: the Shadow player lays each next site, and holds the others to replace one with.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nelya", "11_222");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: the ranger Pathfinder spots
					// Pathfinder: "Fellowship or Regroup: Spot a ranger to play the fellowship's next site
					// (replacing opponent's site if necessary)."
					put("pathfinder", "1_110");
				}},
				new HashMap<>()
				{{
					put("site1", "18_138");       // Sirannon Ruins: River, and no text
					put("glade", "11_239");       // Fangorn Glade: adds (1) for each Free Peoples weapon when moved to; there are none
					put("road", "11_236");        // East Road: each companion is twilight cost +2, and no companion is played
					// Abandoned Mine Shaft: adds a burden only when left in the regroup phase outside region 1
					put("shaft", "13_185");
					// Never laid in these tests, and never named: five copies of Expanding Marshland.
					put("site5", "11_238");
					put("site6", "11_238");
					put("site7", "11_238");
					put("site8", "11_238");
					put("site9", "11_238");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void UlaireNelyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Úlairë Nelya, Third of the Nine Riders
		 * Unique: true
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Race: Nazgûl
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 2
		 * Game Text: <b>Fierce</b>.
		 * 	<b>Shadow:</b> Exert Úlairë Nelya to replace the fellowship's current site with a site from your
		 * 	adventure deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nelya");

		assertEquals("Úlairë Nelya", card.getBlueprint().getTitle());
		assertEquals("Third of the Nine Riders", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(2, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityExertsHerToReplaceTheCurrentSiteWithOneOfHisOwnUntilSheIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");

		var ruins = scn.GetShadowSite("site1");
		var glade = scn.GetShadowSite("glade");
		var road = scn.GetShadowSite("road");
		var shaft = scn.GetShadowSite("shaft");
		var freepsGlade = scn.GetFreepsSite("glade");

		// The fellowship moves from the Free Peoples player's Sirannon Ruins to the Shadow player's Fangorn Glade.
		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(glade);
		scn.MoveMinionsToTable(nelya);
		scn.ShadowRefreshDecision();

		assertEquals(glade, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertInZone(Zone.ADVENTURE_DECK, ruins, road, shaft, freepsGlade);

		assertTrue(scn.ShadowActionAvailable(nelya));
		scn.ShadowUseCardAction(nelya);

		// The cost is paid, and he chooses one site from his own adventure deck: his copy of Sirannon Ruins is
		// there, while Fangorn Glade, which lies under the fellowship, is only in the other player's.
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertTrue(scn.ShadowHasCardChoiceAvailable(ruins, road, shaft));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(freepsGlade));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCardBPFromSelection(road);

		// East Road takes Fangorn Glade's place, and the Glade goes back to its owner's adventure deck.
		assertEquals(road, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_DECK, glade);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// No limit: she exerts again, and replaces East Road in turn.
		assertTrue(scn.ShadowActionAvailable(nelya));
		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCardBPFromSelection(shaft);

		assertEquals(2, scn.GetWoundsOn(nelya));
		assertEquals(shaft, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_DECK, road);

		// Vitality 3 with 2 wounds: exhausted, she cannot pay, and the ability is not offered.
		assertFalse(scn.ShadowActionAvailable(nelya));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayersOwnSiteReplacedGoesBackToHisAdventureDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(pathfinder);

		var nelya = scn.GetShadowCard("nelya");

		var glade = scn.GetShadowSite("glade");
		var shaft = scn.GetShadowSite("shaft");
		var freepsRoad = scn.GetFreepsSite("road");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(glade);

		// In the regroup phase the Free Peoples player lays his own East Road as site 3, and the fellowship moves
		// there: the site under it in the next Shadow phase is his.
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPlayCard(pathfinder);
		scn.FreepsChooseCardBPFromSelection(freepsRoad);
		assertEquals(freepsRoad, scn.GetSite(3));
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(freepsRoad, scn.GetCurrentSite());
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(nelya);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(nelya));
		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCardBPFromSelection(shaft);

		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(shaft, scn.GetCurrentSite());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_DECK, freepsRoad);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
