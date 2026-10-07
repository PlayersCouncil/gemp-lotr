package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_339_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "4_339");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers-block path with this site as site 3.
	protected VirtualTableScenario GetSiteScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eomer", "4_267");      // Eomer, Third Marshal of Riddermark: a [rohan] Man, vitality 3
					put("mount", "4_287");      // Rider's Mount: a [rohan] mount, cost 1; bearer must be a [rohan] Man
					put("mount2", "4_287");
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");
					put("site2", "4_331");
					put("site3", "4_339");
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StablesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Stables
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 0
		 * Type: Sanctuary
		 * Subtype: 
		 * Site Number: 3T
		 * Game Text: <b>Sanctuary</b>. <b>Fellowship:</b> Exert a [rohan] Man to play a [rohan] mount from your draw deck (limit once per turn).
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		//var card = scn.GetFreepsSite(3);
		var card = scn.GetFreepsCard("card");

		assertEquals("Stables", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void StablesTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(0, scn.GetTwilight());
	}

	@Test
	public void FellowshipAbilityAtItsLimitStillExertsAManAndPlaysNoMount() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetSiteScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		scn.MoveCompanionsToTable(eomer);

		scn.StartGame();
		scn.SkipToSite(3);
		// The turns played on the way drew the deck into hand.
		scn.MoveCardsToTopOfDeck(mount, mount2);

		var site = scn.GetCurrentSite();
		assertEquals("Stables", site.getBlueprint().getTitle());

		assertEquals("false", scn.FreepsActionNoEffect(site));
		scn.FreepsUseCardAction(site);
		scn.DismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(mount);
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertEquals(1, scn.GetAttachedCards(eomer).size());

		// Limit once per turn: Eomer can still exert, so the ability is still offered, flagged as doing
		// nothing (Ketura on Q-0041).
		assertTrue(scn.FreepsActionAvailable(site));
		assertEquals("limit", scn.FreepsActionNoEffect(site));

		// Used anyway: Eomer exerts, and the draw deck (which still holds a mount) is not looked through.
		scn.FreepsUseCardAction(site);
		assertEquals(2, scn.GetWoundsOn(eomer));
		assertEquals(1, scn.GetAttachedCards(eomer).size());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
