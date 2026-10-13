package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_049_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stalker1", "103_49");
					put("stalker2", "103_49");    // two dots: a second copy may be in play
					put("explorer", "4_250");     // Southron Explorer: Southron, no ambush, strength 5, vitality 3; a regroup ability never used here
					put("soldier", "4_254");      // Southron Soldier: Southron, ambush (2), cost 2, no other text
					put("scout", "4_252");        // Southron Scout: Southron, ambush (2), no other text
					put("easterling", "7_141");   // Easterling Attacker: [raider] Man, Easterling and no Southron; a skirmish ability never used here
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, no text at all
					// Bladetusk Rearguard: [raider] artifact in the support area. Maneuver: remove (5) to make it a fierce
					// mounted Southron minion until the end of the turn, strength 5, vitality 5, no race; ambush only for
					// Southrons stacked on it, and none is
					put("rearguard", "103_43");
					put("trap", "103_56");        // Sandcraft Trap: Assignment event, cost 3, "Restore all trackers and minions with ambush."
					put("aragorn", "1_89");       // Aragorn, Ranger of the North
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesertWindStalkerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Desert Wind Stalker
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 12
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron.</b>  Tracker.
		 * 	When you play this minion, remove (3) or hinder this minion.
		 * 	Southrons gain <b>ambush (1)</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stalker1");

		assertEquals("Desert Wind Stalker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		// Two dots: no more than two copies in play
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedOffersTheChoiceAndRemovingThreeLeavesItUnhindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stalker = scn.GetShadowCard("stalker1");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToHand(stalker);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 5 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(8, scn.GetTwilight());
		assertEquals(0, scn.GetKeywordCount(explorer, Keyword.AMBUSH));
		assertInZone(Zone.HAND, stalker);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(stalker));

		scn.ShadowPlayCard(stalker);

		assertInZone(Zone.SHADOW_CHARACTERS, stalker);
		assertEquals(0, scn.GetShadowHandCount());
		// 8 - 3 to play - 2 roaming (a site 4 minion played at site 2) leaves exactly the 3 the choice needs
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowChoiceAvailable("Remove (3)"));
		assertTrue(scn.ShadowChoiceAvailable("Hinder"));

		scn.ShadowChoose("Remove (3)");

		// 3 - 3 for the choice
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.IsHindered(stalker));
		assertInZone(Zone.SHADOW_CHARACTERS, stalker);
		// The Explorer is a Southron and has ambush (1) from the Stalker now
		assertEquals(1, scn.GetKeywordCount(explorer, Keyword.AMBUSH));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedChoosingToHinderRemovesNothingAndAHinderedStalkerGivesNoAmbush() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stalker = scn.GetShadowCard("stalker1");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToHand(stalker);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// 5 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(8, scn.GetTwilight());
		assertEquals(0, scn.GetKeywordCount(explorer, Keyword.AMBUSH));

		scn.ShadowPlayCard(stalker);

		// 8 - 3 to play - 2 roaming at site 2
		assertEquals(3, scn.GetTwilight());
		// In play and not yet hindered: the Explorer has its ambush (1)
		assertFalse(scn.IsHindered(stalker));
		assertEquals(1, scn.GetKeywordCount(explorer, Keyword.AMBUSH));
		assertTrue(scn.ShadowChoiceAvailable("Remove (3)"));
		assertTrue(scn.ShadowChoiceAvailable("Hinder"));

		scn.ShadowChoose("Hinder");

		// Nothing more is removed, and the minion is hindered where it stands.
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.IsHindered(stalker));
		assertInZone(Zone.SHADOW_CHARACTERS, stalker);
		// The game text of a hindered card does not apply, so the Explorer has no ambush again.
		assertEquals(0, scn.GetKeywordCount(explorer, Keyword.AMBUSH));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedWithFewerThanThreeLeftInThePoolItIsHinderedWithoutAChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stalker = scn.GetShadowCard("stalker1");
		scn.MoveCardsToHand(stalker);

		scn.StartGame();
		scn.SetTwilight(4);
		scn.FreepsPass();

		// 4 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(7, scn.GetTwilight());
		assertFalse(scn.IsHindered(stalker));

		scn.ShadowPlayCard(stalker);

		// 7 - 3 to play - 2 roaming at site 2 leaves 2, one short of what removing (3) needs.
		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.ShadowChoiceAvailable("Remove (3)"));
		assertTrue(scn.IsHindered(stalker));
		assertInZone(Zone.SHADOW_CHARACTERS, stalker);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayingAnotherMinionWhileItIsInPlayRemovesAndHindersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stalker = scn.GetShadowCard("stalker1");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(soldier);

		scn.StartGame();
		scn.MoveMinionsToTable(stalker);
		scn.SetTwilight(7);
		scn.FreepsPass();

		// 7 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());
		assertFalse(scn.IsHindered(stalker));

		scn.ShadowPlayCard(soldier);

		assertInPlay(soldier);
		// 10 - 2 to play the Soldier - 2 roaming at site 2, and nothing more
		assertEquals(6, scn.GetTwilight());
		assertFalse(scn.IsHindered(stalker));
		assertFalse(scn.IsHindered(soldier));
		assertFalse(scn.ShadowChoiceAvailable("Remove (3)"));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondCopyPlayedMakesOneChoiceAndHindersItselfAndEachCopyGivesItsOwnAmbush() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stalker1 = scn.GetShadowCard("stalker1");
		var stalker2 = scn.GetShadowCard("stalker2");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToHand(stalker2);

		scn.StartGame();
		scn.MoveMinionsToTable(stalker1, explorer);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// 5 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(8, scn.GetTwilight());
		assertEquals(1, scn.GetKeywordCount(explorer, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(stalker1, Keyword.AMBUSH));
		assertTrue(scn.ShadowPlayAvailable(stalker2));

		scn.ShadowPlayCard(stalker2);

		// 8 - 3 to play - 2 roaming at site 2
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, stalker2);
		assertTrue(scn.ShadowChoiceAvailable("Remove (3)"));
		assertTrue(scn.ShadowChoiceAvailable("Hinder"));
		// Both copies are in play and unhindered: each gives every Southron its own ambush (1).
		assertFalse(scn.IsHindered(stalker1));
		assertFalse(scn.IsHindered(stalker2));
		assertEquals(2, scn.GetKeywordCount(explorer, Keyword.AMBUSH));
		assertEquals(2, scn.GetKeywordCount(stalker1, Keyword.AMBUSH));
		assertEquals(2, scn.GetKeywordCount(stalker2, Keyword.AMBUSH));

		scn.ShadowChoose("Hinder");

		// "This minion" is the copy that was played; the copy already in play is left alone.
		assertTrue(scn.IsHindered(stalker2));
		assertFalse(scn.IsHindered(stalker1));
		assertEquals(3, scn.GetTwilight());
		// Only the first copy's text applies now.
		assertEquals(1, scn.GetKeywordCount(explorer, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(stalker1, Keyword.AMBUSH));
		// One choice for one play: nothing is asked for the first copy.
		assertFalse(scn.ShadowChoiceAvailable("Remove (3)"));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EverySouthronGainsAmbushOneAndNoOtherMinionDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stalker = scn.GetShadowCard("stalker1");
		var explorer = scn.GetShadowCard("explorer");
		var soldier = scn.GetShadowCard("soldier");
		var easterling = scn.GetShadowCard("easterling");
		var troop = scn.GetShadowCard("troop");
		var rearguard = scn.GetShadowCard("rearguard");
		scn.MoveCardsToSupportArea(rearguard);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, soldier, easterling, troop);
		// Every card printed a Southron is a [raider] Man. The Southron who is no Man is a Bladetusk Rearguard that
		// has made itself a Southron minion for the turn by its own Maneuver ability, with nothing stacked on it.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();
		scn.ShadowUseCardAction(rearguard);

		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertTrue(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		assertFalse(scn.IsRace(rearguard, Race.MAN));
		assertFalse(scn.HasKeyword(troop, Keyword.SOUTHRON));
		assertFalse(scn.HasKeyword(easterling, Keyword.SOUTHRON));
		assertEquals(0, scn.GetKeywordCount(explorer, Keyword.AMBUSH));
		assertEquals(2, scn.GetKeywordCount(soldier, Keyword.AMBUSH));
		assertEquals(0, scn.GetKeywordCount(rearguard, Keyword.AMBUSH));
		assertEquals(0, scn.GetKeywordCount(easterling, Keyword.AMBUSH));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.AMBUSH));

		scn.MoveMinionsToTable(stalker);

		// A Southron with no ambush of its own
		assertEquals(1, scn.GetKeywordCount(explorer, Keyword.AMBUSH));
		// A Southron with a printed ambush (2): the two add up, 2 + 1
		assertEquals(3, scn.GetKeywordCount(soldier, Keyword.AMBUSH));
		// The Stalker is a Southron itself
		assertEquals(1, scn.GetKeywordCount(stalker, Keyword.AMBUSH));
		// A Southron minion that is no Man
		assertEquals(1, scn.GetKeywordCount(rearguard, Keyword.AMBUSH));
		// A [raider] Man who is no Southron
		assertEquals(0, scn.GetKeywordCount(easterling, Keyword.AMBUSH));
		// A minion that is no Southron
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.AMBUSH));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ASouthronGivenAmbushLetsItsOwnerAddOneWhenTheFreePeoplesPlayerAssignsToIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stalker = scn.GetShadowCard("stalker1");
		var explorer = scn.GetShadowCard("explorer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(stalker, explorer);
		scn.SkipToAssignments();

		// 2 for Frodo and Aragorn + 2 for the shadow number of site 2
		assertEquals(4, scn.GetTwilight());
		assertEquals(1, scn.GetKeywordCount(explorer, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(stalker, Keyword.AMBUSH));

		// The Explorer is assigned by the Free Peoples player; the Stalker is left for the Shadow player.
		scn.FreepsAssignToMinions(aragorn, explorer);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// 4 + 1 for the Explorer's ambush (1), and nothing for the Stalker, whom nobody was assigned to
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.IsCharAssignedAgainst(aragorn, explorer));
		assertFalse(scn.IsCharAssigned(stalker));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions to companions or allies at home"));
	}

	@Test
	public void SandcraftTrapRestoresAHinderedScoutWithPrintedAmbushAndNotAHinderedExplorer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stalker = scn.GetShadowCard("stalker1");
		var scout = scn.GetShadowCard("scout");
		var explorer = scn.GetShadowCard("explorer");
		var trap = scn.GetShadowCard("trap");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(trap);

		scn.StartGame();
		scn.MoveMinionsToTable(stalker, scout, explorer);
		scn.HinderCard(scout, explorer);
		scn.SetTwilight(3);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertFalse(scn.IsHindered(stalker));
		assertTrue(scn.IsHindered(scout));
		assertTrue(scn.IsHindered(explorer));
		// The Stalker's text does not reach a hindered Southron: the Explorer has no ambush to be restored by.
		assertEquals(1, scn.GetKeywordCount(stalker, Keyword.AMBUSH));
		assertEquals(0, scn.GetKeywordCount(explorer, Keyword.AMBUSH));
		assertTrue(scn.ShadowPlayAvailable(trap));

		// "Restore all trackers and minions with ambush."
		scn.ShadowPlayCard(trap);

		// The Scout has ambush (2) printed on it; the Explorer would only gain ambush once restored.
		assertFalse(scn.IsHindered(scout));
		assertTrue(scn.IsHindered(explorer));
		assertFalse(scn.IsHindered(stalker));
		assertInDiscard(trap);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
