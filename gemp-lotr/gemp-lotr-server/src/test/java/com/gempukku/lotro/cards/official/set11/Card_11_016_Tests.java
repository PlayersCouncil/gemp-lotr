package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_016_Tests
{

	// The Fellowship path with Buckleberry Ferry 1_330 as site 2 (a river; its phases are skipped only while
	// only Hobbits are companions) and Rivendell Waterfall 1_342 as site 3 (a forest and a sanctuary; its move
	// limit text needs a ranger, and none is laid).  Site 4, Balin's Tomb 1_343, is underground: neither.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "11_16");
					put("elf", "1_53");         // Lorien Elf: Elf companion, strength 4, vitality 2; "To play, spot an Elf" is its whole text
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: Elf ally, home 3F; a skirmish ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, Knight and no other text
					put("legolas", "11_21");    // Legolas, Companion of the Ring: an Elf companion who bears no blade; his +2 is only at a battleground or forest
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, damage +1 and nothing else
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_330");
					put("site3", "1_342");
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
		 * Strength: +1
		 * Game Text: Bearer must be an Elf.
		 * While bearer is at a forest or river site, each minion he or she is skirmishing is strength -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blade");

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

	@Test
	public void CanBePlayedOnAnElfCompanionOrAnElfAllyAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var blade = scn.GetFreepsCard("blade");
		var elf = scn.GetFreepsCard("elf");
		var golradir = scn.GetFreepsCard("golradir");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(elf, knight);
		scn.MoveAlliesToTable(golradir);
		scn.MoveCardsToHand(blade);

		scn.StartGame();
		assertInHand(blade);

		assertTrue(scn.FreepsPlayAvailable(blade));
		scn.FreepsPlayCard(blade);
		assertTrue(scn.FreepsHasCardChoiceAvailable(elf, golradir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(elf);
		assertAttachedTo(blade, elf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtARiverEachMinionSkirmishingBearerIsStrengthMinus1AndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade");
		var elf = scn.GetFreepsCard("elf");
		var knight = scn.GetFreepsCard("knight");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(elf, knight, legolas);
		scn.AttachCardsTo(elf, blade);

		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertAttachedTo(blade, elf);
		scn.MoveMinionsToTable(troop, troop2, troop3, savage);
		scn.SkipToAssignments();
		assertEquals("Buckleberry Ferry", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{elf, troop},
				new PhysicalCardImpl[]{knight, troop2},
				new PhysicalCardImpl[]{legolas, troop3});
		// The Uruk-hai left over goes to the bearer as well: two minions in his skirmish.
		scn.ShadowAssignToMinions(elf, savage);

		// Assigned to him, but no skirmish has begun.
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop2));
		assertEquals(13, scn.GetStrength(troop3));

		// The Knight's skirmish first: the minions waiting for the bearer, and the Knight's own, are as printed.
		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.IsCharSkirmishingAgainst(knight, troop2));
		assertEquals(13, scn.GetStrength(troop2));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop3));
		scn.PassSkirmishActions();

		// An Elf who bears no blade, at the same river: his minion is as printed.
		scn.FreepsResolveSkirmish(legolas);
		assertTrue(scn.IsCharSkirmishingAgainst(legolas, troop3));
		assertEquals(13, scn.GetStrength(troop3));
		assertEquals(13, scn.GetStrength(troop));
		scn.PassSkirmishActions();

		// The bearer's skirmish: an Orc and an Uruk-hai, each 1 less.
		scn.FreepsResolveSkirmish(elf);
		assertTrue(scn.IsCharSkirmishingAgainst(elf, troop));
		assertTrue(scn.IsCharSkirmishingAgainst(elf, savage));
		assertEquals(12, scn.GetStrength(troop));
		assertEquals(4, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtAForestTheMinionSkirmishingBearerIsStrengthMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(elf);
		scn.AttachCardsTo(elf, blade);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(blade, elf);
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals("Rivendell Waterfall", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));

		scn.FreepsAssignToMinions(elf, troop);
		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsResolveSkirmish(elf);
		assertTrue(scn.IsCharSkirmishingAgainst(elf, troop));
		assertEquals(12, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtASiteThatIsNeitherForestNorRiverTheMinionSkirmishingBearerKeepsItsStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(elf);
		scn.AttachCardsTo(elf, blade);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(blade, elf);
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));

		scn.FreepsAssignToMinions(elf, troop);

		scn.FreepsResolveSkirmish(elf);
		assertTrue(scn.IsCharSkirmishingAgainst(elf, troop));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
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
