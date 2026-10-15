package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_156_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "11_156");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path with Westemnet Hills 4_344 as site 4: a plains, and the home of Guma (4T).  Site 3, the home
	// of the Rohirrim Herdsman (3T), is Barrows of Edoras 4_337, a sanctuary and no plains.  Moving to Westemnet
	// Hills heals each mounted ally; nobody here is wounded.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mount", "11_156");
					put("mount2", "11_156");
					put("mount3", "11_156");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion; valiant and nothing else
					put("guma", "4_277");       // Guma: [rohan] Man ally, home 4T; a fellowship ability used only on purpose
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally, home 3T; a regroup ability used only on purpose
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site4", "4_344");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohirrimMountStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Rohirrim Mount
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Game Text: Bearer must be a [rohan] Man.<br>At the start of each skirmish involving bearer, each minion skirmishing bearer must exert.<br>While bearer is at a plains site, he or she is <b>defender +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Rohirrim Mount", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void RohirrimMountTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}

	@Test
	public void BorneByAnAllyItIsDefenderPlus1OnlyWhileHisHomeSiteIsAPlains() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		var mount3 = scn.GetFreepsCard("mount3");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma, herdsman);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(guma, mount2);
		scn.AttachCardsTo(herdsman, mount3);

		scn.StartGame();
		scn.SkipToSite(4);
		assertEquals("Westemnet Hills", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertFalse(scn.HasKeyword(scn.GetSite(3), Keyword.PLAINS));

		// The companion is with the fellowship, and Guma at his home site, the plains it is at: both defender +1.
		// The Herdsman is at his home site, Barrows of Edoras, which is no plains (CR 4.2 "ally").
		assertTrue(scn.HasKeyword(guard, Keyword.DEFENDER));
		assertTrue(scn.HasKeyword(guma, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(herdsman, Keyword.DEFENDER));
	}
}
