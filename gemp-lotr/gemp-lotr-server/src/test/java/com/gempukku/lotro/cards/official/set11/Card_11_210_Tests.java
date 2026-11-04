package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_210_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hatred", "11_210");
					// Úlairë Cantëa, Lieutenant of Dol Guldur: [wraith] Nazgûl, fierce, strength 10, vitality 3; his skirmish ability is used only on purpose
					put("cantea", "1_230");
					// Morgul Hound: a [wraith] Orc, strength 5 and +4 and fierce while 2 wounds are on the Ring-bearer
					put("hound", "7_192");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl, fierce, strength 12; his bonuses need 3 burdens
					put("khamul", "103_61");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf, strength 6, vitality 3; a skirmish ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, strength 4; skirmishes by his own maneuver ability
					put("lemenya", "1_232");    // Úlairë Lemenya: a [wraith] minion, for the discard pile
					put("ned", "1_220");        // Not Easily Destroyed: a [wraith] condition, for the discard pile
					put("soldier", "1_271");    // Orc Soldier: a [sauron] minion, the card in the discard pile that is not [wraith]
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFierceScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hatred", "11_210");
					// Black Rider: [wraith] Nazgul, fierce, strength 8 and +1 for each companion: 11 beside three
					put("rider1", "12_161");
					put("rider2", "12_161");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4: he loses twice and lives
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3: he loses twice (11 is not double 6) and lives
					put("lemenya", "1_232");    // Ulaire Lemenya: a [wraith] minion, for the discard pile
					put("breath", "1_207");     // Black Breath: a [wraith] condition, for the discard pile
					put("runner", "1_178");     // Goblin Runner: a [moria] minion, the card in the discard pile that is not [wraith]
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HatredStirredStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Hatred Stirred
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a companion loses a fierce skirmish involving a [wraith] minion, you may shuffle a [wraith] card from your discard pile into your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("hatred");

		assertEquals("Hatred Stirred", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	// F-0473.  Ketura on Q-0243 (2026-10-11), "Go for B": "you may shuffle a [wraith] card from your discard pile into your
	// draw deck" is not offered with no [wraith] card in that pile (Q-0034; as Q-0112 for Mumakil Commander).
	@Test
	public void TheShuffleIsNotOfferedWithNoWraithCardInTheDiscardPileAndWithOneShufflesTheCardChosenIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFierceScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var hatred = scn.GetShadowCard("hatred");
		var rider1 = scn.GetShadowCard("rider1");
		var rider2 = scn.GetShadowCard("rider2");
		var lemenya = scn.GetShadowCard("lemenya");
		var breath = scn.GetShadowCard("breath");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(hatred);
		scn.MoveCardsToDiscard(runner);
		assertInDiscard(runner);

		scn.StartGame();
		scn.MoveMinionsToTable(rider1, rider2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, rider1}, new PhysicalCardImpl[]{gimli, rider2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// The fierce skirmishes.  Aragorn (8) loses to a Black Rider (8 + 3 companions) with only the Goblin
		// Runner in the Shadow player's discard pile.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, rider1}, new PhysicalCardImpl[]{gimli, rider2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetShadowDiscardCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hatred));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Two [wraith] cards join the Runner there before Gimli (6) loses his.
		scn.MoveCardsToDiscard(lemenya, breath);
		assertInDiscard(lemenya, breath);
		int shuffles = scn.GetShadowDeckShuffleCount();
		scn.FreepsResolveSkirmish(gimli);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hatred));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, breath));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(breath);

		assertInZone(Zone.DECK, breath);
		assertInDiscard(lemenya, runner);
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedForNormalSkirmishesLostAndOfferedForEachFierceSkirmishLostToAWraithMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli);

		var hatred = scn.GetShadowCard("hatred");
		var cantea = scn.GetShadowCard("cantea");
		var hound = scn.GetShadowCard("hound");
		var lemenya = scn.GetShadowCard("lemenya");
		var ned = scn.GetShadowCard("ned");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(hatred);
		scn.MoveCardsToDiscard(lemenya, ned, soldier);

		scn.StartGame();
		// Two wounds on the Ring-bearer make the Hound 9 and fierce.
		scn.AddWoundsToChar(frodo, 2);
		scn.MoveMinionsToTable(cantea, hound);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertInDiscard(lemenya, ned, soldier);
		assertEquals(3, scn.GetShadowDiscardCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, cantea}, new PhysicalCardImpl[]{gimli, hound});
		// The normal skirmishes: Aragorn (8) loses to the Nazgûl (10), Gimli (6) to the Orc (9).  No offer for either.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hatred));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hatred));
		assertEquals(3, scn.GetShadowDiscardCount());

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, cantea}, new PhysicalCardImpl[]{gimli, hound});

		// Aragorn loses the fierce skirmish to the [wraith] Nazgûl: offered, and taken.
		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hatred));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, ned));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(soldier));
		scn.ShadowChooseCards(lemenya);
		assertInZone(Zone.DECK, lemenya);
		assertInDiscard(ned, soldier);
		assertEquals(deck + 1, scn.GetShadowDeckCount());
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Gimli loses his to the [wraith] Orc: offered again, and declined.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hatred));
		scn.ShadowDeclineOptionalTrigger();
		assertInDiscard(ned, soldier);
		assertEquals(2, scn.GetShadowDiscardCount());
		assertEquals(deck + 1, scn.GetShadowDeckCount());
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedForAFierceSkirmishLostToANazgulWhoIsNotWraithOrLostByAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(thrarin);

		var hatred = scn.GetShadowCard("hatred");
		var cantea = scn.GetShadowCard("cantea");
		var hound = scn.GetShadowCard("hound");
		var khamul = scn.GetShadowCard("khamul");
		var ned = scn.GetShadowCard("ned");
		scn.MoveCardsToSupportArea(hatred);
		scn.MoveCardsToDiscard(ned);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 2);
		scn.MoveMinionsToTable(cantea, hound, khamul);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertInDiscard(ned);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		// Nobody assigned in the normal round: the fierce round follows.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, khamul}, new PhysicalCardImpl[]{thrarin, cantea},
				new PhysicalCardImpl[]{gimli, hound});

		// Aragorn (8) loses to the [raider] Nazgûl (12): no offer.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hatred));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Thrarin, an ally (4), is overwhelmed by the [wraith] Nazgûl (10): no offer.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassFierceSkirmishActions();
		assertInZone(Zone.DEAD, thrarin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hatred));
		assertInDiscard(ned);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The control on the same board: Gimli (6) loses to the [wraith] Orc (9), and it is offered.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassFierceSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hatred));
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.DECK, ned);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
