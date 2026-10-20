package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_042_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("walks", "101_42");

					// Ulaire Attea, The Easterling: a twilight Nazgul, strength 12, vitality 3.  "When you play
					// Ulaire Attea, you may wound an ally"; he is never played here.
					put("attea", "2_82");
					// Ulaire Nelya, Ringwraith in Twilight: a twilight Nazgul, strength 10, vitality 3.  Fierce
					// and damage +1 while there are 2 burdens or 2 wounds on the Ring-bearer; there never are.
					put("nelya", "2_84");
					// Ulaire Lemenya, Lieutenant of Morgul: a Nazgul that is not twilight, strength 9, vitality
					// 2.  His text weakens companions bearing a [ringwraith] condition; nobody bears one.
					put("lemenya", "1_232");
					// Corpse Lights: a twilight minion that is not a Nazgul ([sauron] Wraith), strength 7,
					// vitality 2.  A skirmish ability, never used here.
					put("lights", "6_99");

					// [ringwraith] cards of four types for the discard pile.
					put("gates", "1_217");      // Morgul Gates: event
					put("breath", "1_207");     // Black Breath: condition
					put("steed", "1_208");      // Black Steed: possession
					put("ferny", "2_75");       // Bill Ferny, Swarthy Sneering Fellow: a minion, and a Man, not a Nazgul
					// Goblin Runner: a [moria] minion, for a Shadow card in the discard pile that is not [ringwraith]
					put("runner", "1_178");
					// Nazgul Sword: [ringwraith] possession.  The Free Peoples player's copy is in his discard
					// pile and the Shadow player's stays in the draw deck.
					put("sword", "1_218");

					// Aragorn, Ranger of the North: strength 8, vitality 4.  A maneuver ability, never used here.
					put("aragorn", "1_89");
					// Boromir, Son of Denethor: strength 7, vitality 3.  A skirmish ability, never used here.
					put("boromir", "1_97");
					// Gimli, Son of Gloin: strength 6, vitality 3; damage +1 and a skirmish ability, and he only loses here.
					put("gimli", "1_13");
					// Dwarf Guard: strength 4, vitality 2, no text but what it takes to play him.
					put("guard", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WalksInTwilightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Walks in Twilight
		 * Unique: True
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time a twilight Nazgul wins a skirmish, you may exert a twilight Nazgul to shuffle a
		 * 	[ringwraith] card from your discard pile into your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("walks");

		assertEquals("Walks in Twilight", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ATwilightNazgulWinningLetsAnyTwilightNazgulExertToShuffleARingwraithCardFromDiscardIntoTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var freepsSword = scn.GetFreepsCard("sword");
		scn.MoveCardsToDiscard(freepsSword);

		var walks = scn.GetShadowCard("walks");
		var attea = scn.GetShadowCard("attea");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		var lights = scn.GetShadowCard("lights");
		var gates = scn.GetShadowCard("gates");
		var breath = scn.GetShadowCard("breath");
		var steed = scn.GetShadowCard("steed");
		var ferny = scn.GetShadowCard("ferny");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(walks);
		scn.MoveCardsToDiscard(gates, breath, steed, ferny, runner);

		scn.StartGame();
		scn.MoveMinionsToTable(attea, nelya, lemenya, lights);
		scn.SkipToAssignments();
		// Attea 12 against Aragorn 8.  Nelya, Lemenya and Corpse Lights fight nobody.
		scn.FreepsAssignAndResolve(aragorn, attea);
		scn.PassSkirmishActions();

		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(walks));
		assertEquals(0, scn.GetWoundsOn(attea));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(5, scn.GetShadowDiscardCount());
		// Aragorn, Boromir, Gimli, the Dwarf Guard and Nazgul Sword
		assertEquals(5, scn.GetShadowDeckCount());
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.ShadowAcceptOptionalTrigger();

		// Any twilight Nazgul may be the one to exert, not only the one that won.
		assertTrue(scn.ShadowHasCardChoiceAvailable(attea, nelya));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lemenya, lights));
		scn.ShadowChooseCard(nelya);
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(attea));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(lights));

		// A [ringwraith] card of any type; not a [moria] card, and not a [ringwraith] card in the other
		// player's discard pile or in the Shadow player's own draw deck.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gates, breath, steed, ferny));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, freepsSword));
		// Having paid, the Shadow player shuffles one card in: the text does not say "up to".
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCardBPFromSelection(breath);

		assertInZone(Zone.DECK, breath);
		assertInZone(Zone.DISCARD, gates, steed, ferny, runner, freepsSword);
		assertEquals(4, scn.GetShadowDiscardCount());
		assertEquals(6, scn.GetShadowDeckCount());
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertEquals(1, scn.GetFreepsDiscardCount());

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnlyATwilightNazgulWinningMakesTheOfferAndDecliningChangesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, guard);
		// Boromir at 7 + 4 = 11 beats Nelya's 10 without overwhelming her.
		scn.ApplyAdHocModifier(new StrengthModifier(null, boromir, null, 4));

		var walks = scn.GetShadowCard("walks");
		var attea = scn.GetShadowCard("attea");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		var lights = scn.GetShadowCard("lights");
		var gates = scn.GetShadowCard("gates");
		var breath = scn.GetShadowCard("breath");
		scn.MoveCardsToSupportArea(walks);
		scn.MoveCardsToDiscard(gates, breath);

		scn.StartGame();
		scn.MoveMinionsToTable(attea, nelya, lemenya, lights);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{boromir, nelya},
				new PhysicalCardImpl[]{gimli, lemenya},
				new PhysicalCardImpl[]{guard, lights},
				new PhysicalCardImpl[]{aragorn, attea});
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		// A twilight Nazgul loses: Nelya 10 against Boromir 11.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(11, scn.GetStrength(boromir));
		assertEquals(10, scn.GetStrength(nelya));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// A Nazgul that is not twilight wins: Lemenya 9 against Gimli 6.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// A twilight minion that is not a Nazgul wins: Corpse Lights 7 against the Dwarf Guard 4.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(lights));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// A twilight Nazgul wins: Attea 12 against Aragorn 8.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(walks));

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(attea));
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertInZone(Zone.DISCARD, gates, breath);
		assertEquals(2, scn.GetShadowDiscardCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenEveryTwilightNazgulIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var walks = scn.GetShadowCard("walks");
		var attea = scn.GetShadowCard("attea");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		var lights = scn.GetShadowCard("lights");
		var gates = scn.GetShadowCard("gates");
		var breath = scn.GetShadowCard("breath");
		scn.MoveCardsToSupportArea(walks);
		scn.MoveCardsToDiscard(gates, breath);

		scn.StartGame();
		scn.MoveMinionsToTable(attea, nelya, lemenya, lights);
		// Both twilight Nazgul have vitality 3.  Lemenya and Corpse Lights could exert, and are not twilight Nazgul.
		scn.AddWoundsToChar(attea, 2);
		scn.AddWoundsToChar(nelya, 2);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, attea);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(attea));
		assertEquals(2, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(lights));
		assertInZone(Zone.DISCARD, gates, breath);
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsOfferedAgainWhenATwilightNazgulWinsALaterSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var walks = scn.GetShadowCard("walks");
		var attea = scn.GetShadowCard("attea");
		var nelya = scn.GetShadowCard("nelya");
		var gates = scn.GetShadowCard("gates");
		var breath = scn.GetShadowCard("breath");
		var steed = scn.GetShadowCard("steed");
		scn.MoveCardsToSupportArea(walks);
		scn.MoveCardsToDiscard(gates, breath, steed);

		scn.StartGame();
		scn.MoveMinionsToTable(attea, nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, attea},
				new PhysicalCardImpl[]{boromir, nelya});
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		// Attea 12 against Aragorn 8.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(walks));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(attea);
		assertEquals(1, scn.GetWoundsOn(attea));
		scn.ShadowChooseCardBPFromSelection(gates);
		assertInZone(Zone.DECK, gates);
		assertEquals(2, scn.GetShadowDiscardCount());
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());

		// Nelya 10 against Boromir 7.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(walks));
		scn.ShadowAcceptOptionalTrigger();
		// The same Nazgul may pay again.
		scn.ShadowChooseCard(attea);
		assertEquals(2, scn.GetWoundsOn(attea));
		assertEquals(0, scn.GetWoundsOn(nelya));
		scn.ShadowChooseCardBPFromSelection(steed);
		assertInZone(Zone.DECK, gates, steed);
		assertInZone(Zone.DISCARD, breath);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(shadowShuffles + 2, scn.GetShadowDeckShuffleCount());

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TwoTwilightNazgulWinningOneSkirmishMakeTwoOffers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// Aragorn at 8 + 4 = 12 loses to 12 + 10 = 22 without being overwhelmed.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 4));

		var walks = scn.GetShadowCard("walks");
		var attea = scn.GetShadowCard("attea");
		var nelya = scn.GetShadowCard("nelya");
		var gates = scn.GetShadowCard("gates");
		var breath = scn.GetShadowCard("breath");
		var steed = scn.GetShadowCard("steed");
		scn.MoveCardsToSupportArea(walks);
		scn.MoveCardsToDiscard(gates, breath, steed);

		scn.StartGame();
		scn.MoveMinionsToTable(attea, nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, attea);
		scn.ShadowAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(attea, aragorn));
		assertTrue(scn.IsCharSkirmishingAgainst(nelya, aragorn));
		assertEquals(12, scn.GetStrength(aragorn));

		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// One offer for each of the two winners.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(walks));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(attea);
		scn.ShadowChooseCardBPFromSelection(gates);
		assertEquals(1, scn.GetWoundsOn(attea));
		assertInZone(Zone.DECK, gates);
		assertEquals(2, scn.GetShadowDiscardCount());

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(walks));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(nelya);
		scn.ShadowChooseCardBPFromSelection(breath);
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertInZone(Zone.DECK, gates, breath);
		assertInZone(Zone.DISCARD, steed);
		assertEquals(1, scn.GetShadowDiscardCount());

		// And no third.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsStillOfferedWithNoRingwraithCardInTheDiscardPileAndThenMovesNoCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var freepsSword = scn.GetFreepsCard("sword");
		scn.MoveCardsToDiscard(freepsSword);

		var walks = scn.GetShadowCard("walks");
		var attea = scn.GetShadowCard("attea");
		var nelya = scn.GetShadowCard("nelya");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(walks);
		scn.MoveCardsToDiscard(runner);

		scn.StartGame();
		scn.MoveMinionsToTable(attea, nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, attea);
		scn.PassSkirmishActions();

		// Exerting is the whole cost; the shuffle is the effect, and an effect that can do nothing does not
		// stop the offer.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(walks));
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(11, scn.GetShadowDeckCount());

		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(attea);

		assertEquals(1, scn.GetWoundsOn(attea));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertInZone(Zone.DISCARD, runner, freepsSword);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(11, scn.GetShadowDeckCount());
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
