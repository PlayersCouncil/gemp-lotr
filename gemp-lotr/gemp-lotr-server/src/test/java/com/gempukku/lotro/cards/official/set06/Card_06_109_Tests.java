package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_109_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("held", "6_109");
					put("held2", "6_109");
					put("sam", "1_311");        // Sam, Son of Hamfast: a companion who is no Frodo; his texts answer only Frodo's death
					// Goblin Marksman: [moria] archer, vitality 1; no other text. Three of them: a minion archery total of 3
					put("marksman1", "1_176");
					put("marksman2", "1_176");
					put("marksman3", "1_176");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text: overwhelms Frodo's 4
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability only
					put("nelya", "2_84");       // Úlairë Nelya: [wraith] Nazgûl; fierce and damage +1 only at 2 burdens
					put("few", "1_260");        // The Number Must Be Few: [sauron] support-area condition; nothing below 7 companions
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The card's own block: a set 6 card's "site 9" is the Towers site 9
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("held", "6_109");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli, Bearer of Grudges as the Ring-bearer, beside the one Frodo who can stand there (Q-0212), and an archer
	protected VirtualTableScenario GetFighterScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("held", "6_109");
					put("fighter", "13_149");   // Frodo, Frenzied Fighter: "This version of Frodo cannot become the Ring-bearer"
					put("marksman", "1_176");   // Goblin Marksman: [moria] archer, vitality 1; no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFrodoRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("held", "6_109");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli, Bearer of Grudges as the Ring-bearer, beside the one Frodo who can stand there (Q-0212)
	protected VirtualTableScenario GetOtherRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("held", "6_109");
					put("fighter", "13_149");   // Frodo, Frenzied Fighter: "This version of Frodo cannot become the Ring-bearer"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HeldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Held
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Bearer must be Frodo.<br>Each time Frodo is about to be killed by a wound, add a burden instead.<br>When the fellowship moves to site 9, Frodo is corrupted.<br><b>Regroup:</b> If you can spot no minions, discard this condition.
		*/

		var scn = GetScenario();

		var held = scn.GetFreepsCard("held");

		assertEquals("Held", held.getBlueprint().getTitle());
		assertNull(held.getBlueprint().getSubtitle());
		assertFalse(held.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, held.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, held.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, held.getBlueprint().getCardType());
		assertEquals(1, held.getBlueprint().getTwilightCost());
	}

	@Test
	public void BorneByAFrodoWhoIsTheRingBearerTheMoveToSite9CorruptsHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrodoRingBearerScenario();

		var frodo = scn.GetRingBearer();
		var held = scn.GetFreepsCard("held");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.AttachCardsTo(frodo, held);

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MarkGameLog();
		scn.FreepsPass();

		assertTrue(scn.GameIsFinished());
		assertTrue(scn.GameLogSinceMarkSays(scn.P1() + " lost due to: The Ring-Bearer is corrupted"));
		assertFalse(scn.GameLogSinceMarkSays(scn.P2() + " lost"));
	}

	@Test
	public void BorneByAFrodoWhoIsNotTheRingBearerTheMoveToSite9CorruptsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOtherRingBearerScenario();

		var held = scn.GetFreepsCard("held");
		var fighter = scn.GetFreepsCard("fighter");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(fighter);
		scn.AttachCardsTo(fighter, held);
		assertAttachedTo(held, fighter);

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MarkGameLog();
		scn.FreepsPass();

		// "Frodo is corrupted": the Frodo who bears this is no Ring-bearer, and the Ring-bearer is no Frodo.
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertFalse(scn.GameIsFinished());
		assertFalse(scn.GameLogSinceMarkSays("lost due to"));
		assertAttachedTo(held, fighter);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlaysFromHandOnFrodoTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var held = scn.GetFreepsCard("held");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(held);
		assertInHand(held);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(held));
		// Frodo is the one bearer there can be, so Sam beside him is never asked about.
		scn.FreepsPlayCard(held);

		assertAttachedTo(held, frodo);
		assertNotAttachedTo(held, sam);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotPlayableWithNoFrodoInPlayAndPlaysOnAFrodoWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFighterScenario();

		var gimli = scn.GetRingBearer();
		var held = scn.GetFreepsCard("held");
		var fighter = scn.GetFreepsCard("fighter");
		scn.MoveCardsToHand(held, fighter);
		assertInHand(held, fighter);

		scn.StartGame();

		// Gimli the Ring-bearer is no Frodo.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(held));

		scn.FreepsPlayCard(fighter);
		assertInZone(Zone.FREE_CHARACTERS, fighter);
		assertTrue(scn.FreepsPlayAvailable(held));

		scn.FreepsPlayCard(held);

		assertAttachedTo(held, fighter);
		assertNotAttachedTo(held, gimli);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachWoundThatWouldKillFrodoIsABurdenInsteadAndAWoundThatDoesNotIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var held = scn.GetFreepsCard("held");
		var sam = scn.GetFreepsCard("sam");
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		var marksman3 = scn.GetShadowCard("marksman3");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, held);
		scn.AddWoundsToChar(frodo, 2);

		scn.StartGame();
		assertAttachedTo(held, frodo);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());

		scn.MoveMinionsToTable(marksman1, marksman2, marksman3);
		// Three archers: the Free Peoples player places three wounds, each on Frodo.
		scn.SkipToArcheryWounds();

		// Vitality 4 with 2 wounds: this one does not kill him, and is taken.
		scn.FreepsChooseCard(frodo);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());

		// Exhausted: the next would kill him, and is a burden instead.
		scn.FreepsChooseCard(frodo);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());

		// And again.
		scn.FreepsChooseCard(frodo);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetBurdens());
		assertInZone(Zone.FREE_CHARACTERS, frodo);
		assertAttachedTo(held, frodo);
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TwoCopiesTurnAWoundThatWouldKillFrodoIntoOneBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var held = scn.GetFreepsCard("held");
		var held2 = scn.GetFreepsCard("held2");
		var sam = scn.GetFreepsCard("sam");
		var marksman1 = scn.GetShadowCard("marksman1");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, held, held2);
		scn.AddWoundsToChar(frodo, 3);

		scn.StartGame();
		assertAttachedTo(held, frodo);
		assertAttachedTo(held2, frodo);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());

		scn.MoveMinionsToTable(marksman1);
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(frodo);

		// Both copies answer the one wound; once the first has put a burden in its place, there is no kill left for
		// the second to replace.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveActionOrder("Held");

		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.FREE_CHARACTERS, frodo);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnotherCompanionKilledByAWoundIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var held = scn.GetFreepsCard("held");
		var sam = scn.GetFreepsCard("sam");
		var marksman1 = scn.GetShadowCard("marksman1");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, held);
		scn.AddWoundsToChar(sam, 3);

		scn.StartGame();
		assertAttachedTo(held, frodo);
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetBurdens());

		scn.MoveMinionsToTable(marksman1);
		scn.SkipToArcheryWounds();

		scn.FreepsChooseCard(sam);

		assertInZone(Zone.DEAD, sam);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AWoundThatWouldKillAFrodoWhoIsNotTheRingBearerIsABurdenInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFighterScenario();

		var gimli = scn.GetRingBearer();
		var held = scn.GetFreepsCard("held");
		var fighter = scn.GetFreepsCard("fighter");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(fighter);
		scn.AttachCardsTo(fighter, held);
		scn.AddWoundsToChar(fighter, 3);

		scn.StartGame();
		assertAttachedTo(held, fighter);
		assertEquals(3, scn.GetWoundsOn(fighter));
		assertEquals(0, scn.GetBurdens());

		scn.MoveMinionsToTable(marksman);
		scn.SkipToArcheryWounds();

		scn.FreepsChooseCard(fighter);

		// The burden goes on the Ring-bearer, as every burden does.
		assertEquals(3, scn.GetWoundsOn(fighter));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.FREE_CHARACTERS, fighter);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void FrodoOverwhelmedIsKilledSinceNoWoundKillsHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var held = scn.GetFreepsCard("held");
		var troop = scn.GetShadowCard("troop");
		scn.AttachCardsTo(frodo, held);
		scn.AddWoundsToChar(frodo, 3);

		scn.StartGame();
		assertAttachedTo(held, frodo);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, troop);
		scn.FreepsResolveSkirmish(frodo);
		// 13 against Frodo's 3 + 1 for the Ring: double, and he is killed with no wound.
		scn.PassSkirmishActions();

		assertTrue(scn.GameIsFinished());
		assertTrue(scn.FreepsHasLost());
		assertEquals(0, scn.GetBurdens());
	}

	@Test
	public void TheMoveToSite8DoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var frodo = scn.GetRingBearer();
		var held = scn.GetFreepsCard("held");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.AttachCardsTo(frodo, held);
		assertAttachedTo(held, frodo);
		assertEquals(7, scn.GetCurrentSiteNumber());

		scn.FreepsPass();

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertFalse(scn.GameIsFinished());
		assertAttachedTo(held, frodo);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnItsOwnBlocksPathTheMoveToSite9CorruptsFrodo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var frodo = scn.GetRingBearer();
		var held = scn.GetFreepsCard("held");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.AttachCardsTo(frodo, held);
		assertAttachedTo(held, frodo);
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MarkGameLog();
		scn.FreepsPass();

		assertTrue(scn.GameIsFinished());
		assertTrue(scn.GameLogSinceMarkSays(scn.P1() + " lost due to: The Ring-Bearer is corrupted"));
	}

	@Test
	public void RegroupAbilityDiscardsItWithNoMinionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var held = scn.GetFreepsCard("held");
		var held2 = scn.GetFreepsCard("held2");
		var few = scn.GetShadowCard("few");
		scn.AttachCardsTo(frodo, held, held2);
		scn.MoveCardsToSupportArea(few);

		scn.StartGame();
		assertAttachedTo(held, frodo);
		assertAttachedTo(held2, frodo);
		assertInZone(Zone.SUPPORT, few);

		scn.SkipToPhase(Phase.REGROUP);

		// A Shadow card in play that is no minion does not stop it.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(held));

		scn.FreepsUseCardAction(held);

		// Only the copy used is discarded.
		assertInDiscard(held);
		assertAttachedTo(held2, frodo);
		assertInZone(Zone.SUPPORT, few);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ASauronOrcInPlayStopsTheRegroupAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var held = scn.GetFreepsCard("held");
		var soldier = scn.GetShadowCard("soldier");
		scn.AttachCardsTo(frodo, held);

		scn.StartGame();
		assertAttachedTo(held, frodo);
		scn.MoveMinionsToTable(soldier);

		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertFalse(scn.FreepsActionAvailable(held));

		// With the Orc gone, no minion is left.
		scn.MoveCardsToDiscard(soldier);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(held));
	}

	@Test
	public void AWraithNazgulInPlayStopsTheRegroupAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var held = scn.GetFreepsCard("held");
		var nelya = scn.GetShadowCard("nelya");
		scn.AttachCardsTo(frodo, held);

		scn.StartGame();
		assertAttachedTo(held, frodo);
		scn.MoveMinionsToTable(nelya);

		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertFalse(scn.FreepsActionAvailable(held));

		scn.MoveCardsToDiscard(nelya);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(held));
	}
}
