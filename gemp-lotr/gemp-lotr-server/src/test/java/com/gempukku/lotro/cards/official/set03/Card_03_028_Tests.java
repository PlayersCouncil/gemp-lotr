package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_028_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "3_28");
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
					put("voice", "3_28");
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
	public void VoiceofNimrodelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Voice of Nimrodel
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Tale</b>.<br><b>Skirmish:</b> Exert an Elf at a river or forest to cancel a skirmish involving that Elf.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Voice of Nimrodel", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void VoiceofNimrodelTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}

	@Test
	public void AnAllyAtHisHomeSiteARiverCanBeExertedToCancelHisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var voice = scn.GetFreepsCard("voice");
		var golradir = scn.GetFreepsCard("golradir");
		scn.MoveAlliesToTable(golradir);
		scn.MoveCardsToHand(voice);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals("Ford of Bruinen", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		scn.FreepsAssignToMinions(golradir, troop);
		scn.FreepsResolveSkirmish(golradir);
		assertTrue(scn.IsCharSkirmishingAgainst(golradir, troop));
		assertEquals(0, scn.GetWoundsOn(golradir));

		// Golradir is at his home site, the river the fellowship is at; the only Elf, exerted unasked.
		assertTrue(scn.FreepsPlayAvailable(voice));
		scn.FreepsPlayCard(voice);
		assertEquals(1, scn.GetWoundsOn(golradir));
		assertFalse(scn.IsCharSkirmishing(golradir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyWithNoHomeSiteLaidIsAtNoRiverAndCannotBeExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var voice = scn.GetFreepsCard("voice");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var follow = scn.GetFreepsCard("follow");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(follow, voice);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		scn.FreepsPlayCard(follow);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(celeborn, troop);
		scn.FreepsResolveSkirmish(celeborn);
		assertTrue(scn.IsCharSkirmishingAgainst(celeborn, troop));

		// The only Elf is Celeborn, whose home site 6 is not laid: he is at no site (CR 4.2 "ally"), so not at a
		// river, though the fellowship is.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(voice));
	}
}
