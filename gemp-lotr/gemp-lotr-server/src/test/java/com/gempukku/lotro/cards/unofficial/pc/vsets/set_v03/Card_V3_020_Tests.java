package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_020_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("domain", "103_20");
					put("shards", "3_44");      // The Shards of Narsil: another [gondor] artifact for the support area; fellowship abilities never used here
					put("king", "8_38");        // King of the Dead, Oathbreaker: a [gondor] Wraith, unique, vitality 4; his response is about Aragorn
					put("deadman1", "10_27");   // Dead Man of Dunharrow: a [gondor] Wraith, vitality 3; his text is about being played
					put("deadman2", "10_27");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man, vitality 3; a skirmish ability never used here
					put("legolas", "1_50");     // Legolas, Greenleaf: an archer, for a fellowship archery total of 1; an archery ability never used here
					put("guard", "1_7");        // Dwarf Guard: vitality 2, no text in play; the companion who is killed

					put("slayer1", "3_93");     // Morgul Slayer: Regroup: exert this minion to wound a companion (except the Ring-bearer)
					put("slayer2", "3_93");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: an [isengard] minion, vitality 3; damage +1 is its whole text
					put("marksman1", "1_176");  // Goblin Marksman: archer and nothing else
					put("marksman2", "1_176");
					put("corpse", "6_99");      // Corpse Lights: a [sauron] minion who is a Wraith, vitality 2; a skirmish ability never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DomainoftheDeadKingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Domain of the Dead King
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Artifact
		 * Subtype: Support Area
		 * Game Text: When you play this, spot a [gondor] Wraith to remove a threat.
		 * 	Each time a [gondor] Wraith takes a threat wound, wound a minion.
		 * 	Response: If an exhausted [gondor] Wraith is about to take a wound, add a threat and hinder this artifact to hinder that Wraith.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("domain");

		assertEquals("Domain of the Dead King", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedToTheSupportAreaSpottingAGondorWraithToRemoveOneThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var domain = scn.GetFreepsCard("domain");
		var shards = scn.GetFreepsCard("shards");
		var king = scn.GetFreepsCard("king");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(king, boromir);
		scn.MoveCardsToHand(domain, shards);

		scn.StartGame();
		scn.AddThreats(2);

		assertEquals(2, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(domain));
		scn.FreepsPlayCard(domain);

		assertInZone(Zone.SUPPORT, domain);
		assertNotAttachedTo(domain, king);
		assertEquals(1, scn.GetThreats());

		// Another artifact played beside it is not "this".
		scn.FreepsPlayCard(shards);

		assertInZone(Zone.SUPPORT, shards);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedWithOnlyAGondorManToSpotNoThreatIsRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var domain = scn.GetFreepsCard("domain");
		var boromir = scn.GetFreepsCard("boromir");
		// A [gondor] companion who is no Wraith. (Every Wraith a fellowship phase can have in play is [gondor].)
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(domain);

		scn.StartGame();
		scn.AddThreats(2);

		assertTrue(scn.FreepsPlayAvailable(domain));
		scn.FreepsPlayCard(domain);

		assertInZone(Zone.SUPPORT, domain);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachThreatWoundOnAGondorWraithWoundsAMinionOfTheFreePeoplesPlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var domain = scn.GetFreepsCard("domain");
		var deadman = scn.GetFreepsCard("deadman1");
		var boromir = scn.GetFreepsCard("boromir");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();
		var slayer = scn.GetShadowCard("slayer1");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(deadman, boromir, guard);
		scn.MoveCardsToSupportArea(domain);

		scn.StartGame();
		// Vitality 2 with 1 wound: the Slayer's wound kills him.
		scn.AddWoundsToChar(guard, 1);
		scn.AddThreats(3);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer, troop, savage);

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(guard);

		// The Guard is dead, and the 3 threats have become 3 wounds for the Free Peoples player to place.
		assertInZone(Zone.DEAD, guard);
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(deadman));
		assertEquals(0, scn.GetWoundsOn(savage));

		// The first goes to the Dead Man.
		scn.FreepsChooseCard(deadman);
		assertEquals(1, scn.GetWoundsOn(deadman));

		assertTrue(scn.FreepsHasCardChoiceAvailable(slayer, troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(deadman, boromir, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));

		// The second goes to the same Dead Man, and wounds a minion again.
		scn.FreepsChooseCard(deadman);
		assertEquals(2, scn.GetWoundsOn(deadman));

		assertTrue(scn.FreepsHasCardChoiceAvailable(slayer, troop, savage));
		scn.FreepsChooseCard(savage);
		assertEquals(2, scn.GetWoundsOn(savage));

		// The third goes to Boromir, a [gondor] Man: nothing more.
		scn.FreepsChooseCard(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));

		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		// The Slayer's own exertion.
		assertEquals(1, scn.GetWoundsOn(slayer));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnlyAThreatWoundAndOnlyOnAGondorWraithWoundsAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var domain = scn.GetFreepsCard("domain");
		var deadman = scn.GetFreepsCard("deadman1");
		var boromir = scn.GetFreepsCard("boromir");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();
		var slayer1 = scn.GetShadowCard("slayer1");
		var slayer2 = scn.GetShadowCard("slayer2");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(deadman, boromir, guard);
		scn.MoveCardsToSupportArea(domain);

		scn.StartGame();
		scn.AddWoundsToChar(guard, 1);
		scn.AddThreats(2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer1, slayer2, troop);

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer1);
		scn.ShadowChooseCard(guard);

		assertInZone(Zone.DEAD, guard);

		// One threat wound for the [gondor] Man: the next thing asked is where the second goes.
		scn.FreepsChooseCard(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, deadman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop, slayer2));

		// And one for Frodo, a companion who is neither [gondor] nor a Wraith.
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(slayer2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// A wound on a [gondor] Wraith that is no threat wound.
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer2);
		scn.ShadowChooseCard(deadman);

		assertEquals(1, scn.GetWoundsOn(deadman));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(slayer1));
		assertEquals(1, scn.GetWoundsOn(slayer2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnExhaustedGondorWraithAboutToBeWoundedIsHinderedForAThreatAndTheArtifactAndTakesNoWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var domain = scn.GetFreepsCard("domain");
		var shards = scn.GetFreepsCard("shards");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var deadman2 = scn.GetFreepsCard("deadman2");
		var boromir = scn.GetFreepsCard("boromir");
		var slayer = scn.GetShadowCard("slayer1");
		scn.MoveCompanionsToTable(deadman1, deadman2, boromir);
		scn.MoveCardsToSupportArea(domain, shards);

		scn.StartGame();
		// Vitality 3 with 2 wounds: exhausted, both of them.
		scn.AddWoundsToChar(deadman1, 2);
		scn.AddWoundsToChar(deadman2, 2);
		// Frodo and three more: there is room for exactly one more threat.
		scn.AddThreats(3);
		assertEquals(4, scn.GetThreatLimit());
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer);

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(deadman1);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(domain));
		assertEquals(3, scn.GetThreats());
		assertFalse(scn.IsHindered(domain));
		assertFalse(scn.IsHindered(deadman1));
		assertEquals(2, scn.GetWoundsOn(deadman1));

		scn.FreepsChooseOptionalTrigger(domain);

		assertEquals(4, scn.GetThreats());
		assertTrue(scn.IsHindered(domain));
		assertTrue(scn.IsHindered(deadman1));
		// A hindered character cannot be wounded: the wound is not placed.
		assertEquals(2, scn.GetWoundsOn(deadman1));
		assertInZone(Zone.FREE_CHARACTERS, deadman1);
		// The other exhausted Wraith and the other artifact are untouched.
		assertFalse(scn.IsHindered(deadman2));
		assertFalse(scn.IsHindered(shards));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AWraithWithVitalityToSpareTakesItsWoundAndTheResponseComesOnlyWithTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var domain = scn.GetFreepsCard("domain");
		var deadman = scn.GetFreepsCard("deadman1");
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		scn.MoveCompanionsToTable(deadman);
		scn.MoveCardsToSupportArea(domain);

		scn.StartGame();
		// Vitality 3 with 1 wound: one short of exhausted.
		scn.AddWoundsToChar(deadman, 1);
		scn.MoveMinionsToTable(marksman1, marksman2);
		scn.SkipToArcheryWounds();

		assertEquals(2, scn.GetShadowArcheryTotal());

		// The first archery wound lands with no response.
		scn.FreepsChooseCard(deadman);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(deadman));
		assertFalse(scn.IsHindered(deadman));
		assertEquals(0, scn.GetThreats());

		// He is now exhausted, and the second is answered.
		scn.FreepsChooseCard(deadman);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(domain));
		scn.FreepsChooseOptionalTrigger(domain);

		assertTrue(scn.IsHindered(deadman));
		assertTrue(scn.IsHindered(domain));
		assertEquals(2, scn.GetWoundsOn(deadman));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedForAnExhaustedGondorManNorForAnExhaustedWraithOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var domain = scn.GetFreepsCard("domain");
		var deadman = scn.GetFreepsCard("deadman1");
		var boromir = scn.GetFreepsCard("boromir");
		var legolas = scn.GetFreepsCard("legolas");
		var corpse = scn.GetShadowCard("corpse");
		var marksman = scn.GetShadowCard("marksman1");
		scn.MoveCompanionsToTable(deadman, boromir, legolas);
		scn.MoveCardsToSupportArea(domain);

		scn.StartGame();
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(boromir, 2);
		// The text names no side, and the only Wraiths who are not [gondor] are the Shadow player's: Corpse Lights
		// are a [sauron] Wraith. Vitality 2 with 1 wound: exhausted.
		scn.MoveMinionsToTable(corpse, marksman);
		scn.AddWoundsToChar(corpse, 1);
		scn.SkipToArcheryWounds();

		assertTrue(scn.IsExhausted(corpse));
		assertTrue(scn.IsExhausted(boromir));
		assertEquals(0, scn.GetThreats());

		// The Marksman's archery wound, placed on the exhausted [gondor] Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, deadman));
		scn.FreepsChooseCard(boromir);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DEAD, boromir);

		// Legolas's, placed by the Shadow player on his exhausted Wraith.
		assertTrue(scn.ShadowHasCardChoiceAvailable(corpse, marksman));
		scn.ShadowChooseCard(corpse);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, corpse);

		assertFalse(scn.IsHindered(domain));
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(deadman));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenNoThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var domain = scn.GetFreepsCard("domain");
		var deadman = scn.GetFreepsCard("deadman1");
		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		var slayer = scn.GetShadowCard("slayer1");
		scn.MoveCompanionsToTable(deadman, boromir);
		scn.MoveCardsToSupportArea(domain);

		scn.StartGame();
		scn.AddWoundsToChar(deadman, 2);
		// Three companions and three threats: no more can be added.
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreatLimit());
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer);

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(deadman);

		// No response: he takes the wound and dies, and the threats come down as wounds on the others.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DEAD, deadman);
		assertFalse(scn.IsHindered(domain));
		assertEquals(0, scn.GetThreats());

		scn.FreepsChooseCard(frodo);
		scn.FreepsChooseCard(frodo);
		scn.FreepsChooseCard(boromir);

		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheResponseLeavesTheWraithToTakeTheWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var domain = scn.GetFreepsCard("domain");
		var king = scn.GetFreepsCard("king");
		var boromir = scn.GetFreepsCard("boromir");
		var slayer = scn.GetShadowCard("slayer1");
		scn.MoveCompanionsToTable(king, boromir);
		scn.MoveCardsToSupportArea(domain);

		scn.StartGame();
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(king, 3);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer);

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(king);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(domain));
		scn.FreepsDeclineOptionalTrigger();

		assertFalse(scn.IsHindered(domain));
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.DEAD, king);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhileTheArtifactIsHinderedNoResponseIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var domain = scn.GetFreepsCard("domain");
		var king = scn.GetFreepsCard("king");
		var boromir = scn.GetFreepsCard("boromir");
		var slayer = scn.GetShadowCard("slayer1");
		scn.MoveCompanionsToTable(king, boromir);
		scn.MoveCardsToSupportArea(domain);

		scn.StartGame();
		scn.AddWoundsToChar(king, 3);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer);
		scn.HinderCard(domain);

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(king);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DEAD, king);
		assertTrue(scn.IsHindered(domain));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AThreatWoundOnAnExhaustedGondorWraithMayBeTurnedAsideAndThenWoundsNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var domain = scn.GetFreepsCard("domain");
		var deadman = scn.GetFreepsCard("deadman1");
		var boromir = scn.GetFreepsCard("boromir");
		var guard = scn.GetFreepsCard("guard");
		var slayer = scn.GetShadowCard("slayer1");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(deadman, boromir, guard);
		scn.MoveCardsToSupportArea(domain);

		scn.StartGame();
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(deadman, 2);
		scn.AddThreats(1);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer, troop);

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(guard);

		assertInZone(Zone.DEAD, guard);
		assertEquals(0, scn.GetThreats());

		// The one threat wound is put on the exhausted Dead Man.
		scn.FreepsChooseCard(deadman);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(domain));
		scn.FreepsChooseOptionalTrigger(domain);

		assertTrue(scn.IsHindered(deadman));
		assertTrue(scn.IsHindered(domain));
		assertEquals(2, scn.GetWoundsOn(deadman));
		assertInZone(Zone.FREE_CHARACTERS, deadman);
		assertEquals(1, scn.GetThreats());
		// He took no threat wound (and the artifact is hindered besides): no minion is wounded.
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(slayer));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
