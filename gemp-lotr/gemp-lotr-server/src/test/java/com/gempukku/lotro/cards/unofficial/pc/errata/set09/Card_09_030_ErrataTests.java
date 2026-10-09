package com.gempukku.lotro.cards.unofficial.pc.errata.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_030_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smeagol", "59_30");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, unbound; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, unbound; a skirmish ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: [dwarven] Dwarf, unbound, no text but a requirement to play
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer; abilities used only on purpose
					put("gaffer", "1_291");     // The Gaffer: [shire] ally, home site 1; a fellowship ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; its text is about being played
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5; damage +1 and nothing else
					put("seal", "0_8004");      // Test Seal (the tests' own card): "Burdens cannot be added."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("frodo", "1_290");      // Frodo, Son of Drogo: here a plain companion, Ring-bound by rule; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, unbound
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, unbound
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; its text is about being played
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5; damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					put("fear", "1_278");       // Strength Born of Fear: [sauron] condition in the support area; a skirmish ability used only on purpose
					put("city", "7_56");        // The Dead City: bearer must be Smeagol; if he is about to be killed in a skirmish, he is discarded instead
					put("seal", "0_8004");      // Test Seal (the tests' own card): "Burdens cannot be added."
				}},
				VirtualTableScenario.FellowshipSites,
				"59_30",                        // Smeagol is the Ring-bearer
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SmeagolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Smeagol, Bearer of Great Secrets
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype:
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 8
		 * Can be the Ring-bearer: True
		 * Game Text: <b>Ring-bound.</b> To play, add a burden.
		 * 	Each time the fellowship moves, place an unbound companion in the dead pile.
		 * 	<b>Regroup:</b> If Sméagol is the Ring-bearer, add 2 burdens to discard each minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("smeagol");

		assertEquals("Smeagol", card.getBlueprint().getTitle());
		assertEquals("Bearer of Great Secrets", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getResistance());
		assertTrue(card.getBlueprint().canStartWithRing());
	}

	@Test
	public void SmeagolIsPlayedFromHandForNothingAndAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToHand(smeagol);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsPlayAvailable(smeagol));

		scn.FreepsPlayCard(smeagol);
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileBurdensCannotBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var seal = scn.GetFreepsCard("seal");
		scn.MoveCardsToHand(smeagol);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(smeagol));

		scn.MoveCardsToSupportArea(seal);
		scn.FreepsRefreshDecision();

		// The burden is a cost of playing him, and with the Seal it cannot be paid.
		assertFalse(scn.FreepsPlayAvailable(smeagol));
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.HAND, smeagol);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenTheFellowshipMovesAnUnboundCompanionOfTheOwnersChoiceIsPlacedInTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var gaffer = scn.GetFreepsCard("gaffer");
		scn.MoveCompanionsToTable(smeagol, aragorn, gimli, sam);
		scn.MoveAlliesToTable(gaffer);

		scn.StartGame();

		// Nothing happens while the fellowship stands still.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.FREE_CHARACTERS, aragorn, gimli);
		assertEquals(1, scn.GetCurrentSiteNumber());

		scn.FreepsPass();

		// Unbound companions only: not Sam or Smeagol (Ring-bound), not the Ring-bearer, not an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, smeagol, frodo, gaffer));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(gimli);

		assertInZone(Zone.DEAD, gimli);
		assertInZone(Zone.FREE_CHARACTERS, aragorn, sam, smeagol, frodo);
		assertInZone(Zone.SUPPORT, gaffer);
		assertEquals(2, scn.GetCurrentSiteNumber());
		// "When the fellowship moves" comes before the companions are counted for the move:
		// 2 for Ettenmoors + 4 companions left (Frodo, Smeagol, Sam, Aragorn).
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The fellowship stays at the regroup decision, and the turn ends with nobody else killed.
		scn.SkipToOtherPlayersTurn();

		assertInZone(Zone.DEAD, gimli);
		assertInZone(Zone.FREE_CHARACTERS, aragorn, sam, smeagol, frodo);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void ASecondMoveInTheSameTurnPlacesASecondCompanionInTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(smeagol, aragorn, gimli, guard);

		scn.StartGame();

		scn.FreepsPass();
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli, guard));
		scn.FreepsChooseCard(aragorn);

		assertInZone(Zone.DEAD, aragorn);
		assertInZone(Zone.FREE_CHARACTERS, gimli, guard);

		scn.SkipToMovementDecision();

		// Between the two moves nobody else has gone.
		assertInZone(Zone.FREE_CHARACTERS, gimli, guard);
		assertEquals(2, scn.GetCurrentSiteNumber());

		scn.FreepsChooseToMove();

		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		assertInZone(Zone.DEAD, aragorn, guard);
		assertInZone(Zone.FREE_CHARACTERS, gimli, smeagol);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoUnboundCompanionTheMoveKillsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		var gaffer = scn.GetFreepsCard("gaffer");
		scn.MoveCompanionsToTable(smeagol, sam);
		scn.MoveAlliesToTable(gaffer);

		scn.StartGame();

		assertEquals(1, scn.GetCurrentSiteNumber());

		scn.FreepsPass();

		// Sam and Smeagol are Ring-bound, Frodo is the Ring-bearer and The Gaffer is no companion.
		assertInZone(Zone.FREE_CHARACTERS, frodo, smeagol, sam);
		assertInZone(Zone.SUPPORT, gaffer);
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 2 for Ettenmoors + 3 companions.
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhileSmeagolIsTheRingBearerTheMoveTakesAnUnboundCompanionAndNeverFrodo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var smeagol = scn.GetRingBearer();
		var frodo = scn.GetFreepsCard("frodo");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(frodo, aragorn, gimli);

		scn.StartGame();

		scn.FreepsPass();

		// Every Frodo is Ring-bound, Ring-bearer or not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(aragorn);

		assertInZone(Zone.DEAD, aragorn);
		assertInZone(Zone.FREE_CHARACTERS, gimli, frodo, smeagol);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AsTheRingBearerSmeagolAddsTwoBurdensInTheRegroupPhaseToDiscardEachMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var smeagol = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var fear = scn.GetShadowCard("fear");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);
		scn.MoveCardsToSupportArea(fear);

		scn.SkipToPhase(Phase.MANEUVER);
		// A regroup action: not offered in the maneuver phase.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(smeagol));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, runner, savage);
		assertTrue(scn.FreepsActionAvailable(smeagol));

		scn.FreepsUseCardAction(smeagol);

		assertEquals(2, scn.GetBurdens());
		// Every minion, whatever its culture; a Shadow card that is no minion stays.
		assertInZone(Zone.DISCARD, runner, savage);
		assertInZone(Zone.SUPPORT, fear);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// With no minion left it may still be used: the burdens are a cost, and are paid for nothing.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsUseCardAction(smeagol);

		assertEquals(4, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, fear);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWhileSmeagolIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);

		scn.SkipToPhase(Phase.REGROUP);

		// Frodo is the Ring-bearer; Smeagol is only a companion.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(smeagol));
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, runner, savage);
	}

	@Test
	public void RegroupActionIsNotOfferedWhileBurdensCannotBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var smeagol = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var seal = scn.GetFreepsCard("seal");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsActionAvailable(smeagol));

		scn.MoveCardsToSupportArea(seal);
		scn.FreepsRefreshDecision();

		// The 2 burdens are the cost, and with the Seal they cannot be paid.
		assertFalse(scn.FreepsActionAvailable(smeagol));
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, runner, savage);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void StartingTheGameAsTheRingBearerSmeagolAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var smeagol = scn.GetRingBearer();
		assertEquals("Smeagol", smeagol.getBlueprint().getTitle());
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		// Before the game starts the only burden is the 1 that player 1 bid: the Ring-bearer is placed on
		// the table, not played, so "to play, add a burden" asks nothing of him.
		assertEquals(1, scn.GetBurdens());

		scn.StartGame();

		// The rig takes the bid burden off.
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AsTheRingBearerBearingTheDeadCitySmeagolIsKilledInASkirmishAndNotDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var smeagol = scn.GetRingBearer();
		var city = scn.GetFreepsCard("city");
		var troop = scn.GetShadowCard("troop");
		scn.AttachCardsTo(smeagol, city);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, troop);
		scn.FreepsResolveSkirmish(smeagol);

		// 3 + 1 for The One Ring against 13: overwhelmed, so about to be killed in a skirmish.
		assertEquals(4, scn.GetStrength(smeagol));
		assertEquals(13, scn.GetStrength(troop));
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertInZone(Zone.ATTACHED, city);
		assertFalse(scn.GameIsFinished());

		scn.PassSkirmishActions();

		// The Ring-bearer cannot be discarded, so The Dead City's "instead" cannot happen and he is killed.
		assertInZone(Zone.DEAD, smeagol);
		assertTrue(scn.GameIsFinished());
	}
}
