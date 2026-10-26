package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_103_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("window", "10_103");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability only
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai; damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc; no text
					put("strength", "1_278");   // Strength Born of Fear: [sauron] support-area condition; a skirmish ability only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, unbound; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf, unbound, vitality 3; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound; a fellowship ability and a response to Frodo's death
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] ally; a fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Rivendell Waterfall as site 3, with Aragorn, a ranger: the move limit is 3 there, so a turn has two regroup moves.
	protected VirtualTableScenario GetWaterfallScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("window", "10_103");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a ranger; a maneuver ability only
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_342");   // Rivendell Waterfall
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

	@Test
	public void WindowoftheEyeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Window of the Eye
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a [sauron] minion.<br>Each time the fellowship moves during the regroup phase, you may remove a threat to wound an unbound companion.
		*/

		var scn = GetScenario();

		var window = scn.GetFreepsCard("window");

		assertEquals("Window of the Eye", window.getBlueprint().getTitle());
		assertNull(window.getBlueprint().getSubtitle());
		assertFalse(window.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, window.getBlueprint().getSide());
		assertEquals(Culture.SAURON, window.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, window.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(window, Keyword.SUPPORT_AREA));
		assertEquals(1, window.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilASauronMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var window = scn.GetShadowCard("window");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		var strength = scn.GetShadowCard("strength");
		scn.MoveCardsToHand(window, soldier);
		assertInHand(window, soldier);

		scn.StartGame();
		// A [sauron] card that is no minion, and a minion who is not [sauron].
		scn.MoveCardsToSupportArea(strength);
		scn.MoveMinionsToTable(troop);
		assertInZone(Zone.SUPPORT, strength);
		assertInPlay(troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(window));

		scn.ShadowPlayCard(soldier);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertTrue(scn.ShadowPlayAvailable(window));

		scn.ShadowPlayCard(window);
		assertInZone(Zone.SUPPORT, window);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASauronUrukHaiLetsItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var window = scn.GetShadowCard("window");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToHand(window);
		assertInHand(window);

		scn.StartGame();
		scn.MoveMinionsToTable(guard);
		assertInPlay(guard);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(window));
		scn.ShadowPlayCard(window);

		assertInZone(Zone.SUPPORT, window);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARegroupMoveOffersToRemoveAThreatToWoundAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var window = scn.GetShadowCard("window");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		scn.MoveCardsToSupportArea(window);
		assertInZone(Zone.SUPPORT, window);

		scn.FreepsPass();
		// No minion: the Shadow phase is followed by the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SkipToMovementDecision();
		// Exactly one threat, so that none is left to wound anyone when Gimli is killed.
		scn.AddThreats(1);
		// Vitality 3 with 2 wounds: exhausted, so the wound kills him.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(barliman));

		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(window));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetThreats());
		// Unbound companions of two cultures and races, the exhausted one among them; not the Ring-bearer, nor
		// a Ring-bound companion, nor an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, barliman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		assertInZone(Zone.DEAD, gimli);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFellowshipPhaseMoveIsNotAnsweredAndDecliningAtARegroupMoveLeavesTheThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var window = scn.GetShadowCard("window");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveCardsToSupportArea(window);
		assertInZone(Zone.SUPPORT, window);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The move at the end of the fellowship phase is no regroup move.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(window));
		assertEquals(1, scn.GetThreats());

		scn.SkipToPhase(Phase.REGROUP);
		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetThreats());

		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(window));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoThreatItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var window = scn.GetShadowCard("window");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveCardsToSupportArea(window);
		assertInZone(Zone.SUPPORT, window);

		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.SkipToMovementDecision();
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsChooseToMove();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(window));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void WithNoUnboundCompanionTheThreatIsRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var window = scn.GetShadowCard("window");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		scn.MoveCardsToSupportArea(window);
		assertInZone(Zone.SUPPORT, window);

		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.SkipToMovementDecision();
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(barliman));

		// The cost can be paid, so the trigger is offered though nobody can be wounded.
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(window));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachOfTwoRegroupMovesInOneTurnIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWaterfallScenario();

		var window = scn.GetShadowCard("window");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveCardsToSupportArea(window);
		assertInZone(Zone.SUPPORT, window);

		scn.FreepsPass();
		scn.SkipToMovementDecision();
		// Two companions, two threats.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The first regroup move, to Rivendell Waterfall.  Aragorn is the only unbound companion.
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(window));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetMoveLimit());

		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The second regroup move of the same turn.
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(window));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetMoveCount());
	}
}
