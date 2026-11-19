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

public class Card_07_135_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sneak", "7_135");
					put("sneak2", "7_135");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text
					put("goblin1", "1_178");    // Goblin Runner: [moria] minion; adds (2) only when played, and is cheated in here
					put("goblin2", "1_178");
					put("goblin3", "1_178");
					put("goblin4", "1_178");
					// The End Comes: "Assignment: Spot 5 [moria] minions to make the Free Peoples player assign the
					// Ring-bearer to a skirmish"
					put("endcomes", "1_169");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a maneuver ability only
					put("boromir", "1_96");     // Boromir, Lord of Gondor: vitality 3; his text is about being overwhelmed
					// Éowyn, Northwoman: "Assignment: Exert Éowyn and assign a minion to the Ring-bearer to make that
					// minion lose all game text keywords ..."
					put("eowyn", "17_96");
					put("bounder", "1_286");    // Bounder: Hobbit ally whose home is site 2 of this path
					// Unexpected Visitor: on a [gondor] companion, gives each minion "Assignment: Assign this minion to bearer"
					put("visitor", "7_126");
					// Gimli, Son of Glóin: only ever held in a hand here, to count toward initiative
					put("hand1", "1_13");
					put("hand2", "1_13");
					put("hand3", "1_13");
					put("hand4", "1_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesertSneakStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Desert Sneak
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. <b>Ambush (1)</b>.<br>When the Free Peoples player assigns this minion to a character and you have initiative, you may wound that character.
		*/

		var scn = GetScenario();

		var sneak = scn.GetFreepsCard("sneak");

		assertEquals("Desert Sneak", sneak.getBlueprint().getTitle());
		assertNull(sneak.getBlueprint().getSubtitle());
		assertFalse(sneak.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, sneak.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, sneak.getBlueprint().getCulture());
		assertEquals(CardType.MINION, sneak.getBlueprint().getCardType());
		assertEquals(Race.MAN, sneak.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(sneak, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(sneak, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(sneak, Keyword.AMBUSH));
		assertEquals(1, sneak.getBlueprint().getTwilightCost());
		assertEquals(5, sneak.getBlueprint().getStrength());
		assertEquals(2, sneak.getBlueprint().getVitality());
		assertEquals(4, sneak.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignedByTheFreePeoplesPlayerHoldingThreeCardsItMayWoundThatCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var sneak = scn.GetShadowCard("sneak");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(sneak, troop);
		scn.SkipToAssignments();
		// Three cards in the Free Peoples hand: the Shadow has initiative.  The Shadow player's own four cards
		// count for nothing.
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"));
		scn.MoveCardsToHand(scn.GetShadowCard("hand1"), scn.GetShadowCard("hand2"), scn.GetShadowCard("hand3"), scn.GetShadowCard("hand4"));

		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetShadowHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));

		// The Troop goes to Boromir in the same answer: only the Sneak's assignment sets anything off.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, sneak}, new PhysicalCardImpl[]{boromir, troop});

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak, "Optional trigger"));
		scn.ShadowChooseOptionalTrigger(sneak, "Optional trigger");

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, sneak));

		// The ambush offer is still to be answered.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak, "Ambush"));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void WithFourCardsInTheFreePeoplesHandItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var sneak = scn.GetShadowCard("sneak");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(sneak, troop);
		scn.SkipToAssignments();
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));

		// Four cards: the Free Peoples player has initiative.
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, sneak}, new PhysicalCardImpl[]{boromir, troop});

		// Ambush is offered, and nothing else.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak, "Ambush"));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(sneak, "Optional trigger"));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, sneak));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void DecliningTheOfferWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var sneak = scn.GetShadowCard("sneak");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(sneak, troop);
		scn.SkipToAssignments();

		assertTrue(scn.ShadowHasInitiative());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, sneak}, new PhysicalCardImpl[]{boromir, troop});

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak, "Optional trigger"));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak, "Ambush"));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedByTheShadowPlayerItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var sneak = scn.GetShadowCard("sneak");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(sneak, troop);
		scn.SkipToAssignments();

		assertTrue(scn.ShadowHasInitiative());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The Free Peoples player leaves the Sneak unassigned, and the Shadow player assigns it himself.
		scn.FreepsAssignToMinions(boromir, troop);
		scn.ShadowAssignToMinions(aragorn, sneak);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, sneak));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedByTheShadowPlayersOwnAssignmentActionItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var visitor = scn.GetFreepsCard("visitor");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, visitor);

		var sneak = scn.GetShadowCard("sneak");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(sneak, troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertSame(aragorn, visitor.getAttachedTo());
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Unexpected Visitor gives the minion an assignment action, and the one who uses it is the Shadow player.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(sneak));
		scn.ShadowUseCardAction(sneak);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, sneak));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignedToAnAllyItWoundsTheAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);

		var sneak = scn.GetShadowCard("sneak");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(sneak, troop);
		// The fellowship has moved to site 2, the Bounder's home, where he may be assigned.
		scn.SkipToAssignments();

		assertTrue(scn.ShadowHasInitiative());
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bounder, sneak}, new PhysicalCardImpl[]{aragorn, troop});

		assertTrue(scn.IsCharAssignedAgainst(bounder, sneak));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak, "Optional trigger"));
		scn.ShadowChooseOptionalTrigger(sneak, "Optional trigger");

		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedByTheFreePeoplesPlayersOwnCardToTheRingBearerItWoundsHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(eowyn);

		var sneak = scn.GetShadowCard("sneak");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(sneak, troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.ShadowHasInitiative());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(eowyn));

		// Éowyn exerts and the Free Peoples player assigns a minion of his choice to the Ring-bearer.
		scn.FreepsUseCardAction(eowyn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(sneak, troop));
		scn.FreepsChooseCard(sneak);

		assertTrue(scn.IsCharAssignedAgainst(frodo, sneak));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak, "Optional trigger"));
		scn.ShadowChooseOptionalTrigger(sneak, "Optional trigger");

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak, "Ambush"));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void MadeByAShadowCardToAssignTheRingBearerTheFreePeoplesPlayerWhoPicksTheSneakIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var sneak = scn.GetShadowCard("sneak");
		var troop = scn.GetShadowCard("troop");
		var endcomes = scn.GetShadowCard("endcomes");

		scn.StartGame();
		scn.MoveCardsToHand(endcomes);
		// Five [moria] minions for The End Comes to spot: the Troop and four Goblin Runners.
		scn.MoveMinionsToTable(sneak, troop, scn.GetShadowCard("goblin1"), scn.GetShadowCard("goblin2"),
				scn.GetShadowCard("goblin3"), scn.GetShadowCard("goblin4"));
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.ShadowHasInitiative());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(endcomes));
		scn.ShadowPlayCard(endcomes);

		// The event is the Shadow player's, but the one who assigns is the Free Peoples player: he picks the minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sneak, troop));
		scn.FreepsChooseCard(sneak);

		assertTrue(scn.IsCharAssignedAgainst(frodo, sneak));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak, "Optional trigger"));
		scn.ShadowChooseOptionalTrigger(sneak, "Optional trigger");

		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignedToAnExhaustedCompanionTheWoundKillsHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var sneak = scn.GetShadowCard("sneak");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AddWoundsToChar(boromir, 2);
		scn.MoveMinionsToTable(sneak, troop);
		scn.SkipToAssignments();

		// Vitality 3 with 2 wounds: exhausted, so a wound kills him where an exertion could not be made.
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowHasInitiative());

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, sneak}, new PhysicalCardImpl[]{aragorn, troop});
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak, "Optional trigger"));
		scn.ShadowChooseOptionalTrigger(sneak, "Optional trigger");

		assertInZone(Zone.DEAD, boromir);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void EachCopyAssignedToACompanionWoundsItsOwnCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var sneak = scn.GetShadowCard("sneak");
		var sneak2 = scn.GetShadowCard("sneak2");

		scn.StartGame();
		scn.MoveMinionsToTable(sneak, sneak2);
		scn.SkipToAssignments();

		assertTrue(scn.ShadowHasInitiative());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, sneak}, new PhysicalCardImpl[]{boromir, sneak2});

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak, "Optional trigger"));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak2, "Optional trigger"));
		scn.ShadowChooseOptionalTrigger(sneak2, "Optional trigger");
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak, "Optional trigger"));
		scn.ShadowChooseOptionalTrigger(sneak, "Optional trigger");
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));

		// The two ambush offers are left, and are declined together.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(sneak, "Optional trigger"));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(sneak2, "Optional trigger"));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak, "Ambush"));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak2, "Ambush"));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedAgainInASecondAssignmentPhaseItIsOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var sneak = scn.GetShadowCard("sneak");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(sneak, troop);
		scn.SkipToAssignments();

		assertTrue(scn.ShadowHasInitiative());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Aragorn (8) beats the Sneak (5, vitality 2), who lives through the wound; Boromir (7) loses to the
		// Troop (13) without being overwhelmed.  Both minions live on into the second assignment phase.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, sneak}, new PhysicalCardImpl[]{boromir, troop});
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak, "Optional trigger"));
		scn.ShadowChooseOptionalTrigger(sneak, "Optional trigger");
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.ShadowDeclineOptionalTrigger();

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(sneak));
		assertEquals(1, scn.GetWoundsOn(boromir));

		// The fellowship moves again in the same turn, to a second assignment phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertInZone(Zone.SHADOW_CHARACTERS, sneak);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		scn.SkipToAssignments();

		assertTrue(scn.ShadowHasInitiative());
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, sneak}, new PhysicalCardImpl[]{boromir, troop});
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak, "Optional trigger"));
		scn.ShadowChooseOptionalTrigger(sneak, "Optional trigger");

		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
