package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_017_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("urgency", "19_17");
					put("urgency2", "19_17");

					// Ent Avenger: a [gandalf] Ent companion; a strength bonus only while 3 Ents can be spotted.
					put("avenger", "6_27");
					// Huorn: a [gandalf] Tree companion, no Ent.  Its strength bonus and its Response reach no reading here.
					put("huorn", "9_25");
					// Treebeard, Earthborn: a [gandalf] Ent ally; a Response and a fellowship ability about unbound
					// Hobbits, never used.
					put("treebeard", "4_103");
					// Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4; a maneuver ability never used.
					put("aragorn", "1_89");
					put("sword", "1_299");      // Hobbit Sword: a card for the Free Peoples hand; no text but its bearer

					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, strength 13, no text
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, strength 5; Damage +1 is its whole text
					// Morgul Hound: a [wraith] Orc, strength 5.  Strength +4 and fierce only at 2 wounds on the
					// Ring-bearer, who has none here.
					put("hound", "7_192");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrgencyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Urgency
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Weather</b>.<br>Each time the fellowship moves, add (2) for each Ent you can spot.<br><b>Skirmish:</b> Discard this condition from play to make a non-[wraith] minion strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("urgency");

		assertEquals("Urgency", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.WEATHER));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachMoveAddsTwoForEachEntCountedAtThatMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var urgency = scn.GetShadowCard("urgency");
		var avenger = scn.GetFreepsCard("avenger");
		var huorn = scn.GetFreepsCard("huorn");
		var treebeard = scn.GetFreepsCard("treebeard");
		scn.MoveCardsToSupportArea(urgency);
		scn.MoveCompanionsToTable(avenger, huorn);
		assertInZone(Zone.SUPPORT, urgency);
		assertInZone(Zone.FREE_CHARACTERS, avenger, huorn);

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		// To Ettenmoors: 2 for its Shadow number + 3 for Frodo, the Ent Avenger and the Huorn + 2 for the one Ent.
		// The Huorn is a Tree and no Ent.
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.MoveAlliesToTable(treebeard);
		assertInZone(Zone.SUPPORT, treebeard);
		int pool = scn.GetTwilight();

		scn.FreepsChooseToMove();
		// To Rivendell Valley: 0 for its Shadow number + 3 for the companions + 4 for the Ent companion and the Ent ally.
		assertEquals(pool + 7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingAtTheRegroupDecisionAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var urgency = scn.GetShadowCard("urgency");
		var avenger = scn.GetFreepsCard("avenger");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(avenger);

		scn.StartGame();
		scn.FreepsPass();
		// Put down after the turn's first move, which it would otherwise answer.
		scn.MoveCardsToSupportArea(urgency);
		assertInZone(Zone.SUPPORT, urgency);

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		// A card in hand, so that the Free Peoples player is asked to reconcile and the turn does not end at once.
		scn.SetFreepsHand(sword);
		int pool = scn.GetTwilight();

		scn.FreepsChooseToStay();
		assertEquals(pool, scn.GetTwilight());
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
	}

	@Test
	public void SkirmishAbilityDiscardsItToMakeANonWraithMinionStrengthPlusOneForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var urgency = scn.GetShadowCard("urgency");
		var urgency2 = scn.GetShadowCard("urgency2");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCardsToSupportArea(urgency, urgency2);
		scn.MoveCompanionsToTable(aragorn, avenger);
		assertInZone(Zone.SUPPORT, urgency, urgency2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{avenger, hound});
		// The Savage is left out of every skirmish.
		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(savage));

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.ShadowActionAvailable(urgency));

		scn.ShadowUseCardAction(urgency);
		// The copy used is the one discarded; the other stays.
		assertInDiscard(urgency);
		assertInZone(Zone.SUPPORT, urgency2);
		// A minion of any culture but [wraith]: the [moria] Orc in this skirmish and the [isengard] Uruk-hai in
		// none.  Not the [wraith] Orc, nor Aragorn, who is no minion.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, aragorn));
		scn.ShadowChooseCard(troop);

		assertEquals(14, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(hound));
		assertInZone(Zone.SUPPORT, urgency2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 14 against 8: Aragorn loses and takes a wound.  In the next skirmish, a phase of its own, the Troop is
		// back to its printed 13.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(avenger);
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var urgency = scn.GetShadowCard("urgency");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(urgency);
		scn.MoveCompanionsToTable(aragorn);
		assertInZone(Zone.SUPPORT, urgency);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(urgency));
		assertInZone(Zone.SUPPORT, urgency);
	}
}
