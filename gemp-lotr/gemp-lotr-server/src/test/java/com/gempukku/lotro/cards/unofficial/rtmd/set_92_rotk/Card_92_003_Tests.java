package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_92_003_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Aragorn, Ranger of the North: ranger companion (needed for Pathfinder)
		put("aragorn", "1_89");
		// Pathfinder: Fellowship/Regroup event, spot ranger, play next site from adventure deck
		put("pathfinder", "1_110");
		// Ritual Oath of Enmity: Shadow Dunland artifact, support area
		// Shadow ability: spot Dunland token here to take control of 2 sites, discard this
		put("oath", "102_5");
		// Runner needed so shadow phase doesn't skip
		put("runner", "1_178");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_3", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_3
		 * Type: MetaSite
		 * Game Text: All sites gain underground.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_3", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void CurrentSiteIsUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_3: "All sites gain underground."
		// Site 1 (Bag End) does not natively have underground.

		var scn = GetFreepsScenario();

		scn.StartGame();

		var site1 = scn.GetCurrentSite();
		assertTrue(scn.HasKeyword(site1, Keyword.UNDERGROUND));
	}

	@Test
	public void PastSiteRetainsUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		// After moving past site 1, it should still have underground.

		var scn = GetFreepsScenario();

		scn.StartGame();
		scn.FreepsPass(); // move to site 2

		var site1 = scn.GetSite(1);
		assertTrue(scn.HasKeyword(site1, Keyword.UNDERGROUND));

		var site2 = scn.GetCurrentSite();
		assertTrue(scn.HasKeyword(site2, Keyword.UNDERGROUND));
	}

	@Test
	public void PlayedButNotMovedToSiteIsUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_3: Pathfinder plays the next site from the adventure deck before moving.
		// That site should have underground even before the fellowship moves there.

		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(pathfinder);

		scn.StartGame();

		// We're at site 1. Site 2 is in the adventure deck, not yet in play.
		assertNull(scn.GetSite(2));

		// Play Pathfinder — plays site 2 from adventure deck
		scn.FreepsPlayCard(pathfinder);

		// Site 2 should now be in play but fellowship hasn't moved yet
		var site2 = scn.GetSite(2);
		assertNotNull(site2);
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(site2, Keyword.UNDERGROUND));
	}

	@Test
	public void ControlledSiteIsUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_3: A site controlled by the shadow player should also have underground.

		var scn = GetFreepsScenario();

		var oath = scn.GetShadowCard("oath");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToSupportArea(oath);
		scn.AddTokensToCard(oath, 1);

		scn.StartGame();

		// Need to get past site 1 with both players so it can be controlled.
		scn.SkipToSite(3);

		// Now at site 3, fellowship phase.
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass(); // move to site 4

		// Shadow phase: use Oath's ability to take control of 2 sites
		scn.ShadowUseCardAction(oath);

		// Site 1 should now be controlled and still have underground
		var site1 = scn.GetSite(1);
		assertNotNull(site1);
		assertTrue(scn.HasKeyword(site1, Keyword.UNDERGROUND));
	}
}
