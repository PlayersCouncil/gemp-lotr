package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "11_16");
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
					put("blade", "11_16");
					put("blade2", "11_16");
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
	public void BladeofLindonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Blade of Lindon
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 1
		 * Game Text: Bearer must be an Elf.<br>While bearer is at a forest or river site, each minion he or she is skirmishing is strength -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Blade of Lindon", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void BladeofLindonTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}

	@Test
	public void BorneByAnAllyItWeakensHisMinionOnlyWhileHisHomeSiteIsAForestOrRiver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var blade = scn.GetFreepsCard("blade");
		var blade2 = scn.GetFreepsCard("blade2");
		var golradir = scn.GetFreepsCard("golradir");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var follow = scn.GetFreepsCard("follow");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(golradir, celeborn);
		scn.AttachCardsTo(golradir, blade);
		scn.AttachCardsTo(celeborn, blade2);
		scn.MoveCardsToHand(follow);

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

		// Golradir is at his home site, the river the fellowship is at: his minion is 13 - 1.
		scn.FreepsResolveSkirmish(golradir);
		assertTrue(scn.IsCharSkirmishingAgainst(golradir, troop));
		assertEquals(12, scn.GetStrength(troop));
		scn.PassSkirmishActions();

		// Celeborn's home site is not laid yet: he is at no site (CR 4.2 "ally"), and his minion keeps its 13.
		scn.FreepsResolveSkirmish(celeborn);
		assertTrue(scn.IsCharSkirmishingAgainst(celeborn, troop2));
		assertEquals(13, scn.GetStrength(troop2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
