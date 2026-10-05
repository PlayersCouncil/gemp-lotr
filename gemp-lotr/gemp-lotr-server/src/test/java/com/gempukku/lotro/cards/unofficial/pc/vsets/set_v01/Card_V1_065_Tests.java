package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_065_Tests
{

	// Sites 1 and 2, Bag End and Bree Gate, have no keyword at all, so with the Dam in play both are marshes and
	// the tentacles and the Watcher, which are discarded away from a marsh, stay on the table.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dam", "101_65");

					// Watcher in the Water, Keeper of Westgate: strength 11, vitality 4, damage +1.  Discards every
					// other minion that is not a tentacle; each tentacle is strength +2 and damage +1; discarded
					// if not at a marsh.
					put("watcher", "2_73");
					// Watcher in the Water, Many-tentacled Creature: strength 11, vitality 4.  Discards every other
					// minion that is not a tentacle, and makes the fellowship's site a marsh.
					put("creature", "19_21");

					// Tentacles: [moria] minions, discarded if not at a marsh.
					// Foul Tentacle: strength 4, vitality 1.  May fetch a tentacle when played; never played here.
					put("foul", "2_58");
					// Reaching Tentacle and Strong Tentacle: stronger for each tentacle, nothing else.
					put("reaching", "19_19");
					put("strong", "19_20");

					// Goblin Patrol Troop: a [moria] Orc with no text.
					put("troop", "1_177");
					// Goblin Runner: a [moria] Orc.  "When you play this minion, you may add (2)"; never played.
					put("runner", "1_178");

					// Aragorn, Ranger of the North: strength 8, vitality 4.  His maneuver ability is never used.
					put("aragorn", "1_89");
					// Dwarf Guard: strength 4, vitality 2, no text once in play.
					put("guard", "1_7");
					// Sam, Son of Hamfast: Ring-bound, and not the Ring-bearer.  Strength 3, vitality 4.
					put("sam", "1_311");
					// The Gaffer: a Hobbit ally.  "Fellowship: Exert The Gaffer to heal Frodo or Sam."
					put("gaffer", "1_291");
					// Bounder: a Hobbit ally whose home is site 2, strength 2, vitality 2.  "Skirmish: Exert this
					// ally to prevent a Hobbit from being overwhelmed unless that Hobbit's strength is tripled";
					// never used.
					put("bounder", "1_286");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					// Bree Gate: no keyword.  "While you can spot a ranger, the move limit is +1 for this turn",
					// which nothing here turns on.
					put("site2", "1_327");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A path with every kind of site the Dam's first sentence tells apart.
	protected VirtualTableScenario GetTerrainScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dam", "101_65");
				}},
				new HashMap<>() {{
					// Bag End: no keyword.
					put("site1", "1_319");
					// Ettenmoors: plains.
					put("site2", "1_331");
					// Rivendell Valley: sanctuary, and no terrain keyword.
					put("site3", "1_341");
					// Pass of Caradhras: mountain.  Its Shadow action needs an [isengard] minion.
					put("site4", "1_348");
					// The Bridge of Khazad-dum: underground.
					put("site5", "1_349");
					// Galadriel's Glade: sanctuary, and no terrain keyword.
					put("site6", "1_351");
					// Anduin Wilderland: forest and river, the only river in the Fellowship block that has another
					// terrain keyword as well.  "Skip the archery phase."
					put("site7", "1_354");
					// Anduin Banks: river and nothing else.
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SirannonDamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Sirannon Dam
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Support Area
		 * Game Text: Rivers and sites with no other terrain keywords gain <b>marsh</b>.
		 * 	Companions are <b>defender +1</b>.
		 * 	Skirmish: Remove 2 burdens to have Watcher in the Water replace a tentacle skirmishing an unbound
		 * 	companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dam");

		assertEquals("Sirannon Dam", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedFromHandForNothingItMakesSitesWithNoKeywordMarshesAndTheRingBearerADefender() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var dam = scn.GetShadowCard("dam");
		scn.MoveCardsToHand(dam);

		scn.StartGame();
		scn.FreepsPass();

		var bagEnd = scn.GetSite(1);
		var breeGate = scn.GetCurrentSite();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(bagEnd, Keyword.MARSH));
		assertFalse(scn.HasKeyword(breeGate, Keyword.MARSH));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DEFENDER));
		// 1 companion + 2 for Bree Gate
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.HAND, dam);
		assertTrue(scn.ShadowPlayAvailable(dam));

		scn.ShadowPlayCard(dam);

		// It costs nothing.
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, dam);
		// The site the fellowship is at and the one behind it.
		assertTrue(scn.HasKeyword(bagEnd, Keyword.MARSH));
		assertTrue(scn.HasKeyword(breeGate, Keyword.MARSH));
		assertEquals(1, scn.GetKeywordCount(frodo, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RiversAndSitesWithNoTerrainKeywordGainMarshAndSitesWithAnotherTerrainDoNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTerrainScenario();

		var dam = scn.GetShadowCard("dam");

		scn.StartGame();
		scn.SkipToSite(8);

		var bagEnd = scn.GetSite(1);
		var ettenmoors = scn.GetSite(2);
		var caradhras = scn.GetSite(4);
		var bridge = scn.GetSite(5);
		var wilderland = scn.GetSite(7);
		var banks = scn.GetCurrentSite();
		assertEquals(8, scn.GetCurrentSiteNumber());

		assertTrue(scn.HasKeyword(ettenmoors, Keyword.PLAINS));
		assertTrue(scn.HasKeyword(caradhras, Keyword.MOUNTAIN));
		assertTrue(scn.HasKeyword(bridge, Keyword.UNDERGROUND));
		assertTrue(scn.HasKeyword(wilderland, Keyword.RIVER));
		assertTrue(scn.HasKeyword(wilderland, Keyword.FOREST));
		assertTrue(scn.HasKeyword(banks, Keyword.RIVER));
		assertFalse(scn.HasKeyword(bagEnd, Keyword.MARSH));
		assertFalse(scn.HasKeyword(ettenmoors, Keyword.MARSH));
		assertFalse(scn.HasKeyword(caradhras, Keyword.MARSH));
		assertFalse(scn.HasKeyword(bridge, Keyword.MARSH));
		assertFalse(scn.HasKeyword(wilderland, Keyword.MARSH));
		assertFalse(scn.HasKeyword(banks, Keyword.MARSH));

		scn.MoveCardsToSupportArea(dam);

		// No keyword at all.
		assertTrue(scn.HasKeyword(bagEnd, Keyword.MARSH));
		// A river is a marsh whatever else it is: Anduin Wilderland is a forest too.
		assertTrue(scn.HasKeyword(wilderland, Keyword.MARSH));
		assertTrue(scn.HasKeyword(banks, Keyword.MARSH));
		// One terrain keyword that is not river: plains, mountain, underground.
		assertFalse(scn.HasKeyword(ettenmoors, Keyword.MARSH));
		assertFalse(scn.HasKeyword(caradhras, Keyword.MARSH));
		assertFalse(scn.HasKeyword(bridge, Keyword.MARSH));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ASanctuaryWithNoTerrainKeywordGainsMarsh() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTerrainScenario();

		var dam = scn.GetShadowCard("dam");

		scn.StartGame();
		scn.SkipToSite(6);

		var rivendell = scn.GetSite(3);
		var glade = scn.GetCurrentSite();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(rivendell, Keyword.SANCTUARY));
		assertTrue(scn.HasKeyword(glade, Keyword.SANCTUARY));
		assertFalse(scn.HasKeyword(rivendell, Keyword.MARSH));
		assertFalse(scn.HasKeyword(glade, Keyword.MARSH));

		scn.MoveCardsToSupportArea(dam);

		// Sanctuary is not a terrain keyword (the Rules Councils' ruling of 2025-04-25).
		assertTrue(scn.HasKeyword(rivendell, Keyword.MARSH));
		assertTrue(scn.HasKeyword(glade, Keyword.MARSH));
		// The two sites between them, a mountain and an underground site, are no marshes for it.
		assertFalse(scn.HasKeyword(scn.GetSite(4), Keyword.MARSH));
		assertFalse(scn.HasKeyword(scn.GetSite(5), Keyword.MARSH));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ASiteGivenForestBattlegroundOrDwellingIsNoLongerAMarshAndARiverGivenOneStillIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTerrainScenario();

		var dam = scn.GetShadowCard("dam");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCardsToSupportArea(dam);

		var bagEnd = scn.GetSite(1);
		var rivendell = scn.GetSite(3);
		var glade = scn.GetSite(6);
		var banks = scn.GetCurrentSite();
		assertTrue(scn.HasKeyword(bagEnd, Keyword.MARSH));
		assertTrue(scn.HasKeyword(rivendell, Keyword.MARSH));
		assertTrue(scn.HasKeyword(glade, Keyword.MARSH));
		assertTrue(scn.HasKeyword(banks, Keyword.MARSH));

		// The path has plains, a mountain and an underground site of its own.  No site of the Fellowship block
		// is a forest and nothing else at a number this path can spare, and none at all is a battleground or a
		// dwelling, so the three sites with no terrain keyword are each given one.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, bagEnd, null, Keyword.FOREST));
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, rivendell, null, Keyword.BATTLEGROUND));
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, glade, null, Keyword.DWELLING));
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, banks, null, Keyword.DWELLING));

		assertTrue(scn.HasKeyword(bagEnd, Keyword.FOREST));
		assertTrue(scn.HasKeyword(rivendell, Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(glade, Keyword.DWELLING));
		assertFalse(scn.HasKeyword(bagEnd, Keyword.MARSH));
		assertFalse(scn.HasKeyword(rivendell, Keyword.MARSH));
		assertFalse(scn.HasKeyword(glade, Keyword.MARSH));
		// Anduin Banks is a river, and a river is a marsh whatever else it becomes.
		assertTrue(scn.HasKeyword(banks, Keyword.DWELLING));
		assertTrue(scn.HasKeyword(banks, Keyword.MARSH));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EveryCompanionIsDefenderPlusOneAndAnAllyAndAMinionAreNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gaffer = scn.GetFreepsCard("gaffer");
		var dam = scn.GetShadowCard("dam");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(gaffer);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);

		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(sam, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(gaffer, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DEFENDER));

		scn.MoveCardsToSupportArea(dam);

		// The Ring-bearer, a Ring-bound companion who is not, and an unbound Man.
		assertEquals(1, scn.GetKeywordCount(frodo, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(sam, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(gaffer, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DEFENDER));

		// And it is the real thing: Aragorn defends against two minions.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionRemovesTwoBurdensAndWatcherLeavesHisOwnAssignmentToFightInTheTentaclesPlace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var dam = scn.GetShadowCard("dam");
		var foul = scn.GetShadowCard("foul");
		var watcher = scn.GetShadowCard("watcher");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(dam);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.FreepsPass();
		scn.MoveMinionsToTable(foul, watcher);

		// The assignment phase is not the skirmish phase.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(dam));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(aragorn, foul);
		scn.ShadowAssignToMinions(frodo, watcher);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, foul));
		assertFalse(scn.IsCharSkirmishing(watcher));
		assertTrue(scn.IsCharAssignedAgainst(watcher, frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(dam));

		scn.ShadowUseCardAction(dam);

		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.IsCharSkirmishing(foul));
		assertFalse(scn.IsCharSkirmishingAgainst(aragorn, foul));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, watcher));
		// Watcher is in Aragorn's skirmish now and no longer waiting for Frodo.
		assertFalse(scn.IsCharAssignedAgainst(watcher, frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The skirmish goes on with Watcher where the tentacle was: 11 against Aragorn's 8.
		scn.PassSkirmishActions();

		// 1 for losing + 1 for Watcher's damage +1.  Against the tentacle's 4 + 2 Aragorn's 8 would have won.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(watcher));
		assertEquals(0, scn.GetWoundsOn(foul));
		assertInZone(Zone.SHADOW_CHARACTERS, foul, watcher);
		// Nobody is left to fight Frodo.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOnlyOneBurdenTheSkirmishActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var dam = scn.GetShadowCard("dam");
		var foul = scn.GetShadowCard("foul");
		var watcher = scn.GetShadowCard("watcher");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(dam);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(foul, watcher);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, foul);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Everything the action could want but the second burden.
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, foul));
		assertInZone(Zone.SHADOW_CHARACTERS, watcher);
		assertFalse(scn.ShadowActionAvailable(dam));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WatcherReplacesTheTentacleInTheSkirmishAndNotOneWaitingForAnotherCompanionNorHimself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var dam = scn.GetShadowCard("dam");
		var creature = scn.GetShadowCard("creature");
		var foul = scn.GetShadowCard("foul");
		var strong = scn.GetShadowCard("strong");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToSupportArea(dam);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.FreepsPass();
		scn.MoveMinionsToTable(creature, strong, foul);

		scn.SkipToAssignments();
		// One tentacle for Aragorn and one for the Dwarf Guard, whose skirmish comes later.  The Shadow player
		// sends Watcher into Aragorn's skirmish as well: with a Watcher in play no other minion that is not a
		// tentacle stays on the table, so he is the only one there can be in a skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, foul}, new PhysicalCardImpl[]{guard, strong});
		scn.ShadowAssignToMinions(aragorn, creature);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, foul));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, creature));
		assertFalse(scn.IsCharSkirmishing(strong));
		assertTrue(scn.IsCharAssignedAgainst(strong, guard));
		assertTrue(scn.ShadowActionAvailable(dam));

		scn.ShadowUseCardAction(dam);

		// Exactly 2 burdens were enough.
		assertEquals(0, scn.GetBurdens());
		// The one tentacle skirmishing an unbound companion is the one that leaves.
		assertFalse(scn.IsCharSkirmishing(foul));
		assertInZone(Zone.SHADOW_CHARACTERS, foul);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, creature));
		// The tentacle whose skirmish has not begun is still waiting for it.
		assertFalse(scn.IsCharSkirmishing(strong));
		assertTrue(scn.IsCharAssignedAgainst(strong, guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	@Ignore("F-0044: with two tentacles skirmishing an unbound companion the Shadow player is not asked which one Watcher replaces")
	public void OnlyATentacleInTheSkirmishIsOfferedAndTheOneNotChosenFightsOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var dam = scn.GetShadowCard("dam");
		var creature = scn.GetShadowCard("creature");
		var foul = scn.GetShadowCard("foul");
		var reaching = scn.GetShadowCard("reaching");
		var strong = scn.GetShadowCard("strong");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToSupportArea(dam);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.FreepsPass();
		scn.MoveMinionsToTable(creature, foul, reaching, strong);

		scn.SkipToAssignments();
		// The Dam's defender +1 lets Aragorn take two tentacles.  A third waits for the Dwarf Guard, and the
		// Shadow player sends Watcher into Aragorn's skirmish as well.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, foul, reaching}, new PhysicalCardImpl[]{guard, strong});
		scn.ShadowAssignToMinions(aragorn, creature);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, foul));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, reaching));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, creature));
		assertTrue(scn.IsCharAssignedAgainst(strong, guard));
		assertTrue(scn.ShadowActionAvailable(dam));

		scn.ShadowUseCardAction(dam);

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowHasCardChoiceAvailable(foul, reaching));
		// A tentacle whose skirmish with an unbound companion has not begun, and a minion skirmishing an
		// unbound companion that is no tentacle.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(strong, creature));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(foul);

		assertFalse(scn.IsCharSkirmishing(foul));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, reaching));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, creature));
		assertTrue(scn.IsCharAssignedAgainst(strong, guard));
		assertInZone(Zone.SHADOW_CHARACTERS, foul);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ATentacleSkirmishingARingBoundCompanionIsNotReplacedThoughTheBurdensAreRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var dam = scn.GetShadowCard("dam");
		var foul = scn.GetShadowCard("foul");
		var watcher = scn.GetShadowCard("watcher");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(dam);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.FreepsPass();
		scn.MoveMinionsToTable(foul, watcher);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, foul);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(sam);
		scn.FreepsPass();

		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.IsCharSkirmishingAgainst(sam, foul));
		assertFalse(scn.IsCharSkirmishing(watcher));
		// Removing the burdens is the whole cost, and it can be paid.
		assertTrue(scn.ShadowActionAvailable(dam));

		scn.ShadowUseCardAction(dam);

		assertEquals(0, scn.GetBurdens());
		// Sam is Ring-bound, so there is no tentacle here for Watcher to replace.
		assertTrue(scn.IsCharSkirmishingAgainst(sam, foul));
		assertFalse(scn.IsCharSkirmishing(watcher));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ATentacleSkirmishingAnAllyIsNotReplacedThoughTheBurdensAreRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bounder = scn.GetFreepsCard("bounder");
		var dam = scn.GetShadowCard("dam");
		var foul = scn.GetShadowCard("foul");
		var watcher = scn.GetShadowCard("watcher");
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(dam);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.FreepsPass();
		scn.MoveMinionsToTable(foul, watcher);

		scn.SkipToAssignments();
		// Bree Gate is site 2, the Bounder's home, so he may be assigned to a skirmish here.
		scn.FreepsAssignToMinions(bounder, foul);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(bounder);
		scn.FreepsPass();

		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.IsCharSkirmishingAgainst(bounder, foul));
		assertFalse(scn.IsCharSkirmishing(watcher));
		assertTrue(scn.ShadowActionAvailable(dam));

		scn.ShadowUseCardAction(dam);

		assertEquals(0, scn.GetBurdens());
		// An ally is not Ring-bound, but only a companion is unbound: there is no tentacle here to replace either.
		assertTrue(scn.IsCharSkirmishingAgainst(bounder, foul));
		assertFalse(scn.IsCharSkirmishing(watcher));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
