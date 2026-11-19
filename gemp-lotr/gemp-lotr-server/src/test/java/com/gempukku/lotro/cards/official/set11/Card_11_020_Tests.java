package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_020_Tests
{

	// The Fellowship path with Ford of Bruinen 1_338 (River, Sanctuary) as site 3, Erestor's home. Its text discounts
	// the first Nazgul played there, and none is. The skirmishes are fought at site 4, Balin's Tomb 1_343
	// (Underground, no river), whose maneuver ability wants a tale and is never used.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blessing", "11_20");
					put("blessing2", "11_20");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, strength 6; +3 only against a Nazgul
					put("lorienelf", "1_53");   // Lorien Elf: Elf companion, strength 4, no text but a requirement to play
					put("erestor", "3_14");     // Erestor, Chief Advisor to Elrond: Elf ally, home 3F, strength 5; a response to [sauron] wounds only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability never used here
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its play trigger never fires, as it is cheated in
					put("runner2", "1_178");
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

	// Bag End, then Buckleberry Ferry 1_330 (River) at site 2, where the skirmishes are fought: it skips them only while
	// every companion is a Hobbit, and Arwen is not. Ford of Bruinen is site 3, not yet laid while the fellowship is at
	// site 2, so Erestor is at no site there.
	protected VirtualTableScenario GetRiverScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blessing", "11_20");
					put("blessing2", "11_20");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, strength 6; +3 only against a Nazgul
					put("erestor", "3_14");     // Erestor, Chief Advisor to Elrond: Elf ally, home 3F, strength 5; a response to [sauron] wounds only
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its play trigger never fires, as it is cheated in
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site1", "1_319");
					put("site2", "1_330");
					put("site3", "1_338");
				}},
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

		var card = scn.GetFreepsCard("blessing");

		assertEquals("The Lady's Blessing", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAnElfStrengthPlus2AwayFromARiverAndPlus3ForAnAllyAtHisHomeRiverSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blessing = scn.GetFreepsCard("blessing");
		var blessing2 = scn.GetFreepsCard("blessing2");
		var arwen = scn.GetFreepsCard("arwen");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var erestor = scn.GetFreepsCard("erestor");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, lorienelf);
		scn.MoveAlliesToTable(erestor);
		scn.MoveCardsToHand(blessing, blessing2);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, runner}, new PhysicalCardImpl[]{lorienelf, runner2});
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(4, scn.GetStrength(lorienelf));
		assertEquals(5, scn.GetStrength(erestor));
		assertTrue(scn.FreepsPlayAvailable(blessing));
		scn.FreepsPlayCard(blessing);
		// Any Elf: the one skirmishing, one whose skirmish is still to come, and an ally in no skirmish. Frodo is no Elf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, lorienelf, erestor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(arwen);
		// Arwen is at Balin's Tomb, no river: 6 + 2.
		assertEquals(8, scn.GetStrength(arwen));
		assertInDiscard(blessing);

		scn.ShadowPass();
		scn.FreepsPlayCard(blessing2);
		scn.FreepsChooseCard(erestor);
		// Erestor is at his home site, Ford of Bruinen, a river, though the fellowship is at site 4: 5 + 3.
		assertEquals(8, scn.GetStrength(erestor));
		assertEquals(8, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheBonusLastsTheSkirmishItWasPlayedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blessing = scn.GetFreepsCard("blessing");
		var arwen = scn.GetFreepsCard("arwen");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		scn.MoveCompanionsToTable(arwen, lorienelf);
		scn.MoveCardsToHand(blessing);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, runner}, new PhysicalCardImpl[]{lorienelf, runner2});
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(4, scn.GetStrength(lorienelf));
		scn.FreepsPlayCard(blessing);
		scn.FreepsChooseCard(lorienelf);
		assertEquals(6, scn.GetStrength(lorienelf));
		scn.ShadowPass();
		// Still in force at the next decision of Arwen's skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(lorienelf));

		scn.FreepsPass();
		scn.ShadowPass();
		// Arwen, 6 against 5, wins; on to the Lorien Elf's own skirmish, a phase of its own.
		scn.FreepsResolveSkirmish(lorienelf);
		assertEquals(4, scn.GetStrength(lorienelf));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blessing = scn.GetFreepsCard("blessing");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(blessing);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(blessing));
	}

	@Test
	public void WithNoElfOnTheTableItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blessing = scn.GetFreepsCard("blessing");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(blessing);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.FreepsPlayAvailable(blessing));
		scn.FreepsPlayCard(blessing);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(frodo));
		assertInDiscard(blessing);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AtARiverAnElfCompanionIsPlus3AndAnAllyAtNoSiteIsPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRiverScenario();

		var blessing = scn.GetFreepsCard("blessing");
		var blessing2 = scn.GetFreepsCard("blessing2");
		var arwen = scn.GetFreepsCard("arwen");
		var erestor = scn.GetFreepsCard("erestor");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(erestor);
		scn.MoveCardsToHand(blessing, blessing2);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		assertEquals("Buckleberry Ferry", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		scn.FreepsAssignToMinions(arwen, runner);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(5, scn.GetStrength(erestor));
		scn.FreepsPlayCard(blessing);
		scn.FreepsChooseCard(arwen);
		// Arwen is where the fellowship is, a river: 6 + 3.
		assertEquals(9, scn.GetStrength(arwen));

		scn.ShadowPass();
		scn.FreepsPlayCard(blessing2);
		scn.FreepsChooseCard(erestor);
		// Erestor's home, site 3, is not laid yet: he is at no site, and is 5 + 2.
		assertEquals(7, scn.GetStrength(erestor));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
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
