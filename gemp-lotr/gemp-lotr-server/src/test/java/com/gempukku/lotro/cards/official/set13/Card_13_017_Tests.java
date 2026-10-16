package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_13_017_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "13_17");
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
					put("kindreds", "13_17");
					put("kindreds2", "13_17");
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
	public void KindredsEstrangedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Kindreds Estranged
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: You may remove from the game 4 other [elven] cards in your discard pile to play this event from your discard pile. Then remove this event from the game.<br>Make a minion skirmishing your Elf at a forest or river site strength -3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Kindreds Estranged", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void KindredsEstrangedTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}

	@Test
	public void AMinionSkirmishingAnAllyIsWeakenedOnlyWhileHisHomeSiteIsAForestOrRiver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var kindreds = scn.GetFreepsCard("kindreds");
		var kindreds2 = scn.GetFreepsCard("kindreds2");
		var golradir = scn.GetFreepsCard("golradir");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var follow = scn.GetFreepsCard("follow");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(golradir, celeborn);
		scn.MoveCardsToHand(follow, kindreds, kindreds2);

		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(troop, troop2);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals("Ford of Bruinen", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));

		scn.FreepsPlayCard(follow);
		scn.FreepsChooseCard(celeborn);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{golradir, troop},
				new PhysicalCardImpl[]{celeborn, troop2});

		// Golradir is at his home site, the river the fellowship is at: his minion, the only one, is 13 - 3.
		scn.FreepsResolveSkirmish(golradir);
		assertEquals(13, scn.GetStrength(troop));
		scn.FreepsPlayCard(kindreds);
		assertEquals(10, scn.GetStrength(troop));
		scn.PassSkirmishActions();

		// Celeborn's home site is not laid yet: he is at no site (CR 4.2 "ally"), and the event finds no minion.
		scn.FreepsResolveSkirmish(celeborn);
		assertTrue(scn.IsCharSkirmishingAgainst(celeborn, troop2));
		scn.FreepsPlayCard(kindreds2);
		assertEquals(13, scn.GetStrength(troop2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
