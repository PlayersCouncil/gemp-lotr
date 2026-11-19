package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_109_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("follow", "1_109");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("rohanaragorn", "17_93"); // Aragorn, Defender of Rohan: the [rohan] Aragorn; hunter 1 only while the Ring-bearer is assigned
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion who is no Aragorn; a skirmish ability
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: [elven] Elf ally, home site 3, strength 8, vitality 4
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] Man ally, home site 1, strength 1; a fellowship ability

					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, strength 7, vitality 1, nothing else
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OneWhomMenWouldFollowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: One Whom Men Would Follow
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert Aragorn to spot an ally. Until the regroup phase, that ally is strength +2 and participates in archery fire and skirmishes.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("follow");

		assertEquals("One Whom Men Would Follow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAragornSoThatTheAllySpottedIsStrengthPlus2AndTakesPartInArcheryAndSkirmishesUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var follow = scn.GetFreepsCard("follow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elrond = scn.GetFreepsCard("elrond");
		var barliman = scn.GetFreepsCard("barliman");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(elrond, barliman);
		scn.MoveCardsToHand(follow);

		var marksman = scn.GetShadowCard("marksman");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(marksman, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// Site 2: Elrond's home is site 3 and Barliman's site 1, so neither takes part of his own accord.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(elrond));
		assertEquals(1, scn.GetStrength(barliman));
		assertFalse(scn.FreepsCanAssign(elrond));
		assertFalse(scn.FreepsCanAssign(barliman));
		assertTrue(scn.FreepsPlayAvailable(follow));

		// Aragorn, the one Aragorn, exerts unasked.
		scn.FreepsPlayCard(follow);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// An ally of either culture and race; not a companion, nor a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(elrond, barliman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo, marksman, savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(elrond);

		assertEquals(10, scn.GetStrength(elrond));
		assertEquals(1, scn.GetStrength(barliman));
		assertTrue(scn.FreepsCanAssign(elrond));
		assertFalse(scn.FreepsCanAssign(barliman));
		assertInDiscard(follow);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// The Marksman's archery wound may be placed on Elrond, not on Barliman.
		scn.SkipToArcheryWounds();
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo, elrond));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman));
		assertEquals(0, scn.GetWoundsOn(elrond));
		scn.FreepsChooseCard(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));

		scn.PassAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(elrond));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(barliman));
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, marksman },
				new PhysicalCardImpl[] { elrond, savage }
		);

		// Aragorn's skirmish first: 8 against 7, and the Marksman (vitality 1) dies.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(marksman);

		// Elrond's skirmish follows one already resolved: the bonus still holds, 10 against 5.
		scn.FreepsResolveSkirmish(elrond);
		assertTrue(scn.IsCharSkirmishingAgainst(elrond, savage));
		assertEquals(10, scn.GetStrength(elrond));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		scn.PassSkirmishActions();
		// 10 is double 5: the Savage is overwhelmed (at his printed 8 Elrond would only wound him).
		assertInDiscard(savage);
		assertEquals(1, scn.GetWoundsOn(elrond));

		// At the regroup phase both are gone.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(8, scn.GetStrength(elrond));
		assertFalse(scn.FreepsCanAssign(elrond));
	}

	@Test
	public void TheRohanAragornPaysTheExertion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var follow = scn.GetFreepsCard("follow");
		var aragorn = scn.GetFreepsCard("rohanaragorn");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(follow);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetStrength(barliman));
		assertTrue(scn.FreepsPlayAvailable(follow));

		// Aragorn exerts and Barliman, the one ally, is spotted unasked.
		scn.FreepsPlayCard(follow);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetStrength(barliman));
		assertInDiscard(follow);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileAragornIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var follow = scn.GetFreepsCard("follow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(follow);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInHand(follow);
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// Vitality 4 with 3 wounds: Aragorn cannot exert. Boromir, a [gondor] companion, could, but he is no Aragorn;
		// an ally is there to spot.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertInHand(follow);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(follow));
	}

	@Test
	public void WithNoAllyInPlayItIsPlayedAndAragornExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var follow = scn.GetFreepsCard("follow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(follow);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.FreepsPlayAvailable(follow));

		scn.FreepsPlayCard(follow);

		// The spot comes after "to": with no ally the event is still played, and no companion takes the bonus.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertInDiscard(follow);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
