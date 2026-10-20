package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_028_Tests
{

	// The Fellowship path as it stands: Ettenmoors 1_331 at site 2 is a plains, neither river nor forest.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("voice", "3_28");
					put("arwen", "3_7");        // Arwen, Elven Rider: Elf companion, strength 6, vitality 3, no archer; her response is only for the Ring-bearer
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Buckleberry Ferry 1_330 as site 2: a river ("While only Hobbits are in the fellowship, there are no assignment
	// and skirmish phases at Buckleberry Ferry": Elves are in it here).
	protected VirtualTableScenario GetRiverScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("voice", "3_28");
					put("arwen", "3_7");        // Arwen, Elven Rider: Elf companion, strength 6, vitality 3, no archer; her response is only for the Ring-bearer
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: Elf companion, no archer; +2 only at sites 6 to 8
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Man companion; a maneuver ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_330");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Breeland Forest 1_329 as site 2: a forest ("Stealth events may not be played").
	protected VirtualTableScenario GetForestScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("voice", "3_28");
					put("arwen", "3_7");        // Arwen, Elven Rider: Elf companion, strength 6, vitality 3, no archer; her response is only for the Ring-bearer
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_329");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Rivendell Waterfall 1_342 as site 3: a forest and a sanctuary ("While you can spot a
	// ranger at Rivendell Waterfall, the move limit is +1 for this turn": no ranger is laid).
	protected VirtualTableScenario GetSanctuaryForestScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("voice", "3_28");
					put("arwen", "3_7");        // Arwen, Elven Rider: Elf companion, strength 6, vitality 3, no archer; her response is only for the Ring-bearer
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "1_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Galadriel, Bearer of Wisdom 9_14, an Elf who may bear The One Ring, as the Ring-bearer, at Breeland Forest.
	protected VirtualTableScenario GetFellowshipBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("voice", "3_28");
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_329");
				}},
				"9_14",
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Fellowship
		);
	}

	protected VirtualTableScenario GetMultipathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("voice", "3_28");
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_329");
				}},
				"9_14",
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Multipath
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

		var card = scn.GetFreepsCard("voice");

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

	@Test
	public void AnElfAtARiverExertsToCancelHerOwnSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRiverScenario();

		var voice = scn.GetFreepsCard("voice");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, haldir, aragorn);
		scn.MoveCardsToHand(voice);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		scn.FreepsAssignToMinions(arwen, runner);
		scn.FreepsResolveSkirmish(arwen);

		assertTrue(scn.IsCharSkirmishingAgainst(arwen, runner));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertTrue(scn.FreepsPlayAvailable(voice));
		scn.FreepsPlayCard(voice);

		// Any Elf at the river; not the Man or the Hobbit, though they are there too.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, haldir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(arwen);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(haldir));
		// Cancelled: Arwen's 6 would have killed the Runner (5, vitality 1).
		assertFalse(scn.IsCharSkirmishing(arwen));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.DISCARD, voice);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnElfAtAForestExertsToCancelHerOwnSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetForestScenario();

		var voice = scn.GetFreepsCard("voice");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(voice);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		scn.FreepsAssignToMinions(arwen, runner);
		scn.FreepsResolveSkirmish(arwen);

		assertTrue(scn.IsCharSkirmishingAgainst(arwen, runner));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsPlayAvailable(voice));
		// The only Elf: she exerts unasked.
		scn.FreepsPlayCard(voice);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertFalse(scn.IsCharSkirmishing(arwen));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnElfAtAForestThatIsAlsoASanctuaryExertsToCancelHerOwnSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSanctuaryForestScenario();

		var voice = scn.GetFreepsCard("voice");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(voice);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		assertEquals("Rivendell Waterfall", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		scn.FreepsAssignToMinions(arwen, runner);
		scn.FreepsResolveSkirmish(arwen);

		assertTrue(scn.IsCharSkirmishingAgainst(arwen, runner));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsPlayAvailable(voice));
		// The only Elf: she exerts unasked.
		scn.FreepsPlayCard(voice);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertFalse(scn.IsCharSkirmishing(arwen));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnElfWhoIsNotInTheSkirmishMayPayAndTheSkirmishGoesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRiverScenario();

		var voice = scn.GetFreepsCard("voice");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(arwen, haldir);
		scn.MoveCardsToHand(voice);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		// Haldir is assigned to no skirmish.
		scn.FreepsAssignToMinions(arwen, runner);
		scn.FreepsResolveSkirmish(arwen);

		assertTrue(scn.IsCharSkirmishingAgainst(arwen, runner));
		assertEquals(0, scn.GetWoundsOn(haldir));
		scn.FreepsPlayCard(voice);
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, haldir));
		scn.FreepsChooseCard(haldir);

		// "That Elf" is Haldir, who is in no skirmish: Arwen's goes on.
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, runner));
		assertInZone(Zone.DISCARD, voice);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedAtASiteThatIsNeitherARiverNorAForest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetFreepsCard("voice");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(voice);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, runner);
		scn.FreepsResolveSkirmish(arwen);

		// Ettenmoors is a plains.
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, runner));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(voice));
	}

	@Test
	public void CannotBePlayedWhenTheOnlyElfAtTheRiverIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRiverScenario();

		var voice = scn.GetFreepsCard("voice");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveCardsToHand(voice);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddWoundsToChar(arwen, 2);
		assertEquals(2, scn.GetWoundsOn(arwen));
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, runner);
		scn.FreepsResolveSkirmish(arwen);

		// Vitality 3 with 2 wounds: Arwen cannot exert; Aragorn could, but is no Elf.
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, runner));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(voice));
	}

	@Test
	public void InTheFellowshipBlockFormatItCancelsTheSkirmishOfAnElfWhoIsTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var voice = scn.GetFreepsCard("voice");
		var galadriel = scn.GetRingBearer();
		scn.MoveCardsToHand(voice);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(galadriel, runner);
		scn.FreepsResolveSkirmish(galadriel);

		assertTrue(scn.IsCharSkirmishingAgainst(galadriel, runner));
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertTrue(scn.FreepsPlayAvailable(voice));
		scn.FreepsPlayCard(voice);

		assertEquals(1, scn.GetWoundsOn(galadriel));
		// Cancelled: the Runner (5) would have beaten Galadriel (3, +1 from the Ring).
		assertFalse(scn.IsCharSkirmishing(galadriel));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InMultipathTheRingBearerPaysAndHerSkirmishIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMultipathScenario();

		var voice = scn.GetFreepsCard("voice");
		var galadriel = scn.GetRingBearer();
		scn.MoveCardsToHand(voice);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(galadriel, runner);
		scn.FreepsResolveSkirmish(galadriel);

		assertTrue(scn.IsCharSkirmishingAgainst(galadriel, runner));
		assertEquals(0, scn.GetWoundsOn(galadriel));
		// The exertion can be paid, so the event is played; the skirmish is the Ring-bearer's and goes on.
		assertTrue(scn.FreepsPlayAvailable(voice));
		scn.FreepsPlayCard(voice);

		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertTrue(scn.IsCharSkirmishingAgainst(galadriel, runner));
		assertInZone(Zone.DISCARD, voice);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
