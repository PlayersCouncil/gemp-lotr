package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_163_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chieftain", "7_163");
					put("explorer", "4_250");   // Southron Explorer: a [raider] Southron Man; a regroup ability used only on purpose
					// Easterling Axeman: a [raider] Easterling Man; strength +3 and fierce only at 2 burdens
					put("axeman", "4_224");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is a Nazgul and no Man; his text needs 3 or 5 burdens
					put("khamul", "103_61");
					put("brigand", "4_10");     // Dunlending Brigand: a Man who is not [raider]; no text
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text
					put("goblin1", "1_178");    // Goblin Runner: [moria] minion; adds (2) only when played, and is cheated in here
					put("goblin2", "1_178");
					put("goblin3", "1_178");
					put("goblin4", "1_178");
					// The End Comes: "Assignment: Spot 5 [moria] minions to make the Free Peoples player assign the
					// Ring-bearer to a skirmish"
					put("endcomes", "1_169");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("boromir", "1_96");     // Boromir, Lord of Gondor: vitality 3; his text is about being overwhelmed
					// Éowyn, Northwoman: "Assignment: Exert Éowyn and assign a minion to the Ring-bearer to make that
					// minion lose all game text keywords ..."
					put("eowyn", "17_96");
					put("bounder", "1_286");    // Bounder: Hobbit ally whose home is site 2 of this path
					// Unexpected Visitor: on a [gondor] companion, gives each minion "Assignment: Assign this minion to bearer"
					put("visitor", "7_126");
					// Goblin Patrol Troop: no text.  Four in the Free Peoples hand give him the initiative.
					put("hand1", "1_177");
					put("hand2", "1_177");
					put("hand3", "1_177");
					put("hand4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronChieftainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Southron Chieftain
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. To play, spot a [raider] Man.
		 * Each time the Free Peoples player assigns this minion to a character, the Shadow has initiative until the end of the turn.
		*/

		var scn = GetScenario();

		var chieftain = scn.GetFreepsCard("chieftain");

		assertEquals("Southron Chieftain", chieftain.getBlueprint().getTitle());
		assertNull(chieftain.getBlueprint().getSubtitle());
		assertFalse(chieftain.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, chieftain.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, chieftain.getBlueprint().getCulture());
		assertEquals(CardType.MINION, chieftain.getBlueprint().getCardType());
		assertEquals(Race.MAN, chieftain.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(chieftain, Keyword.SOUTHRON));
		assertEquals(4, chieftain.getBlueprint().getTwilightCost());
		assertEquals(9, chieftain.getBlueprint().getStrength());
		assertEquals(2, chieftain.getBlueprint().getVitality());
		assertEquals(4, chieftain.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsPlayedWithASouthronToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chieftain = scn.GetShadowCard("chieftain");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToHand(chieftain);
		assertInZone(Zone.HAND, chieftain);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(chieftain));
		scn.ShadowPlayCard(chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilARaiderManIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		var chieftain = scn.GetShadowCard("chieftain");
		var axeman = scn.GetShadowCard("axeman");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(chieftain, axeman);
		assertInZone(Zone.HAND, chieftain, axeman);

		scn.StartGame();
		// A [raider] minion who is no Man, a Man who is not [raider], and the Free Peoples player's own Man.
		scn.MoveMinionsToTable(khamul, brigand);
		// Enough for the Axeman and the Chieftain, roaming penalties included.
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.HAND, chieftain);
		assertFalse(scn.ShadowPlayAvailable(chieftain));

		// An Easterling is a [raider] Man.
		scn.ShadowPlayCard(axeman);
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		assertTrue(scn.ShadowPlayAvailable(chieftain));

		scn.ShadowPlayCard(chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AssignedByTheFreePeoplesPlayerItGivesTheShadowInitiativeWithAFullHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));

		var chieftain = scn.GetShadowCard("chieftain");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, troop);
		scn.SkipToAssignments();

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		// The Troop goes to Boromir in the same answer: only the Chieftain's assignment sets anything off.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, chieftain}, new PhysicalCardImpl[]{boromir, troop});

		assertTrue(scn.IsCharAssignedAgainst(aragorn, chieftain));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedByTheShadowPlayerTheFreePeoplesPlayerKeepsTheInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));

		var chieftain = scn.GetShadowCard("chieftain");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, explorer);
		scn.SkipToAssignments();

		assertTrue(scn.FreepsHasInitiative());

		// The Free Peoples player assigns another [raider] Southron Man and leaves the Chieftain; the Shadow player
		// assigns it himself.
		scn.FreepsAssignToMinions(boromir, explorer);
		scn.ShadowAssignToMinions(aragorn, chieftain);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, chieftain));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedByTheShadowPlayersOwnAssignmentActionTheFreePeoplesPlayerKeepsTheInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var visitor = scn.GetFreepsCard("visitor");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, visitor);
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));

		var chieftain = scn.GetShadowCard("chieftain");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertSame(aragorn, visitor.getAttachedTo());
		assertTrue(scn.FreepsHasInitiative());

		// Unexpected Visitor gives the minion an assignment action, and the one who uses it is the Shadow player.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(chieftain));
		scn.ShadowUseCardAction(chieftain);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, chieftain));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignedToAnAllyItGivesTheShadowInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));

		var chieftain = scn.GetShadowCard("chieftain");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, troop);
		// The fellowship has moved to site 2, the Bounder's home, where he may be assigned.
		scn.SkipToAssignments();

		assertTrue(scn.FreepsHasInitiative());

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bounder, chieftain}, new PhysicalCardImpl[]{aragorn, troop});

		assertTrue(scn.IsCharAssignedAgainst(bounder, chieftain));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedByTheFreePeoplesPlayersOwnCardToTheRingBearerItGivesTheShadowInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));

		var chieftain = scn.GetShadowCard("chieftain");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetWoundsOn(eowyn));

		// Éowyn exerts and the Free Peoples player assigns a minion of his choice to the Ring-bearer.
		scn.FreepsUseCardAction(eowyn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(chieftain, troop));
		scn.FreepsChooseCard(chieftain);

		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.IsCharAssignedAgainst(frodo, chieftain));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void MadeByAShadowCardToAssignTheRingBearerTheFreePeoplesPlayerWhoPicksTheChieftainGivesTheShadowInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));

		var chieftain = scn.GetShadowCard("chieftain");
		var troop = scn.GetShadowCard("troop");
		var endcomes = scn.GetShadowCard("endcomes");
		scn.MoveCardsToHand(endcomes);

		scn.StartGame();
		// Five [moria] minions for The End Comes to spot: the Troop and four Goblin Runners.
		scn.MoveMinionsToTable(chieftain, troop, scn.GetShadowCard("goblin1"), scn.GetShadowCard("goblin2"),
				scn.GetShadowCard("goblin3"), scn.GetShadowCard("goblin4"));
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.FreepsHasInitiative());

		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(endcomes));
		scn.ShadowPlayCard(endcomes);

		// The event is the Shadow player's, but the one who assigns is the Free Peoples player: he picks the minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(chieftain, troop));
		scn.FreepsChooseCard(chieftain);

		assertTrue(scn.IsCharAssignedAgainst(frodo, chieftain));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheInitiativeLastsTheTurnAndIsGivenAgainWhenTheChieftainIsAssignedOnALaterTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));

		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToAssignments();

		assertTrue(scn.FreepsHasInitiative());
		scn.FreepsAssignToMinions(aragorn, chieftain);
		assertTrue(scn.ShadowHasInitiative());

		// The Chieftain (9) beats Aragorn (8).
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The regroup phase, the Free Peoples hand still 4.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());

		// After a second move, in the Shadow phase at site 3, it is still so.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertFalse(scn.FreepsHasInitiative());

		// The next turn is player 2's: his fellowship, with a full hand, has the initiative again.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertTrue(scn.GetShadowHandCount() >= 4);
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());

		// Player 1's next turn: the same Chieftain, back on the table, is assigned again and gives it again.
		scn.SkipToOtherPlayersTurn();
		// Site 3 is a sanctuary, and Aragorn is wounded.
		scn.FreepsDeclineSanctuaryHealing();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToAssignments();
		assertTrue(scn.GetFreepsHandCount() >= 4);
		assertTrue(scn.FreepsHasInitiative());

		scn.FreepsAssignToMinions(aragorn, chieftain);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, chieftain));
		assertTrue(scn.GetFreepsHandCount() >= 4);
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
