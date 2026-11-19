package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_025_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("perspective", "18_25");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a Wizard companion; a strength bonus of his own
					// Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4; his maneuver ability is
					// never used.
					put("aragorn", "1_89");
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Wizard; a strength bonus at 3 Ents
					// Defend It and Hope: "Maneuver: Exert a [gondor] Man to wound a minion once (or twice if that
					// minion is an Uruk-hai)."
					put("defendit", "4_115");

					// Gollum, Dark as Darkness: a [gollum] minion of no race, vitality 4, with special abilities in
					// the Shadow, skirmish and regroup phases ("Skirmish: Remove a burden to add (2)", "Regroup:
					// Remove a burden to add (3)", "Regroup: Exert Gollum twice to wound the Ring-bearer").
					put("gollum", "9_28");
					// Uruk Slayer: an [isengard] Uruk-hai, strength 9, damage +1. "Skirmish: Remove (1) to make this
					// minion strength +1 (limit +3)."
					put("slayer", "1_153");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc of vitality 1; "When you play this minion, you may add (2)"
					// Saruman, Coldly Still: a [men] Wizard minion; his strength bonus is for [men] minions bearing
					// possessions, and his skirmish ability is never used.
					put("saruman", "17_48");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PerspectiveStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Perspective
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Maneuver:</b> Add 2 threats and spot a Wizard to spot a minion in play. That minion cannot use special abilities until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("perspective");

		assertEquals("Perspective", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AddsTwoThreatsAndTheMinionSpottedCannotUseSpecialAbilitiesUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var perspective = scn.GetFreepsCard("perspective");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(perspective);
		assertInZone(Zone.SUPPORT, perspective);

		var gollum = scn.GetShadowCard("gollum");
		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		// Burdens for Gollum to pay with.
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.MoveMinionsToTable(gollum, slayer);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsActionAvailable(perspective));
		scn.FreepsUseCardAction(perspective);
		// Three companions: room for both threats.
		assertEquals(2, scn.GetThreats());

		// Either minion, of two cultures and races; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gollum, slayer));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, gandalf, aragorn));
		scn.FreepsChooseCard(gollum);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, slayer}, new PhysicalCardImpl[]{gandalf, gollum});

		// The first skirmish: Gollum's skirmish ability is barred; the Slayer, not spotted, can use his.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.GetTwilight() >= 1);
		assertFalse(scn.ShadowActionAvailable(gollum));
		assertTrue(scn.ShadowActionAvailable(slayer));
		scn.ShadowPass();

		// The second skirmish, after the first has resolved: Gollum is still barred.
		scn.FreepsResolveSkirmish(gandalf);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(2, scn.GetBurdens());
		assertFalse(scn.ShadowActionAvailable(gollum));
		scn.ShadowPass();

		// The regroup phase: the bar is gone.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(gollum, "Add (3)"));
	}

	@Test
	public void IsNotOfferedWithoutAWizardAndTheShadowPlayersSarumanIsAWizardToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var perspective = scn.GetFreepsCard("perspective");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToSupportArea(perspective);
		assertInZone(Zone.SUPPORT, perspective);

		var troop = scn.GetShadowCard("troop");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Frodo and the Ent Avenger, a [gandalf] companion who is no Wizard: not offered, though both threats could be added.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(perspective));

		// The Shadow player's Saruman is a Wizard in play.
		scn.MoveMinionsToTable(saruman);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(perspective));
		scn.FreepsUseCardAction(perspective);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, saruman));
		scn.FreepsChooseCard(troop);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenOnlyOneMoreThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var perspective = scn.GetFreepsCard("perspective");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(perspective);
		assertInZone(Zone.SUPPORT, perspective);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AddThreats(1);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Two companions and one threat: only one more can be added.
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(perspective));

		// With the threat gone, both can be added.
		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsActionAvailable(perspective));
	}

	@Test
	public void WithNoMinionLeftInPlayItIsStillOfferedAndTheThreatsAreAddedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var perspective = scn.GetFreepsCard("perspective");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defendit = scn.GetFreepsCard("defendit");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(perspective);
		scn.MoveCardsToHand(defendit);
		assertInZone(Zone.SUPPORT, perspective);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// The only minion is killed: Aragorn exerts, and the Runner's one vitality goes.
		scn.FreepsPlayCard(defendit);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DISCARD, runner);
		scn.ShadowPass();

		// "Spot a minion in play" stands after "to": it is an effect, and the ability is offered with none to spot.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsActionAvailable(perspective));
		scn.FreepsUseCardAction(perspective);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
