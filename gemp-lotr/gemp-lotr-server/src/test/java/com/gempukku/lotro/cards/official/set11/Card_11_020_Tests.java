package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_020_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "11_20");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Ford of Bruinen 1_338 as site 3: a river and a sanctuary ("The twilight cost of the
	// first Nazgul played to Ford of Bruinen each turn is -5"), the home of Golradir (3F).  Celeborn's home is site 6,
	// Galadriel's Glade 1_351, no river and no forest, which is not laid while the fellowship is at site 3: he is at
	// no site then.  He takes part in skirmishes by One Whom Men Would Follow.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blessing", "11_20");
					put("blessing2", "11_20");
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: Elf ally, home 3F, strength 4, vitality 3; a skirmish ability used only on purpose
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: Elf ally, home 6F, strength 6, vitality 3; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion; a maneuver ability used only on purpose
					// One Whom Men Would Follow: "Exert Aragorn to spot an ally.  Until the regroup phase, that ally is
					// strength +2 and participates in archery fire and skirmishes."
					put("follow", "1_109");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("troop2", "1_177");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "1_338");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheLadysBlessingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: The Lady's Blessing
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make an Elf strength +2 (or +3 at a river site).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("The Lady's Blessing", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void TheLadysBlessingTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}

	@Test
	public void AnAllyIsPlus3OnlyWhileHisHomeSiteIsARiver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var blessing = scn.GetFreepsCard("blessing");
		var blessing2 = scn.GetFreepsCard("blessing2");
		var golradir = scn.GetFreepsCard("golradir");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(golradir, celeborn);
		scn.MoveCardsToHand(blessing, blessing2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals("Ford of Bruinen", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(4, scn.GetStrength(golradir));
		assertEquals(6, scn.GetStrength(celeborn));

		// Golradir is at his home site, the river the fellowship is at: 4 + 3.
		scn.FreepsPlayCard(blessing);
		scn.FreepsChooseCard(golradir);
		assertEquals(7, scn.GetStrength(golradir));

		// Celeborn's home site 6 is not laid yet: he is at no site (CR 4.2 "ally"), and is 6 + 2.
		scn.ShadowPass();
		scn.FreepsPlayCard(blessing2);
		scn.FreepsChooseCard(celeborn);
		assertEquals(8, scn.GetStrength(celeborn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
