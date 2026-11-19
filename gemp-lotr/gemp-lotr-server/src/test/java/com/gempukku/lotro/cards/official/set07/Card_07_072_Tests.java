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

public class Card_07_072_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smeagol", "7_72");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability never used here
					put("fighter1", "7_286");   // Mordor Fighter: [sauron] Orc, strength 7, vitality 2; gives the Shadow initiative while a threat and another [sauron] Orc can be spotted
					put("fighter2", "7_286");
					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, damage +1 and nothing else
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; adds (2) when played, which it never is here

					// Cards for the hand, one of each kind it can hold.  None is played.
					put("guard", "1_7");        // Dwarf Guard: a companion
					put("bow", "1_41");         // Elven Bow: a possession
					put("defiance", "1_37");    // Defiance: a Free Peoples event
					put("troop", "1_177");      // Goblin Patrol Troop: a Shadow minion
					put("runner2", "1_178");    // Goblin Runner: a second Shadow minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SmeagolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Sméagol, Hurried Guide
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype:
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: <b>Ring-bound</b>. To play, add a burden.<br><b>Skirmish:</b> If you have initiative, discard 2 cards from your hand. Sméagol cannot be overwhelmed unless his strength is tripled.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("smeagol");

		assertEquals("Sméagol", card.getBlueprint().getTitle());
		assertEquals("Hurried Guide", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void PlayingSmeagolAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");

		scn.MoveCardsToHand(smeagol);

		scn.StartGame();
		assertInHand(smeagol);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsPlayAvailable(smeagol));
		scn.FreepsPlayCard(smeagol);

		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsTwoCardsOfAnyKindAndSmeagolLivesThroughALossAtMoreThanDouble() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var bow = scn.GetFreepsCard("bow");
		var defiance = scn.GetFreepsCard("defiance");
		var troop = scn.GetFreepsCard("troop");
		var runner2 = scn.GetFreepsCard("runner2");
		var fighter = scn.GetShadowCard("fighter1");
		scn.MoveCompanionsToTable(smeagol, aragorn);

		scn.StartGame();
		scn.MoveCardsToHand(guard, bow, defiance, troop, runner2);
		assertInHand(guard, bow, defiance, troop, runner2);
		scn.MoveMinionsToTable(fighter);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, fighter);
		scn.FreepsResolveSkirmish(smeagol);

		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(2, scn.GetOverwhelmMultiplier(smeagol));
		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsUseCardAction(smeagol);

		// "2 cards" names no kind: a companion, a possession, an event and Shadow minions are all offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, bow, defiance, troop, runner2));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(defiance, troop);

		assertInDiscard(defiance, troop);
		assertInHand(guard, bow, runner2);
		assertEquals(3, scn.GetFreepsHandCount());
		// Sméagol alone is protected; Aragorn and Frodo keep the usual double.
		assertEquals(3, scn.GetOverwhelmMultiplier(smeagol));
		assertEquals(2, scn.GetOverwhelmMultiplier(aragorn));
		assertEquals(2, scn.GetOverwhelmMultiplier(frodo));

		scn.PassCurrentPhaseActions();

		// Strength 7 against 3 is more than double and less than triple: one wound for losing, and no overwhelm.
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedFromThreeCardsInHandAndFromExactlyFourItDiscardsTwoAndStillProtects() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var guard = scn.GetFreepsCard("guard");
		var bow = scn.GetFreepsCard("bow");
		var defiance = scn.GetFreepsCard("defiance");
		var troop = scn.GetFreepsCard("troop");
		var fighter = scn.GetShadowCard("fighter1");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.MoveCardsToHand(guard, bow, defiance);
		scn.MoveMinionsToTable(fighter);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, fighter);
		scn.FreepsResolveSkirmish(smeagol);

		// Three cards: the Shadow has initiative.
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(smeagol));

		scn.MoveCardsToHand(troop);
		scn.FreepsRefreshDecision();
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsUseCardAction(smeagol);
		scn.FreepsChooseCards(guard, troop);

		// The initiative is checked before the cost: the hand falls to 2 and Sméagol is protected all the same.
		assertEquals(2, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertEquals(3, scn.GetOverwhelmMultiplier(smeagol));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheShadowHasInitiativeWhateverTheHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var bow = scn.GetFreepsCard("bow");
		var defiance = scn.GetFreepsCard("defiance");
		var troop = scn.GetFreepsCard("troop");
		var runner2 = scn.GetFreepsCard("runner2");
		var fighter1 = scn.GetShadowCard("fighter1");
		var fighter2 = scn.GetShadowCard("fighter2");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.MoveCardsToHand(guard, bow, defiance, troop, runner2);
		scn.MoveMinionsToTable(fighter1, fighter2);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{smeagol, fighter1}, new PhysicalCardImpl[]{frodo, fighter2});
		scn.FreepsResolveSkirmish(smeagol);

		// Five cards in hand, but a threat and another [sauron] Orc give each Mordor Fighter's initiative to the Shadow.
		assertEquals(1, scn.GetThreats());
		assertEquals(5, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(smeagol));

		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsActionAvailable(smeagol));
	}

	@Test
	public void ProtectedSmeagolIsOverwhelmedAtExactlyTripleHisStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var guard = scn.GetFreepsCard("guard");
		var bow = scn.GetFreepsCard("bow");
		var defiance = scn.GetFreepsCard("defiance");
		var troop = scn.GetFreepsCard("troop");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.MoveCardsToHand(guard, bow, defiance, troop);
		scn.MoveMinionsToTable(party);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, party);
		scn.FreepsResolveSkirmish(smeagol);

		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(smeagol));
		scn.FreepsUseCardAction(smeagol);
		scn.FreepsChooseCards(guard, bow);
		assertEquals(3, scn.GetOverwhelmMultiplier(smeagol));

		scn.PassCurrentPhaseActions();

		// Strength 9 is exactly three times 3: overwhelmed, though one wound and damage +1 would not have killed him.
		assertInZone(Zone.DEAD, smeagol);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void UsedInAnotherSkirmishTheProtectionIsGoneByHisOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var bow = scn.GetFreepsCard("bow");
		var defiance = scn.GetFreepsCard("defiance");
		var troop = scn.GetFreepsCard("troop");
		var runner2 = scn.GetFreepsCard("runner2");
		var runner = scn.GetShadowCard("runner");
		var fighter = scn.GetShadowCard("fighter1");
		scn.MoveCompanionsToTable(smeagol, aragorn);

		scn.StartGame();
		scn.MoveCardsToHand(guard, bow, defiance, troop, runner2);
		scn.MoveMinionsToTable(runner, fighter);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{smeagol, fighter});
		scn.FreepsResolveSkirmish(aragorn);

		// Sméagol is waiting for his own skirmish; the text names no skirmish, so it is offered here.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertFalse(scn.IsCharSkirmishing(smeagol));
		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsUseCardAction(smeagol);
		scn.FreepsChooseCards(guard, bow);
		assertEquals(3, scn.GetOverwhelmMultiplier(smeagol));

		scn.PassCurrentPhaseActions();
		assertInDiscard(runner);
		scn.FreepsResolveSkirmish(smeagol);

		// Each skirmish is a phase of its own: in his skirmish the multiplier is back to 2, and 7 overwhelms 3.
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, fighter));
		assertEquals(2, scn.GetOverwhelmMultiplier(smeagol));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		scn.PassCurrentPhaseActions();

		assertInZone(Zone.DEAD, smeagol);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
