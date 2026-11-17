package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_101_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stump", "4_101");
					put("treebeard", "4_103");  // Treebeard, Earthborn: [gandalf] Ent ally, unhasty, home 2T/8T, strength 12, vitality 4; a response and a fellowship ability about unbound Hobbits
					put("oldest", "4_104");     // Treebeard, Oldest Living Thing: the other printed Ent ally (the same title), a fellowship ability only
					put("avenger", "6_27");     // Ent Avenger: an Ent who is a companion, not an ally
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] Man ally, home 1F, strength 1; a fellowship ability
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, strength 6; a Shadow ability used only on purpose
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, strength 7, vitality 1, nothing else
					put("rider", "12_161");     // Black Rider: [wraith] Nazgul, fierce, strength 8 +1 for each companion, vitality 3; waits for the fierce assignment
					put("murderous", "11_195"); // Murderous Uruk: an Uruk-hai of the [uruk-hai] culture; damage +1 and muster only
					put("ambition", "1_133");   // Saruman's Ambition: an [isengard] condition in the support area; its skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StumpandBrambleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Stump and Bramble
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Spot an [isengard] minion and an Ent ally. Until the regroup phase, that ally is <b>damage +2</b> and participates in archery fire and skirmishes.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stump");

		assertEquals("Stump and Bramble", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ThatEntAllyIsDamagePlus2AndTakesPartInArcheryAndSkirmishesUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stump = scn.GetFreepsCard("stump");
		var treebeard = scn.GetFreepsCard("treebeard");
		var barliman = scn.GetFreepsCard("barliman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(treebeard, barliman);
		scn.MoveCardsToHand(stump);

		var savage = scn.GetShadowCard("savage");
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, marksman);
		scn.SkipToPhase(Phase.MANEUVER);

		// Site 2 of the Fellowship path: Treebeard's homes are Towers sites and Barliman's is site 1, so neither takes part of his own accord.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetKeywordCount(treebeard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(barliman, Keyword.DAMAGE));
		assertFalse(scn.FreepsCanAssign(treebeard));
		assertFalse(scn.FreepsCanAssign(barliman));
		assertTrue(scn.FreepsPlayAvailable(stump));

		// The Uruk Savage is the [isengard] minion, and Treebeard, the one Ent ally, is spotted unasked.
		scn.FreepsPlayCard(stump);
		assertInDiscard(stump);
		assertEquals(2, scn.GetKeywordCount(treebeard, Keyword.DAMAGE));
		assertTrue(scn.FreepsCanAssign(treebeard));
		// Another ally is not "that ally".
		assertEquals(0, scn.GetKeywordCount(barliman, Keyword.DAMAGE));
		assertFalse(scn.FreepsCanAssign(barliman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// The Marksman's archery wound may be placed on Treebeard, not on Barliman.
		scn.SkipToArcheryWounds();
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo, treebeard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman));
		assertEquals(0, scn.GetWoundsOn(treebeard));
		scn.FreepsChooseCard(treebeard);
		assertEquals(1, scn.GetWoundsOn(treebeard));

		// Unhasty as he is, a [gandalf] card has him take part, and the Free Peoples player may assign him (CR 4.2 "unhasty").
		scn.PassAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(treebeard));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(barliman));
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, marksman },
				new PhysicalCardImpl[] { treebeard, savage }
		);

		// Aragorn's skirmish first: 8 against 7, and the Marksman (vitality 1) dies.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(marksman);

		// Treebeard's skirmish follows one already resolved: he is still damage +2.
		scn.FreepsResolveSkirmish(treebeard);
		assertTrue(scn.IsCharSkirmishingAgainst(treebeard, savage));
		assertEquals(2, scn.GetKeywordCount(treebeard, Keyword.DAMAGE));
		scn.PassSkirmishActions();
		// 12 is more than double 5: the Savage is overwhelmed.
		assertInDiscard(savage);

		// At the regroup phase both are gone.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetKeywordCount(treebeard, Keyword.DAMAGE));
		assertFalse(scn.FreepsCanAssign(treebeard));
	}

	@Test
	public void WithNoIsengardMinionItCannotBePlayedUntilOneIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stump = scn.GetFreepsCard("stump");
		var oldest = scn.GetFreepsCard("oldest");
		scn.MoveAlliesToTable(oldest);
		scn.MoveCardsToHand(stump);

		var murderous = scn.GetShadowCard("murderous");
		var ambition = scn.GetShadowCard("ambition");
		var forger = scn.GetShadowCard("forger");

		scn.StartGame();
		assertInHand(stump);
		scn.FreepsPass();
		scn.MoveMinionsToTable(murderous);
		scn.MoveCardsToSupportArea(ambition);
		scn.SkipToPhase(Phase.MANEUVER);

		// An Uruk-hai of another culture, and an [isengard] card that is no minion: nothing to spot.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, murderous);
		assertInZone(Zone.SUPPORT, ambition);
		assertInHand(stump);
		assertFalse(scn.FreepsPlayAvailable(stump));

		// An [isengard] Orc comes into play: an [isengard] minion of another race than Uruk-hai is enough.
		scn.MoveMinionsToTable(forger);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(stump));

		assertEquals(0, scn.GetKeywordCount(oldest, Keyword.DAMAGE));
		assertFalse(scn.FreepsCanAssign(oldest));
		scn.FreepsPlayCard(stump);
		assertInDiscard(stump);
		assertEquals(2, scn.GetKeywordCount(oldest, Keyword.DAMAGE));
		assertTrue(scn.FreepsCanAssign(oldest));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoEntAllyItCannotBePlayedUntilOneIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stump = scn.GetFreepsCard("stump");
		var avenger = scn.GetFreepsCard("avenger");
		var barliman = scn.GetFreepsCard("barliman");
		var treebeard = scn.GetFreepsCard("treebeard");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(stump);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertInHand(stump);
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// An Ent who is a companion, and an ally who is no Ent: nothing to spot.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInHand(stump);
		assertFalse(scn.FreepsPlayAvailable(stump));

		// Treebeard comes into play.
		scn.MoveAlliesToTable(treebeard);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(stump));

		scn.FreepsPlayCard(stump);
		assertInDiscard(stump);
		assertEquals(2, scn.GetKeywordCount(treebeard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(avenger, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(barliman, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ThatEntAllyMayStillBeAssignedInTheFierceAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stump = scn.GetFreepsCard("stump");
		var treebeard = scn.GetFreepsCard("treebeard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCardsToHand(stump);

		var savage = scn.GetShadowCard("savage");
		var rider = scn.GetShadowCard("rider");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, rider);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsCanAssign(treebeard));
		scn.FreepsPlayCard(stump);
		assertEquals(2, scn.GetKeywordCount(treebeard, Keyword.DAMAGE));

		// The fierce Black Rider is left out of the first assignment; the regroup phase is still ahead of the fierce one.
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, savage);
		scn.PassSkirmishActions();
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(1, scn.GetWoundsOn(savage));

		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsCanAssign(treebeard));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(treebeard));
		scn.FreepsAssignAndResolve(treebeard, rider);
		assertTrue(scn.IsCharSkirmishingAgainst(treebeard, rider));
		assertEquals(2, scn.GetKeywordCount(treebeard, Keyword.DAMAGE));
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
